# ========================================
# SQS Queue: External Download
# ========================================
# Standard SQS queue for external download messages
# With DLQ for failed message handling
# Using Infrastructure repository modules
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-sqs"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "confidential"
  }
}

# ========================================
# KMS Key for SQS Encryption
# ========================================
resource "aws_kms_key" "sqs" {
  description             = "KMS key for FileFlow SQS queue encryption"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = {
    Name        = "${var.project_name}-sqs-kms-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-sqs"
  }
}

resource "aws_kms_alias" "sqs" {
  name          = "alias/${var.project_name}-sqs-${var.environment}"
  target_key_id = aws_kms_key.sqs.key_id
}

# ========================================
# SQS Queue: External Download
# ========================================
module "external_download_queue" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/sqs?ref=main"

  name       = "${var.environment}-monitoring-sqs-${var.project_name}-external-download"
  fifo_queue = false

  # KMS Encryption (required)
  kms_key_id = aws_kms_key.sqs.arn

  # Message Configuration
  visibility_timeout_seconds = 360  # 6 minutes - enough time for download + S3 upload
  message_retention_seconds  = 345600  # 4 days
  max_message_size           = 262144  # 256 KB
  delay_seconds              = 0
  receive_wait_time_seconds  = 20  # Long polling enabled

  # DLQ Configuration
  enable_dlq                    = true
  max_receive_count             = 3  # Move to DLQ after 3 failed attempts
  dlq_message_retention_seconds = 1209600  # 14 days for DLQ

  # CloudWatch Alarms
  enable_cloudwatch_alarms         = true
  alarm_evaluation_periods         = 2
  alarm_period                     = 300  # 5 minutes
  alarm_message_age_threshold      = 600  # Alert if message older than 10 minutes
  alarm_messages_visible_threshold = 1000  # Alert if queue depth exceeds 1000
  alarm_dlq_messages_threshold     = 1  # Alert on any DLQ message

  # Required Tags
  environment = local.common_tags.environment
  service     = local.common_tags.service_name
  team        = local.common_tags.team
  owner       = local.common_tags.owner
  cost_center = local.common_tags.cost_center
  project     = local.common_tags.project
  data_class  = local.common_tags.data_class
}

# ========================================
# IAM Policy for SQS Access (ECS Tasks)
# ========================================
data "aws_iam_policy_document" "sqs_access" {
  statement {
    sid    = "AllowSQSAccess"
    effect = "Allow"
    actions = [
      "sqs:SendMessage",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [
      module.external_download_queue.queue_arn,
      module.external_download_queue.dlq_arn
    ]
  }

  statement {
    sid    = "AllowKMSForSQS"
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey"
    ]
    resources = [aws_kms_key.sqs.arn]
  }
}

resource "aws_iam_policy" "sqs_access" {
  name        = "${var.project_name}-sqs-access-${var.environment}"
  description = "IAM policy for FileFlow SQS queue access"
  policy      = data.aws_iam_policy_document.sqs_access.json

  tags = {
    Name        = "${var.project_name}-sqs-access-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-sqs"
  }
}

# ========================================
# SSM Parameters for Cross-Stack Reference
# ========================================
resource "aws_ssm_parameter" "external_download_queue_url" {
  name        = "/${var.project_name}/sqs/external-download-queue-url"
  description = "FileFlow external download queue URL"
  type        = "String"
  value       = module.external_download_queue.queue_url

  tags = {
    Name        = "${var.project_name}-sqs-external-download-queue-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "external_download_queue_arn" {
  name        = "/${var.project_name}/sqs/external-download-queue-arn"
  description = "FileFlow external download queue ARN"
  type        = "String"
  value       = module.external_download_queue.queue_arn

  tags = {
    Name        = "${var.project_name}-sqs-external-download-queue-arn"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "external_download_dlq_url" {
  name        = "/${var.project_name}/sqs/external-download-dlq-url"
  description = "FileFlow external download DLQ URL"
  type        = "String"
  value       = module.external_download_queue.dlq_url

  tags = {
    Name        = "${var.project_name}-sqs-external-download-dlq-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "sqs_policy_arn" {
  name        = "/${var.project_name}/sqs/access-policy-arn"
  description = "FileFlow SQS access IAM policy ARN"
  type        = "String"
  value       = aws_iam_policy.sqs_access.arn

  tags = {
    Name        = "${var.project_name}-sqs-policy-arn"
    Environment = var.environment
  }
}
