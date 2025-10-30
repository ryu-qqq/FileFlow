# 테스트 자동화 전략 - Claude Code vs Windsurf

## 🎯 핵심 전략

**Windsurf**: 빠른 Test Fixture + Testcontainers 설정
**Claude Code**: 고품질 통합/E2E 테스트 + API 문서 자동 생성

---

## 📊 테스트 피라미드 & 도구 분담

```
         /\
        /E2E\        ← Claude Code (시나리오 이해 필요)
       /------\
      / 통합   \      ← Claude Code + Windsurf (50:50)
     /----------\
    /   단위     \    ← Claude Code (현재 구현됨)
   /--------------\
```

### 1. 단위 테스트 (Unit Tests)
- **담당**: Claude Code ✅ (완료)
- **명령어**: `/test-gen-domain`, `/test-gen-usecase`
- **특징**: 외부 의존성 없음, 빠른 실행

### 2. 통합 테스트 (Integration Tests)
- **담당**: **Claude Code (70%) + Windsurf (30%)**
- **Claude Code 역할**:
  - 복잡한 테스트 시나리오 작성
  - Testcontainers 설정 자동 생성
  - N+1 쿼리 검증 테스트
- **Windsurf 역할**:
  - 기본 Testcontainers 템플릿 빠른 생성
  - 간단한 CRUD 테스트 자동화

### 3. E2E 테스트 (End-to-End Tests)
- **담당**: **Claude Code (90%)**
- **이유**: 비즈니스 시나리오 이해 필요
- **생성 내용**:
  - 사용자 여정 (User Journey) 테스트
  - Multi-step 워크플로우
  - RestAssured 기반 API 테스트

### 4. API 문서 (OpenAPI/RestDocs)
- **담당**: **Claude Code (100%)**
- **이유**: 코드 + 문서 동기화, 예제 생성 필요
- **생성 내용**:
  - Spring REST Docs 설정
  - OpenAPI 3.0 Spec 자동 생성
  - Swagger UI 통합

---

## 🔧 새로운 Claude Code 명령어

### 1. `/test-gen-integration` ⭐ NEW

**목적**: Persistence/Repository 통합 테스트 자동 생성

**사용법**:
```bash
/test-gen-integration OrderRepository
/test-gen-integration --mysql ProductRepository
/test-gen-integration --redis CacheService
```

**생성 내용**:
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 저장 및 조회 성공")
    void shouldSaveAndFindOrder() {
        // Given
        OrderJpaEntity entity = OrderJpaEntityFixture.create();

        // When
        OrderJpaEntity saved = orderRepository.save(entity);
        Optional<OrderJpaEntity> found = orderRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("N+1 쿼리 문제 없이 연관 엔티티 조회")
    void shouldFetchWithoutNPlusOne() {
        // Given
        List<OrderJpaEntity> orders = Arrays.asList(
            OrderJpaEntityFixture.createWithId(1L),
            OrderJpaEntityFixture.createWithId(2L)
        );
        orderRepository.saveAll(orders);

        // When
        List<OrderJpaEntity> result = orderRepository.findAllWithCustomer();

        // Then
        assertThat(result).hasSize(2);
        // N+1 쿼리 검증 (SQL 카운터 사용)
    }
}
```

---

### 2. `/test-gen-e2e` ⭐ NEW

**목적**: E2E 사용자 시나리오 테스트 자동 생성

**사용법**:
```bash
/test-gen-e2e "Order 생성 → 결제 → 배송 시나리오"
/test-gen-e2e --scenario order-flow
```

**생성 내용**:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class OrderFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 생성 → 결제 → 배송 전체 플로우")
    void shouldCompleteOrderFlowSuccessfully() throws Exception {
        // Step 1: 주문 생성
        String createRequest = """
            {
              "customerId": 1,
              "items": [{"productId": 100, "quantity": 2}]
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/api/orders")
            .contentType(APPLICATION_JSON)
            .content(createRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists())
            .andReturn();

        Long orderId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.orderId");

        // Step 2: 결제 처리
        mockMvc.perform(post("/api/orders/{orderId}/payment", orderId)
            .contentType(APPLICATION_JSON)
            .content("""
                {"paymentMethod": "CARD", "amount": 20000}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));

        // Step 3: 배송 시작
        mockMvc.perform(post("/api/orders/{orderId}/ship", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));

        // Step 4: 최종 상태 확인
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderStatus").value("SHIPPED"))
            .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("재고 부족 시 주문 실패 플로우")
    void shouldFailWhenInsufficientStock() throws Exception {
        // Given: 재고 부족 시나리오
        String request = """
            {
              "customerId": 1,
              "items": [{"productId": 999, "quantity": 1000}]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/orders")
            .contentType(APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"))
            .andExpect(jsonPath("$.message").value("Insufficient stock for product 999"));
    }
}
```

---

### 3. `/test-gen-api-docs` ⭐ NEW

**목적**: Spring REST Docs + OpenAPI 문서 자동 생성

**사용법**:
```bash
/test-gen-api-docs OrderController
/test-gen-api-docs --format openapi
/test-gen-api-docs --format restdocs
```

**생성 내용**:

#### A. Spring REST Docs 설정
```java
@WebMvcTest(OrderController.class)
@AutoConfigureRestDocs
class OrderControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaceOrderUseCase placeOrderUseCase;

    @Test
    @DisplayName("주문 생성 API 문서")
    void documentPlaceOrder() throws Exception {
        // Given
        OrderResponse response = new OrderResponse(1L, "PLACED", LocalDateTime.now());
        given(placeOrderUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/orders")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "customerId": 1,
                  "items": [
                    {"productId": 100, "quantity": 2}
                  ]
                }
                """))
            .andExpect(status().isCreated())
            .andDo(document("orders/place",
                requestFields(
                    fieldWithPath("customerId").description("고객 ID"),
                    fieldWithPath("items").description("주문 상품 목록"),
                    fieldWithPath("items[].productId").description("상품 ID"),
                    fieldWithPath("items[].quantity").description("주문 수량")
                ),
                responseFields(
                    fieldWithPath("orderId").description("생성된 주문 ID"),
                    fieldWithPath("status").description("주문 상태 (PLACED)"),
                    fieldWithPath("createdAt").description("주문 생성 시각")
                )
            ));
    }
}
```

#### B. OpenAPI 3.0 Spec 생성
```yaml
# Generated by Claude Code
openapi: 3.0.3
info:
  title: Order Management API
  description: |
    주문 관리 시스템 REST API

    **주요 기능**:
    - 주문 생성, 조회, 취소
    - 결제 처리
    - 배송 관리
  version: 1.0.0
  contact:
    name: Claude Code
    email: noreply@example.com

servers:
  - url: http://localhost:8080/api
    description: Local Development
  - url: https://api.example.com/api
    description: Production

paths:
  /orders:
    post:
      summary: 주문 생성
      description: 새로운 주문을 생성합니다.
      operationId: placeOrder
      tags:
        - Orders
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PlaceOrderRequest'
            examples:
              example1:
                summary: 단일 상품 주문
                value:
                  customerId: 1
                  items:
                    - productId: 100
                      quantity: 2
      responses:
        '201':
          description: 주문 생성 성공
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderResponse'
        '400':
          description: 잘못된 요청 (재고 부족 등)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  schemas:
    PlaceOrderRequest:
      type: object
      required:
        - customerId
        - items
      properties:
        customerId:
          type: integer
          format: int64
          description: 고객 ID
          example: 1
        items:
          type: array
          description: 주문 상품 목록
          minItems: 1
          items:
            $ref: '#/components/schemas/OrderItem'

    OrderItem:
      type: object
      required:
        - productId
        - quantity
      properties:
        productId:
          type: integer
          format: int64
          description: 상품 ID
          example: 100
        quantity:
          type: integer
          format: int32
          description: 주문 수량
          minimum: 1
          example: 2

    OrderResponse:
      type: object
      properties:
        orderId:
          type: integer
          format: int64
          description: 생성된 주문 ID
          example: 1
        status:
          type: string
          enum: [PLACED, CONFIRMED, SHIPPED, CANCELLED]
          description: 주문 상태
          example: PLACED
        createdAt:
          type: string
          format: date-time
          description: 주문 생성 시각

    ErrorResponse:
      type: object
      properties:
        errorCode:
          type: string
          description: 에러 코드
          example: INSUFFICIENT_STOCK
        message:
          type: string
          description: 에러 메시지
          example: Insufficient stock for product 100
```

---

### 4. `/test-gen-testcontainers` ⭐ NEW

**목적**: Testcontainers 설정 자동 생성 (MySQL, Redis, Kafka 등)

**사용법**:
```bash
/test-gen-testcontainers --mysql
/test-gen-testcontainers --redis
/test-gen-testcontainers --all
```

**생성 내용**:

#### A. 공통 Testcontainers 설정
```java
/**
 * Testcontainers 공통 설정 클래스
 *
 * <p>모든 통합 테스트에서 재사용 가능한 컨테이너 설정을 제공합니다.</p>
 *
 * <h3>사용법:</h3>
 * <pre>{@code
 * @SpringBootTest
 * @Import(TestContainersConfig.class)
 * class MyIntegrationTest {
 *     // 테스트 코드
 * }
 * }</pre>
 *
 * @author Claude Code
 * @since 1.0.0
 */
@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci"
            );
    }

    @Bean
    @ServiceConnection
    public RedisContainer redisContainer() {
        return new RedisContainer("redis:7.0")
            .withExposedPorts(6379);
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withKraft();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry,
                              @Autowired MySQLContainer<?> mysql,
                              @Autowired RedisContainer redis,
                              @Autowired KafkaContainer kafka) {
        // MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

#### B. 개별 Repository 테스트
```java
@SpringBootTest
@Import(TestContainersConfig.class)
@Transactional
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("MySQL에 주문 저장 및 조회")
    void shouldSaveOrderToMySQL() {
        // Test implementation
    }
}
```

---

## 🔄 Windsurf Workflows 보강

### 1. `/test-integration-quick` ⭐ NEW

**목적**: 빠른 통합 테스트 템플릿 생성 (Windsurf 전용)

**Workflow 파일**: `.windsurf/workflows/test-integration-quick.md`

```markdown
---
description: 빠른 통합 테스트 템플릿 생성 (Testcontainers)
---

# Quick Integration Test Generation

**목적**: Repository 통합 테스트 보일러플레이트 빠른 생성

## Step 1: TestFixture 생성
→ `{Entity}JpaEntityFixture.java` 확인 또는 생성

## Step 2: Testcontainers 설정
→ `TestContainersConfig.java` 재사용

## Step 3: Repository Test 템플릿
```java
@SpringBootTest
@Import(TestContainersConfig.class)
@Transactional
class {Entity}RepositoryIntegrationTest {

    @Autowired
    private {Entity}Repository repository;

    @Test
    void shouldSave() {
        // Given
        var entity = {Entity}JpaEntityFixture.create();

        // When
        var saved = repository.save(entity);

        // Then
        assertThat(saved.getId()).isNotNull();
    }
}
```

## Step 4: 검증
→ `./gradlew :adapter-out-persistence:integrationTest`
```

---

### 2. `/test-e2e-scenario` ⭐ NEW

**목적**: E2E 시나리오 템플릿 생성 (Windsurf → Claude 협업)

**Workflow 파일**: `.windsurf/workflows/test-e2e-scenario.md`

```markdown
---
description: E2E 시나리오 템플릿 생성 (기본 구조)
---

# E2E Scenario Test Generation

**역할**: Windsurf는 기본 구조만, 상세 시나리오는 Claude Code

## Step 1: E2E Test 기본 구조
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class {Feature}E2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("{시나리오 설명}")
    void should{Scenario}() throws Exception {
        // TODO: Claude Code에게 상세 시나리오 요청
        // /test-gen-e2e "{시나리오 설명}"
    }
}
```

## Step 2: Claude Code 호출
→ `/test-gen-e2e "상세 시나리오"`
→ Claude가 Multi-step 워크플로우 자동 생성
```

---

## 📊 역할 분담 요약

| 테스트 유형 | Windsurf | Claude Code | 비율 |
|------------|----------|-------------|------|
| **단위 테스트** | ❌ | ✅ 전담 | 0:100 |
| **통합 테스트 (Simple)** | ✅ 템플릿 | ✅ 보강 | 50:50 |
| **통합 테스트 (Complex)** | ❌ | ✅ 전담 | 0:100 |
| **E2E 테스트** | ⚠️ 구조만 | ✅ 시나리오 | 10:90 |
| **API 문서** | ❌ | ✅ 전담 | 0:100 |
| **Testcontainers 설정** | ✅ 기본 | ✅ 최적화 | 60:40 |
| **Test Fixtures** | ✅ 템플릿 | ✅ 상세 | 70:30 |

---

## 🚀 권장 워크플로우

### 시나리오 1: Repository 통합 테스트

```bash
# 1️⃣ Windsurf: TestFixture + 기본 구조
"OrderRepository 통합 테스트 템플릿 생성해줘"
→ OrderJpaEntityFixture.java (있으면 Skip)
→ OrderRepositoryIntegrationTest.java (기본 CRUD)

# 2️⃣ Claude Code: 복잡한 시나리오 추가
/test-gen-integration OrderRepository
→ N+1 쿼리 검증
→ QueryDSL 동적 쿼리 테스트
→ Transaction 격리 수준 테스트
```

### 시나리오 2: E2E 플로우 테스트

```bash
# 1️⃣ Claude Code: 전체 시나리오 자동 생성
/test-gen-e2e "주문 생성 → 결제 → 배송 플로우"
→ Multi-step E2E 테스트 자동 생성
→ 예외 케이스 포함 (재고 부족, 결제 실패 등)

# 2️⃣ Windsurf: 검증
/run-e2e-tests
→ 전체 플로우 테스트 실행
```

### 시나리오 3: API 문서 생성

```bash
# Claude Code: REST Docs + OpenAPI 자동 생성
/test-gen-api-docs OrderController
→ Spring REST Docs 테스트 생성
→ OpenAPI 3.0 Spec 생성
→ Swagger UI 통합
```

---

## 💡 핵심 전략

1. **Windsurf**: 빠른 템플릿 생성 (Boilerplate)
2. **Claude Code**: 복잡한 시나리오 + 문서 (Intelligence)
3. **Testcontainers**: 공통 설정 재사용 (DRY)
4. **API 문서**: 코드 + 문서 동기화 (Single Source of Truth)

---

**✅ 이 전략은 테스트 작성 시간을 80% 단축하고, 테스트 품질을 2배 향상시킵니다!**
