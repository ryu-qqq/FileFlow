# ============================================================================
# Data Sources - Shared Infrastructure (from Infrastructure repo)
# ============================================================================

# 공유 네트워크 정보 (SSM Parameter에서 조회)
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

data "aws_ssm_parameter" "private_subnet_ids" {
  name = "/shared/network/private-subnet-ids"
}

data "aws_ssm_parameter" "public_subnet_ids" {
  name = "/shared/network/public-subnet-ids"
}

# 공유 KMS 키 (SSM Parameter에서 조회)
data "aws_ssm_parameter" "cloudwatch_logs_key_arn" {
  name = "/shared/kms/cloudwatch-logs-key-arn"
}

data "aws_ssm_parameter" "secrets_manager_key_arn" {
  name = "/shared/kms/secrets-manager-key-arn"
}

data "aws_ssm_parameter" "rds_key_arn" {
  name = "/shared/kms/rds-key-arn"
}

data "aws_ssm_parameter" "s3_key_arn" {
  name = "/shared/kms/s3-key-arn"
}

data "aws_ssm_parameter" "sqs_key_arn" {
  name = "/shared/kms/sqs-key-arn"
}

data "aws_ssm_parameter" "ssm_key_arn" {
  name = "/shared/kms/ssm-key-arn"
}

data "aws_ssm_parameter" "elasticache_key_arn" {
  name = "/shared/kms/elasticache-key-arn"
}

# ECR 레포지토리 (SSM Parameter에서 조회)
data "aws_ssm_parameter" "ecr_repository_url" {
  name = "/shared/ecr/fileflow-repository-url"
}

# VPC 서브넷 조회 (직접 조회 방식 - SSM Parameter fallback)
data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [local.vpc_id]
  }

  tags = {
    Tier = "private"
  }
}

data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [local.vpc_id]
  }

  tags = {
    Tier = "public"
  }
}

# Account ID 조회
data "aws_caller_identity" "current" {}
