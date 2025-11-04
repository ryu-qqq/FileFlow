# Domain Layer 상세 참조 가이드

## 1. Aggregate 설계 규칙

### 1.1 Aggregate Root 식별

**문서**: `docs/coding_convention/02-domain-layer/aggregate-design/00_domain-object-creation-guide.md`

**Aggregate Root 조건**:
1. **자체 생명주기**: 독립적으로 생성/수정/삭제
2. **트랜잭션 경계**: 하나의 Aggregate는 하나의 트랜잭션
3. **일관성 경계**: Invariant를 보호하는 경계
4. **외부 참조**: 다른 Aggregate는 ID로만 참조

**예시**:
- **Order** (Aggregate Root)
  - OrderItem (Entity, Aggregate 내부)
  - Address (ValueObject)
  - OrderStatus (Enum)

- **Customer** (Aggregate Root)
  - CustomerProfile (Entity)
  - Email (ValueObject)

### 1.2 Aggregate Root 구조

```java
public class Order {  // Aggregate Root

    // 1. 식별자
    private Long id;

    // 2. 속성
    private String orderNumber;
    private OrderStatus status;

    // 3. 다른 Aggregate 참조 (Long FK)
    private Long customerId;  // ✅ Customer ID로만 참조
    // ❌ private Customer customer;  // Entity 직접 참조 금지

    // 4. 내부 Entity
    private List<OrderItem> items;  // ✅ Aggregate 내부 Entity

    // 5. ValueObject
    private Address shippingAddress;  // ✅ ValueObject

    // 6. 메타데이터
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 7. Constructor: Invariant 보호
    public Order(
        String orderNumber,
        Long customerId,
        List<OrderItem> items,
        Address shippingAddress
    ) {
        // Validation 로직
        validateOrderNumber(orderNumber);
        validateCustomerId(customerId);
        validateItems(items);

        // 초기화
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);  // Defensive copy
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PLACED;
        this.createdAt = LocalDateTime.now();
    }

    // 8. 비즈니스 메서드: Tell, Don't Ask
    public void confirm() {
        if (status != OrderStatus.PLACED) {
            throw new OrderStatusException(
                "주문 확인은 PLACED 상태에서만 가능합니다"
            );
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new OrderStatusException(
                "배송 완료된 주문은 취소할 수 없습니다"
            );
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        validateItem(item);
        this.items.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(OrderItem item) {
        if (this.items.size() <= 1) {
            throw new IllegalStateException(
                "최소 1개의 주문 항목이 필요합니다"
            );
        }
        this.items.remove(item);
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalAmount() {
        return items.stream()
            .map(OrderItem::calculateTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 9. Pure Java Getters
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getStatus() { return status; }
    public Long getCustomerId() { return customerId; }
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);  // ✅ Unmodifiable
    }
    public Address getShippingAddress() { return shippingAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // 10. Private Validation Methods
    private void validateOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다");
        }
        if (!orderNumber.matches("^ORD-\\d{8}-\\d{3}$")) {
            throw new IllegalArgumentException(
                "주문 번호 형식이 올바르지 않습니다: ORD-YYYYMMDD-###"
            );
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
        if (items.size() > 100) {
            throw new IllegalArgumentException("주문 항목은 최대 100개입니다");
        }
    }

    private void validateItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("주문 항목은 필수입니다");
        }
    }
}
```

## 2. Law of Demeter

### 2.1 Getter 체이닝 금지

**문서**: `docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md`

**위반 패턴**:
```java
// ❌ Bad: Getter 체이닝
String zip = order.getCustomer().getAddress().getZip();
String city = order.getShippingAddress().getCity();  // 2-depth도 금지
BigDecimal price = order.getItems().get(0).getPrice();  // 컬렉션 접근도 주의
```

**올바른 패턴**:
```java
// ✅ Good: Tell Don't Ask
String zip = order.getShippingAddressZip();

// Order 클래스에 추가
public String getShippingAddressZip() {
    return shippingAddress.zipCode();
}

// 또는 Application Layer에서 조합
public String getCustomerZipCode(Long customerId) {
    Customer customer = customerRepository.findById(customerId);
    return customer.getAddressZipCode();
}
```

### 2.2 Tell Don't Ask 원칙

**문서**: `docs/coding_convention/02-domain-layer/law-of-demeter/03_domain-encapsulation.md`

**위반 패턴**:
```java
// ❌ Bad: Ask (상태를 물어서 외부에서 판단)
if (order.getStatus() == OrderStatus.PLACED) {
    if (order.getItems().size() > 0) {
        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
    }
}
```

**올바른 패턴**:
```java
// ✅ Good: Tell (행동을 시킴)
order.confirm();

// Order 클래스에 추가
public void confirm() {
    if (status != OrderStatus.PLACED) {
        throw new OrderStatusException(
            "주문 확인은 PLACED 상태에서만 가능합니다"
        );
    }
    if (items.isEmpty()) {
        throw new IllegalStateException(
            "주문 항목이 없습니다"
        );
    }
    this.status = OrderStatus.CONFIRMED;
    this.updatedAt = LocalDateTime.now();
}
```

## 3. Entity 설계

### 3.1 Aggregate 내부 Entity

**특징**:
- Aggregate Root를 통해서만 접근
- 독립적인 생명주기 없음
- Aggregate Root와 함께 저장/삭제

**예시** (`OrderItem.java`):
```java
public class OrderItem {  // Entity (Aggregate 내부)

    private Long id;
    private Long productId;  // ✅ Product Aggregate 참조 (Long FK)
    private String productName;  // 비정규화 (조회 성능)
    private int quantity;
    private BigDecimal price;

    // Constructor
    public OrderItem(
        Long productId,
        String productName,
        int quantity,
        BigDecimal price
    ) {
        validateProductId(productId);
        validateProductName(productName);
        validateQuantity(quantity);
        validatePrice(price);

        this.productId = productId;
        this.productName = productName;
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
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }

    // Private Validation
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다");
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }
        if (quantity > 1000) {
            throw new IllegalArgumentException("수량은 최대 1000개입니다");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
    }
}
```

## 4. ValueObject 설계

### 4.1 Record 패턴 사용

**문서**: `docs/coding_convention/06-java21-patterns/record-patterns/02_value-objects-with-records.md`

**특징**:
- Immutable
- Value Equality (equals/hashCode 자동)
- Compact Constructor로 Validation

**예시** (`Address.java`):
```java
public record Address(
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
        if (!zipCode.matches("^\\d{5}$")) {
            throw new IllegalArgumentException(
                "우편번호 형식이 올바르지 않습니다: #####"
            );
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

    public boolean isSameCity(Address other) {
        return this.city.equalsIgnoreCase(other.city);
    }
}
```

**OrderId ValueObject**:
```java
public record OrderId(Long value) {
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("유효하지 않은 Order ID입니다");
        }
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }
}
```

## 5. Domain Exception

### 5.1 Exception 계층 구조

```
DomainException (abstract)
├── OrderException
│   ├── OrderNotFoundException
│   ├── OrderStatusException
│   └── OrderValidationException
├── CustomerException
│   ├── CustomerNotFoundException
│   └── CustomerValidationException
└── PaymentException
    ├── PaymentFailedException
    └── PaymentNotFoundException
```

### 5.2 DomainException 기본 클래스

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

    protected DomainException(
        String errorCode,
        String message,
        HttpStatus httpStatus,
        Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
```

### 5.3 구체 Exception 예시

```java
// Order Not Found
public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(Long orderId) {
        super(
            "ORDER_NOT_FOUND",
            "주문을 찾을 수 없습니다: " + orderId,
            HttpStatus.NOT_FOUND
        );
    }
}

// Order Status Invalid
public class OrderStatusException extends DomainException {
    public OrderStatusException(String message) {
        super(
            "ORDER_STATUS_INVALID",
            message,
            HttpStatus.BAD_REQUEST
        );
    }
}

// Order Validation Failed
public class OrderValidationException extends DomainException {
    public OrderValidationException(String message) {
        super(
            "ORDER_VALIDATION_FAILED",
            message,
            HttpStatus.BAD_REQUEST
        );
    }
}
```

## 6. Repository Interface (Port/Out)

**Repository는 Domain Layer에 인터페이스만 정의**:

```java
package com.ryuqq.domain.order;

public interface OrderRepository {

    // Command
    Order save(Order order);
    void delete(Order order);

    // Query
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(Long customerId);

    // Existence Check
    boolean existsByOrderNumber(String orderNumber);
}
```

**규칙**:
- ✅ Domain Layer에 인터페이스 정의
- ✅ Persistence Layer에 구현체 작성
- ✅ 메서드는 Domain 객체 반환 (Entity가 아님)

## 7. Domain Service

**복잡한 비즈니스 로직이 여러 Aggregate에 걸쳐있을 때 사용**:

```java
@Service
public class OrderDomainService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderDomainService(
        OrderRepository orderRepository,
        CustomerRepository customerRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    public void validateOrderForCustomer(Order order, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        if (!customer.canPlaceOrder()) {
            throw new CustomerValidationException(
                "고객이 주문할 수 없는 상태입니다"
            );
        }

        if (order.calculateTotalAmount().compareTo(customer.getCreditLimit()) > 0) {
            throw new OrderValidationException(
                "주문 금액이 고객 신용 한도를 초과합니다"
            );
        }
    }
}
```

## 참고 문서

모든 상세 규칙은 다음 디렉토리를 참조하세요:

- `docs/coding_convention/02-domain-layer/`
