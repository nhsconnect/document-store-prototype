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
