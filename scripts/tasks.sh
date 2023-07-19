#!/usr/bin/env bash

set -e

readonly aws_region=eu-west-2

export AWS_CLI_AUTO_PROMPT=off

function assume_ci_role() {
  role_arn="$(aws ssm get-parameters --region ${aws_region} --names /document-store/${ENVIRONMENT}/user-input/ci-role --query Parameters[0].Value --output text)"
  session_name="document-store-session"

  echo "Assuming ci-role in document store..."

  sts=($(aws sts assume-role \
    --role-arn "$role_arn" \
    --role-session-name "$session_name" \
    --query 'Credentials.[AccessKeyId,SecretAccessKey,SessionToken]' \
    --output text))

  export AWS_ACCESS_KEY_ID="${sts[0]}"
  export AWS_SECRET_ACCESS_KEY="${sts[1]}"
  export AWS_SESSION_TOKEN="${sts[2]}"
}

function assume_test_runner_role() {
  role_arn="arn:aws:iam::533825906475:role/IntegrationTestRunner"
  session_name="document-store-test-runner-session"

  echo "Assuming test runner role in document store..."

  sts=($(aws sts assume-role \
    --role-arn "$role_arn" \
    --role-session-name "$session_name" \
    --query 'Credentials.[AccessKeyId,SecretAccessKey,SessionToken]' \
    --output text))

  export AWS_ACCESS_KEY_ID="${sts[0]}"
  export AWS_SECRET_ACCESS_KEY="${sts[1]}"
  export AWS_SESSION_TOKEN="${sts[2]}"
}

function clear_assumed_iam_role() {
  unset AWS_ACCESS_KEY_ID
  unset AWS_SECRET_ACCESS_KEY
  unset AWS_SESSION_TOKEN
}

function get_ssm_parameter() {
  echo "$(aws ssm get-parameter --region ${aws_region} --name $1 --query Parameter.Value --output text)"
}

function get_encrypted_ssm_parameter() {
  echo "$(aws ssm get-parameter --region ${aws_region} --name $1 --with-decryption --query Parameter.Value --output text)"
}

function deploy_ui() {
  app_id="$(jq -r '.amplify_app_ids.value[0]' "$1")"
  aws amplify create-deployment --region "${aws_region}" --app-id "$app_id" --branch-name main >deployment.output
  jobId="$(jq -r .jobId deployment.output)"
  zipUploadUrl="$(jq -r .zipUploadUrl deployment.output)"
  rm -f deployment.output
  curl -XPUT --data-binary "@$2" "$zipUploadUrl"
  aws amplify start-deployment --region "${aws_region}" --app-id "$app_id" --branch-name main --job-id "${jobId}"
}

function get_signout_url {
  if [[ $ENVIRONMENT -eq "dev" ]]; then
    echo '.cognito_redirect_signout.value[1]'
  else
    echo '.cognito_redirect_signout.value[0]'
  fi
}

function create_ui_config_file() {
  cp ui/src/config.js.example ui/src/config.js
  api_endpoint="$(jq -r '.api_gateway_url.value' "$1")"
  amplify_app_id="$(jq -r '.amplify_app_ids.value[0]' "$1")"
  sed -i "s/%region%/${aws_region}/" ui/src/config.js
  sed -i "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
  sed -i "s/%amplify-app-id%/${amplify_app_id}/" ui/src/config.js
}

function create_cypress_config_file() {
  cp e2eTest/cypress.env.json.example e2eTest/cypress.env.json
  username="$(get_ssm_parameter /prs/${ENVIRONMENT}/user-input/cypress-username)"
  password="$(get_encrypted_ssm_parameter /prs/${ENVIRONMENT}/user-input/cypress-password)"
  basic_auth_username=$ENVIRONMENT
  basic_auth_password="$(get_encrypted_ssm_parameter /prs/${ENVIRONMENT}/user-input/basic-auth-password)"
  oidc_provider=$OIDC_PROVIDER_ID

  sed -i "s/%environment%/${ENVIRONMENT}/" e2eTest/cypress.env.json
  sed -i "s/%username%/${username}/" e2eTest/cypress.env.json
  sed -i "s~%password%~${password}~" e2eTest/cypress.env.json
  sed -i "s~%basic_auth_username%~${basic_auth_username}~" e2eTest/cypress.env.json
  sed -i "s~%basic_auth_password%~${basic_auth_password}~" e2eTest/cypress.env.json
  sed -i "s/%oidc_provider%/${oidc_provider}/" e2eTest/cypress.env.json
}

function export_cypress_base_url() {
  echo "CYPRESS_BASE_URL=https://main.$(jq -r '.amplify_app_ids.value[0]' "$1").amplifyapp.com" >e2eTest/cypress.sh
}

function tf_init {
  check_env
  cd terraform
  terraform init \
    -backend-config ${ENVIRONMENT}.s3.tfbackend
  terraform workspace select ${WORKSPACE}
}

function tf_init_virus_scanner {
  check_env
  cd virusScanner/terraform

  terraform init \
    -backend-config ${ENVIRONMENT}.s3.tfbackend
}

function confirm_current_role {
  sts=$(aws sts get-caller-identity)
  if [[ $? -eq 254 ]]; then
    echo ${sts}
    return 1
  fi

  echo ${sts}
  read -p "Is this the intended role (y/n)? " -n 1 -r
  echo
  if ! [[ $REPLY =~ ^[Yy]$ ]]; then
    exit
  fi
}

function check_env {
  if [[ -z "${ENVIRONMENT}" ]]; then
    echo "Must set ENVIRONMENT"
    exit 1
  fi
}

function get_user_pool_id {
  echo "$(aws cognito-idp list-user-pools --region ${aws_region} --query 'UserPools[?Name==`doc-store-user-pool`].Id' --max-results 1 | jq -r '.[0]')"
}

function get_terraform_output() {
  tf_init
  terraform output -json >"../$1"
  cd ..
}

function create_cognito_user {
  aws cognito-idp admin-create-user --user-pool-id $(get_user_pool_id) --username "$1" --temporary-password "$2"
}

function create_json_for_nhs_api_public_key() {
  echo $(get_ssm_parameter /prs/${ENVIRONMENT}/user-input/pds-fhir-public-key) >ui/public/jwks.json
}

function create_workspace_ui_config_file() {
  cp ui/src/config.js.example ui/src/config.js
  api_endpoint="$(jq -r '.api_gateway_url.value' "$1")"
  amplify_app_id="$(jq -r '.amplify_app_ids.value[0]' "$1")"
  sed -i "" "s/%region%/${aws_region}/" ui/src/config.js
  sed -i "" "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
  sed -i "" "s/%amplify-app-id%/${amplify_app_id}/" ui/src/config.js
  sed -i "" "s/%oidc-provider-id%/$OIDC_PROVIDER_ID/" ui/src/config.js
}


function delete_cognito_user {
  aws cognito-idp admin-delete-user --user-pool-id $(get_user_pool_id) --username "$1"
  if [[ $? -eq 0 ]]; then
    echo "User deleted"
  fi
}

readonly command="$1"
case "${command}" in
install-ui-dependencies)
  cd ui
  npm ci
  cd ..
  ;;
configure-ui)
  assume_ci_role
  get_terraform_output terraform_output.json
  create_ui_config_file terraform_output.json
  create_json_for_nhs_api_public_key
  clear_assumed_iam_role
  ;;
test-ui)
  cd ui
  CI=true npm test
  ;;
build-ui)
  cd ui
  REACT_APP_ENV=${ENVIRONMENT} npm run build
  cd ..
  ;;
deploy-ui)
  assume_ci_role
  get_terraform_output terraform_output.json
  cd ui/build
  zip -r ../../ui.zip *
  cd ../..
  deploy_ui terraform_output.json ui.zip
  clear_assumed_iam_role
  ;;
_build-api-jars)
  gradle assemble
  ;;
build-api-jars)
  dojo './tasks _build-api-jars'
  ;;
start-localstack)
  set -a
  source .env
  set +a
  dojo -c Dojofile-dev
  ;;
view-localstack-logs)
  export LOCALSTACK_CONTAINER=$(docker ps -a | grep localstack/localstack | head -1 | cut -f 1 -d ' ')
  docker logs $LOCALSTACK_CONTAINER --follow
  ;;
bootstrap-terraform)
  dojo './tasks _bootstrap-terraform'
  ;;
_bootstrap-terraform)
  ./bootstrap-terraform.sh
  ;;
_deploy-to-localstack)
  test -e app/build/libs/app.jar || dojo 'gradle assemble'
  ./deploy-to-localstack.sh
  ;;
tf_plan)
  assume_ci_role
  tf_init
  terraform plan -var-file="../terraform/${ENVIRONMENT}.tfvars" \
    -var cis2_provider_client_id=$(get_ssm_parameter /${NHS_CIS2_ENVIRONMENT}/cis2/client_id) \
    -var cis2_provider_client_secret=$(get_encrypted_ssm_parameter /${NHS_CIS2_ENVIRONMENT}/cis2/client_secret) \
    -var basic_auth_password="$(get_encrypted_ssm_parameter /prs/${ENVIRONMENT}/user-input/basic-auth-password)" \
    -var create_doc_manifest_lambda_jar_filename=../jars/libs/CreateDocumentManifestByNhsNumber.jar \
    -var create_doc_ref_lambda_jar_filename=../jars/libs/CreateDocumentReference.jar \
    -var delete_doc_ref_lambda_jar_filename=../jars/libs/DeleteDocumentReference.jar \
    -var doc_ref_search_lambda_jar_filename=../jars/libs/DocumentReferenceSearch.jar \
    -var fake_virus_scanner_event_lambda_jar_filename=../jars/libs/FakeVirusScannedEvent.jar \
    -var reregistration_event_lambda_jar_filename=../jars/libs/ReRegistrationEvent.jar \
    -var search_patient_details_lambda_jar_filename=../jars/libs/SearchPatientDetails.jar \
    -var virus_scanner_event_lambda_jar_filename=../jars/libs/VirusScannedEvent.jar \
    -var authoriser_lambda_jar_filename=../jars/libs/authoriser.jar \
    -var lambda_layers_filename=../jars/distributions/app.zip \
    -out=tfplan
  clear_assumed_iam_role
  ;;
tf_apply)
  assume_ci_role
  tf_init
  terraform apply tfplan
  clear_assumed_iam_role
  ;;
extract-raw-terraform-output)
  terraform_output_name=$2
  assume_ci_role
  tf_init
  terraform output -raw "${terraform_output_name}" >"../${terraform_output_name}_artifact"
  clear_assumed_iam_role
  ;;
extract-json-terraform-output)
  terraform_output_name=$2
  assume_ci_role
  tf_init
  terraform output -json "${terraform_output_name}" >"../${terraform_output_name}_artifact"
  clear_assumed_iam_role
  ;;
run-integration-test)
  assume_test_runner_role
  gradle integrationTest
  clear_assumed_iam_role
  ;;
install-e2e-test-dependencies)
  cd e2eTest
  npm ci
  ;;
configure-e2e-test)
  assume_ci_role
  get_terraform_output terraform_output.json
  export_cypress_base_url terraform_output.json
  create_cypress_config_file
  clear_assumed_iam_role
  ;;
run-e2e-test)
  assume_ci_role
  cd e2eTest
  source ./cypress.sh
  echo $CYPRESS_BASE_URL
  rm cypress.sh
  CYPRESS_BASE_URL=${CYPRESS_BASE_URL} ./node_modules/.bin/cypress run
  clear_assumed_iam_role
  ;;
create-cognito-user)
  username=$2
  password=$3
  dojo "./tasks _create-cognito-user ${username} ${password}"
  ;;
_create-cognito-user)
  username=$2
  password=$3
  confirm_current_role
  create_cognito_user ${username} ${password}
  ;;
delete-cognito-user)
  username=$2
  dojo "./tasks _delete-cognito-user ${username}"
  ;;
_delete-cognito-user)
  username=$2
  confirm_current_role
  delete_cognito_user ${username}
  ;;
attach-policy-to-ci-role)
  dojo "./tasks _attach-policy-to-ci-role"
  ;;
_attach-policy-to-ci-role)
  confirm_current_role
  aws iam put-role-policy --role-name=ci-agent --policy-name=ci-role-policy --policy-document=file://ci-role-policy.json
  ;;
bootstrap-ci-role)
  dojo "./tasks _bootstrap-ci-role"
  ;;
_bootstrap-ci-role)
  confirm_current_role
  aws iam create-role --role-name=ci-agent --assume-role-policy-document=file://ci-assume-role-policy.json
  ;;
_java-dependency-scan)
  gradle dependencyCheckAnalyze
  ;;
java-dependency-scan)
  dojo "./tasks _java-dependency-scan"
  ;;
_javascript-dependency-scan)
  cd ui && npm ci -y
  npm run audit
  ;;
javascript-dependency-scan)
  dojo -c Dojofile-node "./tasks _javascript-dependency-scan"
  ;;
virus-scanner-tf-plan)
  assume_ci_role
  tf_init_virus_scanner
  terraform plan -var-file="${ENVIRONMENT}.tfvars"\
  -out=virus-scanner-tfplan
  clear_assumed_iam_role
  ;;
virus-scanner-tf-apply)
  assume_ci_role
  tf_init_virus_scanner
  terraform apply virus-scanner-tfplan
  clear_assumed_iam_role
  ;;
*)
  echo "make $@"
  make "$@"
  ;;
esac

