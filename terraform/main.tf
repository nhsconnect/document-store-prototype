terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.75.0"
    }
  }

  backend "s3" {
    bucket  = "document-store-terraform-state"
    key     = "document-store/terraform.tfstate"
    region  = "eu-west-2"
    encrypt = true
  }
}

provider "aws" {
  profile = "default"
  region  = var.region

  skip_credentials_validation = var.disable_aws_remote_checks
  skip_requesting_account_id  = var.disable_aws_remote_checks
  s3_force_path_style         = var.disable_aws_remote_checks
  skip_metadata_api_check     = var.disable_aws_remote_checks

  endpoints {
    apigateway = var.aws_endpoint
    cloudwatch = var.aws_endpoint
    dynamodb   = var.aws_endpoint
    iam        = var.aws_endpoint
    lambda     = var.aws_endpoint
    s3         = var.aws_endpoint
  }
}

resource "aws_lambda_function" "get_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.RetrieveDocumentReferenceHandler::handleRequest"
  function_name = "RetrieveDocumentReferenceHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 25
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
      DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
      S3_ENDPOINT                = var.s3_endpoint
    }
  }
}

resource "aws_lambda_function" "create_doc_ref_lambda" {
  handler       = "uk.nhs.digital.docstore.CreateDocumentReferenceHandler::handleRequest"
  function_name = "CreateDocumentReferenceHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
      DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
      S3_ENDPOINT                = var.s3_endpoint
      AMPLIFY_BASE_URL           = var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : ""
    }
  }
}

resource "aws_lambda_function" "document_uploaded_lambda" {
  handler       = "uk.nhs.digital.docstore.DocumentUploadedEventHandler::handleRequest"
  function_name = "DocumentUploadedEventHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 256

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
      DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
      S3_ENDPOINT                = var.s3_endpoint
    }
  }
}

resource "aws_lambda_function" "doc_ref_search_lambda" {
  handler       = "uk.nhs.digital.docstore.search.DocumentReferenceSearchHandler::handleRequest"
  function_name = "DocumentReferenceSearchHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      DOCUMENT_STORE_BUCKET_NAME = aws_s3_bucket.document_store.bucket
      DYNAMODB_ENDPOINT          = var.dynamodb_endpoint
      S3_ENDPOINT                = var.s3_endpoint
      AMPLIFY_BASE_URL           = var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : ""
    }
  }
}

resource "aws_lambda_function" "retrieve_patient_details_lambda" {
  handler       = "uk.nhs.digital.docstore.patientdetails.RetrievePatientDetailsHandler::handleRequest"
  function_name = "RetrievePatientDetailsHandler"
  runtime       = "java11"
  role          = aws_iam_role.lambda_execution_role.arn

  timeout     = 15
  memory_size = 448

  filename = var.lambda_jar_filename

  source_code_hash = filebase64sha256(var.lambda_jar_filename)

  environment {
    variables = {
      AMPLIFY_BASE_URL = var.cloud_only_service_instances > 0 ? "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com" : ""
    }
  }
}

resource "aws_iam_role" "lambda_execution_role" {
  name = "LambdaExecution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_execution_policy" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "dynamodb_get_document_reference_policy" {
  name = "get_document_reference_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ],
        "Resource" : aws_dynamodb_table.doc_ref_store.arn
      }
    ]
  })
}

resource "aws_iam_role_policy" "dynamodb_query_locations_policy" {
  name = "dynamodb_query_locations_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Query",
        ],
        "Resource" : "${aws_dynamodb_table.doc_ref_store.arn}/index/LocationsIndex"
      },
      {
        "Effect" : "Allow",
        "Action" : [
          "dynamodb:Query",
        ],
        "Resource" : "${aws_dynamodb_table.doc_ref_store.arn}/index/NhsNumberIndex"
      }
    ]
  })
}

resource "aws_iam_role_policy" "s3_get_document_data_policy" {
  name = "get_document_data_policy"
  role = aws_iam_role.lambda_execution_role.id

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "s3:GetObject",
          "s3:PutObject",
        ],
        "Resource" : "${aws_s3_bucket.document_store.arn}/*"
      }
    ]
  })
}

resource "aws_api_gateway_rest_api" "lambda_api" {
  name = "DocStoreAPI"
}

module "doc_ref_endpoint" {
  source             = "./modules/api_gateway_endpoint"
  api_gateway_id     = aws_api_gateway_rest_api.lambda_api.id
  parent_resource_id = aws_api_gateway_resource.doc_ref_resource.id
  lambda_arn         = aws_lambda_function.get_doc_ref_lambda.invoke_arn
  path_part          = "{id}"
  http_method        = "GET"
}

module "patient_details_endpoint" {
  source             = "./modules/api_gateway_endpoint"
  api_gateway_id     = aws_api_gateway_rest_api.lambda_api.id
  parent_resource_id = aws_api_gateway_resource.patient_details_resource.id
  lambda_arn         = aws_lambda_function.retrieve_patient_details_lambda.invoke_arn
  path_part          = "{id}"
  http_method        = "GET"
}

resource "aws_api_gateway_method" "create_doc_ref_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.doc_ref_resource.id
  http_method   = "POST"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.doc_ref_authorizer.id
}

resource "aws_api_gateway_integration" "create_doc_ref_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_method.create_doc_ref_method.resource_id
  http_method = aws_api_gateway_method.create_doc_ref_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.create_doc_ref_lambda.invoke_arn
}

resource "aws_api_gateway_method" "doc_ref_search_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.doc_ref_resource.id
  http_method   = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.doc_ref_authorizer.id
}

resource "aws_api_gateway_integration" "doc_ref_search_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_method.doc_ref_search_method.resource_id
  http_method = aws_api_gateway_method.doc_ref_search_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.doc_ref_search_lambda.invoke_arn
}

resource "aws_api_gateway_method" "retrieve_patient_details_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.patient_details_resource.id
  http_method   = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.doc_ref_authorizer.id
}

resource "aws_api_gateway_integration" "retrieve_patient_details_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_method.retrieve_patient_details_method.resource_id
  http_method = aws_api_gateway_method.retrieve_patient_details_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.retrieve_patient_details_lambda.invoke_arn
}

resource "aws_api_gateway_method" "create_and_search_doc_preflight_method" {
  rest_api_id   = aws_api_gateway_rest_api.lambda_api.id
  resource_id   = aws_api_gateway_resource.doc_ref_resource.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "create_and_search_doc_preflight_method_response" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.doc_ref_resource.id
  http_method = aws_api_gateway_method.create_and_search_doc_preflight_method.http_method
  status_code = "200"
  response_models = {
    "application/json" = "Empty"
  }
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true,
    "method.response.header.Access-Control-Allow-Methods" = true,
    "method.response.header.Access-Control-Allow-Origin"  = true
  }
  depends_on = [aws_api_gateway_method.create_and_search_doc_preflight_method]
}

resource "aws_api_gateway_integration" "create_and_search_doc_preflight_integration" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.doc_ref_resource.id
  http_method = aws_api_gateway_method.create_and_search_doc_preflight_method.http_method
  type        = "MOCK"
  depends_on  = [aws_api_gateway_method.create_and_search_doc_preflight_method]
  request_templates = {
    "application/json" = <<EOF
{
   "statusCode" : 200
}
EOF
  }
}

resource "aws_api_gateway_integration_response" "create_and_search_doc_preflight_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id = aws_api_gateway_resource.doc_ref_resource.id
  http_method = aws_api_gateway_method.create_and_search_doc_preflight_method.http_method
  status_code = aws_api_gateway_method_response.create_and_search_doc_preflight_method_response.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'",
    "method.response.header.Access-Control-Allow-Methods" = "'GET,OPTIONS,POST'",
    "method.response.header.Access-Control-Allow-Origin"  = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  }
  depends_on = [aws_api_gateway_method_response.create_and_search_doc_preflight_method_response]
}

resource "aws_api_gateway_resource" "doc_ref_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "DocumentReference"
}

resource "aws_api_gateway_resource" "patient_details_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "PatientDetails"
}

resource "aws_api_gateway_deployment" "api_deploy" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  stage_name  = var.api_gateway_stage

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_rest_api.lambda_api.body,
      module.doc_ref_endpoint,
      module.patient_details_endpoint,
      aws_api_gateway_method.create_doc_ref_method,
      aws_api_gateway_resource.doc_ref_resource,
      aws_api_gateway_resource.patient_details_resource,
      aws_api_gateway_integration.create_doc_ref_integration,
      aws_api_gateway_integration.retrieve_patient_details_integration,
      aws_api_gateway_method.doc_ref_search_method,
      aws_api_gateway_method.retrieve_patient_details_method,
      aws_api_gateway_integration.doc_ref_search_integration,
      aws_api_gateway_authorizer.doc_ref_authorizer,
      aws_api_gateway_integration.create_and_search_doc_preflight_integration,
      aws_api_gateway_method.create_and_search_doc_preflight_method,
    ]))
  }
}

resource "aws_api_gateway_authorizer" "doc_ref_authorizer" {
  name        = "doc-ref-authorizer"
  type        = "COGNITO_USER_POOLS"
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  provider_arns = var.cloud_only_service_instances > 0 ? [for pool_arn in aws_cognito_user_pool.pool[*].arn : pool_arn] : [
    ""
  ]
  authorizer_credentials = aws_iam_role.lambda_execution_role.arn
}

resource "aws_lambda_permission" "api_gateway_permission_for_get_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_create_doc_ref" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.create_doc_ref_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_doc_ref_search" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.doc_ref_search_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "api_gateway_permission_for_retrieve_patient_details" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.retrieve_patient_details_lambda.arn
  principal     = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "s3_permission_for_document_upload_event" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.document_uploaded_lambda.arn
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.document_store.arn
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
