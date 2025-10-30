---
description: 완전한 Git 워크플로우 (Branch → Commit → PR)
---

# Git Complete Workflow

**🎯 역할**: Feature 개발부터 PR 생성까지 완전한 Git 워크플로우

**📋 전략**: Git Flow + Conventional Commits

---

## 🌳 Branch 전략

### Main Branches

- **`main`** - 프로덕션 코드 (안정)
- **`develop`** - 개발 통합 브랜치

### Supporting Branches

- **`feature/*`** - 기능 개발
- **`hotfix/*`** - 긴급 수정
- **`release/*`** - 릴리스 준비

### Branch Naming

```bash
# Feature
feature/PROJ-123-order-aggregate
feature/payment-integration

# Hotfix
hotfix/PROJ-456-critical-bug
hotfix/payment-null-pointer

# Release
release/v1.0.0
release/v2.1.0
```

---

## 📝 Commit Message Format

### Conventional Commits

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type

- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 변경
- `chore`: 빌드, 설정 변경

### Scope (Layer)

- `domain`: Domain layer
- `application`: Application layer (UseCase)
- `adapter-rest`: REST API Adapter
- `adapter-persistence`: Persistence Adapter

---

## 🚀 완전한 Feature 개발 워크플로우

### 1️⃣ 브랜치 생성

```bash
# develop에서 최신 코드 가져오기
git checkout develop
git pull origin develop

# Feature 브랜치 생성
git checkout -b feature/PROJ-123-order-aggregate
```

### 2️⃣ 개발 및 Commit (Layer별)

#### Domain Layer 작업

```bash
# Domain layer 변경
vim domain/src/.../OrderDomain.java

# Stage (Domain만)
git add domain/src/

# Commit (Conventional Commits)
git commit -m "feat(domain): Order Aggregate 구현

- Aggregate Root 추가
- Law of Demeter 준수
- Long FK 전략 적용

Closes #123"
```

#### Application Layer 작업

```bash
# Application layer 변경
vim application/src/.../CreateOrderUseCase.java

# Stage (Application만)
git add application/src/

# Commit
git commit -m "feat(application): CreateOrder UseCase 구현

- Transaction 경계 관리
- Port 인터페이스 사용
- Command/Response 패턴

Relates to #123"
```

#### REST API Layer 작업

```bash
# REST API 변경
vim adapter-in/rest-api/src/.../OrderController.java

# Stage
git add adapter-in/rest-api/src/

# Commit
git commit -m "feat(adapter-rest): Order REST API 구현

- POST /api/orders 엔드포인트
- Request/Response DTO
- GlobalExceptionHandler 통합

Closes #123"
```

### 3️⃣ Pre-commit Hook 자동 검증

```bash
# 자동으로 실행됨 (hooks/pre-commit)
✅ Transaction boundary check
✅ Spring proxy constraint check
✅ Spotless format check (optional)

# 위반 시 Commit 차단
❌ Pre-commit hook failed
   - Transaction 경계 위반 발견: OrderService.java:42

Please fix and try again.
```

### 4️⃣ Push to Remote

```bash
# 첫 Push (upstream 설정)
git push -u origin feature/PROJ-123-order-aggregate

# 이후 Push
git push
```

### 5️⃣ PR 생성 (GitHub CLI)

```bash
# PR 자동 생성 (Claude Code에서)
/git-pr

# 또는 수동 실행
gh pr create \
  --base develop \
  --title "feat: Order Aggregate 구현" \
  --body "$(cat .github/pull_request_template.md)" \
  --label "layer:domain,type:feature"
```

**PR 생성 후 자동 실행**:
- ✅ PR Gate Pipeline (`tools/pipeline/pr_gate.sh`)
  - Code Format Check
  - Convention Validation
  - Unit Tests
  - Architecture Validation
  - Test Coverage

### 6️⃣ Merge 및 정리

```bash
# PR Merge 후
git checkout develop
git pull origin develop

# Feature 브랜치 삭제
git branch -d feature/PROJ-123-order-aggregate
git push origin --delete feature/PROJ-123-order-aggregate
```

---

## 🚨 Hotfix Workflow

### 1. Hotfix 브랜치 생성 (main에서)

```bash
git checkout main
git pull origin main
git checkout -b hotfix/PROJ-456-critical-bug
```

### 2. 수정 및 테스트

```bash
# 버그 수정
vim src/.../PaymentService.java

# 테스트
./gradlew test

# Commit
git commit -m "fix: Critical payment bug 수정

NPE 방어 로직 추가

Fixes #456"
```

### 3. Merge (main + develop)

```bash
# main에 merge
git checkout main
git merge hotfix/PROJ-456-critical-bug
git tag v1.0.1
git push origin main --tags

# develop에도 merge
git checkout develop
git merge hotfix/PROJ-456-critical-bug
git push origin develop

# Hotfix 브랜치 삭제
git branch -d hotfix/PROJ-456-critical-bug
```

---

## 📦 Release Workflow

### 1. Release 브랜치 생성

```bash
git checkout develop
git checkout -b release/v1.0.0
```

### 2. 버전 업데이트

```bash
# gradle.properties
version=1.0.0

git commit -m "chore: Bump version to 1.0.0"
```

### 3. Merge 및 태그

```bash
# main에 merge
git checkout main
git merge release/v1.0.0
git tag v1.0.0

# develop에도 merge
git checkout develop
git merge release/v1.0.0

# Push all
git push origin main develop --tags

# Release 브랜치 삭제
git branch -d release/v1.0.0
```

---

## ✅ Best Practices

### 1. Layer별 Commit 분리
```bash
# ✅ 좋은 예
git add domain/src/
git commit -m "feat(domain): Order Aggregate"

git add application/src/
git commit -m "feat(application): CreateOrder UseCase"

# ❌ 나쁜 예
git add .
git commit -m "feat: everything"
```

### 2. 작은 단위로 자주 Commit
- 하나의 기능/수정 = 하나의 Commit
- 2-3일 내 Feature 브랜치 완료

### 3. 의미 있는 Commit Message
```bash
# ❌ 나쁜 예
git commit -m "update"
git commit -m "fix bug"

# ✅ 좋은 예
git commit -m "feat(domain): Order 생성 기능 추가"
git commit -m "fix(application): NPE 방어 로직 추가"
```

### 4. 이슈 추적 키워드 사용
- `Closes #123` - 이슈 닫기
- `Fixes #456` - 버그 수정
- `Relates to #789` - 관련 이슈

### 5. 정기적인 develop 동기화
```bash
# 매일 아침
git checkout develop
git pull origin develop

# Feature 브랜치에 반영
git checkout feature/my-feature
git merge develop
```

---

## 🛠️ Common Commands

### 브랜치 관리
```bash
# 브랜치 목록
git branch -a

# 로컬 브랜치 삭제
git branch -d feature/old-feature

# 리모트 브랜치 삭제
git push origin --delete feature/old-feature

# 머지된 브랜치 일괄 삭제
git branch --merged | grep -v "main\|develop" | xargs git branch -d
```

### 상태 확인
```bash
# 현재 상태
git status

# 변경 사항
git diff

# 최근 커밋 로그
git log --oneline -10

# 브랜치별 커밋 확인
git log --graph --oneline --all
```

### Undo/Reset
```bash
# 마지막 커밋 취소 (변경사항 유지)
git reset --soft HEAD~1

# 특정 파일만 unstage
git restore --staged <file>

# 변경사항 완전히 취소
git checkout -- <file>
```

---

## 🔗 PR Template

`.github/pull_request_template.md`:

```markdown
## 📋 변경 사항
<!-- 무엇을 변경했나요? -->

## 🔗 관련 이슈
Closes #

## 📚 Layer 변경 사항
- [ ] Domain Layer
- [ ] Application Layer
- [ ] Adapter Layer (REST/Persistence)

## ✅ 체크리스트
- [ ] Zero-Tolerance 규칙 준수
- [ ] 테스트 추가/수정
- [ ] 문서 업데이트
- [ ] Breaking changes 문서화

## 🧪 테스트 방법
<!-- 어떻게 테스트했나요? -->

## 📸 스크린샷 (선택)
<!-- UI 변경 시 -->
```

---

## 🤖 Claude Code 통합

### PR 자동 생성
```bash
# Claude Code에서
/git-pr

→ 현재 브랜치 분석
→ 최근 커밋 메시지로 PR 제목 생성
→ Layer 기반 자동 라벨링
→ PR 템플릿 적용
→ GitHub PR 생성
```

### 자동 검증
```bash
# PR 생성 후 자동 실행
1. Code Format Check (Spotless)
2. Convention Validation (Zero-Tolerance)
3. Unit Tests (병렬)
4. Architecture Validation (ArchUnit)
5. Test Coverage (JaCoCo)

# 실패 시
❌ PR Gate failed
→ Claude Code 자동 수정 제안
```

---

## 📚 References

- **Git Flow**: https://nvie.com/posts/a-successful-git-branching-model/
- **Conventional Commits**: https://www.conventionalcommits.org/
- **GitHub CLI**: https://cli.github.com/
- **Pre-commit Hooks**: `hooks/pre-commit`
- **PR Gate**: `tools/pipeline/pr_gate.sh`
