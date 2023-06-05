data "aws_ssm_parameter" "re_registration_sns_topic_arn" {
  name  = "/prs/${var.environment}/user-input/external/re-registration-sns-topic-arn"
  count = var.cloud_only_service_instances
}

module re_registration_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.re_registration_lambda.function_name
  lambda_timeout             = aws_lambda_function.re_registration_lambda.timeout
  lambda_short_name          = "re_registration_event_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}

resource "aws_lambda_function" "re_registration_lambda" {
  handler          = "uk.nhs.digital.docstore.lambdas.ReRegistrationEventHandler::handleRequest"
  function_name    = "ReRegistrationEventHandler"
  runtime          = "java11"
  role             = aws_iam_role.lambda_execution_role.arn
  timeout          = 15
  memory_size      = 448
  filename         = var.reregistration_event_lambda_jar_filename
  source_code_hash = filebase64sha256(var.reregistration_event_lambda_jar_filename)
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21",
    aws_lambda_layer_version.document_store_lambda_layer.arn
  ]
  environment {
    variables = merge({
      NEMS_SQS_AUDIT_QUEUE_URL = aws_sqs_queue.sensitive_nems_audit.url
    }, local.common_environment_variables)
  }
}

resource "aws_lambda_event_source_mapping" "event_source_mapping" {
  event_source_arn = aws_sqs_queue.re_registration.arn
  function_name    = aws_lambda_function.re_registration_lambda.arn
  batch_size       = 10
  function_response_types = ["ReportBatchItemFailures"]
}

resource "aws_sqs_queue" "re_registration" {
  name                    = "${var.environment}-re-registration"
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue" "re_registration_dlq" {
  name                    = "${var.environment}-re-registration-dlq"
  sqs_managed_sse_enabled = true
}

resource "aws_sns_topic_subscription" "subscription_to_re_registration_sns_topic" {
topic_arn = data.aws_ssm_parameter.re_registration_sns_topic_arn[0].value
protocol  = "sqs"
endpoint  = aws_sqs_queue.re_registration.arn
count     = var.cloud_only_service_instances
}

resource "aws_sqs_queue_redrive_policy" "re_registration_redrive_policy" {
  queue_url      = aws_sqs_queue.re_registration.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.re_registration_dlq.arn
    maxReceiveCount     = 5
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "re_registration_redrive_allow_policy" {
  queue_url            = aws_sqs_queue.re_registration_dlq.id
  redrive_allow_policy = jsonencode({
    sourceQueueArns   = [aws_sqs_queue.re_registration.arn]
    redrivePermission = "byQueue"
  })
}

resource "aws_sqs_queue_policy" "re_registration_queue_policy" {
  queue_url = aws_sqs_queue.re_registration.id
  policy    = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Sid" : "SendMessageToReRegistrationQueue"
        "Effect" : "Allow",
        "Principal" : {
          "Service": "sns.amazonaws.com"
        },
        "Action" : "sqs:SendMessage",
        "Resource" : aws_sqs_queue.re_registration.arn,
        "Condition" : {
          "ArnLike" : {
            "aws:SourceArn" : data.aws_ssm_parameter.re_registration_sns_topic_arn[0].value
          }
        }
      }
    ]
  })
  count = var.cloud_only_service_instances
}

resource "aws_iam_role_policy" "sqs_to_lambda_policy" {
  name = "sqs_to_lambda_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "sqs:GetQueueAttributes",
          "sqs:ListQueues",
          "sqs:ReceiveMessage",
          "sqs:GetQueueUrl",
          "sqs:SendMessage",
          "sqs:DeleteMessage"
        ],
        "Resource" : aws_sqs_queue.re_registration.arn
      }
    ]
  })
}

resource "aws_cloudwatch_metric_alarm" "re_registration_age_of_oldest_message" {
  alarm_name        = "prs_${var.environment}_re_registration_age_of_oldest_message"
  alarm_description = "Triggers when a message has been in the ${aws_sqs_queue.re_registration.name} queue for more than 10 minutes."
  namespace         = "AWS/SQS"
  dimensions        = {
    QueueName = aws_sqs_queue.re_registration.name
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

resource "aws_cloudwatch_metric_alarm" "re_registration_dlq_number_of_messages_visible" {
  alarm_name        = "prs_${var.environment}_re_registration_dlq_number_of_messages_visible"
  alarm_description = "Triggers when the number of messages visible in the ${aws_sqs_queue.re_registration_dlq.name} queue is greater than 0."
  namespace         = "AWS/SQS"
  dimensions        = {
    QueueName = aws_sqs_queue.re_registration_dlq.name
  }
  metric_name         = "ApproximateNumberOfMessagesVisible"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "1800"
  evaluation_periods  = "1"
  statistic           = "Maximum"
  actions_enabled     = "true"
  alarm_actions       = [aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [aws_sns_topic.alarm_notifications.arn]
}