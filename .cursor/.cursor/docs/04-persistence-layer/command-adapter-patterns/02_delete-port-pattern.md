# Delete Port Pattern (삭제 포트 패턴)

**목적**: CQRS Command 패턴에서 Soft Delete 및 Hard Delete를 위한 Port 인터페이스 정의

**위치**: `application/[module]/port/out/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### Soft Delete 우선 전략

모든 삭제는 **Soft Delete(논리 삭제)**를 기본으로 합니다:

```
Application Layer (UseCase)
    ↓ Command
DeleteOrderPort (Interface)
    ↓ 구현
OrderCommandAdapter
    ↓ 호출
JpaRepository.save() (deletedAt 업데이트)
    ↓ 저장
OrderJpaEntity (deletedAt ≠ null)
```

**규칙**:
- ✅ Soft Delete 기본 (deletedAt 타임스탬프)
- ✅ Hard Delete는 명시적으로 별도 메서드
- ❌ 물리 삭제(`DELETE FROM`) 지양

---

## 📦 Delete Port 인터페이스

### Soft Delete 패턴 (기본)

```java
/**
 * Order 삭제 Port (Command - Soft Delete)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface DeleteOrderPort {

    /**
     * Order를 소프트 딜리트합니다. (deletedAt 업데이트)
     *
     * @param orderId 삭제할 Order ID
     */
    void softDelete(OrderId orderId);

    /**
     * Order를 복원합니다. (deletedAt null로 설정)
     *
     * @param orderId 복원할 Order ID
     */
    void restore(OrderId orderId);
}
```

### Hard Delete 패턴 (특수 케이스)

```java
/**
 * Order Hard Delete Port (물리 삭제)
 *
 * ⚠️ 주의: 데이터 복구 불가능, 신중하게 사용
 *
 * @author development-team
 * @since 1.0.0
 */
public interface HardDeleteOrderPort {

    /**
     * Order를 물리적으로 삭제합니다.
     *
     * ⚠️ 경고: 이 작업은 되돌릴 수 없습니다!
     *
     * @param orderId 삭제할 Order ID
     */
    void hardDelete(OrderId orderId);
}
```

---

## 🔄 UseCase에서 사용

### Soft Delete UseCase (기본)

```java
@Service
@Transactional
public class CancelOrderService implements CancelOrderUseCase {

    private final LoadOrderForUpdatePort loadOrderPort;
    private final DeleteOrderPort deleteOrderPort;

    @Override
    public void execute(CancelOrderCommand command) {
        // 1. Domain 조회
        Order order = loadOrderPort.loadById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        // 2. 비즈니스 검증
        order.validateCancellable();

        // 3. Soft Delete
        deleteOrderPort.softDelete(order.getId());
    }
}
```

### Soft Delete with Domain Logic

```java
@Service
@Transactional
public class DeleteOrderService implements DeleteOrderUseCase {

    private final LoadOrderForUpdatePort loadOrderPort;
    private final SaveOrderPort saveOrderPort;  // Soft Delete도 save()로 처리 가능

    @Override
    public void execute(DeleteOrderCommand command) {
        // 1. Domain 조회
        Order order = loadOrderPort.loadById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        // 2. Domain 비즈니스 로직 실행
        order.markAsDeleted();  // Domain에서 deletedAt 설정

        // 3. 저장 (Soft Delete)
        saveOrderPort.save(order);
    }
}
```

**💡 선택 기준**:
- **DeleteOrderPort**: 단순 Soft Delete (비즈니스 로직 없음)
- **SaveOrderPort + Domain.markAsDeleted()**: 비즈니스 로직 필요 시

### Restore UseCase

```java
@Service
@Transactional
public class RestoreOrderService implements RestoreOrderUseCase {

    private final DeleteOrderPort deleteOrderPort;

    @Override
    public void execute(RestoreOrderCommand command) {
        // Soft Delete 취소
        deleteOrderPort.restore(command.orderId());
    }
}
```

### Hard Delete UseCase (특수 케이스)

```java
@Service
@Transactional
public class PurgeOrderService implements PurgeOrderUseCase {

    private final HardDeleteOrderPort hardDeleteOrderPort;

    @Override
    public void execute(PurgeOrderCommand command) {
        // ⚠️ 물리 삭제 (복구 불가)
        hardDeleteOrderPort.hardDelete(command.orderId());
    }
}
```

---

## 🗂️ Soft Delete vs Hard Delete 비교

| 구분 | Soft Delete | Hard Delete |
|-----|------------|-------------|
| **방식** | `deletedAt` 타임스탬프 | `DELETE FROM` SQL |
| **복구** | ✅ 가능 (`restore()`) | ❌ 불가능 |
| **데이터 보존** | ✅ 유지 | ❌ 영구 삭제 |
| **감사 추적** | ✅ 가능 | ❌ 불가능 |
| **사용 케이스** | 주문 취소, 계정 비활성화 | 개인정보 삭제, 테스트 데이터 |
| **Port 인터페이스** | `DeleteOrderPort` | `HardDeleteOrderPort` |

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Hard Delete가 기본 (Soft Delete 우선 원칙 위반)
public interface DeleteOrderPort {
    void delete(OrderId id);  // 물리 삭제인지 논리 삭제인지 모호!
}

// ❌ Query 메서드 포함 (CQRS 위반)
public interface DeleteOrderPort {
    void softDelete(OrderId id);
    Optional<Order> findDeletedOrders();  // Query는 별도 Port로!
}

// ❌ Entity 직접 사용
public interface DeleteOrderPort {
    void softDelete(OrderJpaEntity entity);  // Entity 노출 금지!
}

// ❌ Hard Delete 경고 없음
public interface DeleteOrderPort {
    void hardDelete(OrderId id);  // 위험 표시 필요!
}
```

### ✅ Good Examples

```java
// ✅ Soft Delete 명시
public interface DeleteOrderPort {
    void softDelete(OrderId id);
    void restore(OrderId id);
}

// ✅ Hard Delete 별도 Port + 경고
/**
 * ⚠️ 주의: 물리 삭제, 복구 불가능
 */
public interface HardDeleteOrderPort {
    void hardDelete(OrderId id);
}

// ✅ Domain ID 사용
deleteOrderPort.softDelete(OrderId.of(orderId));

// ✅ 비즈니스 로직 + Soft Delete
order.markAsDeleted();
saveOrderPort.save(order);
```

---

## 📐 Soft Delete 구현 패턴

### Domain Model (SoftDeletableEntity 상속)

```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... 다른 필드

    // SoftDeletableEntity에서 상속:
    // - LocalDateTime deletedAt
    // - boolean isDeleted()
    // - void markAsDeleted()
    // - void restore()
}
```

### Command Adapter 구현

```java
@Component
public class OrderCommandAdapter implements DeleteOrderPort {

    private final OrderJpaRepository jpaRepository;

    @Override
    public void softDelete(OrderId orderId) {
        OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Soft Delete (deletedAt 설정)
        entity.markAsDeleted();
        jpaRepository.save(entity);
    }

    @Override
    public void restore(OrderId orderId) {
        OrderJpaEntity entity = jpaRepository.findById(orderId.getValue())
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Restore (deletedAt null)
        entity.restore();
        jpaRepository.save(entity);
    }
}
```

---

## 🔍 Query에서 Soft Delete 처리

### Query Adapter에서 삭제된 데이터 제외

```java
@Component
public class OrderQueryAdapter implements LoadOrderPort {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<OrderDetailResponse> loadById(OrderId id) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(...))
                .from(order)
                .where(
                    order.id.eq(id.getValue()),
                    order.deletedAt.isNull()  // ✅ Soft Delete 제외
                )
                .fetchOne()
        );
    }
}
```

---

## 📋 체크리스트

Delete Port 작성 시:
- [ ] Soft Delete 기본 사용 (`softDelete()`, `restore()`)
- [ ] Hard Delete는 별도 Port (`HardDeleteOrderPort`)
- [ ] Hard Delete에 경고 주석 포함
- [ ] Domain ID 사용 (`OrderId`)
- [ ] Query 메서드 없음 (CQRS 준수)
- [ ] Query Adapter에서 `deletedAt IS NULL` 조건

---

## 📖 관련 문서

- **[Save Port Pattern](./01_save-port-pattern.md)** - 저장 Port 패턴
- **[Command Adapter Implementation](./03_command-adapter-implementation.md)** - Delete Port 구현
- **[Audit Entity Pattern](../jpa-entity-design/03_audit-entity-pattern.md)** - SoftDeletableEntity
- **[Query Adapter Implementation](../query-adapter-patterns/03_query-adapter-implementation.md)** - deletedAt 필터링

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
