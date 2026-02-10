---
name: legacy-service
description: legacy-query로 생성된 Persistence Layer 기반 Application Layer 생성. Port, Service, Manager, Assembler.
context: fork
agent: legacy-service-generator
allowed-tools: Read, Write, Edit, Glob
---

# /legacy-service

`/legacy-query`로 생성된 **Persistence Layer**를 기반으로 **Application Layer**를 생성합니다.

## 사용법

```bash
/legacy-service admin:BrandController.fetchBrands
/legacy-service web:ProductController.fetchProductGroups
```

## 전제조건

`/legacy-query`로 Persistence Layer가 생성되어 있어야 함

## 출력

```
application/src/main/java/.../legacy/{web|admin}/{domain}/
├── port/
│   ├── in/Legacy{Prefix}{Domain}QueryUseCase.java
│   └── out/Legacy{Prefix}{Domain}CompositeQueryPort.java
├── service/Legacy{Prefix}{Domain}QueryService.java
├── manager/Legacy{Prefix}{Domain}QueryManager.java
└── assembler/Legacy{Prefix}{Domain}Assembler.java
```

## 생성 컴포넌트

| 컴포넌트 | 역할 |
|---------|------|
| **UseCase** | 인바운드 포트 (Controller가 의존) |
| **Port** | 아웃바운드 포트 (Repository 추상화) |
| **Service** | UseCase 구현, 트랜잭션 경계 |
| **Manager** | 비즈니스 로직 |
| **Assembler** | DTO 변환 |

## 다음 단계

Application Layer 생성 완료 후:
```bash
/legacy-controller admin:BrandController.fetchBrands
```
