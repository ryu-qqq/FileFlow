---
name: plan
description: 기능 분석 및 구현 계획 수립. 요구사항 분석 → 영향도 분석 → 구현 전략 결정. Serena memory 자동 저장.
context: fork
agent: planner
allowed-tools: Read, Write, Glob, Grep, Bash
---

# /plan - Feature Planning & Analysis

기능 요청을 분석하고 구현 계획을 수립합니다.

## 사용법

```bash
/plan "주문 취소 기능"
/plan "회원 가입"
/plan "상품 검색"
```

## 실행 프로세스

```
1️⃣ 요구사항 분석 → 비즈니스 규칙 도출
2️⃣ 영향도 분석 (Serena MCP) → 수정/신규 판단
3️⃣ 구현 전략 결정 → 레이어별 매핑
4️⃣ Serena Memory 저장 → plan-{feature}
5️⃣ 사용자 승인 후 다음 단계 안내
```

## 출력 경로

- Serena Memory: `plan-{feature-kebab-case}`

## 다음 단계

- `/epic` - Epic 기획 + Task 분해
- `/work` - 구현 시작
