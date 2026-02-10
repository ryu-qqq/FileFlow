---
name: epic
description: Epic 기획 + Task 분해. 작업 유형을 먼저 파악하고 각 상황에 맞는 분석/설계/구현 단계를 진행합니다.
context: fork
agent: planner
allowed-tools: Read, Write, Glob, Grep, Bash
---

# /epic - Category-Based Epic Planning

작업 유형을 먼저 파악하고, 각 카테고리에 맞는 체계적인 분석과 Task 분해를 수행합니다.

## 사용법

```bash
/epic "주문 기능"
/epic "결제 시스템 리팩토링"
/epic "PG사 연동"
/epic "회원가입" --no-serena
```

## 실행 프로세스

```
0️⃣ 작업 유형 파악 (🆕 신규 / ➕ 확장 / 🔄 리팩토링 / 🐛 버그 / 🔌 연동)
1️⃣ 유형별 분석/설계 진행
2️⃣ Task 분해 (~15K tokens/Task, Layer별 분리)
3️⃣ Serena Memory 저장 → epic-{feature}
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--no-serena` | Serena 통합 비활성화 |

## 출력 경로

- Serena Memory: `epic-{feature-kebab-case}`

## 다음 단계

- `/jira-create` - Jira에 Epic + Task 등록
- `/work EPIC-KEY` - 작업 시작
