---
description: E2E (End-to-End) 테스트 자동 생성 (RestAssured, Scenario-based)
---

# E2E (End-to-End) 테스트 자동 생성

**목적**: REST API의 E2E 시나리오 테스트 자동 생성 (RestAssured 기반)

**타겟**: REST API Layer - End-to-End Scenario Tests

**생성 테스트**: User Journey, Multi-step Scenarios, Error Handling, Security

---

## 🎯 사용법

```bash
# 주문 E2E 테스트 생성
/test-gen-e2e OrderApi

# 결제 E2E 테스트 생성
/test-gen-e2e PaymentApi
```

---

## ✅ 자동 생성되는 테스트 케이스

### 1. RestAssured 기본 설정

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Order API E2E 테스트")
class OrderApiE2ETest {

    @LocalServerPort
    private int port;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }
}
```

### 2. Happy Path 시나리오 (성공 케이스)

```java
@Test
@DisplayName("주문 생성 → 조회 → 확인 → 취소 전체 플로우 성공")
void shouldCompleteOrderLifecycleSuccessfully() {
    // Step 1: 주문 생성
    String orderId = given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "customerId": 100,
                "items": [
                    {"productId": 1, "quantity": 2},
                    {"productId": 2, "quantity": 1}
                ]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
        .body("status", equalTo("PLACED"))
        .body("customerId", equalTo(100))
        .body("items", hasSize(2))
    .extract()
        .path("orderId");

    // Step 2: 주문 조회
    given()
        .pathParam("orderId", orderId)
    .when()
        .get("/orders/{orderId}")
    .then()
        .statusCode(200)
        .body("orderId", equalTo(orderId))
        .body("status", equalTo("PLACED"));

    // Step 3: 주문 확인
    given()
        .pathParam("orderId", orderId)
    .when()
        .post("/orders/{orderId}/confirm")
    .then()
        .statusCode(200)
        .body("status", equalTo("CONFIRMED"));

    // Step 4: 주문 취소
    given()
        .pathParam("orderId", orderId)
        .contentType(ContentType.JSON)
        .body("""
            {
                "reason": "고객 요청"
            }
            """)
    .when()
        .post("/orders/{orderId}/cancel")
    .then()
        .statusCode(200)
        .body("status", equalTo("CANCELLED"))
        .body("cancelReason", equalTo("고객 요청"));
}
```

### 3. Multi-step 복잡 시나리오

```java
@Test
@DisplayName("여러 주문 생성 → 목록 조회 → 필터링 시나리오")
void shouldHandleMultipleOrdersWithFiltering() {
    // Step 1: 3개의 주문 생성
    List<String> orderIds = new ArrayList<>();
    for (int i = 1; i <= 3; i++) {
        String orderId = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "customerId": %d,
                    "items": [{"productId": 1, "quantity": %d}]
                }
                """, 100 + i, i))
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
        .extract()
            .path("orderId");
        orderIds.add(orderId);
    }

    // Step 2: 전체 주문 목록 조회
    given()
        .queryParam("page", 0)
        .queryParam("size", 10)
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .body("content", hasSize(3))
        .body("totalElements", equalTo(3));

    // Step 3: customerId로 필터링
    given()
        .queryParam("customerId", 101)
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .body("content", hasSize(1))
        .body("content[0].customerId", equalTo(101));

    // Step 4: 첫 번째 주문 확인
    given()
        .pathParam("orderId", orderIds.get(0))
    .when()
        .post("/orders/{orderId}/confirm")
    .then()
        .statusCode(200);

    // Step 5: status로 필터링 (CONFIRMED만)
    given()
        .queryParam("status", "CONFIRMED")
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .body("content", hasSize(1))
        .body("content[0].status", equalTo("CONFIRMED"));
}
```

### 4. Error Handling 시나리오

```java
@Test
@DisplayName("잘못된 요청 처리 시나리오")
void shouldHandleInvalidRequestsGracefully() {
    // Case 1: 필수 필드 누락
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "items": [{"productId": 1, "quantity": 2}]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(400)
        .body("error.code", equalTo("INVALID_REQUEST"))
        .body("error.message", containsString("customerId"))
        .body("error.field", equalTo("customerId"));

    // Case 2: 존재하지 않는 주문 조회
    given()
        .pathParam("orderId", "non-existent-id")
    .when()
        .get("/orders/{orderId}")
    .then()
        .statusCode(404)
        .body("error.code", equalTo("ORDER_NOT_FOUND"))
        .body("error.message", containsString("주문을 찾을 수 없습니다"));

    // Case 3: 이미 취소된 주문 재취소 시도
    String orderId = given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "customerId": 100,
                "items": [{"productId": 1, "quantity": 1}]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
    .extract()
        .path("orderId");

    // 첫 번째 취소 성공
    given()
        .pathParam("orderId", orderId)
        .contentType(ContentType.JSON)
        .body("""{"reason": "고객 요청"}""")
    .when()
        .post("/orders/{orderId}/cancel")
    .then()
        .statusCode(200);

    // 두 번째 취소 실패
    given()
        .pathParam("orderId", orderId)
        .contentType(ContentType.JSON)
        .body("""{"reason": "고객 요청"}""")
    .when()
        .post("/orders/{orderId}/cancel")
    .then()
        .statusCode(400)
        .body("error.code", equalTo("INVALID_ORDER_STATUS"))
        .body("error.message", containsString("이미 취소된 주문"));
}
```

### 5. Security 시나리오

```java
@Test
@DisplayName("인증 및 권한 검증 시나리오")
void shouldEnforceSecurityPolicies() {
    // Case 1: 인증 없이 요청 (401 Unauthorized)
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
                "customerId": 100,
                "items": [{"productId": 1, "quantity": 1}]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(401)
        .body("error.code", equalTo("UNAUTHORIZED"))
        .body("error.message", containsString("인증이 필요합니다"));

    // Case 2: 유효한 토큰으로 주문 생성
    String token = getAuthToken("user@example.com", "password");

    String orderId = given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body("""
            {
                "customerId": 100,
                "items": [{"productId": 1, "quantity": 1}]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
    .extract()
        .path("orderId");

    // Case 3: 다른 사용자의 주문 조회 시도 (403 Forbidden)
    String otherToken = getAuthToken("other@example.com", "password");

    given()
        .header("Authorization", "Bearer " + otherToken)
        .pathParam("orderId", orderId)
    .when()
        .get("/orders/{orderId}")
    .then()
        .statusCode(403)
        .body("error.code", equalTo("FORBIDDEN"))
        .body("error.message", containsString("권한이 없습니다"));
}

private String getAuthToken(String email, String password) {
    return given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, email, password))
    .when()
        .post("/auth/login")
    .then()
        .statusCode(200)
    .extract()
        .path("token");
}
```

### 6. Performance 시나리오

```java
@Test
@DisplayName("대량 주문 생성 및 조회 성능 시나리오")
void shouldHandleBulkOrdersEfficiently() {
    // Step 1: 100개의 주문 동시 생성
    long startTime = System.currentTimeMillis();

    List<String> orderIds = IntStream.range(1, 101)
        .parallel()
        .mapToObj(i -> given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "customerId": %d,
                    "items": [{"productId": 1, "quantity": 1}]
                }
                """, 100 + (i % 10)))
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
        .extract()
            .path("orderId"))
        .toList();

    long creationTime = System.currentTimeMillis() - startTime;

    // Step 2: 전체 주문 페이징 조회
    startTime = System.currentTimeMillis();

    given()
        .queryParam("page", 0)
        .queryParam("size", 50)
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .body("content", hasSize(50))
        .body("totalElements", equalTo(100))
        .time(lessThan(1000L));  // 1초 이내 응답

    long queryTime = System.currentTimeMillis() - startTime;

    // Then - 성능 검증
    assertThat(creationTime).isLessThan(10000L)
        .describedAs("100개 주문 생성은 10초 이내");
    assertThat(queryTime).isLessThan(1000L)
        .describedAs("페이징 조회는 1초 이내");
}
```

### 7. Idempotency 시나리오

```java
@Test
@DisplayName("멱등성 보장 시나리오 (동일 요청 중복 방지)")
void shouldEnsureIdempotency() {
    String idempotencyKey = UUID.randomUUID().toString();

    // Step 1: 첫 번째 요청 (성공)
    String orderId1 = given()
        .header("Idempotency-Key", idempotencyKey)
        .contentType(ContentType.JSON)
        .body("""
            {
                "customerId": 100,
                "items": [{"productId": 1, "quantity": 2}]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
    .extract()
        .path("orderId");

    // Step 2: 동일한 Idempotency-Key로 재요청 (동일 응답)
    String orderId2 = given()
        .header("Idempotency-Key", idempotencyKey)
        .contentType(ContentType.JSON)
        .body("""
            {
                "customerId": 100,
                "items": [{"productId": 1, "quantity": 2}]
            }
            """)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
    .extract()
        .path("orderId");

    // Then - 동일한 주문 ID 반환
    assertThat(orderId1).isEqualTo(orderId2);

    // Step 3: DB 확인 (1개만 생성됨)
    given()
        .queryParam("customerId", 100)
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .body("content", hasSize(1));
}
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
adapter-in/rest-api/src/test/java/
└── com/ryuqq/adapter/in/rest/{api}/
    └── {Api}E2ETest.java
```

### 2. RestAssured 설정 템플릿
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("{Api} E2E 테스트")
class {Api}E2ETest {

    @LocalServerPort
    private int port;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **@SpringBootTest(RANDOM_PORT)**: 실제 HTTP 서버 구동
- ✅ **RestAssured**: BDD 스타일 API 테스트
- ✅ **Testcontainers**: 실제 DB 사용
- ✅ **Multi-step Scenarios**: 실제 사용자 플로우 재현
- ✅ **Error Handling**: 모든 예외 케이스 검증
- ✅ **Security**: 인증/권한 검증

---

## 📊 테스트 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| User Journey | 100% | 전체 사용자 플로우 |
| Error Cases | 100% | 모든 예외 상황 |
| Security | 100% | 인증/권한 검증 |
| Performance | 주요 API | 응답 시간 검증 |
| Idempotency | 100% | 멱등성 보장 |

---

## 🚀 실행 예시

### Input (REST Controller)
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // ...
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        // ...
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String orderId) {
        // ...
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId, @RequestBody CancelOrderRequest request) {
        // ...
    }
}
```

### Output (Auto-generated E2E Test)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Order API E2E 테스트")
class OrderApiE2ETest {

    @LocalServerPort
    private int port;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withReuse(true);

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    @Test
    @DisplayName("주문 생성 → 조회 → 확인 → 취소 전체 플로우 성공")
    void shouldCompleteOrderLifecycleSuccessfully() {
        String orderId = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "customerId": 100,
                    "items": [{"productId": 1, "quantity": 2}]
                }
                """)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
        .extract()
            .path("orderId");

        given()
            .pathParam("orderId", orderId)
        .when()
            .get("/orders/{orderId}")
        .then()
            .statusCode(200);

        // ... (10-15개 시나리오 자동 생성)
    }
}
```

---

## 💡 Claude Code 활용 팁

### 1. 전체 User Journey 생성
```
"Generate complete user journey E2E tests for OrderController covering create → retrieve → confirm → cancel"
```

### 2. Error Handling 집중
```
"Add comprehensive error handling scenarios for all edge cases in OrderApi"
```

### 3. Security 테스트 추가
```
"Generate authentication and authorization tests for OrderApi with JWT tokens"
```

### 4. Performance 시나리오
```
"Add performance tests for bulk order creation and pagination queries"
```

---

## 🎯 기대 효과

1. **실제 사용자 플로우**: 전체 시나리오 자동 검증
2. **빠른 회귀 테스트**: API 변경 시 즉시 검증
3. **문서화 효과**: E2E 테스트가 API 사용 예제 역할
4. **안정성 보장**: 배포 전 전체 플로우 검증

---

**✅ 이 명령어는 Claude Code가 REST API의 고품질 E2E 테스트를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: Windsurf가 REST API를 생성하면, Claude Code가 RestAssured 기반 E2E 시나리오 테스트를 자동 생성!
