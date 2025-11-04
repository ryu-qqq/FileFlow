# Domain Test Fixture 패턴

**목적**: Domain 객체(Aggregate, Entity, Value Object)의 테스트 생성을 간소화

**위치**: `domain/src/testFixtures/java/com/ryuqq/domain/{aggregate}/fixture/`

**관련 문서**:
- [Object Mother 패턴](04_object-mother-pattern.md) - 비즈니스 시나리오 표현
- [Testing Support Toolkit](00_testing-support-toolkit.md) - 테스트 유틸리티
- [Aggregate Testing](01_aggregate-testing.md) - Aggregate 테스트 가이드

---

## 📌 핵심 원칙

### Fixture vs Object Mother

Domain Layer에서는 **2가지 테스트 객체 생성 패턴**을 사용합니다:

| 패턴 | 목적 | 생성 방법 | 예시 | 사용 시기 |
|------|------|----------|------|----------|
| **Fixture** | 기본 데이터 생성 | `createWithId(1L)` | `OrderFixture.createWithId(1L)` | 단위 테스트, 단순 데이터 |
| **Object Mother** | 비즈니스 시나리오 | `approvedOrder()` | `Orders.approvedOrder()` | 통합 테스트, 복잡한 시나리오 |

**선택 기준**:
- ✅ **Fixture**: 특정 필드만 설정, 비즈니스 맥락 불필요
- ✅ **Object Mother**: 여러 단계 상태 전이, 비즈니스 의미 명확히 표현

---

## ✅ Fixture 패턴 (Data-Centric)

### 사용 시기

- **단순 데이터 준비**: ID, 이름, 상태 등 기본 필드만 설정
- **단위 테스트**: 특정 메서드만 검증 (비즈니스 맥락 불필요)
- **Value Object 생성**: `Money`, `Email`, `Address` 등
- **빠른 테스트 작성**: Given 단계를 최소화

---

## 🏗️ Fixture 클래스 작성

### 기본 템플릿

```java
package com.ryuqq.domain.order.fixture;

import com.ryuqq.domain.order.*;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Order Aggregate Test Fixture
 *
 * <p>Order 객체의 기본 데이터를 생성하는 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * Order order = OrderFixture.create();
 * Order order = OrderFixture.createWithId(1L);
 * Order order = OrderFixture.createWithCustomer(customerId);
 * }</pre>
 *
 * <h3>복잡한 시나리오:</h3>
 * <p>복잡한 비즈니스 시나리오는 {@link Orders} Object Mother를 사용하세요.</p>
 *
 * @see Orders Object Mother 패턴 (비즈니스 시나리오용)
 * @author development-team
 * @since 1.0.0
 */
public class OrderFixture {

    /**
     * 기본값으로 Order 생성 (신규 엔티티, ID = null)
     */
    public static Order create() {
        return createWithCustomer(CustomerId.of(1L));
    }

    /**
     * 특정 고객으로 Order 생성 (신규 엔티티)
     */
    public static Order createWithCustomer(CustomerId customerId) {
        return Order.forNew(customerId);
    }

    /**
     * ID 포함하여 생성 (기존 엔티티, 조회 시나리오용)
     */
    public static Order createWithId(Long id) {
        return createWithId(id, CustomerId.of(1L));
    }

    /**
     * ID와 고객 지정하여 생성
     */
    public static Order createWithId(Long id, CustomerId customerId) {
        return Order.reconstitute(
            OrderId.of(id),
            customerId,
            OrderStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }

    /**
     * 상태 지정하여 생성
     *
     * <p><strong>주의</strong>: 상태만 변경, 비즈니스 로직 스킵</p>
     * <p><strong>권장</strong>: 복잡한 시나리오는 {@link Orders} Object Mother 사용</p>
     */
    public static Order createWithStatus(OrderStatus status) {
        return Order.reconstitute(
            OrderId.of(1L),
            CustomerId.of(1L),
            status,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }

    /**
     * 여러 개 생성 (bulk 테스트용)
     */
    public static Order[] createMultiple(int count) {
        Order[] orders = new Order[count];
        for (int i = 0; i < count; i++) {
            orders[i] = createWithId((long) (i + 1));
        }
        return orders;
    }

    /**
     * ID 시작 값 지정하여 여러 개 생성
     */
    public static Order[] createMultipleWithId(long startId, int count) {
        Order[] orders = new Order[count];
        for (int i = 0; i < count; i++) {
            orders[i] = createWithId(startId + i);
        }
        return orders;
    }

    // Private 생성자 - 인스턴스화 방지
    private OrderFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### 필수 요소

1. **static 메서드**: 모든 Fixture 메서드는 `static`이어야 함
2. **create*() 네이밍**: `create`로 시작하는 메서드명 필수
3. **Private 생성자**: 인스턴스화 방지
4. **Javadoc**: 사용 예시 및 Object Mother 참조 포함

---

## 🎯 Fixture 사용 예시

### 단위 테스트 (단순 검증)

```java
@Test
void updateCustomer_WithValidCustomer_ShouldUpdateCustomer() {
    // Given - Fixture로 기본 데이터 생성
    Order order = OrderFixture.createWithId(1L);
    CustomerId newCustomerId = CustomerId.of(999L);

    // When
    order.updateCustomer(newCustomerId);

    // Then
    assertThat(order.getCustomerId()).isEqualTo(newCustomerId);
}
```

---

### Value Object 생성

```java
/**
 * Money Value Object Fixture
 */
public class MoneyFixture {

    public static Money create() {
        return Money.of(10000);
    }

    public static Money createWithAmount(long amount) {
        return Money.of(amount);
    }

    public static Money zero() {
        return Money.of(0);
    }

    private MoneyFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

## ⚠️ Fixture 사용 시 주의사항

### ❌ Bad - 복잡한 비즈니스 시나리오를 Fixture로 표현

```java
// ❌ Bad - 가독성 저하
@Test
void ship_WhenOrderIsApproved_ShouldTransitionToShipped() {
    // Given - 여러 단계를 거쳐야 함 (비즈니스 의미 불명확)
    Order order = OrderFixture.createWithStatus(OrderStatus.APPROVED);
    // 이 주문이 어떻게 승인되었는지? 결제는? 상품은?

    // When
    order.ship(ShippingInfo.of("CJ대한통운", "123456789"));

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
}
```

**문제점**:
- ❌ `createWithStatus(APPROVED)`가 무엇을 의미하는지 불명확
- ❌ 승인 과정 (상품 추가, 승인 메서드 호출)이 생략됨
- ❌ 테스트만 봐도 비즈니스 흐름을 이해할 수 없음

---

### ✅ Good - 단순 데이터 준비에만 Fixture 사용

```java
// ✅ Good - 단순한 데이터 준비
@Test
void updateCustomer_WithValidCustomer_ShouldUpdateCustomer() {
    // Given - 단순한 데이터만 필요
    Order order = OrderFixture.createWithId(1L);
    CustomerId newCustomerId = CustomerId.of(999L);

    // When
    order.updateCustomer(newCustomerId);

    // Then
    assertThat(order.getCustomerId()).isEqualTo(newCustomerId);
}
```

**복잡한 시나리오는 Object Mother 사용!**
```java
// ✅ Good - Object Mother 사용
@Test
void ship_WhenOrderIsApproved_ShouldTransitionToShipped() {
    // Given - 비즈니스 의미 명확 ("승인된 주문"이라는 명확한 상태)
    Order order = Orders.approvedOrder();

    // When
    order.ship(ShippingInfo.of("CJ대한통운", "123456789"));

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
}
```

**참고**: [04_object-mother-pattern.md](04_object-mother-pattern.md)

---

## 📋 네이밍 규칙

### 클래스명: `*Fixture`

```java
// ✅ 올바른 네이밍
OrderFixture.java
CustomerFixture.java
MoneyFixture.java
AddressFixture.java

// ❌ 잘못된 네이밍
OrderFactory.java      // Factory는 금지
OrderBuilder.java      // Builder는 금지
OrderTestData.java     // TestData는 금지
TestOrder.java         // Test 접두사는 금지
```

---

### 메서드명: `create*()`

```java
// ✅ 올바른 메서드명
create()                    // 기본값으로 생성
createWithId(Long)          // ID 지정
createWithCustomer(...)     // 특정 값 지정
createWithStatus(...)       // 상태 지정
createMultiple(int)         // 여러 개 생성

// ❌ 잘못된 메서드명
build()                     // build는 금지
of()                        // of는 금지 (Domain 객체 전용)
order()                     // 타입명만 사용 금지
getOrder()                  // get 접두사 금지
newOrder()                  // new 접두사는 forNew() 패턴과 혼동
```

---

## 🔧 고급 패턴

### 패턴 1: Clock 주입 (결정론적 테스트)

```java
public class OrderFixture {

    /**
     * 고정된 시간으로 Order 생성 (테스트용)
     */
    public static Order createWithClock(Clock clock) {
        return Order.reconstitute(
            OrderId.of(1L),
            CustomerId.of(1L),
            OrderStatus.PENDING,
            LocalDateTime.now(clock),
            LocalDateTime.now(clock),
            false
        );
    }
}

// 사용 예시
@Test
void test_WithFixedTime() {
    Clock fixedClock = ClockFixtures.fixedAt("2025-10-16T10:00:00Z");
    Order order = OrderFixture.createWithClock(fixedClock);

    assertThat(order.getCreatedAt()).isEqualTo(
        LocalDateTime.parse("2025-10-16T10:00:00")
    );
}
```

---

### 패턴 2: Builder 스타일 (선택적)

```java
/**
 * Fixture Builder (복잡한 설정이 필요한 경우)
 *
 * <p>주의: 간단한 경우 createWith*() 메서드 권장</p>
 */
public static class Builder {
    private Long id = 1L;
    private CustomerId customerId = CustomerId.of(1L);
    private OrderStatus status = OrderStatus.PENDING;

    public Builder id(Long id) {
        this.id = id;
        return this;
    }

    public Builder customerId(CustomerId customerId) {
        this.customerId = customerId;
        return this;
    }

    public Builder status(OrderStatus status) {
        this.status = status;
        return this;
    }

    public Order build() {
        return Order.reconstitute(
            OrderId.of(id),
            customerId,
            status,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }
}

public static Builder builder() {
    return new Builder();
}

// 사용 예시
Order order = OrderFixture.builder()
    .id(999L)
    .customerId(CustomerId.of(123L))
    .status(OrderStatus.APPROVED)
    .build();
```

---

## 📋 체크리스트

### Fixture 클래스 작성 체크리스트

- [ ] 클래스명에 `Fixture` 접미사 사용
- [ ] `testFixtures/java/.../fixture/` 패키지에 위치
- [ ] 모든 메서드는 `static`으로 선언
- [ ] 기본 생성 메서드 `create()` 제공
- [ ] 커스터마이징 메서드 `createWith*()` 제공
- [ ] Private 생성자로 인스턴스화 방지
- [ ] Javadoc에 사용 예시 및 Object Mother 참조 포함
- [ ] ⚠️ 복잡한 비즈니스 시나리오는 Object Mother 사용

---

## 📚 관련 문서

**다음 단계**:
- [04_object-mother-pattern.md](04_object-mother-pattern.md) - 비즈니스 시나리오 표현

**관련 가이드**:
- [00_testing-support-toolkit.md](00_testing-support-toolkit.md) - 테스트 유틸리티
- [01_aggregate-testing.md](01_aggregate-testing.md) - Aggregate 테스트 가이드
- [02_value-object-testing.md](02_value-object-testing.md) - Value Object 테스트

---

**작성자**: Development Team
**최종 수정일**: 2025-10-16
**버전**: 1.0.0
