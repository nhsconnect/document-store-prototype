resource "aws_lambda_function" "search_patient_details_lambda" {
  handler       = "uk.nhs.digital.docstore.patientdetails.SearchPatientDetailsHandler::handleRequest"
  function_name = "SearchPatientDetailsHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      PDS_ADAPTOR_BASE_URL   = var.pds_adaptor_base_url
      PDS_ADAPTOR_IS_STUBBED = var.pds_adaptor_is_stubbed
      AMPLIFY_BASE_URL       = local.amplify_base_url
    }
  }
}

module "patient_details_endpoint" {
  source             = "./modules/api_gateway_endpoint"
  api_gateway_id     = aws_api_gateway_rest_api.lambda_api.id
  parent_resource_id = aws_api_gateway_resource.patient_details_resource.id
  lambda_arn         = aws_lambda_function.search_patient_details_lambda.invoke_arn
  path_part          = "{id}"
  http_method        = "GET"
}

resource "aws_api_gateway_method" "search_patient_details_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.patient_details_resource.id
  http_method   = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito_authorizer.id
}

resource "aws_api_gateway_integration" "search_patient_details_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_method.search_patient_details_method.resource_id
  http_method = aws_api_gateway_method.search_patient_details_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.search_patient_details_lambda.invoke_arn
}

resource "aws_api_gateway_method" "patient_details_preflight_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.patient_details_resource.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "patient_details_preflight_method_response" {
  rest_api_id     = aws_api_gateway_rest_api.lambda_api.id
  resource_id     = aws_api_gateway_resource.patient_details_resource.id
  http_method     = aws_api_gateway_method.patient_details_preflight_method.http_method
  status_code     = "200"
  response_models = {
    "application/json" = "Empty"
  }
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true,
    "method.response.header.Access-Control-Allow-Methods" = true,
    "method.response.header.Access-Control-Allow-Origin"  = true
  }
  depends_on = [aws_api_gateway_method.patient_details_preflight_method]
}

resource "aws_api_gateway_integration" "patient_details_preflight_integration" {
  rest_api_id       = aws_api_gateway_rest_api.lambda_api.id
  resource_id       = aws_api_gateway_resource.patient_details_resource.id
  http_method       = aws_api_gateway_method.patient_details_preflight_method.http_method
  type              = "MOCK"
  depends_on        = [aws_api_gateway_method.patient_details_preflight_method]
  request_templates = {
    "application/json" = <<EOF
{
   "statusCode" : 200
}
EOF
  }
}

resource "aws_api_gateway_integration_response" "patient_details_preflight_integration_response" {
  rest_api_id         = aws_api_gateway_rest_api.lambda_api.id
  resource_id         = aws_api_gateway_resource.patient_details_resource.id
  http_method         = aws_api_gateway_method.patient_details_preflight_method.http_method
  status_code         = aws_api_gateway_method_response.patient_details_preflight_method_response.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'",
    "method.response.header.Access-Control-Allow-Methods" = "'GET,OPTIONS,POST'",
    "method.response.header.Access-Control-Allow-Origin"  = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  }
  depends_on = [aws_api_gateway_method_response.patient_details_preflight_method_response]
}

resource "aws_api_gateway_resource" "patient_details_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "PatientDetails"
}

resource "aws_lambda_permission" "api_gateway_permission_for_search_patient_details" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.search_patient_details_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}