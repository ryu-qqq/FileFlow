# Audit Entity Pattern (감사 엔티티 패턴)

**목적**: 공통 감사 필드 관리 및 소프트 딜리트 전략

**위치**: `common/entity/` (공통), `adapter-persistence/[module]/entity/` (사용처)

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### 1. 공통 감사 필드 사용

모든 JPA Entity는 `BaseAuditEntity` 또는 `SoftDeletableEntity`를 상속받아야 합니다.

### 2. 모든 삭제는 소프트 딜리트

**Zero-Tolerance 규칙**: 물리 삭제(`DELETE FROM`) 사용 금지. 모든 삭제는 `deletedAt` 타임스탬프로 처리합니다.

---

## 📦 제공되는 추상 클래스

### BaseAuditEntity

**위치**: `common/entity/BaseAuditEntity.java`

**제공 필드**:
- `createdAt`: 생성 일시
- `updatedAt`: 수정 일시

**사용 시기**: 소프트 딜리트가 불필요한 Entity

```java
@MappedSuperclass
public abstract class BaseAuditEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BaseAuditEntity() {
    }

    protected BaseAuditEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### SoftDeletableEntity

**위치**: `common/entity/SoftDeletableEntity.java`

**제공 필드**:
- `createdAt`: 생성 일시 (BaseAuditEntity 상속)
- `updatedAt`: 수정 일시 (BaseAuditEntity 상속)
- `deletedAt`: 삭제 일시

**사용 시기**: 소프트 딜리트가 필요한 Entity (대부분의 경우)

```java
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseAuditEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected SoftDeletableEntity() {
        super();
    }

    protected SoftDeletableEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
    }

    protected SoftDeletableEntity(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(createdAt, updatedAt);
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
```

---

## 📋 사용 예시

### 예시 1: BaseAuditEntity 사용 (소프트 딜리트 불필요)

```java
@Entity
@Table(name = "system_logs")
public class SystemLogJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogLevel level;

    // Protected no-args constructor
    protected SystemLogJpaEntity() {
        super();
    }

    // Private constructor
    private SystemLogJpaEntity(String message, LogLevel level, LocalDateTime createdAt) {
        super(createdAt, createdAt);  // createdAt = updatedAt
        this.message = message;
        this.level = level;
    }

    // Static factory - create
    public static SystemLogJpaEntity create(String message, LogLevel level) {
        return new SystemLogJpaEntity(message, level, LocalDateTime.now());
    }

    // Getters
    public Long getId() { return id; }
    public String getMessage() { return message; }
    public LogLevel getLevel() { return level; }
}
```

### 예시 2: SoftDeletableEntity 사용 (소프트 딜리트 필요)

```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // Protected no-args constructor
    protected OrderJpaEntity() {
        super();
    }

    // Private constructor (create)
    private OrderJpaEntity(Long userId, OrderStatus status, LocalDateTime createdAt) {
        super(createdAt, createdAt);  // createdAt = updatedAt, deletedAt = null
        this.userId = userId;
        this.status = status;
    }

    // Private constructor (reconstitute)
    private OrderJpaEntity(Long id, Long userId, OrderStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.userId = userId;
        this.status = status;
    }

    // Static factory - create
    public static OrderJpaEntity create(Long userId) {
        return new OrderJpaEntity(userId, OrderStatus.PENDING, LocalDateTime.now());
    }

    // Static factory - reconstitute
    public static OrderJpaEntity reconstitute(Long id, Long userId, OrderStatus status,
                                               LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        return new OrderJpaEntity(id, userId, status, createdAt, updatedAt, deletedAt);
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
}
```

---

## 🚨 소프트 딜리트 규칙

### 규칙 1: 물리 삭제 금지

```java
// ❌ Bad - 물리 삭제
@Modifying
@Query("DELETE FROM OrderJpaEntity o WHERE o.id = :id")
void deleteById(@Param("id") Long id);

// ✅ Good - 소프트 딜리트
@Modifying
@Query("UPDATE OrderJpaEntity o SET o.deletedAt = :deletedAt WHERE o.id = :id")
void softDeleteById(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);
```

### 규칙 2: 조회 시 삭제된 데이터 제외

```java
// ✅ Good - deletedAt IS NULL 조건 추가
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.deletedAt IS NULL")
    List<OrderJpaEntity> findAllActive();

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.id = :id AND o.deletedAt IS NULL")
    Optional<OrderJpaEntity> findActiveById(@Param("id") Long id);
}
```

### 규칙 3: Application Layer에서 명시적 삭제 처리

```java
@Service
@Transactional
public class CancelOrderService implements CancelOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final DeleteOrderPort deleteOrderPort;

    @Override
    public void execute(CancelOrderCommand command) {
        // 1. Order 조회
        Order order = loadOrderPort.load(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        // 2. 비즈니스 검증 (Domain Layer)
        order.validateCancellable();

        // 3. 소프트 딜리트 (Port 호출)
        deleteOrderPort.softDelete(order.getId());
    }
}
```

---

## 📋 체크리스트

Entity 작성 시:
- [ ] `BaseAuditEntity` 또는 `SoftDeletableEntity` 상속
- [ ] 소프트 딜리트가 필요하면 `SoftDeletableEntity` 사용
- [ ] 소프트 딜리트가 불필요하면 `BaseAuditEntity` 사용 (예: 로그, 이벤트)
- [ ] `super(createdAt, updatedAt)` 생성자 호출
- [ ] 물리 삭제 쿼리 없음 (`DELETE FROM` 금지)
- [ ] 조회 쿼리에 `deletedAt IS NULL` 조건 포함
- [ ] Repository 메서드명에 `Active` 명시 (예: `findAllActive()`)

---

## 📖 관련 문서

- **[Core Rules](./00_jpa-entity-core-rules.md)** - JPA Entity 핵심 설계 규칙
- **[Constructor Pattern](./02_constructor-pattern.md)** - 3-Tier Constructor 패턴
- **[Long FK Strategy](./01_long-fk-strategy.md)** - Long FK 전략

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
