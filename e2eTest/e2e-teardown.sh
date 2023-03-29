#!/bin/bash

ENVIRONMENT=${1:-local}

if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    AWS_ENDPOINT=http://localhost:4566
fi

#Get test bucket
ENV_S3=$(aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 ls | awk '{print $3}' | grep test)

aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} s3 rm "s3://$ENV_S3" --recursive

TABLE_NAME="DocumentReferenceMetadata"

aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} dynamodb describe-table --table-name $TABLE_NAME | jq '.Table | del(.TableId, .TableArn, .ItemCount, .TableSizeBytes, .CreationDateTime, .TableStatus, .LatestStreamArn, .LatestStreamLabel, .ProvisionedThroughput.NumberOfDecreasesToday, .ProvisionedThroughput.LastIncreaseDateTime)' > schema.json

aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} dynamodb delete-table --table-name $TABLE_NAME

aws ${AWS_ENDPOINT:+--endpoint-url=$AWS_ENDPOINT} dynamodb create-table --cli-input-json file://schema.json \
&& rm schema.json