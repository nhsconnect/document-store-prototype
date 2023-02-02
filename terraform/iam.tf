data "aws_iam_policy_document" "splunk_trust_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "AWS"
      identifiers = var.cloud_only_service_instances > 0 ? split(",", data.aws_ssm_parameter.splunk_trusted_principal[0].value) : []
    }
  }
}

data "aws_iam_policy_document" "splunk_access_policy_document" {
  statement {
    effect  = "Allow"
    actions = [
      "sqs:GetQueueAttributes",
      "sqs:ListQueues",
      "sqs:ReceiveMessage",
      "sqs:GetQueueUrl",
      "sqs:SendMessage",
      "sqs:DeleteMessage"
    ]
    resources = ["arn:aws:sqs:eu-west-2:*:*-sensitive-audit"]
  }
  statement {
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "sensitive_audit" {
  name   = "send_audit_messages_policy"
  role   = aws_iam_role.lambda_execution_role.id
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Sid" : "PublishMessages"
        "Effect" : "Allow",
        "Action" : [
          "sqs:SendMessage",
          "sqs:GetQueueUrl"
        ],
        "Resource" : aws_sqs_queue.sensitive_audit.arn
      }
    ]
  })
}

resource "aws_iam_role" "splunk_sqs_forwarder" {
  name               = "SplunkSqsForwarder"
  description        = "Role to allow ARF to integrate with Splunk"
  assume_role_policy = data.aws_iam_policy_document.splunk_trust_policy.json
}

resource "aws_iam_policy" "splunk_access_policy" {
  name   = "splunk_access_policy"
  policy = data.aws_iam_policy_document.splunk_access_policy_document.json
}

resource "aws_iam_role_policy_attachment" "splunk_access_policy_attachment" {
  role       = aws_iam_role.splunk_sqs_forwarder.name
  policy_arn = aws_iam_policy.splunk_access_policy.arn
}

data "aws_iam_policy_document" "authoriser_execution_access_policy_document" {
  statement {
    effect    = "Allow"
    actions   = ["lambda:InvokeFunction"]
    resources = ["*"]
  }
  statement {
    effect  = "Allow"
    actions = [
      "logs:CreateLogGroup",
    ]
    resources = ["*"]
  }
  statement {
    effect  = "Allow"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = ["arn:aws:logs:*:*:log-group:/aws/lambda-insights:*"]
  }
}

data "aws_iam_policy_document" "authoriser_trust_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      identifiers = ["lambda.amazonaws.com", "apigateway.amazonaws.com"]
      type        = "Service"
    }
  }
}

resource "aws_iam_role" "authoriser_execution" {
  name               = "AuthoriserExecution"
  description        = "Role to allow authoriser to execute"
  assume_role_policy = data.aws_iam_policy_document.authoriser_trust_policy.json
}

resource "aws_iam_policy" "authoriser_access_policy" {
  name   = "authoriser_access_policy"
  policy = data.aws_iam_policy_document.authoriser_execution_access_policy_document.json
}

resource "aws_iam_role_policy_attachment" "authoriser_execution_access_policy_attachment" {
  role       = aws_iam_role.authoriser_execution.name
  policy_arn = aws_iam_policy.authoriser_access_policy.arn
}

