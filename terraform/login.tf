module login_redirect_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.login_redirect_lambda.function_name
  lambda_timeout             = aws_lambda_function.login_redirect_lambda.timeout
  lambda_short_name          = "login_redirect_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}

resource "aws_lambda_function" "login_redirect_lambda" {
  handler          = "uk.nhs.digital.docstore.authoriser.handlers.LoginRedirectHandler::handleRequest"
  function_name    = "LoginRedirectHandler"
  runtime          = "java11"
  role             = aws_iam_role.authoriser_execution_role.arn
  timeout          = 15
  memory_size      = 256
  filename         = var.authoriser_lambda_jar_filename
  source_code_hash = filebase64sha256(var.authoriser_lambda_jar_filename)
  layers           = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]
  environment {
    variables = local.authoriser_environment_variables
  }
}

resource "aws_api_gateway_resource" "login_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_resource.auth_resource.id
  path_part   = "Login"
}

resource "aws_lambda_permission" "api_gateway_permission_for_login_redirect_handler" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.login_redirect_lambda.arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/GET/${aws_api_gateway_resource.auth_resource.path_part}/${aws_api_gateway_resource.login_resource.path_part}"
}

resource "aws_api_gateway_method" "login_proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.login_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "login_lambda_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.login_resource.id
  http_method = aws_api_gateway_method.login_proxy_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.login_redirect_lambda.invoke_arn
}