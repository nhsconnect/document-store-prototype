variable "lambda_execution_role_arn" {}
variable "lambda_jar_filename" {}
variable "s3_bucket_arn" {}
variable "env_vars" {
  type = map(any)
}

resource "aws_lambda_function" "document_uploaded_lambda" {
  handler       = "uk.nhs.digital.docstore.DocumentUploadedEventHandler::handleRequest"
  function_name = "DocumentUploadedEventHandler"
  runtime       = "java11"
  role          = var.lambda_execution_role_arn

  timeout     = 15
  memory_size = 256

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = var.env_vars
  }
}

resource "aws_lambda_permission" "s3_permission_for_document_upload_event" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.document_uploaded_lambda.arn
  principal     = "s3.amazonaws.com"
  source_arn    = var.s3_bucket_arn
}

output lambda_arn {
  value = aws_lambda_function.document_uploaded_lambda.arn
}