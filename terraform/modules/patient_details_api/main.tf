variable api_gateway_id {}
variable api_gateway_root_id {}
variable api_gateway_api_execution_arn {}
variable lambda_execution_role_arn {}
variable lambda_jar_filename {}
variable env_vars {
  type = map(any)
}

resource "aws_lambda_function" "search_patient_details_lambda" {
  handler       = "uk.nhs.digital.docstore.patientdetails.SearchPatientDetailsHandler::handleRequest"
  function_name = "SearchPatientDetailsHandler"
  runtime       = "java11"
  role          = var.lambda_execution_role_arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_api_gateway_resource" "patient_details_collection_resource" {
  rest_api_id = var.api_gateway_id
  parent_id   = var.api_gateway_root_id
  path_part   = "PatientDetails"
}

resource "aws_lambda_permission" "api_gateway_permission_for_search_patient_details" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.search_patient_details_lambda.function_name
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${var.api_gateway_api_execution_arn}/*/*"
}

output "patient_details_resource_id" {
  value = aws_api_gateway_resource.patient_details_collection_resource.id
}

output "patient_details_lambda_arn" {
  value = aws_lambda_function.search_patient_details_lambda.arn
}

output "patient_details_lambda_invocation_arn" {
  value = aws_lambda_function.search_patient_details_lambda.invoke_arn
}