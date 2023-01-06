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
    AMPLIFY_BASE_URL     = "http://localhost:3000"
    SQS_ENDPOINT         = "http://localhost:4566"
    SQS_QUEUE_URL        = aws_sqs_queue.document-store.url
  }
}

module "patient_details_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.patient_details_api.patient_details_resource_id
  lambda_arn     = module.patient_details_api.patient_details_lambda_invocation_arn
  http_method    = "GET"
  authorization = "NONE"
  authorizer_id = null
}

module "patient_details_collection_preflight" {
  source         = "../modules/api_gateway_preflight"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.patient_details_api.patient_details_resource_id
  origin         = "'*'"
  methods        = "'GET,OPTIONS,POST'"
}
