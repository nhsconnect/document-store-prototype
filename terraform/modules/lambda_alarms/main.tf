variable "lambda_function_name" {
  type = string
}

variable "lambda_short_name" {
  type = string
  description = "Human readable short name for lambda in snake case"
}

variable "lambda_timeout" {
  type = number
}

variable "lambda_memory_limit" {
  type = number
}

resource "aws_cloudwatch_metric_alarm" "lambda_error" {
  alarm_name        = "prs_${var.lambda_short_name}_error"
  alarm_description = "Triggers when an error has occurred in ${var.lambda_function_name}."
  dimensions        = {
    FunctionName = var.lambda_function_name
  }
  namespace           = "AWS/Lambda"
  metric_name         = "Errors"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "0"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Sum"
}

resource "aws_cloudwatch_metric_alarm" "lambda_duration_alarm" {
  alarm_name        = "prs_${var.lambda_short_name}_duration"
  alarm_description = "Triggers when duration of ${var.lambda_function_name} exceeds 80% of timeout."
  dimensions        = {
    FunctionName = var.lambda_function_name
  }
  threshold           = var.lambda_timeout * 0.8 * 1000
  namespace           = "AWS/Lambda"
  metric_name         = "Duration"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_metric_alarm" "lambda_memory_alarm" {
  alarm_name        = "prs_${var.lambda_short_name}_memory"
  alarm_description = "Triggers when max memory usage of ${var.lambda_function_name} exceeds 80% of provisioned memory."
  depends_on        = [
    aws_cloudwatch_log_metric_filter.lambda_max_memory_used_log_metric_filter
  ]
  dimensions = {
    FunctionName = var.lambda_function_name
  }
  threshold           = var.lambda_memory_limit * 0.8
  namespace           = "AWS/Lambda"
  metric_name         = "MaxMemoryUsed"
  comparison_operator = "GreaterThanThreshold"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}

resource "aws_cloudwatch_log_metric_filter" "lambda_max_memory_used_log_metric_filter" {
  name           = "prs_${var.lambda_short_name}_max_memory_used_log_metric_filter"
  log_group_name = "/aws/lambda/${var.lambda_function_name}"
  pattern        = "[report_name=\"REPORT\", request_id_name=\"RequestId:\", request_id_value, duration_name=\"Duration:\", duration_value, duration_unit=\"ms\", billed_duration_name_1=\"Billed\", billed_duration_name_2=\"Duration:\", billed_duration_value, billed_duration_unit=\"ms\", memory_size_name_1=\"Memory\", memory_size_name_2=\"Size:\", memory_size_value, memory_size_unit=\"MB\", max_memory_used_name_1=\"Max\", max_memory_used_name_2=\"Memory\", max_memory_used_name_3=\"Used:\", max_memory_used_value, ...]"
  metric_transformation {
    name      = "MaxMemoryUsed"
    namespace = "prs/Lambda/${var.lambda_function_name}"
    value     = "$max_memory_used_value"
  }
}
