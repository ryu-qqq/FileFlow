# /cr/green - GREEN Phase Guide (Cursor TDD)

**목적**: Kent Beck TDD의 GREEN Phase - 최소 구현으로 테스트 통과 가이드

**실행 환경**: Cursor Composer

**전제 조건**: RED Phase 완료 (실패하는 테스트 존재)

---

## 🎯 GREEN Phase 철학

> **"가능한 가장 빠르게, 가장 단순하게 테스트를 통과시켜라."**
> — Kent Beck

### 핵심 원칙
1. **테스트 통과만 목표** (품질은 나중에)
2. **하드코딩 허용** (필요하면 상수 리턴도 OK)
3. **빠른 속도** (복잡한 로직 금지)
4. **GREEN 상태 확인** (모든 테스트 통과)

---

## 📋 작업 흐름

### Step 1: 실패 테스트 확인

```bash
# 현재 상태 확인
$ ./gradlew :domain:test

OrderTest > createOrder_WithValidLineItems_ShouldSucceed FAILED
    Order cannot be resolved to a type

5 tests completed, 5 failed ← RED 상태
```

**목표**: 이 테스트들을 통과시키기

---

### Step 2: Aggregate Root 구현 (최소)

**파일 위치**: `domain/src/main/java/{package}/`

**명명 규칙**: `{Aggregate}.java`

#### 최소 구현 템플릿
```java
package com.company.template.domain.order;

import java.util.List;

/**
 * Order Aggregate Root
 *
 * @author {your-name}
 * @since {date}
 */
public class Order {

    private OrderId orderId;
    private List<OrderLineItem> lineItems;
    private Money totalPrice;
    private OrderStatus status;

    // Private 생성자
    private Order(OrderId orderId, List<OrderLineItem> lineItems, Money totalPrice) {
        this.orderId = orderId;
        this.lineItems = lineItems;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PLACED; // 기본 상태
    }

    /**
     * Order 생성 Factory Method
     */
    public static Order create(OrderId orderId, List<OrderLineItem> lineItems, Money totalPrice) {
        // 검증: 최소한의 비즈니스 규칙만
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("주문은 최소 1개의 라인 아이템이 필요합니다");
        }

        return new Order(orderId, lineItems, totalPrice);
    }

    /**
     * 주문 취소
     */
    public void cancel() {
        // 상태 검증
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("PLACED 상태의 주문만 취소할 수 있습니다");
        }

        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 총 가격 계산
     * (GREEN Phase: 간단하게 구현)
     */
    public Money calculateTotalPrice() {
        // 하드코딩도 OK (REFACTOR에서 개선)
        return totalPrice;
    }

    // Getters (Lombok 사용 금지)
    public OrderId getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalPrice() {
        return totalPrice;
    }

    public List<OrderLineItem> getLineItems() {
        return lineItems;
    }
}
```

#### ✅ Aggregate Root 체크리스트
- [ ] Private 생성자
- [ ] Factory Method (`create()`)
- [ ] 최소한의 검증 (테스트 통과 수준)
- [ ] 비즈니스 메서드 (간단 구현)
- [ ] Getters (Lombok 금지, 직접 작성)

---

### Step 3: ValueObject 구현 (최소)

#### OrderId.java
```java
package com.company.template.domain.order;

import java.util.Objects;

/**
 * Order 식별자 ValueObject
 *
 * @author {your-name}
 * @since {date}
 */
public class OrderId {

    private final Long value;

    // Private 생성자
    private OrderId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("OrderId는 null일 수 없습니다");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("OrderId는 양수여야 합니다");
        }
        this.value = value;
    }

    /**
     * Factory Method
     */
    public static OrderId of(Long value) {
        return new OrderId(value);
    }

    // Getter
    public Long getValue() {
        return value;
    }

    // equals & hashCode (중요!)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId)) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "OrderId{" + value + "}";
    }
}
```

#### Money.java
```java
package com.company.template.domain.order;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 금액 ValueObject
 *
 * @author {your-name}
 * @since {date}
 */
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
        this.amount = amount;
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * 금액 더하기 (GREEN Phase: 간단 구현)
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * 금액 곱하기
     */
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
```

#### OrderLineItem.java
```java
package com.company.template.domain.order;

import java.util.Objects;

/**
 * 주문 라인 아이템 ValueObject
 *
 * @author {your-name}
 * @since {date}
 */
public class OrderLineItem {

    private final ProductId productId;
    private final int quantity;
    private final Money unitPrice;

    private OrderLineItem(ProductId productId, int quantity, Money unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public static OrderLineItem of(ProductId productId, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }
        return new OrderLineItem(productId, quantity, unitPrice);
    }

    /**
     * 라인 아이템 가격 계산
     */
    public Money calculatePrice() {
        return unitPrice.multiply(quantity);
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLineItem)) return false;
        OrderLineItem that = (OrderLineItem) o;
        return quantity == that.quantity &&
               Objects.equals(productId, that.productId) &&
               Objects.equals(unitPrice, that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }
}
```

#### ✅ ValueObject 체크리스트
- [ ] `final` 필드 (불변성)
- [ ] Private 생성자
- [ ] Factory Method (`of()`)
- [ ] 검증 로직 (최소한)
- [ ] `equals()` & `hashCode()` 필수
- [ ] Getter만 (Setter 없음)

---

### Step 4: Enum 구현 (최소)

#### OrderStatus.java
```java
package com.company.template.domain.order;

/**
 * 주문 상태 Enum
 *
 * @author {your-name}
 * @since {date}
 */
public enum OrderStatus {

    PLACED("주문 완료"),
    CONFIRMED("주문 확인"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("취소됨");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * PLACED 상태인지 확인
     */
    public boolean isPlaced() {
        return this == PLACED;
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this == PLACED;
    }
}
```

#### ✅ Enum 체크리스트
- [ ] 모든 값 정의
- [ ] Description 필드 (선택)
- [ ] Helper 메서드 (`isXxx()`) (선택)

---

### Step 5: 테스트 실행 및 GREEN 확인

```bash
$ ./gradlew :domain:test

BUILD SUCCESSFUL in 3s

OrderTest > createOrder_WithValidLineItems_ShouldSucceed PASSED
OrderTest > createOrder_WithEmptyLineItems_ShouldThrowException PASSED
OrderTest > cancelOrder_WhenPlaced_ShouldSucceed PASSED
OrderTest > cancelOrder_WhenConfirmed_ShouldThrowException PASSED
OrderTest > calculateTotalPrice_ShouldSumLineItemPrices PASSED

5 tests completed, 5 passed ← GREEN 상태! ✅
```

---

## ✅ GREEN Phase 완료 체크리스트

### 구현 완료
- [ ] Aggregate Root 구현 (`Order.java`)
- [ ] ValueObjects 구현 (`OrderId`, `Money`, `OrderLineItem` 등)
- [ ] Enum 구현 (`OrderStatus`)
- [ ] ProductId 등 의존 ValueObject 구현

### 테스트 통과
- [ ] 모든 테스트 GREEN 상태
- [ ] 컴파일 에러 없음
- [ ] Runtime 에러 없음

### 구현 품질 (GREEN Phase 기준)
- [ ] Lombok 사용하지 않음 (Pure Java)
- [ ] Private 생성자 + Factory Method 패턴
- [ ] 최소한의 검증 로직 (테스트 통과 수준)
- [ ] equals & hashCode 구현 (ValueObject)

---

## 🚨 주의사항

### ❌ 하지 말아야 할 것
1. **복잡한 로직 구현 금지**
   - GREEN Phase는 최소 구현
   - 복잡한 계산, 최적화는 REFACTOR에서

2. **완벽을 추구하지 말 것**
   - "테스트 통과" = 목표 달성
   - 코드 품질은 REFACTOR에서

3. **Getter 체이닝 사용 금지** (이것만은 지켜야 함)
   ```java
   // ❌ 금지 (Law of Demeter 위반)
   order.getCustomer().getAddress().getZip()

   // ✅ 허용
   order.getCustomerZip()
   ```

### ✅ 해야 할 것
1. **빠른 구현**
   - 테스트 통과에 필요한 최소 코드만

2. **하드코딩 허용**
   ```java
   // GREEN Phase에서는 OK
   public Money calculateTotalPrice() {
       return totalPrice; // 단순 리턴도 OK
   }

   // REFACTOR Phase에서 개선
   public Money calculateTotalPrice() {
       return lineItems.stream()
           .map(OrderLineItem::calculatePrice)
           .reduce(Money.ZERO, Money::add);
   }
   ```

3. **Lombok 금지는 반드시 준수**
   - `@Data`, `@Builder`, `@Getter` 등 사용 금지
   - Pure Java getter/setter 직접 작성

---

## 💡 GREEN Phase 전략

### 전략 1: Fake It (가짜 구현)
```java
// 테스트: assertThat(order.getTotalPrice()).isEqualTo(Money.of(10000));

// GREEN Phase: 하드코딩
public Money getTotalPrice() {
    return Money.of(10000); // 테스트 통과!
}

// REFACTOR Phase: 실제 계산
public Money getTotalPrice() {
    return calculateTotalPrice();
}
```

### 전략 2: Obvious Implementation (명백한 구현)
```java
// 간단한 로직은 바로 구현
public void cancel() {
    if (status != OrderStatus.PLACED) {
        throw new IllegalStateException("PLACED 상태의 주문만 취소할 수 있습니다");
    }
    this.status = OrderStatus.CANCELLED;
}
```

### 전략 3: Triangulation (삼각측량)
```java
// 테스트 1: Money.of(5000).add(Money.of(3000)) → Money.of(8000)
// 테스트 2: Money.of(0).add(Money.of(100)) → Money.of(100)

// 여러 테스트를 만족하는 일반 구현
public Money add(Money other) {
    return new Money(this.amount.add(other.amount));
}
```

---

## 🎯 Cursor Composer 실행 예시

### 자동 실행 프롬프트
```
GREEN Phase를 실행해줘.

1. 실패하는 테스트를 통과시키는 최소 구현
2. Order.java, OrderId.java, Money.java 등 생성
3. Lombok 사용 금지, Pure Java로
4. 하드코딩 허용, 빠르게 진행
5. 모든 테스트 GREEN 상태 확인

.cursorrules의 Domain Layer 컨벤션 중 Lombok 금지만 필수 준수.
```

---

## 📊 예상 산출물

### 파일 구조
```
domain/
├── src/
│   ├── main/java/com/company/template/domain/order/
│   │   ├── Order.java (Aggregate Root)
│   │   ├── OrderId.java (ValueObject)
│   │   ├── OrderStatus.java (Enum)
│   │   ├── OrderLineItem.java (ValueObject)
│   │   ├── Money.java (ValueObject)
│   │   └── ProductId.java (ValueObject)
│   │
│   ├── testFixtures/...
│   └── test/...
```

### 테스트 실행 결과
```bash
$ ./gradlew :domain:test

BUILD SUCCESSFUL

5 tests completed, 5 passed ← GREEN 상태! ✅
```

---

## 🔗 다음 단계

1. **GREEN Phase 완료 확인**
   - [ ] 모든 테스트 통과 (GREEN 상태)
   - [ ] Lombok 사용하지 않음
   - [ ] 컴파일 에러 없음

2. **REFACTOR Phase로 이동**
   ```bash
   # Cursor Composer에서
   "/cr/refactor 실행해줘"
   ```

---

**✅ GREEN Phase는 "테스트를 빠르게 통과"시키는 단계입니다. 품질은 REFACTOR에서!**
