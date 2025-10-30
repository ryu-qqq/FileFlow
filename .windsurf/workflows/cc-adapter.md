---
description: persistence layer adapter 보일러 템플릿 를 CC에 준수하여 만든다
---

# Repository Adapter Generation Workflow (Alias)

**목적**: Repository Adapter 자동 생성 (cc-repository.md와 동일)
**타겟**: Persistence Layer - Repository Adapter Pattern
**검증**: RepositoryAdapterConventionTest (ArchUnit)

---

## 📋 워크플로우

이 워크플로우는 **cc-repository.md**와 동일합니다.

**참고**: `@workflows/cc-repository.md`

---

## 🔀 cc-repository vs cc-adapter

| 항목 | cc-repository | cc-adapter |
|------|---------------|-----------|
| **목적** | Repository Adapter 생성 | Repository Adapter 생성 (동일) |
| **대상** | Persistence Layer | Persistence Layer (동일) |
| **컨벤션** | RepositoryAdapterConventionTest | RepositoryAdapterConventionTest (동일) |
| **워크플로우** | 5-step (PRD → Adapter → Mapper → Validation → Results) | 동일 |

**결론**: cc-adapter는 cc-repository의 별칭(alias)입니다.

---

## 🚀 빠른 시작

### 사용 예시 (Windsurf Cascade)

```
사용자: "Tenant Repository Adapter를 생성해줘"

Cascade:
1. @workflows/cc-repository.md 참고
2. TenantPersistenceAdapter.java 생성
3. TenantEntityMapper.java 생성
4. ArchUnit 자동 검증
```

### 또는

```
사용자: "Order Adapter를 만들어줘"

Cascade:
1. @workflows/cc-repository.md 참고
2. OrderPersistenceAdapter.java 생성
3. OrderEntityMapper.java 생성
4. ArchUnit 자동 검증
```

---

## 📚 상세 문서

**전체 워크플로우**: `@workflows/cc-repository.md`

**주요 내용**:
- STEP 1: PRD 분석 (Aggregate, Port, CQRS)
- STEP 2: Repository Adapter 생성 (템플릿)
- STEP 3: Mapper 생성
- STEP 4: ArchUnit 자동 검증
- STEP 5: 검증 결과 출력

**고급 옵션**:
- CQRS Separation (Command/Query 분리)
- QueryDSL 통합 (동적 쿼리)
- Soft Delete 지원

---

**✅ 이 워크플로우는 cc-repository.md의 별칭입니다. 상세 내용은 cc-repository.md를 참고하세요.**
