# FileFlow Scheduler 배포 가이드

## 개요

FileFlow는 세 개의 ECS Service로 구성됩니다:

| Service | Type | Desired Count | Port | Load Balancer |
|---------|------|---------------|------|---------------|
| **fileflow-web-api** | REST API | 1+ (Auto Scaling) | 8083 | ✅ ALB |
| **fileflow-scheduler-download** | Background Scheduler | **1 (고정)** | 9091 (Actuator) | ❌ None |
| **fileflow-scheduler-pipeline** | Background Scheduler | **1 (고정)** | 9092 (Actuator) | ❌ None |

---

## 스케줄러 특징

### 1. Desired Count = 1 (고정)

스케줄러는 **반드시 1개의 인스턴스만** 실행되어야 합니다:

- ✅ **중복 실행 방지**: 동일한 작업이 여러 번 실행되는 것을 방지
- ✅ **데이터 정합성**: Outbox 패턴에서 메시지 중복 처리 방지
- ✅ **리소스 효율성**: 불필요한 리소스 낭비 방지

### 2. Load Balancer 없음

스케줄러는 외부 트래픽을 받지 않으므로 ALB가 필요 없습니다:

- ✅ **Actuator 포트만 노출**: Prometheus 메트릭 수집용
- ✅ **내부 통신만**: VPC 내부에서만 접근 가능

### 3. 리소스 할당

```hcl
# Download Scheduler
cpu    = 512   # 0.5 vCPU
memory = 1024  # 1 GB

# Pipeline Scheduler
cpu    = 512   # 0.5 vCPU
memory = 1024  # 1 GB
```

---

## 배포 구조

```
ECS Cluster: fileflow-prod
├── Service: fileflow-web-api (desired_count: 1+)
│   ├── Task Definition: fileflow-web-api-prod
│   ├── Container: fileflow (port: 8083)
│   ├── ALB: fileflow-prod-alb
│   └── CloudWatch Logs: /aws/ecs/fileflow
│
├── Service: fileflow-scheduler-download (desired_count: 1)
│   ├── Task Definition: fileflow-scheduler-download-prod
│   ├── Container: fileflow-scheduler-download (port: 9091)
│   ├── No ALB
│   └── CloudWatch Logs: /aws/ecs/fileflow-scheduler-download
│
└── Service: fileflow-scheduler-pipeline (desired_count: 1)
    ├── Task Definition: fileflow-scheduler-pipeline-prod
    ├── Container: fileflow-scheduler-pipeline (port: 9092)
    ├── No ALB
    └── CloudWatch Logs: /aws/ecs/fileflow-scheduler-pipeline
```

---

## Terraform 배포

### 1. 초기 배포

```bash
cd terraform/ecs-service

# Terraform 초기화
terraform init

# 변경 사항 확인
terraform plan

# 배포 실행
terraform apply
```

### 2. 배포 확인

```bash
# ECS 클러스터 확인
aws ecs list-clusters --region ap-northeast-2

# ECS 서비스 확인
aws ecs list-services \
  --cluster fileflow-prod \
  --region ap-northeast-2

# 서비스 상세 정보
aws ecs describe-services \
  --cluster fileflow-prod \
  --services fileflow-web-api fileflow-scheduler-download fileflow-scheduler-pipeline \
  --region ap-northeast-2

# 실행 중인 태스크 확인
aws ecs list-tasks \
  --cluster fileflow-prod \
  --region ap-northeast-2
```

### 3. 로그 확인

```bash
# Web API 로그
aws logs tail /aws/ecs/fileflow --follow --region ap-northeast-2

# Download Scheduler 로그
aws logs tail /aws/ecs/fileflow-scheduler-download --follow --region ap-northeast-2

# Pipeline Scheduler 로그
aws logs tail /aws/ecs/fileflow-scheduler-pipeline --follow --region ap-northeast-2
```

---

## ECR 이미지 태그 전략

### 이미지 이름 규칙

```
# Web API
{ECR_REPOSITORY_URL}:latest
{ECR_REPOSITORY_URL}:web-api-{VERSION}

# Download Scheduler
{ECR_REPOSITORY_URL}:download-scheduler-latest
{ECR_REPOSITORY_URL}:download-scheduler-{VERSION}

# Pipeline Scheduler
{ECR_REPOSITORY_URL}:pipeline-scheduler-latest
{ECR_REPOSITORY_URL}:pipeline-scheduler-{VERSION}
```

### 예시

```
646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:latest
646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:download-scheduler-latest
646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:pipeline-scheduler-latest
```

---

## Docker 이미지 빌드 & 푸시

### 1. Web API

```bash
# 빌드
./gradlew :bootstrap:bootstrap-web-api:bootBuildImage

# 태그
docker tag fileflow-web-api:latest \
  646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:latest

# 푸시
docker push 646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:latest
```

### 2. Download Scheduler

```bash
# 빌드
./gradlew :bootstrap:bootstrap-scheduler-download:bootBuildImage

# 태그
docker tag fileflow-scheduler-download:latest \
  646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:download-scheduler-latest

# 푸시
docker push 646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:download-scheduler-latest
```

### 3. Pipeline Scheduler

```bash
# 빌드
./gradlew :bootstrap:bootstrap-scheduler-pipeline:bootBuildImage

# 태그
docker tag fileflow-scheduler-pipeline:latest \
  646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:pipeline-scheduler-latest

# 푸시
docker push 646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow:pipeline-scheduler-latest
```

---

## 스케줄러 재시작

### 방법 1: ECS 콘솔

1. ECS 클러스터 → `fileflow-prod` 선택
2. 서비스 → `fileflow-scheduler-download` 또는 `fileflow-scheduler-pipeline` 선택
3. **Update Service** 클릭
4. **Force new deployment** 체크
5. **Update** 클릭

### 방법 2: AWS CLI

```bash
# Download Scheduler 재시작
aws ecs update-service \
  --cluster fileflow-prod \
  --service fileflow-scheduler-download \
  --force-new-deployment \
  --region ap-northeast-2

# Pipeline Scheduler 재시작
aws ecs update-service \
  --cluster fileflow-prod \
  --service fileflow-scheduler-pipeline \
  --force-new-deployment \
  --region ap-northeast-2
```

---

## 모니터링

### 1. Prometheus 메트릭

스케줄러는 Actuator를 통해 메트릭을 노출합니다:

```bash
# Download Scheduler (VPC 내부에서만 접근 가능)
curl http://<TASK_PRIVATE_IP>:9091/actuator/prometheus

# Pipeline Scheduler
curl http://<TASK_PRIVATE_IP>:9092/actuator/prometheus
```

### 2. CloudWatch 메트릭

```bash
# CPU 사용률
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name CPUUtilization \
  --dimensions Name=ServiceName,Value=fileflow-scheduler-download Name=ClusterName,Value=fileflow-prod \
  --start-time 2025-01-01T00:00:00Z \
  --end-time 2025-01-01T23:59:59Z \
  --period 3600 \
  --statistics Average \
  --region ap-northeast-2

# 메모리 사용률
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name MemoryUtilization \
  --dimensions Name=ServiceName,Value=fileflow-scheduler-download Name=ClusterName,Value=fileflow-prod \
  --start-time 2025-01-01T00:00:00Z \
  --end-time 2025-01-01T23:59:59Z \
  --period 3600 \
  --statistics Average \
  --region ap-northeast-2
```

---

## 트러블슈팅

### 1. 스케줄러가 시작되지 않음

**증상**: ECS 태스크가 계속 재시작됨

**확인 사항**:
```bash
# 태스크 상태 확인
aws ecs describe-tasks \
  --cluster fileflow-prod \
  --tasks <TASK_ARN> \
  --region ap-northeast-2

# 로그 확인
aws logs tail /aws/ecs/fileflow-scheduler-download --follow --region ap-northeast-2
```

**일반적인 원인**:
- DB 연결 실패 (환경변수 확인)
- Redis 연결 실패
- Secrets Manager 접근 권한 없음
- 메모리 부족 (OOM)

### 2. 스케줄러가 2개 이상 실행됨

**증상**: Desired Count가 1인데 여러 태스크가 실행 중

**해결**:
```bash
# 서비스 업데이트로 강제 조정
aws ecs update-service \
  --cluster fileflow-prod \
  --service fileflow-scheduler-download \
  --desired-count 1 \
  --region ap-northeast-2
```

### 3. 메트릭 수집 안됨

**증상**: Prometheus에서 스케줄러 메트릭이 보이지 않음

**확인**:
```bash
# 태스크 Private IP 확인
aws ecs describe-tasks \
  --cluster fileflow-prod \
  --tasks <TASK_ARN> \
  --region ap-northeast-2 \
  --query 'tasks[0].attachments[0].details[?name==`privateIPv4Address`].value' \
  --output text

# Actuator 엔드포인트 테스트 (VPC 내부에서)
curl http://<PRIVATE_IP>:9091/actuator/health
curl http://<PRIVATE_IP>:9091/actuator/prometheus
```

---

## Terraform Outputs

배포 후 확인 가능한 Output 값들:

```bash
terraform output

# Web API
ecs_service_name = "fileflow"
alb_dns_name = "fileflow-prod-alb-123456789.ap-northeast-2.elb.amazonaws.com"
cloudwatch_log_group_name = "/aws/ecs/fileflow"

# Download Scheduler
download_scheduler_service_name = "fileflow-scheduler-download"
download_scheduler_log_group_name = "/aws/ecs/fileflow-scheduler-download"

# Pipeline Scheduler
pipeline_scheduler_service_name = "fileflow-scheduler-pipeline"
pipeline_scheduler_log_group_name = "/aws/ecs/fileflow-scheduler-pipeline"
```

---

## 비용 최적화

### 스케줄러 리소스 조정

현재 설정:
```hcl
cpu    = 512   # 0.5 vCPU
memory = 1024  # 1 GB
```

부하가 낮다면 더 줄일 수 있습니다:
```hcl
cpu    = 256   # 0.25 vCPU
memory = 512   # 0.5 GB
```

### Fargate Spot 사용 (선택사항)

스케줄러는 중단되어도 자동으로 재시작되므로 Fargate Spot을 사용하여 비용을 절감할 수 있습니다:

```hcl
capacity_provider_strategy = [
  {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
    base              = 0
  }
]
```

---

## 참고 자료

- [ECS Service Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs_services.html)
- [Fargate Task Definitions](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html)
- [CloudWatch Logs for ECS](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/using_cloudwatch_logs.html)
