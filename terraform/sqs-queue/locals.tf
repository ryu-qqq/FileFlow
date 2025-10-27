locals {
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = "platform-team@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "permanent"
    DataClass   = "internal"
  }

  queue_name = "${var.service_name}-prod"
  dlq_name   = "${var.service_name}-dlq-prod"
}
