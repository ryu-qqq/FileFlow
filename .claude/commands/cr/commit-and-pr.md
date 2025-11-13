---
description: Cursor TDD 작업 커밋 + PR 생성 (보기 좋은 템플릿)
tags: [project]
---

# /cr/commit-and-pr - Git Workflow 자동화

당신은 **Cursor TDD 워크플로우**로 개발된 Domain Layer 코드를 커밋하고, 푸시한 다음 Pull Request를 생성하는 작업을 수행합니다.

## 목적

1. **자동 커밋**: 템플릿 기반 보기 좋은 커밋 메시지 생성
2. **자동 푸시**: Remote 브랜치로 푸시
3. **자동 PR 생성**: GitHub Pull Request 자동 생성

---

## 입력 형식

사용자는 다음과 같이 명령합니다:

```bash
# 기본 사용 (도메인 이름 자동 감지)
/cr/commit-and-pr

# 도메인 이름 명시
/cr/commit-and-pr Order

# 커밋 메시지 수동 작성 모드
/cr/commit-and-pr --manual
```

---

## 실행 단계

### 1️⃣ Phase 1: 도메인 이름 감지

**자동 감지 우선순위**:
1. 사용자가 명시한 경우 (`/cr/commit-and-pr Order`) → Order 사용
2. 최근 PRD 파일에서 추출 (`docs/prd/{domain}-domain-prd.md`)
3. Git staged files에서 추출 (`domain/src/main/java/.../Order.java`)
4. 사용자에게 직접 질문

**도메인 이름 추출 로직**:
```bash
# 1. Git staged files에서 도메인 추출
git diff --cached --name-only | grep "domain/src/main/java" | \
  sed 's|domain/src/main/java/.*/\([A-Z][a-zA-Z]*\)\.java|\1|' | \
  grep -v "Id\|Status\|Type\|Exception" | head -1

# 2. 최근 PRD 파일에서 추출
ls -t docs/prd/*-domain-prd.md 2>/dev/null | head -1 | \
  sed 's|docs/prd/\(.*\)-domain-prd.md|\1|' | \
  awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}'
```

**예시**:
```
Staged files:
- domain/src/main/java/com/company/domain/order/Order.java
- domain/src/main/java/com/company/domain/order/OrderId.java
- domain/src/main/java/com/company/domain/order/OrderLineItem.java

→ 도메인 이름: "Order" (Aggregate Root 파일명 기준)
```

---

### 2️⃣ Phase 2: 생성된 파일 분석

**Git staged files 분석**:
```bash
git diff --cached --name-only --diff-filter=A
```

**파일 분류 로직**:
- **Aggregate Root**: `{Domain}.java` (예: `Order.java`)
- **ValueObject**: `{Domain}Id.java`, `{Something}.java` (예: `OrderId.java`, `Money.java`)
- **Enum**: `{Domain}Status.java`, `{Domain}Type.java` (예: `OrderStatus.java`)
- **TestFixture**: `{Domain}DomainFixture.java` (예: `OrderDomainFixture.java`)
- **Unit Test**: `{Something}Test.java` (예: `OrderTest.java`, `MoneyTest.java`)

**예시 분석 결과**:
```
📦 Domain Components:
- ✅ Order.java (Aggregate Root)
- ✅ OrderId.java (ValueObject)
- ✅ OrderLineItem.java (ValueObject)
- ✅ Money.java (ValueObject)
- ✅ OrderStatus.java (Enum)

🧪 Test Coverage:
- ✅ OrderDomainFixture.java (TestFixture)
- ✅ OrderTest.java (Unit Test)
- ✅ OrderLineItemTest.java (Unit Test)
- ✅ MoneyTest.java (Unit Test)
```

---

### 3️⃣ Phase 3: 커밋 메시지 생성

**템플릿 (보기 좋은 포맷)**:

```
feat(domain): Add {DomainName} Domain with Cursor TDD

## 📦 생성된 Domain 컴포넌트
- ✅ {DomainName}.java (Aggregate Root)
- ✅ {DomainName}Id.java (ValueObject)
{additional_value_objects}
- ✅ {DomainName}Status.java (Enum)

## 🧪 테스트 커버리지
- ✅ {DomainName}DomainFixture.java (TestFixture)
- ✅ {DomainName}Test.java (Unit Test)
{additional_tests}

## 🎯 개발 방식
- **TDD 사이클**: RED → GREEN → REFACTOR (Kent Beck)
- **도구**: Cursor IDE 실행 + Claude Code 검증
- **Zero-Tolerance**: Lombok 금지, Law of Demeter, Pure Java
- **검증 결과**: 0 violations, 100% 컨벤션 준수

## 📊 효율 메트릭
- 개발 시간: {development_time}
- 컨벤션 위반: 0회
- 테스트 커버리지: 100%

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**실제 예시 (Order Domain)**:
```
feat(domain): Add Order Domain with Cursor TDD

## 📦 생성된 Domain 컴포넌트
- ✅ Order.java (Aggregate Root)
- ✅ OrderId.java (ValueObject)
- ✅ OrderLineItem.java (ValueObject)
- ✅ Money.java (ValueObject)
- ✅ OrderStatus.java (Enum)

## 🧪 테스트 커버리지
- ✅ OrderDomainFixture.java (TestFixture)
- ✅ OrderTest.java (Unit Test)
- ✅ OrderLineItemTest.java (Unit Test)
- ✅ MoneyTest.java (Unit Test)

## 🎯 개발 방식
- **TDD 사이클**: RED → GREEN → REFACTOR (Kent Beck)
- **도구**: Cursor IDE 실행 + Claude Code 검증
- **Zero-Tolerance**: Lombok 금지, Law of Demeter, Pure Java
- **검증 결과**: 0 violations, 100% 컨벤션 준수

## 📊 효율 메트릭
- 개발 시간: 7분 30초
- 컨벤션 위반: 0회
- 테스트 커버리지: 100%

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

### 4️⃣ Phase 4: Git 커밋 실행

**실행 명령**:
```bash
# 커밋 메시지를 HEREDOC으로 전달
git commit -m "$(cat <<'EOF'
feat(domain): Add Order Domain with Cursor TDD

## 📦 생성된 Domain 컴포넌트
- ✅ Order.java (Aggregate Root)
- ✅ OrderId.java (ValueObject)
- ✅ OrderLineItem.java (ValueObject)
- ✅ Money.java (ValueObject)
- ✅ OrderStatus.java (Enum)

## 🧪 테스트 커버리지
- ✅ OrderDomainFixture.java (TestFixture)
- ✅ OrderTest.java (Unit Test)
- ✅ OrderLineItemTest.java (Unit Test)
- ✅ MoneyTest.java (Unit Test)

## 🎯 개발 방식
- **TDD 사이클**: RED → GREEN → REFACTOR (Kent Beck)
- **도구**: Cursor IDE 실행 + Claude Code 검증
- **Zero-Tolerance**: Lombok 금지, Law of Demeter, Pure Java
- **검증 결과**: 0 violations, 100% 컨벤션 준수

## 📊 효율 메트릭
- 개발 시간: 7분 30초
- 컨벤션 위반: 0회
- 테스트 커버리지: 100%

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

**출력 예시**:
```
✅ Commit 완료
[feature/PROJ-123-order abc1234] feat(domain): Add Order Domain with Cursor TDD
 9 files changed, 450 insertions(+)
 create mode 100644 domain/src/main/java/.../Order.java
 create mode 100644 domain/src/main/java/.../OrderId.java
 create mode 100644 domain/src/main/java/.../OrderLineItem.java
 create mode 100644 domain/src/main/java/.../Money.java
 create mode 100644 domain/src/main/java/.../OrderStatus.java
 create mode 100644 domain/src/test/java/.../OrderDomainFixture.java
 create mode 100644 domain/src/test/java/.../OrderTest.java
 create mode 100644 domain/src/test/java/.../OrderLineItemTest.java
 create mode 100644 domain/src/test/java/.../MoneyTest.java
```

---

### 5️⃣ Phase 5: Remote 푸시

**실행 명령**:
```bash
# 현재 브랜치를 remote에 푸시
git push origin HEAD
```

**upstream 설정이 필요한 경우**:
```bash
# -u 플래그로 upstream 설정
git push -u origin HEAD
```

**출력 예시**:
```
✅ Push 완료
Enumerating objects: 25, done.
Counting objects: 100% (25/25), done.
Delta compression using up to 8 threads
Compressing objects: 100% (15/15), done.
Writing objects: 100% (18/18), 5.23 KiB | 5.23 MiB/s, done.
Total 18 (delta 8), reused 0 (delta 0), pack-reused 0
remote: Resolving deltas: 100% (8/8), completed with 5 local objects.
To github.com:company/project.git
   abc1234..def5678  feature/PROJ-123-order -> feature/PROJ-123-order
```

---

### 6️⃣ Phase 6: Pull Request 생성

**실행 명령**:
```bash
# gh CLI를 사용하여 PR 생성
gh pr create --title "feat(domain): Add {DomainName} Domain with Cursor TDD" --body "$(cat <<'EOF'
## 📋 Summary

{DomainName} Domain을 Cursor TDD 워크플로우로 개발했습니다.

## 📦 생성된 Domain 컴포넌트
- ✅ {DomainName}.java (Aggregate Root)
- ✅ {DomainName}Id.java (ValueObject)
{additional_value_objects}
- ✅ {DomainName}Status.java (Enum)

## 🧪 테스트 커버리지
- ✅ {DomainName}DomainFixture.java (TestFixture)
- ✅ {DomainName}Test.java (Unit Test)
{additional_tests}

## ✅ 검증 완료
- [x] validation-helper.py: 0 violations
- [x] ArchUnit tests: 통과
- [x] Unit tests: 100% coverage
- [x] Zero-Tolerance 규칙: 100% 준수

## 🎯 개발 방식
- **TDD 사이클**: RED → GREEN → REFACTOR (Kent Beck)
- **도구**: Cursor IDE (실행) + Claude Code (검증)
- **시간**: {development_time}
- **효율**: 기존 대비 65% 단축

## 🔍 리뷰 포인트
1. **Law of Demeter**: Getter 체이닝 없음 확인
2. **Lombok 금지**: Plain Java 사용 확인
3. **Tell, Don't Ask**: 비즈니스 메서드 구현 확인
4. **Immutability**: ValueObject final 필드 확인

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

**출력 예시**:
```
✅ Pull Request 생성 완료

Creating pull request for feature/PROJ-123-order into main in company/project

https://github.com/company/project/pull/456
```

---

## 실행 예시

### 예시 1: 기본 사용 (자동 감지)

```bash
사용자: /cr/commit-and-pr

Claude:
1. 도메인 이름 감지 중...
   ✅ 감지 완료: "Order" (Git staged files 기준)

2. 생성된 파일 분석 중...
   📦 Domain 컴포넌트: 5개
   🧪 테스트: 4개

3. 커밋 메시지 생성 중...
   ✅ 템플릿 적용 완료

4. Git 커밋 실행 중...
   ✅ Commit 완료: [feature/PROJ-123-order abc1234]

5. Remote 푸시 중...
   ✅ Push 완료: feature/PROJ-123-order

6. Pull Request 생성 중...
   ✅ PR 생성 완료: https://github.com/company/project/pull/456

════════════════════════════════════════════════════════════
🎉 Git Workflow 완료
════════════════════════════════════════════════════════════

✅ Commit: feat(domain): Add Order Domain with Cursor TDD
✅ Push: feature/PROJ-123-order → origin
✅ PR: #456 생성 완료

🔗 Next Steps:
   1. PR 리뷰: https://github.com/company/project/pull/456
   2. AI 리뷰: /ai-review 456
   3. Merge 후 배포

════════════════════════════════════════════════════════════
```

### 예시 2: 도메인 이름 명시

```bash
사용자: /cr/commit-and-pr User

Claude:
1. 도메인 이름: "User" (사용자 명시)

2. 생성된 파일 분석 중...
   📦 Domain 컴포넌트: 4개
   🧪 테스트: 3개

[이하 동일]
```

### 예시 3: 수동 커밋 메시지 모드

```bash
사용자: /cr/commit-and-pr --manual

Claude:
⚠️ 수동 모드 활성화
사용자가 커밋 메시지를 직접 작성합니다.

1. 도메인 이름을 입력하세요:
   > Order

2. 커밋 메시지를 입력하세요:
   > feat(domain): Add Order Domain

   추가 설명을 입력하세요 (종료: Ctrl+D):
   > - Order Aggregate 생성
   > - TDD로 개발
   > ^D

✅ 수동 커밋 메시지 적용:
feat(domain): Add Order Domain

- Order Aggregate 생성
- TDD로 개발

계속하시겠습니까? (y/n): y

3. Git 커밋 실행 중...
[이하 동일]
```

---

## 오류 시나리오 처리

### 시나리오 1: Staged files 없음

```bash
❌ Error: No staged files found

Fix:
1. 파일을 먼저 stage하세요:
   git add domain/src/main/java/.../Order.java
   git add domain/src/test/java/.../OrderTest.java

2. 다시 실행:
   /cr/commit-and-pr
```

### 시나리오 2: 도메인 이름 감지 실패

```bash
⚠️ Warning: 도메인 이름을 자동으로 감지할 수 없습니다.

도메인 이름을 입력하세요:
> Order

✅ 도메인 이름: "Order" (사용자 입력)

계속 진행...
```

### 시나리오 3: Push 실패 (upstream 미설정)

```bash
❌ Push Failed: no upstream branch

Fix:
git push --set-upstream origin feature/PROJ-123-order

자동으로 재시도합니다...
✅ Push 완료 (upstream 설정됨)
```

### 시나리오 4: gh CLI 미설치

```bash
❌ Error: gh CLI not installed

Fix:
1. macOS: brew install gh
2. Linux: sudo apt install gh
3. Windows: winget install GitHub.cli

4. 인증:
   gh auth login

5. 다시 실행:
   /cr/commit-and-pr
```

---

## 관련 명령어

### Cursor TDD 워크플로우 (전체)

```bash
# 1. PRD 작성 (Claude Code)
/cr/domain-prd "Order"

# 2. RED Phase (Cursor IDE)
# Cursor에서: /cr/red

# 3. GREEN Phase (Cursor IDE)
# Cursor에서: /cr/green

# 4. REFACTOR Phase (Cursor IDE)
# Cursor에서: /cr/refactor

# 5. 검증 + 효율 측정 (Claude Code)
/cr/validate

# 6. Git Workflow (Claude Code) ⭐ NEW
/cr/commit-and-pr

# 7. AI 리뷰 (Claude Code)
/ai-review {pr-number}
```

---

## 주의사항

1. **Staged files 필수**: 커밋할 파일이 stage되어 있어야 함
2. **검증 선행 필수**: `/cr/validate`를 먼저 실행하여 0 violations 확인
3. **브랜치 확인**: feature 브랜치에서 실행 (main/master 금지)
4. **커밋 메시지 수정**: 자동 생성된 메시지가 부적절하면 `--manual` 사용
5. **PR 설명 보완**: 필요시 GitHub에서 PR 설명 추가 편집

---

**✅ 이제 Cursor TDD 워크플로우가 Git Workflow까지 완전 자동화되었습니다!**
