variable "environment" {
  type = string
}

variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "account_id" {
  type = string
}

variable "cloud_only_service_instances" {
  type    = number
  default = 1
}

variable "enable_basic_auth" {
  type    = bool
  default = true
}

variable "basic_auth_password" {
  type    = string
  default = ""
}

variable "lambda_jar_filename" {
  type    = string
  default = "../app/build/libs/app.jar"
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

variable "cognito_oidc_providers" {
  type    = list(string)
  default = []
}

variable "cognito_cis2_provider_client_id" {
  type    = string
  default = "0000"
}

variable "cognito_cis2_provider_client_secret" {
  type    = string
  default = "0000"
}

variable "cognito_cis2_provider_oidc_issuer" {
  type    = string
  default = ""
}

variable "cognito_cis2_provider_authorize_url" {
  type    = string
  default = ""
}

variable "cognito_cis2_provider_token_url" {
  type    = string
  default = ""
}

variable "cognito_cis2_provider_attributes_url" {
  type    = string
  default = ""
}

variable "cognito_cis2_provider_jwks_uri" {
  type    = string
  default = ""
}

variable "cognito_cis2_client_callback_urls" {
  type    = list(string)
  default = [""]
}

variable "cognito_cis2_client_signout_urls" {
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

variable "lambdas" {
  type    = map(object({ function_name = string }))
  default = {
    authoriser                     = { function_name = "Authoriser" }
    search_patient-details-handler = {
      function_name = "SearchPatientDetailsHandler"
    }
    create-document-reference-handler = {
      function_name = "CreateDocumentReferenceHandler"
    }
    document-uploaded-event-handler = {
      function_name = "DocumentUploadedEventHandler"
    }
    create-document-manifest-by-nhs-number-handler = {
      function_name = "CreateDocumentManifestByNhsNumberHandler"
    }
    document-reference-search-handler = {
      function_name = "DocumentReferenceSearchHandler"
    }
    retrieve-document-reference-handler = {
      function_name = "RetrieveDocumentReferenceHandler"
    }
    delete-document-reference-handler = {
      function_name = "DeleteDocumentReferenceHandler"
    }
  }
}
