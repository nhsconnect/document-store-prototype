module document_uploaded_event_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.document_uploaded_lambda.function_name
  lambda_timeout             = aws_lambda_function.document_uploaded_lambda.timeout
  lambda_short_name          = "document_uploaded_event_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}

resource "aws_lambda_function" "document_uploaded_lambda" {
  handler       = "uk.nhs.digital.docstore.handlers.DocumentUploadedEventHandler::handleRequest"
  function_name = "DocumentUploadedEventHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 256

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]

  environment {
    variables = local.common_environment_variables
  }
}

resource "aws_lambda_permission" "s3_permission_for_document_upload_event" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.document_uploaded_lambda.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.document_store.arn
}