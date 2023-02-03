resource "aws_sqs_queue" "sensitive_audit" {
  name                      = "${var.environment}-sensitive-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled   = true
}

resource "aws_sqs_queue" "sensitive_nems_audit" {
  name = "${var.environment}-sensitive-nems-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue" "re_registration" {
  name = "${var.environment}-re-registration"
  sqs_managed_sse_enabled = true
}
