# ============================================================================
# FILEFLOW - Outputs
# ============================================================================

# ECS Service Outputs
output "ecs_service_id" {
  description = "ECS service ID"
  value       = module.fileflow_service.service_id
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = module.fileflow_service.service_name
}

output "security_group_id" {
  description = "Security group ID for ECS tasks"
  value       = aws_security_group.fileflow.id
}

output "cloudwatch_log_group_name" {
  description = "CloudWatch log group name"
  value       = module.fileflow_logs.name
}

output "cloudwatch_log_group_arn" {
  description = "CloudWatch log group ARN"
  value       = module.fileflow_logs.arn
}

# ALB Outputs
output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.fileflow_alb.dns_name
}

output "alb_arn" {
  description = "ALB ARN"
  value       = module.fileflow_alb.arn
}

output "target_group_arn" {
  description = "Target group ARN"
  value       = module.fileflow_alb.target_group_arn
}

output "alb_security_group_id" {
  description = "ALB security group ID"
  value       = aws_security_group.fileflow_alb.id
}


