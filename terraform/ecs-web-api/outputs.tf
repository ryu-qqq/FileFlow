# ========================================
# ECS web-api Outputs
# ========================================
# NOTE: ALB/Route53 관련 outputs 제거됨
# API Gateway를 통한 외부 접근, Service Discovery를 통한 내부 접근

output "service_name" {
  description = "ECS service name"
  value       = module.ecs_service.service_name
}

output "service_arn" {
  description = "ECS service ARN"
  value       = module.ecs_service.service_id
}

output "task_definition_arn" {
  description = "Task definition ARN"
  value       = module.ecs_service.task_definition_arn
}

output "service_discovery_name" {
  description = "Service Discovery DNS name for internal communication"
  value       = "web-api.connectly.local"
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.web_api_logs.log_group_name
}

output "kms_key_arn" {
  description = "KMS key ARN for logs encryption"
  value       = aws_kms_key.logs.arn
}
