resource "aws_lambda_function" "back_channel_logout_lambda" {
  handler          = "uk.nhs.digital.docstore.authoriser.handlers.BackChannelLogoutHandler::handleRequest"
  function_name    = "${terraform.workspace}_BackChannelLogoutHandler"
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
    variables = local.authoriser_environment_variables
  }
}

resource "aws_api_gateway_resource" "back_channel_logout_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_resource.auth_resource.id
  path_part   = "BackChannelLogout"
}

resource "aws_lambda_permission" "api_gateway_permission_for_back_channel_logout_handler" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.back_channel_logout_lambda.arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/POST/${aws_api_gateway_resource.auth_resource.path_part}/${aws_api_gateway_resource.back_channel_logout_resource.path_part}"
}

resource "aws_api_gateway_method" "back_channel_logout_proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.back_channel_logout_resource.id
  http_method   = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "back_channel_logout_lambda_integration" {
  rest_api_id             = aws_api_gateway_rest_api.lambda_api.id
  resource_id             = aws_api_gateway_resource.back_channel_logout_resource.id
  http_method             = aws_api_gateway_method.back_channel_logout_proxy_method.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.back_channel_logout_lambda.invoke_arn
}
