#!/bin/bash

ENVIRONMENT=${1:-local}
BUCKET="dev-test-document-store"

if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    AWS_ENDPOINT=http://localhost:4566
    BUCKET="dev-test-document-store"
fi

#Get test bucket
S3_BUCKET_NAME=$(aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 ls | awk '{print $3}' | grep $BUCKET)
echo S3_BUCKET_NAME
aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 rm "s3://$S3_BUCKET_NAME" --recursive

TABLE_NAME="dev_DocumentReferenceMetadata"

aws dynamodb scan \
  --attributes-to-get "ID" \
  --table-name $TABLE_NAME --query "Items[*]" \
  ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} \
  | jq --compact-output '.[]' \
  | tr '\n' '\0' \
  | xargs -0 -t -I keyItem \
    aws dynamodb delete-item ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} --table-name $TABLE_NAME --key=keyItem