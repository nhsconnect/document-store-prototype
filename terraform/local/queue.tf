resource "aws_sqs_queue" "document-store" {
  name = "document-store-audit"
  message_retention_seconds = 1209600
  sqs_managed_sse_enabled = true
}

resource "aws_iam_role_policy" "document-store" {
  name = "send_audit_messages_policy"
  role = module.lambda_iam_role.lambda_execution_role_name
  policy    = jsonencode({
    "Version": "2012-10-17",
    "Statement" : [
      {
        "Sid": "PublishMessages"
        "Effect" : "Allow",
        "Action" : [
          "sqs:SendMessage",
          "sqs:GetQueueUrl"
        ],
        "Resource" : aws_sqs_queue.document-store.arn
      }
    ]
  })
}
