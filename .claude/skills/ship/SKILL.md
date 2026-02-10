---
name: ship
description: Epic 완료 처리. WIP 커밋 정리, 최종 커밋, PR 생성, Jira Epic 완료.
context: fork
agent: shipper
allowed-tools: Read, Glob, Grep, Bash
---

# /ship - Ship Your Epic

Epic 전체를 완료하고 PR을 생성합니다.

## 사용법

```bash
/ship                       # Epic 전체 ship
/ship --no-squash           # WIP 커밋 유지
/ship --draft               # Draft PR 생성
/ship --dry-run             # 미리보기만
/ship --no-serena           # Serena 없이 실행
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--no-squash` | WIP 커밋 유지 (히스토리 보존) |
| `--draft` | Draft PR 생성 |
| `--dry-run` | 미리보기만 |
| `--no-jira` | Jira 업데이트 스킵 |
| `--no-pr` | PR 생성 스킵 (푸시만) |
| `--force` | 미완료 Task 있어도 강제 진행 |
| `--message "msg"` | 커밋 메시지 직접 지정 |
| `--no-serena` | Serena 없이 실행 |

## 다음 단계

- `/jira-fetch EPIC-XXX` - 다음 Epic 가져오기
- `/work EPIC-XXX` - 새 작업 시작
