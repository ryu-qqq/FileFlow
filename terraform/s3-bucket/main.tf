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

variable "bucket_purpose" {
  description = "Bucket purpose (e.g., uploads, backups, logs)"
  type        = string
  default     = ""
}

# KMS Key for S3 encryption
# Note: GitHubActionsRole has kms:UpdateKeyDescription permission (added 2025-11-10)
resource "aws_kms_key" "s3" {
  description             = "KMS key for ${local.bucket_name} S3 bucket"
  enable_key_rotation     = true
  deletion_window_in_days = 30

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow CloudWatch Logs"
        Effect = "Allow"
        Principal = {
          Service = "logs.${data.aws_region.current.name}.amazonaws.com"
        }
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:CreateGrant",
          "kms:DescribeKey"
        ]
        Resource = "*"
        Condition = {
          ArnLike = {
            "kms:EncryptionContext:aws:logs:arn" = "arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/aws/s3/*"
          }
        }
      },
      {
        Sid    = "Allow S3 Service"
        Effect = "Allow"
        Principal = {
          Service = "s3.amazonaws.com"
        }
        Action = [
          "kms:Decrypt",
          "kms:GenerateDataKey"
        ]
        Resource = "*"
      }
    ]
  })

  tags = merge(
    local.required_tags,
    {
      Name      = "kms-${local.bucket_name}"
      Component = "s3-encryption"
    }
  )
}

resource "aws_kms_alias" "s3" {
  name          = "alias/${local.bucket_name}"
  target_key_id = aws_kms_key.s3.key_id
}

# S3 Bucket
resource "aws_s3_bucket" "main" {
  bucket = local.bucket_name

  tags = merge(
    local.required_tags,
    {
      Name      = local.bucket_name
      Component = "storage"
      Purpose   = var.bucket_purpose
    }
  )
}

# Block Public Access
resource "aws_s3_bucket_public_access_block" "main" {
  bucket = aws_s3_bucket.main.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Versioning
resource "aws_s3_bucket_versioning" "main" {
  bucket = aws_s3_bucket.main.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Server-side Encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.s3.arn
    }
    bucket_key_enabled = true
  }
}

# Lifecycle Policy
resource "aws_s3_bucket_lifecycle_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  rule {
    id     = "transition-to-ia"
    status = "Enabled"

    filter {}

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 90
      storage_class = "GLACIER_IR"
    }

    transition {
      days          = 180
      storage_class = "DEEP_ARCHIVE"
    }
  }

  rule {
    id     = "expire-old-versions"
    status = "Enabled"

    filter {}

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }

  rule {
    id     = "delete-incomplete-uploads"
    status = "Enabled"

    filter {}

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# Bucket Policy
# Note: atlantis-ecs-task-prod has s3:GetBucketPolicy permission (added 2025-11-10)
resource "aws_s3_bucket_policy" "main" {
  bucket = aws_s3_bucket.main.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "EnforcedTLS"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.main.arn,
          "${aws_s3_bucket.main.arn}/*"
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      },
      {
        Sid       = "DenyUnencryptedObjectUploads"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:PutObject"
        Resource  = "${aws_s3_bucket.main.arn}/*"
        Condition = {
          StringNotEquals = {
            "s3:x-amz-server-side-encryption" = "aws:kms"
          }
        }
      }
    ]
  })
}

# CloudWatch Logging for S3 access
resource "aws_cloudwatch_log_group" "s3_access" {
  name              = "/aws/s3/${local.bucket_name}"
  retention_in_days = 7
  kms_key_id        = aws_kms_key.s3.arn

  tags = merge(
    local.required_tags,
    {
      Name      = "log-${local.bucket_name}"
      Component = "s3-logging"
    }
  )
}

# SSM Parameters for bucket information
resource "aws_ssm_parameter" "bucket_name" {
  name        = "/fileflow/prod/s3/bucket-name"
  description = "S3 bucket name for fileflow"
  type        = "String"
  value       = aws_s3_bucket.main.id

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.bucket_name}-name"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "bucket_arn" {
  name        = "/fileflow/prod/s3/bucket-arn"
  description = "S3 bucket ARN for fileflow"
  type        = "String"
  value       = aws_s3_bucket.main.arn

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.bucket_name}-arn"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "kms_key_id" {
  name        = "/fileflow/prod/s3/kms-key-id"
  description = "KMS key ID for S3 bucket encryption"
  type        = "String"
  value       = aws_kms_key.s3.id

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.bucket_name}-kms"
      Component = "parameter-store"
    }
  )
}
