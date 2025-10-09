variable "bucket_id" {
  description = "The ID of the S3 bucket"
  type        = string
}

variable "bucket_arn" {
  description = "The ARN of the S3 bucket"
  type        = string
}

variable "queue_arn" {
  description = "The ARN of the SQS queue to receive notifications"
  type        = string
}

variable "filter_prefix" {
  description = "Object key name prefix to filter notifications"
  type        = string
  default     = ""
}

variable "filter_suffix" {
  description = "Object key name suffix to filter notifications"
  type        = string
  default     = ""
}

variable "events" {
  description = "List of S3 events to trigger notifications"
  type        = list(string)
  default = [
    "s3:ObjectCreated:Put",
    "s3:ObjectCreated:CompleteMultipartUpload"
  ]
}
