module "patient_details_endpoint" {
  source         = "./modules/api_gateway_endpoint"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.patient_details_collection_resource.id
  lambda_arn     = aws_lambda_function.search_patient_details_lambda.invoke_arn
  http_method    = "GET"
  authorization  = "CUSTOM"
  authorizer_id  =  aws_api_gateway_authorizer.cis2_authoriser.id
}

module "patient_details_collection_preflight" {
  source         = "./modules/api_gateway_preflight"
  api_gateway_id = aws_api_gateway_rest_api.lambda_api.id
  resource_id    = aws_api_gateway_resource.patient_details_collection_resource.id
  origin         = var.cloud_only_service_instances > 0 ? "'https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com'" : "'*'"
  methods        = "'GET,OPTIONS,POST'"
}

module search_patient_details_alarms {
  source                     = "./modules/lambda_alarms"
  lambda_function_name       = aws_lambda_function.search_patient_details_lambda.function_name
  lambda_timeout             = aws_lambda_function.search_patient_details_lambda.timeout
  lambda_short_name          = "search_patient_details_handler"
  notification_sns_topic_arn = aws_sns_topic.alarm_notifications.arn
  environment                = var.environment
}

data "aws_ssm_parameter" "pds_fhir_private_key" {
  name  = "/prs/${var.environment}/user-input/pds-fhir-private-key"
  count = var.cloud_only_service_instances
}

data "aws_ssm_parameter" "nhs_api_key" {
  name  = "/prs/${var.environment}/user-input/nhs-api-key"
  count = var.cloud_only_service_instances
}

data "aws_ssm_parameter" "nhs_oauth_endpoint" {
  name  = "/prs/${var.environment}/user-input/nhs-oauth-endpoint"
  count = var.cloud_only_service_instances
}

data "aws_ssm_parameter" "pds_fhir_endpoint" {
  name  = "/prs/${var.environment}/user-input/pds-fhir-endpoint"
  count = var.cloud_only_service_instances
}

data "aws_ssm_parameter" "pds_fhir_kid" {
  name  = "/prs/${var.environment}/user-input/pds-fhir-kid"
  count = var.cloud_only_service_instances
}

resource "aws_lambda_function" "search_patient_details_lambda" {
  handler          = "uk.nhs.digital.docstore.lambdas.SearchPatientDetailsHandler::handleRequest"
  function_name    = "SearchPatientDetailsHandler"
  runtime          = "java11"
  role             = aws_iam_role.lambda_execution_role.arn
  timeout          = 15
  memory_size      = 448
  filename         = var.search_patient_details_lambda_jar_filename
  source_code_hash = filebase64sha256(var.search_patient_details_lambda_jar_filename)
  layers           = [
    "arn:aws:lambda:eu-west-2:580247275435:layer:LambdaInsightsExtension:21",
    "arn:aws:lambda:eu-west-2:133256977650:layer:AWS-Parameters-and-Secrets-Lambda-Extension:4",
    aws_lambda_layer_version.document_store_lambda_layer.arn
  ]
  environment {
    variables = {
      PDS_FHIR_TOKEN_NAME  = "/prs/${var.environment}/pds-fhir-access-token"
      PDS_FHIR_ENDPOINT    = var.cloud_only_service_instances > 0 ? data.aws_ssm_parameter.pds_fhir_endpoint[0].value : var.pds_fhir_sandbox_url
      PDS_FHIR_IS_STUBBED  = var.pds_fhir_is_stubbed
      PDS_FHIR_PRIVATE_KEY = var.cloud_only_service_instances > 0 ? data.aws_ssm_parameter.pds_fhir_private_key[0].value : ""
      PDS_FHIR_KID         = var.cloud_only_service_instances > 0 ? data.aws_ssm_parameter.pds_fhir_kid[0].value : ""
      NHS_API_KEY          = var.cloud_only_service_instances > 0 ? data.aws_ssm_parameter.nhs_api_key[0].value : ""
      NHS_OAUTH_ENDPOINT   = var.cloud_only_service_instances > 0 ? data.aws_ssm_parameter.nhs_oauth_endpoint[0].value : ""
      AMPLIFY_BASE_URL     = local.amplify_base_url
      SQS_ENDPOINT         = var.sqs_endpoint
      SQS_AUDIT_QUEUE_URL  = aws_sqs_queue.sensitive_audit.url
    }
  }
}

resource "aws_api_gateway_resource" "patient_details_collection_resource" {
  rest_api_id = aws_api_gateway_rest_api.lambda_api.id
  parent_id   = aws_api_gateway_rest_api.lambda_api.root_resource_id
  path_part   = "PatientDetails"
}

resource "aws_lambda_permission" "api_gateway_permission_for_search_patient_details" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.search_patient_details_lambda.arn
  principal     = "apigateway.amazonaws.com"
  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn    = "${aws_api_gateway_rest_api.lambda_api.execution_arn}/*/*"
}

resource "aws_iam_role_policy" "lambda_kms_policy" {
  name   = "lambda_decrypt_from_kms"
  role   = aws_iam_role.lambda_execution_role.id
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "kms:Decrypt"
        ],
        "Resource" : "arn:aws:kms:*:*:key/69f6093c-d0b9-4fee-b84d-96f8799ec71c"
      },
    ]
  })
}

resource "aws_iam_role_policy" "lambda_get_parameter_policy" {
  name   = "lambda_get_parameter_from_ssm_policy"
  role   = aws_iam_role.lambda_execution_role.id
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "ssm:GetParameter",
          "ssm:PutParameter"
        ],
        "Resource" : [
          "arn:aws:ssm:*:*:parameter/prs/*/pds-fhir-access-token",
          "arn:aws:ssm:*:*:parameter/prs/*/pds-fhir-private-key",
          "arn:aws:ssm:*:*:parameter/prs/*/nhs-api-key"
        ]
      },
    ]
  })
}

locals {
  search_patient_details_invocation_arn = "arn:aws:execute-api:${var.region}:${var.account_id}:${aws_api_gateway_rest_api.lambda_api.id}/${var.api_gateway_stage}/${module.patient_details_endpoint.http_method}${aws_api_gateway_resource.patient_details_collection_resource.path}"
}
