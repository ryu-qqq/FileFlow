# CommonCode API 호출 흐름 분석

공통 코드 도메인의 API 호출 흐름을 Hexagonal 아키텍처 레이어별로 추적한 문서입니다.

---

## 목차

1. [GET /api/v1/market/admin/common-codes - 공통 코드 목록 조회](#1-get-공통-코드-목록-조회)
2. [POST /api/v1/market/admin/common-codes - 공통 코드 등록](#2-post-공통-코드-등록)
3. [PUT /api/v1/market/admin/common-codes/{id} - 공통 코드 수정](#3-put-공통-코드-수정)
4. [PATCH /api/v1/market/admin/common-codes/active-status - 활성화 상태 변경](#4-patch-활성화-상태-변경)

---

## 1. GET 공통 코드 목록 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /api/v1/market/admin/common-codes |
| Controller | CommonCodeQueryController |
| Method | search |
| UseCase | SearchCommonCodeUseCase |
| Service | SearchCommonCodeService |

---

### 호출 흐름 다이어그램

```
CommonCodeQueryController.search(SearchCommonCodesPageApiRequest)
  |- CommonCodeQueryApiMapper.toSearchParams(request)
  |   +-- -> CommonCodeSearchParams
  |- SearchCommonCodeUseCase.execute(params)                       [Port]
  |   +-- SearchCommonCodeService.execute(params)                  [Impl]
  |       |- CommonCodeQueryFactory.createCriteria(params)
  |       |   +-- -> CommonCodeSearchCriteria
  |       |- CommonCodeReadManager.findByCriteria(criteria)
  |       |   +-- CommonCodeQueryPort.findByCriteria(criteria)     [Port]
  |       |       +-- CommonCodeQueryAdapter.findByCriteria()      [Impl]
  |       |           +-- CommonCodeQueryDslRepository.findByCriteria()
  |       |               +-- QueryDSL: WHERE + ORDER BY + OFFSET + LIMIT
  |       |- CommonCodeReadManager.countByCriteria(criteria)
  |       |   +-- CommonCodeQueryPort.countByCriteria(criteria)
  |       |       +-- CommonCodeQueryAdapter.countByCriteria()
  |       |           +-- CommonCodeQueryDslRepository.countByCriteria()
  |       |               +-- QueryDSL: COUNT(*)
  |       +-- CommonCodeAssembler.toPageResult(domains, page, size, total)
  |           +-- -> CommonCodePageResult
  +-- CommonCodeQueryApiMapper.toPageResponse(pageResult)
      +-- -> ApiResponse<PageApiResponse<CommonCodeApiResponse>>
```

---

### Layer별 상세

#### Adapter-In Layer

**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/commoncode/`

**Controller**: `CommonCodeQueryController`

```java
@GetMapping
public ResponseEntity<ApiResponse<PageApiResponse<CommonCodeApiResponse>>> search(
        @Valid SearchCommonCodesPageApiRequest request) {

    CommonCodeSearchParams searchParams = mapper.toSearchParams(request);
    CommonCodePageResult pageResult = searchCommonCodeUseCase.execute(searchParams);
    PageApiResponse<CommonCodeApiResponse> response = mapper.toPageResponse(pageResult);

    return ResponseEntity.ok(ApiResponse.of(response));
}
```

**Request DTO**: `SearchCommonCodesPageApiRequest`

| 필드 | 타입 | 설명 | 필수 |
|------|------|------|------|
| commonCodeTypeId | Long | 공통 코드 타입 ID | Y |
| active | Boolean | 활성화 여부 필터 | N |
| code | String | 코드값 검색 | N |
| sortKey | String | 정렬 키 | N |
| sortDirection | String | 정렬 방향 | N |
| page | Integer | 페이지 번호 | N (기본 0) |
| size | Integer | 페이지 크기 | N (기본 20) |

**Response DTO**: `CommonCodeApiResponse`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 공통 코드 ID |
| commonCodeTypeId | Long | 공통 코드 타입 ID |
| code | String | 코드값 |
| displayName | String | 표시명 |
| displayOrder | Integer | 표시 순서 |
| active | Boolean | 활성화 여부 |
| createdAt | String | 생성일시 (ISO-8601) |
| updatedAt | String | 수정일시 (ISO-8601) |

**ApiMapper**: `CommonCodeQueryApiMapper`

```java
public CommonCodeSearchParams toSearchParams(SearchCommonCodesPageApiRequest request) {
    int page = request.page() != null ? request.page() : DEFAULT_PAGE;
    int size = request.size() != null ? request.size() : DEFAULT_SIZE;

    CommonSearchParams searchParams =
        CommonSearchParams.of(null, null, null, request.sortKey(), request.sortDirection(), page, size);

    return CommonCodeSearchParams.of(
        request.commonCodeTypeId(),
        request.active(),
        request.code(),
        searchParams);
}
```

---

#### Application Layer

**UseCase Interface**: `SearchCommonCodeUseCase`

```java
public interface SearchCommonCodeUseCase {
    CommonCodePageResult execute(CommonCodeSearchParams params);
}
```

**Service 구현체**: `SearchCommonCodeService`

```java
@Service
public class SearchCommonCodeService implements SearchCommonCodeUseCase {

    private final CommonCodeReadManager readManager;
    private final CommonCodeQueryFactory queryFactory;
    private final CommonCodeAssembler assembler;

    @Override
    public CommonCodePageResult execute(CommonCodeSearchParams params) {
        // 1. Params -> Criteria 변환 (Factory)
        CommonCodeSearchCriteria criteria = queryFactory.createCriteria(params);

        // 2. Domain 조회 (Manager)
        List<CommonCode> domains = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        // 3. Result 조립 (Assembler)
        return assembler.toPageResult(domains, params.page(), params.size(), totalElements);
    }
}
```

**Params DTO**: `CommonCodeSearchParams`

| 필드 | 타입 | 설명 |
|------|------|------|
| commonCodeTypeId | Long | 공통 코드 타입 ID |
| active | Boolean | 활성화 여부 |
| code | String | 코드 검색어 |
| searchParams | CommonSearchParams | 페이징/정렬 정보 |

**Result DTO**: `CommonCodePageResult`

| 필드 | 타입 | 설명 |
|------|------|------|
| results | List<CommonCodeResult> | 조회 결과 목록 |
| pageMeta | PageMeta | 페이징 메타 정보 |

**Manager**: `CommonCodeReadManager`

```java
@Component
public class CommonCodeReadManager {

    private final CommonCodeQueryPort queryPort;

    @Transactional(readOnly = true)
    public List<CommonCode> findByCriteria(CommonCodeSearchCriteria criteria) {
        return queryPort.findByCriteria(criteria);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CommonCodeSearchCriteria criteria) {
        return queryPort.countByCriteria(criteria);
    }
}
```

---

#### Domain Layer

**Domain Port**: `CommonCodeQueryPort`

```java
public interface CommonCodeQueryPort {
    List<CommonCode> findByCriteria(CommonCodeSearchCriteria criteria);
    long countByCriteria(CommonCodeSearchCriteria criteria);
}
```

**Criteria**: `CommonCodeSearchCriteria`

```java
public record CommonCodeSearchCriteria(
    CommonCodeTypeId commonCodeTypeId,
    Boolean active,
    String code,
    QueryContext<CommonCodeSortKey> queryContext) {

    public Long commonCodeTypeIdValue() {
        return commonCodeTypeId.value();
    }

    public int size() {
        return queryContext.size();
    }

    public long offset() {
        return queryContext.offset();
    }
}
```

**Aggregate**: `CommonCode`

주요 도메인 로직:
- `forNew()`: 새 공통 코드 생성
- `reconstitute()`: 영속성 계층에서 복원
- `update()`: 정보 수정
- `activate()` / `deactivate()`: 활성화 상태 변경
- `delete()` / `restore()`: Soft Delete

---

#### Adapter-Out Layer

**위치**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/commoncode/`

**Adapter 구현체**: `CommonCodeQueryAdapter`

```java
@Component
@ConditionalOnProperty(name = "persistence.legacy.common-code.enabled", havingValue = "false")
public class CommonCodeQueryAdapter implements CommonCodeQueryPort {

    private final CommonCodeQueryDslRepository queryDslRepository;
    private final CommonCodeJpaEntityMapper mapper;

    @Override
    public List<CommonCode> findByCriteria(CommonCodeSearchCriteria criteria) {
        List<CommonCodeJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByCriteria(CommonCodeSearchCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }
}
```

**Repository**: `CommonCodeQueryDslRepository`

```java
@Repository
public class CommonCodeQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final CommonCodeConditionBuilder conditionBuilder;

    public List<CommonCodeJpaEntity> findByCriteria(CommonCodeSearchCriteria criteria) {
        QueryContext<CommonCodeSortKey> qc = criteria.queryContext();

        return queryFactory
            .selectFrom(commonCodeJpaEntity)
            .where(
                conditionBuilder.commonCodeTypeIdEq(criteria.commonCodeTypeIdValue()),
                conditionBuilder.activeEq(criteria.active()),
                conditionBuilder.codeContains(criteria.code()),
                conditionBuilder.notDeleted())
            .orderBy(createOrderSpecifier(qc.sortKey(), qc.sortDirection()))
            .offset(criteria.offset())
            .limit(criteria.size())
            .fetch();
    }

    public long countByCriteria(CommonCodeSearchCriteria criteria) {
        Long count = queryFactory
            .select(commonCodeJpaEntity.count())
            .from(commonCodeJpaEntity)
            .where(
                conditionBuilder.commonCodeTypeIdEq(criteria.commonCodeTypeIdValue()),
                conditionBuilder.activeEq(criteria.active()),
                conditionBuilder.codeContains(criteria.code()),
                conditionBuilder.notDeleted())
            .fetchOne();
        return count != null ? count : 0L;
    }
}
```

---

### Database Query 분석

**대상 테이블**: `common_code`

**WHERE 조건**:
- `common_code_type_id = ?` (필수)
- `active = ?` (옵션)
- `code LIKE ?` (옵션)
- `deleted_at IS NULL` (필수)

**ORDER BY**:
- `created_at DESC` (기본)
- `display_order ASC/DESC` (옵션)
- `code ASC/DESC` (옵션)

**PAGING**:
- `OFFSET ?`
- `LIMIT ?`

**예시 SQL**:
```sql
SELECT cc.*
FROM common_code cc
WHERE cc.common_code_type_id = 1
  AND cc.active = true
  AND cc.code LIKE '%CARD%'
  AND cc.deleted_at IS NULL
ORDER BY cc.created_at DESC
OFFSET 0
LIMIT 20;
```

---

## 2. POST 공통 코드 등록

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | /api/v1/market/admin/common-codes |
| Controller | CommonCodeCommandController |
| Method | register |
| UseCase | RegisterCommonCodeUseCase |
| Service | RegisterCommonCodeService |

---

### 호출 흐름 다이어그램

```
CommonCodeCommandController.register(RegisterCommonCodeApiRequest)
  |- CommonCodeCommandApiMapper.toCommand(request)
  |   +-- -> RegisterCommonCodeCommand
  |- RegisterCommonCodeUseCase.execute(command)                    [Port]
  |   +-- RegisterCommonCodeService.execute(command)               [Impl]
  |       |- CommonCodeValidator.validateCommonCodeTypeExists(typeId)
  |       |   +-- CommonCodeTypeReadManager.existsActiveById(typeId)
  |       |- CommonCodeValidator.validateNotDuplicate(typeId, code)
  |       |   +-- CommonCodeReadManager.existsByCommonCodeTypeIdAndCode(typeId, code)
  |       |- CommonCodeCommandFactory.create(command)
  |       |   +-- TimeProvider.now()
  |       |   +-- CommonCode.forNew(...)
  |       |       +-- -> CommonCode (Domain)
  |       +-- CommonCodeCommandManager.persist(commonCode)
  |           +-- CommonCodeCommandPort.persist(commonCode)        [Port]
  |               +-- CommonCodeCommandAdapter.persist()           [Impl]
  |                   +-- CommonCodeJpaEntityMapper.toEntity(domain)
  |                   +-- CommonCodeJpaRepository.save(entity)
  |                       +-- -> Long (ID)
  +-- -> ApiResponse<Long>
```

---

### Layer별 상세

#### Adapter-In Layer

**Request DTO**: `RegisterCommonCodeApiRequest`

| 필드 | 타입 | 설명 | 필수 | Validation |
|------|------|------|------|------------|
| commonCodeTypeId | Long | 공통 코드 타입 ID | Y | @NotNull |
| code | String | 코드값 | Y | @NotBlank, 영문대문자+숫자+언더스코어 |
| displayName | String | 표시명 | Y | @NotBlank |
| displayOrder | Integer | 표시 순서 | Y | @Min(0) |

**Controller**: `CommonCodeCommandController.register()`

```java
@PostMapping(REGISTER)
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<Long> register(@Valid @RequestBody RegisterCommonCodeApiRequest request) {
    RegisterCommonCodeCommand command = commandMapper.toCommand(request);
    Long createdId = registerUseCase.execute(command);
    return ApiResponse.of(createdId);
}
```

---

#### Application Layer

**Command DTO**: `RegisterCommonCodeCommand`

| 필드 | 타입 | 설명 |
|------|------|------|
| commonCodeTypeId | Long | 공통 코드 타입 ID |
| code | String | 코드값 |
| displayName | String | 표시명 |
| displayOrder | Integer | 표시 순서 |

**Service**: `RegisterCommonCodeService`

```java
@Service
public class RegisterCommonCodeService implements RegisterCommonCodeUseCase {

    private final CommonCodeCommandFactory commandFactory;
    private final CommonCodeCommandManager commandManager;
    private final CommonCodeValidator validator;

    @Override
    public Long execute(RegisterCommonCodeCommand command) {
        // 1. 검증
        validator.validateCommonCodeTypeExists(command.commonCodeTypeId());
        validator.validateNotDuplicate(command.commonCodeTypeId(), command.code());

        // 2. Domain 생성 (Factory)
        CommonCode commonCode = commandFactory.create(command);

        // 3. 영속화 (Manager)
        return commandManager.persist(commonCode);
    }
}
```

**Validator**: `CommonCodeValidator`

주요 검증:
- `validateCommonCodeTypeExists()`: CommonCodeType 존재 여부 확인
- `validateNotDuplicate()`: 동일 타입 내 코드 중복 확인
- `findExistingOrThrow()`: 존재하는 공통 코드 조회 (없으면 예외)

**Factory**: `CommonCodeCommandFactory`

```java
public CommonCode create(RegisterCommonCodeCommand command) {
    Instant now = timeProvider.now();
    return CommonCode.forNew(
        CommonCodeTypeId.of(command.commonCodeTypeId()),
        command.code(),
        command.displayName(),
        command.displayOrder(),
        now
    );
}
```

**Manager**: `CommonCodeCommandManager`

```java
@Component
@Transactional
public class CommonCodeCommandManager {

    private final CommonCodeCommandPort commandPort;

    public Long persist(CommonCode commonCode) {
        return commandPort.persist(commonCode);
    }
}
```

---

#### Domain Layer

**Aggregate**: `CommonCode.forNew()`

```java
public static CommonCode forNew(
    CommonCodeTypeId commonCodeTypeId,
    String code,
    String displayName,
    int displayOrder,
    Instant now) {
    return new CommonCode(
        CommonCodeId.forNew(),
        commonCodeTypeId,
        CommonCodeValue.of(code),
        CommonCodeDisplayName.of(displayName),
        DisplayOrder.of(displayOrder),
        true,
        DeletionStatus.active(),
        now,
        now
    );
}
```

**Value Objects**:
- `CommonCodeId`: 식별자 (새 인스턴스는 null, 저장 후 Long 값)
- `CommonCodeValue`: 코드값 (영문 대문자 + 숫자 + 언더스코어)
- `CommonCodeDisplayName`: 표시명 (1~50자)
- `DisplayOrder`: 표시 순서 (0 이상)

---

#### Adapter-Out Layer

**Adapter**: `CommonCodeCommandAdapter.persist()`

```java
@Override
public Long persist(CommonCode commonCode) {
    CommonCodeJpaEntity entity = mapper.toEntity(commonCode);
    CommonCodeJpaEntity saved = jpaRepository.save(entity);
    return saved.getId();
}
```

**JPA Repository**: `CommonCodeJpaRepository`

```java
public interface CommonCodeJpaRepository extends JpaRepository<CommonCodeJpaEntity, Long> {}
```

**Entity**: `CommonCodeJpaEntity`

| 필드 | 타입 | 설명 | DB 컬럼 |
|------|------|------|---------|
| id | Long | PK | id |
| commonCodeTypeId | Long | FK | common_code_type_id |
| code | String | 코드값 | code |
| displayName | String | 표시명 | display_name |
| displayOrder | Integer | 표시 순서 | display_order |
| active | Boolean | 활성화 여부 | active |
| createdAt | Instant | 생성일시 | created_at |
| updatedAt | Instant | 수정일시 | updated_at |
| deletedAt | Instant | 삭제일시 | deleted_at |

---

### Database Query 분석

**INSERT SQL**:
```sql
INSERT INTO common_code (
    common_code_type_id,
    code,
    display_name,
    display_order,
    active,
    created_at,
    updated_at,
    deleted_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
```

**검증 SQL** (중복 확인):
```sql
SELECT 1
FROM common_code
WHERE common_code_type_id = ?
  AND code = ?
  AND deleted_at IS NULL
LIMIT 1;
```

---

## 3. PUT 공통 코드 수정

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PUT |
| Path | /api/v1/market/admin/common-codes/{id} |
| Controller | CommonCodeCommandController |
| Method | update |
| UseCase | UpdateCommonCodeUseCase |
| Service | UpdateCommonCodeService |

---

### 호출 흐름 다이어그램

```
CommonCodeCommandController.update(id, UpdateCommonCodeApiRequest)
  |- CommonCodeCommandApiMapper.toCommand(id, request)
  |   +-- -> UpdateCommonCodeCommand
  |- UpdateCommonCodeUseCase.execute(command)                      [Port]
  |   +-- UpdateCommonCodeService.execute(command)                 [Impl]
  |       |- CommonCodeCommandFactory.createUpdateContext(command)
  |       |   +-- TimeProvider.now()
  |       |   +-- -> UpdateContext<CommonCodeId, CommonCodeUpdateData>
  |       |- CommonCodeValidator.findExistingOrThrow(id)
  |       |   +-- CommonCodeReadManager.getById(id)
  |       |       +-- CommonCodeQueryPort.findById(id)
  |       |           +-- CommonCodeQueryAdapter.findById()
  |       |               +-- CommonCodeQueryDslRepository.findById()
  |       |                   +-- -> Optional<CommonCode>
  |       |- CommonCode.update(updateData, changedAt)
  |       |   +-- 도메인 로직: displayName, displayOrder, updatedAt 변경
  |       +-- CommonCodeCommandManager.persist(commonCode)
  |           +-- CommonCodeCommandPort.persist(commonCode)
  |               +-- CommonCodeCommandAdapter.persist()
  |                   +-- JpaRepository.save()
  +-- -> ApiResponse<Void>
```

---

### Layer별 상세

#### Adapter-In Layer

**Request DTO**: `UpdateCommonCodeApiRequest`

| 필드 | 타입 | 설명 | 필수 | Validation |
|------|------|------|------|------------|
| displayName | String | 표시명 | Y | @NotBlank |
| displayOrder | Integer | 표시 순서 | Y | @Min(0) |

**Controller**: `CommonCodeCommandController.update()`

```java
@PutMapping(UPDATE)
public ApiResponse<Void> update(
    @PathVariable Long id,
    @Valid @RequestBody UpdateCommonCodeApiRequest request) {

    UpdateCommonCodeCommand command = commandMapper.toCommand(id, request);
    updateUseCase.execute(command);
    return ApiResponse.of();
}
```

---

#### Application Layer

**Command DTO**: `UpdateCommonCodeCommand`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 공통 코드 ID |
| displayName | String | 표시명 |
| displayOrder | Integer | 표시 순서 |

**Service**: `UpdateCommonCodeService`

```java
@Service
public class UpdateCommonCodeService implements UpdateCommonCodeUseCase {

    private final CommonCodeCommandFactory commandFactory;
    private final CommonCodeCommandManager commandManager;
    private final CommonCodeValidator validator;

    @Override
    public void execute(UpdateCommonCodeCommand command) {
        // 1. UpdateContext 생성 (Factory)
        UpdateContext<CommonCodeId, CommonCodeUpdateData> context =
            commandFactory.createUpdateContext(command);

        // 2. Domain 조회 + 검증 (Validator)
        CommonCode commonCode = validator.findExistingOrThrow(context.id());

        // 3. Domain 수정
        commonCode.update(context.updateData(), context.changedAt());

        // 4. 영속화 (Manager)
        commandManager.persist(commonCode);
    }
}
```

**Factory**: `CommonCodeCommandFactory.createUpdateContext()`

```java
public UpdateContext<CommonCodeId, CommonCodeUpdateData> createUpdateContext(
    UpdateCommonCodeCommand command) {

    CommonCodeId id = CommonCodeId.of(command.id());
    CommonCodeUpdateData updateData = CommonCodeUpdateData.of(
        command.displayName(),
        command.displayOrder()
    );
    Instant changedAt = timeProvider.now();

    return UpdateContext.of(id, updateData, changedAt);
}
```

---

#### Domain Layer

**Aggregate**: `CommonCode.update()`

```java
public void update(CommonCodeUpdateData data, Instant now) {
    this.displayName = data.displayName();
    this.displayOrder = data.displayOrder();
    this.updatedAt = now;
}
```

**UpdateData**: `CommonCodeUpdateData`

```java
public record CommonCodeUpdateData(
    CommonCodeDisplayName displayName,
    DisplayOrder displayOrder) {

    public static CommonCodeUpdateData of(String displayName, int displayOrder) {
        return new CommonCodeUpdateData(
            CommonCodeDisplayName.of(displayName),
            DisplayOrder.of(displayOrder)
        );
    }
}
```

---

#### Adapter-Out Layer

**UPDATE SQL** (JPA Dirty Checking):
```sql
UPDATE common_code
SET display_name = ?,
    display_order = ?,
    updated_at = ?
WHERE id = ?;
```

---

## 4. PATCH 활성화 상태 변경

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PATCH |
| Path | /api/v1/market/admin/common-codes/active-status |
| Controller | CommonCodeCommandController |
| Method | changeActiveStatus |
| UseCase | ChangeCommonCodeStatusUseCase |
| Service | ChangeCommonCodeStatusService |

---

### 호출 흐름 다이어그램

```
CommonCodeCommandController.changeActiveStatus(ChangeActiveStatusApiRequest)
  |- CommonCodeCommandApiMapper.toCommand(request)
  |   +-- -> ChangeCommonCodeStatusCommand
  |- ChangeCommonCodeStatusUseCase.execute(command)                [Port]
  |   +-- ChangeCommonCodeStatusService.execute(command)           [Impl]
  |       |- CommonCodeCommandFactory.createStatusChangeContexts(command)
  |       |   +-- TimeProvider.now()
  |       |   +-- -> List<StatusChangeContext<CommonCodeId>>
  |       |- CommonCodeValidator.findAllExistingOrThrow(ids)
  |       |   +-- CommonCodeReadManager.getByIds(ids)
  |       |       +-- CommonCodeQueryPort.findByIds(ids)
  |       |           +-- CommonCodeQueryAdapter.findByIds()
  |       |               +-- CommonCodeQueryDslRepository.findByIds()
  |       |                   +-- -> List<CommonCode>
  |       |- foreach CommonCode:
  |       |   +-- if (active) commonCode.activate(changedAt)
  |       |   +-- else commonCode.deactivate(changedAt)
  |       +-- CommonCodeCommandManager.persistAll(commonCodes)
  |           +-- CommonCodeCommandPort.persistAll(commonCodes)
  |               +-- CommonCodeCommandAdapter.persistAll()
  |                   +-- JpaRepository.saveAll()
  +-- -> ApiResponse<Void>
```

---

### Layer별 상세

#### Adapter-In Layer

**Request DTO**: `ChangeActiveStatusApiRequest`

| 필드 | 타입 | 설명 | 필수 | Validation |
|------|------|------|------|------------|
| ids | List<Long> | 공통 코드 ID 목록 | Y | @NotEmpty |
| active | Boolean | 활성화 여부 | Y | @NotNull |

**Controller**: `CommonCodeCommandController.changeActiveStatus()`

```java
@PatchMapping(CHANGE_ACTIVE_STATUS)
public ApiResponse<Void> changeActiveStatus(
    @Valid @RequestBody ChangeActiveStatusApiRequest request) {

    ChangeCommonCodeStatusCommand command = commandMapper.toCommand(request);
    changeStatusUseCase.execute(command);
    return ApiResponse.of();
}
```

---

#### Application Layer

**Command DTO**: `ChangeCommonCodeStatusCommand`

| 필드 | 타입 | 설명 |
|------|------|------|
| ids | List<Long> | 공통 코드 ID 목록 |
| active | Boolean | 활성화 여부 |

**Service**: `ChangeCommonCodeStatusService`

```java
@Service
public class ChangeCommonCodeStatusService implements ChangeCommonCodeStatusUseCase {

    private final CommonCodeCommandFactory commandFactory;
    private final CommonCodeCommandManager commandManager;
    private final CommonCodeValidator validator;

    @Override
    public void execute(ChangeCommonCodeStatusCommand command) {
        // 1. StatusChangeContext 생성 (Factory)
        List<StatusChangeContext<CommonCodeId>> contexts =
            commandFactory.createStatusChangeContexts(command);

        if (contexts.isEmpty()) {
            return;
        }

        // 2. Domain 목록 조회 + 검증 (Validator)
        List<CommonCodeId> ids = contexts.stream().map(StatusChangeContext::id).toList();
        List<CommonCode> commonCodes = validator.findAllExistingOrThrow(ids);

        // 3. 상태 변경 시각 (Factory에서 동일한 시간 생성)
        Instant changedAt = contexts.get(0).changedAt();

        // 4. Domain 상태 변경
        for (CommonCode cc : commonCodes) {
            if (command.active()) {
                cc.activate(changedAt);
            } else {
                cc.deactivate(changedAt);
            }
        }

        // 5. 일괄 영속화 (Manager)
        commandManager.persistAll(commonCodes);
    }
}
```

**Factory**: `CommonCodeCommandFactory.createStatusChangeContexts()`

```java
public List<StatusChangeContext<CommonCodeId>> createStatusChangeContexts(
    ChangeCommonCodeStatusCommand command) {

    Instant changedAt = timeProvider.now();

    return command.ids().stream()
        .map(CommonCodeId::of)
        .map(id -> StatusChangeContext.of(id, command.active(), changedAt))
        .toList();
}
```

---

#### Domain Layer

**Aggregate**: `CommonCode.activate()` / `deactivate()`

```java
public void activate(Instant now) {
    this.active = true;
    this.updatedAt = now;
}

public void deactivate(Instant now) {
    this.active = false;
    this.updatedAt = now;
}
```

---

#### Adapter-Out Layer

**Adapter**: `CommonCodeCommandAdapter.persistAll()`

```java
@Override
public void persistAll(List<CommonCode> commonCodes) {
    List<CommonCodeJpaEntity> entities =
        commonCodes.stream().map(mapper::toEntity).toList();
    jpaRepository.saveAll(entities);
}
```

**BATCH UPDATE SQL** (JPA Dirty Checking):
```sql
UPDATE common_code
SET active = ?,
    updated_at = ?
WHERE id = ?;

-- 각 ID마다 반복 실행 (Hibernate Batch 활성화 시 배치 처리)
```

---

## 아키텍처 패턴 요약

### Hexagonal 레이어 책임

| 레이어 | 책임 | 주요 컴포넌트 |
|--------|------|---------------|
| **Adapter-In** | HTTP 요청 처리, DTO 변환 | Controller, ApiMapper, Request/Response DTO |
| **Application** | 비즈니스 흐름 조율, 트랜잭션 경계 | UseCase(Port), Service, Manager, Factory, Validator |
| **Domain** | 핵심 비즈니스 규칙, 도메인 로직 | Aggregate, VO, Domain Port, Criteria |
| **Adapter-Out** | 영속성 처리, DB 접근 | Adapter, Repository, Entity, Mapper |

---

### CQRS 패턴 적용

**Query 흐름**:
```
QueryController → QueryUseCase → QueryService → ReadManager → QueryPort → QueryAdapter → QueryDslRepository
```

**Command 흐름**:
```
CommandController → CommandUseCase → CommandService → Validator + Factory → CommandManager → CommandPort → CommandAdapter → JpaRepository
```

---

### 주요 설계 원칙

1. **Port-Adapter 패턴**: Application이 Domain Port를 정의, Adapter-Out이 구현
2. **Factory 패턴**: Domain 객체 생성 로직 집중화, TimeProvider 추상화
3. **Validator 패턴**: 검증 로직 분리, 도메인 조회 + 예외 처리 통합
4. **Assembler 패턴**: Domain → Result DTO 변환 담당
5. **Manager 패턴**: Port 호출 + 트랜잭션 경계 관리

---

## 트랜잭션 경계

| 레이어 | 트랜잭션 | 설명 |
|--------|----------|------|
| Controller | ❌ | 트랜잭션 없음 |
| Service | ❌ | 트랜잭션 없음 (조율만) |
| Manager | ✅ | `@Transactional` (ReadManager: readOnly=true) |
| Adapter | ❌ | 트랜잭션 금지 (Manager에서 관리) |

---

## 주요 DTO 변환 흐름

### Query 흐름
```
SearchCommonCodesPageApiRequest (API)
  → CommonCodeSearchParams (Application)
    → CommonCodeSearchCriteria (Domain)
      → QueryDSL 조건
        → CommonCode (Domain)
          → CommonCodeResult (Application)
            → CommonCodeApiResponse (API)
```

### Command 흐름
```
RegisterCommonCodeApiRequest (API)
  → RegisterCommonCodeCommand (Application)
    → CommonCode.forNew() (Domain)
      → CommonCodeJpaEntity (Persistence)
        → DB INSERT
          → Long (ID)
```

---

## 에러 처리

### Domain 예외

| 예외 클래스 | 발생 상황 | HTTP 상태 |
|-------------|----------|-----------|
| `CommonCodeNotFoundException` | ID로 조회 시 없음 | 404 |
| `CommonCodeDuplicateException` | 동일 타입 내 코드 중복 | 409 |
| `CommonCodeInactiveException` | 비활성화된 코드 사용 | 400 |

### GlobalExceptionHandler 매핑

```java
@ExceptionHandler(CommonCodeException.class)
public ResponseEntity<ApiResponse<Void>> handleCommonCodeException(CommonCodeException ex) {
    CommonCodeErrorMapper mapper = errorMapperRegistry.getCommonCodeMapper();
    ErrorInfo errorInfo = mapper.toErrorInfo(ex.getErrorCode());
    return ResponseEntity.status(errorInfo.httpStatus()).body(ApiResponse.error(errorInfo));
}
```

---

## 성능 고려사항

### QueryDSL 최적화

1. **동적 쿼리**: `ConditionBuilder`로 NULL 조건 자동 제외
2. **Soft Delete**: `deleted_at IS NULL` 조건 자동 추가
3. **Paging**: `offset()` + `limit()` 적용
4. **Index**: `common_code_type_id`, `code`, `deleted_at` 복합 인덱스

### Batch 처리

- `changeActiveStatus`: `saveAll()` 사용하여 배치 UPDATE
- Hibernate `batch_size` 설정 권장

---

## 테스트 전략

### Layer별 테스트

| 레이어 | 테스트 타입 | 주요 검증 |
|--------|-------------|-----------|
| Controller | `@WebMvcTest` | HTTP 요청/응답, DTO 변환 |
| Service | `@ExtendWith(MockitoExtension)` | 비즈니스 흐름, Manager 호출 |
| Manager | `@ExtendWith(MockitoExtension)` | Port 호출, 트랜잭션 |
| Domain | 단위 테스트 | 비즈니스 규칙, VO 검증 |
| Adapter | `@DataJpaTest` | Repository 쿼리, Entity 매핑 |

---

## 참고 문서

- **Domain 설계**: `domain/src/main/java/com/ryuqq/marketplace/domain/commoncode/`
- **Application 설계**: `application/src/main/java/com/ryuqq/marketplace/application/commoncode/`
- **Persistence 설계**: `adapter-out/persistence-mysql/.../commoncode/`
- **REST API 설계**: `adapter-in/rest-api/.../commoncode/`

---

**작성일**: 2026-02-06
**버전**: 1.0.0
**작성자**: Claude Code (api-flow-analyzer)
