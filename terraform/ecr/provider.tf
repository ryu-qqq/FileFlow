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

  # S3 Backend (Infrastructure 레포지토리와 동일한 bucket 사용)
  backend "s3" {
    bucket         = "terraform-state-ryuqqq-infrastructure-prod"
    key            = "fileflow/ecr/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "terraform-state-lock-ryuqqq-infrastructure-prod"
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
