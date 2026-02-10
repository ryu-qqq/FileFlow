---
name: legacy-controller
description: 마이그레이션 사이클의 마지막 단계. Controller + ApiMapper 생성하여 전체 흐름 완성.
context: fork
agent: legacy-controller-generator
allowed-tools: Read, Write, Edit, Glob
---

# /legacy-controller

마이그레이션 사이클의 **마지막 단계**. Controller + ApiMapper를 생성합니다.

## 사용법

```bash
/legacy-controller admin:BrandController.fetchBrands
/legacy-controller web:ProductController.fetchProductGroups
```

## 전제조건

이전 단계들이 완료되어 있어야 함:
1. `/legacy-convert` - Request/Response DTO
2. `/legacy-query` - Persistence Layer
3. `/legacy-service` - Application Layer

## 출력

**Admin API:**
```
adapter-in/rest-api-admin/.../admin/v1/{domain}/
├── controller/Legacy{Domain}QueryV1Controller.java
└── mapper/Legacy{Domain}V1ApiMapper.java
```

**Web API:**
```
adapter-in/rest-api/.../v1/{domain}/
├── controller/Legacy{Domain}QueryV1Controller.java
└── mapper/Legacy{Domain}V1ApiMapper.java
```

## 생성 컴포넌트

| 컴포넌트 | 역할 |
|---------|------|
| **Controller** | REST 엔드포인트, UseCase 호출 |
| **ApiMapper** | Request→Command, Result→Response 변환 |

## 마이그레이션 완료

이 단계 완료 시 하나의 엔드포인트에 대한 전체 마이그레이션이 완료됩니다.
