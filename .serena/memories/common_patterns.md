# FileFlow Common Development Patterns

## Domain Layer Patterns

### 1. Aggregate Root Pattern
```java
/**
 * Order Aggregate Root
 * 
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class Order {
    private final Long id;
    private final String orderNumber;
    private final List<OrderLine> orderLines;
    private OrderStatus status;
    
    private Order(Long id, String orderNumber) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.orderLines = new ArrayList<>();
        this.status = OrderStatus.PENDING;
    }
    
    // Static factory method (preferred over public constructor)
    public static Order create(String orderNumber) {
        return new Order(null, orderNumber);
    }
    
    // Tell, Don't Ask - encapsulate business logic
    public void complete() {
        if (!canComplete()) {
            throw new InvalidOrderStateException("Cannot complete order in " + status);
        }
        this.status = OrderStatus.COMPLETED;
    }
    
    private boolean canComplete() {
        return status == OrderStatus.PENDING && !orderLines.isEmpty();
    }
    
    // Law of Demeter - provide domain query, not getter chain
    public boolean hasProductInCategory(String categoryCode) {
        return orderLines.stream()
            .anyMatch(line -> line.belongsToCategory(categoryCode));
    }
    
    // Manual getters (No Lombok!)
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getOrderLines() { 
        return Collections.unmodifiableList(orderLines); 
    }
}
```

### 2. Value Object Pattern
```java
/**
 * Money Value Object (immutable)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    private Money(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
        this.currency = currency;
    }
    
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot operate on different currencies");
        }
    }
    
    // Manual getters
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && 
               currency.equals(money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
```

## Application Layer Patterns

### 3. Use Case Pattern (Command)
```java
/**
 * Create Order Use Case
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;
    
    public CreateOrderUseCase(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        EventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional  // Transaction boundary at use case
    public OrderResponse execute(CreateOrderCommand command) {
        // 1. Load domain objects (inside transaction)
        List<Product> products = productRepository.findByIds(command.getProductIds());
        
        // 2. Execute domain logic (inside transaction)
        Order order = Order.create(generateOrderNumber());
        products.forEach(order::addProduct);
        
        // 3. Persist (inside transaction)
        Order savedOrder = orderRepository.save(order);
        
        // 4. Publish events (inside transaction - ensure atomicity)
        eventPublisher.publish(new OrderCreated(savedOrder.getId()));
        
        // 5. Return response DTO
        return OrderAssembler.toResponse(savedOrder);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
```

### 4. Assembler Pattern (DTO Conversion)
```java
/**
 * Order Assembler - converts between domain and DTOs
 * 
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class OrderAssembler {
    
    // Domain → Response DTO
    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus().name(),
            order.getOrderLines().stream()
                .map(OrderAssembler::toLineResponse)
                .collect(Collectors.toList())
        );
    }
    
    private static OrderLineResponse toLineResponse(OrderLine line) {
        return new OrderLineResponse(
            line.getProductId(),
            line.getProductName(),
            line.getQuantity()
        );
    }
    
    // Request DTO → Command
    public static CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
            request.getCustomerId(),
            request.getProductIds()
        );
    }
}
```

## Adapter Layer Patterns

### 5. REST Controller Pattern
```java
/**
 * Order REST Controller
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    
    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = OrderApiMapper.toCommand(request);
        OrderResponse response = createOrderUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        // Implementation
        return ResponseEntity.ok(null);
    }
}
```

### 6. JPA Entity Pattern (Long FK Only)
```java
/**
 * Order JPA Entity
 * Note: Uses Long FK strategy, NO JPA relationships (@ManyToOne, @OneToMany, etc.)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    
    // ✅ Long FK - NOT @ManyToOne relationship
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    // Manual getters and setters (No Lombok!)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
```

### 7. Repository Pattern
```java
/**
 * Order JPA Repository
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    List<OrderEntity> findByCustomerId(Long customerId);
    
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status")
    List<OrderEntity> findByStatus(@Param("status") OrderStatus status);
}
```

## Testing Patterns

### 8. Domain Unit Test
```java
/**
 * Order Domain Test
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
class OrderTest {
    
    @Test
    void shouldCreateOrderWithPendingStatus() {
        // Given
        String orderNumber = "ORD-001";
        
        // When
        Order order = Order.create(orderNumber);
        
        // Then
        assertThat(order.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
    
    @Test
    void shouldCompleteOrderWhenValid() {
        // Given
        Order order = Order.create("ORD-001");
        order.addProduct(1L, "Product A", 2);
        
        // When
        order.complete();
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
    
    @Test
    void shouldThrowExceptionWhenCompletingEmptyOrder() {
        // Given
        Order order = Order.create("ORD-001");
        
        // When & Then
        assertThatThrownBy(() -> order.complete())
            .isInstanceOf(InvalidOrderStateException.class)
            .hasMessageContaining("Cannot complete order");
    }
}
```

## Anti-Patterns to Avoid

### ❌ Lombok Usage
```java
// DON'T DO THIS
@Data
@Builder
public class Order { ... }
```

### ❌ Getter Chaining (Law of Demeter Violation)
```java
// DON'T DO THIS
String zip = order.getCustomer().getAddress().getZip();

// DO THIS
String zip = order.getCustomerZip();
```

### ❌ JPA Relationships
```java
// DON'T DO THIS
@Entity
public class Order {
    @ManyToOne
    private Customer customer;  // ❌ NO!
}

// DO THIS
@Entity
public class Order {
    private Long customerId;  // ✅ YES!
}
```

### ❌ External Calls in Transactions
```java
// DON'T DO THIS
@Transactional
public void createOrder(CreateOrderCommand command) {
    Order order = Order.create();
    orderRepository.save(order);
    
    // ❌ External API call inside transaction!
    restTemplate.postForObject(url, request, Response.class);
}

// DO THIS
public void createOrder(CreateOrderCommand command) {
    // External call OUTSIDE transaction
    Response externalResponse = restTemplate.postForObject(url, request, Response.class);
    
    // Transaction boundary starts here
    executeInTransaction(command, externalResponse);
}

@Transactional
private void executeInTransaction(CreateOrderCommand command, Response externalResponse) {
    Order order = Order.create();
    orderRepository.save(order);
}
```
