# ============================================================================
# Shared Infrastructure Reference: Bastion Host
# ============================================================================
# References centrally managed Bastion from Infrastructure repository
# Read-only access via SSM Parameter Store
#
# Bastion은 Infrastructure 레포에서 관리하고,
# fileflow는 SSM Session Manager로 접근만 수행
# ============================================================================

# Bastion Instance ID (SSM Session Manager 접근용)
data "aws_ssm_parameter" "bastion_instance_id" {
  name = "/shared/bastion/instance-id"
}

# Bastion Security Group ID (필요 시 접근 규칙 추가용)
data "aws_ssm_parameter" "bastion_security_group_id" {
  name = "/shared/bastion/security-group-id"
}

# ============================================================================
# Local Variables
# ============================================================================

locals {
  bastion_instance_id        = data.aws_ssm_parameter.bastion_instance_id.value
  bastion_security_group_id  = data.aws_ssm_parameter.bastion_security_group_id.value
}

# ============================================================================
# Outputs (참조용)
# ============================================================================

output "bastion_instance_id" {
  description = "Bastion EC2 instance ID (SSM 접근 시 사용)"
  value       = local.bastion_instance_id
}

output "bastion_connection_command" {
  description = "SSM Session Manager 접속 명령어"
  value       = "aws ssm start-session --target ${local.bastion_instance_id} --region ap-northeast-2"
}

# ============================================================================
# 사용 방법:
# ============================================================================
# 1. Infrastructure 레포에서 bastion이 생성되어 있어야 함
# 2. Infrastructure에서 SSM Parameter Store에 값 저장:
#    - /shared/bastion/instance-id
#    - /shared/bastion/security-group-id
# 3. 로컬에서 접근:
#    aws ssm start-session --target <instance-id>
# ============================================================================
