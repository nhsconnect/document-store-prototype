#!/usr/bin/env bash

set -e

readonly aws_region=eu-west-2

function assume_ci_role() {
  role_arn="$(aws ssm get-parameters --region ${aws_region} --names /document-store/dev/user-input/ci-role --query Parameters[0].Value --output text)"
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

function export_aws_credentials() {
  export_aws_access_key="export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID"
  export_aws_secret_access_key="export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY"
  export_aws_session_token="export AWS_SESSION_TOKEN=$AWS_SESSION_TOKEN"
  echo "${export_aws_access_key}
${export_aws_secret_access_key}
${export_aws_session_token}" >temp_aws_credentials.sh
  chmod +x temp_aws_credentials.sh
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

function update_ui_config_file() {
  user_pool="$(jq -r '.cognito_user_pool_ids.value[0]' "$1")"
  user_pool_client_id="$(jq -r '.cognito_client_ids.value[0]' "$1")"
  api_endpoint="$(jq -r '.api_gateway_url.value' "$1")"
  sed -i "s/%pool-id%/${user_pool}/" ui/src/config.js
  sed -i "s/%client-id%/${user_pool_client_id}/" ui/src/config.js
  sed -i "s/%region%/${aws_region}/" ui/src/config.js
  sed -i "s~%api-endpoint%~${api_endpoint}~" ui/src/config.js
}

function get_terraform_output() {
  cd terraform
  assume_ci_role
  terraform init
  terraform output -json >"../$1"
  cd ..
}

function repackage_tgz_as_zip() {
  mkdir ui-build-artefacts
  cd ui-build-artefacts
  tar -xf "../$1"
  zip -r "../$2" *
  cd ..
  rm -rf ui-build-artefacts
}

readonly command="$1"
case "${command}" in
install-ui-dependencies)
  cd ui
  npm install
  ;;
configure-ui)
  get_terraform_output terraform_output.json
  update_ui_config_file terraform_output.json
  ;;
test-ui)
  cd ui
  CI=true npm test
  ;;
build-ui)
  cd ui
  npm run build
  cd build
  tar -czf ../ui.tgz *
  ;;
plan-deploy)
  cd terraform
  assume_ci_role
  terraform init
  terraform plan -var lambda_jar_filename=../jars/libs/app.jar -out=tfplan
  ;;
deploy)
  cd terraform
  assume_ci_role
  terraform init
  terraform apply tfplan
  ;;
deploy-ui)
  get_terraform_output terraform_output.json
  repackage_tgz_as_zip tars/ui.tgz ui.zip
  deploy_ui terraform_output.json ui.zip
  ;;
extract-api-url)
  cd terraform
  assume_ci_role
  terraform init
  terraform output api_gateway_url >../api_gateway_url_artifact
  ;;
export-aws-creds)
  assume_ci_role
  export_aws_credentials
  ;;
run-test-harness)
  source ./temp_aws_credentials.sh
  echo "auth: $API_AUTH"
  rm temp_aws_credentials.sh
  ./gradlew testHarness:test
  ;;
esac
