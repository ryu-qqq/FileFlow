---
description: Conventional Commits 기반 Git Commit 워크플로우
---

# Git Commit Workflow

**🎯 역할**: 표준화된 Git Commit 및 검증 프로세스

**📋 표준**: Conventional Commits

## What It Does

일관된 커밋 메시지 작성 및 Pre-commit 검증:

1. ✅ **Conventional Commits** - 구조화된 커밋 메시지
2. ✅ **Pre-commit Hook** - Transaction 경계 자동 검증
3. ✅ **Layer 기반 Commit** - Layer별로 나눠서 커밋
4. ✅ **이슈 추적** - Closes/Fixes 키워드 사용

## Commit Message Format

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

## Usage

### 1. 변경사항 확인

```bash
git status
git diff
```

### 2. Stage Changes (Layer별)

```bash
# Domain layer만
git add domain/src/

# Application layer만
git add application/src/

# 전체
git add .
```

### 3. Commit with Message

```bash
git commit -m "feat(domain): Order Aggregate 구현

- Aggregate Root 추가
- Law of Demeter 준수
- Long FK 전략 적용

Closes #123"
```

### 4. Push

```bash
# 첫 Push
git push -u origin feature/order

# 이후 Push
git push
```

## Examples

### Feature 추가

```bash
git commit -m "feat(domain): Order Aggregate 생성

- Aggregate Root 구현
- Law of Demeter 준수
- 불변성 유지

Closes #123"
```

### Bug 수정

```bash
git commit -m "fix(application): Transaction 경계 수정

외부 API 호출을 트랜잭션 밖으로 이동

Fixes #456"
```

### Refactoring

```bash
git commit -m "refactor(persistence): QueryDSL 최적화

N+1 문제 해결로 성능 30% 개선"
```

## Pre-commit Hook

자동으로 검증되는 항목:

```bash
hooks/pre-commit:
  ✅ Transaction boundary check
  ✅ Spring proxy constraint check
  ✅ Spotless format check (optional)
```

## Best Practices

1. **작은 단위로 자주 Commit**
   - 하나의 기능/수정 = 하나의 Commit

2. **Layer별로 Commit 분리**
   - Domain → Application → Adapter 순서

3. **Commit 전 항상 검증**
   - Pre-commit hook 실행 확인

4. **의미 있는 Message**
   - 무엇을(What) + 왜(Why)

5. **이슈 추적**
   - Closes #123, Fixes #456 사용

## Common Mistakes

### ❌ 너무 큰 Commit

```bash
# ❌ 나쁜 예
git add .
git commit -m "feat: 기능 추가"

# ✅ 좋은 예
git add domain/
git commit -m "feat(domain): Order Aggregate 구현"
```

### ❌ 모호한 Message

```bash
# ❌ 나쁜 예
git commit -m "update"
git commit -m "fix bug"

# ✅ 좋은 예
git commit -m "feat(domain): Order 생성 기능 추가"
git commit -m "fix(application): NPE 방어 로직 추가"
```

## Related

- **Pre-commit Hooks**: `hooks/pre-commit`
- **Validators**: `hooks/validators/`
- **Conventional Commits**: https://www.conventionalcommits.org/
