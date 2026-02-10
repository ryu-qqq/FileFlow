---
name: legacy-convert
description: legacy-flow 분석 결과를 새 컨벤션에 맞는 record 기반 Request/Response DTO로 변환.
context: fork
agent: legacy-dto-converter
allowed-tools: Read, Write, Edit, Glob
---

# /legacy-convert

레거시 API 분석 결과를 **새 컨벤션**에 맞는 **record 기반 DTO**로 변환합니다.

## 사용법

```bash
/legacy-convert admin:BrandController.fetchBrands
/legacy-convert web:ProductController.fetchProductGroups
```

## 전제조건

`/legacy-flow` 분석 문서가 존재해야 함:
- `claudedocs/legacy-flows/{web|admin}/{Controller}_{method}.md`

## 출력

**Admin API:**
```
adapter-in/rest-api-admin/.../admin/v1/{domain}/dto/
├── request/Search{Domain}V1ApiRequest.java
└── response/{Domain}V1ApiResponse.java
```

**Web API:**
```
adapter-in/rest-api/.../v1/{domain}/dto/
├── request/Search{Domain}V1ApiRequest.java
└── response/{Domain}V1ApiResponse.java
```

## 변환 규칙

- `record` 타입 필수
- `@Schema` / `@Parameter` 어노테이션
- `@NotNull` / `@NotBlank` Validation
- Enum → String + `allowableValues`

## 다음 단계

DTO 생성 완료 후:
```bash
/legacy-query admin:BrandController.fetchBrands
```
