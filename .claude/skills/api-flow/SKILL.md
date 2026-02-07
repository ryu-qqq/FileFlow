---
name: api-flow
description: 엔드포인트의 전체 호출 흐름을 Hexagonal 레이어별로 추적하여 문서화. 통합 테스트 파이프라인의 두 번째 단계.
context: fork
agent: api-flow-analyzer
allowed-tools: Read, Write, Glob, Grep
---

# /api-flow

`/api-endpoints`로 분류된 엔드포인트의 전체 호출 흐름을 Hexagonal 레이어별로 추적합니다.

## 사용법

```bash
# 단일 엔드포인트 분석
/api-flow admin:SellerQueryController.searchSellers

# 모듈 전체 분석
/api-flow admin:seller --all

# Query/Command 필터
/api-flow admin:seller --query-only
/api-flow admin:seller --command-only
```

## 입력

- `$ARGUMENTS[0]`: `{prefix}:{Controller.method}` 또는 `{prefix}:{module}`
- `$ARGUMENTS[1]`: (선택) `--all`, `--query-only`, `--command-only`, `--no-db`

## 전제조건

`/api-endpoints` 분석 문서 (--all 모드 시 필요):
- `claudedocs/api-endpoints/{admin|web}/{module}_endpoints.md`

## 출력

```
claudedocs/api-flows/{admin|web}/{Controller}_{method}.md    # 단일
claudedocs/api-flows/{admin|web}/{module}_all_flows.md       # --all
```

## 다음 단계

플로우 분석 완료 후:
```bash
/test-scenario admin:seller
```

## 통합 테스트 파이프라인

```
/api-endpoints → /api-flow → /test-scenario → /test-e2e
```
