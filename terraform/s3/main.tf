# ============================================================================
# FileFlow - S3 Bucket for File Uploads
# ============================================================================
# S3 bucket using Infrastructure module with KMS encryption
# Presigned URL uploads with encryption and lifecycle policies
# ============================================================================

# ============================================================================
# Common Tags (for governance)
# ============================================================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-s3"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "confidential"
  }
}

# ============================================================================
# KMS Key for S3 Encryption
# ============================================================================
resource "aws_kms_key" "s3" {
  description             = "KMS key for FileFlow S3 bucket encryption"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = {
    Name        = "${var.project_name}-s3-kms-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-s3"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

resource "aws_kms_alias" "s3" {
  name          = "alias/${var.project_name}-s3-${var.environment}"
  target_key_id = aws_kms_key.s3.key_id
}

# ============================================================================
# S3 Bucket using Infrastructure Module
# ============================================================================
module "fileflow_uploads" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/s3-bucket?ref=main"

  bucket_name = "${var.project_name}-uploads-${var.environment}"

  # KMS Encryption (required for governance)
  kms_key_id = aws_kms_key.s3.arn

  # Versioning
  versioning_enabled = true

  # Public Access Block (all blocked)
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true

  # Lifecycle Rules
  lifecycle_rules = [
    {
      id                           = "abort-incomplete-multipart-uploads"
      enabled                      = true
      prefix                       = null
      expiration_days              = null
      transition_to_ia_days        = null
      transition_to_glacier_days   = null
      noncurrent_expiration_days   = null
      abort_incomplete_upload_days = 7
    },
    {
      id                           = "intelligent-tiering-transition"
      enabled                      = true
      prefix                       = null
      expiration_days              = null
      transition_to_ia_days        = 30
      transition_to_glacier_days   = null
      noncurrent_expiration_days   = null
      abort_incomplete_upload_days = null
    },
    {
      id                           = "delete-old-versions"
      enabled                      = true
      prefix                       = null
      expiration_days              = null
      transition_to_ia_days        = null
      transition_to_glacier_days   = null
      noncurrent_expiration_days   = 90
      abort_incomplete_upload_days = null
    }
  ]

  # CORS Configuration (for Presigned URL uploads)
  cors_rules = [
    {
      allowed_headers = ["*"]
      allowed_methods = ["GET", "PUT", "POST", "HEAD"]
      allowed_origins = var.cors_allowed_origins
      expose_headers  = ["ETag", "x-amz-meta-*"]
      max_age_seconds = 3600
    }
  ]

  # CloudWatch Alarms
  enable_cloudwatch_alarms    = true
  alarm_bucket_size_threshold = 107374182400  # 100GB
  alarm_object_count_threshold = 1000000      # 1M objects

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ============================================================================
# Variables
# ============================================================================
variable "cors_allowed_origins" {
  description = "List of allowed origins for CORS"
  type        = list(string)
  default     = ["https://*.connectly.com", "http://localhost:*"]
}

# ============================================================================
# IAM Policy for S3 Access (ECS Tasks)
# ============================================================================
data "aws_iam_policy_document" "s3_access" {
  statement {
    sid    = "AllowS3Access"
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
      "s3:ListBucket",
      "s3:GetBucketLocation"
    ]
    resources = [
      module.fileflow_uploads.bucket_arn,
      "${module.fileflow_uploads.bucket_arn}/*"
    ]
  }

  statement {
    sid    = "AllowKMSForS3"
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey",
      "kms:DescribeKey"
    ]
    resources = [aws_kms_key.s3.arn]
  }
}

resource "aws_iam_policy" "s3_access" {
  name        = "${var.project_name}-s3-access-${var.environment}"
  description = "IAM policy for FileFlow S3 bucket access"
  policy      = data.aws_iam_policy_document.s3_access.json

  tags = {
    Name        = "${var.project_name}-s3-access-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-s3"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
  }
}

# ============================================================================
# SSM Parameters for Cross-Stack Reference
# ============================================================================
resource "aws_ssm_parameter" "bucket_name" {
  name        = "/${var.project_name}/s3/uploads-bucket-name"
  description = "FileFlow uploads bucket name"
  type        = "String"
  value       = module.fileflow_uploads.bucket_id

  tags = {
    Name        = "${var.project_name}-s3-bucket-name"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "bucket_arn" {
  name        = "/${var.project_name}/s3/uploads-bucket-arn"
  description = "FileFlow uploads bucket ARN"
  type        = "String"
  value       = module.fileflow_uploads.bucket_arn

  tags = {
    Name        = "${var.project_name}-s3-bucket-arn"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "s3_policy_arn" {
  name        = "/${var.project_name}/s3/access-policy-arn"
  description = "FileFlow S3 access IAM policy ARN"
  type        = "String"
  value       = aws_iam_policy.s3_access.arn

  tags = {
    Name        = "${var.project_name}-s3-policy-arn"
    Environment = var.environment
  }
}
