terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.47.0"
    }
  }
}

provider "aws" {
  region  = var.region

  skip_credentials_validation = true
  skip_requesting_account_id  = true
  s3_use_path_style         = true
  skip_metadata_api_check     = true

  endpoints {
    apigateway = "http://localstack:4566"
    cloudwatch = "http://localstack:4566"
    dynamodb   = "http://localstack:4566"
    iam        = "http://localstack:4566"
    lambda     = "http://localstack:4566"
    s3         = "http://localstack:4566"
    sqs        = "http://localstack:4566"
  }
}

module apigateway {
  source = "../modules/api_gateway"
  api_gateway_stage = "test"
  redeployment_triggers = [
    module.get_doc_ref_endpoint,
    module.create_doc_ref_endpoint,
    module.patient_details_endpoint,
    module.delete_doc_ref_endpoint,
    module.patient_details_endpoint,
    module.document_manifest_endpoint,
    module.patient_details_collection_preflight,
    module.doc_ref_collection_preflight,
    module.get_doc_ref_preflight,
    module.document_manifest_preflight,
    module.document_reference_api,
    module.patient_details_api,
    module.document_manifest_api
  ]
}

module lambda_iam_role {
  source = "../modules/lambda_iam_role"
}

locals {
  web_url = "http://localhost:3000"
}