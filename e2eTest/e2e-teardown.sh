#!/bin/bash

ENVIRONMENT=${1:-local}

if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    AWS_ENDPOINT=http://localhost:4566
fi

#Get test bucket
ENV_S3=$(aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 ls | awk '{print $3}' | grep test)

aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 rm "s3://$ENV_S3" --recursive

TABLE_NAME="DocumentReferenceMetadata"

aws dynamodb scan \
  --attributes-to-get "ID" \
  --table-name $TABLE_NAME --query "Items[*]" \
  ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} \
  | jq --compact-output '.[]' \
  | tr '\n' '\0' \
  | xargs -0 -t -I keyItem \
    aws dynamodb delete-item ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} --table-name $TABLE_NAME --key=keyItem