resource "aws_sqs_queue" "document-store" {
  name = "document-store-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled = true
}
