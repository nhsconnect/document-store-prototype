resource "aws_sqs_queue" "sensitive_audit" {
  name                      = "${var.environment}-sensitive-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled   = true
}
