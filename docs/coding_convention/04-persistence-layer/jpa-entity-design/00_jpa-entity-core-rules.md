# JPA Entity 핵심 설계 규칙

**목적**: JPA Entity 설계의 Zero-Tolerance 규칙 정의

**위치**: `adapter-persistence/[module]/entity/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

JPA Entity는 **데이터 매핑 전용 객체**입니다. 비즈니스 로직은 Domain Layer에 위치해야 합니다.

### Entity vs Domain Model 분리

```
Persistence Layer (Entity)    →  Domain Layer (Domain Model)
    ├─ 데이터베이스 매핑            ├─ 비즈니스 로직
    ├─ Getter만 제공               ├─ 상태 변경 메서드
    ├─ 3개 생성자                  ├─ 도메인 규칙 검증
    └─ JPA 어노테이션              └─ Tell, Don't Ask
```

---

## 🚨 Zero-Tolerance 규칙 (5가지)

### 1. Lombok 금지

**규칙**: 모든 Lombok 어노테이션 사용 금지

**이유**:
- JPA Lazy Loading 문제 (`@ToString`, `@EqualsAndHashCode`)
- 양방향 관계 무한 루프
- 불변성 보장 불가 (`@Data`, `@Builder`)

```java
// ❌ Bad - Lombok 사용
@Entity
@Data  // 금지!
@Builder  // 금지!
@NoArgsConstructor  // 금지!
@AllArgsConstructor  // 금지!
public class OrderJpaEntity {
    @Id
    private Long id;
}

// ✅ Good - Plain Java
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    // Protected no-args constructor (JPA)
    protected OrderJpaEntity() {
        this.userId = null;
    }

    // Private constructor (for factory)
    private OrderJpaEntity(Long userId, OrderStatus status) {
        this.userId = userId;
        this.status = status;
    }

    // Static factory methods
    public static OrderJpaEntity create(Long userId) {
        return new OrderJpaEntity(userId, OrderStatus.PENDING);
    }

    public static OrderJpaEntity reconstitute(Long id, Long userId, OrderStatus status) {
        OrderJpaEntity entity = new OrderJpaEntity(userId, status);
        entity.id = id;
        return entity;
    }

    // Getters only
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
}
```

---

### 2. JPA 관계 어노테이션 금지 (Long FK 전략)

**규칙**: `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` 사용 금지

**이유**:
- N+1 쿼리 문제 근본 차단
- Law of Demeter 위반 방지 (`order.getCustomer().getAddress()` 불가)
- 명시적 데이터 로딩 강제

```java
// ❌ Bad - JPA 관계 어노테이션
@Entity
public class OrderJpaEntity {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 금지!
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;  // Entity 참조 금지!
}

// ✅ Good - Long FK
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // Long FK 사용

    // Getter
    public Long getUserId() { return userId; }
}
```

**Application Layer에서 명시적 조회**:

```java
@Service
@Transactional(readOnly = true)
public class GetOrderWithUserService implements GetOrderWithUserUseCase {

    private final LoadOrderPort loadOrderPort;
    private final LoadUserPort loadUserPort;

    @Override
    public OrderWithUserResponse execute(GetOrderQuery query) {
        // 1. Order 조회
        Order order = loadOrderPort.load(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));

        // 2. User 조회 (Long FK 사용)
        User user = loadUserPort.load(order.getUserId())
            .orElseThrow(() -> new UserNotFoundException(order.getUserId()));

        // 3. 조합
        return OrderWithUserResponse.of(order, user);
    }
}
```

---

### 3. Entity 불변성 (비즈니스 로직 금지)

**규칙**: Entity에는 비즈니스 로직이 절대 없어야 함

**허용되는 것**:
- ✅ Getter 메서드
- ✅ 3개 생성자 (no-args, create, reconstitute)
- ✅ Static factory 메서드

**금지되는 것**:
- ❌ 비즈니스 메서드 (`confirm()`, `cancel()`, `ship()` 등)
- ❌ Setter 메서드
- ❌ 상태 변경 로직
- ❌ 검증 로직

```java
// ❌ Bad - 비즈니스 로직 포함
@Entity
public class OrderJpaEntity {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // ❌ 비즈니스 메서드 금지!
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm non-pending order");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    // ❌ Setter 금지!
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

// ✅ Good - 데이터 매핑만
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    // Protected no-args constructor (JPA)
    protected OrderJpaEntity() {
        this.userId = null;
        this.status = null;
        this.totalAmount = null;
    }

    // Private constructor
    private OrderJpaEntity(Long userId, OrderStatus status, BigDecimal totalAmount) {
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    // Static factory - create (신규 저장)
    public static OrderJpaEntity create(Long userId, BigDecimal totalAmount) {
        return new OrderJpaEntity(userId, OrderStatus.PENDING, totalAmount);
    }

    // Static factory - reconstitute (DB 조회)
    public static OrderJpaEntity reconstitute(Long id, Long userId, OrderStatus status, BigDecimal totalAmount) {
        OrderJpaEntity entity = new OrderJpaEntity(userId, status, totalAmount);
        entity.id = id;
        return entity;
    }

    // ✅ Getter만 제공
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
```

**Domain Model에서 비즈니스 로직 구현**:

```java
// Domain Layer
public class Order {
    private final OrderId id;
    private final UserId userId;
    private OrderStatus status;
    private final Money totalAmount;

    // ✅ 비즈니스 메서드는 Domain Layer에
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderCannotBeConfirmedException(
                "Order must be in PENDING status to confirm. Current: " + this.status
            );
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(
                "Delivered orders cannot be cancelled"
            );
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

---

### 4. Setter 금지

**규칙**: 모든 Setter 메서드 사용 금지

**이유**:
- 불변성 보장
- 의도하지 않은 상태 변경 방지
- Entity는 생성 후 변경 불가 (Immutable)

```java
// ❌ Bad - Setter 사용
@Entity
public class OrderJpaEntity {
    @Id
    private Long id;

    private OrderStatus status;

    // ❌ Setter 금지!
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

// ✅ Good - Getter만, 새 Entity 생성으로 변경
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // Getter만
    public OrderStatus getStatus() { return status; }

    // 변경이 필요하면 새 Entity 생성
    public static OrderJpaEntity reconstitute(Long id, OrderStatus newStatus) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = id;
        entity.status = newStatus;
        return entity;
    }
}
```

---

### 5. 3-Tier Constructor 패턴

**규칙**: 정확히 3개의 생성자만 허용

**3가지 생성자**:
1. **Protected no-args**: JPA 전용
2. **Protected create**: 신규 저장 (ID 없음)
3. **Private reconstitute**: DB 조회 (ID 있음)

```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // ✅ Tier 1: Protected no-args (JPA)
    protected OrderJpaEntity() {
        super();
        this.userId = null;
        this.status = null;
    }

    // ✅ Tier 2: Protected create (신규 저장, ID 없음)
    protected OrderJpaEntity(Long userId, OrderStatus status, LocalDateTime createdAt) {
        super(createdAt, createdAt);  // BaseAuditEntity
        this.userId = userId;
        this.status = status;
    }

    // ✅ Tier 3: Private reconstitute (DB 조회, ID 있음)
    private OrderJpaEntity(Long id, Long userId, OrderStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdAt, updatedAt);  // BaseAuditEntity
        this.id = id;
        this.userId = userId;
        this.status = status;
    }

    // ✅ Static Factory - create
    public static OrderJpaEntity create(Long userId) {
        return new OrderJpaEntity(userId, OrderStatus.PENDING, LocalDateTime.now());
    }

    // ✅ Static Factory - reconstitute
    public static OrderJpaEntity reconstitute(Long id, Long userId, OrderStatus status,
                                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new OrderJpaEntity(id, userId, status, createdAt, updatedAt);
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
}
```

---

## 📋 체크리스트

Entity 작성 전 확인:
- [ ] Lombok 어노테이션 없음 (`@Data`, `@Builder`, `@Getter` 등)
- [ ] JPA 관계 어노테이션 없음 (`@OneToMany`, `@ManyToOne` 등)
- [ ] Long FK 사용 (`private Long userId;`)
- [ ] 비즈니스 메서드 없음 (`confirm()`, `cancel()` 등)
- [ ] Setter 메서드 없음
- [ ] 정확히 3개 생성자 (no-args, create, reconstitute)
- [ ] Getter만 제공
- [ ] Static factory 메서드 사용

---

## 📖 관련 문서

- **[Long FK Strategy](./01_long-fk-strategy.md)** - Long FK 전략 상세 가이드
- **[Constructor Pattern](./02_constructor-pattern.md)** - 3-Tier Constructor 패턴 상세
- **[Audit Entity Pattern](./03_audit-entity-pattern.md)** - BaseAuditEntity, SoftDeletableEntity 사용
- **[ArchUnit JPA Entity Rules](../../05-testing/05_archunit-jpa-entity-rules.md)** - 자동 검증 규칙

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
