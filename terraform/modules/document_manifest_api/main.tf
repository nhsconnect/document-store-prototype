variable api_gateway_id {}
variable api_gateway_root_id {}
variable api_gateway_api_execution_arn {}
variable lambda_execution_role_arn {}
variable lambda_jar_filename {}
variable env_vars {
  type = map(any)
}

resource "aws_lambda_function" "document_manifest_lambda" {
  handler       = "uk.nhs.digital.docstore.documentmanifest.CreateDocumentManifestByNhsNumberHandler::handleRequest"
  function_name = "CreateDocumentManifestByNhsNumberHandler"
  runtime       = "java11"
  role          = var.lambda_execution_role_arn

  timeout     = 60
  memory_size = 1000

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_api_gateway_resource" "document_manifest_resource" {
  rest_api_id = var.api_gateway_id
  parent_id   = var.api_gateway_root_id
  path_part   = "DocumentManifest"
}

resource "aws_lambda_permission" "api_gateway_permission_for_document_manifest" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.document_manifest_lambda.function_name
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${var.api_gateway_api_execution_arn}/*/*"
}

output "document_manifest_resource_id" {
  value = aws_api_gateway_resource.document_manifest_resource.id
}

output "document_manifest_lambda_invocation_arn" {
  value = aws_lambda_function.document_manifest_lambda.invoke_arn
}

output document_manifest_lambda_arn {
  value = aws_lambda_function.document_manifest_lambda.arn
}