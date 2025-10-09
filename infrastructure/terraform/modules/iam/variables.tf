variable "role_name" {
  description = "Name of the IAM role for application"
  type        = string
}

variable "environment" {
  description = "Environment name (dev, prod)"
  type        = string
}

variable "queue_arn" {
  description = "ARN of the SQS queue to grant access to"
  type        = string
}

variable "trusted_service" {
  description = "AWS service that can assume this role"
  type        = string
  default     = "ec2.amazonaws.com"
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}
