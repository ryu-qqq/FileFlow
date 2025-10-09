# FileFlow Infrastructure - S3 Event Notification

S3 버킷 이벤트를 SQS로 전달하는 인프라 구성

## 📋 개요

이 Terraform 구성은 다음을 제공합니다:

- **SQS Queue**: 파일 업로드 이벤트를 수신하는 메인 큐
- **Dead Letter Queue**: 처리 실패 메시지를 저장하는 DLQ
- **S3 Event Notification**: S3 업로드 이벤트를 SQS로 전송
- **IAM Role & Policy**: 애플리케이션이 SQS 메시지를 읽을 수 있는 권한

## 🏗️ 아키텍처

```
┌─────────────┐      Event       ┌─────────────┐
│             │  Notification    │             │
│  S3 Bucket  ├─────────────────>│  SQS Queue  │
│             │                  │             │
└─────────────┘                  └──────┬──────┘
                                        │
                                        │ Poll
                                        │
                                 ┌──────▼──────┐
                                 │             │
                                 │ Application │
                                 │             │
                                 └─────────────┘
```

## 📂 디렉토리 구조

```
infrastructure/terraform/
├── modules/
│   ├── sqs/                    # SQS 큐 모듈
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── s3-event-notification/  # S3 이벤트 알림 모듈
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── iam/                    # IAM 역할/정책 모듈
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
└── environments/
    ├── dev/                    # 개발 환경
    │   ├── providers.tf
    │   ├── variables.tf
    │   ├── main.tf
    │   ├── outputs.tf
    │   └── terraform.tfvars.example
    └── prod/                   # 프로덕션 환경
        ├── providers.tf
        ├── variables.tf
        ├── main.tf
        ├── outputs.tf
        └── terraform.tfvars.example
```

## 🚀 사용 방법

### 1. 사전 요구사항

- Terraform >= 1.5.0
- AWS CLI 설정 완료
- S3 버킷이 이미 존재해야 함

### 2. 환경별 설정

#### Dev 환경 배포

```bash
cd infrastructure/terraform/environments/dev

# terraform.tfvars 파일 생성
cp terraform.tfvars.example terraform.tfvars

# 실제 값으로 수정
vim terraform.tfvars

# Terraform 초기화
terraform init

# 실행 계획 확인
terraform plan

# 적용
terraform apply
```

#### Prod 환경 배포

```bash
cd infrastructure/terraform/environments/prod

# terraform.tfvars 파일 생성
cp terraform.tfvars.example terraform.tfvars

# 실제 값으로 수정
vim terraform.tfvars

# Terraform 초기화
terraform init

# 실행 계획 확인
terraform plan

# 적용
terraform apply
```

### 3. 출력 값 확인

```bash
terraform output
```

주요 출력 값:
- `sqs_queue_url`: 애플리케이션에서 사용할 SQS 큐 URL
- `sqs_queue_arn`: SQS 큐 ARN
- `app_role_arn`: 애플리케이션 IAM 역할 ARN

## ⚙️ 설정 값

### SQS Queue 설정

| 설정 | 값 | 설명 |
|-----|-----|-----|
| Message Retention | 14일 | 메시지 보존 기간 |
| Visibility Timeout | 30초 | 메시지 처리 타임아웃 |
| Max Receive Count | 3회 | DLQ로 이동하기 전 최대 수신 횟수 |

### S3 Event Types

- `s3:ObjectCreated:Put`: PUT 메서드로 객체 생성
- `s3:ObjectCreated:CompleteMultipartUpload`: 멀티파트 업로드 완료

### IAM Permissions

**Application Role이 가진 권한:**
- `sqs:ReceiveMessage`: 메시지 수신
- `sqs:DeleteMessage`: 처리 완료된 메시지 삭제
- `sqs:GetQueueAttributes`: 큐 속성 조회
- `sqs:ChangeMessageVisibility`: 메시지 가시성 타임아웃 변경

## 🔍 테스트

### 1. S3 파일 업로드 테스트

```bash
# AWS CLI로 파일 업로드
aws s3 cp test-file.txt s3://your-bucket-name/

# SQS 메시지 확인
aws sqs receive-message \
  --queue-url <queue-url> \
  --max-number-of-messages 1
```

### 2. 메시지 형식 예시

```json
{
  "Records": [
    {
      "eventVersion": "2.1",
      "eventSource": "aws:s3",
      "awsRegion": "ap-northeast-2",
      "eventTime": "2024-10-09T12:00:00.000Z",
      "eventName": "ObjectCreated:Put",
      "s3": {
        "bucket": {
          "name": "fileflow-dev-uploads",
          "arn": "arn:aws:s3:::fileflow-dev-uploads"
        },
        "object": {
          "key": "test-file.txt",
          "size": 1024,
          "eTag": "abc123..."
        }
      }
    }
  ]
}
```

## 🛠️ 유지보수

### State 관리

프로덕션 환경에서는 S3 백엔드 사용을 권장합니다:

```hcl
# providers.tf의 backend 블록 주석 해제 및 설정
backend "s3" {
  bucket         = "fileflow-terraform-state-dev"
  key            = "dev/s3-event-notification/terraform.tfstate"
  region         = "ap-northeast-2"
  encrypt        = true
  dynamodb_table = "fileflow-terraform-locks"
}
```

### 리소스 삭제

```bash
terraform destroy
```

## 📝 주의사항

1. **S3 버킷 필수**: Terraform 실행 전 S3 버킷이 존재해야 합니다
2. **권한 관리**: 최소 권한 원칙에 따라 필요한 권한만 부여됩니다
3. **환경 분리**: Dev와 Prod는 완전히 독립된 리소스를 사용합니다
4. **태그 전략**: 모든 리소스에 Environment, Project, ManagedBy 태그가 자동으로 적용됩니다

## 🔗 관련 문서

- [AWS S3 Event Notifications](https://docs.aws.amazon.com/AmazonS3/latest/userguide/NotificationHowTo.html)
- [AWS SQS Developer Guide](https://docs.aws.amazon.com/sqs/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
