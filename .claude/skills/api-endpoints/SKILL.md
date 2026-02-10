---
name: api-endpoints
description: 현재 프로젝트의 엔드포인트를 분석하여 Query(조회)와 Command(명령)로 분류. 통합 테스트 파이프라인의 첫 단계.
context: fork
agent: api-endpoints-analyzer
allowed-tools: Read, Write, Glob, Grep
---

# /api-endpoints

adapter-in 모듈의 Controller에서 엔드포인트를 추출하여 Query/Command로 분류하고 문서화합니다.

## 사용법

```bash
# Admin API 분석
/api-endpoints admin:seller
/api-endpoints admin:selleradmin

# Public API 분석
/api-endpoints web:product
/api-endpoints web:brand

# 버전 명시
/api-endpoints admin:v2/sellerapplication
```

## 입력

- `$ARGUMENTS[0]`: `{prefix}:{module}` (예: admin:seller, web:product)
- prefix 생략 시 admin이 기본값

## 출력

- `claudedocs/api-endpoints/{admin|web}/{module}_endpoints.md`

## 다음 단계

엔드포인트 분류 완료 후:
```bash
/api-flow admin:seller --all
```

## 통합 테스트 파이프라인

```
/api-endpoints → /api-flow → /test-scenario → /test-e2e
```
