blue=$(tput setaf 4)
green=$(tput setaf 2)
red=$(tput setaf 1)
yellow=$(tput setaf 3)
normal=$(tput sgr0)
function run_sandbox() {
    WORKSPACE=$1
    ENVIRONMENT="dev"
    MODE=$2
    TF_FILE="./terraform_output.json"
    OS_TYPE=$(uname)
    cd ./terraform
    printf "${blue}\nFinding workspace...\n\n${normal}"
    if [ $MODE == --destroy ]; then
      terraform workspace select ${WORKSPACE} || (printf "${red}\n${WORKSPACE} not found, exiting...\n\n${normal}" && exit 1)
      printf "${green}\n${WORKSPACE} selected!\n\n${normal}"
      terraform destroy -var-file="dev.tfvars"
      printf "${blue}\nFinished destroy process for ${WORKSPACE}.\n\n${normal}"
    elif [ $MODE == --deploy-app ]; then
      terraform workspace select ${WORKSPACE} || (printf "${yellow}\n${WORKSPACE} not found, initialising local state...\n\n${normal}" && terraform init && terraform workspace select -or-create ${WORKSPACE})
      printf "${green}\n${WORKSPACE} selected!\n\n${normal}"
      printf "${blue}\nBuilding lambda layers...\n\n${normal}"
      cd ..
      ./gradlew app:build
      build_lambdas
      ./gradlew app:buildZip
      printf "${blue}\nRefreshing local plan...\n\n${normal}"
      cd ./terraform
      terraform refresh -var-file="dev.tfvars"
      terraform plan -var-file="dev.tfvars" -out tfplan
      terraform apply tfplan
      terraform output -json >"../terraform_output.json"
      cd ..
      printf "${blue}\nCreating UI...\n\n${normal}"
      if [[ $OS_TYPE == "darwin"* ]]; then
        create_sandbox_config $TF_FILE --osx
      else
        create_sandbox_config $TF_FILE --linux
      fi
      REACT_APP_ENV=${ENVIRONMENT} npm --prefix ./ui run build
      cd ui/build
      zip -r ../../ui.zip *
      cd ../..
      printf "${blue}\nDeploying UI...\n\n${normal}"
      deploy_sandbox_ui terraform_output.json ui.zip ${WORKSPACE}
      printf "${blue}\nFinished deployment process.\n\n${normal}"
    elif [ $MODE == --deploy-ui ]; then
      terraform workspace select ${WORKSPACE} || (printf "${yellow}\n${WORKSPACE} not found, initialising local state...\n\n${normal}" && terraform init && terraform workspace select ${WORKSPACE}) || (printf "${red}\n Remote state for ${WORKSPACE} not found. \n Initialise remote state by running deploy for the first time...\n\n${normal}" && rm -rf ./.terraform && rm .terraform.lock.hcl && exit 1);
      printf "${green}\n${WORKSPACE} selected!\n\n${normal}"
      if [ -f $TF_FILE ]; then
        cd ..
        printf "${blue}\nCreating UI...\n\n${normal}"
        if [[ "$OSTYPE" == "darwin"* ]]; then
          create_sandbox_config $TF_FILE --osx
        else
          create_sandbox_config $TF_FILE --linux
        fi
        REACT_APP_ENV=${ENVIRONMENT} npm --prefix ./ui run build
        cd ui/build
        zip -r ../../ui.zip *
        cd ../..
        printf "${blue}\nDeploying UI...\n\n${normal}"
        deploy_sandbox_ui $TF_FILE ui.zip ${WORKSPACE}
        printf "${blue}\nFinished deployment process.\n\n${normal}"
      else
        printf "${yellow}\nTerraform config not found, running deploy for the first time...\n\n${normal}"
        cd ..
        make -f ./makefile deploy-app-${WORKSPACE}
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
    ./gradlew lambdas:VirusScannedEvent:build) || printf "${red}\nError building lambdas, ensure you are in root directory.\n\n${normal}"
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
  aws amplify create-deployment --region "${aws_region}" --app-id "$app_id" --branch-name $3 >deployment.output
  jobId="$(jq -r .jobId deployment.output)"
  zipUploadUrl="$(jq -r .zipUploadUrl deployment.output)"
  rm -f deployment.output

  curl -XPUT --data-binary "@$2" "$zipUploadUrl"
  aws amplify start-deployment --region "${aws_region}" --app-id "$app_id" --branch-name $3 --job-id "${jobId}"
}

readonly command="$1"
case "${command}" in
deploy-app-sanda)
  run_sandbox "sanda" --deploy-app
  ;;
deploy-app-sandb)
  run_sandbox "sandb" --deploy-app
  ;;
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
*)
  echo "make $@"
  make "$@"
  ;;
esac