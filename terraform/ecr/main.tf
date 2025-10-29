# ============================================================================
# FILEFLOW - ECR Repository
# ============================================================================
# Purpose: Docker 이미지 저장소
# - Spring Boot 애플리케이션 이미지 저장
# - 이미지 태깅: latest, git-sha
# - 라이프사이클: 최근 10개 이미지 유지
# ============================================================================

resource "aws_ecr_repository" "fileflow" {
  name                 = "fileflow-prod"
  image_tag_mutability = "MUTABLE"

  # 이미지 스캔 설정 (보안 취약점 검사)
  image_scanning_configuration {
    scan_on_push = true
  }

  # 암호화 설정
  encryption_configuration {
    encryption_type = "AES256" # AWS 관리형 키 사용
  }

  tags = {
    Name        = "fileflow-prod-ecr"
    Environment = "prod"
    Service     = "fileflow"
    ManagedBy   = "terraform"
    Team        = "platform-team"
    Project     = "fileflow"
  }
}

# ECR 라이프사이클 정책 (이미지 정리)
resource "aws_ecr_lifecycle_policy" "fileflow" {
  repository = aws_ecr_repository.fileflow.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "최근 10개 이미지만 유지"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "prod-", "release-"]
          countType     = "imageCountMoreThan"
          countNumber   = 10
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "태그 없는 이미지 7일 후 삭제"
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

# GitHub Actions용 ECR 접근 정책
resource "aws_ecr_repository_policy" "fileflow_github_actions" {
  repository = aws_ecr_repository.fileflow.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowGitHubActions"
        Effect = "Allow"
        Principal = {
          AWS = data.aws_caller_identity.current.account_id
        }
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
      }
    ]
  })
}
