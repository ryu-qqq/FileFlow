# /cr/red - RED Phase Guide (Cursor TDD)

**목적**: Kent Beck TDD의 RED Phase - 실패하는 테스트 작성 가이드

**실행 환경**: Cursor Composer

**전제 조건**: Domain PRD 존재 (`docs/prd/domain/{name}-domain-prd.md`)

---

## 🎯 RED Phase 철학

> **"먼저 실패하는 테스트를 작성하라. 그래야 무엇을 구현해야 할지 명확해진다."**
> — Kent Beck

### 핵심 원칙
1. **TestFixture부터 시작** (가장 중요!) ⭐
2. **비즈니스 규칙을 테스트로 표현**
3. **최소한의 테스트만** (한 번에 하나씩)
4. **실패를 확인** (RED 상태)

---

## 📋 작업 흐름

### Step 1: Domain PRD 읽기 (필수)

```bash
# Cursor Composer에서
"docs/prd/domain/{name}-domain-prd.md 파일을 읽어줘"
```

**확인 사항**:
- [ ] Aggregate Root 이름
- [ ] ValueObject 목록
- [ ] Business Rules
- [ ] TDD Plan의 TestFixture 예시

---

### Step 2: TestFixture 생성 (FIRST STEP) ⭐

**파일 위치**: `domain/src/testFixtures/java/{package}/`

**명명 규칙**: `{Aggregate}DomainFixture.java`

#### 템플릿
```java
package com.company.template.domain.order;

import java.util.List;

public class OrderDomainFixture {

    // 기본값 상수
    public static final Long DEFAULT_ORDER_ID = 1L;
    public static final OrderStatus DEFAULT_STATUS = OrderStatus.PLACED;
    public static final Money DEFAULT_TOTAL_PRICE = Money.of(10000);

    /**
     * 기본 Order 생성
     */
    public static Order create() {
        return Order.create(
            OrderId.of(DEFAULT_ORDER_ID),
            createDefaultLineItems(),
            DEFAULT_TOTAL_PRICE
        );
    }

    /**
     * 특정 상태의 Order 생성
     */
    public static Order createWithStatus(OrderStatus status) {
        Order order = create();
        // 상태 전이 로직 (나중에 구현)
        return order;
    }

    /**
     * 특정 ID의 Order 생성
     */
    public static Order createWithId(Long orderId) {
        return Order.create(
            OrderId.of(orderId),
            createDefaultLineItems(),
            DEFAULT_TOTAL_PRICE
        );
    }

    /**
     * 기본 OrderLineItem 목록 생성
     */
    public static List<OrderLineItem> createDefaultLineItems() {
        return List.of(
            OrderLineItem.of(
                ProductId.of(1L),
                2,
                Money.of(5000)
            )
        );
    }

    /**
     * 빈 OrderLineItem 목록 (실패 테스트용)
     */
    public static List<OrderLineItem> createEmptyLineItems() {
        return List.of();
    }
}
```

#### ✅ TestFixture 체크리스트
- [ ] 기본값 상수 정의 (`DEFAULT_*`)
- [ ] `create()` 메서드 (기본 Aggregate 생성)
- [ ] `createWith*()` 메서드들 (다양한 시나리오)
- [ ] Helper 메서드들 (`createDefaultLineItems()` 등)
- [ ] 실패 테스트용 메서드 (`createEmptyLineItems()` 등)

---

### Step 3: 테스트 클래스 생성

**파일 위치**: `domain/src/test/java/{package}/`

**명명 규칙**: `{Aggregate}Test.java`

#### 기본 구조
```java
package com.company.template.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Aggregate 테스트")
class OrderTest {

    // 테스트 케이스들...
}
```

---

### Step 4: 테스트 케이스 작성 (Business Rules 기반)

#### Rule 1: Aggregate 생성 규칙

**PRD Business Rule 예시**:
```
Rule: 주문 생성 시 최소 1개의 라인 아이템 필요
조건: lineItems.size() >= 1
결과: Order 생성 성공
예외: IllegalArgumentException
```

**테스트 코드**:
```java
@Test
@DisplayName("주문 생성 - 유효한 라인 아이템으로 생성 성공")
void createOrder_WithValidLineItems_ShouldSucceed() {
    // Given
    List<OrderLineItem> lineItems = OrderDomainFixture.createDefaultLineItems();

    // When
    Order order = Order.create(
        OrderId.of(1L),
        lineItems,
        Money.of(10000)
    );

    // Then
    assertThat(order).isNotNull();
    assertThat(order.getOrderId()).isEqualTo(OrderId.of(1L));
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
    assertThat(order.getTotalPrice()).isEqualTo(Money.of(10000));
}

@Test
@DisplayName("주문 생성 - 빈 라인 아이템으로 생성 실패")
void createOrder_WithEmptyLineItems_ShouldThrowException() {
    // Given
    List<OrderLineItem> emptyLineItems = OrderDomainFixture.createEmptyLineItems();

    // When & Then
    assertThatThrownBy(() ->
        Order.create(OrderId.of(1L), emptyLineItems, Money.of(0))
    )
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("주문은 최소 1개의 라인 아이템이 필요합니다");
}
```

#### Rule 2: 상태 전이 규칙

**PRD Business Rule 예시**:
```
Rule: 주문 취소는 PLACED 상태에서만 가능
조건: status == PLACED
결과: status → CANCELLED
예외: IllegalStateException
```

**테스트 코드**:
```java
@Test
@DisplayName("주문 취소 - PLACED 상태에서 취소 성공")
void cancelOrder_WhenPlaced_ShouldSucceed() {
    // Given
    Order order = OrderDomainFixture.createWithStatus(OrderStatus.PLACED);

    // When
    order.cancel();

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
}

@Test
@DisplayName("주문 취소 - CONFIRMED 상태에서 취소 실패")
void cancelOrder_WhenConfirmed_ShouldThrowException() {
    // Given
    Order order = OrderDomainFixture.createWithStatus(OrderStatus.CONFIRMED);

    // When & Then
    assertThatThrownBy(() -> order.cancel())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("PLACED 상태의 주문만 취소할 수 있습니다");
}

@Test
@DisplayName("주문 취소 - CANCELLED 상태에서 중복 취소 실패")
void cancelOrder_WhenAlreadyCancelled_ShouldThrowException() {
    // Given
    Order order = OrderDomainFixture.createWithStatus(OrderStatus.CANCELLED);

    // When & Then
    assertThatThrownBy(() -> order.cancel())
        .isInstanceOf(IllegalStateException.class);
}
```

#### Rule 3: 계산 로직 규칙

**PRD Business Rule 예시**:
```
Rule: 총 가격은 라인 아이템 가격의 합계
조건: lineItems 존재
결과: sum(lineItem.calculatePrice())
```

**테스트 코드**:
```java
@Test
@DisplayName("총 가격 계산 - 라인 아이템 가격 합계와 일치")
void calculateTotalPrice_ShouldSumLineItemPrices() {
    // Given
    Order order = OrderDomainFixture.create();

    // When
    Money totalPrice = order.calculateTotalPrice();

    // Then
    // DEFAULT_LINE_ITEMS: 2개 * 5000원 = 10000원
    assertThat(totalPrice).isEqualTo(Money.of(10000));
}

@Test
@DisplayName("총 가격 계산 - 여러 라인 아이템의 합계")
void calculateTotalPrice_WithMultipleItems_ShouldSumCorrectly() {
    // Given
    List<OrderLineItem> lineItems = List.of(
        OrderLineItem.of(ProductId.of(1L), 2, Money.of(5000)),  // 10000
        OrderLineItem.of(ProductId.of(2L), 1, Money.of(3000))   // 3000
    );
    Order order = Order.create(OrderId.of(1L), lineItems, Money.of(13000));

    // When
    Money totalPrice = order.calculateTotalPrice();

    // Then
    assertThat(totalPrice).isEqualTo(Money.of(13000));
}
```

---

### Step 5: ValueObject 테스트

**OrderId ValueObject 테스트**:
```java
@Test
@DisplayName("OrderId 생성 - 유효한 값으로 생성 성공")
void createOrderId_WithValidValue_ShouldSucceed() {
    // When
    OrderId orderId = OrderId.of(1L);

    // Then
    assertThat(orderId.getValue()).isEqualTo(1L);
}

@Test
@DisplayName("OrderId 생성 - null 값으로 생성 실패")
void createOrderId_WithNullValue_ShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> OrderId.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("OrderId는 null일 수 없습니다");
}

@Test
@DisplayName("OrderId 생성 - 음수 값으로 생성 실패")
void createOrderId_WithNegativeValue_ShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> OrderId.of(-1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("OrderId는 양수여야 합니다");
}

@Test
@DisplayName("OrderId 동등성 - 같은 값이면 동등")
void orderIdEquality_WithSameValue_ShouldBeEqual() {
    // Given
    OrderId orderId1 = OrderId.of(1L);
    OrderId orderId2 = OrderId.of(1L);

    // Then
    assertThat(orderId1).isEqualTo(orderId2);
    assertThat(orderId1.hashCode()).isEqualTo(orderId2.hashCode());
}
```

---

## ✅ RED Phase 완료 체크리스트

### TestFixture
- [ ] `{Aggregate}DomainFixture.java` 생성
- [ ] 기본값 상수 정의
- [ ] `create()` 메서드 구현
- [ ] `createWith*()` 메서드들 구현
- [ ] Helper 메서드들 구현

### 테스트 케이스
- [ ] Aggregate 생성 테스트 (성공 케이스)
- [ ] Aggregate 생성 테스트 (실패 케이스)
- [ ] 비즈니스 로직 테스트 (각 Business Rule마다)
- [ ] 상태 전이 테스트 (상태 Enum이 있는 경우)
- [ ] 계산 로직 테스트 (계산이 필요한 경우)
- [ ] ValueObject 테스트 (생성, 검증, 동등성)

### 테스트 실행
- [ ] 테스트 실행 → **RED 상태 확인** (실패해야 함!)
- [ ] 컴파일 에러 확인 (Aggregate, ValueObject 미구현)

---

## 🚨 주의사항

### ❌ 하지 말아야 할 것
1. **구현 코드 작성 금지**
   - Order.java, OrderId.java 등은 아직 작성하지 않음
   - 테스트만 작성!

2. **테스트를 통과시키려고 하지 말 것**
   - RED Phase는 실패가 목표
   - 컴파일 에러, 테스트 실패 = 정상

3. **너무 많은 테스트 작성 금지**
   - 한 번에 1-2개 Business Rule만
   - 작은 단위로 나눠서 진행

### ✅ 해야 할 것
1. **TestFixture부터 시작**
   - 테스트 작성 전 반드시 Fixture 먼저

2. **Given-When-Then 구조 준수**
   - 가독성 중요

3. **DisplayName 명확히**
   - 비즈니스 규칙이 명확히 드러나게

---

## 🎯 Cursor Composer 실행 예시

### 자동 실행 프롬프트
```
docs/prd/domain/order-domain-prd.md를 읽고 RED Phase를 실행해줘.

1. OrderDomainFixture.java 먼저 생성
2. OrderTest.java 생성
3. PRD의 Business Rules를 테스트로 변환
4. Given-When-Then 구조로 작성
5. 테스트는 실패해야 함 (RED 상태)

.cursorrules의 Domain Layer 컨벤션을 따라줘.
```

---

## 📊 예상 산출물

### 파일 구조
```
domain/
├── src/
│   ├── testFixtures/java/com/company/template/domain/order/
│   │   └── OrderDomainFixture.java ⭐ (가장 먼저 생성)
│   │
│   └── test/java/com/company/template/domain/order/
│       └── OrderTest.java
```

### 테스트 실행 결과
```bash
$ ./gradlew :domain:test

> Task :domain:test FAILED

OrderTest > createOrder_WithValidLineItems_ShouldSucceed FAILED
    java.lang.Error: Unresolved compilation problems:
        Order cannot be resolved to a type

5 tests completed, 5 failed ← RED 상태 (정상!)
```

---

## 🔗 다음 단계

1. **RED Phase 완료 확인**
   - [ ] TestFixture 생성됨
   - [ ] 테스트 케이스 작성됨
   - [ ] 테스트 실행 시 RED 상태 (실패)

2. **GREEN Phase로 이동**
   ```bash
   # Cursor Composer에서
   "/cr/green 실행해줘"
   ```

---

**✅ RED Phase는 "무엇을 구현할지" 명확히 하는 단계입니다. 실패는 성공입니다!**
