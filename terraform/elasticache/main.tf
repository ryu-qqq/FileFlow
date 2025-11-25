# ========================================
# ElastiCache (Redis) for Crawlinghub
# ========================================
# Dedicated Redis cluster
# Naming: fileflow-redis-prod
# ========================================

# ========================================
# Security Group for Redis
# ========================================
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-redis-sg-${var.environment}"
  description = "Security group for ElastiCache Redis"
  vpc_id      = local.vpc_id

  ingress {
    description = "Redis from ECS tasks"
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/8"] # VPC CIDR
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-redis-sg-${var.environment}"
  }
}

# ========================================
# ElastiCache Subnet Group
# ========================================
resource "aws_elasticache_subnet_group" "redis" {
  name       = "${var.project_name}-redis-subnet-${var.environment}"
  subnet_ids = local.private_subnets

  tags = {
    Name        = "${var.project_name}-redis-subnet-${var.environment}"
    Environment = var.environment
  }
}

# ========================================
# ElastiCache Parameter Group
# ========================================
resource "aws_elasticache_parameter_group" "redis" {
  name   = "${var.project_name}-redis-params-${var.environment}"
  family = "redis7"

  parameter {
    name  = "maxmemory-policy"
    value = "allkeys-lru"
  }

  tags = {
    Name        = "${var.project_name}-redis-params-${var.environment}"
    Environment = var.environment
  }
}

# ========================================
# ElastiCache Cluster
# ========================================
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "${var.project_name}-redis-${var.environment}"
  engine               = "redis"
  engine_version       = "7.0"
  node_type            = var.redis_node_type
  num_cache_nodes      = 1
  parameter_group_name = aws_elasticache_parameter_group.redis.name
  subnet_group_name    = aws_elasticache_subnet_group.redis.name
  security_group_ids   = [aws_security_group.redis.id]
  port                 = 6379

  snapshot_retention_limit = 1
  snapshot_window          = "05:00-09:00"
  maintenance_window       = "mon:09:00-mon:10:00"

  tags = {
    Name        = "${var.project_name}-redis-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-redis-${var.environment}"
  }
}

# ========================================
# Variables
# ========================================
variable "redis_node_type" {
  description = "ElastiCache node type"
  type        = string
  default     = "cache.t3.micro"
}

# ========================================
# Outputs
# ========================================
output "redis_endpoint" {
  description = "Redis primary endpoint"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
}

output "redis_port" {
  description = "Redis port"
  value       = aws_elasticache_cluster.redis.port
}

output "redis_security_group_id" {
  description = "Redis security group ID"
  value       = aws_security_group.redis.id
}
