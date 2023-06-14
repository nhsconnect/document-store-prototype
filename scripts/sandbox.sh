set -e

readonly aws_region=eu-west-2
readonly green=$(tput setaf 2)
readonly red=$(tput setaf 1)
readonly yellow=$(tput setaf 3)
readonly normal=$(tput sgr0)
    
function find_workspace(){
  MODE=$2
  WORKSPACE=$1
  printf "\n⌛${yellow} Finding local workspace...\n\n${normal}"
  terraform workspace select $WORKSPACE || (printf "\n⌛${yellow} $WORKSPACE not found, initialising local state...\n\n${normal}" && terraform init)
  printf "\n⌛${yellow} Terraform state initialised, selecting workspace...\n\n${normal}"
  if [ $MODE == --create ]; then
    terraform workspace select $WORKSPACE || (printf "\n⌛${yellow} Remote state not found for $WORKSPACE,\n creating workspace...\n\n${normal}" && terraform workspace select -or-create $WORKSPACE) || (printf "\nℹ️${red} Unknown error, check aws is authorised.\n\n${normal}" && return 1)
  else 
    terraform workspace select $WORKSPACE  || (printf "\nℹ️${red} Unknown error, check aws is authorised.\n\n${normal}" && return 1)
  fi
}

function run_sandbox() {
    WORKSPACE=$1
    ENVIRONMENT="dev"
    MODE=$2
    TF_FILE="./terraform_output.json"
    OSTYPE=$(uname)
    cd ./terraform
    if [ $MODE == --destroy ]; then
      printf "\n⌛${yellow} Finding local workspace...\n\n${normal}"
      terraform workspace select $WORKSPACE
      printf "\n✅${green} Workspace $WORKSPACE selected!\n\n${normal}"
      printf "\n⌛${yellow} Destroying resources for $WORKSPACE...\n\n${normal}"
      terraform destroy -var-file="dev.tfvars"
      printf "\n✅${green} Finished destroy process for $WORKSPACE!\n\n${normal}"
    elif [ $MODE == --deploy-app ]; then
      find_workspace $WORKSPACE --create
      local TF_STATE=$?
      if [[ $TF_STATE -ne 0 ]]; then
        exit 1;
      fi
      printf "\n✅${green} Workspace $WORKSPACE selected!\n\n${normal}"
      printf "\n⌛${yellow} Building lambda layers...\n\n${normal}"
      touch ./.dynamo_env
      echo "WORKSPACE=$WORKSPACE" > .dynamo_env
      cd ..
      ./gradlew app:build
      build_lambdas
      local LAMBDA_STATE=$?
      echo $LAMBDA_STATE
      if [[ $LAMBDA_STATE -ne 0 ]]; then
        exit 1;
      fi
      ./gradlew app:buildZip
      printf "\n⌛${yellow} Refreshing local plan...\n\n${normal}"
      cd ./terraform
      terraform refresh -var-file="dev.tfvars"
      terraform plan -var-file="dev.tfvars" -out tfplan
      terraform output -json >"../terraform_output.json"
      terraform apply tfplan
      printf "\n✅${green} Terraform environment ready!\n   Run ${normal}$ make deploy-ui-$WORKSPACE${green}\n   to deploy the ui to amplify.\n\n${normal}"
    elif [ $MODE == --deploy-ui ]; then
      if [ -f ".$TF_FILE" ]; then
        local TF_STATE=$(find_workspace $WORKSPACE --create)
        if [ $TF_STATE -ne 0 ]; then 
          exit $TF_STATE
        fi
        printf "\n✅${green} Workspace $WORKSPACE selected!\n\n${normal}"
        cd ..
        printf "\n⌛${yellow} Using $OSTYPE config...\n\n${normal}"
        if [[ "$OSTYPE" == "Darwin"* ]]; then
          create_sandbox_config $TF_FILE --osx
        else
          create_sandbox_config $TF_FILE --linux
        fi
        printf "\n⌛${yellow} Building UI for the $ENVIRONMENT environment...\n\n${normal}"
        REACT_APP_ENV=${ENVIRONMENT} npm --prefix ./ui run build
        cd ui/build
        zip -r ../../ui.zip *
        cd ../..
        printf "\n⌛${yellow} Deploying UI...\n\n${normal}"
        deploy_sandbox_ui $TF_FILE ./ui.zip $WORKSPACE
        printf "\n✅${green} Finished deployment process.\n\n${normal}"
      else
        printf "\nℹ️${red} Terraform config not found.\n   Please run ${normal}$ deploy-app-$WORKSPACE${red}\n   for the first time.\n\n${normal}"
      fi
    fi
    exit 0;
}

function build_lambdas() {
    (./gradlew lambdas:CreateDocumentManifestByNhsNumber:build &&
    ./gradlew lambdas:CreateDocumentReference:build &&
    ./gradlew lambdas:DeleteDocumentReference:build &&
    ./gradlew lambdas:DocumentReferenceSearch:build &&
    ./gradlew lambdas:FakeVirusScannedEvent:build &&
    ./gradlew lambdas:ReRegistrationEvent:build &&
    ./gradlew lambdas:SearchPatientDetails:build &&
    ./gradlew lambdas:VirusScannedEvent:build) || (printf "\nℹ️${red} Error building lambdas, ensure you are in root directory and gradle is installed.\n\n${normal}" && return 1);
}

function create_sandbox_config() {
  MODE=$2
  cp ui/src/config.js.example ui/src/config.js
  user_pool="$(jq -r '.cognito_user_pool_ids.value' "$1")"
  user_pool_client_id="$(jq -r '.cognito_client_ids.value' "$1")"
  api_endpoint="$(jq -r '.api_gateway_url.value' "$1")"
  cognito_domain="$(jq -r '.cognito_user_pool_domain.value' "$1")"
  amplify_app_id="$(jq -r '.amplify_app_ids.value[0]' "$1")"
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
*)
  echo "make $@"
  make "$@"
  ;;
esac

# // Plan and apply seperate steps
# // DNS Changes
# // Dynamo Table names