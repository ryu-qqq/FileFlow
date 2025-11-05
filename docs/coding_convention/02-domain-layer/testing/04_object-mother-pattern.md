# Object Mother 패턴 (비즈니스 시나리오 표현)

**목적**: 복잡한 비즈니스 시나리오를 의미 있는 이름으로 표현

**위치**: `domain/src/testFixtures/java/com/ryuqq/domain/{aggregate}/mother/`

**관련 문서**:
- [Test Fixture 패턴](03_test-fixture-pattern.md) - 기본 데이터 생성
- [Aggregate Testing](01_aggregate-testing.md) - Aggregate 테스트 가이드
- [Testing Support Toolkit](00_testing-support-toolkit.md) - 테스트 유틸리티

---

## 📌 핵심 개념

### Object Mother란?

**Object Mother**는 **"비즈니스적으로 의미 있는 상태"**를 가진 도메인 객체를 생성하는 패턴입니다.

Martin Fowler가 2006년에 소개한 패턴으로, 테스트 코드의 가독성과 유지보수성을 크게 향상시킵니다.

---

### Fixture vs Object Mother

| 구분 | Fixture | Object Mother |
|------|---------|---------------|
| **목적** | 기본 데이터 생성 | 비즈니스 시나리오 표현 |
| **네이밍** | `createWithId(1L)` | `approvedOrder()` |
| **복잡도** | 단순 (1-2 필드 설정) | 복잡 (여러 단계 상태 전이) |
| **비즈니스 의미** | 없음 (데이터 중심) | 있음 (시나리오 중심) |
| **테스트 가독성** | 낮음 | 높음 (시나리오가 명확) |
| **패키지** | `fixture/` | `mother/` |

---

## ✅ Object Mother 패턴

### 사용 시기

다음 조건 **2개 이상** 해당 시 Object Mother 사용:

- [ ] **복잡한 비즈니스 시나리오**: 승인된 주문, 취소된 주문, 배송 중인 주문
- [ ] **여러 단계 상태 전이**: 생성 → 승인 → 결제 → 배송
- [ ] **테스트 가독성 중요**: Given 단계에서 비즈니스 맥락 명확히 표현
- [ ] **통합 테스트**: End-to-End 시나리오 검증
- [ ] **도메인 이벤트 검증**: 특정 비즈니스 상태에서 발행되는 이벤트 확인

---

## 🏗️ Object Mother 클래스 작성

### 기본 템플릿

```java
package com.ryuqq.domain.order.mother;

import com.ryuqq.domain.order.*;
import com.ryuqq.domain.order.fixture.OrderFixture;
import com.ryuqq.domain.product.ProductId;
import com.ryuqq.domain.customer.CustomerId;
import com.ryuqq.domain.common.Money;
import com.ryuqq.domain.common.Quantity;

/**
 * Order Object Mother - 비즈니스 시나리오 표현
 *
 * <p>비즈니스적으로 의미 있는 상태의 Order를 생성하는 클래스입니다.
 * 복잡한 상태 전이를 거친 주문 객체를 명확한 이름으로 표현합니다.</p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * Order order = Orders.pendingOrder();       // 대기 중인 주문
 * Order order = Orders.approvedOrder();      // 승인된 주문
 * Order order = Orders.shippedOrder();       // 배송 중인 주문
 * Order order = Orders.completedOrder();     // 완료된 주문
 * Order order = Orders.cancelledOrder();     // 취소된 주문
 * }</pre>
 *
 * <h3>네이밍 원칙:</h3>
 * <ul>
 *   <li>클래스명: 복수형 명사 (Orders, Customers, Payments)</li>
 *   <li>메서드명: 비즈니스 상태 표현 (approvedOrder, cancelledOrder)</li>
 *   <li>비즈니스 로직 사용: 실제 메서드 호출 (Reflection 금지)</li>
 * </ul>
 *
 * @see OrderFixture 단순 데이터 생성용
 * @author development-team
 * @since 1.0.0
 */
public class Orders {

    /**
     * 대기 중인 주문 (생성 직후 상태)
     *
     * <p><strong>비즈니스 상태</strong>: 주문 생성됨, 상품 추가 대기</p>
     */
    public static Order pendingOrder() {
        return OrderFixture.create();
    }

    /**
     * 승인된 주문 (결제 완료 후 상태)
     *
     * <p><strong>비즈니스 로직</strong>:</p>
     * <ol>
     *   <li>주문 생성</li>
     *   <li>상품 추가</li>
     *   <li>승인 (approve 메서드 호출)</li>
     * </ol>
     *
     * <p><strong>비즈니스 상태</strong>: 결제 완료, 배송 대기</p>
     */
    public static Order approvedOrder() {
        Order order = OrderFixture.createWithCustomer(CustomerId.of(1L));

        // ✅ 비즈니스 로직을 통한 상태 전이
        order.addItem(ProductId.of(101L), Quantity.of(1), Money.of(10000));
        order.approve();

        return order;
    }

    /**
     * 승인된 주문 (특정 고객, 특정 금액)
     *
     * <p><strong>파라미터화</strong>: 테스트 시나리오에 맞게 커스터마이징</p>
     */
    public static Order approvedOrder(CustomerId customerId, Money totalAmount) {
        Order order = OrderFixture.createWithCustomer(customerId);
        order.addItem(ProductId.of(101L), Quantity.of(1), totalAmount);
        order.approve();
        return order;
    }

    /**
     * 배송 중인 주문
     *
     * <p><strong>비즈니스 로직</strong>:</p>
     * <ol>
     *   <li>승인된 주문 생성</li>
     *   <li>배송 정보 등록 및 배송 시작</li>
     * </ol>
     *
     * <p><strong>비즈니스 상태</strong>: 배송 중, 완료 대기</p>
     */
    public static Order shippedOrder() {
        Order order = approvedOrder();

        // ✅ 비즈니스 로직: 승인 → 배송
        order.ship(ShippingInfo.of("CJ대한통운", "123456789"));

        return order;
    }

    /**
     * 완료된 주문 (배송 완료)
     *
     * <p><strong>비즈니스 로직</strong>:</p>
     * <ol>
     *   <li>배송 중인 주문 생성</li>
     *   <li>배송 완료 처리</li>
     * </ol>
     *
     * <p><strong>비즈니스 상태</strong>: 주문 완료, 종료 상태</p>
     */
    public static Order completedOrder() {
        Order order = shippedOrder();

        // ✅ 비즈니스 로직: 배송 중 → 완료
        order.complete();

        return order;
    }

    /**
     * 취소된 주문
     *
     * <p><strong>비즈니스 로직</strong>:</p>
     * <ol>
     *   <li>대기 중인 주문 생성</li>
     *   <li>상품 추가</li>
     *   <li>주문 취소</li>
     * </ol>
     *
     * <p><strong>비즈니스 상태</strong>: 주문 취소됨, 종료 상태</p>
     */
    public static Order cancelledOrder() {
        Order order = pendingOrder();
        order.addItem(ProductId.of(101L), Quantity.of(1), Money.of(10000));

        // ✅ 비즈니스 로직: 취소
        order.cancel("고객 요청");

        return order;
    }

    /**
     * 취소된 주문 (특정 사유)
     */
    public static Order cancelledOrder(String reason) {
        Order order = pendingOrder();
        order.addItem(ProductId.of(101L), Quantity.of(1), Money.of(10000));
        order.cancel(reason);
        return order;
    }

    /**
     * 취소 불가능한 주문 (배송 중인 주문)
     *
     * <p><strong>테스트 용도</strong>: 취소 불가 상태 검증</p>
     */
    public static Order nonCancellableOrder() {
        return shippedOrder();  // 배송 중인 주문은 취소 불가
    }

    /**
     * 다량 주문 (VIP 고객용)
     *
     * <p><strong>비즈니스 시나리오</strong>: 5개 이상 상품 주문</p>
     */
    public static Order bulkOrder() {
        Order order = OrderFixture.createWithCustomer(CustomerId.of(1L));

        // ✅ 비즈니스 로직: 다량 상품 추가
        order.addItem(ProductId.of(101L), Quantity.of(10), Money.of(100000));
        order.addItem(ProductId.of(102L), Quantity.of(5), Money.of(50000));
        order.approve();

        return order;
    }

    // Private 생성자 - 인스턴스화 방지
    private Orders() {
        throw new AssertionError("Object Mother 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

## 🎯 실전 사용 예시

### Before (Fixture만 사용)

```java
@Test
void ship_WhenOrderIsApproved_ShouldTransitionToShipped() {
    // Given - ❌ 비즈니스 의미 불명확
    Order order = OrderFixture.createWithStatus(OrderStatus.APPROVED);
    ShippingInfo shippingInfo = ShippingInfo.of("CJ대한통운", "123456789");

    // When
    order.ship(shippingInfo);

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
}
```

**문제점**:
- ❌ `createWithStatus(APPROVED)`가 무엇을 의미하는지 불명확
- ❌ 승인된 주문이 어떤 상태인지 알 수 없음 (상품은? 결제는?)
- ❌ 테스트 가독성 저하

---

### After (Object Mother 사용)

```java
@Test
void ship_WhenOrderIsApproved_ShouldTransitionToShipped() {
    // Given - ✅ 비즈니스 의미 명확
    Order order = Orders.approvedOrder();  // ✅ "승인된 주문"이라는 명확한 의미
    ShippingInfo shippingInfo = ShippingInfo.of("CJ대한통운", "123456789");

    // When
    order.ship(shippingInfo);

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
}
```

**장점**:
- ✅ `approvedOrder()`가 비즈니스 시나리오를 명확히 표현
- ✅ 테스트 가독성 향상 (Given 단계만 봐도 무엇을 테스트하는지 이해)
- ✅ 비즈니스 로직 재사용 (상품 추가 + 승인 메서드 호출)

---

## 📊 네이밍 규칙

### 클래스명: 복수형 명사

```java
// ✅ Good
Orders.approvedOrder()
Customers.activeCustomer()
Payments.completedPayment()
Invoices.paidInvoice()
Shipments.inTransitShipment()

// ❌ Bad
OrderMother.approvedOrder()    // Mother 접미사 불필요
OrderFactory.approvedOrder()   // Factory는 다른 의미
OrderCreator.approvedOrder()   // Creator는 불필요
```

**이유**:
- `Orders`는 "주문들의 집합"을 의미
- Martin Fowler의 Object Mother 패턴 원본 스타일
- 간결하고 자연스러운 네이밍

---

### 메서드명: 비즈니스 시나리오 표현

```java
// ✅ Good - 비즈니스 상태 명확
Orders.pendingOrder()      // 대기 중
Orders.approvedOrder()     // 승인됨
Orders.shippedOrder()      // 배송 중
Orders.completedOrder()    // 완료됨
Orders.cancelledOrder()    // 취소됨

Customers.activeCustomer()      // 활성 고객
Customers.suspendedCustomer()   // 정지된 고객
Customers.deletedCustomer()     // 삭제된 고객

Payments.completedPayment()     // 완료된 결제
Payments.failedPayment()        // 실패한 결제
Payments.refundedPayment()      // 환불된 결제

// ❌ Bad - 데이터 중심 (Fixture 스타일)
Orders.createWithStatus(OrderStatus.APPROVED)  // ❌ Object Mother에서 사용 금지
Orders.createApproved()                        // ❌ create 접두사 불필요
Orders.getApprovedOrder()                      // ❌ get 접두사 금지
```

---

## 🔧 고급 패턴

### 패턴 1: 파라미터화된 시나리오

```java
/**
 * 특정 금액의 승인된 주문
 */
public static Order approvedOrder(Money totalAmount) {
    Order order = OrderFixture.createWithCustomer(CustomerId.of(1L));
    order.addItem(ProductId.of(101L), Quantity.of(1), totalAmount);
    order.approve();
    return order;
}

/**
 * 특정 고객의 승인된 주문
 */
public static Order approvedOrder(CustomerId customerId) {
    Order order = OrderFixture.createWithCustomer(customerId);
    order.addItem(ProductId.of(101L), Quantity.of(1), Money.of(10000));
    order.approve();
    return order;
}
```

**사용**:
```java
// 5만원 주문 테스트
Order order = Orders.approvedOrder(Money.of(50000));

// 특정 고객 주문 테스트
Order order = Orders.approvedOrder(CustomerId.of(999L));
```

---

### 패턴 2: 체이닝 (선택적)

```java
/**
 * 승인된 주문에 특정 상품 포함
 */
public static Order approvedOrderWith(ProductId productId, Quantity quantity, Money price) {
    Order order = OrderFixture.createWithCustomer(CustomerId.of(1L));
    order.addItem(productId, quantity, price);
    order.approve();
    return order;
}

/**
 * 승인된 주문에 여러 상품 포함
 */
public static Order approvedOrderWith(List<OrderLineItem> items) {
    Order order = OrderFixture.createWithCustomer(CustomerId.of(1L));
    items.forEach(item -> order.addItem(item.getProductId(), item.getQuantity(), item.getPrice()));
    order.approve();
    return order;
}
```

---

### 패턴 3: 도메인 이벤트 검증용

```java
/**
 * 승인 시 이벤트 발행 검증용
 *
 * <p><strong>테스트 용도</strong>: OrderApproved 이벤트 발행 확인</p>
 */
public static Order orderReadyForApproval() {
    Order order = OrderFixture.createWithCustomer(CustomerId.of(1L));
    order.addItem(ProductId.of(101L), Quantity.of(1), Money.of(10000));
    // approve() 호출 전 상태 반환
    return order;
}

// 사용 예시
@Test
void approve_ShouldPublishOrderApprovedEvent() {
    // Given
    Order order = Orders.orderReadyForApproval();

    // When
    order.approve();

    // Then
    assertThat(eventsSpy.ofType(OrderApprovedEvent.class))
        .hasSize(1)
        .first()
        .satisfies(event -> {
            assertThat(event.orderId()).isEqualTo(order.getId());
        });
}
```

---

## 📋 체크리스트

### Object Mother 클래스 작성 체크리스트

- [ ] 클래스명은 **복수형 명사** (`Orders`, `Customers`, `Payments`)
- [ ] 메서드명은 **비즈니스 시나리오 표현** (`approvedOrder()`, `cancelledOrder()`)
- [ ] **비즈니스 로직을 통한 상태 전이** (Reflection 사용 금지)
- [ ] Fixture 재사용 (`OrderFixture.create()` 등)
- [ ] Private 생성자로 인스턴스화 방지
- [ ] Javadoc에 **비즈니스 로직 단계** 명시
- [ ] `mother/` 패키지에 위치
- [ ] 파라미터화 옵션 제공 (필요 시)

---

## ⚠️ 주의사항

### ❌ Reflection 사용 금지

```java
// ❌ Bad - Reflection으로 상태 변경
public static Order approvedOrder() {
    Order order = OrderFixture.createWithId(1L);
    ReflectionTestUtils.setField(order, "status", OrderStatus.APPROVED);  // ❌ 금지
    return order;
}

// ✅ Good - 비즈니스 로직 사용
public static Order approvedOrder() {
    Order order = OrderFixture.create();
    order.addItem(ProductId.of(101L), Quantity.of(1), Money.of(10000));
    order.approve();  // ✅ 비즈니스 메서드 호출
    return order;
}
```

**이유**:
- Object Mother는 **"실제 비즈니스 흐름"**을 재현해야 함
- Reflection은 비즈니스 로직을 우회하므로 금지
- 테스트가 실제 운영 환경과 동일한 방식으로 동작해야 함

---

### ❌ 과도한 파라미터화 지양

```java
// ❌ Bad - 파라미터가 너무 많음
public static Order approvedOrder(
    CustomerId customerId,
    ProductId productId,
    Quantity quantity,
    Money price,
    ShippingAddress address,
    PaymentMethod method
) {
    // 이건 사실상 Builder 패턴...
}

// ✅ Good - 기본 시나리오 + 필요 시 오버로딩
public static Order approvedOrder() {
    // 기본값 사용
}

public static Order approvedOrder(CustomerId customerId) {
    // 고객만 변경
}

public static Order approvedOrder(Money totalAmount) {
    // 금액만 변경
}
```

---

## 🎓 실전 예제: Customer Aggregate

```java
package com.ryuqq.domain.customer.mother;

import com.ryuqq.domain.customer.*;
import com.ryuqq.domain.customer.fixture.CustomerFixture;

/**
 * Customer Object Mother
 *
 * @author development-team
 * @since 1.0.0
 */
public class Customers {

    /**
     * 활성 고객 (일반 고객)
     */
    public static Customer activeCustomer() {
        return CustomerFixture.create();
    }

    /**
     * VIP 고객 (누적 구매 100만원 이상)
     */
    public static Customer vipCustomer() {
        Customer customer = CustomerFixture.create();
        customer.upgradeTo(CustomerTier.VIP);
        return customer;
    }

    /**
     * 정지된 고객 (약관 위반)
     */
    public static Customer suspendedCustomer() {
        Customer customer = activeCustomer();
        customer.suspend("약관 위반");
        return customer;
    }

    /**
     * 탈퇴한 고객
     */
    public static Customer deletedCustomer() {
        Customer customer = activeCustomer();
        customer.softDelete();
        return customer;
    }

    /**
     * 인증 완료 고객 (이메일 인증됨)
     */
    public static Customer verifiedCustomer() {
        Customer customer = CustomerFixture.create();
        customer.verifyEmail("verification-token-123");
        return customer;
    }

    private Customers() {
        throw new AssertionError("Object Mother 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

## 📚 관련 문서

**이전**:
- [03_test-fixture-pattern.md](03_test-fixture-pattern.md) - 기본 데이터 생성

**관련 가이드**:
- [01_aggregate-testing.md](01_aggregate-testing.md) - Aggregate 테스트 가이드
- [05_domain-event-testing.md](05_domain-event-testing.md) - 도메인 이벤트 테스트

---

**작성자**: Development Team
**최종 수정일**: 2025-10-16
**버전**: 1.0.0
