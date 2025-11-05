# Command Adapter Unit Testing (커맨드 어댑터 단위 테스트)

**목적**: CQRS Command Adapter의 단위 테스트 전략 정의

**위치**: `adapter-persistence/src/test/java/[module]/adapter/`

**필수 버전**: Java 21+, Spring Boot 3.0+, JUnit 5

---

## 🎯 핵심 원칙

### Command Adapter 테스트 전략

Command Adapter는 **Domain Model을 Entity로 변환하여 저장**하는 책임만 검증합니다:

```
테스트 대상:
1. Domain → Entity 변환
2. JpaRepository.save() 호출
3. Entity → Domain 변환
4. ID 할당 확인
5. Soft Delete 동작
```

**규칙**:
- ✅ `@DataJpaTest` (JPA 슬라이스 테스트)
- ✅ H2 In-Memory DB 사용
- ✅ `@Tag("unit")`, `@Tag("command")` 필수
- ✅ Domain Model 중심 테스트
- ❌ Query 테스트 금지 (Query Adapter 테스트로 분리)

---

## 📦 기본 테스트 구조

### Command Adapter 단위 테스트 템플릿

```java
package com.company.adapter.out.persistence.order.adapter;

import com.company.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.company.adapter.out.persistence.order.mapper.OrderEntityMapper;
import com.company.adapter.out.persistence.order.repository.OrderJpaRepository;
import com.company.domain.order.Order;
import com.company.domain.order.OrderId;
import com.company.domain.order.UserId;
import com.company.domain.order.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Command Adapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DataJpaTest
@Import({OrderCommandAdapter.class, OrderEntityMapperImpl.class})
@Tag("unit")
@Tag("command")
@DisplayName("Order Command Adapter 단위 테스트")
class OrderCommandAdapterTest {

    @Autowired
    private OrderCommandAdapter commandAdapter;

    @Autowired
    private OrderJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("신규 Order 저장 시 ID가 할당되어야 한다")
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
        assertThat(savedOrder.getUserId().getValue()).isEqualTo(100L);

        // DB 검증
        Optional<OrderJpaEntity> entity =
            jpaRepository.findById(savedOrder.getId().getValue());
        assertThat(entity).isPresent();
        assertThat(entity.get().getUserId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("기존 Order 수정 시 업데이트되어야 한다")
    void save_WithExistingOrder_ShouldUpdate() {
        // Given - 기존 Order 저장
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity = jpaRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

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
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Soft Delete 시 deletedAt이 설정되어야 한다")
    void softDelete_WithExistingOrder_ShouldMarkAsDeleted() {
        // Given
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity = jpaRepository.save(entity);
        entityManager.flush();

        // When
        commandAdapter.softDelete(OrderId.of(entity.getId()));

        // Then
        OrderJpaEntity deleted =
            jpaRepository.findById(entity.getId()).get();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 Order Soft Delete 시 예외 발생")
    void softDelete_WithNonExistentOrder_ShouldThrowException() {
        // Given
        OrderId nonExistentId = OrderId.of(999L);

        // When & Then
        assertThatThrownBy(() -> commandAdapter.softDelete(nonExistentId))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Restore 시 deletedAt이 null이 되어야 한다")
    void restore_WithDeletedOrder_ShouldClearDeletedAt() {
        // Given - Soft Delete된 Order
        OrderJpaEntity entity = OrderJpaEntity.create(100L, "ORDER-001");
        entity.markAsDeleted();
        entity = jpaRepository.save(entity);
        entityManager.flush();

        assertThat(entity.isDeleted()).isTrue();

        // When
        commandAdapter.restore(OrderId.of(entity.getId()));

        // Then
        OrderJpaEntity restored =
            jpaRepository.findById(entity.getId()).get();
        assertThat(restored.isDeleted()).isFalse();
        assertThat(restored.getDeletedAt()).isNull();
    }
}
```

---

## 🧪 테스트 케이스 패턴

### 1. 신규 저장 테스트 (Create)

```java
@Test
@DisplayName("신규 Order 저장 시 ID가 할당되어야 한다")
void save_WithNewOrder_ShouldPersistAndReturnId() {
    // Given
    Order order = Order.create(UserId.of(100L), OrderItems.of(...));

    // When
    Order savedOrder = commandAdapter.save(order);

    // Then
    assertThat(savedOrder.getId()).isNotNull();
}
```

### 2. 기존 수정 테스트 (Update)

```java
@Test
@DisplayName("기존 Order 수정 시 업데이트되어야 한다")
void save_WithExistingOrder_ShouldUpdate() {
    // Given - 기존 Order 저장
    OrderJpaEntity entity = jpaRepository.save(
        OrderJpaEntity.create(100L, "ORDER-001")
    );
    entityManager.flush();
    entityManager.clear();

    // Domain으로 변환 후 수정
    Order order = Order.reconstitute(
        OrderId.of(entity.getId()),
        UserId.of(100L),
        OrderNumber.of("ORDER-002"),  // 변경
        OrderStatus.CONFIRMED,
        Money.of(BigDecimal.valueOf(10000))
    );

    // When
    Order savedOrder = commandAdapter.save(order);

    // Then
    OrderJpaEntity updated = jpaRepository.findById(savedOrder.getId().getValue()).get();
    assertThat(updated.getOrderNumber()).isEqualTo("ORDER-002");
}
```

### 3. Soft Delete 테스트

```java
@Test
@DisplayName("Soft Delete 시 deletedAt이 설정되어야 한다")
void softDelete_WithExistingOrder_ShouldMarkAsDeleted() {
    // Given
    OrderJpaEntity entity = jpaRepository.save(
        OrderJpaEntity.create(100L, "ORDER-001")
    );

    // When
    commandAdapter.softDelete(OrderId.of(entity.getId()));

    // Then
    OrderJpaEntity deleted = jpaRepository.findById(entity.getId()).get();
    assertThat(deleted.isDeleted()).isTrue();
    assertThat(deleted.getDeletedAt()).isNotNull();
}
```

### 4. Exception 테스트

```java
@Test
@DisplayName("존재하지 않는 Order Soft Delete 시 예외 발생")
void softDelete_WithNonExistentOrder_ShouldThrowException() {
    // Given
    OrderId nonExistentId = OrderId.of(999L);

    // When & Then
    assertThatThrownBy(() -> commandAdapter.softDelete(nonExistentId))
        .isInstanceOf(OrderNotFoundException.class);
}
```

---

## 🔧 테스트 설정

### @DataJpaTest 설정

```java
@DataJpaTest
@Import({OrderCommandAdapter.class, OrderEntityMapperImpl.class})
@Tag("unit")
@Tag("command")
class OrderCommandAdapterTest {
    // ...
}
```

**설명**:
- `@DataJpaTest`: JPA 관련 Bean만 로드 (빠른 실행)
- `@Import`: 테스트 대상 Adapter와 Mapper 로드
- `@Tag("unit")`: 단위 테스트 태그
- `@Tag("command")`: Command 테스트 태그 (Query와 분리)

### H2 In-Memory DB 설정

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
        show_sql: true
```

---

## 📊 EntityManager 활용

### Flush & Clear 패턴

```java
@Test
void save_WithExistingOrder_ShouldUpdate() {
    // Given
    OrderJpaEntity entity = jpaRepository.save(
        OrderJpaEntity.create(100L, "ORDER-001")
    );
    entityManager.flush();  // DB에 강제 저장
    entityManager.clear();  // 1차 캐시 초기화

    // When
    Order order = Order.reconstitute(...);
    Order savedOrder = commandAdapter.save(order);

    // Then
    // DB에서 다시 조회 (캐시 미사용)
    OrderJpaEntity updated = jpaRepository.findById(savedOrder.getId().getValue()).get();
    assertThat(updated.getOrderNumber()).isEqualTo("ORDER-002");
}
```

**설명**:
- `flush()`: 영속성 컨텍스트 변경 내용을 DB에 반영
- `clear()`: 1차 캐시 초기화 (DB에서 실제로 조회하도록)

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Query 테스트 포함 (Command Adapter는 Query 테스트 금지)
@Test
void loadById_WithExistingOrder_ShouldReturnDTO() {
    // Query 테스트는 Query Adapter Test로!
}

// ❌ @SpringBootTest 사용 (무겁고 느림)
@SpringBootTest
class OrderCommandAdapterTest {
    // @DataJpaTest 사용!
}

// ❌ @Tag 누락
@DataJpaTest
class OrderCommandAdapterTest {
    // @Tag("unit"), @Tag("command") 필수!
}

// ❌ Entity 직접 검증 (Domain 중심 검증)
@Test
void save_WithNewOrder_ShouldPersist() {
    OrderJpaEntity entity = new OrderJpaEntity();
    entity.setUserId(100L);
    commandAdapter.save(entity);  // Domain Model 사용!
}
```

### ✅ Good Examples

```java
// ✅ @DataJpaTest + @Tag
@DataJpaTest
@Import({OrderCommandAdapter.class, OrderEntityMapperImpl.class})
@Tag("unit")
@Tag("command")
class OrderCommandAdapterTest {
    // ...
}

// ✅ Domain Model 중심 테스트
@Test
void save_WithNewOrder_ShouldPersist() {
    Order order = Order.create(UserId.of(100L), ...);
    Order savedOrder = commandAdapter.save(order);
    assertThat(savedOrder.getId()).isNotNull();
}

// ✅ EntityManager flush & clear
@Test
void save_WithExistingOrder_ShouldUpdate() {
    OrderJpaEntity entity = jpaRepository.save(...);
    entityManager.flush();
    entityManager.clear();

    Order order = Order.reconstitute(...);
    Order savedOrder = commandAdapter.save(order);
}
```

---

## 📋 체크리스트

Command Adapter 테스트 작성 시:
- [ ] `@DataJpaTest` 사용
- [ ] `@Import(Adapter, Mapper)` 설정
- [ ] `@Tag("unit")`, `@Tag("command")` 필수
- [ ] H2 In-Memory DB 사용
- [ ] Domain Model 중심 테스트
- [ ] ID 할당 검증
- [ ] Soft Delete 검증
- [ ] Exception 검증
- [ ] EntityManager flush & clear 활용

---

## 📖 관련 문서

- **[Command Adapter Implementation](../command-adapter-patterns/03_command-adapter-implementation.md)** - Command Adapter 구현
- **[Query Adapter Unit Testing](./02_query-adapter-unit-testing.md)** - Query Adapter 테스트
- **[Testcontainers Integration](./03_testcontainers-integration.md)** - 통합 테스트
- **[Test Tags Strategy](./04_test-tags-strategy.md)** - 테스트 태그 전략

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
