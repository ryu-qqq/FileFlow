# Dead Letter Queue
resource "aws_sqs_queue" "dlq" {
  name                      = "${var.queue_name}-dlq"
  message_retention_seconds = var.message_retention_seconds

  tags = merge(
    var.tags,
    {
      Name        = "${var.queue_name}-dlq"
      Environment = var.environment
      Purpose     = "Dead Letter Queue"
    }
  )
}

# Main Queue
resource "aws_sqs_queue" "main" {
  name                       = var.queue_name
  message_retention_seconds  = var.message_retention_seconds
  visibility_timeout_seconds = var.visibility_timeout_seconds

  # Dead Letter Queue configuration
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = var.max_receive_count
  })

  tags = merge(
    var.tags,
    {
      Name        = var.queue_name
      Environment = var.environment
      Purpose     = "File Upload Events"
    }
  )
}

# Queue Policy to allow S3 to send messages
resource "aws_sqs_queue_policy" "main" {
  queue_url = aws_sqs_queue.main.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowS3ToSendMessage"
        Effect = "Allow"
        Principal = {
          Service = "s3.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.main.arn
        Condition = {
          ArnLike = {
            "aws:SourceArn" = "arn:aws:s3:::*"
          }
        }
      }
    ]
  })
}
