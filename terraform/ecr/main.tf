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
# KMS Key for ECR Encryption (DISABLED)
# ========================================
# NOTE: KMS encryption requires ECR repository replacement.
# Existing repositories use AES256. Enabling KMS would destroy
# all existing images. Keep disabled unless creating new repos.
# ========================================

# ========================================
# ECR Repository: web-api
# ========================================
module "ecr_web_api" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-web-api-${var.environment}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # Lifecycle Policy
  enable_lifecycle_policy    = true
  max_image_count            = 30
  lifecycle_tag_prefixes     = ["v", "prod", "latest"]
  untagged_image_expiry_days = 7

  # SSM Parameter for cross-stack reference
  create_ssm_parameter = true

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = "${var.project_name}-web-api"
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# ECR Repository: scheduler
# ========================================
module "ecr_scheduler" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-scheduler-${var.environment}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # Lifecycle Policy
  enable_lifecycle_policy    = true
  max_image_count            = 30
  lifecycle_tag_prefixes     = ["v", "prod", "latest"]
  untagged_image_expiry_days = 7

  # SSM Parameter for cross-stack reference
  create_ssm_parameter = true

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = "${var.project_name}-scheduler"
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}
