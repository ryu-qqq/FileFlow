# Brand API Flow Analysis

## 개요

Brand 도메인의 API 호출 흐름을 Hexagonal Architecture 레이어별로 추적하여 문서화합니다.

---

## 엔드포인트 목록

| HTTP Method | Path | Description | Controller Method |
|-------------|------|-------------|-------------------|
| GET | `/api/v1/market/admin/brands` | 브랜드 목록 조회 | `searchBrandsByOffset` |

---

## API Flow: GET /api/v1/market/admin/brands

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | `/api/v1/market/admin/brands` |
| Controller | BrandQueryController |
| Method | searchBrandsByOffset |
| UseCase | SearchBrandByOffsetUseCase |
| Service | SearchBrandByOffsetService |
| Domain Port | BrandQueryPort |
| Adapter | BrandQueryAdapter |
| Repository | BrandQueryDslRepository |

---

## 호출 흐름 다이어그램

```
BrandQueryController.searchBrandsByOffset(SearchBrandsApiRequest)
  │
  ├─ BrandQueryApiMapper.toSearchParams(request)
  │   └─> BrandSearchParams (Application DTO)
  │
  ├─ SearchBrandByOffsetUseCase.execute(params)           [Port Interface]
  │   │
  │   └─ SearchBrandByOffsetService.execute(params)       [Service Implementation]
  │       │
  │       ├─ BrandQueryFactory.createCriteria(params)
  │       │   └─> BrandSearchCriteria (Domain VO)
  │       │
  │       ├─ BrandReadManager.findByCriteria(criteria)
  │       │   └─ BrandQueryPort.findByCriteria(criteria)  [Domain Port]
  │       │       └─ BrandQueryAdapter.findByCriteria()   [Port Implementation]
  │       │           └─ BrandQueryDslRepository.findByCriteria()
  │       │               ├─ BrandConditionBuilder (WHERE 조건 생성)
  │       │               ├─ QueryDSL (SELECT, ORDER BY, LIMIT)
  │       │               └─> List<BrandJpaEntity>
  │       │           └─ BrandJpaEntityMapper.toDomain()
  │       │               └─> List<Brand> (Domain Aggregate)
  │       │
  │       ├─ BrandReadManager.countByCriteria(criteria)
  │       │   └─ BrandQueryPort.countByCriteria(criteria)
  │       │       └─ BrandQueryAdapter.countByCriteria()
  │       │           └─ BrandQueryDslRepository.countByCriteria()
  │       │               └─> long totalElements
  │       │
  │       └─ BrandAssembler.toPageResult(brands, page, size, totalElements)
  │           └─> BrandPageResult
  │
  └─ BrandQueryApiMapper.toPageResponse(pageResult)
      └─> ApiResponse<PageApiResponse<BrandApiResponse>>
```

---

## Layer별 상세 분석

### 1. Adapter-In Layer (REST API)

#### Controller
**파일**: `BrandQueryController.java`

```java
@RestController
@RequestMapping(BrandAdminEndpoints.BRANDS)  // "/api/v1/market/admin/brands"
public class BrandQueryController {

    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<BrandApiResponse>>>
            searchBrandsByOffset(@ParameterObject @Valid SearchBrandsApiRequest request) {
        BrandPageResult pageResult =
            searchBrandByOffsetUseCase.execute(mapper.toSearchParams(request));
        return ResponseEntity.ok(ApiResponse.of(mapper.toPageResponse(pageResult)));
    }
}
```

#### Request DTO
**파일**: `SearchBrandsApiRequest.java`

| 필드 | 타입 | 설명 | 검증 |
|-----|------|------|-----|
| statuses | List\<String\> | 상태 필터 (ACTIVE, INACTIVE) | - |
| searchField | String | 검색 필드 (code, nameKo, nameEn) | - |
| searchWord | String | 검색어 | - |
| sortKey | String | 정렬 키 (createdAt, nameKo, updatedAt) | - |
| sortDirection | String | 정렬 방향 (ASC, DESC) | - |
| page | Integer | 페이지 번호 (0부터) | - |
| size | Integer | 페이지 크기 | - |

**기본값**:
- page: 0
- size: 20

#### Response DTO
**파일**: `BrandApiResponse.java`

| 필드 | 타입 | 설명 |
|-----|------|------|
| id | Long | 브랜드 ID |
| code | String | 브랜드 코드 |
| nameKo | String | 한글명 |
| nameEn | String | 영문명 |
| shortName | String | 약칭 |
| status | String | 상태 |
| logoUrl | String | 로고 URL |
| createdAt | String | 생성일시 (ISO-8601) |
| updatedAt | String | 수정일시 (ISO-8601) |

#### API Mapper
**파일**: `BrandQueryApiMapper.java`

**주요 메서드**:
1. `toSearchParams(SearchBrandsApiRequest)` → `BrandSearchParams`
   - API Request를 Application Layer DTO로 변환
   - 기본값 처리 (page: 0, size: 20)
   - CommonSearchParams 생성

2. `toResponse(BrandResult)` → `BrandApiResponse`
   - Application Result를 API Response로 변환
   - Instant → ISO-8601 String 변환

3. `toPageResponse(BrandPageResult)` → `PageApiResponse<BrandApiResponse>`
   - 페이징 정보와 함께 응답 생성

---

### 2. Application Layer

#### UseCase (Port Interface)
**파일**: `SearchBrandByOffsetUseCase.java`

```java
public interface SearchBrandByOffsetUseCase {
    BrandPageResult execute(BrandSearchParams params);
}
```

#### Service Implementation
**파일**: `SearchBrandByOffsetService.java`

**의존성**:
- BrandReadManager: 도메인 객체 조회
- BrandQueryFactory: Domain Criteria 생성
- BrandAssembler: Domain → DTO 변환

**실행 흐름**:
```java
@Override
public BrandPageResult execute(BrandSearchParams params) {
    // 1. Domain Criteria 생성
    BrandSearchCriteria criteria = queryFactory.createCriteria(params);

    // 2. Domain 객체 조회
    List<Brand> brands = readManager.findByCriteria(criteria);

    // 3. 전체 개수 조회
    long totalElements = readManager.countByCriteria(criteria);

    // 4. Page Result 조립
    return assembler.toPageResult(brands, params.page(), params.size(), totalElements);
}
```

#### Application DTOs

**BrandSearchParams**: API → Application 변환 DTO
```java
public record BrandSearchParams(
    List<String> statuses,
    String searchField,
    String searchWord,
    CommonSearchParams searchParams  // page, size, sort 정보
)
```

**BrandResult**: Domain → Application 변환 DTO
```java
public record BrandResult(
    Long id,
    String code,
    String nameKo,
    String nameEn,
    String shortName,
    String status,
    String logoUrl,
    Instant createdAt,
    Instant updatedAt
)
```

**BrandPageResult**: 페이징 결과 DTO
```java
public record BrandPageResult(
    List<BrandResult> results,
    PageMeta pageMeta  // page, size, totalElements
)
```

#### BrandReadManager
**파일**: `BrandReadManager.java`

**역할**: 도메인 객체 조회 조율 (Manager 패턴)

**주요 메서드**:
- `findByCriteria(BrandSearchCriteria)` → `List<Brand>`
- `countByCriteria(BrandSearchCriteria)` → `long`
- `getById(BrandId)` → `Brand` (with exception)
- `existsByCode(String)` → `boolean`

**트랜잭션**: `@Transactional(readOnly = true)`

#### BrandQueryFactory
**파일**: `BrandQueryFactory.java`

**역할**: Application DTO → Domain Criteria 변환

**변환 로직**:
1. `BrandSearchParams` → `BrandSearchCriteria`
2. sortKey 문자열 → `BrandSortKey` enum
3. sortDirection 문자열 → `SortDirection` enum
4. statuses 문자열 리스트 → `BrandStatus` enum 리스트
5. searchField 문자열 → `BrandSearchField` enum
6. page, size → `PageRequest` VO
7. 전체를 `QueryContext<BrandSortKey>` VO로 조합

#### BrandAssembler
**파일**: `BrandAssembler.java`

**역할**: Domain Aggregate → Application DTO 변환

**주요 메서드**:
- `toResult(Brand)` → `BrandResult`
- `toResults(List<Brand>)` → `List<BrandResult>`
- `toPageResult(List<Brand>, page, size, totalElements)` → `BrandPageResult`

---

### 3. Domain Layer

#### Domain Port (Interface)
**파일**: `BrandQueryPort.java`

```java
public interface BrandQueryPort {
    Optional<Brand> findById(BrandId id);
    List<Brand> findByCriteria(BrandSearchCriteria criteria);
    long countByCriteria(BrandSearchCriteria criteria);
    boolean existsByCode(String code);
}
```

#### Domain Aggregate
**파일**: `Brand.java`

**주요 필드**:
- `BrandId id` (VO)
- `BrandCode code` (VO)
- `BrandName brandName` (VO: nameKo, nameEn, shortName)
- `BrandStatus status` (Enum)
- `LogoUrl logoUrl` (VO)
- `DeletionStatus deletionStatus` (VO)
- `Instant createdAt`
- `Instant updatedAt`

**팩토리 메서드**:
- `forNew()`: 새 브랜드 생성
- `reconstitute()`: 영속성에서 복원

#### Domain Query Criteria
**파일**: `BrandSearchCriteria.java`

```java
public record BrandSearchCriteria(
    List<BrandStatus> statuses,         // Enum 리스트
    BrandSearchField searchField,       // Enum
    String searchWord,
    QueryContext<BrandSortKey> queryContext  // sortKey, direction, pageRequest
)
```

**헬퍼 메서드**:
- `hasStatusFilter()`: 상태 필터 존재 여부
- `hasSearchCondition()`: 검색어 존재 여부
- `hasSearchField()`: 특정 필드 검색 여부
- `size()`, `offset()`, `page()`: 페이징 정보 접근

**정적 팩토리**:
- `defaultCriteria()`: 기본 조건
- `activeOnly()`: ACTIVE 상태만

#### Domain Enums

**BrandSearchField**: 검색 필드
- CODE
- NAME_KO
- NAME_EN

**BrandSortKey**: 정렬 키
- CREATED_AT (기본값)
- NAME_KO
- UPDATED_AT

**BrandStatus**: 브랜드 상태
- ACTIVE
- INACTIVE

---

### 4. Adapter-Out Layer (Persistence)

#### Adapter Implementation
**파일**: `BrandQueryAdapter.java`

**역할**: Domain Port 구현체

```java
@Component
public class BrandQueryAdapter implements BrandQueryPort {

    @Override
    public List<Brand> findByCriteria(BrandSearchCriteria criteria) {
        return repository.findByCriteria(criteria).stream()
                .map(mapper::toDomain)  // JpaEntity → Domain
                .toList();
    }

    @Override
    public long countByCriteria(BrandSearchCriteria criteria) {
        return repository.countByCriteria(criteria);
    }
}
```

**의존성**:
- BrandQueryDslRepository: QueryDSL 쿼리 실행
- BrandJpaEntityMapper: JpaEntity ↔ Domain 변환

#### QueryDSL Repository
**파일**: `BrandQueryDslRepository.java`

**주요 메서드**:

1. **findByCriteria()**:
```java
public List<BrandJpaEntity> findByCriteria(BrandSearchCriteria criteria) {
    return queryFactory
        .selectFrom(brand)
        .where(
            conditionBuilder.statusIn(criteria),
            conditionBuilder.searchCondition(criteria),
            conditionBuilder.notDeleted()
        )
        .orderBy(resolveOrderSpecifier(criteria))
        .offset(criteria.offset())
        .limit(criteria.size())
        .fetch();
}
```

2. **countByCriteria()**:
```java
public long countByCriteria(BrandSearchCriteria criteria) {
    Long count = queryFactory
        .select(brand.count())
        .from(brand)
        .where(
            conditionBuilder.statusIn(criteria),
            conditionBuilder.searchCondition(criteria),
            conditionBuilder.notDeleted()
        )
        .fetchOne();
    return count != null ? count : 0L;
}
```

3. **existsByCode()**:
```java
public boolean existsByCode(String code) {
    Integer result = queryFactory
        .selectOne()
        .from(brand)
        .where(
            brand.code.eq(code),
            conditionBuilder.notDeleted()
        )
        .fetchFirst();
    return result != null;
}
```

#### Condition Builder
**파일**: `BrandConditionBuilder.java`

**역할**: QueryDSL WHERE 조건 생성

**주요 메서드**:

1. **statusIn()**: 상태 필터
```java
public BooleanExpression statusIn(BrandSearchCriteria criteria) {
    if (!criteria.hasStatusFilter()) return null;

    List<String> statusNames = criteria.statuses().stream()
        .map(BrandStatus::name)
        .toList();
    return brand.status.in(statusNames);
}
```

2. **searchCondition()**: 검색 조건
```java
public BooleanExpression searchCondition(BrandSearchCriteria criteria) {
    if (!criteria.hasSearchCondition()) return null;

    String word = "%" + criteria.searchWord() + "%";

    // 전체 필드 검색
    if (!criteria.hasSearchField()) {
        return brand.nameKo.like(word)
            .or(brand.nameEn.like(word))
            .or(brand.code.like(word));
    }

    // 특정 필드 검색
    return switch (criteria.searchField()) {
        case CODE -> brand.code.like(word);
        case NAME_KO -> brand.nameKo.like(word);
        case NAME_EN -> brand.nameEn.like(word);
    };
}
```

3. **notDeleted()**: Soft Delete 필터
```java
public BooleanExpression notDeleted() {
    return brand.deletedAt.isNull();
}
```

#### JPA Repository
**파일**: `BrandJpaRepository.java`

```java
public interface BrandJpaRepository extends JpaRepository<BrandJpaEntity, Long> {}
```

**용도**: Command 작업 (save, delete) 전용

#### JPA Entity
**파일**: `BrandJpaEntity.java`

**테이블**: `brand`

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 브랜드 ID |
| code | VARCHAR(100) | NOT NULL, UNIQUE | 브랜드 코드 |
| name_ko | VARCHAR(255) | - | 한글명 |
| name_en | VARCHAR(255) | - | 영문명 |
| short_name | VARCHAR(100) | - | 약칭 |
| status | VARCHAR(20) | NOT NULL | 상태 |
| logo_url | VARCHAR(500) | - | 로고 URL |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 |
| deleted_at | TIMESTAMP | - | 삭제일시 (Soft Delete) |

**상속**: `SoftDeletableEntity` → `BaseAuditEntity`

#### Entity Mapper
**파일**: `BrandJpaEntityMapper.java`

**주요 메서드**:

1. **toDomain()**: JpaEntity → Domain Aggregate
```java
public Brand toDomain(BrandJpaEntity entity) {
    return Brand.reconstitute(
        BrandId.of(entity.getId()),
        BrandCode.of(entity.getCode()),
        BrandName.of(entity.getNameKo(), entity.getNameEn(), entity.getShortName()),
        BrandStatus.fromString(entity.getStatus()),
        LogoUrl.of(entity.getLogoUrl()),
        entity.getDeletedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
}
```

2. **toEntity()**: Domain Aggregate → JpaEntity
```java
public BrandJpaEntity toEntity(Brand brand) {
    return BrandJpaEntity.create(
        brand.idValue(),
        brand.codeValue(),
        brand.nameKo(),
        brand.nameEn(),
        brand.shortName(),
        brand.status().name(),
        brand.logoUrlValue(),
        brand.createdAt(),
        brand.updatedAt(),
        brand.deletedAt()
    );
}
```

---

## Database Query 분석

### 브랜드 목록 조회 쿼리

**기본 구조**:
```sql
SELECT *
FROM brand
WHERE deleted_at IS NULL
  AND status IN (?, ?)           -- statuses 필터 (선택)
  AND (
    name_ko LIKE ?               -- 전체 필드 검색
    OR name_en LIKE ?
    OR code LIKE ?
  )
ORDER BY created_at DESC         -- sortKey에 따라 변경
LIMIT ? OFFSET ?
```

**WHERE 조건**:
1. `deleted_at IS NULL`: Soft Delete 필터 (항상 적용)
2. `status IN (?, ?)`: 상태 필터 (statuses가 있을 때만)
3. 검색 조건 (searchWord가 있을 때만):
   - searchField 없음 → 전체 필드 OR 검색
   - searchField 있음 → 특정 필드만 검색

**ORDER BY**:
- CREATED_AT: `created_at DESC` (기본값)
- NAME_KO: `name_ko ASC/DESC`
- UPDATED_AT: `updated_at DESC`

**PAGINATION**:
- LIMIT: criteria.size()
- OFFSET: criteria.offset() = page * size

### 전체 개수 조회 쿼리

```sql
SELECT COUNT(*)
FROM brand
WHERE deleted_at IS NULL
  AND status IN (?, ?)
  AND (name_ko LIKE ? OR name_en LIKE ? OR code LIKE ?)
```

**특징**: WHERE 조건은 동일, ORDER BY와 LIMIT/OFFSET 없음

### 코드 존재 여부 확인 쿼리

```sql
SELECT 1
FROM brand
WHERE code = ?
  AND deleted_at IS NULL
LIMIT 1
```

---

## 데이터 흐름 요약

### Request → Response 전체 흐름

```
1. API Request (SearchBrandsApiRequest)
   ↓ BrandQueryApiMapper
2. Application DTO (BrandSearchParams)
   ↓ BrandQueryFactory
3. Domain Criteria (BrandSearchCriteria)
   ↓ BrandReadManager → BrandQueryPort
4. QueryDSL (BrandConditionBuilder)
   ↓ JPAQueryFactory
5. JPA Entity (BrandJpaEntity)
   ↓ BrandJpaEntityMapper
6. Domain Aggregate (Brand)
   ↓ BrandAssembler
7. Application Result (BrandPageResult)
   ↓ BrandQueryApiMapper
8. API Response (PageApiResponse<BrandApiResponse>)
```

### DTO 변환 체인

```
[Adapter-In]
SearchBrandsApiRequest
  → BrandSearchParams                    (API → Application)

[Application]
BrandSearchParams
  → BrandSearchCriteria                  (Application → Domain)

[Adapter-Out]
BrandSearchCriteria
  → QueryDSL BooleanExpression           (Domain → Persistence)

BrandJpaEntity
  → Brand                                (Persistence → Domain)

[Application]
Brand
  → BrandResult                          (Domain → Application)

[Adapter-In]
BrandResult
  → BrandApiResponse                     (Application → API)
```

---

## 핵심 설계 패턴

### 1. Hexagonal Architecture (Port & Adapter)

**Inbound**:
- Port: `SearchBrandByOffsetUseCase`
- Adapter: `BrandQueryController`

**Outbound**:
- Port: `BrandQueryPort`
- Adapter: `BrandQueryAdapter`

### 2. CQRS (Command Query Responsibility Segregation)

**Query Side** (이 API):
- `BrandQueryPort`: 조회 전용 포트
- `BrandQueryAdapter`: 조회 전용 어댑터
- `BrandQueryDslRepository`: QueryDSL 기반 조회
- `SearchBrandByOffsetUseCase`: 조회 UseCase

**Command Side** (별도):
- `BrandCommandPort`: 명령 전용 포트
- `BrandCommandAdapter`: 명령 전용 어댑터
- `BrandJpaRepository`: JPA save/delete

### 3. DDD (Domain-Driven Design)

**Aggregate Root**:
- `Brand`: 브랜드 Aggregate Root

**Value Objects**:
- `BrandId`: 식별자 VO
- `BrandCode`: 코드 VO (unique)
- `BrandName`: 이름 VO (nameKo, nameEn, shortName)
- `BrandStatus`: 상태 Enum
- `LogoUrl`: URL VO
- `DeletionStatus`: 삭제 상태 VO

**Query Criteria**:
- `BrandSearchCriteria`: 도메인 검색 조건
- `QueryContext<BrandSortKey>`: 쿼리 컨텍스트

### 4. Factory Pattern

**BrandQueryFactory**:
- Application DTO → Domain Criteria 변환 책임 분리
- 복잡한 변환 로직 캡슐화

### 5. Manager Pattern

**BrandReadManager**:
- 도메인 조회 조율
- Port 호출 + 예외 처리
- 트랜잭션 경계 설정

### 6. Assembler Pattern

**BrandAssembler**:
- Domain → Application DTO 변환 책임 분리
- 단순 매핑 로직 캡슐화

### 7. Builder Pattern

**BrandConditionBuilder**:
- QueryDSL WHERE 조건 생성 책임 분리
- 복잡한 동적 쿼리 조건 관리

---

## 트랜잭션 경계

### Read Transaction
**위치**: `BrandReadManager`

```java
@Transactional(readOnly = true)
public List<Brand> findByCriteria(BrandSearchCriteria criteria) {
    return queryPort.findByCriteria(criteria);
}
```

**특징**:
- `readOnly = true`: 읽기 전용 최적화
- 영속성 컨텍스트 플러시 생략
- 스냅샷 저장 생략

---

## 성능 최적화 포인트

### 1. QueryDSL 동적 쿼리
- 조건이 없으면 WHERE 절 추가 안 함
- `BooleanExpression` null 처리로 동적 쿼리 생성

### 2. 읽기 전용 트랜잭션
- `@Transactional(readOnly = true)` 사용
- 불필요한 스냅샷 저장 방지

### 3. Soft Delete 인덱스
- `deleted_at` 컬럼에 인덱스 권장
- 대부분의 쿼리가 `deleted_at IS NULL` 조건 포함

### 4. 복합 인덱스 고려
```sql
CREATE INDEX idx_brand_status_deleted ON brand(status, deleted_at);
CREATE INDEX idx_brand_name_ko_deleted ON brand(name_ko, deleted_at);
CREATE INDEX idx_brand_created_deleted ON brand(created_at DESC, deleted_at);
```

---

## 테스트 전략

### 1. Controller Layer
- `@WebMvcTest(BrandQueryController.class)`
- MockMvc로 HTTP 요청/응답 검증
- UseCase와 Mapper는 Mock

### 2. Service Layer
- `@ExtendWith(MockitoExtension.class)`
- Manager, Factory, Assembler Mock
- 비즈니스 로직 단위 테스트

### 3. Manager Layer
- Port Mock으로 도메인 조회 시뮬레이션
- 예외 처리 검증

### 4. Repository Layer
- `@DataJpaTest` + QueryDSL 설정
- 실제 DB 쿼리 검증
- 테스트 데이터 픽스처 활용

### 5. Integration Test
- 전체 Layer 통합 검증
- TestContainers로 실제 DB 환경

---

## 에러 처리

### Domain Exception
- `BrandNotFoundException`: 브랜드 미존재
- `BrandCodeDuplicateException`: 코드 중복

### Exception 처리 위치
**BrandReadManager**:
```java
public Brand getById(BrandId id) {
    return queryPort.findById(id)
        .orElseThrow(() -> new BrandNotFoundException(id.value()));
}
```

**GlobalExceptionHandler** (REST Layer):
- Domain Exception → HTTP 상태 코드 매핑
- `BrandNotFoundException` → 404 NOT_FOUND

---

## 확장 포인트

### 1. 검색 기능 확장
- `BrandSearchField`에 새 필드 추가
- `BrandConditionBuilder.searchCondition()` 수정

### 2. 정렬 기준 추가
- `BrandSortKey`에 새 키 추가
- `BrandQueryDslRepository.resolveOrderSpecifier()` 수정

### 3. 필터 추가
- `BrandSearchCriteria`에 새 필드 추가
- `BrandConditionBuilder`에 새 조건 메서드 추가

### 4. 캐싱 적용
- `BrandReadManager`에 `@Cacheable` 추가
- Redis 캐시 전략 수립

---

## 참고 사항

### 1. Soft Delete 정책
- `deleted_at IS NULL` 조건이 모든 조회 쿼리에 포함
- 실제 삭제는 배치 작업으로 별도 처리 권장

### 2. 페이징 기본값
- page: 0
- size: 20
- sortKey: CREATED_AT
- sortDirection: DESC

### 3. 검색 방식
- LIKE 패턴: `%{searchWord}%` (부분 일치)
- 대소문자 구분 없음 (MySQL 기본 collation)

### 4. DTO 불변성
- 모든 DTO는 record로 불변 객체
- 방어적 복사 적용 (`List.copyOf()`)

---

## 관련 문서

- [아키텍처 개요](../architecture/hexagonal.md)
- [CQRS 패턴](../patterns/cqrs.md)
- [DDD 전략](../patterns/ddd.md)
- [QueryDSL 가이드](../tech/querydsl.md)
