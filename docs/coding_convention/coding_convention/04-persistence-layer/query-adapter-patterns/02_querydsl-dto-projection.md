# QueryDSL DTO Projection (QueryDSL DTO 프로젝션)

**목적**: QueryDSL을 사용한 Pure CQRS DTO 직접 조회 패턴

**위치**: `adapter-persistence/[module]/adapter/`

**필수 버전**: Java 21+, QueryDSL 5.0+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### QueryDSL DTO Projection

QueryDSL `Projections.constructor()`를 사용하여 **DTO를 직접 조회**합니다:

```
Query Adapter
    ↓
JPAQueryFactory
    ↓
Projections.constructor(OrderDetailResponse.class, ...)
    ↓
DTO 직접 생성 (Domain Model 거치지 않음)
    ↓
OrderDetailResponse 반환
```

**규칙**:
- ✅ `Projections.constructor()` 사용
- ✅ DTO Record 패턴
- ✅ N+1 문제 방지 (Join + Fetch)
- ❌ Entity 조회 후 DTO 변환 금지 (성능 저하)
- ❌ Domain Model 거치지 않음

---

## 📦 QueryDSL DTO Projection 패턴

### 기본 패턴 (단일 테이블)

```java
package com.company.adapter.out.persistence.order.adapter;

import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.port.out.LoadOrderPort;
import com.company.domain.order.OrderId;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.company.adapter.out.persistence.order.entity.QOrderJpaEntity.orderJpaEntity;

/**
 * Order Query Adapter (QueryDSL DTO Projection)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OrderQueryAdapter implements LoadOrderPort {

    private final JPAQueryFactory queryFactory;

    public OrderQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<OrderDetailResponse> loadById(OrderId orderId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(
                    OrderDetailResponse.class,
                    orderJpaEntity.id,
                    orderJpaEntity.userId,
                    orderJpaEntity.orderNumber,
                    orderJpaEntity.status,
                    orderJpaEntity.totalAmount,
                    orderJpaEntity.createdAt,
                    orderJpaEntity.updatedAt
                ))
                .from(orderJpaEntity)
                .where(
                    orderJpaEntity.id.eq(orderId.getValue()),
                    orderJpaEntity.deletedAt.isNull()  // Soft Delete 제외
                )
                .fetchOne()
        );
    }
}
```

**핵심**:
- `Projections.constructor()`: DTO 생성자 호출
- `fetchOne()`: 단일 결과 (null 가능)
- `deletedAt.isNull()`: Soft Delete 제외

---

## 🔗 Join을 포함한 DTO Projection

### 1:N Join (Order + OrderItems)

```java
@Override
public Optional<OrderWithItemsResponse> loadWithItems(OrderId orderId) {
    return Optional.ofNullable(
        queryFactory
            .select(Projections.constructor(
                OrderWithItemsResponse.class,
                orderJpaEntity.id,
                orderJpaEntity.userId,
                orderJpaEntity.orderNumber,
                Projections.list(
                    Projections.constructor(
                        OrderItemResponse.class,
                        orderItemJpaEntity.id,
                        orderItemJpaEntity.productId,
                        orderItemJpaEntity.quantity,
                        orderItemJpaEntity.price
                    )
                )
            ))
            .from(orderJpaEntity)
            .leftJoin(orderJpaEntity.orderItems, orderItemJpaEntity)  // Join
            .where(
                orderJpaEntity.id.eq(orderId.getValue()),
                orderJpaEntity.deletedAt.isNull()
            )
            .fetchOne()
    );
}
```

### Many-to-One Join (Order + Customer)

```java
@Override
public Optional<OrderDetailResponse> loadWithCustomer(OrderId orderId) {
    return Optional.ofNullable(
        queryFactory
            .select(Projections.constructor(
                OrderDetailResponse.class,
                orderJpaEntity.id,
                orderJpaEntity.userId,
                orderJpaEntity.orderNumber,
                customerJpaEntity.name,  // Customer 정보 포함
                customerJpaEntity.email
            ))
            .from(orderJpaEntity)
            .join(customerJpaEntity)
            .on(orderJpaEntity.userId.eq(customerJpaEntity.id))  // Long FK Join
            .where(
                orderJpaEntity.id.eq(orderId.getValue()),
                orderJpaEntity.deletedAt.isNull()
            )
            .fetchOne()
    );
}
```

**💡 포인트**:
- Long FK로 Join (`on(order.userId.eq(customer.id))`)
- N+1 문제 방지 (한 번의 Query로 조회)

---

## 📋 목록 조회 패턴

### 기본 목록 조회

```java
@Override
public List<OrderSummaryResponse> loadByCustomerId(CustomerId customerId) {
    return queryFactory
        .select(Projections.constructor(
            OrderSummaryResponse.class,
            orderJpaEntity.id,
            orderJpaEntity.orderNumber,
            orderJpaEntity.status,
            orderJpaEntity.totalAmount,
            orderJpaEntity.createdAt
        ))
        .from(orderJpaEntity)
        .where(
            orderJpaEntity.userId.eq(customerId.getValue()),
            orderJpaEntity.deletedAt.isNull()
        )
        .orderBy(orderJpaEntity.createdAt.desc())
        .fetch();
}
```

### 페이징 조회

```java
@Override
public Page<OrderSummaryResponse> loadAll(Pageable pageable) {
    // 1. 데이터 조회
    List<OrderSummaryResponse> content = queryFactory
        .select(Projections.constructor(
            OrderSummaryResponse.class,
            orderJpaEntity.id,
            orderJpaEntity.orderNumber,
            orderJpaEntity.status,
            orderJpaEntity.totalAmount,
            orderJpaEntity.createdAt
        ))
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .orderBy(orderJpaEntity.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 2. 총 개수 조회 (Count Query)
    Long total = queryFactory
        .select(orderJpaEntity.count())
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
}
```

---

## 🔍 동적 쿼리 (BooleanBuilder)

### 검색 조건 동적 생성

```java
@Override
public List<OrderSummaryResponse> search(OrderSearchCriteria criteria) {
    BooleanBuilder builder = new BooleanBuilder();

    // Soft Delete 제외 (필수)
    builder.and(orderJpaEntity.deletedAt.isNull());

    // 동적 조건 추가
    if (criteria.userId() != null) {
        builder.and(orderJpaEntity.userId.eq(criteria.userId()));
    }

    if (criteria.status() != null) {
        builder.and(orderJpaEntity.status.eq(criteria.status()));
    }

    if (criteria.fromDate() != null) {
        builder.and(orderJpaEntity.createdAt.goe(criteria.fromDate()));
    }

    if (criteria.toDate() != null) {
        builder.and(orderJpaEntity.createdAt.loe(criteria.toDate()));
    }

    return queryFactory
        .select(Projections.constructor(
            OrderSummaryResponse.class,
            orderJpaEntity.id,
            orderJpaEntity.orderNumber,
            orderJpaEntity.status,
            orderJpaEntity.totalAmount,
            orderJpaEntity.createdAt
        ))
        .from(orderJpaEntity)
        .where(builder)
        .orderBy(orderJpaEntity.createdAt.desc())
        .fetch();
}
```

---

## 🧪 Query Adapter 테스트

### 단위 테스트 (@DataJpaTest)

```java
@DataJpaTest
@Import({OrderQueryAdapter.class, JPAQueryFactory.class})
@Tag("unit")
@Tag("query")
class OrderQueryAdapterTest {

    @Autowired
    private OrderQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loadById_WithExistingOrder_ShouldReturnDTO() {
        // Given - Entity 직접 저장
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // When - DTO 직접 조회
        Optional<OrderDetailResponse> result =
            queryAdapter.loadById(OrderId.of(entity.getId()));

        // Then - DTO 검증
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(entity.getId());
        assertThat(result.get().orderNumber()).isEqualTo("ORDER-001");
    }

    @Test
    void loadById_WithDeletedOrder_ShouldReturnEmpty() {
        // Given - Soft Delete된 Entity
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.markAsDeleted();
        entityManager.persist(entity);
        entityManager.flush();

        // When
        Optional<OrderDetailResponse> result =
            queryAdapter.loadById(OrderId.of(entity.getId()));

        // Then - 조회 안 됨
        assertThat(result).isEmpty();
    }

    @Test
    void loadByCustomerId_ShouldReturnList() {
        // Given - 여러 Order 저장
        OrderJpaEntity order1 = OrderJpaEntity.create(100L, "ORDER-001");
        OrderJpaEntity order2 = OrderJpaEntity.create(100L, "ORDER-002");
        OrderJpaEntity order3 = OrderJpaEntity.create(200L, "ORDER-003");

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();

        // When
        List<OrderSummaryResponse> results =
            queryAdapter.loadByCustomerId(CustomerId.of(100L));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(OrderSummaryResponse::orderNumber)
            .containsExactly("ORDER-002", "ORDER-001");  // createdAt desc
    }
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Entity 조회 후 DTO 변환 (비효율)
public Optional<OrderDetailResponse> loadById(OrderId id) {
    OrderJpaEntity entity = entityManager.find(OrderJpaEntity.class, id.getValue());
    return Optional.ofNullable(entity)
        .map(e -> new OrderDetailResponse(
            e.getId(), e.getUserId(), ...
        ));
}

// ❌ Domain Model 거침 (Pure CQRS 위반)
public Optional<OrderDetailResponse> loadById(OrderId id) {
    Order order = commandAdapter.load(id);  // Domain 조회
    return Optional.of(OrderDetailResponse.from(order));  // DTO 변환
}

// ❌ Soft Delete 체크 없음
public Optional<OrderDetailResponse> loadById(OrderId id) {
    return Optional.ofNullable(
        queryFactory.select(...)
            .from(orderJpaEntity)
            .where(orderJpaEntity.id.eq(id.getValue()))
            // deletedAt 체크 없음!
            .fetchOne()
    );
}

// ❌ N+1 문제 발생 (Join 없음)
public List<OrderDetailResponse> loadAll() {
    List<OrderJpaEntity> orders = queryFactory
        .selectFrom(orderJpaEntity)
        .fetch();

    return orders.stream()
        .map(order -> {
            Customer customer = customerRepository.findById(order.getUserId());  // N+1!
            return new OrderDetailResponse(...);
        })
        .toList();
}
```

### ✅ Good Examples

```java
// ✅ QueryDSL DTO Projection
public Optional<OrderDetailResponse> loadById(OrderId id) {
    return Optional.ofNullable(
        queryFactory
            .select(Projections.constructor(
                OrderDetailResponse.class,
                orderJpaEntity.id,
                orderJpaEntity.userId,
                orderJpaEntity.orderNumber
            ))
            .from(orderJpaEntity)
            .where(
                orderJpaEntity.id.eq(id.getValue()),
                orderJpaEntity.deletedAt.isNull()  // ✅ Soft Delete 체크
            )
            .fetchOne()
    );
}

// ✅ Join으로 N+1 방지
public Optional<OrderDetailResponse> loadWithCustomer(OrderId id) {
    return Optional.ofNullable(
        queryFactory
            .select(Projections.constructor(
                OrderDetailResponse.class,
                orderJpaEntity.id,
                customerJpaEntity.name  // Join으로 한 번에 조회
            ))
            .from(orderJpaEntity)
            .join(customerJpaEntity)
            .on(orderJpaEntity.userId.eq(customerJpaEntity.id))
            .where(orderJpaEntity.id.eq(id.getValue()))
            .fetchOne()
    );
}
```

---

## 📋 체크리스트

QueryDSL DTO Projection 작성 시:
- [ ] `Projections.constructor()` 사용
- [ ] DTO Record 패턴
- [ ] `deletedAt.isNull()` 조건 포함
- [ ] Join으로 N+1 방지
- [ ] `@DataJpaTest` + `@Tag("query")` 테스트
- [ ] Entity 조회 후 DTO 변환 금지
- [ ] Domain Model 거치지 않음
- [ ] 페이징 시 Count Query 별도 실행

---

## 📖 관련 문서

- **[Load Port Pattern](./01_load-port-pattern.md)** - LoadOrderPort 인터페이스
- **[Query Adapter Implementation](./03_query-adapter-implementation.md)** - Query Adapter 전체 구조
- **[Query Performance Optimization](./04_query-performance-optimization.md)** - N+1 문제, Batch Fetch
- **[Long FK Strategy](../jpa-entity-design/01_long-fk-strategy.md)** - Long FK Join 패턴

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
