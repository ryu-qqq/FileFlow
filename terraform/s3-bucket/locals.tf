locals {
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = "platform-team@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "permanent"
    DataClass   = "internal"
  }

  bucket_name = "${var.service_name}-${var.bucket_purpose}-${var.environment}"
}
