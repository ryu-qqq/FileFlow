# Category API Flow Analysis

카테고리 도메인 API 호출 흐름 분석 문서

---

## 📋 엔드포인트 목록

| HTTP Method | Path | Controller | Method | 설명 |
|-------------|------|------------|--------|------|
| GET | /api/v1/market/admin/categories | CategoryQueryController | searchCategoriesByOffset | 카테고리 목록 조회 (페이징) |

---

## 🔍 GET /api/v1/market/admin/categories - 카테고리 목록 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /api/v1/market/admin/categories |
| Controller | CategoryQueryController |
| Method | searchCategoriesByOffset |
| UseCase | SearchCategoryByOffsetUseCase |
| Service | SearchCategoryByOffsetService |

---

### 호출 흐름 다이어그램

```
CategoryQueryController.searchCategoriesByOffset(SearchCategoriesApiRequest)
  │
  ├─ CategoryQueryApiMapper.toSearchParams(request)
  │   └─> CategorySearchParams (Application DTO)
  │
  ├─ SearchCategoryByOffsetUseCase.execute(params) ..................... [Port]
  │   │
  │   └─ SearchCategoryByOffsetService.execute(params) ................. [Impl]
  │       │
  │       ├─ CategoryQueryFactory.createCriteria(params)
  │       │   └─> CategorySearchCriteria (Domain VO)
  │       │
  │       ├─ CategoryReadManager.findByCriteria(criteria) ........... [@Transactional(readOnly=true)]
  │       │   │
  │       │   └─ CategoryQueryPort.findByCriteria(criteria) ............ [Domain Port]
  │       │       │
  │       │       └─ CategoryQueryAdapter.findByCriteria(criteria) ..... [Adapter Impl]
  │       │           │
  │       │           └─ CategoryQueryDslRepository.findByCriteria(criteria)
  │       │               │
  │       │               ├─ CategoryConditionBuilder (QueryDSL 조건 생성)
  │       │               │   ├─ parentIdEq()
  │       │               │   ├─ depthEq()
  │       │               │   ├─ leafEq()
  │       │               │   ├─ statusIn()
  │       │               │   ├─ departmentIn()
  │       │               │   ├─ categoryGroupIn()
  │       │               │   ├─ searchCondition()
  │       │               │   └─ notDeleted()
  │       │               │
  │       │               ├─ orderBy (sortKey + sortDirection)
  │       │               ├─ offset + limit (페이징)
  │       │               └─ fetch() → List<CategoryJpaEntity>
  │       │               │
  │       │               └─ CategoryJpaEntityMapper.toDomain()
  │       │                   └─> List<Category> (Domain Aggregate)
  │       │
  │       ├─ CategoryReadManager.countByCriteria(criteria)
  │       │   └─> totalElements (long)
  │       │
  │       └─ CategoryAssembler.toPageResult(categories, page, size, totalElements)
  │           └─> CategoryPageResult (Application DTO)
  │
  └─ CategoryQueryApiMapper.toPageResponse(pageResult)
      └─> ApiResponse<PageApiResponse<CategoryApiResponse>>
```

---

### Layer별 상세 분석

#### 1️⃣ Adapter-In Layer (REST API)

**Controller**: `CategoryQueryController`

```java
@RestController
@RequestMapping("/api/v1/market/admin/categories")
public class CategoryQueryController {
    private final SearchCategoryByOffsetUseCase searchCategoryByOffsetUseCase;
    private final CategoryQueryApiMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<CategoryApiResponse>>>
        searchCategoriesByOffset(@ParameterObject @Valid SearchCategoriesApiRequest request)
}
```

**Request DTO**: `SearchCategoriesApiRequest`

| 필드 | 타입 | 설명 | 필수 |
|------|------|------|------|
| parentId | Long | 부모 카테고리 ID | ❌ |
| depth | Integer | 계층 깊이 | ❌ |
| leaf | Boolean | 리프 노드 여부 | ❌ |
| statuses | List\<String\> | 상태 필터 (ACTIVE, INACTIVE) | ❌ |
| departments | List\<String\> | 부문 필터 (FASHION, BEAUTY, LIVING 등) | ❌ |
| categoryGroups | List\<String\> | 카테고리 그룹 필터 (CLOTHING, SHOES, DIGITAL 등) | ❌ |
| searchField | String | 검색 필드 (code, nameKo, nameEn) | ❌ |
| searchWord | String | 검색어 | ❌ |
| sortKey | String | 정렬 키 (sortOrder, createdAt, nameKo, code) | ❌ |
| sortDirection | String | 정렬 방향 (ASC, DESC) | ❌ |
| page | Integer | 페이지 번호 (0부터) | ❌ |
| size | Integer | 페이지 크기 | ❌ |

**Response DTO**: `CategoryApiResponse`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 카테고리 ID |
| code | String | 카테고리 코드 |
| nameKo | String | 한글명 |
| nameEn | String | 영문명 |
| parentId | Long | 부모 카테고리 ID |
| depth | int | 계층 깊이 |
| path | String | 경로 |
| sortOrder | int | 정렬 순서 |
| leaf | boolean | 리프 노드 여부 |
| status | String | 상태 |
| department | String | 부문 |
| categoryGroup | String | 카테고리 그룹 (고시정보 연결용) |
| createdAt | String | 생성일시 (ISO-8601) |
| updatedAt | String | 수정일시 (ISO-8601) |

**ApiMapper**: `CategoryQueryApiMapper`

```java
@Component
public class CategoryQueryApiMapper {
    // Request → Application Params
    public CategorySearchParams toSearchParams(SearchCategoriesApiRequest request)

    // Application Result → Response
    public CategoryApiResponse toResponse(CategoryResult result)
    public List<CategoryApiResponse> toResponses(List<CategoryResult> results)
    public PageApiResponse<CategoryApiResponse> toPageResponse(CategoryPageResult pageResult)
}
```

**변환 로직**:
- `SearchCategoriesApiRequest` → `CategorySearchParams` (Application Layer DTO)
- `CategoryPageResult` → `PageApiResponse<CategoryApiResponse>` (API Response)
- 날짜 포맷: `Instant` → ISO-8601 String (`DateTimeFormatUtils.formatIso8601()`)

---

#### 2️⃣ Application Layer

**UseCase Interface**: `SearchCategoryByOffsetUseCase`

```java
public interface SearchCategoryByOffsetUseCase {
    CategoryPageResult execute(CategorySearchParams params);
}
```

**Service Implementation**: `SearchCategoryByOffsetService`

```java
@Service
public class SearchCategoryByOffsetService implements SearchCategoryByOffsetUseCase {
    private final CategoryReadManager readManager;
    private final CategoryQueryFactory queryFactory;
    private final CategoryAssembler assembler;

    @Override
    public CategoryPageResult execute(CategorySearchParams params) {
        // 1. Criteria 생성 (Factory 패턴)
        CategorySearchCriteria criteria = queryFactory.createCriteria(params);

        // 2. 데이터 조회 (Manager 패턴)
        List<Category> categories = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        // 3. 결과 조립 (Assembler 패턴)
        return assembler.toPageResult(categories, params.page(), params.size(), totalElements);
    }
}
```

**Application DTOs**:

1. **CategorySearchParams** (Input)
   - `parentId`, `depth`, `leaf`
   - `statuses`, `departments`, `categoryGroups`
   - `searchField`, `searchWord`
   - `CommonSearchParams` (sortKey, sortDirection, page, size)

2. **CategoryPageResult** (Output)
   - `List<CategoryResult> results`
   - `PageMeta pageMeta` (page, size, totalElements)

3. **CategoryResult** (Item)
   - 도메인 Aggregate의 모든 필드를 평탄화한 DTO
   - `from(Category)` 정적 팩토리 메서드

**CategoryQueryFactory**:
- `CategorySearchParams` → `CategorySearchCriteria` 변환
- 문자열 → Enum 변환 (CategoryStatus, Department, CategoryGroup)
- 정렬 키 해석 (CategorySortKey)
- 페이징 정보 생성 (PageRequest, QueryContext)

**CategoryReadManager**:
- `@Transactional(readOnly = true)` 적용
- `CategoryQueryPort` 위임
- `getById()`, `findByCriteria()`, `countByCriteria()`, `existsByCode()`

**CategoryAssembler**:
- `Category` → `CategoryResult` 변환
- 페이징 결과 조립 (`CategoryPageResult`)

---

#### 3️⃣ Domain Layer

**Domain Port**: `CategoryQueryPort`

```java
public interface CategoryQueryPort {
    Optional<Category> findById(CategoryId id);
    List<Category> findByCriteria(CategorySearchCriteria criteria);
    long countByCriteria(CategorySearchCriteria criteria);
    boolean existsByCode(String code);
}
```

**Search Criteria**: `CategorySearchCriteria`

| 필드 | 타입 | 설명 |
|------|------|------|
| parentId | Long | 부모 카테고리 ID 필터 |
| depth | Integer | 계층 깊이 필터 |
| leaf | Boolean | 리프 노드 여부 필터 |
| statuses | List\<CategoryStatus\> | 상태 필터 (Enum) |
| departments | List\<Department\> | 부문 필터 (Enum) |
| categoryGroups | List\<CategoryGroup\> | 카테고리 그룹 필터 (Enum) |
| searchField | CategorySearchField | 검색 필드 (Enum) |
| searchWord | String | 검색어 |
| queryContext | QueryContext\<CategorySortKey\> | 정렬 + 페이징 |

**Aggregate Root**: `Category`

```java
public class Category {
    private final CategoryId id;
    private final CategoryCode code;
    private CategoryName categoryName;    // nameKo, nameEn
    private final Long parentId;
    private CategoryDepth depth;
    private CategoryPath path;
    private SortOrder sortOrder;
    private boolean leaf;
    private CategoryStatus status;
    private Department department;
    private CategoryGroup categoryGroup;
    private DeletionStatus deletionStatus;
    private final Instant createdAt;
    private Instant updatedAt;
}
```

**Domain Value Objects**:
- `CategoryId` (ID 타입 안전성)
- `CategoryCode` (카테고리 코드)
- `CategoryName` (nameKo, nameEn)
- `CategoryDepth` (계층 깊이)
- `CategoryPath` (경로)
- `CategoryStatus` (ACTIVE, INACTIVE)
- `Department` (FASHION, BEAUTY, LIVING, HOME_LIVING, SPORTS_LEISURE, KIDS_BABY, FOOD_BEVERAGES, PET_SUPPLIES, ETC)
- `CategoryGroup` (CLOTHING, SHOES, BAGS, ACCESSORIES, COSMETICS, JEWELRY, WATCHES, FURNITURE, DIGITAL, SPORTS, BABY_KIDS, ETC)

---

#### 4️⃣ Adapter-Out Layer (Persistence)

**Adapter Implementation**: `CategoryQueryAdapter`

```java
@Component
public class CategoryQueryAdapter implements CategoryQueryPort {
    private final CategoryQueryDslRepository repository;
    private final CategoryJpaEntityMapper mapper;

    @Override
    public List<Category> findByCriteria(CategorySearchCriteria criteria) {
        return repository.findByCriteria(criteria).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
```

**QueryDSL Repository**: `CategoryQueryDslRepository`

```java
@Repository
public class CategoryQueryDslRepository {
    private final JPAQueryFactory queryFactory;
    private final CategoryConditionBuilder conditionBuilder;

    public List<CategoryJpaEntity> findByCriteria(CategorySearchCriteria criteria) {
        return queryFactory
                .selectFrom(category)
                .where(
                        conditionBuilder.parentIdEq(criteria),
                        conditionBuilder.depthEq(criteria),
                        conditionBuilder.leafEq(criteria),
                        conditionBuilder.statusIn(criteria),
                        conditionBuilder.departmentIn(criteria),
                        conditionBuilder.categoryGroupIn(criteria),
                        conditionBuilder.searchCondition(criteria),
                        conditionBuilder.notDeleted())
                .orderBy(resolveOrderSpecifier(criteria))
                .offset(criteria.offset())
                .limit(criteria.size())
                .fetch();
    }
}
```

**Condition Builder**: `CategoryConditionBuilder`

| 메서드 | 조건 | 설명 |
|--------|------|------|
| parentIdEq() | `parent_id = ?` | 부모 카테고리 필터 |
| depthEq() | `depth = ?` | 깊이 필터 |
| leafEq() | `leaf = ?` | 리프 노드 여부 |
| statusIn() | `status IN (?)` | 상태 필터 (다중) |
| departmentIn() | `department IN (?)` | 부문 필터 (다중) |
| categoryGroupIn() | `category_group IN (?)` | 카테고리 그룹 필터 (다중) |
| searchCondition() | `name_ko LIKE ? OR name_en LIKE ? OR code LIKE ?` | 검색 (특정 필드 or 전체) |
| notDeleted() | `deleted_at IS NULL` | Soft Delete 제외 |

**Order By 해석**:

```java
private OrderSpecifier<?> resolveOrderSpecifier(CategorySearchCriteria criteria) {
    CategorySortKey sortKey = criteria.queryContext().sortKey();
    SortDirection direction = criteria.queryContext().sortDirection();

    return switch (sortKey) {
        case SORT_ORDER -> isAsc ? category.sortOrder.asc() : category.sortOrder.desc();
        case CREATED_AT -> isAsc ? category.createdAt.asc() : category.createdAt.desc();
        case NAME_KO -> isAsc ? category.nameKo.asc() : category.nameKo.desc();
        case CODE -> isAsc ? category.code.asc() : category.code.desc();
    };
}
```

**JPA Entity**: `CategoryJpaEntity`

```java
@Entity
@Table(name = "category")
public class CategoryJpaEntity extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "name_ko", nullable = false, length = 255)
    private String nameKo;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "depth", nullable = false)
    private int depth;

    @Column(name = "path", nullable = false, length = 1000)
    private String path;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "leaf", nullable = false)
    private boolean leaf;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "department", nullable = false, length = 30)
    private String department;

    @Column(name = "category_group", nullable = false, length = 50)
    private String categoryGroup;

    // SoftDeletableEntity: createdAt, updatedAt, deletedAt (Instant 타입)
}
```

**Entity Mapper**: `CategoryJpaEntityMapper`

```java
@Component
public class CategoryJpaEntityMapper {
    // Domain → Entity
    public CategoryJpaEntity toEntity(Category category)

    // Entity → Domain
    public Category toDomain(CategoryJpaEntity entity)
}
```

**매핑 특징**:
- Domain VO → String/int (Entity)
- String/int (Entity) → Domain VO
- `CategoryStatus.name()` ↔ `CategoryStatus.fromString()`
- `Department.name()` ↔ `Department.fromString()`
- `CategoryGroup.name()` ↔ `CategoryGroup.fromString()`

---

### Database Query 분석

**대상 테이블**: `category`

**SELECT 쿼리**:

```sql
SELECT *
FROM category
WHERE
    (parent_id = ? OR ? IS NULL)              -- 부모 카테고리 필터
    AND (depth = ? OR ? IS NULL)              -- 깊이 필터
    AND (leaf = ? OR ? IS NULL)               -- 리프 노드 필터
    AND (status IN (?, ?) OR status IS NULL)  -- 상태 필터
    AND (department IN (?, ?, ?) OR department IS NULL)  -- 부문 필터
    AND (category_group IN (?, ?, ?) OR category_group IS NULL)  -- 카테고리 그룹 필터
    AND (
        name_ko LIKE ?
        OR name_en LIKE ?
        OR code LIKE ?
        OR ? IS NULL
    )                                         -- 검색 조건
    AND deleted_at IS NULL                    -- Soft Delete 제외
ORDER BY sort_order DESC                      -- 정렬 (동적)
LIMIT ? OFFSET ?                              -- 페이징
```

**COUNT 쿼리**:

```sql
SELECT COUNT(*)
FROM category
WHERE
    (parent_id = ? OR ? IS NULL)
    AND (depth = ? OR ? IS NULL)
    AND (leaf = ? OR ? IS NULL)
    AND (status IN (?, ?) OR status IS NULL)
    AND (department IN (?, ?, ?) OR department IS NULL)
    AND (category_group IN (?, ?, ?) OR category_group IS NULL)
    AND (
        name_ko LIKE ?
        OR name_en LIKE ?
        OR code LIKE ?
        OR ? IS NULL
    )
    AND deleted_at IS NULL
```

**인덱스 활용**:
- `code` (UNIQUE)
- `parent_id` (조회 빈도 높음)
- `depth`, `status`, `department`, `category_group` (복합 인덱스 고려)
- `deleted_at` (Soft Delete 필터)

---

### 트랜잭션 경계

```
CategoryReadManager.findByCriteria()
    @Transactional(readOnly = true)
    └─> CategoryQueryPort.findByCriteria()
        └─> CategoryQueryAdapter.findByCriteria()
            └─> CategoryQueryDslRepository.findByCriteria()
                └─> JPAQueryFactory.fetch()
```

**특징**:
- `@Transactional(readOnly = true)`: 읽기 전용 최적화
- 조회 전용이므로 Dirty Checking 비활성화
- Transaction 범위: Manager 메서드 진입 ~ 종료

---

### 주요 패턴 및 원칙

#### 1. Hexagonal Architecture (포트/어댑터)
- **Application Port**: `SearchCategoryByOffsetUseCase` (In), `CategoryQueryPort` (Out)
- **Adapter-In**: `CategoryQueryController`, `CategoryQueryApiMapper`
- **Adapter-Out**: `CategoryQueryAdapter`, `CategoryQueryDslRepository`

#### 2. CQRS (Command-Query Separation)
- **Query 전용**: 조회만 수행, 상태 변경 없음
- **ReadManager**: `@Transactional(readOnly = true)` 적용

#### 3. DDD (Domain-Driven Design)
- **Aggregate Root**: `Category`
- **Value Objects**: `CategoryId`, `CategoryCode`, `CategoryName`, `CategoryDepth` 등
- **Domain Port**: 인프라 추상화

#### 4. Layered Architecture
- **Adapter-In**: DTO 변환 (API ↔ Application)
- **Application**: 비즈니스 로직 조율
- **Domain**: 핵심 도메인 규칙
- **Adapter-Out**: 영속성 구현

#### 5. Factory Pattern
- `CategoryQueryFactory`: Criteria 생성
- Enum 변환, 페이징 정보 생성

#### 6. Assembler Pattern
- `CategoryAssembler`: Domain → Application DTO 변환
- 페이징 결과 조립

#### 7. Manager Pattern
- `CategoryReadManager`: 조회 로직 캡슐화
- 트랜잭션 경계 설정

---

### 호출 흐름 요약

```
1. [Adapter-In] CategoryQueryController
   ├─ SearchCategoriesApiRequest (API 요청)
   └─ CategoryQueryApiMapper → CategorySearchParams

2. [Application] SearchCategoryByOffsetService
   ├─ CategoryQueryFactory → CategorySearchCriteria (Domain)
   ├─ CategoryReadManager → findByCriteria() + countByCriteria()
   └─ CategoryAssembler → CategoryPageResult

3. [Domain] CategoryQueryPort (인터페이스)

4. [Adapter-Out] CategoryQueryAdapter
   ├─ CategoryQueryDslRepository
   │   ├─ CategoryConditionBuilder (QueryDSL 조건)
   │   └─ fetch() → List<CategoryJpaEntity>
   └─ CategoryJpaEntityMapper → List<Category> (Domain)

5. [Adapter-In] CategoryQueryApiMapper
   └─ ApiResponse<PageApiResponse<CategoryApiResponse>>
```

---

### 참고사항

#### CategoryGroup (고시정보 연결)
- **12개 그룹**: CLOTHING, SHOES, BAGS, ACCESSORIES, COSMETICS, JEWELRY, WATCHES, FURNITURE, DIGITAL, SPORTS, BABY_KIDS, ETC
- **연결 구조**: `category` → `category_group` → `category_attribute_template` → `category_attribute_spec`
- **마이그레이션**: V12 (product_group→category_group), V13 (missing templates)

#### Soft Delete
- `deleted_at IS NULL` 조건으로 삭제된 카테고리 제외
- `SoftDeletableEntity` 상속 (createdAt, updatedAt, deletedAt)

#### 기본값
- Page: 0
- Size: 20
- SortKey: `SORT_ORDER`
- SortDirection: `DESC`

---

## 📊 성능 고려사항

### 1. N+1 문제 방지
- 단일 쿼리로 모든 필드 조회 (JOIN 없음)
- `CategoryJpaEntity`가 모든 필드 포함

### 2. 페이징 최적화
- Offset 기반 페이징 (작은 데이터셋에 적합)
- COUNT 쿼리 별도 실행 (totalElements 조회)

### 3. 인덱스 활용
- `code` UNIQUE 인덱스
- `parent_id` 조회 빈도 높음 → 인덱스 고려
- 복합 조건 조회 → 복합 인덱스 고려

### 4. 트랜잭션 최적화
- `@Transactional(readOnly = true)`: Dirty Checking 비활성화
- 조회 전용 최적화

---

## 🔧 개선 제안

### 1. 대규모 데이터
- Offset 페이징 → Cursor 기반 페이징 (성능)
- 캐싱 전략 (Redis) 고려

### 2. 복잡한 필터링
- ElasticSearch 연동 고려 (전문 검색)

### 3. 계층 구조 조회
- Recursive CTE (MySQL 8.0+) 활용
- 트리 구조 최적화 (Nested Set, Closure Table)

---

## 📝 테스트 가이드

### 1. 단위 테스트
- `CategoryQueryFactory` (Criteria 생성)
- `CategoryAssembler` (DTO 변환)
- `CategoryConditionBuilder` (QueryDSL 조건)

### 2. 통합 테스트
- `SearchCategoryByOffsetService` (Service 계층)
- `CategoryQueryAdapter` (Repository 계층)

### 3. E2E 테스트
- `CategoryQueryController` (API 계층)
- 페이징, 필터링, 정렬 조합 테스트

---

## 📚 관련 문서

- [Category 도메인 분석 보고서](../category-analysis-report.md)
- [API 엔드포인트 명세](../../api-endpoints/admin/category_endpoints.md) (생성 필요)
- [마이그레이션 가이드](../../migrations/V12_V13_category_refinement.md) (생성 필요)

---

**분석 완료일**: 2026-02-06
**분석 대상 브랜치**: feature/MAR-56-full-project-refactoring
