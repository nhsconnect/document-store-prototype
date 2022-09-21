terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.75.0"
    }
  }

  backend "s3" {}
}

provider "aws" {
  profile = "default"
  region  = var.region

  skip_credentials_validation = var.disable_aws_remote_checks
  skip_requesting_account_id  = var.disable_aws_remote_checks
  s3_force_path_style         = var.disable_aws_remote_checks
  skip_metadata_api_check     = var.disable_aws_remote_checks

  endpoints {
    apigateway = var.aws_endpoint
    cloudwatch = var.aws_endpoint
    dynamodb   = var.aws_endpoint
    iam        = var.aws_endpoint
    lambda     = var.aws_endpoint
    s3         = var.aws_endpoint
  }
}

resource "aws_lambda_function" "document_uploaded_lambda" {
  handler       = "uk.nhs.digital.docstore.DocumentUploadedEventHandler::handleRequest"
  function_name = "DocumentUploadedEventHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 256

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
      DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
      S3_ENDPOINT                = var.s3_endpoint
    }
  }
}

resource "aws_iam_role" "lambda_execution_role" {
  name = "LambdaExecution"

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

resource "aws_iam_role_policy" "dynamodb_get_document_reference_policy" {
  name = "get_document_reference_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ],
        "Resource" : aws_dynamodb_table.doc_ref_store.arn
      }
    ]
  })
}

resource "aws_iam_role_policy" "dynamodb_query_locations_policy" {
  name = "dynamodb_query_locations_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Query",
        ],
        "Resource" : "${aws_dynamodb_table.doc_ref_store.arn}/index/LocationsIndex"
      },
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Query",
        ],
        "Resource" : "${aws_dynamodb_table.doc_ref_store.arn}/index/NhsNumberIndex"
      }
    ]
  })
}

resource "aws_iam_role_policy" "s3_get_document_data_policy" {
  name = "get_document_data_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "s3:GetObject",
          "s3:PutObject",
        ],
        "Resource" : "${aws_s3_bucket.document_store.arn}/*"
      }
    ]
  })
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
      module.doc_ref_endpoint,
      module.patient_details_endpoint,
      aws_api_gateway_method.create_doc_ref_method,
      aws_api_gateway_resource.doc_ref_resource,
      aws_api_gateway_resource.patient_details_resource,
      aws_api_gateway_integration.create_doc_ref_integration,
      aws_api_gateway_integration.search_patient_details_integration,
      aws_api_gateway_method.doc_ref_search_method,
      aws_api_gateway_method.search_patient_details_method,
      aws_api_gateway_integration.doc_ref_search_integration,
      aws_api_gateway_authorizer.cognito_authorizer,
      aws_api_gateway_integration.create_and_search_doc_preflight_integration,
      aws_api_gateway_method.create_and_search_doc_preflight_method,
      aws_api_gateway_integration.patient_details_preflight_integration,
      aws_api_gateway_method.patient_details_preflight_method,
    ]))
  }
}

resource "aws_api_gateway_authorizer" "cognito_authorizer" {
  name        = "cognito-authorizer"
  type        = "COGNITO_USER_POOLS"
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  provider_arns = var.cloud_only_service_instances > 0 ? [for pool_arn in aws_cognito_user_pool.pool[*].arn : pool_arn] : [
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
