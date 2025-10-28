# ============================================================================
# ECS Cluster for FileFlow
# ============================================================================

resource "aws_ecs_cluster" "fileflow" {
  name = "${local.service_name}-${local.environment}"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(
    local.required_tags,
    {
      Name        = "${local.service_name}-${local.environment}"
      Component   = "ecs-cluster"
      Description = "ECS cluster for ${local.service_name}"
    }
  )
}

# ECS Cluster Capacity Providers
resource "aws_ecs_cluster_capacity_providers" "fileflow" {
  cluster_name = aws_ecs_cluster.fileflow.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
    base              = 1
  }
}

# SSM Parameter for cluster ID (for future reference)
resource "aws_ssm_parameter" "cluster_id" {
  name        = "/${local.service_name}/${local.environment}/ecs/cluster-id"
  description = "ECS cluster ID for ${local.service_name}"
  type        = "String"
  value       = aws_ecs_cluster.fileflow.id

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.service_name}-${local.environment}-cluster-id"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "cluster_name" {
  name        = "/${local.service_name}/${local.environment}/ecs/cluster-name"
  description = "ECS cluster name for ${local.service_name}"
  type        = "String"
  value       = aws_ecs_cluster.fileflow.name

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.service_name}-${local.environment}-cluster-name"
      Component = "parameter-store"
    }
  )
}
