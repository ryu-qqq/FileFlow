# CommonCodeType API Flow Analysis

공통 코드 타입(CommonCodeType) 도메인의 전체 API 호출 흐름 분석 문서입니다.

## 목차

1. [GET /api/v1/market/admin/common-code-types - 목록 조회](#1-get-목록-조회)
2. [POST /api/v1/market/admin/common-code-types - 등록](#2-post-등록)
3. [PUT /api/v1/market/admin/common-code-types/{id} - 수정](#3-put-수정)
4. [PATCH /api/v1/market/admin/common-code-types/active-status - 상태 변경](#4-patch-상태-변경)

---

## 1. GET 목록 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /api/v1/market/admin/common-code-types |
| Controller | CommonCodeTypeQueryController |
| Method | search |
| UseCase | SearchCommonCodeTypeUseCase |
| Service | SearchCommonCodeTypeService |

### 호출 흐름 다이어그램

```
CommonCodeTypeQueryController.search(SearchCommonCodeTypesPageApiRequest)
  |- CommonCodeTypeQueryApiMapper.toSearchParams(request)
  |   +-- -> CommonCodeTypeSearchParams
  |
  |- SearchCommonCodeTypeUseCase.execute(params)                    [Port]
  |   +-- SearchCommonCodeTypeService.execute(params)                [Impl]
  |       |
  |       |- CommonCodeTypeQueryFactory.createCriteria(params)
  |       |   +-- -> CommonCodeTypeSearchCriteria
  |       |
  |       |- CommonCodeTypeReadManager.findByCriteria(criteria)
  |       |   +-- CommonCodeTypeQueryPort.findByCriteria(criteria)   [Port]
  |       |       +-- CommonCodeTypeQueryAdapter.findByCriteria()     [Impl]
  |       |           +-- CommonCodeTypeQueryDslRepository.findByCriteria()
  |       |               +-- QueryDSL Query Execution
  |       |               +-- CommonCodeTypeJpaEntityMapper.toDomain()
  |       |               +-- -> List<CommonCodeType>
  |       |
  |       |- CommonCodeTypeReadManager.countByCriteria(criteria)
  |       |   +-- CommonCodeTypeQueryPort.countByCriteria(criteria)
  |       |       +-- CommonCodeTypeQueryAdapter.countByCriteria()
  |       |           +-- CommonCodeTypeQueryDslRepository.countByCriteria()
  |       |               +-- -> long totalElements
  |       |
  |       +-- CommonCodeTypeAssembler.toPageResult(domains, page, size, total)
  |           +-- -> CommonCodeTypePageResult
  |
  +-- CommonCodeTypeQueryApiMapper.toPageResponse(pageResult)
      +-- -> PageApiResponse<CommonCodeTypeApiResponse>
```

### Layer별 상세

#### Adapter-In (Controller)

**파일**: `CommonCodeTypeQueryController.java`

```java
@GetMapping
public ResponseEntity<ApiResponse<PageApiResponse<CommonCodeTypeApiResponse>>> search(
        @Valid SearchCommonCodeTypesPageApiRequest request) {

    CommonCodeTypeSearchParams searchParams = mapper.toSearchParams(request);
    CommonCodeTypePageResult pageResult = searchCommonCodeTypeUseCase.execute(searchParams);
    PageApiResponse<CommonCodeTypeApiResponse> response = mapper.toPageResponse(pageResult);

    return ResponseEntity.ok(ApiResponse.of(response));
}
```

**Request DTO**: `SearchCommonCodeTypesPageApiRequest`
- `active`: Boolean (활성화 여부 필터)
- `searchField`: CommonCodeTypeSearchField (검색 필드)
- `searchWord`: String (검색어)
- `type`: Long (CommonCodeType ID로 필터링)
- `sortKey`: String (정렬 기준)
- `sortDirection`: String (정렬 방향)
- `page`: Integer (페이지 번호, 기본값 0)
- `size`: Integer (페이지 크기, 기본값 20)

**Response DTO**: `CommonCodeTypeApiResponse`
- `id`: Long
- `code`: String
- `name`: String
- `description`: String
- `displayOrder`: int
- `active`: boolean
- `createdAt`: String (ISO-8601)
- `updatedAt`: String (ISO-8601)

**Mapper**: `CommonCodeTypeQueryApiMapper`
- `toSearchParams()`: Request → Application DTO 변환
- `toPageResponse()`: Application Result → API Response 변환
- `toResponse()`: 단일 Result → Response 변환

#### Application Layer

**UseCase Interface**: `SearchCommonCodeTypeUseCase`
```java
public interface SearchCommonCodeTypeUseCase {
    CommonCodeTypePageResult execute(CommonCodeTypeSearchParams params);
}
```

**Service**: `SearchCommonCodeTypeService`
```java
@Override
public CommonCodeTypePageResult execute(CommonCodeTypeSearchParams params) {
    CommonCodeTypeSearchCriteria criteria = queryFactory.createCriteria(params);

    List<CommonCodeType> domains = readManager.findByCriteria(criteria);
    long totalElements = readManager.countByCriteria(criteria);

    return assembler.toPageResult(domains, params.page(), params.size(), totalElements);
}
```

**주요 컴포넌트**:
- `CommonCodeTypeQueryFactory`: Params → Criteria 변환
- `CommonCodeTypeReadManager`: Port 호출 + 트랜잭션 관리
- `CommonCodeTypeAssembler`: Domain → Result 변환

**Application DTO**:
- `CommonCodeTypeSearchParams`: API 계층 검색 조건
- `CommonCodeTypePageResult`: 페이징 결과
- `CommonCodeTypeResult`: 단일 결과 DTO

#### Domain Layer

**Domain Port**: `CommonCodeTypeQueryPort`
```java
List<CommonCodeType> findByCriteria(CommonCodeTypeSearchCriteria criteria);
long countByCriteria(CommonCodeTypeSearchCriteria criteria);
```

**Domain Objects**:
- `CommonCodeTypeSearchCriteria`: 도메인 검색 조건
  - `active`: Boolean 필터
  - `searchField` + `searchWord`: 검색 조건
  - `type`: Long (CommonCodeType ID)
  - `queryContext`: 페이징, 정렬, 삭제 포함 여부

- `CommonCodeType`: Aggregate Root
  - VO들: `CommonCodeTypeId`, `CommonCodeTypeCode`, `CommonCodeTypeName`, etc.
  - 비즈니스 메서드: `update()`, `changeActiveStatus()`, `delete()`, `restore()`

#### Adapter-Out (Persistence)

**Adapter**: `CommonCodeTypeQueryAdapter`
```java
@Override
public List<CommonCodeType> findByCriteria(CommonCodeTypeSearchCriteria criteria) {
    List<CommonCodeTypeJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
    return entities.stream().map(mapper::toDomain).toList();
}

@Override
public long countByCriteria(CommonCodeTypeSearchCriteria criteria) {
    return queryDslRepository.countByCriteria(criteria);
}
```

**Repository**: `CommonCodeTypeQueryDslRepository`

**QueryDSL 쿼리**:
```java
public List<CommonCodeTypeJpaEntity> findByCriteria(CommonCodeTypeSearchCriteria criteria) {
    var qc = criteria.queryContext();
    return queryFactory
        .selectFrom(commonCodeTypeJpaEntity)
        .where(
            deletedAtFilter(criteria),           // deletedAt IS NULL (기본)
            conditionBuilder.activeEq(criteria), // active = ?
            conditionBuilder.searchCondition(criteria), // name LIKE ? OR code LIKE ?
            conditionBuilder.typeHasCommonCodeValue(criteria) // EXISTS (하위 공통 코드)
        )
        .orderBy(createOrderSpecifier(qc.sortKey(), qc.sortDirection()))
        .offset(criteria.offset())
        .limit(criteria.size())
        .fetch();
}
```

**WHERE 조건**:
- `deletedAt IS NULL`: Soft Delete 필터 (기본)
- `active = ?`: 활성화 여부
- `name LIKE ? OR code LIKE ?`: 검색어
- `EXISTS (SELECT 1 FROM common_code WHERE ...)`: type 필터

**ORDER BY**:
- `CREATED_AT`, `DISPLAY_ORDER`, `CODE` 지원

**JPA Entity**: `CommonCodeTypeJpaEntity`
- `@Table(name = "common_code_type")`
- `BaseAuditEntity` 상속 → `createdAt`, `updatedAt`
- `SoftDeletableEntity` 가능성 → `deletedAt`

**Mapper**: `CommonCodeTypeJpaEntityMapper`
- `toDomain()`: Entity → Domain
- `toEntity()`: Domain → Entity

---

## 2. POST 등록

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | /api/v1/market/admin/common-code-types |
| Controller | CommonCodeTypeCommandController |
| Method | register |
| UseCase | RegisterCommonCodeTypeUseCase |
| Service | RegisterCommonCodeTypeService |

### 호출 흐름 다이어그램

```
CommonCodeTypeCommandController.register(RegisterCommonCodeTypeApiRequest)
  |- CommonCodeTypeCommandApiMapper.toCommand(request)
  |   +-- -> RegisterCommonCodeTypeCommand
  |
  |- RegisterCommonCodeTypeUseCase.execute(command)                 [Port]
  |   +-- RegisterCommonCodeTypeService.execute(command)             [Impl]
  |       |
  |       |- CommonCodeTypeValidator.validateCodeNotDuplicate(code)
  |       |   +-- CommonCodeTypeReadManager.existsByCode(code)
  |       |       +-- CommonCodeTypeQueryPort.existsByCode(code)
  |       |           +-- CommonCodeTypeQueryAdapter.existsByCode(code)
  |       |               +-- CommonCodeTypeQueryDslRepository.existsByCode(code)
  |       |                   +-- QueryDSL: SELECT 1 WHERE code = ? LIMIT 1
  |       |                   +-- throws DuplicateCommonCodeTypeCodeException (중복 시)
  |       |
  |       |- CommonCodeTypeCommandFactory.create(command)
  |       |   +-- CommonCodeType.forNew(code, name, description, displayOrder, now)
  |       |   +-- -> CommonCodeType (신규, id.isNew() = true)
  |       |
  |       +-- CommonCodeTypeCommandManager.persist(commonCodeType)
  |           +-- CommonCodeTypeCommandPort.persist(commonCodeType)  [Port]
  |               +-- CommonCodeTypeCommandAdapter.persist(...)       [Impl]
  |                   +-- CommonCodeTypeJpaEntityMapper.toEntity(commonCodeType)
  |                   +-- CommonCodeTypeJpaRepository.save(entity)
  |                   +-- -> Long (생성된 ID)
  |
  +-- return 201 CREATED with ApiResponse<Long>
```

### Layer별 상세

#### Adapter-In

**Request DTO**: `RegisterCommonCodeTypeApiRequest`
```java
record RegisterCommonCodeTypeApiRequest(
    @NotBlank String code,
    @NotBlank String name,
    String description,
    @Min(0) int displayOrder
) {}
```

**Validation**:
- `code`: 필수, 영문 대문자 + 언더스코어
- `name`: 필수
- `description`: 선택
- `displayOrder`: 0 이상

**Controller 응답**:
```java
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(createdId));
```

#### Application Layer

**Command DTO**: `RegisterCommonCodeTypeCommand`
```java
public record RegisterCommonCodeTypeCommand(
    String code,
    String name,
    String description,
    int displayOrder
) {}
```

**Service 로직**:
```java
@Override
public Long execute(RegisterCommonCodeTypeCommand command) {
    validator.validateCodeNotDuplicate(command.code());

    CommonCodeType commonCodeType = commandFactory.create(command);
    return commandManager.persist(commonCodeType);
}
```

**검증**: `CommonCodeTypeValidator`
- `validateCodeNotDuplicate()`: 코드 중복 확인
  - 중복 시 `DuplicateCommonCodeTypeCodeException` 발생

**Factory**: `CommonCodeTypeCommandFactory`
- `create()`: Command → Domain Aggregate 생성
- TimeProvider를 통해 `createdAt`, `updatedAt` 설정

**Manager**: `CommonCodeTypeCommandManager`
- `@Transactional` 경계
- Port 호출

#### Domain Layer

**Aggregate**: `CommonCodeType`

**Factory Method**:
```java
public static CommonCodeType forNew(
        String code, String name, String description, int displayOrder, Instant now) {
    return new CommonCodeType(
        CommonCodeTypeId.forNew(),  // isNew() = true
        CommonCodeTypeCode.of(code),
        CommonCodeTypeName.of(name),
        CommonCodeTypeDescription.of(description),
        DisplayOrder.of(displayOrder),
        true,  // active = true
        DeletionStatus.active(),
        now,
        now
    );
}
```

**VO 검증**:
- `CommonCodeTypeCode`: 영문 대문자 + 언더스코어 패턴
- `CommonCodeTypeName`: 길이 제한
- `DisplayOrder`: 0 이상

#### Adapter-Out

**Port**: `CommonCodeTypeCommandPort`
```java
Long persist(CommonCodeType commonCodeType);
```

**Adapter**: `CommonCodeTypeCommandAdapter`
```java
@Override
public Long persist(CommonCodeType commonCodeType) {
    CommonCodeTypeJpaEntity entity = mapper.toEntity(commonCodeType);
    CommonCodeTypeJpaEntity saved = repository.save(entity);
    return saved.getId();
}
```

**JPA Repository**:
- `CommonCodeTypeJpaRepository extends JpaRepository<CommonCodeTypeJpaEntity, Long>`
- `save()` 메서드만 사용 (PER-REP-001)

**Database Operation**:
```sql
INSERT INTO common_code_type (
    code, name, description, display_order,
    active, created_at, updated_at
) VALUES (?, ?, ?, ?, true, ?, ?)
```

---

## 3. PUT 수정

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PUT |
| Path | /api/v1/market/admin/common-code-types/{id} |
| Controller | CommonCodeTypeCommandController |
| Method | update |
| UseCase | UpdateCommonCodeTypeUseCase |
| Service | UpdateCommonCodeTypeService |

### 호출 흐름 다이어그램

```
CommonCodeTypeCommandController.update(commonCodeTypeId, UpdateCommonCodeTypeApiRequest)
  |- CommonCodeTypeCommandApiMapper.toCommand(commonCodeTypeId, request)
  |   +-- -> UpdateCommonCodeTypeCommand
  |
  |- UpdateCommonCodeTypeUseCase.execute(command)                   [Port]
  |   +-- UpdateCommonCodeTypeService.execute(command)               [Impl]
  |       |
  |       |- CommonCodeTypeValidator.validateDisplayOrderNotDuplicate(displayOrder, id)
  |       |   +-- CommonCodeTypeReadManager.existsByDisplayOrderExcludingId(displayOrder, id)
  |       |       +-- CommonCodeTypeQueryPort.existsByDisplayOrderExcludingId(...)
  |       |           +-- QueryDSL: SELECT 1 WHERE display_order = ? AND id != ? LIMIT 1
  |       |           +-- throws DuplicateCommonCodeTypeDisplayOrderException (중복 시)
  |       |
  |       |- CommonCodeTypeCommandFactory.createUpdateContext(command)
  |       |   +-- -> UpdateContext<CommonCodeTypeId, CommonCodeTypeUpdateData>
  |       |       - id: CommonCodeTypeId
  |       |       - updateData: CommonCodeTypeUpdateData (name, description, displayOrder)
  |       |       - changedAt: Instant (현재 시간)
  |       |
  |       |- CommonCodeTypeValidator.findExistingOrThrow(context.id())
  |       |   +-- CommonCodeTypeReadManager.getById(id)
  |       |       +-- CommonCodeTypeQueryPort.findById(id)
  |       |           +-- QueryDSL: SELECT * WHERE id = ?
  |       |           +-- throws CommonCodeTypeNotFoundException (없으면)
  |       |           +-- -> CommonCodeType
  |       |
  |       |- CommonCodeType.update(context.updateData(), context.changedAt())
  |       |   +-- Domain 객체 상태 변경
  |       |       - this.name = updateData.name()
  |       |       - this.description = updateData.description()
  |       |       - this.displayOrder = updateData.displayOrder()
  |       |       - this.updatedAt = now
  |       |
  |       +-- CommonCodeTypeCommandManager.persist(commonCodeType)
  |           +-- CommonCodeTypeCommandPort.persist(commonCodeType)
  |               +-- CommonCodeTypeCommandAdapter.persist(...)
  |                   +-- CommonCodeTypeJpaEntityMapper.toEntity(commonCodeType)
  |                   +-- JpaRepository.save(entity)
  |                       +-- UPDATE common_code_type SET name=?, description=?, display_order=?, updated_at=? WHERE id=?
  |
  +-- return 200 OK with ApiResponse<Void>
```

### Layer별 상세

#### Adapter-In

**Request DTO**: `UpdateCommonCodeTypeApiRequest`
```java
record UpdateCommonCodeTypeApiRequest(
    @NotBlank String name,
    String description,
    @Min(0) int displayOrder
) {}
```

**PathVariable**: `commonCodeTypeId` (Long)

**API-DTO-004**: Update Request에 ID 포함 금지 → PathVariable에서 전달

#### Application Layer

**Command DTO**: `UpdateCommonCodeTypeCommand`
```java
public record UpdateCommonCodeTypeCommand(
    Long id,
    String name,
    String description,
    int displayOrder
) {}
```

**Service 로직**:
```java
@Override
public void execute(UpdateCommonCodeTypeCommand command) {
    validator.validateDisplayOrderNotDuplicate(command.displayOrder(), command.id());

    UpdateContext<CommonCodeTypeId, CommonCodeTypeUpdateData> context =
            commandFactory.createUpdateContext(command);

    CommonCodeType commonCodeType = validator.findExistingOrThrow(context.id());
    commonCodeType.update(context.updateData(), context.changedAt());

    commandManager.persist(commonCodeType);
}
```

**검증**:
- `validateDisplayOrderNotDuplicate()`: 자신을 제외한 displayOrder 중복 확인
- `findExistingOrThrow()`: 존재 여부 검증 + Domain 객체 반환

**Factory Pattern**:
- `createUpdateContext()`: ID, UpdateData, changedAt을 한 번에 생성 (FAC-008)
- `UpdateContext` 사용으로 TimeProvider 일관성 보장

#### Domain Layer

**UpdateData**: `CommonCodeTypeUpdateData`
```java
public record CommonCodeTypeUpdateData(
    CommonCodeTypeName name,
    CommonCodeTypeDescription description,
    DisplayOrder displayOrder
) {}
```

**Aggregate 메서드**:
```java
public void update(CommonCodeTypeUpdateData updateData, Instant now) {
    this.name = updateData.name();
    this.description = updateData.description();
    this.displayOrder = updateData.displayOrder();
    this.updatedAt = now;
}
```

**불변성**: `code`는 수정 불가 (생성 시에만 설정)

---

## 4. PATCH 상태 변경

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PATCH |
| Path | /api/v1/market/admin/common-code-types/active-status |
| Controller | CommonCodeTypeCommandController |
| Method | changeActiveStatus |
| UseCase | ChangeCommonCodeTypeStatusUseCase |
| Service | ChangeCommonCodeTypeStatusService |

### 호출 흐름 다이어그램

```
CommonCodeTypeCommandController.changeActiveStatus(ChangeActiveStatusApiRequest)
  |- CommonCodeTypeCommandApiMapper.toCommand(request)
  |   +-- -> ChangeActiveStatusCommand
  |
  |- ChangeCommonCodeTypeStatusUseCase.execute(command)             [Port]
  |   +-- ChangeCommonCodeTypeStatusService.execute(command)         [Impl]
  |       |
  |       |- CommonCodeTypeCommandFactory.createStatusChangeContexts(command)
  |       |   +-- -> List<StatusChangeContext<CommonCodeTypeId>>
  |       |       - id: CommonCodeTypeId
  |       |       - changedAt: Instant (모든 context에 동일한 시간)
  |       |
  |       |- CommonCodeTypeValidator.findAllExistingOrThrow(ids)
  |       |   +-- CommonCodeTypeReadManager.getByIds(ids)
  |       |       +-- CommonCodeTypeQueryPort.findByIds(ids)
  |       |           +-- QueryDSL: SELECT * WHERE id IN (?, ?, ...)
  |       |           +-- throws CommonCodeTypeNotFoundException (없으면)
  |       |           +-- -> List<CommonCodeType> (ID 순서 보장)
  |       |
  |       |- if (!command.active()) {  // 비활성화 시에만
  |       |   for (CommonCodeType cct : commonCodeTypes) {
  |       |       CommonCodeTypeValidator.validateNoActiveCommonCodes(cct.id().value())
  |       |           +-- CommonCodeReadManager.existsActiveByCommonCodeTypeId(id)
  |       |               +-- throws ActiveCommonCodesExistException (활성 공통 코드 존재 시)
  |       |   }
  |       | }
  |       |
  |       |- Instant changedAt = contexts.get(0).changedAt();  // 동일한 시간 사용
  |       |
  |       |- for (CommonCodeType cct : commonCodeTypes) {
  |       |   cct.changeActiveStatus(command.active(), changedAt);
  |       |       +-- Domain 객체 상태 변경
  |       |           - this.active = active
  |       |           - this.updatedAt = now
  |       | }
  |       |
  |       +-- CommonCodeTypeCommandManager.persistAll(commonCodeTypes)
  |           +-- CommonCodeTypeCommandPort.persistAll(commonCodeTypes)
  |               +-- CommonCodeTypeCommandAdapter.persistAll(...)
  |                   +-- List<Entity> entities = commonCodeTypes.stream().map(mapper::toEntity).toList()
  |                   +-- JpaRepository.saveAll(entities)
  |                       +-- UPDATE common_code_type SET active=?, updated_at=? WHERE id=? (일괄)
  |
  +-- return 200 OK with ApiResponse<Void>
```

### Layer별 상세

#### Adapter-In

**Request DTO**: `ChangeActiveStatusApiRequest`
```java
record ChangeActiveStatusApiRequest(
    @NotEmpty List<Long> ids,
    @NotNull Boolean active
) {}
```

**요청 예시**:
```json
{
  "ids": [1, 2, 3],
  "active": false
}
```

#### Application Layer

**Command DTO**: `ChangeActiveStatusCommand`
```java
public record ChangeActiveStatusCommand(
    List<Long> ids,
    boolean active
) {}
```

**Service 로직**:
```java
@Override
public void execute(ChangeActiveStatusCommand command) {
    List<StatusChangeContext<CommonCodeTypeId>> contexts =
            commandFactory.createStatusChangeContexts(command);

    if (contexts.isEmpty()) {
        return;
    }

    List<CommonCodeTypeId> ids = contexts.stream().map(StatusChangeContext::id).toList();
    List<CommonCodeType> commonCodeTypes = validator.findAllExistingOrThrow(ids);

    // 비활성화 시 활성화된 하위 공통 코드 존재 여부 검증
    if (!command.active()) {
        for (CommonCodeType cct : commonCodeTypes) {
            validator.validateNoActiveCommonCodes(cct.id().value());
        }
    }

    // changedAt은 Factory에서 동일한 시간으로 생성됨
    Instant changedAt = contexts.get(0).changedAt();

    for (CommonCodeType cct : commonCodeTypes) {
        cct.changeActiveStatus(command.active(), changedAt);
    }

    commandManager.persistAll(commonCodeTypes);
}
```

**특이사항**:
- **일괄 처리**: 여러 ID를 한 번에 처리
- **동일 시간**: 모든 변경에 동일한 `changedAt` 사용 (일관성)
- **비즈니스 규칙**: 비활성화 시 활성화된 하위 공통 코드 존재 여부 검증

**검증**:
- `findAllExistingOrThrow()`: ID 목록 존재 여부 검증 + ID 순서 보장
- `validateNoActiveCommonCodes()`: 활성화된 하위 공통 코드 존재 확인

**Factory Pattern**:
- `createStatusChangeContexts()`: `List<StatusChangeContext>` 생성
- `StatusChangeContext`: id + changedAt (모든 항목에 동일한 시간)

#### Domain Layer

**Aggregate 메서드**:
```java
public void changeActiveStatus(boolean active, Instant now) {
    this.active = active;
    this.updatedAt = now;
}
```

**비즈니스 규칙**:
- 공통 코드 타입을 비활성화하려면 해당 타입의 모든 하위 공통 코드가 비활성화되어 있어야 함
- 도메인 예외: `ActiveCommonCodesExistException`

#### Adapter-Out

**Port**: `CommonCodeTypeCommandPort`
```java
void persistAll(List<CommonCodeType> commonCodeTypes);
```

**Adapter**: `CommonCodeTypeCommandAdapter`
```java
@Override
public void persistAll(List<CommonCodeType> commonCodeTypes) {
    List<CommonCodeTypeJpaEntity> entities =
            commonCodeTypes.stream().map(mapper::toEntity).toList();
    repository.saveAll(entities);
}
```

**JPA Batch Update**:
```sql
UPDATE common_code_type
SET active = ?, updated_at = ?
WHERE id = ?
-- (여러 건 일괄 실행)
```

---

## 아키텍처 특징 정리

### Hexagonal 레이어 격리

1. **Adapter-In** → **Application** → **Domain** → **Adapter-Out**
2. 각 레이어의 DTO가 다름:
   - `ApiRequest` → `Command/Params` → `Criteria` → `Entity`
   - `Entity` → `Domain` → `Result` → `ApiResponse`

### CQRS 분리

| 측면 | Command | Query |
|------|---------|-------|
| Controller | `CommonCodeTypeCommandController` | `CommonCodeTypeQueryController` |
| Mapper | `CommonCodeTypeCommandApiMapper` | `CommonCodeTypeQueryApiMapper` |
| Service | `RegisterService`, `UpdateService`, etc. | `SearchService` |
| Port | `CommonCodeTypeCommandPort` | `CommonCodeTypeQueryPort` |
| Adapter | `CommonCodeTypeCommandAdapter` | `CommonCodeTypeQueryAdapter` |
| Repository | `CommonCodeTypeJpaRepository` | `CommonCodeTypeQueryDslRepository` |

### Port 패턴

- **Application Port (UseCase)**: `SearchCommonCodeTypeUseCase`, `RegisterCommonCodeTypeUseCase`, etc.
- **Domain Port**: `CommonCodeTypeQueryPort`, `CommonCodeTypeCommandPort`
- Port는 인터페이스, 구현은 Adapter에서

### Mapper 체인

1. **Adapter-In**: `ApiRequest` → `Command/Params` (CommandApiMapper, QueryApiMapper)
2. **Application**: Factory에서 `Command` → `Domain`, Assembler에서 `Domain` → `Result`
3. **Adapter-Out**: `Domain` ↔ `Entity` (JpaEntityMapper)

### Manager 계층

- **역할**: Port 호출 + 트랜잭션 경계 (@Transactional)
- **ReadManager**: QueryPort 호출, `@Transactional(readOnly = true)`
- **CommandManager**: CommandPort 호출, `@Transactional`

### Validator 패턴

- **역할**: 도메인 규칙 검증 + Domain 객체 반환
- **APP-VAL-001**: 검증 성공 시 Domain 객체 반환
- **APP-VAL-002**: 도메인 전용 예외 발생
- 예시: `validateCodeNotDuplicate()`, `findExistingOrThrow()`, `validateNoActiveCommonCodes()`

### Factory 패턴

- **역할**: Command → Domain 변환, TimeProvider 관리
- **APP-TIM-001**: Service에서 TimeProvider 직접 사용 금지 → Factory에서 처리
- **FAC-008**: `createUpdateContext()`, `createStatusChangeContexts()` - ID, Data, 시간을 한 번에 생성

### QueryDSL 패턴

- **ConditionBuilder**: BooleanExpression 분리 (PER-CND-001)
- **동적 쿼리**: WHERE 조건을 메서드로 분리
- **정렬**: `createOrderSpecifier()` - SortKey별 분기
- **Soft Delete**: `deletedAt IS NULL` 기본 필터

### 트랜잭션 경계

- **Controller**: `@Transactional` 금지 (API-CTR-005)
- **Adapter**: `@Transactional` 금지 (PER-ADP-002)
- **Service**: 트랜잭션 경계 아님
- **Manager**: `@Transactional` 경계

### Domain 불변성

- **생성**: `forNew()` - 신규 생성
- **복원**: `reconstitute()` - 영속성 계층에서 복원
- **수정**: `update()`, `changeActiveStatus()`, `delete()`, `restore()` - 메서드를 통해서만 상태 변경
- **ID 불변**: `CommonCodeTypeId`는 생성 후 변경 불가
- **Code 불변**: `code`는 생성 후 수정 불가

### 에러 처리

| 예외 | 발생 위치 | HTTP Status |
|------|----------|-------------|
| `DuplicateCommonCodeTypeCodeException` | Validator | 409 CONFLICT |
| `DuplicateCommonCodeTypeDisplayOrderException` | Validator | 409 CONFLICT |
| `CommonCodeTypeNotFoundException` | Validator | 404 NOT FOUND |
| `ActiveCommonCodesExistException` | Validator | 400 BAD REQUEST |

**ErrorMapper**: `CommonCodeTypeErrorMapper` → `ErrorMapperRegistry`에 등록

---

## Database 쿼리 분석

### 테이블 구조

```sql
CREATE TABLE common_code_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### 주요 쿼리

**1. 목록 조회**
```sql
SELECT *
FROM common_code_type
WHERE deleted_at IS NULL
  AND active = ?
  AND (name LIKE ? OR code LIKE ?)
  AND EXISTS (
      SELECT 1 FROM common_code
      WHERE common_code.common_code_type_id = common_code_type.id
  )
ORDER BY display_order ASC
LIMIT ? OFFSET ?;
```

**2. 개수 조회**
```sql
SELECT COUNT(*)
FROM common_code_type
WHERE deleted_at IS NULL
  AND active = ?
  AND (name LIKE ? OR code LIKE ?);
```

**3. 코드 중복 확인**
```sql
SELECT 1
FROM common_code_type
WHERE code = ?
LIMIT 1;
```

**4. DisplayOrder 중복 확인 (수정 시)**
```sql
SELECT 1
FROM common_code_type
WHERE display_order = ?
  AND id != ?
LIMIT 1;
```

**5. ID 목록 조회**
```sql
SELECT *
FROM common_code_type
WHERE id IN (?, ?, ?);
```

**6. 활성 공통 코드 존재 확인**
```sql
SELECT 1
FROM common_code
WHERE common_code_type_id = ?
  AND active = true
LIMIT 1;
```

**7. 등록**
```sql
INSERT INTO common_code_type (
    code, name, description, display_order,
    active, created_at, updated_at
) VALUES (?, ?, ?, ?, true, ?, ?);
```

**8. 수정**
```sql
UPDATE common_code_type
SET name = ?,
    description = ?,
    display_order = ?,
    updated_at = ?
WHERE id = ?;
```

**9. 상태 변경 (일괄)**
```sql
UPDATE common_code_type
SET active = ?,
    updated_at = ?
WHERE id = ?;
-- (여러 건 반복)
```

### 인덱스 추천

```sql
-- 코드 유니크 인덱스 (중복 검증)
CREATE UNIQUE INDEX idx_common_code_type_code ON common_code_type(code);

-- 표시 순서 인덱스 (정렬, 중복 검증)
CREATE INDEX idx_common_code_type_display_order ON common_code_type(display_order);

-- 활성화 + Soft Delete 복합 인덱스 (목록 조회)
CREATE INDEX idx_common_code_type_active_deleted
ON common_code_type(active, deleted_at);

-- 생성일시 인덱스 (정렬)
CREATE INDEX idx_common_code_type_created_at ON common_code_type(created_at);
```

---

## 참고 사항

### 컨벤션 준수

- **API-CTR-001**: Controller는 @RestController로 정의
- **API-CTR-003**: UseCase(Port-In) 인터페이스 의존
- **API-CTR-004**: ResponseEntity<ApiResponse<T>> 래핑 필수
- **API-CTR-005**: Controller에서 @Transactional 금지
- **API-CTR-007**: Controller 비즈니스 로직 금지
- **API-CTR-009**: @Valid 어노테이션 필수
- **API-CTR-010**: CQRS Controller 분리
- **API-CTR-011**: List 직접 반환 금지 → PageApiResponse 페이징 필수
- **API-CTR-012**: URL 경로 소문자 + 복수형
- **APP-TIM-001**: TimeProvider 직접 사용 금지 - Factory에서 처리
- **APP-VAL-001**: 검증 성공 시 Domain 객체 반환
- **APP-VAL-002**: 도메인 전용 예외 발생
- **FAC-008**: createUpdateContext()로 ID, UpdateData, changedAt 한 번에 생성
- **PER-ADP-001**: CommandAdapter는 JpaRepository만 사용
- **PER-ADP-002**: Adapter에서 @Transactional 금지
- **PER-ADP-003**: Domain 반환 (DTO 반환 금지)
- **PER-ADP-004**: QueryAdapter는 QueryDslRepository만 사용
- **PER-REP-001**: JpaRepository는 save/saveAll만 사용
- **PER-REP-003**: 모든 조회는 QueryDslRepository
- **PER-CND-001**: BooleanExpression은 ConditionBuilder로 분리

### 주요 설계 결정

1. **CQRS 철저 분리**: Controller, Mapper, Service, Port, Adapter, Repository 모두 Command/Query 분리
2. **Manager 계층**: Port 호출 + 트랜잭션 경계 역할
3. **Validator 패턴**: 검증 + Domain 조회를 함께 수행하여 재조회 방지
4. **Factory 패턴**: TimeProvider 관리 + Context 객체 생성
5. **Assembler 패턴**: Domain → Result 변환
6. **일괄 처리 최적화**: `persistAll()`, `findAllExistingOrThrow()`
7. **동일 시간 보장**: StatusChangeContext로 모든 변경에 동일한 시간 적용

### 테스트 작성 시 참고

- **Controller 테스트**: `@WebMvcTest` + `@MockitoBean ErrorMapperRegistry` 필수
- **Service 테스트**: Manager, Validator, Factory 모킹
- **Manager 테스트**: Port 모킹
- **Validator 테스트**: Manager 모킹
- **Adapter 테스트**: Repository 모킹
- **Repository 테스트**: `@DataJpaTest` + QueryDSL 설정

---

## 문서 작성 정보

- **작성일**: 2026-02-06
- **분석 대상**: CommonCodeType 도메인 전체 API
- **레이어**: Adapter-In → Application → Domain → Adapter-Out
- **패턴**: Hexagonal, CQRS, DDD
