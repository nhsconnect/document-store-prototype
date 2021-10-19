variable "region" {
  type    = string
  default = "eu-west-2"
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
