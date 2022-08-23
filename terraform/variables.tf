variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "cloud_only_service_instances" {
  type    = number
  default = 1
}

variable "lambda_jar_filename" {
  type    = string
  default = "../app/build/libs/app.jar"
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

variable "cognito_cis2_provider_name" {
  type    = string
  default = ""
}

variable "cognito_cis2_provider_client_id" {
  type    = string
  default = ""
}

variable "cognito_cis2_provider_client_secret" {
  type    = string
  default = ""
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