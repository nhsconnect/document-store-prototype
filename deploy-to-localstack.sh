#!/bin/sh
terraform -version
cd terraform/local
terraform init
terraform plan -var-file=local.tfvars -var lambda_jar_filename=../app/build/libs/app.jar  -out=tfplan
terraform apply tfplan
