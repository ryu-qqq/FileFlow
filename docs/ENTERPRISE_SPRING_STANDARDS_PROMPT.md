# Spring Boot Enterprise Coding Standards

**Version**: 1.0.0
**Last Updated**: 2025-10-05
**Author**: Architecture Team (arch-team@company.com)

## 개요
이 문서는 Spring Boot 3.3.x + Java 21 기반 헥사고날 아키텍처 프로젝트의 **87개 코딩 표준**을 정의합니다.

모든 규칙은 다음 도구로 자동 검증됩니다:
- **ArchUnit**: 아키텍처 경계 및 의존성 규칙
- **Checkstyle**: 코드 품질 및 네이밍 규칙
- **Git Hooks**: Pre-commit 단계 실시간 검증

## 목차
- [Domain Layer Rules (30개)](#domain-layer-rules)
- [Application Layer Rules (25개)](#application-layer-rules)
- [Adapter Layer Rules (32개)](#adapter-layer-rules)

---

## Domain Layer Rules

### D-001: NO Spring Framework Dependencies

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + domain-validator.sh

**규칙**:
Domain 레이어는 Spring Framework에 의존해서는 안 됩니다.

**금지**:
- `org.springframework.*` import
- `@Component`, `@Service`, `@Repository`, `@Autowired` 어노테이션

**허용**:
- Pure Java (java.util.*, java.time.*)
- Apache Commons Lang3

**Good Example**:
```java
public class Order {
    private final OrderId id;
    private final Money totalAmount;

    private Order(OrderId id, Money totalAmount) {
        this.id = id;
        this.totalAmount = totalAmount;
    }
}
```

**Bad Example**:
```java
import org.springframework.stereotype.Component; // ❌

@Component // ❌
public class Order {
    @Autowired // ❌
    private OrderValidator validator;
}
```

**위반 시**:
- ArchUnit 테스트 실패
- Git pre-commit hook 차단
- 빌드 중단

---

### D-002: NO JPA/Hibernate Dependencies

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + domain-validator.sh

**규칙**:
Domain 레이어는 JPA/Hibernate에 의존해서는 안 됩니다.

**금지**:
- `jakarta.persistence.*` import
- `org.hibernate.*` import
- `@Entity`, `@Table`, `@Id`, `@Column` 어노테이션

**Good Example**:
```java
public class Product {
    private final ProductId id;
    private final String name;
    // Pure Java domain model
}
```

**Bad Example**:
```java
import jakarta.persistence.Entity; // ❌
import jakarta.persistence.Id; // ❌

@Entity // ❌
public class Product {
    @Id // ❌
    private Long id;
}
```

**위반 시**:
- ArchUnit 테스트 실패
- domain-validator.sh 검증 실패

---

### D-003: NO Lombok (STRICTLY PROHIBITED)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + Checkstyle + domain-validator.sh

**규칙**:
전체 프로젝트에서 Lombok 사용 금지. Domain 레이어부터 엄격히 적용.

**금지**:
- `lombok.*` import
- `@Data`, `@Builder`, `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`

**Good Example**:
```java
public class Customer {
    private final CustomerId id;
    private final String name;

    private Customer(CustomerId id, String name) {
        this.id = id;
        this.name = name;
    }

    public CustomerId getId() { return id; }
    public String getName() { return name; }
}
```

**Bad Example**:
```java
import lombok.Data; // ❌
import lombok.Builder; // ❌

@Data // ❌
@Builder // ❌
public class Customer {
    private Long id;
    private String name;
}
```

**위반 시**:
- ArchUnit 테스트 실패
- Checkstyle 에러
- Git hook 차단

---

### D-004: Fields MUST be private final

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Domain 객체의 모든 필드는 `private final`이어야 합니다.

**Good Example**:
```java
public class Order {
    private final OrderId id;
    private final List<OrderLine> lines;

    private Order(OrderId id, List<OrderLine> lines) {
        this.id = id;
        this.lines = List.copyOf(lines); // Defensive copy
    }
}
```

**Bad Example**:
```java
public class Order {
    public Long id; // ❌ not private
    private String status; // ❌ not final

    public void setId(Long id) { // ❌ mutable
        this.id = id;
    }
}
```

**위반 시**:
- ArchUnit: `fields().should().bePrivate().andShould().beFinal()` 실패

---

### D-005: NO Setter Methods

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + Checkstyle

**규칙**:
Domain 객체는 setter 메서드를 가질 수 없습니다. 불변성 유지.

**Good Example**:
```java
public class Product {
    private final ProductId id;
    private final Money price;

    public Product changePrice(Money newPrice) {
        return new Product(this.id, newPrice); // New instance
    }
}
```

**Bad Example**:
```java
public class Product {
    private Long id;
    private BigDecimal price;

    public void setPrice(BigDecimal price) { // ❌
        this.price = price;
    }
}
```

**위반 시**:
- ArchUnit: `noMethods().haveNameMatching("set[A-Z].*")` 실패
- Checkstyle: Regexp 패턴 검출

---

### D-006: NO Public Constructors

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Domain 객체는 public 생성자를 가질 수 없습니다. Static factory method 사용.

**Good Example**:
```java
public class Order {
    private final OrderId id;

    private Order(OrderId id) { // ✅ private
        this.id = id;
    }

    public static Order create(OrderId id) { // ✅ factory method
        return new Order(id);
    }
}
```

**Bad Example**:
```java
public class Order {
    private final OrderId id;

    public Order(OrderId id) { // ❌ public constructor
        this.id = id;
    }
}
```

**위반 시**:
- ArchUnit 커스텀 조건 실패

---

### D-007: Domain Exceptions MUST extend DomainException

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
모든 Domain 예외는 `DomainException`(RuntimeException)을 상속해야 합니다.

**Good Example**:
```java
public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id);
    }
}
```

**Bad Example**:
```java
public class OrderException extends Exception { // ❌ checked exception
    // ...
}
```

**위반 시**:
- ArchUnit: `classes().should().beAssignableTo(RuntimeException.class)` 실패

---

### D-008: NO JPA Annotations in Domain

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + domain-validator.sh

**규칙**:
Domain 엔티티에 JPA 어노테이션 사용 금지.

**Good Example**:
```java
public class Product {
    private final ProductId id;
    private final String name;
    // Pure POJO
}
```

**Bad Example**:
```java
@Entity // ❌
@Table(name = "products") // ❌
public class Product {
    @Id // ❌
    @GeneratedValue // ❌
    private Long id;
}
```

**위반 시**:
- ArchUnit: `noClasses().should().beAnnotatedWith(Entity.class)` 실패

---

### D-009: NO Jackson Annotations in Domain

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + domain-validator.sh

**규칙**:
Domain은 JSON 직렬화 관심사로부터 자유로워야 합니다.

**금지**:
- `com.fasterxml.jackson.*` import
- `@JsonProperty`, `@JsonIgnore`, `@JsonFormat`

**Good Example**:
```java
public class Customer {
    private final String email;
    // No serialization concerns
}
```

**Bad Example**:
```java
import com.fasterxml.jackson.annotation.JsonProperty; // ❌

public class Customer {
    @JsonProperty("email_address") // ❌
    private final String email;
}
```

**위반 시**:
- ArchUnit: `noClasses().should().dependOnClassesThat().resideInPackage("com.fasterxml.jackson..")` 실패

---

### D-010: Domain MUST NOT depend on other layers

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Domain은 Application, Adapter, Bootstrap 레이어에 의존할 수 없습니다.

**Good Example**:
```java
package com.company.template.domain.order;

import com.company.template.domain.customer.CustomerId; // ✅ domain only
import java.time.LocalDateTime; // ✅ JDK
```

**Bad Example**:
```java
package com.company.template.domain.order;

import com.company.template.application.OrderUseCase; // ❌
import com.company.template.adapter.persistence.OrderEntity; // ❌
```

**위반 시**:
- ArchUnit: Hexagonal architecture layer 규칙 실패

---

### D-011: Domain Services MUST end with 'Service' or 'DomainService'

**레벨**: 🟡 IMPORTANT
**검증**: ArchUnit

**규칙**:
Domain 서비스 클래스는 명확한 네이밍 규칙을 따라야 합니다.

**Good Example**:
```java
public class OrderPricingService {
    public Money calculateTotal(Order order) {
        // Business logic
    }
}
```

**Bad Example**:
```java
public class OrderHelper { // ❌ not 'Service' suffix
    // ...
}
```

**위반 시**:
- ArchUnit: `classes().should().haveSimpleNameEndingWith("Service")` 실패

---

### D-012: Value Objects MUST be immutable (records or final classes)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Value Objects는 record 또는 final class여야 합니다.

**Good Example**:
```java
// Option 1: Record
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}

// Option 2: Final class
public final class Email {
    private final String value;

    private Email(String value) {
        this.value = value;
    }
}
```

**Bad Example**:
```java
public class Money { // ❌ not final, not record
    private BigDecimal amount;

    public void setAmount(BigDecimal amount) { // ❌ mutable
        this.amount = amount;
    }
}
```

**위반 시**:
- ArchUnit: `classes().should().beRecords().orShould().haveModifier(FINAL)` 실패

---

### D-013: NO cyclic dependencies in Domain packages

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Domain 패키지 간 순환 의존성 금지.

**Good Example**:
```
domain/
  order/
    Order.java → CustomerId (one-way)
  customer/
    Customer.java
```

**Bad Example**:
```
domain/
  order/
    Order.java → Customer
  customer/
    Customer.java → Order ❌ circular!
```

**위반 시**:
- ArchUnit: `slices().should().beFreeOfCycles()` 실패

---

### D-014: Domain MUST use business-meaningful names

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
기술적 용어 대신 비즈니스 용어 사용.

**Good Example**:
```java
public class OrderLine {
    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;
}
```

**Bad Example**:
```java
public class OrderItem { // ❌ 'Item' is too generic
    private Long prodId; // ❌ use ProductId
    private int qty; // ❌ use Quantity
}
```

---

### D-015: Aggregate Root MUST enforce invariants

**레벨**: 🔴 CRITICAL
**검증**: Manual Code Review

**규칙**:
Aggregate Root는 모든 불변 조건을 강제해야 합니다.

**Good Example**:
```java
public class Order {
    private final List<OrderLine> lines;

    public void addLine(OrderLine line) {
        if (this.lines.size() >= MAX_LINES) {
            throw new OrderLineExceededException();
        }
        // Enforce invariant
    }
}
```

**Bad Example**:
```java
public class Order {
    private List<OrderLine> lines;

    public void addLine(OrderLine line) {
        this.lines.add(line); // ❌ no validation
    }
}
```

---

### D-016: Use Static Factory Methods with business names

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
생성 메서드는 비즈니스 의미를 담아야 합니다.

**Good Example**:
```java
public class Order {
    public static Order createNew(CustomerId customerId) { }
    public static Order createFromDraft(DraftOrder draft) { }
}
```

**Bad Example**:
```java
public class Order {
    public static Order create(Long id) { } // ❌ generic
    public static Order newInstance() { } // ❌ technical
}
```

---

### D-017: Domain Events MUST be immutable

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Domain Event는 불변 객체여야 합니다.

**Good Example**:
```java
public record OrderCreatedEvent(
    OrderId orderId,
    CustomerId customerId,
    LocalDateTime occurredAt
) { }
```

**Bad Example**:
```java
public class OrderCreatedEvent {
    private OrderId orderId;

    public void setOrderId(OrderId id) { // ❌ mutable
        this.orderId = id;
    }
}
```

---

### D-018: Repository interfaces MUST be in Domain

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Repository 인터페이스는 Domain에, 구현체는 Adapter에 위치.

**Good Example**:
```java
// domain/repository/OrderRepository.java
public interface OrderRepository {
    Order findById(OrderId id);
    void save(Order order);
}

// adapter/out/persistence/OrderRepositoryImpl.java
@Repository
class OrderRepositoryImpl implements OrderRepository { }
```

**Bad Example**:
```java
// adapter/out/persistence/OrderRepository.java ❌
public interface OrderRepository {
    // Wrong location!
}
```

---

### D-019: Repository methods MUST use Domain types

**레벨**: 🔴 CRITICAL
**검증**: Manual Code Review

**규칙**:
Repository 메서드는 Domain 타입만 사용.

**Good Example**:
```java
public interface OrderRepository {
    Optional<Order> findById(OrderId id); // ✅ Domain types
    void save(Order order);
}
```

**Bad Example**:
```java
public interface OrderRepository {
    Optional<OrderEntity> findById(Long id); // ❌ Entity, Long
}
```

---

### D-020: NO infrastructure concerns in Domain

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + domain-validator.sh

**규칙**:
Domain에서 인프라 관심사 금지.

**금지**:
- Database concerns (트랜잭션, 쿼리)
- Logging frameworks
- External API clients
- Messaging/Event bus 구현체

**Good Example**:
```java
public class Order {
    public void cancel() {
        validateCancellable();
        this.status = OrderStatus.CANCELLED;
    }
}
```

**Bad Example**:
```java
import org.slf4j.Logger; // ❌

public class Order {
    private static final Logger log = LoggerFactory.getLogger(...); // ❌

    public void cancel() {
        log.info("Cancelling order"); // ❌ logging
    }
}
```

---

### D-021: Validation logic MUST be in Domain

**레벨**: 🔴 CRITICAL
**검증**: Manual Code Review

**규칙**:
비즈니스 검증은 Domain에서 수행.

**Good Example**:
```java
public class Order {
    private Order(OrderId id, Money total) {
        if (total.isNegative()) {
            throw new InvalidOrderAmountException();
        }
        this.id = id;
        this.total = total;
    }
}
```

**Bad Example**:
```java
// Controller에서 검증 ❌
@PostMapping
public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest req) {
    if (req.total() < 0) { // ❌ business validation in controller
        throw new BadRequestException();
    }
}
```

---

### D-022: NO utility classes in Domain

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Domain에 static utility class 금지. Domain Service 사용.

**Good Example**:
```java
public class OrderPricingService {
    public Money calculateTotal(Order order) {
        // Business logic as service
    }
}
```

**Bad Example**:
```java
public class OrderUtils { // ❌ utility class
    public static Money calculateTotal(Order order) {
        // ...
    }
}
```

---

### D-023: Domain models MUST be persistence-ignorant

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + domain-validator.sh

**규칙**:
Domain 모델은 영속성 메커니즘을 알아서는 안 됩니다.

**Good Example**:
```java
public class Product {
    private final ProductId id;
    private final CategoryId categoryId; // ✅ FK as value object
}
```

**Bad Example**:
```java
@Entity // ❌
public class Product {
    @ManyToOne // ❌ JPA relationship
    private Category category;
}
```

---

### D-024: Use Domain-specific collections

**레벨**: 🟢 RECOMMENDED
**검증**: Manual Code Review

**규칙**:
비즈니스 의미를 가진 컬렉션 타입 사용.

**Good Example**:
```java
public class OrderLines {
    private final List<OrderLine> lines;

    public Money calculateTotal() {
        return lines.stream()
            .map(OrderLine::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
}
```

**Bad Example**:
```java
public class Order {
    private List<OrderLine> lines; // ❌ generic List
}
```

---

### D-025: Domain MUST have comprehensive tests

**레벨**: 🟡 IMPORTANT
**검증**: Code Coverage (>80%)

**규칙**:
Domain 로직은 80% 이상 테스트 커버리지.

**Good Example**:
```java
@Test
void whenCancelOrder_thenStatusChangedToCancelled() {
    Order order = Order.createNew(customerId);

    order.cancel();

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
}
```

---

### D-026: NO Optional in Domain constructors

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
생성자/Factory 메서드에서 Optional 사용 금지.

**Good Example**:
```java
public static Order create(OrderId id, CustomerId customerId) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(customerId);
    return new Order(id, customerId);
}
```

**Bad Example**:
```java
public static Order create(Optional<OrderId> id) { // ❌ Optional param
    // ...
}
```

---

### D-027: Domain Exceptions MUST have meaningful messages

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
예외 메시지는 비즈니스 컨텍스트 포함.

**Good Example**:
```java
public class InsufficientStockException extends DomainException {
    public InsufficientStockException(ProductId id, int requested, int available) {
        super(String.format(
            "Insufficient stock for product %s: requested %d, available %d",
            id, requested, available
        ));
    }
}
```

**Bad Example**:
```java
throw new RuntimeException("Error"); // ❌ generic
```

---

### D-028: NO null returns from Domain methods

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Domain 메서드는 null 대신 Optional 사용.

**Good Example**:
```java
public Optional<Customer> findPreferredCustomer() {
    return Optional.ofNullable(preferredCustomer);
}
```

**Bad Example**:
```java
public Customer findPreferredCustomer() {
    return null; // ❌
}
```

---

### D-029: Domain methods MUST have single responsibility

**레벨**: 🟡 IMPORTANT
**검증**: Checkstyle (Cyclomatic Complexity ≤ 10)

**규칙**:
메서드는 하나의 책임만 가져야 합니다.

**Good Example**:
```java
public void processOrder() {
    validateOrder();
    calculateTotal();
    applyDiscount();
}
```

**Bad Example**:
```java
public void processOrder() {
    // 50 lines of validation
    // 30 lines of calculation
    // 20 lines of discount logic
    // ❌ too many responsibilities
}
```

**위반 시**:
- Checkstyle: CyclomaticComplexity > 10

---

### D-030: Domain MUST be tested without Spring context

**레벨**: 🔴 CRITICAL
**검증**: Manual Code Review

**규칙**:
Domain 테스트는 Spring 컨텍스트 없이 실행.

**Good Example**:
```java
class OrderTest {
    @Test
    void test() {
        Order order = Order.createNew(customerId); // ✅ Pure Java
        // ...
    }
}
```

**Bad Example**:
```java
@SpringBootTest // ❌
class OrderTest {
    @Autowired // ❌
    private Order order;
}
```

---

### D-031: Domain classes MUST have ≤ 7 public methods (SRP)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + PMD GodClass rule

**규칙**:
Domain 클래스는 최대 7개의 public 메서드만 가져야 합니다. 많은 메서드는 여러 책임을 의미합니다.

**Good Example**:
```java
public class Order {
    // Public methods: 6개 (SRP 준수)
    public OrderId getId() { }
    public Money getTotal() { }
    public void addLine(OrderLine line) { }
    public void removeLine(OrderLineId lineId) { }
    public void cancel() { }
    public OrderStatus getStatus() { }
}
```

**Bad Example**:
```java
public class Order {
    // Public methods: 10개 (SRP 위반)
    public OrderId getId() { }
    public Money getTotal() { }
    public void addLine(...) { }
    public void removeLine(...) { }
    public void cancel() { }
    public void approve() { }
    public void ship() { }
    public void deliver() { }
    public void refund() { }
    public void archive() { }
}
```

**위반 시**:
- ArchUnit: `SingleResponsibilityTest.java` 실패
- PMD: GodClass rule 검출

---

### D-032: Domain classes MUST have ≤ 5 instance fields (SRP)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Domain 클래스는 최대 5개의 instance 필드만 가져야 합니다. 많은 필드는 여러 관심사를 의미합니다.

**Good Example**:
```java
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final OrderStatus status;
    private final Money total;
    // 5개 필드 - SRP 준수
}
```

**Bad Example**:
```java
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final OrderStatus status;
    private final Money total;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final PaymentMethod paymentMethod;
    // 8개 필드 - SRP 위반, 너무 많은 관심사
}
```

**위반 시**:
- ArchUnit: `SingleResponsibilityTest.java` 실패

---

### D-033: Domain MUST provide delegation methods (Law of Demeter)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + PMD DomainLayerDemeterStrict rule

**규칙**:
Domain 객체는 내부 구조를 노출하지 않고 위임 메서드를 통해 기능을 제공해야 합니다.

**Good Example**:
```java
public class Order {
    private final Customer customer;

    // ✅ 위임 메서드 제공 (Tell, Don't Ask)
    public String getCustomerName() {
        return customer.getName();
    }

    public boolean isVipCustomer() {
        return customer.isVip();
    }
}
```

**Bad Example**:
```java
public class Order {
    private final Customer customer;

    // ❌ getter만 제공 (Law of Demeter 위반)
    public Customer getCustomer() {
        return customer;
    }
}

// 호출부에서 체이닝 발생
order.getCustomer().getName(); // ❌ Train Wreck
order.getCustomer().isVip();   // ❌ Train Wreck
```

**위반 시**:
- ArchUnit: `LawOfDemeterTest.java` - `provideBusinessMethods()` 조건 실패
- PMD: DomainLayerDemeterStrict XPath 규칙 검출

---

### D-034: NO getter chaining in Domain (Law of Demeter)

**레벨**: 🔴 CRITICAL
**검증**: PMD DomainLayerDemeterStrict XPath rule

**규칙**:
Domain에서 getter 체이닝(Train Wreck) 절대 금지. 2단계 이상의 메서드 체이닝은 Law of Demeter 위반입니다.

**Good Example**:
```java
public class OrderService {
    public Money calculateDiscount(Order order) {
        // ✅ 위임 메서드 사용
        Money customerDiscount = order.getCustomerDiscount();
        return order.getTotal().multiply(customerDiscount);
    }
}
```

**Bad Example**:
```java
public class OrderService {
    public Money calculateDiscount(Order order) {
        // ❌ getter 체이닝 (Law of Demeter 위반)
        Money discount = order.getCustomer().getDiscountRate();

        // ❌ 3단계 체이닝
        String city = order.getCustomer().getAddress().getCity();

        return order.getTotal().multiply(discount);
    }
}
```

**허용 패턴**:
- ✅ Builder 패턴: `Order.builder().id(...).total(...).build()`
- ✅ Stream API: `list.stream().filter(...).map(...).collect(...)`
- ✅ StringBuilder: `new StringBuilder().append(...).append(...).toString()`

**위반 시**:
- PMD: DomainLayerDemeterStrict XPath 규칙 검출
  ```xml
  <!-- PMD ruleset: 2단계 이상 체이닝 금지 -->
  //PrimaryExpression[count(PrimarySuffix) > 1]
  ```

---

## Application Layer Rules

### A-001: Application CAN depend on Domain ONLY

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + application-validator.sh

**규칙**:
Application 레이어는 Domain만 의존 가능. Adapter 의존 금지.

**Good Example**:
```java
package com.company.template.application.order;

import com.company.template.domain.order.Order; // ✅ domain
import com.company.template.domain.order.OrderRepository; // ✅ domain port
```

**Bad Example**:
```java
package com.company.template.application.order;

import com.company.template.adapter.out.persistence.OrderEntity; // ❌
import com.company.template.adapter.in.web.OrderController; // ❌
```

**위반 시**:
- ArchUnit: Hexagonal architecture layer 규칙 실패
- application-validator.sh 검증 실패

---

### A-002: Application MUST NOT use JPA directly

**레벨**: 🔴 CRITICAL
**검증**: application-validator.sh

**규칙**:
Application에서 JPA 어노테이션/EntityManager 직접 사용 금지.

**Good Example**:
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository; // ✅ port interface

    public Order findOrder(OrderId id) {
        return orderRepository.findById(id);
    }
}
```

**Bad Example**:
```java
@Service
public class OrderService {
    @PersistenceContext // ❌
    private EntityManager em;

    public Order findOrder(Long id) {
        return em.find(Order.class, id); // ❌ direct JPA
    }
}
```

---

### A-003: NO Lombok in Application

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit + Checkstyle

**규칙**:
Application 레이어에서도 Lombok 사용 금지.

**Good Example**:
```java
@Service
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;

    public CreateOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

**Bad Example**:
```java
@Service
@RequiredArgsConstructor // ❌ Lombok
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
}
```

---

### A-004: Use Cases MUST end with 'UseCase' suffix

**레벨**: 🟡 IMPORTANT
**검증**: ArchUnit + application-validator.sh

**규칙**:
Use Case 클래스는 명확한 네이밍 규칙 준수.

**Good Example**:
```java
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        // ...
    }
}
```

**Bad Example**:
```java
public class OrderCreator { // ❌ not 'UseCase' suffix
    // ...
}
```

---

### A-005: Use Cases MUST have single execute method

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
각 Use Case는 하나의 주요 실행 메서드만 가져야 합니다.

**Good Example**:
```java
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        // Single responsibility
    }
}
```

**Bad Example**:
```java
public class OrderUseCase { // ❌ multiple responsibilities
    public void createOrder() { }
    public void updateOrder() { }
    public void deleteOrder() { }
}
```

---

### A-006: @Transactional MUST be in Application layer ONLY

**레벨**: 🔴 CRITICAL
**검증**: Checkstyle + persistence-validator.sh

**규칙**:
트랜잭션 관리는 Application 레이어에서만.

**Good Example**:
```java
// Application Layer
@Service
@Transactional // ✅ Application layer
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        // Transaction boundary
    }
}
```

**Bad Example**:
```java
// Adapter Layer
@Repository
@Transactional // ❌ adapter layer
public class OrderRepositoryImpl {
    public void save(Order order) { }
}
```

**위반 시**:
- persistence-validator.sh 차단
- Checkstyle Regexp 검출

---

### A-007: Use Command/Query pattern

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
명령(Command)과 조회(Query)를 분리.

**Good Example**:
```java
public record CreateOrderCommand(CustomerId customerId, List<OrderLineData> lines) { }

public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) { }
}

public class GetOrderQuery {
    public OrderDto execute(OrderId id) { }
}
```

**Bad Example**:
```java
public class OrderService { // ❌ mixed command/query
    public Order createAndGetOrder(...) { }
}
```

---

### A-008: Commands MUST be immutable records

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Command 객체는 불변 record로 정의.

**Good Example**:
```java
public record CreateOrderCommand(
    CustomerId customerId,
    List<OrderLineItem> items
) {
    public CreateOrderCommand {
        Objects.requireNonNull(customerId);
        items = List.copyOf(items);
    }
}
```

**Bad Example**:
```java
public class CreateOrderCommand { // ❌ not record
    private CustomerId customerId;

    public void setCustomerId(CustomerId id) { } // ❌ mutable
}
```

---

### A-009: Use Cases MUST validate commands

**レ벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Use Case는 Command 유효성을 검증해야 합니다.

**Good Example**:
```java
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        validateCommand(command);
        // ... business logic
    }

    private void validateCommand(CreateOrderCommand command) {
        if (command.items().isEmpty()) {
            throw new EmptyOrderException();
        }
    }
}
```

---

### A-010: Port interfaces MUST be in Application

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Input/Output Port 인터페이스는 Application에 정의.

**Good Example**:
```java
// application/port/in/CreateOrderUseCase.java
public interface CreateOrderUseCase {
    OrderId execute(CreateOrderCommand command);
}

// application/port/out/LoadOrderPort.java
public interface LoadOrderPort {
    Optional<Order> loadById(OrderId id);
}
```

**Bad Example**:
```java
// adapter/in/web/CreateOrderController.java
public interface CreateOrderUseCase { // ❌ wrong location
}
```

---

### A-011: Use Case implementations MUST be package-private

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Use Case 구현체는 package-private, 인터페이스만 public.

**Good Example**:
```java
public interface CreateOrderUseCase { }

@Service
class CreateOrderService implements CreateOrderUseCase { // ✅ package-private
}
```

**Bad Example**:
```java
@Service
public class CreateOrderService implements CreateOrderUseCase { // ❌ public
}
```

---

### A-012: NO business logic in Application

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Application은 orchestration만. 비즈니스 로직은 Domain에.

**Good Example**:
```java
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        Customer customer = loadCustomer(command.customerId());
        Order order = Order.createFor(customer); // ✅ Domain logic
        orderRepository.save(order);
        return order.getId();
    }
}
```

**Bad Example**:
```java
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        if (command.total() < 0) { // ❌ business validation in application
            throw new InvalidAmountException();
        }
        // ...
    }
}
```

---

### A-013: Exception handling in Application

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Application은 Domain 예외를 잡아 Application 예외로 변환.

**Good Example**:
```java
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        try {
            return orderService.createOrder(command);
        } catch (DomainException e) {
            throw new CreateOrderFailedException(e);
        }
    }
}
```

---

### A-014: Use constructor injection ONLY

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
의존성 주입은 생성자 주입만 사용.

**Good Example**:
```java
@Service
public class CreateOrderUseCase {
    private final OrderRepository repository;

    public CreateOrderUseCase(OrderRepository repository) { // ✅ constructor
        this.repository = repository;
    }
}
```

**Bad Example**:
```java
@Service
public class CreateOrderUseCase {
    @Autowired // ❌ field injection
    private OrderRepository repository;
}
```

---

### A-015: Application exceptions MUST be in exception package

**레벨**: 🟡 IMPORTANT
**검증**: ArchUnit

**규칙**:
Application 예외는 `application.exception` 패키지에 위치.

**Good Example**:
```java
// application/exception/CreateOrderFailedException.java
public class CreateOrderFailedException extends ApplicationException {
}
```

**Bad Example**:
```java
// application/usecase/CreateOrderException.java ❌ wrong location
```

**위반 시**:
- ArchUnit: Package location 규칙 실패

---

### A-016: Use Cases MUST NOT return entities

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Use Case는 Domain 객체가 아닌 DTO/ID 반환.

**Good Example**:
```java
public OrderId execute(CreateOrderCommand command) {
    Order order = orderService.create(command);
    return order.getId(); // ✅ return ID
}
```

**Bad Example**:
```java
public Order execute(CreateOrderCommand command) {
    return orderService.create(command); // ❌ return entity
}
```

---

### A-017: Event publishing in Application

**レ벨**: 🟢 RECOMMENDED
**검증**: Manual Code Review

**규칙**:
Domain Event 발행은 Application 레이어에서.

**Good Example**:
```java
@Service
public class CreateOrderUseCase {
    private final ApplicationEventPublisher eventPublisher;

    public OrderId execute(CreateOrderCommand command) {
        Order order = orderService.create(command);
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId()));
        return order.getId();
    }
}
```

---

### A-018: Use Case methods MUST have max 5 parameters

**레벨**: 🟡 IMPORTANT
**검증**: ArchUnit + Checkstyle

**규칙**:
메서드 파라미터는 최대 5개.

**Good Example**:
```java
public OrderId execute(CreateOrderCommand command) { // ✅ 1 parameter
}
```

**Bad Example**:
```java
public OrderId execute(Long customerId, String name,
                       String address, String phone,
                       String email, List<Item> items) { // ❌ 6 parameters
}
```

**위반 시**:
- Checkstyle: ParameterNumber > 5

---

### A-019: Use QueryDSL for complex queries

**레벨**: 🟢 RECOMMENDED
**검증**: Manual Code Review

**규칙**:
복잡한 조회는 QueryDSL 사용.

**Good Example**:
```java
// application/port/out/OrderQueryPort.java
public interface OrderQueryPort {
    List<Order> findByComplexCriteria(OrderSearchCriteria criteria);
}

// adapter/out/persistence/OrderQueryAdapter.java (QueryDSL)
@Repository
class OrderQueryAdapter implements OrderQueryPort {
    public List<Order> findByComplexCriteria(OrderSearchCriteria criteria) {
        return queryFactory
            .selectFrom(orderEntity)
            .where(buildPredicates(criteria))
            .fetch();
    }
}
```

---

### A-020: Service methods MUST be focused

**レ벨**: 🟡 IMPORTANT
**검증**: Checkstyle (MethodLength ≤ 50)

**규칙**:
메서드 길이는 50줄 이하.

**위반 시**:
- Checkstyle: MethodLength > 50

---

### A-021: Use DTOs for cross-layer communication

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
레이어 간 통신은 DTO 사용.

**Good Example**:
```java
public record OrderDto(
    Long id,
    String customerName,
    BigDecimal total
) {
    public static OrderDto from(Order order) {
        return new OrderDto(
            order.getId().value(),
            order.getCustomer().getName(),
            order.getTotal().amount()
        );
    }
}
```

---

### A-022: Application MUST define SPI (Service Provider Interface)

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Adapter가 구현할 인터페이스는 Application에서 정의.

**Good Example**:
```java
// application/port/out/NotificationPort.java
public interface NotificationPort {
    void sendOrderConfirmation(OrderId orderId);
}

// adapter/out/notification/EmailNotificationAdapter.java
@Component
class EmailNotificationAdapter implements NotificationPort {
}
```

---

### A-023: Use Case tests MUST mock ports

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
Use Case 테스트는 Port를 mock.

**Good Example**:
```java
class CreateOrderUseCaseTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CreateOrderUseCase useCase;

    @Test
    void test() {
        when(orderRepository.save(any())).thenReturn(order);
        // ...
    }
}
```

---

### A-024: Application MUST NOT use @Controller

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Application에 웹 관련 어노테이션 금지.

**Bad Example**:
```java
@RestController // ❌ belongs to adapter-in-web
public class OrderService {
}
```

---

### A-025: Use Cases MUST log important operations

**레벨**: 🟢 RECOMMENDED
**검증**: Manual Code Review

**규칙**:
중요한 비즈니스 작업은 로깅.

**Good Example**:
```java
@Service
public class CreateOrderUseCase {
    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

    public OrderId execute(CreateOrderCommand command) {
        log.info("Creating order for customer: {}", command.customerId());
        // ...
    }
}
```

---

### A-026: UseCases MUST have ≤ 5 public methods (SRP)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
UseCase 클래스는 최대 5개의 public 메서드만 가져야 합니다. 하나의 UseCase는 하나의 작업만 수행해야 합니다.

**Good Example**:
```java
@Service
public class CreateOrderUseCase {
    // Public methods: 1개 (이상적)
    public OrderId execute(CreateOrderCommand command) {
        validateCommand(command);
        return createOrder(command);
    }

    // Private helper methods
    private void validateCommand(CreateOrderCommand command) { }
    private OrderId createOrder(CreateOrderCommand command) { }
}
```

**Bad Example**:
```java
@Service
public class OrderUseCase {
    // Public methods: 6개 (SRP 위반 - 여러 책임)
    public OrderId createOrder(CreateOrderCommand cmd) { }
    public void updateOrder(UpdateOrderCommand cmd) { }
    public void cancelOrder(CancelOrderCommand cmd) { }
    public void approveOrder(ApproveOrderCommand cmd) { }
    public void shipOrder(ShipOrderCommand cmd) { }
    public void deliverOrder(DeliverOrderCommand cmd) { }
}
```

**위반 시**:
- ArchUnit: `SingleResponsibilityTest.java` 실패
- 하나의 UseCase로 분리 필요

---

### A-027: UseCases SHOULD have single @Transactional method (SRP)

**레벨**: 🟡 IMPORTANT
**검증**: ArchUnit

**규칙**:
UseCase는 보통 하나의 트랜잭션 메서드만 가져야 합니다. 여러 개의 @Transactional 메서드는 여러 책임을 의심해야 합니다.

**Good Example**:
```java
@Service
public class CreateOrderUseCase {
    @Transactional // ✅ 단일 트랜잭션 메서드
    public OrderId execute(CreateOrderCommand command) {
        Order order = orderService.create(command);
        orderRepository.save(order);
        return order.getId();
    }

    // 조회 메서드는 @Transactional(readOnly = true) 가능
    @Transactional(readOnly = true)
    public OrderDto getOrder(OrderId id) {
        return orderRepository.findById(id)
            .map(OrderDto::from)
            .orElseThrow();
    }
}
```

**Bad Example**:
```java
@Service
public class OrderService {
    @Transactional // ❌ 여러 트랜잭션 메서드 = 여러 책임
    public void createOrder() { }

    @Transactional // ❌
    public void updateOrder() { }

    @Transactional // ❌
    public void cancelOrder() { }

    // 3개의 트랜잭션 메서드 → CreateOrderUseCase, UpdateOrderUseCase, CancelOrderUseCase로 분리 필요
}
```

**권장 사항**:
- Command UseCase: 1개의 @Transactional 메서드
- Query UseCase: 1개의 @Transactional(readOnly = true) 메서드
- 여러 트랜잭션 필요 시 → 별도 UseCase로 분리

**위반 시**:
- ArchUnit: `SingleResponsibilityTest.java` - `haveAtMostTransactionalMethods(1)` 조건 실패

---

## Adapter Layer Rules

### AI-001: Request/Response DTOs MUST be records

**레벨**: 🔴 CRITICAL
**검증**: Checkstyle + controller-validator.sh

**규칙**:
모든 Request/Response DTO는 Java record로 정의.

**Good Example**:
```java
// CreateOrderRequest.java
public record CreateOrderRequest(
    Long customerId,
    List<OrderLineRequest> lines
) {
    public CreateOrderRequest {
        Objects.requireNonNull(customerId);
        lines = List.copyOf(lines);
    }

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
            new CustomerId(customerId),
            lines.stream().map(OrderLineRequest::toOrderLine).toList()
        );
    }
}
```

**Bad Example**:
```java
public class CreateOrderRequest { // ❌ not record
    private Long customerId;

    public void setCustomerId(Long id) { } // ❌ mutable
}
```

**위반 시**:
- Checkstyle: RegexpSingleline 검출
- controller-validator.sh 차단

---

### AI-002: NO inner classes in Controllers

**레벨**: 🔴 CRITICAL
**검증**: Checkstyle + controller-validator.sh

**규칙**:
Controller 안에 Request/Response inner class 금지.

**Good Example**:
```
adapter/in/web/
  order/
    CreateOrderController.java
    CreateOrderRequest.java  ✅ Separate file
    OrderResponse.java       ✅ Separate file
```

**Bad Example**:
```java
@RestController
public class OrderController {

    public static class CreateOrderRequest { } // ❌ inner class

    public static class OrderResponse { } // ❌ inner class
}
```

**위반 시**:
- Checkstyle: Regexp 검출
- controller-validator.sh 차단

---

### AI-003: DTOs MUST have compact constructor validation

**레벨**: 🟡 IMPORTANT
**검증**: controller-validator.sh (warning)

**규칙**:
Record는 compact constructor에서 유효성 검증.

**Good Example**:
```java
public record CreateOrderRequest(
    Long customerId,
    List<OrderLineRequest> lines
) {
    public CreateOrderRequest {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line");
        }
        lines = List.copyOf(lines);
    }
}
```

**Bad Example**:
```java
public record CreateOrderRequest(
    Long customerId,
    List<OrderLineRequest> lines
) { } // ❌ no validation
```

---

### AI-004: Request DTOs MUST have toCommand method

**레벨**: 🟡 IMPORTANT
**검증**: controller-validator.sh (warning)

**규칙**:
Request DTO는 Command 변환 메서드 제공.

**Good Example**:
```java
public record CreateOrderRequest(
    Long customerId,
    List<OrderLineRequest> lines
) {
    public CreateOrderCommand toCommand() { // ✅ conversion method
        return new CreateOrderCommand(
            new CustomerId(customerId),
            lines.stream().map(OrderLineRequest::toDomain).toList()
        );
    }
}
```

**Bad Example**:
```java
public record CreateOrderRequest(
    Long customerId,
    List<OrderLineRequest> lines
) { } // ❌ no conversion method
```

---

### AI-005: Response DTOs MUST have static from method

**레벨**: 🟡 IMPORTANT
**검증**: controller-validator.sh (warning)

**규칙**:
Response DTO는 Domain 객체로부터 생성하는 static factory method 제공.

**Good Example**:
```java
public record OrderResponse(
    Long id,
    String status,
    BigDecimal total
) {
    public static OrderResponse from(Order order) { // ✅ factory method
        return new OrderResponse(
            order.getId().value(),
            order.getStatus().name(),
            order.getTotal().amount()
        );
    }
}
```

**Bad Example**:
```java
public record OrderResponse(
    Long id,
    String status
) { } // ❌ no factory method
```

---

### AI-006: Controllers MUST NOT have business logic

**レベル**: 🔴 CRITICAL
**検証**: controller-validator.sh (warning)

**規칙**:
Controller는 orchestration만. 비즈니스 로직 금지.

**Good Example**:
```java
@RestController
@RequestMapping("/orders")
public class CreateOrderController {
    private final CreateOrderUseCase useCase;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        OrderId orderId = useCase.execute(request.toCommand()); // ✅ delegate
        return ResponseEntity.ok(OrderResponse.from(orderId));
    }
}
```

**Bad Example**:
```java
@RestController
public class OrderController {
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest request) {
        if (request.total() < 0) { // ❌ business validation
            throw new BadRequestException();
        }
        // ❌ business logic in controller
    }
}
```

---

### AI-007: Controllers MUST depend on UseCase interfaces ONLY

**레벨**: 🔴 CRITICAL
**検証**: controller-validator.sh

**規칙**:
Controller는 UseCase 인터페이스만 의존. Repository/Entity 직접 사용 금지.

**Good Example**:
```java
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase; // ✅ UseCase interface

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }
}
```

**Bad Example**:
```java
@RestController
public class OrderController {
    private final OrderRepository orderRepository; // ❌ Repository
    private final OrderEntity orderEntity; // ❌ Entity
}
```

**위반 시**:
- controller-validator.sh 차단

---

### AI-008: Controllers MUST end with 'Controller' suffix

**레벨**: 🟡 IMPORTANT
**검증**: ArchUnit

**규칙**:
Controller 클래스는 명확한 네이밍 규칙 준수.

**Good Example**:
```java
@RestController
public class CreateOrderController { }
```

**Bad Example**:
```java
@RestController
public class OrderApi { } // ❌ not 'Controller' suffix
```

**위반 시**:
- ArchUnit: Naming convention 규칙 실패

---

### AI-009: Use HTTP method-specific annotations

**레벨**: 🟡 IMPORTANT
**검증**: Manual Code Review

**규칙**:
명확한 HTTP 메서드 어노테이션 사용.

**Good Example**:
```java
@PostMapping("/orders")  // ✅ @PostMapping
@GetMapping("/orders/{id}")  // ✅ @GetMapping
@PutMapping("/orders/{id}")  // ✅ @PutMapping
@DeleteMapping("/orders/{id}")  // ✅ @DeleteMapping
```

**Bad Example**:
```java
@RequestMapping(value = "/orders", method = RequestMethod.POST) // ❌ verbose
```

---

### AI-010: Controllers MUST return ResponseEntity

**レベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
Controller는 ResponseEntity 반환.

**Good Example**:
```java
@PostMapping
public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
    OrderId id = useCase.execute(request.toCommand());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(OrderResponse.from(id));
}
```

**Bad Example**:
```java
@PostMapping
public OrderResponse create(@RequestBody CreateOrderRequest request) { // ❌ no ResponseEntity
    return OrderResponse.from(useCase.execute(request.toCommand()));
}
```

---

### AI-011: Use @Valid for request validation

**레ベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
Bean Validation 활용.

**Good Example**:
```java
public record CreateOrderRequest(
    @NotNull @Positive Long customerId,
    @NotEmpty List<OrderLineRequest> lines
) { }

@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest request) {
}
```

---

### AI-012: Controllers MUST have single responsibility

**レベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
각 Controller는 하나의 리소스/작업만 담당.

**Good Example**:
```java
@RestController
@RequestMapping("/orders")
public class CreateOrderController { // ✅ Single operation
    @PostMapping
    public ResponseEntity<?> create(...) { }
}
```

**Bad Example**:
```java
@RestController
public class OrderController { // ❌ Multiple operations
    @PostMapping("/orders")
    public ResponseEntity<?> create(...) { }

    @GetMapping("/orders")
    public ResponseEntity<?> list(...) { }

    @PutMapping("/orders/{id}")
    public ResponseEntity<?> update(...) { }
}
```

---

### AI-013: Use proper HTTP status codes

**레ベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
적절한 HTTP 상태 코드 사용.

**Good Example**:
```java
@PostMapping
public ResponseEntity<?> create(...) {
    return ResponseEntity.status(HttpStatus.CREATED).body(...); // ✅ 201
}

@GetMapping("/{id}")
public ResponseEntity<?> get(@PathVariable Long id) {
    return ResponseEntity.ok(...); // ✅ 200
}
```

---

### AI-014: Global exception handling with @RestControllerAdvice

**レベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
전역 예외 처리 핸들러 사용.

**Good Example**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }
}
```

---

### AI-015: DTOs MUST use primitive wrappers

**レベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
DTO는 null 가능성을 위해 wrapper 타입 사용.

**Good Example**:
```java
public record OrderRequest(
    Long customerId,  // ✅ Long (nullable)
    Integer quantity  // ✅ Integer (nullable)
) { }
```

**Bad Example**:
```java
public record OrderRequest(
    long customerId,  // ❌ primitive (cannot be null)
    int quantity
) { }
```

---

### AI-016: Controllers MUST have API documentation

**レベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
Public API에 Javadoc/OpenAPI 문서화.

**Good Example**:
```java
/**
 * Creates a new order.
 *
 * @param request order creation details
 * @return created order ID
 */
@PostMapping
@Operation(summary = "Create new order")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Order created"),
    @ApiResponse(responseCode = "400", description = "Invalid request")
})
public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
}
```

---

### AI-017: Controllers MUST have ≤ 10 endpoints (SRP)

**레벨**: 🔴 CRITICAL
**검증**: ArchUnit

**규칙**:
Controller는 최대 10개의 엔드포인트(public 메서드)만 가져야 합니다. 하나의 Controller는 하나의 REST 리소스를 담당해야 합니다.

**Good Example**:
```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    // 엔드포인트: 5개 (SRP 준수 - 단일 Order 리소스)
    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) { }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) { }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @RequestBody UpdateOrderRequest request) { }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list(@RequestParam(required = false) String status) { }
}
```

**Bad Example**:
```java
@RestController
@RequestMapping("/api")
public class ApiController {
    // 엔드포인트: 12개 (SRP 위반 - 여러 리소스)
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(...) { }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrder(...) { }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(...) { }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(...) { }

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(...) { }

    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomer(...) { }

    // ... 6개 더
    // → OrderController, ProductController, CustomerController로 분리 필요
}
```

**리소스별 분리 원칙**:
- 1 Controller = 1 REST Resource
- `/orders` → OrderController
- `/products` → ProductController
- `/customers` → CustomerController

**위반 시**:
- ArchUnit: `SingleResponsibilityTest.java` 실패
- 리소스별로 Controller 분리 필요

---

### AO-001: NO JPA relationship annotations (@OneToMany, @ManyToOne, etc.)

**レベル**: 🔴 CRITICAL
**検証**: Checkstyle + persistence-validator.sh

**規칙**:
JPA 관계 어노테이션 **절대 금지**. Long FK 필드 사용.

**Good Example**:
```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private Long id;

    @Column(name = "customer_id")
    private Long customerId; // ✅ FK as Long

    @Column(name = "product_id")
    private Long productId; // ✅ FK as Long
}
```

**Bad Example**:
```java
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @ManyToOne // ❌ FORBIDDEN
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @OneToMany(mappedBy = "order") // ❌ FORBIDDEN
    private List<OrderLineEntity> lines;
}
```

**위반 시**:
- Checkstyle: RegexpSingleline 검출
- persistence-validator.sh 차단
- 빌드 실패

---

### AO-002: Entities MUST NOT have setter methods

**레벨**: 🔴 CRITICAL
**검증**: Checkstyle + persistence-validator.sh

**規칙**:
JPA Entity는 setter 금지. 불변성 유지.

**Good Example**:
```java
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    protected ProductEntity() { } // ✅ JPA only

    private ProductEntity(Long id, String name) { // ✅ private
        this.id = id;
        this.name = name;
    }

    public static ProductEntity create(Long id, String name) { // ✅ factory
        return new ProductEntity(id, name);
    }
}
```

**Bad Example**:
```java
@Entity
public class ProductEntity {
    @Id
    private Long id;

    private String name;

    public void setName(String name) { // ❌ setter
        this.name = name;
    }
}
```

**위반 시**:
- persistence-validator.sh 차단

---

### AO-003: Entities MUST NOT have public constructors

**レベル**: 🔴 CRITICAL
**検証**: persistence-validator.sh

**規칙**:
JPA Entity는 public 생성자 금지. Protected + static factory method.

**Good Example**:
```java
@Entity
public class OrderEntity {
    @Id
    private Long id;

    protected OrderEntity() { } // ✅ JPA requires no-arg constructor

    private OrderEntity(Long id) { // ✅ private
        this.id = id;
    }

    public static OrderEntity create(Long id) { // ✅ factory method
        return new OrderEntity(id);
    }
}
```

**Bad Example**:
```java
@Entity
public class OrderEntity {
    @Id
    private Long id;

    public OrderEntity() { } // ❌ public

    public OrderEntity(Long id) { // ❌ public
        this.id = id;
    }
}
```

**위반 시**:
- persistence-validator.sh 차단

---

### AO-004: NO @Transactional in Adapters

**レ벨**: 🔴 CRITICAL
**検証**: Checkstyle + persistence-validator.sh

**規칙**:
Adapter에서 @Transactional 사용 금지. Application 레이어에서만.

**Good Example**:
```java
// Application Layer
@Service
@Transactional // ✅ Application layer
public class CreateOrderUseCase {
    public OrderId execute(CreateOrderCommand command) {
        // Transaction boundary
    }
}

// Adapter Layer
@Repository
class OrderJpaAdapter implements OrderRepository { // ✅ NO @Transactional
    public void save(Order order) {
        // Participates in existing transaction
    }
}
```

**Bad Example**:
```java
@Repository
@Transactional // ❌ adapter layer
public class OrderJpaAdapter {
    public void save(Order order) { }
}
```

**위반 시**:
- Checkstyle: Regexp 검출
- persistence-validator.sh 차단

---

### AO-005: Use Mapper for Entity ↔ Domain conversion

**レ벨**: 🟡 IMPORTANT
**検証**: persistence-validator.sh (warning)

**規칙**:
Entity와 Domain 변환은 별도 Mapper 클래스 사용.

**Good Example**:
```java
@Component
class OrderMapper {
    public Order toDomain(OrderEntity entity) {
        return Order.reconstitute(
            new OrderId(entity.getId()),
            new CustomerId(entity.getCustomerId()),
            Money.of(entity.getTotal())
        );
    }

    public OrderEntity toEntity(Order order) {
        return OrderEntity.create(
            order.getId().value(),
            order.getCustomerId().value(),
            order.getTotal().amount()
        );
    }
}
```

**Bad Example**:
```java
@Repository
public class OrderRepository {
    public Order findById(Long id) {
        OrderEntity entity = em.find(OrderEntity.class, id);
        // ❌ inline conversion
        return new Order(entity.getId(), entity.getCustomerId());
    }
}
```

---

### AO-006: Entities MUST have business-agnostic names

**レベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
Entity 클래스는 'Entity' suffix 사용.

**Good Example**:
```java
@Entity
@Table(name = "orders")
public class OrderEntity { } // ✅ Entity suffix
```

**Bad Example**:
```java
@Entity
@Table(name = "orders")
public class Order { } // ❌ conflicts with domain Order
```

---

### AO-007: NO business logic in Entities

**レベル**: 🟡 IMPORTANT
**検証**: persistence-validator.sh (warning)

**規칙**:
Entity는 데이터 저장만. 비즈니스 로직은 Domain에.

**Good Example**:
```java
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    // Only getters, no business methods
}
```

**Bad Example**:
```java
@Entity
public class OrderEntity {
    public void calculateTotal() { } // ❌ business logic
    public void validate() { } // ❌ business logic
}
```

---

### AO-008: Repository implementations MUST be package-private

**레ベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
Repository 구현체는 package-private.

**Good Example**:
```java
@Repository
class OrderJpaAdapter implements OrderRepository { // ✅ package-private
}
```

**Bad Example**:
```java
@Repository
public class OrderJpaAdapter implements OrderRepository { // ❌ public
}
```

---

### AO-009: Use @Column for all fields

**레ベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
명시적 컬럼 매핑.

**Good Example**:
```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;
}
```

---

### AO-010: NO cascading operations

**レベル**: 🔴 CRITICAL
**検証**: Manual Code Review

**規칙**:
JPA 관계 금지로 cascade도 금지.

**Bad Example**:
```java
@OneToMany(cascade = CascadeType.ALL) // ❌ relationship + cascade
private List<OrderLineEntity> lines;
```

---

### AO-011: Use QueryDSL for complex queries

**レベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
복잡한 쿼리는 QueryDSL 사용.

**Good Example**:
```java
@Repository
class OrderQueryAdapter {
    private final JPAQueryFactory queryFactory;

    public List<OrderEntity> findByCriteria(OrderSearchCriteria criteria) {
        QOrderEntity order = QOrderEntity.orderEntity;

        return queryFactory
            .selectFrom(order)
            .where(
                order.customerId.eq(criteria.customerId()),
                order.status.in(criteria.statuses())
            )
            .fetch();
    }
}
```

---

### AO-012: Repositories MUST return Domain objects

**レベル**: 🔴 CRITICAL
**検証**: Manual Code Review

**規칙**:
Repository는 Entity가 아닌 Domain 객체 반환.

**Good Example**:
```java
@Repository
class OrderJpaAdapter implements OrderRepository {
    public Optional<Order> findById(OrderId id) {
        return orderJpaRepository.findById(id.value())
            .map(orderMapper::toDomain); // ✅ return Domain
    }
}
```

**Bad Example**:
```java
public Optional<OrderEntity> findById(Long id) { // ❌ return Entity
    return orderJpaRepository.findById(id);
}
```

---

### AO-013: Use optimistic locking

**レベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
동시성 제어를 위한 @Version 사용.

**Good Example**:
```java
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @Version
    private Long version; // ✅ optimistic locking
}
```

---

### AO-014: NO lazy loading issues

**レベル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
JPA 관계 금지로 N+1 문제 자동 해결.

---

### AO-015: Entity fields MUST match database columns

**レ베ル**: 🟡 IMPORTANT
**検証**: Manual Code Review

**規칙**:
필드명과 컬럼명 일치 또는 명시적 매핑.

**Good Example**:
```java
@Column(name = "customer_id")
private Long customerId; // ✅ explicit mapping
```

---

### AO-016: Use appropriate fetch strategies

**レ베ル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**:
JPA 관계 금지로 불필요. 필요시 명시적 JOIN 쿼리.

**Good Example**:
```java
// QueryDSL
public OrderEntity findWithCustomer(Long orderId) {
    return queryFactory
        .selectFrom(orderEntity)
        .join(customerEntity).on(orderEntity.customerId.eq(customerEntity.id))
        .where(orderEntity.id.eq(orderId))
        .fetchOne();
}
```

---

### 추가 규칙 (AO-017 ~ AO-032) - Common Rules

여기서 나머지 16개 규칙을 추가합니다:

### AO-017: NO cyclic package dependencies

**레ベル**: 🔴 CRITICAL
**検証**: ArchUnit

**規칙**: 패키지 간 순환 의존성 금지

**위반 시**: ArchUnit `slices().should().beFreeOfCycles()` 실패

---

### AO-018: Methods MUST have max 5 parameters

**レベル**: 🟡 IMPORTANT
**検証**: ArchUnit + Checkstyle

**規칙**: 메서드 파라미터 최대 5개

**위반 시**: Checkstyle ParameterNumber > 5

---

### AO-019: Methods MUST have cyclomatic complexity ≤ 10

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 순환 복잡도 10 이하

**위반 시**: Checkstyle CyclomaticComplexity > 10

---

### AO-020: Methods MUST be ≤ 50 lines

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 메서드 길이 50줄 이하

**위반 시**: Checkstyle MethodLength > 50

---

### AO-021: NO star imports

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: `import com.example.*` 금지

**위반 시**: Checkstyle AvoidStarImport

---

### AO-022: NO unused imports

**레ベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 사용하지 않는 import 금지

**위반 시**: Checkstyle UnusedImports

---

### AO-023: Public classes MUST have Javadoc

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: Public 클래스/메서드는 Javadoc 필수

**위반 시**: Checkstyle MissingJavadocMethod

---

### AO-024: Javadoc MUST have @author

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 클래스 Javadoc에 @author 필수

**위반 시**: Checkstyle JavadocType authorFormat

---

### AO-025: Constants MUST be UPPER_SNAKE_CASE

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 상수는 대문자 스네이크 케이스

**Good Example**: `private static final int MAX_RETRY_COUNT = 3;`

**Bad Example**: `private static final int maxRetryCount = 3;`

---

### AO-026: Package names MUST be lowercase

**レベル**: 🟡 IMPORTANT
**検증**: Checkstyle

**規칙**: 패키지명은 소문자만

**위반 시**: Checkstyle PackageName

---

### AO-027: NO empty blocks

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 빈 블록 금지

**위반 시**: Checkstyle EmptyBlock

---

### AO-028: MUST use braces for all control structures

**레벨**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: if/for/while은 반드시 중괄호 사용

**위반 시**: Checkstyle NeedBraces

---

### AO-029: equals() override MUST override hashCode()

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: equals() 오버라이드시 hashCode()도 필수

**위반 시**: Checkstyle EqualsHashCode

---

### AO-030: NO magic numbers

**레ベル**: 🟢 RECOMMENDED
**検証**: Manual Code Review

**規칙**: 매직 넘버 금지, 상수 사용

**Good Example**: `private static final int MAX_ATTEMPTS = 3;`

---

### AO-031: Follow modifier order (public static final)

**レ베ル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 수정자 순서 준수

**위반 시**: Checkstyle ModifierOrder

---

### AO-032: NO redundant modifiers

**レベル**: 🟡 IMPORTANT
**検証**: Checkstyle

**規칙**: 불필요한 수정자 금지 (interface의 public 등)

**위반 시**: Checkstyle RedundantModifier

---

### AO-033: Repositories MUST focus on single Entity (SRP)

**レベル**: 🔴 CRITICAL
**検証**: ArchUnit

**規칙**:
Repository는 하나의 Entity만 다뤄야 합니다. 여러 Entity에 의존하는 것은 여러 책임을 의미합니다.

**Good Example**:
```java
@Repository
class OrderJpaAdapter implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository; // ✅ 단일 Entity Repository
    private final OrderMapper orderMapper;

    public Optional<Order> findById(OrderId id) {
        return orderJpaRepository.findById(id.value())
            .map(orderMapper::toDomain);
    }

    public void save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        orderJpaRepository.save(entity);
    }
}
```

**Bad Example**:
```java
@Repository
class OrderRepositoryImpl {
    // ❌ 여러 Entity 의존 = 여러 책임
    private final OrderJpaRepository orderRepository;
    private final CustomerJpaRepository customerRepository;
    private final ProductJpaRepository productRepository;

    public OrderWithDetails findOrderWithDetails(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow();
        CustomerEntity customer = customerRepository.findById(order.getCustomerId()).orElseThrow();
        // ❌ Repository가 여러 Entity를 직접 조합 → Application Layer에서 해야 함
        return new OrderWithDetails(order, customer);
    }
}
```

**올바른 패턴**:
```java
// ✅ Application Layer에서 명시적 조합
@Service
public class GetOrderWithDetailsUseCase {
    private final LoadOrderPort loadOrderPort;
    private final LoadCustomerPort loadCustomerPort;

    public OrderWithDetailsDto execute(OrderId orderId) {
        Order order = loadOrderPort.loadById(orderId).orElseThrow();
        Customer customer = loadCustomerPort.loadById(order.getCustomerId()).orElseThrow();
        return OrderWithDetailsDto.of(order, customer);
    }
}
```

**위반 시**:
- ArchUnit: `SingleResponsibilityTest.java` - `haveSingleEntityDependency()` 조건 실패
- Application Layer로 로직 이동 필요

---

### AO-034: Entities MUST use Long FK, NO JPA relationships (Law of Demeter)

**レベル**: 🔴 CRITICAL
**検証**: ArchUnit + Checkstyle + persistence-validator.sh

**規칙**:
Entity는 Long FK 필드만 사용해야 합니다. JPA 관계 어노테이션(@OneToMany, @ManyToOne 등)은 Law of Demeter 위반을 유발하므로 절대 금지입니다.

**Good Example**:
```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @Column(name = "id")
    private Long id;

    // ✅ Long FK 전략
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // ✅ Application Layer에서 명시적으로 로드
    // OrderService: loadOrder() + loadCustomer() + loadProduct()
}
```

**Bad Example**:
```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private Long id;

    // ❌ JPA 관계 = Law of Demeter 위반
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderLineEntity> orderLines;
}

// 호출부에서 Law of Demeter 위반 발생
order.getCustomer().getName(); // ❌ Train Wreck
order.getCustomer().getAddress().getCity(); // ❌ 3단계 체이닝
```

**JPA 관계의 문제점**:
1. **Law of Demeter 위반**: Getter 체이닝 유발
2. **N+1 쿼리 문제**: 암묵적 추가 쿼리 발생
3. **양방향 관계 복잡도**: 순환 참조, JSON 직렬화 문제
4. **LazyInitializationException**: 트랜잭션 밖에서 접근 시 예외
5. **테스트 어려움**: Entity 관계 설정 복잡

**Long FK 전략의 장점**:
1. **Law of Demeter 준수**: Getter 체이닝 불가능
2. **명시적 데이터 로딩**: Application Layer에서 제어
3. **성능 예측 가능**: 필요한 데이터만 로드
4. **테스트 단순화**: Entity 독립적 테스트 가능
5. **레이어 분리 명확**: Persistence 관심사 격리

**Application Layer 패턴**:
```java
@Service
public class GetOrderWithCustomerUseCase {
    private final LoadOrderPort loadOrderPort;
    private final LoadCustomerPort loadCustomerPort;

    @Transactional(readOnly = true)
    public OrderWithCustomerDto execute(OrderId orderId) {
        // ✅ 명시적 로드 (Long FK 활용)
        Order order = loadOrderPort.loadById(orderId).orElseThrow();
        Customer customer = loadCustomerPort.loadById(order.getCustomerId()).orElseThrow();
        return OrderWithCustomerDto.of(order, customer);
    }
}
```

**위반 시**:
- ArchUnit: `LawOfDemeterTest.java` 실패
- Checkstyle: RegexpSingleline JPA 관계 어노테이션 검출
- persistence-validator.sh 차단
- 빌드 중단

---

## 요약

총 **96개 규칙**:
- **Domain Layer**: 34개 규칙 (D-001 ~ D-034)
  - 기존 30개 + SRP 2개 (D-031, D-032) + Law of Demeter 2개 (D-033, D-034)
- **Application Layer**: 27개 규칙 (A-001 ~ A-027)
  - 기존 25개 + SRP 2개 (A-026, A-027)
- **Adapter Layer**: 35개 규칙
  - **Controller (AI)**: 17개 (AI-001 ~ AI-017) - 기존 16개 + SRP 1개 (AI-017)
  - **Persistence (AO)**: 18개 (AO-001 ~ AO-034) - 기존 16개 + SRP 1개 (AO-033) + Law of Demeter 1개 (AO-034)

모든 규칙은 다음 도구로 자동 검증:
- ArchUnit (아키텍처 경계)
- Checkstyle (코드 품질)
- Git Hooks (실시간 검증)

---

**문서 버전**: 1.0.0
**최종 업데이트**: 2025-10-05
**담당팀**: arch-team@company.com
