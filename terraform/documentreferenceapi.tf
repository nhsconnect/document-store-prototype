locals {
  common_environment_variables = {
    DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
    DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
    S3_ENDPOINT                = var.s3_endpoint
    S3_USE_PATH_STYLE          = var.s3_use_path_style
    SQS_ENDPOINT               = var.sqs_endpoint
  }
  amplify_base_url = var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : ""
}

resource "aws_lambda_function" "get_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.RetrieveDocumentReferenceHandler::handleRequest"
  function_name = "RetrieveDocumentReferenceHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 25
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = merge({
      AMPLIFY_BASE_URL = local.amplify_base_url
    }, local.common_environment_variables)
  }
}

resource "aws_lambda_function" "create_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.CreateDocumentReferenceHandler::handleRequest"
  function_name = "CreateDocumentReferenceHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = merge({
      AMPLIFY_BASE_URL = local.amplify_base_url
    }, local.common_environment_variables)
  }
}

resource "aws_lambda_function" "doc_ref_search_lambda" {
  handler       = "uk.nhs.digital.docstore.search.DocumentReferenceSearchHandler::handleRequest"
  function_name = "DocumentReferenceSearchHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = merge({
      AMPLIFY_BASE_URL = local.amplify_base_url
    }, local.common_environment_variables)
  }
}

resource "aws_api_gateway_resource" "doc_ref_collection_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "DocumentReference"
}

resource "aws_api_gateway_resource" "get_doc_ref_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_resource.doc_ref_collection_resource.id
  path_part   = "{id}"
}

module "get_doc_ref_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.get_doc_ref_resource.id
  lambda_arn     = aws_lambda_function.get_doc_ref_lambda.invoke_arn
  http_method    = "GET"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "create_doc_ref_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  lambda_arn     = aws_lambda_function.create_doc_ref_lambda.invoke_arn
  http_method    = "POST"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "search_doc_ref_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  lambda_arn     = aws_lambda_function.doc_ref_search_lambda.invoke_arn
  http_method    = "GET"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "doc_ref_collection_preflight" {
  source         = "./modules/api_gateway_preflight"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  origin         = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods        = "'GET,OPTIONS,POST'"
}

module "get_doc_ref_preflight" {
  source         = "./modules/api_gateway_preflight"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.get_doc_ref_resource.id
  origin         = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods        = "'GET,OPTIONS,POST'"
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

resource "aws_lambda_permission" "api_gateway_permission_for_create_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.create_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_doc_ref_search" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.doc_ref_search_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}
