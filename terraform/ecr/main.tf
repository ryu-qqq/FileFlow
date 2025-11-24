# ========================================
# ECR Repositories for fileflow
# ========================================
# Container registries for:
# - web-api: REST API server
# - scheduler: Background scheduler
# ========================================

data "aws_caller_identity" "current" {}

# ========================================
# ECR Repository: web-api (using infrastructure module)
# ========================================
module "ecr_web_api" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-web-api-${var.environment}"
  image_tag_mutability = "IMMUTABLE"
  scan_on_push         = true

  # Governance tags
  environment  = var.environment
  service_name = "${var.project_name}-web-api"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
  data_class   = "confidential"
  project      = "infrastructure"

  # Lifecycle policy
  enable_lifecycle_policy    = true
  max_image_count            = 30
  untagged_image_expiry_days = 7

  # SSM parameter for cross-stack reference
  create_ssm_parameter = true
}

# ========================================
# ECR Repository: scheduler (using infrastructure module)
# ========================================
module "ecr_scheduler" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-scheduler-${var.environment}"
  image_tag_mutability = "IMMUTABLE"
  scan_on_push         = true

  # Governance tags
  environment  = var.environment
  service_name = "${var.project_name}-scheduler"
  team         = "platform-team"
  owner        = "platform@ryuqqq.com"
  cost_center  = "engineering"
  data_class   = "confidential"
  project      = "infrastructure"

  # Lifecycle policy
  enable_lifecycle_policy    = true
  max_image_count            = 30
  untagged_image_expiry_days = 7

  # SSM parameter for cross-stack reference
  create_ssm_parameter = true
}
