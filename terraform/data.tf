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