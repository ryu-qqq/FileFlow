---
name: "domain-expert"
description: "Spring DDD Domain Layer 전문가. Aggregate 설계, Law of Demeter, Tell Don't Ask 원칙을 준수하는 Domain 객체를 생성합니다. Lombok 금지, Pure Java, Invariant 보호를 보장합니다."
---

# Spring DDD Domain Layer Expert

Domain Layer 전문가 Skill입니다. 비즈니스 로직의 핵심인 Domain 객체 설계를 담당합니다.

## 전문 분야

1. **Aggregate 설계**: Aggregate Root, Entity, ValueObject
2. **Law of Demeter**: Tell Don't Ask, Getter 체이닝 금지
3. **Domain 캡슐화**: Invariant 보호, 행동 메서드
4. **DDD 패턴**: Factory, Repository Interface, Domain Service

## 사용 시점

- Domain Aggregate 생성 또는 수정
- Entity, ValueObject 설계
- Domain 비즈니스 메서드 구현
- Domain Event 처리

## 핵심 규칙

### 1. Aggregate 설계 원칙

**위치**: `docs/coding_convention/02-domain-layer/aggregate-design/`

**Aggregate Root 예시**:
```java
public class Order {  // Aggregate Root
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private Long customerId;
    private List<OrderItem> items;  // Entity
    private Address shippingAddress;  // ValueObject
    private LocalDateTime createdAt;

    // ✅ Constructor: Invariant 보호
    public Order(
        String orderNumber,
        Long customerId,
        List<OrderItem> items,
        Address shippingAddress
    ) {
        validateOrderNumber(orderNumber);
        validateCustomerId(customerId);
        validateItems(items);
        validateAddress(shippingAddress);

        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);  // Defensive copy
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PLACED;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ 비즈니스 메서드: Tell, Don't Ask
    public void confirm() {
        if (status != OrderStatus.PLACED) {
            throw new OrderStatusException(
                "주문 확인은 PLACED 상태에서만 가능합니다"
            );
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderStatusException(
                "배송 완료된 주문은 취소할 수 없습니다"
            );
        }
        this.status = OrderStatus.CANCELLED;
    }

    // ❌ Getter 체이닝 방지용 메서드
    public String getCustomerName() {
        // ❌ return customer.getName();  // Getter 체이닝
        // ✅ Domain Service 또는 Application Layer에서 조합
        throw new UnsupportedOperationException(
            "Customer 정보는 Application Layer에서 조합하세요"
        );
    }

    // ✅ Pure Java Getter
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getStatus() { return status; }
    public Long getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Address getShippingAddress() { return shippingAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Private validation methods
    private void validateOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다");
        }
    }

    private void validateCustomerId(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 고객 ID입니다");
        }
    }

    private void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 항목은 1개 이상이어야 합니다");
        }
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("배송 주소는 필수입니다");
        }
    }
}
```

**규칙**:
- ✅ **Lombok 절대 금지**: `@Data`, `@Builder`, `@Getter`, `@Setter` 사용 불가
- ✅ **Pure Java**: 모든 getter/setter 직접 작성
- ✅ **Invariant 보호**: Constructor에서 모든 유효성 검증
- ✅ **Tell, Don't Ask**: 상태 묻지 말고 행동 시키기
- ✅ **Defensive Copy**: 컬렉션은 방어적 복사
- ✅ **Unmodifiable Collection**: 외부에 컬렉션 반환 시 불변

### 2. Law of Demeter

**위치**: `docs/coding_convention/02-domain-layer/law-of-demeter/`

**Getter 체이닝 금지**:
```java
// ❌ Bad: Getter 체이닝
String zip = order.getCustomer().getAddress().getZip();

// ✅ Good: Tell Don't Ask
String zip = order.getCustomerZipCode();

// Order 클래스에 추가
public String getCustomerZipCode() {
    // Application Layer에서 Customer 정보를 조합하여 전달
    throw new UnsupportedOperationException(
        "Customer 정보는 Application Layer에서 조합하세요"
    );
}
```

**Tell Don't Ask 원칙**:
```java
// ❌ Bad: 상태를 물어서 외부에서 판단
if (order.getStatus() == OrderStatus.PLACED) {
    order.setStatus(OrderStatus.CONFIRMED);
}

// ✅ Good: 행동을 시킴
order.confirm();

// Domain 클래스에 추가
public void confirm() {
    if (status != OrderStatus.PLACED) {
        throw new OrderStatusException(
            "주문 확인은 PLACED 상태에서만 가능합니다"
        );
    }
    this.status = OrderStatus.CONFIRMED;
}
```

### 3. Entity 설계

**Entity 예시** (`OrderItem.java`):
```java
public class OrderItem {  // Entity (Aggregate 내부)
    private Long id;
    private Long productId;
    private int quantity;
    private BigDecimal price;

    // Constructor
    public OrderItem(Long productId, int quantity, BigDecimal price) {
        validateProductId(productId);
        validateQuantity(quantity);
        validatePrice(price);

        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // 비즈니스 메서드
    public void changeQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public BigDecimal calculateTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Pure Java Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }

    // Private validation
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
    }
}
```

### 4. ValueObject 설계

**ValueObject 예시** (`Address.java`):
```java
public record Address(  // ✅ Java 21 Record 패턴
    String zipCode,
    String street,
    String city,
    String state
) {
    // Compact Constructor: Validation
    public Address {
        Objects.requireNonNull(zipCode, "zipCode must not be null");
        Objects.requireNonNull(street, "street must not be null");
        Objects.requireNonNull(city, "city must not be null");
        Objects.requireNonNull(state, "state must not be null");

        if (zipCode.isBlank()) {
            throw new IllegalArgumentException("우편번호는 필수입니다");
        }
        if (street.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }
    }

    // 비즈니스 메서드
    public String fullAddress() {
        return String.format("%s, %s, %s %s",
            street, city, state, zipCode);
    }
}
```

**규칙**:
- ✅ **Record 패턴** 사용 (Java 21)
- ✅ **Immutable**: 모든 필드 final
- ✅ **Compact Constructor**: Validation 포함
- ✅ **Value Equality**: Record가 자동 제공

### 5. Domain Exception

**Domain Exception 예시**:
```java
public abstract class DomainException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    protected DomainException(
        String errorCode,
        String message,
        HttpStatus httpStatus
    ) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}

public class OrderStatusException extends DomainException {
    public OrderStatusException(String message) {
        super("ORDER_STATUS_INVALID", message, HttpStatus.BAD_REQUEST);
    }
}

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(Long orderId) {
        super(
            "ORDER_NOT_FOUND",
            "주문을 찾을 수 없습니다: " + orderId,
            HttpStatus.NOT_FOUND
        );
    }
}
```

## 패키지 구조

```
domain/
├── order/
│   ├── Order.java              (Aggregate Root)
│   ├── OrderItem.java          (Entity)
│   ├── OrderStatus.java        (Enum)
│   ├── Address.java            (ValueObject)
│   ├── OrderId.java            (ValueObject)
│   ├── OrderFactory.java       (Factory)
│   └── OrderRepository.java    (Port/Out Interface)
├── customer/
│   ├── Customer.java
│   └── ...
└── exception/
    ├── DomainException.java
    ├── OrderStatusException.java
    └── OrderNotFoundException.java
```

## Zero-Tolerance 체크리스트

Domain Layer 코드 작성 후 반드시 확인:

- [ ] **Lombok 절대 금지**: `@Data`, `@Builder`, `@Getter`, `@Setter` 없음
- [ ] **Getter 체이닝 금지**: `.get().get()` 패턴 없음
- [ ] **Tell Don't Ask**: 상태 묻지 말고 행동 시키기
- [ ] **Invariant 보호**: Constructor에서 모든 검증
- [ ] **Defensive Copy**: 컬렉션 방어적 복사
- [ ] **Unmodifiable**: 외부 반환 컬렉션은 불변
- [ ] **Pure Java**: 모든 getter/setter 직접 작성
- [ ] **비즈니스 메서드**: Aggregate에 행동 메서드 포함

## 추가 리소스

상세 규칙:

```bash
cat .claude/skills/domain-expert/REFERENCE.md
```

검증 스크립트:

```bash
bash .claude/skills/domain-expert/scripts/validate-domain.sh [file_path]
```

## 참고 문서

- `docs/coding_convention/02-domain-layer/`
- `docs/coding_convention/02-domain-layer/aggregate-design/00_domain-object-creation-guide.md`
- `docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md`
- `docs/coding_convention/02-domain-layer/law-of-demeter/03_domain-encapsulation.md`
