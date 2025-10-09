variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "fileflow"
}

variable "s3_bucket_name" {
  description = "Name of the S3 bucket for file uploads"
  type        = string
}

variable "queue_name" {
  description = "Name of the SQS queue"
  type        = string
  default     = "fileflow-upload-events"
}

variable "app_role_name" {
  description = "Name of the IAM role for the application"
  type        = string
  default     = "fileflow-app-role"
}
