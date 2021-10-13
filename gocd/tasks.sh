#!/usr/bin/env bash

set -e

declare command="$1"
case "${command}" in
plan-deploy)
  cd terraform
  terraform init
  terraform plan -var lambda_jar_filename=app.jar -out=planfile
  ;;
deploy)
  cd terraform
  terraform init
  terraform apply planfile
  ;;
esac
