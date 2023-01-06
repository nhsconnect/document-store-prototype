module "document_manifest_api" {
  source = "../modules/document_manifest_api"
  lambda_execution_role_arn = module.lambda_iam_role.lambda_execution_role_arn
  env_vars = {
    amplify_base_url = local.web_url
    document_zip_trace_ttl_in_days = 30
  }
  lambda_jar_filename = var.lambda_jar_filename
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  api_gateway_root_id = module.apigateway.api_gateway_rest_api_root_resource_id
  api_gateway_api_execution_arn = module.apigateway.api_gateway_execution_arn
}

module "document_manifest_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.apigateway.api_gateway_rest_api_root_resource_id
  lambda_arn     = module.document_manifest_api.document_manifest_lambda_invocation_arn
  http_method    = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "document_manifest_preflight" {
  source             = "../modules/api_gateway_preflight"
  api_gateway_id     = module.apigateway.api_gateway_rest_api_id
  resource_id = module.document_manifest_api.document_manifest_resource_id
  origin = "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'"
  methods = "'GET,OPTIONS,POST'"
}