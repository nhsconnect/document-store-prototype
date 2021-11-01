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

readonly command="$1"
case "${command}" in
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
