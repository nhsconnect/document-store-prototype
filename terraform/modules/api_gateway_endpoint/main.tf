variable "api_gateway_id" {}
variable "parent_resource_id" {}
variable "lambda_arn" {}
variable "path_part" {}

variable http_method {
  type = string
}

resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = var.api_gateway_id
  parent_id   = var.parent_resource_id
  path_part   = var.path_part
}

resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = var.api_gateway_id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = var.http_method
  authorization = "AWS_IAM"
}

resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id = var.api_gateway_id
  resource_id = aws_api_gateway_method.proxy_method.resource_id
  http_method = aws_api_gateway_method.proxy_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_arn
}
