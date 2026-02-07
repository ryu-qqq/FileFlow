# ========================================
# ECS download-worker Outputs (stage)
# ========================================

output "service_name" {
  description = "ECS download-worker service name"
  value       = module.download_worker_service.service_name
}

output "service_arn" {
  description = "ECS download-worker service ARN"
  value       = module.download_worker_service.service_id
}

output "task_definition_arn" {
  description = "Task definition ARN"
  value       = module.download_worker_service.task_definition_arn
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.download_worker_logs.log_group_name
}

output "kms_key_arn" {
  description = "KMS key ARN for logs encryption"
  value       = aws_kms_key.logs.arn
}

output "task_role_arn" {
  description = "ECS task role ARN"
  value       = module.download_worker_task_role.role_arn
}

output "execution_role_arn" {
  description = "ECS task execution role ARN"
  value       = module.download_worker_task_execution_role.role_arn
}
