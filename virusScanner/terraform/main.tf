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
    Name = "Virus scanning VPC"
  }
}

resource "aws_subnet" "virus_scanning_subnet1" {
  availability_zone = "eu-west-2a"
  vpc_id            = aws_vpc.virus_scanning_vpc.id
  cidr_block        = "10.0.1.0/24"

  tags = {
    Name = "Virus scanning subnet for eu-west-2a"
  }
}

resource "aws_subnet" "virus_scanning_subnet2" {
  availability_zone = "eu-west-2b"
  vpc_id            = aws_vpc.virus_scanning_vpc.id
  cidr_block        = "10.0.2.0/24"

  tags = {
    Name = "Virus scanning subnet for eu-west-2b"
  }
}

resource "aws_internet_gateway" "virus_scanning_internet_gateway" {
  vpc_id = aws_vpc.virus_scanning_vpc.id

  tags = {
    Name = "Virus scanning internet gateway"
  }
}

resource "aws_route_table" "virus_scanning_route_table" {
  vpc_id = aws_vpc.virus_scanning_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.virus_scanning_internet_gateway.id
  }

  tags = {
    Name = "Virus scanning route table"
  }
}

resource "aws_route_table_association" "virus_scanning_subnet1_route_table_association" {
  subnet_id      = aws_subnet.virus_scanning_subnet1.id
  route_table_id = aws_route_table.virus_scanning_route_table.id
}

resource "aws_route_table_association" "virus_scanning_subnet2_route_table_association" {
  subnet_id      = aws_subnet.virus_scanning_subnet2.id
  route_table_id = aws_route_table.virus_scanning_route_table.id
}

data "aws_ssm_parameter" "cloud_security_admin_email" {
  name = "/prs/${var.environment}/user-input/cloud-security-admin-email"
}

resource "aws_cloudformation_stack" "s3_virus_scanning_stack" {
  name = "s3-virus-scanning-cloudformation-stack"
  parameters = {
    VPC                                = aws_vpc.virus_scanning_vpc.id
    SubnetA                            = aws_subnet.virus_scanning_subnet1.id
    SubnetB                            = aws_subnet.virus_scanning_subnet2.id
    ConsoleSecurityGroupCidrBlock      = var.black_hole_address
    Email                              = data.aws_ssm_parameter.cloud_security_admin_email.value
    OnlyScanWhenQueueThresholdExceeded = "Yes"
    MinRunningAgents                   = 0
    NumMessagesInQueueScalingThreshold = 1
    AllowAccessToAllKmsKeys            = "No"
  }
  timeouts {
    create = "60m"
    delete = "2h"
  }
  template_url = "https://css-cft.s3.amazonaws.com/ConsoleCloudFormationTemplate.yaml"
  capabilities = ["CAPABILITY_NAMED_IAM"]
}
