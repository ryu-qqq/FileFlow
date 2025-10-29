---
description: Git Branching 전략 (Feature/Hotfix/Release)
---

# Git Workflow

**🎯 역할**: Git Branching 전략 및 워크플로우

**📋 전략**: Git Flow 기반

## What It Does

프로젝트의 Git Branching 전략 및 워크플로우:

1. ✅ **Feature Workflow** - 기능 개발 브랜치
2. ✅ **Hotfix Workflow** - 긴급 버그 수정
3. ✅ **Release Workflow** - 버전 릴리스
4. ✅ **Branch Naming** - 일관된 브랜치 네이밍

## Branch Types

### Main Branches

- `main` - 프로덕션 코드 (안정)
- `develop` - 개발 통합 브랜치

### Supporting Branches

- `feature/*` - 기능 개발
- `hotfix/*` - 긴급 수정
- `release/*` - 릴리스 준비

## Feature Workflow

### 1. 브랜치 생성

```bash
# develop에서 분기
git checkout develop
git checkout -b feature/PROJ-123-order-aggregate
```

### 2. 개발

```bash
# 작업
git add .
git commit -m "feat(domain): Order Aggregate 구현"

# Push
git push -u origin feature/PROJ-123-order-aggregate
```

### 3. PR 생성

```bash
# GitHub CLI 사용
gh pr create \
  --base develop \
  --title "feat: Order Aggregate 구현" \
  --body-file .github/pull_request_template.md
```

### 4. Merge 후 정리

```bash
# develop으로 merge 후
git checkout develop
git pull
git branch -d feature/PROJ-123-order-aggregate
```

## Hotfix Workflow

### 1. 브랜치 생성 (main에서)

```bash
git checkout main
git checkout -b hotfix/PROJ-456-critical-bug
```

### 2. 수정 및 테스트

```bash
# 수정
git commit -m "fix: Critical bug in payment"

# 테스트
./gradlew test
```

### 3. Merge (main + develop)

```bash
# main에 merge
git checkout main
git merge hotfix/PROJ-456-critical-bug
git tag v1.0.1
git push --tags

# develop에도 merge
git checkout develop
git merge hotfix/PROJ-456-critical-bug
```

## Release Workflow

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

# Push
git push --all
git push --tags
```

## Branch Naming

### Feature

```
feature/PROJ-123-description
feature/order-aggregate
feature/payment-integration
```

### Hotfix

```
hotfix/PROJ-456-critical-bug
hotfix/payment-null-pointer
```

### Release

```
release/v1.0.0
release/v2.1.0
```

## Best Practices

1. **항상 develop에서 분기**
   - Feature는 develop에서 시작

2. **짧은 생명주기**
   - Feature 브랜치는 2-3일 내 merge

3. **Rebase 금지 (Public 브랜치)**
   - main/develop은 절대 rebase 금지

4. **정기적인 동기화**
   - 매일 develop pull

5. **브랜치 정리**
   - Merge 후 즉시 삭제

## Common Commands

```bash
# 브랜치 목록
git branch -a

# 최신 develop 동기화
git checkout develop
git pull origin develop

# Feature 브랜치 생성
git checkout -b feature/new-feature develop

# 브랜치 삭제
git branch -d feature/old-feature
git push origin --delete feature/old-feature
```

## Related

- **PR Workflow**: `git-pr.md`
- **Commit Workflow**: `git-commit-workflow.md`
- **Git Flow**: https://nvie.com/posts/a-successful-git-branching-model/
