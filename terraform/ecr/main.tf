# ========================================
# ECR Repositories for fileflow
# ========================================
# Container registries using Infrastructure module
# - web-api: REST API server
# - scheduler: Background scheduler
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-ecr"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "internal"
  }
}

# ========================================
# KMS Key for ECR Encryption
# ========================================
resource "aws_kms_key" "ecr" {
  description             = "KMS key for FileFlow ECR repository encryption"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = {
    Name        = "${var.project_name}-ecr-kms-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-ecr"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

resource "aws_kms_alias" "ecr" {
  name          = "alias/${var.project_name}-ecr-${var.environment}"
  target_key_id = aws_kms_key.ecr.key_id
}

# ========================================
# ECR Repository: web-api
# ========================================
module "ecr_web_api" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-web-api-${var.environment}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # KMS Encryption (required for governance)
  kms_key_arn = aws_kms_key.ecr.arn

  # Lifecycle Policy
  enable_lifecycle_policy   = true
  max_image_count           = 30
  lifecycle_tag_prefixes    = ["v", "prod", "latest"]
  untagged_image_expiry_days = 7

  # SSM Parameter for cross-stack reference
  create_ssm_parameter = true

  # Required Tags (governance compliance)
  environment = local.common_tags.environment
  service_name = "${var.project_name}-web-api"
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}

# ========================================
# ECR Repository: scheduler
# ========================================
module "ecr_scheduler" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-scheduler-${var.environment}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # KMS Encryption (required for governance)
  kms_key_arn = aws_kms_key.ecr.arn

  # Lifecycle Policy
  enable_lifecycle_policy   = true
  max_image_count           = 30
  lifecycle_tag_prefixes    = ["v", "prod", "latest"]
  untagged_image_expiry_days = 7

  # SSM Parameter for cross-stack reference
  create_ssm_parameter = true

  # Required Tags (governance compliance)
  environment = local.common_tags.environment
  service_name = "${var.project_name}-scheduler"
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}
