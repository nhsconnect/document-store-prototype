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

resource "aws_cloudwatch_metric_alarm" "search_patient_details_handler_error" {
  alarm_name        = "prs-${var.environment}-search-patient-details-handler-error"
  alarm_description = "Triggers when an error has occurred in SearchPatientDetailsHandler."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.search_patient_details_lambda.function_name
  }
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
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

resource "aws_cloudwatch_metric_alarm" "create_document_manifest_by_nhs_number_handler_error" {
  alarm_name        = "prs-${var.environment}-create_document_manifest_by_nhs_number_handler_error"
  alarm_description = "Triggers when an error has occurred in CreateDocumentManifestByNhsNumberHandler."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.create_doc_ref_lambda.function_name
  }
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}

resource "aws_cloudwatch_metric_alarm" "retrieve_document_reference_handler_error" {
  alarm_name        = "prs-${var.environment}-retrieve_document_reference_handler_error"
  alarm_description = "Triggers when an error has occurred in RetrieveDocumentReferenceHandler."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.get_doc_ref_lambda.function_name
  }
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}

resource "aws_cloudwatch_metric_alarm" "delete_document_reference_handler_error" {
  alarm_name        = "prs-${var.environment}-delete_document_reference_handler_error"
  alarm_description = "Triggers when an error has occurred in DeleteDocumentReferenceHandler."
  namespace         = "AWS/Lambda"
  dimensions        = {
    FunctionName = aws_lambda_function.delete_doc_ref_lambda.function_name
  }
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}
