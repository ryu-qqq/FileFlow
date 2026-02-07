# Brand E2E Integration Test - 생성 리포트

## 개요

Brand 도메인의 REST API E2E 통합 테스트 코드 생성 완료.

| 항목 | 내용 |
|------|------|
| 생성일 | 2026-02-06 |
| 테스트 파일 | `BrandQueryE2ETest.java` |
| 위치 | `integration-test/src/test/java/com/ryuqq/marketplace/integration/brand/` |
| 테스트 대상 | GET /api/v1/market/admin/brands |
| 총 라인 수 | 653 lines |

---

## 생성된 테스트 구조

### 1. 테스트 클래스 구성

```java
@Tag("e2e")
@Tag("brand")
@Tag("query")
@DisplayName("Brand Query API E2E 테스트")
class BrandQueryE2ETest extends E2ETestBase
```

**Base Class 상속**: `E2ETestBase`
- RestAssured 기반 HTTP 요청/응답 검증
- `givenAdmin()` 헬퍼 메서드 제공
- H2 In-Memory Database 사용 (test 프로파일)

**Repository 주입**:
- `BrandJpaRepository`: 사전 데이터 저장 및 DB 검증

**LifeCycle**:
- `@BeforeEach setUp()`: 모든 브랜드 삭제 (DB 초기화)
- `@AfterEach tearDown()`: 테스트 후 정리

---

### 2. @Nested 그룹 구조

#### 📦 SearchBrandsByOffsetTest (14개 테스트)
**GET /admin/brands - 브랜드 목록 조회**

| # | 시나리오 | Priority | 검증 항목 |
|---|---------|---------|-----------|
| Q1.1 | 데이터 존재 시 정상 조회 | P0 | 기본 페이징, Soft Delete 필터 |
| Q1.2 | 데이터 없을 때 빈 목록 반환 | P0 | 빈 결과 처리 |
| Q1.3 | 페이징 동작 확인 | P0 | page, size, totalElements |
| Q1.4 | 상태 필터 - ACTIVE | P0 | status IN (ACTIVE) |
| Q1.5 | 상태 필터 - INACTIVE | P0 | status IN (INACTIVE) |
| Q1.6 | 상태 필터 - 다중 상태 | P0 | status IN (ACTIVE, INACTIVE) |
| Q1.7 | searchWord만 (전체 필드 검색) | P0 | nameKo, nameEn, code LIKE |
| Q1.8 | searchField + searchWord | P0 | 특정 필드만 검색 |
| Q1.9 | 정렬 - createdAt DESC | P1 | 생성일시 내림차순 |
| Q1.10 | 정렬 - nameKo ASC | P1 | 한글명 오름차순 |
| Q1.11 | 정렬 - updatedAt DESC | P1 | 수정일시 내림차순 |
| Q1.12 | 복합 필터 | P1 | 상태+검색+정렬+페이징 조합 |
| Q1.13 | 대량 데이터 조회 | P1 | 1000건 조회 |
| Q1.14 | 마지막 페이지 조회 | P1 | 부분 페이지 처리 |

#### 📦 FullFlowTest (1개 테스트)
**전체 플로우 시나리오**

| # | 시나리오 | Priority | 검증 항목 |
|---|---------|---------|-----------|
| F1 | 목록 조회 → Response 검증 플로우 | P0 | 6단계 전체 플로우 검증 |

**플로우 단계**:
1. 사전 데이터 저장 (3건)
2. 목록 조회 (GET /admin/brands)
3. Response 구조 검증 (ApiResponse, PageApiResponse)
4. BrandApiResponse 필드 검증 (id, code, nameKo, nameEn, status, createdAt, updatedAt)
5. 날짜 포맷 검증 (ISO-8601)
6. DB 일관성 검증 (Response ID로 재조회)

---

### 3. Helper 메서드

| 메서드 | 용도 |
|--------|------|
| `saveBrands(count, codePrefix, status)` | 지정 개수만큼 브랜드 일괄 저장 |
| `createBrand(code, nameKo, nameEn, shortName)` | 기본 브랜드 Entity 생성 (ACTIVE) |
| `createBrand(code, nameKo, nameEn, shortName, status)` | 상태 지정 브랜드 Entity 생성 |
| `createBrandWithTime(code, nameKo, nameEn, shortName, createdAt)` | createdAt 지정 Entity 생성 |
| `createBrandWithUpdatedAt(code, nameKo, nameEn, shortName, updatedAt)` | updatedAt 지정 Entity 생성 |

---

## 코드 검증 항목

### ✅ REST Assured 패턴

```java
// 기본 조회
given()
    .spec(givenAdmin())
    .when()
    .get(BASE_URL)
    .then()
    .statusCode(HttpStatus.OK.value())
    .body("success", equalTo(true))
    .body("data.content.size()", equalTo(8));

// Query Parameter
given()
    .spec(givenAdmin())
    .queryParam("statuses", "ACTIVE")
    .queryParam("page", 0)
    .queryParam("size", 10)
    .when()
    .get(BASE_URL)
    .then()
    .statusCode(HttpStatus.OK.value());

// Response 추출 및 검증
var response = given()
    .spec(givenAdmin())
    .when()
    .get(BASE_URL)
    .then()
    .statusCode(HttpStatus.OK.value())
    .extract()
    .response();

assertThat(response.jsonPath().getList("data.content")).hasSize(3);
```

### ✅ Hamcrest Matchers

```java
.body("data.content", empty())                    // 빈 리스트
.body("data.content.size()", equalTo(10))         // 크기 검증
.body("data.content[0].status", equalTo("ACTIVE")) // 필드 값 검증
.body("data.createdAt", notNullValue())           // Null 검증
.body("data.createdAt", containsString("2025-01-03")) // 문자열 포함
```

### ✅ AssertJ Assertions

```java
assertThat(brandRepository.count()).isEqualTo(3);
assertThat(response.jsonPath().getList("data.content")).hasSize(3);
assertThat(dbBrand).isPresent();
assertThat(createdAt).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z");
```

---

## 시나리오 커버리지

### Query 엔드포인트 검증

| 검증 항목 | 개수 | 상태 |
|----------|------|------|
| 기본 조회 (데이터 있음/없음) | 2개 | ✅ |
| 페이징 동작 | 3개 | ✅ |
| 상태 필터 (ACTIVE, INACTIVE, 다중) | 3개 | ✅ |
| 검색 필터 (전체 필드, 특정 필드) | 2개 | ✅ |
| 정렬 동작 (createdAt, nameKo, updatedAt) | 3개 | ✅ |
| 복합 필터 조합 | 1개 | ✅ |
| 엣지 케이스 (대량 데이터, 마지막 페이지) | 2개 | ✅ |

### DB 연동 검증

| 검증 항목 | 상태 |
|----------|------|
| Soft Delete 적용 확인 (deleted_at IS NULL 조건) | ✅ |
| 페이징 쿼리 동작 확인 (LIMIT, OFFSET) | ✅ |
| 정렬 쿼리 동작 확인 (ORDER BY) | ✅ |
| 동적 쿼리 조건 확인 (WHERE 절 동적 생성) | ✅ |

### Response 검증

| 검증 항목 | 상태 |
|----------|------|
| ApiResponse 구조 검증 | ✅ |
| PageApiResponse 구조 검증 | ✅ |
| BrandApiResponse 필드 검증 | ✅ |
| 날짜 포맷 검증 (ISO-8601) | ✅ |
| Null 필드 처리 확인 (logoUrl, shortName) | ✅ |

---

## 테스트 데이터 패턴

### 1. 사전 데이터 생성 예시

```java
// ACTIVE 5건 + INACTIVE 3건 + DELETED 2건
saveBrands(5, "ACTIVE", "ACTIVE");
saveBrands(3, "INACTIVE", "INACTIVE");
saveBrands(2, "DELETED", "ACTIVE"); // 삭제된 브랜드
```

### 2. 특정 브랜드 생성 예시

```java
brandRepository.save(createBrand("NIKE001", "나이키", "Nike", "NK"));
brandRepository.save(createBrand("ADIDAS001", "아디다스", "Adidas", "AD"));
brandRepository.save(createBrand("PUMA001", "퓨마", "Puma", "PM"));
```

### 3. 시간 지정 브랜드 생성 예시

```java
Instant time1 = Instant.parse("2025-01-01T00:00:00Z");
Instant time2 = Instant.parse("2025-01-02T00:00:00Z");
Instant time3 = Instant.parse("2025-01-03T00:00:00Z");

brandRepository.save(createBrandWithTime("BRAND1", "브랜드1", "Brand1", "B1", time1));
brandRepository.save(createBrandWithTime("BRAND2", "브랜드2", "Brand2", "B2", time2));
brandRepository.save(createBrandWithTime("BRAND3", "브랜드3", "Brand3", "B3", time3));
```

---

## testFixtures 활용

### BrandJpaEntityFixtures 사용 예시

현재 테스트에서는 직접 Entity 생성 메서드를 사용하고 있지만, 향후 다음과 같이 Fixtures를 활용할 수 있습니다:

```java
// 기존 Fixtures 활용
var brand1 = brandRepository.save(BrandJpaEntityFixtures.activeEntity());
var brand2 = brandRepository.save(BrandJpaEntityFixtures.inactiveEntity());
var brand3 = brandRepository.save(BrandJpaEntityFixtures.deletedEntity());

// 커스텀 코드로 Fixtures 활용
var brand = brandRepository.save(BrandJpaEntityFixtures.activeEntityWithCode("NIKE001"));
var brand = brandRepository.save(BrandJpaEntityFixtures.inactiveEntityWithCode("ADIDAS001"));
```

**권장사항**: 테스트 시나리오에서 특정 필드 값(code, name)이 중요한 경우 직접 생성 메서드를 사용하고, 일반적인 ACTIVE/INACTIVE 상태만 필요한 경우 Fixtures를 활용하세요.

---

## 실행 방법

### 전체 E2E 테스트 실행

```bash
./gradlew :integration-test:test --tests "BrandQueryE2ETest"
```

### 특정 시나리오 실행

```bash
# 페이징 테스트만 실행
./gradlew :integration-test:test --tests "BrandQueryE2ETest.SearchBrandsByOffsetTest.searchBrands_paging_worksCorrectly"

# 전체 플로우 테스트만 실행
./gradlew :integration-test:test --tests "BrandQueryE2ETest.FullFlowTest.fullFlow_listAndVerifyResponse"
```

### 태그 기반 실행

```bash
# P0 시나리오만 실행
./gradlew :integration-test:test --tests "BrandQueryE2ETest" --tests "*P0*"

# P1 시나리오만 실행
./gradlew :integration-test:test --tests "BrandQueryE2ETest" --tests "*P1*"
```

---

## 예상 테스트 결과

### 성공 시나리오

```
BrandQueryE2ETest > SearchBrandsByOffsetTest
  ✅ [Q1.1] 데이터 존재 시 정상 조회 (기본 페이징)
  ✅ [Q1.2] 데이터 없을 때 빈 목록 반환
  ✅ [Q1.3] 페이징 동작 확인 (page, size)
  ✅ [Q1.4] 상태 필터 - ACTIVE만 조회
  ✅ [Q1.5] 상태 필터 - INACTIVE만 조회
  ✅ [Q1.6] 상태 필터 - 다중 상태 (ACTIVE, INACTIVE)
  ✅ [Q1.7] 검색 - searchWord만 (전체 필드 검색)
  ✅ [Q1.8] 검색 - searchField + searchWord (특정 필드 검색)
  ✅ [Q1.9] 정렬 - createdAt DESC (기본 정렬)
  ✅ [Q1.10] 정렬 - nameKo ASC (가나다순)
  ✅ [Q1.11] 정렬 - updatedAt DESC (최근 수정순)
  ✅ [Q1.12] 복합 필터 - 상태 + 검색 + 정렬 + 페이징
  ✅ [Q1.13] 엣지 케이스 - 대량 데이터 조회
  ✅ [Q1.14] 엣지 케이스 - 마지막 페이지 조회

BrandQueryE2ETest > FullFlowTest
  ✅ [F1] 목록 조회 → Response 검증 플로우

총 15개 시나리오 PASSED
```

---

## 추가 개선 사항

### 1. 인증 구현 후 활성화

현재 `givenAdmin()`은 인증 헤더가 없지만, JWT 기반 인증 구현 후 다음과 같이 활성화:

```java
protected RequestSpecification givenAdmin() {
    return new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addHeader("Authorization", "Bearer {admin-token}") // 활성화
            .build();
}
```

### 2. 성능 테스트 추가

대량 데이터 조회 시나리오(Q1.13)에 응답 시간 검증 추가:

```java
@Test
@DisplayName("[Q1.13] 엣지 케이스 - 대량 데이터 조회")
void searchBrands_largeDataset() {
    // given: 1000건 저장
    saveBrands(1000, "BRAND", "ACTIVE");

    // when & then
    long startTime = System.currentTimeMillis();

    given()
        .spec(givenAdmin())
        .queryParam("page", 0)
        .queryParam("size", 100)
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("data.content.size()", equalTo(100))
        .body("data.totalElements", equalTo(1000));

    long endTime = System.currentTimeMillis();
    assertThat(endTime - startTime).isLessThan(500); // 500ms 이내
}
```

### 3. 예외 시나리오 추가

향후 Validation 예외 시나리오 추가 가능:

```java
@Test
@DisplayName("잘못된 page 값 (page=-1)")
void searchBrands_invalidPageNegative_Returns400() {
    given()
        .spec(givenAdmin())
        .queryParam("page", -1)
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
}
```

---

## 테스트 품질 체크리스트

### ✅ 구조

- [x] E2ETestBase 상속
- [x] @Nested 그룹 구조 사용
- [x] @Tag 적용 (e2e, brand, query)
- [x] @DisplayName 명확한 시나리오 설명
- [x] @BeforeEach / @AfterEach 데이터 정리

### ✅ 검증

- [x] HTTP Status 검증
- [x] Response Body 검증 (Hamcrest Matchers)
- [x] 페이징 정보 검증
- [x] 날짜 포맷 검증 (ISO-8601)
- [x] DB 일관성 검증 (Repository 재조회)

### ✅ 데이터

- [x] testFixtures 참조 가능
- [x] Helper 메서드 활용
- [x] Soft Delete 고려
- [x] 시간 제어 가능 (createdAt, updatedAt)

### ✅ 가독성

- [x] 명확한 given-when-then 구조
- [x] 의미 있는 변수명
- [x] 적절한 주석
- [x] 일관된 코드 스타일

---

## 참고 문서

- `.claude/docs/test-scenario/brand.md` - 시나리오 설계 문서
- `.claude/docs/api-endpoints/brand.md` - API 엔드포인트 명세
- `.claude/docs/api-flow/brand.md` - API 호출 흐름 분석
- `BrandJpaEntityFixtures.java` - testFixtures 클래스

---

## 문서 정보

- **생성일**: 2026-02-06
- **작성자**: Claude Code (ryu-qqq)
- **테스트 파일**: `BrandQueryE2ETest.java`
- **시나리오 개수**: 15개 (P0: 9개, P1: 6개)
- **총 라인 수**: 653 lines
- **테스트 방법**: REST Assured + Hamcrest + AssertJ
