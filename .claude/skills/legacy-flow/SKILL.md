---
name: legacy-flow
description: 레거시 API 엔드포인트의 전체 호출 흐름을 추적하여 문서화. Controller→Service→Repository→DB.
context: fork
agent: legacy-flow-analyzer
allowed-tools: Glob, Grep, Read, Write
---

# /legacy-flow

레거시 API 엔드포인트의 **전체 호출 스택**을 추적하여 문서화합니다.

## 사용법

```bash
/legacy-flow admin:BrandController.fetchBrands
/legacy-flow web:ProductController.fetchProductGroups
/legacy-flow OrderController.createOrder
```

## 입력 형식

| 형식 | 설명 |
|------|------|
| `{Controller}.{method}` | web API 대상 (기본) |
| `web:{Controller}.{method}` | 명시적 web API |
| `admin:{Controller}.{method}` | admin API 대상 |

## 출력

- `claudedocs/legacy-flows/{web|admin}/{Controller}_{method}.md`

## 분석 내용

- HTTP Method, Path, 파라미터
- Request/Response DTO 구조
- Service → Repository 호출 흐름
- QueryDSL 쿼리 분석 (테이블, JOIN, WHERE)

## 다음 단계

분석 완료 후:
```bash
/legacy-convert admin:BrandController.fetchBrands
```
