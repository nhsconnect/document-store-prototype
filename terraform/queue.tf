resource "aws_sqs_queue" "document-store" {
  name = "document-store-audit"
}
