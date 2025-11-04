# Generate Fixtures Command

**테스트 픽스처 자동 생성 (Template 기반)**

---

## 🎯 목적

Layer별 테스트 픽스처를 자동 생성:
1. **Fixture**: 단순 객체 생성 (특정 필드만 설정)
2. **Object Mother**: 비즈니스 맥락이 명확한 객체 조합
3. **Template 기반**: 반복 코드 자동화

---

## 📝 사용법

```bash
# 기본 Fixture 생성 (ID 포함)
/generate-fixtures Order

# ID 없는 Fixture
/generate-fixtures Order --without-id

# 상태 변경 포함
/generate-fixtures Order --with-states

# VIP 고객 시나리오 (Object Mother)
/generate-fixtures Order --vip

# 모든 Fixture 생성
/generate-fixtures Order --all
```

---

## 🏗️ Fixture 패턴

### Pattern 1: Fixture (단순 객체 생성)

**목적**: 특정 필드만 설정, 비즈니스 맥락 불필요

**생성 파일**: `{Layer}TestFixtures.java`

**Template:**
```java
public class OrderTestFixtures {
    
    /**
     * ID 포함 기본 Order Fixture
     */
    public static Order order() {
        return Order.builder()
            .id(1L)
            .customerId(100L)
            .status(OrderStatus.PENDING)
            .amount(BigDecimal.valueOf(10000))
            .build();
    }
    
    /**
     * ID 없는 Order Fixture (생성 테스트용)
     */
    public static Order orderWithoutId() {
        return Order.builder()
            .customerId(100L)
            .status(OrderStatus.PENDING)
            .amount(BigDecimal.valueOf(10000))
            .build();
    }
    
    /**
     * 특정 ID를 가진 Order Fixture
     */
    public static Order order(Long id) {
        return order().toBuilder()
            .id(id)
            .build();
    }
    
    /**
     * 특정 상태의 Order Fixture
     */
    public static Order orderWithStatus(OrderStatus status) {
        return order().toBuilder()
            .status(status)
            .build();
    }
}
```

### Pattern 2: Object Mother (비즈니스 맥락)

**목적**: 여러 DTO 조합, 비즈니스 의미 명확히 표현

**생성 파일**: `{Aggregate}ObjectMother.java`

**Template:**
```java
public class OrderObjectMother {
    
    /**
     * VIP 고객 주문 (할인 적용, 빠른 배송)
     */
    public static CreateOrderCommand vipCustomerOrder() {
        return CreateOrderCommand.builder()
            .customerId(1000L)  // VIP 고객
            .items(List.of(
                OrderItemCommand.builder()
                    .productId(1L)
                    .quantity(10)
                    .price(BigDecimal.valueOf(50000))
                    .build()
            ))
            .shippingType(ShippingType.EXPRESS)  // 빠른 배송
            .discountRate(BigDecimal.valueOf(0.15))  // 15% 할인
            .build();
    }
    
    /**
     * 일반 고객 대량 주문
     */
    public static CreateOrderCommand bulkOrder() {
        return CreateOrderCommand.builder()
            .customerId(2000L)
            .items(List.of(
                OrderItemCommand.builder()
                    .productId(1L)
                    .quantity(100)  // 대량
                    .price(BigDecimal.valueOf(10000))
                    .build(),
                OrderItemCommand.builder()
                    .productId(2L)
                    .quantity(50)
                    .price(BigDecimal.valueOf(20000))
                    .build()
            ))
            .shippingType(ShippingType.STANDARD)
            .build();
    }
    
    /**
     * 취소 가능한 주문 (PENDING 상태)
     */
    public static Order cancellableOrder() {
        return OrderTestFixtures.order()
            .toBuilder()
            .status(OrderStatus.PENDING)
            .build();
    }
    
    /**
     * 취소 불가능한 주문 (CONFIRMED 상태)
     */
    public static Order nonCancellableOrder() {
        return OrderTestFixtures.order()
            .toBuilder()
            .status(OrderStatus.CONFIRMED)
            .build();
    }
}
```

---

## 🎯 Layer별 생성 전략

### Domain Layer

**생성 파일:**
```
domain/{aggregate}/model/{Aggregate}TestFixtures.java
domain/{aggregate}/model/{Aggregate}ObjectMother.java
```

**포함 내용:**
- Aggregate Root 기본 객체
- Value Object (Id, Status 등)
- 상태별 객체 (PENDING, CONFIRMED, CANCELLED)
- 비즈니스 시나리오 (VIP, Bulk, Edge Cases)

### Application Layer

**생성 파일:**
```
application/{aggregate}/dto/{Aggregate}CommandFixtures.java
application/{aggregate}/dto/{Aggregate}ObjectMother.java
```

**포함 내용:**
- Command DTO (Create, Update)
- Query Condition
- 복잡한 Command 조합 (Object Mother)

### REST API Layer

**생성 파일:**
```
adapter/in/web/{aggregate}/dto/{Aggregate}RequestFixtures.java
```

**포함 내용:**
- Request DTO
- Validation 테스트용 잘못된 Request

### Persistence Layer

**생성 파일:**
```
adapter/out/persistence/{aggregate}/entity/{Aggregate}EntityFixtures.java
```

**포함 내용:**
- JPA Entity
- ID 포함/미포함 버전

---

## 🔧 옵션별 생성 규칙

### `--without-id`
```java
public static Order orderWithoutId() {
    return Order.builder()
        // .id(1L)  ← ID 제외
        .customerId(100L)
        .status(OrderStatus.PENDING)
        .build();
}
```

### `--with-states`
```java
public static Order pendingOrder() { ... }
public static Order confirmedOrder() { ... }
public static Order cancelledOrder() { ... }
public static Order completedOrder() { ... }
```

### `--vip`
```java
// Object Mother 패턴 자동 생성
public static CreateOrderCommand vipCustomerOrder() { ... }
public static CreateOrderCommand premiumMemberOrder() { ... }
```

### `--all`
```
모든 패턴 조합 생성:
- Fixture (ID 포함/미포함)
- 상태별 Fixture
- Object Mother (모든 비즈니스 시나리오)
```

---

## 📦 출력

**생성 파일 예시 (Order):**
```
domain/order/model/OrderTestFixtures.java
domain/order/model/OrderObjectMother.java
application/order/dto/OrderCommandFixtures.java
application/order/dto/OrderObjectMother.java
adapter/in/web/order/dto/OrderRequestFixtures.java
adapter/out/persistence/order/entity/OrderEntityFixtures.java
```

**확인 메시지:**
```
✅ Fixture 생성 완료

📋 생성된 파일:
- Domain Layer: 2개
- Application Layer: 2개
- REST API Layer: 1개
- Persistence Layer: 1개

📝 사용 예시:
// Domain 테스트
Order order = OrderTestFixtures.order();
Order vipOrder = OrderObjectMother.vipCustomerOrder();

// UseCase 테스트
CreateOrderCommand command = OrderCommandFixtures.createOrderCommand();
```

---

## 🎯 Fixture vs Object Mother

| 구분 | Fixture | Object Mother |
|------|---------|---------------|
| **목적** | 단순 객체 생성 | 비즈니스 맥락 표현 |
| **복잡도** | 단일 객체 | 여러 객체 조합 |
| **사용** | 단위 테스트 | 통합 테스트 |
| **예시** | `order()` | `vipCustomerOrder()` |

**장점:**
- ✅ `vipCustomerOrderCommand()`가 비즈니스 시나리오를 명확히 표현
- ✅ 테스트 가독성 향상 (Given 단계만 봐도 이해 가능)
- ✅ 여러 Command의 조합 의미가 명확

---

## 🔗 통합 워크플로우

**1. Cursor AI가 Boilerplate 생성**
```bash
# Domain, UseCase, Controller 생성
```

**2. Claude Code가 Fixture 생성**
```bash
/generate-fixtures Order --all
```

**3. Claude Code가 테스트 작성**
```bash
# Fixture를 활용한 테스트 코드 작성
@Test
void vipCustomerOrder_shouldApplyDiscount() {
    // Given
    CreateOrderCommand command = OrderObjectMother.vipCustomerOrder();
    
    // When
    OrderResponse response = createOrderUseCase.execute(command);
    
    // Then
    assertThat(response.discountRate()).isEqualTo(0.15);
}
```

---

**✅ 이 커맨드는 테스트 픽스처 자동 생성을 담당합니다!**
