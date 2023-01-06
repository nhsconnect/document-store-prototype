variable "lambda_role_arn" {}

variable "lambda_jar_filename" {}

variable "api_gateway_api_id" {}

variable "api_gateway_api_root_resource_id" {}

variable "api_gateway_api_invocation_arn" {}

variable "env_vars" {
  type = map(any)
}

resource "aws_lambda_function" "get_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.RetrieveDocumentReferenceHandler::handleRequest"
  function_name = "RetrieveDocumentReferenceHandler"
  runtime       = "java11"
  role          = var.lambda_role_arn

  timeout     = 25
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_lambda_function" "create_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.CreateDocumentReferenceHandler::handleRequest"
  function_name = "CreateDocumentReferenceHandler"
  runtime       = "java11"
  role          = var.lambda_role_arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_lambda_function" "delete_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.DeleteDocumentReferenceHandler::handleRequest"
  function_name = "DeleteDocumentReferenceHandler"
  runtime       = "java11"
  role          = var.lambda_role_arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_lambda_function" "doc_ref_search_lambda" {
  handler       = "uk.nhs.digital.docstore.search.DocumentReferenceSearchHandler::handleRequest"
  function_name = "DocumentReferenceSearchHandler"
  runtime       = "java11"
  role          = var.lambda_role_arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_api_gateway_resource" "doc_ref_collection_resource" {
  rest_api_id = var.api_gateway_api_id
  parent_id   = var.api_gateway_api_root_resource_id
  path_part   = "DocumentReference"
}

resource "aws_api_gateway_resource" "get_doc_ref_resource" {
  rest_api_id = var.api_gateway_api_id
  parent_id   = aws_api_gateway_resource.doc_ref_collection_resource.id
  path_part   = "{id}"
}

resource "aws_lambda_permission" "api_gateway_permission_for_get_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${var.api_gateway_api_invocation_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_create_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.create_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${var.api_gateway_api_invocation_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_doc_ref_search" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.doc_ref_search_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${var.api_gateway_api_invocation_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_delete_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.delete_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${var.api_gateway_api_invocation_arn}/*/*"
}

output "doc_ref_collection_resource_id" {
  value = aws_api_gateway_resource.doc_ref_collection_resource.id
}

output "get_doc_ref_resource_id" {
  value = aws_api_gateway_resource.get_doc_ref_resource.id
}