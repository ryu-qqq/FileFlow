---
name: work
description: Epic 작업 시작. Epic 브랜치 생성, Task 큐 로드, Auto-commit Hook 활성화.
context: fork
agent: implementer
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /work - Start Working on Epic

Epic 단위로 작업을 시작합니다. Epic 브랜치 생성 → Task 큐 로드 → 순차 작업.

## 사용법

```bash
/work EPIC-123              # Epic으로 작업 시작
/work TASK-456              # 특정 Task부터 시작
/work                       # 가장 최근 Epic/Task 로드
/work --continue            # 중단된 작업 이어서
/work EPIC-123 --no-serena  # Serena 없이 실행
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--continue` | 중단된 작업 이어서 |
| `--from N` | N번째 Task부터 시작 |
| `--no-auto-commit` | Auto-commit 비활성화 |
| `--status` | 현재 작업 상태만 표시 |
| `--no-serena` | Serena 없이 실행 |

## 다음 단계

- `/next` - 현재 Task 완료 → 다음 Task
- `/review` - 코드 리뷰
- `/ship` - 모든 Task 완료 후 PR 생성
