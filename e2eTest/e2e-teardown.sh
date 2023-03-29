#!/bin/bash

ENVIRONMENT=${1:-local}

if [[ -z "$AWS_ENDPOINT" && $ENVIRONMENT == "local" ]]; then
    var AWS_ENDPOINT=http://localhost:4566
fi

#Get test bucket
var ENV_S3 = aws --endpoint-url=http://localhost:4566 s3 ls | awk '{print $3}' | grep test

aws --endpoint-url="$AWS_ENDPOINT" s3 rm "s3://$ENV_S3" --recursive
