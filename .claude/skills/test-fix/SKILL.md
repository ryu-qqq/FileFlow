---
name: test-fix
description: test-audit 감사 리포트 기반 테스트 보완. 갭 유형별 타겟 테스트 코드 생성/수정.
context: fork
agent: test-fixer
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-fix

test-audit 감사 리포트를 읽고 식별된 갭을 기반으로 테스트 코드를 보완합니다.
전체를 새로 생성하는 것이 아니라 리포트에 명시된 갭만 정밀 보완합니다.

## 사용법

```bash
/test-fix domain seller                  # 감사 리포트 기반 domain/seller 보완
/test-fix application seller             # application/seller 보완
/test-fix domain seller --high-only      # HIGH 갭만 보완
/test-fix domain seller --dry-run        # 보완 계획만 출력 (코드 생성 안함)
/test-fix domain seller --no-run         # 코드 생성만, 테스트 실행 안함
/test-fix domain seller --report path    # 커스텀 리포트 경로
```

## 입력

- `$ARGUMENTS[0]`: 레이어 (domain, application, adapter-out, adapter-in)
- `$ARGUMENTS[1]`: 패키지명
- `$ARGUMENTS[2]`: (선택) `--high-only`, `--dry-run`, `--no-run`, `--report {path}`

## 전제 조건

`/test-audit`로 생성된 감사 리포트가 존재해야 합니다:
```
claudedocs/test-audit/{layer}-{package}-audit.md
```

## 처리 흐름

```
/test-audit domain seller     ← 감사 리포트 생성
         ↓
/test-fix domain seller       ← 리포트 기반 보완
```

## 갭별 처리

| 갭 유형 | 처리 방식 |
|---------|----------|
| MISSING_TEST | 새 테스트 파일 Write |
| MISSING_FIXTURES | 새 Fixtures 파일 Write |
| MISSING_METHOD | 기존 테스트 파일 Edit (메서드 추가) |
| MISSING_EDGE_CASE | 기존 테스트 파일 Edit (엣지 케이스 추가) |
| MISSING_STATE_TRANSITION | 기존 테스트 파일 Edit (전이 시나리오 추가) |
| PATTERN_VIOLATION | 기존 테스트 파일 Edit (컨벤션 수정) |
```
