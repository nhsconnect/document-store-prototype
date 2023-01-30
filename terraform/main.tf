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
    lambda     = var.aws_endpoint
    s3         = var.aws_endpoint
    sns        = var.aws_endpoint
    sqs        = var.aws_endpoint
    logs       = var.aws_endpoint
  }
}

resource "aws_lambda_function" "document_uploaded_lambda" {
  handler       = "uk.nhs.digital.docstore.handlers.DocumentUploadedEventHandler::handleRequest"
  function_name = "DocumentUploadedEventHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 256

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]

  environment {
    variables = local.common_environment_variables
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

resource "aws_api_gateway_authorizer" "cognito_authorizer" {
  name          = "cognito-authorizer"
  type          = "COGNITO_USER_POOLS"
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  provider_arns = var.cloud_only_service_instances > 0 ? [
    for pool_arn in aws_cognito_user_pool.pool[*].arn :pool_arn
  ] : [
    ""
  ]
  authorizer_credentials = aws_iam_role.lambda_execution_role.arn
}

resource "aws_lambda_permission" "s3_permission_for_document_upload_event" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.document_uploaded_lambda.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.document_store.arn
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

resource "aws_iam_role" "authoriser_execution_role" {
  name = "AuthoriserLambdaExecution"

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

resource "aws_iam_role_policy_attachment" "authoriser_lambda_execution_policy" {
  role       = aws_iam_role.authoriser_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "authoriser_insights_policy" {
  role       = aws_iam_role.authoriser_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLambdaInsightsExecutionRolePolicy"
}

resource "aws_lambda_function" "authoriser" {
  handler       = "uk.nhs.digital.docstore.authoriser.Authoriser::handleRequest"
  function_name = "Authoriser"
  runtime       = "java11"
  role          = aws_iam_role.authoriser_execution_role.arn

  timeout     = 15
  memory_size = 256

  filename = var.authoriser_lambda_jar_filename

  source_code_hash = filebase64sha256(var.authoriser_lambda_jar_filename)
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]

  environment {
    variables = {
      AUTH_CONFIG = jsonencode({
        allowedResourcesForPCSEUsers = [
          local.search_patient_details_invocation_arn,
          local.search_document_reference_invocation_arn,
          local.get_document_manifest_invocation_arn,
          local.delete_document_reference_invocation_arn
        ],
        allowedResourcesForClinicalUsers = [
          local.search_patient_details_invocation_arn,
          local.create_document_reference_invocation_arn,
        ]
      })
      COGNITO_PUBLIC_KEY_URL = var.cloud_only_service_instances > 0 ? "https://cognito-idp.${var.region}.amazonaws.com/${aws_cognito_user_pool.pool[0].id}" : ""
      COGNITO_KEY_ID         = var.cognito_key_id
    }
  }
}
