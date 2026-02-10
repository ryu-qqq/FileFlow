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
    key            = "fileflow/ecs-web-api/terraform.tfstate"
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

variable "web_api_cpu" {
  description = "CPU units for web-api task"
  type        = number
  default     = 512
}

variable "web_api_memory" {
  description = "Memory for web-api task"
  type        = number
  default     = 1024
}

variable "web_api_desired_count" {
  description = "Desired count for web-api service"
  type        = number
  default     = 2
}

variable "image_tag" {
  description = "Docker image tag to deploy. Auto-set by GitHub Actions build-and-deploy.yml. Format: {component}-{build-number}-{git-sha}"
  type        = string
  default     = "web-api-92-f08d571"  # Fallback only - GitHub Actions will override this

  validation {
    condition     = can(regex("^web-api-[0-9]+-[a-f0-9]+$", var.image_tag))
    error_message = "Image tag must follow format: web-api-{build-number}-{git-sha} (e.g., web-api-92-f08d571)"
  }
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

# NOTE: ALB/Route53 제거로 인해 아래 리소스들은 더 이상 사용되지 않음
# API Gateway를 통한 외부 접근으로 전환
# data "aws_ssm_parameter" "public_subnets" {
#   name = "/shared/network/public-subnets"
# }
# data "aws_ssm_parameter" "certificate_arn" {
#   name = "/shared/network/certificate-arn"
# }
# data "aws_ssm_parameter" "route53_zone_id" {
#   name = "/shared/network/route53-zone-id"
# }

# ========================================
# RDS Configuration (MySQL)
# ========================================

# RDS Proxy endpoint from SSM Parameter Store
data "aws_ssm_parameter" "rds_proxy_endpoint" {
  name = "/shared/rds/proxy-endpoint"
}

# Fileflow-specific Secrets Manager secret
data "aws_secretsmanager_secret" "rds" {
  name = "fileflow/rds/credentials"
}

data "aws_secretsmanager_secret_version" "rds" {
  secret_id = data.aws_secretsmanager_secret.rds.id
}

# ========================================
# S3 Bucket Reference (from SSM Parameters)
# ========================================
data "aws_ssm_parameter" "s3_bucket_name" {
  name = "/${var.project_name}/s3/uploads-bucket-name"
}

# ========================================
# SQS Queue References (from SSM Parameters)
# ========================================
data "aws_ssm_parameter" "external_download_queue_url" {
  name = "/${var.project_name}/sqs/external-download-queue-url"
}

data "aws_ssm_parameter" "file_processing_queue_url" {
  name = "/${var.project_name}/sqs/file-processing-queue-url"
}

# ========================================
# Monitoring Configuration (AMP)
# ========================================
data "aws_ssm_parameter" "amp_workspace_arn" {
  name = "/shared/monitoring/amp-workspace-arn"
}

data "aws_ssm_parameter" "amp_remote_write_url" {
  name = "/shared/monitoring/amp-remote-write-url"
}

# ========================================
# Locals
# ========================================
locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  private_subnets = split(",", data.aws_ssm_parameter.private_subnets.value)

  # NOTE: ALB/Route53 제거로 인해 아래 locals는 더 이상 사용되지 않음
  # public_subnets  = split(",", data.aws_ssm_parameter.public_subnets.value)
  # certificate_arn = data.aws_ssm_parameter.certificate_arn.value
  # route53_zone_id = data.aws_ssm_parameter.route53_zone_id.value
  # fqdn            = "files.set-of.com"

  # RDS Configuration (MySQL)
  # Using RDS Proxy for connection pooling and failover resilience
  rds_credentials = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_host        = data.aws_ssm_parameter.rds_proxy_endpoint.value
  rds_port        = "3306"
  rds_dbname      = "fileflow"
  rds_username    = local.rds_credentials.username

  # Redis Configuration
  redis_host = "fileflow-redis-prod.j9czrc.0001.apn2.cache.amazonaws.com"
  redis_port = 6379

  # AMP Configuration
  amp_workspace_arn     = data.aws_ssm_parameter.amp_workspace_arn.value
  amp_remote_write_url  = data.aws_ssm_parameter.amp_remote_write_url.value
}
