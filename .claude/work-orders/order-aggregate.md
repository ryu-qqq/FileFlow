# 작업지시서: Order Aggregate

> **생성일**: 2024-11-04  
> **Aggregate**: Order (주문)  
> **목적**: 주문 생성, 취소, 상태 변경 관리

---

## 📋 생성할 파일

### Domain Layer (domain/order/)

```
domain/order/
├── model/
│   ├── OrderDomain.java           (Aggregate Root)
│   ├── OrderId.java                (Value Object)
│   ├── OrderStatus.java            (Enum: PENDING, CONFIRMED, CANCELLED, COMPLETED)
│   └── OrderItem.java              (Entity)
└── event/
    ├── OrderCreatedEvent.java      (Domain Event)
    ├── OrderConfirmedEvent.java    (Domain Event)
    └── OrderCancelledEvent.java    (Domain Event)
```

### Application Layer (application/order/)

```
application/order/
├── port/
│   ├── in/
│   │   ├── CreateOrderPort.java         (Command Port)
│   │   ├── CancelOrderPort.java         (Command Port)
│   │   ├── ConfirmOrderPort.java        (Command Port)
│   │   ├── GetOrderPort.java            (Query Port)
│   │   └── SearchOrderPort.java         (Query Port)
│   └── out/
│       ├── LoadOrderPort.java           (Repository Port)
│       └── SaveOrderPort.java           (Repository Port)
├── usecase/
│   ├── CreateOrderUseCase.java          (Command UseCase)
│   ├── CancelOrderUseCase.java          (Command UseCase)
│   ├── ConfirmOrderUseCase.java         (Command UseCase)
│   ├── GetOrderUseCase.java             (Query UseCase)
│   └── SearchOrderUseCase.java          (Query UseCase)
└── dto/
    ├── command/
    │   ├── CreateOrderCommand.java
    │   ├── CancelOrderCommand.java
    │   └── ConfirmOrderCommand.java
    ├── query/
    │   └── OrderSearchCondition.java
    └── response/
        ├── OrderResponse.java
        └── OrderListResponse.java
```

### REST API Layer (adapter-in/web/order/)

```
adapter-in/web/order/
├── controller/
│   └── OrderController.java             (REST Controller)
└── dto/
    ├── request/
    │   ├── CreateOrderRequest.java
    │   ├── CancelOrderRequest.java
    │   └── OrderSearchRequest.java
    └── response/
        └── OrderApiResponse.java
```

---

## ✅ 필수 규칙 (Zero-Tolerance)

### 1. Lombok 금지
- ❌ `@Data`, `@Builder`, `@Getter`, `@Setter` 모두 금지
- ✅ Pure Java getter/setter 직접 작성

### 2. Law of Demeter (Getter 체이닝 금지)
- ❌ `order.getCustomer().getAddress().getZipCode()`
- ✅ `order.getCustomerZipCode()` (Tell, Don't Ask 패턴)

### 3. Long FK Strategy (JPA 관계 금지)
- ❌ `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`
- ✅ `private Long customerId;` (Long FK 사용)

### 4. Transaction 경계
- ❌ `@Transactional` 내 외부 API 호출 (RestTemplate, WebClient 등)
- ✅ 트랜잭션은 짧게 유지, 외부 호출은 트랜잭션 밖에서

### 5. Javadoc 필수
- ❌ `@author`, `@since` 없는 public 클래스/메서드
- ✅ 모든 public 클래스/메서드에 Javadoc 포함

---

## 🎯 Domain 스켈레톤

### OrderDomain.java (Aggregate Root)

```java
package com.ryuqq.domain.order.model;

import com.ryuqq.domain.common.AbstractAggregateRoot;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Domain Aggregate
 * 
 * 주문의 생성, 취소, 확인 등 핵심 비즈니스 로직을 담당합니다.
 *
 * @author Claude Code
 * @since 1.0
 */
public class OrderDomain extends AbstractAggregateRoot<OrderDomain> {
    
    private final OrderId id;
    private final Long customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    
    /**
     * Private Constructor (Factory Pattern 강제)
     */
    private OrderDomain(
        OrderId id,
        Long customerId,
        List<OrderItem> items,
        OrderStatus status,
        BigDecimal totalAmount
    ) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderedAt = LocalDateTime.now();
    }
    
    /**
     * Factory Method: 주문 생성
     *
     * @param customerId 고객 ID
     * @param items 주문 항목 목록
     * @return 생성된 Order Domain
     */
    public static OrderDomain create(Long customerId, List<OrderItem> items) {
        // TODO: 비즈니스 로직 구현 (Claude Code 작업)
        // - 주문 항목 검증 (1개 이상)
        // - 총액 계산
        // - OrderCreatedEvent 등록
        return null;
    }
    
    /**
     * 비즈니스 메서드: 주문 확인
     * 
     * PENDING 상태인 경우에만 확인 가능
     */
    public void confirm() {
        // TODO: 비즈니스 로직 구현 (Claude Code 작업)
        // - 상태 검증 (PENDING만 가능)
        // - 상태 변경 → CONFIRMED
        // - OrderConfirmedEvent 등록
    }
    
    /**
     * 비즈니스 메서드: 주문 취소
     * 
     * PENDING 또는 CONFIRMED 상태인 경우에만 취소 가능
     */
    public void cancel() {
        // TODO: 비즈니스 로직 구현 (Claude Code 작업)
        // - 상태 검증 (CANCELLED, COMPLETED 불가)
        // - 상태 변경 → CANCELLED
        // - OrderCancelledEvent 등록
    }
    
    /**
     * 주문이 취소 가능한지 확인
     * 
     * @return 취소 가능 여부
     */
    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    // Getters (Pure Java)
    
    public OrderId getId() {
        return id;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }
    
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}
```

### OrderId.java (Value Object)

```java
package com.ryuqq.domain.order.model;

import java.util.Objects;

/**
 * Order ID Value Object
 *
 * @author Claude Code
 * @since 1.0
 */
public class OrderId {
    
    private final Long value;
    
    private OrderId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        this.value = value;
    }
    
    public static OrderId of(Long value) {
        return new OrderId(value);
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

### OrderStatus.java (Enum)

```java
package com.ryuqq.domain.order.model;

/**
 * Order Status Enum
 *
 * @author Claude Code
 * @since 1.0
 */
public enum OrderStatus {
    
    /**
     * 주문 대기 (생성 직후)
     */
    PENDING,
    
    /**
     * 주문 확인됨
     */
    CONFIRMED,
    
    /**
     * 주문 취소됨
     */
    CANCELLED,
    
    /**
     * 주문 완료됨
     */
    COMPLETED
}
```

---

## 🎯 UseCase 스켈레톤

### CreateOrderUseCase.java

```java
package com.ryuqq.application.order.usecase;

import com.ryuqq.application.common.UseCase;
import com.ryuqq.application.order.dto.command.CreateOrderCommand;
import com.ryuqq.application.order.dto.response.OrderResponse;
import com.ryuqq.application.order.port.in.CreateOrderPort;
import com.ryuqq.application.order.port.out.SaveOrderPort;
import com.ryuqq.domain.order.model.OrderDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create Order UseCase
 *
 * @author Claude Code
 * @since 1.0
 */
@UseCase
public class CreateOrderUseCase implements CreateOrderPort {
    
    private final SaveOrderPort saveOrderPort;
    
    public CreateOrderUseCase(SaveOrderPort saveOrderPort) {
        this.saveOrderPort = saveOrderPort;
    }
    
    /**
     * 주문 생성
     *
     * @param command 주문 생성 커맨드
     * @return 생성된 주문 정보
     */
    @Transactional
    @Override
    public OrderResponse execute(CreateOrderCommand command) {
        // TODO: UseCase 로직 (Claude Code 작업)
        // 1. Domain Factory로 Order 생성
        // 2. Repository에 저장
        // 3. Response DTO 변환 후 반환
        return null;
    }
}
```

---

## 🎯 Controller 스켈레톤

### OrderController.java

```java
package com.ryuqq.adapter.in.web.order.controller;

import com.ryuqq.adapter.in.web.common.ApiResponse;
import com.ryuqq.adapter.in.web.order.dto.request.CreateOrderRequest;
import com.ryuqq.application.order.dto.command.CreateOrderCommand;
import com.ryuqq.application.order.dto.response.OrderResponse;
import com.ryuqq.application.order.port.in.CreateOrderPort;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Order REST Controller
 *
 * @author Claude Code
 * @since 1.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final CreateOrderPort createOrderPort;
    
    public OrderController(CreateOrderPort createOrderPort) {
        this.createOrderPort = createOrderPort;
    }
    
    /**
     * 주문 생성
     *
     * @param request 주문 생성 요청
     * @return 생성된 주문 정보
     */
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        // TODO: Controller 로직 (Cursor AI 작업)
        // 1. Request → Command 변환
        // 2. UseCase 실행
        // 3. ApiResponse 포맷으로 반환
        return ApiResponse.success(null);
    }
}
```

---

## 📝 다음 단계

### 1. Cursor AI 작업 (Git Worktree)

```bash
# Worktree 생성
git worktree add ../wt-order feature/order-aggregate

# Cursor AI에서 작업
cd ../wt-order
# → .cursorrules 자동 로드
# → 이 작업지시서 참조
# → 위 스켈레톤 코드 생성
```

**Cursor AI 작업 체크리스트:**
- [ ] Domain Layer 파일 생성 (4개)
- [ ] Application Layer 파일 생성 (10개)
- [ ] REST API Layer 파일 생성 (4개)
- [ ] Javadoc 모든 public 메서드 포함
- [ ] Lombok 사용 금지 확인
- [ ] Git Commit

### 2. Git Commit (Cursor AI)

```bash
git add .
git commit -m "feat: Order Aggregate Boilerplate 생성

- Domain: OrderDomain, OrderId, OrderStatus
- Application: CreateOrderUseCase, Ports, DTOs
- REST API: OrderController, Request/Response DTOs

Refs: .claude/work-orders/order-aggregate.md"
```

**Commit 시 자동 실행:**
- Git Hook → `.claude/cursor-changes.md` 생성

### 3. Claude Code 검증 (Main 디렉토리)

```bash
# Main으로 복귀
cd ~/claude-spring-standards

# Cursor 변경 사항 검증
/validate-cursor-changes

# 검증 내용:
# - Lombok 금지
# - Law of Demeter
# - Transaction 경계
# - Javadoc 필수
# - Long FK Strategy
```

### 4. Claude Code 비즈니스 로직 구현

```bash
# Fixture 먼저 생성
/generate-fixtures Order --all

# 비즈니스 로직 구현
# → OrderDomain.create() 구현
# → OrderDomain.confirm() 구현
# → OrderDomain.cancel() 구현
# → UseCase 로직 구현
```

### 5. Claude Code 테스트 작성

```java
// Domain 테스트 (Happy/Edge/Exception Cases)
@Test
void create_shouldCreateOrderWithPendingStatus() {
    // Given
    Long customerId = 1L;
    List<OrderItem> items = OrderTestFixtures.orderItems();
    
    // When
    OrderDomain order = OrderDomain.create(customerId, items);
    
    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(order.getCustomerId()).isEqualTo(customerId);
}

// UseCase 테스트 (Object Mother 활용)
@Test
void createOrder_vipCustomer_shouldApplyDiscount() {
    // Given
    CreateOrderCommand command = OrderObjectMother.vipCustomerOrder();
    
    // When
    OrderResponse response = createOrderUseCase.execute(command);
    
    // Then
    assertThat(response.discountRate()).isEqualTo(0.15);
}
```

### 6. 최종 검증 및 Merge

```bash
# ArchUnit 검증
./gradlew test --tests "*ArchitectureTest"

# 모든 검증 통과 시 Merge
git worktree remove ../wt-order
git merge feature/order-aggregate

# PR 생성
gh pr create --title "feat: Order Aggregate 구현" \
  --body "$(cat .claude/work-orders/order-aggregate.md)"
```

---

## 🔗 참고 문서

- **컨벤션**: `docs/coding_convention/02-domain-layer/`
- **Cache 규칙**: `.claude/cache/rules/domain-layer-*`
- **검증 도구**: `.claude/hooks/scripts/validation-helper.py`

---

**✅ 이 작업지시서는 Cursor AI가 Order Aggregate Boilerplate를 생성하기 위한 완전한 가이드입니다!**
