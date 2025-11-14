# Step-by-Step: ECR 생성 및 CI/CD 파이프라인 구축

이 가이드는 Infrastructure 프로젝트의 ECR 모듈을 사용하여 ECR Repository를 생성하고, Spring 프로젝트의 CI/CD 파이프라인을 구축하는 전체 과정을 설명합니다.

---

## 📋 전제 조건

- Infrastructure 프로젝트 위치: `/Users/sangwon-ryu/infrastructure`
- Spring 프로젝트 위치: `/Users/sangwon-ryu/claude-spring-standards`
- AWS 계정 및 IAM 권한 설정 완료
- Terraform 설치 완료
- Docker 설치 완료

---

## Step 1: ECR Repository 생성 (Infrastructure 프로젝트)

### 1.1. Infrastructure 프로젝트로 이동

```bash
cd /Users/sangwon-ryu/infrastructure
```

### 1.2. ECR Repository 생성 (web-api)

```bash
# docs/if-commands-guide.md의 /if:create ecr 명령어 사용
/if:create ecr spring-web-api dev
```

**실행 결과 예시:**
```
✅ ECR Repository 생성 완료!

Repository Name: spring-web-api-dev
Repository URI: 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/spring-web-api-dev
Region: ap-northeast-2

보안 설정:
- KMS 암호화: 활성화
- Image Scanning: Push 시 자동 실행
- Lifecycle Policy: 30개 태그 이미지 유지, 7일 후 미태그 이미지 삭제

SSM Parameter Store:
- /ecr/spring-web-api-dev/repository-url
- /ecr/spring-web-api-dev/repository-arn
```

### 1.3. ECR Repository 생성 (scheduler)

```bash
/if:create ecr spring-scheduler dev
```

**실행 결과 예시:**
```
✅ ECR Repository 생성 완료!

Repository Name: spring-scheduler-dev
Repository URI: 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/spring-scheduler-dev
Region: ap-northeast-2
```

### 1.4. ECR URL 확인 및 저장

생성된 ECR URL을 Spring 프로젝트에서 사용하기 위해 기록:

```bash
# web-api ECR URL
export ECR_WEB_API="123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/spring-web-api-dev"

# scheduler ECR URL
export ECR_SCHEDULER="123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/spring-scheduler-dev"
```

**GitHub Secrets에 등록할 값:**
- `AWS_ACCOUNT_ID`: 123456789012
- `AWS_REGION`: ap-northeast-2
- `ECR_REPOSITORY_WEB_API`: spring-web-api-dev
- `ECR_REPOSITORY_SCHEDULER`: spring-scheduler-dev

---

## Step 2: Dockerfile 작성 (Spring 프로젝트)

### 2.1. Spring 프로젝트로 이동

```bash
cd /Users/sangwon-ryu/claude-spring-standards
```

### 2.2. bootstrap-web-api Dockerfile 작성

파일 위치: `bootstrap/bootstrap-web-api/Dockerfile`

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle

# 빌드 스크립트 복사
COPY build.gradle settings.gradle ./

# 소스 코드 복사 (의존성 순서: domain → application → adapter → bootstrap)
COPY domain domain
COPY application application
COPY adapter-in adapter-in
COPY adapter-out adapter-out
COPY bootstrap/bootstrap-web-api bootstrap/bootstrap-web-api

# 빌드 실행 (테스트 제외 - CI에서 이미 실행됨)
RUN ./gradlew :bootstrap:bootstrap-web-api:build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안: non-root 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring

# JAR 파일 복사
COPY --from=builder /app/bootstrap/bootstrap-web-api/build/libs/*.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

# 사용자 전환
USER spring

# 포트 노출
EXPOSE 8080

# Health Check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 실행
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 2.3. bootstrap-scheduler Dockerfile 작성

파일 위치: `bootstrap/bootstrap-scheduler/Dockerfile`

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle

# 빌드 스크립트 복사
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY domain domain
COPY application application
COPY adapter-in adapter-in
COPY adapter-out adapter-out
COPY bootstrap/bootstrap-scheduler bootstrap/bootstrap-scheduler

# 빌드 실행
RUN ./gradlew :bootstrap:bootstrap-scheduler:build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안: non-root 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring

# JAR 파일 복사
COPY --from=builder /app/bootstrap/bootstrap-scheduler/build/libs/*.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

# 사용자 전환
USER spring

# 포트 노출 (스케줄러는 HTTP 엔드포인트가 없을 수 있음, 필요시 주석 제거)
# EXPOSE 8081

# Health Check (Actuator 사용 시)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#   CMD curl -f http://localhost:8081/actuator/health || exit 1

# 실행
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 2.4. 로컬 Docker 빌드 테스트

```bash
# web-api 빌드 테스트
docker build -f bootstrap/bootstrap-web-api/Dockerfile -t spring-web-api:local .

# scheduler 빌드 테스트
docker build -f bootstrap/bootstrap-scheduler/Dockerfile -t spring-scheduler:local .

# 빌드된 이미지 확인
docker images | grep spring
```

---

## Step 3: GitHub Actions 워크플로우 작성

### 3.1. 기존 워크플로우 분석

**기존 파일:**
- `.github/workflows/ci-build-test.yml`: PR 시 빌드/테스트
- `.github/workflows/ci-module-validation.yml`: 모듈 변경 감지

**전략:**
- 기존 CI는 그대로 유지 (PR 검증)
- 새로운 CD 워크플로우 추가 (main 브랜치 머지 후 ECR 푸시)

### 3.2. CD 워크플로우 생성

파일 위치: `.github/workflows/cd-build-push-ecr.yml`

```yaml
name: CD - Build and Push to ECR

on:
  push:
    branches:
      - main
  workflow_dispatch:  # 수동 실행 가능

# AWS 계정 정보
env:
  AWS_REGION: ap-northeast-2
  ECR_REPOSITORY_WEB_API: spring-web-api-dev
  ECR_REPOSITORY_SCHEDULER: spring-scheduler-dev

# GitHub OIDC를 사용한 AWS 인증 권한
permissions:
  id-token: write    # OIDC 토큰 발급
  contents: read     # 코드 체크아웃

jobs:
  # Job 1: 변경된 모듈 감지
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
            adapters:
              - 'adapter-out/**'

  # Job 2: Web API - Docker 빌드 및 ECR 푸시
  build-and-push-web-api:
    name: Build and Push Web API to ECR
    runs-on: ubuntu-latest
    needs: detect-changes
    # 조건: web-api 변경 OR domain/application 변경 (core 모듈 변경 시 모든 bootstrap 재배포)
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

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle (Skip tests - already done in CI)
        run: ./gradlew :bootstrap:bootstrap-web-api:build -x test

      - name: Configure AWS credentials (OIDC)
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag, and push image to Amazon ECR
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          # Docker 이미지 빌드
          docker build \
            -f bootstrap/bootstrap-web-api/Dockerfile \
            -t $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:$IMAGE_TAG \
            -t $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:latest \
            .

          # ECR에 푸시
          docker push $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:latest

          echo "✅ Web API Image pushed to ECR:"
          echo "   - $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:$IMAGE_TAG"
          echo "   - $ECR_REGISTRY/$ECR_REPOSITORY_WEB_API:latest"

      - name: Scan image for vulnerabilities
        run: |
          aws ecr start-image-scan \
            --repository-name $ECR_REPOSITORY_WEB_API \
            --image-id imageTag=${{ github.sha }} \
            --region $AWS_REGION

  # Job 3: Scheduler - Docker 빌드 및 ECR 푸시
  build-and-push-scheduler:
    name: Build and Push Scheduler to ECR
    runs-on: ubuntu-latest
    needs: detect-changes
    if: |
      needs.detect-changes.outputs.scheduler == 'true' ||
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

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew :bootstrap:bootstrap-scheduler:build -x test

      - name: Configure AWS credentials (OIDC)
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag, and push image to Amazon ECR
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build \
            -f bootstrap/bootstrap-scheduler/Dockerfile \
            -t $ECR_REGISTRY/$ECR_REPOSITORY_SCHEDULER:$IMAGE_TAG \
            -t $ECR_REGISTRY/$ECR_REPOSITORY_SCHEDULER:latest \
            .

          docker push $ECR_REGISTRY/$ECR_REPOSITORY_SCHEDULER:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY_SCHEDULER:latest

          echo "✅ Scheduler Image pushed to ECR:"
          echo "   - $ECR_REGISTRY/$ECR_REPOSITORY_SCHEDULER:$IMAGE_TAG"
          echo "   - $ECR_REGISTRY/$ECR_REPOSITORY_SCHEDULER:latest"

      - name: Scan image for vulnerabilities
        run: |
          aws ecr start-image-scan \
            --repository-name $ECR_REPOSITORY_SCHEDULER \
            --image-id imageTag=${{ github.sha }} \
            --region $AWS_REGION
```

---

## Step 4: AWS 인증 설정 (GitHub OIDC + IAM Role)

### 4.1. IAM OIDC Identity Provider 생성

**Infrastructure 프로젝트에서 실행:**

```bash
cd /Users/sangwon-ryu/infrastructure

# /if:create iam-oidc github-actions 명령어 사용 (if available)
# 또는 수동 생성:
```

**AWS Console에서 수동 생성 (대안):**

1. IAM → Identity providers → Add provider
2. Provider type: OpenID Connect
3. Provider URL: `https://token.actions.githubusercontent.com`
4. Audience: `sts.amazonaws.com`

### 4.2. IAM Role 생성 (GitHub Actions용)

**Trust Policy:**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::123456789012:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:your-org/claude-spring-standards:*"
        }
      }
    }
  ]
}
```

**Permission Policy:**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:DescribeRepositories",
        "ecr:ListImages",
        "ecr:DescribeImages",
        "ecr:StartImageScan"
      ],
      "Resource": "*"
    }
  ]
}
```

### 4.3. GitHub Secrets 등록

GitHub Repository → Settings → Secrets and variables → Actions

**필수 Secrets:**
- `AWS_ROLE_TO_ASSUME`: `arn:aws:iam::123456789012:role/GitHubActionsRole`

**선택 Secrets (환경별):**
- `AWS_ACCOUNT_ID`: 123456789012
- `AWS_REGION`: ap-northeast-2

---

## Step 5: 전체 파이프라인 테스트

### 5.1. 로컬 테스트

```bash
# 1. Docker 빌드 테스트 (이미 Step 2.4에서 완료)
docker build -f bootstrap/bootstrap-web-api/Dockerfile -t spring-web-api:local .

# 2. 로컬 실행 테스트
docker run -p 8080:8080 spring-web-api:local

# 3. Health Check
curl http://localhost:8080/actuator/health
```

### 5.2. GitHub Actions 테스트

```bash
# 1. 브랜치 생성 및 커밋
git checkout -b test/ecr-pipeline
git add .
git commit -m "feat: Add ECR CI/CD pipeline"

# 2. PR 생성 (기존 CI 실행 확인)
git push origin test/ecr-pipeline
gh pr create --title "Add ECR CI/CD Pipeline" --body "ECR 푸시 파이프라인 추가"

# 3. PR 머지 후 main 브랜치에서 CD 실행 확인
# GitHub Actions 탭에서 cd-build-push-ecr.yml 워크플로우 확인
```

### 5.3. ECR에 이미지 푸시 확인

```bash
# AWS CLI로 ECR 이미지 확인
aws ecr describe-images \
  --repository-name spring-web-api-dev \
  --region ap-northeast-2

# 출력 예시:
# {
#   "imageDetails": [
#     {
#       "imageDigest": "sha256:abc123...",
#       "imageTags": ["abc123def456", "latest"],
#       "imagePushedAt": "2025-01-15T10:30:00+00:00"
#     }
#   ]
# }
```

---

## 📊 전체 워크플로우 요약

```
Developer → PR 생성
    ↓
ci-build-test.yml (기존)
    - Code Quality
    - Build
    - Unit Tests
    - Architecture Tests
    - Integration Tests
    ↓
PR 승인 → main 머지
    ↓
cd-build-push-ecr.yml (신규)
    - Detect Changes (paths-filter)
    - Build Docker Image (web-api OR scheduler)
    - Push to ECR
    - Scan for Vulnerabilities
    ↓
ECR Repository
    - spring-web-api-dev:latest
    - spring-web-api-dev:<commit-sha>
    - spring-scheduler-dev:latest
    - spring-scheduler-dev:<commit-sha>
```

---

## 🚨 문제 해결 (Troubleshooting)

### 1. Docker 빌드 실패

**증상:** Gradle 빌드 중 의존성 다운로드 실패

**해결:**
```dockerfile
# Dockerfile에 Gradle 캐시 활용
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :bootstrap:bootstrap-web-api:build -x test
```

### 2. ECR 푸시 권한 오류

**증상:** `denied: User is not authorized to perform: ecr:PutImage`

**해결:** IAM Role의 Permission Policy에 ECR 권한 추가 (Step 4.2 참고)

### 3. GitHub Actions에서 AWS 인증 실패

**증상:** `Error: Could not assume role with OIDC`

**해결:**
1. GitHub Repository 주소가 IAM Role Trust Policy와 일치하는지 확인
2. OIDC Provider가 올바르게 생성되었는지 확인

### 4. 이미지 스캔 실패

**증상:** `ImageScanningConfiguration is not set`

**해결:** ECR Repository 생성 시 `scan_on_push = true` 설정 확인 (Infrastructure 모듈에서 자동 설정됨)

---

## 🎯 다음 단계 (ECS 배포)

이 가이드는 ECR까지의 CI/CD 파이프라인을 다룹니다. ECS 배포는 별도 가이드를 참고하세요:

1. **ECS Cluster 생성**: `/if:create ecs-cluster`
2. **ECS Task Definition 작성**: `.aws/task-definition-web-api.json`
3. **ECS Service 생성**: Blue/Green 배포 설정
4. **CD 워크플로우 확장**: ECR 푸시 → ECS 배포 추가

---

## 📚 참고 문서

- [Infrastructure ECR 모듈](../infrastructure/terraform/modules/ecr/README.md)
- [GitHub Actions - AWS OIDC](https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-amazon-web-services)
- [AWS ECR Best Practices](https://docs.aws.amazon.com/AmazonECR/latest/userguide/best-practices.html)
- [Docker Multi-Stage Builds](https://docs.docker.com/build/building/multi-stage/)
