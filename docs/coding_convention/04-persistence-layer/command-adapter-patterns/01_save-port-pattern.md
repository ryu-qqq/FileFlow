# Save Port Pattern (저장 포트 패턴)

**목적**: CQRS Command 패턴에서 Domain Model 저장을 위한 Port 인터페이스 정의

**위치**: `application/[module]/port/out/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### CQRS Command 패턴

Save Port는 **Command(쓰기) 전용** Port입니다:

```
Application Layer (UseCase)
    ↓ Command
SaveOrderPort (Interface)
    ↓ 구현
OrderCommandAdapter
    ↓ 호출
JpaRepository.save()
    ↓ 저장
OrderJpaEntity
```

**규칙**:
- ✅ Domain Model 입력/출력
- ✅ Command(저장/수정)만 담당
- ❌ Query(조회) 메서드 금지 (별도 Query Port 사용)

---

## 📦 Save Port 인터페이스

### 기본 패턴

```java
/**
 * Order 저장 Port (Command)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SaveOrderPort {

    /**
     * Order를 저장합니다.
     *
     * @param order 저장할 Order (Domain Model)
     * @return 저장된 Order (ID 할당됨)
     */
    Order save(Order order);
}
```

**핵심**:
- **입력**: `Order` (Domain Model)
- **출력**: `Order` (ID가 할당된 Domain Model)
- **책임**: 저장만 담당, 조회는 Query Port로 분리

---

## 🔄 UseCase에서 사용

### 신규 Order 생성 UseCase

```java
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final SaveOrderPort saveOrderPort;

    @Override
    public OrderResponse execute(CreateOrderCommand command) {
        // 1. Domain Model 생성 (비즈니스 로직)
        Order order = Order.create(
            UserId.of(command.userId()),
            OrderItems.of(command.items())
        );

        // 2. Port를 통해 저장 (Command)
        Order savedOrder = saveOrderPort.save(order);

        // 3. Response 변환
        return OrderResponse.from(savedOrder);
    }
}
```

### Order 상태 변경 UseCase

```java
@Service
@Transactional
public class ConfirmOrderService implements ConfirmOrderUseCase {

    private final LoadOrderForUpdatePort loadOrderPort;  // Domain 조회
    private final SaveOrderPort saveOrderPort;           // Domain 저장

    @Override
    public OrderResponse execute(ConfirmOrderCommand command) {
        // 1. Domain 조회 (Load Command Port)
        Order order = loadOrderPort.loadById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        // 2. Domain 비즈니스 로직 실행
        order.confirm();

        // 3. Domain 저장 (Save Port)
        Order savedOrder = saveOrderPort.save(order);

        return OrderResponse.from(savedOrder);
    }
}
```

**💡 포인트**:
- `SaveOrderPort`: Command(저장)만 담당
- `LoadOrderForUpdatePort`: Domain 로직 필요 시 사용 (별도 Port)
- `LoadOrderPort`: DTO 조회 전용 (Query Port, 다음 섹션 참고)

---

## 📋 Save Port vs Load Port 비교

| 구분 | Save Port | Load Port (Query) | Load Port (Command) |
|-----|-----------|------------------|-------------------|
| **목적** | Domain 저장 | DTO 조회 | Domain 조회 (수정 목적) |
| **입력** | `Order` (Domain) | `OrderId` | `OrderId` |
| **출력** | `Order` (Domain) | `OrderDetailResponse` (DTO) | `Order` (Domain) |
| **사용처** | Command UseCase | Query UseCase | Command UseCase (상태 변경) |
| **예시** | `CreateOrderService` | `GetOrderDetailService` | `ConfirmOrderService` |

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Query 메서드 포함 (CQRS 위반)
public interface SaveOrderPort {
    Order save(Order order);
    Optional<Order> findById(OrderId id);  // Query는 별도 Port로!
}

// ❌ DTO 반환 (Command는 Domain만)
public interface SaveOrderPort {
    OrderResponse save(Order order);  // DTO 반환 금지!
}

// ❌ void 반환 (저장된 Domain 필요)
public interface SaveOrderPort {
    void save(Order order);  // ID 할당 확인 불가!
}

// ❌ Entity 직접 사용
public interface SaveOrderPort {
    OrderJpaEntity save(OrderJpaEntity entity);  // Entity 노출 금지!
}
```

### ✅ Good Examples

```java
// ✅ Command만 담당 (저장)
public interface SaveOrderPort {
    Order save(Order order);
}

// ✅ Domain Model 입력/출력
Order order = Order.create(userId, items);
Order savedOrder = saveOrderPort.save(order);
assertThat(savedOrder.getId()).isNotNull();

// ✅ Query는 별도 Port 사용
public interface LoadOrderPort {
    Optional<OrderDetailResponse> loadById(OrderId id);  // DTO 반환
}

// ✅ Load Command는 또 다른 Port
public interface LoadOrderForUpdatePort {
    Optional<Order> loadById(OrderId id);  // Domain 반환
}
```

---

## 📐 Port 인터페이스 설계 규칙

### 1. 단일 책임 원칙 (SRP)

```java
// ✅ Good - 저장만 담당
public interface SaveOrderPort {
    Order save(Order order);
}

// ✅ Good - 삭제만 담당
public interface DeleteOrderPort {
    void delete(OrderId id);
}

// ❌ Bad - 여러 책임 혼재
public interface OrderPort {
    Order save(Order order);
    void delete(OrderId id);
    Optional<Order> findById(OrderId id);  // Query 혼재!
}
```

### 2. Domain Model 중심

```java
// ✅ Good - Domain Model 사용
public interface SaveOrderPort {
    Order save(Order order);  // Domain Model
}

// ❌ Bad - DTO 사용
public interface SaveOrderPort {
    OrderResponse save(CreateOrderCommand command);  // DTO 금지!
}
```

### 3. ID 할당 확인 가능

```java
// ✅ Good - 저장된 Domain 반환
Order savedOrder = saveOrderPort.save(order);
assertThat(savedOrder.getId()).isNotNull();

// ❌ Bad - void 반환 (ID 확인 불가)
saveOrderPort.save(order);  // ID가 할당되었는지 알 수 없음!
```

---

## 📖 관련 문서

- **[Command Adapter Implementation](./03_command-adapter-implementation.md)** - Save Port 구현 패턴
- **[Delete Port Pattern](./02_delete-port-pattern.md)** - 삭제 Port 패턴
- **[Load Port Pattern](../query-adapter-patterns/01_load-port-pattern.md)** - Query Port 패턴
- **[Command Mapper Patterns](./04_command-mapper-patterns.md)** - Domain ↔ Entity 변환

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
