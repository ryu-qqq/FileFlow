# ========================================
# ECS scheduler Outputs
# ========================================

output "service_name" {
  description = "ECS scheduler service name"
  value       = module.scheduler_service.service_name
}

output "service_arn" {
  description = "ECS scheduler service ARN"
  value       = module.scheduler_service.service_id
}

output "task_definition_arn" {
  description = "Task definition ARN"
  value       = module.scheduler_service.task_definition_arn
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.scheduler_logs.log_group_name
}

output "kms_key_arn" {
  description = "KMS key ARN for logs encryption"
  value       = aws_kms_key.logs.arn
}
