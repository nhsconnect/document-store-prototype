module "search_doc_ref_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.doc_ref_collection_resource.id
  lambda_arn     = aws_lambda_function.doc_ref_search_lambda.invoke_arn
  http_method    = "GET"
  authorization  = "CUSTOM" //TODO
  authorizer_id  = aws_api_gateway_authorizer.cis2_authoriser.id
}

module "document_reference_search_alarms" {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.doc_ref_search_lambda.function_name
  lambda_timeout             = aws_lambda_function.doc_ref_search_lambda.timeout
  lambda_short_name          = "document_reference_search_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = terraform.workspace
}

resource "aws_lambda_function" "doc_ref_search_lambda" {
  handler          = "uk.nhs.digital.docstore.handlers.DocumentReferenceSearchHandler::handleRequest"
  function_name    = "${terraform.workspace}_DocumentReferenceSearchHandler"
  runtime          = "java11"
  role             = aws_iam_role.lambda_execution_role.arn
  timeout          = 15
  memory_size      = 448
  filename         = var.lambda_jar_filename
  source_code_hash = filebase64sha256(var.lambda_jar_filename)
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]
  environment {
    variables = merge({
      AMPLIFY_BASE_URL = local.amplify_base_url
    }, local.common_environment_variables)
  }
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

locals {
  search_document_reference_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.search_doc_ref_endpoint.http_method}${aws_api_gateway_resource.doc_ref_collection_resource.path}"
}
