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

resource "aws_sqs_queue" "re_registration" {
  name                    = "${var.environment}-re-registration"
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue" "re_registration_dlq" {
  name                    = "${var.environment}-re-registration-dlq"
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue_redrive_policy" "re_registration_redrive_policy" {
  queue_url      = aws_sqs_queue.re_registration.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.re_registration_dlq.arn
    maxReceiveCount     = 1
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "re_registration_redrive_allow_policy" {
  queue_url            = aws_sqs_queue.re_registration_dlq.id
  redrive_allow_policy = jsonencode({
    sourceQueueArns   = [aws_sqs_queue.re_registration.arn]
    redrivePermission = "byQueue"
  })
}
