# ========================================
# ECR Outputs
# ========================================

output "web_api_repository_url" {
  description = "ECR repository URL for web-api"
  value       = aws_ecr_repository.web_api.repository_url
}

output "web_api_repository_arn" {
  description = "ECR repository ARN for web-api"
  value       = aws_ecr_repository.web_api.arn
}

output "scheduler_repository_url" {
  description = "ECR repository URL for scheduler"
  value       = aws_ecr_repository.scheduler.repository_url
}

output "scheduler_repository_arn" {
  description = "ECR repository ARN for scheduler"
  value       = aws_ecr_repository.scheduler.arn
}
