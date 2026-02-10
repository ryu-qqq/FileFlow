# ============================================================================
# Sentry Configuration (Error Tracking)
# ============================================================================

data "aws_ssm_parameter" "sentry_dsn" {
  name = "/fileflow/sentry/dsn"
}

locals {
  sentry_dsn = data.aws_ssm_parameter.sentry_dsn.value
}
