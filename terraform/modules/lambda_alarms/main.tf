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
  dimensions = {
    function_name = var.lambda_function_name
  }
  threshold           = var.lambda_memory_limit * 0.8
  namespace           = "LambdaInsights"
  metric_name         = "memory_utilization"
  comparison_operator = "GreaterThanThreshold"
  period              = "300"
  evaluation_periods  = "1"
  statistic           = "Maximum"
}
