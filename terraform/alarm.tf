resource "aws_cloudwatch_metric_alarm" "sensitive_index_age_of_oldest_message" {
  alarm_name        = "prs-${var.environment}-sensitive-index-age-of-oldest-message"
  alarm_description = "Triggers when a message has been in the sensitive-index SQS queue for more than 10 minutes."
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
  alarm_name        = "prs-${var.environment}-doc-store-api-5xx-error"
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
  for_each = {
    "authoriser"                                     = aws_lambda_function.authoriser.function_name
    "search_patient-details-handler"                 = aws_lambda_function.search_patient_details_lambda.function_name
    "create-document-reference-handler"              = aws_lambda_function.create_doc_ref_lambda.function_name
    "document-uploaded-event-handler"                = aws_lambda_function.document_uploaded_lambda.function_name
    "create-document-manifest-by-nhs-number-handler" = aws_lambda_function.document_manifest_lambda.function_name
    "document-reference-search-handler"              = aws_lambda_function.doc_ref_search_lambda.function_name
    "retrieve-document-reference-handler"            = aws_lambda_function.get_doc_ref_lambda.function_name
    "delete-document-reference-handler"              = aws_lambda_function.delete_doc_ref_lambda.function_name
  }
  alarm_name        = "prs-${var.environment}-${each.key}-error"
  alarm_description = "Triggers when an error has occurred in ${each.value}."
  dimensions        = {
    FunctionName = each.value
  }
  namespace           = "AWS/Lambda"
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}

resource "aws_cloudwatch_metric_alarm" "authoriser_duration" {
  alarm_name        = "prs-${var.environment}-authoriser-duration"
  alarm_description = "Triggers when duration of Authoriser Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.authoriser.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "12"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "search_patient_details_handler_duration" {
  alarm_name        = "prs-${var.environment}-search-patient-details-handler-duration"
  alarm_description = "Triggers when duration of SearchPatientDetailsHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.search_patient_details_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "12"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "create_document_reference_handler_duration" {
  alarm_name        = "prs-${var.environment}-create-document-reference-handler-duration"
  alarm_description = "Triggers when duration of CreateDocumentReferenceHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.create_doc_ref_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "12"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "document_uploaded_event_handler_duration" {
  alarm_name        = "prs-${var.environment}-document-uploaded-event-handler-duration"
  alarm_description = "Triggers when duration of DocumentUploadedEventHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.document_uploaded_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "12"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "create_document_manifest_by_nhs_number_handler_duration" {
  alarm_name        = "prs-${var.environment}-create-document-manifest-by-nhs-number-handler-duration"
  alarm_description = "Triggers when duration of CreateDocumentManifestByNhsNumberHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.document_manifest_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "48"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "document_reference_search_handler_duration" {
  alarm_name        = "prs-${var.environment}-document-reference-search-handler-duration"
  alarm_description = "Triggers when duration of DocumentReferenceSearchHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.doc_ref_search_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "12"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "retrieve_document_reference_handler_duration" {
  alarm_name        = "prs-${var.environment}-retrieve-document-reference-handler-duration"
  alarm_description = "Triggers when duration of RetrieveDocumentReferenceHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.get_doc_ref_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "20"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "delete_document_reference_handler_duration" {
  alarm_name        = "prs-${var.environment}-delete-document-reference-handler-duration"
  alarm_description = "Triggers when duration of DeleteDocumentReferenceHandler Lambda exceeds 80% of timeout."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.delete_doc_ref_lambda.function_name
  }
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = "12"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}
