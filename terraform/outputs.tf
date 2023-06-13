output "amplify_app_ids" {
  value = aws_amplify_app.doc-store-ui[*].id
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
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

output "cognito_user_pool_ids" {
  value = ""
}

output "cognito_client_ids" {
  value = ""
}

output "cognito_user_pool_domain" {
  value = ""
}

output "cognito_redirect_signin" {
  value = ""
}

output "cognito_redirect_signout" {
  value = ["", ""]
}
