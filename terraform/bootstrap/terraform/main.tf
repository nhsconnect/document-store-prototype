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

  // Domain Setup
  
}

