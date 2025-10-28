#!/bin/bash

# Terraform Import If Exists Script
# 이 스크립트는 리소스가 이미 존재하면 자동으로 import하여 409 에러를 방지합니다.

set -e

MODULE_NAME=$1
MODULE_DIR=$2

if [ -z "$MODULE_NAME" ] || [ -z "$MODULE_DIR" ]; then
  echo "사용법: $0 <module-name> <module-dir>"
  exit 1
fi

echo "🔍 $MODULE_NAME 모듈의 기존 리소스 확인 중..."

cd "$MODULE_DIR"

# Terraform plan을 실행하여 생성될 리소스 확인
terraform plan -out=tfplan 2>&1 | tee plan_output.txt || true

# 409 에러 패턴 감지
if grep -q "EntityAlreadyExists\|already exists" plan_output.txt; then
  echo "⚠️  기존 리소스 발견! Import 작업 시작..."

  # ECS Service 모듈
  if [ "$MODULE_NAME" == "ecs-service" ]; then
    echo "📦 ECS Service 리소스 Import 중..."

    # IAM Roles
    if aws iam get-role --role-name fileflow-prod-ecs-execution-role --region ap-northeast-2 >/dev/null 2>&1; then
      echo "  → fileflow-prod-ecs-execution-role Import 중..."
      terraform import aws_iam_role.fileflow_execution_role fileflow-prod-ecs-execution-role || true
    fi

    if aws iam get-role --role-name fileflow-prod-ecs-task-role --region ap-northeast-2 >/dev/null 2>&1; then
      echo "  → fileflow-prod-ecs-task-role Import 중..."
      terraform import aws_iam_role.fileflow_task_role fileflow-prod-ecs-task-role || true
    fi

    # ALB
    ALB_ARN=$(aws elbv2 describe-load-balancers --names fileflow-prod-alb --region ap-northeast-2 --query 'LoadBalancers[0].LoadBalancerArn' --output text 2>/dev/null || echo "")
    if [ -n "$ALB_ARN" ] && [ "$ALB_ARN" != "None" ]; then
      echo "  → fileflow-prod-alb Import 중..."
      terraform import aws_lb.fileflow_alb "$ALB_ARN" || true
    fi

    # Target Group
    TG_ARN=$(aws elbv2 describe-target-groups --names fileflow-prod-alb-fileflow --region ap-northeast-2 --query 'TargetGroups[0].TargetGroupArn' --output text 2>/dev/null || echo "")
    if [ -n "$TG_ARN" ] && [ "$TG_ARN" != "None" ]; then
      echo "  → Target Group Import 중..."
      terraform import aws_lb_target_group.fileflow "$TG_ARN" || true
    fi

    # ECS Service
    if aws ecs describe-services --cluster fileflow-prod-cluster --services fileflow-prod-service --region ap-northeast-2 >/dev/null 2>&1; then
      echo "  → ECS Service Import 중..."
      terraform import aws_ecs_service.fileflow "fileflow-prod-cluster/fileflow-prod-service" || true
    fi

    # ECS Task Definition (latest revision)
    TASK_DEF_ARN=$(aws ecs list-task-definitions --family-prefix fileflow-prod --region ap-northeast-2 --query 'taskDefinitionArns[-1]' --output text 2>/dev/null || echo "")
    if [ -n "$TASK_DEF_ARN" ] && [ "$TASK_DEF_ARN" != "None" ]; then
      echo "  → ECS Task Definition Import 중..."
      terraform import aws_ecs_task_definition.fileflow "$TASK_DEF_ARN" || true
    fi
  fi

  # ElastiCache Redis 모듈
  if [ "$MODULE_NAME" == "elasticache-redis" ]; then
    echo "📦 ElastiCache Redis 리소스 Import 중..."

    if aws elasticache describe-replication-groups --replication-group-id fileflow-prod-redis --region ap-northeast-2 >/dev/null 2>&1; then
      echo "  → fileflow-prod-redis Import 중..."
      terraform import aws_elasticache_replication_group.redis fileflow-prod-redis || true
    fi
  fi

  # S3 Bucket 모듈
  if [ "$MODULE_NAME" == "s3-bucket" ]; then
    echo "📦 S3 Bucket 리소스 Import 중..."

    BUCKET_NAME="fileflow--prod"
    if aws s3api head-bucket --bucket "$BUCKET_NAME" --region ap-northeast-2 >/dev/null 2>&1; then
      echo "  → $BUCKET_NAME Import 중..."
      terraform import aws_s3_bucket.main "$BUCKET_NAME" || true
    fi
  fi

  # SQS Queue 모듈
  if [ "$MODULE_NAME" == "sqs-queue" ]; then
    echo "📦 SQS Queue 리소스 Import 중..."

    QUEUE_URL=$(aws sqs get-queue-url --queue-name fileflow-prod-queue --region ap-northeast-2 --query 'QueueUrl' --output text 2>/dev/null || echo "")
    if [ -n "$QUEUE_URL" ] && [ "$QUEUE_URL" != "None" ]; then
      echo "  → fileflow-prod-queue Import 중..."
      terraform import aws_sqs_queue.main "$QUEUE_URL" || true
    fi
  fi

  echo "✅ Import 완료! Terraform plan 재실행..."
  terraform plan -out=tfplan
else
  echo "✅ 기존 리소스 없음. 정상적으로 진행합니다."
fi

echo "🎯 $MODULE_NAME 모듈 준비 완료!"

# Cleanup
rm -f plan_output.txt
