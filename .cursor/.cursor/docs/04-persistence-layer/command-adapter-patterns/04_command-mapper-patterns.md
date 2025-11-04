# Command Mapper Patterns (커맨드 매퍼 패턴)

**목적**: Domain Model과 JPA Entity 간 변환 패턴 정의

**위치**: `adapter-persistence/[module]/mapper/`

**필수 버전**: Java 21+, Spring Boot 3.0+, MapStruct 1.5+

---

## 🎯 핵심 원칙

### Mapper 책임

Mapper는 **Domain Model ↔ JPA Entity 양방향 변환**을 담당합니다:

```
Domain Layer              Persistence Layer
    ↓                           ↓
Order (Domain Model)    ←→  OrderJpaEntity
    ↓                           ↓
OrderId, UserId, etc.   ←→  Long id, Long userId, etc.
```

**규칙**:
- ✅ MapStruct 사용 (컴파일 타임 코드 생성)
- ✅ Value Object ↔ Primitive 변환
- ✅ 양방향 변환 (`toEntity`, `toDomain`)
- ❌ 비즈니스 로직 없음
- ❌ Lombok `@Builder` 금지 (MapStruct 충돌)

---

## 📦 Mapper 인터페이스

### 기본 패턴

```java
package com.company.adapter.out.persistence.order.mapper;

import com.company.domain.order.Order;
import com.company.domain.order.OrderId;
import com.company.domain.order.UserId;
import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Order Entity Mapper
 *
 * Domain Model ↔ JPA Entity 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapper {

    /**
     * Domain Model → JPA Entity 변환
     *
     * @param order Domain Model
     * @return JPA Entity
     */
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "orderNumber", source = "orderNumber.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", source = "totalAmount.value")
    OrderJpaEntity toEntity(Order order);

    /**
     * JPA Entity → Domain Model 변환
     *
     * @param entity JPA Entity
     * @return Domain Model
     */
    @Mapping(target = "id", expression = "java(OrderId.of(entity.getId()))")
    @Mapping(target = "userId", expression = "java(UserId.of(entity.getUserId()))")
    @Mapping(target = "orderNumber", expression = "java(OrderNumber.of(entity.getOrderNumber()))")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", expression = "java(Money.of(entity.getTotalAmount()))")
    Order toDomain(OrderJpaEntity entity);
}
```

---

## 🔄 Value Object 변환 패턴

### Primitive → Value Object

```java
// ID 변환
@Mapping(target = "id", expression = "java(OrderId.of(entity.getId()))")

// UserId 변환
@Mapping(target = "userId", expression = "java(UserId.of(entity.getUserId()))")

// Money 변환
@Mapping(target = "totalAmount", expression = "java(Money.of(entity.getTotalAmount()))")

// OrderNumber 변환
@Mapping(target = "orderNumber", expression = "java(OrderNumber.of(entity.getOrderNumber()))")
```

### Value Object → Primitive

```java
// ID 변환
@Mapping(target = "id", source = "id.value")

// UserId 변환
@Mapping(target = "userId", source = "userId.value")

// Money 변환
@Mapping(target = "totalAmount", source = "totalAmount.value")

// OrderNumber 변환
@Mapping(target = "orderNumber", source = "orderNumber.value")
```

---

## 📋 Enum 변환

### 동일한 Enum 사용 (권장)

```java
// Domain과 Persistence Layer에서 동일한 Enum 공유
@Mapping(target = "status", source = "status")  // 자동 변환
```

```java
// Enum 정의 (domain/order/OrderStatus.java)
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

// Entity에서 사용
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private OrderStatus status;
```

### 다른 Enum 사용 (특수 케이스)

```java
// Domain Enum → Persistence Enum 변환 필요 시
@Mapping(target = "status", expression = "java(toEntityStatus(order.getStatus()))")

default OrderEntityStatus toEntityStatus(OrderStatus domainStatus) {
    return switch (domainStatus) {
        case PENDING -> OrderEntityStatus.PENDING;
        case CONFIRMED -> OrderEntityStatus.CONFIRMED;
        case SHIPPED -> OrderEntityStatus.SHIPPED;
        case DELIVERED -> OrderEntityStatus.DELIVERED;
        case CANCELLED -> OrderEntityStatus.CANCELLED;
    };
}
```

---

## 🗂️ Collection 변환

### List<OrderItem> 변환

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapper {

    @Mapping(target = "orderItems", source = "orderItems")
    OrderJpaEntity toEntity(Order order);

    @Mapping(target = "orderItems", source = "orderItems")
    Order toDomain(OrderJpaEntity entity);

    // Collection 변환 메서드
    List<OrderItemJpaEntity> toItemEntities(List<OrderItem> items);
    List<OrderItem> toItemDomains(List<OrderItemJpaEntity> entities);

    // 개별 Item 변환
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantity", source = "quantity.value")
    OrderItemJpaEntity toItemEntity(OrderItem item);

    @Mapping(target = "id", expression = "java(OrderItemId.of(entity.getId()))")
    @Mapping(target = "productId", expression = "java(ProductId.of(entity.getProductId()))")
    @Mapping(target = "quantity", expression = "java(Quantity.of(entity.getQuantity()))")
    OrderItem toItemDomain(OrderItemJpaEntity entity);
}
```

---

## 🔍 Null 처리

### Null-Safe 변환

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderEntityMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    OrderJpaEntity toEntity(Order order);

    // Null 처리 예시
    default Long mapOrderId(OrderId orderId) {
        return orderId != null ? orderId.getValue() : null;
    }

    default OrderId mapIdToOrderId(Long id) {
        return id != null ? OrderId.of(id) : null;
    }
}
```

---

## 🧪 Mapper 테스트

### 단위 테스트

```java
@SpringBootTest
@Tag("unit")
class OrderEntityMapperTest {

    @Autowired
    private OrderEntityMapper mapper;

    @Test
    void toEntity_WithDomainModel_ShouldConvertCorrectly() {
        // Given - Domain Model
        Order order = Order.create(
            UserId.of(100L),
            OrderItems.of(
                OrderItem.of(ProductId.of(1L), Quantity.of(2))
            )
        );

        // When - Domain → Entity
        OrderJpaEntity entity = mapper.toEntity(order);

        // Then
        assertThat(entity.getUserId()).isEqualTo(100L);
        assertThat(entity.getOrderItems()).hasSize(1);
        assertThat(entity.getOrderItems().get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void toDomain_WithEntity_ShouldConvertCorrectly() {
        // Given - Entity
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.setId(1L);

        // When - Entity → Domain
        Order order = mapper.toDomain(entity);

        // Then
        assertThat(order.getId().getValue()).isEqualTo(1L);
        assertThat(order.getUserId().getValue()).isEqualTo(100L);
        assertThat(order.getOrderNumber().getValue()).isEqualTo("ORDER-001");
    }

    @Test
    void toEntity_toDomain_ShouldBeReversible() {
        // Given - Domain Model
        Order originalOrder = Order.reconstitute(
            OrderId.of(1L),
            UserId.of(100L),
            OrderNumber.of("ORDER-001"),
            OrderStatus.CONFIRMED,
            Money.of(BigDecimal.valueOf(10000))
        );

        // When - Domain → Entity → Domain
        OrderJpaEntity entity = mapper.toEntity(originalOrder);
        Order reconvertedOrder = mapper.toDomain(entity);

        // Then - 원본과 동일
        assertThat(reconvertedOrder.getId()).isEqualTo(originalOrder.getId());
        assertThat(reconvertedOrder.getUserId()).isEqualTo(originalOrder.getUserId());
        assertThat(reconvertedOrder.getOrderNumber()).isEqualTo(originalOrder.getOrderNumber());
    }
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Lombok @Builder 사용 (MapStruct 충돌)
@Mapper
public interface OrderEntityMapper {
    @Mapping(target = "id", source = "id.value")
    OrderJpaEntity toEntity(Order order);  // Order에 @Builder 있으면 오류!
}

// ❌ 비즈니스 로직 포함
@Mapper
public interface OrderEntityMapper {
    default OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        if (order.getStatus() == OrderStatus.PENDING) {
            entity.setStatus(OrderStatus.CONFIRMED);  // 비즈니스 로직 금지!
        }
        return entity;
    }
}

// ❌ Exception 발생 (Mapper는 단순 변환만)
@Mapper
public interface OrderEntityMapper {
    default OrderJpaEntity toEntity(Order order) {
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("UserId required");  // 금지!
        }
        // ...
    }
}

// ❌ Value Object getValue() 직접 호출
@Mapper
public interface OrderEntityMapper {
    default OrderJpaEntity toEntity(Order order) {
        entity.setId(order.getId().getValue());  // @Mapping 사용!
    }
}
```

### ✅ Good Examples

```java
// ✅ MapStruct 어노테이션 사용
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapper {
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    OrderJpaEntity toEntity(Order order);
}

// ✅ Expression으로 Value Object 변환
@Mapper
public interface OrderEntityMapper {
    @Mapping(target = "id", expression = "java(OrderId.of(entity.getId()))")
    Order toDomain(OrderJpaEntity entity);
}

// ✅ Null-Safe 변환
@Mapper
public interface OrderEntityMapper {
    default Long mapOrderId(OrderId orderId) {
        return orderId != null ? orderId.getValue() : null;
    }
}

// ✅ Collection 변환
@Mapper
public interface OrderEntityMapper {
    List<OrderItemJpaEntity> toItemEntities(List<OrderItem> items);
    List<OrderItem> toItemDomains(List<OrderItemJpaEntity> entities);
}
```

---

## 📐 Mapper 설계 규칙

### 1. MapStruct 설정

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

### 2. Component Model

```java
// ✅ Spring Bean으로 등록
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapper { ... }

// Adapter에서 주입 가능
@Component
public class OrderCommandAdapter {
    private final OrderEntityMapper mapper;
}
```

### 3. 단방향 vs 양방향

```java
// ✅ 양방향 변환 제공
@Mapper
public interface OrderEntityMapper {
    OrderJpaEntity toEntity(Order order);      // Domain → Entity
    Order toDomain(OrderJpaEntity entity);     // Entity → Domain
}

// ❌ 단방향만 (불편)
@Mapper
public interface OrderEntityMapper {
    OrderJpaEntity toEntity(Order order);  // Entity → Domain 불가!
}
```

---

## 📋 체크리스트

Mapper 작성 시:
- [ ] `@Mapper(componentModel = SPRING)`
- [ ] `toEntity()`, `toDomain()` 양방향 변환
- [ ] Value Object 변환 (`expression = "java(...)"`)
- [ ] Enum 변환 (@Mapping 또는 default 메서드)
- [ ] Collection 변환 메서드
- [ ] Null-Safe 변환
- [ ] 비즈니스 로직 없음
- [ ] Exception 없음
- [ ] 단위 테스트 작성

---

## 📖 관련 문서

- **[Command Adapter Implementation](./03_command-adapter-implementation.md)** - Mapper 사용 예시
- **[Save Port Pattern](./01_save-port-pattern.md)** - Domain Model 정의
- **[JPA Entity Design](../jpa-entity-design/00_jpa-entity-core-rules.md)** - Entity 설계 규칙
- **[Domain Value Objects](../../02-domain-layer/aggregate-design/02_value-object-patterns.md)** - Value Object 패턴

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
