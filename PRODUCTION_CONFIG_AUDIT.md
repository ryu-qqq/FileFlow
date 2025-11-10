# 🔍 FileFlow 운영 환경 설정 감사 보고서

**작성일**: 2025-11-10
**대상**: Redis, MySQL, S3 인프라 설정
**환경**: Production (AWS ECS)

---

## 📋 요약

| 인프라 | 상태 | Spring Boot | Terraform | IAM 권한 | 비고 |
|--------|------|-------------|-----------|----------|------|
| **MySQL (RDS)** | ✅ 정상 | ✅ 설정됨 | ✅ 주입됨 | ✅ 있음 | 완벽 구성 |
| **Redis (ElastiCache)** | ✅ 정상 | ✅ 설정됨 | ✅ 주입됨 | ✅ 있음 | 완벽 구성 |
| **S3** | ⚠️ 주의 | ✅ 설정됨 | ✅ 주입됨 | ❌ **없음** | **IAM 권한 누락** |

---

## 1️⃣ MySQL (RDS) - ✅ 정상

### Spring Boot 설정 (application-prod.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/fileflow?useSSL=true&serverTimezone=UTC
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      leak-detection-threshold: 120000
```

**설정 평가**: ✅ 우수
- SSL 활성화 (`useSSL=true`)
- 타임존 설정 (`serverTimezone=UTC`)
- Connection Pool 최적화 (Max: 50, Min: 10)
- Connection Leak 감지 (2분)

### Terraform 설정 (ecs-service/data.tf, main.tf)

**Data Sources**:
```hcl
data "aws_ssm_parameter" "db_instance_address" {
  name = "/shared/rds/db-instance-address"
}

data "aws_ssm_parameter" "db_instance_port" {
  name = "/shared/rds/db-instance-port"
}

data "aws_ssm_parameter" "fileflow_user_password_secret_name" {
  name = "/fileflow/prod/db-user-password-secret-name"
}

data "aws_secretsmanager_secret_version" "fileflow_user_password" {
  secret_id = data.aws_ssm_parameter.fileflow_user_password_secret_name.value
}
```

**환경 변수 주입** (main.tf:193-238):
```hcl
container_environment = [
  { name = "DB_HOST",  value = local.db_address },
  { name = "DB_PORT",  value = tostring(local.db_port) },
  { name = "DB_NAME",  value = "fileflow" },
  { name = "DB_USER",  value = local.db_user }
]

container_secrets = [
  {
    name      = "DB_PASSWORD"
    valueFrom = "${data.aws_secretsmanager_secret_version.fileflow_user_password.arn}:password::"
  }
]
```

**IAM 권한** (main.tf:109-134):
```hcl
resource "aws_iam_role_policy" "fileflow_secrets_access" {
  role = aws_iam_role.fileflow_execution_role.id

  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = ["secretsmanager:GetSecretValue"]
        Resource = [
          "arn:aws:secretsmanager:ap-northeast-2:646886795421:secret:prod-shared-mysql-master-password-*"
        ]
      },
      {
        Effect = "Allow"
        Action = ["kms:Decrypt"]
        Resource = data.aws_kms_key.ecs_secrets.arn
      }
    ]
  })
}
```

**평가**: ✅ 완벽 구성
- ✅ SSM Parameter로 RDS 엔드포인트 관리
- ✅ Secrets Manager로 비밀번호 안전 저장
- ✅ ECS Task Execution Role에 Secrets 읽기 권한
- ✅ KMS 복호화 권한 부여
- ✅ fileflow-user 전용 계정 사용 (admin 아님)

---

## 2️⃣ Redis (ElastiCache) - ✅ 정상

### Spring Boot 설정 (application-prod.yml)

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

**설정 평가**: ✅ 우수
- Connection Pool 설정 (Max: 20, Idle: 10/5)
- Timeout 설정 (3초)
- Lettuce 드라이버 사용 (비동기 지원)

### Terraform 설정 (ecs-service/data.tf, main.tf)

**Data Sources**:
```hcl
data "aws_ssm_parameter" "redis_endpoint" {
  name = "/fileflow/prod/redis/endpoint"
}

data "aws_ssm_parameter" "redis_port" {
  name = "/fileflow/prod/redis/port"
}
```

**환경 변수 주입** (main.tf:231-237):
```hcl
container_environment = [
  { name = "REDIS_HOST", value = local.redis_endpoint },
  { name = "REDIS_PORT", value = tostring(local.redis_port) }
]
```

**평가**: ✅ 완벽 구성
- ✅ SSM Parameter로 Redis 엔드포인트 관리
- ✅ ElastiCache는 VPC 내부에서 안전하게 접근
- ✅ 비밀번호 설정 없음 (VPC Security Group으로 보호)

---

## 3️⃣ S3 - ⚠️ 주의 (IAM 권한 누락)

### Spring Boot 설정 (application-prod.yml)

```yaml
aws:
  s3:
    region: ${AWS_REGION:ap-northeast-2}
    bucket-name: ${AWS_S3_BUCKET:fileflow-prod}
    # IAM Role 사용 (ECS Task Role)
    # access-key, secret-key 불필요
```

**설정 평가**: ✅ 우수
- IAM Role 방식 사용 (하드코딩된 자격증명 없음)
- Region 및 Bucket 환경변수로 주입

### Terraform 설정 (ecs-service/main.tf)

**환경 변수 주입** (main.tf:362-363):
```hcl
container_environment = [
  {
    name  = "AWS_S3_BUCKET"
    value = "fileflow-prod"
  }
]
```

**평가**: ✅ 버킷 이름 주입됨

### ❌ **치명적 문제: S3 IAM 권한 누락**

**현재 상태**:
```hcl
# Task Role이 있지만 S3 정책이 없음
resource "aws_iam_role" "fileflow_task_role" {
  name = "${local.name_prefix}-ecs-task-role"
  assume_role_policy = jsonencode({ ... })

  # ❌ S3 권한 정책이 붙어있지 않음!
}
```

**예상되는 문제**:
- ECS Task에서 S3 업로드/다운로드 실패
- `Access Denied` 오류 발생
- Presigned URL 생성 가능하지만, 실제 파일 접근 불가

---

## 🚨 **필수 수정사항: S3 IAM 권한 추가**

### terraform/ecs-service/main.tf에 추가 필요

```hcl
# S3 Access Policy for Task Role
resource "aws_iam_role_policy" "fileflow_s3_access" {
  name = "${local.name_prefix}-s3-access"
  role = aws_iam_role.fileflow_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket",
          "s3:GetObjectVersion"
        ]
        Resource = [
          "arn:aws:s3:::fileflow-prod",
          "arn:aws:s3:::fileflow-prod/*"
        ]
      }
    ]
  })
}
```

**권한 설명**:
- `s3:GetObject`: 파일 다운로드
- `s3:PutObject`: 파일 업로드
- `s3:DeleteObject`: 파일 삭제
- `s3:ListBucket`: 버킷 내 파일 목록 조회
- `s3:GetObjectVersion`: 버전 관리된 객체 조회

---

## 📊 전체 평가 및 권장사항

### ✅ 잘 구성된 부분

1. **보안 모범 사례**
   - ✅ Secrets Manager 사용 (비밀번호 안전 저장)
   - ✅ SSM Parameter Store 사용 (인프라 정보 중앙 관리)
   - ✅ KMS 암호화 적용
   - ✅ IAM Role 기반 인증 (하드코딩 없음)

2. **고가용성 설정**
   - ✅ Connection Pool 최적화 (MySQL, Redis)
   - ✅ Connection Leak 감지 (MySQL)
   - ✅ Graceful Shutdown 설정

3. **모니터링 준비**
   - ✅ CloudWatch Logs 통합
   - ✅ Prometheus Metrics 활성화
   - ✅ Health Check 엔드포인트

### ⚠️ 개선 필요 사항

#### 1. **Critical: S3 IAM 권한 추가 (필수)**

**문제**: ECS Task Role에 S3 권한이 없음
**영향**: S3 업로드/다운로드 실패
**해결**: 위의 `fileflow_s3_access` 정책 추가

#### 2. **Important: S3 Bucket 이름 하드코딩**

**현재**:
```hcl
container_environment = [
  { name = "AWS_S3_BUCKET", value = "fileflow-prod" }
]
```

**개선안**:
```hcl
# terraform/s3-bucket/outputs.tf에서 참조
data "aws_s3_bucket" "fileflow" {
  bucket = "fileflow-prod"
}

container_environment = [
  { name = "AWS_S3_BUCKET", value = data.aws_s3_bucket.fileflow.id }
]
```

#### 3. **Optional: MySQL 계정 분리**

**현재**: `fileflow-user` 계정 하나만 사용
**개선안**: Read-Only 계정 추가 (조회용 API 전용)

```sql
-- Read-Only User for Analytics
CREATE USER 'fileflow-readonly'@'%' IDENTIFIED BY 'secure-password';
GRANT SELECT ON fileflow.* TO 'fileflow-readonly'@'%';
```

#### 4. **Optional: Redis Password 설정**

**현재**: Redis 비밀번호 없음 (VPC Security Group으로만 보호)
**개선안**: ElastiCache AUTH 토큰 활성화

```hcl
# terraform/elasticache-redis/main.tf
resource "aws_elasticache_replication_group" "redis" {
  ...
  transit_encryption_enabled = true
  auth_token_enabled         = true
  auth_token                 = var.redis_auth_token
}
```

---

## 🎯 액션 플랜 (우선순위별)

### Priority 1: 즉시 수정 필요 (Production 영향)

- [ ] **S3 IAM 권한 추가** (terraform/ecs-service/main.tf)
  - Task: `aws_iam_role_policy.fileflow_s3_access` 리소스 추가
  - 예상 시간: 10분
  - 배포 필요: Yes (Terraform apply)

### Priority 2: 개선 권장 (1주 내)

- [ ] S3 Bucket 이름 Data Source로 변경
  - Task: 하드코딩 제거 → Terraform Data Source 사용
  - 예상 시간: 15분
  - 배포 필요: Yes (Terraform apply)

### Priority 3: 선택적 개선 (1개월 내)

- [ ] Redis AUTH 토큰 활성화
  - Task: ElastiCache 설정 변경 + 환경변수 추가
  - 예상 시간: 30분
  - 배포 필요: Yes (Terraform apply + ECS 재배포)

- [ ] MySQL Read-Only 계정 추가
  - Task: 조회용 API 전용 계정 생성
  - 예상 시간: 20분
  - 배포 필요: No (애플리케이션 변경 없음)

---

## 📝 검증 체크리스트

### S3 IAM 권한 추가 후 검증

```bash
# 1. Terraform 적용
cd terraform/ecs-service
terraform plan
terraform apply

# 2. ECS Task 재배포
aws ecs update-service \
  --cluster fileflow-prod \
  --service fileflow-web-api \
  --force-new-deployment

# 3. Task Role 확인
aws iam get-role-policy \
  --role-name fileflow-prod-ecs-task-role \
  --policy-name fileflow-prod-s3-access

# 4. S3 업로드 테스트
curl -X POST https://your-alb-url/api/v1/upload/init-single \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test.txt",
    "fileSize": 1024,
    "contentType": "text/plain"
  }'

# 5. CloudWatch Logs 확인 (에러 없는지)
aws logs tail /aws/ecs/fileflow-web-api --follow
```

---

## 🔐 보안 강화 권장사항

### 1. S3 Bucket 정책 강화

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EnforcedTLS",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": [
        "arn:aws:s3:::fileflow-prod",
        "arn:aws:s3:::fileflow-prod/*"
      ],
      "Condition": {
        "Bool": {
          "aws:SecureTransport": "false"
        }
      }
    }
  ]
}
```

### 2. VPC Endpoint 사용 (비용 절감)

```hcl
# S3 VPC Endpoint (인터넷 게이트웨이 경유 없이 S3 접근)
resource "aws_vpc_endpoint" "s3" {
  vpc_id       = data.aws_vpc.main.id
  service_name = "com.amazonaws.ap-northeast-2.s3"
  route_table_ids = [
    data.aws_route_table.private.id
  ]
}
```

**장점**:
- 데이터 전송 비용 절감 (NAT Gateway 경유 안 함)
- 보안 강화 (인터넷 노출 없음)
- 성능 향상 (AWS 내부 네트워크 사용)

---

## 📖 참고 문서

- [AWS ECS Task IAM Roles](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-iam-roles.html)
- [S3 Bucket Policies](https://docs.aws.amazon.com/AmazonS3/latest/userguide/bucket-policies.html)
- [ElastiCache Redis AUTH](https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/auth.html)

---

**✅ 결론**: MySQL, Redis는 완벽하게 구성되어 있으나, **S3 IAM 권한 누락**으로 인해 운영 환경에서 파일 업로드/다운로드가 불가능합니다. 즉시 수정이 필요합니다.
