readonly aws_region=eu-west-2
readonly green=$(tput setaf 2)
readonly red=$(tput setaf 1)
readonly yellow=$(tput setaf 3)
readonly normal=$(tput sgr0)
readonly icon_ld="⌛"
readonly icon_sc="✅"
readonly icon_er="ℹ️"

function run_sandbox() {
    WORKSPACE=$1
    ENVIRONMENT="dev"
    MODE=$2
    TF_FILE="./terraform_output.json"
    OSTYPE=$(uname)
    cd ./terraform
    if [ $MODE == --destroy ]; then
      find_workspace $WORKSPACE
      local TF_STATE=$?
      if [[ $TF_STATE -ne 0 ]]; then
        printf "\n${icon_er}${red} Unknown error, check awsume is authorised.\n\n${normal}" && exit $TF_STATE;
      fi
      printf "\n${icon_sc}${green} Workspace $WORKSPACE selected!\n\n${normal}"
      printf "\n${icon_ld}${yellow} Destroying resources for $WORKSPACE...\n\n${normal}"
      terraform destroy -var-file="dev.tfvars"
      printf "\n${icon_sc}${green} Finished destroy process for $WORKSPACE!\n\n${normal}"
    elif [ $MODE == --plan-app ]; then
      find_workspace $WORKSPACE --create
      local TF_STATE=$?
      if [[ $TF_STATE -ne 0 ]]; then
        printf "\n${icon_ld}${red} Unknown error, check awsume is authorised.\n\n${normal}" && exit $TF_STATE;
      fi
      exit 0;
      printf "\n${icon_ld}${yellow} Building lambda layers...\n\n${normal}"

      touch ./.dynamo_env
      echo "WORKSPACE=$WORKSPACE" > .dynamo_env
      cd ..
      ./gradlew app:build
      local LAMBDA_STATE=$(build_lambdas)
      if [[ $LAMBDA_STATE -ne 0 ]]; then
       printf "\n${icon_er}${red} Error building lambdas, ensure you are in root directory and gradle is installed.\n\n${normal}" && exit $LAMBDA_STATE;
      fi
      ./gradlew app:buildZip
      printf "\n${icon_ld}${yellow} Syncing local plan with remote plan...\n\n${normal}"
      cd ./terraform
      refresh_plan_and_output
    elif [ $MODE == --deploy-app ]; then
      if [ -f ".$TF_FILE" ]; then
        find_workspace $WORKSPACE
        local TF_STATE=$?
        if [[ $TF_STATE -ne 0 ]]; then
          printf "\n${icon_er}${red} Unknown error, check aws is authorised.\n\n${normal}" && exit $TF_STATE;
        fi
        printf "\n${icon_sc}${green} Terraform environment ready!\n   Run ${normal}$ make deploy-ui-$WORKSPACE${green}\n   to deploy the ui to amplify.\n\n${normal}"
      else
        printf "\n${icon_er}${red} Terraform config not found.\n   Please run ${normal}$ plan-app-$WORKSPACE${red}\n   for the first time.\n\n${normal}"
      fi
    elif [ $MODE == --deploy-ui ]; then
      if [ -f ".$TF_FILE" ]; then
        local TF_STATE=$(find_workspace $WORKSPACE)
        if [[ $TF_STATE -ne 0 ]]; then
          printf "\n${icon_er}${red} Unknown error, check aws is authorised.\n\n${normal}" && exit $TF_STATE;
        fi
        printf "\n${icon_sc}${green} Workspace $WORKSPACE selected!\n\n${normal}"
        cd ..
        if [ "$OSTYPE" == "Darwin"* ]; then
          create_sandbox_config $TF_FILE --osx
        else
          create_sandbox_config $TF_FILE --linux
        fi
        printf "\n${icon_ld}${yellow} Building UI for the $ENVIRONMENT environment...\n\n${normal}"
        REACT_APP_ENV=${ENVIRONMENT} npm --prefix ./ui run build
        cd ui/build
        zip -r ../../ui.zip *
        cd ../..
        printf "\n${icon_ld}${yellow} Deploying UI...\n\n${normal}"
        deploy_sandbox_ui $TF_FILE ./ui.zip $WORKSPACE
        printf "\n${icon_sc}${green} Finished deployment process.\n\n${normal}"
      else
        printf "\n${icon_er}${red} Terraform config not found.\n   Please run ${normal}$ plan-app-$WORKSPACE${red}\n   for the first time.\n\n${normal}"
      fi
    fi
    exit 0;
}

function find_workspace(){
  MODE=$2
  WORKSPACE=$1
  LOCK_FILE="./.terraform.lock.hcl"
  printf "\n${icon_ld}${yellow} Checking Terraform...\n\n${normal}"
  if [ ! -f $LOCK_FILE ]; then
    printf "\n${icon_sc}${green} Terraform not found, initialising Terraform for the first time.\n\n${normal}"
    terraform init || return 1;
  else
    printf "\n${icon_sc}${green} Successfully found Terraform!\n\n${normal}"
  fi
  terraform refresh -var-file="dev.tfvars"
  printf "\n${icon_ld}${yellow} Terraform state initialised, selecting workspace...\n\n${normal}"
  if [ $MODE == --create ]; then
    (terraform workspace select -or-create $WORKSPACE || return 1);
  else 
    (terraform workspace select $WORKSPACE || return 1);
  fi
  return 0;
}

function refresh_plan_and_output() {
  terraform refresh -var-file="dev.tfvars"
  terraform plan -var-file="dev.tfvars" -out tfplan 2>&1 | tee tfplan.log
  terraform output -json >"../terraform_output.json"
  printf "\n${icon_sc}${green} Terraform plan created!\n   Plan output has been logged to ${normal}tfplan.log${green}\n   check the output then run ${normal}$ make deploy-app-${WORKSPACE} to continue deployment.${green}.\n\n${normal}"
}

function build_lambdas() {
    (./gradlew lambdas:CreateDocumentManifestByNhsNumber:build &&
    ./gradlew lambdas:CreateDocumentReference:build &&
    ./gradlew lambdas:DeleteDocumentReference:build &&
    ./gradlew lambdas:DocumentReferenceSearch:build &&
    ./gradlew lambdas:FakeVirusScannedEvent:build &&
    ./gradlew lambdas:ReRegistrationEvent:build &&
    ./gradlew lambdas:SearchPatientDetails:build &&
    ./gradlew lambdas:VirusScannedEvent:build) || return 1;
}

function create_sandbox_config() {
  MODE=$2
  cp ui/src/config.js.example ui/src/config.js
  user_pool="$(jq -r '.cognito_user_pool_ids.value' "$1")"
  user_pool_client_id="$(jq -r '.cognito_client_ids.value' "$1")"
  api_endpoint="$(jq -r '.api_gateway_url.value' "$1")"
  cognito_domain="$(jq -r '.cognito_user_pool_domain.value' "$1")"
  amplify_app_id="$(jq -r '.amplify_app_ids.value[0]' "$1")"

  printf "\n${icon_ld}${yellow} Using $OSTYPE config...\n\n${normal}"
  if [ $MODE == --osx ]; then
    sed -i "" "s/%pool-id%/${user_pool}/" ui/src/config.js
    sed -i "" "s/%client-id%/${user_pool_client_id}/" ui/src/config.js
    sed -i "" "s/%region%/${aws_region}/" ui/src/config.js
    sed -i "" "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
    sed -i "" "s/%cognito-domain%/${cognito_domain}/" ui/src/config.js
    sed -i "" "s/%amplify-app-id%/${amplify_app_id}/" ui/src/config.js
    sed -i "" "s/%oidc-provider-id%/$OIDC_PROVIDER_ID/" ui/src/config.js
  elif [ $MODE == --linux ]; then
    sed -i "s/%pool-id%/${user_pool}/" ui/src/config.js
    sed -i "s/%client-id%/${user_pool_client_id}/" ui/src/config.js
    sed -i "s/%region%/${aws_region}/" ui/src/config.js
    sed -i "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
    sed -i "s/%cognito-domain%/${cognito_domain}/" ui/src/config.js
    sed -i "s/%amplify-app-id%/${amplify_app_id}/" ui/src/config.js
    sed -i "s/%oidc-provider-id%/$OIDC_PROVIDER_ID/" ui/src/config.js
  fi
  cat ui/src/config.js
}

function deploy_sandbox_ui() {
  app_id="$(jq -r '.amplify_app_ids.value[0]' "$1")"
  echo $app_id
  aws amplify create-deployment --region "${aws_region}" --app-id "$app_id" --branch-name $3 >deployment.output
  jobId="$(jq -r .jobId deployment.output)"
  zipUploadUrl="$(jq -r .zipUploadUrl deployment.output)"

  curl -XPUT --data-binary "@$2" "$zipUploadUrl"
  aws amplify start-deployment --region "${aws_region}" --app-id "$app_id" --branch-name $3 --job-id "$jobId"
}

readonly command="$1"
case "${command}" in
deploy-ui-sanda)
  run_sandbox "sanda" --deploy-ui
  ;;
deploy-ui-sandb)
  run_sandbox "sandb" --deploy-ui
  ;;
destroy-sanda)
  run_sandbox "sanda" --destroy
  ;;
destroy-sandb)
  run_sandbox "sandb" --destroy
  ;;
deploy-app-sanda)
  run_sandbox "sanda" --deploy-app
  ;;
deploy-app-sandb)
  run_sandbox "sandb" --deploy-app
  ;;
plan-app-sanda)
  run_sandbox "sanda" --plan-app
  ;;
plan-app-sandb)
  run_sandbox "sandb" --plan-app
  ;;
*)
  echo "make $@"
  make "$@"
  ;;
esac
