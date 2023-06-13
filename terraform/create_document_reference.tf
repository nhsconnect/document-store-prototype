module "create_doc_ref_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  lambda_arn     = aws_lambda_function.create_doc_ref_lambda.invoke_arn
  http_method    = "POST"
  authorization  = "CUSTOM" //TODO
authorizer_id  = aws_api_gateway_authorizer.cis2_authoriser.id
}

module create_document_reference_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.create_doc_ref_lambda.function_name
  lambda_timeout             = aws_lambda_function.create_doc_ref_lambda.timeout
  lambda_short_name          = "create_document_reference_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}

resource "aws_lambda_function" "create_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.lambdas.CreateDocumentReferenceHandler::handleRequest"
  function_name = "CreateDocumentReferenceHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn
  timeout     = 15
  memory_size = 448
  filename = var.create_doc_ref_lambda_jar_filename
  source_code_hash = filebase64sha256(var.create_doc_ref_lambda_jar_filename)
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21",
    aws_lambda_layer_version.document_store_lambda_layer.arn
  ]
  environment {
    variables = merge({
      AMPLIFY_BASE_URL = local.amplify_base_url
      TEST_DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.test_document_store.bucket
      VIRUS_SCANNER_IS_STUBBED  = var.virus_scanner_is_stubbed
    }, local.common_environment_variables)
  }
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

locals {
  create_document_reference_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.create_doc_ref_endpoint.http_method}${aws_api_gateway_resource.doc_ref_collection_resource.path}"
}
