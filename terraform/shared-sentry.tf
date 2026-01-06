# ============================================================================
# Sentry Configuration (Error Tracking)
# ============================================================================
# Sentry DSN for FileFlow project error tracking and performance monitoring
# SSM Parameter must be created manually:
#   aws ssm put-parameter --name "/fileflow/sentry/dsn" --value "YOUR_DSN" --type "String"
# ============================================================================

data "aws_ssm_parameter" "sentry_dsn" {
  name = "/fileflow/sentry/dsn"
}

# Output for use in ECS services
locals {
  sentry_dsn = data.aws_ssm_parameter.sentry_dsn.value
}
