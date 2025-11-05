---
description: Jira 태스크 분석 및 TodoList 생성
tags: [project, gitignored]
---

# Jira Task Analysis & TodoList Creation

당신은 Jira 이슈를 분석하고 구조화된 TodoList를 생성하는 작업을 수행합니다.

## 입력 형식

사용자는 다음 형식 중 하나로 정보를 제공합니다:
- Jira URL: `https://ryuqqq.atlassian.net/browse/{ISSUE-KEY}` 또는 `https://ryuqqq.atlassian.net/jira/software/projects/{PROJECT}/boards/{BOARD}?selectedIssue={ISSUE-KEY}`
- 이슈 키만: `{PROJECT}-{NUMBER}` (예: KAN-6)

## 실행 단계

### 1. Cloud ID 확인
먼저 Atlassian Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 2. Jira 이슈 상세 정보 조회
URL 또는 이슈 키에서 추출한 정보로 이슈를 조회합니다:
```
mcp__atlassian__getJiraIssue 도구 사용
- cloudId: 1단계에서 획득한 Cloud ID 또는 URL에서 추출한 사이트명 (예: "ryuqqq.atlassian.net")
- issueIdOrKey: 추출한 이슈 키 (예: KAN-6)
- fields: ["summary", "description", "status", "issuetype", "parent", "subtasks", "customfield_*"]

참고: cloudId는 프로젝트별로 다를 수 있으므로, URL에서 사이트명을 추출하거나
getAccessibleAtlassianResources를 통해 동적으로 가져오는 것을 권장합니다.
```

### 3. Epic 정보 조회 (해당되는 경우)
이슈가 Epic의 하위 태스크인 경우, Epic 정보도 조회합니다:
```
parent 필드에 Epic이 있다면:
- mcp__atlassian__getJiraIssue로 Epic 정보도 조회
```

### 4. 태스크 분석
조회한 정보에서 다음을 추출합니다:
- **요약**: 태스크 제목
- **설명**: 상세 작업 내용 (Markdown 형식)
- **현재 상태**: 진행 상황
- **Epic 정보**: 소속 Epic의 제목 및 목표
- **하위 태스크**: Subtask 목록
- **브랜치 정보**: 커스텀 필드에서 브랜치명 추출 (있는 경우)

### 5. Git 브랜치 처리
브랜치 정보가 있는 경우:
```bash
git fetch origin
if git rev-parse --verify --quiet "origin/{branch-name}"; then
  git checkout {branch-name}
  git pull origin {branch-name}
else
  git checkout -b {branch-name}
fi
```

브랜치 정보가 없는 경우, 이슈 키 기반으로 제안:
```bash
# 제안: feature/{ISSUE-KEY}-{요약-kebab-case}
# 이슈 요약을 kebab-case로 변환하여 브랜치명 생성
git checkout -b feature/{ISSUE-KEY}-{요약-kebab-case}
```

### 6. TodoList 생성
TodoWrite 도구를 사용하여 구조화된 작업 목록 생성:

**분석 기준**:
- Epic의 목표와 연계된 작업인지 확인
- 설명(description)에서 acceptance criteria 추출
- Subtask가 있다면 각각을 별도 todo 항목으로
- 기술적 구현 단계 식별
- 테스트 및 검증 단계 추가

**Todo 항목 구조**:
1. 브랜치 체크아웃 (completed 상태로 시작)
2. 설계/분석 작업 (필요시)
3. 구현 작업들 (acceptance criteria 기반)
4. 테스트 작성 및 실행
5. 코드 리뷰 준비
6. PR 생성 (마지막 단계)

### 7. 출력 형식

```markdown
## Jira 태스크 분석: {ISSUE-KEY}

**제목**: {summary}
**Epic**: {epic_summary} (있는 경우)
**현재 상태**: {status}
**브랜치**: {branch_name}

### 작업 설명
{description 요약}

### TodoList 생성 완료
{TodoWrite 도구로 생성된 항목 개수}개 작업 항목이 생성되었습니다.
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `mcp__atlassian__getJiraIssue` → 이슈 상세 정보
3. `mcp__atlassian__getJiraIssue` (선택) → Epic 정보
4. `Bash` → git 브랜치 체크아웃
5. `TodoWrite` → 구조화된 작업 목록 생성

## 에러 처리

- **Cloud ID 없음**: URL에서 사이트명 추출하여 사용 (예: "yoursite.atlassian.net")
- **이슈 없음**: 이슈 키 확인 요청
- **권한 없음**: 사용자에게 Jira 접근 권한 확인 요청
- **브랜치 충돌**: 사용자에게 브랜치 전략 확인

## 추가 기능

- 관련 PR 링크가 있다면 함께 출력
- 차단/차단된 이슈 표시
- 예상 작업 시간 표시 (있는 경우)
- 담당자 정보 표시

## 사용 예시

```bash
/jira-task AAA-6
/jira-task https://ryuqqq.atlassian.net/browse/AAA-6
```
