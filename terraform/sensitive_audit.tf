data "aws_ssm_parameter" "splunk_trusted_principal" {
  name  = "/prs/user-input/external/splunk-trusted-principal"
  count = var.cloud_only_service_instances
}

data "aws_iam_policy_document" "splunk_trust_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "AWS"
      identifiers = var.cloud_only_service_instances > 0 ? split(",", data.aws_ssm_parameter.splunk_trusted_principal[0].value) : []
    }
  }
}

data "aws_iam_policy_document" "splunk_access_policy_document" {
  statement {
    effect  = "Allow"
    actions = [
      "sqs:GetQueueAttributes",
      "sqs:ListQueues",
      "sqs:ReceiveMessage",
      "sqs:GetQueueUrl",
      "sqs:SendMessage",
      "sqs:DeleteMessage"
    ]
    resources = [
      aws_sqs_queue.sensitive_audit.arn,
      aws_sqs_queue.sensitive_nems_audit.arn
    ]
  }
  statement {
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["*"]
  }
}

resource "aws_sqs_queue" "sensitive_audit" {
  name                      = "${var.environment}-sensitive-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled   = true
}

resource "aws_sqs_queue" "sensitive_nems_audit" {
  name                      = "${var.environment}-sensitive-nems-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled   = true
}

resource "aws_iam_role" "splunk_sqs_forwarder" {
  name               = "SplunkSqsForwarder"
  description        = "Role to allow ARF to integrate with Splunk"
  assume_role_policy = data.aws_iam_policy_document.splunk_trust_policy.json
}

resource "aws_iam_policy" "splunk_access_policy" {
  name   = "splunk_access_policy"
  policy = data.aws_iam_policy_document.splunk_access_policy_document.json
}

resource "aws_iam_role_policy" "sqs_policy" {
  name   = "send_audit_messages_policy"
  role   = aws_iam_role.lambda_execution_role.id
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Sid" : "PublishMessages"
        "Effect" : "Allow",
        "Action" : [
          "sqs:SendMessage",
          "sqs:GetQueueUrl"
        ],
        "Resource" : [
          aws_sqs_queue.sensitive_audit.arn,
          aws_sqs_queue.sensitive_nems_audit.arn
        ]
      },
      {
        "Sid" : "ReadMessages"
        "Effect" : "Allow",
        "Action" : "sqs:ReceiveMessage",
        "Resource" : aws_sqs_queue.re_registration.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "splunk_access_policy_attachment" {
  role       = aws_iam_role.splunk_sqs_forwarder.name
  policy_arn = aws_iam_policy.splunk_access_policy.arn
}

resource "aws_cloudwatch_metric_alarm" "sensitive_index_age_of_oldest_message" {
  alarm_name        = "prs_${var.environment}_sensitive_index_age_of_oldest_message"
  alarm_description = "Triggers when a message has been in the ${aws_sqs_queue.sensitive_audit.name} queue for more than 10 minutes."
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
  actions_enabled     = "true"
  alarm_actions       = [aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_metric_alarm" "sensitive_nems_index_age_of_oldest_message" {
  alarm_name        = "prs_${var.environment}_sensitive_nems_index_age_of_oldest_message"
  alarm_description = "Triggers when a message has been in the ${aws_sqs_queue.sensitive_nems_audit.name} queue for more than 10 minutes."
  namespace         = "AWS/SQS"
  dimensions        = {
    QueueName = aws_sqs_queue.sensitive_nems_audit.name
  }
  metric_name         = "ApproximateAgeOfOldestMessage"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "600"
  period              = "1800"
  evaluation_periods  = "1"
  statistic           = "Maximum"
  actions_enabled     = "true"
  alarm_actions       = [aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [aws_sns_topic.alarm_notifications.arn]
}