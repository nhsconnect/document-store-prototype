#!/bin/sh
terraform -version
cd terraform/local
terraform init
echo "Planning and applying terraform"
terraform apply -var-file=local.tfvars -auto-approve
