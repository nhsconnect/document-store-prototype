resource "aws_sqs_queue" "document-store" {
  name = "document-store-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled = true
}

resource "aws_iam_role_policy" "document-store" {
  name = "send_audit_messages_policy"
  role = aws_iam_role.lambda_execution_role.id
  policy    = jsonencode({
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : ["sqs:SendMessage"],
        "Resource" : "${aws_sqs_queue.document-store.arn}/*"
      }
    ]
  })
}
