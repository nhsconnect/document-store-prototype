resource "aws_cloudwatch_metric_alarm" "sensitive_index_age_of_oldest_message" {
  alarm_name        = "prs-${var.environment}-sensitive-index-age-of-oldest-message"
  alarm_description = "Triggers when a message has been in the sensitive-index SQS queue for more than 10 mins."
  namespace         = "AWS/SQS"
  dimensions        = {
    QueueName = aws_sqs_queue.sensitive_audit.name
  }
  metric_name         = "ApproximateAgeOfOldestMessage"
  comparison_operator = "GreaterThanThreshold"
  threshold           = 600
  period              = 1800
  evaluation_periods  = "1"
  statistic           = "Maximum"
}
