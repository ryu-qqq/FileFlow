# ========================================
# ECS Service: download-worker
# ========================================
# SQS-based background worker service
# Using Infrastructure modules
# No ALB, no auto scaling (can be enabled)
# ========================================

# ========================================
# Common Tags (for governance)
# ========================================
locals {
  common_tags = {
    environment  = var.environment
    service_name = "${var.project_name}-download-worker"
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
data "aws_ecr_repository" "download_worker" {
  name = "${var.project_name}-download-worker-${var.environment}"
}

# ========================================
# ECS Cluster Reference (from ecs-cluster)
# ========================================
data "aws_ecs_cluster" "main" {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
}

data "aws_caller_identity" "current" {}

# ========================================
# KMS Key for CloudWatch Logs Encryption
# ========================================
resource "aws_kms_key" "logs" {
  description             = "KMS key for FileFlow download-worker CloudWatch logs encryption"
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
            "kms:EncryptionContext:aws:logs:arn" = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/ecs/${var.project_name}-download-worker/${var.environment}"
          }
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-download-worker-logs-kms-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-download-worker"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

resource "aws_kms_alias" "logs" {
  name          = "alias/${var.project_name}-download-worker-logs-${var.environment}"
  target_key_id = aws_kms_key.logs.key_id
}

# ========================================
# CloudWatch Log Group with KMS Encryption
# ========================================
module "download_worker_logs" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/cloudwatch-log-group?ref=main"

  name              = "/aws/ecs/${var.project_name}-download-worker/${var.environment}"
  retention_in_days = 30
  kms_key_id        = aws_kms_key.logs.arn

  # Required tag variables (new module interface)
  environment  = var.environment
  service_name = "${var.project_name}-download-worker"
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = var.project_name
  data_class   = local.common_tags.data_class
}

# ========================================
# Security Groups
# ========================================
resource "aws_security_group" "ecs_download_worker" {
  name        = "${var.project_name}-download-worker-sg-${var.environment}"
  description = "Security group for download-worker ECS tasks"
  vpc_id      = local.vpc_id

  # No ingress - download-worker doesn't expose any ports

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name        = "${var.project_name}-download-worker-sg-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-download-worker"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
    DataClass   = local.common_tags.data_class
    Lifecycle   = "production"
    ManagedBy   = "terraform"
    Project     = var.project_name
  }
}

# ========================================
# IAM Role for ECS Task Execution
# ========================================
resource "aws_iam_role" "download_worker_task_execution" {
  name = "${var.project_name}-download-worker-execution-role-${var.environment}"

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
    Name        = "${var.project_name}-download-worker-execution-role-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-download-worker"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
  }
}

resource "aws_iam_role_policy_attachment" "download_worker_task_execution" {
  role       = aws_iam_role.download_worker_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "download_worker_secrets_access" {
  name = "${var.project_name}-download-worker-secrets-access-${var.environment}"
  role = aws_iam_role.download_worker_task_execution.id

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
resource "aws_iam_role" "download_worker_task" {
  name = "${var.project_name}-download-worker-task-role-${var.environment}"

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
    Name        = "${var.project_name}-download-worker-task-role-${var.environment}"
    Environment = var.environment
    Service     = "${var.project_name}-download-worker"
    Owner       = local.common_tags.owner
    CostCenter  = local.common_tags.cost_center
  }
}

# SQS permissions for download-worker
resource "aws_iam_role_policy" "download_worker_sqs_access" {
  name = "${var.project_name}-download-worker-sqs-access-${var.environment}"
  role = aws_iam_role.download_worker_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:GetQueueUrl",
          "sqs:ChangeMessageVisibility"
        ]
        Resource = [
          data.aws_sqs_queue.download_queue.arn,
          data.aws_sqs_queue.download_dlq.arn
        ]
      }
    ]
  })
}

# S3 permissions for download-worker (file downloads)
resource "aws_iam_role_policy" "download_worker_s3_access" {
  name = "${var.project_name}-download-worker-s3-access-${var.environment}"
  role = aws_iam_role.download_worker_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::${var.project_name}-*",
          "arn:aws:s3:::${var.project_name}-*/*"
        ]
      }
    ]
  })
}

# Add OpenTelemetry permissions for ADOT Collector sidecar
resource "aws_iam_role_policy" "download_worker_otel_access" {
  name = "${var.project_name}-download-worker-otel-access-${var.environment}"
  role = aws_iam_role.download_worker_task.id

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
module "download_worker_service" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/ecs-service?ref=main"

  name            = "${var.project_name}-download-worker-${var.environment}"
  cluster_id      = data.aws_ecs_cluster.main.arn
  container_name  = "download-worker"
  container_image = "${data.aws_ecr_repository.download_worker.repository_url}:${var.image_tag}"
  container_port  = 8080  # Required by module, but no actual port exposure

  cpu    = var.worker_cpu
  memory = var.worker_memory

  desired_count = var.worker_desired_count

  subnet_ids         = local.private_subnets
  security_group_ids = [aws_security_group.ecs_download_worker.id]

  execution_role_arn = aws_iam_role.download_worker_task_execution.arn
  task_role_arn      = aws_iam_role.download_worker_task.arn

  # No Load Balancer for download-worker

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
    },
    {
      name  = "SQS_QUEUE_URL"
      value = data.aws_sqs_queue.download_queue.url
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
      "awslogs-group"         = module.download_worker_logs.log_group_name
      "awslogs-region"        = var.aws_region
      "awslogs-stream-prefix" = "download-worker"
    }
  }

  # Deployment Configuration
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  deployment_circuit_breaker_enable   = true
  deployment_circuit_breaker_rollback = true

  # Enable ECS Exec for debugging
  enable_execute_command = true

  # No Auto Scaling for download-worker (can be enabled for SQS-based scaling)
  enable_autoscaling = false

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class

  # OpenTelemetry Collector Sidecar
  sidecars = [
    {
      name      = "otel-collector"
      image     = "public.ecr.aws/aws-observability/aws-otel-collector:latest"
      cpu       = 256
      memory    = 512
      essential = false
      portMappings = [
        { containerPort = 4317, protocol = "tcp" },
        { containerPort = 4318, protocol = "tcp" }
      ]
      environment = [
        {
          name  = "AOT_CONFIG_CONTENT"
          value = <<-EOT
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318
exporters:
  awsxray:
    region: ${var.aws_region}
  awsemf:
    region: ${var.aws_region}
    namespace: FileFlow
    log_group_name: /ecs/fileflow/otel
    dimension_rollup_option: NoDimensionRollup
service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [awsxray]
    metrics:
      receivers: [otlp]
      exporters: [awsemf]
EOT
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/fileflow/otel-collector"
          "awslogs-create-group"  = "true"
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "otel-download-worker"
        }
      }
    }
  ]
}
