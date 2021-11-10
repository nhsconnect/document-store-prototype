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
${export_aws_session_token}" > temp_aws_credentials.sh
chmod +x temp_aws_credentials.sh
}

function deploy_ui() {
  app_id="$(jq -r '.[0]' amplify_app_ids.json)"
  aws amplify create-deployment --region "${aws_region}" --app-id "$app_id" --branch-name main > deployment.output
  jobId="$(jq -r .jobId deployment.output)"
  zipUploadUrl="$(jq -r .zipUploadUrl deployment.output)"
  echo $jobId
  echo $zipUploadUrl

  mkdir ui-build-artefacts
  cd ui-build-artefacts
  tar -xf ../tars/ui.tgz
  zip -r ui.zip *

  curl -XPUT --data-binary "@ui.zip" "$zipUploadUrl"
  aws amplify start-deployment --region "${aws_region}" --app-id "$app_id" --branch-name main --job-id "${jobId}"
  rm -f ../deployment.output
}

readonly command="$1"
case "${command}" in
install-ui-dependencies)
  cd ui
  npm install
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
  cd terraform
  assume_ci_role
  terraform init
  terraform output -json amplify_app_ids > ../amplify_app_ids.json

  cd ..
  deploy_ui
  ;;
extract-api-url)
  cd terraform
  assume_ci_role
  terraform init
  terraform output api_gateway_url > ../api_gateway_url_artifact
  ;;
export-aws-creds)
  assume_ci_role
  export_aws_credentials
  ;;
run-test-harness)
  source ./temp_aws_credentials.sh
  rm temp_aws_credentials.sh
  ./gradlew testHarness:test
  ;;
esac
