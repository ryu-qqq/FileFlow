---
name: legacy-endpoints
description: 레거시 모듈의 엔드포인트를 Query/Command로 분류하여 문서화. 마이그레이션 계획 수립의 첫 단계.
context: fork
agent: legacy-endpoints-analyzer
allowed-tools: Glob, Grep, Read, Write
---

# /legacy-endpoints

레거시 모듈의 엔드포인트를 **Query(조회)**와 **Command(명령)**로 분류합니다.

## 사용법

```bash
/legacy-endpoints admin:brand      # Admin API의 brand 모듈 분석
/legacy-endpoints web:product      # Web API의 product 모듈 분석
/legacy-endpoints order            # Web API의 order 모듈 분석 (기본값)
```

## 입력 형식

| 형식 | 설명 |
|------|------|
| `{module}` | web API 대상 (기본) |
| `web:{module}` | 명시적 web API |
| `admin:{module}` | admin API 대상 |

## 출력

- `claudedocs/legacy-endpoints/{web|admin}/{module}_endpoints.md`

## 다음 단계

분석 완료 후 각 엔드포인트에 대해:
```bash
/legacy-flow admin:BrandController.fetchBrands
```
