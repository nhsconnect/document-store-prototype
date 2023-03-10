terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.52.0"
    }
  }
  backend "s3" {}
}

provider "aws" {
  region = var.region
}

resource "aws_vpc" "virus_scanning_vpc" {
  cidr_block = "10.0.0.0/16"
  tags = {
    Name = "Virus Scanning Default VPC"
  }
}

resource "aws_subnet" "virus_scanning_subnet1" {
  availability_zone = "eu-west-2a"
  vpc_id = aws_vpc.virus_scanning_vpc.id
  cidr_block = "10.0.1.0/24"

  tags = {
    Name = "Subnet for eu-west-2a"
  }
}

resource "aws_subnet" "virus_scanning_subnet2" {
  availability_zone = "eu-west-2b"
  vpc_id = aws_vpc.virus_scanning_vpc.id
  cidr_block = "10.0.2.0/24"

  tags = {
    Name = "Subnet for eu-west-2b"
  }
}

data "aws_ssm_parameter" "cloud_security_email" {
  name = "/prs/${var.environment}/user-input/cloud-security-email"
}