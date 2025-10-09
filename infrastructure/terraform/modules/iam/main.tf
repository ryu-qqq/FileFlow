# IAM Role for Application
resource "aws_iam_role" "app_role" {
  name = var.role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = var.trusted_service
        }
      }
    ]
  })

  tags = merge(
    var.tags,
    {
      Name        = var.role_name
      Environment = var.environment
    }
  )
}

# IAM Policy for SQS Access
resource "aws_iam_policy" "sqs_access" {
  name        = "${var.role_name}-sqs-access"
  description = "Policy for application to receive messages from SQS"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowSQSReceive"
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:ChangeMessageVisibility"
        ]
        Resource = var.queue_arn
      }
    ]
  })

  tags = merge(
    var.tags,
    {
      Name        = "${var.role_name}-sqs-access"
      Environment = var.environment
    }
  )
}

# Attach Policy to Role
resource "aws_iam_role_policy_attachment" "sqs_access" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.sqs_access.arn
}
