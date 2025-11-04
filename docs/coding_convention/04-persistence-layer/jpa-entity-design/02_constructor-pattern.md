# Constructor Pattern (3-Tier 생성자 패턴)

**목적**: JPA Entity의 생성자 및 Static Factory 메서드 패턴 정의

**위치**: `adapter-persistence/[module]/entity/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### 3-Tier Constructor 패턴

JPA Entity는 **정확히 3개의 생성자**와 **2개의 Static Factory 메서드**를 가져야 합니다.

```
Tier 1: Protected no-args Constructor (JPA 전용)
Tier 2: Protected Constructor (신규 저장, ID 없음)
Tier 3: Private Constructor (DB 조회, ID 있음)

Static Factory Methods:
- create() (Tier 2 호출)
- reconstitute() (Tier 3 호출)
```

---

## 📦 3-Tier Constructor 상세

### Tier 1: Protected No-Args Constructor (JPA 전용)

**목적**: JPA가 Entity를 생성할 때 사용

**규칙**:
- `protected` 접근 제한자
- 매개변수 없음
- 모든 필드 `null` 초기화 (참조 타입) 또는 기본값 (원시 타입)
- **Application Layer에서 절대 사용 금지**

```java
// ✅ Tier 1: JPA 전용
protected OrderJpaEntity() {
    super();  // BaseAuditEntity 호출
    this.userId = null;
    this.orderNumber = null;
    this.status = null;
}
```

### Tier 2: Protected Constructor (신규 저장, ID 없음)

**목적**: 신규 Entity 생성 시 사용 (DB 저장 전)

**규칙**:
- `protected` 접근 제한자
- **ID 필드 제외**, 비즈니스 필드만 매개변수
- `createdAt`, `updatedAt`은 동일한 값으로 초기화
- `create()` Static Factory 메서드에서만 호출

```java
// ✅ Tier 2: 신규 저장 (ID 없음)
protected OrderJpaEntity(Long userId, String orderNumber, OrderStatus status,
                         LocalDateTime createdAt) {
    super(createdAt, createdAt);  // createdAt = updatedAt
    this.userId = userId;
    this.orderNumber = orderNumber;
    this.status = status;
}
```

### Tier 3: Private Constructor (DB 조회, ID 있음)

**목적**: DB에서 조회한 Entity 재구성 시 사용

**규칙**:
- `private` 접근 제한자
- **ID 필드 포함**, 모든 필드를 매개변수로 받음
- `createdAt`, `updatedAt`은 DB 값 그대로 사용
- `reconstitute()` Static Factory 메서드에서만 호출

```java
// ✅ Tier 3: DB 조회 (ID 있음)
private OrderJpaEntity(Long id, Long userId, String orderNumber, OrderStatus status,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
    super(createdAt, updatedAt);
    this.id = id;
    this.userId = userId;
    this.orderNumber = orderNumber;
    this.status = status;
}
```

---

## 🏭 Static Factory Methods

### create() - 신규 Entity 생성

**목적**: Application Layer에서 신규 Entity를 생성할 때 사용

**규칙**:
- `public static` 메서드
- Tier 2 Constructor 호출
- ID 없이 생성
- `createdAt = LocalDateTime.now()`

```java
// ✅ Static Factory - create
public static OrderJpaEntity create(Long userId, String orderNumber) {
    return new OrderJpaEntity(
        userId,
        orderNumber,
        OrderStatus.PENDING,
        LocalDateTime.now()
    );
}
```

### reconstitute() - DB 조회 Entity 재구성

**목적**: DB에서 조회한 데이터를 Entity로 재구성할 때 사용

**규칙**:
- `public static` 메서드
- Tier 3 Constructor 호출
- ID 포함, 모든 필드 전달
- `createdAt`, `updatedAt` DB 값 사용

```java
// ✅ Static Factory - reconstitute
public static OrderJpaEntity reconstitute(Long id, Long userId, String orderNumber,
                                           OrderStatus status, LocalDateTime createdAt,
                                           LocalDateTime updatedAt) {
    return new OrderJpaEntity(id, userId, orderNumber, status, createdAt, updatedAt);
}
```

---

## 📋 전체 예시

### 기본 Entity (BaseAuditEntity 상속)

```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // ✅ Tier 1: Protected no-args (JPA)
    protected OrderJpaEntity() {
        super();
        this.userId = null;
        this.orderNumber = null;
        this.status = null;
    }

    // ✅ Tier 2: Protected (신규 저장, ID 없음)
    protected OrderJpaEntity(Long userId, String orderNumber, OrderStatus status,
                             LocalDateTime createdAt) {
        super(createdAt, createdAt);
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.status = status;
    }

    // ✅ Tier 3: Private (DB 조회, ID 있음)
    private OrderJpaEntity(Long id, Long userId, String orderNumber, OrderStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.status = status;
    }

    // ✅ Static Factory - create
    public static OrderJpaEntity create(Long userId, String orderNumber) {
        return new OrderJpaEntity(userId, orderNumber, OrderStatus.PENDING, LocalDateTime.now());
    }

    // ✅ Static Factory - reconstitute
    public static OrderJpaEntity reconstitute(Long id, Long userId, String orderNumber,
                                               OrderStatus status, LocalDateTime createdAt,
                                               LocalDateTime updatedAt) {
        return new OrderJpaEntity(id, userId, orderNumber, status, createdAt, updatedAt);
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getStatus() { return status; }
}
```

### 소프트 딜리트 Entity (SoftDeletableEntity 상속)

```java
@Entity
@Table(name = "products")
public class ProductJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    // ✅ Tier 1: Protected no-args (JPA)
    protected ProductJpaEntity() {
        super();
    }

    // ✅ Tier 2: Protected (신규 저장, ID 없음, deletedAt 없음)
    protected ProductJpaEntity(String name, BigDecimal price, LocalDateTime createdAt) {
        super(createdAt, createdAt);  // deletedAt = null
        this.name = name;
        this.price = price;
    }

    // ✅ Tier 3: Private (DB 조회, ID 있음, deletedAt 포함)
    private ProductJpaEntity(Long id, String name, BigDecimal price,
                             LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // ✅ Static Factory - create
    public static ProductJpaEntity create(String name, BigDecimal price) {
        return new ProductJpaEntity(name, price, LocalDateTime.now());
    }

    // ✅ Static Factory - reconstitute
    public static ProductJpaEntity reconstitute(Long id, String name, BigDecimal price,
                                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                                 LocalDateTime deletedAt) {
        return new ProductJpaEntity(id, name, price, createdAt, updatedAt, deletedAt);
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
}
```

---

## 🔄 Application Layer 사용 예시

### 신규 Entity 저장

```java
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final SaveOrderPort saveOrderPort;

    @Override
    public OrderResponse execute(CreateOrderCommand command) {
        // ✅ create() 사용 (신규 저장)
        OrderJpaEntity entity = OrderJpaEntity.create(
            command.userId(),
            command.orderNumber()
        );

        OrderJpaEntity savedEntity = saveOrderPort.save(entity);

        return OrderMapper.toResponse(savedEntity);
    }
}
```

### DB 조회 Entity 재구성

```java
@Component
public class OrderJpaAdapter implements LoadOrderPort {

    private final OrderJpaRepository orderRepository;

    @Override
    public Optional<Order> load(OrderId orderId) {
        Optional<OrderJpaEntity> entityOpt = orderRepository.findById(orderId.getValue());

        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        OrderJpaEntity entity = entityOpt.get();

        // ✅ reconstitute() 사용 (DB 조회)
        OrderJpaEntity reconstituted = OrderJpaEntity.reconstitute(
            entity.getId(),
            entity.getUserId(),
            entity.getOrderNumber(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );

        // Domain Model로 변환
        return Optional.of(OrderMapper.toDomain(reconstituted));
    }
}
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Public no-args constructor
public OrderJpaEntity() { }  // JPA만 사용해야 함

// ❌ Public constructor (신규 저장)
public OrderJpaEntity(Long userId, String orderNumber) { }  // Static factory 사용

// ❌ Setter 사용
public void setStatus(OrderStatus status) {  // Setter 금지
    this.status = status;
}

// ❌ Application Layer에서 new 사용
OrderJpaEntity order = new OrderJpaEntity();  // Static factory 사용해야 함
order.setUserId(userId);  // Setter 금지
```

### ✅ Good Examples

```java
// ✅ Static factory 사용 (신규 저장)
OrderJpaEntity order = OrderJpaEntity.create(userId, orderNumber);

// ✅ Static factory 사용 (DB 조회)
OrderJpaEntity order = OrderJpaEntity.reconstitute(
    id, userId, orderNumber, status, createdAt, updatedAt
);

// ✅ Immutable (Getter만)
Long userId = order.getUserId();
String orderNumber = order.getOrderNumber();
```

---

## 📋 체크리스트

Entity 작성 시:
- [ ] 정확히 3개 생성자 (Tier 1, 2, 3)
- [ ] Tier 1: `protected` no-args (JPA 전용)
- [ ] Tier 2: `protected` (신규 저장, ID 없음)
- [ ] Tier 3: `private` (DB 조회, ID 있음)
- [ ] `create()` Static Factory (신규 저장)
- [ ] `reconstitute()` Static Factory (DB 조회)
- [ ] Application Layer에서 `new` 사용 안 함
- [ ] Setter 메서드 없음

---

## 📖 관련 문서

- **[Core Rules](./00_jpa-entity-core-rules.md)** - JPA Entity 핵심 설계 규칙
- **[Long FK Strategy](./01_long-fk-strategy.md)** - Long FK 전략
- **[Audit Entity Pattern](./03_audit-entity-pattern.md)** - BaseAuditEntity, SoftDeletableEntity

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
