# ============================================================================
# FileFlow S3 Bucket - Outputs
# ============================================================================

output "bucket_name" {
  description = "Name of the S3 bucket"
  value       = aws_s3_bucket.fileflow_uploads.bucket
}

output "bucket_arn" {
  description = "ARN of the S3 bucket"
  value       = aws_s3_bucket.fileflow_uploads.arn
}

output "bucket_regional_domain_name" {
  description = "Regional domain name of the S3 bucket"
  value       = aws_s3_bucket.fileflow_uploads.bucket_regional_domain_name
}
