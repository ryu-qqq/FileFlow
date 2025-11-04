# UseCase 메서드명 컨벤션

**목적**: Command/Query UseCase 메서드 네이밍 규칙 표준화 (CQRS 구분)

**위치**: `application/[context]/port/in/`

**관련 문서**:
- [Command UseCase](./01_command-usecase.md)
- [Query UseCase](./02_query-usecase.md)
- [Application Package Guide](../package-guide/01_application_package_guide.md)
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md)

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### UseCase 메서드명 3대 원칙

1. **CQRS 구분**: Command와 Query 메서드는 명확히 구분되어야 함
2. **의도 표현**: 메서드명만으로 수행할 작업이 명확해야 함
3. **일관성 유지**: 모든 UseCase에서 동일한 네이밍 패턴 사용

---

## 📌 Command 메서드 네이밍

### 규칙: `execute{Aggregate}{Action}()`

**패턴**:
- **접두사**: `execute` (필수)
- **집합체**: Order, Payment, Product 등
- **행동**: Creation, Cancellation, Update 등

### 예시

```java
/**
 * ✅ Command UseCase 메서드명
 */
public interface CreateOrderUseCase {
    OrderResponse executeOrderCreation(CreateOrderCommand command);
}

public interface CancelOrderUseCase {
    void executeOrderCancellation(CancelOrderCommand command);
}

public interface UpdateOrderStatusUseCase {
    OrderResponse executeOrderStatusUpdate(UpdateOrderStatusCommand command);
}

public interface DeleteProductUseCase {
    void executeProductDeletion(DeleteProductCommand command);
}

public interface ApprovePaymentUseCase {
    PaymentResponse executePaymentApproval(ApprovePaymentCommand command);
}
```

### 행동(Action) 명사화 가이드

| 동사 (Command) | 명사화 (Method) | 예시 |
|---------------|----------------|------|
| Create | Creation | `executeOrderCreation()` |
| Update | Update | `executeOrderStatusUpdate()` |
| Delete | Deletion | `executeProductDeletion()` |
| Cancel | Cancellation | `executeOrderCancellation()` |
| Approve | Approval | `executePaymentApproval()` |
| Confirm | Confirmation | `executeOrderConfirmation()` |
| Reject | Rejection | `executePaymentRejection()` |
| Issue | Issuance | `executeRefundIssuance()` |
| Activate | Activation | `executeAccountActivation()` |
| Deactivate | Deactivation | `executeAccountDeactivation()` |

---

## 🔍 Query 메서드 네이밍

### 규칙: `query{Aggregate}By{Condition}()` 또는 `query{Aggregate}()`

**패턴**:
- **접두사**: `query` (필수)
- **집합체**: Order, Payment, Product 등
- **조건** (선택): ById, ByCustomer, ByStatus 등

### 예시

```java
/**
 * ✅ Query UseCase 메서드명 - 단순 조회
 */
public interface GetOrderUseCase {
    OrderDetailResponse queryOrderById(GetOrderQuery query);
}

public interface GetProductUseCase {
    ProductResponse queryProductById(GetProductQuery query);
}

/**
 * ✅ Query UseCase 메서드명 - 조건부 조회
 */
public interface FindOrdersByCustomerUseCase {
    OrderListResponse queryOrdersByCustomer(FindOrdersByCustomerQuery query);
}

public interface FindProductsByStatusUseCase {
    ProductListResponse queryProductsByStatus(FindProductsByStatusQuery query);
}

/**
 * ✅ Query UseCase 메서드명 - 검색
 */
public interface SearchOrdersUseCase {
    OrderPageResponse queryOrders(SearchOrdersQuery query);
}

public interface SearchProductsUseCase {
    ProductPageResponse queryProducts(SearchProductsQuery query);
}

/**
 * ✅ Query UseCase 메서드명 - 목록 조회
 */
public interface ListOrdersUseCase {
    OrderListResponse queryOrders(ListOrdersQuery query);
}

/**
 * ✅ Query UseCase 메서드명 - 집계
 */
public interface CountOrdersByStatusUseCase {
    Long queryOrderCountByStatus(CountOrdersByStatusQuery query);
}

public interface ExistsOrderUseCase {
    Boolean queryOrderExistence(ExistsOrderQuery query);
}
```

---

## 📋 전체 예시: Order Context

### Command UseCases

```java
package com.company.application.order.port.in;

/**
 * 주문 생성 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CreateOrderUseCase {
    /**
     * ✅ execute + Order + Creation
     */
    OrderResponse executeOrderCreation(CreateOrderCommand command);
}

/**
 * 주문 취소 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CancelOrderUseCase {
    /**
     * ✅ execute + Order + Cancellation
     */
    void executeOrderCancellation(CancelOrderCommand command);
}

/**
 * 주문 확인 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ConfirmOrderUseCase {
    /**
     * ✅ execute + Order + Confirmation
     */
    OrderResponse executeOrderConfirmation(ConfirmOrderCommand command);
}
```

### Query UseCases

```java
package com.company.application.order.port.in;

/**
 * 주문 조회 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetOrderUseCase {
    /**
     * ✅ query + Order + ById
     */
    OrderDetailResponse queryOrderById(GetOrderQuery query);
}

/**
 * 고객별 주문 조회 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FindOrdersByCustomerUseCase {
    /**
     * ✅ query + Orders + ByCustomer
     */
    OrderListResponse queryOrdersByCustomer(FindOrdersByCustomerQuery query);
}

/**
 * 주문 검색 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchOrdersUseCase {
    /**
     * ✅ query + Orders (복잡한 검색은 조건 생략)
     */
    OrderPageResponse queryOrders(SearchOrdersQuery query);
}
```

---

## 🔄 Service 구현체 예시

### Command Service

```java
package com.company.application.order.service.command;

import com.company.application.order.port.in.CreateOrderUseCase;
import com.company.application.order.dto.command.CreateOrderCommand;
import com.company.application.order.dto.response.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 생성 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderAssembler orderAssembler;
    private final SaveOrderPort saveOrderPort;

    public CreateOrderService(
        OrderAssembler orderAssembler,
        SaveOrderPort saveOrderPort
    ) {
        this.orderAssembler = orderAssembler;
        this.saveOrderPort = saveOrderPort;
    }

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // 1. Assembler: Command → Domain
        Order order = orderAssembler.toDomain(command);

        // 2. Port: Domain 저장
        Order savedOrder = saveOrderPort.save(order);

        // 3. Assembler: Domain → Response
        return orderAssembler.toResponse(savedOrder);
    }
}
```

### Query Service

```java
package com.company.application.order.service.query;

import com.company.application.order.port.in.GetOrderUseCase;
import com.company.application.order.dto.query.GetOrderQuery;
import com.company.application.order.dto.response.OrderDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 조회 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final OrderAssembler orderAssembler;

    public GetOrderService(
        LoadOrderPort loadOrderPort,
        OrderAssembler orderAssembler
    ) {
        this.loadOrderPort = loadOrderPort;
        this.orderAssembler = orderAssembler;
    }

    @Override
    public OrderDetailResponse queryOrderById(GetOrderQuery query) {
        // 1. Port: Domain 조회
        Order order = loadOrderPort.load(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));

        // 2. Assembler: Domain → Response
        return orderAssembler.toDetailResponse(order);
    }
}
```

---

## 🔗 Adapter Layer에서의 호출

```java
package com.company.adapter.in.web;

import com.company.adapter.in.web.dto.OrderApiRequest;
import com.company.adapter.in.web.dto.OrderApiResponse;
import com.company.adapter.in.web.mapper.OrderApiMapper;
import com.company.application.order.port.in.CreateOrderUseCase;
import com.company.application.order.port.in.GetOrderUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order Controller
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OrderApiMapper orderApiMapper;

    public OrderController(
        CreateOrderUseCase createOrderUseCase,
        GetOrderUseCase getOrderUseCase,
        OrderApiMapper orderApiMapper
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.orderApiMapper = orderApiMapper;
    }

    /**
     * ✅ Command 실행: executeOrderCreation()
     */
    @PostMapping
    public ResponseEntity<OrderApiResponse> createOrder(
        @Valid @RequestBody OrderApiRequest request
    ) {
        // 1. Mapper: API Request → Command
        CreateOrderCommand command = orderApiMapper.toCommand(request);

        // 2. UseCase 실행 (Command 메서드)
        OrderResponse response = createOrderUseCase.executeOrderCreation(command);

        // 3. Mapper: Response → API Response
        OrderApiResponse apiResponse = orderApiMapper.toApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * ✅ Query 실행: queryOrderById()
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailApiResponse> getOrder(
        @PathVariable Long orderId
    ) {
        // 1. Mapper: Path Variable → Query
        GetOrderQuery query = orderApiMapper.toQuery(orderId);

        // 2. UseCase 실행 (Query 메서드)
        OrderDetailResponse response = getOrderUseCase.queryOrderById(query);

        // 3. Mapper: Response → API Response
        OrderDetailApiResponse apiResponse = orderApiMapper.toDetailApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }
}
```

---

## 🚨 Do / Don't

### Command 메서드

```java
// ✅ Good
OrderResponse executeOrderCreation(CreateOrderCommand command);
void executeOrderCancellation(CancelOrderCommand command);
OrderResponse executeOrderStatusUpdate(UpdateOrderStatusCommand command);

// ❌ Bad
OrderResponse createOrder(CreateOrderCommand command);           // execute 접두사 없음
OrderResponse executeOrder(CreateOrderCommand command);          // 행동 명사화 없음
OrderResponse executeCreate(CreateOrderCommand command);         // Aggregate 누락
OrderResponse performOrderCreation(CreateOrderCommand command);  // execute 대신 perform 사용
OrderResponse doOrderCreation(CreateOrderCommand command);       // execute 대신 do 사용
```

### Query 메서드

```java
// ✅ Good
OrderDetailResponse queryOrderById(GetOrderQuery query);
OrderListResponse queryOrdersByCustomer(FindOrdersByCustomerQuery query);
OrderPageResponse queryOrders(SearchOrdersQuery query);

// ❌ Bad
OrderDetailResponse getOrder(GetOrderQuery query);              // query 접두사 없음
OrderDetailResponse queryById(GetOrderQuery query);             // Aggregate 누락
OrderDetailResponse findOrder(GetOrderQuery query);             // query 대신 find 사용
OrderDetailResponse retrieveOrder(GetOrderQuery query);         // query 대신 retrieve 사용
OrderDetailResponse fetchOrder(GetOrderQuery query);            // query 대신 fetch 사용
```

---

## 📊 메서드명 vs UseCase명 비교

| UseCase 인터페이스 | 메서드명 | Command/Query DTO |
|-------------------|---------|-------------------|
| `CreateOrderUseCase` | `executeOrderCreation()` | `CreateOrderCommand` |
| `CancelOrderUseCase` | `executeOrderCancellation()` | `CancelOrderCommand` |
| `GetOrderUseCase` | `queryOrderById()` | `GetOrderQuery` |
| `FindOrdersByCustomerUseCase` | `queryOrdersByCustomer()` | `FindOrdersByCustomerQuery` |
| `SearchOrdersUseCase` | `queryOrders()` | `SearchOrdersQuery` |

**패턴 일관성**:
- UseCase 인터페이스명: `{Verb}{Aggregate}UseCase`
- 메서드명 (Command): `execute{Aggregate}{Action}()`
- 메서드명 (Query): `query{Aggregate}By{Condition}()`
- DTO명: `{Verb}{Aggregate}Command/Query`

---

## 📋 체크리스트

### Command 메서드
- [ ] `execute` 접두사 사용
- [ ] `{Aggregate}{Action}` 패턴 준수 (예: `OrderCreation`)
- [ ] 행동은 명사화 (Creation, Cancellation, Update 등)
- [ ] Command DTO를 파라미터로 받음
- [ ] Response DTO 또는 void 반환

### Query 메서드
- [ ] `query` 접두사 사용
- [ ] `{Aggregate}By{Condition}` 패턴 준수 (예: `OrdersByCustomer`)
- [ ] 조건은 선택적 (복잡한 검색은 생략 가능)
- [ ] Query DTO를 파라미터로 받음
- [ ] Response DTO 반환

---

## 📖 관련 문서

- **[Command UseCase](./01_command-usecase.md)** - Command UseCase 설계 패턴
- **[Query UseCase](./02_query-usecase.md)** - Query UseCase 설계 패턴
- **[DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md)** - DTO 네이밍 규칙
- **[Application Package Guide](../package-guide/01_application_package_guide.md)** - 전체 패키지 구조

---

**작성자**: Development Team
**최종 수정일**: 2025-11-03
**버전**: 1.0.0
