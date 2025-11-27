# ========================================
# ECR Outputs
# ========================================

output "web_api_repository_url" {
  description = "ECR repository URL for web-api"
  value       = module.ecr_web_api.repository_url
}

output "web_api_repository_arn" {
  description = "ECR repository ARN for web-api"
  value       = module.ecr_web_api.repository_arn
}

output "scheduler_repository_url" {
  description = "ECR repository URL for scheduler"
  value       = module.ecr_scheduler.repository_url
}

output "scheduler_repository_arn" {
  description = "ECR repository ARN for scheduler"
  value       = module.ecr_scheduler.repository_arn
}

output "kms_key_arn" {
  description = "KMS key ARN for ECR encryption"
  value       = aws_kms_key.ecr.arn
}
