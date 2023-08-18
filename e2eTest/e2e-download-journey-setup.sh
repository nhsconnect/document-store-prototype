#!/bin/bash

ENVIRONMENT=${1:-local}
REGION=eu-west-2
echo "Environment set" > AlexCool.log
if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    AWS_ENDPOINT=http://localhost:4566
fi

S3_BUCKET_NAME=$(aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 ls | awk '{print $3}' | grep '^dev-test-document-store')
echo "Bucket name set" > AlexCool.log
KEY1="file-1"
KEY2="file-2"

S3_LOCATION_1="s3://${S3_BUCKET_NAME}/${KEY1}"
S3_LOCATION_2="s3://${S3_BUCKET_NAME}/${KEY2}"
echo "S3 locations set" > AlexCool.log
jq --arg s3location1 "$S3_LOCATION_1" --arg s3location2 "$S3_LOCATION_2" ".dev_DocumentReferenceMetadata[].PutRequest.Item |= (
    if .Location.S == \"s3-location-1\" then
        .Location.S = \$s3location1\
    elif
        .Location.S == \"s3-location-2\" then
          .Location.S = \$s3location2\
    else
        .
    end)" uploaded-docs.json.example > uploaded-docs.json
echo "JQ done" > AlexCool.log
aws dynamodb batch-write-item --request-items file://uploaded-docs.json ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}
echo "Writeen to Dynamo" > AlexCool.log
aws s3api put-object --bucket "$S3_BUCKET_NAME" --key $KEY1 --body content.txt ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}
echo "Uploaded 1 file to S3" > AlexCool.log
aws s3api put-object --bucket "$S3_BUCKET_NAME" --key $KEY2 --body content.txt ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT}
echo "Uploaded 2 files to S3" > AlexCool.log