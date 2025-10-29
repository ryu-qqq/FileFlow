---
description: GitHub PR 자동 생성 (gh CLI)
---

# Create Pull Request

**🎯 역할**: GitHub PR 자동 생성 및 설정

**📋 도구**: `gh` CLI (GitHub CLI)

## What It Does

현재 브랜치에서 PR을 자동으로 생성하고 설정합니다:

1. ✅ **PR 생성** - 현재 브랜치 → main/develop
2. ✅ **자동 제목** - 최근 커밋 메시지 사용
3. ✅ **템플릿 적용** - `.github/pull_request_template.md`
4. ✅ **Layer 라벨** - 브랜치명 기반 자동 라벨
5. ✅ **CI 트리거** - PR 생성 시 자동 검증 시작

## Usage

### 기본 사용 (현재 브랜치)

```bash
gh pr create \
  --title "$(git log -1 --pretty=%s)" \
  --body-file .github/pull_request_template.md \
  --base main
```

### Draft PR 생성

```bash
gh pr create --draft \
  --title "WIP: Order aggregate" \
  --base main
```

### 특정 브랜치로 PR

```bash
gh pr create \
  --base develop \
  --head feature/order-domain
```

## Cascade에서 실행

```
/git-pr
```

## Auto-Labeling (옵션)

브랜치명 기반 자동 라벨링:

```bash
BRANCH=$(git branch --show-current)

# Layer labels
case "$BRANCH" in
  *domain*)
    gh pr edit --add-label "layer:domain"
    ;;
  *application*|*usecase*)
    gh pr edit --add-label "layer:application"
    ;;
  *adapter*|*controller*|*persistence*)
    gh pr edit --add-label "layer:adapter"
    ;;
esac

# Type labels (from commit message)
TITLE=$(git log -1 --pretty=%s)
case "$TITLE" in
  feat*)
    gh pr edit --add-label "type:feature"
    ;;
  fix*)
    gh pr edit --add-label "type:bugfix"
    ;;
  refactor*)
    gh pr edit --add-label "type:refactor"
    ;;
esac
```

## PR Template

`.github/pull_request_template.md`:

```markdown
## 📋 변경 사항
<!-- 무엇을 변경했나요? -->

## 🔗 관련 이슈
Closes #

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

## Output

**성공 시**:
```
✅ Pull request created!

https://github.com/username/repo/pull/123

Title: feat: Add Order aggregate
Base: main ← Head: feature/order-domain
Labels: layer:domain, type:feature
```

## CI Integration

PR 생성 시 자동으로 실행되는 검증:

```yaml
# .github/workflows/pr.yml
name: PR Validation

on:
  pull_request:
    branches: [main, develop]

jobs:
  pr-gate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run PR Gate
        run: ./tools/pipeline/pr_gate.sh
```

## Related

- **GitHub CLI**: https://cli.github.com/
- **PR Template**: `.github/pull_request_template.md`
- **CI Workflow**: `.github/workflows/pr.yml`
- **PR Gate**: `tools/pipeline/pr_gate.sh`

## Prerequisites

```bash
# GitHub CLI 설치 확인
gh --version

# 인증 확인
gh auth status

# 없으면 로그인
gh auth login
```
