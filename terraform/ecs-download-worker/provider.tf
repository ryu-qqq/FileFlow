# ========================================
# Terraform Provider Configuration
# ========================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "prod-connectly"
    key            = "fileflow/ecs-download-worker/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "prod-connectly-tf-lock"
    encrypt        = true
    kms_key_id     = "arn:aws:kms:ap-northeast-2:646886795421:key/086b1677-614f-46ba-863e-23c215fb5010"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# ========================================
# Common Variables
# ========================================
variable "project_name" {
  description = "Project name"
  type        = string
  default     = "fileflow"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "worker_cpu" {
  description = "CPU units for download-worker task"
  type        = number
  default     = 512
}

variable "worker_memory" {
  description = "Memory for download-worker task"
  type        = number
  default     = 1024
}

variable "worker_desired_count" {
  description = "Desired count for download-worker tasks"
  type        = number
  default     = 1
}

variable "image_tag" {
  description = "Docker image tag to deploy (CI/CD sets this value)"
  type        = string
  default     = "download-worker-82-377de0d"
}

# ========================================
# Shared Resource References (SSM)
# ========================================
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

data "aws_ssm_parameter" "private_subnets" {
  name = "/shared/network/private-subnets"
}

# ========================================
# RDS Configuration (MySQL)
# ========================================
data "aws_secretsmanager_secret" "rds" {
  name = "fileflow/rds/credentials"
}

data "aws_secretsmanager_secret_version" "rds" {
  secret_id = data.aws_secretsmanager_secret.rds.id
}

# ========================================
# SQS Queue References
# ========================================
data "aws_sqs_queue" "download_queue" {
  name = "${var.project_name}-${var.environment}"
}

data "aws_sqs_queue" "download_dlq" {
  name = "${var.project_name}-dlq-${var.environment}"
}

# ========================================
# Locals
# ========================================
locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  private_subnets = split(",", data.aws_ssm_parameter.private_subnets.value)

  # RDS Configuration (MySQL)
  rds_credentials = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_host        = "prod-shared-mysql.cfacertspqbw.ap-northeast-2.rds.amazonaws.com"
  rds_port        = "3306"
  rds_dbname      = "fileflow"
  rds_username    = local.rds_credentials.username

  # Redis Configuration
  redis_host = "fileflow-redis-prod.j9czrc.0001.apn2.cache.amazonaws.com"
  redis_port = 6379
}
