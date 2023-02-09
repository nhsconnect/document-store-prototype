resource "aws_lambda_function" "re_registration_lambda" {
  handler          = "uk.nhs.digital.docstore.handlers.ReRegistrationEventHandler::handleRequest"
  function_name    = "ReRegistrationEventHandler"
  runtime          = "java11"
  role             = aws_iam_role.lambda_execution_role.arn
  timeout          = 15
  memory_size      = 448
  filename         = var.lambda_jar_filename
  source_code_hash = filebase64sha256(var.lambda_jar_filename)
  layers = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]
  environment {
    variables = merge({
      NEMS_SQS_AUDIT_QUEUE_URL = aws_sqs_queue.sensitive_nems_audit.url
    }, local.common_environment_variables)
  }
}

resource "aws_lambda_event_source_mapping" "event_source_mapping" {
  event_source_arn = aws_sqs_queue.re_registration.arn
  enabled          = true
  function_name    = aws_lambda_function.re_registration_lambda.arn
  batch_size       = 1
  function_response_types = ["ReportBatchItemFailures"]
}

resource "aws_iam_role_policy" "sqs_to_lambda_policy" {
  name = "sqs_to_lambda_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "sqs:GetQueueAttributes",
          "sqs:ListQueues",
          "sqs:ReceiveMessage",
          "sqs:GetQueueUrl",
          "sqs:SendMessage",
          "sqs:DeleteMessage"
        ],
        "Resource" : aws_sqs_queue.re_registration.arn
      }
    ]
  })
}

resource "aws_sns_topic_subscription" "subscription_to_re_registration_sns_topic" {
  topic_arn = data.aws_ssm_parameter.re_registration_sns_topic_arn[0].value
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.re_registration.arn
  count     = var.cloud_only_service_instances
}