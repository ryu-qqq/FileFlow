# ============================================================================
# FileFlow S3 Bucket - Outputs
# ============================================================================

output "bucket_name" {
  description = "Name of the S3 bucket"
  value       = module.fileflow_uploads.bucket_id
}

output "bucket_arn" {
  description = "ARN of the S3 bucket"
  value       = module.fileflow_uploads.bucket_arn
}

output "bucket_regional_domain_name" {
  description = "Regional domain name of the S3 bucket"
  value       = module.fileflow_uploads.bucket_regional_domain_name
}
