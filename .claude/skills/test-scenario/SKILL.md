---
name: test-scenario
description: E2E 통합 테스트 시나리오 설계. api-endpoints + api-flow 분석 결과 기반.
context: fork
agent: test-scenario-designer
allowed-tools: Read, Write, Glob, Grep
---

# /test-scenario

api-endpoints + api-flow 분석 결과를 기반으로 E2E 통합 테스트 시나리오를 설계합니다.

## 사용법

```bash
/test-scenario admin:seller
/test-scenario admin:sellerapplication
/test-scenario web:product
/test-scenario admin:seller --query-only
/test-scenario admin:seller --command-only
```

## 전제조건

`/api-endpoints` 분석 문서가 존재해야 함:
- `claudedocs/api-endpoints/{admin|web}/{module}_endpoints.md`

`/api-flow` 분석 문서 (권장):
- `claudedocs/api-flows/{admin|web}/*`

## 출력

- `claudedocs/test-scenarios/{admin|web}/{module}_scenarios.md`

## 다음 단계

시나리오 설계 완료 후:
```bash
/test-e2e admin:seller
```
