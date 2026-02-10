# RefundPolicy API Flow Analysis

환불정책 도메인의 전체 API 호출 흐름을 Hexagonal 아키텍처 레이어별로 추적한 문서입니다.

---

## 목차

- [1. GET /refund-policies - 환불정책 목록 조회](#1-get-refund-policies---환불정책-목록-조회)
- [2. POST /refund-policies - 환불정책 등록](#2-post-refund-policies---환불정책-등록)
- [3. PUT /refund-policies/{policyId} - 환불정책 수정](#3-put-refund-policiespolicyid---환불정책-수정)
- [4. PATCH /refund-policies/status - 환불정책 상태 변경](#4-patch-refund-policiesstatus---환불정책-상태-변경)
- [Appendix: 핵심 컴포넌트](#appendix-핵심-컴포넌트)

---

## 1. GET /refund-policies - 환불정책 목록 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | `/v2/admin/sellers/{sellerId}/refund-policies` |
| Controller | RefundPolicyQueryController |
| Method | search |
| UseCase | SearchRefundPolicyUseCase |
| Service | SearchRefundPolicyService |
| 트랜잭션 | ReadOnly |

---

### 호출 흐름 다이어그램

```
[Adapter-In Layer]
RefundPolicyQueryController.search(sellerId, SearchRefundPoliciesPageApiRequest)
  │
  ├─ RefundPolicyQueryApiMapper.toSearchParams(sellerId, request)
  │   └─> RefundPolicySearchParams
  │
  ├─ SearchRefundPolicyUseCase.execute(params)              [Port]
  │   │
  │   └─ SearchRefundPolicyService.execute(params)          [Impl]
  │       │
  │       ├─ RefundPolicyQueryFactory.createCriteria(params)
  │       │   └─> RefundPolicySearchCriteria
  │       │
  │       ├─ RefundPolicyReadManager.findByCriteria(criteria)
  │       │   └─ RefundPolicyQueryPort.findByCriteria(criteria)  [Port]
  │       │       │
  │       │       └─ RefundPolicyQueryAdapter.findByCriteria()   [Impl]
  │       │           ├─ RefundPolicyQueryDslRepository.findByCriteria()
  │       │           │   └─ JPAQueryFactory (QueryDSL)
  │       │           │       └─ WHERE sellerId = ? AND deleted_at IS NULL
  │       │           │       └─ ORDER BY created_at DESC
  │       │           │       └─ OFFSET ? LIMIT ?
  │       │           └─ RefundPolicyJpaEntityMapper.toDomain()
  │       │               └─> List<RefundPolicy>
  │       │
  │       ├─ RefundPolicyReadManager.countByCriteria(criteria)
  │       │   └─ RefundPolicyQueryPort.countByCriteria(criteria)
  │       │       └─ RefundPolicyQueryAdapter.countByCriteria()
  │       │           └─ RefundPolicyQueryDslRepository.countByCriteria()
  │       │               └─ SELECT COUNT(*) WHERE sellerId = ? AND deleted_at IS NULL
  │       │
  │       └─ RefundPolicyAssembler.toPageResult(domains, page, size, totalElements)
  │           └─> RefundPolicyPageResult
  │
  └─ RefundPolicyQueryApiMapper.toPageResponse(pageResult)
      └─> PageApiResponse<RefundPolicyApiResponse>
```

---

### Layer별 상세

#### Adapter-In (REST API)

**Request DTO**: `SearchRefundPoliciesPageApiRequest`
```java
record SearchRefundPoliciesPageApiRequest(
    Integer page,           // default: 0
    Integer size,           // default: 20
    String sortKey,         // CREATED_AT, POLICY_NAME, RETURN_PERIOD_DAYS
    String sortDirection    // ASC, DESC
)
```

**Response DTO**: `RefundPolicyApiResponse`
```java
record RefundPolicyApiResponse(
    Long policyId,
    String policyName,
    Boolean defaultPolicy,
    Boolean active,
    Integer returnPeriodDays,
    Integer exchangePeriodDays,
    List<NonReturnableConditionApiResponse> nonReturnableConditions,
    String createdAt        // ISO-8601 format
)
```

**Controller**:
- `RefundPolicyQueryController.search()`
- PathVariable: `sellerId`
- Validation: `@Valid`
- Response: `ApiResponse<PageApiResponse<RefundPolicyApiResponse>>`

**Mapper**:
- `RefundPolicyQueryApiMapper`
- `toSearchParams()`: API Request → Application Params
- `toPageResponse()`: Application PageResult → API PageResponse

---

#### Application Layer

**UseCase Port**: `SearchRefundPolicyUseCase`
```java
RefundPolicyPageResult execute(RefundPolicySearchParams params)
```

**Service**: `SearchRefundPolicyService`
- 역할: 조회 로직 조율
- 의존성:
  - `RefundPolicyReadManager`: Domain 조회
  - `RefundPolicyQueryFactory`: Params → Criteria 변환
  - `RefundPolicyAssembler`: Domain → Result 변환

**DTO 변환 흐름**:
```
SearchRefundPoliciesPageApiRequest (Adapter-In)
  → RefundPolicySearchParams (Application)
  → RefundPolicySearchCriteria (Domain)
```

**Factory**: `RefundPolicyQueryFactory`
- `createCriteria()`: SearchParams → SearchCriteria
- SortKey 해석 (String → Enum)
- PageRequest 생성

**Assembler**: `RefundPolicyAssembler`
- `toPageResult()`: Domain List → PageResult
- `toResult()`: RefundPolicy → RefundPolicyResult

---

#### Domain Layer

**Port**: `RefundPolicyQueryPort`
```java
List<RefundPolicy> findByCriteria(RefundPolicySearchCriteria criteria)
long countByCriteria(RefundPolicySearchCriteria criteria)
```

**SearchCriteria**:
```java
record RefundPolicySearchCriteria(
    SellerId sellerId,
    QueryContext<RefundPolicySortKey> queryContext
)
```

**SortKey Enum**:
- `CREATED_AT` (default)
- `POLICY_NAME`
- `RETURN_PERIOD_DAYS`

**Aggregate**: `RefundPolicy`
- Immutable Value Objects: `RefundPolicyId`, `SellerId`, `RefundPolicyName`
- 비즈니스 규칙: POL-DEF-001, POL-DEF-002, POL-DEACT-001, POL-DEACT-002

---

#### Adapter-Out (Persistence)

**Adapter**: `RefundPolicyQueryAdapter`
- Port 구현: `RefundPolicyQueryPort`
- Repository: `RefundPolicyQueryDslRepository`
- Mapper: `RefundPolicyJpaEntityMapper`

**QueryDSL Repository**:
```java
public List<RefundPolicyJpaEntity> findByCriteria(RefundPolicySearchCriteria criteria) {
    return queryFactory
        .selectFrom(refundPolicyJpaEntity)
        .where(
            conditionBuilder.sellerIdEq(criteria.sellerIdValue()),
            conditionBuilder.notDeleted()
        )
        .orderBy(createOrderSpecifier(qc.sortKey(), qc.sortDirection()))
        .offset(criteria.offset())
        .limit(criteria.size())
        .fetch();
}
```

**ConditionBuilder**: `RefundPolicyConditionBuilder`
- `sellerIdEq()`: sellerId 조건
- `notDeleted()`: deletedAt IS NULL
- `idEq()`, `idIn()`: ID 조건
- `defaultPolicyEq()`: 기본 정책 필터
- `activeEq()`: 활성 상태 필터

**Entity**: `RefundPolicyJpaEntity`
- Table: `refund_policy`
- 상속: `SoftDeletableEntity` → `BaseAuditEntity`
- 필드: id, seller_id, policy_name, default_policy, active, return_period_days, exchange_period_days, non_returnable_conditions (JSON), partial_refund_enabled, inspection_required, inspection_period_days, additional_info

---

### Database Query 분석

**대상 테이블**: `refund_policy`

**SELECT 쿼리**:
```sql
-- findByCriteria
SELECT *
FROM refund_policy
WHERE seller_id = ?
  AND deleted_at IS NULL
ORDER BY created_at DESC  -- 또는 sortKey에 따라 변경
LIMIT ? OFFSET ?;

-- countByCriteria
SELECT COUNT(*)
FROM refund_policy
WHERE seller_id = ?
  AND deleted_at IS NULL;
```

**인덱스 활용**:
- `seller_id` + `deleted_at`: 복합 인덱스 필요
- `created_at`, `policy_name`, `return_period_days`: 정렬 인덱스

---

## 2. POST /refund-policies - 환불정책 등록

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | `/v2/admin/sellers/{sellerId}/refund-policies` |
| Controller | RefundPolicyCommandController |
| Method | register |
| UseCase | RegisterRefundPolicyUseCase |
| Service | RegisterRefundPolicyService |
| 트랜잭션 | ReadWrite (@Transactional) |

---

### 호출 흐름 다이어그램

```
[Adapter-In Layer]
RefundPolicyCommandController.register(sellerId, RegisterRefundPolicyApiRequest)
  │
  ├─ RefundPolicyCommandApiMapper.toCommand(sellerId, request)
  │   └─> RegisterRefundPolicyCommand
  │
  ├─ RegisterRefundPolicyUseCase.execute(command)              [Port]
  │   │
  │   └─ RegisterRefundPolicyService.execute(command)          [Impl]
  │       │                                                    [@Transactional]
  │       ├─ RefundPolicyCommandFactory.create(command)
  │       │   ├─ TimeProvider.now() → Instant
  │       │   └─ RefundPolicy.forNew(...)
  │       │       └─> RefundPolicy (Domain)
  │       │
  │       ├─ DefaultRefundPolicyResolver.resolveForRegistration()
  │       │   ├─ ReadManager.findDefaultBySellerId(sellerId)
  │       │   │   └─ QueryPort.findDefaultBySellerId()
  │       │   │       └─ QueryAdapter.findDefaultBySellerId()
  │       │   │           └─ QueryDslRepository.findDefaultBySellerId()
  │       │   │               └─ SELECT * WHERE seller_id = ? AND default_policy = true
  │       │   │
  │       │   ├─ Case 1: 새 정책이 기본 정책 + 기존 기본 정책 존재
  │       │   │   └─ oldDefault.unmarkDefault(timestamp)
  │       │   │       └─ CommandManager.persist(oldDefault)
  │       │   │
  │       │   └─ Case 2: 첫 번째 정책 등록 (기존 기본 정책 없음)
  │       │       └─ newPolicy.markAsDefault(timestamp)
  │       │
  │       └─ RefundPolicyCommandManager.persist(refundPolicy)
  │           └─ RefundPolicyCommandPort.persist(refundPolicy)   [Port]
  │               │
  │               └─ RefundPolicyCommandAdapter.persist()        [Impl]
  │                   ├─ RefundPolicyJpaEntityMapper.toEntity()
  │                   │   └─> RefundPolicyJpaEntity
  │                   └─ RefundPolicyJpaRepository.save(entity)
  │                       └─ INSERT INTO refund_policy
  │
  └─ ResponseEntity.status(201).body(new RegisterRefundPolicyApiResponse(createdId))
```

---

### Layer별 상세

#### Adapter-In (REST API)

**Request DTO**: `RegisterRefundPolicyApiRequest`
```java
record RegisterRefundPolicyApiRequest(
    @NotBlank String policyName,
    Boolean defaultPolicy,              // nullable
    @NotNull Integer returnPeriodDays,  // 1-90
    @NotNull Integer exchangePeriodDays,
    List<String> nonReturnableConditions,
    Boolean partialRefundEnabled,
    Boolean inspectionRequired,
    Integer inspectionPeriodDays,
    String additionalInfo
)
```

**Response DTO**: `RegisterRefundPolicyApiResponse`
```java
record RegisterRefundPolicyApiResponse(Long policyId)
```

**Controller**:
- `RefundPolicyCommandController.register()`
- PathVariable: `sellerId`
- Validation: `@Valid @RequestBody`
- Response: `201 CREATED`

---

#### Application Layer

**UseCase Port**: `RegisterRefundPolicyUseCase`
```java
Long execute(RegisterRefundPolicyCommand command)
```

**Service**: `RegisterRefundPolicyService`
- 트랜잭션 경계: `@Transactional`
- 역할: 등록 로직 + 기본 정책 해결
- 의존성:
  - `RefundPolicyCommandFactory`: Command → Domain
  - `RefundPolicyCommandManager`: 저장
  - `DefaultRefundPolicyResolver`: 기본 정책 규칙 처리

**Factory**: `RefundPolicyCommandFactory`
- `create()`: Command → RefundPolicy
- TimeProvider 사용: `timeProvider.now()`
- Boolean null 처리: `Boolean.TRUE.equals(command.defaultPolicy())`
- NonReturnableCondition 변환: String → Enum

**Internal**: `DefaultRefundPolicyResolver`
- 역할: 기본 정책 관련 비즈니스 규칙 처리
- `resolveForRegistration()`:
  1. 새 정책이 기본 정책이면 → 기존 기본 정책 해제
  2. 첫 번째 정책이면 → 자동으로 기본 정책 설정

---

#### Domain Layer

**Aggregate**: `RefundPolicy`
- 생성 메서드: `RefundPolicy.forNew()`
- 검증:
  - `validateReturnPeriod()`: 1-90일
  - `validateExchangePeriod()`: 1-90일
- 초기 상태: `active = true`

**비즈니스 규칙**:
- **POL-DEF-001**: 셀러당 기본 정책은 정확히 1개
- **POL-DEF-002**: 기본 정책은 활성화 상태여야 함

**Port**: `RefundPolicyCommandPort`
```java
Long persist(RefundPolicy refundPolicy)
```

---

#### Adapter-Out (Persistence)

**Adapter**: `RefundPolicyCommandAdapter`
- Port 구현: `RefundPolicyCommandPort`
- Repository: `RefundPolicyJpaRepository` (Spring Data JPA)

**JPA Repository**:
```java
public interface RefundPolicyJpaRepository extends JpaRepository<RefundPolicyJpaEntity, Long>
```

**Mapper**: `RefundPolicyJpaEntityMapper`
- `toEntity()`: Domain → Entity
- NonReturnableCondition → JSON String 변환

---

### Database Query 분석

**INSERT 쿼리**:
```sql
INSERT INTO refund_policy (
    seller_id,
    policy_name,
    default_policy,
    active,
    return_period_days,
    exchange_period_days,
    non_returnable_conditions,
    partial_refund_enabled,
    inspection_required,
    inspection_period_days,
    additional_info,
    created_at,
    updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
```

**기본 정책 조회 및 업데이트** (resolveForRegistration):
```sql
-- 기존 기본 정책 조회
SELECT *
FROM refund_policy
WHERE seller_id = ?
  AND default_policy = true
  AND deleted_at IS NULL;

-- 기존 기본 정책 해제
UPDATE refund_policy
SET default_policy = false,
    updated_at = ?
WHERE id = ?;
```

---

## 3. PUT /refund-policies/{policyId} - 환불정책 수정

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PUT |
| Path | `/v2/admin/sellers/{sellerId}/refund-policies/{policyId}` |
| Controller | RefundPolicyCommandController |
| Method | update |
| UseCase | UpdateRefundPolicyUseCase |
| Service | UpdateRefundPolicyService |
| 트랜잭션 | ReadWrite (@Transactional) |

---

### 호출 흐름 다이어그램

```
[Adapter-In Layer]
RefundPolicyCommandController.update(sellerId, policyId, UpdateRefundPolicyApiRequest)
  │
  ├─ RefundPolicyCommandApiMapper.toCommand(sellerId, policyId, request)
  │   └─> UpdateRefundPolicyCommand
  │
  ├─ UpdateRefundPolicyUseCase.execute(command)                [Port]
  │   │
  │   └─ UpdateRefundPolicyService.execute(command)            [Impl]
  │       │                                                    [@Transactional]
  │       ├─ RefundPolicyCommandFactory.createUpdateContext(command)
  │       │   ├─ TimeProvider.now() → Instant
  │       │   └─> UpdateContext<RefundPolicyId, RefundPolicyUpdateData>
  │       │
  │       ├─ RefundPolicyValidator.findExistingBySellerOrThrow(sellerId, policyId)
  │       │   └─ ReadManager.findBySellerIdAndId()
  │       │       └─ QueryPort.findBySellerIdAndId()
  │       │           └─ QueryAdapter.findBySellerIdAndId()
  │       │               └─ QueryDslRepository.findBySellerIdAndId()
  │       │                   └─ SELECT * WHERE seller_id = ? AND id = ? AND deleted_at IS NULL
  │       │                   └─> RefundPolicy (or throw RefundPolicyException)
  │       │
  │       ├─ DefaultRefundPolicyResolver.resolveForUpdate()
  │       │   ├─ Case 1: 기본 정책이 아니었는데 → 기본 정책으로 변경
  │       │   │   ├─ ReadManager.findDefaultBySellerId(sellerId)
  │       │   │   └─ oldDefault.unmarkDefault(timestamp)
  │       │   │       └─ CommandManager.persist(oldDefault)
  │       │   │
  │       │   └─ Case 2: 기본 정책이었는데 → 기본 정책 해제
  │       │       └─ throw CannotUnmarkOnlyDefaultRefundPolicyException
  │       │
  │       ├─ refundPolicy.update(updateData, changedAt)
  │       │   └─ Domain 내부 검증 + 필드 업데이트
  │       │
  │       └─ RefundPolicyCommandManager.persist(refundPolicy)
  │           └─ RefundPolicyCommandPort.persist(refundPolicy)
  │               └─ RefundPolicyCommandAdapter.persist()
  │                   └─ RefundPolicyJpaRepository.save(entity)
  │                       └─ UPDATE refund_policy SET ... WHERE id = ?
  │
  └─ ResponseEntity.noContent().build()  // 204 No Content
```

---

### Layer별 상세

#### Adapter-In (REST API)

**Request DTO**: `UpdateRefundPolicyApiRequest`
```java
record UpdateRefundPolicyApiRequest(
    @NotBlank String policyName,
    Boolean defaultPolicy,              // nullable (null이면 변경 안 함)
    @NotNull Integer returnPeriodDays,
    @NotNull Integer exchangePeriodDays,
    List<String> nonReturnableConditions,
    Boolean partialRefundEnabled,
    Boolean inspectionRequired,
    Integer inspectionPeriodDays,
    String additionalInfo
)
```

**Controller**:
- `RefundPolicyCommandController.update()`
- PathVariables: `sellerId`, `policyId`
- Response: `204 No Content`

**Mapper**:
- `toCommand()`: API Request + PathVariables → Command

---

#### Application Layer

**UseCase Port**: `UpdateRefundPolicyUseCase`
```java
void execute(UpdateRefundPolicyCommand command)
```

**Service**: `UpdateRefundPolicyService`
- 트랜잭션 경계: `@Transactional`
- 역할: 수정 로직 + 기본 정책 해결 + 검증
- 의존성:
  - `RefundPolicyCommandFactory`: Command → UpdateContext
  - `RefundPolicyCommandManager`: 저장
  - `RefundPolicyValidator`: 존재 여부 검증
  - `DefaultRefundPolicyResolver`: 기본 정책 규칙 처리

**Factory**: `RefundPolicyCommandFactory`
- `createUpdateContext()`: Command → UpdateContext
- UpdateContext 구성:
  - `RefundPolicyId`
  - `RefundPolicyUpdateData` (VO)
  - `Instant changedAt`

**Validator**: `RefundPolicyValidator`
- `findExistingBySellerOrThrow()`: 셀러 소유 정책인지 검증
- 실패 시: `RefundPolicyException.policyNotFound()`

**Internal**: `DefaultRefundPolicyResolver`
- `resolveForUpdate()`:
  1. `newDefaultPolicy == null` → 변경 안 함
  2. 기본 정책으로 변경 → 기존 기본 정책 해제
  3. 기본 정책 해제 → `CannotUnmarkOnlyDefaultRefundPolicyException`

---

#### Domain Layer

**UpdateData**: `RefundPolicyUpdateData`
```java
record RefundPolicyUpdateData(
    RefundPolicyName policyName,
    int returnPeriodDays,
    int exchangePeriodDays,
    List<NonReturnableCondition> nonReturnableConditions,
    boolean partialRefundEnabled,
    boolean inspectionRequired,
    int inspectionPeriodDays,
    String additionalInfo
)
```

**Aggregate**: `RefundPolicy`
- `update()`: UpdateData로 정책 수정
- 내부 검증:
  - `validateReturnPeriod()`
  - `validateExchangePeriod()`
- 필드 업데이트 + `updatedAt` 갱신

**비즈니스 규칙**:
- 유일한 기본 정책은 해제 불가

---

### Database Query 분석

**조회 쿼리**:
```sql
-- 정책 존재 여부 + 셀러 소유권 검증
SELECT *
FROM refund_policy
WHERE seller_id = ?
  AND id = ?
  AND deleted_at IS NULL;
```

**UPDATE 쿼리**:
```sql
-- 정책 정보 수정
UPDATE refund_policy
SET policy_name = ?,
    default_policy = ?,
    return_period_days = ?,
    exchange_period_days = ?,
    non_returnable_conditions = ?,
    partial_refund_enabled = ?,
    inspection_required = ?,
    inspection_period_days = ?,
    additional_info = ?,
    updated_at = ?
WHERE id = ?;
```

---

## 4. PATCH /refund-policies/status - 환불정책 상태 변경

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PATCH |
| Path | `/v2/admin/sellers/{sellerId}/refund-policies/status` |
| Controller | RefundPolicyCommandController |
| Method | changeStatus |
| UseCase | ChangeRefundPolicyStatusUseCase |
| Service | ChangeRefundPolicyStatusService |
| 트랜잭션 | ReadWrite (@Transactional) |

---

### 호출 흐름 다이어그램

```
[Adapter-In Layer]
RefundPolicyCommandController.changeStatus(sellerId, ChangeRefundPolicyStatusApiRequest)
  │
  ├─ RefundPolicyCommandApiMapper.toCommand(sellerId, request)
  │   └─> ChangeRefundPolicyStatusCommand
  │
  ├─ ChangeRefundPolicyStatusUseCase.execute(command)          [Port]
  │   │
  │   └─ ChangeRefundPolicyStatusService.execute(command)      [Impl]
  │       │                                                    [@Transactional]
  │       ├─ RefundPolicyCommandFactory.createStatusChangeContexts(command)
  │       │   ├─ TimeProvider.now() → Instant
  │       │   └─> List<StatusChangeContext<RefundPolicyId>>
  │       │
  │       ├─ RefundPolicyValidator.findAllExistingOrThrow(ids)
  │       │   └─ ReadManager.getByIds(ids)
  │       │       └─ QueryPort.findByIds(ids)
  │       │           └─ QueryAdapter.findByIds()
  │       │               └─ QueryDslRepository.findByIds()
  │       │                   └─ SELECT * WHERE id IN (?, ?, ...) AND deleted_at IS NULL
  │       │                   └─> List<RefundPolicy> (or throw if any missing)
  │       │
  │       ├─ Case 1: active = true (활성화)
  │       │   └─ activateAll(policies, changedAt)
  │       │       └─ for each: policy.activate(changedAt)
  │       │
  │       └─ Case 2: active = false (비활성화)
  │           ├─ RefundPolicyValidator.validateNotLastActivePolicy(sellerId, policies)
  │           │   ├─ activePoliciesInRequest = policies.stream().filter(isActive).count()
  │           │   ├─ ReadManager.countActiveBySellerId(sellerId)
  │           │   │   └─ QueryPort.countActiveBySellerId()
  │           │   │       └─ QueryAdapter.countActiveBySellerId()
  │           │   │           └─ SELECT COUNT(*) WHERE seller_id = ? AND active = true
  │           │   │
  │           │   └─ if (totalActiveCount <= activePoliciesInRequest)
  │           │       throw LastActiveRefundPolicyCannotBeDeactivatedException
  │           │
  │           └─ deactivateAll(policies, changedAt)
  │               └─ for each: policy.deactivate(changedAt)
  │                   └─ if (policy.isDefaultPolicy())
  │                       throw CannotDeactivateDefaultRefundPolicyException
  │
  └─ RefundPolicyCommandManager.persistAll(policies)
      └─ RefundPolicyCommandPort.persistAll(policies)
          └─ RefundPolicyCommandAdapter.persistAll()
              └─ RefundPolicyJpaRepository.saveAll(entities)
                  └─ UPDATE refund_policy SET active = ?, updated_at = ? WHERE id IN (...)
```

---

### Layer별 상세

#### Adapter-In (REST API)

**Request DTO**: `ChangeRefundPolicyStatusApiRequest`
```java
record ChangeRefundPolicyStatusApiRequest(
    @NotEmpty List<Long> policyIds,
    @NotNull Boolean active
)
```

**Controller**:
- `RefundPolicyCommandController.changeStatus()`
- PathVariable: `sellerId`
- Response: `204 No Content`

---

#### Application Layer

**UseCase Port**: `ChangeRefundPolicyStatusUseCase`
```java
void execute(ChangeRefundPolicyStatusCommand command)
```

**Service**: `ChangeRefundPolicyStatusService`
- 트랜잭션 경계: `@Transactional`
- 역할: 다건 상태 변경 + 검증
- 의존성:
  - `RefundPolicyCommandFactory`: Command → StatusChangeContexts
  - `RefundPolicyCommandManager`: 다건 저장
  - `RefundPolicyValidator`: 존재 여부 + 마지막 활성 정책 검증

**Factory**: `RefundPolicyCommandFactory`
- `createStatusChangeContexts()`: Command → List<StatusChangeContext>
- StatusChangeContext 구성:
  - `RefundPolicyId`
  - `Instant changedAt`

**Validator**: `RefundPolicyValidator`
- `findAllExistingOrThrow()`: ID 목록으로 조회 + 검증
  - 최적화: Map 생성 후 단일 순회로 검증 + 순서 보장
- `validateNotLastActivePolicy()`: 마지막 활성 정책 비활성화 방지
  - 비활성화 대상 중 활성 정책 개수 카운트
  - 전체 활성 정책 개수 조회
  - `totalActiveCount <= activePoliciesInRequest` → 예외

---

#### Domain Layer

**Aggregate**: `RefundPolicy`
- `activate()`: 활성화 (제약 없음)
- `deactivate()`: 비활성화
  - **POL-DEACT-001**: 기본 정책이면 예외
  - 비활성화 후 `updatedAt` 갱신

**비즈니스 규칙**:
- **POL-DEACT-001**: 기본 정책은 비활성화 불가
- **POL-DEACT-002**: 마지막 활성 정책은 비활성화 불가

**Port**: `RefundPolicyCommandPort`
```java
void persistAll(List<RefundPolicy> refundPolicies)
```

---

### Database Query 분석

**조회 쿼리**:
```sql
-- 다건 조회
SELECT *
FROM refund_policy
WHERE id IN (?, ?, ...)
  AND deleted_at IS NULL;

-- 활성 정책 개수 조회 (비활성화 검증용)
SELECT COUNT(*)
FROM refund_policy
WHERE seller_id = ?
  AND active = true
  AND deleted_at IS NULL;
```

**UPDATE 쿼리**:
```sql
-- 다건 상태 변경
UPDATE refund_policy
SET active = ?,
    updated_at = ?
WHERE id = ?;
-- (각 ID마다 반복 실행)
```

---

## Appendix: 핵심 컴포넌트

### 1. Manager 패턴

**RefundPolicyReadManager**:
- 역할: Query Port 래핑 + 도메인 예외 변환
- 주요 메서드:
  - `getById()`: ID로 조회 (없으면 예외)
  - `findBySellerIdAndId()`: 셀러 소유권 검증
  - `findDefaultBySellerId()`: 기본 정책 조회
  - `countActiveBySellerId()`: 활성 정책 개수

**RefundPolicyCommandManager**:
- 역할: Command Port 래핑
- 주요 메서드:
  - `persist()`: 단건 저장
  - `persistAll()`: 다건 저장

---

### 2. Factory 패턴

**RefundPolicyQueryFactory**:
- 역할: SearchParams → SearchCriteria 변환
- SortKey 해석 (String → Enum)
- PageRequest 생성

**RefundPolicyCommandFactory**:
- 역할: Command → Domain 변환
- **APP-TIM-001**: TimeProvider 유일한 사용처
- Boolean null 처리
- NonReturnableCondition 변환 (String → Enum)

---

### 3. Validator 패턴

**RefundPolicyValidator**:
- 역할: 도메인 존재 여부 검증 + 비즈니스 규칙 검증
- **APP-VAL-001**: 검증 성공 시 Domain 객체 반환
- 주요 메서드:
  - `findExistingOrThrow()`: 단건 조회 + 검증
  - `findExistingBySellerOrThrow()`: 셀러 소유권 검증
  - `findAllExistingOrThrow()`: 다건 조회 + 검증
  - `validateNotLastActivePolicy()`: 마지막 활성 정책 검증

---

### 4. Resolver 패턴

**DefaultRefundPolicyResolver**:
- 역할: 기본 정책 관련 복잡한 비즈니스 규칙 처리
- 주요 메서드:
  - `resolveForRegistration()`: 등록 시 기본 정책 해결
    - 새 정책이 기본 정책이면 기존 기본 정책 해제
    - 첫 번째 정책이면 자동으로 기본 정책 설정
  - `resolveForUpdate()`: 수정 시 기본 정책 해결
    - 기본 정책으로 변경 시 기존 기본 정책 해제
    - 기본 정책 해제 시도 시 예외 발생

---

### 5. Assembler 패턴

**RefundPolicyAssembler**:
- 역할: Domain → Result 변환
- **APP-ASM-001**: 도메인별 구체 Result 클래스 사용
- 주요 메서드:
  - `toResult()`: Domain → Result
  - `toResults()`: Domain List → Result List
  - `toPageResult()`: Domain List → PageResult

---

### 6. DTO 변환 흐름

#### Query (조회)
```
SearchRefundPoliciesPageApiRequest (Adapter-In)
  → RefundPolicySearchParams (Application)
  → RefundPolicySearchCriteria (Domain)
  → QueryDSL → RefundPolicyJpaEntity (Persistence)
  → RefundPolicy (Domain)
  → RefundPolicyResult (Application)
  → RefundPolicyApiResponse (Adapter-In)
```

#### Command (등록/수정)
```
RegisterRefundPolicyApiRequest (Adapter-In)
  → RegisterRefundPolicyCommand (Application)
  → RefundPolicy (Domain)
  → RefundPolicyJpaEntity (Persistence)
  → Database
```

---

### 7. 트랜잭션 경계

- **Query (ReadOnly)**: Manager에서 `@Transactional(readOnly = true)`
- **Command (ReadWrite)**: Service에서 `@Transactional`
- **Adapter**: @Transactional 금지 (PER-ADP-002)

---

### 8. 예외 처리

**Domain Exception**:
- `RefundPolicyException`: 정책 조회 실패
- `InvalidReturnPeriodException`: 반품 기간 검증 실패
- `InvalidExchangePeriodException`: 교환 기간 검증 실패
- `CannotDeactivateDefaultRefundPolicyException`: 기본 정책 비활성화 시도
- `LastActiveRefundPolicyCannotBeDeactivatedException`: 마지막 활성 정책 비활성화 시도
- `CannotUnmarkOnlyDefaultRefundPolicyException`: 유일한 기본 정책 해제 시도

**API Error Handling**:
- `RefundPolicyErrorMapper`: Domain Exception → ErrorCode 매핑
- `GlobalExceptionHandler`: 전역 예외 처리

---

### 9. 비즈니스 규칙 요약

| 규칙 코드 | 설명 | 검증 위치 |
|-----------|------|----------|
| POL-DEF-001 | 셀러당 기본 정책은 정확히 1개 | DefaultRefundPolicyResolver |
| POL-DEF-002 | 기본 정책은 활성화 상태여야 함 | DefaultRefundPolicyResolver |
| POL-DEACT-001 | 기본 정책은 비활성화 불가 | RefundPolicy.deactivate() |
| POL-DEACT-002 | 마지막 활성 정책은 비활성화 불가 | RefundPolicyValidator |

---

### 10. 주요 쿼리 패턴

**단건 조회**:
- `findById()`: ID로 조회
- `findBySellerIdAndId()`: 셀러 소유권 검증
- `findDefaultBySellerId()`: 기본 정책 조회

**다건 조회**:
- `findByIds()`: ID 목록으로 조회
- `findByCriteria()`: 검색 조건으로 조회 (페이징)

**집계 쿼리**:
- `countByCriteria()`: 검색 조건으로 개수 조회
- `countActiveBySellerId()`: 활성 정책 개수 조회

**CUD**:
- `persist()`: 단건 저장 (INSERT/UPDATE)
- `persistAll()`: 다건 저장

---

## 문서 정보

- **작성일**: 2026-02-06
- **대상 버전**: SPRING_BOOT 3.5.x + JAVA 21
- **아키텍처**: Hexagonal Multi-Module
- **작성 도구**: Claude Code (API Flow Analyzer)
