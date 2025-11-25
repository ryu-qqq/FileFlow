# ============================================================================
# Shared Infrastructure Reference: RDS MySQL
# ============================================================================
# References centrally managed RDS from Infrastructure repository
# Credentials accessed via Secrets Manager
# ============================================================================

# RDS Connection Information
data "aws_ssm_parameter" "rds_address" {
  name = "/shared/rds/db-instance-address"
}

data "aws_ssm_parameter" "rds_port" {
  name = "/shared/rds/db-instance-port"
}

data "aws_ssm_parameter" "rds_security_group_id" {
  name = "/shared/rds/security-group-id"
}

# RDS Credentials (from Secrets Manager)
data "aws_ssm_parameter" "rds_secret_name" {
  name = "/shared/rds/master-password-secret-name"
}

data "aws_secretsmanager_secret" "rds" {
  name = data.aws_ssm_parameter.rds_secret_name.value
}

data "aws_secretsmanager_secret_version" "rds" {
  secret_id = data.aws_secretsmanager_secret.rds.id
}

# ============================================================================
# Local Variables
# ============================================================================

locals {
  rds_credentials       = jsondecode(data.aws_secretsmanager_secret_version.rds.secret_string)
  rds_username          = local.rds_credentials.username
  rds_password          = local.rds_credentials.password
  rds_dbname            = local.rds_credentials.dbname
  rds_endpoint          = "${data.aws_ssm_parameter.rds_address.value}:${data.aws_ssm_parameter.rds_port.value}"
  rds_address           = data.aws_ssm_parameter.rds_address.value
  rds_port              = data.aws_ssm_parameter.rds_port.value
  rds_security_group_id = data.aws_ssm_parameter.rds_security_group_id.value
}
