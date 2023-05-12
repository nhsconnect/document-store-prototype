variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "environment" {
  type = string
}

variable "black_hole_address" {
  type = string
  default = "198.51.100.0/24"
  description = "using reserved address that does not lead anywhere to make sure CloudStorageSecurity console is not available"
}

variable "public_address" {
  type = string
  default = "0.0.0.0/0"
  description = "using public address to make sure CloudStorageSecurity console is available"
}