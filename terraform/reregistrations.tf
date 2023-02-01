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
    variables = local.common_environment_variables
  }
}