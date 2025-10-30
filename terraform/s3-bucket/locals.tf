locals {
  required_tags = {
    Environment = var.environment
    Service     = var.service_name
    Owner       = "platform-team@ryuqqq.com"
    CostCenter  = "engineering"
    Lifecycle   = "permanent"
    DataClass   = "internal"
  }

  # bucket_purpose가 비어있으면 하이픈 없이 생성
  # 비어있음: fileflow-prod
  # 설정됨: fileflow-uploads-prod
  bucket_name = var.bucket_purpose != "" ? "${var.service_name}-${var.bucket_purpose}-${var.environment}" : "${var.service_name}-${var.environment}"
}
