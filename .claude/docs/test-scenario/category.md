# Category E2E 통합 테스트 시나리오

**작성일**: 2026-02-06
**대상 엔드포인트**: GET /api/v1/market/admin/categories
**테스트 범위**: 카테고리 목록 조회 (Offset 기반 페이징)

---

## 목차

1. [개요](#개요)
2. [테스트 환경 설정](#테스트-환경-설정)
3. [시나리오 분류](#시나리오-분류)
4. [Query 엔드포인트 시나리오](#query-엔드포인트-시나리오)
5. [Fixture 설계](#fixture-설계)
6. [실행 가이드](#실행-가이드)

---

## 개요

### 테스트 대상

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /api/v1/market/admin/categories |
| Controller | CategoryQueryController |
| UseCase | SearchCategoryByOffsetUseCase |

### 주요 기능

- **계층 구조 조회**: parentId, depth, path 기반 트리 구조
- **고시정보 연결**: categoryGroup 필드로 고시정보 템플릿 매핑
- **12개 필터 지원**: 상태, 부문, 카테고리 그룹, 리프 노드 등
- **검색 기능**: 코드, 한글명, 영문명 검색
- **정렬 옵션**: sortOrder, createdAt, nameKo, code
- **페이징**: Offset 기반 페이징 (page, size)

### 시나리오 통계

| 우선순위 | 개수 | 설명 |
|---------|------|------|
| P0 (필수) | 18개 | 기본 조회, 필터링, 페이징 핵심 기능 |
| P1 (중요) | 12개 | 복합 필터, 엣지 케이스, 성능 검증 |
| **합계** | **30개** | 전체 시나리오 |

---

## 테스트 환경 설정

### 필요 Repository

```java
@Autowired
private CategoryJpaRepository categoryJpaRepository;
```

### testFixtures

```java
// adapter-out/persistence-mysql/src/testFixtures/
import static com.ryuqq.marketplace.adapter.out.persistence.category.CategoryJpaEntityFixtures.*;
```

### setUp 패턴

```java
@BeforeEach
void setUp() {
    categoryJpaRepository.deleteAll();
    // 각 테스트에서 필요한 데이터 생성
}
```

---

## 시나리오 분류

### Query 엔드포인트 시나리오: 30개

#### 기본 조회 (4개 - P0)
1. 데이터 존재 시 정상 조회
2. 데이터 없을 때 빈 목록 반환
3. 삭제된 카테고리 제외 확인
4. 기본 정렬 순서 확인 (sortOrder DESC)

#### 필터링 - parentId (3개 - P0)
5. parentId 필터 - 최상위 카테고리 조회 (parentId=null)
6. parentId 필터 - 특정 부모의 자식 조회
7. parentId 필터 - 존재하지 않는 부모 ID (빈 목록)

#### 필터링 - depth (3개 - P0)
8. depth 필터 - depth=1 조회
9. depth 필터 - depth=2 조회
10. depth 필터 - depth=3 조회

#### 필터링 - leaf (2개 - P0)
11. leaf 필터 - 리프 노드만 조회 (leaf=true)
12. leaf 필터 - 비리프 노드만 조회 (leaf=false)

#### 필터링 - statuses (3개 - P0)
13. statuses 필터 - ACTIVE만 조회
14. statuses 필터 - INACTIVE만 조회
15. statuses 필터 - 다중 상태 조회 (ACTIVE, INACTIVE)

#### 필터링 - departments (4개 - P0)
16. departments 필터 - FASHION만 조회
17. departments 필터 - BEAUTY만 조회
18. departments 필터 - 다중 부문 조회 (FASHION, BEAUTY)
19. departments 필터 - 존재하지 않는 부문 (빈 목록)

#### 필터링 - categoryGroups (5개 - P0)
20. categoryGroups 필터 - CLOTHING만 조회
21. categoryGroups 필터 - SHOES만 조회
22. categoryGroups 필터 - DIGITAL만 조회
23. categoryGroups 필터 - 다중 그룹 조회 (CLOTHING, SHOES)
24. categoryGroups 필터 - ETC 그룹 조회

#### 검색 (4개 - P0)
25. searchField=code + searchWord 검색
26. searchField=nameKo + searchWord 검색
27. searchField=nameEn + searchWord 검색
28. searchField 없이 전체 필드 검색

#### 페이징 (2개 - P0)
29. 페이징 - page=0, size=2 (첫 페이지)
30. 페이징 - page=1, size=2 (두 번째 페이지)

#### 정렬 (4개 - P1)
31. 정렬 - sortKey=sortOrder, sortDirection=ASC
32. 정렬 - sortKey=createdAt, sortDirection=DESC
33. 정렬 - sortKey=nameKo, sortDirection=ASC
34. 정렬 - sortKey=code, sortDirection=DESC

#### 복합 필터 (6개 - P1)
35. 복합 필터 - parentId + statuses
36. 복합 필터 - depth + departments
37. 복합 필터 - leaf + categoryGroups
38. 복합 필터 - statuses + departments + categoryGroups
39. 복합 필터 - 검색 + 필터 조합
40. 복합 필터 - 모든 필터 조합

---

## Query 엔드포인트 시나리오

### 1. 기본 조회 시나리오

#### TC-Q-001: 데이터 존재 시 정상 조회
**우선순위**: P0
**사전 데이터**: 카테고리 5건 저장

```java
@Test
@DisplayName("카테고리 목록 조회 - 데이터 존재 시 정상 조회")
void searchCategories_whenDataExists_thenReturns200() {
    // Given: 5개 카테고리 저장
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),
        activeRootEntity(2L),
        activeRootEntity(3L),
        activeRootEntity(4L),
        activeRootEntity(5L)
    ));

    // When: 조회
    GET /api/v1/market/admin/categories

    // Then: 200 OK
    // - success: true
    // - data.content.size > 0
    // - data.totalElements = 5
    // - data.hasNext = false
}
```

**검증 포인트**:
- HTTP 상태 코드: 200
- `content` 배열에 데이터 존재
- `totalElements` = 5
- `hasNext` = false (한 페이지에 모두 포함)

---

#### TC-Q-002: 데이터 없을 때 빈 목록 반환
**우선순위**: P0
**사전 데이터**: 없음

```java
@Test
@DisplayName("카테고리 목록 조회 - 데이터 없을 때 빈 목록 반환")
void searchCategories_whenNoData_thenReturnsEmptyList() {
    // Given: 데이터 없음

    // When: 조회
    GET /api/v1/market/admin/categories

    // Then: 200 OK
    // - success: true
    // - data.content.size = 0
    // - data.totalElements = 0
}
```

**검증 포인트**:
- HTTP 상태 코드: 200
- `content` 빈 배열
- `totalElements` = 0

---

#### TC-Q-003: 삭제된 카테고리 제외 확인
**우선순위**: P0
**사전 데이터**: 활성 2건 + 삭제 1건

```java
@Test
@DisplayName("카테고리 목록 조회 - 삭제된 카테고리 제외")
void searchCategories_whenDeletedExist_thenExcludesDeleted() {
    // Given: 활성 2건, 삭제 1건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),
        activeRootEntity(2L),
        deletedEntity()  // ID=3, deletedAt != null
    ));

    // When: 조회
    GET /api/v1/market/admin/categories

    // Then: 200 OK
    // - data.content.size = 2 (삭제된 항목 제외)
    // - data.totalElements = 2
}
```

**검증 포인트**:
- 삭제된 카테고리(deletedAt != null) 제외
- 활성 카테고리만 반환

---

#### TC-Q-004: 기본 정렬 순서 확인 (sortOrder DESC)
**우선순위**: P0
**사전 데이터**: sortOrder가 다른 카테고리 3건

```java
@Test
@DisplayName("카테고리 목록 조회 - 기본 정렬 순서 (sortOrder DESC)")
void searchCategories_whenNoSortSpecified_thenSortsBySortOrderDesc() {
    // Given: sortOrder가 다른 카테고리 3건
    categoryJpaRepository.saveAll(List.of(
        CategoryJpaEntity.create(1L, "CAT001", "카테고리1", "Category1",
            null, 1, "/1", 100, true, "ACTIVE", "FASHION", "CLOTHING",
            Instant.now(), Instant.now(), null),
        CategoryJpaEntity.create(2L, "CAT002", "카테고리2", "Category2",
            null, 1, "/2", 200, true, "ACTIVE", "FASHION", "CLOTHING",
            Instant.now(), Instant.now(), null),
        CategoryJpaEntity.create(3L, "CAT003", "카테고리3", "Category3",
            null, 1, "/3", 150, true, "ACTIVE", "FASHION", "CLOTHING",
            Instant.now(), Instant.now(), null)
    ));

    // When: 정렬 지정 없이 조회
    GET /api/v1/market/admin/categories

    // Then: 200 OK
    // - data.content[0].sortOrder = 200
    // - data.content[1].sortOrder = 150
    // - data.content[2].sortOrder = 100
}
```

**검증 포인트**:
- 기본 정렬: sortOrder DESC
- 순서 확인: 200 → 150 → 100

---

### 2. parentId 필터 시나리오

#### TC-Q-005: 최상위 카테고리 조회 (parentId=null)
**우선순위**: P0
**사전 데이터**: 루트 카테고리 3건 + 자식 카테고리 2건

```java
@Test
@DisplayName("parentId 필터 - 최상위 카테고리 조회")
void searchCategories_whenFilterByParentIdNull_thenReturnsRootCategories() {
    // Given: 루트 3건, 자식 2건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),  // parentId = null
        activeRootEntity(2L),  // parentId = null
        activeRootEntity(3L),  // parentId = null
        activeChildEntity(1L), // parentId = 1
        activeChildEntity(2L)  // parentId = 2
    ));

    // When: parentId 없이 조회 (최상위만)
    GET /api/v1/market/admin/categories?depth=1

    // Then: 200 OK
    // - data.content.size = 3
    // - 모든 항목의 parentId = null
}
```

**검증 포인트**:
- depth=1 조건으로 루트 카테고리 조회
- 반환된 모든 항목의 parentId = null

---

#### TC-Q-006: 특정 부모의 자식 조회
**우선순위**: P0
**사전 데이터**: 부모 카테고리 1건 + 자식 3건

```java
@Test
@DisplayName("parentId 필터 - 특정 부모의 자식 조회")
void searchCategories_whenFilterByParentId_thenReturnsChildren() {
    // Given: 부모 1건, 자식 3건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),
        depth2Entity(1L),  // parentId = 1
        depth2Entity(1L),  // parentId = 1
        depth2Entity(1L)   // parentId = 1
    ));

    // When: parentId=1 조회
    GET /api/v1/market/admin/categories?parentId=1

    // Then: 200 OK
    // - data.content.size = 3
    // - 모든 항목의 parentId = 1
}
```

**검증 포인트**:
- parentId 필터 동작 확인
- 반환된 모든 항목의 parentId = 1

---

#### TC-Q-007: 존재하지 않는 부모 ID (빈 목록)
**우선순위**: P0
**사전 데이터**: 카테고리 3건

```java
@Test
@DisplayName("parentId 필터 - 존재하지 않는 부모 ID")
void searchCategories_whenFilterByNonExistentParentId_thenReturnsEmpty() {
    // Given: 카테고리 3건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),
        activeRootEntity(2L),
        activeRootEntity(3L)
    ));

    // When: parentId=9999 조회
    GET /api/v1/market/admin/categories?parentId=9999

    // Then: 200 OK
    // - data.content.size = 0
}
```

**검증 포인트**:
- 존재하지 않는 parentId 조회 시 빈 목록 반환
- 오류 발생하지 않음

---

### 3. depth 필터 시나리오

#### TC-Q-008: depth=1 조회
**우선순위**: P0
**사전 데이터**: depth 1, 2, 3 각 2건씩

```java
@Test
@DisplayName("depth 필터 - depth=1 조회")
void searchCategories_whenFilterByDepth1_thenReturnsDepth1Only() {
    // Given: depth 1, 2, 3 각 2건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),        // depth=1
        activeRootEntity(2L),        // depth=1
        depth2Entity(1L),            // depth=2
        depth2Entity(2L),            // depth=2
        depth3Entity(3L),            // depth=3
        depth3Entity(4L)             // depth=3
    ));

    // When: depth=1 조회
    GET /api/v1/market/admin/categories?depth=1

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 depth = 1
}
```

**검증 포인트**:
- depth 필터 동작 확인
- 반환된 모든 항목의 depth = 1

---

#### TC-Q-009: depth=2 조회
**우선순위**: P0
**사전 데이터**: depth 1, 2, 3 각 2건씩

```java
@Test
@DisplayName("depth 필터 - depth=2 조회")
void searchCategories_whenFilterByDepth2_thenReturnsDepth2Only() {
    // Given: depth 1, 2, 3 각 2건
    // (TC-Q-008과 동일)

    // When: depth=2 조회
    GET /api/v1/market/admin/categories?depth=2

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 depth = 2
}
```

**검증 포인트**:
- depth=2 필터 동작
- 반환된 모든 항목의 depth = 2

---

#### TC-Q-010: depth=3 조회
**우선순위**: P0
**사전 데이터**: depth 1, 2, 3 각 2건씩

```java
@Test
@DisplayName("depth 필터 - depth=3 조회")
void searchCategories_whenFilterByDepth3_thenReturnsDepth3Only() {
    // Given: depth 1, 2, 3 각 2건
    // (TC-Q-008과 동일)

    // When: depth=3 조회
    GET /api/v1/market/admin/categories?depth=3

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 depth = 3
}
```

**검증 포인트**:
- depth=3 필터 동작
- 반환된 모든 항목의 depth = 3

---

### 4. leaf 필터 시나리오

#### TC-Q-011: 리프 노드만 조회 (leaf=true)
**우선순위**: P0
**사전 데이터**: 리프 노드 3건 + 비리프 노드 2건

```java
@Test
@DisplayName("leaf 필터 - 리프 노드만 조회")
void searchCategories_whenFilterByLeafTrue_thenReturnsLeafOnly() {
    // Given: 리프 3건, 비리프 2건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),    // leaf=true
        activeRootEntity(2L),    // leaf=true
        activeRootEntity(3L),    // leaf=true
        nonLeafEntity(),         // leaf=false
        nonLeafEntity()          // leaf=false
    ));

    // When: leaf=true 조회
    GET /api/v1/market/admin/categories?leaf=true

    // Then: 200 OK
    // - data.content.size = 3
    // - 모든 항목의 leaf = true
}
```

**검증 포인트**:
- leaf=true 필터 동작
- 상품 등록 가능한 카테고리만 반환

---

#### TC-Q-012: 비리프 노드만 조회 (leaf=false)
**우선순위**: P0
**사전 데이터**: 리프 노드 3건 + 비리프 노드 2건

```java
@Test
@DisplayName("leaf 필터 - 비리프 노드만 조회")
void searchCategories_whenFilterByLeafFalse_thenReturnsNonLeafOnly() {
    // Given: 리프 3건, 비리프 2건
    // (TC-Q-011과 동일)

    // When: leaf=false 조회
    GET /api/v1/market/admin/categories?leaf=false

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 leaf = false
}
```

**검증 포인트**:
- leaf=false 필터 동작
- 자식이 있는 부모 카테고리만 반환

---

### 5. statuses 필터 시나리오

#### TC-Q-013: ACTIVE만 조회
**우선순위**: P0
**사전 데이터**: ACTIVE 3건 + INACTIVE 2건

```java
@Test
@DisplayName("statuses 필터 - ACTIVE만 조회")
void searchCategories_whenFilterByActiveStatus_thenReturnsActiveOnly() {
    // Given: ACTIVE 3건, INACTIVE 2건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),      // status=ACTIVE
        activeRootEntity(2L),      // status=ACTIVE
        activeRootEntity(3L),      // status=ACTIVE
        inactiveEntity(),          // status=INACTIVE
        newInactiveEntity()        // status=INACTIVE
    ));

    // When: statuses=ACTIVE 조회
    GET /api/v1/market/admin/categories?statuses=ACTIVE

    // Then: 200 OK
    // - data.content.size = 3
    // - 모든 항목의 status = "ACTIVE"
}
```

**검증 포인트**:
- statuses 필터 동작
- ACTIVE 상태만 반환

---

#### TC-Q-014: INACTIVE만 조회
**우선순위**: P0
**사전 데이터**: ACTIVE 3건 + INACTIVE 2건

```java
@Test
@DisplayName("statuses 필터 - INACTIVE만 조회")
void searchCategories_whenFilterByInactiveStatus_thenReturnsInactiveOnly() {
    // Given: ACTIVE 3건, INACTIVE 2건
    // (TC-Q-013과 동일)

    // When: statuses=INACTIVE 조회
    GET /api/v1/market/admin/categories?statuses=INACTIVE

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 status = "INACTIVE"
}
```

**검증 포인트**:
- INACTIVE 상태만 반환
- 비활성 카테고리 필터링

---

#### TC-Q-015: 다중 상태 조회 (ACTIVE, INACTIVE)
**우선순위**: P0
**사전 데이터**: ACTIVE 3건 + INACTIVE 2건

```java
@Test
@DisplayName("statuses 필터 - 다중 상태 조회")
void searchCategories_whenFilterByMultipleStatuses_thenReturnsAll() {
    // Given: ACTIVE 3건, INACTIVE 2건
    // (TC-Q-013과 동일)

    // When: statuses=ACTIVE,INACTIVE 조회
    GET /api/v1/market/admin/categories?statuses=ACTIVE,INACTIVE

    // Then: 200 OK
    // - data.content.size = 5
}
```

**검증 포인트**:
- 다중 상태 필터 동작
- ACTIVE + INACTIVE 모두 반환

---

### 6. departments 필터 시나리오

#### TC-Q-016: FASHION만 조회
**우선순위**: P0
**사전 데이터**: FASHION 3건, BEAUTY 2건, DIGITAL 1건

```java
@Test
@DisplayName("departments 필터 - FASHION만 조회")
void searchCategories_whenFilterByFashionDepartment_thenReturnsFashionOnly() {
    // Given: 다양한 부문
    categoryJpaRepository.saveAll(List.of(
        entityWithDepartment("FASHION"),
        entityWithDepartment("FASHION"),
        entityWithDepartment("FASHION"),
        entityWithDepartment("BEAUTY"),
        entityWithDepartment("BEAUTY"),
        entityWithDepartment("DIGITAL")
    ));

    // When: departments=FASHION 조회
    GET /api/v1/market/admin/categories?departments=FASHION

    // Then: 200 OK
    // - data.content.size = 3
    // - 모든 항목의 department = "FASHION"
}
```

**검증 포인트**:
- departments 필터 동작
- FASHION 부문만 반환

---

#### TC-Q-017: BEAUTY만 조회
**우선순위**: P0
**사전 데이터**: FASHION 3건, BEAUTY 2건, DIGITAL 1건

```java
@Test
@DisplayName("departments 필터 - BEAUTY만 조회")
void searchCategories_whenFilterByBeautyDepartment_thenReturnsBeautyOnly() {
    // Given: 다양한 부문 (TC-Q-016과 동일)

    // When: departments=BEAUTY 조회
    GET /api/v1/market/admin/categories?departments=BEAUTY

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 department = "BEAUTY"
}
```

**검증 포인트**:
- BEAUTY 부문만 반환

---

#### TC-Q-018: 다중 부문 조회 (FASHION, BEAUTY)
**우선순위**: P0
**사전 데이터**: FASHION 3건, BEAUTY 2건, DIGITAL 1건

```java
@Test
@DisplayName("departments 필터 - 다중 부문 조회")
void searchCategories_whenFilterByMultipleDepartments_thenReturnsMatching() {
    // Given: 다양한 부문 (TC-Q-016과 동일)

    // When: departments=FASHION,BEAUTY 조회
    GET /api/v1/market/admin/categories?departments=FASHION,BEAUTY

    // Then: 200 OK
    // - data.content.size = 5
}
```

**검증 포인트**:
- 다중 부문 필터 동작
- FASHION + BEAUTY 반환

---

#### TC-Q-019: 존재하지 않는 부문 (빈 목록)
**우선순위**: P0
**사전 데이터**: FASHION 3건

```java
@Test
@DisplayName("departments 필터 - 존재하지 않는 부문")
void searchCategories_whenFilterByNonExistentDepartment_thenReturnsEmpty() {
    // Given: FASHION만 존재
    categoryJpaRepository.saveAll(List.of(
        entityWithDepartment("FASHION"),
        entityWithDepartment("FASHION"),
        entityWithDepartment("FASHION")
    ));

    // When: departments=PET 조회 (데이터 없음)
    GET /api/v1/market/admin/categories?departments=PET

    // Then: 200 OK
    // - data.content.size = 0
}
```

**검증 포인트**:
- 존재하지 않는 부문 조회 시 빈 목록
- 오류 발생하지 않음

---

### 7. categoryGroups 필터 시나리오

#### TC-Q-020: CLOTHING만 조회
**우선순위**: P0
**사전 데이터**: CLOTHING 3건, SHOES 2건, DIGITAL 1건, ETC 1건

```java
@Test
@DisplayName("categoryGroups 필터 - CLOTHING만 조회")
void searchCategories_whenFilterByClothingGroup_thenReturnsClothingOnly() {
    // Given: 다양한 카테고리 그룹
    categoryJpaRepository.saveAll(List.of(
        entityWithCategoryGroup("CLOTHING"),
        entityWithCategoryGroup("CLOTHING"),
        entityWithCategoryGroup("CLOTHING"),
        entityWithCategoryGroup("SHOES"),
        entityWithCategoryGroup("SHOES"),
        entityWithCategoryGroup("DIGITAL"),
        entityWithCategoryGroup("ETC")
    ));

    // When: categoryGroups=CLOTHING 조회
    GET /api/v1/market/admin/categories?categoryGroups=CLOTHING

    // Then: 200 OK
    // - data.content.size = 3
    // - 모든 항목의 categoryGroup = "CLOTHING"
}
```

**검증 포인트**:
- categoryGroups 필터 동작
- CLOTHING 그룹만 반환 (고시정보 연결)

---

#### TC-Q-021: SHOES만 조회
**우선순위**: P0
**사전 데이터**: CLOTHING 3건, SHOES 2건, DIGITAL 1건, ETC 1건

```java
@Test
@DisplayName("categoryGroups 필터 - SHOES만 조회")
void searchCategories_whenFilterByShoesGroup_thenReturnsShoesOnly() {
    // Given: 다양한 카테고리 그룹 (TC-Q-020과 동일)

    // When: categoryGroups=SHOES 조회
    GET /api/v1/market/admin/categories?categoryGroups=SHOES

    // Then: 200 OK
    // - data.content.size = 2
    // - 모든 항목의 categoryGroup = "SHOES"
}
```

**검증 포인트**:
- SHOES 그룹만 반환

---

#### TC-Q-022: DIGITAL만 조회
**우선순위**: P0
**사전 데이터**: CLOTHING 3건, SHOES 2건, DIGITAL 1건, ETC 1건

```java
@Test
@DisplayName("categoryGroups 필터 - DIGITAL만 조회")
void searchCategories_whenFilterByDigitalGroup_thenReturnsDigitalOnly() {
    // Given: 다양한 카테고리 그룹 (TC-Q-020과 동일)

    // When: categoryGroups=DIGITAL 조회
    GET /api/v1/market/admin/categories?categoryGroups=DIGITAL

    // Then: 200 OK
    // - data.content.size = 1
    // - categoryGroup = "DIGITAL"
}
```

**검증 포인트**:
- DIGITAL 그룹만 반환 (가전/전자제품)

---

#### TC-Q-023: 다중 그룹 조회 (CLOTHING, SHOES)
**우선순위**: P0
**사전 데이터**: CLOTHING 3건, SHOES 2건, DIGITAL 1건, ETC 1건

```java
@Test
@DisplayName("categoryGroups 필터 - 다중 그룹 조회")
void searchCategories_whenFilterByMultipleGroups_thenReturnsMatching() {
    // Given: 다양한 카테고리 그룹 (TC-Q-020과 동일)

    // When: categoryGroups=CLOTHING,SHOES 조회
    GET /api/v1/market/admin/categories?categoryGroups=CLOTHING,SHOES

    // Then: 200 OK
    // - data.content.size = 5
}
```

**검증 포인트**:
- 다중 그룹 필터 동작
- CLOTHING + SHOES 반환

---

#### TC-Q-024: ETC 그룹 조회
**우선순위**: P0
**사전 데이터**: CLOTHING 3건, SHOES 2건, DIGITAL 1건, ETC 1건

```java
@Test
@DisplayName("categoryGroups 필터 - ETC 그룹 조회")
void searchCategories_whenFilterByEtcGroup_thenReturnsEtcOnly() {
    // Given: 다양한 카테고리 그룹 (TC-Q-020과 동일)

    // When: categoryGroups=ETC 조회
    GET /api/v1/market/admin/categories?categoryGroups=ETC

    // Then: 200 OK
    // - data.content.size = 1
    // - categoryGroup = "ETC"
}
```

**검증 포인트**:
- ETC 그룹 조회 (미분류 카테고리)

---

### 8. 검색 시나리오

#### TC-Q-025: searchField=code + searchWord 검색
**우선순위**: P0
**사전 데이터**: 다양한 code를 가진 카테고리 5건

```java
@Test
@DisplayName("검색 - code 필드 검색")
void searchCategories_whenSearchByCode_thenReturnsMatchingCode() {
    // Given: 다양한 code
    categoryJpaRepository.saveAll(List.of(
        activeEntityWithCode("CLOTH_001"),
        activeEntityWithCode("CLOTH_002"),
        activeEntityWithCode("SHOES_001"),
        activeEntityWithCode("BAG_001"),
        activeEntityWithCode("DIGITAL_001")
    ));

    // When: searchField=code&searchWord=CLOTH 검색
    GET /api/v1/market/admin/categories?searchField=code&searchWord=CLOTH

    // Then: 200 OK
    // - data.content.size = 2
    // - code에 "CLOTH" 포함
}
```

**검증 포인트**:
- code 필드 검색 동작
- LIKE 검색 (부분 일치)

---

#### TC-Q-026: searchField=nameKo + searchWord 검색
**우선순위**: P0
**사전 데이터**: 다양한 한글명 카테고리 5건

```java
@Test
@DisplayName("검색 - nameKo 필드 검색")
void searchCategories_whenSearchByNameKo_thenReturnsMatchingNameKo() {
    // Given: 다양한 한글명
    categoryJpaRepository.saveAll(List.of(
        activeEntityWithName("남성 의류", "Men's Clothing"),
        activeEntityWithName("여성 의류", "Women's Clothing"),
        activeEntityWithName("아동 의류", "Kids Clothing"),
        activeEntityWithName("신발", "Shoes"),
        activeEntityWithName("가방", "Bags")
    ));

    // When: searchField=nameKo&searchWord=의류 검색
    GET /api/v1/market/admin/categories?searchField=nameKo&searchWord=의류

    // Then: 200 OK
    // - data.content.size = 3
    // - nameKo에 "의류" 포함
}
```

**검증 포인트**:
- nameKo 필드 검색 동작
- 한글 검색 지원

---

#### TC-Q-027: searchField=nameEn + searchWord 검색
**우선순위**: P0
**사전 데이터**: 다양한 영문명 카테고리 5건

```java
@Test
@DisplayName("검색 - nameEn 필드 검색")
void searchCategories_whenSearchByNameEn_thenReturnsMatchingNameEn() {
    // Given: 다양한 영문명 (TC-Q-026과 동일)

    // When: searchField=nameEn&searchWord=Clothing 검색
    GET /api/v1/market/admin/categories?searchField=nameEn&searchWord=Clothing

    // Then: 200 OK
    // - data.content.size = 3
    // - nameEn에 "Clothing" 포함
}
```

**검증 포인트**:
- nameEn 필드 검색 동작
- 영문 검색 지원

---

#### TC-Q-028: searchField 없이 전체 필드 검색
**우선순위**: P0
**사전 데이터**: 다양한 카테고리 5건

```java
@Test
@DisplayName("검색 - searchField 없이 전체 필드 검색")
void searchCategories_whenSearchWithoutField_thenSearchesAllFields() {
    // Given: 다양한 카테고리
    categoryJpaRepository.saveAll(List.of(
        activeEntityWithCode("TEST_001"),
        activeEntityWithName("테스트 카테고리", "Test Category"),
        activeEntityWithCode("OTHER_002"),
        activeEntityWithName("기타", "Other"),
        activeEntityWithCode("FINAL_003")
    ));

    // When: searchWord=TEST (searchField 없음)
    GET /api/v1/market/admin/categories?searchWord=TEST

    // Then: 200 OK
    // - data.content.size >= 2
    // - code 또는 nameKo 또는 nameEn에 "TEST" 포함
}
```

**검증 포인트**:
- searchField 없을 때 전체 필드 검색
- OR 조건 동작 확인

---

### 9. 페이징 시나리오

#### TC-Q-029: 페이징 - page=0, size=2 (첫 페이지)
**우선순위**: P0
**사전 데이터**: 카테고리 5건

```java
@Test
@DisplayName("페이징 - 첫 페이지 조회 (page=0, size=2)")
void searchCategories_whenPagingFirstPage_thenReturnsFirstPage() {
    // Given: 5건 저장
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),
        activeRootEntity(2L),
        activeRootEntity(3L),
        activeRootEntity(4L),
        activeRootEntity(5L)
    ));

    // When: page=0, size=2 조회
    GET /api/v1/market/admin/categories?page=0&size=2

    // Then: 200 OK
    // - data.content.size = 2
    // - data.totalElements = 5
    // - data.currentPage = 0
    // - data.pageSize = 2
    // - data.hasNext = true
    // - data.hasPrevious = false
}
```

**검증 포인트**:
- 페이징 동작 확인
- hasNext = true (다음 페이지 존재)
- hasPrevious = false (이전 페이지 없음)

---

#### TC-Q-030: 페이징 - page=1, size=2 (두 번째 페이지)
**우선순위**: P0
**사전 데이터**: 카테고리 5건

```java
@Test
@DisplayName("페이징 - 두 번째 페이지 조회 (page=1, size=2)")
void searchCategories_whenPagingSecondPage_thenReturnsSecondPage() {
    // Given: 5건 저장 (TC-Q-029와 동일)

    // When: page=1, size=2 조회
    GET /api/v1/market/admin/categories?page=1&size=2

    // Then: 200 OK
    // - data.content.size = 2
    // - data.totalElements = 5
    // - data.currentPage = 1
    // - data.pageSize = 2
    // - data.hasNext = true
    // - data.hasPrevious = true
}
```

**검증 포인트**:
- 두 번째 페이지 조회
- hasNext = true (다음 페이지 존재)
- hasPrevious = true (이전 페이지 존재)

---

### 10. 정렬 시나리오 (P1)

#### TC-Q-031: 정렬 - sortKey=sortOrder, sortDirection=ASC
**우선순위**: P1
**사전 데이터**: sortOrder가 다른 카테고리 5건

```java
@Test
@DisplayName("정렬 - sortOrder ASC")
void searchCategories_whenSortBySortOrderAsc_thenSortsAscending() {
    // Given: sortOrder가 다른 카테고리
    categoryJpaRepository.saveAll(List.of(
        entityWithSortOrder(100),
        entityWithSortOrder(200),
        entityWithSortOrder(50),
        entityWithSortOrder(150),
        entityWithSortOrder(300)
    ));

    // When: sortKey=sortOrder&sortDirection=ASC
    GET /api/v1/market/admin/categories?sortKey=sortOrder&sortDirection=ASC

    // Then: 200 OK
    // - data.content[0].sortOrder = 50
    // - data.content[1].sortOrder = 100
    // - data.content[2].sortOrder = 150
    // - 오름차순 정렬 확인
}
```

**검증 포인트**:
- sortOrder ASC 정렬 동작
- 순서 확인

---

#### TC-Q-032: 정렬 - sortKey=createdAt, sortDirection=DESC
**우선순위**: P1
**사전 데이터**: 생성 시간이 다른 카테고리 5건

```java
@Test
@DisplayName("정렬 - createdAt DESC")
void searchCategories_whenSortByCreatedAtDesc_thenSortsDescending() {
    // Given: 시간차를 두고 생성
    categoryJpaRepository.saveAll(List.of(
        entityWithCreatedAt(Instant.parse("2026-01-01T00:00:00Z")),
        entityWithCreatedAt(Instant.parse("2026-01-02T00:00:00Z")),
        entityWithCreatedAt(Instant.parse("2026-01-03T00:00:00Z")),
        entityWithCreatedAt(Instant.parse("2026-01-04T00:00:00Z")),
        entityWithCreatedAt(Instant.parse("2026-01-05T00:00:00Z"))
    ));

    // When: sortKey=createdAt&sortDirection=DESC
    GET /api/v1/market/admin/categories?sortKey=createdAt&sortDirection=DESC

    // Then: 200 OK
    // - 최신 생성 항목이 먼저 나옴
    // - 내림차순 정렬 확인
}
```

**검증 포인트**:
- createdAt DESC 정렬 동작
- 최신 데이터 우선

---

#### TC-Q-033: 정렬 - sortKey=nameKo, sortDirection=ASC
**우선순위**: P1
**사전 데이터**: 한글명이 다른 카테고리 5건

```java
@Test
@DisplayName("정렬 - nameKo ASC")
void searchCategories_whenSortByNameKoAsc_thenSortsAscending() {
    // Given: 다양한 한글명
    categoryJpaRepository.saveAll(List.of(
        activeEntityWithName("가방", "Bags"),
        activeEntityWithName("의류", "Clothing"),
        activeEntityWithName("신발", "Shoes"),
        activeEntityWithName("디지털", "Digital"),
        activeEntityWithName("가구", "Furniture")
    ));

    // When: sortKey=nameKo&sortDirection=ASC
    GET /api/v1/market/admin/categories?sortKey=nameKo&sortDirection=ASC

    // Then: 200 OK
    // - 한글 오름차순 정렬
    // - "가구" → "가방" → "디지털" → "신발" → "의류"
}
```

**검증 포인트**:
- nameKo ASC 정렬 동작
- 한글 정렬 확인

---

#### TC-Q-034: 정렬 - sortKey=code, sortDirection=DESC
**우선순위**: P1
**사전 데이터**: 코드가 다른 카테고리 5건

```java
@Test
@DisplayName("정렬 - code DESC")
void searchCategories_whenSortByCodeDesc_thenSortsDescending() {
    // Given: 다양한 code
    categoryJpaRepository.saveAll(List.of(
        activeEntityWithCode("CAT_001"),
        activeEntityWithCode("CAT_002"),
        activeEntityWithCode("CAT_003"),
        activeEntityWithCode("CAT_004"),
        activeEntityWithCode("CAT_005")
    ));

    // When: sortKey=code&sortDirection=DESC
    GET /api/v1/market/admin/categories?sortKey=code&sortDirection=DESC

    // Then: 200 OK
    // - code 내림차순 정렬
    // - "CAT_005" → "CAT_004" → "CAT_003" → ...
}
```

**검증 포인트**:
- code DESC 정렬 동작
- 문자열 내림차순 확인

---

### 11. 복합 필터 시나리오 (P1)

#### TC-Q-035: 복합 필터 - parentId + statuses
**우선순위**: P1
**사전 데이터**: 부모 1건 + 자식 3건 (ACTIVE 2건, INACTIVE 1건)

```java
@Test
@DisplayName("복합 필터 - parentId + statuses")
void searchCategories_whenFilterByParentIdAndStatuses_thenReturnsMatching() {
    // Given: 부모 1건, 자식 3건
    categoryJpaRepository.saveAll(List.of(
        activeRootEntity(1L),
        activeChildEntity(1L),   // ACTIVE
        activeChildEntity(1L),   // ACTIVE
        inactiveChildEntity(1L)  // INACTIVE
    ));

    // When: parentId=1&statuses=ACTIVE
    GET /api/v1/market/admin/categories?parentId=1&statuses=ACTIVE

    // Then: 200 OK
    // - data.content.size = 2
    // - parentId=1 AND status=ACTIVE
}
```

**검증 포인트**:
- 복합 필터 AND 조건 동작
- parentId + statuses 조합

---

#### TC-Q-036: 복합 필터 - depth + departments
**우선순위**: P1
**사전 데이터**: depth 1, 2 각 3건, 다양한 부문

```java
@Test
@DisplayName("복합 필터 - depth + departments")
void searchCategories_whenFilterByDepthAndDepartments_thenReturnsMatching() {
    // Given: depth + departments 조합
    categoryJpaRepository.saveAll(List.of(
        entityWith(1, "FASHION"),
        entityWith(1, "BEAUTY"),
        entityWith(1, "DIGITAL"),
        entityWith(2, "FASHION"),
        entityWith(2, "BEAUTY"),
        entityWith(2, "DIGITAL")
    ));

    // When: depth=1&departments=FASHION,BEAUTY
    GET /api/v1/market/admin/categories?depth=1&departments=FASHION,BEAUTY

    // Then: 200 OK
    // - data.content.size = 2
    // - depth=1 AND department IN (FASHION, BEAUTY)
}
```

**검증 포인트**:
- depth + departments 필터 조합
- AND 조건 동작

---

#### TC-Q-037: 복합 필터 - leaf + categoryGroups
**우선순위**: P1
**사전 데이터**: leaf/non-leaf 각 3건, 다양한 카테고리 그룹

```java
@Test
@DisplayName("복합 필터 - leaf + categoryGroups")
void searchCategories_whenFilterByLeafAndCategoryGroups_thenReturnsMatching() {
    // Given: leaf + categoryGroups 조합
    categoryJpaRepository.saveAll(List.of(
        entityWith(true, "CLOTHING"),
        entityWith(true, "SHOES"),
        entityWith(true, "DIGITAL"),
        entityWith(false, "CLOTHING"),
        entityWith(false, "SHOES"),
        entityWith(false, "DIGITAL")
    ));

    // When: leaf=true&categoryGroups=CLOTHING,SHOES
    GET /api/v1/market/admin/categories?leaf=true&categoryGroups=CLOTHING,SHOES

    // Then: 200 OK
    // - data.content.size = 2
    // - leaf=true AND categoryGroup IN (CLOTHING, SHOES)
}
```

**검증 포인트**:
- leaf + categoryGroups 필터 조합
- 상품 등록 가능 + 고시정보 필터

---

#### TC-Q-038: 복합 필터 - statuses + departments + categoryGroups
**우선순위**: P1
**사전 데이터**: 다양한 조합 10건

```java
@Test
@DisplayName("복합 필터 - statuses + departments + categoryGroups")
void searchCategories_whenFilterByMultipleConditions_thenReturnsMatching() {
    // Given: 다양한 조합
    categoryJpaRepository.saveAll(List.of(
        entityWith("ACTIVE", "FASHION", "CLOTHING"),
        entityWith("ACTIVE", "FASHION", "SHOES"),
        entityWith("ACTIVE", "BEAUTY", "COSMETICS"),
        entityWith("INACTIVE", "FASHION", "CLOTHING"),
        entityWith("INACTIVE", "BEAUTY", "COSMETICS"),
        entityWith("ACTIVE", "DIGITAL", "DIGITAL"),
        // ...
    ));

    // When: statuses=ACTIVE&departments=FASHION&categoryGroups=CLOTHING
    GET /api/v1/market/admin/categories?statuses=ACTIVE&departments=FASHION&categoryGroups=CLOTHING

    // Then: 200 OK
    // - data.content.size = 1
    // - 모든 조건 만족하는 항목만
}
```

**검증 포인트**:
- 3개 필터 복합 조건
- AND 조건 정확성

---

#### TC-Q-039: 복합 필터 - 검색 + 필터 조합
**우선순위**: P1
**사전 데이터**: 다양한 카테고리 10건

```java
@Test
@DisplayName("복합 필터 - 검색 + 필터 조합")
void searchCategories_whenSearchWithFilters_thenReturnsMatching() {
    // Given: 다양한 카테고리
    categoryJpaRepository.saveAll(List.of(
        entityWith("남성 의류", "ACTIVE", "FASHION", "CLOTHING"),
        entityWith("여성 의류", "ACTIVE", "FASHION", "CLOTHING"),
        entityWith("아동 의류", "INACTIVE", "FASHION", "CLOTHING"),
        entityWith("신발", "ACTIVE", "FASHION", "SHOES"),
        // ...
    ));

    // When: searchWord=의류&statuses=ACTIVE&departments=FASHION
    GET /api/v1/market/admin/categories?searchWord=의류&statuses=ACTIVE&departments=FASHION

    // Then: 200 OK
    // - data.content.size = 2
    // - "의류" 포함 AND ACTIVE AND FASHION
}
```

**검증 포인트**:
- 검색 + 필터 조합
- LIKE + AND 조건 동작

---

#### TC-Q-040: 복합 필터 - 모든 필터 조합
**우선순위**: P1
**사전 데이터**: 복잡한 조합 15건

```java
@Test
@DisplayName("복합 필터 - 모든 필터 조합 (엣지 케이스)")
void searchCategories_whenAllFiltersApplied_thenReturnsMatching() {
    // Given: 다양한 조합
    categoryJpaRepository.saveAll(/* 15건 */);

    // When: 모든 필터 적용
    GET /api/v1/market/admin/categories
        ?parentId=1
        &depth=2
        &leaf=true
        &statuses=ACTIVE
        &departments=FASHION
        &categoryGroups=CLOTHING
        &searchWord=테스트
        &sortKey=sortOrder
        &sortDirection=ASC
        &page=0
        &size=5

    // Then: 200 OK
    // - 모든 조건 만족하는 결과만
}
```

**검증 포인트**:
- 모든 필터 동시 적용
- 복잡한 쿼리 정확성

---

## Fixture 설계

### 필요 Repository

```java
@Autowired
private CategoryJpaRepository categoryJpaRepository;
```

### testFixtures 활용

```java
// adapter-out/persistence-mysql/src/testFixtures/
import static com.ryuqq.marketplace.adapter.out.persistence.category.CategoryJpaEntityFixtures.*;

// 기본 Fixtures
- activeRootEntity()           // 활성 루트 카테고리
- activeRootEntity(Long id)    // ID 지정 루트 카테고리
- activeChildEntity(Long parentId)  // 자식 카테고리
- inactiveEntity()             // 비활성 카테고리
- deletedEntity()              // 삭제된 카테고리

// 커스텀 Fixtures
- activeEntityWithCode(String code)
- activeEntityWithName(String nameKo, String nameEn)
- entityWithDepartment(String department)
- entityWithCategoryGroup(String categoryGroup)
- nonLeafEntity()              // 비리프 노드
- depth2Entity(Long parentId)
- depth3Entity(Long parentId)
```

### setUp 패턴

```java
@BeforeEach
void setUp() {
    // 각 테스트마다 DB 초기화
    categoryJpaRepository.deleteAll();
}
```

### 사전 데이터 설정 예시

```java
// 1. 기본 조회 테스트용
List<CategoryJpaEntity> entities = List.of(
    activeRootEntity(1L),
    activeRootEntity(2L),
    activeRootEntity(3L),
    activeRootEntity(4L),
    activeRootEntity(5L)
);
categoryJpaRepository.saveAll(entities);

// 2. 계층 구조 테스트용
CategoryJpaEntity root = categoryJpaRepository.save(activeRootEntity(1L));
CategoryJpaEntity child1 = categoryJpaRepository.save(depth2Entity(root.getId()));
CategoryJpaEntity child2 = categoryJpaRepository.save(depth2Entity(root.getId()));
CategoryJpaEntity grandChild = categoryJpaRepository.save(depth3Entity(child1.getId()));

// 3. 다양한 상태/부문/그룹 테스트용
categoryJpaRepository.saveAll(List.of(
    entityWith("ACTIVE", "FASHION", "CLOTHING"),
    entityWith("ACTIVE", "BEAUTY", "COSMETICS"),
    entityWith("INACTIVE", "DIGITAL", "DIGITAL"),
    entityWith("ACTIVE", "FASHION", "SHOES"),
    entityWith("INACTIVE", "FASHION", "CLOTHING")
));
```

---

## 실행 가이드

### 테스트 클래스 구조

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Category E2E 통합 테스트")
class CategoryE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        categoryJpaRepository.deleteAll();
    }

    // TC-Q-001 ~ TC-Q-040
}
```

### 실행 방법

```bash
# 전체 E2E 테스트 실행
./gradlew :integration-test:test --tests "*CategoryE2ETest"

# 특정 시나리오만 실행
./gradlew :integration-test:test --tests "*CategoryE2ETest.searchCategories_whenDataExists_thenReturns200"

# P0 시나리오만 실행 (태그 기반)
./gradlew :integration-test:test --tests "*CategoryE2ETest" --tests "*P0*"
```

### 검증 패턴

```java
// 1. HTTP 상태 코드 검증
mockMvc.perform(get("/api/v1/market/admin/categories"))
       .andExpect(status().isOk());

// 2. Response Body 검증
mockMvc.perform(get("/api/v1/market/admin/categories"))
       .andExpect(jsonPath("$.success").value(true))
       .andExpect(jsonPath("$.data.content.size()").value(5))
       .andExpect(jsonPath("$.data.totalElements").value(5));

// 3. DB 상태 검증
List<CategoryJpaEntity> result = categoryJpaRepository.findAll();
assertThat(result).hasSize(5);
```

---

## 우선순위별 실행 계획

### Phase 1: P0 시나리오 (필수 - 18개)
- **목표**: 핵심 기능 검증
- **실행 시간**: 약 10분
- **시나리오**: TC-Q-001 ~ TC-Q-030

### Phase 2: P1 시나리오 (중요 - 12개)
- **목표**: 복합 조건 및 엣지 케이스 검증
- **실행 시간**: 약 8분
- **시나리오**: TC-Q-031 ~ TC-Q-040

### 전체 실행
- **총 시나리오**: 30개
- **예상 실행 시간**: 약 18분
- **커버리지 목표**: 95% 이상

---

## 참고사항

### 고시정보 연결 (categoryGroup)
- **12개 그룹**: CLOTHING, SHOES, BAGS, ACCESSORIES, COSMETICS, JEWELRY, WATCHES, FURNITURE, DIGITAL, SPORTS, BABY_KIDS, ETC
- **연결 구조**: `category` → `category_group` → `category_attribute_template` → `category_attribute_spec`

### Soft Delete
- `deleted_at IS NULL` 조건으로 삭제된 카테고리 제외
- 모든 조회 쿼리에 자동 적용

### 기본값
- Page: 0
- Size: 20
- SortKey: `SORT_ORDER`
- SortDirection: `DESC`

### 성능 고려사항
- N+1 문제 없음 (단일 쿼리)
- 페이징: Offset 기반 (소규모 데이터 적합)
- 인덱스 활용: `code`, `parent_id`, `depth`, `status`, `department`, `category_group`

---

**문서 버전**: 1.0
**최종 업데이트**: 2026-02-06
