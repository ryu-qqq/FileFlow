# ========================================
# ECR Repositories for fileflow (Stage)
# ========================================
# Container registries using Infrastructure module
# - web-api: REST API server
# - scheduler: Background scheduler
# - download-worker: Download processing worker
# - resizing-worker: Image resizing worker
# ========================================

# ========================================
# Local Variables
# ========================================
locals {
  ecr_name_suffix = "stage"
  ecr_environment = "staging"

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
# ECR Repository: web-api
# ========================================
module "ecr_web_api" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-web-api-${local.ecr_name_suffix}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # Encryption (AES256 for stage)
  encryption_type = "AES256"

  # Lifecycle Policy
  enable_lifecycle_policy    = true
  max_image_count            = 15
  lifecycle_tag_prefixes     = ["v", "stage", "latest"]
  untagged_image_expiry_days = 3

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

  name                 = "${var.project_name}-scheduler-${local.ecr_name_suffix}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # Encryption (AES256 for stage)
  encryption_type = "AES256"

  # Lifecycle Policy
  enable_lifecycle_policy    = true
  max_image_count            = 15
  lifecycle_tag_prefixes     = ["v", "stage", "latest"]
  untagged_image_expiry_days = 3

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

# ========================================
# ECR Repository: download-worker
# ========================================
module "ecr_download_worker" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-download-worker-${local.ecr_name_suffix}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # Encryption (AES256 for stage)
  encryption_type = "AES256"

  # Lifecycle Policy
  enable_lifecycle_policy    = true
  max_image_count            = 15
  lifecycle_tag_prefixes     = ["v", "stage", "latest"]
  untagged_image_expiry_days = 3

  # SSM Parameter for cross-stack reference
  create_ssm_parameter = true

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = "${var.project_name}-download-worker"
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# ECR Repository: resizing-worker
# ========================================
module "ecr_resizing_worker" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecr?ref=main"

  name                 = "${var.project_name}-resizing-worker-${local.ecr_name_suffix}"
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  # Encryption (AES256 for stage)
  encryption_type = "AES256"

  # Lifecycle Policy
  enable_lifecycle_policy    = true
  max_image_count            = 15
  lifecycle_tag_prefixes     = ["v", "stage", "latest"]
  untagged_image_expiry_days = 3

  # SSM Parameter for cross-stack reference
  create_ssm_parameter = true

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = "${var.project_name}-resizing-worker"
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}
