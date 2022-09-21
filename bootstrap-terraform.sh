#!/bin/bash

ENVIRONMENT=$1
REGION=eu-west-2

function createS3Bucket() {
  BUCKET_NAME=prs-${ENVIRONMENT}-terraform-state

  aws s3api create-bucket \
    --bucket $BUCKET_NAME\
    --acl private \
    --create-bucket-configuration '{ "LocationConstraint": "eu-west-2" }'

  aws s3api put-bucket-encryption \
      --bucket $BUCKET_NAME \
      --server-side-encryption-configuration '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}'

  aws s3api put-public-access-block\
    --bucket $BUCKET_NAME \
    --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

  aws s3api put-bucket-tagging \
    --bucket $BUCKET_NAME \
    --tagging "TagSet=[{Key=createdBy,Value=prm-repo-team},{Key=name,Value=PRS terraform state for ${ENVIRONMENT} environment}]"

  aws s3api put-bucket-lifecycle \
    --bucket $BUCKET_NAME \
    --lifecycle-configuration '{"Rules": [{"ID": "Expiration lifecycle rule","Prefix": "","Status": "Enabled","NoncurrentVersionExpiration": {"NoncurrentDays": 360}}]}'

  aws s3api put-bucket-versioning \
    --bucket $BUCKET_NAME \
    --versioning-configuration Status=Enabled
}

function createDynamoDBTable() {
  TABLE_NAME=prs-${ENVIRONMENT}-terraform-state-locking

  aws dynamodb create-table \
      --table-name $TABLE_NAME \
      --attribute-definitions AttributeName=LockID,AttributeType=S \
      --key-schema AttributeName=LockID,KeyType=HASH \
      --billing-mode PROVISIONED \
      --provisioned-throughput ReadCapacityUnits=2,WriteCapacityUnits=2 \
      --tags Key=createdBy,Value=prm-repo-team Key=name,Value="PRS terraform state locking table for ${ENVIRONMENT} environment"
}

createS3Bucket
createDynamoDBTable
