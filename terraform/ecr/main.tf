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

  repository_name      = "${var.project_name}-web-api-${var.environment}"
  image_tag_mutability = "IMMUTABLE"
  scan_on_push         = true

  # Governance tags
  environment     = var.environment
  service_name    = "${var.project_name}-web-api"
  owner           = "platform@ryuqqq.com"
  cost_center     = "engineering"
  data_class      = "confidential"
  lifecycle_stage = "production"
  project         = "infrastructure"

  # Lifecycle policy
  lifecycle_policy_enabled = true
  max_image_count         = 30
  untagged_image_days     = 7

  # Repository policy
  create_repository_policy = true
  repository_policy_principals = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]

  # SSM parameter for cross-stack reference
  create_ssm_parameter = true
}

# ========================================
# ECR Repository: scheduler (using infrastructure module)
# ========================================
module "ecr_scheduler" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  repository_name      = "${var.project_name}-scheduler-${var.environment}"
  image_tag_mutability = "IMMUTABLE"
  scan_on_push         = true

  # Governance tags
  environment     = var.environment
  service_name    = "${var.project_name}-scheduler"
  owner           = "platform@ryuqqq.com"
  cost_center     = "engineering"
  data_class      = "confidential"
  lifecycle_stage = "production"
  project         = "infrastructure"

  # Lifecycle policy
  lifecycle_policy_enabled = true
  max_image_count         = 30
  untagged_image_days     = 7

  # Repository policy
  create_repository_policy = true
  repository_policy_principals = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]

  # SSM parameter for cross-stack reference
  create_ssm_parameter = true
}
