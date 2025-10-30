---
description: Repository 통합 테스트 자동 생성 (Testcontainers 기반, Real DB)
---

# Repository 통합 테스트 자동 생성 (Testcontainers)

**목적**: Repository 계층의 실제 DB 기반 통합 테스트 자동 생성

**타겟**: Persistence Layer - Repository Integration Tests (Real DB, N+1 Detection)

**생성 테스트**: Testcontainers, Real CRUD, N+1 Query Detection, Transaction

---

## 🎯 사용법

```bash
# Repository 통합 테스트 생성 (Testcontainers)
/test-gen-repository-integration OrderRepository

# QueryService 통합 테스트 생성 (Testcontainers)
/test-gen-repository-integration OrderQueryService
```

---

## ✅ 자동 생성되는 테스트 케이스

### 1. Testcontainers 기본 설정

```java
@SpringBootTest
@Testcontainers
@DisplayName("OrderRepository 통합 테스트 (Real DB)")
class OrderRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true)
        .withCommand(
            "--character-set-server=utf8mb4",
            "--collation-server=utf8mb4_unicode_ci"
        );

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }
}
```

### 2. Real CRUD 테스트

```java
@Test
@DisplayName("주문 저장 및 실제 DB 조회 성공")
void shouldSaveAndFindOrderInRealDB() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .createdAt(LocalDateTime.now())
        .build();

    // When
    OrderEntity savedOrder = orderRepository.save(order);
    entityManager.flush();  // 쓰기 지연 즉시 실행
    entityManager.clear();  // 1차 캐시 초기화

    // Then - 실제 DB에서 조회
    Optional<OrderEntity> found = orderRepository.findById(savedOrder.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getOrderId()).isEqualTo(1L);
    assertThat(found.get().getCustomerId()).isEqualTo(100L);
}

@Test
@DisplayName("주문 수정 및 실제 DB 반영 확인")
void shouldUpdateOrderInRealDB() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();
    OrderEntity savedOrder = orderRepository.save(order);
    entityManager.flush();
    entityManager.clear();

    // When - 상태 변경
    OrderEntity foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
    foundOrder.updateStatus("CONFIRMED");
    orderRepository.save(foundOrder);
    entityManager.flush();
    entityManager.clear();

    // Then - 실제 DB에서 확인
    OrderEntity updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
    assertThat(updatedOrder.getStatus()).isEqualTo("CONFIRMED");
}

@Test
@DisplayName("주문 삭제 및 실제 DB 반영 확인")
void shouldDeleteOrderFromRealDB() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();
    OrderEntity savedOrder = orderRepository.save(order);
    entityManager.flush();
    Long savedId = savedOrder.getId();

    // When
    orderRepository.delete(savedOrder);
    entityManager.flush();

    // Then - 실제 DB에서 삭제 확인
    assertThat(orderRepository.findById(savedId)).isEmpty();
}
```

### 3. N+1 쿼리 검증 (핵심!)

```java
@Test
@DisplayName("주문 목록 조회 시 N+1 문제 없음 (Fetch Join)")
void shouldNotHaveNPlusOneProblemWhenFetchingOrders() {
    // Given - 5개 주문 생성
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

    // When - Fetch Join 사용
    List<OrderEntity> orders = orderRepository.findAllWithCustomer();

    // Then - 단일 쿼리로 조회 확인
    assertThat(orders).hasSize(5);

    // N+1 검증: 추가 쿼리 없이 customer 접근 가능
    orders.forEach(order -> {
        assertThat(order.getCustomerId()).isNotNull();
        // Fetch Join으로 이미 로드되어 있어 추가 쿼리 없음
    });
}

@Test
@DisplayName("Lazy Loading 시 N+1 문제 발생 확인 (Anti-pattern)")
void shouldDetectNPlusOneProblemWithLazyLoading() {
    // Given - 5개 주문 생성
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

    // When - Fetch Join 없이 조회 (N+1 발생 예상)
    List<OrderEntity> orders = orderRepository.findAll();

    // Then - N+1 경고 (실제 프로젝트에서는 이 패턴 금지)
    assertThat(orders).hasSize(5);

    // 각 order마다 추가 쿼리 발생 (N+1)
    // 이 테스트는 N+1이 발생함을 증명하기 위한 Anti-pattern 예시
}
```

### 4. QueryDSL 실제 쿼리 테스트

```java
@Test
@DisplayName("QueryDSL 동적 조건 검색 - 실제 DB 쿼리")
void shouldFindOrdersByDynamicConditionsInRealDB() {
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

    // Then - 실제 DB에서 조회된 결과 검증
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getOrderId()).isEqualTo(1L);
    assertThat(results.get(0).getCustomerId()).isEqualTo(100L);
    assertThat(results.get(0).getStatus()).isEqualTo("PLACED");
}

@Test
@DisplayName("QueryDSL Projection - 실제 DB에서 필요한 컬럼만 조회")
void shouldProjectOnlyRequiredColumnsFromRealDB() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .createdAt(LocalDateTime.now())
        .build();
    orderRepository.save(order);
    entityManager.flush();
    entityManager.clear();

    // When - Projection 사용
    OrderProjection projection = orderRepository.findProjectionById(1L);

    // Then - 필요한 필드만 조회됨
    assertThat(projection).isNotNull();
    assertThat(projection.orderId()).isEqualTo(1L);
    assertThat(projection.status()).isEqualTo("PLACED");
}
```

### 5. Transaction Rollback 테스트

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

    // When & Then - 예외 발생 시 저장 안됨
    assertThatThrownBy(() -> {
        orderRepository.save(order);
        entityManager.flush();
        throw new RuntimeException("Intentional exception");
    }).isInstanceOf(RuntimeException.class);

    // Transaction Rollback 확인
    entityManager.clear();
    assertThat(orderRepository.findByOrderId(1L)).isEmpty();
}

@Test
@DisplayName("Transactional 메서드 내 예외 시 자동 Rollback")
@Transactional
void shouldAutoRollbackInTransactionalMethod() {
    // Given
    OrderEntity order = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();

    // When - 저장 후 예외 발생
    orderRepository.save(order);
    entityManager.flush();

    // Then - RuntimeException 발생
    throw new RuntimeException("Force rollback");
}

@Test
@DisplayName("이전 테스트의 Rollback 확인")
void shouldVerifyPreviousTestRolledBack() {
    // Then - 이전 테스트에서 저장한 데이터 없음
    assertThat(orderRepository.findAll()).isEmpty();
}
```

### 6. 동시성 테스트 (Real DB)

```java
@Test
@DisplayName("동시 저장 시 충돌 없음 (Real DB)")
void shouldHandleConcurrentSaves() throws InterruptedException {
    // Given
    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    List<Long> orderIds = Collections.synchronizedList(new ArrayList<>());

    // When - 10개 스레드에서 동시 저장
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    for (int i = 1; i <= threadCount; i++) {
        final int orderId = i;
        executor.submit(() -> {
            try {
                OrderEntity order = OrderEntity.builder()
                    .orderId((long) orderId)
                    .customerId(100L)
                    .status("PLACED")
                    .build();
                OrderEntity saved = orderRepository.save(order);
                entityManager.flush();
                orderIds.add(saved.getOrderId());
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - 모든 주문이 저장됨
    assertThat(orderRepository.findAll()).hasSize(threadCount);
    assertThat(orderIds).hasSize(threadCount);
}
```

### 7. Unique Constraint 테스트 (Real DB)

```java
@Test
@DisplayName("동일 orderId 저장 시 Unique Constraint 위반")
void shouldThrowExceptionOnDuplicateOrderId() {
    // Given
    OrderEntity order1 = OrderEntity.builder()
        .orderId(1L)
        .customerId(100L)
        .status("PLACED")
        .build();
    orderRepository.save(order1);
    entityManager.flush();
    entityManager.clear();

    // When & Then - 동일 orderId 저장 시도
    OrderEntity order2 = OrderEntity.builder()
        .orderId(1L)  // 중복
        .customerId(200L)
        .status("PLACED")
        .build();

    assertThatThrownBy(() -> {
        orderRepository.save(order2);
        entityManager.flush();
    })
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("Duplicate entry");
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

### 2. Testcontainers 템플릿
```java
@SpringBootTest
@Testcontainers
@DisplayName("{Entity}Repository 통합 테스트 (Real DB)")
class {Entity}RepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withReuse(true);

    @Autowired
    private {Entity}Repository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **@SpringBootTest**: 실제 Spring Context
- ✅ **@Testcontainers**: Docker 기반 실제 MySQL
- ✅ **EntityManager.flush()**: 쓰기 지연 즉시 실행
- ✅ **EntityManager.clear()**: 1차 캐시 초기화 (DB 직접 조회 강제)
- ✅ **N+1 Detection**: Fetch Join 검증
- ✅ **Transaction Rollback**: 예외 시 롤백 검증
- ✅ **Real Constraints**: Unique, FK 등 실제 제약조건 테스트

---

## 📊 테스트 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| Real CRUD | 100% | 실제 DB 저장/조회/수정/삭제 |
| N+1 Detection | 100% | Fetch Join 검증 |
| QueryDSL | 100% | 동적 쿼리, Projection |
| Transaction | 100% | Rollback 검증 |
| Constraints | 100% | Unique, FK 등 제약조건 |
| Concurrency | 주요 케이스 | 동시성 처리 |

---

## 💡 Claude Code 활용 팁

### 1. Repository 통합 테스트 생성
```
"Generate Testcontainers-based integration tests for OrderRepository with real DB"
```

### 2. N+1 검증 집중
```
"Add N+1 query detection tests for all findAll methods with Fetch Join validation"
```

### 3. Transaction Rollback 추가
```
"Add comprehensive transaction rollback tests for all save operations"
```

### 4. 동시성 테스트
```
"Add concurrency tests for OrderRepository with multiple threads"
```

---

## 🎯 기대 효과

1. **실제 DB 검증**: Testcontainers로 실제 MySQL 사용
2. **N+1 쿼리 방지**: Fetch Join 자동 검증
3. **Transaction 안정성**: Rollback 자동 검증
4. **DB Constraints**: Unique, FK 등 실제 제약조건 테스트

---

## 📌 Unit vs Integration 비교

| 특성 | Unit Test (Mock) | Integration Test (Testcontainers) |
|------|------------------|-----------------------------------|
| **속도** | 밀리초 (매우 빠름) | 초 단위 (느림) |
| **의존성** | 없음 (Mock) | 실제 DB 필요 |
| **격리** | 완벽한 격리 | DB 상태 의존 |
| **목적** | 로직 검증 | DB 상호작용 검증 |
| **N+1 검증** | 불가능 (Mock) | 가능 (Real Query) ✅ |
| **Constraints** | 불가능 (Mock) | 가능 (Real DB) ✅ |
| **사용 시점** | TDD, 빠른 피드백 | 최종 검증 |

**권장 전략**:
1. **TDD 단계**: Unit Test (Mock) 먼저 작성 → 빠른 피드백
2. **완성 단계**: Integration Test (Testcontainers) 추가 → 실제 DB 검증
3. **CI/CD**: Unit Test (빌드마다) + Integration Test (PR 시)

---

**✅ 이 명령어는 Claude Code가 Repository의 실제 DB 기반 통합 테스트를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: Mock으로 빠르게 TDD → Testcontainers로 실제 DB 최종 검증! (N+1, Transaction, Constraints)
