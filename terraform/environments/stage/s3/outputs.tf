# ============================================================================
# FileFlow S3 Bucket - Outputs (Stage)
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

output "kms_key_arn" {
  description = "KMS key ARN for S3 encryption"
  value       = aws_kms_key.s3.arn
}

output "s3_access_policy_arn" {
  description = "IAM policy ARN for S3 access"
  value       = aws_iam_policy.s3_access.arn
}
