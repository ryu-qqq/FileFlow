locals {
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = "platform-team@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "permanent"
    DataClass   = "internal"
  }

  redis_name = "${var.service_name}-redis-${var.environment}"

  # Network from SSM Parameters
  vpc_id             = data.aws_ssm_parameter.vpc_id.value
  private_subnet_ids = split(",", data.aws_ssm_parameter.private_subnet_ids.value)
}
