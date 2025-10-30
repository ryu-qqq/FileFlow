variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment (dev/staging/prod)"
  type        = string
  default     = "prod"
}

variable "service_name" {
  description = "Service name"
  type        = string
  default     = "fileflow"
}

variable "node_type" {
  description = "Cache node type"
  type        = string
  default     = "cache.t3.small"
}

variable "num_cache_nodes" {
  description = "Number of cache nodes"
  type        = number
  default     = 1
}

# Subnet Group
resource "aws_elasticache_subnet_group" "main" {
  name       = local.redis_name
  subnet_ids = local.private_subnet_ids

  tags = merge(
    local.required_tags,
    {
      Name      = local.redis_name
      Component = "elasticache-subnet-group"
    }
  )
}

# Security Group
resource "aws_security_group" "redis" {
  name        = "${local.redis_name}-sg"
  description = "Security group for ${local.redis_name}"
  vpc_id      = local.vpc_id

  ingress {
    description = "Redis from VPC"
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"] # VPC CIDR - update with actual VPC CIDR
  }

  egress {
    description = "Allow all outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.redis_name}-sg"
      Component = "security-group"
    }
  )
}

# Parameter Group
resource "aws_elasticache_parameter_group" "main" {
  name   = local.redis_name
  family = "redis7"

  parameter {
    name  = "maxmemory-policy"
    value = "allkeys-lru"
  }

  parameter {
    name  = "timeout"
    value = "300"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = local.redis_name
      Component = "parameter-group"
    }
  )
}

# Single Cache Cluster (without automatic failover)
resource "aws_elasticache_cluster" "main" {
  cluster_id           = local.redis_name
  engine               = "redis"
  engine_version       = "7.0"
  node_type            = var.node_type
  num_cache_nodes      = 1
  parameter_group_name = aws_elasticache_parameter_group.main.name

  port               = 6379
  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [aws_security_group.redis.id]

  snapshot_retention_limit = 7
  snapshot_window          = "03:00-05:00"
  maintenance_window       = "sun:05:00-sun:07:00"

  auto_minor_version_upgrade = true

  log_delivery_configuration {
    destination      = aws_cloudwatch_log_group.redis_slow.name
    destination_type = "cloudwatch-logs"
    log_format       = "json"
    log_type         = "slow-log"
  }

  log_delivery_configuration {
    destination      = aws_cloudwatch_log_group.redis_engine.name
    destination_type = "cloudwatch-logs"
    log_format       = "json"
    log_type         = "engine-log"
  }

  tags = merge(
    local.required_tags,
    {
      Name      = local.redis_name
      Component = "elasticache-cluster"
    }
  )
}

# CloudWatch Log Groups
resource "aws_cloudwatch_log_group" "redis_slow" {
  name              = "/aws/elasticache/${local.redis_name}/slow-log"
  retention_in_days = 7

  tags = merge(
    local.required_tags,
    {
      Name      = "log-${local.redis_name}-slow"
      Component = "cloudwatch-logs"
    }
  )
}

resource "aws_cloudwatch_log_group" "redis_engine" {
  name              = "/aws/elasticache/${local.redis_name}/engine-log"
  retention_in_days = 7

  tags = merge(
    local.required_tags,
    {
      Name      = "log-${local.redis_name}-engine"
      Component = "cloudwatch-logs"
    }
  )
}

# CloudWatch Alarms
resource "aws_cloudwatch_metric_alarm" "cpu_utilization" {
  alarm_name          = "${local.redis_name}-cpu-utilization"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = 75
  alarm_description   = "Redis CPU utilization is too high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    CacheClusterId = aws_elasticache_cluster.main.id
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.redis_name}-cpu-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "memory_utilization" {
  alarm_name          = "${local.redis_name}-memory-utilization"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseMemoryUsagePercentage"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "Redis memory utilization is too high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    CacheClusterId = aws_elasticache_cluster.main.id
  }

  tags = merge(
    local.required_tags,
    {
      Name      = "${local.redis_name}-memory-alarm"
      Component = "cloudwatch-alarm"
    }
  )
}

# SSM Parameters
resource "aws_ssm_parameter" "redis_endpoint" {
  name        = "/fileflow/prod/redis/endpoint"
  description = "Redis endpoint for fileflow"
  type        = "String"
  value       = aws_elasticache_cluster.main.cache_nodes[0].address

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.redis_name}-endpoint"
      Component = "parameter-store"
    }
  )
}

resource "aws_ssm_parameter" "redis_port" {
  name        = "/fileflow/prod/redis/port"
  description = "Redis port for fileflow"
  type        = "String"
  value       = tostring(aws_elasticache_cluster.main.port)

  tags = merge(
    local.required_tags,
    {
      Name      = "ssm-${local.redis_name}-port"
      Component = "parameter-store"
    }
  )
}
