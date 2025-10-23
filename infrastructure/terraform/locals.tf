# ============================================================================
# Local Values
# ============================================================================

locals {
  # Naming
  name_prefix  = "${var.service_name}-${var.environment}"
  service_name = var.service_name

  # Account
  account_id = data.aws_caller_identity.current.account_id

  # Network (from SSM Parameter)
  vpc_id             = data.aws_ssm_parameter.vpc_id.value
  private_subnet_ids = split(",", data.aws_ssm_parameter.private_subnet_ids.value)
  public_subnet_ids  = split(",", data.aws_ssm_parameter.public_subnet_ids.value)

  # KMS Keys (from SSM Parameter)
  cloudwatch_key_arn  = data.aws_ssm_parameter.cloudwatch_logs_key_arn.value
  secrets_key_arn     = data.aws_ssm_parameter.secrets_manager_key_arn.value
  rds_key_arn         = data.aws_ssm_parameter.rds_key_arn.value
  s3_key_arn          = data.aws_ssm_parameter.s3_key_arn.value
  sqs_key_arn         = data.aws_ssm_parameter.sqs_key_arn.value
  ssm_key_arn         = data.aws_ssm_parameter.ssm_key_arn.value
  elasticache_key_arn = data.aws_ssm_parameter.elasticache_key_arn.value

  # ECR (from SSM Parameter)
  ecr_repository_url = data.aws_ssm_parameter.ecr_repository_url.value

  # Required Tags
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = var.tags_owner
    CostCenter  = var.tags_cost_center
    Team        = var.tags_team
    Lifecycle   = var.environment == "prod" ? "critical" : "non-critical"
    DataClass   = "sensitive"
    ManagedBy   = "Terraform"
    Repository  = "fileflow"
  }
}
