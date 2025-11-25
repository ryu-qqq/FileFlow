# ========================================
# ECS Service: scheduler
# ========================================
# Background scheduler service
# No ALB, no auto scaling
# Desired count: 1 (fixed)
# ========================================

# ========================================
# ECR Repository Reference
# ========================================
data "aws_ecr_repository" "scheduler" {
  name = "${var.project_name}-scheduler-${var.environment}"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

# ========================================
# Security Groups
# ========================================

resource "aws_security_group" "ecs_scheduler" {
  name        = "${var.project_name}-scheduler-sg-${var.environment}"
  description = "Security group for scheduler ECS tasks"
  vpc_id      = local.vpc_id

  # No ingress - scheduler doesn't expose any ports

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.project_name}-scheduler-sg-${var.environment}"
  }
}

# ========================================
# IAM Role for ECS Task Execution
# ========================================

resource "aws_iam_role" "scheduler_task_execution" {
  name = "${var.project_name}-scheduler-execution-role-${var.environment}"

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
}

resource "aws_iam_role_policy_attachment" "scheduler_task_execution" {
  role       = aws_iam_role.scheduler_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "scheduler_secrets_access" {
  name = "${var.project_name}-scheduler-secrets-access-${var.environment}"
  role = aws_iam_role.scheduler_task_execution.id

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
          "arn:aws:ssm:${var.aws_region}:*:parameter/shared/*"
        ]
      }
    ]
  })
}

# ========================================
# IAM Role for ECS Task
# ========================================

resource "aws_iam_role" "scheduler_task" {
  name = "${var.project_name}-scheduler-task-role-${var.environment}"

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
}

# EventBridge permissions for scheduler
resource "aws_iam_role_policy" "scheduler_eventbridge_access" {
  name = "${var.project_name}-scheduler-eventbridge-access-${var.environment}"
  role = aws_iam_role.scheduler_task.id

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

# ========================================
# CloudWatch Log Group
# ========================================

resource "aws_cloudwatch_log_group" "scheduler" {
  name              = "/ecs/${var.project_name}-scheduler-${var.environment}"
  retention_in_days = 30

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-scheduler-${var.environment}"
  }
}

# ========================================
# ECS Task Definition
# ========================================

resource "aws_ecs_task_definition" "scheduler" {
  family                   = "${var.project_name}-scheduler-${var.environment}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.scheduler_cpu
  memory                   = var.scheduler_memory
  execution_role_arn       = aws_iam_role.scheduler_task_execution.arn
  task_role_arn            = aws_iam_role.scheduler_task.arn

  container_definitions = jsonencode([
    {
      name  = "scheduler"
      image = "${data.aws_ecr_repository.scheduler.repository_url}:latest"

      # No port mappings - scheduler doesn't expose ports

      environment = [
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

      secrets = [
        {
          name      = "DB_PASSWORD"
          valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.scheduler.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "scheduler"
        }
      }
    }
  ])

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-scheduler-${var.environment}"
  }
}

# ========================================
# ECS Service (No ALB, No Auto Scaling)
# ========================================

resource "aws_ecs_service" "scheduler" {
  name            = "${var.project_name}-scheduler-${var.environment}"
  cluster         = data.aws_ecs_cluster.main.arn
  task_definition = aws_ecs_task_definition.scheduler.arn
  desired_count   = 1 # Fixed at 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = local.private_subnets
    security_groups  = [aws_security_group.ecs_scheduler.id]
    assign_public_ip = false
  }

  # Deployment configuration
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100

  # Enable execute command for debugging
  enable_execute_command = true

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-scheduler-${var.environment}"
  }

  lifecycle {
    ignore_changes = [task_definition]
  }
}
