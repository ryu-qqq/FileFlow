output "role_arn" {
  description = "ARN of the IAM role"
  value       = aws_iam_role.app_role.arn
}

output "role_name" {
  description = "Name of the IAM role"
  value       = aws_iam_role.app_role.name
}

output "policy_arn" {
  description = "ARN of the SQS access policy"
  value       = aws_iam_policy.sqs_access.arn
}
