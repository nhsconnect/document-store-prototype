data "aws_iam_policy_document" "authoriser_execution_access_policy_document" {
  statement {
    effect    = "Allow"
    actions   = ["lambda:InvokeFunction"]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
    ]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
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

module "authoriser_alarms" {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.authoriser_lambda.function_name
  lambda_timeout             = aws_lambda_function.authoriser_lambda.timeout
  lambda_short_name          = "authoriser"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = terraform.workspace
}

resource "aws_api_gateway_authorizer" "cis2_authoriser" {
  name                   = "${terraform.workspace}_cis2-authoriser"
  type                   = "REQUEST"
  identity_source        = "method.request.header.Cookie"
  rest_api_id            = aws_api_gateway_rest_api.lambda_api.id
  authorizer_uri         = aws_lambda_function.authoriser_lambda.invoke_arn
  authorizer_credentials = aws_iam_role.authoriser_execution.arn
  authorizer_result_ttl_in_seconds = 0
}

resource "aws_lambda_function" "authoriser_lambda" {
  handler          = "uk.nhs.digital.docstore.authoriser.handlers.AuthoriserHandler::handleRequest"
  function_name    = "${terraform.workspace}_AuthoriserHandler"
  runtime          = "java11"
  role             = aws_iam_role.authoriser_execution_role.arn
  timeout          = 15
  memory_size      = 256
  filename         = var.authoriser_lambda_jar_filename
  source_code_hash = filebase64sha256(var.authoriser_lambda_jar_filename)
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]

  environment {
    variables = var.enable_session_auth ? local.authoriser_environment_variables : {
      AUTH_CONFIG = jsonencode({
        resourcesForPCSEUsers = [
          local.search_patient_details_invocation_arn,
          local.search_document_reference_invocation_arn,
          local.get_document_manifest_invocation_arn,
          local.delete_document_reference_invocation_arn
        ],
        resourcesForClinicalUsers = [
          local.search_patient_details_invocation_arn,
          local.create_document_reference_invocation_arn,
        ]
      })
    }
  }
}

resource "aws_iam_role" "authoriser_execution_role" {
  name = "${terraform.workspace}_AuthoriserLambdaExecution"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "authoriser_lambda_execution_policy" {
  role       = aws_iam_role.authoriser_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "authoriser_insights_policy" {
  role       = aws_iam_role.authoriser_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLambdaInsightsExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "authoriser_sqs_policy" {
  role       = aws_iam_role.authoriser_execution_role.name
  policy_arn = aws_iam_policy.splunk_access_policy.arn
}

resource "aws_iam_role" "authoriser_execution" {
  name               = "${terraform.workspace}_AuthoriserExecution"
  description        = "Role to allow authoriser to execute"
  assume_role_policy = data.aws_iam_policy_document.authoriser_trust_policy.json
}

resource "aws_iam_policy" "authoriser_access_policy" {
  name   = "${terraform.workspace}_authoriser_access_policy"
  policy = data.aws_iam_policy_document.authoriser_execution_access_policy_document.json
}

resource "aws_iam_role_policy_attachment" "authoriser_execution_access_policy_attachment" {
  role       = aws_iam_role.authoriser_execution.name
  policy_arn = aws_iam_policy.authoriser_access_policy.arn
}
