variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment (dev/staging/prod)"
  type        = string
  default     = "prod"
}

variable "service_name" {
  description = "Service name"
  type        = string
  default     = "fileflow"
}

variable "message_retention" {
  description = "Message retention period in seconds"
  type        = number
  default     = 345600
}

variable "visibility_timeout" {
  description = "Visibility timeout in seconds"
  type        = number
  default     = 30
}

# KMS Key for SQS encryption
resource "aws_kms_key" "sqs" {
  description             = "KMS key for ${local.queue_name} SQS encryption"
  enable_key_rotation     = true
  deletion_window_in_days = 30

  tags = merge(
    local.required_tags,
    {
      Name      = "kms-${local.queue_name}"
      Component = "sqs-encryption"
    }
  )
}

resource "aws_kms_alias" "sqs" {
  name          = "alias/${local.queue_name}"
  target_key_id = aws_kms_key.sqs.key_id
}

# Dead Letter Queue
resource "aws_sqs_queue" "dlq" {
  name = local.dlq_name


  message_retention_seconds = 1209600  # 14 days
  visibility_timeout_seconds = var.visibility_timeout

  kms_master_key_id                 = aws_kms_key.sqs.id
  kms_data_key_reuse_period_seconds = 300

  tags = merge(
    local.required_tags,
    {
      Name      = local.dlq_name
      Component = "dead-letter-queue"
    }
  )
}

# Main Queue
resource "aws_sqs_queue" "main" {
  name = local.queue_name


  message_retention_seconds  = var.message_retention
  visibility_timeout_seconds = var.visibility_timeout
  delay_seconds             = 0
  receive_wait_time_seconds = 20  # Long polling

  max_message_size = 262144  # 256 KB

  kms_master_key_id                 = aws_kms_key.sqs.id
  kms_data_key_reuse_period_seconds = 300

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })

  tags = merge(
    local.required_tags,
    {
      Name      = local.queue_name
      Component = "sqs-queue"
    }
  )
}

# Queue Policy
resource "aws_sqs_queue_policy" "main" {
  queue_url = aws_sqs_queue.main.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DenyInsecureTransport"
        Effect = "Deny"
        Principal = "*"
        Action = "sqs:*"
        Resource = aws_sqs_queue.main.arn
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "dlq" {
  queue_url = aws_sqs_queue.dlq.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DenyInsecureTransport"
        Effect = "Deny"
        Principal = "*"
        Action = "sqs:*"
        Resource = aws_sqs_queue.dlq.arn
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      }
    ]
  })
}

# CloudWatch Alarms
resource "aws_cloudwatch_metric_alarm" "queue_depth" {
  alarm_name          = "${local.queue_name}-depth"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Average"
  threshold           = 1000
  alarm_description   = "Queue depth is too high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.main.name
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.queue_name}-depth-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "message_age" {
  alarm_name          = "${local.queue_name}-age"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Maximum"
  threshold           = 3600  # 1 hour
  alarm_description   = "Messages are aging in the queue"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.main.name
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.queue_name}-age-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "dlq_depth" {
  alarm_name          = "${local.dlq_name}-depth"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 60
  statistic           = "Sum"
  threshold           = 0
  alarm_description   = "Messages in dead letter queue"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.dlq.name
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.dlq_name}-depth-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

# SSM Parameters
resource "aws_ssm_parameter" "queue_url" {
  name        = "/fileflow/prod/sqs/queue-url"
  description = "SQS queue URL for fileflow"
  type        = "String"
  value       = aws_sqs_queue.main.url

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.queue_name}-url"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "queue_arn" {
  name        = "/fileflow/prod/sqs/queue-arn"
  description = "SQS queue ARN for fileflow"
  type        = "String"
  value       = aws_sqs_queue.main.arn

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.queue_name}-arn"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "dlq_url" {
  name        = "/fileflow/prod/sqs/dlq-url"
  description = "Dead letter queue URL for fileflow"
  type        = "String"
  value       = aws_sqs_queue.dlq.url

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.dlq_name}-url"
      Component = "parameter-store"
    }
  )
}
