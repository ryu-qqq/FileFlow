---
name: test-e2e
description: E2E 통합 테스트 코드 생성. test-scenario 문서 기반 실제 테스트 코드 생성 및 실행.
context: fork
agent: e2e-test-generator
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-e2e

test-scenario로 설계된 시나리오를 기반으로 E2E 통합 테스트 코드를 생성합니다.

## 사용법

```bash
/test-e2e admin:seller
/test-e2e admin:sellerapplication
/test-e2e web:product
/test-e2e admin:seller --query-only
/test-e2e admin:seller --command-only
/test-e2e admin:seller --no-run
```

## 전제조건

`/test-scenario` 시나리오 문서가 존재해야 함:
- `claudedocs/test-scenarios/{admin|web}/{module}_scenarios.md`

## 출력

```
integration-test/src/test/java/.../e2e/{admin|web}/{domain}/
└── {Domain}{Admin}E2ETest.java
```

## 옵션

| 옵션 | 설명 |
|------|------|
| `--query-only` | Query 테스트만 생성 |
| `--command-only` | Command 테스트만 생성 |
| `--no-run` | 생성만, 실행 안 함 |
| `--dry-run` | 미리보기 |
