---
description: GitHub PR과 Jira 이슈 연동
tags: [project, gitignored]
---

# Jira Issue & GitHub PR Integration

당신은 GitHub Pull Request와 Jira 이슈를 연동하는 작업을 수행합니다.

## 입력 형식

사용자는 다음 정보를 제공합니다:
- **이슈 키**: PROJ-123 형식의 이슈 키 또는 Jira URL
- **PR 번호**: GitHub PR 번호 또는 PR URL (선택, 자동 감지 가능)
- **연동 방식**:
  - `link`: PR 링크만 추가
  - `comment`: PR 정보를 코멘트로 추가
  - `transition`: PR 생성 시 이슈 상태 변경 (예: In Progress → Code Review)

## 실행 단계

### 1. Cloud ID 및 PR 정보 확인
```
1. mcp__atlassian__getAccessibleAtlassianResources → Cloud ID 획득
2. gh pr view {PR_NUMBER} --json number,title,url,state,author → PR 정보 조회
```

### 2. 현재 브랜치에서 PR 자동 감지 (PR 번호가 없는 경우)
```bash
# 현재 브랜치의 PR 찾기
gh pr list --head $(git branch --show-current) --json number,title,url

# 또는 이슈 키로 PR 검색
gh pr list --search "{ISSUE-KEY}" --json number,title,url
```

### 3. 이슈 정보 조회
```
mcp__atlassian__getJiraIssue 도구 사용
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- fields: ["summary", "status", "description"]
```

### 4. PR 정보를 Jira 이슈에 연동

#### 옵션 A: Web Link 추가 (Development 섹션)
Jira의 Development 섹션에 PR 링크를 추가합니다:
```
mcp__atlassian__createRemoteLink 도구 사용 (지원되는 경우)
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- object: {
    "url": "{PR_URL}",
    "title": "PR #{PR_NUMBER}: {PR_TITLE}",
    "icon": {
      "url16x16": "https://github.com/favicon.ico"
    }
  }
```

#### 옵션 B: 코멘트 추가
PR 정보를 코멘트로 추가합니다:
```
mcp__atlassian__addJiraComment 도구 사용
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- body: ADF 형식 코멘트
```

코멘트 내용:
```markdown
## 🔗 Pull Request 생성

**PR**: #{PR_NUMBER} - {PR_TITLE}
**URL**: {PR_URL}
**상태**: {PR_STATE}
**작성자**: @{PR_AUTHOR}

### 변경 사항
{PR 설명 요약}

### 다음 단계
- 코드 리뷰 대기
- CI/CD 통과 확인
- 승인 후 머지
```

### 5. 이슈 상태 전환 (선택)
PR 생성 시 이슈를 "Code Review" 상태로 전환합니다:
```
mcp__atlassian__transitionJiraIssue 도구 사용
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- transition: {"id": "{Code Review 전환 ID}"}
```

### 6. PR에 Jira 이슈 정보 추가
GitHub PR 본문에 Jira 이슈 링크를 추가합니다:
```bash
# PR 본문 업데이트
gh pr edit {PR_NUMBER} --body "$(cat <<EOF
{기존 PR 본문}

---
**Jira 이슈**: [{ISSUE-KEY}](https://{사이트명}.atlassian.net/browse/{ISSUE-KEY})
**이슈 제목**: {ISSUE_SUMMARY}
EOF
)"
```

### 7. 출력 형식

```markdown
## ✅ PR과 Jira 이슈 연동 완료

### Jira 이슈: {ISSUE-KEY}
**제목**: {issue_summary}
**이전 상태**: {old_status}
**현재 상태**: {new_status}
**URL**: https://{사이트명}.atlassian.net/browse/{ISSUE-KEY}

### GitHub PR: #{PR_NUMBER}
**제목**: {pr_title}
**URL**: {pr_url}
**상태**: {pr_state}
**작성자**: {pr_author}

### 연동 내용
- [x] Jira 이슈에 PR 링크 추가
- [x] PR에 Jira 이슈 링크 추가
- [x] 이슈 상태 변경 (선택)
- [x] 코멘트 추가 (선택)

### 다음 단계
1. 코드 리뷰 진행
2. CI/CD 통과 확인
3. PR 승인 및 머지
4. 이슈 상태를 Done으로 변경: `/jira-transition {ISSUE-KEY} Done`
```

## 사용 예시

### 기본 연동 (현재 브랜치의 PR)
```bash
/jira-link-pr PROJ-123
```

### PR 번호 지정
```bash
/jira-link-pr PROJ-123 --pr 456
```

### PR URL로 연동
```bash
/jira-link-pr PROJ-123 --pr "https://github.com/org/repo/pull/456"
```

### 상태 전환과 함께 연동
```bash
/jira-link-pr PROJ-123 --pr 456 --transition "Code Review"
```

### 코멘트만 추가
```bash
/jira-link-pr PROJ-123 --pr 456 --comment-only
```

## 자동화 워크플로우

### PR 생성 시 자동 연동
```bash
# 1. 브랜치에서 PR 생성
gh pr create --title "{제목}" --body "{본문}"

# 2. 자동으로 Jira 이슈 연동
/jira-link-pr {ISSUE-KEY}  # 현재 브랜치의 PR 자동 감지
```

### PR 머지 시 자동 완료
GitHub Actions를 통한 자동화 예시:
```yaml
# .github/workflows/jira-integration.yml
on:
  pull_request:
    types: [closed]

jobs:
  update-jira:
    if: github.event.pull_request.merged == true
    runs-on: ubuntu-latest
    steps:
      - name: Extract Jira Issue Key
        run: |
          ISSUE_KEY=$(echo "${{ github.event.pull_request.head.ref }}" | grep -oP 'PROJ-\d+')
          echo "ISSUE_KEY=$ISSUE_KEY" >> $GITHUB_ENV

      - name: Transition to Done
        run: |
          # Jira API 호출하여 상태 변경
          # 또는 Claude Code로 처리
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `Bash` (gh pr view/list) → PR 정보 조회
3. `mcp__atlassian__getJiraIssue` → 이슈 정보 조회
4. `mcp__atlassian__createRemoteLink` 또는 `addJiraComment` → PR 링크 추가
5. `mcp__atlassian__transitionJiraIssue` (선택) → 상태 전환
6. `Bash` (gh pr edit) → PR에 이슈 링크 추가

## 에러 처리

- **Cloud ID 없음**: 사용자에게 Atlassian 계정 연동 확인 요청
- **이슈 없음**: 이슈 키 확인 요청
- **PR 없음**: 현재 브랜치에 PR이 없거나 번호가 잘못됨
- **GitHub CLI 미설치**: `gh` 설치 안내
- **GitHub 인증 실패**: `gh auth login` 실행 안내
- **Remote Link 권한 없음**: 코멘트 방식으로 대체

## 브랜치 네이밍 컨벤션

Jira 이슈와 연동하기 위한 브랜치 네이밍:

```
feature/{ISSUE-KEY}-{요약-kebab-case}
bugfix/{ISSUE-KEY}-{요약-kebab-case}
hotfix/{ISSUE-KEY}-{요약-kebab-case}

예시:
- feature/PROJ-123-user-login
- bugfix/PROJ-124-fix-validation
- hotfix/PROJ-125-security-patch
```

## PR 템플릿 예시

`.github/pull_request_template.md`:
```markdown
## 개요
<!-- 변경 사항 요약 -->

## Jira 이슈
<!-- 자동으로 채워질 예정 -->
**이슈**: [PROJ-XXX](https://yoursite.atlassian.net/browse/PROJ-XXX)

## 변경 사항
- [ ] 변경 사항 1
- [ ] 변경 사항 2

## 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 통과
- [ ] 수동 테스트 완료

## 체크리스트
- [ ] 코드 리뷰 준비 완료
- [ ] 문서 업데이트
- [ ] Breaking changes 없음
```

## 통합 시나리오

### 전체 워크플로우
```bash
# 1. Jira 이슈 분석 및 브랜치 생성
/jira-analyze PROJ-123

# 2. 작업 진행 및 커밋
git add .
git commit -m "feat(PROJ-123): 사용자 로그인 기능 구현"

# 3. PR 생성
gh pr create --title "feat(PROJ-123): 사용자 로그인 기능 구현" --body "..."

# 4. PR과 Jira 연동
/jira-link-pr PROJ-123 --transition "Code Review"

# 5. PR 머지 후
/jira-transition PROJ-123 Done --comment "PR #456 머지 완료"
```

## 추가 기능

- Jira Smart Commits 연동 (`feat(PROJ-123): ...` 형식)
- CI/CD 상태를 Jira에 자동 업데이트
- PR 리뷰 코멘트를 Jira에 동기화
- Jira Sprint와 GitHub Milestone 연동
- Release Notes 자동 생성
