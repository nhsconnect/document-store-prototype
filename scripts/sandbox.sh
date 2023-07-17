set -e

readonly aws_region=eu-west-2
readonly i_ld="⌛"
readonly i_sc="✅"
readonly i_er="❌"
readonly green="${i_sc}$(tput setaf 2)"
readonly red="${i_er}$(tput setaf 1)"
readonly yellow="${i_ld}$(tput setaf 3)"
readonly normal=$(tput sgr0)
readonly os_type=$(uname)

function run_sandbox() {
    WORKSPACE=$1
    ENVIRONMENT="dev"
    MODE=$2
    TF_FILE="./terraform_output.json"
    cd ./terraform
    if [ "$MODE" == "--destroy" ]; then
      find_workspace $WORKSPACE
      local TF_STATE=$?
      if [[ $TF_STATE -ne 0 ]]; then
         exit $TF_STATE
      fi
      printf "\n${yellow} Destroying resources for $WORKSPACE...\n\n${normal}"
      terraform destroy -var-file="dev.tfvars"
      printf "\n${green} Finished destroy process for $WORKSPACE!\n\n${normal}"
    elif [ "$MODE" == "--plan-app" ]; then
      find_workspace $WORKSPACE --or-create
      local TF_STATE=$?
      if [[ $TF_STATE -ne 0 ]]; then
         exit $TF_STATE
      fi
      printf "\n${yellow} Building lambda layers...\n\n${normal}"
      cd ..
      ./gradlew app:build || (printf "\n${yellow} Formatting code...\n\n${normal}" && ./gradlew app:spotlessApply)
      ./gradlew authoriser:build && ./gradlew authoriser:spotlessApply
      build_lambdas
      local LAMBDA_STATE=$?
      echo $LAMBDA_STATE
      if [[ $LAMBDA_STATE -ne 0 ]]; then
        exit $LAMBDA_STATE;
      fi
      ./gradlew app:buildZip
      printf "\n${yellow} Creating Terraform plan...\n\n${normal}"
      cd ./terraform
      terraform plan -var workspace_is_a_sandbox=true -var-file="dev.tfvars" -out tfplan 2>&1 | tee tfplan.log
      terraform output -json >"../terraform_output.json"
      printf "\n${green} Terraform plan created!\n   Plan output has been logged to ${normal}tfplan.log$(tput setaf 2)\n   check the output then run ${normal}$ make deploy-app-${WORKSPACE} $(tput setaf 2)to continue deployment.${normal}.\n\n${normal}"
    elif [ $MODE == --deploy-app ]; then
      find_workspace $WORKSPACE --or-create
      local TF_STATE=$?
      if [[ $TF_STATE -ne 0 ]]; then
          exit $TF_STATE;
      fi
      terraform apply tfplan || (printf "\n${red} Terraform config not found.\n   Please run ${normal}$ plan-app-$WORKSPACE$(tput setaf 1)\n   for the first time.\n\n${normal}" && exit 1);
      printf "\n${green} Terraform plan successfully deployed to AWS!\n   Run ${normal}$ make deploy-ui-$WORKSPACE$(tput setaf 2)\n   to deploy the ui to amplify.\n\n${normal}"
    elif [ $MODE == --deploy-ui ]; then
      if [ -f ".$TF_FILE" ]; then
        find_workspace $WORKSPACE
        local TF_STATE=$?
        if [[ $TF_STATE -ne 0 ]]; then 
          exit $TF_STATE
        fi
        cd ..
        if [[ "$os_type" == "Darwin"* ]]; then
          create_sandbox_config $TF_FILE --osx
        else
          create_sandbox_config $TF_FILE --linux
        fi
        printf "\n${yellow} Building UI for the $ENVIRONMENT environment...\n\n${normal}"
        REACT_APP_ENV=${ENVIRONMENT} npm --prefix ./ui run build
        cd ui/build
        zip -r ../../ui.zip *
        cd ../..
        printf "\n${yellow} Deploying UI...\n\n${normal}"
        deploy_sandbox_ui $TF_FILE ./ui.zip $WORKSPACE
        printf "\n${green} Finished deployment process!\n\n${normal}"
      else
        printf "\n${red} Terraform config not found.\n   Please run ${normal}$ plan-app-$WORKSPACE$(tput setaf 1)\n   for the first time.\n\n${normal}"
      fi
    fi
    exit 0;
}
    
function find_workspace(){
  MODE=$2
  export WORKSPACE=$1
  printf "\n${yellow} Initialising local environment...\n\n${normal}"
  terraform init
  printf "\n${yellow} Finding workspace...\n\n${normal}"
  if [ "$MODE" == "--or-create" ]; then
    terraform workspace select -or-create $WORKSPACE || (printf "\n${red} Unknown error, check AWS is authorised.\n\n${normal}" && return 1)
  else 
    terraform workspace select $WORKSPACE || (printf "\n${red} Unknown error, check AWS is authorised.\n\n${normal}" && return 1)
  fi
  printf "\n${green} Workspace $WORKSPACE selected!\n\n${normal}"
  printf "\n${yellow} Syncing Terraform config...\n\n${normal}"
  terraform refresh -var-file="dev.tfvars"
}

function build_lambdas() {
    (./gradlew lambdas:CreateDocumentManifestByNhsNumber:build &&
    ./gradlew lambdas:CreateDocumentReference:build &&
    ./gradlew lambdas:DeleteDocumentReference:build &&
    ./gradlew lambdas:DocumentReferenceSearch:build &&
    ./gradlew lambdas:FakeVirusScannedEvent:build &&
    ./gradlew lambdas:ReRegistrationEvent:build &&
    ./gradlew lambdas:SearchPatientDetails:build &&
    ./gradlew lambdas:VirusScannedEvent:build) || (printf "\n${red} Unknown error building lambdas.\n\n${normal}" && return 1);
}

function create_sandbox_config() {
  printf "\n${yellow} Creating OS - ${os_type} config...\n\n${normal}"
  TF_FILE=$1
  MODE=$2
  cp ui/src/config.js.example ui/src/config.js
  user_pool="$(jq -r '.cognito_user_pool_ids.value' "$TF_FILE")"
  user_pool_client_id="$(jq -r '.cognito_client_ids.value' "$TF_FILE")"
  api_endpoint="$(jq -r '.api_gateway_url.value' "$TF_FILE")"
  cognito_domain="$(jq -r '.cognito_user_pool_domain.value' "$TF_FILE")"
  amplify_app_id="$(jq -r '.amplify_app_ids.value[0]' "$TF_FILE")"
  redirect_signin="https://${WORKSPACE}.access-request-fulfilment.patient-deductions.nhs.uk"
  redirect_signout="https://${WORKSPACE}.access-request-fulfilment.patient-deductions.nhs.uk"
  if [ $MODE == --osx ]; then
    sed -i "" "s/%pool-id%/${user_pool}/" ui/src/config.js
    sed -i "" "s/%client-id%/${user_pool_client_id}/" ui/src/config.js
    sed -i "" "s/%region%/${aws_region}/" ui/src/config.js
    sed -i "" "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
    sed -i "" "s/%cognito-domain%/${cognito_domain}/" ui/src/config.js
    sed -i "" "s/%amplify-app-id%/${amplify_app_id}/" ui/src/config.js
    sed -i "" "s/%oidc-provider-id%/$OIDC_PROVIDER_ID/" ui/src/config.js
    sed -i "" "s~%cognito-redirect-signin%~${redirect_signin}~" ui/src/config.js
    sed -i "" "s~%cognito-redirect-signout%~${redirect_signout}~" ui/src/config.js
  elif [ $MODE == --linux ]; then
    sed -i "s/%pool-id%/${user_pool}/" ui/src/config.js
    sed -i "s/%client-id%/${user_pool_client_id}/" ui/src/config.js
    sed -i "s/%region%/${aws_region}/" ui/src/config.js
    sed -i "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
    sed -i "s/%cognito-domain%/${cognito_domain}/" ui/src/config.js
    sed -i "s/%amplify-app-id%/${amplify_app_id}/" ui/src/config.js
    sed -i "s/%oidc-provider-id%/${OIDC_PROVIDER_ID}/" ui/src/config.js
    sed -i "s~%cognito-redirect-signin%~${redirect_signin}~" ui/src/config.js
    sed -i "s~%cognito-redirect-signout%~${redirect_signout}~" ui/src/config.js

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
  # shellcheck disable=SC2145
  echo "make $@"
  make "$@"
  ;;
esac
