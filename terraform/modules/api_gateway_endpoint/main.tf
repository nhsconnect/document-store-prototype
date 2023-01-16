variable "api_gateway_id" {}
variable "resource_id" {}
variable "lambda_arn" {}
variable "authorizer_id" {}

variable http_method {
  type = string
}

resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = var.api_gateway_id
  resource_id   = var.resource_id
  http_method   = var.http_method
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = var.authorizer_id
}

resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id = var.api_gateway_id
  resource_id = var.resource_id
  http_method = var.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_arn
}

output http_method {
  value = var.http_method
}
