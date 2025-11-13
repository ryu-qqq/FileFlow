# /cr/domain-prd - Domain Layer PRD 생성 (Cursor TDD 최적화)

**목적**: Cursor IDE의 Kent Beck TDD 워크플로우를 위한 Domain Layer 전용 PRD 생성

**실행 환경**: Claude Code

---

## 🎯 명령어 사용법

```bash
# 기본 사용
/cr/domain-prd "Order Management"

# PRD 파일 지정
/cr/domain-prd "Order Management" --prd docs/prd/order-system.md

# 강제 덮어쓰기
/cr/domain-prd "Order Management" --force
```

---

## 📋 작업 흐름

### Phase 1: 입력 수집
1. **Domain 이름** 확인
   ```
   입력: "Order Management"
   → Domain: Order
   → PRD 파일: docs/prd/domain/order-domain-prd.md
   ```

2. **기존 PRD 탐색** (선택)
   - `--prd` 옵션이 있으면 해당 파일 읽기
   - 없으면 대화형으로 요구사항 수집

---

### Phase 2: 요구사항 분석

#### 2.1 Aggregate Root 식별
**질문**:
```
1. 주요 Aggregate Root는 무엇인가요?
   예: Order, Payment, Shipping

2. 각 Aggregate의 책임은?
   - Order: 주문 생성, 취소, 상태 관리
   - Payment: 결제 처리, 환불
```

#### 2.2 ValueObject 식별
**질문**:
```
3. 각 Aggregate의 ValueObject는?
   - Order: OrderId, Money, OrderLineItem
   - Payment: PaymentId, PaymentMethod, Amount
```

#### 2.3 Enum 식별
**질문**:
```
4. 상태나 타입을 나타내는 Enum은?
   - OrderStatus: PLACED, CONFIRMED, CANCELLED
   - PaymentMethod: CREDIT_CARD, BANK_TRANSFER
```

#### 2.4 Business Rules 식별
**질문**:
```
5. 핵심 비즈니스 규칙은?
   예:
   - 주문 생성: 최소 1개 라인 아이템 필요
   - 주문 취소: PLACED 상태만 가능
   - 가격 계산: 라인 아이템 합계 + 배송비
```

---

### Phase 3: Domain PRD 생성

#### 템플릿 구조
```markdown
# Domain Layer PRD: {Domain Name}

**생성일**: {YYYY-MM-DD}
**목적**: Cursor IDE Kent Beck TDD 워크플로우

---

## 📦 1. Aggregate Root: {AggregateName}

### 책임
- {Aggregate의 주요 책임}

### 불변 조건 (Invariants)
- {반드시 유지되어야 할 조건}

### 의존 ValueObjects
- {OrderId}
- {OrderLineItem}
- {Money}

---

## 💎 2. ValueObjects

### 2.1 {ValueObjectName}
**타입**: Identifier / Quantity / Money / etc.
**검증 규칙**:
- {검증 조건}

**예시**:
```java
OrderId orderId = OrderId.of(1L);
```

### 2.2 {AnotherValueObject}
...

---

## 🏷️ 3. Enums

### 3.1 {EnumName}
**값**:
- {VALUE_1}: {설명}
- {VALUE_2}: {설명}

**전이 규칙** (State Transition):
```
PLACED → CONFIRMED → SHIPPED → DELIVERED
         ↓
      CANCELLED
```

---

## 📐 4. Business Rules

### Rule 1: {규칙 이름}
**조건**: {언제 적용되는가?}
**결과**: {무엇이 일어나는가?}
**예외**: {위반 시 어떻게?}

**예시**:
```java
// ✅ 올바른 사용
Order order = Order.create(lineItems); // lineItems.size() >= 1

// ❌ 잘못된 사용
Order order = Order.create(emptyList()); // → IllegalArgumentException
```

### Rule 2: {규칙 이름}
...

---

## 🧪 5. TDD Plan (Cursor Workflow)

### 5.1 TestFixture 생성 (FIRST STEP) ⭐

**파일**: `domain/src/testFixtures/java/.../OrderDomainFixture.java`

```java
public class OrderDomainFixture {

    public static final Long DEFAULT_ORDER_ID = 1L;
    public static final OrderStatus DEFAULT_STATUS = OrderStatus.PLACED;

    public static Order create() {
        return Order.create(
            OrderId.of(DEFAULT_ORDER_ID),
            createDefaultLineItems(),
            Money.of(10000)
        );
    }

    public static Order createWithStatus(OrderStatus status) {
        Order order = create();
        // 상태 설정 로직
        return order;
    }

    public static List<OrderLineItem> createDefaultLineItems() {
        return List.of(
            OrderLineItem.of(ProductId.of(1L), 2, Money.of(5000))
        );
    }
}
```

---

### 5.2 RED Phase: 실패하는 테스트

**파일**: `domain/src/test/java/.../OrderTest.java`

#### Test 1: Aggregate 생성
```java
@Test
@DisplayName("주문 생성 - 유효한 라인 아이템으로 주문 생성 성공")
void createOrder_WithValidLineItems_ShouldSucceed() {
    // Given
    List<OrderLineItem> lineItems = OrderDomainFixture.createDefaultLineItems();

    // When
    Order order = Order.create(OrderId.of(1L), lineItems, Money.of(10000));

    // Then
    assertThat(order.getOrderId()).isEqualTo(OrderId.of(1L));
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
    assertThat(order.getTotalPrice()).isEqualTo(Money.of(10000));
}

@Test
@DisplayName("주문 생성 - 빈 라인 아이템으로 생성 실패")
void createOrder_WithEmptyLineItems_ShouldThrowException() {
    // Given
    List<OrderLineItem> emptyLineItems = List.of();

    // When & Then
    assertThatThrownBy(() ->
        Order.create(OrderId.of(1L), emptyLineItems, Money.of(0))
    )
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("주문은 최소 1개의 라인 아이템이 필요합니다");
}
```

#### Test 2: 비즈니스 로직
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
```

---

### 5.3 GREEN Phase: 최소 구현

**원칙**: 테스트 통과만 목표, 하드코딩 허용

#### Order.java (초기 구현)
```java
public class Order {

    private OrderId orderId;
    private List<OrderLineItem> lineItems;
    private Money totalPrice;
    private OrderStatus status;

    private Order(OrderId orderId, List<OrderLineItem> lineItems, Money totalPrice) {
        this.orderId = orderId;
        this.lineItems = lineItems;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PLACED;
    }

    public static Order create(OrderId orderId, List<OrderLineItem> lineItems, Money totalPrice) {
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("주문은 최소 1개의 라인 아이템이 필요합니다");
        }
        return new Order(orderId, lineItems, totalPrice);
    }

    public void cancel() {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("PLACED 상태의 주문만 취소할 수 있습니다");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // Getters (Lombok 사용 금지)
    public OrderId getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public Money getTotalPrice() { return totalPrice; }
}
```

#### OrderId.java (ValueObject)
```java
public class OrderId {

    private final Long value;

    private OrderId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderId는 양수여야 합니다");
        }
        this.value = value;
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }

    public Long getValue() {
        return value;
    }

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
}
```

---

### 5.4 REFACTOR Phase: 컨벤션 적용

#### ✅ Law of Demeter 적용
```java
// ❌ Before (Getter 체이닝)
Money total = order.getLineItems()
    .stream()
    .map(item -> item.getPrice())
    .reduce(Money.ZERO, Money::add);

// ✅ After (Tell, Don't Ask)
Money total = order.calculateTotalPrice();
```

#### ✅ ValueObject 불변성 보장
```java
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

    public Money calculatePrice() {
        return unitPrice.multiply(quantity);
    }
}
```

#### ✅ Lombok 제거 (Pure Java)
```java
// ❌ Lombok 사용 금지
@Data
@Builder
public class Order { ... }

// ✅ Pure Java Getters
public class Order {

    public OrderId getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalPrice() {
        return totalPrice;
    }
}
```

---

## ✅ 6. Zero-Tolerance 체크리스트

### Domain Layer 필수 규칙

#### ❌ 금지 사항
- [ ] Lombok 사용 금지 (`@Data`, `@Builder`, `@Getter` 등)
- [ ] Getter 체이닝 금지 (`order.getCustomer().getAddress().getZip()`)
- [ ] Spring 의존성 금지 (`@Component`, `@Service`, `@Autowired`)
- [ ] JPA 어노테이션 금지 (`@Entity`, `@Id`, `@ManyToOne`)

#### ✅ 필수 사항
- [ ] ValueObject 패턴 적용 (OrderId, Money 등)
- [ ] Law of Demeter 준수 (Tell, Don't Ask)
- [ ] 불변 조건(Invariants) 생성자에서 검증
- [ ] Pure Java (외부 라이브러리 의존 최소화)
- [ ] TestFixture Pattern 사용
- [ ] Given-When-Then 테스트 구조

---

## 🚀 7. Cursor 실행 가이드

### Cursor Composer에서 실행

```
1. PRD 파일 읽기
   "docs/prd/domain/order-domain-prd.md 파일을 읽어줘"

2. RED Phase 실행
   "/cr/red 실행해줘. TestFixture부터 시작"

3. GREEN Phase 실행
   "/cr/green 실행해줘. 최소 구현으로 테스트 통과"

4. REFACTOR Phase 실행
   "/cr/refactor 실행해줘. .cursorrules 컨벤션 적용"
```

### 자동 사이클 실행 (권장)
```
"order-domain-prd.md 기반으로 Kent Beck TDD 사이클 전체 실행해줘.
.cursorrules의 Domain Layer 컨벤션을 따라서."
```

---

## 📊 8. 예상 산출물

### 생성되는 파일 구조
```
domain/
├── src/
│   ├── main/java/com/company/template/domain/order/
│   │   ├── Order.java (Aggregate Root)
│   │   ├── OrderId.java (ValueObject)
│   │   ├── OrderStatus.java (Enum)
│   │   ├── OrderLineItem.java (ValueObject)
│   │   └── Money.java (ValueObject)
│   │
│   ├── test/java/com/company/template/domain/order/
│   │   └── OrderTest.java
│   │
│   └── testFixtures/java/com/company/template/domain/order/
│       └── OrderDomainFixture.java
```

### 예상 시간
- **PRD 생성** (Claude Code): 2-3분
- **RED Phase** (Cursor): 1분
- **GREEN Phase** (Cursor): 1-2분
- **REFACTOR Phase** (Cursor): 2-3분
- **검증** (Claude Code): 30초

**총 시간**: **~7-10분** (vs Claude 단독: ~20-30분)

---

## ⚠️ 주의사항

### 적용 범위
- ✅ **Domain Layer만**: Aggregate, ValueObject, Enum
- ❌ **Application Layer 제외**: UseCase, Command, Query
- ❌ **Persistence Layer 제외**: JPA Entity, Repository
- ❌ **Adapter Layer 제외**: Controller, REST API

### Cursor TDD 한계
- 복잡한 비즈니스 로직: Claude Code 권장
- 여러 Aggregate 간 조율: Application Layer (Claude Code)
- 트랜잭션 경계: Application Layer (Claude Code)

---

## 🔗 다음 단계

1. **Domain PRD 생성 완료**
   ```bash
   /cr/domain-prd "Order Management"
   → docs/prd/domain/order-domain-prd.md
   ```

2. **Cursor IDE로 이동**
   - Cursor Composer 열기
   - PRD 파일 로드
   - TDD 사이클 실행

3. **검증 및 효율 측정**
   ```bash
   /cr/validate
   → validation-helper.py + LangFuse 업로드
   ```

---

**✅ 이 명령어는 Domain Layer 개발을 Cursor TDD로 빠르게 수행하기 위한 기반을 제공합니다.**
