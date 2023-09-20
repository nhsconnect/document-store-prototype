module "fake_virus_scanned_event_alarms" {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.fake_virus_scanned_event_lambda.function_name
  lambda_timeout             = aws_lambda_function.fake_virus_scanned_event_lambda.timeout
  lambda_short_name          = "fake_virus_scanned_event_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = terraform.workspace
}

resource "aws_lambda_function" "fake_virus_scanned_event_lambda" {
  handler       = "uk.nhs.digital.docstore.lambdas.FakeVirusScannedEventHandler::handleRequest"
  function_name = "${terraform.workspace}_FakeVirusScannedEventHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn
  timeout     = 15
  memory_size = 256
  filename = var.fake_virus_scanner_event_lambda_jar_filename
  source_code_hash = filebase64sha256(var.fake_virus_scanner_event_lambda_jar_filename)

  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21",
    aws_lambda_layer_version.document_store_lambda_layer.arn
  ]

  environment {
    variables = local.common_environment_variables
  }
}

resource "aws_lambda_permission" "s3_permission_for_fake_virus_scanned_event" {
  statement_id  = "AllowFakeScanExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.fake_virus_scanned_event_lambda.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.test_document_store.arn
}

resource "aws_lambda_permission" "s3_permission_for_virus_scanned_event" {
  statement_id  = "AllowScanExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.fake_virus_scanned_event_lambda.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.document_store[0].arn
}
