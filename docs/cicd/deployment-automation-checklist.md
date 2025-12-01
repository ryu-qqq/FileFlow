# ECS 배포 자동화 체크리스트

> **목적**: ECR IMMUTABLE 태그 전략에서 Terraform image_tag 자동 업데이트 구현
> **대상**: GitHub Actions + Terraform + ECS 환경의 모든 프로젝트
> **소요 시간**: 약 30분

---

## 📋 목차

1. [현재 상태 진단](#1-현재-상태-진단)
2. [구현 전 준비사항](#2-구현-전-준비사항)
3. [단계별 구현 가이드](#3-단계별-구현-가이드)
4. [검증 방법](#4-검증-방법)
5. [트러블슈팅](#5-트러블슈팅)

---

## 1. 현재 상태 진단

### ✅ 체크리스트: 자동화가 필요한가?

다음 중 **하나라도 해당**되면 이 가이드를 따라 구현하세요:

- [ ] ECR `image_tag_mutability = "IMMUTABLE"` 설정
- [ ] 배포 시마다 `provider.tf`의 `image_tag` 변수를 **수동으로 수정**
- [ ] Git 커밋 히스토리에 `fix: Update image_tag to xxx` 같은 커밋 존재
- [ ] GitHub Actions에서 Docker 빌드는 성공하지만 ECS 배포는 이전 이미지 사용
- [ ] `terraform apply` 실행 시 `-var` 옵션 사용하지 않음

### ❌ 현재 문제점

```bash
# 현재 워크플로우 (반자동):
코드 수정 → PR 머지
  ↓
GitHub Actions 빌드 (web-api-93-a1b2c3d) ✅
  ↓
ECR 푸시 ✅
  ↓
ECS 배포... ❌ (여전히 web-api-92-f08d571 사용!)
  ↓
수동으로 provider.tf 수정 필요! ⚠️
  ↓
terraform apply 재실행
```

### ✅ 목표: 완전 자동화

```bash
# 목표 워크플로우 (완전 자동):
코드 수정 → PR 머지
  ↓
GitHub Actions 빌드 (web-api-93-a1b2c3d) ✅
  ↓
ECR 푸시 ✅
  ↓
Terraform 변수 자동 업데이트 ✅
  ↓
ECS 배포 (최신 이미지) ✅
```

---

## 2. 구현 전 준비사항

### 2.1 파일 위치 확인

```bash
# 확인 필요한 파일들:
.github/workflows/build-and-deploy.yml    # ✅ 존재해야 함
.github/workflows/terraform-apply.yml     # ✅ 존재해야 함
terraform/ecs-*/provider.tf               # ✅ 각 ECS 서비스별 존재
```

### 2.2 Reusable Workflow 출력 확인

**Infrastructure 레포지토리의 `reusable-build-docker.yml`이 다음을 출력하는지 확인:**

```yaml
outputs:
  image-uri:
    description: "Full ECR image URI with tag"
    value: ${{ jobs.build.outputs.image-uri }}
  image-tag:
    description: "Image tag only (e.g., web-api-92-f08d571)"
    value: ${{ jobs.build.outputs.image-tag }}
```

> ⚠️ **중요**: `image-tag` 출력이 없다면 Infrastructure 레포지토리를 먼저 수정해야 합니다.

### 2.3 ECS 서비스 목록 파악

```bash
# 프로젝트의 ECS 서비스 목록:
# 예: web-api, download-worker, scheduler

# 각 서비스의 Terraform 디렉토리:
ls terraform/ecs-*
```

---

## 3. 단계별 구현 가이드

### Step 1: build-and-deploy.yml 수정

**파일**: `.github/workflows/build-and-deploy.yml`

**수정 위치**: 배포 완료 알림(`notify`) job 이전에 추가

```yaml
  # ============================================================================
  # Terraform Image Tag 업데이트 (자동화)
  # ============================================================================
  update-terraform-tags:
    name: Update Terraform Image Tags
    needs: [build-web-api, build-download-worker, build-scheduler]  # 👈 프로젝트에 맞게 수정
    uses: ./.github/workflows/terraform-apply.yml
    with:
      web-api-image-tag: ${{ needs.build-web-api.outputs.image-tag }}
      download-worker-image-tag: ${{ needs.build-download-worker.outputs.image-tag }}
      scheduler-image-tag: ${{ needs.build-scheduler.outputs.image-tag }}
    secrets:
      AWS_ROLE_ARN: ${{ secrets.AWS_ROLE_ARN }}
```

**수정사항**:
1. `needs` 배열에 프로젝트의 build job 이름 나열
2. `with` 섹션에 각 서비스의 image-tag 전달
3. `notify` job의 `needs`에 `update-terraform-tags` 추가

### Step 2: terraform-apply.yml 수정

**파일**: `.github/workflows/terraform-apply.yml`

**2.1 workflow_call 트리거 추가:**

```yaml
on:
  workflow_dispatch:  # 수동 트리거 허용
  workflow_call:      # 👈 추가!
    inputs:
      web-api-image-tag:
        description: 'Web API Docker image tag'
        required: false
        type: string
      download-worker-image-tag:  # 👈 프로젝트에 맞게 추가
        description: 'Download Worker Docker image tag'
        required: false
        type: string
      # ... 다른 서비스도 동일하게 추가
    secrets:
      AWS_ROLE_ARN:
        required: true
```

**2.2 Terraform Apply 단계 수정:**

기존:
```yaml
- name: Terraform Apply
  run: |
    terraform plan -out=tfplan
    terraform apply -auto-approve tfplan
```

수정 후:
```yaml
- name: Terraform Apply - ${{ matrix.module.name }}
  working-directory: ${{ matrix.module.dir }}
  run: |
    echo "🚀 Applying Terraform for ${{ matrix.module.name }}..."

    # Image Tag 변수 설정
    EXTRA_VARS=""

    if [ "${{ matrix.module.name }}" == "ecs-web-api" ] && [ -n "${{ inputs.web-api-image-tag }}" ]; then
      echo "📦 Using Web API image tag: ${{ inputs.web-api-image-tag }}"
      EXTRA_VARS="-var=image_tag=${{ inputs.web-api-image-tag }}"
    elif [ "${{ matrix.module.name }}" == "ecs-download-worker" ] && [ -n "${{ inputs.download-worker-image-tag }}" ]; then
      echo "📦 Using Download Worker image tag: ${{ inputs.download-worker-image-tag }}"
      EXTRA_VARS="-var=image_tag=${{ inputs.download-worker-image-tag }}"
    # ... 다른 서비스도 동일하게 추가
    else
      echo "ℹ️  No image tag provided, using default from provider.tf"
    fi

    # Terraform Plan with optional image_tag
    terraform plan $EXTRA_VARS -out=tfplan
    terraform apply -auto-approve -no-color tfplan
```

### Step 3: provider.tf 수정 (각 ECS 서비스별)

**파일들**: `terraform/ecs-*/provider.tf`

**수정 전**:
```hcl
variable "image_tag" {
  description = "Docker image tag to deploy (CI/CD sets this value)"
  type        = string
  default     = "web-api-92-f08d571"  # 👈 하드코딩!
}
```

**수정 후**:
```hcl
variable "image_tag" {
  description = "Docker image tag to deploy. Auto-set by GitHub Actions build-and-deploy.yml. Format: {component}-{build-number}-{git-sha}"
  type        = string
  default     = "web-api-92-f08d571"  # Fallback only - GitHub Actions will override this

  validation {
    condition     = can(regex("^web-api-[0-9]+-[a-f0-9]+$", var.image_tag))
    error_message = "Image tag must follow format: web-api-{build-number}-{git-sha}"
  }
}
```

> ⚠️ **주의**: `validation` 블록의 정규식은 서비스별로 수정 (web-api, download-worker, scheduler 등)

---

## 4. 검증 방법

### 4.1 로컬 Syntax 검증

```bash
# GitHub Actions Workflow 문법 검증
cd .github/workflows
yamllint build-and-deploy.yml terraform-apply.yml

# Terraform 문법 검증
cd terraform/ecs-web-api
terraform init
terraform validate
```

### 4.2 Dry-Run 테스트

```bash
# Terraform Plan으로 변수 전달 테스트
cd terraform/ecs-web-api
terraform plan -var="image_tag=web-api-99-test123"

# 예상 출력:
# ~ image = "xxx.dkr.ecr.xxx.amazonaws.com/fileflow-web-api-prod:web-api-92-f08d571" -> "xxx:web-api-99-test123"
```

### 4.3 실제 배포 테스트

1. **작은 변경으로 테스트**:
   ```bash
   # README.md 같은 무해한 파일 수정
   git checkout -b test/deployment-automation
   echo "# Test" >> README.md
   git commit -m "test: 배포 자동화 테스트"
   git push origin test/deployment-automation
   ```

2. **PR 생성 → 머지**

3. **GitHub Actions 로그 확인**:
   ```
   ✅ Build web-api (image-tag: web-api-100-abc1234)
   ✅ Build download-worker (image-tag: download-worker-100-abc1234)
   ✅ Deploy web-api
   ✅ Update Terraform Tags
       📦 Using Web API image tag: web-api-100-abc1234
       📦 Using Download Worker image tag: download-worker-100-abc1234
   ✅ Terraform Apply completed
   ```

4. **ECS 콘솔 확인**:
   - ECS → Clusters → Services → Task Definition
   - 컨테이너 이미지 태그가 최신 버전인지 확인

---

## 5. 트러블슈팅

### 문제 1: `needs.build-web-api.outputs.image-tag` 값이 비어있음

**증상**:
```
ℹ️  No image tag provided for ecs-web-api, using default from provider.tf
```

**원인**: Reusable workflow가 `image-tag`를 출력하지 않음

**해결**:
1. Infrastructure 레포지토리의 `reusable-build-docker.yml` 확인
2. 다음과 같이 출력 추가:
   ```yaml
   outputs:
     image-tag:
       description: "Image tag only"
       value: ${{ jobs.build.outputs.image-tag }}
   ```
3. Build job에서 태그 추출:
   ```yaml
   - name: Extract image tag
     id: tag
     run: |
       TAG=$(echo "$IMAGE_URI" | awk -F: '{print $2}')
       echo "image-tag=$TAG" >> $GITHUB_OUTPUT
   ```

### 문제 2: Terraform validation 실패

**증상**:
```
Error: Invalid value for variable "image_tag"
```

**원인**: 이미지 태그 형식이 validation 규칙과 맞지 않음

**해결**:
1. provider.tf의 정규식 확인:
   ```hcl
   can(regex("^web-api-[0-9]+-[a-f0-9]+$", var.image_tag))
   ```
2. 실제 태그 형식 확인: `web-api-92-f08d571`
3. 정규식을 태그 형식에 맞게 수정

### 문제 3: `terraform plan $EXTRA_VARS` 실패

**증상**:
```
Error: Invalid command-line option
```

**원인**: `EXTRA_VARS`가 비어있을 때 bash가 잘못 해석

**해결**:
```bash
# 수정 전:
terraform plan $EXTRA_VARS -out=tfplan

# 수정 후:
if [ -n "$EXTRA_VARS" ]; then
  terraform plan $EXTRA_VARS -out=tfplan
else
  terraform plan -out=tfplan
fi
```

### 문제 4: Matrix strategy에서 조건문 실패

**증상**:
```
elif: command not found
```

**원인**: GitHub Actions의 YAML 멀티라인 문자열 처리 문제

**해결**:
```yaml
run: |
  # 👈 반드시 | 사용 (멀티라인)
  if [ ... ]; then
    ...
  elif [ ... ]; then  # 👈 같은 들여쓰기
    ...
  fi
```

---

## 6. 다른 프로젝트 적용 가이드

### 6.1 체크리스트

- [ ] 1단계: `build-and-deploy.yml`에 `update-terraform-tags` job 추가
- [ ] 2단계: `terraform-apply.yml`에 `workflow_call` 트리거 추가
- [ ] 3단계: `terraform-apply.yml` Apply 단계에 조건부 `-var` 전달 로직 추가
- [ ] 4단계: 각 `provider.tf`에 validation 추가
- [ ] 5단계: Dry-run 테스트 (terraform plan -var)
- [ ] 6단계: 실제 배포 테스트 (작은 변경)
- [ ] 7단계: ECS 콘솔에서 최신 이미지 확인

### 6.2 프로젝트별 커스터마이징

| 항목 | 수정 위치 | 예시 |
|------|----------|------|
| **서비스 목록** | `build-and-deploy.yml`의 `needs` | `[build-api, build-worker]` |
| **Image tag 입력** | `terraform-apply.yml`의 `inputs` | `api-image-tag`, `worker-image-tag` |
| **조건문** | `terraform-apply.yml`의 `if` | `ecs-api`, `ecs-worker` |
| **Validation 정규식** | `provider.tf`의 `validation` | `^api-[0-9]+-[a-f0-9]+$` |

### 6.3 시간 절약 팁

1. **FileFlow 프로젝트 코드 복사**:
   - 이 프로젝트의 수정사항을 템플릿으로 사용
   - 서비스 이름만 치환 (web-api → your-service)

2. **검증 자동화**:
   ```bash
   # Terraform validation 스크립트
   for dir in terraform/ecs-*; do
     cd $dir
     terraform validate || echo "❌ Validation failed: $dir"
     cd -
   done
   ```

3. **롤백 계획**:
   - 변경 전 현재 코드를 별도 브랜치에 백업
   - 문제 발생 시 빠르게 원복 가능

---

## 7. 참고 자료

### 7.1 관련 문서

- [ECR IMMUTABLE 태그 전략](./step-by-step-ecr-setup.md)
- [GitHub Actions Reusable Workflows](https://docs.github.com/en/actions/using-workflows/reusing-workflows)
- [Terraform Variables](https://developer.hashicorp.com/terraform/language/values/variables)

### 7.2 예제 프로젝트

- **FileFlow**: 본 가이드의 기준 프로젝트
  - 3개 ECS 서비스 (web-api, download-worker, scheduler)
  - GitHub Actions + Terraform + ECS
  - ECR IMMUTABLE 태그 전략

---

## 📝 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2025-12-01 | 1.0.0 | 초기 작성 (FileFlow 프로젝트 기준) |

---

## 💬 질문 & 피드백

문제가 발생하거나 개선 사항이 있다면:
1. GitHub Issue 생성
2. 프로젝트 팀 Slack 채널에 문의
3. DevOps 팀에 문의
