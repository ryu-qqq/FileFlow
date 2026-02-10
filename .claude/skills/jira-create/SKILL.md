---
name: jira-create
description: Epic 문서를 기반으로 Jira Epic + Sub-task를 일괄 생성. Jira MCP 기반.
context: fork
agent: jira-manager
allowed-tools: Read, Write, Glob, Grep, Bash
---

# /jira-create

`.claude/plans/epic-{feature}.md` 문서를 기반으로 Jira Epic과 Sub-task를 일괄 생성합니다.

## 사용법

```bash
/jira-create                          # 가장 최근 epic 문서 사용
/jira-create epic-payment             # 특정 epic 문서 지정
/jira-create --dry-run                # 등록 없이 미리보기
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--dry-run` | 실제 등록 없이 미리보기 |
| `--project KEY` | Jira 프로젝트 키 지정 (기본: AESA) |
| `--assignee user` | 다른 담당자 지정 (기본: 본인) |
| `--sprint N` | 스프린트 지정 |
| `--priority P` | 우선순위 수동 지정 |
| `--no-subtasks` | Sub-task 없이 Epic만 생성 |

## 출력

- Jira Epic + Sub-task 생성
- Serena Memory: `jira-{epic-key}`
- Epic 문서에 Jira ID 추가

## 워크플로우

```
/epic → /jira-create → /work → /review → /ship
```
