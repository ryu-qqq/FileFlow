# Component 패턴 (Manager)

**목적**: 여러 UseCase가 공통으로 사용하는 트랜잭션 로직 캡슐화

**위치**: `application/[context]/component/`

**관련 문서**:
- [Application Package Guide](../package-guide/01_application_package_guide.md)
- [Facade Pattern](../facade/01_facade-usage-guide.md)
- [Transaction Management](../transaction-management/01_transaction-boundaries.md)

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 Component란?

**Component**는 여러 UseCase(Service)에서 공통으로 사용하는 **트랜잭션 로직**을 캡슐화한 객체입니다.

### 핵심 특징
- ✅ **횡단 관심사**: 여러 UseCase가 공통으로 사용하는 로직
- ✅ **트랜잭션 경계**: `@Transactional` 적용하여 트랜잭션 관리
- ✅ **Bounded Context 관리**: 특정 도메인(Order, Product 등)의 상태 변경 관리
- ✅ **재사용성**: 중복 코드 제거, 일관성 유지

### 네이밍 규칙
- **패턴**: `{Context}Manager`
- **예시**: `OrderManager`, `ProductManager`, `OutboxManager`

---

## 📁 패키지 구조

```
application/
└─ [context]/
   ├─ component/           # ⭐ Component 위치
   │  ├─ OrderManager.java
   │  ├─ ProductManager.java
   │  └─ OutboxManager.java
   ├─ service/
   │  ├─ command/
   │  │  ├─ CreateOrderService.java    # OrderManager 사용
   │  │  ├─ UpdateOrderService.java    # OrderManager 사용
   │  │  └─ CancelOrderService.java    # OrderManager 사용
   │  └─ query/
   └─ port/
      ├─ in/
      └─ out/
         ├─ SaveOrderPort.java
         └─ LoadOrderPort.java
```

---

## 🔄 Component vs Facade vs Service

| 구분 | Component | Facade | Service |
|------|-----------|--------|---------|
| **역할** | 공통 트랜잭션 로직 | UseCase 조율 | UseCase 구현 |
| **위치** | `component/` | `facade/` | `service/` |
| **사용처** | 여러 Service에서 공통 사용 | Controller에서 호출 | Facade 또는 Controller에서 호출 |
| **트랜잭션** | `@Transactional` 필수 | 선택적 | `@Transactional` 필수 |
| **네이밍** | `{Context}Manager` | `{Context}Facade` | `{Feature}{Context}Service` |
| **예시** | `OrderManager` | `OrderFacade` | `CreateOrderService` |

### 의존성 관계

```
Controller
    ↓
Facade (선택적)
    ↓
Service (UseCase 구현)
    ↓
Component (공통 트랜잭션 로직) ← 여러 Service에서 공통 사용
    ↓
Port (Outbound)
    ↓
Adapter
```

---

## 📌 사용 기준

### ✅ Component가 필요한 경우

1. **여러 UseCase가 공통 로직 사용**
   - Create, Update, Cancel Service 모두 Order 상태 변경 로직 필요
   - 중복 코드 제거

2. **트랜잭션 경계가 필요한 공통 로직**
   - Outbox 패턴: 이벤트 저장 → 상태 변경
   - Saga 패턴: 보상 트랜잭션 관리

3. **Bounded Context별 상태 변경 관리**
   - Order Context: OrderManager
   - Product Context: ProductManager
   - Payment Context: PaymentManager

### ❌ Component가 불필요한 경우

1. **단일 UseCase만 사용하는 로직**
   - UseCase Service에 직접 구현

2. **트랜잭션이 필요 없는 로직**
   - Stateless Utility 클래스로 충분

3. **단순 조회 로직**
   - Query Service에서 직접 Port 호출

---

## ✅ 예시 1: OrderManager (Order 상태 관리)

### 문제: 여러 Service에서 Order 상태 변경 로직 중복

```java
// ❌ Before - 중복 코드
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final SaveOrderPort saveOrderPort;
    private final OutboxPort outboxPort;

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        Order order = Order.forNew(command);

        // ❌ 중복 1: Order 저장 + Outbox 저장
        Order savedOrder = saveOrderPort.save(order);
        outboxPort.save(new OutboxEvent("OrderCreated", savedOrder.getId()));

        return OrderResponse.from(savedOrder);
    }
}

@Service
@Transactional
public class UpdateOrderService implements UpdateOrderUseCase {
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OutboxPort outboxPort;

    @Override
    public OrderResponse executeOrderUpdate(UpdateOrderCommand command) {
        Order order = loadOrderPort.load(command.orderId()).orElseThrow();
        order.update(command);

        // ❌ 중복 2: Order 저장 + Outbox 저장 (같은 로직)
        Order savedOrder = saveOrderPort.save(order);
        outboxPort.save(new OutboxEvent("OrderUpdated", savedOrder.getId()));

        return OrderResponse.from(savedOrder);
    }
}
```

### 해결: OrderManager로 공통 로직 캡슐화

```java
// ✅ Component: OrderManager
package com.company.application.order.component;

import com.company.application.order.port.out.SaveOrderPort;
import com.company.application.order.port.out.OutboxPort;
import com.company.domain.order.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order 상태 변경 관리 Component
 * - 여러 Command Service에서 공통으로 사용
 * - 트랜잭션 경계 관리
 * - Outbox 패턴 적용
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class OrderManager {

    private final SaveOrderPort saveOrderPort;
    private final OutboxPort outboxPort;

    public OrderManager(
        SaveOrderPort saveOrderPort,
        OutboxPort outboxPort
    ) {
        this.saveOrderPort = saveOrderPort;
        this.outboxPort = outboxPort;
    }

    /**
     * ✅ Order 생성 + Outbox 저장 (트랜잭션)
     */
    public Order createOrder(Order order) {
        // 1. Order 저장
        Order savedOrder = saveOrderPort.save(order);

        // 2. Outbox 이벤트 저장 (같은 트랜잭션)
        outboxPort.save(new OutboxEvent("OrderCreated", savedOrder.getId()));

        return savedOrder;
    }

    /**
     * ✅ Order 수정 + Outbox 저장 (트랜잭션)
     */
    public Order updateOrder(Order order) {
        // 1. Order 저장
        Order savedOrder = saveOrderPort.save(order);

        // 2. Outbox 이벤트 저장 (같은 트랜잭션)
        outboxPort.save(new OutboxEvent("OrderUpdated", savedOrder.getId()));

        return savedOrder;
    }

    /**
     * ✅ Order 취소 + Outbox 저장 (트랜잭션)
     */
    public Order cancelOrder(Order order) {
        // 1. Order 저장
        Order savedOrder = saveOrderPort.save(order);

        // 2. Outbox 이벤트 저장 (같은 트랜잭션)
        outboxPort.save(new OutboxEvent("OrderCancelled", savedOrder.getId()));

        return savedOrder;
    }
}
```

### 사용: Service에서 OrderManager 의존

```java
// ✅ After - OrderManager 사용
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderManager orderManager;  // ✅ Component 의존

    public CreateOrderService(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // 1. Domain 생성
        Order order = Order.forNew(command);

        // 2. ✅ OrderManager로 위임 (트랜잭션 + Outbox)
        Order savedOrder = orderManager.createOrder(order);

        return OrderResponse.from(savedOrder);
    }
}

@Service
@Transactional
public class UpdateOrderService implements UpdateOrderUseCase {
    private final LoadOrderPort loadOrderPort;
    private final OrderManager orderManager;  // ✅ Component 의존

    @Override
    public OrderResponse executeOrderUpdate(UpdateOrderCommand command) {
        // 1. Domain 조회
        Order order = loadOrderPort.load(command.orderId()).orElseThrow();

        // 2. Domain 수정
        order.update(command);

        // 3. ✅ OrderManager로 위임 (트랜잭션 + Outbox)
        Order savedOrder = orderManager.updateOrder(order);

        return OrderResponse.from(savedOrder);
    }
}
```

**이점**:
- ✅ 중복 코드 제거: Order 저장 + Outbox 저장 로직 한 곳에
- ✅ 트랜잭션 일관성: OrderManager에서 트랜잭션 경계 관리
- ✅ 유지보수성 향상: Outbox 로직 변경 시 OrderManager만 수정

---

## ✅ 예시 2: OutboxManager (Outbox 패턴)

### 역할
- Outbox 이벤트 저장 (트랜잭션)
- Outbox 상태 변경 (Pending → Sent → Failed)

```java
package com.company.application.outbox.component;

import com.company.application.outbox.port.out.SaveOutboxPort;
import com.company.domain.outbox.OutboxEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 이벤트 관리 Component
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class OutboxManager {

    private final SaveOutboxPort saveOutboxPort;

    public OutboxManager(SaveOutboxPort saveOutboxPort) {
        this.saveOutboxPort = saveOutboxPort;
    }

    /**
     * ✅ Outbox 이벤트 저장 (Pending 상태)
     */
    public OutboxEvent createEvent(String eventType, String payload) {
        OutboxEvent event = OutboxEvent.forNew(eventType, payload);
        return saveOutboxPort.save(event);
    }

    /**
     * ✅ Outbox 상태 변경: Pending → Sent
     */
    public OutboxEvent markAsSent(OutboxEvent event) {
        event.markAsSent();
        return saveOutboxPort.save(event);
    }

    /**
     * ✅ Outbox 상태 변경: Pending → Failed
     */
    public OutboxEvent markAsFailed(OutboxEvent event, String errorMessage) {
        event.markAsFailed(errorMessage);
        return saveOutboxPort.save(event);
    }
}
```

### 사용 예시

```java
@Service
public class OutboxPublisher {
    private final OutboxManager outboxManager;
    private final MessageBrokerPort messageBrokerPort;

    @Scheduled(fixedDelay = 5000)  // 5초마다 실행
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = loadPendingEvents();

        for (OutboxEvent event : pendingEvents) {
            try {
                // 1. 메시지 발행
                messageBrokerPort.publish(event.getPayload());

                // 2. ✅ OutboxManager로 상태 변경 (트랜잭션)
                outboxManager.markAsSent(event);
            } catch (Exception e) {
                // 3. ✅ OutboxManager로 상태 변경 (트랜잭션)
                outboxManager.markAsFailed(event, e.getMessage());
            }
        }
    }
}
```

---

## ✅ 예시 3: ProductManager (Product 상태 관리)

```java
package com.company.application.product.component;

import com.company.application.product.port.out.SaveProductPort;
import com.company.domain.product.Product;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product 상태 변경 관리 Component
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class ProductManager {

    private final SaveProductPort saveProductPort;

    public ProductManager(SaveProductPort saveProductPort) {
        this.saveProductPort = saveProductPort;
    }

    /**
     * ✅ Product 재고 차감 (트랜잭션)
     */
    public Product decreaseStock(Product product, int quantity) {
        // 1. Domain 로직 실행
        product.decreaseStock(quantity);

        // 2. 저장
        return saveProductPort.save(product);
    }

    /**
     * ✅ Product 재고 증가 (트랜잭션)
     */
    public Product increaseStock(Product product, int quantity) {
        // 1. Domain 로직 실행
        product.increaseStock(quantity);

        // 2. 저장
        return saveProductPort.save(product);
    }
}
```

---

## 🚨 Do / Don't

### Do ✅

```java
// ✅ Good: 여러 Service에서 공통 사용하는 트랜잭션 로직
@Component
@Transactional
public class OrderManager {
    // Order 상태 변경 + Outbox 저장 (공통 로직)
    public Order createOrder(Order order) { ... }
    public Order updateOrder(Order order) { ... }
}

// ✅ Good: Service에서 OrderManager 사용
@Service
@Transactional
public class CreateOrderService {
    private final OrderManager orderManager;  // Component 의존
}
```

### Don't ❌

```java
// ❌ Bad: Service에서 직접 중복 코드 작성
@Service
@Transactional
public class CreateOrderService {
    private final SaveOrderPort saveOrderPort;
    private final OutboxPort outboxPort;

    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // ❌ 중복: 다른 Service에서도 같은 로직
        Order savedOrder = saveOrderPort.save(order);
        outboxPort.save(new OutboxEvent(...));
    }
}

// ❌ Bad: Component에 비즈니스 로직 작성
@Component
public class OrderManager {
    // ❌ 비즈니스 로직은 Domain Layer에
    public Order createOrder(CreateOrderCommand command) {
        // ❌ 비즈니스 규칙 판단 (Domain으로 가야 함)
        if (command.amount() > 10000) {
            throw new BusinessException("Too much");
        }
        return saveOrderPort.save(order);
    }
}

// ❌ Bad: Component에 UseCase 로직 작성
@Component
public class OrderManager {
    // ❌ UseCase 로직은 Service에
    public OrderResponse processOrder(CreateOrderCommand command) {
        // ❌ DTO 변환, 조회, 변환 등 UseCase 로직
        Order order = orderAssembler.toDomain(command);
        Order savedOrder = saveOrderPort.save(order);
        return orderAssembler.toResponse(savedOrder);
    }
}
```

---

## 📊 체크리스트

### Component 생성 전 확인사항

#### 1. 공통 사용 여부
- [ ] 2개 이상의 Service에서 사용하는가?
  - ✅ Yes → Component 필요
  - ❌ No → Service에 직접 구현

#### 2. 트랜잭션 필요 여부
- [ ] 트랜잭션 경계가 필요한가?
  - ✅ Yes → Component에 `@Transactional` 적용
  - ❌ No → Utility 클래스 충분

#### 3. Bounded Context 관리 여부
- [ ] 특정 도메인(Order, Product 등) 상태 변경인가?
  - ✅ Yes → `{Context}Manager` 네이밍
  - ❌ No → 일반 Component

#### 4. 비즈니스 로직 분리
- [ ] Component에 비즈니스 로직이 없는가?
  - ✅ Yes → 올바른 Component
  - ❌ No → Domain Layer로 이동

---

## 📖 관련 문서

- **[Application Package Guide](../package-guide/01_application_package_guide.md)** - 전체 패키지 구조
- **[Facade Pattern](../facade/01_facade-usage-guide.md)** - Facade vs Component 차이
- **[Transaction Management](../transaction-management/01_transaction-boundaries.md)** - 트랜잭션 경계 관리
- **[Command UseCase](../usecase-design/01_command-usecase.md)** - Service 구현 패턴

---

**작성자**: Development Team
**최종 수정일**: 2025-11-03
**버전**: 1.0.0
