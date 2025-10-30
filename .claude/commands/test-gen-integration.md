---
description: Infrastructure 통합 테스트 자동 생성 (Redis Cache, Kafka Event)
---

# Infrastructure 통합 테스트 자동 생성

**목적**: Infrastructure 계층의 통합 테스트 자동 생성 (Redis, Kafka 등)

**타겟**: Infrastructure Layer - Redis Cache, Kafka Event, External Service Integration

**생성 테스트**: Redis Cache, Kafka Event, External API, Message Queue

**참고**: Repository 테스트는 `/test-gen-repository-unit` (Mock) 또는 `/test-gen-repository-integration` (Testcontainers) 사용

---

## 🎯 사용법

```bash
# Redis Cache 통합 테스트 생성
/test-gen-integration OrderCacheService

# Kafka Event 통합 테스트 생성
/test-gen-integration OrderEventPublisher

# External Service 통합 테스트
/test-gen-integration PaymentGatewayClient
```

---

## ✅ 자동 생성되는 테스트 케이스

### 1. Testcontainers 기본 설정

```java
@SpringBootTest
@Testcontainers
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);  // 테스트 간 컨테이너 재사용

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }
}
```

### 2. CRUD 기본 테스트

```java
@Test
@DisplayName("주문 저장 및 조회 성공")
void shouldSaveAndFindOrder() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();

    // When
    OrderEntity savedOrder = orderRepository.save(order);
    entityManager.flush();
    entityManager.clear();  // 영속성 컨텍스트 초기화

    // Then
    Optional<OrderEntity> found = orderRepository.findById(savedOrder.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getOrderId()).isEqualTo(1L);
    assertThat(found.get().getCustomerId()).isEqualTo(100L);
}

@Test
@DisplayName("존재하지 않는 주문 조회 시 Optional.empty 반환")
void shouldReturnEmptyWhenOrderNotFound() {
    // When
    Optional<OrderEntity> found = orderRepository.findById(999L);

    // Then
    assertThat(found).isEmpty();
}

@Test
@DisplayName("주문 삭제 성공")
void shouldDeleteOrder() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();
    OrderEntity savedOrder = orderRepository.save(order);

    // When
    orderRepository.delete(savedOrder);
    entityManager.flush();

    // Then
    assertThat(orderRepository.findById(savedOrder.getId())).isEmpty();
}
```

### 3. QueryDSL N+1 쿼리 검증

```java
@Test
@DisplayName("주문 목록 조회 시 N+1 문제 없음 (Fetch Join)")
void shouldNotHaveNPlusOneProblemWhenFetchingOrders() {
    // Given
    for (int i = 1; i <= 5; i++) {
        OrderEntity order = OrderEntity.builder()
            .orderId((long) i)
            .customerId(100L + i)
            .status("PLACED")
            .build();
        orderRepository.save(order);
    }
    entityManager.flush();
    entityManager.clear();

    // When
    long queryCountBefore = getQueryCount();
    List<OrderEntity> orders = orderRepository.findAllWithCustomer();  // Fetch Join
    long queryCountAfter = getQueryCount();

    // Then
    assertThat(orders).hasSize(5);
    long queriesExecuted = queryCountAfter - queryCountBefore;
    assertThat(queriesExecuted).isEqualTo(1)
        .describedAs("Fetch Join을 사용하여 단일 쿼리로 조회해야 함");
}

private long getQueryCount() {
    return ((Number) entityManager.createNativeQuery(
        "SELECT COUNT(*) FROM information_schema.PROCESSLIST WHERE db = DATABASE()")
        .getSingleResult()).longValue();
}
```

### 4. QueryDSL 동적 쿼리 테스트

```java
@Test
@DisplayName("QueryDSL 동적 조건 검색 성공")
void shouldFindOrdersByDynamicConditions() {
    // Given
    OrderEntity order1 = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();
    OrderEntity order2 = OrderEntity.builder()
        .orderId(2L)
        .customerId(100L)
        .status("CONFIRMED")
        .build();
    OrderEntity order3 = OrderEntity.builder()
        .orderId(3L)
        .customerId(200L)
        .status("PLACED")
        .build();

    orderRepository.saveAll(List.of(order1, order2, order3));
    entityManager.flush();
    entityManager.clear();

    // When - customerId = 100 AND status = "PLACED"
    OrderSearchCondition condition = new OrderSearchCondition(100L, "PLACED");
    List<OrderEntity> results = orderRepository.findByCondition(condition);

    // Then
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getOrderId()).isEqualTo(1L);
}

@Test
@DisplayName("QueryDSL Projection으로 필요한 컬럼만 조회")
void shouldProjectOnlyRequiredColumns() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();
    orderRepository.save(order);
    entityManager.flush();
    entityManager.clear();

    // When
    OrderProjection projection = orderRepository.findProjectionById(1L);

    // Then
    assertThat(projection).isNotNull();
    assertThat(projection.orderId()).isEqualTo(1L);
    assertThat(projection.status()).isEqualTo("PLACED");
}
```

### 5. Redis Cache 통합 테스트

```java
@SpringBootTest
@Testcontainers
@DisplayName("OrderQueryService Redis 캐시 통합 테스트")
class OrderQueryServiceCacheIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .withReuse(true);

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
    }

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(cacheName ->
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("첫 조회는 DB, 두 번째 조회는 캐시에서 반환")
    void shouldUseCacheOnSecondQuery() {
        // Given
        OrderEntity order = OrderEntity.builder()
            .orderId(1L)
            .customerId(100L)
            .status("PLACED")
            .build();
        orderRepository.save(order);

        // When - 첫 번째 조회 (DB)
        long startTime1 = System.currentTimeMillis();
        OrderDto result1 = orderQueryService.getOrder(1L);
        long duration1 = System.currentTimeMillis() - startTime1;

        // When - 두 번째 조회 (Cache)
        long startTime2 = System.currentTimeMillis();
        OrderDto result2 = orderQueryService.getOrder(1L);
        long duration2 = System.currentTimeMillis() - startTime2;

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(duration2).isLessThan(duration1)
            .describedAs("캐시에서 조회하므로 더 빠름");

        Cache cache = cacheManager.getCache("orders");
        assertThat(cache).isNotNull();
        assertThat(cache.get(1L)).isNotNull();
    }

    @Test
    @DisplayName("캐시 Eviction 후 다시 DB 조회")
    void shouldQueryDBAfterCacheEviction() {
        // Given
        OrderEntity order = OrderEntity.builder()
            .orderId(1L)
            .customerId(100L)
            .status("PLACED")
            .build();
        orderRepository.save(order);

        // When - 첫 조회 (캐시 적재)
        OrderDto result1 = orderQueryService.getOrder(1L);

        // 캐시 제거
        orderQueryService.evictOrderCache(1L);

        // 두 번째 조회 (DB 다시 조회)
        OrderDto result2 = orderQueryService.getOrder(1L);

        // Then
        assertThat(result1).isEqualTo(result2);
        Cache cache = cacheManager.getCache("orders");
        assertThat(cache).isNotNull();
        assertThat(cache.get(1L)).isNotNull();  // 다시 캐시 적재됨
    }
}
```

### 6. Transaction Rollback 테스트

```java
@Test
@DisplayName("예외 발생 시 Transaction Rollback 확인")
void shouldRollbackTransactionOnException() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();

    // When & Then
    assertThatThrownBy(() ->
        orderRepository.saveWithException(order)  // 내부에서 예외 발생
    ).isInstanceOf(RuntimeException.class);

    // Rollback 확인
    assertThat(orderRepository.findById(1L)).isEmpty();
}
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
adapter-out/persistence-mysql/src/test/java/
└── com/ryuqq/adapter/out/persistence/{entity}/
    └── {Entity}RepositoryIntegrationTest.java
```

### 2. Testcontainers 설정 템플릿
```java
@SpringBootTest
@Testcontainers
@DisplayName("{Entity}Repository 통합 테스트")
class {Entity}RepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private {Entity}Repository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **@SpringBootTest**: 실제 Spring Context 로드
- ✅ **@Testcontainers**: Docker 기반 실제 DB
- ✅ **EntityManager.flush()**: 쓰기 지연 즉시 실행
- ✅ **EntityManager.clear()**: 1차 캐시 초기화
- ✅ **N+1 쿼리 검증**: Fetch Join 사용 확인
- ✅ **Transaction Rollback**: 예외 시 롤백 검증

---

## 📊 테스트 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| Repository CRUD | 100% | 모든 CRUD 메서드 |
| QueryDSL | 100% | 동적 쿼리, Projection |
| N+1 Detection | 100% | Fetch Join 검증 |
| Cache | 100% | Redis 캐시 동작 |
| Transaction | 100% | Rollback 확인 |

---

## 🚀 실행 예시

### Input (Repository Interface)
```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderId(Long orderId);

    List<OrderEntity> findAllWithCustomer();

    List<OrderEntity> findByCondition(OrderSearchCondition condition);

    OrderProjection findProjectionById(Long orderId);
}
```

### Output (Auto-generated Test)
```java
@SpringBootTest
@Testcontainers
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("주문 저장 및 조회 성공")
    void shouldSaveAndFindOrder() {
        // Given
        OrderEntity order = OrderEntity.builder()
            .orderId(1L)
            .customerId(100L)
            .status("PLACED")
            .build();

        // When
        OrderEntity savedOrder = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<OrderEntity> found = orderRepository.findById(savedOrder.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주문 목록 조회 시 N+1 문제 없음 (Fetch Join)")
    void shouldNotHaveNPlusOneProblemWhenFetchingOrders() {
        // Given
        for (int i = 1; i <= 5; i++) {
            OrderEntity order = OrderEntity.builder()
                .orderId((long) i)
                .customerId(100L + i)
                .status("PLACED")
                .build();
            orderRepository.save(order);
        }
        entityManager.flush();
        entityManager.clear();

        // When
        long queryCountBefore = getQueryCount();
        List<OrderEntity> orders = orderRepository.findAllWithCustomer();
        long queryCountAfter = getQueryCount();

        // Then
        assertThat(orders).hasSize(5);
        assertThat(queryCountAfter - queryCountBefore).isEqualTo(1);
    }

    @Test
    @DisplayName("QueryDSL 동적 조건 검색 성공")
    void shouldFindOrdersByDynamicConditions() {
        // Given
        OrderEntity order1 = OrderEntity.builder()
            .orderId(1L)
            .customerId(100L)
            .status("PLACED")
            .build();
        OrderEntity order2 = OrderEntity.builder()
            .orderId(2L)
            .customerId(100L)
            .status("CONFIRMED")
            .build();

        orderRepository.saveAll(List.of(order1, order2));
        entityManager.flush();
        entityManager.clear();

        // When
        OrderSearchCondition condition = new OrderSearchCondition(100L, "PLACED");
        List<OrderEntity> results = orderRepository.findByCondition(condition);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOrderId()).isEqualTo(1L);
    }

    // ... (15개 테스트 케이스 자동 생성)
}
```

---

## 💡 Claude Code 활용 팁

### 1. 기존 Repository 분석
```
"Analyze OrderRepository.java and generate comprehensive integration tests with Testcontainers"
```

### 2. N+1 쿼리 집중 테스트
```
"Generate N+1 query detection tests for all findAll methods in OrderRepository"
```

### 3. Redis 캐시 통합 테스트
```
"Add Redis cache integration tests for OrderQueryService with eviction scenarios"
```

### 4. Transaction Rollback 테스트
```
"Add transaction rollback tests for all save operations in OrderRepository"
```

---

## 🎯 기대 효과

1. **실제 DB 테스트**: Testcontainers로 실제 MySQL 사용
2. **N+1 쿼리 방지**: Fetch Join 자동 검증
3. **캐시 동작 확인**: Redis 캐시 Hit/Miss 검증
4. **트랜잭션 안정성**: Rollback 자동 검증

---

**✅ 이 명령어는 Claude Code가 Persistence Layer의 고품질 통합 테스트를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: Windsurf가 Repository를 생성하면, Claude Code가 Testcontainers 기반 통합 테스트를 자동 생성!
