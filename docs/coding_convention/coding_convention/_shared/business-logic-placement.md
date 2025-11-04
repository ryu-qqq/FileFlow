# 비즈니스 로직 배치 원칙 (Layer별 책임)

> **핵심 원칙**: 데이터 변경은 **무조건 Domain Layer**에서만. Application은 흐름 연결, Persistence는 저장/조회만.

---

## 📋 Layer별 책임 분리

### 1️⃣ Domain Layer: 비즈니스 로직 & 데이터 변경

**책임**:
- ✅ **비즈니스 규칙 구현**: 주문 취소 조건, 재고 차감 규칙 등
- ✅ **데이터 변경(상태 전이)**: `Order.cancel()`, `Stock.decrease()` 등
- ✅ **불변식 보장**: 생성자/메서드에서 검증
- ✅ **도메인 이벤트 발행**: `OrderCancelledEvent` 등

**금지**:
- ❌ 외부 시스템 호출 (API, DB, 메시징)
- ❌ 프레임워크 의존 (Spring, JPA)
- ❌ DTO/Entity 변환 로직

**예시**:
```java
// ✅ Good: Domain에서 비즈니스 로직과 데이터 변경
public class Order {
    private final OrderId id;
    private OrderStatus status;
    private final List<OrderLineItem> lineItems;

    // 비즈니스 로직: 주문 취소
    public void cancel(String reason) {
        if (this.status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("배송된 주문은 취소할 수 없습니다");
        }
        this.status = OrderStatus.CANCELLED;  // 데이터 변경
        registerEvent(new OrderCancelledEvent(this.id, reason));
    }

    // 비즈니스 로직: 배송 가능 여부
    public boolean isShippable() {
        return this.status == OrderStatus.PAID
            && this.lineItems.stream().allMatch(OrderLineItem::isInStock);
    }
}
```

---

### 2️⃣ Application Layer: 오케스트레이션 (흐름 연결)

**책임**:
- ✅ **UseCase 구현**: 여러 Domain 객체 호출 순서 제어
- ✅ **트랜잭션 경계**: `@Transactional` 설정
- ✅ **Port 호출**: Repository, External API 호출
- ✅ **DTO ↔ Domain 변환**: Command → Domain, Domain → Response

**금지**:
- ❌ **비즈니스 로직 작성**: Domain 메서드로 위임
- ❌ **데이터 직접 변경**: `order.setStatus()` 같은 setter 호출
- ❌ **조건 분기 과다**: 복잡한 if-else는 Domain으로

**예시**:
```java
// ✅ Good: Application은 흐름만 연결
@Service
@Transactional
public class CancelOrderUseCase {
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final SendEmailPort sendEmailPort;

    public void execute(CancelOrderCommand command) {
        // 1. Domain 로드
        Order order = loadOrderPort.loadById(command.orderId());

        // 2. Domain 메서드 호출 (비즈니스 로직은 Domain에)
        order.cancel(command.reason());  // ← Domain이 상태 변경

        // 3. 저장 (Persistence는 변경 감지만)
        saveOrderPort.save(order);

        // 4. 외부 시스템 호출
        sendEmailPort.send(order.getCustomerEmail(), "주문이 취소되었습니다");
    }
}
```

```java
// ❌ Bad: Application에서 비즈니스 로직 작성
@Service
@Transactional
public class CancelOrderUseCase {
    public void execute(CancelOrderCommand command) {
        Order order = loadOrderPort.loadById(command.orderId());

        // ❌ Application에서 상태 검증 (Domain으로 가야 함)
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("배송된 주문은 취소할 수 없습니다");
        }

        // ❌ Application에서 데이터 직접 변경 (Domain 메서드로 가야 함)
        order.setStatus(OrderStatus.CANCELLED);  // setter 사용 금지!

        saveOrderPort.save(order);
    }
}
```

---

### 3️⃣ Persistence Layer: 저장/조회만 (변경 금지)

**책임**:
- ✅ **저장/조회**: JPA Entity ↔ Domain Entity 변환
- ✅ **쿼리 최적화**: N+1 방지, Fetch Join
- ✅ **변경 감지**: JPA Dirty Checking으로 UPDATE 자동 처리

**금지**:
- ❌ **데이터 직접 변경**: `entity.setStatus()` 같은 setter 호출
- ❌ **비즈니스 로직**: `if (entity.getAmount() > 1000) ...` 같은 조건 분기
- ❌ **상태 전이**: Domain 메서드 없이 필드 직접 수정

**예시**:
```java
// ✅ Good: Persistence는 저장/조회만
@Repository
public class OrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    @Override
    public Order loadById(OrderId id) {
        OrderJpaEntity entity = orderRepository.findById(id.value())
            .orElseThrow(() -> new OrderNotFoundException(id));
        return orderMapper.toDomain(entity);  // JPA → Domain 변환
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = orderMapper.toEntity(order);  // Domain → JPA 변환
        orderRepository.save(entity);  // 변경 감지 → UPDATE 자동
    }
}
```

```java
// ❌ Bad: Persistence에서 데이터 변경
@Repository
public class OrderPersistenceAdapter {

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = orderMapper.toEntity(order);

        // ❌ Persistence에서 비즈니스 로직 (Domain으로 가야 함)
        if (entity.getStatus() == OrderStatus.PAID) {
            entity.setStatus(OrderStatus.READY_TO_SHIP);  // 변경 금지!
        }

        orderRepository.save(entity);
    }
}
```

---

## 🚨 불변성 강제 (Record, final, setter 없앰)

### Domain Entity: final + 메서드만

```java
// ✅ Good: final + 비즈니스 메서드
public class Order {
    private final OrderId id;
    private OrderStatus status;  // 상태만 변경 가능
    private final List<OrderLineItem> lineItems;

    // ❌ setter 없음
    // public void setStatus(OrderStatus status) { ... }  // 금지!

    // ✅ 비즈니스 메서드만
    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }
}
```

### Value Object: record 사용

```java
// ✅ Good: record = 완전 불변
public record OrderId(Long value) {
    public static OrderId of(Long value) {
        if (value == null || value <= 0) {
            throw new InvalidValueException("OrderId는 양수여야 합니다");
        }
        return new OrderId(value);
    }
}

public record Money(BigDecimal amount, Currency currency) {
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvalidOperationException("통화가 다릅니다");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### JPA Entity: setter 없음 + protected 생성자

```java
// ✅ Good: setter 없음
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // JPA 필수 (외부에서 호출 불가)
    protected OrderJpaEntity() {}

    // ❌ setter 없음
    // public void setStatus(OrderStatus status) { ... }  // 금지!

    // ✅ 정적 팩토리 메서드
    public static OrderJpaEntity of(Long id, OrderStatus status) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = id;
        entity.status = status;
        return entity;
    }

    // ✅ getter만
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
```

---

## 📊 Layer별 책임 비교표

| Layer | 비즈니스 로직 | 데이터 변경 | 외부 호출 | DTO 변환 | 트랜잭션 |
|-------|-------------|-----------|----------|----------|---------|
| **Domain** | ✅ 주인공 | ✅ 허용 | ❌ 금지 | ❌ 금지 | ❌ 금지 |
| **Application** | ❌ 위임 | ❌ 금지 | ✅ 허용 | ✅ 허용 | ✅ 경계 설정 |
| **Persistence** | ❌ 금지 | ❌ 금지 | ❌ 금지 | ✅ 허용 | ❌ 금지 |

---

## ✅ 체크리스트

### Domain Layer
- [ ] 비즈니스 로직이 Domain Entity 메서드에 있는가?
- [ ] 데이터 변경이 Domain 메서드를 통해서만 이루어지는가?
- [ ] setter가 없는가?
- [ ] Value Object는 record로 불변인가?

### Application Layer
- [ ] UseCase가 Domain 메서드만 호출하는가?
- [ ] if-else로 비즈니스 규칙을 판단하지 않는가?
- [ ] 데이터를 직접 변경(setter 호출)하지 않는가?

### Persistence Layer
- [ ] 저장/조회만 하는가?
- [ ] 비즈니스 로직이 없는가?
- [ ] JPA Entity에 setter가 없는가?
- [ ] Domain → JPA, JPA → Domain 변환만 하는가?

---

## 🎯 핵심 원칙 요약

1. **비즈니스 로직 = Domain Layer**: `order.cancel()`, `stock.decrease()`
2. **데이터 변경 = Domain Layer**: setter 없음, 메서드로만 변경
3. **Application = 오케스트레이션**: Domain 메서드 호출 + 흐름 제어
4. **Persistence = 저장소**: 변경 금지, 변환만
5. **불변성 강제**: Record(VO), final(Entity), setter 없앰

**이를 지키지 않으면**:
- ❌ Anemic Domain Model (빈약한 도메인)
- ❌ 비즈니스 로직 중복 (Application, Persistence 곳곳에 흩어짐)
- ❌ 테스트 어려움 (Domain 단독 테스트 불가)
- ❌ 유지보수 악화 (규칙 변경 시 여러 곳 수정)
