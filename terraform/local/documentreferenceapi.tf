module "document_reference_api" {
  source = "../modules/document_reference_api"
  lambda_role_arn = module.lambda_iam_role.lambda_execution_role_arn
  lambda_jar_filename = var.lambda_jar_filename
  api_gateway_api_id = module.apigateway.api_gateway_rest_api_id
  api_gateway_api_root_resource_id = module.apigateway.api_gateway_rest_api_root_resource_id
  api_gateway_api_invocation_arn = module.apigateway.api_gateway_execution_arn
  env_vars = {
    DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
    SQS_QUEUE_URL              = aws_sqs_queue.document-store.url
    DYNAMODB_ENDPOINT          = "http://localhost:4566"
    S3_ENDPOINT                = "http://localhost:4566"
    SQS_ENDPOINT               = "http://localhost:4566"
    S3_USE_PATH_STYLE          = true
  }
}

module "get_doc_ref_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.document_reference_api.get_doc_ref_resource_id
  lambda_arn     = module.lambda_iam_role.lambda_execution_role_arn
  http_method    = "GET"
  authorization = "NONE"
  authorizer_id = null
}

module "create_doc_ref_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.document_reference_api.doc_ref_collection_resource_id
  lambda_arn     = module.lambda_iam_role.lambda_execution_role_arn
  http_method    = "POST"
  authorization = "NONE"
  authorizer_id  = null
}

module "search_doc_ref_endpoint" {
  source         = "../modules/api_gateway_endpoint"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.document_reference_api.doc_ref_collection_resource_id
  lambda_arn     = module.lambda_iam_role.lambda_execution_role_arn
  http_method    = "GET"
  authorization = "NONE"
  authorizer_id  = null
}

module "delete_doc_ref_endpoint" {
  source             = "../modules/api_gateway_endpoint"
  api_gateway_id     = module.apigateway.api_gateway_rest_api_id
  resource_id = module.document_reference_api.doc_ref_collection_resource_id
  lambda_arn         = module.lambda_iam_role.lambda_execution_role_arn
  http_method        = "DELETE"
  authorization = "NONE"
  authorizer_id = null
}

module "doc_ref_collection_preflight" {
  source             = "../modules/api_gateway_preflight"
  api_gateway_id     = module.apigateway.api_gateway_rest_api_id
  resource_id = module.document_reference_api.doc_ref_collection_resource_id
  origin = "'${local.web_url}'"
  methods = "'GET,OPTIONS,POST,DELETE'"
}

module "get_doc_ref_preflight" {
  source         = "../modules/api_gateway_preflight"
  api_gateway_id = module.apigateway.api_gateway_rest_api_id
  resource_id    = module.document_reference_api.get_doc_ref_resource_id
  origin         = "'${local.web_url}'"
  methods        = "'GET,OPTIONS,POST'"
}