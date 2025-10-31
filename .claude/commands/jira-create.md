---
description: Jira 이슈 생성
tags: [project, gitignored]
---

# Jira Issue Creation

당신은 새로운 Jira 이슈를 생성하는 작업을 수행합니다.

## 입력 형식

사용자는 다음 정보를 제공합니다:
- **프로젝트 키**: Jira 프로젝트 키 (예: KAN, PROJ)
- **이슈 타입**: Task, Story, Bug, Epic 등
- **제목 (Summary)**: 이슈 제목
- **설명 (Description)**: 상세 내용 (선택)
- **담당자 (Assignee)**: 담당자 ID 또는 이메일 (선택)
- **우선순위 (Priority)**: Highest, High, Medium, Low, Lowest (선택)
- **Epic Link**: 소속 Epic 키 (선택)
- **Labels**: 라벨 목록 (선택)

## 실행 단계

### 1. Cloud ID 확인
먼저 Atlassian Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 2. 프로젝트 정보 확인
프로젝트가 존재하는지 확인하고, 사용 가능한 이슈 타입을 조회합니다:
```
mcp__atlassian__getJiraProject 도구 사용
- cloudId: 1단계에서 획득한 Cloud ID
- projectIdOrKey: 사용자가 입력한 프로젝트 키
```

### 3. 이슈 필드 준비
다음 정보를 기반으로 이슈 필드를 구성합니다:

**필수 필드**:
- `project`: {"key": "{PROJECT_KEY}"}
- `summary`: "{이슈 제목}"
- `issuetype`: {"name": "{이슈 타입}"}

**선택 필드**:
- `description`: ADF (Atlassian Document Format) 형식
  ```json
  {
    "type": "doc",
    "version": 1,
    "content": [
      {
        "type": "paragraph",
        "content": [
          {
            "type": "text",
            "text": "{설명 내용}"
          }
        ]
      }
    ]
  }
  ```
- `assignee`: {"accountId": "{담당자 ID}"} 또는 {"emailAddress": "{이메일}"}
- `priority`: {"name": "{우선순위}"}
- `parent`: {"key": "{EPIC_KEY}"} (Epic Link의 경우)
- `labels`: ["{라벨1}", "{라벨2}"]

### 4. Jira 이슈 생성
구성한 필드로 이슈를 생성합니다:
```
mcp__atlassian__createJiraIssue 도구 사용
- cloudId: Cloud ID
- fields: 3단계에서 준비한 필드 객체
```

### 5. 브랜치 생성 (선택)
이슈가 성공적으로 생성되면, 연관된 Git 브랜치 생성을 제안합니다:
```bash
# 제안: feature/{ISSUE-KEY}-{요약-kebab-case}
git checkout -b feature/{ISSUE-KEY}-{요약-kebab-case}
```

### 6. 출력 형식

```markdown
## ✅ Jira 이슈 생성 완료: {ISSUE-KEY}

**프로젝트**: {PROJECT_KEY}
**타입**: {이슈 타입}
**제목**: {summary}
**URL**: https://{사이트명}.atlassian.net/browse/{ISSUE-KEY}
**상태**: {status}
**담당자**: {assignee} (설정된 경우)
**우선순위**: {priority} (설정된 경우)

### 다음 단계

1. 이슈 URL에서 상세 내용 확인
2. 브랜치 생성: `git checkout -b feature/{ISSUE-KEY}-{요약-kebab-case}`
3. 작업 시작: `/jira-analyze {ISSUE-KEY}`
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `mcp__atlassian__getJiraProject` → 프로젝트 정보 확인
3. `mcp__atlassian__createJiraIssue` → 이슈 생성
4. `Bash` (선택) → git 브랜치 생성

## 에러 처리

- **Cloud ID 없음**: 사용자에게 Atlassian 계정 연동 확인 요청
- **프로젝트 없음**: 프로젝트 키 확인 요청
- **권한 없음**: 사용자에게 Jira 이슈 생성 권한 확인 요청
- **필수 필드 누락**: 누락된 필드 입력 요청
- **Epic 없음**: Epic Link가 유효하지 않음을 알림

## 사용 예시

### 기본 Task 생성
```bash
/jira-create --project PROJ --type Task --summary "사용자 로그인 기능 구현"
```

### 상세 Story 생성
```bash
/jira-create --project PROJ --type Story --summary "주문 목록 조회 API" --description "사용자가 자신의 주문 목록을 조회할 수 있는 REST API를 구현합니다." --assignee "user@example.com" --priority High
```

### Epic 하위 Task 생성
```bash
/jira-create --project PROJ --type Task --summary "결제 모듈 테스트" --epic PROJ-10 --labels "testing,payment"
```

## 추가 기능

- 이슈 생성 후 자동으로 `/jira-analyze {ISSUE-KEY}` 실행 여부 확인
- Sprint에 이슈 추가 옵션
- Story Point 추가 옵션
- Watchers 추가 옵션
