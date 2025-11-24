# ============================================================================
# FileFlow - S3 Bucket for File Uploads
# ============================================================================
# S3 bucket for presigned URL uploads with encryption and lifecycle policies
# Using infrastructure module for governance compliance
# ============================================================================

# ============================================================================
# S3 Bucket (using infrastructure module)
# ============================================================================

module "fileflow_uploads" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/s3-bucket?ref=feat/fileflow-migration-modules"

  bucket_name        = "${var.project_name}-uploads-${var.environment}"
  versioning_enabled = true

  # Governance tags
  environment     = var.environment
  service_name    = "${var.project_name}-uploads"
  owner           = "platform@ryuqqq.com"
  cost_center     = "engineering"
  data_class      = "sensitive"
  lifecycle_stage = "production"
  project         = "infrastructure"

  # CORS configuration
  cors_rules = [
    {
      allowed_headers = ["*"]
      allowed_methods = ["GET", "PUT", "POST", "HEAD"]
      allowed_origins = var.cors_allowed_origins
      expose_headers  = ["ETag"]
      max_age_seconds = 3600
    }
  ]

  # Lifecycle rules
  lifecycle_rules = [
    {
      id                                     = "abort-incomplete-multipart-uploads"
      enabled                                = true
      abort_incomplete_multipart_upload_days = 7
    },
    {
      id      = "intelligent-tiering-transition"
      enabled = true
      transitions = [
        {
          days          = 30
          storage_class = "INTELLIGENT_TIERING"
        }
      ]
    },
    {
      id                                 = "delete-old-versions"
      enabled                            = true
      noncurrent_version_expiration_days = 90
    }
  ]

  # SSM parameter for cross-stack reference
  create_ssm_parameter = true
}

# ============================================================================
# Variables
# ============================================================================

variable "cors_allowed_origins" {
  description = "List of allowed origins for CORS"
  type        = list(string)
  default     = ["https://*.connectly.com", "http://localhost:*"]
}
