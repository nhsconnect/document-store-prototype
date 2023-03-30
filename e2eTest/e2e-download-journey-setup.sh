#!/bin/bash

ENVIRONMENT=${1:-local}
REGION=eu-west-2

if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    AWS_ENDPOINT=http://localhost:4566
fi

echo ENVIRONMENT
aws --version

S3_BUCKET_NAME=$(aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 ls | awk '{print $3}' | grep test)
KEY1="file-1"
KEY2="file-2"

cp uploaded-docs.json.example uploaded-docs.json

if [[ $ENVIRONMENT == "local" ]]; then
    sed -i '' -e "s/%s3-location-1%/s3:\/\/${S3_BUCKET_NAME}\/${KEY1}/" uploaded-docs.json
    sed -i '' -e "s/%s3-location-2%/s3:\/\/${S3_BUCKET_NAME}\/${KEY2}/" uploaded-docs.json
else
  sed -i "s/%s3-location-1%/s3:\/\/${S3_BUCKET_NAME}\/${KEY1}/" uploaded-docs.json
  sed -i "s/%s3-location-2%/s3:\/\/${S3_BUCKET_NAME}\/${KEY2}/" uploaded-docs.json
fi

aws dynamodb batch-write-item --request-items file://uploaded-docs.json ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

aws s3api put-object --bucket $S3_BUCKET_NAME --key $KEY1 --body content.txt ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}

aws s3api put-object --bucket $S3_BUCKET_NAME --key $KEY2 --body content.txt ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}