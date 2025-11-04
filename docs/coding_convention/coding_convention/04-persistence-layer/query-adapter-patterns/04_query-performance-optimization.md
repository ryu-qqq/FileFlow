# Query Performance Optimization (쿼리 성능 최적화)

**목적**: QueryDSL Query 성능 최적화 패턴 정의

**위치**: `adapter-persistence/[module]/adapter/`

**필수 버전**: Java 21+, QueryDSL 5.0+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### Query 성능 최적화 전략

Pure CQRS에서는 **DTO 직접 조회**로 대부분의 성능 문제를 해결하지만, 추가 최적화가 필요한 경우가 있습니다:

```
1. N+1 문제 방지 → Join + DTO Projection
2. 페이징 최적화 → Count Query 분리
3. Index 활용 → @Index 어노테이션
4. Soft Delete 최적화 → deletedAt Index
5. 동적 Query 최적화 → BooleanBuilder 캐싱
```

---

## 🚨 N+1 문제 해결

### ❌ Bad: N+1 문제 발생

```java
// ❌ Entity 조회 후 연관 데이터 개별 조회 (N+1 발생)
public List<OrderDetailResponse> loadAllWithCustomer() {
    List<OrderJpaEntity> orders = queryFactory
        .selectFrom(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .fetch();

    return orders.stream()
        .map(order -> {
            // 각 Order마다 Customer 조회 (N+1 문제!)
            CustomerJpaEntity customer = queryFactory
                .selectFrom(customerJpaEntity)
                .where(customerJpaEntity.id.eq(order.getUserId()))
                .fetchOne();

            return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                customer.getName(),
                customer.getEmail()
            );
        })
        .toList();
}

// 실행된 Query:
// 1. SELECT * FROM orders WHERE deleted_at IS NULL  (1개 Query)
// 2. SELECT * FROM customers WHERE id = ?  (N개 Query)
// 총 N+1개 Query!
```

### ✅ Good: Join으로 한 번에 조회

```java
// ✅ Join + DTO Projection으로 N+1 방지
public List<OrderDetailResponse> loadAllWithCustomer() {
    return queryFactory
        .select(Projections.constructor(
            OrderDetailResponse.class,
            orderJpaEntity.id,
            orderJpaEntity.orderNumber,
            customerJpaEntity.name,
            customerJpaEntity.email
        ))
        .from(orderJpaEntity)
        .join(customerJpaEntity)
        .on(orderJpaEntity.userId.eq(customerJpaEntity.id))
        .where(
            orderJpaEntity.deletedAt.isNull(),
            customerJpaEntity.deletedAt.isNull()
        )
        .fetch();
}

// 실행된 Query:
// SELECT o.id, o.order_number, c.name, c.email
// FROM orders o
// JOIN customers c ON o.user_id = c.id
// WHERE o.deleted_at IS NULL AND c.deleted_at IS NULL
// 총 1개 Query!
```

---

## 📊 페이징 최적화

### ❌ Bad: Count Query 최적화 없음

```java
// ❌ fetchResults() 사용 (Deprecated, 비효율)
public Page<OrderSummaryResponse> loadAll(Pageable pageable) {
    QueryResults<OrderSummaryResponse> results = queryFactory
        .select(Projections.constructor(
            OrderSummaryResponse.class,
            orderJpaEntity.id,
            orderJpaEntity.orderNumber
        ))
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();  // Deprecated!

    return new PageImpl<>(
        results.getResults(),
        pageable,
        results.getTotal()
    );
}
```

### ✅ Good: Count Query 분리 + 최적화

```java
// ✅ Count Query 분리 (성능 최적화)
public Page<OrderSummaryResponse> loadAll(Pageable pageable) {
    // 1. 데이터 조회 Query
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

    // 2. Count Query (필요한 경우에만 실행)
    Long total = queryFactory
        .select(orderJpaEntity.count())
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
}

// 또는 조건부 Count Query (첫 페이지는 Count 생략 가능)
public Page<OrderSummaryResponse> loadAllOptimized(Pageable pageable) {
    List<OrderSummaryResponse> content = queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 첫 페이지이고 결과가 pageSize보다 작으면 Count Query 생략
    if (pageable.getOffset() == 0 && content.size() < pageable.getPageSize()) {
        return new PageImpl<>(content, pageable, content.size());
    }

    // 그 외에는 Count Query 실행
    Long total = queryFactory
        .select(orderJpaEntity.count())
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())
        .fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
}
```

---

## 🔍 Index 활용

### Entity에 Index 정의

```java
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_orders_created_at", columnList = "created_at"),
        @Index(name = "idx_orders_user_status", columnList = "user_id, status")
    }
)
public class OrderJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // ...
}
```

### Index 활용 Query

```java
// ✅ userId Index 활용
public List<OrderSummaryResponse> loadByCustomerId(CustomerId customerId) {
    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(
            orderJpaEntity.userId.eq(customerId.getValue()),  // Index 활용
            orderJpaEntity.deletedAt.isNull()                // Index 활용
        )
        .fetch();
}

// ✅ Composite Index 활용
public List<OrderSummaryResponse> loadByCustomerIdAndStatus(
    CustomerId customerId,
    OrderStatus status
) {
    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(
            orderJpaEntity.userId.eq(customerId.getValue()),
            orderJpaEntity.status.eq(status),  // Composite Index: (user_id, status)
            orderJpaEntity.deletedAt.isNull()
        )
        .fetch();
}
```

---

## 🗂️ Soft Delete 최적화

### deletedAt Index 활용

```sql
-- ✅ deletedAt Index 생성 (Filtered Index)
CREATE INDEX idx_orders_deleted_at ON orders (deleted_at);

-- Query 실행 시 Index 활용
SELECT * FROM orders
WHERE deleted_at IS NULL;  -- Index Scan
```

### Query 패턴

```java
// ✅ deletedAt IS NULL 조건은 항상 포함 (Index 활용)
public List<OrderSummaryResponse> loadAll() {
    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(orderJpaEntity.deletedAt.isNull())  // Index 활용
        .fetch();
}

// ✅ deletedAt 조건 + 다른 조건 (Composite Index 활용 가능)
public List<OrderSummaryResponse> loadByStatus(OrderStatus status) {
    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(
            orderJpaEntity.status.eq(status),
            orderJpaEntity.deletedAt.isNull()  // Index 활용
        )
        .fetch();
}
```

---

## 🔄 동적 Query 최적화

### BooleanBuilder 재사용

```java
// ✅ 공통 조건 메서드로 분리
private BooleanBuilder buildBaseConditions() {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(orderJpaEntity.deletedAt.isNull());  // 필수 조건
    return builder;
}

public List<OrderSummaryResponse> search(OrderSearchCriteria criteria) {
    BooleanBuilder builder = buildBaseConditions();

    // 동적 조건 추가
    if (criteria.userId() != null) {
        builder.and(orderJpaEntity.userId.eq(criteria.userId()));
    }

    if (criteria.status() != null) {
        builder.and(orderJpaEntity.status.eq(criteria.status()));
    }

    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(builder)
        .fetch();
}
```

---

## 📊 Query 성능 측정

### 실행 계획 확인 (EXPLAIN)

```java
// MySQL EXPLAIN 실행
@Test
void explainQuery() {
    String sql = queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(
            orderJpaEntity.userId.eq(100L),
            orderJpaEntity.deletedAt.isNull()
        )
        .getSQL();

    // EXPLAIN 실행
    List<Map<String, Object>> explainResult = jdbcTemplate.queryForList(
        "EXPLAIN " + sql
    );

    // Index 사용 확인
    explainResult.forEach(row -> {
        System.out.println("type: " + row.get("type"));
        System.out.println("possible_keys: " + row.get("possible_keys"));
        System.out.println("key: " + row.get("key"));
        System.out.println("rows: " + row.get("rows"));
    });
}
```

### Hibernate Query Log

```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ N+1 문제 발생
public List<OrderDetailResponse> loadAll() {
    List<OrderJpaEntity> orders = queryFactory
        .selectFrom(orderJpaEntity)
        .fetch();

    return orders.stream()
        .map(order -> {
            CustomerJpaEntity customer = queryFactory
                .selectFrom(customerJpaEntity)
                .where(customerJpaEntity.id.eq(order.getUserId()))
                .fetchOne();  // N+1!

            return new OrderDetailResponse(...);
        })
        .toList();
}

// ❌ Index 미사용 (LIKE %term%)
public List<OrderSummaryResponse> searchByOrderNumber(String term) {
    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(orderJpaEntity.orderNumber.like("%" + term + "%"))  // Index 미사용!
        .fetch();
}

// ❌ Count Query 최적화 없음
public Page<OrderSummaryResponse> loadAll(Pageable pageable) {
    return new PageImpl<>(
        queryFactory.select(...).fetch(),
        pageable,
        queryFactory.select(orderJpaEntity.count()).fetchOne()  // 매번 실행!
    );
}
```

### ✅ Good Examples

```java
// ✅ Join으로 N+1 방지
public List<OrderDetailResponse> loadAll() {
    return queryFactory
        .select(Projections.constructor(
            OrderDetailResponse.class,
            orderJpaEntity.id,
            customerJpaEntity.name
        ))
        .from(orderJpaEntity)
        .join(customerJpaEntity)
        .on(orderJpaEntity.userId.eq(customerJpaEntity.id))
        .fetch();
}

// ✅ Index 활용 (term%)
public List<OrderSummaryResponse> searchByOrderNumber(String term) {
    return queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .where(orderJpaEntity.orderNumber.startsWith(term))  // Index 활용!
        .fetch();
}

// ✅ Count Query 조건부 실행
public Page<OrderSummaryResponse> loadAll(Pageable pageable) {
    List<OrderSummaryResponse> content = queryFactory
        .select(Projections.constructor(...))
        .from(orderJpaEntity)
        .fetch();

    if (pageable.getOffset() == 0 && content.size() < pageable.getPageSize()) {
        return new PageImpl<>(content, pageable, content.size());
    }

    Long total = queryFactory.select(orderJpaEntity.count()).fetchOne();
    return new PageImpl<>(content, pageable, total != null ? total : 0L);
}
```

---

## 📋 체크리스트

Query 성능 최적화 시:
- [ ] N+1 문제 방지 (Join + DTO Projection)
- [ ] Count Query 분리 및 조건부 실행
- [ ] Index 정의 (`@Index`)
- [ ] deletedAt Index 활용
- [ ] LIKE 패턴 최적화 (`startsWith()`)
- [ ] BooleanBuilder 재사용
- [ ] EXPLAIN으로 실행 계획 확인
- [ ] Hibernate Query Log 활성화

---

## 📖 관련 문서

- **[QueryDSL DTO Projection](./02_querydsl-dto-projection.md)** - DTO Projection 기본
- **[Query Adapter Implementation](./03_query-adapter-implementation.md)** - Query Adapter 구현
- **[Long FK Strategy](../jpa-entity-design/01_long-fk-strategy.md)** - Long FK Join 패턴
- **[JPA Entity Design](../jpa-entity-design/00_jpa-entity-core-rules.md)** - Entity Index 정의

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
