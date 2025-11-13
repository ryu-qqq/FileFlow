# /cr/refactor - REFACTOR Phase Guide (Cursor TDD)

**목적**: Kent Beck TDD의 REFACTOR Phase - 컨벤션 적용 및 코드 개선 가이드

**실행 환경**: Cursor Composer

**전제 조건**: GREEN Phase 완료 (모든 테스트 통과)

---

## 🎯 REFACTOR Phase 철학

> **"테스트가 통과하면, 이제 코드를 개선할 시간이다. 중복을 제거하고, 의도를 명확히 하라."**
> — Kent Beck

### 핵심 원칙
1. **GREEN 상태 유지** (테스트는 항상 통과 상태)
2. **컨벤션 적용** (Zero-Tolerance 규칙 100% 준수)
3. **점진적 개선** (한 번에 하나씩)
4. **테스트로 검증** (매 개선 후 테스트 실행)

---

## 📋 작업 흐름

### Step 1: GREEN 상태 확인

```bash
# 현재 모든 테스트가 통과하는지 확인
$ ./gradlew :domain:test

BUILD SUCCESSFUL in 3s

5 tests completed, 5 passed ← GREEN 상태 확인! ✅
```

**⚠️ 중요**: REFACTOR는 GREEN 상태에서만 시작!

---

### Step 2: Lombok 제거 확인 (Zero-Tolerance)

**검증 대상**: GREEN Phase에서 Lombok이 없는지 재확인

#### ❌ 제거해야 할 것
```java
// GREEN Phase에서 실수로 추가된 Lombok (절대 있으면 안 됨!)
@Data
@Builder
@Getter
@Setter
public class Order { ... }
```

#### ✅ Pure Java로 유지
```java
// GREEN Phase에서 이미 작성된 Pure Java (유지)
public class Order {

    private OrderId orderId;
    private OrderStatus status;

    // Pure Java getter (유지)
    public OrderId getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
```

**검증 명령어**:
```bash
# Lombok 어노테이션 검색 (결과 없어야 함)
grep -r "@Data\|@Builder\|@Getter\|@Setter" domain/src/main/java/
```

---

### Step 3: Law of Demeter 적용 (Tell, Don't Ask)

**목적**: Getter 체이닝 제거, 비즈니스 로직을 적절한 위치로 이동

#### ❌ Before (GREEN Phase에서 하드코딩된 로직)
```java
public class Order {

    private List<OrderLineItem> lineItems;

    // GREEN Phase: 단순 리턴 (하드코딩)
    public Money calculateTotalPrice() {
        return totalPrice; // 필드 그대로 리턴
    }

    public List<OrderLineItem> getLineItems() {
        return lineItems; // Getter 노출
    }
}

// 외부에서 계산 (Law of Demeter 위반 가능성)
Money total = order.getLineItems()
    .stream()
    .map(item -> item.getPrice())
    .reduce(Money.ZERO, Money::add);
```

#### ✅ After (REFACTOR Phase: 비즈니스 로직 내재화)
```java
public class Order {

    private List<OrderLineItem> lineItems;

    /**
     * 총 가격 계산
     * REFACTOR Phase: 실제 계산 로직 구현
     */
    public Money calculateTotalPrice() {
        return lineItems.stream()
            .map(OrderLineItem::calculatePrice)
            .reduce(Money.ZERO, Money::add);
    }

    /**
     * 라인 아이템 조회 (불변 컬렉션 반환)
     */
    public List<OrderLineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }
}

public class OrderLineItem {

    private Money unitPrice;
    private int quantity;

    /**
     * 라인 아이템 가격 계산
     * Tell, Don't Ask: 계산 로직을 객체 내부로
     */
    public Money calculatePrice() {
        return unitPrice.multiply(quantity);
    }
}
```

---

### Step 4: ValueObject 불변성 강화

**목적**: ValueObject의 불변성을 완전히 보장

#### ✅ REFACTOR 체크리스트
- [ ] 모든 필드 `final` 선언
- [ ] Private 생성자 유지
- [ ] Factory Method (`of()`) 유지
- [ ] `equals()` & `hashCode()` 구현 확인
- [ ] Getter만 제공 (Setter 없음)
- [ ] 불변 컬렉션 반환 (`Collections.unmodifiableList()`)

#### 예시: Money ValueObject
```java
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount; // final 확인

    private Money(BigDecimal amount) { // private 확인
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
        this.amount = amount;
    }

    // Factory Method
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * 금액 더하기 (새 객체 반환)
     * REFACTOR: 불변성 보장
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * 금액 곱하기 (새 객체 반환)
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
}
```

---

### Step 5: 비즈니스 로직 구조 개선

**목적**: GREEN Phase의 단순 구현을 실제 비즈니스 로직으로 교체

#### ❌ Before (GREEN Phase: 하드코딩)
```java
public class Order {

    private OrderStatus status;

    public void cancel() {
        // GREEN Phase: 간단한 검증
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("PLACED 상태의 주문만 취소할 수 있습니다");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

#### ✅ After (REFACTOR Phase: 명확한 구조)
```java
public class Order {

    private OrderStatus status;

    /**
     * 주문 취소
     *
     * @throws IllegalStateException PLACED 상태가 아닌 경우
     */
    public void cancel() {
        validateCancellable();
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 취소 가능 상태 검증
     * REFACTOR: 검증 로직 분리
     */
    private void validateCancellable() {
        if (!status.isCancellable()) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서는 주문을 취소할 수 없습니다", status)
            );
        }
    }
}

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

    /**
     * 취소 가능한 상태인지 확인
     * REFACTOR: 상태 로직을 Enum으로 이동
     */
    public boolean isCancellable() {
        return this == PLACED;
    }
}
```

---

### Step 6: Javadoc 보강

**목적**: 모든 public 클래스/메서드에 Javadoc 추가

#### ✅ REFACTOR Javadoc 체크리스트
- [ ] `@author` 추가
- [ ] `@since` 추가
- [ ] 클래스 설명 (책임, 역할)
- [ ] 메서드 설명 (목적, 파라미터, 반환값, 예외)
- [ ] 비즈니스 규칙 명시

#### 예시
```java
package com.company.template.domain.order;

import java.util.List;

/**
 * Order Aggregate Root
 *
 * <p>주문의 생명주기를 관리하고 주문 관련 비즈니스 규칙을 적용합니다.</p>
 *
 * <h3>주요 책임</h3>
 * <ul>
 *   <li>주문 생성 및 검증</li>
 *   <li>주문 상태 관리 (PLACED → CONFIRMED → SHIPPED → DELIVERED)</li>
 *   <li>주문 취소 규칙 적용</li>
 * </ul>
 *
 * <h3>불변 조건 (Invariants)</h3>
 * <ul>
 *   <li>주문은 최소 1개 이상의 라인 아이템을 포함해야 함</li>
 *   <li>PLACED 상태에서만 취소 가능</li>
 *   <li>총 가격은 라인 아이템 가격의 합계와 일치</li>
 * </ul>
 *
 * @author your-name
 * @since 2025-01-01
 */
public class Order {

    /**
     * 주문 생성 Factory Method
     *
     * @param orderId 주문 ID (null 불가, 양수)
     * @param lineItems 주문 라인 아이템 목록 (최소 1개 이상)
     * @param totalPrice 총 가격 (0 이상)
     * @return 생성된 주문 객체
     * @throws IllegalArgumentException 라인 아이템이 비어있는 경우
     */
    public static Order create(OrderId orderId, List<OrderLineItem> lineItems, Money totalPrice) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("주문은 최소 1개의 라인 아이템이 필요합니다");
        }
        return new Order(orderId, lineItems, totalPrice);
    }

    /**
     * 주문 취소
     *
     * <p>PLACED 상태의 주문만 취소할 수 있습니다.</p>
     *
     * @throws IllegalStateException PLACED 상태가 아닌 경우
     */
    public void cancel() {
        validateCancellable();
        this.status = OrderStatus.CANCELLED;
    }
}
```

---

### Step 7: 테스트 실행 및 GREEN 유지

**중요**: REFACTOR 작업 후 반드시 테스트 실행!

```bash
# 매 개선 작업 후 테스트 실행
$ ./gradlew :domain:test

BUILD SUCCESSFUL in 3s

5 tests completed, 5 passed ← GREEN 유지 확인! ✅
```

**⚠️ 주의**: 테스트가 실패하면 REFACTOR 중단 → 원인 파악 → 수정

---

### Step 8: forNew() 패턴 적용 (NEW)

**목적**: ID ValueObject와 Aggregate에 `forNew()` 패턴 추가

**배경**: 새로운 엔티티를 생성할 때 ID가 `null`이어야 하지만, 기존 `of()` 메서드는 `null`을 허용하지 않아 생성 과정에서 에러가 발생합니다.

#### ❌ Before (GREEN Phase: of() 메서드만 존재)
```java
// ID ValueObject
public record OrderId(Long value) {

    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Order ID는 양수여야 합니다");
        }
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }
}

// Aggregate
public class Order {
    private OrderId orderId;

    // 생성 시 문제 발생!
    public static Order create(...) {
        return new Order(OrderId.of(null), ...); // ❌ 에러!
    }
}
```

#### ✅ After (REFACTOR Phase: forNew() 패턴 추가)

**1. ID ValueObject에 forNew() 추가**
```java
/**
 * Order 식별자
 */
public record OrderId(Long value) {

    // Compact 생성자 (검증)
    public OrderId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Order ID는 양수여야 합니다");
        }
        // null 허용: 새로운 엔티티를 의미 (save 전)
    }

    /**
     * 새로운 엔티티를 위한 ID 생성 (아직 저장되지 않음)
     * REFACTOR: forNew() 패턴 추가
     */
    public static OrderId forNew() {
        return new OrderId(null);
    }

    /**
     * 기존 엔티티를 위한 ID 생성 (DB에서 로드됨)
     */
    public static OrderId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("저장된 엔티티 ID는 필수입니다");
        }
        return new OrderId(value);
    }
}
```

**2. Aggregate에 forNew() 추가**
```java
public class Order {

    private OrderId orderId;
    private CustomerId customerId;
    private OrderStatus status;

    // Private 생성자
    private Order(OrderId orderId, CustomerId customerId, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
    }

    /**
     * 새로운 주문 생성 (아직 저장되지 않음)
     * REFACTOR: forNew() 패턴 추가
     *
     * @param customerId 고객 ID
     * @return 새로운 주문 객체 (ID는 null)
     */
    public static Order forNew(CustomerId customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("고객 ID는 필수입니다");
        }
        return new Order(
            OrderId.forNew(),           // ID는 null (DB save 전)
            customerId,
            OrderStatus.PENDING
        );
    }

    /**
     * 기존 주문 재구성 (DB에서 로드됨)
     *
     * @param orderId 주문 ID (필수)
     * @param customerId 고객 ID
     * @param status 주문 상태
     * @return 재구성된 주문 객체
     */
    public static Order of(Long orderId, Long customerId, OrderStatus status) {
        return new Order(
            OrderId.of(orderId),        // ID 필수 검증
            CustomerId.of(customerId),
            status
        );
    }
}
```

**3. 사용 예시**
```java
// 새로운 주문 생성 (DB save 전)
Order newOrder = Order.forNew(CustomerId.of(1L));
assertThat(newOrder.getOrderId().value()).isNull(); // ✅ null 허용

// DB에 저장 후 ID 할당
Order savedOrder = orderRepository.save(newOrder);
assertThat(savedOrder.getOrderId().value()).isNotNull(); // ✅ ID 존재

// DB에서 로드된 주문
Order loadedOrder = Order.of(123L, 1L, OrderStatus.PLACED);
assertThat(loadedOrder.getOrderId().value()).isEqualTo(123L); // ✅ ID 필수
```

**✅ REFACTOR 체크리스트**:
- [ ] 모든 ID ValueObject에 `forNew()` 메서드 추가
- [ ] `of()` 메서드는 null 체크 유지
- [ ] Compact 생성자는 null 허용 (새 엔티티용)
- [ ] 모든 Aggregate에 `forNew()` 메서드 추가
- [ ] `forNew()`는 ID를 `null`로 초기화
- [ ] Javadoc 추가 ("새로운 엔티티", "아직 저장되지 않음")

---

### Step 9: 테스트 조직화 (NEW)

**목적**: 생성된 테스트를 @Tag, @Nested, @ParameterizedTest로 구조화

**배경**: GREEN Phase에서 생성된 테스트가 평면적으로 나열되어 있어 가독성과 유지보수성이 낮습니다.

#### ❌ Before (GREEN Phase: 평면적 테스트)
```java
class OrderTest {

    @Test
    void create_WithValidData_ShouldSucceed() { ... }

    @Test
    void create_WithNullCustomerId_ShouldThrowException() { ... }

    @Test
    void cancel_WithPlacedStatus_ShouldSucceed() { ... }

    @Test
    void cancel_WithApprovedStatus_ShouldThrowException() { ... }

    @Test
    void calculateTotalPrice_ShouldReturnSum() { ... }
}
```

#### ✅ After (REFACTOR Phase: 구조화된 테스트)

**1. @Tag 추가 (테스트 카테고리화)**
```java
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@Tag("unit")        // 단위 테스트
@Tag("domain")      // Domain Layer 테스트
@DisplayName("Order 도메인 테스트")
class OrderTest {

    @Test
    @Tag("fast")    // 빠른 테스트
    void create_WithValidData_ShouldSucceed() { ... }

    @Test
    @Tag("slow")    // 느린 테스트 (복잡한 검증)
    void validateBusinessRules_WithComplexScenario() { ... }
}
```

**2. @Nested 추가 (관심사별 그룹화)**
```java
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
        @DisplayName("PLACED 상태에서 취소 성공")
        void cancel_WithPlacedStatus_ShouldSucceed() { ... }

        @Test
        @DisplayName("APPROVED 상태에서 취소 시 예외 발생")
        void cancel_WithApprovedStatus_ShouldThrowException() { ... }
    }

    @Nested
    @DisplayName("비즈니스 로직 테스트")
    class BusinessLogicTests {

        @Test
        @DisplayName("총 가격 계산이 정확함")
        void calculateTotalPrice_ShouldReturnSum() { ... }
    }
}
```

**3. @ParameterizedTest 추가 (반복 테스트 간소화)**

**Pattern 1: @ValueSource (단일 파라미터)**
```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Nested
@DisplayName("ID 생성 테스트")
class OrderIdCreationTests {

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, -100L})
    @DisplayName("음수 또는 0인 ID로 생성 시 예외 발생")
    void of_WithInvalidId_ShouldThrowException(Long invalidId) {
        // When & Then
        assertThatThrownBy(() -> OrderId.of(invalidId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order ID는 양수여야 합니다");
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 999999L})
    @DisplayName("유효한 ID로 생성 성공")
    void of_WithValidId_ShouldSucceed(Long validId) {
        // When
        OrderId orderId = OrderId.of(validId);

        // Then
        assertThat(orderId.value()).isEqualTo(validId);
    }
}
```

**Pattern 2: @CsvSource (여러 파라미터)**
```java
import org.junit.jupiter.params.provider.CsvSource;

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
```

**Pattern 3: @EnumSource (Enum 전체 테스트)**
```java
import org.junit.jupiter.params.provider.EnumSource;

@Nested
@DisplayName("주문 상태 테스트")
class OrderStatusTests {

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
}
```

**✅ REFACTOR 체크리스트**:
- [ ] 클래스 레벨에 `@Tag("unit")`, `@Tag("domain")` 추가
- [ ] 관련 테스트는 `@Nested` 클래스로 그룹핑
- [ ] 각 Nested 클래스에 `@DisplayName` 추가 (한글 권장)
- [ ] 동일 로직의 여러 케이스는 `@ParameterizedTest` 사용
- [ ] @ValueSource, @CsvSource, @EnumSource 적절히 선택
- [ ] 느린 테스트는 `@Tag("slow")` 추가
- [ ] 테스트 메서드명은 `메서드_조건_결과` 패턴 유지

---

## ✅ REFACTOR Phase 완료 체크리스트

### 1. Zero-Tolerance 규칙 준수
- [ ] Lombok 사용하지 않음 (`@Data`, `@Builder`, `@Getter` 등)
- [ ] Law of Demeter 준수 (Getter 체이닝 없음)
- [ ] Tell, Don't Ask 원칙 적용
- [ ] Pure Java 유지 (외부 라이브러리 최소화)

### 2. Domain 패턴 적용
- [ ] ValueObject 불변성 완전 보장 (`final` 필드)
- [ ] Factory Method 패턴 유지 (Private 생성자)
- [ ] `equals()` & `hashCode()` 구현 (ValueObject)
- [ ] 불변 컬렉션 반환 (`Collections.unmodifiableList()`)
- [ ] **forNew() 패턴 추가** (ID ValueObject + Aggregate)
- [ ] **of() 메서드 null 체크 유지** (저장된 엔티티용)

### 3. 비즈니스 로직 구조
- [ ] 하드코딩 제거 (실제 계산 로직 구현)
- [ ] 검증 로직 분리 (`validateXxx()` 메서드)
- [ ] 상태 로직 Enum으로 이동 (`isXxx()` 메서드)
- [ ] 비즈니스 메서드 명확히 (`placeOrder()`, `cancelOrder()`)

### 4. 문서화
- [ ] 모든 public 클래스에 Javadoc (`@author`, `@since`)
- [ ] 모든 public 메서드에 Javadoc (목적, 파라미터, 예외)
- [ ] 비즈니스 규칙 명시
- [ ] 불변 조건 문서화

### 5. 테스트 검증
- [ ] 모든 테스트 통과 (GREEN 상태 유지)
- [ ] 컴파일 에러 없음
- [ ] Runtime 에러 없음

### 6. 테스트 조직화 (NEW)
- [ ] **@Tag 추가** (`@Tag("unit")`, `@Tag("domain")`)
- [ ] **@Nested로 그룹화** (생성, 검증, 비즈니스 로직)
- [ ] **@DisplayName 추가** (한글로 명확한 설명)
- [ ] **@ParameterizedTest 적용** (반복 케이스 간소화)
- [ ] **@ValueSource, @CsvSource, @EnumSource 활용**
- [ ] **느린 테스트 @Tag("slow") 추가**

---

## 🚨 주의사항

### ❌ 하지 말아야 할 것
1. **테스트 수정 금지**
   - REFACTOR는 구현 개선만
   - 테스트 로직은 변경하지 않음

2. **새로운 기능 추가 금지**
   - REFACTOR는 개선만
   - 새 기능은 새로운 RED 사이클에서

3. **한 번에 많은 개선 금지**
   - 점진적 개선 (한 번에 하나씩)
   - 매 개선 후 테스트 실행

### ✅ 해야 할 것
1. **점진적 개선**
   - 한 번에 하나씩 개선
   - 매 개선 후 테스트 실행

2. **GREEN 상태 유지**
   - 항상 테스트 통과 상태 유지
   - 테스트 실패 시 즉시 롤백

3. **컨벤션 100% 준수**
   - Zero-Tolerance 규칙 완벽히 적용
   - Law of Demeter, Lombok 금지 등

---

## 💡 REFACTOR 전략

### 전략 1: 작은 단위로 개선
```bash
# 1. Lombok 확인 및 제거
→ 테스트 실행 ✅

# 2. Law of Demeter 적용 (1개 클래스)
→ 테스트 실행 ✅

# 3. ValueObject 불변성 강화
→ 테스트 실행 ✅

# 4. 비즈니스 로직 구조 개선
→ 테스트 실행 ✅

# 5. Javadoc 추가
→ 테스트 실행 ✅
```

### 전략 2: 우선순위 적용
```
Priority 1: Zero-Tolerance 규칙 (Lombok, Law of Demeter)
Priority 2: 비즈니스 로직 구조 (하드코딩 제거)
Priority 3: 문서화 (Javadoc)
```

### 전략 3: 리팩토링 패턴
```java
// Pattern 1: Extract Method
// Before
public void cancel() {
    if (status != OrderStatus.PLACED) {
        throw new IllegalStateException("...");
    }
    this.status = OrderStatus.CANCELLED;
}

// After
public void cancel() {
    validateCancellable(); // 추출된 메서드
    this.status = OrderStatus.CANCELLED;
}

// Pattern 2: Move Method
// Before (외부에서 계산)
Money total = order.getLineItems()
    .stream()
    .map(item -> item.calculatePrice())
    .reduce(Money.ZERO, Money::add);

// After (내부로 이동)
Money total = order.calculateTotalPrice();

// Pattern 3: Replace Magic Number
// Before
if (quantity > 0) { ... }

// After
private static final int MIN_QUANTITY = 1;
if (quantity >= MIN_QUANTITY) { ... }
```

---

## 🎯 Cursor Composer 실행 예시

### 자동 실행 프롬프트
```
REFACTOR Phase를 실행해줘.

1. GREEN 상태 확인 (테스트 통과 확인)
2. Lombok 제거 확인 (있으면 안 됨)
3. Law of Demeter 적용 (Getter 체이닝 제거)
4. ValueObject 불변성 강화 (final, unmodifiable)
5. 비즈니스 로직 구조 개선 (하드코딩 → 실제 로직)
6. Javadoc 보강 (@author, @since, 설명)
7. 테스트 실행 (GREEN 유지 확인)
8. forNew() 패턴 적용 (ID ValueObject + Aggregate) ← NEW
9. 테스트 조직화 (@Tag, @Nested, @ParameterizedTest) ← NEW

.cursorrules의 Domain Layer 컨벤션을 100% 준수해야 해.
```

---

## 📊 예상 결과

### Before REFACTOR (GREEN Phase)
```java
// 하드코딩, 단순 구현
public Money calculateTotalPrice() {
    return totalPrice; // 필드 그대로 리턴
}

// Getter 노출
public List<OrderLineItem> getLineItems() {
    return lineItems;
}
```

### After REFACTOR
```java
/**
 * 총 가격 계산
 *
 * @return 라인 아이템 가격의 합계
 */
public Money calculateTotalPrice() {
    return lineItems.stream()
        .map(OrderLineItem::calculatePrice)
        .reduce(Money.ZERO, Money::add);
}

/**
 * 라인 아이템 조회 (불변 컬렉션)
 *
 * @return 주문 라인 아이템 목록 (읽기 전용)
 */
public List<OrderLineItem> getLineItems() {
    return Collections.unmodifiableList(lineItems);
}
```

---

## 🔗 다음 단계

1. **REFACTOR Phase 완료 확인**
   - [ ] 모든 컨벤션 적용됨
   - [ ] 모든 테스트 통과 (GREEN 상태)
   - [ ] Lombok 없음
   - [ ] Law of Demeter 준수
   - [ ] **forNew() 패턴 적용됨** (ID + Aggregate)
   - [ ] **테스트 조직화 완료** (@Tag, @Nested, @ParameterizedTest)

2. **검증 및 효율 측정**
   ```bash
   # Claude Code에서
   "/cr/validate 실행해줘"
   → validation-helper.py
   → LangFuse 자동 업로드
   ```

---

**✅ REFACTOR Phase는 "코드를 개선"하는 단계입니다. 테스트는 항상 GREEN 상태 유지!**
