terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "prod-connectly"
    key            = "fileflow-app/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "prod-connectly-tf-lock"
    kms_key_id     = "alias/terraform-state"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project    = "FileFlow"
      ManagedBy  = "Terraform"
      Repository = "fileflow"
    }
  }
}
