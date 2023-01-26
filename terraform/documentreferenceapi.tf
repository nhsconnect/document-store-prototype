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

module "delete_doc_ref_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  lambda_arn     = aws_lambda_function.delete_doc_ref_lambda.invoke_arn
  http_method    = "DELETE"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "doc_ref_collection_preflight" {
  source         = "./modules/api_gateway_preflight"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  origin         = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods        = "'GET,OPTIONS,POST,DELETE'"
}

resource "aws_lambda_function" "create_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.handlers.CreateDocumentReferenceHandler::handleRequest"
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

resource "aws_lambda_function" "delete_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.handlers.DeleteDocumentReferenceHandler::handleRequest"
  function_name = "DeleteDocumentReferenceHandler"
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
  handler       = "uk.nhs.digital.docstore.handlers.DocumentReferenceSearchHandler::handleRequest"
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

resource "aws_lambda_permission" "api_gateway_permission_for_delete_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.delete_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"
  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

locals {
  search_document_reference_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.search_doc_ref_endpoint.http_method}${aws_api_gateway_resource.doc_ref_collection_resource.path}"
  create_document_reference_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.create_doc_ref_endpoint.http_method}${aws_api_gateway_resource.doc_ref_collection_resource.path}"
  delete_document_reference_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.delete_doc_ref_endpoint.http_method}${aws_api_gateway_resource.doc_ref_collection_resource.path}"
  common_environment_variables             = {
    DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
    DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
    S3_ENDPOINT                = var.s3_endpoint
    S3_USE_PATH_STYLE          = var.s3_use_path_style
    SQS_ENDPOINT               = var.sqs_endpoint
    SQS_QUEUE_URL              = aws_sqs_queue.sensitive_audit.url
  }
  amplify_base_url = var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : ""
}
