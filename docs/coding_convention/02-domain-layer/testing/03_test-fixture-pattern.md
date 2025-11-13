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

## 🏷️ 테스트 조직화 패턴

### 1. @Tag - 테스트 카테고리화

**목적**: 테스트를 카테고리별로 분류하여 선택적 실행 가능

**사용 가능한 태그**:
```java
@Tag("unit")           // 단위 테스트
@Tag("domain")         // 도메인 테스트
@Tag("integration")    // 통합 테스트
@Tag("slow")           // 느린 테스트
@Tag("fast")           // 빠른 테스트
```

**예시**:
```java
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
class OrderTest {

    @Test
    @Tag("fast")
    void create_WithValidData_ShouldCreateOrder() {
        // Given
        CustomerId customerId = CustomerId.of(1L);

        // When
        Order order = Order.forNew(customerId);

        // Then
        assertThat(order.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @Tag("slow")
    void validate_WithComplexRules_ShouldValidate() {
        // ... 복잡한 검증 로직
    }
}
```

**Gradle 설정** (선택적 실행):
```groovy
// build.gradle
test {
    useJUnitPlatform {
        includeTags 'unit'           // unit 태그만 실행
        excludeTags 'slow'           // slow 태그 제외
    }
}
```

---

### 2. @Nested - 관심사별 그룹핑

**목적**: 관련된 테스트를 논리적으로 그룹화하여 가독성 향상

**사용 시기**:
- 생성 관련 테스트
- 검증 관련 테스트
- 비즈니스 로직별 테스트
- 예외 케이스 테스트

**예시**:
```java
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("Order 도메인 테스트")
class OrderTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 데이터로 생성 시 성공")
        void create_WithValidData_ShouldSucceed() {
            // Given
            CustomerId customerId = CustomerId.of(1L);

            // When
            Order order = Order.forNew(customerId);

            // Then
            assertThat(order.getCustomerId()).isEqualTo(customerId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("null 고객 ID로 생성 시 예외 발생")
        void create_WithNullCustomerId_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> Order.forNew(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("고객 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("상태 변경 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("PENDING → APPROVED 전이 성공")
        void approve_FromPending_ShouldTransitionToApproved() {
            // Given
            Order order = OrderFixture.createWithStatus(OrderStatus.PENDING);

            // When
            order.approve();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
        }

        @Test
        @DisplayName("CANCELLED 상태에서 승인 시 예외 발생")
        void approve_FromCancelled_ShouldThrowException() {
            // Given
            Order order = OrderFixture.createWithStatus(OrderStatus.CANCELLED);

            // When & Then
            assertThatThrownBy(() -> order.approve())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("취소된 주문은 승인할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 검증 테스트")
    class BusinessRuleTests {

        @Test
        @DisplayName("최소 주문 금액 미달 시 예외 발생")
        void validate_BelowMinimumAmount_ShouldThrowException() {
            // Given
            Order order = OrderFixture.create();
            Money minimumAmount = Money.of(5000);

            // When & Then
            assertThatThrownBy(() -> order.validateMinimumAmount(minimumAmount))
                .isInstanceOf(IllegalStateException.class);
        }
    }
}
```

**@Nested 구조 권장사항**:
- ✅ 테스트 클래스당 2-5개의 Nested 클래스 (너무 많으면 분리 고려)
- ✅ Nested 클래스명은 명확하게 (`CreateTests`, `ValidationTests`)
- ✅ @DisplayName으로 한글 설명 추가 (가독성 향상)
- ❌ Nested 안에 Nested는 지양 (깊이 1단계까지만)

---

### 3. @ParameterizedTest - 여러 케이스 테스트

**목적**: 동일한 테스트 로직을 여러 입력값으로 반복 실행

**사용 시기**:
- 경계값 테스트 (Boundary Value Testing)
- 동등 분할 테스트 (Equivalence Partitioning)
- 여러 유효/무효 입력값 검증
- 다양한 상태 조합 테스트

#### 패턴 1: @ValueSource (단일 파라미터)

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ParameterizedTest
@ValueSource(longs = {-1L, 0L, -100L})
@DisplayName("음수 또는 0인 ID로 생성 시 예외 발생")
void of_WithInvalidId_ShouldThrowException(Long invalidId) {
    // When & Then
    assertThatThrownBy(() -> OrderId.of(invalidId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order ID는 양수여야 합니다");
}
```

#### 패턴 2: @CsvSource (여러 파라미터)

```java
import org.junit.jupiter.params.provider.CsvSource;

@ParameterizedTest
@CsvSource({
    "PENDING,    true,   승인 가능",
    "APPROVED,   false,  이미 승인됨",
    "CANCELLED,  false,  취소된 주문",
    "SHIPPED,    false,  배송 중"
})
@DisplayName("주문 상태별 승인 가능 여부 검증")
void canApprove_WithVariousStatuses_ShouldReturnExpectedResult(
    OrderStatus status,
    boolean expectedResult,
    String description
) {
    // Given
    Order order = OrderFixture.createWithStatus(status);

    // When
    boolean result = order.canApprove();

    // Then
    assertThat(result).isEqualTo(expectedResult);
}
```

#### 패턴 3: @MethodSource (복잡한 객체)

```java
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

@ParameterizedTest
@MethodSource("provideInvalidOrders")
@DisplayName("유효하지 않은 주문 데이터로 생성 시 예외 발생")
void create_WithInvalidData_ShouldThrowException(
    CustomerId customerId,
    String expectedMessage
) {
    // When & Then
    assertThatThrownBy(() -> Order.forNew(customerId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedMessage);
}

private static Stream<Arguments> provideInvalidOrders() {
    return Stream.of(
        Arguments.of(null, "고객 ID는 필수입니다"),
        Arguments.of(CustomerId.of(-1L), "고객 ID는 양수여야 합니다")
    );
}
```

#### 패턴 4: @EnumSource (Enum 전체 테스트)

```java
import org.junit.jupiter.params.provider.EnumSource;

@ParameterizedTest
@EnumSource(OrderStatus.class)
@DisplayName("모든 주문 상태에 대해 toString() 반환값 검증")
void toString_WithAllStatuses_ShouldReturnNonEmpty(OrderStatus status) {
    // When
    String result = status.toString();

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
}

@ParameterizedTest
@EnumSource(
    value = OrderStatus.class,
    names = {"APPROVED", "SHIPPED", "DELIVERED"}
)
@DisplayName("특정 상태만 테스트")
void test_OnlySpecificStatuses(OrderStatus status) {
    // ...
}
```

---

### 통합 예시: 모든 패턴 결합

```java
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Tag("unit")
@Tag("domain")
@DisplayName("Order 도메인 종합 테스트")
class OrderComprehensiveTest {

    @Nested
    @DisplayName("ID 생성 테스트")
    class OrderIdCreationTests {

        @ParameterizedTest
        @ValueSource(longs = {1L, 100L, 999999L})
        @DisplayName("유효한 ID로 생성 성공")
        void of_WithValidId_ShouldSucceed(Long validId) {
            // When
            OrderId orderId = OrderId.of(validId);

            // Then
            assertThat(orderId.value()).isEqualTo(validId);
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L, -100L})
        @DisplayName("무효한 ID로 생성 시 예외 발생")
        void of_WithInvalidId_ShouldThrowException(Long invalidId) {
            // When & Then
            assertThatThrownBy(() -> OrderId.of(invalidId))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("forNew()로 null ID 생성 성공")
        void forNew_ShouldCreateWithNullId() {
            // When
            OrderId orderId = OrderId.forNew();

            // Then
            assertThat(orderId.value()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTests {

        @ParameterizedTest
        @CsvSource({
            "PENDING,    APPROVED,   true",
            "APPROVED,   SHIPPED,    true",
            "SHIPPED,    DELIVERED,  true",
            "CANCELLED,  APPROVED,   false",
            "DELIVERED,  CANCELLED,  false"
        })
        @DisplayName("상태 전이 가능 여부 검증")
        void canTransition_WithVariousStates_ShouldReturnExpected(
            OrderStatus from,
            OrderStatus to,
            boolean expected
        ) {
            // Given
            Order order = OrderFixture.createWithStatus(from);

            // When
            boolean result = order.canTransitionTo(to);

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTests {

        @Test
        @Tag("fast")
        @DisplayName("기본 생성 테스트")
        void create_WithDefaultValues_ShouldSucceed() {
            // When
            Order order = OrderFixture.create();

            // Then
            assertThat(order).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @Tag("slow")
        @DisplayName("복잡한 비즈니스 규칙 검증")
        void validate_WithComplexRules_ShouldPass() {
            // ... 복잡한 로직
        }
    }
}
```

---

### 테스트 조직화 체크리스트

- [ ] 클래스 레벨에 `@Tag("unit")`, `@Tag("domain")` 추가
- [ ] 관련 테스트는 `@Nested` 클래스로 그룹핑
- [ ] 각 Nested 클래스에 `@DisplayName` 추가 (한글 권장)
- [ ] 동일 로직의 여러 케이스는 `@ParameterizedTest` 사용
- [ ] @ValueSource, @CsvSource, @MethodSource, @EnumSource 적절히 선택
- [ ] 느린 테스트는 `@Tag("slow")` 추가하여 선택적 실행 지원
- [ ] 테스트 메서드명은 `메서드_조건_결과` 패턴 사용

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
