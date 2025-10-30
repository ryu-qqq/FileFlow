# JPA 감사 엔티티 패턴 (Audit Entity Pattern)

## 개요

JPA 엔티티의 공통 감사 필드(`createdAt`, `updatedAt`, `deletedAt`)를 추상 클래스로 분리하여 중복 코드를 제거하고 일관성을 유지하는 패턴입니다.

## 아키텍처 계층 구조

```
BaseAuditEntity (추상 클래스)
├─ createdAt: LocalDateTime
├─ updatedAt: LocalDateTime
└─ markAsUpdated(): void

     ↓ 상속 (소프트 딜리트 불필요)

ExampleJpaEntity
├─ id: Long
├─ message: String
├─ status: ExampleStatus
└─ (createdAt, updatedAt 상속)

     ↓ 상속 (소프트 딜리트 필요)

SoftDeletableEntity (추상 클래스)
├─ deletedAt: LocalDateTime
├─ isDeleted(): boolean
├─ markAsDeleted(): void
└─ restore(): void

     ↓ 상속

OrderJpaEntity
├─ id: Long
├─ orderNumber: String
└─ (createdAt, updatedAt, deletedAt 상속)
```

---

## 1. BaseAuditEntity (공통 감사 필드)

### 역할
- 모든 JPA 엔티티의 기본 감사 필드 제공
- `createdAt`, `updatedAt` 관리
- `markAsUpdated()` 메서드로 수정 일시 자동 갱신

### 코드 예시

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### 사용 예시

```java
@Entity
@Table(name = "example")
public class ExampleJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", nullable = false)
    private String message;

    protected ExampleJpaEntity() {
        super();
    }

    public ExampleJpaEntity(
        Long id,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.message = message;
    }

    // 비즈니스 메서드
    public void updateMessage(String newMessage) {
        this.message = newMessage;
        markAsUpdated();  // 수정 일시 자동 갱신
    }
}
```

---

## 2. SoftDeletableEntity (소프트 딜리트)

### 역할
- `BaseAuditEntity` 상속
- 논리적 삭제 필드 제공 (`deletedAt`)
- **중요: 비즈니스 로직은 포함하지 않음!**

### ⚠️ 핵심 원칙: Entity는 데이터 컨테이너일 뿐

**잘못된 설계 (비즈니스 로직이 Entity에 존재):**
```java
// ❌ Entity에 비즈니스 로직 포함
orderEntity.markAsDeleted();  // 삭제 여부 결정은 비즈니스 로직!
orderEntity.restore();         // 복구 여부 결정도 비즈니스 로직!
```

**올바른 설계 (비즈니스 로직은 Domain Layer):**
```java
// ✅ Domain Layer: 비즈니스 로직
OrderDomain orderDomain = ...;
orderDomain.delete();  // 비즈니스 검증 후 deletedAt 설정

// ✅ Mapper: Domain → Entity 변환
OrderJpaEntity entity = mapper.toEntity(orderDomain);

// ✅ Repository: 단순 저장
repository.save(entity);
```

### 소프트 딜리트 전략

**장점:**
- 삭제 이력 추적 가능
- 데이터 복구 가능
- GDPR 준수 (삭제 기록 보존)

**단점:**
- 디스크 공간 증가
- 쿼리 복잡도 증가 (WHERE deleted_at IS NULL 필수)
- 인덱스 최적화 필요

### 사용 여부 결정 기준

| 엔티티 타입 | 소프트 딜리트 | 이유 |
|------------|--------------|------|
| 주문 (Order) | ✅ O | 삭제 이력 추적, 복구 필요 |
| 사용자 (User) | ✅ O | GDPR 준수, 복구 가능성 |
| 게시글 (Post) | ✅ O | 삭제 이력, 복구 필요 |
| 로그 (AuditLog) | ❌ X | 임시 데이터, 용량 최적화 |
| 세션 (Session) | ❌ X | 만료 후 물리적 삭제 |

### 코드 예시

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
        this.deletedAt = null;  // 기본값: 활성 상태
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * 단순 상태 조회만 수행 (비즈니스 로직 없음)
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 단순 상태 조회만 수행 (비즈니스 로직 없음)
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    // ❌ markAsDeleted(), restore() 같은 비즈니스 메서드는 제공하지 않음!
}
```

### 사용 예시 (올바른 레이어 분리)

```java
// ============================================
// 1. Domain Layer: 비즈니스 로직
// ============================================
public class OrderDomain {

    private OrderId orderId;
    private OrderStatus status;
    private LocalDateTime deletedAt;

    /**
     * 주문 삭제 (비즈니스 로직)
     */
    public void delete() {
        // 비즈니스 검증
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("완료된 주문은 삭제할 수 없습니다.");
        }

        if (isDeleted()) {
            throw new IllegalStateException("이미 삭제된 주문입니다.");
        }

        // 삭제 처리
        this.deletedAt = LocalDateTime.now();
        this.status = OrderStatus.DELETED;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}

// ============================================
// 2. Application Layer: UseCase
// ============================================
@UseCase
@Transactional
public class DeleteOrderUseCase implements DeleteOrderPort {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;

    @Override
    public void execute(OrderId orderId) {
        // 1. Domain 조회
        OrderDomain orderDomain = loadOrderPort.loadOrder(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. Domain에서 비즈니스 로직 실행
        orderDomain.delete();  // 여기서 비즈니스 검증 수행

        // 3. 저장
        saveOrderPort.save(orderDomain);
    }
}

// ============================================
// 3. Persistence Layer: Adapter
// ============================================
@Component
public class OrderPersistenceAdapter implements SaveOrderPort {

    private final OrderRepository orderRepository;
    private final OrderEntityMapper orderEntityMapper;

    @Override
    public void save(OrderDomain orderDomain) {
        // Domain → Entity 변환 (단순 매핑)
        OrderJpaEntity entity = orderEntityMapper.toEntity(orderDomain);

        // 저장
        orderRepository.save(entity);
    }
}

// ============================================
// 4. Mapper: Domain ↔ Entity 변환
// ============================================
@Component
public class OrderEntityMapper {

    public OrderJpaEntity toEntity(OrderDomain domain) {
        return new OrderJpaEntity(
            domain.getOrderId().getValue(),
            domain.getOrderNumber(),
            domain.getStatus(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            domain.getDeletedAt()  // Domain의 deletedAt을 그대로 전달
        );
    }

    public OrderDomain toDomain(OrderJpaEntity entity) {
        return new OrderDomain(
            new OrderId(entity.getId()),
            entity.getOrderNumber(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt()  // Entity의 deletedAt을 그대로 전달
        );
    }
}

// ============================================
// 5. Entity: 단순 데이터 컨테이너
// ============================================
@Entity
@Table(name = "order")
public class OrderJpaEntity extends SoftDeletableEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    protected OrderJpaEntity() {
        super();
    }

    public OrderJpaEntity(
        Long id,
        String orderNumber,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        // deletedAt은 부모 클래스에서 생성자로 설정 불가능하므로
        // Mapper에서 별도 처리 필요 (아래 참고)
    }

    // Getter만 제공, Setter 없음
    // ❌ updateStatus(), delete(), restore() 같은 메서드 없음!
}
```

---

## 3. QueryDSL 조회 시 주의사항

### 소프트 딜리트 엔티티 조회

**문제:**
```java
// ❌ 잘못된 예시: 삭제된 데이터도 조회됨
return queryFactory
    .selectFrom(order)
    .where(order.orderNumber.eq(orderNumber))
    .fetchOne();
```

**해결:**
```java
// ✅ 올바른 예시: 삭제되지 않은 데이터만 조회
return queryFactory
    .selectFrom(order)
    .where(
        order.orderNumber.eq(orderNumber),
        order.deletedAt.isNull()  // 필수 조건
    )
    .fetchOne();
```

### 공통 QueryDSL 유틸 메서드

```java
public class QueryDslUtils {

    /**
     * 소프트 딜리트 엔티티 활성 조건
     *
     * @param deletedAt QEntity의 deletedAt 필드
     * @return 활성 상태 조건 (deletedAt IS NULL)
     */
    public static BooleanExpression isActive(DateTimePath<LocalDateTime> deletedAt) {
        return deletedAt.isNull();
    }
}

// 사용 예시
return queryFactory
    .selectFrom(order)
    .where(
        order.orderNumber.eq(orderNumber),
        QueryDslUtils.isActive(order.deletedAt)  // 재사용 가능
    )
    .fetchOne();
```

---

## 4. final 제거 전략

### JPA 프록시 제약사항

**문제:**
```java
// ❌ JPA 프록시 생성 불가능
@Entity
public class ExampleJpaEntity {
    private final String message;  // final 필드는 프록시 생성 불가
    private final ExampleStatus status;
}
```

**해결:**
```java
// ✅ final 제거, setter 미제공으로 불변성 보장
@Entity
public class ExampleJpaEntity extends BaseAuditEntity {
    private String message;  // final 제거
    private ExampleStatus status;

    // setter 없음, 명시적 메서드만 제공
    public void updateMessage(String newMessage) {
        this.message = newMessage;
        markAsUpdated();
    }

    public void updateStatus(ExampleStatus newStatus) {
        this.status = newStatus;
        markAsUpdated();
    }
}
```

### 불변성 보장 전략

| 전략 | 설명 | 적용 방법 |
|------|------|----------|
| **Setter 미제공** | public setter를 제공하지 않음 | getter만 제공, 변경 메서드는 명시적 이름 사용 |
| **명시적 메서드** | 도메인 의미를 가진 메서드만 제공 | `updateStatus()`, `changeMessage()` 등 |
| **Domain Layer 검증** | 비즈니스 규칙 검증 후 호출 | Application Layer에서 Domain 검증 → Entity 변경 |

---

## 5. 마이그레이션 가이드

### 기존 엔티티 → BaseAuditEntity 전환

**Before:**
```java
@Entity
@Table(name = "example")
public class ExampleJpaEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private final LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

**After:**
```java
@Entity
@Table(name = "example")
public class ExampleJpaEntity extends BaseAuditEntity {

    // createdAt, updatedAt, markAsUpdated() 모두 상속으로 제거

    protected ExampleJpaEntity() {
        super();
    }

    public ExampleJpaEntity(
        Long id,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);  // 부모 생성자 호출
        this.id = id;
        this.message = message;
    }
}
```

### 소프트 딜리트 추가

**Before:**
```java
@Entity
public class OrderJpaEntity extends BaseAuditEntity {
    // 소프트 딜리트 없음
}
```

**After:**
```java
@Entity
public class OrderJpaEntity extends SoftDeletableEntity {
    // deletedAt, markAsDeleted(), restore() 자동 상속
}
```

---

## 6. 테스트 전략

### BaseAuditEntity 테스트

```java
@Test
void markAsUpdated_should_update_updatedAt() {
    // Given
    LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
    ExampleJpaEntity entity = new ExampleJpaEntity(
        1L, "message", createdAt, updatedAt
    );

    // When
    entity.markAsUpdated();

    // Then
    assertThat(entity.getUpdatedAt()).isAfter(updatedAt);
    assertThat(entity.getCreatedAt()).isEqualTo(createdAt);  // 생성 일시는 불변
}
```

### SoftDeletableEntity 테스트

**Entity 테스트 (단순 상태 조회만):**
```java
@Test
void isDeleted_should_return_true_when_deletedAt_is_not_null() {
    // Given
    LocalDateTime deletedAt = LocalDateTime.now();
    OrderJpaEntity order = new OrderJpaEntity(
        1L, "ORDER-001", OrderStatus.DELETED,
        LocalDateTime.now(), LocalDateTime.now(), deletedAt
    );

    // When & Then
    assertThat(order.isDeleted()).isTrue();
    assertThat(order.isActive()).isFalse();
}

@Test
void isDeleted_should_return_false_when_deletedAt_is_null() {
    // Given
    OrderJpaEntity order = new OrderJpaEntity(
        1L, "ORDER-001", OrderStatus.ACTIVE,
        LocalDateTime.now(), LocalDateTime.now(), null
    );

    // When & Then
    assertThat(order.isDeleted()).isFalse();
    assertThat(order.isActive()).isTrue();
}
```

**Domain 테스트 (비즈니스 로직):**
```java
@Test
void delete_should_set_deletedAt_and_change_status() {
    // Given
    OrderDomain order = OrderDomain.create("ORDER-001");

    // When
    order.delete();

    // Then
    assertThat(order.isDeleted()).isTrue();
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELETED);
    assertThat(order.getDeletedAt()).isNotNull();
}

@Test
void delete_should_throw_exception_when_order_is_completed() {
    // Given
    OrderDomain order = OrderDomain.create("ORDER-001");
    order.complete();

    // When & Then
    assertThatThrownBy(() -> order.delete())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("완료된 주문은 삭제할 수 없습니다.");
}

@Test
void delete_should_throw_exception_when_already_deleted() {
    // Given
    OrderDomain order = OrderDomain.create("ORDER-001");
    order.delete();

    // When & Then
    assertThatThrownBy(() -> order.delete())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("이미 삭제된 주문입니다.");
}
```

---

## 7. 성능 최적화

### 인덱스 전략

**Partial Index (MySQL 8.0+):**
```sql
-- 활성 상태 데이터만 인덱스 생성 (deleted_at IS NULL)
CREATE INDEX idx_order_active ON `order` (order_number) WHERE deleted_at IS NULL;

-- 삭제된 데이터만 인덱스 생성 (deleted_at IS NOT NULL)
CREATE INDEX idx_order_deleted ON `order` (deleted_at) WHERE deleted_at IS NOT NULL;
```

**Composite Index:**
```sql
-- 주문 번호 + 삭제 일시 복합 인덱스
CREATE INDEX idx_order_number_deleted ON `order` (order_number, deleted_at);
```

### 쿼리 최적화

```java
// ❌ 비효율적: 전체 데이터 조회 후 필터링
List<OrderJpaEntity> orders = orderRepository.findAll()
    .stream()
    .filter(OrderJpaEntity::isActive)
    .toList();

// ✅ 효율적: DB 레벨에서 필터링
List<OrderJpaEntity> orders = queryFactory
    .selectFrom(order)
    .where(order.deletedAt.isNull())
    .fetch();
```

---

## 8. 아키텍처 규칙 (ArchUnit)

```java
@ArchTest
static final ArchRule entities_should_extend_audit_entity =
    classes()
        .that().areAnnotatedWith(Entity.class)
        .and().resideInAPackage("..persistence..entity..")
        .should().beAssignableTo(BaseAuditEntity.class)
        .as("모든 JPA 엔티티는 BaseAuditEntity를 상속해야 합니다.");

@ArchTest
static final ArchRule entities_should_not_have_final_fields =
    fields()
        .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
        .and().areNotStatic()
        .should().notBeFinal()
        .as("JPA 엔티티는 프록시 생성을 위해 final 필드를 사용할 수 없습니다.");
```

---

## 9. 체크리스트

### BaseAuditEntity 적용 시
- [ ] 모든 JPA 엔티티는 `BaseAuditEntity` 상속
- [ ] `createdAt`, `updatedAt` 필드 제거 (중복)
- [ ] 생성자에서 `super(createdAt, updatedAt)` 호출
- [ ] `markAsUpdated()` 호출로 수정 일시 갱신

### SoftDeletableEntity 적용 시
- [ ] 삭제 이력 추적이 필요한 엔티티만 사용
- [ ] QueryDSL 조회 시 `deletedAt.isNull()` 조건 필수
- [ ] `markAsDeleted()` 호출 전 Domain Layer 검증
- [ ] 인덱스 전략 수립 (Partial Index 권장)

### final 제거 전략
- [ ] JPA 엔티티 필드에서 `final` 제거
- [ ] Setter 미제공으로 불변성 보장
- [ ] 명시적 메서드만 제공 (`updateStatus()` 등)
- [ ] Domain Layer에서 비즈니스 규칙 검증

---

## 요약

### 패턴 개요

| 패턴 | 목적 | 상속 관계 | 적용 대상 |
|------|------|----------|----------|
| **BaseAuditEntity** | 공통 감사 필드 | `@MappedSuperclass` | 모든 JPA 엔티티 |
| **SoftDeletableEntity** | 소프트 딜리트 필드 제공 | `BaseAuditEntity` 상속 | 삭제 이력 추적 필요 엔티티 |
| **final 제거** | JPA 프록시 지원 | - | 모든 JPA 엔티티 필드 |

### 핵심 원칙

#### ✅ Entity는 데이터 컨테이너

```java
// ✅ 올바른 설계
@Entity
public class OrderJpaEntity extends SoftDeletableEntity {
    private Long id;
    private String orderNumber;

    // Getter만 제공, 비즈니스 메서드 없음
}
```

#### ❌ Entity에 비즈니스 로직 금지

```java
// ❌ 잘못된 설계
@Entity
public class OrderJpaEntity extends SoftDeletableEntity {

    public void markAsDeleted() {  // ❌ 비즈니스 로직!
        this.deletedAt = LocalDateTime.now();
    }

    public void updateStatus(OrderStatus status) {  // ❌ 비즈니스 로직!
        this.status = status;
    }
}
```

### 레이어 책임

| Layer | 책임 | 예시 |
|-------|------|------|
| **Domain** | 비즈니스 로직 | `orderDomain.delete()` |
| **Application** | 흐름 제어 | `loadOrderPort.load()` → `orderDomain.delete()` → `saveOrderPort.save()` |
| **Persistence** | 데이터 변환 및 저장 | `mapper.toEntity()` → `repository.save()` |

### 올바른 흐름

```
1. Domain Layer
   ↓ orderDomain.delete() - 비즈니스 검증 수행
2. Application Layer
   ↓ UseCase가 Domain 호출
3. Mapper
   ↓ Domain → Entity 변환 (단순 매핑)
4. Repository
   ↓ Entity 저장
5. Database
```

✅ **이 패턴은 프로젝트의 모든 JPA 엔티티에 일관되게 적용되어야 합니다.**

✅ **Entity는 절대 비즈니스 로직을 포함하지 않습니다. (Zero-Tolerance 규칙)**
