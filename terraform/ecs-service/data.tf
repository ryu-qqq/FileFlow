# ============================================================================
# Shared Infrastructure References
# ============================================================================
# This file references shared infrastructure managed in terraform/network/,
# terraform/kms/, etc. DO NOT modify shared infrastructure from here.
# ============================================================================

# Network Resources
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

data "aws_ssm_parameter" "private_subnet_ids" {
  name = "/shared/network/private-subnet-ids"
}

data "aws_ssm_parameter" "public_subnet_ids" {
  name = "/shared/network/public-subnet-ids"
}

# KMS Keys
data "aws_kms_key" "ecs_secrets" {
  key_id = "alias/ecs-secrets"
}

data "aws_kms_key" "cloudwatch_logs" {
  key_id = "alias/cloudwatch-logs"
}

# Shared RDS Database
data "aws_ssm_parameter" "db_instance_address" {
  name = "/shared/rds/db-instance-address"
}

data "aws_ssm_parameter" "db_instance_port" {
  name = "/shared/rds/db-instance-port"
}

data "aws_ssm_parameter" "master_password_secret_name" {
  name = "/shared/rds/master-password-secret-name"
}

data "aws_secretsmanager_secret_version" "db_password" {
  secret_id = data.aws_ssm_parameter.master_password_secret_name.value
}

# ECR Repository
data "aws_ecr_repository" "fileflow" {
  name = "fileflow"
}

# Redis (ElastiCache)
data "aws_ssm_parameter" "redis_endpoint" {
  name = "/fileflow/prod/redis/endpoint"
}

data "aws_ssm_parameter" "redis_port" {
  name = "/fileflow/prod/redis/port"
}


# ============================================================================
# Locals
# ============================================================================

locals {
  # Network
  vpc_id             = data.aws_ssm_parameter.vpc_id.value
  private_subnet_ids = split(",", data.aws_ssm_parameter.private_subnet_ids.value)
  public_subnet_ids  = split(",", data.aws_ssm_parameter.public_subnet_ids.value)

  # Shared Database
  db_address  = data.aws_ssm_parameter.db_instance_address.value
  db_port     = data.aws_ssm_parameter.db_instance_port.value
  db_password = jsondecode(data.aws_secretsmanager_secret_version.db_password.secret_string)["password"]

  # Redis Cache
  redis_endpoint = data.aws_ssm_parameter.redis_endpoint.value
  redis_port     = data.aws_ssm_parameter.redis_port.value

  # ECR Image
  ecr_image_uri = "${data.aws_ecr_repository.fileflow.repository_url}:latest"

}
