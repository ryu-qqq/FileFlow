# Data source to get existing S3 bucket
data "aws_s3_bucket" "uploads" {
  bucket = var.s3_bucket_name
}

# SQS Queue Module
module "upload_events_queue" {
  source = "../../modules/sqs"

  queue_name                 = "${var.project_name}-${var.environment}-${var.queue_name}"
  environment                = var.environment
  message_retention_seconds  = 1209600 # 14 days
  visibility_timeout_seconds = 30
  max_receive_count          = 3
  s3_bucket_arn              = data.aws_s3_bucket.uploads.arn

  tags = {
    Project = var.project_name
  }
}

# S3 Event Notification Module
module "s3_event_notification" {
  source = "../../modules/s3-event-notification"

  bucket_id  = data.aws_s3_bucket.uploads.id
  bucket_arn = data.aws_s3_bucket.uploads.arn
  queue_arn  = module.upload_events_queue.queue_arn

  events = [
    "s3:ObjectCreated:Put",
    "s3:ObjectCreated:CompleteMultipartUpload"
  ]

  # Optional: Add prefix filter if needed
  # filter_prefix = "uploads/"

  # Optional: Add suffix filter if needed
  # filter_suffix = ".jpg"
}

# IAM Role for Application
module "app_iam_role" {
  source = "../../modules/iam"

  role_name       = "${var.project_name}-${var.environment}-${var.app_role_name}"
  environment     = var.environment
  queue_arn       = module.upload_events_queue.queue_arn
  trusted_service = var.app_trusted_service

  tags = {
    Project = var.project_name
  }
}
