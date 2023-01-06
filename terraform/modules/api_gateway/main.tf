variable api_gateway_stage {}
variable redeployment_triggers {}

resource "aws_api_gateway_rest_api" "lambda_api" {
  name = "DocStoreAPI"
}

resource "aws_api_gateway_deployment" "api_deploy" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  stage_name  = var.api_gateway_stage

  triggers = {
    redeployment = sha1(jsonencode(concat([
      aws_api_gateway_rest_api.lambda_api.body
    ], var.redeployment_triggers)))
  }
}

output "api_gateway_rest_api_id" {
  value = aws_api_gateway_rest_api.lambda_api.id
}

output "api_gateway_rest_api_root_resource_id" {
  value = aws_api_gateway_rest_api.lambda_api.root_resource_id
}

output "api_gateway_rest_api_stage" {
  value = aws_api_gateway_deployment.api_deploy.stage_name
}

output "api_gateway_url" {
  value = aws_api_gateway_deployment.api_deploy.invoke_url
}

output "api_gateway_execution_arn" {
  value = aws_api_gateway_rest_api.lambda_api.execution_arn
}