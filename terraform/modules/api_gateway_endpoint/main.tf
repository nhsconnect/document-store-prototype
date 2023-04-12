variable "api_gateway_id" {}
variable "resource_id" {}
variable "lambda_arn" {}
variable "authorization" {}
variable "authorizer_id" {}

variable http_method {
  type = string
}

resource "aws_api_gateway_method_response" "get_method_200_response" {
  rest_api_id = var.api_gateway_id
  resource_id = var.resource_id
  http_method = "GET"
  status_code = "200"
  response_models = {
    "application/json" = "Empty"
  }
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers"     = true,
    "method.response.header.Access-Control-Allow-Methods"     = true,
    "method.response.header.Access-Control-Allow-Origin"      = true,
    "method.response.header.Access-Control-Allow-Credentials" = true
  }
}

resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = var.api_gateway_id
  resource_id   = var.resource_id
  http_method   = var.http_method
  authorization = var.authorization
  authorizer_id = var.authorizer_id
}

resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id             = var.api_gateway_id
  resource_id             = var.resource_id
  http_method             = var.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_arn
}

output http_method {
  value = var.http_method
}
