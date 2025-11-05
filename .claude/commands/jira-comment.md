---
description: Jira 이슈에 코멘트 추가
tags: [project, gitignored]
---

# Jira Issue Comment

당신은 Jira 이슈에 코멘트를 추가하는 작업을 수행합니다.

## 입력 형식

사용자는 다음 정보를 제공합니다:
- **이슈 키**: PROJ-123 형식의 이슈 키 또는 Jira URL
- **코멘트 내용**: 추가할 코멘트 텍스트
- **멘션**: 특정 사용자 멘션 (선택, @username 형식)
- **첨부**: 코드 스니펫, 링크 등 (선택)

## 실행 단계

### 1. Cloud ID 확인
먼저 Atlassian Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 2. 이슈 정보 확인 (선택)
코멘트를 추가할 이슈의 기본 정보를 확인합니다:
```
mcp__atlassian__getJiraIssue 도구 사용
- cloudId: 1단계에서 획득한 Cloud ID
- issueIdOrKey: 이슈 키 (예: PROJ-123)
- fields: ["summary", "status"]
```

### 3. 코멘트 본문 준비
ADF (Atlassian Document Format) 형식으로 코멘트를 구성합니다:

**기본 텍스트**:
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
          "text": "{코멘트 내용}"
        }
      ]
    }
  ]
}
```

**사용자 멘션**:
```json
{
  "type": "paragraph",
  "content": [
    {
      "type": "mention",
      "attrs": {
        "id": "{accountId}",
        "text": "@{displayName}"
      }
    },
    {
      "type": "text",
      "text": " {추가 메시지}"
    }
  ]
}
```

**코드 블록**:
```json
{
  "type": "codeBlock",
  "attrs": {
    "language": "java"
  },
  "content": [
    {
      "type": "text",
      "text": "{코드 내용}"
    }
  ]
}
```

**링크**:
```json
{
  "type": "paragraph",
  "content": [
    {
      "type": "text",
      "text": "PR 링크: ",
      "marks": []
    },
    {
      "type": "text",
      "text": "{URL}",
      "marks": [
        {
          "type": "link",
          "attrs": {
            "href": "{URL}"
          }
        }
      ]
    }
  ]
}
```

### 4. 코멘트 추가
준비한 ADF 본문으로 코멘트를 추가합니다:
```
mcp__atlassian__addJiraComment 도구 사용
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- body: 3단계에서 준비한 ADF 객체
```

### 5. 출력 형식

```markdown
## ✅ 코멘트 추가 완료: {ISSUE-KEY}

**이슈**: {summary}
**URL**: https://{사이트명}.atlassian.net/browse/{ISSUE-KEY}

### 추가된 코멘트
```
{코멘트 내용 미리보기}
```

**작성 시각**: {timestamp}
**작성자**: {current_user}
```

## 사용 예시

### 간단한 코멘트
```bash
/jira-comment PROJ-123 "테스트 완료했습니다. 모든 케이스 통과!"
```

### 사용자 멘션
```bash
/jira-comment PROJ-123 "@john.doe 리뷰 부탁드립니다."
```

### 코드 스니펫 포함
```bash
/jira-comment PROJ-123 "다음 코드로 수정했습니다:
\`\`\`java
public void process() {
    // 개선된 로직
}
\`\`\`"
```

### PR 링크 포함
```bash
/jira-comment PROJ-123 "PR 생성 완료: https://github.com/org/repo/pull/456"
```

### 진행 상황 업데이트
```bash
/jira-comment PROJ-123 "## 진행 상황
- ✅ API 개발 완료
- ✅ 단위 테스트 작성
- 🔄 통합 테스트 진행 중
- ⏳ 문서화 예정"
```

### 블로킹 이슈 보고
```bash
/jira-comment PROJ-123 "⚠️ 블로킹 이슈 발견
PROJ-100이 완료되어야 진행 가능합니다.
@tech.lead 확인 부탁드립니다."
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `mcp__atlassian__getJiraIssue` (선택) → 이슈 정보 확인
3. `mcp__atlassian__addJiraComment` → 코멘트 추가

## 에러 처리

- **Cloud ID 없음**: 사용자에게 Atlassian 계정 연동 확인 요청
- **이슈 없음**: 이슈 키 확인 요청
- **권한 없음**: 사용자에게 이슈 코멘트 권한 확인 요청
- **멘션 실패**: 사용자 ID를 찾을 수 없는 경우 텍스트로 대체
- **ADF 파싱 오류**: 코멘트 형식 단순화하여 재시도

## 코멘트 템플릿

### 작업 시작
```
작업을 시작합니다.

## 계획
- [ ] {작업 1}
- [ ] {작업 2}
- [ ] {작업 3}

예상 완료: {날짜}
```

### 작업 완료
```
작업 완료했습니다.

## 변경 사항
- {변경 사항 1}
- {변경 사항 2}

## 테스트 결과
✅ 모든 테스트 통과 (XXX개)

PR: {PR_URL}
```

### 블로커 보고
```
⚠️ 블로킹 이슈 발견

## 문제
{문제 설명}

## 영향
{영향도}

## 필요한 조치
{필요한 조치}

@{담당자} 확인 부탁드립니다.
```

### 질문
```
## ❓ 질문

{질문 내용}

## 배경
{배경 설명}

## 제안
1. 방안 A: {설명}
2. 방안 B: {설명}

@{담당자} 의견 부탁드립니다.
```

## Markdown to ADF 변환

자동으로 Markdown을 ADF로 변환합니다:

- `**굵게**` → Bold
- `*기울임*` → Italic
- `` `코드` `` → Inline code
- ` ```언어\n코드\n``` ` → Code block
- `[텍스트](URL)` → Link
- `# 헤딩` → Heading
- `- 항목` → Bullet list
- `1. 항목` → Ordered list

## 추가 기능

- Git commit 메시지와 연동하여 자동 코멘트 추가
- PR 머지 시 자동 완료 코멘트
- CI/CD 결과 자동 코멘트
- 코드 리뷰 결과 연동
- 스크린샷 첨부 (이미지 URL)
