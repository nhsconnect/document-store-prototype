#!/bin/bash

set -euo pipefail

terraform -version
cd terraform
terraform init --backend-config="path=/home/dojo/terraform.tfstate"
terraform plan -var-file=local.tfvars -var lambda_jar_filename=../app/build/libs/app.jar  -out=tfplan
terraform apply tfplan
