variable "environment" {
  type = string
}
variable "region" {
  type    = string
  default = "eu-west-2"
}
variable "workspace" {
  type    = string
  default = "default"
}

variable "account_id" {
  type = string
}

variable "cloud_only_service_instances" {
  type    = number
  default = 1
}

// TODO: [PRMT-2779] Consider improving the naming for this
variable "enable_session_auth" {
  type    = bool
  default = true
}

variable "enable_basic_auth" {
  type    = bool
  default = true
}

variable "basic_auth_password" {
  type    = string
  default = ""
}

variable "create_doc_manifest_lambda_jar_filename" {
  type    = string
  default = "..lambdas/CreateDocumentManifestByNhsNumber/build/libs/CreateDocumentManifestByNhsNumber.jar"
}

variable "create_doc_ref_lambda_jar_filename" {
  type    = string
  default = "..lambdas/CreateDocumentReference/build/libs/CreateDocumentReference.jar"
}

variable "delete_doc_ref_lambda_jar_filename" {
  type    = string
  default = "..lambdas/DeleteDocumentReference/build/libs/DeleteDocumentReference.jar"
}

variable "doc_ref_search_lambda_jar_filename" {
  type    = string
  default = "..lambdas/DocumentReferenceSearch/build/libs/DocumentReferenceSearch.jar"
}

variable "fake_virus_scanner_event_lambda_jar_filename" {
  type    = string
  default = "..lambdas/FakeVirusScannedEvent/build/libs/FakeVirusScannedEvent.jar"
}

variable "reregistration_event_lambda_jar_filename" {
  type    = string
  default = "..lambdas/ReRegistrationEvent/build/libs/ReRegistrationEvent.jar"
}

variable "search_patient_details_lambda_jar_filename" {
  type    = string
  default = "..lambdas/SearchPatientDetails/build/libs/SearchPatientDetails.jar"
}

variable "virus_scanner_event_lambda_jar_filename" {
  type    = string
  default = "..lambdas/VirusScannedEvent/build/libs/VirusScannedEvent.jar"
}

variable "lambda_layers_filename" {
  type    = string
  default = "../app/build/distributions/app.zip"
}

variable "authoriser_lambda_jar_filename" {
  type    = string
  default = "../authoriser/build/libs/authoriser.jar"
}

variable "api_gateway_stage" {
  type    = string
  default = "prod"
}

variable "disable_aws_remote_checks" {
  type    = bool
  default = false
}

variable "aws_endpoint" {
  type    = string
  default = ""
}

variable "dynamodb_endpoint" {
  type    = string
  default = ""
}

variable "s3_endpoint" {
  type    = string
  default = ""
}

variable "s3_use_path_style" {
  type        = bool
  description = "Needed to drive path-style presigned URLs that are simpler for localstack to use in testing"
  default     = false
}

variable "pds_fhir_sandbox_url" {
  type    = string
  default = "https://sandbox.api.service.nhs.uk/personal-demographics/FHIR/R4/"
}

variable "pds_fhir_is_stubbed" {
  type    = string
  default = "true"
}

variable "cognito_domain_prefix" {
  type        = string
  default     = ""
  description = "This should be an empty string for the dev environment. If this changes it must be updated with CIS2"
}

variable "oidc_providers" {
  type    = list(string)
  default = []
}

variable "cis2_provider_client_id" {
  type    = string
  default = "0000"
}

variable "cis2_provider_client_secret" {
  type    = string
  default = "0000"
}

variable "cis2_provider_oidc_issuer" {
  type    = string
  default = ""
}

variable "NHS_CIS2_ENVIRONMENT" {
  type    = string
  default = ""
}

variable "cis2_provider_authorize_url" {
  type    = string
  default = ""
}

variable "cis2_provider_token_url" {
  type    = string
  default = ""
}

variable "cis2_provider_user_info_url" {
  type    = string
  default = ""
}

variable "cis2_provider_jwks_uri" {
  type    = string
  default = ""
}

variable "cis2_client_callback_urls" {
  type    = list(string)
  default = [""]
}

variable "cis2_client_signout_urls" {
  type    = list(string)
  default = [""]
}

variable "document_zip_trace_ttl_in_days" {
  type    = number
  default = 90
}

variable "sqs_endpoint" {
  type    = string
  default = ""
}

variable "cognito_key_id" {
  type    = string
  default = ""
}

variable "cloud_storage_security_agent_role_arn" {
  type    = string
  default = ""
}

variable "quarantine_bucket_name" {
  type    = string
  default = ""
}

variable "virus_scanner_is_stubbed" {
  type    = string
  default = "false"
}
