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
  default     = "upload-events"
}

variable "app_role_name" {
  description = "Name of the IAM role for the application"
  type        = string
  default     = "app-role"
}

variable "app_trusted_service" {
  description = "AWS service that can assume the application IAM role"
  type        = string
  default     = null
}
