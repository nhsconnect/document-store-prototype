module "document_manifest_api" {
  source = "../modules/document_manifest_api"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  api_gateway_root_id = module.apigateway.api_gateway_rest_api_root_resource_id
  api_gateway_api_execution_arn = module.apigateway.api_gateway_execution_arn
  lambda_execution_role_arn = module.lambda_iam_role.lambda_execution_role_arn
  env_vars = {
    amplify_base_url = "http://localhost:3000"
    document_zip_trace_ttl_in_days = 1
  }
  lambda_jar_filename = var.lambda_jar_filename
}

module "document_manifest_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.document_manifest_api.document_manifest_resource_id
  lambda_arn     = module.document_manifest_api.document_manifest_lambda_invocation_arn
  http_method    = "GET"
  authorization = "NONE"
  authorizer_id = null
}

module "document_manifest_preflight" {
  source = "../modules/api_gateway_preflight"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id = module.document_manifest_api.document_manifest_resource_id
  origin = "'*'"
  methods = "'GET,OPTIONS,POST'"
}