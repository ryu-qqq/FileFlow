---
name: legacy-query
description: legacy-flow 분석 결과 기반 Composite 패턴 QueryDSL Repository 생성. Persistence Layer.
context: fork
agent: legacy-query-generator
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /legacy-query

`/legacy-flow` 분석 결과를 기반으로 **Composite 패턴 QueryDSL Repository**를 생성합니다.

## 사용법

```bash
/legacy-query admin:BrandController.fetchBrands
/legacy-query web:ProductController.fetchProductGroups
```

## 전제조건

`/legacy-flow` 분석 문서가 존재해야 함

## 출력 (3개 모듈에 분산)

**Domain Layer** (검색 조건):
```
domain/src/main/java/.../legacy/{web|admin}/{domain}/dto/query/
└── Legacy{Prefix}{Domain}SearchCondition.java
```

**Application Layer** (결과 DTO):
```
application/src/main/java/.../legacy/{web|admin}/{domain}/dto/response/
└── Legacy{Prefix}{Domain}Result.java
```

**Persistence Layer** (Repository):
```
adapter-out/persistence-mysql-legacy/.../legacy/composite/{web|admin}/{domain}/
├── repository/Legacy{Domain}CompositeQueryDslRepository.java
└── adapter/Legacy{Domain}CompositeQueryAdapter.java
```

## 다음 단계

Repository 생성 완료 후:
```bash
/legacy-service admin:BrandController.fetchBrands
```
