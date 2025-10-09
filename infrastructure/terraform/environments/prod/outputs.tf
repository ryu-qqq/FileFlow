output "sqs_queue_url" {
  description = "URL of the SQS queue"
  value       = module.upload_events_queue.queue_id
}

output "sqs_queue_arn" {
  description = "ARN of the SQS queue"
  value       = module.upload_events_queue.queue_arn
}

output "sqs_queue_name" {
  description = "Name of the SQS queue"
  value       = module.upload_events_queue.queue_name
}

output "dlq_url" {
  description = "URL of the Dead Letter Queue"
  value       = module.upload_events_queue.dlq_id
}

output "dlq_arn" {
  description = "ARN of the Dead Letter Queue"
  value       = module.upload_events_queue.dlq_arn
}

output "app_role_arn" {
  description = "ARN of the application IAM role"
  value       = module.app_iam_role.role_arn
}

output "app_role_name" {
  description = "Name of the application IAM role"
  value       = module.app_iam_role.role_name
}

output "s3_event_notification_id" {
  description = "ID of the S3 bucket notification configuration"
  value       = module.s3_event_notification.notification_id
}
