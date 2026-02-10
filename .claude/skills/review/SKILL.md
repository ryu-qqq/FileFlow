---
name: review
description: Claude 직접 코드 리뷰. Knowledge Base 기반 컨벤션 검토 + Zero-Tolerance 체크.
context: fork
agent: reviewer
allowed-tools: Read, Edit, Glob, Grep, Bash
---

# /review - Code Review by Claude

Convention Hub 규칙 기반으로 직접 코드 리뷰를 수행합니다.

## 사용법

```bash
/review                     # 현재 변경사항 전체 리뷰
/review --staged            # staged 파일만 리뷰
/review --file Order.java   # 특정 파일만 리뷰
/review --fix               # 자동 수정 가능 항목 즉시 수정
/review --no-serena         # Serena 없이 실행
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--staged` | staged 파일만 리뷰 |
| `--file FILE` | 특정 파일만 리뷰 |
| `--fix` | 자동 수정 실행 |
| `--fix-all` | 모든 수정 적용 |
| `--verbose` | 상세 출력 |
| `--layer LAYER` | 특정 레이어만 |
| `--no-serena` | Serena 없이 실행 |

## 다음 단계

- `/check` - 정적 분석 실행
- `/ship` - 최종 커밋 및 PR 생성
