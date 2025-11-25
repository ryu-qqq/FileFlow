# ========================================
# ECS scheduler Outputs
# ========================================

output "service_name" {
  description = "ECS scheduler service name"
  value       = aws_ecs_service.scheduler.name
}

output "service_arn" {
  description = "ECS scheduler service ARN"
  value       = aws_ecs_service.scheduler.id
}

output "task_definition_arn" {
  description = "Task definition ARN"
  value       = aws_ecs_task_definition.scheduler.arn
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = aws_cloudwatch_log_group.scheduler.name
}
