resource "aws_lambda_function" "token_request_lambda" {
  handler          = "uk.nhs.digital.docstore.authoriser.TokenRequestHandler::handleRequest"
  function_name    = "TokenRequestHandler"
  runtime          = "java11"
  role             = aws_iam_role.authoriser_execution_role.arn
  timeout          = 60
  memory_size      = 448
  filename         = var.authoriser_lambda_jar_filename
  source_code_hash = filebase64sha256(var.authoriser_lambda_jar_filename)
  layers           = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21"
  ]
  environment {
    variables = {
      DYNAMODB_ENDPOINT  = var.dynamodb_endpoint
    }
  }
}

resource "aws_api_gateway_resource" "token_request_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_resource.auth_resource.id
  path_part   = "TokenRequest"
}

resource "aws_lambda_permission" "api_gateway_permission_for_token_request_handler" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.token_request_lambda.arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/GET/${aws_api_gateway_resource.auth_resource.path_part}/${aws_api_gateway_resource.token_request_resource.path_part}"
}

resource "aws_api_gateway_method" "token_request_proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.token_request_resource.id
  http_method   = "GET"
  authorization = "CUSTOM"
  authorizer_id = aws_api_gateway_authorizer.cis2_authoriser.id
}

resource "aws_api_gateway_integration" "token_request_lambda_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.token_request_resource.id
  http_method = "GET"

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.token_request_lambda.invoke_arn
}
