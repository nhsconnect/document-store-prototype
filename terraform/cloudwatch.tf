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
}

resource "aws_cloudwatch_metric_alarm" "lambda_error" {
  for_each          = local.lambdas
  alarm_name        = "prs_${var.environment}_${each.key}_error"
  alarm_description = "Triggers when an error has occurred in ${each.value.function_name}."
  dimensions        = {
    FunctionName = each.value.function_name
  }
  namespace           = "AWS/Lambda"
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}

resource "aws_cloudwatch_metric_alarm" "lambda_duration" {
  for_each          = local.lambdas
  alarm_name        = "prs_${var.environment}_${each.key}_duration"
  alarm_description = "Triggers when duration of ${each.value.function_name} exceeds 80% of timeout."
  dimensions        = {
    FunctionName = each.value.function_name
  }
  threshold           = each.value.timeout * 0.8
  namespace           = "AWS/Lambda"
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_log_metric_filter" "authoriser_max_memory_used_log_metric_filter" {
  name           = "prs_${var.environment}_authoriser_max_memory_used_log_metric_filter"
  log_group_name = "/aws/lambda/${local.lambdas.authoriser.function_name}"
  pattern        = "[report_name=\"REPORT\", request_id_name=\"RequestId:\", request_id_value, duration_name=\"Duration:\", duration_value, duration_unit=\"ms\", billed_duration_name_1=\"Billed\", billed_duration_name_2=\"Duration:\", billed_duration_value, billed_duration_unit=\"ms\", memory_size_name_1=\"Memory\", memory_size_name_2=\"Size:\", memory_size_value, memory_size_unit=\"MB\", max_memory_used_name_1=\"Max\", max_memory_used_name_2=\"Memory\", max_memory_used_name_3=\"Used:\", max_memory_used_value, max_memory_used_unit=\"MB\", init_duration_name_1=\"Init\", init_duration_name_2=\"Duration:\", init_duration_value, init_duration_unit=\"ms\"]"
  metric_transformation {
    name      = "MaxMemoryUsed"
    namespace = "Lambda/${local.lambdas.authoriser.function_name}"
    value     = "$max_memory_used_value"
  }
  count = var.cloud_only_service_instances
}

locals {
  lambdas = {
    authoriser = {
      function_name = aws_lambda_function.authoriser.function_name
      timeout       = aws_lambda_function.authoriser.timeout
    }
    search_patient_details_handler = {
      function_name = aws_lambda_function.search_patient_details_lambda.function_name
      timeout       = aws_lambda_function.search_patient_details_lambda.timeout
    }
    create_document_reference_handler = {
      function_name = aws_lambda_function.create_doc_ref_lambda.function_name
      timeout       = aws_lambda_function.create_doc_ref_lambda.timeout
    }
    document_uploaded_event_handler = {
      function_name = aws_lambda_function.document_uploaded_lambda.function_name
      timeout       = aws_lambda_function.document_uploaded_lambda.timeout
    }
    create_document_manifest_by_nhs_number_handler = {
      function_name = aws_lambda_function.document_manifest_lambda.function_name
      timeout       = aws_lambda_function.document_manifest_lambda.timeout
    }
    document_reference_search_handler = {
      function_name = aws_lambda_function.doc_ref_search_lambda.function_name
      timeout       = aws_lambda_function.doc_ref_search_lambda.timeout
    }
    delete_document_reference_handler = {
      function_name = aws_lambda_function.delete_doc_ref_lambda.function_name
      timeout       = aws_lambda_function.delete_doc_ref_lambda.timeout
    }
  }
}
