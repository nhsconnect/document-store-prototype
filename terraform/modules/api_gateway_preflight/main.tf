variable "api_gateway_id" {}
variable "resource_id" {}
variable "origin" {}
variable "methods" {}

resource "aws_api_gateway_method" "preflight_method" {
  rest_api_id   = var.api_gateway_id
  resource_id   = var.resource_id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "preflight_method_response" {
  rest_api_id = var.api_gateway_id
  resource_id = var.resource_id
  http_method = aws_api_gateway_method.preflight_method.http_method
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
  depends_on = [aws_api_gateway_method.preflight_method]
}

resource "aws_api_gateway_integration" "preflight_integration" {
  rest_api_id = var.api_gateway_id
  resource_id = var.resource_id
  http_method = "OPTIONS"
  type        = "MOCK"
  depends_on  = [aws_api_gateway_method.preflight_method]
  request_templates = {
    "application/json" = <<EOF
{
   "statusCode" : 200
}
EOF
  }
}

resource "aws_api_gateway_integration_response" "preflight_integration_response" {
  rest_api_id = var.api_gateway_id
  resource_id = var.resource_id
  http_method = aws_api_gateway_method.preflight_method.http_method
  status_code = aws_api_gateway_method_response.preflight_method_response.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers"     = "'Content-Type,X-Amz-Date,Authorization,Cookie,X-Api-Key,X-Amz-Security-Token,X-Auth-Token,Accept'",
    "method.response.header.Access-Control-Allow-Methods"     = var.methods,
    "method.response.header.Access-Control-Allow-Origin"      = var.origin,
    "method.response.header.Access-Control-Allow-Credentials" = "'true'"
  }
  depends_on = [aws_api_gateway_method_response.preflight_method_response]
}
