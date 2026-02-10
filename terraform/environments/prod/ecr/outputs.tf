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

output "download_worker_repository_url" {
  description = "ECR repository URL for download-worker"
  value       = module.ecr_download_worker.repository_url
}

output "download_worker_repository_arn" {
  description = "ECR repository ARN for download-worker"
  value       = module.ecr_download_worker.repository_arn
}

output "resizing_worker_repository_url" {
  description = "ECR repository URL for resizing-worker"
  value       = module.ecr_resizing_worker.repository_url
}

output "resizing_worker_repository_arn" {
  description = "ECR repository ARN for resizing-worker"
  value       = module.ecr_resizing_worker.repository_arn
}

# KMS key output removed - encryption disabled to prevent ECR replacement
