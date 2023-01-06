#!/bin/sh
terraform -version
cd terraform/local
terraform init
terraform plan -var-file=local.tfvars  -out=tfplan
terraform apply tfplan
