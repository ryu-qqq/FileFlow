# ========================================
# ECS Service: web-api
# ========================================
# REST API server with ALB and Auto Scaling
# Using Infrastructure modules
# Domain: files.set-of.com
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-web-api"
    team         = "platform-team"
    owner        = "platform@ryuqqq.com"
    cost_center  = "engineering"
    project      = var.project_name
    data_class   = "internal"
  }
}

# ========================================
# ECR Repository Reference
# ========================================
data "aws_ecr_repository" "web_api" {
  name = "${var.project_name}-web-api-${var.environment}"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

# ========================================
# KMS Key for CloudWatch Logs Encryption
# ========================================
resource "aws_kms_key" "logs" {
  description             = "KMS key for FileFlow web-api CloudWatch logs encryption"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow CloudWatch Logs"
        Effect = "Allow"
        Principal = {
          Service = "logs.${var.aws_region}.amazonaws.com"
        }
        Action = [
          "kms:Encrypt*",
          "kms:Decrypt*",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:Describe*"
        ]
        Resource = "*"
        Condition = {
          ArnLike = {
            "kms:EncryptionContext:aws:logs:arn" = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/ecs/${var.project_name}-web-api-${var.environment}"
          }
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-web-api-logs-kms-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

resource "aws_kms_alias" "logs" {
  name          = "alias/${var.project_name}-web-api-logs-${var.environment}"
  target_key_id = aws_kms_key.logs.key_id
}

data "aws_caller_identity" "current" {}

# ========================================
# CloudWatch Log Group with KMS Encryption
# ========================================
module "web_api_logs" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/cloudwatch-log-group?ref=main"

  name              = "/ecs/${var.project_name}-web-api-${var.environment}"
  retention_in_days = 30
  kms_key_id        = aws_kms_key.logs.arn

  common_tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

# ========================================
# Security Groups
# ========================================
resource "aws_security_group" "alb" {
  name        = "${var.project_name}-alb-sg-${var.environment}"
  description = "Security group for ALB"
  vpc_id      = local.vpc_id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS from anywhere"
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP for redirect"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-alb-sg-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

resource "aws_security_group" "ecs_web_api" {
  name        = "${var.project_name}-web-api-sg-${var.environment}"
  description = "Security group for web-api ECS tasks"
  vpc_id      = local.vpc_id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
    description     = "From ALB only"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-web-api-sg-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

# ========================================
# Application Load Balancer
# ========================================
resource "aws_lb" "web_api" {
  name               = "${var.project_name}-alb-${var.environment}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = local.public_subnets

  enable_deletion_protection = false

  tags = {
    Name        = "${var.project_name}-alb-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

# Target Group
resource "aws_lb_target_group" "web_api" {
  name        = "${var.project_name}-web-api-tg-${var.environment}"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = local.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  tags = {
    Name        = "${var.project_name}-web-api-tg-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
  }
}

# HTTPS Listener
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.web_api.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = local.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.web_api.arn
  }
}

# HTTP to HTTPS Redirect
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.web_api.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# ========================================
# Route53 DNS Record
# ========================================
resource "aws_route53_record" "web_api" {
  zone_id = local.route53_zone_id
  name    = local.fqdn
  type    = "A"

  alias {
    name                   = aws_lb.web_api.dns_name
    zone_id                = aws_lb.web_api.zone_id
    evaluate_target_health = true
  }
}

# ========================================
# IAM Role for ECS Task Execution
# ========================================
resource "aws_iam_role" "ecs_task_execution" {
  name = "${var.project_name}-web-api-execution-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-web-api-execution-role-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "secrets_access" {
  name = "${var.project_name}-secrets-access-${var.environment}"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          data.aws_secretsmanager_secret.rds.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameters",
          "ssm:GetParameter"
        ]
        Resource = [
          "arn:aws:ssm:${var.aws_region}:*:parameter/shared/*",
          "arn:aws:ssm:${var.aws_region}:*:parameter/${var.project_name}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = [
          aws_kms_key.logs.arn
        ]
      }
    ]
  })
}

# ========================================
# IAM Role for ECS Task
# ========================================
resource "aws_iam_role" "ecs_task" {
  name = "${var.project_name}-web-api-task-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-web-api-task-role-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-web-api"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
  }
}

# Add EventBridge permissions for web-api
resource "aws_iam_role_policy" "eventbridge_access" {
  name = "${var.project_name}-eventbridge-access-${var.environment}"
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "events:PutEvents",
          "events:PutRule",
          "events:PutTargets",
          "events:DeleteRule",
          "events:RemoveTargets"
        ]
        Resource = "*"
      }
    ]
  })
}

# Add OpenTelemetry permissions for ADOT Collector sidecar
resource "aws_iam_role_policy" "otel_access" {
  name = "${var.project_name}-otel-access-${var.environment}"
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "XRayAccess"
        Effect = "Allow"
        Action = [
          "xray:PutTraceSegments",
          "xray:PutTelemetryRecords",
          "xray:GetSamplingRules",
          "xray:GetSamplingTargets"
        ]
        Resource = "*"
      },
      {
        Sid    = "CloudWatchLogsAccess"
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = [
          "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/ecs/fileflow/otel:*"
        ]
      },
      {
        Sid    = "CloudWatchMetricsAccess"
        Effect = "Allow"
        Action = [
          "cloudwatch:PutMetricData"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "cloudwatch:namespace" = "FileFlow"
          }
        }
      }
    ]
  })
}

# ========================================
# ECS Service using Infrastructure Module
# ========================================
module "web_api_service" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecs-service?ref=main"

  name            = "${var.project_name}-web-api-${var.environment}"
  cluster_id      = data.aws_ecs_cluster.main.arn
  container_name  = "web-api"
  container_image = "${data.aws_ecr_repository.web_api.repository_url}:latest"
  container_port  = 8080

  cpu    = var.web_api_cpu
  memory = var.web_api_memory

  desired_count = var.web_api_desired_count

  subnet_ids         = local.private_subnets
  security_group_ids = [aws_security_group.ecs_web_api.id]

  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task.arn

  # Load Balancer Configuration
  load_balancer_config = {
    target_group_arn = aws_lb_target_group.web_api.arn
    container_name   = "web-api"
    container_port   = 8080
  }

  # Health Check Grace Period
  health_check_grace_period_seconds = 60

  # Container Environment Variables
  container_environment = [
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = var.environment
    },
    {
      name  = "DB_HOST"
      value = local.rds_host
    },
    {
      name  = "DB_PORT"
      value = local.rds_port
    },
    {
      name  = "DB_NAME"
      value = local.rds_dbname
    },
    {
      name  = "DB_USER"
      value = local.rds_username
    },
    {
      name  = "REDIS_HOST"
      value = local.redis_host
    },
    {
      name  = "REDIS_PORT"
      value = tostring(local.redis_port)
    }
  ]

  # Container Secrets
  container_secrets = [
    {
      name      = "DB_PASSWORD"
      valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::"
    }
  ]

  # Custom Log Configuration (using KMS-encrypted log group)
  log_configuration = {
    log_driver = "awslogs"
    options = {
      "awslogs-group"         = module.web_api_logs.log_group_name
      "awslogs-region"        = var.aws_region
      "awslogs-stream-prefix" = "web-api"
    }
  }

  # Health Check
  health_check_command = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
  health_check_interval    = 30
  health_check_timeout     = 5
  health_check_retries     = 3
  health_check_start_period = 60

  # Deployment Configuration
  deployment_circuit_breaker_enable   = true
  deployment_circuit_breaker_rollback = true

  # Auto Scaling
  enable_autoscaling       = true
  autoscaling_min_capacity = 2
  autoscaling_max_capacity = 10
  autoscaling_target_cpu   = 70
  autoscaling_target_memory = 80

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}
