# ============================================================================
# FileFlow - S3 Bucket for File Uploads
# ============================================================================
# S3 bucket for presigned URL uploads with encryption and lifecycle policies
# ============================================================================

# ============================================================================
# S3 Bucket
# ============================================================================

resource "aws_s3_bucket" "fileflow_uploads" {
  bucket = "${var.project_name}-uploads-${var.environment}"

  tags = merge(var.common_tags, {
    Name        = "${var.project_name}-uploads-${var.environment}"
    Environment = var.environment
  })
}

# ============================================================================
# Bucket Versioning
# ============================================================================

resource "aws_s3_bucket_versioning" "fileflow_uploads" {
  bucket = aws_s3_bucket.fileflow_uploads.id
  versioning_configuration {
    status = "Enabled"
  }
}

# ============================================================================
# Server-Side Encryption
# ============================================================================

resource "aws_s3_bucket_server_side_encryption_configuration" "fileflow_uploads" {
  bucket = aws_s3_bucket.fileflow_uploads.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# ============================================================================
# Public Access Block
# ============================================================================

resource "aws_s3_bucket_public_access_block" "fileflow_uploads" {
  bucket = aws_s3_bucket.fileflow_uploads.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ============================================================================
# CORS Configuration (for Presigned URL uploads)
# ============================================================================

resource "aws_s3_bucket_cors_configuration" "fileflow_uploads" {
  bucket = aws_s3_bucket.fileflow_uploads.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "HEAD"]
    allowed_origins = var.cors_allowed_origins
    expose_headers  = ["ETag", "x-amz-meta-*"]
    max_age_seconds = 3600
  }
}

# ============================================================================
# Lifecycle Rules
# ============================================================================

resource "aws_s3_bucket_lifecycle_configuration" "fileflow_uploads" {
  bucket = aws_s3_bucket.fileflow_uploads.id

  # Abort incomplete multipart uploads after 7 days
  rule {
    id     = "abort-incomplete-multipart-uploads"
    status = "Enabled"

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  # Move to Intelligent-Tiering after 30 days
  rule {
    id     = "intelligent-tiering-transition"
    status = "Enabled"

    transition {
      days          = 30
      storage_class = "INTELLIGENT_TIERING"
    }
  }

  # Delete non-current versions after 90 days
  rule {
    id     = "delete-old-versions"
    status = "Enabled"

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }
}

# ============================================================================
# Variables
# ============================================================================

variable "cors_allowed_origins" {
  description = "List of allowed origins for CORS"
  type        = list(string)
  default     = ["https://*.connectly.com", "http://localhost:*"]
}
