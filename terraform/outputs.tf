output "amplify_app_ids" {
  value = aws_amplify_app.doc-store-ui[*].id
}

output "document-store-bucket" {
  value = var.workspace_is_a_sandbox ? "" : aws_s3_bucket.document_store[0].bucket
}

output "api_gateway_rest_api_id" {
  value = aws_api_gateway_deployment.api_deploy.rest_api_id
}

output "api_gateway_rest_api_stage" {
  value = aws_api_gateway_deployment.api_deploy.stage_name
}

output "api_gateway_url" {
  value = aws_api_gateway_deployment.api_deploy.invoke_url
}
