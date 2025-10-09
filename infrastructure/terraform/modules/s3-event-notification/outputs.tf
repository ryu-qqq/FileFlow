output "notification_id" {
  description = "The ID of the S3 bucket notification configuration"
  value       = aws_s3_bucket_notification.upload_events.id
}

output "notification_events" {
  description = "List of events that trigger notifications"
  value       = var.events
}
