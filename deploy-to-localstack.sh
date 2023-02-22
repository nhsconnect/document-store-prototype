#!/bin/bash

set -euo pipefail

terraform -version
cd terraform
terraform init --backend-config="path=/home/dojo/terraform.tfstate"
terraform plan -var-file=local.tfvars -var lambda_jar_filename=../app/build/libs/app.jar -var cognito_cis2_provider_client_id="$OIDC_CLIENT_ID" -out=tfplan
terraform apply tfplan
