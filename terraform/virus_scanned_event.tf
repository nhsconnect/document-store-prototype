resource "aws_lambda_function" "virus_scanned_event_lambda" {
  function_name    = "VirusScannedEventHandler"
  role             = aws_iam_role.lambda_execution_role.arn
  handler          = "uk.nhs.digital.docstore.handlers.VirusScannedEventHandler::handleRequest"
  runtime          = "java11"
  filename         = var.lambda_jar_filename
  source_code_hash = filebase64sha256(var.lambda_jar_filename)
  timeout = 15
  memory_size = 256
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]
  environment {
    variables = {
      QUARANTINE_BUCKET_NAME = var.quarantine_bucket_name
      DYNAMODB_ENDPOINT = var.dynamodb_endpoint
      SQS_AUDIT_QUEUE_URL = aws_sqs_queue.sensitive_audit.url
      SQS_ENDPOINT = var.sqs_endpoint
    }
  }
}

data "aws_ssm_parameter" "virus_scan_notifications_sns_topic_arn" {
  name = "/prs/${var.environment}/virus-scan-notifications-sns-topic-arn"
  count = var.environment == "dev" ? 1 : 0
}

resource "aws_sns_topic_subscription" "virus_scanned_lambda_topic_subscription" {
  endpoint  = aws_lambda_function.virus_scanned_event_lambda.arn
  protocol  = "lambda"
  topic_arn = data.aws_ssm_parameter.virus_scan_notifications_sns_topic_arn[0].value
  count = var.environment == "dev" ? 1 : 0
}

resource "aws_lambda_permission" "sns_permission_for_virus_scan_event" {
  statement_id  = "AllowExecutionFromSNSTopic"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.virus_scanned_event_lambda.arn
  principal     = "sns.amazonaws.com"
  source_arn    = data.aws_ssm_parameter.virus_scan_notifications_sns_topic_arn[0].value
  count         = var.environment == "dev" ? 1 : 0
}

module virus_scanner_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.virus_scanned_event_lambda.function_name
  lambda_timeout             = aws_lambda_function.virus_scanned_event_lambda.timeout
  lambda_short_name          = "virus_scanned_event_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}