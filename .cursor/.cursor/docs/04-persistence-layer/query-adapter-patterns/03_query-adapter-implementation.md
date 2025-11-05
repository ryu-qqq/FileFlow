# Query Adapter Implementation (쿼리 어댑터 구현 패턴)

**목적**: Pure CQRS Query Adapter의 전체 구현 패턴 정의

**위치**: `adapter-persistence/[module]/adapter/`

**필수 버전**: Java 21+, QueryDSL 5.0+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### Query Adapter 책임

Query Adapter는 **QueryDSL을 사용하여 DTO를 직접 조회**하는 역할만 담당합니다:

```
Application Layer (Port Interface)
    ↓
LoadOrderPort
    ↓ 구현
OrderQueryAdapter (@Component)
    ↓ 사용
JPAQueryFactory
    ↓ 호출
Projections.constructor()
    ↓ 반환
OrderDetailResponse (DTO)
```

**규칙**:
- ✅ `@Component` 어노테이션
- ✅ Query Port 인터페이스 구현
- ✅ JPAQueryFactory 사용
- ✅ DTO 직접 반환 (Domain 변환 없음)
- ❌ JpaRepository 사용 금지 (Command Adapter에서 사용)
- ❌ Entity 조회 후 DTO 변환 금지 (성능 저하)
- ❌ Command 메서드 없음 (저장/삭제는 Command Adapter)

---

## 📦 Query Adapter 전체 구조

### 기본 구현

```java
package com.company.adapter.out.persistence.order.adapter;

import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.dto.response.OrderSummaryResponse;
import com.company.application.order.port.out.LoadOrderPort;
import com.company.domain.order.OrderId;
import com.company.domain.order.CustomerId;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.company.adapter.out.persistence.order.entity.QOrderJpaEntity.orderJpaEntity;

/**
 * Order Query Adapter (Pure CQRS - DTO 직접 조회)
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
                    orderJpaEntity.deletedAt.isNull()
                )
                .fetchOne()
        );
    }

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

    @Override
    public Page<OrderSummaryResponse> loadAll(Pageable pageable) {
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

        Long total = queryFactory
            .select(orderJpaEntity.count())
            .from(orderJpaEntity)
            .where(orderJpaEntity.deletedAt.isNull())
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
```

---

## 🔧 JPAQueryFactory 설정

### Configuration 클래스

```java
package com.company.adapter.out.persistence.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL Configuration
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

---

## 📋 복잡한 Query 패턴

### 여러 테이블 Join

```java
@Override
public Optional<OrderDetailWithCustomerResponse> loadWithCustomer(OrderId orderId) {
    return Optional.ofNullable(
        queryFactory
            .select(Projections.constructor(
                OrderDetailWithCustomerResponse.class,
                orderJpaEntity.id,
                orderJpaEntity.orderNumber,
                orderJpaEntity.status,
                orderJpaEntity.totalAmount,
                customerJpaEntity.name,
                customerJpaEntity.email,
                customerJpaEntity.phone
            ))
            .from(orderJpaEntity)
            .join(customerJpaEntity)
            .on(orderJpaEntity.userId.eq(customerJpaEntity.id))
            .where(
                orderJpaEntity.id.eq(orderId.getValue()),
                orderJpaEntity.deletedAt.isNull(),
                customerJpaEntity.deletedAt.isNull()
            )
            .fetchOne()
    );
}
```

### 집계 함수 사용

```java
@Override
public OrderStatisticsResponse loadStatistics(CustomerId customerId) {
    return queryFactory
        .select(Projections.constructor(
            OrderStatisticsResponse.class,
            orderJpaEntity.count(),
            orderJpaEntity.totalAmount.sum(),
            orderJpaEntity.totalAmount.avg()
        ))
        .from(orderJpaEntity)
        .where(
            orderJpaEntity.userId.eq(customerId.getValue()),
            orderJpaEntity.deletedAt.isNull()
        )
        .fetchOne();
}
```

### Subquery 사용

```java
@Override
public List<OrderSummaryResponse> loadLargeOrders() {
    QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;

    return queryFactory
        .select(Projections.constructor(
            OrderSummaryResponse.class,
            order.id,
            order.orderNumber,
            order.status,
            order.totalAmount,
            order.createdAt
        ))
        .from(order)
        .where(
            order.totalAmount.gt(
                JPAExpressions
                    .select(order.totalAmount.avg())
                    .from(order)
                    .where(order.deletedAt.isNull())
            ),
            order.deletedAt.isNull()
        )
        .fetch();
}
```

---

## 🔍 동적 쿼리 패턴

### BooleanBuilder 사용

```java
@Override
public List<OrderSummaryResponse> search(OrderSearchCriteria criteria) {
    BooleanBuilder builder = new BooleanBuilder();

    // 필수 조건
    builder.and(orderJpaEntity.deletedAt.isNull());

    // 동적 조건
    if (criteria.userId() != null) {
        builder.and(orderJpaEntity.userId.eq(criteria.userId()));
    }

    if (criteria.status() != null) {
        builder.and(orderJpaEntity.status.eq(criteria.status()));
    }

    if (criteria.minAmount() != null) {
        builder.and(orderJpaEntity.totalAmount.goe(criteria.minAmount()));
    }

    if (criteria.maxAmount() != null) {
        builder.and(orderJpaEntity.totalAmount.loe(criteria.maxAmount()));
    }

    if (criteria.fromDate() != null && criteria.toDate() != null) {
        builder.and(orderJpaEntity.createdAt.between(
            criteria.fromDate(),
            criteria.toDate()
        ));
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
@Import({OrderQueryAdapter.class, QueryDslConfig.class})
@Tag("unit")
@Tag("query")
class OrderQueryAdapterTest {

    @Autowired
    private OrderQueryAdapter queryAdapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loadById_WithExistingOrder_ShouldReturnDTO() {
        // Given
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.setTotalAmount(BigDecimal.valueOf(10000));
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<OrderDetailResponse> result =
            queryAdapter.loadById(OrderId.of(entity.getId()));

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(entity.getId());
        assertThat(result.get().orderNumber()).isEqualTo("ORDER-001");
        assertThat(result.get().totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    void loadById_WithDeletedOrder_ShouldReturnEmpty() {
        // Given
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.markAsDeleted();
        entityManager.persist(entity);
        entityManager.flush();

        // When
        Optional<OrderDetailResponse> result =
            queryAdapter.loadById(OrderId.of(entity.getId()));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void loadByCustomerId_WithMultipleOrders_ShouldReturnList() {
        // Given
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
        assertThat(results)
            .extracting(OrderSummaryResponse::orderNumber)
            .containsExactly("ORDER-002", "ORDER-001");
    }

    @Test
    void loadAll_WithPageable_ShouldReturnPage() {
        // Given
        for (int i = 1; i <= 15; i++) {
            OrderJpaEntity order = OrderJpaEntity.create(100L, "ORDER-" + String.format("%03d", i));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderSummaryResponse> page = queryAdapter.loadAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ JpaRepository 사용 (Command Adapter에서 사용)
@Component
public class OrderQueryAdapter {
    private final OrderJpaRepository jpaRepository;  // Query Adapter는 JPAQueryFactory만!
}

// ❌ Entity 조회 후 DTO 변환 (비효율)
public Optional<OrderDetailResponse> loadById(OrderId id) {
    OrderJpaEntity entity = queryFactory
        .selectFrom(orderJpaEntity)
        .where(orderJpaEntity.id.eq(id.getValue()))
        .fetchOne();

    return Optional.ofNullable(entity)
        .map(e -> new OrderDetailResponse(...));  // 불필요한 변환!
}

// ❌ Command 메서드 포함 (CQRS 위반)
@Component
public class OrderQueryAdapter implements LoadOrderPort {
    public Optional<OrderDetailResponse> loadById(OrderId id) { ... }
    public Order save(Order order) { ... }  // Command는 Command Adapter로!
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
```

### ✅ Good Examples

```java
// ✅ JPAQueryFactory만 사용
@Component
public class OrderQueryAdapter implements LoadOrderPort {
    private final JPAQueryFactory queryFactory;
}

// ✅ Projections.constructor()로 DTO 직접 생성
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

// ✅ Query만 담당
@Component
public class OrderQueryAdapter implements LoadOrderPort {
    public Optional<OrderDetailResponse> loadById(OrderId id) { ... }
    public List<OrderSummaryResponse> loadByCustomerId(CustomerId id) { ... }
    // Command 메서드 없음
}
```

---

## 📐 Query Adapter 설계 규칙

### 1. 단일 책임 원칙 (SRP)

```java
// ✅ Good - Query만 담당
@Component
public class OrderQueryAdapter implements LoadOrderPort {
    // loadById(), loadByCustomerId() 등
}

// ❌ Bad - Query + Command 혼재
@Component
public class OrderAdapter implements LoadOrderPort, SaveOrderPort {
    // CQRS 위반!
}
```

### 2. JPAQueryFactory만 사용

```java
// ✅ Good
@Component
public class OrderQueryAdapter {
    private final JPAQueryFactory queryFactory;
}

// ❌ Bad - JpaRepository 혼재
@Component
public class OrderQueryAdapter {
    private final JPAQueryFactory queryFactory;
    private final OrderJpaRepository jpaRepository;  // 금지!
}
```

### 3. Soft Delete 체크 일관성

```java
// ✅ Good - 모든 Query에 deletedAt 체크
.where(
    orderJpaEntity.id.eq(id.getValue()),
    orderJpaEntity.deletedAt.isNull()  // 필수
)

// ❌ Bad - deletedAt 체크 누락
.where(orderJpaEntity.id.eq(id.getValue()))
```

---

## 📋 체크리스트

Query Adapter 작성 시:
- [ ] `@Component` 어노테이션
- [ ] LoadOrderPort 구현
- [ ] JPAQueryFactory 의존성 주입
- [ ] `Projections.constructor()` 사용
- [ ] `deletedAt.isNull()` 조건 포함
- [ ] Command 메서드 없음
- [ ] JpaRepository 사용 안 함
- [ ] `@DataJpaTest` + `@Tag("query")` 테스트

---

## 📖 관련 문서

- **[Load Port Pattern](./01_load-port-pattern.md)** - LoadOrderPort 인터페이스
- **[QueryDSL DTO Projection](./02_querydsl-dto-projection.md)** - DTO Projection 상세
- **[Query Performance Optimization](./04_query-performance-optimization.md)** - 성능 최적화
- **[Query Adapter Unit Testing](../testing/02_query-adapter-unit-testing.md)** - 테스트 전략

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
