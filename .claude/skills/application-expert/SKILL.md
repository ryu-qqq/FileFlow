---
name: "application-expert"
description: "Spring Application Layer 전문가. UseCase, Command/Query 분리, Transaction 경계, Facade 패턴을 준수하는 Application 서비스를 생성합니다. @Transactional 내 외부 API 호출 금지, Spring Proxy 제약사항을 보장합니다."
---

# Spring Application Layer Expert

Application Layer 전문가 Skill입니다. UseCase(포트), 비즈니스 흐름 조율, Transaction 관리를 담당합니다.

## 전문 분야

1. **UseCase 설계**: Port/In 인터페이스, Command/Query 분리, 메서드 네이밍 규칙
2. **Transaction 경계**: `@Transactional` 올바른 사용, 외부 API 호출 분리
3. **Facade 패턴**: 복잡한 UseCase 조합
4. **Manager 패턴**: 복잡한 비즈니스 흐름 조율 (Facade보다 상위 개념)
5. **Assembler 패턴**: DTO ↔ Domain 변환

## 사용 시점

- UseCase(Port/In) 생성 또는 수정
- Command/Query DTO 설계
- Transaction 경계 관리
- 복잡한 비즈니스 흐름 구현

## 핵심 규칙

### 1. UseCase 설계 (Port/In)

**위치**: `docs/coding_convention/03-application-layer/usecase-design/`

**메서드 네이밍 규칙**:
- **Command 메서드**: `execute{Aggregate}{Action}()` 패턴 (예: `executeOrderCreation()`)
- **Query 메서드**: `query{Aggregate}By{Condition}()` 패턴 (예: `queryOrderById()`)
- **상세 가이드**: [UseCase 메서드 네이밍 컨벤션](../../docs/coding_convention/03-application-layer/usecase-design/04_usecase-method-naming.md)

**Port/In 인터페이스**:
```java
package com.ryuqq.application.order.port.in;

public interface CreateOrderUseCase {
    // ✅ Command 메서드: execute + Order + Creation
    OrderResult executeOrderCreation(CreateOrderCommand command);
}

public interface GetOrderUseCase {
    // ✅ Query 메서드: query + Order + ById
    OrderDetailResponse queryOrderById(GetOrderQuery query);
}
```

**UseCase 구현체**:
```java
package com.ryuqq.application.order.service;

@Service
@Transactional(readOnly = true)
public class CreateOrderService implements CreateOrderUseCase {

    // Port/Out 의존성
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    // Assembler
    private final OrderAssembler orderAssembler;

    // External Service (Transaction 밖에서 호출)
    private final PaymentClient paymentClient;

    // ✅ Plain Java Constructor (Lombok 금지)
    public CreateOrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            OrderAssembler orderAssembler,
            PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderAssembler = orderAssembler;
        this.paymentClient = paymentClient;
    }

    @Override
    @Transactional  // Write 작업
    public OrderResult executeOrderCreation(CreateOrderCommand command) {
        // 1. Domain 조회
        Customer customer = customerRepository.findById(command.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        // 2. Domain 생성
        Order order = orderAssembler.toDomain(command);

        // 3. 비즈니스 로직 (Domain 메서드 호출)
        order.validateForPlacement(customer);

        // 4. 저장
        Order savedOrder = orderRepository.save(order);

        // ⚠️ 주의: 외부 API 호출은 Transaction 밖에서!
        // ❌ paymentClient.processPayment(savedOrder);  // 여기서 호출 금지!

        // 5. 결과 반환
        return orderAssembler.toResult(savedOrder);
    }
}
```

**규칙**:
- ✅ Port/In 인터페이스 정의
- ✅ `@Service` + `@Transactional(readOnly = true)` 기본
- ✅ Write 작업에는 `@Transactional` 오버라이드
- ✅ Port/Out(Repository) 의존성 주입
- ✅ Assembler로 DTO ↔ Domain 변환
- ❌ **Transaction 내 외부 API 호출 절대 금지**

### 2. Transaction 경계 (Zero-Tolerance)

**위치**: `docs/coding_convention/03-application-layer/transaction-management/`

**올바른 Transaction 분리**:
```java
@Service
public class PlaceOrderFacade {

    private final PlaceOrderService placeOrderService;
    private final PaymentClient paymentClient;
    private final NotificationService notificationService;

    // ✅ Plain Java Constructor (Lombok 금지)
    public PlaceOrderFacade(
            PlaceOrderService placeOrderService,
            PaymentClient paymentClient,
            NotificationService notificationService) {
        this.placeOrderService = placeOrderService;
        this.paymentClient = paymentClient;
        this.notificationService = notificationService;
    }

    public OrderResult placeOrder(PlaceOrderCommand command) {
        // 1. Transaction 내: Domain 작업
        OrderResult result = placeOrderService.execute(command);

        // 2. Transaction 밖: 외부 API 호출
        try {
            paymentClient.processPayment(result.orderId());
        } catch (PaymentException ex) {
            // 결제 실패 시 보상 Transaction
            placeOrderService.cancelOrder(result.orderId());
            throw ex;
        }

        // 3. Transaction 밖: 알림 발송
        notificationService.sendOrderConfirmation(result);

        return result;
    }
}

@Service
@Transactional(readOnly = true)
class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderAssembler orderAssembler;

    // ✅ Plain Java Constructor (Lombok 금지)
    public PlaceOrderService(
            OrderRepository orderRepository,
            OrderAssembler orderAssembler) {
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
    }

    @Override
    @Transactional  // ✅ Transaction은 짧게 유지
    public OrderResult execute(PlaceOrderCommand command) {
        // Domain 작업만 수행
        Order order = orderAssembler.toDomain(command);
        Order savedOrder = orderRepository.save(order);
        return orderAssembler.toResult(savedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        orderRepository.save(order);
    }
}
```

**규칙**:
- ✅ Transaction은 짧게 유지 (Domain 작업만)
- ✅ 외부 API 호출은 Facade에서 Transaction 밖에서
- ✅ 보상 Transaction은 별도 메서드로 분리
- ❌ **`@Transactional` 내 RestTemplate, WebClient, Feign 호출 금지**

### 3. Spring Proxy 제약사항 (Zero-Tolerance)

**위치**: `docs/coding_convention/03-application-layer/transaction-management/02_spring-proxy-constraints.md`

**위반 패턴**:
```java
@Service
public class OrderService {

    // ❌ Private 메서드에 @Transactional (작동 안 함)
    @Transactional
    private void createOrder(Order order) {
        orderRepository.save(order);
    }

    // ❌ 같은 클래스 내부 호출 (작동 안 함)
    public void placeOrder(PlaceOrderCommand command) {
        Order order = new Order(command);
        this.createOrder(order);  // ❌ Proxy 우회, Transaction 작동 안 함
    }
}

// ❌ Final 클래스 (Proxy 생성 불가)
@Service
public final class OrderService {
    @Transactional
    public void createOrder(Order order) {
        // Transaction 작동 안 함
    }
}
```

**올바른 패턴**:
```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderAssembler orderAssembler;

    // ✅ Plain Java Constructor (Lombok 금지)
    public OrderService(
            OrderRepository orderRepository,
            OrderAssembler orderAssembler) {
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
    }

    // ✅ Public 메서드에만 @Transactional
    @Transactional
    public OrderResult createOrder(PlaceOrderCommand command) {
        Order order = new Order(command);
        orderRepository.save(order);
        return orderAssembler.toResult(order);
    }

    // ✅ Transaction이 필요 없는 메서드는 private
    private void validateCommand(PlaceOrderCommand command) {
        // Validation only
    }
}
```

**규칙**:
- ✅ `@Transactional`은 **Public 메서드에만** 사용
- ✅ 클래스는 **Final 금지**
- ✅ 내부 호출이 필요하면 **별도 Service로 분리**
- ❌ Private/Final/내부 호출에서 `@Transactional` 작동 안 함

### 4. Command/Query 분리

**Command (Write)**:
```java
public record PlaceOrderCommand(
    String orderNumber,
    Long customerId,
    List<OrderItemCommand> items,
    AddressCommand shippingAddress
) {
    // Validation
    public PlaceOrderCommand {
        Objects.requireNonNull(orderNumber);
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(items);
        items = List.copyOf(items);
    }
}

public record OrderItemCommand(
    Long productId,
    int quantity,
    BigDecimal price
) {}
```

**Query (Read)**:
```java
public record FindOrderQuery(
    Long orderId
) {
    public FindOrderQuery {
        Objects.requireNonNull(orderId);
    }
}

public record OrderSearchCriteria(
    Long customerId,
    OrderStatus status,
    LocalDate startDate,
    LocalDate endDate,
    Pageable pageable
) {}
```

**Result (Output)**:
```java
public record OrderResult(
    Long orderId,
    String orderNumber,
    OrderStatus status,
    Long customerId,
    List<OrderItemResult> items,
    AddressResult shippingAddress,
    LocalDateTime createdAt
) {}
```

### 5. Assembler 패턴

**Assembler 역할**: DTO ↔ Domain 변환
```java
@Component
public class OrderAssembler {

    // Command → Domain
    public Order toDomain(PlaceOrderCommand command) {
        List<OrderItem> items = command.items().stream()
            .map(this::toOrderItem)
            .toList();

        Address address = toAddress(command.shippingAddress());

        return new Order(
            command.orderNumber(),
            command.customerId(),
            items,
            address
        );
    }

    // Domain → Result
    public OrderResult toResult(Order order) {
        List<OrderItemResult> items = order.getItems().stream()
            .map(this::toOrderItemResult)
            .toList();

        AddressResult address = toAddressResult(order.getShippingAddress());

        return new OrderResult(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getCustomerId(),
            items,
            address,
            order.getCreatedAt()
        );
    }

    // Private helper methods
    private OrderItem toOrderItem(OrderItemCommand command) {
        return new OrderItem(
            command.productId(),
            command.quantity(),
            command.price()
        );
    }

    private OrderItemResult toOrderItemResult(OrderItem item) {
        return new OrderItemResult(
            item.getProductId(),
            item.getQuantity(),
            item.getPrice()
        );
    }

    private Address toAddress(AddressCommand command) {
        return new Address(
            command.zipCode(),
            command.street(),
            command.city(),
            command.state()
        );
    }

    private AddressResult toAddressResult(Address address) {
        return new AddressResult(
            address.zipCode(),
            address.street(),
            address.city(),
            address.state()
        );
    }
}
```

### 6. Manager vs StateManager vs Facade 패턴

**아키텍처**:
```
UseCase Service → Facade → Manager → StateManager → Port(Repository)
```

**네이밍 규칙**:
- **StateManager**: `{BoundedContext}StateManager` (예: `OrderStateManager`, `PaymentStateManager`)
- **Manager**: `{BusinessFlow}Manager` (예: `OrderPaymentManager`, `OrderShippingManager`)
- **Facade**: `{Domain}Facade` (예: `OrderFulfillmentFacade`, `CheckoutFacade`)

#### StateManager 패턴

**역할**: 단일 Bounded Context(Order, Payment 등)의 상태 관리

```java
@Component
public class OrderStateManager {

    private final SaveOrderPort saveOrderPort;
    private final LoadOrderPort loadOrderPort;
    private final OrderAssembler orderAssembler;

    // ✅ Plain Java Constructor (Lombok 금지)
    public OrderStateManager(
            SaveOrderPort saveOrderPort,
            LoadOrderPort loadOrderPort,
            OrderAssembler orderAssembler) {
        this.saveOrderPort = saveOrderPort;
        this.loadOrderPort = loadOrderPort;
        this.orderAssembler = orderAssembler;
    }

    /**
     * Order 생성
     */
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = orderAssembler.toDomain(command);
        return saveOrderPort.save(order);
    }

    /**
     * Order 완료
     */
    @Transactional
    public Order completeOrder(Long orderId) {
        Order order = loadOrderPort.load(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.complete();
        return saveOrderPort.save(order);
    }
}
```

#### PaymentStateManager + Outbox Pattern

```java
@Component
public class PaymentStateManager {

    private final SavePaymentPort savePaymentPort;
    private final LoadPaymentPort loadPaymentPort;
    private final OutboxStateManager outboxStateManager;
    private final PaymentAssembler paymentAssembler;

    // ✅ Plain Java Constructor
    public PaymentStateManager(
            SavePaymentPort savePaymentPort,
            LoadPaymentPort loadPaymentPort,
            OutboxStateManager outboxStateManager,
            PaymentAssembler paymentAssembler) {
        this.savePaymentPort = savePaymentPort;
        this.loadPaymentPort = loadPaymentPort;
        this.outboxStateManager = outboxStateManager;
        this.paymentAssembler = paymentAssembler;
    }

    /**
     * 외부 결제 처리 요청 (Outbox 저장)
     *
     * ✅ @Transactional: Payment + Outbox 원자성 보장
     * ❌ 외부 API 호출 없음 (Scheduler가 비동기 처리)
     */
    @Transactional
    public Payment processExternalPayment(CreatePaymentCommand command) {
        // 1. Payment 생성 (Assembler 사용)
        Payment payment = paymentAssembler.toDomain(command);
        Payment saved = savePaymentPort.save(payment);

        // 2. Outbox 저장 (동일 트랜잭션)
        outboxStateManager.createOutboxEntry(
            "PAYMENT",
            saved.getId().toString(),
            "PAYMENT_REQUESTED",
            paymentAssembler.toPayloadJson(saved)
        );

        return saved;
    }

    /**
     * 결제 상태 업데이트 (Handler에서 호출)
     */
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = loadPaymentPort.load(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        payment.updateStatus(status);
        return savePaymentPort.save(payment);
    }
}
```

#### Manager 패턴 (StateManager 오케스트레이션)

**역할**: 2-3개 StateManager 오케스트레이션

```java
@Component
public class OrderPaymentManager {

    private final OrderStateManager orderStateManager;
    private final PaymentStateManager paymentStateManager;

    // ✅ Plain Java Constructor
    public OrderPaymentManager(
            OrderStateManager orderStateManager,
            PaymentStateManager paymentStateManager) {
        this.orderStateManager = orderStateManager;
        this.paymentStateManager = paymentStateManager;
    }

    /**
     * 주문 + 결제 처리
     *
     * ✅ 각 StateManager 호출은 별도 트랜잭션
     * ✅ 외부 API 호출은 Outbox Pattern으로 비동기 처리
     */
    public OrderResult placeOrderWithPayment(PlaceOrderCommand command) {
        // 1. Order 생성 (TX1)
        Order order = orderStateManager.createOrder(command);

        // 2. Payment 요청 + Outbox 저장 (TX2)
        //    ✅ Payment PENDING + Outbox PENDING (원자성 보장)
        Payment payment = paymentStateManager.processExternalPayment(
            CreatePaymentCommand.of(order, command.paymentMethod())
        );

        // 3-5는 Scheduler가 비동기 처리:
        //    - OutboxScheduler: PENDING 엔트리 폴링
        //    - PaymentOutboxHandler: 외부 API 호출 + 상태 업데이트
        //    - Order 완료는 별도 로직 (Payment 완료 이벤트 구독)

        return new OrderResult(order, payment);
    }
}
```

#### Facade 패턴 (여러 Manager 통합)

```java
@Component
public class CheckoutFacade {

    private final OrderPaymentManager orderPaymentManager;
    private final OrderShippingManager orderShippingManager;

    // ✅ Plain Java Constructor
    public CheckoutFacade(
            OrderPaymentManager orderPaymentManager,
            OrderShippingManager orderShippingManager) {
        this.orderPaymentManager = orderPaymentManager;
        this.orderShippingManager = orderShippingManager;
    }

    /**
     * 전체 체크아웃 프로세스
     */
    public CheckoutResult processCheckout(CheckoutCommand command) {
        // 1. Order + Payment
        OrderResult orderResult = orderPaymentManager.placeOrderWithPayment(command);

        // 2. Shipping
        ShippingResult shippingResult = orderShippingManager.arrangeShipping(orderResult);

        return new CheckoutResult(orderResult, shippingResult);
    }
}
```

**Manager 사용 시점**:
- ✅ **여러 Bounded Context** 조율 (Order + Payment + Inventory)
- ✅ **복잡한 보상 Transaction** 필요
- ✅ **Saga 패턴** 구현
- ✅ **비즈니스 흐름이 5단계 이상** 복잡할 때

**Facade 사용 시점** (Manager보다 간단):
- ✅ **단일 Bounded Context** 내 여러 UseCase 조율
- ✅ **간단한 보상 Transaction**
- ✅ **비즈니스 흐름이 2-3단계**

## 패키지 구조

```
application/
├── order/
│   ├── port/
│   │   ├── in/
│   │   │   ├── PlaceOrderUseCase.java          # UseCase 인터페이스 (Command)
│   │   │   ├── CancelOrderUseCase.java
│   │   │   └── FindOrderUseCase.java           # UseCase 인터페이스 (Query)
│   │   └── out/
│   │       ├── SaveOrderPort.java              # Command Port
│   │       ├── DeleteOrderPort.java
│   │       └── LoadOrderPort.java              # Query Port
│   ├── service/
│   │   ├── PlaceOrderService.java              # UseCase 구현체 (Service 접미사)
│   │   ├── CancelOrderService.java
│   │   └── FindOrderService.java
│   ├── manager/                                # ⭐ 상태 관리 및 조율 계층
│   │   ├── OrderStateManager.java              # 단일 Bounded Context 상태 관리
│   │   ├── PaymentStateManager.java
│   │   ├── OutboxStateManager.java             # Outbox 상태 관리
│   │   └── OrderPaymentManager.java            # 2-3개 StateManager 조율
│   ├── facade/
│   │   └── CheckoutFacade.java                 # 여러 Manager 통합 (복잡한 워크플로우)
│   ├── assembler/
│   │   ├── OrderAssembler.java                 # Order DTO ↔ Domain 변환
│   │   ├── PaymentAssembler.java
│   │   └── OutboxAssembler.java
│   └── dto/
│       ├── command/
│       │   ├── PlaceOrderCommand.java          # Command DTO (Write)
│       │   ├── OrderItemCommand.java
│       │   └── CreatePaymentCommand.java
│       ├── query/
│       │   └── FindOrderQuery.java             # Query DTO (Read)
│       └── result/
│           ├── OrderResult.java                # Response DTO
│           ├── OrderItemResult.java
│           └── PaymentResult.java
├── payment/
│   ├── port/in/
│   ├── service/
│   └── ...
└── customer/
    └── ...
```

**계층 구조**:
```
UseCase Service (접미사: Service)
    ↓ 의존
Facade (복잡한 워크플로우)
    ↓ 의존
Manager (2-3개 StateManager 조율)
    ↓ 의존
StateManager (단일 Bounded Context 상태 관리)
    ↓ 의존
Port (Out)
```

**네이밍 규칙**:
- **StateManager**: `{BoundedContext}StateManager` (예: `OrderStateManager`, `PaymentStateManager`)
- **Manager**: `{BusinessFlow}Manager` (예: `OrderPaymentManager`, `CheckoutManager`)
- **Facade**: `{Domain}Facade` (예: `CheckoutFacade`, `OrderProcessingFacade`)
- **UseCase Service**: `{Action}{Aggregate}Service` (예: `PlaceOrderService`, `CancelOrderService`)

## Zero-Tolerance 체크리스트

Application Layer 코드 작성 후 반드시 확인:

### 🚨 아키텍처 규칙 (Zero-Tolerance)
- [ ] **Lombok 금지**: `@RequiredArgsConstructor`, `@Data`, `@Builder` 등 모두 금지 → Plain Java Constructor 사용
- [ ] **Assembler 필수**: 모든 DTO ↔ Domain 변환은 Assembler를 통해서만 수행
- [ ] **StateManager 사용**: 단일 Bounded Context 상태 변경은 StateManager에서
- [ ] **Port/In 인터페이스**: UseCase는 인터페이스로 정의
- [ ] **Command/Query 분리**: Write/Read DTO 완전 분리

### ⚡ Transaction 경계 규칙 (Zero-Tolerance)
- [ ] **Transaction 경계**: `@Transactional` 내 외부 API 호출 **절대 금지**
- [ ] **State 분리**: 외부 호출 전후로 상태 변경을 별도 트랜잭션으로 분리
- [ ] **Outbox Pattern**: 외부 API 호출 필요 시 Outbox + Scheduler 패턴 사용
- [ ] **Scheduler 규칙**: OutboxScheduler의 `pollOutbox()`에 `@Transactional` 금지

### 🔧 Spring Proxy 제약사항 (Zero-Tolerance)
- [ ] **Public Only**: Public 메서드에만 `@Transactional` 사용 (Private/Protected 금지)
- [ ] **내부 호출 금지**: 같은 클래스 내 `@Transactional` 메서드 호출 없음 (`this.method()` 금지)
- [ ] **Final 금지**: Service/Manager/StateManager 클래스는 Final 아님

### 📝 네이밍 규칙
- [ ] **UseCase Method**: Command는 `execute{Aggregate}{Action}()`, Query는 `query{Aggregate}By{Condition}()`
- [ ] **UseCase Service**: 구현체는 `Service` 접미사 필수 (예: `PlaceOrderService`)
- [ ] **StateManager**: `{BoundedContext}StateManager` (예: `OrderStateManager`)
- [ ] **Manager**: `{BusinessFlow}Manager` (예: `OrderPaymentManager`)
- [ ] **Facade**: `{Domain}Facade` (예: `CheckoutFacade`)

## 추가 리소스

상세 규칙:

```bash
cat .claude/skills/application-expert/REFERENCE.md
```

검증 스크립트:

```bash
bash .claude/skills/application-expert/scripts/validate-application.sh [file_path]
```

## 참고 문서

- `docs/coding_convention/03-application-layer/`
- `docs/coding_convention/03-application-layer/transaction-management/01_transaction-boundary.md`
- `docs/coding_convention/03-application-layer/transaction-management/02_spring-proxy-constraints.md`
- `docs/coding_convention/03-application-layer/usecase-design/01_usecase-interface.md`
- `docs/coding_convention/03-application-layer/usecase-design/04_usecase-method-naming.md` ⭐ NEW
- `docs/coding_convention/03-application-layer/facade/01_facade-usage-guide.md`
