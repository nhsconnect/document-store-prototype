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

resource "aws_cloudwatch_metric_alarm" "doc_store_api_5xx_error" {
  alarm_name        = "prs_${var.environment}_doc_store_api_5xx_error"
  alarm_description = "Triggers when a 5xx status code has been returned by the DocStoreAPI."
  namespace         = "AWS/ApiGateway"
  dimensions        = {
    ApiName = aws_api_gateway_rest_api.lambda_api.name
  }
  metric_name         = "5XXError"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
  actions_enabled     = "true"
  alarm_actions       = [aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [aws_sns_topic.alarm_notifications.arn]
}

module create_document_reference_alarms {
  source               = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.create_doc_ref_lambda.function_name
  lambda_timeout       = aws_lambda_function.create_doc_ref_lambda.timeout
  lambda_short_name    = "create_document_reference_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

module authoriser_alarms {
  source               = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.authoriser.function_name
  lambda_timeout = aws_lambda_function.authoriser.timeout
  lambda_short_name = "authoriser"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

module search_patient_details_alarms {
  source = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.search_patient_details_lambda.function_name
  lambda_timeout = aws_lambda_function.search_patient_details_lambda.timeout
  lambda_short_name = "search_patient_details_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

module document_uploaded_event_alarms {
  source = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.document_uploaded_lambda.function_name
  lambda_timeout = aws_lambda_function.document_uploaded_lambda.timeout
  lambda_short_name = "document_uploaded_event_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

module create_document_manifest_by_nhs_number_alarms {
  source = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.document_manifest_lambda.function_name
  lambda_timeout = aws_lambda_function.document_manifest_lambda.timeout
  lambda_short_name = "create_document_manifest_by_nhs_number_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

module document_reference_search_alarms {
  source = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.doc_ref_search_lambda.function_name
  lambda_timeout = aws_lambda_function.doc_ref_search_lambda.timeout
  lambda_short_name = "document_reference_search_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

module delete_document_reference_alarms {
  source = "./modules/lambda_alarms"
  lambda_function_name = aws_lambda_function.delete_doc_ref_lambda.function_name
  lambda_timeout = aws_lambda_function.delete_doc_ref_lambda.timeout
  lambda_short_name = "delete_document_reference_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
}

resource "aws_sns_topic" "alarm_notifications" {
  name   = "alarms-notifications-topic"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Principal" : {
          "Service" : "cloudwatch.amazonaws.com"
        },
        "Action" : "SNS:Publish",
        "Condition" : {
          "ArnLike" : {
            "aws:SourceArn" : "arn:aws:cloudwatch:${var.region}:${var.account_id}:alarm:*"
          }
        }
        "Resource": "*"
      }
    ]
  })
}

resource "aws_kms_key" "alarm_notification_encryption_key" {
  description = "Custom KMS Key to enable server side encryption for alarm notifications"
  policy      = data.aws_iam_policy_document.alarm_notification_kms_key_policy_doc.json
  enable_key_rotation = true
}

resource "aws_kms_alias" "alarm_notification_encryption_key_alias" {
  name          = "alias/alarm-notification-encryption-kms-key"
  target_key_id = aws_kms_key.alarm_notification_encryption_key.id
}

data "aws_iam_policy_document" "alarm_notification_kms_key_policy_doc" {
  statement {
    effect = "Allow"
    principals {
      identifiers = ["arn:aws:iam::${var.account_id}:root"]
      type        = "AWS"
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    principals {
      identifiers = ["cloudwatch.amazonaws.com"]
      type        = "Service"
    }
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]
    resources = ["*"]
  }
}


