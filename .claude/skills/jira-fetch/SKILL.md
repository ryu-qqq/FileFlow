---
name: jira-fetch
description: Jira Epic을 조회하여 Task Queue를 구성하고 작업 컨텍스트를 로드. Jira MCP 기반.
context: fork
agent: jira-manager
allowed-tools: Read, Write, Glob, Grep, Bash
---

# /jira-fetch

Jira Epic을 조회하여 Sub-task 기반 Task Queue를 구성하고 작업 컨텍스트를 로드합니다.

## 사용법

```bash
/jira-fetch EPIC-123           # Epic 전체 가져오기
/jira-fetch TASK-456           # 개별 Task만
/jira-fetch --my-epics         # 나에게 할당된 Epic 목록
/jira-fetch --sprint current   # 현재 스프린트 Epic
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--my-epics` | 나에게 할당된 Epic 목록 |
| `--sprint current` | 현재 스프린트 Epic |
| `--status todo` | 특정 상태 필터 |
| `--refresh` | 캐시 무시하고 새로 조회 |

## 출력

- Task Queue 구성 (순서, 상태, Story Points)
- 브랜치명 생성: `feature/EPIC-{key}-{short-desc}`
- Serena Memory: `jira-epic-{key}`

## 워크플로우

```
/jira-fetch → /work → /review → /ship
```
