terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend configuration for state management
  # Uncomment and configure when ready to use remote state
  # backend "s3" {
  #   bucket         = "fileflow-terraform-state-prod"
  #   key            = "prod/s3-event-notification/terraform.tfstate"
  #   region         = "ap-northeast-2"
  #   encrypt        = true
  #   dynamodb_table = "fileflow-terraform-locks"
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "FileFlow"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
