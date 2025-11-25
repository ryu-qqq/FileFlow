# ========================================
# ECR Repositories for fileflow
# ========================================
# Container registries for:
# - web-api: REST API server
# - scheduler: Background scheduler
# ========================================

# ========================================
# ECR Repository: web-api
# ========================================
resource "aws_ecr_repository" "web_api" {
  name                 = "${var.project_name}-web-api-${var.environment}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-web-api-${var.environment}"
  }
}

resource "aws_ecr_lifecycle_policy" "web_api" {
  repository = aws_ecr_repository.web_api.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 30 tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "prod", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = 30
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Delete untagged images older than 7 days"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# ========================================
# ECR Repository: scheduler
# ========================================
resource "aws_ecr_repository" "scheduler" {
  name                 = "${var.project_name}-scheduler-${var.environment}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Service     = "${var.project_name}-scheduler-${var.environment}"
  }
}

resource "aws_ecr_lifecycle_policy" "scheduler" {
  repository = aws_ecr_repository.scheduler.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 30 tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "prod", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = 30
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Delete untagged images older than 7 days"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
