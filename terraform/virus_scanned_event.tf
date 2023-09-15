resource "aws_lambda_function" "virus_scanned_event_lambda" {
  function_name    = "${terraform.workspace}_VirusScannedEventHandler"
  count            = var.workspace_is_a_sandbox ? 0 : 1
  role             = aws_iam_role.lambda_execution_role.arn
  handler          = "uk.nhs.digital.docstore.lambdas.VirusScannedEventHandler::handleRequest"
  runtime          = "java11"
  filename         = var.virus_scanner_event_lambda_jar_filename
  source_code_hash = filebase64sha256(var.virus_scanner_event_lambda_jar_filename)
  timeout          = 15
  memory_size      = 256
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21",
    aws_lambda_layer_version.document_store_lambda_layer.arn
  ]
  environment {
    variables = {
      QUARANTINE_BUCKET_NAME = var.quarantine_bucket_name
      DYNAMODB_ENDPOINT      = var.dynamodb_endpoint
      SQS_AUDIT_QUEUE_URL    = aws_sqs_queue.sensitive_audit.url
      SQS_ENDPOINT           = var.sqs_endpoint
      WORKSPACE              = terraform.workspace
    }
  }
}

data "aws_ssm_parameter" "virus_scan_notifications_sns_topic_arn" {
  name  = "/prs/${var.environment}/virus-scan-notifications-sns-topic-arn"
}

resource "aws_sns_topic_subscription" "virus_scanned_lambda_topic_subscription" {
  endpoint  = aws_lambda_function.virus_scanned_event_lambda[0].arn
  count     = var.workspace_is_a_sandbox ? 0 : 1
  protocol  = "lambda"
  topic_arn = data.aws_ssm_parameter.virus_scan_notifications_sns_topic_arn.value
}

resource "aws_lambda_permission" "sns_permission_for_virus_scan_event" {
  statement_id  = "AllowExecutionFromSNSTopic"
  action        = "lambda:InvokeFunction"
  count         = var.workspace_is_a_sandbox ? 0 : 1
  function_name = aws_lambda_function.virus_scanned_event_lambda[0].arn
  principal     = "sns.amazonaws.com"
  source_arn    = data.aws_ssm_parameter.virus_scan_notifications_sns_topic_arn.value
}

module "virus_scanner_alarms" {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.virus_scanned_event_lambda[0].function_name
  lambda_timeout             = aws_lambda_function.virus_scanned_event_lambda[0].timeout
  lambda_short_name          = "virus_scanned_event_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = terraform.workspace
  count                      = var.workspace_is_a_sandbox ? 0 : 1
}

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
