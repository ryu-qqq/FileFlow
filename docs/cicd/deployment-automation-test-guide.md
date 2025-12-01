# 배포 자동화 테스트 가이드

> **목적**: 배포 자동화 구현 후 안전하게 검증하는 방법
> **대상**: 배포 자동화 체크리스트를 완료한 프로젝트
> **소요 시간**: 약 15분

---

## 📋 목차

1. [사전 검증 (Local)](#1-사전-검증-local)
2. [Dry-Run 테스트](#2-dry-run-테스트)
3. [실제 배포 테스트](#3-실제-배포-테스트)
4. [모니터링 & 검증](#4-모니터링--검증)
5. [롤백 절차](#5-롤백-절차)

---

## 1. 사전 검증 (Local)

### 1.1 Workflow 문법 검증

```bash
# GitHub Actions Workflow 문법 검증
cd .github/workflows

# yamllint 설치 (필요시)
brew install yamllint

# YAML 문법 검증
yamllint build-and-deploy.yml
yamllint terraform-apply.yml

# 예상 출력: 에러 없음
```

### 1.2 Terraform 변수 검증

```bash
# 각 ECS 서비스 디렉토리에서 검증
cd terraform/ecs-web-api

# Terraform 초기화
terraform init

# 변수 validation 테스트
terraform validate

# Image tag 변수 테스트
terraform plan -var="image_tag=web-api-999-test123"

# 예상 출력:
# ✅ Plan: X to add, Y to change, Z to destroy.
#    (image 변경이 감지되어야 함)
```

**검증 포인트**:
- ✅ `terraform validate` 성공
- ✅ `terraform plan -var` 실행 가능
- ✅ Image tag 변경이 Plan에 표시됨

### 1.3 정규식 Validation 테스트

```bash
# provider.tf의 validation 규칙 테스트
cd terraform/ecs-web-api

# ✅ 올바른 형식 (성공해야 함)
terraform plan -var="image_tag=web-api-100-abc1234"

# ❌ 잘못된 형식 (실패해야 함)
terraform plan -var="image_tag=wrong-format"
terraform plan -var="image_tag=web-api-abc-123"  # 숫자 빠짐
terraform plan -var="image_tag=web-api-100"      # git-sha 빠짐

# 예상 출력:
# Error: Invalid value for variable "image_tag"
# Image tag must follow format: web-api-{build-number}-{git-sha}
```

---

## 2. Dry-Run 테스트

### 2.1 GitHub Actions Workflow 로컬 실행

```bash
# act 도구 설치 (GitHub Actions 로컬 실행)
brew install act

# build-and-deploy.yml Dry-run
cd /path/to/project
act -W .github/workflows/build-and-deploy.yml -n

# -n: Dry-run 모드 (실제 실행 안 함)
```

**검증 포인트**:
- ✅ Workflow 문법 에러 없음
- ✅ Job 의존성 (`needs`) 올바름
- ✅ Secret 누락 확인

### 2.2 Terraform Plan with Custom Variables

```bash
# 시뮬레이션: GitHub Actions에서 전달하는 변수
export WEB_API_TAG="web-api-100-test123"
export WORKER_TAG="download-worker-100-test123"
export SCHEDULER_TAG="scheduler-100-test123"

# Web API
cd terraform/ecs-web-api
terraform plan -var="image_tag=$WEB_API_TAG" -out=tfplan

# Download Worker
cd ../ecs-download-worker
terraform plan -var="image_tag=$WORKER_TAG" -out=tfplan

# Scheduler
cd ../ecs-scheduler
terraform plan -var="image_tag=$SCHEDULER_TAG" -out=tfplan
```

**검증 포인트**:
- ✅ 모든 서비스에서 Plan 성공
- ✅ Image tag 변경이 감지됨
- ✅ 다른 리소스는 변경 없음 (image만 변경)

예상 출력:
```
Terraform will perform the following actions:

  # module.ecs_service.aws_ecs_task_definition.main will be updated in-place
  ~ resource "aws_ecs_task_definition" "main" {
      ~ container_definitions = jsonencode(
          ~ [
              ~ {
                  ~ image = "646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow-web-api-prod:web-api-92-f08d571"
                           -> "646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow-web-api-prod:web-api-100-test123"
                },
            ]
        )
    }

Plan: 0 to add, 1 to change, 0 to destroy.
```

---

## 3. 실제 배포 테스트

### 3.1 안전한 테스트 시나리오

**목표**: 비즈니스 로직 변경 없이 배포 자동화만 테스트

```bash
# 1. 테스트 브랜치 생성
git checkout main
git pull origin main
git checkout -b test/deployment-automation-$(date +%Y%m%d)

# 2. 무해한 변경 (README 또는 주석)
echo "# Deployment Automation Test - $(date)" >> README.md

# 또는 코드 주석 추가
vim bootstrap/bootstrap-web-api/src/main/resources/application.yml
# 예: "# Deployment test comment"

# 3. 커밋 & 푸시
git add .
git commit -m "test: 배포 자동화 검증 - image tag 자동 업데이트"
git push origin test/deployment-automation-$(date +%Y%m%d)
```

### 3.2 PR 생성 & 머지

1. **GitHub PR 생성**:
   - 제목: `test: 배포 자동화 검증`
   - 본문: 체크리스트 포함
     ```markdown
     ## 테스트 목적
     - [ ] Docker 빌드 성공
     - [ ] ECR 푸시 성공
     - [ ] Terraform image_tag 자동 업데이트
     - [ ] ECS 배포 성공
     - [ ] 최신 이미지 태그 확인
     ```

2. **PR 검토**:
   - Terraform Plan 결과 확인
   - Image tag 변경 확인

3. **PR 머지**:
   - `Squash and merge` 권장
   - 머지 후 `main` 브랜치로 자동 배포 시작

### 3.3 GitHub Actions 로그 모니터링

**모니터링 체크리스트**:

```bash
# GitHub Actions 페이지에서 실시간 모니터링
# https://github.com/{owner}/{repo}/actions

✅ 1. Test Job
   - 테스트 실행
   - 모든 테스트 통과

✅ 2. Build Jobs (병렬)
   - build-web-api
   - build-download-worker
   - build-scheduler

   각 빌드에서 확인:
   - Docker 빌드 성공
   - ECR 푸시 성공
   - image-tag 출력: web-api-101-abc1234

✅ 3. Deploy Jobs (병렬)
   - deploy-web-api
   - deploy-download-worker
   - deploy-scheduler

   각 배포에서 확인:
   - ECS 서비스 업데이트 성공

✅ 4. Update Terraform Tags (새로 추가된 Job!)
   📦 Using Web API image tag: web-api-101-abc1234
   📦 Using Download Worker image tag: download-worker-101-abc1234
   📦 Using Scheduler image tag: scheduler-101-abc1234

   각 ECS 모듈에서:
   - terraform plan 성공
   - terraform apply 성공
   - Image tag 업데이트 확인

✅ 5. Notify
   - Slack 알림 (성공)
   - GitHub Summary 생성
```

---

## 4. 모니터링 & 검증

### 4.1 ECS 콘솔 검증

```bash
# AWS CLI로 Task Definition 확인
aws ecs describe-task-definition \
  --task-definition fileflow-web-api-prod \
  --region ap-northeast-2 \
  --query 'taskDefinition.containerDefinitions[0].image' \
  --output text

# 예상 출력:
# 646886795421.dkr.ecr.ap-northeast-2.amazonaws.com/fileflow-web-api-prod:web-api-101-abc1234
#                                                                            ^^^^^^^^^^^^^^^^^^^
#                                                                            👈 최신 태그 확인!
```

**ECS 콘솔 확인**:
1. AWS Console → ECS → Clusters → `fileflow-cluster-prod`
2. Services → `fileflow-web-api-prod` 클릭
3. Tasks 탭 → 실행 중인 Task 클릭
4. Configuration 섹션에서 Image 확인

**검증 포인트**:
- ✅ Task Definition 버전이 증가함 (예: `:34` → `:35`)
- ✅ Image tag가 최신 버전 (GitHub Actions 로그와 일치)
- ✅ 모든 Task가 `RUNNING` 상태
- ✅ Health Check 통과

### 4.2 CloudWatch Logs 검증

```bash
# 애플리케이션 로그 확인
aws logs tail /aws/ecs/fileflow-web-api-prod/application \
  --follow \
  --region ap-northeast-2

# 예상 로그:
# [INFO] Starting application with Spring Boot 3.x
# [INFO] Connected to database
# [INFO] Application started successfully
```

**검증 포인트**:
- ✅ 애플리케이션 정상 시작
- ✅ 에러 로그 없음
- ✅ Health Check 엔드포인트 응답

### 4.3 ALB Health Check 검증

```bash
# ALB Target Group Health Check
aws elbv2 describe-target-health \
  --target-group-arn $(aws elbv2 describe-target-groups \
    --names fileflow-web-api-tg-prod \
    --query 'TargetGroups[0].TargetGroupArn' \
    --output text) \
  --region ap-northeast-2

# 예상 출력:
# "TargetHealth": {
#   "State": "healthy"
# }
```

### 4.4 API 엔드포인트 테스트

```bash
# Health Check 엔드포인트 테스트
curl https://files.set-of.com/actuator/health

# 예상 응답:
# {"status":"UP"}

# 실제 API 테스트 (예: 파일 목록 조회)
curl -X GET https://files.set-of.com/api/v1/files \
  -H "Authorization: Bearer $TOKEN"

# 정상 응답 확인
```

---

## 5. 롤백 절차

### 5.1 즉시 롤백 (긴급)

**방법 1: 이전 Task Definition으로 복원**

```bash
# 1. 현재 서비스 확인
aws ecs describe-services \
  --cluster fileflow-cluster-prod \
  --services fileflow-web-api-prod \
  --query 'services[0].taskDefinition' \
  --output text

# 출력: arn:aws:ecs:ap-northeast-2:xxx:task-definition/fileflow-web-api-prod:35

# 2. 이전 버전으로 롤백 (예: :34)
aws ecs update-service \
  --cluster fileflow-cluster-prod \
  --service fileflow-web-api-prod \
  --task-definition fileflow-web-api-prod:34 \
  --region ap-northeast-2

# 3. 롤백 진행 확인
aws ecs describe-services \
  --cluster fileflow-cluster-prod \
  --services fileflow-web-api-prod \
  --query 'services[0].deployments' \
  --region ap-northeast-2
```

**방법 2: Terraform으로 롤백**

```bash
# 1. 이전 image_tag로 Terraform 실행
cd terraform/ecs-web-api
terraform plan -var="image_tag=web-api-92-f08d571" -out=tfplan
terraform apply tfplan

# 2. ECS 서비스 강제 재배포
aws ecs update-service \
  --cluster fileflow-cluster-prod \
  --service fileflow-web-api-prod \
  --force-new-deployment \
  --region ap-northeast-2
```

### 5.2 Git 롤백

```bash
# 1. 문제가 된 머지 커밋 찾기
git log --oneline -n 10

# 2. Revert 커밋 생성
git revert <merge-commit-hash>

# 3. 푸시
git push origin main

# GitHub Actions가 자동으로 이전 상태로 배포
```

### 5.3 Circuit Breaker 동작 확인

```bash
# ECS Deployment Circuit Breaker 설정 확인
aws ecs describe-services \
  --cluster fileflow-cluster-prod \
  --services fileflow-web-api-prod \
  --query 'services[0].deploymentConfiguration.deploymentCircuitBreaker' \
  --region ap-northeast-2

# 출력:
# {
#   "enable": true,
#   "rollback": true
# }
```

**자동 롤백 조건**:
- Health Check 연속 실패
- Task 시작 실패
- 배포 타임아웃

---

## 6. 체크리스트 템플릿

### 배포 전 체크리스트

```markdown
## 배포 전 검증

- [ ] Workflow YAML 문법 검증 완료
- [ ] Terraform validate 성공 (모든 ECS 서비스)
- [ ] Image tag validation 규칙 테스트 완료
- [ ] Dry-run terraform plan 성공
- [ ] 테스트 브랜치 준비 (무해한 변경)

## 배포 중 모니터링

- [ ] GitHub Actions: Build Jobs 성공
- [ ] GitHub Actions: Deploy Jobs 성공
- [ ] GitHub Actions: Update Terraform Tags 성공
- [ ] Image tag가 자동으로 전달되는지 로그 확인

## 배포 후 검증

- [ ] ECS Task Definition 최신 이미지 태그 확인
- [ ] ECS Tasks 모두 RUNNING 상태
- [ ] CloudWatch Logs 에러 없음
- [ ] ALB Health Check 통과
- [ ] API 엔드포인트 정상 응답
- [ ] Terraform State 최신 상태 확인

## 롤백 준비

- [ ] 이전 Task Definition 버전 기록
- [ ] 이전 이미지 태그 기록
- [ ] 롤백 스크립트 준비
- [ ] 모니터링 알람 설정
```

---

## 7. 자주 발생하는 문제

### 문제 1: Image Tag가 업데이트되지 않음

**증상**: GitHub Actions는 성공하지만 ECS는 여전히 이전 이미지 사용

**확인**:
```bash
# GitHub Actions 로그에서 image-tag 출력 확인
# "📦 Using Web API image tag: ..." 메시지 있는지 확인

# Terraform Apply 로그에서 변수 전달 확인
# terraform plan -var=image_tag=xxx 실행되는지 확인
```

**해결**: [deployment-automation-checklist.md](./deployment-automation-checklist.md#5-트러블슈팅) 참고

### 문제 2: ECS 배포는 성공했지만 Terraform은 실패

**증상**: Deploy Jobs는 성공, Update Terraform Tags는 실패

**확인**:
```bash
# Terraform 로그에서 에러 확인
# validation 규칙 위반인지 확인
```

**해결**:
1. provider.tf의 validation 규칙 확인
2. 실제 이미지 태그 형식 확인
3. 정규식 수정 또는 태그 형식 통일

### 문제 3: 배포 후 Health Check 실패

**증상**: 새 Task가 시작되지만 계속 재시작됨

**확인**:
```bash
# Task 로그 확인
aws logs tail /aws/ecs/fileflow-web-api-prod/application --follow

# Health Check 설정 확인
aws ecs describe-services \
  --cluster fileflow-cluster-prod \
  --services fileflow-web-api-prod \
  --query 'services[0].healthCheckGracePeriodSeconds'
```

**해결**:
1. 애플리케이션 에러 로그 확인
2. Health Check Grace Period 증가 (Terraform)
3. 이미지 빌드 문제 확인

---

## 8. 참고 자료

- [배포 자동화 체크리스트](./deployment-automation-checklist.md)
- [AWS ECS Rolling Update](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/deployment-type-ecs.html)
- [Terraform Plan & Apply](https://developer.hashicorp.com/terraform/cli/commands/plan)
- [GitHub Actions Monitoring](https://docs.github.com/en/actions/monitoring-and-troubleshooting-workflows)

---

## 📝 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2025-12-01 | 1.0.0 | 초기 작성 |
