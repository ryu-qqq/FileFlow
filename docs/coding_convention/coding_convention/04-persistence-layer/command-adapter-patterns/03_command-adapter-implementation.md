# Command Adapter Implementation (커맨드 어댑터 구현 패턴)

**목적**: CQRS Command Port의 실제 구현 패턴 정의

**위치**: `adapter-persistence/[module]/adapter/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### Command Adapter 책임

Command Adapter는 **Domain Model을 Entity로 변환하여 저장**하는 역할만 담당합니다:

```
Application Layer (Port Interface)
    ↓
SaveOrderPort, DeleteOrderPort
    ↓ 구현
OrderCommandAdapter (@Component)
    ↓ 사용
OrderJpaRepository (JpaRepository)
    ↓ 호출
JpaRepository.save() / delete()
    ↓ 저장
OrderJpaEntity
```

**규칙**:
- ✅ `@Component` 어노테이션
- ✅ Port 인터페이스 구현
- ✅ JpaRepository 사용 (Query 메서드 없음)
- ✅ Domain ↔ Entity 변환 (Mapper 사용)
- ❌ 비즈니스 로직 없음 (Domain에서 처리)
- ❌ Query 메서드 없음 (Query Adapter로 분리)

---

## 📦 Command Adapter 구현

### 기본 구조

```java
package com.company.adapter.out.persistence.order.adapter;

import com.company.application.order.port.out.SaveOrderPort;
import com.company.application.order.port.out.DeleteOrderPort;
import com.company.domain.order.Order;
import com.company.domain.order.OrderId;
import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.company.adapter.out.persistence.order.repository.OrderJpaRepository;
import com.company.adapter.out.persistence.order.mapper.OrderEntityMapper;
import org.springframework.stereotype.Component;

/**
 * Order Command Adapter (저장/삭제 전용)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OrderCommandAdapter implements SaveOrderPort, DeleteOrderPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public OrderCommandAdapter(
        OrderJpaRepository jpaRepository,
        OrderEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        // 1. Domain → Entity 변환
        OrderJpaEntity entity = mapper.toEntity(order);

        // 2. JpaRepository.save() 호출
        OrderJpaEntity savedEntity = jpaRepository.save(entity);

        // 3. Entity → Domain 변환
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void softDelete(OrderId orderId) {
        OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        entity.markAsDeleted();
        jpaRepository.save(entity);
    }

    @Override
    public void restore(OrderId orderId) {
        OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        entity.restore();
        jpaRepository.save(entity);
    }
}
```

---

## 🗂️ JpaRepository 인터페이스

### Command 전용 Repository

```java
package com.company.adapter.out.persistence.order.repository;

import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Order JPA Repository (Command 전용)
 *
 * ✅ save() 메서드만 사용
 * ❌ Query 메서드 정의 금지 (Query Adapter에서 처리)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    // ✅ JpaRepository의 기본 메서드만 사용:
    //    - save(entity)
    //    - findById(id) - Soft Delete 시에만 사용
    //    - delete(entity) - Hard Delete 시에만 사용

    // ❌ Query 메서드 정의 금지!
    // List<OrderJpaEntity> findByUserId(Long userId);  // 금지!
    // List<OrderJpaEntity> findByStatus(OrderStatus status);  // 금지!
}
```

**💡 포인트**:
- Query 메서드는 Query Adapter에서 QueryDSL로 처리
- Command Adapter는 `save()`, `findById()`, `delete()`만 사용

---

## 🔄 Domain ↔ Entity 변환

### Mapper 인터페이스

```java
package com.company.adapter.out.persistence.order.mapper;

import com.company.domain.order.Order;
import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;

/**
 * Order Entity Mapper
 *
 * @author development-team
 * @since 1.0.0
 */
public interface OrderEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * @param order Domain Model
     * @return JPA Entity
     */
    OrderJpaEntity toEntity(Order order);

    /**
     * Entity → Domain 변환
     *
     * @param entity JPA Entity
     * @return Domain Model
     */
    Order toDomain(OrderJpaEntity entity);
}
```

### Mapper 구현 (MapStruct 사용)

```java
package com.company.adapter.out.persistence.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Order Entity Mapper 구현
 *
 * @author development-team
 * @since 1.0.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapperImpl extends OrderEntityMapper {

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "orderNumber", source = "orderNumber.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", source = "totalAmount.value")
    OrderJpaEntity toEntity(Order order);

    @Override
    @Mapping(target = "id", expression = "java(OrderId.of(entity.getId()))")
    @Mapping(target = "userId", expression = "java(UserId.of(entity.getUserId()))")
    @Mapping(target = "orderNumber", expression = "java(OrderNumber.of(entity.getOrderNumber()))")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", expression = "java(Money.of(entity.getTotalAmount()))")
    Order toDomain(OrderJpaEntity entity);
}
```

---

## 📋 신규 저장 vs 수정 저장

### 신규 Order 저장 (ID 없음)

```java
@Override
public Order save(Order order) {
    // Domain에 ID가 없음 → 신규 저장
    if (order.getId() == null) {
        OrderJpaEntity entity = mapper.toEntity(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);  // ID 할당됨
    }

    // Domain에 ID 있음 → 수정 저장
    OrderJpaEntity entity = mapper.toEntity(order);
    OrderJpaEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
}
```

**💡 포인트**:
- `save()` 메서드는 신규/수정 모두 처리
- ID 없으면 `INSERT`, ID 있으면 `UPDATE`
- JPA가 자동 판별

---

## 🧪 Command Adapter 테스트

### 단위 테스트 (@DataJpaTest)

```java
@DataJpaTest
@Import({OrderCommandAdapter.class, OrderEntityMapperImpl.class})
@Tag("unit")
@Tag("command")
class OrderCommandAdapterTest {

    @Autowired
    private OrderCommandAdapter commandAdapter;

    @Autowired
    private OrderJpaRepository jpaRepository;

    @Test
    void save_WithNewOrder_ShouldPersistAndReturnId() {
        // Given - Domain Model (ID 없음)
        Order order = Order.create(
            UserId.of(100L),
            OrderItems.of(
                OrderItem.of(ProductId.of(1L), Quantity.of(2))
            )
        );

        // When - Command Adapter로 저장
        Order savedOrder = commandAdapter.save(order);

        // Then - ID 할당 확인
        assertThat(savedOrder.getId()).isNotNull();

        // DB 검증
        Optional<OrderJpaEntity> entity =
            jpaRepository.findById(savedOrder.getId().getValue());
        assertThat(entity).isPresent();
        assertThat(entity.get().getUserId()).isEqualTo(100L);
    }

    @Test
    void save_WithExistingOrder_ShouldUpdate() {
        // Given - 기존 Order 저장
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity = jpaRepository.save(entity);

        // Domain으로 변환 후 수정
        Order order = Order.reconstitute(
            OrderId.of(entity.getId()),
            UserId.of(100L),
            OrderNumber.of("ORDER-002"),  // 변경
            OrderStatus.CONFIRMED,
            Money.of(BigDecimal.valueOf(10000))
        );

        // When - 저장
        Order savedOrder = commandAdapter.save(order);

        // Then - 수정 확인
        OrderJpaEntity updated =
            jpaRepository.findById(savedOrder.getId().getValue()).get();
        assertThat(updated.getOrderNumber()).isEqualTo("ORDER-002");
    }

    @Test
    void softDelete_WithExistingOrder_ShouldMarkAsDeleted() {
        // Given
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity = jpaRepository.save(entity);

        // When
        commandAdapter.softDelete(OrderId.of(entity.getId()));

        // Then
        OrderJpaEntity deleted =
            jpaRepository.findById(entity.getId()).get();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ 비즈니스 로직 포함 (Domain에서 처리해야 함)
@Override
public Order save(Order order) {
    if (order.getStatus() == OrderStatus.PENDING) {
        order.confirm();  // 비즈니스 로직 금지!
    }
    OrderJpaEntity entity = mapper.toEntity(order);
    return mapper.toDomain(jpaRepository.save(entity));
}

// ❌ Query 메서드 사용 (Query Adapter로 분리)
@Override
public List<Order> findByUserId(UserId userId) {
    return jpaRepository.findByUserId(userId.getValue())
        .stream()
        .map(mapper::toDomain)
        .toList();
}

// ❌ Entity 직접 노출
@Override
public OrderJpaEntity save(Order order) {
    return jpaRepository.save(mapper.toEntity(order));
}

// ❌ Exception 처리 없음 (findById 사용 시)
@Override
public void softDelete(OrderId orderId) {
    OrderJpaEntity entity = jpaRepository.findById(orderId.getValue()).get();
    entity.markAsDeleted();
    jpaRepository.save(entity);
}
```

### ✅ Good Examples

```java
// ✅ 단순 변환 및 저장만
@Override
public Order save(Order order) {
    OrderJpaEntity entity = mapper.toEntity(order);
    OrderJpaEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
}

// ✅ Exception 처리
@Override
public void softDelete(OrderId orderId) {
    OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    entity.markAsDeleted();
    jpaRepository.save(entity);
}

// ✅ Domain Model 입력/출력
Order savedOrder = commandAdapter.save(order);

// ✅ Mapper 사용
OrderJpaEntity entity = mapper.toEntity(order);
Order domain = mapper.toDomain(entity);
```

---

## 📐 Command Adapter 설계 규칙

### 1. 단일 책임 원칙 (SRP)

```java
// ✅ Good - Command만 담당
@Component
public class OrderCommandAdapter implements SaveOrderPort, DeleteOrderPort {
    // save(), softDelete(), restore()만
}

// ❌ Bad - Command + Query 혼재
@Component
public class OrderAdapter implements SaveOrderPort, LoadOrderPort {
    // CQRS 위반!
}
```

### 2. Mapper 분리

```java
// ✅ Good - Mapper 별도 클래스
@Component
public class OrderCommandAdapter {
    private final OrderEntityMapper mapper;
}

// ❌ Bad - Adapter에 변환 로직 직접 작성
@Component
public class OrderCommandAdapter {
    private OrderJpaEntity toEntity(Order order) {
        // 변환 로직이 Adapter에 있으면 테스트/재사용 어려움
    }
}
```

### 3. Exception 처리

```java
// ✅ Good - Domain Exception 사용
OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
    .orElseThrow(() -> new OrderNotFoundException(orderId));

// ❌ Bad - 일반 Exception
OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
    .orElseThrow(() -> new RuntimeException("Order not found"));
```

---

## 📋 체크리스트

Command Adapter 작성 시:
- [ ] `@Component` 어노테이션
- [ ] SaveOrderPort, DeleteOrderPort 구현
- [ ] JpaRepository 의존성 주입
- [ ] Mapper 의존성 주입
- [ ] Domain ↔ Entity 변환
- [ ] 비즈니스 로직 없음
- [ ] Query 메서드 없음
- [ ] Exception 처리 (Domain Exception)

---

## 📖 관련 문서

- **[Save Port Pattern](./01_save-port-pattern.md)** - SaveOrderPort 인터페이스
- **[Delete Port Pattern](./02_delete-port-pattern.md)** - DeleteOrderPort 인터페이스
- **[Command Mapper Patterns](./04_command-mapper-patterns.md)** - Mapper 상세 가이드
- **[Command Adapter Unit Testing](../testing/01_command-adapter-unit-testing.md)** - 테스트 전략

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
