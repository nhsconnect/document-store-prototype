terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.61.0"
    }
  }

  backend "s3" {
    bucket  = "document-store-terraform-state"
    key     = "document-store/terraform.tfstate"
    region  = "eu-west-2"
    encrypt = true
  }
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

resource "aws_lambda_function" "hello_world_lambda" {
  handler       = "uk.nhs.digital.docstore.HelloWorldHandler::handleRequest"
  function_name = "HelloWorldHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)
}

resource "aws_lambda_function" "get_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.RetrieveDocumentReferenceHandler::handleRequest"
  function_name = "RetrieveDocumentReferenceHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

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
        ],
        "Resource" : aws_dynamodb_table.doc_ref_store.arn
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
        ],
        "Resource" : aws_s3_bucket.document_store.arn
      }
    ]
  })
}

resource "aws_api_gateway_rest_api" "lambda_api" {
  name = "DocStoreAPI"
}

module "hello_endpoint" {
  source             = "./modules/api_gateway_endpoint"
  api_gateway_id     = aws_api_gateway_rest_api.lambda_api.id
  parent_resource_id = aws_api_gateway_rest_api.lambda_api.root_resource_id
  lambda_arn         = aws_lambda_function.hello_world_lambda.invoke_arn
  path_part          = "hello"
  http_method        = "GET"
}

module "doc_ref_endpoint" {
  source             = "./modules/api_gateway_endpoint"
  api_gateway_id     = aws_api_gateway_rest_api.lambda_api.id
  parent_resource_id = aws_api_gateway_resource.doc_ref_resource.id
  lambda_arn         = aws_lambda_function.get_doc_ref_lambda.invoke_arn
  path_part          = "{id}"
  http_method        = "GET"
}

resource "aws_api_gateway_resource" "doc_ref_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "DocumentReference"
}

resource "aws_api_gateway_deployment" "api_deploy" {
  depends_on = [
    module.doc_ref_endpoint,
    module.hello_endpoint,
  ]

  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  stage_name  = var.api_gateway_stage

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_rest_api.lambda_api.body,
      module.doc_ref_endpoint,
      module.hello_endpoint
    ]))
  }
}

resource "aws_lambda_permission" "api_gateway_permission_for_hello" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.hello_world_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_get_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}
