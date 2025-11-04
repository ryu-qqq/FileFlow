# REST API Integration Test 가이드

> **통합 테스트는 REST API Adapter와 실제 Infrastructure를 함께 테스트합니다.**
> Testcontainers를 사용하여 실제 데이터베이스와 통합 검증합니다.

## 목차
1. [Integration Test 소개](#1-integration-test-소개)
2. [프로젝트 설정](#2-프로젝트-설정)
3. [AbstractIntegrationTest 베이스 클래스](#3-abstractintegrationtest-베이스-클래스)
4. [MockMvc vs REST Assured](#4-mockmvc-vs-rest-assured)
5. [Controller Integration Test 패턴](#5-controller-integration-test-패턴)
6. [데이터베이스 상태 관리](#6-데이터베이스-상태-관리)
7. [Test Fixtures](#7-test-fixtures)
8. [Best Practices](#8-best-practices)

---

## 1. Integration Test 소개

### 1.1 Integration Test란?

**Integration Test**는 여러 컴포넌트를 함께 테스트하여 **실제 시스템 동작**을 검증합니다.

**검증 범위**:
```
REST API Controller
    ↓
Application Layer (UseCase)
    ↓
Domain Layer (Aggregate)
    ↓
Persistence Layer (Repository)
    ↓
실제 Database (Testcontainers)
```

### 1.2 Unit Test vs Integration Test

| 항목 | Unit Test | Integration Test |
|------|-----------|------------------|
| **범위** | 단일 클래스 | 여러 Layer 통합 |
| **속도** | 빠름 (ms) | 느림 (초) |
| **의존성** | Mock/Stub | 실제 Infrastructure |
| **목적** | 로직 검증 | 통합 동작 검증 |
| **실행 시점** | 매 커밋 | PR/빌드 시 |

**예시**:
```java
// ❌ Unit Test (Mock 사용, 빠름)
@Test
void createOrder_ShouldCallUseCase() {
    // Given
    PlaceOrderUseCase mockUseCase = mock(PlaceOrderUseCase.class);
    OrderController controller = new OrderController(mockUseCase);

    // When
    controller.createOrder(request);

    // Then
    verify(mockUseCase).execute(any());  // UseCase 호출 여부만 검증
}

// ✅ Integration Test (실제 DB, 느림)
@Test
void createOrder_ShouldPersistToDatabase() {
    // Given
    CreateOrderApiRequest request = new CreateOrderApiRequest(productId, 10);

    // When
    ResponseEntity<ApiResponse<OrderApiResponse>> response =
        restTemplate.postForEntity("/api/v1/orders", request, ...);

    // Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    OrderJpaEntity saved = orderRepository.findById(1L).orElseThrow();
    assertEquals(10, saved.getQuantity());  // 실제 DB에 저장됨을 검증
}
```

### 1.3 언제 Integration Test를 작성하는가?

✅ **Integration Test가 필요한 경우**:
- **Happy Path 검증**: 전체 흐름이 정상 동작하는지 확인
- **데이터베이스 통합**: JPA Entity, Query 동작 검증
- **Transaction 경계**: @Transactional 동작 검증
- **Validation**: Request DTO Validation 동작 검증
- **API Contract**: HTTP 요청/응답 포맷 검증
- **페이지네이션**: Cursor/Offset 기반 Pagination 검증

❌ **Integration Test가 불필요한 경우**:
- **단순 로직**: 계산, 변환 등 (Unit Test로 충분)
- **Edge Case**: 예외 상황 (Unit Test가 더 빠름)
- **Private 메서드**: 내부 구현 (Unit Test 또는 리팩토링)

---

## 2. 프로젝트 설정

### 2.1 의존성 추가

**`build.gradle.kts` (bootstrap-web-api 모듈)**:
```kotlin
dependencies {
    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // Testcontainers (실제 DB 컨테이너)
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")

    // REST Assured (HTTP 테스트)
    testImplementation("io.rest-assured:rest-assured:5.4.0")
}
```

**의존성 설명**:
- **spring-boot-starter-test**: MockMvc, JUnit 5, AssertJ, Mockito 포함
- **spring-security-test**: Security 테스트 유틸리티
- **testcontainers-postgresql**: Docker 기반 PostgreSQL 컨테이너
- **testcontainers-junit-jupiter**: JUnit 5 통합
- **rest-assured**: HTTP API 테스트 DSL (선택사항)

### 2.2 테스트 패키지 구조

```
bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/
├── architecture/                    # ArchUnit 테스트 (기존)
│   ├── RestApiLayerRulesTest.java
│   └── RestApiAdapterConventionTest.java
├── integration/                     # Integration Test (신규)
│   ├── AbstractIntegrationTest.java  # 베이스 클래스
│   └── rest/
│       └── example/
│           ├── ExampleControllerIntegrationTest.java  # Controller 통합 테스트
│           └── ExampleSearchIntegrationTest.java       # 검색/페이징 통합 테스트
└── fixtures/                        # Test Fixtures (신규)
    └── ExampleTestFixtures.java     # 테스트 데이터 생성
```

---

## 3. AbstractIntegrationTest 베이스 클래스

### 3.1 베이스 클래스 목적

**AbstractIntegrationTest**는 모든 Integration Test의 부모 클래스입니다.

**제공 기능**:
- 🐳 **Testcontainers 설정**: 실제 PostgreSQL 컨테이너 실행
- 🌱 **Spring Context 로딩**: @SpringBootTest로 전체 애플리케이션 실행
- 🔧 **공통 설정**: DB 초기화, 포트 설정 등
- 🧪 **테스트 유틸리티**: REST Assured, TestRestTemplate 등

### 3.2 AbstractIntegrationTest 구현

**파일**: `bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/integration/AbstractIntegrationTest.java`

```java
package com.ryuqq.bootstrap.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration Test 베이스 클래스
 *
 * <p>모든 REST API Integration Test의 부모 클래스입니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>Testcontainers PostgreSQL 컨테이너 실행</li>
 *   <li>Spring Boot Context 로딩 (@SpringBootTest)</li>
 *   <li>REST Assured 설정 (baseURI, port 자동 설정)</li>
 *   <li>랜덤 포트 (충돌 방지)</li>
 * </ul>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * class ExampleControllerIntegrationTest extends AbstractIntegrationTest {
 *
 *     @Test
 *     void createExample_ShouldReturnCreated() {
 *         given()
 *             .contentType(ContentType.JSON)
 *             .body(request)
 *         .when()
 *             .post("/api/v1/examples")
 *         .then()
 *             .statusCode(201);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Testcontainers 라이프사이클:</strong></p>
 * <ul>
 *   <li>전체 테스트 클래스 시작 전: PostgreSQL 컨테이너 1회 실행</li>
 *   <li>모든 테스트 완료 후: 컨테이너 자동 종료</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-31
 * @see <a href="https://testcontainers.com/">Testcontainers 공식 문서</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL Testcontainer
     *
     * <p>실제 PostgreSQL 데이터베이스를 Docker 컨테이너로 실행합니다.</p>
     * <p>{@code @Container}: JUnit 5 라이프사이클에 통합 (자동 시작/종료)</p>
     * <p>{@code static}: 전체 테스트 클래스에서 1개 컨테이너 공유 (성능 최적화)</p>
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);  // 컨테이너 재사용 (빠른 테스트)

    @LocalServerPort
    private int port;

    /**
     * Testcontainer 데이터베이스 연결 정보를 Spring에 주입
     *
     * <p>Testcontainer가 실행되면 랜덤 포트로 PostgreSQL이 시작됩니다.</p>
     * <p>이 메서드는 해당 포트와 연결 정보를 Spring Boot의 DataSource 설정에 주입합니다.</p>
     *
     * @param registry Spring Dynamic Property Registry
     */
    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * REST Assured 설정 (각 테스트 전 실행)
     *
     * <p>REST Assured의 baseURI와 port를 설정합니다.</p>
     * <p>테스트 코드에서 {@code given().when().get("/api/v1/examples")}만 작성하면 됩니다.</p>
     */
    @BeforeEach
    void setUpRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}
```

### 3.3 베이스 클래스 사용법

```java
/**
 * ExampleController Integration Test
 */
class ExampleControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Example 생성 - 정상 케이스")
    void createExample_ShouldReturnCreated() {
        // AbstractIntegrationTest가 제공:
        // 1. PostgreSQL 컨테이너 실행
        // 2. Spring Boot 전체 Context 로딩
        // 3. REST Assured 설정 (baseURI, port)

        // Given
        ExampleApiRequest request = new ExampleApiRequest("Hello World");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.message", equalTo("Hello World"));
    }
}
```

---

## 4. MockMvc vs REST Assured

### 4.1 비교표

| 항목 | MockMvc | REST Assured |
|------|---------|--------------|
| **실행 방식** | Servlet Container 없음 | 실제 HTTP 요청 |
| **속도** | 빠름 | 약간 느림 |
| **가독성** | 낮음 (Verbose) | 높음 (BDD DSL) |
| **Spring 통합** | 완벽 | 별도 설정 필요 |
| **사용 추천** | Unit Test 수준 | Integration Test |

### 4.2 MockMvc 예시

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExampleControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createExample_ShouldReturnCreated() throws Exception {
        // Given
        String requestBody = """
            {
                "message": "Hello World"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/examples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").value("Hello World"))
            .andDo(print());
    }
}
```

### 4.3 REST Assured 예시 (권장)

```java
class ExampleControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createExample_ShouldReturnCreated() {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Hello World");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.message", equalTo("Hello World"));
    }
}
```

**REST Assured의 장점**:
- ✅ **BDD 스타일**: Given-When-Then (가독성 높음)
- ✅ **간결한 구문**: JsonPath, Hamcrest Matcher 내장
- ✅ **실제 HTTP**: 진짜 HTTP 요청/응답 테스트
- ✅ **API 문서화**: API Contract 명확히 표현

---

## 5. Controller Integration Test 패턴

### 5.1 POST (Command 생성)

**테스트 목적**: Example 생성 API가 정상 동작하는지 검증

```java
package com.ryuqq.bootstrap.integration.rest.example;

import com.ryuqq.adapter.in.rest.example.dto.request.ExampleApiRequest;
import com.ryuqq.adapter.out.persistence.example.ExampleJpaEntity;
import com.ryuqq.adapter.out.persistence.example.ExampleJpaRepository;
import com.ryuqq.bootstrap.integration.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ExampleController Integration Test (Command 생성)
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@DisplayName("ExampleController Integration Test - POST (생성)")
class ExampleControllerCreateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExampleJpaRepository exampleRepository;

    @Test
    @DisplayName("Example 생성 - 정상 케이스")
    @DirtiesContext  // 테스트 후 Spring Context 초기화 (DB 상태 리셋)
    void createExample_ShouldReturnCreatedAndPersistToDatabase() {
        // Given: Example 생성 요청 DTO
        ExampleApiRequest request = new ExampleApiRequest("Hello Integration Test");

        // When: POST /api/v1/examples
        var response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201)  // ✅ HTTP 201 Created
            .body("success", equalTo(true))
            .body("data.message", equalTo("Hello Integration Test"))
            .extract()
            .path("data.id");

        // Then: 데이터베이스 검증
        Long savedId = ((Number) response).longValue();
        ExampleJpaEntity saved = exampleRepository.findById(savedId).orElseThrow();

        assertEquals("Hello Integration Test", saved.getMessage());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Example 생성 - Validation 실패 (message가 blank)")
    void createExample_ShouldReturn400_WhenMessageIsBlank() {
        // Given: 유효하지 않은 요청 (message가 blank)
        ExampleApiRequest invalidRequest = new ExampleApiRequest("");

        // When & Then: POST /api/v1/examples
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(400)  // ✅ HTTP 400 Bad Request
            .body("success", equalTo(false))
            .body("error.title", containsString("Validation Failed"));
    }

    @Test
    @DisplayName("Example 생성 - Validation 실패 (message가 null)")
    void createExample_ShouldReturn400_WhenMessageIsNull() {
        // Given: 유효하지 않은 요청 (message가 null)
        String requestJson = """
            {
                "message": null
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestJson)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(400)
            .body("success", equalTo(false));
    }
}
```

### 5.2 GET (Query 조회)

**테스트 목적**: Example 단건 조회 API 검증

```java
package com.ryuqq.bootstrap.integration.rest.example;

import com.ryuqq.adapter.out.persistence.example.ExampleJpaEntity;
import com.ryuqq.adapter.out.persistence.example.ExampleJpaRepository;
import com.ryuqq.bootstrap.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * ExampleController Integration Test (Query 조회)
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@DisplayName("ExampleController Integration Test - GET (조회)")
class ExampleControllerGetIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExampleJpaRepository exampleRepository;

    @Test
    @DisplayName("Example 단건 조회 - 정상 케이스")
    @DirtiesContext
    void getExample_ShouldReturnExampleDetail() {
        // Given: 테스트 데이터 저장
        ExampleJpaEntity saved = exampleRepository.save(
            ExampleJpaEntity.builder()
                .message("Test Example")
                .status("ACTIVE")
                .build()
        );

        // When & Then: GET /api/v1/examples/{id}
        given()
        .when()
            .get("/api/v1/examples/{id}", saved.getId())
        .then()
            .statusCode(200)  // ✅ HTTP 200 OK
            .body("success", equalTo(true))
            .body("data.id", equalTo(saved.getId().intValue()))
            .body("data.message", equalTo("Test Example"))
            .body("data.status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Example 단건 조회 - 존재하지 않는 ID")
    void getExample_ShouldReturn404_WhenNotFound() {
        // Given: 존재하지 않는 ID
        Long nonExistentId = 99999L;

        // When & Then: GET /api/v1/examples/99999
        given()
        .when()
            .get("/api/v1/examples/{id}", nonExistentId)
        .then()
            .statusCode(404)  // ✅ HTTP 404 Not Found
            .body("success", equalTo(false))
            .body("error.title", equalTo("Example Not Found"));
    }

    @Test
    @DisplayName("Example 단건 조회 - 유효하지 않은 ID (음수)")
    void getExample_ShouldReturn400_WhenIdIsNegative() {
        // Given: 음수 ID (Validation 실패)
        Long invalidId = -1L;

        // When & Then
        given()
        .when()
            .get("/api/v1/examples/{id}", invalidId)
        .then()
            .statusCode(400)  // ✅ HTTP 400 Bad Request
            .body("success", equalTo(false));
    }
}
```

### 5.3 Pagination (Cursor 기반)

**테스트 목적**: Cursor 기반 무한 스크롤 검증

```java
package com.ryuqq.bootstrap.integration.rest.example;

import com.ryuqq.adapter.out.persistence.example.ExampleJpaEntity;
import com.ryuqq.adapter.out.persistence.example.ExampleJpaRepository;
import com.ryuqq.bootstrap.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ExampleController Integration Test (Cursor 기반 Pagination)
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@DisplayName("ExampleController Integration Test - Cursor Pagination")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ExampleControllerCursorPaginationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExampleJpaRepository exampleRepository;

    @BeforeEach
    void setUpTestData() {
        // Given: 테스트 데이터 30개 저장
        List<ExampleJpaEntity> examples = IntStream.range(1, 31)
            .mapToObj(i -> ExampleJpaEntity.builder()
                .message("Example " + i)
                .status("ACTIVE")
                .build())
            .toList();

        exampleRepository.saveAll(examples);
    }

    @Test
    @DisplayName("Cursor 기반 검색 - 첫 페이지")
    void searchExamplesByCursor_ShouldReturnFirstPage() {
        // When & Then: GET /api/v1/examples?size=10
        given()
            .queryParam("size", 10)
        .when()
            .get("/api/v1/examples")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.content", hasSize(10))  // 10개 반환
            .body("data.size", equalTo(10))
            .body("data.hasNext", equalTo(true))  // 다음 페이지 존재
            .body("data.nextCursor", notNullValue());  // nextCursor 있음
    }

    @Test
    @DisplayName("Cursor 기반 검색 - 다음 페이지")
    void searchExamplesByCursor_ShouldReturnNextPage() {
        // Given: 첫 페이지 조회하여 nextCursor 얻기
        String nextCursor = given()
            .queryParam("size", 10)
        .when()
            .get("/api/v1/examples")
        .then()
            .extract()
            .path("data.nextCursor");

        // When & Then: GET /api/v1/examples?cursor={nextCursor}&size=10
        given()
            .queryParam("cursor", nextCursor)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/examples")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.content", hasSize(10))  // 10개 반환
            .body("data.hasNext", equalTo(true));  // 아직 다음 페이지 존재
    }

    @Test
    @DisplayName("Cursor 기반 검색 - 마지막 페이지")
    void searchExamplesByCursor_ShouldReturnLastPage() {
        // When & Then: size=50 (30개만 있으므로 마지막 페이지)
        given()
            .queryParam("size", 50)
        .when()
            .get("/api/v1/examples")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.content", hasSize(30))  // 30개 반환
            .body("data.hasNext", equalTo(false))  // 다음 페이지 없음
            .body("data.nextCursor", nullValue());  // nextCursor null
    }
}
```

### 5.4 Pagination (Offset 기반)

**테스트 목적**: Offset 기반 관리자 페이지 검증

```java
@Test
@DisplayName("Offset 기반 검색 - 페이지 번호와 전체 개수")
void searchExamplesByPage_ShouldReturnPageWithTotalCount() {
    // When & Then: GET /api/v1/admin/examples/search?page=0&size=10
    given()
        .queryParam("page", 0)
        .queryParam("size", 10)
    .when()
        .get("/api/v1/admin/examples/search")
    .then()
        .statusCode(200)
        .body("success", equalTo(true))
        .body("data.content", hasSize(10))
        .body("data.page", equalTo(0))
        .body("data.size", equalTo(10))
        .body("data.totalElements", equalTo(30))  // 전체 개수
        .body("data.totalPages", equalTo(3))  // 전체 페이지 수
        .body("data.first", equalTo(true))  // 첫 페이지
        .body("data.last", equalTo(false));  // 마지막 페이지 아님
}
```

### 5.5 DomainException 처리

**테스트 목적**: Domain 예외가 HTTP 응답으로 올바르게 변환되는지 검증

```java
@Test
@DisplayName("Example 조회 - DomainException (NOT_FOUND)")
void getExample_ShouldReturn404_WhenDomainExceptionThrown() {
    // Given: 존재하지 않는 ID (DomainException 발생)
    Long nonExistentId = 99999L;

    // When & Then: GlobalExceptionHandler가 DomainException을 HTTP 404로 변환
    given()
    .when()
        .get("/api/v1/examples/{id}", nonExistentId)
    .then()
        .statusCode(404)  // ✅ DomainException → HTTP 404
        .body("success", equalTo(false))
        .body("error.code", equalTo("EXAMPLE.NOT_FOUND"))
        .body("error.title", equalTo("Example Not Found"))
        .body("error.detail", containsString("Example with ID"));
}
```

---

## 6. 데이터베이스 상태 관리

### 6.1 테스트 격리 전략

**문제**: Integration Test는 실제 DB를 사용하므로 **테스트 간 상태 공유** 문제 발생

**해결 방법**:
1. ✅ **@DirtiesContext**: 테스트마다 Spring Context 초기화 (느림)
2. ✅ **@Transactional + @Rollback**: 트랜잭션 롤백 (빠름, 권장)
3. ✅ **@BeforeEach에서 수동 삭제**: Repository.deleteAll() (유연함)

### 6.2 @DirtiesContext (Spring Context 초기화)

**사용 시점**: 테스트가 Spring Context를 변경하는 경우

```java
@Test
@DisplayName("Example 생성 - Spring Context 초기화")
@DirtiesContext  // 테스트 후 Context 재생성 (DB 리셋)
void createExample_WithDirtiesContext() {
    // Given
    ExampleApiRequest request = new ExampleApiRequest("Test");

    // When
    given()
        .contentType(ContentType.JSON)
        .body(request)
    .when()
        .post("/api/v1/examples")
    .then()
        .statusCode(201);

    // Then: 이 테스트 후 DB가 리셋됨
}
```

**장점**: 완벽한 격리
**단점**: 느림 (Context 재생성 비용)

### 6.3 @Transactional + @Rollback (권장)

**사용 시점**: 대부분의 테스트 (빠르고 안전)

```java
@SpringBootTest
@Transactional  // 테스트 메서드를 트랜잭션으로 감싸기
class ExampleControllerTransactionalTest extends AbstractIntegrationTest {

    @Autowired
    private ExampleJpaRepository exampleRepository;

    @Test
    @Rollback  // 테스트 후 자동 롤백 (기본값 true)
    @DisplayName("Example 생성 - 자동 롤백")
    void createExample_ShouldRollback() {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Test");

        // When
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201);

        // Then: 트랜잭션이 롤백되어 DB에 저장되지 않음
        assertEquals(0, exampleRepository.count());  // ✅ 롤백됨
    }
}
```

**장점**: 빠름, 자동 롤백
**단점**: Async/Multi-Thread 테스트에는 부적합

### 6.4 수동 삭제 (@BeforeEach)

**사용 시점**: 유연한 데이터 관리가 필요한 경우

```java
class ExampleControllerManualCleanupTest extends AbstractIntegrationTest {

    @Autowired
    private ExampleJpaRepository exampleRepository;

    @BeforeEach
    void cleanUp() {
        // 각 테스트 전 수동 삭제
        exampleRepository.deleteAll();
    }

    @Test
    void createExample_ShouldPersist() {
        // Given
        ExampleApiRequest request = new ExampleApiRequest("Test");

        // When
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201);

        // Then
        assertEquals(1, exampleRepository.count());  // ✅ 실제로 저장됨
    }
}
```

**장점**: 유연함, 명시적
**단점**: 수동 관리 필요

---

## 7. Test Fixtures

### 7.1 Test Fixture란?

**Test Fixture**는 테스트에 필요한 **데이터 생성 유틸리티**입니다.

**목적**:
- 🔧 **재사용성**: 중복 코드 제거
- 📦 **일관성**: 동일한 방식으로 테스트 데이터 생성
- 🧪 **가독성**: 테스트 의도 명확히 표현

### 7.2 ExampleTestFixtures 구현

**파일**: `bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/fixtures/ExampleTestFixtures.java`

```java
package com.ryuqq.bootstrap.fixtures;

import com.ryuqq.adapter.in.rest.example.dto.request.ExampleApiRequest;
import com.ryuqq.adapter.out.persistence.example.ExampleJpaEntity;

/**
 * Example 테스트 데이터 생성 유틸리티
 *
 * <p>Integration Test에서 사용할 Example 테스트 데이터를 생성합니다.</p>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * // Given
 * ExampleApiRequest request = ExampleTestFixtures.createExampleRequest();
 * ExampleJpaEntity entity = ExampleTestFixtures.createExampleEntity("Test");
 * }</pre>
 *
 * @author Claude Code
 * @since 2025-10-31
 */
public class ExampleTestFixtures {

    private ExampleTestFixtures() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 기본 ExampleApiRequest 생성
     *
     * @return ExampleApiRequest
     */
    public static ExampleApiRequest createExampleRequest() {
        return new ExampleApiRequest("Default Test Message");
    }

    /**
     * 커스텀 메시지로 ExampleApiRequest 생성
     *
     * @param message 메시지
     * @return ExampleApiRequest
     */
    public static ExampleApiRequest createExampleRequest(String message) {
        return new ExampleApiRequest(message);
    }

    /**
     * 기본 ExampleJpaEntity 생성 (ACTIVE 상태)
     *
     * @return ExampleJpaEntity
     */
    public static ExampleJpaEntity createExampleEntity() {
        return ExampleJpaEntity.builder()
            .message("Default Test Entity")
            .status("ACTIVE")
            .build();
    }

    /**
     * 커스텀 메시지로 ExampleJpaEntity 생성
     *
     * @param message 메시지
     * @return ExampleJpaEntity
     */
    public static ExampleJpaEntity createExampleEntity(String message) {
        return ExampleJpaEntity.builder()
            .message(message)
            .status("ACTIVE")
            .build();
    }

    /**
     * 커스텀 메시지와 상태로 ExampleJpaEntity 생성
     *
     * @param message 메시지
     * @param status 상태
     * @return ExampleJpaEntity
     */
    public static ExampleJpaEntity createExampleEntity(String message, String status) {
        return ExampleJpaEntity.builder()
            .message(message)
            .status(status)
            .build();
    }

    /**
     * 여러 ExampleJpaEntity를 생성 (일괄 생성)
     *
     * @param count 생성할 개수
     * @return List<ExampleJpaEntity>
     */
    public static List<ExampleJpaEntity> createExampleEntities(int count) {
        return IntStream.range(1, count + 1)
            .mapToObj(i -> createExampleEntity("Example " + i))
            .toList();
    }
}
```

### 7.3 Test Fixture 사용 예시

```java
class ExampleControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExampleJpaRepository exampleRepository;

    @Test
    @DisplayName("Example 생성 - Fixture 사용")
    void createExample_UsingFixture() {
        // Given: Fixture로 요청 데이터 생성
        ExampleApiRequest request = ExampleTestFixtures.createExampleRequest("Test");

        // When
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/examples")
        .then()
            .statusCode(201);
    }

    @Test
    @DisplayName("Example 검색 - Fixture로 테스트 데이터 생성")
    void searchExamples_UsingFixtures() {
        // Given: Fixture로 30개 테스트 데이터 생성
        List<ExampleJpaEntity> entities = ExampleTestFixtures.createExampleEntities(30);
        exampleRepository.saveAll(entities);

        // When & Then
        given()
            .queryParam("size", 10)
        .when()
            .get("/api/v1/examples")
        .then()
            .statusCode(200)
            .body("data.content", hasSize(10));
    }
}
```

---

## 8. Best Practices

### 8.1 테스트 네이밍 규칙

**패턴**: `{메서드명}_{예상결과}_{조건}`

```java
✅ createExample_ShouldReturnCreated_WhenValidRequest()
✅ getExample_ShouldReturn404_WhenNotFound()
✅ searchExamples_ShouldReturnFirstPage_WhenCursorIsNull()

❌ testCreateExample()
❌ test1()
❌ shouldWork()
```

### 8.2 Given-When-Then 패턴

**구조**:
- **Given**: 테스트 전제 조건 (데이터 준비)
- **When**: 테스트 실행 (API 호출)
- **Then**: 결과 검증 (Assertion)

```java
@Test
void createExample_ShouldReturnCreated() {
    // Given: 요청 데이터 준비
    ExampleApiRequest request = new ExampleApiRequest("Test");

    // When: API 호출
    var response = given()
        .contentType(ContentType.JSON)
        .body(request)
    .when()
        .post("/api/v1/examples");

    // Then: 결과 검증
    response.then()
        .statusCode(201)
        .body("success", equalTo(true));
}
```

### 8.3 테스트 범위 최소화

**원칙**: **한 테스트는 하나의 시나리오만 검증**

```java
// ❌ 나쁜 예: 여러 시나리오를 한 테스트에서 검증
@Test
void createExample_MultipleScenarios() {
    // Scenario 1: 정상 케이스
    given().body(validRequest).when().post("/api/v1/examples").then().statusCode(201);

    // Scenario 2: Validation 실패
    given().body(invalidRequest).when().post("/api/v1/examples").then().statusCode(400);

    // Scenario 3: Duplicate
    given().body(duplicateRequest).when().post("/api/v1/examples").then().statusCode(409);
}

// ✅ 좋은 예: 각 시나리오를 별도 테스트로 분리
@Test
void createExample_ShouldReturnCreated_WhenValidRequest() {
    given().body(validRequest).when().post("/api/v1/examples").then().statusCode(201);
}

@Test
void createExample_ShouldReturn400_WhenInvalidRequest() {
    given().body(invalidRequest).when().post("/api/v1/examples").then().statusCode(400);
}

@Test
void createExample_ShouldReturn409_WhenDuplicate() {
    given().body(duplicateRequest).when().post("/api/v1/examples").then().statusCode(409);
}
```

### 8.4 테스트 독립성 보장

**원칙**: **테스트 실행 순서에 무관하게 동작**

```java
// ❌ 나쁜 예: 테스트 순서 의존
@Test
void test1_CreateExample() {
    given().body(request).when().post("/api/v1/examples").then().statusCode(201);
}

@Test
void test2_GetExample() {
    // test1_CreateExample()가 먼저 실행되어야 동작함 (❌)
    given().when().get("/api/v1/examples/1").then().statusCode(200);
}

// ✅ 좋은 예: 각 테스트가 독립적
@Test
void createExample_ShouldReturnCreated() {
    given().body(request).when().post("/api/v1/examples").then().statusCode(201);
}

@Test
void getExample_ShouldReturnDetail() {
    // Given: 이 테스트만을 위한 데이터 준비
    ExampleJpaEntity saved = exampleRepository.save(ExampleTestFixtures.createExampleEntity());

    // When & Then
    given().when().get("/api/v1/examples/{id}", saved.getId()).then().statusCode(200);
}
```

### 8.5 Testcontainers 재사용

**최적화**: 컨테이너를 재사용하여 테스트 속도 향상

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withReuse(true);  // ✅ 컨테이너 재사용 (빠른 테스트)
```

**효과**:
- 첫 테스트: 컨테이너 시작 (5-10초)
- 이후 테스트: 기존 컨테이너 재사용 (0.1초)

### 8.6 병렬 테스트 실행

**설정**: `gradle.properties`
```properties
# 병렬 테스트 실행 (CPU 코어 수만큼)
org.gradle.parallel=true
org.gradle.workers.max=4
```

**결과**: 4개 테스트를 동시 실행 → **4배 빠름**

### 8.7 Integration Test vs Unit Test 비율

**권장 비율**: **70% Unit Test + 30% Integration Test**

**이유**:
- Unit Test: 빠르고 Edge Case 검증에 유리
- Integration Test: 느리지만 실제 동작 검증에 필수

**예시**:
```
프로젝트 테스트 구성:
- Unit Test: 700개 (70%) - 실행 시간 30초
- Integration Test: 300개 (30%) - 실행 시간 90초
- 총 실행 시간: 2분
```

---

## 요약

### Integration Test 체크리스트

#### 프로젝트 설정
- [ ] `spring-boot-starter-test` 의존성 추가
- [ ] `testcontainers-postgresql` 의존성 추가
- [ ] `rest-assured` 의존성 추가 (선택)
- [ ] `AbstractIntegrationTest` 베이스 클래스 작성

#### 테스트 작성
- [ ] Given-When-Then 패턴 사용
- [ ] 한 테스트는 하나의 시나리오만 검증
- [ ] @DisplayName으로 명확한 테스트 이름 작성
- [ ] Test Fixture로 테스트 데이터 재사용

#### 데이터 관리
- [ ] @DirtiesContext 또는 @Transactional로 테스트 격리
- [ ] @BeforeEach에서 데이터 초기화 (필요 시)
- [ ] 테스트 독립성 보장 (실행 순서 무관)

#### 검증 범위
- [ ] HTTP 상태 코드 검증 (200, 201, 400, 404 등)
- [ ] Response Body 검증 (ApiResponse<T> 구조)
- [ ] 데이터베이스 상태 검증 (실제 저장 확인)
- [ ] Pagination 검증 (hasNext, nextCursor 등)
- [ ] Validation 실패 케이스 검증
- [ ] DomainException 처리 검증

#### 최적화
- [ ] Testcontainers 재사용 (withReuse(true))
- [ ] 병렬 테스트 실행 (gradle.properties)
- [ ] 적절한 테스트 비율 (70% Unit + 30% Integration)

---

## 참고 문서

### REST API Layer 컨벤션
- [ArchUnit 테스트 가이드](./01_archunit-test-guide.md) - 아키텍처 자동 검증
- [Controller 디자인](../controller-design/) - Controller 설계 가이드
- [DTO 패턴](../dto-patterns/) - Request/Response DTO 가이드

### 외부 링크
- [Testcontainers 공식 문서](https://testcontainers.com/)
- [REST Assured 공식 문서](https://rest-assured.io/)
- [Spring Boot Test 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

**✅ 이 가이드를 따르면 견고한 REST API Integration Test를 작성할 수 있습니다!**
