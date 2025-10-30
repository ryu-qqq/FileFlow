# ============================================================================
# Terraform & Provider Configuration
# ============================================================================

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # S3 Backend
  backend "s3" {
    bucket         = "prod-connectly"
    key            = "fileflow/ecr/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "prod-connectly-tf-lock"
  }
}

provider "aws" {
  region = "ap-northeast-2"

  default_tags {
    tags = {
      ManagedBy = "terraform"
      Project   = "fileflow"
      Component = "ecr"
    }
  }
}
