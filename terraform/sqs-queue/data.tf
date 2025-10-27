data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

data "aws_kms_key" "sqs" {
  key_id = "alias/sqs-encryption"
}
