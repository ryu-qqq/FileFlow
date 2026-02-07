# SellerAddress API 호출 흐름 분석

> **분석 대상**: 셀러 주소 관리 API (CQRS 패턴)
>
> **분석일**: 2026-02-06

---

## 📋 목차

1. [개요](#개요)
2. [엔드포인트 목록](#엔드포인트-목록)
3. [API 흐름 분석](#api-흐름-분석)
   - [Query: 주소 목록 조회](#1-query-주소-목록-조회)
   - [Command: 주소 등록](#2-command-주소-등록)
   - [Command: 주소 수정](#3-command-주소-수정)
   - [Command: 주소 삭제](#4-command-주소-삭제)
4. [레이어별 책임](#레이어별-책임)
5. [주요 패턴 및 규칙](#주요-패턴-및-규칙)

---

## 개요

셀러 주소 관리는 **CQRS 패턴**을 적용하여 조회(Query)와 명령(Command)을 명확히 분리합니다.

### 아키텍처 레이어

```
Adapter-In (REST API)
    ↓
Application (UseCase + Service)
    ↓
Domain (Port Interface)
    ↓
Adapter-Out (Persistence)
    ↓
Repository (JPA + QueryDSL)
```

### 주요 특징

- **CQRS 분리**: `SellerAddressQueryController` / `SellerAddressCommandController`
- **소프트 삭제**: DELETE 메서드 금지, PATCH로 소프트 삭제
- **기본 주소 관리**: 타입별(SHIPPING/RETURN) 기본 주소 자동 전환
- **Hexagonal 아키텍처**: Port-Adapter 패턴으로 레이어 간 의존성 역전

---

## 엔드포인트 목록

| HTTP Method | Path | 기능 | Controller |
|-------------|------|------|-----------|
| GET | `/sellers/{sellerId}/addresses` | 주소 목록 조회 | SellerAddressQueryController |
| POST | `/sellers/{sellerId}/addresses` | 주소 등록 | SellerAddressCommandController |
| PUT | `/sellers/{sellerId}/addresses/{addressId}` | 주소 수정 | SellerAddressCommandController |
| PATCH | `/sellers/{sellerId}/addresses/{addressId}/status` | 주소 삭제(소프트) | SellerAddressCommandController |

---

## API 흐름 분석

---

### 1. Query: 주소 목록 조회

**Endpoint**: `GET /sellers/{sellerId}/addresses`

#### 전체 호출 흐름

```
SellerAddressQueryController.search()
  ├─ SellerIdsResolver.resolve()              [권한별 셀러ID 결정]
  ├─ SellerAddressQueryApiMapper.toSearchParams()
  │   └─> SellerAddressSearchParams           [API → Application DTO]
  │
  ├─ SearchSellerAddressUseCase.execute()     [Port-In Interface]
  │   └─ SearchSellerAddressService.execute()
  │       ├─ SellerAddressQueryFactory.createSearchCriteria()
  │       │   └─> SellerAddressSearchCriteria [Application → Domain]
  │       │
  │       ├─ SellerAddressReadManager.search()
  │       │   └─ SellerAddressQueryPort.search() [Domain Port]
  │       │       └─ SellerAddressQueryAdapter.search()
  │       │           ├─ SellerAddressConditionBuilder.buildConditions()
  │       │           ├─ SellerAddressQueryDslRepository.search()
  │       │           │   └─ QueryDSL 실행: WHERE + ORDER BY + OFFSET + LIMIT
  │       │           └─ SellerAddressJpaEntityMapper.toDomain()
  │       │               └─> List<SellerAddress>
  │       │
  │       ├─ SellerAddressReadManager.count()
  │       │   └─ SellerAddressQueryPort.count()
  │       │       └─ SellerAddressQueryAdapter.count()
  │       │           └─ SellerAddressQueryDslRepository.count()
  │       │
  │       └─ SellerAddressAssembler.toResults()
  │           └─> List<SellerAddressResult>
  │
  └─ SellerAddressQueryApiMapper.toPageResponse()
      └─> ApiResponse<PageApiResponse<SellerAddressApiResponse>>
```

---

#### 레이어별 상세

##### 1️⃣ Adapter-In Layer

**Controller**: `SellerAddressQueryController`

```java
@GetMapping
public ResponseEntity<ApiResponse<PageApiResponse<SellerAddressApiResponse>>> search(
    @PathVariable Long sellerId,
    @Valid SearchSellerAddressesApiRequest request) {

    // 1. 권한별 셀러ID 결정 (슈퍼관리자: 다건, 셀러: 본인만)
    List<Long> effectiveSellerIds = sellerIdsResolver.resolve(request.sellerIds(), sellerId);

    // 2. API Request → Application Params 변환
    SellerAddressSearchParams searchParams = mapper.toSearchParams(effectiveSellerIds, request);

    // 3. UseCase 실행
    PagedResult<SellerAddressResult> pagedResult = searchUseCase.execute(searchParams);

    // 4. Application Result → API Response 변환
    PageApiResponse<SellerAddressApiResponse> response = mapper.toPageResponse(pagedResult);

    return ResponseEntity.ok(ApiResponse.of(response));
}
```

**Mapper**: `SellerAddressQueryApiMapper`
- `toSearchParams()`: API Request → Application Params (기본값 적용: page=0, size=20)
- `toPageResponse()`: Application Result → API Response (날짜 ISO8601 변환)

**DTO**:
- **Request**: `SearchSellerAddressesApiRequest` (sellerIds, addressTypes, defaultAddress, searchField, searchWord, page, size)
- **Response**: `SellerAddressApiResponse` (id, sellerId, addressType, addressName, address, defaultAddress, createdAt, updatedAt)

---

##### 2️⃣ Application Layer

**UseCase**: `SearchSellerAddressUseCase` (Port-In Interface)

```java
public interface SearchSellerAddressUseCase {
    PagedResult<SellerAddressResult> execute(SellerAddressSearchParams params);
}
```

**Service**: `SearchSellerAddressService`

```java
@Service
public class SearchSellerAddressService implements SearchSellerAddressUseCase {

    @Override
    public PagedResult<SellerAddressResult> execute(SellerAddressSearchParams params) {
        // 1. Application Params → Domain Criteria 변환
        SellerAddressSearchCriteria criteria = queryFactory.createSearchCriteria(params);

        // 2. Domain 조회 (Manager를 통한 Port 호출)
        List<SellerAddress> addresses = readManager.search(criteria);
        long totalCount = readManager.count(criteria);

        // 3. Domain → Application Result 변환
        List<SellerAddressResult> results = assembler.toResults(addresses);

        // 4. 페이징 결과 생성
        return PagedResult.of(results, criteria.page(), criteria.size(), totalCount);
    }
}
```

**Manager**: `SellerAddressReadManager`
- Port 호출을 관리하는 중간 레이어
- `search(criteria)`: QueryPort.search() 호출
- `count(criteria)`: QueryPort.count() 호출

**Factory**: `SellerAddressQueryFactory`
- `createSearchCriteria()`: Application Params → Domain Criteria 변환

**Assembler**: `SellerAddressAssembler`
- `toResults()`: Domain Aggregate → Application Result DTO 변환

---

##### 3️⃣ Domain Layer

**Port**: `SellerAddressQueryPort` (Interface)

```java
public interface SellerAddressQueryPort {
    List<SellerAddress> search(SellerAddressSearchCriteria criteria);
    long count(SellerAddressSearchCriteria criteria);
}
```

**Aggregate**: `SellerAddress`
- Domain 핵심 객체 (비즈니스 로직 포함)

**Criteria**: `SellerAddressSearchCriteria`
- 검색 조건을 Domain 관점에서 표현

---

##### 4️⃣ Adapter-Out Layer

**Adapter**: `SellerAddressQueryAdapter`

```java
@Component
public class SellerAddressQueryAdapter implements SellerAddressQueryPort {

    @Override
    public List<SellerAddress> search(SellerAddressSearchCriteria criteria) {
        BooleanBuilder conditions = buildConditions(criteria);
        return queryDslRepository.search(conditions, criteria.offset(), criteria.size())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    private BooleanBuilder buildConditions(SellerAddressSearchCriteria criteria) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(conditionBuilder.sellerIdIn(criteria.sellerIdValues()));
        if (criteria.hasAddressTypesFilter()) {
            builder.and(conditionBuilder.addressTypeIn(criteria.addressTypeNames()));
        }
        if (criteria.hasDefaultFilter()) {
            builder.and(conditionBuilder.defaultAddressEq(criteria.defaultAddress()));
        }
        if (criteria.hasKeyword()) {
            builder.and(conditionBuilder.keywordContains(criteria.keyword()));
        }
        return builder;
    }
}
```

**Repository**: `SellerAddressQueryDslRepository`

```java
@Repository
public class SellerAddressQueryDslRepository {

    public List<SellerAddressJpaEntity> search(BooleanBuilder conditions, long offset, int size) {
        return queryFactory
                .selectFrom(sellerAddressJpaEntity)
                .where(conditions, notDeleted())  // 소프트 삭제 필터
                .orderBy(sellerAddressJpaEntity.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    private BooleanExpression notDeleted() {
        return sellerAddressJpaEntity.deletedAt.isNull();
    }
}
```

**Mapper**: `SellerAddressJpaEntityMapper`
- `toDomain()`: JPA Entity → Domain Aggregate 변환

**Entity**: `SellerAddressJpaEntity`
- JPA 엔티티 (테이블: `seller_addresses`)
- `SoftDeletableEntity` 상속 (createdAt, updatedAt, deletedAt)

---

#### Database Query 분석

```sql
SELECT *
FROM seller_addresses
WHERE seller_id IN (?, ?, ...)
  AND address_type IN (?, ?)  -- optional
  AND is_default = ?          -- optional
  AND (address_name LIKE ? OR address LIKE ? OR address_detail LIKE ?)  -- optional
  AND deleted_at IS NULL
ORDER BY created_at DESC
LIMIT ? OFFSET ?
```

---

### 2. Command: 주소 등록

**Endpoint**: `POST /sellers/{sellerId}/addresses`

#### 전체 호출 흐름

```
SellerAddressCommandController.register()
  ├─ SellerAddressCommandApiMapper.toCommand()
  │   └─> RegisterSellerAddressCommand        [API → Application DTO]
  │
  ├─ RegisterSellerAddressUseCase.execute()   [Port-In Interface]
  │   └─ RegisterSellerAddressService.execute()
  │       ├─ SellerAddressValidator.validateNoDuplicateAddressName()
  │       │   └─ SellerAddressQueryPort.existsBySellerIdAndAddressTypeAndAddressName()
  │       │
  │       ├─ SellerAddressCommandFactory.createRegisterContext()
  │       │   └─> RegisterContext<SellerAddress>
  │       │       └─ SellerAddress.create()   [Domain Aggregate 생성]
  │       │
  │       ├─ [기본 주소 전환 로직]
  │       │   └─ SellerAddressReadManager.findDefaultBySellerId()
  │       │       └─ SellerAddressQueryPort.findDefaultBySellerId()
  │       │           └─ existingDefault.unmarkDefault(changedAt)
  │       │               └─ SellerAddressCommandPort.persist()
  │       │
  │       └─ SellerAddressCommandManager.persist()
  │           └─ SellerAddressCommandPort.persist() [Domain Port]
  │               └─ SellerAddressCommandAdapter.persist()
  │                   ├─ SellerAddressJpaEntityMapper.toEntity()
  │                   ├─ SellerAddressJpaRepository.save()
  │                   └─> Long (생성된 ID)
  │
  └─> ApiResponse<RegisterSellerAddressApiResponse(id)>
```

---

#### 레이어별 상세

##### 1️⃣ Adapter-In Layer

**Controller**: `SellerAddressCommandController`

```java
@PostMapping
public ResponseEntity<ApiResponse<RegisterSellerAddressApiResponse>> register(
    @PathVariable Long sellerId,
    @Valid @RequestBody RegisterSellerAddressApiRequest request) {

    // 1. API Request → Application Command 변환
    RegisterSellerAddressCommand command = mapper.toCommand(sellerId, request);

    // 2. UseCase 실행
    Long createdId = registerUseCase.execute(command);

    // 3. 201 Created 응답
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.of(new RegisterSellerAddressApiResponse(createdId)));
}
```

**Mapper**: `SellerAddressCommandApiMapper`
- `toCommand()`: API Request + PathVariable → Application Command

**DTO**:
- **Request**: `RegisterSellerAddressApiRequest` (addressType, addressName, address, defaultAddress)
- **Response**: `RegisterSellerAddressApiResponse` (id)

---

##### 2️⃣ Application Layer

**UseCase**: `RegisterSellerAddressUseCase` (Port-In Interface)

```java
public interface RegisterSellerAddressUseCase {
    Long execute(RegisterSellerAddressCommand command);
}
```

**Service**: `RegisterSellerAddressService`

```java
@Service
public class RegisterSellerAddressService implements RegisterSellerAddressUseCase {

    @Override
    public Long execute(RegisterSellerAddressCommand command) {
        SellerId sellerId = SellerId.of(command.sellerId());
        AddressType addressType = AddressType.valueOf(command.addressType());

        // 1. 중복 주소명 검증
        validator.validateNoDuplicateAddressName(sellerId, addressType, command.addressName());

        // 2. Domain Aggregate 생성
        RegisterContext<SellerAddress> context = commandFactory.createRegisterContext(command);

        // 3. 기본 주소 전환 로직
        if (command.defaultAddress()) {
            unmarkExistingDefaults(sellerId, addressType, context.changedAt());
        }

        // 4. 저장
        return commandManager.persist(context.newEntity());
    }

    private void unmarkExistingDefaults(SellerId sellerId, AddressType addressType, Instant changedAt) {
        readManager.findDefaultBySellerId(sellerId, addressType)
            .ifPresent(existingDefault -> {
                existingDefault.unmarkDefault(changedAt);  // Domain 메서드 호출
                commandManager.persist(existingDefault);
            });
    }
}
```

**Validator**: `SellerAddressValidator`
- `validateNoDuplicateAddressName()`: 동일 셀러·타입 내 주소명 중복 검증

**Factory**: `SellerAddressCommandFactory`
- `createRegisterContext()`: Command → RegisterContext (Domain Aggregate 생성 포함)

**Manager**: `SellerAddressCommandManager`
- `persist()`: CommandPort 호출

---

##### 3️⃣ Domain Layer

**Port**: `SellerAddressCommandPort` (Interface)

```java
public interface SellerAddressCommandPort {
    Long persist(SellerAddress address);
}
```

**Aggregate**: `SellerAddress`

```java
public class SellerAddress {
    public static SellerAddress create(
        SellerId sellerId,
        AddressType addressType,
        AddressName addressName,
        Address address,
        boolean defaultAddress,
        Instant createdAt
    ) {
        // 비즈니스 규칙 검증 후 생성
    }

    public void unmarkDefault(Instant updatedAt) {
        this.defaultAddress = false;
        this.updatedAt = updatedAt;
    }
}
```

---

##### 4️⃣ Adapter-Out Layer

**Adapter**: `SellerAddressCommandAdapter`

```java
@Component
public class SellerAddressCommandAdapter implements SellerAddressCommandPort {

    @Override
    public Long persist(SellerAddress address) {
        SellerAddressJpaEntity entity = mapper.toEntity(address);
        SellerAddressJpaEntity saved = repository.save(entity);
        return saved.getId();
    }
}
```

**Repository**: `SellerAddressJpaRepository`

```java
public interface SellerAddressJpaRepository extends JpaRepository<SellerAddressJpaEntity, Long> {}
```

- Spring Data JPA의 `save()` 메서드만 사용
- 조회는 QueryDslRepository에서 처리

---

#### Database Query 분석

```sql
-- 1. 중복 검증
SELECT 1
FROM seller_addresses
WHERE seller_id = ?
  AND address_type = ?
  AND address_name = ?
  AND deleted_at IS NULL
LIMIT 1

-- 2. 기본 주소 조회 (기본 주소 전환 시)
SELECT *
FROM seller_addresses
WHERE seller_id = ?
  AND address_type = ?
  AND is_default = true
  AND deleted_at IS NULL

-- 3. 기존 기본 주소 업데이트 (존재 시)
UPDATE seller_addresses
SET is_default = false, updated_at = ?
WHERE id = ?

-- 4. 새 주소 등록
INSERT INTO seller_addresses
(seller_id, address_type, address_name, zipcode, address, address_detail, is_default, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
```

---

### 3. Command: 주소 수정

**Endpoint**: `PUT /sellers/{sellerId}/addresses/{addressId}`

#### 전체 호출 흐름

```
SellerAddressCommandController.update()
  ├─ SellerAddressCommandApiMapper.toCommand()
  │   └─> UpdateSellerAddressCommand          [API → Application DTO]
  │
  ├─ UpdateSellerAddressUseCase.execute()     [Port-In Interface]
  │   └─ UpdateSellerAddressService.execute()
  │       ├─ SellerAddressCommandFactory.createUpdateContext()
  │       │   └─> UpdateContext<SellerAddressId, SellerAddressUpdateData>
  │       │
  │       ├─ SellerAddressValidator.findExistingOrThrow()
  │       │   └─ SellerAddressQueryPort.findById()
  │       │       └─> SellerAddress (Domain Aggregate)
  │       │
  │       ├─ SellerAddress.update(updateData, changedAt)  [Domain 메서드]
  │       │
  │       ├─ [기본 주소 전환 로직] (defaultAddress=true인 경우)
  │       │   ├─ SellerAddressReadManager.findDefaultBySellerId()
  │       │   │   └─ 기존 기본 주소.unmarkDefault(changedAt)
  │       │   └─ 현재 주소.markAsDefault(changedAt)
  │       │
  │       └─ SellerAddressCommandManager.persist()
  │           └─ SellerAddressCommandPort.persist()
  │               └─ SellerAddressCommandAdapter.persist()
  │                   └─ SellerAddressJpaRepository.save()
  │
  └─> ResponseEntity.noContent().build() (204 No Content)
```

---

#### 레이어별 상세

##### 1️⃣ Adapter-In Layer

**Controller**: `SellerAddressCommandController`

```java
@PutMapping("/{addressId}")
public ResponseEntity<Void> update(
    @PathVariable Long sellerId,
    @PathVariable Long addressId,
    @Valid @RequestBody UpdateSellerAddressApiRequest request) {

    // 1. API Request → Application Command 변환
    UpdateSellerAddressCommand command = mapper.toCommand(addressId, request);

    // 2. UseCase 실행
    updateUseCase.execute(command);

    // 3. 204 No Content 응답
    return ResponseEntity.noContent().build();
}
```

**DTO**:
- **Request**: `UpdateSellerAddressApiRequest` (addressName, address, defaultAddress)
  - **규칙**: API-DTO-004 - Update Request에 ID 포함 금지 → PathVariable에서 전달

---

##### 2️⃣ Application Layer

**Service**: `UpdateSellerAddressService`

```java
@Service
public class UpdateSellerAddressService implements UpdateSellerAddressUseCase {

    @Override
    public void execute(UpdateSellerAddressCommand command) {
        // 1. Command → UpdateContext 변환
        UpdateContext<SellerAddressId, SellerAddressUpdateData> context =
                commandFactory.createUpdateContext(command);

        // 2. 기존 주소 조회 (없으면 예외)
        SellerAddress address = validator.findExistingOrThrow(context.id());

        // 3. Domain 업데이트
        address.update(context.updateData(), context.changedAt());

        // 4. 기본 주소 전환
        if (Boolean.TRUE.equals(command.defaultAddress())) {
            unmarkExistingDefaultThenMarkThis(address.sellerId(), address, context.changedAt());
        }

        // 5. 저장
        commandManager.persist(address);
    }

    private void unmarkExistingDefaultThenMarkThis(
            SellerId sellerId, SellerAddress newDefault, Instant changedAt) {
        readManager.findDefaultBySellerId(sellerId, newDefault.addressType())
            .filter(current -> !current.id().equals(newDefault.id()))  // 자기 자신 제외
            .ifPresent(current -> {
                current.unmarkDefault(changedAt);
                commandManager.persist(current);
            });
        newDefault.markAsDefault(changedAt);
    }
}
```

---

##### 3️⃣ Domain Layer

**Aggregate**: `SellerAddress`

```java
public void update(SellerAddressUpdateData updateData, Instant updatedAt) {
    this.addressName = updateData.addressName();
    this.address = updateData.address();
    this.updatedAt = updatedAt;
}

public void markAsDefault(Instant updatedAt) {
    this.defaultAddress = true;
    this.updatedAt = updatedAt;
}

public void unmarkDefault(Instant updatedAt) {
    this.defaultAddress = false;
    this.updatedAt = updatedAt;
}
```

---

#### Database Query 분석

```sql
-- 1. 기존 주소 조회
SELECT *
FROM seller_addresses
WHERE id = ?
  AND deleted_at IS NULL

-- 2. 기존 기본 주소 조회 (기본 주소 전환 시)
SELECT *
FROM seller_addresses
WHERE seller_id = ?
  AND address_type = ?
  AND is_default = true
  AND deleted_at IS NULL

-- 3. 기존 기본 주소 업데이트 (존재 시)
UPDATE seller_addresses
SET is_default = false, updated_at = ?
WHERE id = ?

-- 4. 현재 주소 업데이트
UPDATE seller_addresses
SET address_name = ?, zipcode = ?, address = ?, address_detail = ?, is_default = ?, updated_at = ?
WHERE id = ?
```

---

### 4. Command: 주소 삭제

**Endpoint**: `PATCH /sellers/{sellerId}/addresses/{addressId}/status`

#### 전체 호출 흐름

```
SellerAddressCommandController.delete()
  ├─ SellerAddressCommandApiMapper.toDeleteCommand()
  │   └─> DeleteSellerAddressCommand          [API → Application DTO]
  │
  ├─ DeleteSellerAddressUseCase.execute()     [Port-In Interface]
  │   └─ DeleteSellerAddressService.execute()
  │       ├─ SellerAddressCommandFactory.createDeleteContext()
  │       │   └─> StatusChangeContext<SellerAddressId>
  │       │
  │       ├─ SellerAddressValidator.findExistingOrThrow()
  │       │   └─ SellerAddressQueryPort.findById()
  │       │
  │       ├─ SellerAddressValidator.validateNotDefaultAddress()
  │       │   └─ if (address.isDefault()) throw Exception
  │       │
  │       ├─ SellerAddress.delete(changedAt)   [Domain 메서드]
  │       │   └─ deletedAt = changedAt (소프트 삭제)
  │       │
  │       └─ SellerAddressCommandManager.persist()
  │           └─ SellerAddressCommandPort.persist()
  │               └─ SellerAddressJpaRepository.save()
  │
  └─> ResponseEntity.noContent().build() (204 No Content)
```

---

#### 레이어별 상세

##### 1️⃣ Adapter-In Layer

**Controller**: `SellerAddressCommandController`

```java
@PatchMapping("/{addressId}/status")
public ResponseEntity<Void> delete(
    @PathVariable Long sellerId,
    @PathVariable Long addressId) {

    // 1. PathVariable → Application Command 변환
    DeleteSellerAddressCommand command = mapper.toDeleteCommand(addressId);

    // 2. UseCase 실행
    deleteUseCase.execute(command);

    // 3. 204 No Content 응답
    return ResponseEntity.noContent().build();
}
```

**규칙**:
- **API-CTR-002**: DELETE 메서드 금지 → PATCH로 소프트 삭제
- 기본 주소는 삭제 불가 (비즈니스 규칙)

---

##### 2️⃣ Application Layer

**Service**: `DeleteSellerAddressService`

```java
@Service
public class DeleteSellerAddressService implements DeleteSellerAddressUseCase {

    @Override
    public void execute(DeleteSellerAddressCommand command) {
        // 1. Command → StatusChangeContext 변환
        StatusChangeContext<SellerAddressId> context = commandFactory.createDeleteContext(command);

        // 2. 기존 주소 조회
        SellerAddress address = validator.findExistingOrThrow(context.id());

        // 3. 기본 주소 삭제 방지 검증
        validator.validateNotDefaultAddress(address);

        // 4. Domain 소프트 삭제
        address.delete(context.changedAt());

        // 5. 저장
        commandManager.persist(address);
    }
}
```

**Validator**: `SellerAddressValidator`
- `validateNotDefaultAddress()`: 기본 주소(isDefault=true) 삭제 방지

---

##### 3️⃣ Domain Layer

**Aggregate**: `SellerAddress`

```java
public void delete(Instant deletedAt) {
    this.deletedAt = deletedAt;
    this.updatedAt = deletedAt;
}
```

---

#### Database Query 분석

```sql
-- 1. 기존 주소 조회
SELECT *
FROM seller_addresses
WHERE id = ?
  AND deleted_at IS NULL

-- 2. 소프트 삭제 (UPDATE)
UPDATE seller_addresses
SET deleted_at = ?, updated_at = ?
WHERE id = ?
```

---

## 레이어별 책임

### Adapter-In Layer (REST API)

| 컴포넌트 | 책임 |
|---------|------|
| **Controller** | HTTP 요청 수신, UseCase 호출, HTTP 응답 반환 |
| **ApiMapper** | API DTO ↔ Application DTO 변환 |
| **Request DTO** | API 입력 검증 (@Valid) |
| **Response DTO** | API 응답 형식 정의 (날짜 String 변환) |

**규칙**:
- Controller는 비즈니스 로직 포함 금지 (API-CTR-007)
- @Transactional 금지 (API-CTR-005)
- ResponseEntity<ApiResponse<T>> 래핑 필수 (API-CTR-004)

---

### Application Layer

| 컴포넌트 | 책임 |
|---------|------|
| **UseCase** | Port-In 인터페이스 정의 |
| **Service** | 비즈니스 로직 실행, 트랜잭션 경계 (@Transactional) |
| **Manager** | Port 호출 관리 (ReadManager, CommandManager) |
| **Factory** | Domain 객체 생성 지원 |
| **Validator** | 비즈니스 규칙 검증 |
| **Assembler** | Domain → Application Result 변환 |

**규칙**:
- Service에서 @Transactional 처리 (APP-SVC-003)
- Manager를 통한 Port 호출 (레이어 간 의존성 역전)

---

### Domain Layer

| 컴포넌트 | 책임 |
|---------|------|
| **Aggregate** | 비즈니스 규칙, 상태 변경 메서드 |
| **Port (Interface)** | 인프라 추상화 (QueryPort, CommandPort) |
| **Criteria** | 검색 조건 Domain 표현 |
| **VO** | 값 객체 (SellerId, AddressType, etc.) |

**규칙**:
- Aggregate에 비즈니스 로직 집중 (DOM-AGG-002)
- Port는 인터페이스만 정의 (구현은 Adapter-Out)

---

### Adapter-Out Layer (Persistence)

| 컴포넌트 | 책임 |
|---------|------|
| **Adapter** | Port 구현, Repository 호출, Entity ↔ Domain 변환 |
| **Repository** | 데이터 액세스 (JPA + QueryDSL) |
| **JpaEntity** | JPA 엔티티 (테이블 매핑) |
| **EntityMapper** | JPA Entity ↔ Domain Aggregate 변환 |
| **ConditionBuilder** | QueryDSL BooleanExpression 빌더 |

**규칙**:
- CommandAdapter는 JpaRepository만 사용 (PER-ADP-001)
- QueryAdapter는 QueryDslRepository만 사용 (PER-ADP-004)
- Adapter에서 @Transactional 금지 (PER-ADP-002)
- JPA 관계 어노테이션 금지 (@OneToMany, @ManyToOne 등)

---

## 주요 패턴 및 규칙

### 1. CQRS 패턴

- **Query**: `SellerAddressQueryController` → `SearchSellerAddressUseCase` → `SellerAddressQueryPort`
- **Command**: `SellerAddressCommandController` → `RegisterSellerAddressUseCase` → `SellerAddressCommandPort`

**이점**:
- 조회와 명령의 복잡도 분리
- 각 작업에 최적화된 구조 설계 가능

---

### 2. Hexagonal 아키텍처 (Port-Adapter)

```
Application Layer (비즈니스 로직)
    ↑ Port (Interface)
    ↓ Adapter (구현)
Adapter-Out Layer (인프라)
```

**이점**:
- 인프라 변경 시 비즈니스 로직 영향 없음
- 테스트 시 Port를 Mock으로 대체 가능

---

### 3. 소프트 삭제

- DELETE 메서드 금지 → **PATCH로 소프트 삭제**
- `deletedAt` 필드에 타임스탬프 기록
- 모든 조회 쿼리에 `deleted_at IS NULL` 조건 자동 추가

**이유**:
- 데이터 복구 가능
- 감사 추적(Audit Trail) 유지

---

### 4. 기본 주소 관리

**비즈니스 규칙**:
- 타입별(SHIPPING/RETURN) 기본 주소는 **1개만 존재**
- 신규 등록/수정 시 기본 주소 설정 → 기존 기본 주소 자동 해제
- 기본 주소는 삭제 불가

**구현 위치**:
- Application Service에서 처리 (RegisterSellerAddressService, UpdateSellerAddressService)
- Domain Aggregate 메서드 활용 (`markAsDefault()`, `unmarkDefault()`)

---

### 5. Manager 패턴

**목적**: Application Layer에서 Port 호출을 관리

- **ReadManager**: QueryPort 호출 관리
- **CommandManager**: CommandPort 호출 관리

**이점**:
- Service 코드 간결화
- Port 호출 로직 재사용

---

### 6. Factory 패턴

**목적**: Domain 객체 생성 로직 캡슐화

- **CommandFactory**: Command → Context → Domain Aggregate 생성
- **QueryFactory**: Params → Criteria 변환

**이점**:
- 생성 로직 중앙화
- Service 코드 간결화

---

### 7. Validator 패턴

**목적**: 비즈니스 규칙 검증 로직 분리

- `validateNoDuplicateAddressName()`: 주소명 중복 검증
- `findExistingOrThrow()`: 존재 여부 확인 및 예외 처리
- `validateNotDefaultAddress()`: 기본 주소 삭제 방지

**이점**:
- Service 코드 간결화
- 검증 로직 재사용

---

### 8. DTO 변환 레이어

```
API Request
  ↓ ApiMapper
Application Command/Params
  ↓ Factory
Domain Aggregate/Criteria
  ↓ Adapter
JPA Entity
```

**규칙**:
- 각 레이어는 자신의 DTO만 사용
- 레이어 간 DTO 변환은 Mapper/Factory/Assembler가 담당

---

## 요약

### 핵심 플로우

1. **Controller**: HTTP 요청 수신 → UseCase 호출
2. **Service**: 비즈니스 로직 실행 → Port 호출
3. **Adapter**: Port 구현 → Repository 호출
4. **Repository**: 데이터 액세스 (JPA + QueryDSL)

### 주요 특징

- ✅ **CQRS 분리**: Query/Command 명확한 책임 분리
- ✅ **Hexagonal 아키텍처**: Port-Adapter 패턴으로 인프라 의존성 역전
- ✅ **소프트 삭제**: PATCH를 통한 안전한 삭제
- ✅ **기본 주소 관리**: 비즈니스 규칙 자동화
- ✅ **Manager/Factory/Validator 패턴**: 코드 간결화 및 재사용성 향상

---

**분석 완료일**: 2026-02-06
**담당자**: Claude (API Flow Analyzer)
