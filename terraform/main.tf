terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.52.0"
    }
  }
  backend "s3" {
    bucket         = "prs-dev-terraform-state"
    dynamodb_table = "prs-dev-terraform-state-locking"
    region         = "eu-west-2"
    key            = "prs/terraform.tfstate"
    encrypt        = true
  }
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
  name = "${terraform.workspace}_LambdaExecution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
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
  name = "${terraform.workspace}_DocStoreAPI"
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
      aws_api_gateway_authorizer.cis2_authoriser,
      aws_api_gateway_resource.doc_ref_collection_resource,
      aws_api_gateway_resource.patient_details_collection_resource,
      aws_api_gateway_resource.document_manifest_resource,
      aws_api_gateway_resource.login_resource,
      aws_api_gateway_resource.logout_resource,
      aws_api_gateway_resource.back_channel_logout_resource,
      aws_api_gateway_resource.auth_resource,
      aws_api_gateway_resource.token_request_resource,
      aws_api_gateway_resource.verify_organisation_resource,
      aws_api_gateway_integration.back_channel_logout_lambda_integration,
      aws_api_gateway_integration.login_lambda_integration,
      aws_api_gateway_integration.logout_lambda_integration,
      aws_api_gateway_integration.token_request_lambda_integration,
      aws_api_gateway_integration.verify_organisation_lambda_integration,
      aws_api_gateway_method.token_request_proxy_method,
      aws_api_gateway_method.login_proxy_method,
      aws_api_gateway_method.logout_proxy_method,
      aws_api_gateway_method.back_channel_logout_proxy_method,
      aws_api_gateway_method.verify_organisation_request_proxy_method
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
    "gatewayresponse.header.Access-Control-Allow-Origin"      = terraform.workspace != "prod" ? "'https://${terraform.workspace}.access-request-fulfilment.patient-deductions.nhs.uk'" : "'https://access-request-fulfilment.patient-deductions.nhs.uk'"
    "gatewayresponse.header.Access-Control-Allow-Methods"     = "'*'"
    "gatewayresponse.header.Access-Control-Allow-Headers"     = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Auth-Cookie,Accept'"
    "gatewayresponse.header.Access-Control-Allow-Credentials" = "'true'"
  }
}

resource "aws_api_gateway_gateway_response" "doc_store_bad_gateway_response" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  response_type = "DEFAULT_5XX"

  response_templates = {
    "application/json" = "{\"message\":$context.error.messageString}"
  }

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin"      = terraform.workspace != "prod" ? "'https://${terraform.workspace}.access-request-fulfilment.patient-deductions.nhs.uk'" : "'https://access-request-fulfilment.patient-deductions.nhs.uk'"
    "gatewayresponse.header.Access-Control-Allow-Methods"     = "'*'"
    "gatewayresponse.header.Access-Control-Allow-Headers"     = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Auth-Cookie,Accept'"
    "gatewayresponse.header.Access-Control-Allow-Credentials" = "'true'"
  }
}

resource "aws_iam_policy" "dynamodb_table_access_policy" {
  name   = "${terraform.workspace}_DynamoDBTableAccess"
  policy = data.aws_iam_policy_document.dynamodb_table_access_policy_doc.json
}
data "aws_ssm_parameter" "cis2_provider_client_id" {
  name = "/${var.NHS_CIS2_ENVIRONMENT}/cis2/client_id"
}

data "aws_ssm_parameter" "cis2_provider_client_secret" {
  name = "/${var.NHS_CIS2_ENVIRONMENT}/cis2/client_secret"
}

data "aws_iam_policy_document" "dynamodb_table_access_policy_doc" {
  statement {
    effect = "Allow"
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

locals {
  common_environment_variables = {
    DOCUMENT_STORE_BUCKET_NAME = var.workspace_is_a_sandbox ? aws_s3_bucket.test_document_store.bucket : aws_s3_bucket.document_store[0].bucket
    DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
    S3_ENDPOINT                = var.s3_endpoint
    S3_USE_PATH_STYLE          = var.s3_use_path_style
    SQS_ENDPOINT               = var.sqs_endpoint
    SQS_AUDIT_QUEUE_URL        = aws_sqs_queue.sensitive_audit.url
    WORKSPACE                  = terraform.workspace
    ENVIRONMENT                = var.environment
  }

  authoriser_environment_variables = {
    DYNAMODB_ENDPOINT  = var.dynamodb_endpoint
    ENVIRONMENT        = var.environment
    OIDC_ISSUER_URL    = var.cis2_provider_oidc_issuer
    OIDC_AUTHORIZE_URL = var.cis2_provider_authorize_url
    OIDC_JWKS_URL      = var.cis2_provider_jwks_uri
    OIDC_CALLBACK_URL  = var.cloud_only_service_instances > 0 ? "${local.amplify_base_url}/auth-callback" : var.cis2_client_callback_urls[0]
    OIDC_CLIENT_ID     = data.aws_ssm_parameter.cis2_provider_client_id.value
    OIDC_CLIENT_SECRET = data.aws_ssm_parameter.cis2_provider_client_secret.value
    OIDC_TOKEN_URL     = var.cis2_provider_token_url
    OIDC_USER_INFO_URL = var.cis2_provider_user_info_url
    SQS_AUDIT_QUEUE_URL = aws_sqs_queue.sensitive_audit.url
    SQS_ENDPOINT       = var.sqs_endpoint
    WORKSPACE          = terraform.workspace
  }
  amplify_base_url    = var.cloud_only_service_instances > 0 ? "https://${terraform.workspace}.access-request-fulfilment.patient-deductions.nhs.uk" : ""
  app_base_url        = terraform.workspace != "prod" ? "https://${terraform.workspace}.access-request-fulfilment.patient-deductions.nhs.uk" : "https://access-request-fulfilment.patient-deductions.nhs.uk"
}
