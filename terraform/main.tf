terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.52.0"
    }
  }
  backend "s3" {}
}

provider "aws" {
  region = var.region

  skip_credentials_validation = var.disable_aws_remote_checks
  skip_requesting_account_id  = var.disable_aws_remote_checks
  s3_use_path_style           = var.disable_aws_remote_checks
  skip_metadata_api_check     = var.disable_aws_remote_checks

  endpoints {
    apigateway = var.aws_endpoint
    cloudwatch = var.aws_endpoint
    dynamodb   = var.aws_endpoint
    iam        = var.aws_endpoint
    kms        = var.aws_endpoint
    lambda     = var.aws_endpoint
    s3         = var.aws_endpoint
    sns        = var.aws_endpoint
    sqs        = var.aws_endpoint
    logs       = var.aws_endpoint
  }
}

resource "aws_iam_role" "lambda_execution_role" {
  name = "LambdaExecution"

  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Action    = "sts:AssumeRole"
        Effect    = "Allow"
        Sid       = ""
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_execution_policy" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_insights_policy" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLambdaInsightsExecutionRolePolicy"
}

resource "aws_api_gateway_rest_api" "lambda_api" {
  name = "DocStoreAPI"
}

resource "aws_api_gateway_deployment" "api_deploy" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  stage_name  = var.api_gateway_stage

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_rest_api.lambda_api.body,
      module.create_doc_ref_endpoint,
      module.search_doc_ref_endpoint,
      module.delete_doc_ref_endpoint,
      module.patient_details_endpoint,
      module.document_manifest_endpoint,
      module.patient_details_collection_preflight,
      module.doc_ref_collection_preflight,
      module.document_manifest_preflight,
      aws_api_gateway_resource.doc_ref_collection_resource,
      aws_api_gateway_resource.patient_details_collection_resource,
      aws_api_gateway_resource.document_manifest_resource,
      aws_api_gateway_authorizer.cognito_authorizer,
    ]))
  }
}

resource "aws_api_gateway_gateway_response" "doc_store_unauthorised_response" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  response_type = "DEFAULT_4XX"

  response_templates = {
    "application/json" = "{\"message\":$context.error.messageString}"
  }

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin" = "'${local.amplify_base_url}'"
    "gatewayresponse.header.Access-Control-Allow-Methods" = "'*'"
    "gatewayresponse.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
  }
}

resource "aws_api_gateway_gateway_response" "doc_store_bad_gateway_response" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  response_type = "DEFAULT_5XX"

  response_templates = {
    "application/json" = "{\"message\":$context.error.messageString}"
  }

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin" = "'${local.amplify_base_url}'"
    "gatewayresponse.header.Access-Control-Allow-Methods" = "'*'"
    "gatewayresponse.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
  }
}

resource aws_iam_policy "dynamodb_table_access_policy" {
  name   = "DynamoDBTableAccess"
  policy = data.aws_iam_policy_document.dynamodb_table_access_policy_doc.json
}

data aws_iam_policy_document "dynamodb_table_access_policy_doc" {
  statement {
    effect  = "Allow"
    actions = [
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
      "dynamodb:UpdateTimeToLive",
      "dynamodb:ConditionCheckItem",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:PartiQLUpdate",
      "dynamodb:Scan",
      "dynamodb:Query",
      "dynamodb:UpdateItem",
      "dynamodb:DescribeTimeToLive",
      "dynamodb:PartiQLSelect",
      "dynamodb:DescribeTable",
      "dynamodb:PartiQLInsert",
      "dynamodb:GetItem",
      "dynamodb:PartiQLDelete"
    ]
    resources = ["arn:aws:dynamodb:*:*:table/*"]
  }
}

resource "aws_vpc" "virus_scanning_vpc" {
  cidr_block = "10.0.0.0/16"
  tags = {
    Name = "Virus Scanning Default VPC"
  }
  count = var.cloud_only_service_instances
}

resource "aws_subnet" "virus_scanning_subnet1" {
  availability_zone = "eu-west-2a"
  vpc_id = aws_vpc.virus_scanning_vpc[0].id
  cidr_block = "10.0.1.0/24"

  tags = {
    Name = "Subnet for eu-west-2a"
  }
  count = var.cloud_only_service_instances
}

resource "aws_subnet" "virus_scanning_subnet2" {
  availability_zone = "eu-west-2b"
  vpc_id = aws_vpc.virus_scanning_vpc[0].id
  cidr_block = "10.0.2.0/24"

  tags = {
    Name = "Subnet for eu-west-2b"
  }
  count = var.cloud_only_service_instances
}

data "aws_ssm_parameter" "cloud_security_email" {
  name = "/prs/${var.environment}/user-input/cloud-security-email"
  count = var.cloud_only_service_instances
}

output "api_gateway_rest_api_id" {
  value = aws_api_gateway_deployment.api_deploy.rest_api_id
}

output "api_gateway_rest_api_stage" {
  value = aws_api_gateway_deployment.api_deploy.stage_name
}

output "api_gateway_url" {
  value = aws_api_gateway_deployment.api_deploy.invoke_url
}

locals {
  common_environment_variables             = {
    DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
    DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
    S3_ENDPOINT                = var.s3_endpoint
    S3_USE_PATH_STYLE          = var.s3_use_path_style
    SQS_ENDPOINT               = var.sqs_endpoint
    SQS_AUDIT_QUEUE_URL        = aws_sqs_queue.sensitive_audit.url
  }
  authoriser_environment_variables = {
    DYNAMODB_ENDPOINT  = var.dynamodb_endpoint
    OIDC_ISSUER_URL    = var.cognito_cis2_provider_oidc_issuer
    OIDC_AUTHORIZE_URL = var.cognito_cis2_provider_authorize_url
    OIDC_JWKS_URL      = var.cognito_cis2_provider_jwks_uri
    OIDC_CALLBACK_URL  = var.cloud_only_service_instances > 0 ? "${local.amplify_base_url}/cis2-auth-callback" : var.cognito_cis2_client_callback_urls[0]
    OIDC_CLIENT_ID     = var.cognito_cis2_provider_client_id
    OIDC_CLIENT_SECRET = var.cognito_cis2_provider_client_secret
    OIDC_TOKEN_URL     = var.cognito_cis2_provider_token_url
  }
  amplify_base_url = var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : ""
}
