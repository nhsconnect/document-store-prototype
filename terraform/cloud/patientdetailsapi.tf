module "patient_details_api" {
  source = "../modules/patient_details_api"
  lambda_execution_role_arn = module.lambda_iam_role.lambda_execution_role_arn
  lambda_jar_filename = var.lambda_jar_filename
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  api_gateway_root_id = module.apigateway.api_gateway_rest_api_root_resource_id
  api_gateway_api_execution_arn = module.apigateway.api_gateway_execution_arn
  env_vars = {
    PDS_FHIR_ENDPOINT    = var.pds_fhir_sandbox_url
    PDS_FHIR_IS_STUBBED  = var.pds_fhir_is_stubbed
    PDS_FHIR_PRIVATE_KEY = data.aws_ssm_parameter.pds_fhir_private_key.value
    NHS_API_KEY          = data.aws_ssm_parameter.nhs_api_key.value
    NHS_OAUTH_ENDPOINT   = data.aws_ssm_parameter.nhs_oauth_endpoint.value
    AMPLIFY_BASE_URL     = local.web_url
    SQS_QUEUE_URL        = aws_sqs_queue.document-store.url
  }
}

module "patient_details_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.patient_details_api.patient_details_resource_id
  lambda_arn     = module.patient_details_api.patient_details_lambda_invocation_arn
  http_method    = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id  = aws_api_gateway_authorizer.cognito_authorizer.id
}

module "patient_details_collection_preflight" {
  source         = "../modules/api_gateway_preflight"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.patient_details_api.patient_details_resource_id
  origin         = "'${local.web_url}'"
  methods        = "'GET,OPTIONS,POST'"
}