# ============================================================================
# Shared Infrastructure References
# ============================================================================
# This file references shared infrastructure managed in Infrastructure repo
# DO NOT modify shared infrastructure from here.
# ============================================================================

# Network Resources
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/network/vpc-id"
}

data "aws_ssm_parameter" "private_subnet_ids" {
  name = "/shared/network/private-subnet-ids"
}
