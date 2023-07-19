terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.52.0"
    }
  }
  backend "s3" {
    bucket         = "prs-dev-terraform-state"
    dynamodb_table = "prs-dev-terraform-state-locking"
    region         = "eu-west-2"
    key            = "bootstrap/terraform.tfstate"
    encrypt        = true
  }
}

// Domain Setup
resource "aws_route53_zone" "arf_zone" {
  name = "access-request-fulfilment.patient-deductions.nhs.uk"
}

output "arf_dns_zone_id" {
  value = aws_route53_zone.arf_zone.zone_id
}
output "arf_dns_name_zones" {
  value = aws_route53_zone.arf_zone.name_servers
}

