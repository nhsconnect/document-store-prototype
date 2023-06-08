resource aws_iam_role "integration_test_runner" {
  name               = "IntegrationTestRunner"
  description        = "Role for running integration tests against AWS resources"
  assume_role_policy = data.aws_iam_policy_document.integration_test_runner_trust_policy.json
}

data "aws_iam_policy_document" "integration_test_runner_trust_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    effect  = "Allow"
    principals {
      identifiers = ["arn:aws:iam::327778747031:role/gocd_agent-prod"]
      type        = "AWS"
    }
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