module "document_manifest_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.document_manifest_resource.id
  lambda_arn     = aws_lambda_function.document_manifest_lambda.invoke_arn
  http_method    = "GET"
  authorization  = "CUSTOM" //TODO
authorizer_id  = aws_api_gateway_authorizer.cis2_authoriser.id
}

module "document_manifest_preflight" {
  source         = "./modules/api_gateway_preflight"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.document_manifest_resource.id
  origin         = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods        = "'GET,OPTIONS,POST'"
}

resource "aws_lambda_function" "document_manifest_lambda" {
  handler          = "uk.nhs.digital.docstore.handlers.CreateDocumentManifestByNhsNumberHandler::handleRequest"
  function_name    = "CreateDocumentManifestByNhsNumberHandler"
  runtime          = "java11"
  role             = aws_iam_role.lambda_execution_role.arn
  timeout          = 60
  memory_size      = 1000
  filename         = var.lambda_CreateDocumentManifestByNhsNumberHandler_jar_filename
  source_code_hash = filebase64sha256(var.lambda_CreateDocumentManifestByNhsNumberHandler_jar_filename)
  layers           = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21",
    aws_lambda_layer_version.lambda_document_store_layer.arn
  ]
  environment {
    variables = merge({
      AMPLIFY_BASE_URL               = local.amplify_base_url,
      DOCUMENT_ZIP_TRACE_TTL_IN_DAYS = var.document_zip_trace_ttl_in_days,
    }, local.common_environment_variables)
  }
}

resource "aws_lambda_layer_version" "lambda_document_store_layer" {
  filename   = var.lambda_jar_filename
  layer_name = "document_store_layer"

  compatible_runtimes = ["java11"]
}


resource "aws_api_gateway_resource" "document_manifest_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "DocumentManifest"
}

resource "aws_lambda_permission" "api_gateway_permission_for_document_manifest" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.document_manifest_lambda.arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

module create_document_manifest_by_nhs_number_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.document_manifest_lambda.function_name
  lambda_timeout             = aws_lambda_function.document_manifest_lambda.timeout
  lambda_short_name          = "create_document_manifest_by_nhs_number_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}

resource "aws_dynamodb_table" "doc_zip_trace_store" {
  name           = "DocumentZipTrace"
  hash_key       = "ID"
  billing_mode   = "PAY_PER_REQUEST"
  stream_enabled = false

  attribute {
    name = "ID"
    type = "S"
  }

  ttl {
    attribute_name = "ExpiryDate"
    enabled        = true
  }
}

locals {
  get_document_manifest_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.document_manifest_endpoint.http_method}${aws_api_gateway_resource.document_manifest_resource.path}"
}
