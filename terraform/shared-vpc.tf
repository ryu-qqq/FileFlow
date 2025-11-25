# ============================================================================
# Shared Infrastructure Reference: VPC
# ============================================================================
# References centrally managed VPC from Infrastructure repository
# Read-only access via SSM Parameter Store
# ============================================================================

# VPC ID
data "aws_ssm_parameter" "vpc_id" {
  name = "/shared/vpc/vpc-id"
}

# Subnet IDs
data "aws_ssm_parameter" "public_subnet_ids" {
  name = "/shared/vpc/public-subnet-ids"
}

data "aws_ssm_parameter" "private_subnet_ids" {
  name = "/shared/vpc/private-subnet-ids"
}

data "aws_ssm_parameter" "data_subnet_ids" {
  name = "/shared/vpc/data-subnet-ids"
}

# ============================================================================
# Local Variables
# ============================================================================

locals {
  vpc_id          = data.aws_ssm_parameter.vpc_id.value
  public_subnets  = split(",", data.aws_ssm_parameter.public_subnet_ids.value)
  private_subnets = split(",", data.aws_ssm_parameter.private_subnet_ids.value)
  data_subnets    = split(",", data.aws_ssm_parameter.data_subnet_ids.value)
}
