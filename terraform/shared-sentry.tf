# ============================================================================
# Shared Infrastructure Reference: Sentry (Error Tracking)
# ============================================================================
# Sentry DSN for error tracking and performance monitoring
# SSM Parameter must be created manually or via Infrastructure repository:
#   aws ssm put-parameter --name "/shared/sentry/dsn" --value "YOUR_DSN" --type "String"
# ============================================================================

data "aws_ssm_parameter" "sentry_dsn" {
  name = "/shared/sentry/dsn"
}

# Output for use in ECS services
locals {
  sentry_dsn = data.aws_ssm_parameter.sentry_dsn.value
}
