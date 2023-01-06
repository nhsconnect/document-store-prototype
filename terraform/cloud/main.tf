terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.47.0"
    }
  }

  backend "s3" {}
}

provider "aws" {
  region  = var.region
}

module apigateway {
  source = "../modules/api_gateway"
  api_gateway_stage = "prod"
  redeployment_triggers = [
    module.get_doc_ref_endpoint,
    module.create_doc_ref_endpoint,
    module.search_doc_ref_endpoint,
    module.delete_doc_ref_endpoint,
    module.patient_details_endpoint,
    module.document_manifest_endpoint,
    module.patient_details_collection_preflight,
    module.doc_ref_collection_preflight,
    module.get_doc_ref_preflight,
    module.document_manifest_preflight,
    module.document_reference_api,
    module.patient_details_api,
    module.document_manifest_api,
    aws_api_gateway_authorizer.cognito_authorizer
  ]
}

module lambda_iam_role {
  source = "../modules/lambda_iam_role"
}

locals {
  web_url = "https://${aws_amplify_branch.main.branch_name}.${aws_amplify_app.doc-store-ui.id}.amplifyapp.com"
}