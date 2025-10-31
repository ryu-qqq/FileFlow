---
description: Jira 이슈 상태 변경 (완료, 진행 중 등)
tags: [project, gitignored]
---

# Jira Issue Transition (Status Change)

당신은 Jira 이슈의 상태를 변경하는 작업을 수행합니다 (예: To Do → In Progress → Done).

## 입력 형식

사용자는 다음 정보를 제공합니다:
- **이슈 키**: PROJ-123 형식의 이슈 키 또는 Jira URL
- **목표 상태**: Done, In Progress, To Do, Code Review 등
- **코멘트**: 상태 변경 시 추가할 코멘트 (선택)
- **해결 방법**: Done 전환 시 Resolution (Fixed, Won't Fix 등) (선택)

## 실행 단계

### 1. Cloud ID 확인
먼저 Atlassian Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 2. 이슈 정보 및 가능한 전환 조회
현재 이슈 상태와 가능한 상태 전환을 확인합니다:
```
mcp__atlassian__getJiraIssue 도구 사용
- cloudId: 1단계에서 획득한 Cloud ID
- issueIdOrKey: 이슈 키 (예: PROJ-123)
- fields: ["summary", "status", "transitions"]
- expand: ["transitions"]
```

### 3. 전환 ID 식별
2단계에서 조회한 가능한 전환(transitions) 목록에서 목표 상태에 해당하는 전환 ID를 찾습니다:

예시:
```json
"transitions": [
  {"id": "11", "name": "In Progress", "to": {"name": "In Progress"}},
  {"id": "21", "name": "Done", "to": {"name": "Done"}},
  {"id": "31", "name": "Code Review", "to": {"name": "Code Review"}}
]
```

### 4. 이슈 상태 전환
찾은 전환 ID로 상태를 변경합니다:
```
mcp__atlassian__transitionJiraIssue 도구 사용
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- transition: {"id": "{전환 ID}"}
- fields: (선택) 해결 방법 등 추가 필드
  {
    "resolution": {"name": "Fixed"}
  }
```

### 5. 코멘트 추가 (선택)
상태 변경에 대한 코멘트를 추가합니다:
```
mcp__atlassian__addJiraComment 도구 사용 (코멘트가 제공된 경우)
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- body: ADF 형식 코멘트
```

### 6. Git 작업 연동 (Done 전환 시)
이슈를 Done으로 전환하는 경우, Git 작업 정리를 제안합니다:
```bash
# PR이 머지되었는지 확인
gh pr list --search "{ISSUE-KEY}" --state merged

# 머지된 경우
git checkout main
git pull origin main
git branch -d feature/{ISSUE-KEY}-*
```

### 7. 출력 형식

```markdown
## ✅ Jira 이슈 상태 변경 완료: {ISSUE-KEY}

**이슈**: {summary}
**이전 상태**: {old_status}
**현재 상태**: {new_status}
**URL**: https://{사이트명}.atlassian.net/browse/{ISSUE-KEY}

### 상태 변경 내역
- 전환: {transition_name}
- 변경 시각: {timestamp}
- 해결 방법: {resolution} (설정된 경우)

### 다음 단계 (Done 전환 시)

1. PR 머지 확인: `gh pr list --search "{ISSUE-KEY}" --state merged`
2. 로컬 브랜치 정리: `git branch -d feature/{ISSUE-KEY}-*`
3. Epic 진행률 확인 (Epic의 하위 태스크인 경우)
```

## 일반적인 워크플로우

### 작업 시작
```bash
/jira-transition PROJ-123 "In Progress"
```

### 코드 리뷰 요청
```bash
/jira-transition PROJ-123 "Code Review" --comment "PR 생성 완료: #456"
```

### 작업 완료
```bash
/jira-transition PROJ-123 "Done" --resolution "Fixed" --comment "모든 테스트 통과, PR 머지 완료"
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `mcp__atlassian__getJiraIssue` (expand=transitions) → 가능한 전환 조회
3. `mcp__atlassian__transitionJiraIssue` → 상태 전환
4. `mcp__atlassian__addJiraComment` (선택) → 코멘트 추가
5. `Bash` (Done 시) → git 브랜치 정리

## 에러 처리

- **Cloud ID 없음**: 사용자에게 Atlassian 계정 연동 확인 요청
- **이슈 없음**: 이슈 키 확인 요청
- **전환 불가능**: 현재 상태에서 목표 상태로 전환할 수 없음을 알림
  - 가능한 전환 목록 표시
- **권한 없음**: 사용자에게 이슈 편집 권한 확인 요청
- **필수 필드 누락**: Resolution 등 필수 필드 입력 요청

## 상태 전환 매핑

일반적인 Jira 워크플로우 상태 전환:

| 현재 상태 | 가능한 전환 | 설명 |
|----------|-----------|------|
| To Do | In Progress | 작업 시작 |
| In Progress | Code Review | 코드 리뷰 요청 |
| In Progress | Done | 직접 완료 (간단한 작업) |
| Code Review | In Progress | 수정 필요 |
| Code Review | Done | 리뷰 통과 및 완료 |
| Done | To Do | 재작업 필요 |

## 추가 기능

- Epic 진행률 자동 업데이트 (하위 태스크 완료 시)
- Slack/Discord 알림 연동
- 자동 Sprint 완료 처리
- Cycle Time 측정 및 표시
