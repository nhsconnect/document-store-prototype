#!/bin/sh
terraform -version
cd terraform
terraform init -reconfigure -backend-config local.s3.tfbackend
terraform plan -var-file=local.tfvars -var lambda_jar_filename=../app/build/libs/app.jar  -out=tfplan
terraform apply tfplan
