# Terraform Import Scripts

## 개요

이 디렉토리는 Terraform 배포 시 **409 에러 (EntityAlreadyExists)** 를 방지하기 위한 자동 Import 스크립트를 포함합니다.

## 문제 상황

Terraform State 파일에는 리소스가 없지만, 실제 AWS에 리소스가 존재하는 경우 다음과 같은 에러가 발생합니다:

```
Error: creating IAM Role (fileflow-prod-ecs-execution-role):
operation error IAM: CreateRole, https response error StatusCode: 409,
EntityAlreadyExists: Role with name fileflow-prod-ecs-execution-role already exists.
```

## 해결 방법

### 자동 방지 (권장)

GitHub Actions 워크플로우에 자동 Import 로직이 통합되어 있습니다.

**`.github/workflows/terraform-apply.yml`** 워크플로우는:
1. `terraform plan`을 실행하여 충돌 확인
2. "EntityAlreadyExists" 또는 "already exists" 패턴 감지
3. 기존 리소스를 자동으로 `terraform import`
4. Import 완료 후 정상적으로 apply 진행

**지원하는 모듈:**
- `ecs-service`: IAM Roles, ALB, Target Groups, ECS Service, Task Definitions
- `elasticache-redis`: ElastiCache Replication Groups
- `s3-bucket`: S3 Buckets
- `sqs-queue`: SQS Queues

### 수동 Import (필요 시)

스크립트를 직접 실행할 수 있습니다:

```bash
# ECS Service 모듈
./scripts/terraform-import-if-exists.sh ecs-service terraform/ecs-service

# ElastiCache Redis 모듈
./scripts/terraform-import-if-exists.sh elasticache-redis terraform/elasticache-redis

# S3 Bucket 모듈
./scripts/terraform-import-if-exists.sh s3-bucket terraform/s3-bucket

# SQS Queue 모듈
./scripts/terraform-import-if-exists.sh sqs-queue terraform/sqs-queue
```

## Import 대상 리소스

### ECS Service 모듈
- `aws_iam_role.fileflow_execution_role` → `fileflow-prod-ecs-execution-role`
- `aws_iam_role.fileflow_task_role` → `fileflow-prod-ecs-task-role`
- `aws_lb.fileflow_alb` → ALB ARN
- `aws_lb_target_group.fileflow` → Target Group ARN
- `aws_ecs_service.fileflow` → `fileflow-prod-cluster/fileflow-prod-service`
- `aws_ecs_task_definition.fileflow` → Task Definition ARN

### ElastiCache Redis 모듈
- `aws_elasticache_replication_group.redis` → `fileflow-prod-redis`

### S3 Bucket 모듈
- `aws_s3_bucket.main` → `fileflow--prod`

### SQS Queue 모듈
- `aws_sqs_queue.main` → Queue URL

## 작동 방식

1. **Plan 실행**: `terraform plan`을 실행하여 출력을 캡처
2. **패턴 감지**: "EntityAlreadyExists" 또는 "already exists" 문자열 검색
3. **리소스 확인**: AWS CLI로 실제 리소스 존재 여부 확인
4. **Import 실행**: 존재하는 리소스를 `terraform import`로 State에 추가
5. **재시도**: Import 완료 후 Plan 재실행하여 정상 진행

## 주의사항

### Import는 언제 필요한가?

- ❌ **Terraform State가 손실된 경우**
- ❌ **Console/CLI로 리소스를 수동 생성한 경우**
- ❌ **Apply 실패 후 State와 AWS가 불일치한 경우**

### Import로 해결할 수 없는 경우

- ⚠️ **리소스 설정이 코드와 크게 다른 경우**: Import는 성공하지만 plan에서 많은 변경 사항 표시
- ⚠️ **리소스 이름이 다른 경우**: Import 대상을 찾을 수 없음

### 대안

리소스가 필요 없다면 **삭제** 후 새로 생성하는 것이 더 깨끗합니다:

```bash
# IAM Role 삭제 예시
aws iam delete-role --role-name fileflow-prod-ecs-execution-role --region ap-northeast-2

# ALB 삭제 예시 (deletion protection 해제 필요)
aws elbv2 modify-load-balancer-attributes \
  --load-balancer-arn <ALB_ARN> \
  --attributes Key=deletion_protection.enabled,Value=false

aws elbv2 delete-load-balancer --load-balancer-arn <ALB_ARN>
```

## 앞으로 방지하는 방법

1. **Terraform으로만 리소스 생성/삭제**
2. **State 파일 백업** (S3 backend 사용 중 ✅)
3. **Apply 실패 시 즉시 확인**
   - State를 정리하거나 (`terraform state rm`)
   - 실제 리소스를 삭제
4. **자동 Import 로직 활용** (이미 적용됨 ✅)

## 트러블슈팅

### Import 실패: "Resource already managed"
```
Error: Resource already exists in state
```
→ 이미 State에 있는 리소스입니다. `terraform state list`로 확인

### Import 실패: "Resource not found"
```
Error: Cannot import non-existent resource
```
→ AWS에 리소스가 실제로 존재하지 않습니다. 리소스 이름 확인

### Import 후에도 Plan에서 변경사항
```
Terraform will perform the following actions:
  ~ update in-place
```
→ 정상입니다. Import는 리소스를 State에 추가만 하고, 설정을 동기화하지 않습니다.
   apply를 실행하여 코드 설정을 AWS에 적용하세요.

## 참고 문서

- [Terraform Import 공식 문서](https://www.terraform.io/docs/cli/import/index.html)
- [AWS CLI Reference](https://docs.aws.amazon.com/cli/latest/reference/)
- [FileFlow Infrastructure CLAUDE.md](/Users/sangwon-ryu/fileflow/CLAUDE.md)
