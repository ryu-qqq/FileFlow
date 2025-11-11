# ============================================================================
# Local Variables
# ============================================================================

locals {
  # Service Configuration
  service_name = "fileflow-api"
  environment  = "prod"

  # Common Tags (Required by governance)
  required_tags = {
    Environment = "prod"
    Service     = "fileflow-api"
    Owner       = "platform-team@example.com"
    CostCenter  = "engineering"
    Lifecycle   = "production"
    DataClass   = "internal"
    ManagedBy   = "terraform"
    Repository  = "infrastructure"
  }

  # Resource Naming
  name_prefix = "${local.service_name}-${local.environment}"

  # Container Configuration
  container_name = "fileflow"
  container_port = 8080

  # CloudWatch Log Group
  log_group_name     = "/aws/ecs/${local.service_name}"
  log_retention_days = 30
}
