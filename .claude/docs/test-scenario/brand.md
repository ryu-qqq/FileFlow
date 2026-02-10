# Brand E2E Integration Test Scenario

## 개요

Brand 도메인의 REST API E2E 통합 테스트 시나리오 설계 문서입니다.

| 항목 | 내용 |
|------|------|
| 대상 엔드포인트 | GET /api/v1/market/admin/brands |
| 테스트 타입 | E2E Integration Test |
| 테스트 범위 | Controller → Service → Repository → Database |
| 작성일 | 2026-02-06 |

---

## 입력 분석

### API Endpoints 분석

| 분류 | 개수 | 엔드포인트 |
|------|------|-----------|
| Query | 1개 | GET /api/v1/market/admin/brands |
| Command | 0개 | - |

### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | Validation |
|---------|------|------|------|-----------|
| statuses | List\<String\> | ❌ | 상태 필터 (ACTIVE, INACTIVE) | - |
| searchField | String | ❌ | 검색 필드 (code, nameKo, nameEn) | - |
| searchWord | String | ❌ | 검색어 | - |
| sortKey | String | ❌ | 정렬 키 (createdAt, nameKo, updatedAt) | - |
| sortDirection | String | ❌ | 정렬 방향 (ASC, DESC) | - |
| page | Integer | ❌ | 페이지 번호 (0부터 시작, 기본값: 0) | - |
| size | Integer | ❌ | 페이지 크기 (기본값: 20) | - |

### Domain 분석

**Brand Aggregate**:
- `BrandId`: 식별자 VO
- `BrandCode`: 브랜드 코드 (UNIQUE)
- `BrandName`: 이름 VO (nameKo, nameEn, shortName)
- `BrandStatus`: ACTIVE, INACTIVE
- `LogoUrl`: 로고 URL (nullable)
- `DeletionStatus`: Soft Delete 지원
- `createdAt`, `updatedAt`: 감사 정보 (Instant)

**비즈니스 규칙**:
1. Soft Delete 적용 (deleted_at IS NULL 조건)
2. 기본 상태는 ACTIVE
3. 브랜드 코드는 중복 불가
4. 삭제된 브랜드는 조회 결과에서 제외

---

## 시나리오 설계

### 1. Query 시나리오: searchBrandsByOffset (14개)

#### P0: 필수 시나리오 (8개)

##### Q1.1 데이터 존재 시 정상 조회 (기본 페이징)
**Priority**: P0
**Category**: 기본 조회

**사전 데이터**:
- ACTIVE 브랜드 5건
- INACTIVE 브랜드 3건
- Deleted 브랜드 2건 (조회 제외 대상)

**Request**:
```
GET /api/v1/market/admin/brands
```

**Expected**:
- HTTP Status: 200 OK
- Response Body:
  - `success`: true
  - `data.content`: List (8개, ACTIVE 5 + INACTIVE 3)
  - `data.page`: 0
  - `data.size`: 20 (기본값)
  - `data.totalElements`: 8
- 삭제된 브랜드는 결과에 미포함

---

##### Q1.2 데이터 없을 때 빈 목록 반환
**Priority**: P0
**Category**: 빈 결과

**사전 데이터**: 없음 (DB cleanup)

**Request**:
```
GET /api/v1/market/admin/brands
```

**Expected**:
- HTTP Status: 200 OK
- Response Body:
  - `success`: true
  - `data.content`: [] (빈 리스트)
  - `data.page`: 0
  - `data.size`: 20
  - `data.totalElements`: 0

---

##### Q1.3 페이징 동작 확인 (page, size)
**Priority**: P0
**Category**: 페이징

**사전 데이터**: 브랜드 25건 저장

**Request**:
```
GET /api/v1/market/admin/brands?page=0&size=10
GET /api/v1/market/admin/brands?page=1&size=10
GET /api/v1/market/admin/brands?page=2&size=10
```

**Expected (page=0)**:
- HTTP Status: 200 OK
- `data.content.size()`: 10
- `data.page`: 0
- `data.size`: 10
- `data.totalElements`: 25

**Expected (page=1)**:
- `data.content.size()`: 10
- `data.page`: 1
- `data.totalElements`: 25

**Expected (page=2)**:
- `data.content.size()`: 5
- `data.page`: 2
- `data.totalElements`: 25

---

##### Q1.4 상태 필터 - ACTIVE만 조회
**Priority**: P0
**Category**: 상태 필터

**사전 데이터**:
- ACTIVE 브랜드 7건
- INACTIVE 브랜드 3건

**Request**:
```
GET /api/v1/market/admin/brands?statuses=ACTIVE
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 7
- `data.totalElements`: 7
- 모든 결과의 `status`: "ACTIVE"

---

##### Q1.5 상태 필터 - INACTIVE만 조회
**Priority**: P0
**Category**: 상태 필터

**사전 데이터**:
- ACTIVE 브랜드 7건
- INACTIVE 브랜드 3건

**Request**:
```
GET /api/v1/market/admin/brands?statuses=INACTIVE
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 3
- `data.totalElements`: 3
- 모든 결과의 `status`: "INACTIVE"

---

##### Q1.6 상태 필터 - 다중 상태 (ACTIVE, INACTIVE)
**Priority**: P0
**Category**: 상태 필터

**사전 데이터**:
- ACTIVE 브랜드 7건
- INACTIVE 브랜드 3건

**Request**:
```
GET /api/v1/market/admin/brands?statuses=ACTIVE&statuses=INACTIVE
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 10
- `data.totalElements`: 10
- 결과에 ACTIVE와 INACTIVE 모두 포함

---

##### Q1.7 검색 - searchWord만 (전체 필드 검색)
**Priority**: P0
**Category**: 검색 필터

**사전 데이터**:
- Brand("NIKE001", "나이키", "Nike", "NK")
- Brand("ADIDAS001", "아디다스", "Adidas", "AD")
- Brand("PUMA001", "퓨마", "Puma", "PM")

**Request**:
```
GET /api/v1/market/admin/brands?searchWord=나이키
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].nameKo`: "나이키"
- `data.totalElements`: 1

**Request (영문명 검색)**:
```
GET /api/v1/market/admin/brands?searchWord=Nike
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].nameEn`: "Nike"

**Request (코드 검색)**:
```
GET /api/v1/market/admin/brands?searchWord=NIKE001
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].code`: "NIKE001"

---

##### Q1.8 검색 - searchField + searchWord (특정 필드 검색)
**Priority**: P0
**Category**: 검색 필터

**사전 데이터**:
- Brand("NIKE001", "나이키", "Nike", "NK")
- Brand("NIKELAB", "나이키랩", "NikeLab", "NL")

**Request (nameKo 필드만)**:
```
GET /api/v1/market/admin/brands?searchField=nameKo&searchWord=나이키
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 2 (나이키, 나이키랩)
- `data.totalElements`: 2

**Request (code 필드만)**:
```
GET /api/v1/market/admin/brands?searchField=code&searchWord=NIKE
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 2 (NIKE001, NIKELAB)

**Request (nameEn 필드만)**:
```
GET /api/v1/market/admin/brands?searchField=nameEn&searchWord=Nike
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 2

---

#### P1: 중요 시나리오 (6개)

##### Q1.9 정렬 - createdAt DESC (기본 정렬)
**Priority**: P1
**Category**: 정렬

**사전 데이터**:
- Brand1 (createdAt: 2025-01-01T00:00:00Z)
- Brand2 (createdAt: 2025-01-02T00:00:00Z)
- Brand3 (createdAt: 2025-01-03T00:00:00Z)

**Request**:
```
GET /api/v1/market/admin/brands?sortKey=createdAt&sortDirection=DESC
```

**Expected**:
- HTTP Status: 200 OK
- `data.content[0].createdAt`: "2025-01-03T00:00:00Z" (최신)
- `data.content[1].createdAt`: "2025-01-02T00:00:00Z"
- `data.content[2].createdAt`: "2025-01-01T00:00:00Z" (가장 오래됨)

---

##### Q1.10 정렬 - nameKo ASC (가나다순)
**Priority**: P1
**Category**: 정렬

**사전 데이터**:
- Brand("CODE1", "퓨마", "Puma", "PM")
- Brand("CODE2", "나이키", "Nike", "NK")
- Brand("CODE3", "아디다스", "Adidas", "AD")

**Request**:
```
GET /api/v1/market/admin/brands?sortKey=nameKo&sortDirection=ASC
```

**Expected**:
- HTTP Status: 200 OK
- `data.content[0].nameKo`: "나이키"
- `data.content[1].nameKo`: "아디다스"
- `data.content[2].nameKo`: "퓨마"

---

##### Q1.11 정렬 - updatedAt DESC (최근 수정순)
**Priority**: P1
**Category**: 정렬

**사전 데이터**:
- Brand1 (updatedAt: 2025-01-01T00:00:00Z)
- Brand2 (updatedAt: 2025-01-03T00:00:00Z)
- Brand3 (updatedAt: 2025-01-02T00:00:00Z)

**Request**:
```
GET /api/v1/market/admin/brands?sortKey=updatedAt&sortDirection=DESC
```

**Expected**:
- HTTP Status: 200 OK
- `data.content[0].updatedAt`: "2025-01-03T00:00:00Z"
- `data.content[1].updatedAt`: "2025-01-02T00:00:00Z"
- `data.content[2].updatedAt`: "2025-01-01T00:00:00Z"

---

##### Q1.12 복합 필터 - 상태 + 검색 + 정렬 + 페이징
**Priority**: P1
**Category**: 복합 필터

**사전 데이터**:
- ACTIVE + "나이키" 포함 브랜드 15건
- ACTIVE + "아디다스" 포함 브랜드 10건
- INACTIVE + "나이키" 포함 브랜드 5건

**Request**:
```
GET /api/v1/market/admin/brands?statuses=ACTIVE&searchWord=나이키&sortKey=nameKo&sortDirection=ASC&page=0&size=10
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 10 (첫 페이지)
- `data.totalElements`: 15
- 모든 결과: status="ACTIVE" && nameKo.contains("나이키")
- 정렬: nameKo 오름차순

---

##### Q1.13 엣지 케이스 - 대량 데이터 조회
**Priority**: P1
**Category**: 성능

**사전 데이터**: 브랜드 1000건

**Request**:
```
GET /api/v1/market/admin/brands?page=0&size=100
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 100
- `data.totalElements`: 1000
- 응답 시간: < 500ms (성능 검증)

---

##### Q1.14 엣지 케이스 - 마지막 페이지 조회
**Priority**: P1
**Category**: 페이징

**사전 데이터**: 브랜드 23건

**Request**:
```
GET /api/v1/market/admin/brands?page=2&size=10
```

**Expected**:
- HTTP Status: 200 OK
- `data.content.size()`: 3 (23 - 20)
- `data.page`: 2
- `data.totalElements`: 23

---

### 2. 전체 플로우 시나리오 (1개)

#### F1. 목록 조회 → Response 검증 플로우
**Priority**: P0
**Category**: 전체 플로우

**Steps**:
1. **사전 데이터 저장**:
   - ACTIVE 브랜드 3건 저장 (DB insert)
   - Repository.save() 확인

2. **목록 조회**:
   - GET /api/v1/market/admin/brands

3. **Response 검증**:
   - HTTP Status: 200 OK
   - ApiResponse 구조 확인
   - PageApiResponse 구조 확인
   - BrandApiResponse 필드 확인 (id, code, nameKo, nameEn, status, createdAt, updatedAt)

4. **날짜 포맷 검증**:
   - `createdAt`: ISO-8601 형식 (yyyy-MM-dd'T'HH:mm:ss'Z')
   - `updatedAt`: ISO-8601 형식

5. **DB 일관성 검증**:
   - Response의 ID로 Repository.findById() 재조회
   - DB 값과 Response 값 일치 확인

---

## Fixture 설계

### 필요 Repository

| Repository | 용도 |
|-----------|------|
| BrandJpaRepository | 사전 데이터 저장 (save), 검증 (findById) |

### testFixtures

| Fixture 클래스 | 위치 | 메서드 |
|---------------|------|-------|
| BrandJpaEntityFixtures | adapter-out/persistence-mysql/src/testFixtures | activeEntity(), inactiveEntity(), deletedEntity() |

### 사전 데이터 설정 방법

#### @BeforeEach setUp()

```java
@BeforeEach
void setUp() {
    // 1. DB 초기화 (모든 브랜드 삭제)
    brandJpaRepository.deleteAll();

    // 2. 시나리오별 필요 데이터는 각 테스트 메서드에서 삽입
}
```

#### 시나리오별 데이터 예시

**기본 조회 시나리오**:
```java
// ACTIVE 브랜드 5건
for (int i = 1; i <= 5; i++) {
    BrandJpaEntity entity = BrandJpaEntityFixtures.activeEntityWithCode("ACTIVE" + i);
    brandJpaRepository.save(entity);
}

// INACTIVE 브랜드 3건
for (int i = 1; i <= 3; i++) {
    BrandJpaEntity entity = BrandJpaEntityFixtures.inactiveEntityWithCode("INACTIVE" + i);
    brandJpaRepository.save(entity);
}

// DELETED 브랜드 2건 (조회 제외 대상)
for (int i = 1; i <= 2; i++) {
    BrandJpaEntity entity = BrandJpaEntityFixtures.deletedEntityWithCode("DELETED" + i);
    brandJpaRepository.save(entity);
}
```

**검색 시나리오**:
```java
brandJpaRepository.save(
    BrandJpaEntityFixtures.activeEntityWithName("나이키", "Nike", "NK")
);
brandJpaRepository.save(
    BrandJpaEntityFixtures.activeEntityWithName("아디다스", "Adidas", "AD")
);
```

---

## 테스트 구조

### 테스트 클래스 구조

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BrandQueryE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @BeforeEach
    void setUp() {
        brandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Q1.1 데이터 존재 시 정상 조회 (기본 페이징)")
    void searchBrands_withData_returnsDefaultPage() throws Exception {
        // given: 사전 데이터 저장

        // when: GET /api/v1/market/admin/brands

        // then: HTTP 200, content.size = 8, totalElements = 8
    }

    // ... 나머지 테스트 메서드
}
```

### MockMvc 호출 패턴

```java
mockMvc.perform(
    get("/api/v1/market/admin/brands")
        .param("statuses", "ACTIVE")
        .param("searchWord", "나이키")
        .param("page", "0")
        .param("size", "10")
        .contentType(MediaType.APPLICATION_JSON)
)
.andExpect(status().isOk())
.andExpect(jsonPath("$.success").value(true))
.andExpect(jsonPath("$.data.content.size()").value(10))
.andExpect(jsonPath("$.data.totalElements").value(15));
```

---

## 검증 체크리스트

### Query 엔드포인트 검증

- [x] 정상 조회 (데이터 있을 때)
- [x] 빈 결과 (데이터 없을 때)
- [x] 페이징 동작 확인 (page, size)
- [x] 상태 필터 각각 동작 확인 (ACTIVE, INACTIVE, 다중)
- [x] 검색 필터 동작 확인 (전체 필드, 특정 필드)
- [x] 정렬 동작 확인 (createdAt, nameKo, updatedAt)
- [x] 복합 필터 조합 (상태 + 검색 + 정렬 + 페이징)
- [x] 엣지 케이스 (대량 데이터, 마지막 페이지)

### DB 연동 검증

- [x] Soft Delete 적용 확인 (deleted_at IS NULL 조건)
- [x] 페이징 쿼리 동작 확인 (LIMIT, OFFSET)
- [x] 정렬 쿼리 동작 확인 (ORDER BY)
- [x] 동적 쿼리 조건 확인 (WHERE 절 동적 생성)

### Response 검증

- [x] ApiResponse 구조 검증
- [x] PageApiResponse 구조 검증
- [x] BrandApiResponse 필드 검증
- [x] 날짜 포맷 검증 (ISO-8601)
- [x] Null 필드 처리 확인 (logoUrl, shortName)

---

## 시나리오 요약

| 분류 | P0 | P1 | 합계 |
|------|----|----|------|
| Query 시나리오 | 8 | 6 | 14 |
| 전체 플로우 시나리오 | 1 | 0 | 1 |
| **총합** | **9** | **6** | **15** |

---

## 다음 단계

### 1. 테스트 코드 작성
```bash
# E2E 테스트 클래스 생성
touch adapter-in/rest-api/src/test/java/com/ryuqq/marketplace/adapter/in/rest/brand/BrandQueryE2ETest.java
```

### 2. 테스트 실행
```bash
./gradlew :adapter-in:rest-api:test --tests BrandQueryE2ETest
```

### 3. 커버리지 확인
- Query 엔드포인트: 100% (1개 중 1개)
- 시나리오: 15개 (P0: 9개, P1: 6개)

---

## 참고 사항

### 1. Spring Boot Test 설정
- `@SpringBootTest`: 전체 애플리케이션 컨텍스트 로드
- `webEnvironment = RANDOM_PORT`: 랜덤 포트 사용
- `@AutoConfigureMockMvc`: MockMvc 자동 설정

### 2. 트랜잭션 정책
- 각 테스트는 독립적으로 실행
- `@BeforeEach`에서 DB 초기화 (`deleteAll()`)
- 트랜잭션 롤백 없음 (실제 DB 상태 유지)

### 3. 테스트 데이터 관리
- testFixtures 활용하여 재사용 가능한 Fixture 생성
- 코드 중복 최소화
- 가독성 향상

### 4. 성능 고려사항
- 대량 데이터 테스트 시 시간 제한 설정
- 페이징 성능 검증 (100개 이상)
- 검색 성능 검증 (LIKE 쿼리)

---

## 문서 정보

- **작성일**: 2026-02-06
- **대상 도메인**: Brand
- **엔드포인트 개수**: 1개 (Query)
- **시나리오 개수**: 15개 (P0: 9, P1: 6)
- **참고 문서**:
  - `.claude/docs/api-endpoints/brand.md`
  - `.claude/docs/api-flow/brand.md`
  - `BrandJpaEntityFixtures.java`
