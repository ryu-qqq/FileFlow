# ShippingPolicy API 호출 흐름 분석

배송정책(ShippingPolicy) 도메인의 전체 API 호출 흐름을 Hexagonal 아키텍처 레이어별로 추적한 문서입니다.

---

## 목차

1. [GET /shipping-policies - 배송정책 목록 조회](#1-get-shipping-policies---배송정책-목록-조회)
2. [POST /shipping-policies - 배송정책 등록](#2-post-shipping-policies---배송정책-등록)
3. [PUT /shipping-policies/{policyId} - 배송정책 수정](#3-put-shipping-policiespolicyid---배송정책-수정)
4. [PATCH /shipping-policies/status - 배송정책 상태 변경](#4-patch-shipping-policiesstatus---배송정책-상태-변경)

---

## 1. GET /shipping-policies - 배송정책 목록 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | `/v2/admin/sellers/{sellerId}/shipping-policies` |
| Controller | ShippingPolicyQueryController |
| Method | search |
| UseCase | SearchShippingPolicyUseCase |
| Service | SearchShippingPolicyService |

---

### 호출 흐름 다이어그램

```
ShippingPolicyQueryController.search(SearchShippingPoliciesPageApiRequest)
  ├─ ShippingPolicyQueryApiMapper.toSearchParams(sellerId, request)
  │   └─→ ShippingPolicySearchParams
  │
  ├─ SearchShippingPolicyUseCase.execute(params)                    [Port-In]
  │   └─ SearchShippingPolicyService.execute(params)                [Service]
  │       ├─ ShippingPolicyQueryFactory.createCriteria(params)
  │       │   └─→ ShippingPolicySearchCriteria
  │       │
  │       ├─ ShippingPolicyReadManager.findByCriteria(criteria)
  │       │   └─ ShippingPolicyQueryPort.findByCriteria(criteria)   [Port-Out]
  │       │       └─ ShippingPolicyQueryAdapter.findByCriteria()    [Adapter]
  │       │           ├─ ShippingPolicyQueryDslRepository.findByCriteria()
  │       │           │   └─ QueryDSL: WHERE seller_id = ?, ORDER BY, LIMIT, OFFSET
  │       │           └─ ShippingPolicyJpaEntityMapper.toDomain()
  │       │               └─→ List<ShippingPolicy>
  │       │
  │       ├─ ShippingPolicyReadManager.countByCriteria(criteria)
  │       │   └─ ShippingPolicyQueryPort.countByCriteria(criteria)
  │       │       └─ ShippingPolicyQueryAdapter.countByCriteria()
  │       │           └─ ShippingPolicyQueryDslRepository.countByCriteria()
  │       │               └─ QueryDSL: SELECT COUNT(*) WHERE seller_id = ?
  │       │
  │       └─ ShippingPolicyAssembler.toPageResult(domains, page, size, totalElements)
  │           └─→ ShippingPolicyPageResult
  │
  └─ ShippingPolicyQueryApiMapper.toPageResponse(pageResult)
      └─→ PageApiResponse<ShippingPolicyApiResponse>
```

---

### Layer별 상세

#### Adapter-In (REST API)

- **Controller**: `ShippingPolicyQueryController`
- **Method**: `search(Long sellerId, SearchShippingPoliciesPageApiRequest request)`
- **Request DTO**: `SearchShippingPoliciesPageApiRequest`
  - `Integer page` (default: 0)
  - `Integer size` (default: 20)
  - `String sortKey` (CREATED_AT, POLICY_NAME, BASE_FEE)
  - `String sortDirection` (ASC, DESC)
- **Response DTO**: `ShippingPolicyApiResponse`
  - `Long policyId`
  - `String policyName`
  - `Boolean defaultPolicy`
  - `Boolean active`
  - `String shippingFeeType`
  - `String shippingFeeTypeDisplayName`
  - `Integer baseFee`
  - `Integer freeThreshold`
  - `String createdAt` (ISO-8601)

**Mapper 역할**:
- `ShippingPolicyQueryApiMapper.toSearchParams()`: API Request → Application Params
- `ShippingPolicyQueryApiMapper.toPageResponse()`: Application Result → API Response

---

#### Application Layer

**UseCase (Port-In)**:
- Interface: `SearchShippingPolicyUseCase`
- Method: `ShippingPolicyPageResult execute(ShippingPolicySearchParams)`

**Service 구현체**:
- Class: `SearchShippingPolicyService`
- 의존성:
  - `ShippingPolicyReadManager`: Domain 조회 및 카운트
  - `ShippingPolicyQueryFactory`: Params → Criteria 변환
  - `ShippingPolicyAssembler`: Domain → Result 변환

**DTO**:
- `ShippingPolicySearchParams`: sellerId, CommonSearchParams
- `ShippingPolicyPageResult`: List<ShippingPolicyResult>, PageMeta

**트랜잭션 경계**: `@Transactional(readOnly = true)` (Manager 레벨)

---

#### Domain Layer

**Domain Port (Query)**:
- Interface: `ShippingPolicyQueryPort`
- Methods:
  - `List<ShippingPolicy> findByCriteria(ShippingPolicySearchCriteria)`
  - `long countByCriteria(ShippingPolicySearchCriteria)`

**Criteria**:
- `ShippingPolicySearchCriteria`: SellerId + QueryContext (정렬, 페이징)

**Aggregate**:
- `ShippingPolicy`: 배송 정책 도메인 객체

---

#### Adapter-Out (Persistence)

**Adapter 구현체**:
- Class: `ShippingPolicyQueryAdapter`
- 역할: Port-Out 구현, QueryDsl Repository 위임

**Repository**:
- `ShippingPolicyQueryDslRepository.findByCriteria()`
  - QueryDSL 쿼리: `WHERE seller_id = ? AND deleted_at IS NULL`
  - ORDER BY: `created_at DESC / policy_name ASC / base_fee DESC`
  - LIMIT, OFFSET 적용
- `ShippingPolicyQueryDslRepository.countByCriteria()`
  - QueryDSL 쿼리: `SELECT COUNT(*) WHERE seller_id = ? AND deleted_at IS NULL`

**Mapper**:
- `ShippingPolicyJpaEntityMapper.toDomain()`: Entity → Domain

**JPA Entity**:
- `ShippingPolicyJpaEntity`: @Table("shipping_policy")

---

### Database Query 분석

**대상 테이블**: `shipping_policy`

**조회 쿼리**:
```sql
SELECT *
FROM shipping_policy
WHERE seller_id = :sellerId
  AND deleted_at IS NULL
ORDER BY created_at DESC  -- or policy_name, base_fee
LIMIT :size OFFSET :offset
```

**카운트 쿼리**:
```sql
SELECT COUNT(*)
FROM shipping_policy
WHERE seller_id = :sellerId
  AND deleted_at IS NULL
```

**인덱스 고려사항**:
- `seller_id` (WHERE)
- `deleted_at` (Soft Delete 필터)
- `created_at`, `policy_name`, `base_fee` (ORDER BY)

---

## 2. POST /shipping-policies - 배송정책 등록

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | `/v2/admin/sellers/{sellerId}/shipping-policies` |
| Controller | ShippingPolicyCommandController |
| Method | register |
| UseCase | RegisterShippingPolicyUseCase |
| Service | RegisterShippingPolicyService |

---

### 호출 흐름 다이어그램

```
ShippingPolicyCommandController.register(RegisterShippingPolicyApiRequest)
  ├─ ShippingPolicyCommandApiMapper.toCommand(sellerId, request)
  │   └─→ RegisterShippingPolicyCommand
  │
  ├─ RegisterShippingPolicyUseCase.execute(command)                      [Port-In]
  │   └─ RegisterShippingPolicyService.execute(command)                  [Service]
  │       ├─ ShippingPolicyCommandFactory.create(command)
  │       │   ├─ TimeProvider.now() → Instant
  │       │   └─ ShippingPolicy.forNew(..., now)
  │       │       ├─ 검증: ShippingFeeType이 CONDITIONAL_FREE이면 freeThreshold 필수
  │       │       └─→ ShippingPolicy (new, active=true)
  │       │
  │       ├─ DefaultShippingPolicyResolver.resolveForRegistration(sellerId, policy, now)
  │       │   ├─ IF policy.isDefaultPolicy() → 기존 기본 정책 해제
  │       │   │   └─ ShippingPolicyReadManager.findDefaultBySellerId(sellerId)
  │       │   │       └─ existingDefault.unmarkDefault(now)
  │       │   │
  │       │   └─ ELSE IF 첫 번째 정책 → 자동으로 기본 정책 설정
  │       │       └─ policy.markAsDefault(now)
  │       │
  │       └─ ShippingPolicyCommandManager.persist(policy)
  │           └─ ShippingPolicyCommandPort.persist(policy)                [Port-Out]
  │               └─ ShippingPolicyCommandAdapter.persist(policy)         [Adapter]
  │                   ├─ ShippingPolicyJpaEntityMapper.toEntity(policy)
  │                   └─ ShippingPolicyJpaRepository.save(entity)
  │                       └─ INSERT INTO shipping_policy
  │                           └─→ Long (생성된 ID)
  │
  └─→ RegisterShippingPolicyApiResponse(createdId)
```

---

### Layer별 상세

#### Adapter-In (REST API)

- **Controller**: `ShippingPolicyCommandController`
- **Method**: `register(Long sellerId, RegisterShippingPolicyApiRequest request)`
- **Request DTO**: `RegisterShippingPolicyApiRequest`
  - `String policyName` (필수)
  - `Boolean defaultPolicy`
  - `String shippingFeeType` (FREE, PAID, CONDITIONAL_FREE, QUANTITY_BASED)
  - `Integer baseFee`
  - `Integer freeThreshold`
  - `Integer jejuExtraFee`
  - `Integer islandExtraFee`
  - `Integer returnFee`
  - `Integer exchangeFee`
  - `LeadTimeApiRequest leadTime`
    - `Integer minDays`
    - `Integer maxDays`
    - `LocalTime cutoffTime`
- **Response DTO**: `RegisterShippingPolicyApiResponse`
  - `Long policyId`
- **HTTP Status**: `201 CREATED`

**Mapper 역할**:
- `ShippingPolicyCommandApiMapper.toCommand()`: API Request → Application Command

---

#### Application Layer

**UseCase (Port-In)**:
- Interface: `RegisterShippingPolicyUseCase`
- Method: `Long execute(RegisterShippingPolicyCommand)`

**Service 구현체**:
- Class: `RegisterShippingPolicyService`
- 의존성:
  - `ShippingPolicyCommandFactory`: Command → Domain 생성
  - `ShippingPolicyCommandManager`: Domain 저장
  - `DefaultShippingPolicyResolver`: 기본 정책 로직 처리

**Command DTO**:
- `RegisterShippingPolicyCommand`: 모든 필드 포함
- `LeadTimeCommand`: minDays, maxDays, cutoffTime

**비즈니스 로직**:
1. Factory를 통해 ShippingPolicy 도메인 생성 (TimeProvider로 현재 시간 주입)
2. DefaultShippingPolicyResolver가 기본 정책 로직 처리:
   - 기본 정책으로 등록 시 → 기존 기본 정책 해제
   - 첫 번째 정책 등록 시 → 자동으로 기본 정책 설정
3. Manager를 통해 저장

**트랜잭션 경계**: `@Transactional` (Service 레벨)

---

#### Domain Layer

**Domain 생성**:
- `ShippingPolicy.forNew()`: Static Factory Method
  - 검증: `CONDITIONAL_FREE`인 경우 `freeThreshold` 필수
  - 생성 시 `active = true` (자동 활성화)
  - `id = ShippingPolicyId.forNew()` (새 ID)

**Domain Port (Command)**:
- Interface: `ShippingPolicyCommandPort`
- Method: `Long persist(ShippingPolicy)`

**비즈니스 규칙**:
- **POL-DEF-001**: 셀러당 기본 정책은 정확히 1개만 존재
- **POL-DEF-002**: 기본 정책은 활성화 상태여야 함

---

#### Adapter-Out (Persistence)

**Adapter 구현체**:
- Class: `ShippingPolicyCommandAdapter`
- 역할: Port-Out 구현, JPA Repository 위임

**Repository**:
- `ShippingPolicyJpaRepository.save(entity)`
  - Spring Data JPA save()
  - INSERT 실행

**Mapper**:
- `ShippingPolicyJpaEntityMapper.toEntity()`: Domain → Entity

---

### Database Query 분석

**대상 테이블**: `shipping_policy`

**등록 쿼리**:
```sql
INSERT INTO shipping_policy (
  seller_id,
  policy_name,
  default_policy,
  active,
  shipping_fee_type,
  base_fee,
  free_threshold,
  jeju_extra_fee,
  island_extra_fee,
  return_fee,
  exchange_fee,
  lead_time_min_days,
  lead_time_max_days,
  lead_time_cutoff_time,
  created_at,
  updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```

**기본 정책 조회 (기존 정책 해제용)**:
```sql
SELECT *
FROM shipping_policy
WHERE seller_id = :sellerId
  AND default_policy = TRUE
  AND deleted_at IS NULL
```

**기존 기본 정책 해제**:
```sql
UPDATE shipping_policy
SET default_policy = FALSE,
    updated_at = :now
WHERE id = :existingDefaultId
```

---

## 3. PUT /shipping-policies/{policyId} - 배송정책 수정

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PUT |
| Path | `/v2/admin/sellers/{sellerId}/shipping-policies/{policyId}` |
| Controller | ShippingPolicyCommandController |
| Method | update |
| UseCase | UpdateShippingPolicyUseCase |
| Service | UpdateShippingPolicyService |

---

### 호출 흐름 다이어그램

```
ShippingPolicyCommandController.update(UpdateShippingPolicyApiRequest)
  ├─ ShippingPolicyCommandApiMapper.toCommand(sellerId, policyId, request)
  │   └─→ UpdateShippingPolicyCommand
  │
  ├─ UpdateShippingPolicyUseCase.execute(command)                        [Port-In]
  │   └─ UpdateShippingPolicyService.execute(command)                    [Service]
  │       ├─ ShippingPolicyCommandFactory.createUpdateContext(command)
  │       │   ├─ TimeProvider.now() → Instant
  │       │   └─→ UpdateContext<ShippingPolicyId, ShippingPolicyUpdateData>
  │       │
  │       ├─ ShippingPolicyValidator.findExistingBySellerOrThrow(sellerId, policyId)
  │       │   └─ ShippingPolicyReadManager.findBySellerIdAndId(sellerId, policyId)
  │       │       └─ ShippingPolicyQueryPort.findBySellerIdAndId()
  │       │           └─ ShippingPolicyQueryAdapter.findBySellerIdAndId()
  │       │               └─ QueryDSL: WHERE seller_id = ? AND id = ?
  │       │                   └─→ ShippingPolicy (기존 정책)
  │       │
  │       ├─ DefaultShippingPolicyResolver.resolveForUpdate(sellerId, policy, newDefaultFlag, now)
  │       │   ├─ IF 기본 정책 아님 → 기본 정책으로 변경
  │       │   │   └─ 기존 기본 정책 해제 + 현재 정책 기본 설정
  │       │   └─ ELSE IF 기본 정책 → 기본 해제 시도
  │       │       └─ 검증: 유일한 기본 정책이면 해제 불가 (Exception)
  │       │
  │       ├─ shippingPolicy.update(updateData, changedAt)
  │       │   └─ Domain 내부 필드 업데이트 + 검증 (freeThreshold)
  │       │
  │       └─ ShippingPolicyCommandManager.persist(policy)
  │           └─ JpaRepository.save() → UPDATE
  │
  └─→ 204 No Content
```

---

### Layer별 상세

#### Adapter-In (REST API)

- **Controller**: `ShippingPolicyCommandController`
- **Method**: `update(Long sellerId, Long policyId, UpdateShippingPolicyApiRequest request)`
- **Request DTO**: `UpdateShippingPolicyApiRequest`
  - 필드: RegisterShippingPolicyApiRequest와 동일
  - PathVariable에서 `sellerId`, `policyId` 전달
- **Response**: `204 No Content`

**API-DTO-004 규칙**: Update Request에 ID 포함 금지 → PathVariable로 전달

---

#### Application Layer

**UseCase (Port-In)**:
- Interface: `UpdateShippingPolicyUseCase`
- Method: `void execute(UpdateShippingPolicyCommand)`

**Service 구현체**:
- Class: `UpdateShippingPolicyService`
- 의존성:
  - `ShippingPolicyCommandFactory`: UpdateContext 생성
  - `ShippingPolicyValidator`: 기존 정책 존재 검증
  - `DefaultShippingPolicyResolver`: 기본 정책 변경 로직
  - `ShippingPolicyCommandManager`: 저장

**비즈니스 로직**:
1. Factory로 UpdateContext 생성 (UpdateData + changedAt)
2. Validator로 기존 정책 조회 및 검증
3. DefaultShippingPolicyResolver로 기본 정책 변경 처리:
   - 기본 정책 아님 → 기본으로 변경: 기존 기본 해제
   - 기본 정책 → 해제: 유일한 기본 정책이면 Exception
4. Domain의 update() 메서드로 필드 업데이트
5. Manager를 통해 저장

**트랜잭션 경계**: `@Transactional` (Service 레벨)

---

#### Domain Layer

**Domain 메서드**:
- `ShippingPolicy.update(ShippingPolicyUpdateData, Instant)`
  - 검증: `CONDITIONAL_FREE`인 경우 `freeThreshold` 필수
  - 모든 필드 업데이트 + `updatedAt` 갱신
- `ShippingPolicy.markAsDefault(Instant)`
- `ShippingPolicy.unmarkDefault(Instant)`

**비즈니스 규칙**:
- 유일한 기본 정책은 해제 불가 (`CannotUnmarkOnlyDefaultShippingPolicyException`)

---

#### Adapter-Out (Persistence)

**Repository**:
- `ShippingPolicyQueryDslRepository.findBySellerIdAndId()`
  - QueryDSL: `WHERE seller_id = ? AND id = ? AND deleted_at IS NULL`
- `ShippingPolicyJpaRepository.save()`
  - UPDATE 실행 (기존 Entity)

---

### Database Query 분석

**기존 정책 조회**:
```sql
SELECT *
FROM shipping_policy
WHERE seller_id = :sellerId
  AND id = :policyId
  AND deleted_at IS NULL
```

**수정 쿼리**:
```sql
UPDATE shipping_policy
SET policy_name = :policyName,
    default_policy = :defaultPolicy,
    shipping_fee_type = :shippingFeeType,
    base_fee = :baseFee,
    free_threshold = :freeThreshold,
    jeju_extra_fee = :jejuExtraFee,
    island_extra_fee = :islandExtraFee,
    return_fee = :returnFee,
    exchange_fee = :exchangeFee,
    lead_time_min_days = :minDays,
    lead_time_max_days = :maxDays,
    lead_time_cutoff_time = :cutoffTime,
    updated_at = :now
WHERE id = :policyId
```

**기본 정책 변경 시 기존 기본 정책 해제**:
```sql
-- 기존 기본 정책 조회
SELECT *
FROM shipping_policy
WHERE seller_id = :sellerId
  AND default_policy = TRUE
  AND deleted_at IS NULL

-- 기존 기본 정책 해제
UPDATE shipping_policy
SET default_policy = FALSE,
    updated_at = :now
WHERE id = :existingDefaultId
```

---

## 4. PATCH /shipping-policies/status - 배송정책 상태 변경

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PATCH |
| Path | `/v2/admin/sellers/{sellerId}/shipping-policies/status` |
| Controller | ShippingPolicyCommandController |
| Method | changeStatus |
| UseCase | ChangeShippingPolicyStatusUseCase |
| Service | ChangeShippingPolicyStatusService |

---

### 호출 흐름 다이어그램

```
ShippingPolicyCommandController.changeStatus(ChangeShippingPolicyStatusApiRequest)
  ├─ ShippingPolicyCommandApiMapper.toCommand(sellerId, request)
  │   └─→ ChangeShippingPolicyStatusCommand
  │
  ├─ ChangeShippingPolicyStatusUseCase.execute(command)                  [Port-In]
  │   └─ ChangeShippingPolicyStatusService.execute(command)              [Service]
  │       ├─ ShippingPolicyCommandFactory.createStatusChangeContexts(command)
  │       │   ├─ TimeProvider.now() → Instant
  │       │   └─→ List<StatusChangeContext<ShippingPolicyId>>
  │       │
  │       ├─ ShippingPolicyValidator.findAllExistingOrThrow(policyIds)
  │       │   └─ ShippingPolicyReadManager.getByIds(policyIds)
  │       │       └─ ShippingPolicyQueryPort.findByIds(policyIds)
  │       │           └─ QueryDSL: WHERE id IN (?, ?, ...)
  │       │               └─→ List<ShippingPolicy>
  │       │
  │       ├─ IF active = true → activateAll(policies, changedAt)
  │       │   └─ policy.activate(changedAt) (각 정책)
  │       │
  │       └─ IF active = false → deactivateAll(sellerId, policies, changedAt)
  │           ├─ ShippingPolicyValidator.validateNotLastActivePolicy(sellerId, policies)
  │           │   ├─ ShippingPolicyReadManager.countActiveBySellerId(sellerId)
  │           │   │   └─ QueryDSL: SELECT COUNT(*) WHERE seller_id = ? AND active = TRUE
  │           │   └─ IF (activeCount - policies.size) = 0 → Exception
  │           │
  │           └─ policy.deactivate(changedAt) (각 정책)
  │               └─ IF policy.isDefaultPolicy() → CannotDeactivateDefaultShippingPolicyException
  │
  └─→ 204 No Content
```

---

### Layer별 상세

#### Adapter-In (REST API)

- **Controller**: `ShippingPolicyCommandController`
- **Method**: `changeStatus(Long sellerId, ChangeShippingPolicyStatusApiRequest request)`
- **Request DTO**: `ChangeShippingPolicyStatusApiRequest`
  - `List<Long> policyIds` (필수)
  - `Boolean active` (필수)
- **Response**: `204 No Content`

---

#### Application Layer

**UseCase (Port-In)**:
- Interface: `ChangeShippingPolicyStatusUseCase`
- Method: `void execute(ChangeShippingPolicyStatusCommand)`

**Service 구현체**:
- Class: `ChangeShippingPolicyStatusService`
- 의존성:
  - `ShippingPolicyCommandFactory`: StatusChangeContext 생성
  - `ShippingPolicyValidator`: 기존 정책 존재 검증 + 비활성화 검증
  - `ShippingPolicyCommandManager`: 저장

**비즈니스 로직**:
1. Factory로 StatusChangeContext 생성 (ID 리스트 + changedAt)
2. Validator로 모든 정책 조회 및 존재 검증
3. active = true → 각 정책 활성화
4. active = false → 비활성화 전 검증:
   - **POL-DEACT-002**: 마지막 활성 정책 비활성화 검증
     - 현재 활성 정책 개수 조회
     - 비활성화 후 활성 정책이 0개가 되면 Exception
   - **POL-DEACT-001**: 기본 정책 비활성화 불가 (Domain에서 검증)
5. Manager를 통해 일괄 저장

**트랜잭션 경계**: `@Transactional` (Service 레벨)

---

#### Domain Layer

**Domain 메서드**:
- `ShippingPolicy.activate(Instant)`: active = true, updatedAt 갱신
- `ShippingPolicy.deactivate(Instant)`:
  - 검증: `defaultPolicy = true`이면 Exception
  - active = false, updatedAt 갱신

**비즈니스 규칙**:
- **POL-DEACT-001**: 기본 정책은 비활성화 불가
- **POL-DEACT-002**: 마지막 활성 정책은 비활성화 불가

---

#### Adapter-Out (Persistence)

**Repository**:
- `ShippingPolicyQueryDslRepository.findByIds(List<Long>)`
  - QueryDSL: `WHERE id IN (:ids) AND deleted_at IS NULL`
- `ShippingPolicyQueryDslRepository.countActiveBySellerId(Long)`
  - QueryDSL: `SELECT COUNT(*) WHERE seller_id = ? AND active = TRUE AND deleted_at IS NULL`
- `ShippingPolicyJpaRepository.saveAll(List<Entity>)`
  - UPDATE 일괄 실행

---

### Database Query 분석

**정책 조회 (다건)**:
```sql
SELECT *
FROM shipping_policy
WHERE id IN (:policyId1, :policyId2, :policyId3)
  AND deleted_at IS NULL
```

**활성 정책 개수 조회 (비활성화 검증용)**:
```sql
SELECT COUNT(*)
FROM shipping_policy
WHERE seller_id = :sellerId
  AND active = TRUE
  AND deleted_at IS NULL
```

**상태 변경 쿼리 (다건)**:
```sql
UPDATE shipping_policy
SET active = :active,
    updated_at = :now
WHERE id IN (:policyId1, :policyId2, :policyId3)
```

---

## 아키텍처 패턴 요약

### Hexagonal 레이어 구조

```
┌─────────────────────────────────────────────────────────┐
│  Adapter-In (REST API)                                  │
│  - Controller: HTTP 엔드포인트                            │
│  - ApiMapper: API DTO ↔ Application DTO 변환              │
│  - Request/Response DTO: @Valid 검증                      │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  Application Layer                                      │
│  - UseCase (Port-In): 인터페이스                          │
│  - Service: UseCase 구현 + 비즈니스 로직 오케스트레이션        │
│  - Factory: Command → Domain 생성, Context 생성            │
│  - Manager: Port-Out 래핑 + @Transactional 관리            │
│  - Validator: 검증 로직 집중                               │
│  - Assembler: Domain → Result 변환                        │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  Domain Layer                                           │
│  - Aggregate: ShippingPolicy (비즈니스 규칙 + 불변성)        │
│  - VO: ShippingPolicyName, LeadTime, Money 등             │
│  - ID: ShippingPolicyId (Type-Safe)                      │
│  - Port (Port-Out): QueryPort, CommandPort 인터페이스       │
│  - Criteria: 검색 조건 정의                                │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  Adapter-Out (Persistence)                              │
│  - Adapter: Port-Out 구현                                 │
│  - JpaRepository: save, saveAll (Command용)               │
│  - QueryDslRepository: 모든 조회 로직 (Query용)             │
│  - EntityMapper: Entity ↔ Domain 변환                     │
│  - ConditionBuilder: 동적 쿼리 조건 생성                     │
└─────────────────────────────────────────────────────────┘
```

---

### CQRS 패턴

| 구분 | Query | Command |
|------|-------|---------|
| Controller | `ShippingPolicyQueryController` | `ShippingPolicyCommandController` |
| UseCase | `SearchShippingPolicyUseCase` | `RegisterShippingPolicyUseCase`, `UpdateShippingPolicyUseCase`, `ChangeShippingPolicyStatusUseCase` |
| Service | `SearchShippingPolicyService` | `RegisterShippingPolicyService`, `UpdateShippingPolicyService`, `ChangeShippingPolicyStatusService` |
| Port-Out | `ShippingPolicyQueryPort` | `ShippingPolicyCommandPort` |
| Adapter | `ShippingPolicyQueryAdapter` | `ShippingPolicyCommandAdapter` |
| Repository | `ShippingPolicyQueryDslRepository` | `ShippingPolicyJpaRepository` |
| Manager | `ShippingPolicyReadManager` | `ShippingPolicyCommandManager` |

---

### 트랜잭션 관리

- **Application Service**: `@Transactional` (Command만)
- **Manager**: `@Transactional(readOnly = true)` (Query)
- **Adapter**: `@Transactional` 없음 (Manager에서 관리)

---

### 주요 컴포넌트 역할

| 컴포넌트 | 역할 | 규칙 |
|----------|------|------|
| **Factory** | Command → Domain 생성, TimeProvider 주입 | APP-TIM-001: TimeProvider 직접 사용 금지 |
| **Manager** | Port 래핑 + 트랜잭션 경계 관리 | APP-MGR-001: Port-Out을 한 번 래핑 |
| **Validator** | 검증 + Domain 조회 | APP-VAL-001: findExistingOrThrow 패턴 |
| **Assembler** | Domain → Result 변환 | APP-ASM-001: PageResult 생성 |
| **Resolver** | 복잡한 비즈니스 로직 처리 | DefaultShippingPolicyResolver |

---

## 비즈니스 규칙 요약

| 규칙 ID | 규칙 내용 | 검증 위치 |
|---------|----------|----------|
| **POL-DEF-001** | 셀러당 기본 정책은 정확히 1개만 존재 | DefaultShippingPolicyResolver |
| **POL-DEF-002** | 기본 정책은 활성화 상태여야 함 | Domain (forNew) |
| **POL-DEACT-001** | 기본 정책은 비활성화 불가 | Domain (deactivate) |
| **POL-DEACT-002** | 마지막 활성 정책은 비활성화 불가 | ShippingPolicyValidator |
| **POL-FEE-001** | CONDITIONAL_FREE는 freeThreshold 필수 | Domain (validateFeeSettings) |

---

## 참고 문서

- **Domain**: `/domain/src/main/java/com/ryuqq/marketplace/domain/shippingpolicy/`
- **Application**: `/application/src/main/java/com/ryuqq/marketplace/application/shippingpolicy/`
- **Adapter-Out**: `/adapter-out/persistence-mysql/.../shippingpolicy/`
- **Adapter-In**: `/adapter-in/rest-api/.../shippingpolicy/`
