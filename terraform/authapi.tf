resource "aws_lambda_function" "login_redirect_lambda" {
  handler          = "uk.nhs.digital.docstore.authoriser.LoginHandler::handleRequest"
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
    variables = {
      DYNAMODB_ENDPOINT  = var.dynamodb_endpoint
      OIDC_AUTHORIZE_URL = var.cognito_cis2_provider_authorize_url
      OIDC_CALLBACK_URL  = var.cloud_only_service_instances > 0 ? "${local.amplify_base_url}/cis2-auth-callback" : var.cognito_cis2_client_callback_urls[0]
      OIDC_CLIENT_ID     = var.cognito_cis2_provider_client_id
    }
  }
}

resource "aws_api_gateway_resource" "auth_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "Auth"
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

resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.login_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.login_resource.id
  http_method = "GET"

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.login_redirect_lambda.invoke_arn
}