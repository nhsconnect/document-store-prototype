resource "aws_cloudwatch_metric_alarm" "sensitive_index_age_of_oldest_message" {
  alarm_name        = "prs-${var.environment}-sensitive-index-age-of-oldest-message"
  alarm_description = "Triggers when a message has been in the sensitive-index SQS queue for more than 10 mins."
  namespace         = "AWS/SQS"
  dimensions        = {
    QueueName = aws_sqs_queue.sensitive_audit.name
  }
  metric_name         = "ApproximateAgeOfOldestMessage"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "600"
  period              = "1800"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "document_uploaded_event_handler_error" {
  alarm_name        = "prs-${var.environment}-document-uploaded-event-handler-error"
  alarm_description = "Triggers when an error has occurred in DocumentUploadedEventHandler."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.document_uploaded_lambda.function_name
  }
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}
