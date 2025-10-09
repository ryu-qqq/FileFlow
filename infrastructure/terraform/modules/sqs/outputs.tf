output "queue_id" {
  description = "The URL of the SQS queue"
  value       = aws_sqs_queue.main.id
}

output "queue_arn" {
  description = "The ARN of the SQS queue"
  value       = aws_sqs_queue.main.arn
}

output "queue_name" {
  description = "The name of the SQS queue"
  value       = aws_sqs_queue.main.name
}

output "dlq_id" {
  description = "The URL of the Dead Letter Queue"
  value       = aws_sqs_queue.dlq.id
}

output "dlq_arn" {
  description = "The ARN of the Dead Letter Queue"
  value       = aws_sqs_queue.dlq.arn
}

output "dlq_name" {
  description = "The name of the Dead Letter Queue"
  value       = aws_sqs_queue.dlq.name
}
