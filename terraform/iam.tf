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
    resources = [
      aws_sqs_queue.sensitive_audit.arn,
      aws_sqs_queue.sensitive_nems_audit.arn
    ]
  }
  statement {
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "sqs_policy" {
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
        "Resource" : [
          aws_sqs_queue.sensitive_audit.arn,
          aws_sqs_queue.sensitive_nems_audit.arn
        ]
      },
      {
        "Sid" : "ReadMessages"
        "Effect" : "Allow",
        "Action" : "sqs:ReceiveMessage",
        "Resource" : aws_sqs_queue.re_registration.arn
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

resource aws_iam_role "integration_test_runner" {
  name               = "IntegrationTestRunner"
  description        = "Role for running integration tests against AWS resources"
  assume_role_policy = data.aws_iam_policy_document.integration_test_runner_trust_policy.json
}

data "aws_iam_policy_document" "integration_test_runner_trust_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    effect = "Allow"
    principals {
      identifiers = ["arn:aws:iam::327778747031:role/gocd_agent-prod"]
      type        = "AWS"
    }
  }
}

resource aws_iam_policy "dynamodb_table_access_policy" {
  name   = "DynamoDBTableAccess"
  policy = data.aws_iam_policy_document.dynamodb_table_access_policy_doc.json
}

data aws_iam_policy_document "dynamodb_table_access_policy_doc" {
  statement {
    effect  = "Allow"
    actions = [
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
      "dynamodb:UpdateTimeToLive",
      "dynamodb:ConditionCheckItem",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:PartiQLUpdate",
      "dynamodb:Scan",
      "dynamodb:Query",
      "dynamodb:UpdateItem",
      "dynamodb:DescribeTimeToLive",
      "dynamodb:PartiQLSelect",
      "dynamodb:DescribeTable",
      "dynamodb:PartiQLInsert",
      "dynamodb:GetItem",
      "dynamodb:PartiQLDelete"
    ]
    resources = ["arn:aws:dynamodb:*:*:table/*"]
  }
}

resource aws_iam_policy "s3_object_access_policy" {
  name   = "S3ObjectAccess"
  policy = data.aws_iam_policy_document.s3_object_access_policy_doc.json
}

data aws_iam_policy_document "s3_object_access_policy_doc" {
  statement {
    effect  = "Allow"
    actions = [
      "s3:ListBucketMultipartUploads",
      "s3:ListBucketVersions",
      "s3:ListBucket"
    ]
    resources = ["arn:aws:s3:::*"]
  }
  statement {
    effect  = "Allow"
    actions = [
      "s3:DeleteObjectTagging",
      "s3:GetObjectRetention",
      "s3:DeleteObjectVersion",
      "s3:GetObjectVersionTagging",
      "s3:GetObjectAttributes",
      "s3:RestoreObject",
      "s3:PutObjectVersionTagging",
      "s3:DeleteObjectVersionTagging",
      "s3:GetObjectVersionAttributes",
      "s3:PutObject",
      "s3:GetObjectAcl",
      "s3:GetObject",
      "s3:AbortMultipartUpload",
      "s3:GetObjectVersionAcl",
      "s3:GetObjectTagging",
      "s3:PutObjectTagging",
      "s3:DeleteObject",
      "s3:GetObjectVersion"
    ]
    resources = ["arn:aws:s3:::*/*"]
  }
}

resource aws_iam_role_policy_attachment "integration_test_runner_dynamodb_access_policy_attachment" {
  role       = aws_iam_role.integration_test_runner.name
  policy_arn = aws_iam_policy.dynamodb_table_access_policy.arn
}

resource aws_iam_role_policy_attachment "integration_test_runner_s3_access_policy_attachment" {
  role       = aws_iam_role.integration_test_runner.name
  policy_arn = aws_iam_policy.s3_object_access_policy.arn
}

