# 완전한 개발 파이프라인 전략

> **목표**: 초기 인프라 구축부터 지속적인 개발까지 체계적이고 반복 가능한 파이프라인 구축

---

## 📋 목차

1. [전체 아키텍처 개요](#1-전체-아키텍처-개요)
2. [Phase 1: 초기 인프라 구축 (One-time Setup)](#2-phase-1-초기-인프라-구축-one-time-setup)
3. [Phase 2: 지속적인 개발 파이프라인](#3-phase-2-지속적인-개발-파이프라인)
4. [Phase 3: 인프라 변경 관리](#4-phase-3-인프라-변경-관리)
5. [시나리오별 워크플로우](#5-시나리오별-워크플로우)
6. [버전 관리 전략](#6-버전-관리-전략)
7. [체크리스트 & 템플릿](#7-체크리스트--템플릿)

---

## 1. 전체 아키텍처 개요

### 1.1. 프로젝트 구조

```
┌─────────────────────────────────────────────────────────────┐
│ 👨‍💻 Developer                                                  │
└─────────────────┬───────────────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│ Infrastructure   │  │ Spring Project   │
│ (Terraform)      │  │ (Application)    │
├──────────────────┤  ├──────────────────┤
│ - VPC/Subnet     │  │ - Domain         │
│ - RDS/Cache      │  │ - Application    │
│ - ECR/ECS        │  │ - Adapter        │
│ - ALB/S3         │  │ - Bootstrap      │
│ - Security       │  │ - Dockerfile     │
└────────┬─────────┘  └────────┬─────────┘
         │                     │
         │ Terraform Apply     │ Git Push
         │                     │
         ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│ ☁️  AWS Cloud                                                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐                │
│  │   VPC    │   │   RDS    │   │   S3     │                │
│  └──────────┘   └──────────┘   └──────────┘                │
│                                                              │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐                │
│  │   ECR    │→  │   ECS    │←  │   ALB    │                │
│  └──────────┘   └──────────┘   └──────────┘                │
│                                                              │
└─────────────────────────────────────────────────────────────┘
         ▲                     ▲
         │                     │
         │ Deploy              │ CI/CD
         │                     │
┌────────┴─────────────────────┴─────────┐
│ 🤖 GitHub Actions                       │
├─────────────────────────────────────────┤
│ - ci-build-test.yml (PR 검증)          │
│ - cd-build-push-ecr.yml (ECR 푸시)     │
│ - cd-deploy-ecs.yml (ECS 배포)         │
└─────────────────────────────────────────┘
```

### 1.2. 핵심 원칙

1. **Infrastructure as Code**: 모든 인프라는 Terraform으로 관리, 수동 변경 금지
2. **명확한 의존성 순서**: 인프라는 아래에서 위로 (VPC → RDS → ECS)
3. **자동화된 파이프라인**: 반복 가능한 프로세스, 수동 개입 최소화
4. **버전 관리**: 코드, 인프라, 이미지 모두 버전 관리
5. **환경 분리**: dev, staging, prod 환경 완전 분리

### 1.3. 주요 컴포넌트

| 컴포넌트 | 역할 | 관리 방법 |
|---------|------|----------|
| **Infrastructure 프로젝트** | AWS 인프라 정의 | Terraform (IaC) |
| **Spring 프로젝트** | 애플리케이션 코드 | Git (GitHub) |
| **ECR** | Docker 이미지 저장소 | Terraform + GitHub Actions |
| **ECS** | 컨테이너 실행 환경 | Terraform + Task Definition |
| **RDS/Cache** | 데이터 레이어 | Terraform |
| **ALB** | 로드 밸런서 | Terraform |
| **GitHub Actions** | CI/CD 파이프라인 | YAML 워크플로우 |

---

## 2. Phase 1: 초기 인프라 구축 (One-time Setup)

### 2.1. 목표

- 애플리케이션이 실행될 수 있는 완전한 AWS 환경 구축
- 모든 리소스는 Terraform으로 관리
- 한 번 구축 후 코드로만 관리

### 2.2. 인프라 구축 순서 (의존성 기반)

```
1️⃣  VPC & Networking
     ↓
2️⃣  Security Groups
     ↓
3️⃣  Data Layer (RDS, ElastiCache)
     ↓
4️⃣  Storage (S3)
     ↓
5️⃣  Container Registry (ECR)
     ↓
6️⃣  Load Balancer (ALB)
     ↓
7️⃣  Compute Layer (ECS Cluster)
     ↓
8️⃣  CI/CD Infrastructure (IAM OIDC)
     ↓
9️⃣  ECS Task Definition & Service (애플리케이션 준비 후)
```

### 2.3. 실행 명령어 (Infrastructure 프로젝트)

**위치**: `/Users/sangwon-ryu/infrastructure`

#### Step 1: VPC & Networking (5분)

```bash
# VPC 생성 (자동으로 Subnet, NAT Gateway, Route Table 생성)
/if:create vpc spring-app dev

# 생성 결과:
# - VPC: spring-app-dev-vpc
# - Public Subnets: 2개 (AZ-a, AZ-c)
# - Private Subnets: 2개 (AZ-a, AZ-c)
# - NAT Gateway: 1개 (고가용성 필요 시 2개)
# - Internet Gateway: 1개
```

#### Step 2: Security Groups (3분)

```bash
# ALB Security Group (인터넷에서 80/443 접근 허용)
/if:create security-group alb-sg dev \
  --vpc spring-app-dev \
  --ingress "0.0.0.0/0:80,443" \
  --description "ALB security group"

# ECS Security Group (ALB에서만 접근 허용)
/if:create security-group ecs-sg dev \
  --vpc spring-app-dev \
  --ingress "alb-sg:8080" \
  --description "ECS tasks security group"

# RDS Security Group (ECS에서만 접근 허용)
/if:create security-group rds-sg dev \
  --vpc spring-app-dev \
  --ingress "ecs-sg:3306" \
  --description "RDS MySQL security group"

# ElastiCache Security Group
/if:create security-group cache-sg dev \
  --vpc spring-app-dev \
  --ingress "ecs-sg:6379" \
  --description "ElastiCache Redis security group"
```

#### Step 3: Data Layer (10-15분)

```bash
# RDS MySQL 생성
/if:create rds spring-db dev \
  --engine mysql \
  --version 8.0 \
  --instance-class db.t3.medium \
  --allocated-storage 20 \
  --multi-az false \
  --security-group rds-sg \
  --subnet-group spring-app-private

# 생성 결과:
# - Endpoint: spring-db-dev.xxxxx.ap-northeast-2.rds.amazonaws.com
# - Port: 3306
# - Master Username: admin
# - Master Password: (Secrets Manager에 자동 저장)
# - SSM Parameters:
#   - /rds/spring-db-dev/endpoint
#   - /rds/spring-db-dev/port
#   - /rds/spring-db-dev/username
#   - /rds/spring-db-dev/secret-arn

# ElastiCache Redis 생성
/if:create elasticache spring-cache dev \
  --engine redis \
  --node-type cache.t3.micro \
  --num-nodes 1 \
  --security-group cache-sg \
  --subnet-group spring-app-private

# 생성 결과:
# - Endpoint: spring-cache-dev.xxxxx.cache.amazonaws.com
# - Port: 6379
# - SSM Parameters:
#   - /elasticache/spring-cache-dev/endpoint
#   - /elasticache/spring-cache-dev/port
```

#### Step 4: Storage (2분)

```bash
# S3 버킷 생성 (파일 업로드용)
/if:create s3 spring-assets dev \
  --versioning enabled \
  --encryption enabled \
  --lifecycle-days 90

# 생성 결과:
# - Bucket Name: spring-assets-dev-xxxxxxxx
# - Versioning: Enabled
# - Encryption: AES256
# - Lifecycle: 90일 후 Glacier로 이동
```

#### Step 5: Container Registry (2분)

```bash
# Web API용 ECR
/if:create ecr spring-web-api dev

# Scheduler용 ECR
/if:create ecr spring-scheduler dev

# 생성 결과:
# - Repository URI: 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/spring-web-api-dev
# - KMS Encryption: Enabled
# - Image Scanning: Enabled
# - Lifecycle Policy: 30개 이미지 유지
# - SSM Parameters:
#   - /ecr/spring-web-api-dev/repository-url
#   - /ecr/spring-web-api-dev/repository-arn
```

#### Step 6: Load Balancer (5분)

```bash
# ALB 생성
/if:create alb spring-alb dev \
  --vpc spring-app-dev \
  --subnets "public-a,public-c" \
  --security-group alb-sg \
  --scheme internet-facing

# 생성 결과:
# - DNS Name: spring-alb-dev-xxxxxxxx.ap-northeast-2.elb.amazonaws.com
# - Listener: 80 (HTTP)
# - Listener: 443 (HTTPS, 인증서 필요)

# Target Group 생성 (Web API용)
/if:create alb-target-group spring-web-api-tg dev \
  --vpc spring-app-dev \
  --port 8080 \
  --protocol HTTP \
  --health-check-path /actuator/health \
  --target-type ip

# Target Group 생성 (Scheduler용, 필요 시)
# Scheduler는 주로 내부 작업이므로 ALB 불필요, 생략 가능
```

#### Step 7: ECS Cluster (3분)

```bash
# ECS Cluster 생성
/if:create ecs-cluster spring-cluster dev \
  --launch-type FARGATE \
  --container-insights enabled

# 생성 결과:
# - Cluster Name: spring-cluster-dev
# - Launch Type: FARGATE
# - Container Insights: Enabled (CloudWatch 모니터링)
```

#### Step 8: CI/CD Infrastructure (5분)

```bash
# GitHub OIDC Provider 생성 (GitHub Actions → AWS 인증)
/if:create iam-oidc github-actions \
  --provider-url "https://token.actions.githubusercontent.com" \
  --client-id "sts.amazonaws.com"

# IAM Role 생성 (GitHub Actions용)
/if:create iam-role github-actions-role \
  --trust-entity github-oidc \
  --policies "ECRPowerUser,ECSTaskExecutionRole,S3FullAccess" \
  --repository "your-org/claude-spring-standards"

# 생성 결과:
# - Role ARN: arn:aws:iam::123456789012:role/github-actions-role
# - Trust Policy: GitHub OIDC 기반
# - GitHub Secrets에 등록 필요:
#   - AWS_ROLE_TO_ASSUME: arn:aws:iam::123456789012:role/github-actions-role
```

#### Step 9: ECS Task Definition & Service (애플리케이션 준비 후)

**주의**: 이 단계는 Spring 프로젝트에서 Dockerfile과 애플리케이션 코드가 준비된 후 실행!

```bash
# Task Definition 생성은 Spring 프로젝트에서 관리
# (.aws/task-definition-web-api.json)

# ECS Service 생성 (Web API)
/if:create ecs-service spring-web-api-service dev \
  --cluster spring-cluster-dev \
  --task-definition spring-web-api:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --subnets "private-a,private-c" \
  --security-groups ecs-sg \
  --target-group spring-web-api-tg \
  --deployment-type blue-green

# ECS Service 생성 (Scheduler)
/if:create ecs-service spring-scheduler-service dev \
  --cluster spring-cluster-dev \
  --task-definition spring-scheduler:1 \
  --desired-count 1 \
  --launch-type FARGATE \
  --subnets "private-a,private-c" \
  --security-groups ecs-sg \
  --deployment-type rolling
```

### 2.4. 초기 구축 체크리스트

- [ ] VPC 및 네트워킹 구성 완료
- [ ] Security Groups 생성 완료 (ALB, ECS, RDS, Cache)
- [ ] RDS MySQL 생성 및 접근 가능 확인
- [ ] ElastiCache Redis 생성 및 접근 가능 확인
- [ ] S3 버킷 생성 및 업로드 테스트
- [ ] ECR Repository 생성 (web-api, scheduler)
- [ ] ALB 및 Target Group 생성 완료
- [ ] ECS Cluster 생성 완료
- [ ] GitHub OIDC 및 IAM Role 설정 완료
- [ ] SSM Parameter Store에 모든 엔드포인트 저장 확인
- [ ] Terraform State는 S3 Backend에 안전하게 저장

**예상 소요 시간**: 30-40분

---

## 3. Phase 2: 지속적인 개발 파이프라인

### 3.1. 목표

- 코드 변경 시 자동으로 빌드 → 테스트 → ECR 푸시 → ECS 배포
- 변경된 모듈만 선택적으로 배포 (효율성)
- Blue/Green 또는 Rolling 배포로 무중단 배포

### 3.2. GitHub Actions 워크플로우 구조

```
PR 생성
  ↓
ci-build-test.yml (기존)
  - Code Quality (Checkstyle, SpotBugs, PMD)
  - Build (Gradle)
  - Unit Tests
  - Architecture Tests (ArchUnit)
  - Integration Tests (Testcontainers)
  ↓
PR 승인 → main 머지
  ↓
cd-build-push-ecr.yml (신규)
  - Detect Changes (paths-filter)
  - Build Docker Image (변경된 bootstrap만)
  - Push to ECR (tag: commit-sha, latest)
  - Scan for Vulnerabilities
  ↓
cd-deploy-ecs.yml (신규)
  - Update Task Definition (new image tag)
  - Update ECS Service
  - Blue/Green or Rolling Deployment
  - Health Check
  ↓
✅ 배포 완료
```

### 3.3. Spring 프로젝트 구성

#### 3.3.1. Dockerfile 작성

**위치**: `bootstrap/bootstrap-web-api/Dockerfile`

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY domain domain
COPY application application
COPY adapter-in adapter-in
COPY adapter-out adapter-out
COPY bootstrap/bootstrap-web-api bootstrap/bootstrap-web-api
RUN ./gradlew :bootstrap:bootstrap-web-api:build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=builder /app/bootstrap/bootstrap-web-api/build/libs/*.jar app.jar
RUN chown spring:spring app.jar
USER spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

**위치**: `bootstrap/bootstrap-scheduler/Dockerfile` (유사하게 작성)

#### 3.3.2. Task Definition 작성

**위치**: `.aws/task-definition-web-api.json`

```json
{
  "family": "spring-web-api",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::123456789012:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "spring-web-api",
      "image": "123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/spring-web-api-dev:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "essential": true,
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "dev"
        }
      ],
      "secrets": [
        {
          "name": "DB_ENDPOINT",
          "valueFrom": "/rds/spring-db-dev/endpoint"
        },
        {
          "name": "DB_USERNAME",
          "valueFrom": "/rds/spring-db-dev/username"
        },
        {
          "name": "DB_PASSWORD",
          "valueFrom": "/rds/spring-db-dev/secret-arn:password::"
        },
        {
          "name": "REDIS_ENDPOINT",
          "valueFrom": "/elasticache/spring-cache-dev/endpoint"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/spring-web-api",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

**위치**: `.aws/task-definition-scheduler.json` (유사하게 작성)

#### 3.3.3. GitHub Actions 워크플로우

**위치**: `.github/workflows/cd-build-push-ecr.yml`

```yaml
name: CD - Build and Push to ECR

on:
  push:
    branches:
      - main
  workflow_dispatch:

env:
  AWS_REGION: ap-northeast-2
  ECR_REPOSITORY_WEB_API: spring-web-api-dev
  ECR_REPOSITORY_SCHEDULER: spring-scheduler-dev

permissions:
  id-token: write
  contents: read

jobs:
  detect-changes:
    name: Detect Changed Modules
    runs-on: ubuntu-latest
    outputs:
      web-api: ${{ steps.filter.outputs.web-api }}
      scheduler: ${{ steps.filter.outputs.scheduler }}
      domain: ${{ steps.filter.outputs.domain }}
      application: ${{ steps.filter.outputs.application }}
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Detect module changes
        uses: dorny/paths-filter@v3
        id: filter
        with:
          filters: |
            domain:
              - 'domain/**'
            application:
              - 'application/**'
            web-api:
              - 'bootstrap/bootstrap-web-api/**'
              - 'adapter-in/rest-api/**'
            scheduler:
              - 'bootstrap/bootstrap-scheduler/**'

  build-and-push-web-api:
    name: Build and Push Web API to ECR
    runs-on: ubuntu-latest
    needs: detect-changes
    if: |
      needs.detect-changes.outputs.web-api == 'true' ||
      needs.detect-changes.outputs.domain == 'true' ||
      needs.detect-changes.outputs.application == 'true'
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Build with Gradle
        run: ./gradlew :bootstrap:bootstrap-web-api:build -x test

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag, and push image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build \
            -f bootstrap/bootstrap-web-api/Dockerfile \
            -t $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:$IMAGE_TAG \
            -t $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:latest \
            .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:latest

      - name: Scan image
        run: |
          aws ecr start-image-scan \
            --repository-name $ECR_REPOSITORY_WEB_API \
            --image-id imageTag=${{ github.sha }} \
            --region $AWS_REGION

  # build-and-push-scheduler job (유사하게 작성)
```

**위치**: `.github/workflows/cd-deploy-ecs.yml`

```yaml
name: CD - Deploy to ECS

on:
  workflow_run:
    workflows: ["CD - Build and Push to ECR"]
    types:
      - completed
  workflow_dispatch:

env:
  AWS_REGION: ap-northeast-2
  ECS_CLUSTER: spring-cluster-dev
  ECS_SERVICE_WEB_API: spring-web-api-service
  ECS_SERVICE_SCHEDULER: spring-scheduler-service

permissions:
  id-token: write
  contents: read

jobs:
  deploy-web-api:
    name: Deploy Web API to ECS
    runs-on: ubuntu-latest
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Update Task Definition
        id: task-def
        run: |
          # 새 이미지 태그로 Task Definition 업데이트
          NEW_IMAGE="${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.${{ env.AWS_REGION }}.amazonaws.com/spring-web-api-dev:${{ github.sha }}"

          # Task Definition JSON 업데이트
          TASK_DEFINITION=$(cat .aws/task-definition-web-api.json | \
            jq --arg IMAGE "$NEW_IMAGE" '.containerDefinitions[0].image = $IMAGE')

          # 새 Task Definition 등록
          aws ecs register-task-definition \
            --cli-input-json "$TASK_DEFINITION" \
            --region ${{ env.AWS_REGION }}

          # 최신 Task Definition Revision 가져오기
          TASK_DEF_ARN=$(aws ecs describe-task-definition \
            --task-definition spring-web-api \
            --query 'taskDefinition.taskDefinitionArn' \
            --output text)

          echo "task-def-arn=$TASK_DEF_ARN" >> $GITHUB_OUTPUT

      - name: Deploy to ECS (Blue/Green)
        uses: aws-actions/amazon-ecs-deploy-task-definition@v1
        with:
          task-definition: ${{ steps.task-def.outputs.task-def-arn }}
          service: ${{ env.ECS_SERVICE_WEB_API }}
          cluster: ${{ env.ECS_CLUSTER }}
          wait-for-service-stability: true
          codedeploy-appspec: .aws/appspec-web-api.yaml
          codedeploy-application: spring-web-api-deploy
          codedeploy-deployment-group: spring-web-api-dg

      - name: Verify deployment
        run: |
          # ALB DNS 이름 가져오기
          ALB_DNS=$(aws elbv2 describe-load-balancers \
            --names spring-alb-dev \
            --query 'LoadBalancers[0].DNSName' \
            --output text)

          # Health Check
          for i in {1..30}; do
            STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://$ALB_DNS/actuator/health)
            if [ "$STATUS" = "200" ]; then
              echo "✅ Deployment successful! Health check passed."
              exit 0
            fi
            echo "Waiting for service to be healthy... ($i/30)"
            sleep 10
          done

          echo "❌ Deployment failed! Service not healthy after 5 minutes."
          exit 1

  # deploy-scheduler job (유사하게 작성, Rolling Update 사용)
```

### 3.4. 배포 전략

#### 3.4.1. Web API (Blue/Green 배포)

**장점**: 무중단 배포, 빠른 롤백

**AppSpec 파일**: `.aws/appspec-web-api.yaml`

```yaml
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "arn:aws:ecs:ap-northeast-2:123456789012:task-definition/spring-web-api:1"
        LoadBalancerInfo:
          ContainerName: "spring-web-api"
          ContainerPort: 8080
        PlatformVersion: "LATEST"
        NetworkConfiguration:
          AwsvpcConfiguration:
            Subnets:
              - "subnet-xxxxx"
              - "subnet-yyyyy"
            SecurityGroups:
              - "sg-zzzzz"
            AssignPublicIp: "DISABLED"

Hooks:
  - BeforeInstall: "LambdaFunctionToValidateBeforeInstall"
  - AfterInstall: "LambdaFunctionToValidateAfterInstall"
  - AfterAllowTestTraffic: "LambdaFunctionToValidateTestTraffic"
  - BeforeAllowTraffic: "LambdaFunctionToValidateBeforeAllowTraffic"
  - AfterAllowTraffic: "LambdaFunctionToValidateAfterAllowTraffic"
```

#### 3.4.2. Scheduler (Rolling Update)

**이유**: 백그라운드 작업, 다운타임 허용 가능, Blue/Green 불필요

**설정**: ECS Service에서 `deployment_type = "rolling"` 설정

---

## 4. Phase 3: 인프라 변경 관리

### 4.1. 목표

- 인프라 변경 시 체계적 관리
- 모든 변경은 Terraform 코드로 관리
- 변경 이력 추적 및 롤백 가능

### 4.2. 인프라 변경 시나리오

#### 시나리오 1: RDS 스펙 변경

```bash
# Infrastructure 프로젝트
cd /Users/sangwon-ryu/infrastructure

# Terraform 코드 수정
vim terraform/modules/rds/main.tf

# 예: instance_class 변경
resource "aws_db_instance" "main" {
  instance_class = "db.t3.large"  # db.t3.medium → db.t3.large
  # ...
}

# 변경 사항 확인
terraform plan -target=module.rds

# 적용
terraform apply -target=module.rds

# 결과: RDS 인스턴스가 새 스펙으로 교체됨 (다운타임 발생 가능)
# 권장: Multi-AZ를 사용하거나 유지보수 창 시간대에 적용
```

#### 시나리오 2: 새로운 S3 버킷 추가

```bash
# Infrastructure 프로젝트
/if:create s3 spring-logs dev \
  --versioning enabled \
  --encryption enabled

# Spring 프로젝트 - 환경 변수 추가
# .aws/task-definition-web-api.json 수정
{
  "environment": [
    {
      "name": "LOG_BUCKET",
      "value": "spring-logs-dev-xxxxxxxx"
    }
  ]
}

# GitHub Actions에서 자동으로 새 Task Definition 등록 및 배포
```

#### 시나리오 3: ECS Task 메모리 증설

```bash
# Spring 프로젝트
# .aws/task-definition-web-api.json 수정
{
  "cpu": "1024",     # 512 → 1024
  "memory": "2048"   # 1024 → 2048
}

# Git Push → GitHub Actions에서 자동으로 배포
git add .aws/task-definition-web-api.json
git commit -m "chore: Increase ECS task memory to 2GB"
git push origin main

# 자동 실행:
# 1. cd-deploy-ecs.yml 워크플로우 실행
# 2. 새 Task Definition 등록
# 3. ECS Service 업데이트 (Rolling Update)
# 4. 기존 Task 종료 → 새 Task 시작
```

### 4.3. 인프라 변경 체크리스트

**변경 전:**
- [ ] 변경 사항을 Terraform 코드로 작성
- [ ] `terraform plan`으로 변경 영향 확인
- [ ] 다운타임 예상 시 유지보수 창 공지
- [ ] 백업 및 스냅샷 생성 (RDS, S3 등)

**변경 중:**
- [ ] `terraform apply` 실행
- [ ] 변경 로그 기록 (Git commit message)
- [ ] 모니터링 (CloudWatch, ECS Logs)

**변경 후:**
- [ ] 애플리케이션 정상 동작 확인
- [ ] Health Check 통과 확인
- [ ] 롤백 계획 준비 (필요 시)

---

## 5. 시나리오별 워크플로우

### 5.1. 시나리오 1: 코드만 변경 (가장 빈번)

```
Developer → Feature 브랜치 생성
  ↓
코드 수정 (Domain, Application, Adapter, Bootstrap)
  ↓
Git Push → PR 생성
  ↓
ci-build-test.yml 실행
  - Code Quality: Checkstyle, SpotBugs, PMD ✅
  - Build: Gradle 빌드 ✅
  - Unit Tests: 도메인/애플리케이션 테스트 ✅
  - Architecture Tests: ArchUnit ✅
  - Integration Tests: Testcontainers ✅
  ↓
코드 리뷰 (팀원)
  ↓
PR 승인 → main 머지
  ↓
cd-build-push-ecr.yml 실행
  - Detect Changes: paths-filter ✅
  - Build Docker: web-api (변경됨) ✅
  - Push ECR: commit-sha, latest ✅
  - Scan: 취약점 스캔 ✅
  ↓
cd-deploy-ecs.yml 실행
  - Update Task Definition: 새 이미지 ✅
  - Deploy ECS Service: Blue/Green ✅
  - Health Check: 200 OK ✅
  ↓
✅ 배포 완료! (5-10분 소요)
```

### 5.2. 시나리오 2: Task Definition 설정 변경

```
Developer → .aws/task-definition-web-api.json 수정
  - CPU: 512 → 1024
  - Memory: 1024 → 2048
  - Environment: 새 환경 변수 추가
  ↓
Git Push → main
  ↓
cd-deploy-ecs.yml 실행
  - Update Task Definition: 새 설정 ✅
  - Deploy ECS Service: Rolling Update ✅
  - Health Check ✅
  ↓
✅ 설정 변경 완료! (3-5분 소요)
```

### 5.3. 시나리오 3: 외부 인프라 추가 (예: S3 버킷)

```
Developer → Infrastructure 프로젝트
  ↓
/if:create s3 spring-uploads dev
  ↓
Terraform Apply
  - S3 버킷 생성 ✅
  - SSM Parameter 등록: /s3/spring-uploads-dev/bucket-name ✅
  ↓
Spring 프로젝트 → application.yml 수정
  - cloud.aws.s3.bucket: ${S3_BUCKET_NAME}
  ↓
.aws/task-definition-web-api.json 수정
  - secrets:
      - name: S3_BUCKET_NAME
        valueFrom: /s3/spring-uploads-dev/bucket-name
  ↓
Git Push → main
  ↓
cd-deploy-ecs.yml 실행
  - 새 환경 변수로 배포 ✅
  ↓
✅ 인프라 추가 완료! (10-15분 소요)
```

### 5.4. 시나리오 4: 새로운 Bootstrap 모듈 추가

```
Developer → Spring 프로젝트
  ↓
1. bootstrap/bootstrap-batch 생성
2. Dockerfile 작성
3. .aws/task-definition-batch.json 작성
  ↓
Infrastructure 프로젝트
  ↓
4. /if:create ecr spring-batch dev
5. /if:create ecs-service spring-batch-service dev \
     --cluster spring-cluster-dev \
     --task-definition spring-batch:1 \
     --desired-count 1 \
     --deployment-type rolling
  ↓
Spring 프로젝트 → GitHub Actions 수정
  ↓
6. cd-build-push-ecr.yml에 build-and-push-batch job 추가
7. cd-deploy-ecs.yml에 deploy-batch job 추가
  ↓
Git Push → main
  ↓
8. 새 Bootstrap 자동 배포 ✅
  ↓
✅ 새 서비스 추가 완료! (20-30분 소요)
```

### 5.5. 시나리오 5: 롤백 (긴급 상황)

```
배포 후 문제 발견 (5xx 에러, 성능 저하 등)
  ↓
Option 1: ECS Service 롤백 (빠름, 1-2분)
  - AWS Console → ECS Service → Deployments → Rollback
  - 또는 CLI:
    aws ecs update-service \
      --cluster spring-cluster-dev \
      --service spring-web-api-service \
      --task-definition spring-web-api:123 \  # 이전 버전
      --force-new-deployment
  ↓
Option 2: Git Revert (안전, 5-10분)
  - git revert <commit-sha>
  - git push origin main
  - GitHub Actions에서 자동으로 이전 버전 배포
  ↓
Option 3: ECR 이미지 태그 변경 (중간, 3-5분)
  - .aws/task-definition-web-api.json 수정
  - image: ...ecr.../spring-web-api-dev:<previous-commit-sha>
  - git push origin main
  ↓
✅ 롤백 완료! 서비스 정상화
```

---

## 6. 버전 관리 전략

### 6.1. ECR 이미지 태그 전략

| 태그 | 용도 | 예시 | 설명 |
|------|------|------|------|
| `latest` | 개발 환경 | `spring-web-api-dev:latest` | 항상 최신 이미지 |
| `<commit-sha>` | 추적용 | `spring-web-api-dev:abc123def456` | Git commit SHA (7자리) |
| `v<version>` | 프로덕션 | `spring-web-api-prod:v1.2.3` | Semantic Versioning |
| `pr-<number>` | 테스트용 | `spring-web-api-dev:pr-123` | PR별 테스트 이미지 |
| `<branch>-<sha>` | 브랜치별 | `spring-web-api-dev:feature-auth-abc123` | 브랜치별 이미지 |

**권장 전략:**
- **dev 환경**: `latest` + `commit-sha`
- **staging 환경**: `commit-sha` (특정 버전 테스트)
- **prod 환경**: `v1.2.3` (Semantic Versioning)

### 6.2. ECS Task Definition 버전 관리

- Task Definition은 **Immutable** (변경 시 새 Revision 생성)
- `.aws/task-definition-*.json` 파일은 Git으로 관리
- 변경 이력:
  ```
  spring-web-api:1 → 초기 버전
  spring-web-api:2 → CPU/메모리 증설
  spring-web-api:3 → 환경 변수 추가
  spring-web-api:4 → 이미지 버전 업데이트
  ```

### 6.3. Terraform State 버전 관리

**S3 Backend 설정:**

```hcl
terraform {
  backend "s3" {
    bucket         = "terraform-state-spring-app"
    key            = "dev/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "terraform-locks"
  }
}
```

**장점:**
- 버전 관리 (S3 Versioning)
- 동시 실행 방지 (DynamoDB Lock)
- 팀 협업 가능
- 자동 백업

### 6.4. Git Branching 전략 (GitFlow)

```
main (프로덕션 배포)
  ↑
develop (개발 완료 코드)
  ↑
feature/* (기능 개발)
  - feature/user-auth
  - feature/order-payment

hotfix/* (긴급 수정)
  - hotfix/security-patch

release/* (릴리스 준비)
  - release/v1.2.0
```

**워크플로우:**
1. `feature/xxx` 브랜치 생성 → 개발
2. PR 생성 → `develop` 머지
3. `develop`에서 충분히 테스트
4. `release/vX.Y.Z` 브랜치 생성
5. 테스트 완료 후 `main` 머지 → 프로덕션 배포

---

## 7. 체크리스트 & 템플릿

### 7.1. 새 Bootstrap 모듈 추가 체크리스트

**Phase 1: 코드 작성 (Spring 프로젝트)**
- [ ] `bootstrap/bootstrap-xxx` 모듈 생성
- [ ] `build.gradle` 설정 (의존성, plugins)
- [ ] 메인 클래스 작성 (`@SpringBootApplication`)
- [ ] `application.yml` 설정
- [ ] Dockerfile 작성 (`bootstrap/bootstrap-xxx/Dockerfile`)
- [ ] 로컬 빌드 테스트 (`./gradlew :bootstrap:bootstrap-xxx:build`)
- [ ] 로컬 Docker 빌드 테스트 (`docker build -f bootstrap/bootstrap-xxx/Dockerfile .`)

**Phase 2: 인프라 구성 (Infrastructure 프로젝트)**
- [ ] ECR Repository 생성 (`/if:create ecr spring-xxx dev`)
- [ ] ALB Target Group 생성 (필요 시, `/if:create alb-target-group`)
- [ ] Task Definition 작성 (`.aws/task-definition-xxx.json`)
- [ ] ECS Service 생성 (`/if:create ecs-service spring-xxx-service dev`)
- [ ] CloudWatch Log Group 생성 (`/logs/ecs/spring-xxx`)

**Phase 3: CI/CD 설정 (Spring 프로젝트)**
- [ ] `cd-build-push-ecr.yml`에 `build-and-push-xxx` job 추가
- [ ] `cd-deploy-ecs.yml`에 `deploy-xxx` job 추가
- [ ] GitHub Secrets 확인 (AWS_ROLE_TO_ASSUME, AWS_ACCOUNT_ID 등)
- [ ] paths-filter에 새 모듈 경로 추가

**Phase 4: 테스트 및 배포**
- [ ] PR 생성 → CI 테스트 통과 확인
- [ ] main 머지 → CD 파이프라인 실행 확인
- [ ] ECR에 이미지 푸시 확인
- [ ] ECS Service 정상 실행 확인
- [ ] Health Check 통과 확인 (ALB Target Group)
- [ ] CloudWatch Logs 확인

### 7.2. Task Definition 템플릿

**파일**: `.aws/task-definition-template.json`

```json
{
  "family": "spring-{{MODULE_NAME}}",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::{{AWS_ACCOUNT_ID}}:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::{{AWS_ACCOUNT_ID}}:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "spring-{{MODULE_NAME}}",
      "image": "{{AWS_ACCOUNT_ID}}.dkr.ecr.{{AWS_REGION}}.amazonaws.com/spring-{{MODULE_NAME}}-{{ENV}}:latest",
      "portMappings": [
        {
          "containerPort": {{PORT}},
          "protocol": "tcp"
        }
      ],
      "essential": true,
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "{{ENV}}"
        },
        {
          "name": "TZ",
          "value": "Asia/Seoul"
        }
      ],
      "secrets": [
        {
          "name": "DB_ENDPOINT",
          "valueFrom": "/rds/spring-db-{{ENV}}/endpoint"
        },
        {
          "name": "DB_USERNAME",
          "valueFrom": "/rds/spring-db-{{ENV}}/username"
        },
        {
          "name": "DB_PASSWORD",
          "valueFrom": "/rds/spring-db-{{ENV}}/secret-arn:password::"
        },
        {
          "name": "REDIS_ENDPOINT",
          "valueFrom": "/elasticache/spring-cache-{{ENV}}/endpoint"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/spring-{{MODULE_NAME}}",
          "awslogs-region": "{{AWS_REGION}}",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:{{PORT}}/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

**사용 방법:**
```bash
# 변수 치환
sed -e 's/{{MODULE_NAME}}/web-api/g' \
    -e 's/{{AWS_ACCOUNT_ID}}/123456789012/g' \
    -e 's/{{AWS_REGION}}/ap-northeast-2/g' \
    -e 's/{{ENV}}/dev/g' \
    -e 's/{{PORT}}/8080/g' \
    .aws/task-definition-template.json > .aws/task-definition-web-api.json
```

### 7.3. GitHub Actions Job 템플릿

**Build & Push Job 템플릿:**

```yaml
build-and-push-{{MODULE_NAME}}:
  name: Build and Push {{MODULE_NAME}} to ECR
  runs-on: ubuntu-latest
  needs: detect-changes
  if: |
    needs.detect-changes.outputs.{{MODULE_NAME}} == 'true' ||
    needs.detect-changes.outputs.domain == 'true' ||
    needs.detect-changes.outputs.application == 'true'
  steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: 'gradle'

    - name: Build with Gradle
      run: ./gradlew :bootstrap:bootstrap-{{MODULE_NAME}}:build -x test

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
        aws-region: ${{ env.AWS_REGION }}

    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v2

    - name: Build, tag, and push image
      env:
        ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
        ECR_REPOSITORY: spring-{{MODULE_NAME}}-{{ENV}}
        IMAGE_TAG: ${{ github.sha }}
      run: |
        docker build \
          -f bootstrap/bootstrap-{{MODULE_NAME}}/Dockerfile \
          -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG \
          -t $ECR_REGISTRY/$ECR_REPOSITORY:latest \
          .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
```

**Deploy Job 템플릿:**

```yaml
deploy-{{MODULE_NAME}}:
  name: Deploy {{MODULE_NAME}} to ECS
  runs-on: ubuntu-latest
  needs: build-and-push-{{MODULE_NAME}}
  steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
        aws-region: ${{ env.AWS_REGION }}

    - name: Update Task Definition
      id: task-def
      run: |
        NEW_IMAGE="${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.${{ env.AWS_REGION }}.amazonaws.com/spring-{{MODULE_NAME}}-{{ENV}}:${{ github.sha }}"

        TASK_DEFINITION=$(cat .aws/task-definition-{{MODULE_NAME}}.json | \
          jq --arg IMAGE "$NEW_IMAGE" '.containerDefinitions[0].image = $IMAGE')

        aws ecs register-task-definition \
          --cli-input-json "$TASK_DEFINITION" \
          --region ${{ env.AWS_REGION }}

        TASK_DEF_ARN=$(aws ecs describe-task-definition \
          --task-definition spring-{{MODULE_NAME}} \
          --query 'taskDefinition.taskDefinitionArn' \
          --output text)

        echo "task-def-arn=$TASK_DEF_ARN" >> $GITHUB_OUTPUT

    - name: Deploy to ECS
      run: |
        aws ecs update-service \
          --cluster ${{ env.ECS_CLUSTER }} \
          --service spring-{{MODULE_NAME}}-service \
          --task-definition ${{ steps.task-def.outputs.task-def-arn }} \
          --force-new-deployment \
          --region ${{ env.AWS_REGION }}

        aws ecs wait services-stable \
          --cluster ${{ env.ECS_CLUSTER }} \
          --services spring-{{MODULE_NAME}}-service \
          --region ${{ env.AWS_REGION }}
```

---

## 8. 문제 해결 (Troubleshooting)

### 8.1. Docker 빌드 실패

**증상**: Gradle 빌드 중 의존성 다운로드 실패

**해결**:
```dockerfile
# Dockerfile에 Gradle 캐시 활용
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :bootstrap:bootstrap-web-api:build -x test
```

### 8.2. ECR 푸시 권한 오류

**증상**: `denied: User is not authorized to perform: ecr:PutImage`

**해결**:
1. IAM Role의 Permission Policy 확인
2. ECR Repository Policy 확인
3. GitHub Actions OIDC Trust Policy 확인

### 8.3. ECS Task 시작 실패

**증상**: Task가 PENDING 상태에서 멈춤

**원인 및 해결**:
1. **이미지를 찾을 수 없음**: ECR URL 확인
2. **IAM 권한 부족**: Task Execution Role 확인
3. **리소스 부족**: ECS Cluster에 충분한 CPU/메모리 있는지 확인
4. **네트워크 문제**: Security Group, Subnet 설정 확인

### 8.4. Health Check 실패

**증상**: ALB Target Group에서 Unhealthy

**해결**:
1. Health Check Path 확인 (`/actuator/health`)
2. Security Group에서 ALB → ECS 통신 허용 확인
3. Task가 실제로 8080 포트에서 실행 중인지 확인
4. CloudWatch Logs에서 애플리케이션 로그 확인

### 8.5. Blue/Green 배포 실패

**증상**: CodeDeploy에서 배포 실패

**해결**:
1. AppSpec 파일 문법 확인
2. Lambda 훅 함수 로그 확인
3. Task Definition이 올바르게 등록되었는지 확인
4. Target Group의 Health Check 설정 확인

---

## 9. 모니터링 및 로깅

### 9.1. CloudWatch Logs

**로그 그룹**:
- `/ecs/spring-web-api`: Web API 로그
- `/ecs/spring-scheduler`: Scheduler 로그
- `/aws/codedeploy/spring-web-api`: Blue/Green 배포 로그

**로그 보기**:
```bash
# 최근 로그 (1시간)
aws logs tail /ecs/spring-web-api --follow --since 1h

# 특정 Task 로그
aws logs filter-log-events \
  --log-group-name /ecs/spring-web-api \
  --log-stream-name-prefix ecs/spring-web-api/abc123
```

### 9.2. CloudWatch Metrics

**ECS 메트릭**:
- `CPUUtilization`: CPU 사용률
- `MemoryUtilization`: 메모리 사용률
- `DesiredTaskCount`: 목표 Task 수
- `RunningTaskCount`: 실행 중인 Task 수

**ALB 메트릭**:
- `TargetResponseTime`: 응답 시간
- `HealthyHostCount`: 정상 호스트 수
- `UnHealthyHostCount`: 비정상 호스트 수
- `RequestCount`: 요청 수

### 9.3. Alarms 설정

**권장 알람**:
1. **ECS CPU > 80%**: Task 스케일 아웃 필요
2. **ECS Memory > 80%**: 메모리 증설 필요
3. **ALB UnHealthyHostCount > 0**: 서비스 장애
4. **ALB TargetResponseTime > 2s**: 성능 저하

---

## 10. 다음 단계

### 10.1. 현재 완료된 것

- ✅ 전체 아키텍처 설계
- ✅ 초기 인프라 구축 순서 정의
- ✅ 지속적인 개발 파이프라인 설계
- ✅ 버전 관리 전략 수립
- ✅ 시나리오별 워크플로우 정의

### 10.2. 다음 실행할 것

**Option A: 초기 인프라 구축 시작**
```bash
cd /Users/sangwon-ryu/infrastructure
/if:create vpc spring-app dev
# ... (Phase 1 순서대로 실행)
```

**Option B: Spring 프로젝트 파일 생성**
- Dockerfile 작성 (web-api, scheduler)
- Task Definition 작성
- GitHub Actions 워크플로우 작성

**Option C: 문서 검토 및 수정**
- 이 문서 리뷰
- 필요한 부분 추가/수정

---

**✅ 이 문서는 전체 개발 파이프라인의 완전한 가이드입니다.**
**초기 구축부터 지속적인 개발, 인프라 관리까지 모든 시나리오를 다룹니다.**
