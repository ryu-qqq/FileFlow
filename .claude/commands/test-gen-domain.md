---
description: Domain 계층 단위 테스트 자동 생성 (Happy/Edge/Exception Cases)
---

# Domain Layer 단위 테스트 자동 생성

**목적**: Domain Aggregate/ValueObject/DomainEvent에 대한 고품질 단위 테스트 자동 생성

**타겟**: Domain Layer - Pure Business Logic Tests

**생성 테스트**: Happy Path, Edge Cases, Exception Cases, Invariant Validation

---

## 🎯 사용법

```bash
# Domain Aggregate 테스트 생성
/test-gen-domain Order

# ValueObject 테스트 생성
/test-gen-domain OrderId

# DomainEvent 테스트 생성
/test-gen-domain OrderPlacedEvent
```

---

## ✅ 자동 생성되는 테스트 케이스

### 1. Happy Path (성공 케이스)

**Aggregate 생성 테스트**:
```java
@Test
@DisplayName("유효한 입력으로 Order Aggregate 생성 성공")
void shouldCreateOrderWithValidInputs() {
    // Given
    OrderId orderId = OrderId.of(1L);
    CustomerId customerId = CustomerId.of(100L);
    OrderStatus status = OrderStatus.PLACED;

    // When
    Order order = Order.create(orderId, customerId, status);

    // Then
    assertThat(order.getIdValue()).isEqualTo(1L);
    assertThat(order.getCustomerIdValue()).isEqualTo(100L);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
}
```

**비즈니스 메서드 테스트**:
```java
@Test
@DisplayName("PLACED 상태의 주문을 성공적으로 취소")
void shouldCancelOrderWhenStatusIsPlaced() {
    // Given
    Order order = Order.create(/*...*/);

    // When
    order.cancel();

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
}
```

### 2. Edge Cases (경계값 테스트)

```java
@Test
@DisplayName("OrderId에 최소값 (1L) 사용 가능")
void shouldAcceptMinimumOrderId() {
    // When
    OrderId orderId = OrderId.of(1L);

    // Then
    assertThat(orderId.value()).isEqualTo(1L);
}

@Test
@DisplayName("OrderId에 최대값 (Long.MAX_VALUE) 사용 가능")
void shouldAcceptMaximumOrderId() {
    // When
    OrderId orderId = OrderId.of(Long.MAX_VALUE);

    // Then
    assertThat(orderId.value()).isEqualTo(Long.MAX_VALUE);
}
```

### 3. Exception Cases (예외 처리)

```java
@Test
@DisplayName("OrderId가 null이면 IllegalArgumentException 발생")
void shouldThrowExceptionWhenOrderIdIsNull() {
    // When & Then
    assertThatThrownBy(() -> OrderId.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("OrderId must not be null");
}

@Test
@DisplayName("CANCELLED 상태의 주문을 취소하면 IllegalStateException 발생")
void shouldThrowExceptionWhenCancellingCancelledOrder() {
    // Given
    Order order = Order.create(/*...*/);
    order.cancel();

    // When & Then
    assertThatThrownBy(() -> order.cancel())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Order already cancelled");
}
```

### 4. Invariant Validation (불변식 검증)

```java
@Test
@DisplayName("Order Aggregate는 항상 유효한 상태를 유지")
void shouldMaintainInvariantsAfterStateChanges() {
    // Given
    Order order = Order.create(/*...*/);

    // When
    order.confirm();

    // Then
    assertThat(order.getIdValue()).isNotNull();
    assertThat(order.getCustomerIdValue()).isNotNull();
    assertThat(order.getStatus()).isIn(
        OrderStatus.PLACED,
        OrderStatus.CONFIRMED,
        OrderStatus.CANCELLED
    );
}
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
domain/src/test/java/com/ryuqq/domain/{aggregate}/
└── {Aggregate}Test.java
```

### 2. 테스트 클래스 템플릿
```java
package com.ryuqq.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Order Domain Aggregate 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 생성 및 비즈니스 메서드</li>
 *   <li>Edge Cases: 경계값 테스트</li>
 *   <li>Exception Cases: 예외 상황 처리</li>
 *   <li>Invariant Validation: 불변식 검증</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@DisplayName("Order Domain 단위 테스트")
class OrderTest {

    // Happy Path Tests

    // Edge Case Tests

    // Exception Tests

    // Invariant Tests
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **Lombok 금지**: AssertJ 사용 (`assertThat()`)
- ✅ **Law of Demeter**: `order.getIdValue()` (Getter 체이닝 금지)
- ✅ **DisplayName 필수**: 모든 테스트에 한글 설명
- ✅ **Given-When-Then**: BDD 스타일 통일
- ✅ **Pure Java**: Spring Context 없음, 순수 로직 테스트

---

## 📊 테스트 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| Line Coverage | 100% | 모든 코드 라인 실행 |
| Branch Coverage | 100% | 모든 조건문 분기 |
| Method Coverage | 100% | 모든 public 메서드 |
| Exception Coverage | 100% | 모든 예외 케이스 |

---

## 🚀 실행 예시

### Input (Domain Aggregate)
```java
public final class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;

    public static Order create(OrderId id, CustomerId customerId) {
        validateNotNull(id, "OrderId must not be null");
        validateNotNull(customerId, "CustomerId must not be null");
        return new Order(id, customerId, OrderStatus.PLACED);
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public Long getIdValue() {
        return this.id.value();
    }

    public Long getCustomerIdValue() {
        return this.customerId.value();
    }

    public OrderStatus getStatus() {
        return this.status;
    }
}
```

### Output (Auto-generated Test)
```java
@DisplayName("Order Domain 단위 테스트")
class OrderTest {

    @Test
    @DisplayName("유효한 입력으로 Order 생성 성공")
    void shouldCreateOrderWithValidInputs() {
        // Given
        OrderId orderId = OrderId.of(1L);
        CustomerId customerId = CustomerId.of(100L);

        // When
        Order order = Order.create(orderId, customerId);

        // Then
        assertThat(order.getIdValue()).isEqualTo(1L);
        assertThat(order.getCustomerIdValue()).isEqualTo(100L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    @DisplayName("OrderId가 null이면 예외 발생")
    void shouldThrowExceptionWhenOrderIdIsNull() {
        // Given
        CustomerId customerId = CustomerId.of(100L);

        // When & Then
        assertThatThrownBy(() -> Order.create(null, customerId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OrderId must not be null");
    }

    @Test
    @DisplayName("PLACED 상태의 주문 취소 성공")
    void shouldCancelOrderWhenStatusIsPlaced() {
        // Given
        Order order = Order.create(
            OrderId.of(1L),
            CustomerId.of(100L)
        );

        // When
        order.cancel();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("CANCELLED 상태의 주문 재취소 시 예외 발생")
    void shouldThrowExceptionWhenCancellingCancelledOrder() {
        // Given
        Order order = Order.create(
            OrderId.of(1L),
            CustomerId.of(100L)
        );
        order.cancel();

        // When & Then
        assertThatThrownBy(() -> order.cancel())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Order already cancelled");
    }

    // ... (15개 테스트 케이스 자동 생성)
}
```

---

## 💡 Claude Code 활용 팁

### 1. 기존 Domain 코드 분석
```
"Analyze OrderDomain.java and generate comprehensive unit tests"
```

### 2. 특정 메서드만 테스트
```
"Generate tests only for the cancel() method in OrderDomain.java"
```

### 3. 엣지 케이스 추가
```
"Add edge case tests for OrderId boundary values"
```

### 4. Exception 메시지 검증
```
"Add assertion for exception messages in all exception tests"
```

---

## 🎯 기대 효과

1. **시간 절약**: 수동 테스트 작성 대비 **80% 시간 절감**
2. **품질 향상**: 놓치기 쉬운 엣지 케이스 자동 커버
3. **일관성**: 모든 Domain 테스트가 동일한 패턴 준수
4. **문서화**: DisplayName이 살아있는 문서 역할

---

**✅ 이 명령어는 Claude Code가 Domain 계층의 고품질 단위 테스트를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: Windsurf가 Domain Aggregate를 생성하면, Claude Code가 테스트를 자동 생성!
