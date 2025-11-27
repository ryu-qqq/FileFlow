# ========================================
# Outputs - External Download Queue
# ========================================

output "external_download_queue_url" {
  description = "URL of the external download SQS queue"
  value       = module.external_download_queue.queue_url
}

output "external_download_queue_arn" {
  description = "ARN of the external download SQS queue"
  value       = module.external_download_queue.queue_arn
}

output "external_download_queue_name" {
  description = "Name of the external download SQS queue"
  value       = module.external_download_queue.queue_name
}

output "external_download_dlq_url" {
  description = "URL of the external download dead letter queue"
  value       = module.external_download_queue.dlq_url
}

output "external_download_dlq_arn" {
  description = "ARN of the external download dead letter queue"
  value       = module.external_download_queue.dlq_arn
}

output "external_download_dlq_name" {
  description = "Name of the external download dead letter queue"
  value       = module.external_download_queue.dlq_name
}

# ========================================
# Common Outputs
# ========================================

output "kms_key_arn" {
  description = "ARN of the KMS key used for SQS encryption"
  value       = aws_kms_key.sqs.arn
}

output "sqs_access_policy_arn" {
  description = "ARN of the IAM policy for SQS access"
  value       = aws_iam_policy.sqs_access.arn
}
