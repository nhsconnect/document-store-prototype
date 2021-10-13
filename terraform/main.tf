terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.61.0"
    }
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

#resource "aws_lambda_function" "hello_world_lambda" {
#  handler       = "uk.nhs.digital.docstore.HelloWorldHandler::handleRequest"
#  function_name = "HelloWorldHandler"
#  runtime       = "java11"
#  role          = aws_iam_role.lambda_execution_role.arn
#
#  filename = var.lambda_jar_filename
#
#  source_code_hash = filebase64sha256(var.lambda_jar_filename)
#}
#
#resource "aws_iam_role" "lambda_execution_role" {
#  name = "LambdaExecution"
#
#  assume_role_policy = jsonencode({
#    Version   = "2012-10-17"
#    Statement = [
#      {
#        Action    = "sts:AssumeRole"
#        Effect    = "Allow"
#        Sid       = ""
#        Principal = {
#          Service = "lambda.amazonaws.com"
#        }
#      }
#    ]
#  })
#}
#
#resource "aws_iam_role_policy_attachment" "lambda_execution_policy" {
#  role       = aws_iam_role.lambda_execution_role.name
#  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
#}

resource "aws_api_gateway_rest_api" "lambda_api" {
  name = "DocStoreAPI"
}

resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "{proxy+}"
}

#resource "aws_api_gateway_method" "proxy_method" {
#  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
#  resource_id   = aws_api_gateway_resource.proxy.id
#  http_method   = "ANY"
#  authorization = "NONE"
#}
#
#resource "aws_api_gateway_integration" "lambda_integration" {
#  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
#  resource_id = aws_api_gateway_method.proxy_method.resource_id
#  http_method = aws_api_gateway_method.proxy_method.http_method
#
#  integration_http_method = "POST"
#  type                    = "AWS_PROXY"
#  uri                     = aws_lambda_function.hello_world_lambda.invoke_arn
#}
#
#
#resource "aws_api_gateway_method" "proxy_root" {
#  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
#  resource_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
#  http_method   = "ANY"
#  authorization = "NONE"
#}
#
#resource "aws_api_gateway_integration" "lambda_root_integration" {
#  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
#  resource_id = aws_api_gateway_method.proxy_root.resource_id
#  http_method = aws_api_gateway_method.proxy_root.http_method
#
#  integration_http_method = "POST"
#  type                    = "AWS_PROXY"
#  uri                     = aws_lambda_function.hello_world_lambda.invoke_arn
#}
#
#
#resource "aws_api_gateway_deployment" "api_deploy" {
#  depends_on = [
#    aws_api_gateway_integration.lambda_integration,
#    aws_api_gateway_integration.lambda_root_integration,
#  ]
#
#  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
#  stage_name  = var.api_gateway_stage
#}
#
#
#resource "aws_lambda_permission" "api_gateway" {
#  statement_id  = "AllowAPIGatewayInvoke"
#  action        = "lambda:InvokeFunction"
#  function_name = aws_lambda_function.hello_world_lambda.arn
#  principal     = "apigateway.amazonaws.com"
#
#  # The "/*/*" portion grants access from any method on any resource
#  # within the API Gateway REST API.
#  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
#}


#output "base_url" {
#  value = aws_api_gateway_deployment.api_deploy.invoke_url
#}