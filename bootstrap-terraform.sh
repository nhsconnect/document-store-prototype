#!/bin/bash

ENVIRONMENT=${1:-local}
REGION=eu-west-2

if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    AWS_ENDPOINT=http://localhost:4566
fi

function createS3Bucket {
  BUCKET_NAME=prs-${ENVIRONMENT}-terraform-state

  echo 1
  aws s3api create-bucket \
    --bucket $BUCKET_NAME\
    --acl private \
    --create-bucket-configuration '{ "LocationConstraint": "eu-west-2" }' \
    ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

  echo 2
  aws s3api put-bucket-encryption \
    --bucket $BUCKET_NAME \
    --server-side-encryption-configuration '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}' \
    ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

  echo 3
  aws s3api put-public-access-block\
    --bucket $BUCKET_NAME \
    --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true \
    ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

  echo 4
  aws s3api put-bucket-tagging \
    --bucket $BUCKET_NAME \
    --tagging "TagSet=[{Key=createdBy,Value=prm-repo-team},{Key=name,Value=PRS terraform state for ${ENVIRONMENT} environment}]" \
    ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

  echo 5
  aws s3api put-bucket-lifecycle \
    --bucket $BUCKET_NAME \
    --lifecycle-configuration '{"Rules": [{"ID": "Expiration lifecycle rule","Prefix": "","Status": "Enabled","NoncurrentVersionExpiration": {"NoncurrentDays": 360}}]}' \
    ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

  echo 6
  aws s3api put-bucket-versioning \
    --bucket $BUCKET_NAME \
    --versioning-configuration Status=Enabled \
    ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}
}

function createDynamoDBTable {
  TABLE_NAME=prs-${ENVIRONMENT}-terraform-state-locking

  echo 10
  aws dynamodb create-table \
      --table-name $TABLE_NAME \
      --attribute-definitions AttributeName=LockID,AttributeType=S \
      --key-schema AttributeName=LockID,KeyType=HASH \
      --billing-mode PROVISIONED \
      --provisioned-throughput ReadCapacityUnits=2,WriteCapacityUnits=2 \
      --tags Key=createdBy,Value=prm-repo-team Key=name,Value="PRS terraform state locking table for ${ENVIRONMENT} environment" \
      ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}
}

function createPdsFhirAccessTokenParameter() {
  PARAM_NAME=/prs/${ENVIRONMENT}/pds-fhir-access-token
    aws ssm put-parameter \
        --name $PARAM_NAME \
        --value access-token-placeholder \
        --type SecureString \
        ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}
}

echo "bootstrapping..."

read -r -p "Are you sure you want to bootstrap terraform for the ${ENVIRONMENT} environment at ${AWS_ENDPOINT}? [y/N] " response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]
then
    createS3Bucket
    createDynamoDBTable
    createPdsFhirAccessTokenParameter
else
    exit
fi
echo "bootstrapped"
