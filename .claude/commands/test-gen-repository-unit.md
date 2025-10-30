---
description: Repository 단위 테스트 자동 생성 (Mock 기반, Test Fixtures)
---

# Repository 단위 테스트 자동 생성 (Mock)

**목적**: Repository 계층의 Mock 기반 순수 단위 테스트 자동 생성

**타겟**: Persistence Layer - Repository Unit Tests (Fast, Isolated)

**생성 테스트**: Mock 기반 CRUD, Test Fixtures, 비즈니스 로직 격리

---

## 🎯 사용법

```bash
# Repository 단위 테스트 생성 (Mock)
/test-gen-repository-unit OrderRepository

# QueryService 단위 테스트 생성 (Mock)
/test-gen-repository-unit OrderQueryService
```

---

## ✅ 자동 생성되는 테스트 케이스

### 1. MockitoExtension 기본 설정

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRepository 단위 테스트 (Mock)")
class OrderRepositoryUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private OrderRepositoryImpl orderRepository;

    // Test Fixtures 사용
    private OrderEntityFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new OrderEntityFixtures();
    }
}
```

### 2. CRUD Mock 테스트

```java
@Test
@DisplayName("주문 저장 성공")
void shouldSaveOrder() {
    // Given
    OrderEntity order = fixtures.createOrder(
        1L,  // orderId
        100L,  // customerId
        "PLACED"
    );

    // When
    orderRepository.save(order);

    // Then
    verify(entityManager).persist(order);
    verifyNoMoreInteractions(entityManager);
}

@Test
@DisplayName("주문 조회 성공")
void shouldFindOrder() {
    // Given
    Long orderId = 1L;
    OrderEntity expectedOrder = fixtures.createOrder(orderId, 100L, "PLACED");

    given(entityManager.find(OrderEntity.class, orderId))
        .willReturn(expectedOrder);

    // When
    Optional<OrderEntity> result = orderRepository.findById(orderId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getOrderId()).isEqualTo(orderId);
    verify(entityManager).find(OrderEntity.class, orderId);
}

@Test
@DisplayName("존재하지 않는 주문 조회 시 Optional.empty 반환")
void shouldReturnEmptyWhenOrderNotFound() {
    // Given
    Long orderId = 999L;
    given(entityManager.find(OrderEntity.class, orderId))
        .willReturn(null);

    // When
    Optional<OrderEntity> result = orderRepository.findById(orderId);

    // Then
    assertThat(result).isEmpty();
    verify(entityManager).find(OrderEntity.class, orderId);
}

@Test
@DisplayName("주문 삭제 성공")
void shouldDeleteOrder() {
    // Given
    OrderEntity order = fixtures.createOrder(1L, 100L, "PLACED");
    given(entityManager.contains(order)).willReturn(true);

    // When
    orderRepository.delete(order);

    // Then
    verify(entityManager).remove(order);
}
```

### 3. QueryDSL Mock 테스트

```java
@Test
@DisplayName("QueryDSL 동적 조건 검색 (Mock)")
void shouldFindOrdersByConditionWithMock() {
    // Given
    OrderSearchCondition condition = new OrderSearchCondition(100L, "PLACED");

    List<OrderEntity> expectedOrders = List.of(
        fixtures.createOrder(1L, 100L, "PLACED"),
        fixtures.createOrder(2L, 100L, "PLACED")
    );

    JPAQuery<OrderEntity> mockQuery = mock(JPAQuery.class);
    given(queryFactory.selectFrom(any(QOrderEntity.class)))
        .willReturn(mockQuery);
    given(mockQuery.where(any(BooleanExpression.class)))
        .willReturn(mockQuery);
    given(mockQuery.fetch())
        .willReturn(expectedOrders);

    // When
    List<OrderEntity> results = orderRepository.findByCondition(condition);

    // Then
    assertThat(results).hasSize(2);
    assertThat(results).extracting(OrderEntity::getCustomerId)
        .containsOnly(100L);

    verify(queryFactory).selectFrom(any(QOrderEntity.class));
    verify(mockQuery).where(any(BooleanExpression.class));
    verify(mockQuery).fetch();
}

@Test
@DisplayName("QueryDSL Projection (Mock)")
void shouldProjectRequiredFieldsOnly() {
    // Given
    Long orderId = 1L;
    OrderProjection expectedProjection = new OrderProjection(
        orderId,
        "PLACED",
        LocalDateTime.now()
    );

    JPAQuery<OrderProjection> mockQuery = mock(JPAQuery.class);
    given(queryFactory.select(any(QBean.class)))
        .willReturn(mockQuery);
    given(mockQuery.from(any(QOrderEntity.class)))
        .willReturn(mockQuery);
    given(mockQuery.where(any(BooleanExpression.class)))
        .willReturn(mockQuery);
    given(mockQuery.fetchOne())
        .willReturn(expectedProjection);

    // When
    OrderProjection result = orderRepository.findProjectionById(orderId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.orderId()).isEqualTo(orderId);
    assertThat(result.status()).isEqualTo("PLACED");
}
```

### 4. Test Fixtures 클래스

```java
package com.ryuqq.adapter.out.persistence.order.fixtures;

/**
 * OrderEntity Test Fixtures
 *
 * <p>테스트에서 사용할 OrderEntity 객체 생성을 담당합니다.</p>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
public class OrderEntityFixtures {

    /**
     * 기본 주문 엔티티 생성
     *
     * @param orderId 주문 ID
     * @param customerId 고객 ID
     * @param status 주문 상태
     * @return 생성된 주문 엔티티
     */
    public OrderEntity createOrder(Long orderId, Long customerId, String status) {
        return OrderEntity.builder()
            .id(orderId)
            .orderId(orderId)
            .customerId(customerId)
            .status(status)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * PLACED 상태 주문 생성
     *
     * @param orderId 주문 ID
     * @param customerId 고객 ID
     * @return PLACED 상태의 주문 엔티티
     */
    public OrderEntity createPlacedOrder(Long orderId, Long customerId) {
        return createOrder(orderId, customerId, "PLACED");
    }

    /**
     * CONFIRMED 상태 주문 생성
     *
     * @param orderId 주문 ID
     * @param customerId 고객 ID
     * @return CONFIRMED 상태의 주문 엔티티
     */
    public OrderEntity createConfirmedOrder(Long orderId, Long customerId) {
        return createOrder(orderId, customerId, "CONFIRMED");
    }

    /**
     * CANCELLED 상태 주문 생성
     *
     * @param orderId 주문 ID
     * @param customerId 고객 ID
     * @return CANCELLED 상태의 주문 엔티티
     */
    public OrderEntity createCancelledOrder(Long orderId, Long customerId) {
        return createOrder(orderId, customerId, "CANCELLED");
    }

    /**
     * 주문 목록 생성 (벌크)
     *
     * @param count 생성할 주문 수
     * @param customerId 고객 ID
     * @return 주문 엔티티 목록
     */
    public List<OrderEntity> createOrders(int count, Long customerId) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> createOrder((long) i, customerId, "PLACED"))
            .toList();
    }
}
```

### 5. 비즈니스 로직 격리 테스트

```java
@Test
@DisplayName("특정 고객의 활성 주문만 조회 (비즈니스 로직 테스트)")
void shouldFindActiveOrdersByCustomerId() {
    // Given
    Long customerId = 100L;
    List<OrderEntity> allOrders = List.of(
        fixtures.createPlacedOrder(1L, customerId),
        fixtures.createConfirmedOrder(2L, customerId),
        fixtures.createCancelledOrder(3L, customerId)  // 제외되어야 함
    );

    JPAQuery<OrderEntity> mockQuery = mock(JPAQuery.class);
    given(queryFactory.selectFrom(any(QOrderEntity.class)))
        .willReturn(mockQuery);
    given(mockQuery.where(any(BooleanExpression.class)))
        .willReturn(mockQuery);
    given(mockQuery.fetch())
        .willReturn(allOrders.stream()
            .filter(order -> !order.getStatus().equals("CANCELLED"))
            .toList());

    // When
    List<OrderEntity> activeOrders = orderRepository.findActiveOrdersByCustomerId(customerId);

    // Then
    assertThat(activeOrders).hasSize(2);
    assertThat(activeOrders)
        .extracting(OrderEntity::getStatus)
        .doesNotContain("CANCELLED");
}

@Test
@DisplayName("페이징 처리 (비즈니스 로직 테스트)")
void shouldHandlePagination() {
    // Given
    int page = 0;
    int size = 10;

    List<OrderEntity> orders = fixtures.createOrders(10, 100L);

    JPAQuery<OrderEntity> mockQuery = mock(JPAQuery.class);
    given(queryFactory.selectFrom(any(QOrderEntity.class)))
        .willReturn(mockQuery);
    given(mockQuery.offset(page * size))
        .willReturn(mockQuery);
    given(mockQuery.limit(size))
        .willReturn(mockQuery);
    given(mockQuery.fetch())
        .willReturn(orders);

    // When
    List<OrderEntity> results = orderRepository.findAll(page, size);

    // Then
    assertThat(results).hasSize(10);
    verify(mockQuery).offset(0);
    verify(mockQuery).limit(10);
}
```

### 6. Exception 처리 테스트

```java
@Test
@DisplayName("EntityManager 예외 발생 시 적절한 예외로 변환")
void shouldHandleEntityManagerException() {
    // Given
    OrderEntity order = fixtures.createOrder(1L, 100L, "PLACED");

    doThrow(new PersistenceException("DB connection failed"))
        .when(entityManager).persist(any(OrderEntity.class));

    // When & Then
    assertThatThrownBy(() -> orderRepository.save(order))
        .isInstanceOf(RepositoryException.class)
        .hasCauseInstanceOf(PersistenceException.class)
        .hasMessageContaining("Failed to save order");

    verify(entityManager).persist(order);
}

@Test
@DisplayName("null 입력 시 예외 발생")
void shouldThrowExceptionWhenSavingNull() {
    // When & Then
    assertThatThrownBy(() -> orderRepository.save(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Order entity must not be null");

    verifyNoInteractions(entityManager);
}
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
adapter-out/persistence-mysql/src/test/java/
└── com/ryuqq/adapter/out/persistence/{entity}/
    ├── {Entity}RepositoryUnitTest.java
    └── fixtures/
        └── {Entity}Fixtures.java
```

### 2. MockitoExtension 템플릿
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("{Entity}Repository 단위 테스트 (Mock)")
class {Entity}RepositoryUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private {Entity}RepositoryImpl repository;

    private {Entity}Fixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new {Entity}Fixtures();
    }
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **@ExtendWith(MockitoExtension.class)**: Mockito 사용
- ✅ **BDD Mockito**: `given()` / `willReturn()` / `verify()`
- ✅ **Test Fixtures**: 재사용 가능한 테스트 데이터
- ✅ **Mock Verification**: 모든 Mock 호출 검증
- ✅ **Fast Execution**: 실제 DB 없이 밀리초 단위 실행
- ✅ **Isolation**: 각 테스트 완전 격리

---

## 📊 테스트 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| CRUD Methods | 100% | save, find, delete 등 |
| QueryDSL Logic | 100% | 동적 쿼리, Projection |
| Business Logic | 100% | 커스텀 메서드 |
| Exception Handling | 100% | 예외 처리 |
| Null Safety | 100% | null 입력 검증 |

---

## 🚀 실행 예시

### Input (Repository Interface)
```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderId(Long orderId);
    List<OrderEntity> findByCondition(OrderSearchCondition condition);
    List<OrderEntity> findActiveOrdersByCustomerId(Long customerId);
}
```

### Output (Auto-generated Unit Test)
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRepository 단위 테스트 (Mock)")
class OrderRepositoryUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private OrderRepositoryImpl orderRepository;

    private OrderEntityFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new OrderEntityFixtures();
    }

    @Test
    @DisplayName("주문 저장 성공")
    void shouldSaveOrder() {
        // Given
        OrderEntity order = fixtures.createOrder(1L, 100L, "PLACED");

        // When
        orderRepository.save(order);

        // Then
        verify(entityManager).persist(order);
    }

    @Test
    @DisplayName("QueryDSL 동적 조건 검색 (Mock)")
    void shouldFindOrdersByConditionWithMock() {
        // Given
        OrderSearchCondition condition = new OrderSearchCondition(100L, "PLACED");
        List<OrderEntity> expectedOrders = List.of(
            fixtures.createOrder(1L, 100L, "PLACED")
        );

        JPAQuery<OrderEntity> mockQuery = mock(JPAQuery.class);
        given(queryFactory.selectFrom(any(QOrderEntity.class)))
            .willReturn(mockQuery);
        given(mockQuery.where(any(BooleanExpression.class)))
            .willReturn(mockQuery);
        given(mockQuery.fetch())
            .willReturn(expectedOrders);

        // When
        List<OrderEntity> results = orderRepository.findByCondition(condition);

        // Then
        assertThat(results).hasSize(1);
        verify(queryFactory).selectFrom(any(QOrderEntity.class));
    }

    // ... (10-12개 테스트 케이스 자동 생성)
}
```

### Output (Auto-generated Test Fixtures)
```java
public class OrderEntityFixtures {

    public OrderEntity createOrder(Long orderId, Long customerId, String status) {
        return OrderEntity.builder()
            .id(orderId)
            .orderId(orderId)
            .customerId(customerId)
            .status(status)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public OrderEntity createPlacedOrder(Long orderId, Long customerId) {
        return createOrder(orderId, customerId, "PLACED");
    }

    public List<OrderEntity> createOrders(int count, Long customerId) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> createOrder((long) i, customerId, "PLACED"))
            .toList();
    }
}
```

---

## 💡 Claude Code 활용 팁

### 1. Repository 단위 테스트 생성
```
"Generate Mock-based unit tests for OrderRepository with Test Fixtures"
```

### 2. QueryDSL Mock 집중
```
"Add QueryDSL mock tests for all dynamic query methods in OrderRepository"
```

### 3. Test Fixtures 보강
```
"Enhance OrderEntityFixtures with builder pattern and more factory methods"
```

### 4. Exception 테스트 추가
```
"Add comprehensive exception handling tests for all repository methods"
```

---

## 🎯 기대 효과

1. **빠른 실행**: Mock 기반으로 밀리초 단위 (DB 없이)
2. **완벽한 격리**: 외부 의존성 없음
3. **Test Fixtures**: 재사용 가능한 테스트 데이터
4. **비즈니스 로직 집중**: DB 세부사항 무시, 로직만 테스트

---

## 📌 Unit vs Integration 비교

| 특성 | Unit Test (Mock) | Integration Test (Testcontainers) |
|------|------------------|-----------------------------------|
| **속도** | 밀리초 (매우 빠름) | 초 단위 (느림) |
| **의존성** | 없음 (Mock) | 실제 DB 필요 |
| **격리** | 완벽한 격리 | DB 상태 의존 |
| **목적** | 로직 검증 | DB 상호작용 검증 |
| **사용 시점** | TDD, 빠른 피드백 | 최종 검증 |

**권장**: Unit Test 먼저 작성 → Integration Test로 최종 검증

---

**✅ 이 명령어는 Claude Code가 Repository의 Mock 기반 순수 단위 테스트를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: 빠른 피드백이 필요한 TDD에서는 Mock 기반 단위 테스트, 실제 DB 검증은 Integration Test!
