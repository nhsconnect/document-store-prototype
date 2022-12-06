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
      PDS_FHIR_ENDPOINT = data.aws_ssm_parameter.pds_fhir_endpoint.value
      PDS_ADAPTOR_IS_STUBBED = var.pds_adaptor_is_stubbed
      AMPLIFY_BASE_URL       = local.amplify_base_url
    }
  }
}

resource "aws_api_gateway_resource" "patient_details_collection_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "PatientDetails"
}

module "patient_details_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.patient_details_collection_resource.id
  lambda_arn     = aws_lambda_function.search_patient_details_lambda.invoke_arn
  http_method    = "GET"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "patient_details_collection_preflight" {
  source             = "./modules/api_gateway_preflight"
  api_gateway_id     = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.patient_details_collection_resource.id
  origin = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods = "'GET,OPTIONS,POST'"
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