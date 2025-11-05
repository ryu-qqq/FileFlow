---
description: Jira 이슈 정보 업데이트 (설명, 라벨, 담당자 등)
tags: [project, gitignored]
---

# Jira Issue Update

당신은 Jira 이슈의 정보를 업데이트하는 작업을 수행합니다.

## 입력 형식

사용자는 다음 정보를 제공합니다:
- **이슈 키**: PROJ-123 형식의 이슈 키 또는 Jira URL
- **업데이트 필드**: 변경할 필드와 새 값
  - Summary (제목)
  - Description (설명)
  - Assignee (담당자)
  - Priority (우선순위)
  - Labels (라벨)
  - Story Points (스토리 포인트)
  - Epic Link (소속 Epic)
  - Sprint (스프린트)
  - Custom Fields (커스텀 필드)

## 실행 단계

### 1. Cloud ID 확인
먼저 Atlassian Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 2. 현재 이슈 정보 조회
업데이트 전 현재 이슈 정보를 확인합니다:
```
mcp__atlassian__getJiraIssue 도구 사용
- cloudId: 1단계에서 획득한 Cloud ID
- issueIdOrKey: 이슈 키 (예: PROJ-123)
- fields: ["summary", "description", "assignee", "priority", "labels", "customfield_*"]
```

### 3. 업데이트 필드 준비
변경할 필드를 구성합니다:

**일반 필드**:
- `summary`: "{새 제목}"
- `description`: ADF 형식
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
            "text": "{새 설명}"
          }
        ]
      }
    ]
  }
  ```
- `assignee`: {"accountId": "{담당자 ID}"} 또는 {"emailAddress": "{이메일}"}
- `priority`: {"name": "{우선순위}"}
- `labels`: ["{라벨1}", "{라벨2}"] (기존 라벨 덮어쓰기)

**라벨 추가/제거** (덮어쓰기 대신):
```json
{
  "update": {
    "labels": [
      {"add": "{추가할 라벨}"},
      {"remove": "{제거할 라벨}"}
    ]
  }
}
```

**Epic Link 변경**:
- `parent`: {"key": "{새로운 EPIC_KEY}"}

**Story Points** (커스텀 필드):
- `customfield_10016`: {story_point_value}

### 4. Jira 이슈 업데이트
준비한 필드로 이슈를 업데이트합니다:
```
mcp__atlassian__updateJiraIssue 도구 사용
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- fields: 3단계에서 준비한 필드 객체
또는
- update: 라벨 추가/제거 등 부분 업데이트
```

### 5. 변경 사항 확인
업데이트된 이슈 정보를 다시 조회하여 확인합니다:
```
mcp__atlassian__getJiraIssue 도구 사용 (변경된 필드만)
```

### 6. 출력 형식

```markdown
## ✅ Jira 이슈 업데이트 완료: {ISSUE-KEY}

**이슈**: {summary}
**URL**: https://{사이트명}.atlassian.net/browse/{ISSUE-KEY}

### 변경 사항

| 필드 | 이전 값 | 새 값 |
|------|---------|-------|
| Summary | {old_summary} | {new_summary} |
| Description | (변경됨) | (업데이트 완료) |
| Assignee | {old_assignee} | {new_assignee} |
| Priority | {old_priority} | {new_priority} |
| Labels | {old_labels} | {new_labels} |

### 업데이트 시각
{timestamp}
```

## 사용 예시

### 제목 변경
```bash
/jira-update PROJ-123 --summary "사용자 로그인 기능 개선"
```

### 설명 업데이트
```bash
/jira-update PROJ-123 --description "OAuth 2.0을 사용한 소셜 로그인 기능을 추가합니다.\n\n## Acceptance Criteria\n- Google 로그인 연동\n- Facebook 로그인 연동\n- 보안 토큰 관리"
```

### 담당자 변경
```bash
/jira-update PROJ-123 --assignee "user@example.com"
```

### 라벨 추가
```bash
/jira-update PROJ-123 --add-labels "security,authentication"
```

### 라벨 제거
```bash
/jira-update PROJ-123 --remove-labels "deprecated"
```

### 우선순위 변경
```bash
/jira-update PROJ-123 --priority "High"
```

### Story Points 설정
```bash
/jira-update PROJ-123 --story-points 5
```

### Epic 변경
```bash
/jira-update PROJ-123 --epic PROJ-10
```

### 복합 업데이트
```bash
/jira-update PROJ-123 --summary "로그인 기능 개선" --priority "High" --add-labels "security" --story-points 8
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `mcp__atlassian__getJiraIssue` → 현재 이슈 정보 조회
3. `mcp__atlassian__updateJiraIssue` → 이슈 업데이트
4. `mcp__atlassian__getJiraIssue` → 변경 사항 확인

## 에러 처리

- **Cloud ID 없음**: 사용자에게 Atlassian 계정 연동 확인 요청
- **이슈 없음**: 이슈 키 확인 요청
- **권한 없음**: 사용자에게 이슈 편집 권한 확인 요청
- **필드 검증 실패**:
  - 우선순위 값 오류 → 사용 가능한 우선순위 목록 표시
  - Epic 없음 → Epic 키 확인 요청
  - 담당자 없음 → 담당자 ID/이메일 확인 요청
- **커스텀 필드 ID 확인 실패**: 프로젝트 설정에서 필드 ID 확인 요청

## Description ADF 형식 예시

Jira의 Description은 Atlassian Document Format (ADF)를 사용합니다:

### 간단한 텍스트
```json
{
  "type": "doc",
  "version": 1,
  "content": [
    {
      "type": "paragraph",
      "content": [
        {"type": "text", "text": "간단한 설명입니다."}
      ]
    }
  ]
}
```

### 헤딩과 리스트
```json
{
  "type": "doc",
  "version": 1,
  "content": [
    {
      "type": "heading",
      "attrs": {"level": 2},
      "content": [{"type": "text", "text": "작업 목표"}]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [{"type": "text", "text": "목표 1"}]
            }
          ]
        }
      ]
    }
  ]
}
```

## 추가 기능

- Markdown을 ADF로 자동 변환
- 업데이트 내역 로깅
- 대량 이슈 업데이트 (여러 이슈 키 지원)
- 템플릿 기반 업데이트
