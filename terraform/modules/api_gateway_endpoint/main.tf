variable "api_gateway_id" {}
variable "resource_id" {}
variable "lambda_arn" {}
variable "authorization" {}
variable "authorizer_id" {}

variable "http_method" {
  type = string
}

resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id             = var.api_gateway_id
  resource_id             = var.resource_id
  http_method             = var.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_arn
}

///NO RESPONSE 
resource "aws_api_gateway_method" "get_method" {
  rest_api_id   = var.api_gateway_id
  resource_id   = var.resource_id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "get_method_response" {
  rest_api_id = var.api_gateway_id
  resource_id = var.resource_id
  http_method = aws_api_gateway_method.get_method.http_method
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
  depends_on = [aws_api_gateway_method.get_method]
}


output "http_method" {
  value = var.http_method
}
