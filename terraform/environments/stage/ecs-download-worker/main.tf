# ========================================
# ECS Service: download-worker (stage)
# ========================================
# SQS-based background worker service
# Using Infrastructure modules
# No ALB, no auto scaling
# No log streaming (stage environment)
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
  name = "${var.project_name}-download-worker-stage"
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
  description             = "KMS key for FileFlow download-worker CloudWatch logs encryption (stage)"
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
    Lifecycle   = "staging"
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
  retention_in_days = 14
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

# No log streaming for stage environment

# ========================================
# Security Group (using Infrastructure module)
# ========================================
module "ecs_security_group" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/security-group?ref=main"

  name        = "${var.project_name}-download-worker-sg-${var.environment}"
  description = "Security group for download-worker ECS tasks (stage)"
  vpc_id      = local.vpc_id

  # Custom type for download-worker (egress only - no ingress needed)
  type = "custom"

  # No ingress rules - download-worker doesn't expose any ports

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# IAM Role for ECS Task Execution (using Infrastructure module)
# ========================================
module "download_worker_task_execution_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-download-worker-execution-role-${var.environment}"

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

  attach_aws_managed_policies = [
    "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  ]

  enable_secrets_manager_policy = true
  secrets_manager_secret_arns   = [data.aws_secretsmanager_secret.rds.arn]

  custom_inline_policies = {
    ssm-and-kms-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
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
  }

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# IAM Role for ECS Task (using Infrastructure module)
# ========================================
module "download_worker_task_role" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/iam-role-policy?ref=main"

  role_name = "${var.project_name}-download-worker-task-role-${var.environment}"

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

  custom_inline_policies = {
    sqs-access = {
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
              data.aws_ssm_parameter.external_download_queue_arn.value,
              "${data.aws_ssm_parameter.external_download_queue_arn.value}-dlq"
            ]
          }
        ]
      })
    }
    s3-access = {
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
    s3-otel-config-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
          {
            Sid    = "OtelConfigAccess"
            Effect = "Allow"
            Action = [
              "s3:GetObject"
            ]
            Resource = "arn:aws:s3:::prod-connectly/otel-config/*"
          }
        ]
      })
    }
    adot-amp-access = {
      policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
          {
            Sid    = "AMPRemoteWrite"
            Effect = "Allow"
            Action = [
              "aps:RemoteWrite"
            ]
            Resource = local.amp_workspace_arn
          },
          {
            Sid    = "XRayTracing"
            Effect = "Allow"
            Action = [
              "xray:PutTraceSegments",
              "xray:PutTelemetryRecords",
              "xray:GetSamplingRules",
              "xray:GetSamplingTargets",
              "xray:GetSamplingStatisticSummaries"
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
              "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/ecs/fileflow/otel:*",
              "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/ecs/fileflow/otel-collector:*"
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
  }

  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class
}

# ========================================
# ADOT Sidecar (using Infrastructure module)
# ========================================
module "adot_sidecar" {
  source = "git::https://github.com/ryu-qqq/Infrastructure.git//terraform/modules/adot-sidecar?ref=main"

  project_name              = var.project_name
  service_name              = "download-worker"
  aws_region                = var.aws_region
  amp_workspace_arn         = local.amp_workspace_arn
  amp_remote_write_endpoint = local.amp_remote_write_url
  log_group_name            = module.download_worker_logs.log_group_name
  app_port                  = 8082 # Worker management port
  cluster_name              = data.aws_ecs_cluster.main.cluster_name
  environment               = var.environment
  config_bucket             = "prod-connectly"
  config_version            = "20251215" # Cache busting for OTEL config
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
  container_port  = 8082 # Management port for health checks

  cpu    = var.worker_cpu
  memory = var.worker_memory

  desired_count = var.worker_desired_count

  subnet_ids         = local.private_subnets
  security_group_ids = [module.ecs_security_group.security_group_id]

  execution_role_arn = module.download_worker_task_execution_role.role_arn
  task_role_arn      = module.download_worker_task_role.role_arn

  # No Load Balancer for download-worker

  # Container Environment Variables
  container_environment = [
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "stage"
    },
    {
      name  = "APP_PORT"
      value = "8082"
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
      name  = "SQS_EXTERNAL_DOWNLOAD_QUEUE_URL"
      value = data.aws_ssm_parameter.external_download_queue_url.value
    },
    {
      name  = "SQS_EXTERNAL_DOWNLOAD_DLQ_URL"
      value = data.aws_ssm_parameter.external_download_dlq_url.value
    },
    # Sentry (Error Tracking)
    {
      name  = "SENTRY_DSN"
      value = local.sentry_dsn
    },
    {
      name  = "SENTRY_ENVIRONMENT"
      value = var.environment
    },
    {
      name  = "APP_VERSION"
      value = var.image_tag
    }
  ]

  # Container Secrets
  container_secrets = [
    {
      name      = "DB_PASSWORD"
      valueFrom = "${data.aws_secretsmanager_secret.rds.arn}:password::"
    }
  ]

  # Health Check (Spring Boot Actuator on management port 8082)
  # Management server runs on separate port for health checks and metrics
  health_check_command      = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1"]
  health_check_start_period = 120 # Worker needs time to initialize Redis, SQS, S3 connections

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

  # No Auto Scaling for download-worker (stage)
  enable_autoscaling = false

  # Required Tags (governance compliance)
  environment  = local.common_tags.environment
  service_name = local.common_tags.service_name
  team         = local.common_tags.team
  owner        = local.common_tags.owner
  cost_center  = local.common_tags.cost_center
  project      = local.common_tags.project
  data_class   = local.common_tags.data_class

  # ADOT Collector Sidecar
  sidecars = [module.adot_sidecar.container_definition]
}
