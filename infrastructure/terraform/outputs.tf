# ============================================================================
# FileFlow Infrastructure Outputs
# ============================================================================

# ============================================================================
# ALB Outputs
# ============================================================================

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = module.alb.alb_zone_id
}

output "alb_arn" {
  description = "ARN of the Application Load Balancer"
  value       = module.alb.alb_arn
}

output "alb_security_group_id" {
  description = "Security group ID of the Application Load Balancer"
  value       = aws_security_group.alb.id
}

# ============================================================================
# ECS Outputs
# ============================================================================

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.fileflow.name
}

output "ecs_cluster_arn" {
  description = "ARN of the ECS cluster"
  value       = aws_ecs_cluster.fileflow.arn
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs_service.service_name
}

output "ecs_service_id" {
  description = "ID of the ECS service"
  value       = module.ecs_service.service_id
}

output "ecs_task_definition_arn" {
  description = "ARN of the ECS task definition"
  value       = module.ecs_service.task_definition_arn
}

output "ecs_task_role_arn" {
  description = "ARN of the ECS task role"
  value       = aws_iam_role.ecs_task_role.arn
}

output "ecs_execution_role_arn" {
  description = "ARN of the ECS execution role"
  value       = aws_iam_role.ecs_execution_role.arn
}

output "ecs_tasks_security_group_id" {
  description = "Security group ID of the ECS tasks"
  value       = aws_security_group.ecs_tasks.id
}

# ============================================================================
# Shared RDS Outputs
# ============================================================================

output "shared_rds_endpoint" {
  description = "Shared RDS instance endpoint"
  value       = data.aws_db_instance.shared.endpoint
  sensitive   = true
}

output "shared_rds_address" {
  description = "Shared RDS instance address"
  value       = data.aws_db_instance.shared.address
  sensitive   = true
}

output "shared_rds_port" {
  description = "Shared RDS instance port"
  value       = data.aws_db_instance.shared.port
}

output "fileflow_db_name" {
  description = "FileFlow database name in shared RDS"
  value       = var.db_name
}

# ============================================================================
# ElastiCache Redis Outputs
# ============================================================================

output "redis_endpoint" {
  description = "ElastiCache Redis endpoint"
  value       = module.redis.endpoint
  sensitive   = true
}

output "redis_port" {
  description = "ElastiCache Redis port"
  value       = module.redis.port
}

output "redis_security_group_id" {
  description = "Security group ID of the ElastiCache Redis cluster"
  value       = aws_security_group.redis.id
}

# ============================================================================
# S3 Outputs
# ============================================================================

output "s3_storage_bucket_name" {
  description = "Name of the S3 storage bucket"
  value       = module.fileflow_bucket.bucket_id
}

output "s3_storage_bucket_arn" {
  description = "ARN of the S3 storage bucket"
  value       = module.fileflow_bucket.bucket_arn
}

output "s3_logs_bucket_name" {
  description = "Name of the S3 logs bucket"
  value       = module.fileflow_logs_bucket.bucket_id
}

output "s3_logs_bucket_arn" {
  description = "ARN of the S3 logs bucket"
  value       = module.fileflow_logs_bucket.bucket_arn
}

# ============================================================================
# SQS Outputs
# ============================================================================

output "sqs_file_processing_queue_url" {
  description = "URL of the file processing SQS queue"
  value       = module.file_processing_queue.queue_url
}

output "sqs_file_processing_queue_arn" {
  description = "ARN of the file processing SQS queue"
  value       = module.file_processing_queue.queue_arn
}

output "sqs_file_upload_queue_url" {
  description = "URL of the file upload SQS queue"
  value       = module.file_upload_queue.queue_url
}

output "sqs_file_upload_queue_arn" {
  description = "ARN of the file upload SQS queue"
  value       = module.file_upload_queue.queue_arn
}

output "sqs_file_completion_queue_url" {
  description = "URL of the file completion SQS queue"
  value       = module.file_completion_queue.queue_url
}

output "sqs_file_completion_queue_arn" {
  description = "ARN of the file completion SQS queue"
  value       = module.file_completion_queue.queue_arn
}

# ============================================================================
# Database Credentials Outputs
# ============================================================================

output "fileflow_db_credentials_arn" {
  description = "ARN of the fileflow database credentials secret"
  value       = aws_secretsmanager_secret.fileflow_db_credentials.arn
  sensitive   = true
}

output "fileflow_db_credentials_name" {
  description = "Name of the fileflow database credentials secret"
  value       = aws_secretsmanager_secret.fileflow_db_credentials.name
}

# ============================================================================
# CloudWatch Log Groups Outputs
# ============================================================================

output "application_log_group_name" {
  description = "Name of the application CloudWatch log group"
  value       = aws_cloudwatch_log_group.app.name
}

output "application_log_group_arn" {
  description = "ARN of the application CloudWatch log group"
  value       = aws_cloudwatch_log_group.app.arn
}

output "ecs_exec_log_group_name" {
  description = "Name of the ECS Exec CloudWatch log group"
  value       = aws_cloudwatch_log_group.ecs_exec.name
}

output "ecs_exec_log_group_arn" {
  description = "ARN of the ECS Exec CloudWatch log group"
  value       = aws_cloudwatch_log_group.ecs_exec.arn
}
