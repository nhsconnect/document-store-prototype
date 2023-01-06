variable "environment" {
  type = string
}

variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "lambda_jar_filename" {
  type    = string
  default = "../../app/build/libs/app.jar"
}

variable "api_gateway_stage" {
  type    = string
  default = "prod"
}

variable "pds_fhir_sandbox_url" {
  type    = string
  default = "https://sandbox.api.service.nhs.uk/personal-demographics/FHIR/R4/"
}

variable "pds_fhir_is_stubbed" {
  type    = string
  default = "true"
}
