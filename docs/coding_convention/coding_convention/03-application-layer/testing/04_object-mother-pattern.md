# Object Mother 패턴 (비즈니스 시나리오 표현)

**목적**: 복잡한 비즈니스 시나리오를 의미 있는 DTO 조합으로 표현

**위치**: `application/src/testFixtures/java/com/company/application/{context}/mother/`

**관련 문서**:
- [Test Fixture 패턴](03_test-fixture-pattern.md) - 기본 DTO 생성
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md) - DTO 네이밍 규칙
- [UseCase Method Naming](../usecase-design/04_usecase-method-naming.md) - UseCase 메서드명 규칙

---

## 📌 핵심 개념

### Object Mother란?

**Object Mother**는 **"비즈니스적으로 의미 있는 상태"**를 가진 Application DTO를 생성하는 패턴입니다.

Domain Layer Object Mother와 달리, **Application Layer Object Mother**는:
- Command/Query DTO의 비즈니스 시나리오 표현
- 여러 DTO를 조합하여 복잡한 테스트 케이스 구성
- 통합 테스트 및 End-to-End 테스트에 최적화

---

### Fixture vs Object Mother

| 구분 | Fixture | Object Mother |
|------|---------|---------------|
| **목적** | 기본 DTO 생성 | 비즈니스 시나리오 표현 |
| **네이밍** | `create()` | `pendingOrderCommand()` |
| **복잡도** | 단순 (1-2 필드 설정) | 복잡 (여러 DTO 조합) |
| **비즈니스 의미** | 없음 (데이터 중심) | 있음 (시나리오 중심) |
| **테스트 가독성** | 낮음 | 높음 (시나리오가 명확) |
| **패키지** | `fixture/` | `mother/` |

---

## ✅ Object Mother 패턴

### 사용 시기

다음 조건 **2개 이상** 해당 시 Object Mother 사용:

- [ ] **복잡한 비즈니스 시나리오**: 승인 요청, 취소 요청, 검색 조건 조합
- [ ] **여러 DTO 조합**: Command + Query 조합, 다단계 요청
- [ ] **테스트 가독성 중요**: Given 단계에서 비즈니스 맥락 명확히 표현
- [ ] **통합 테스트**: End-to-End 시나리오 검증
- [ ] **Facade/Component 테스트**: 여러 UseCase를 호출하는 복잡한 흐름

---

## 🏗️ Object Mother 클래스 작성

### Command Object Mother

```java
package com.company.application.order.mother;

import com.company.application.order.fixture.CreateOrderCommandFixture;
import com.company.application.order.fixture.OrderItemFixture;
import com.company.application.order.dto.command.*;
import com.company.application.order.dto.command.CreateOrderCommand.OrderItem;

import java.util.List;

/**
 * Order Command Object Mother - 비즈니스 시나리오 표현
 *
 * <p>비즈니스적으로 의미 있는 상태의 Order Command를 생성하는 클래스입니다.
 * 복잡한 비즈니스 흐름을 명확한 이름으로 표현합니다.</p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * CreateOrderCommand command = OrderCommands.newOrderCommand();           // 신규 주문
 * CreateOrderCommand command = OrderCommands.bulkOrderCommand();          // 대량 주문
 * CreateOrderCommand command = OrderCommands.vipCustomerOrderCommand();   // VIP 고객 주문
 * CancelOrderCommand command = OrderCommands.cancelPendingOrderCommand(); // 대기 중인 주문 취소
 * }</pre>
 *
 * <h3>네이밍 원칙:</h3>
 * <ul>
 *   <li>클래스명: 복수형 명사 (OrderCommands, PaymentCommands)</li>
 *   <li>메서드명: 비즈니스 시나리오 표현 (newOrderCommand, bulkOrderCommand)</li>
 *   <li>Command 접미사: 모든 메서드는 'Command' 접미사 필수</li>
 * </ul>
 *
 * @see CreateOrderCommandFixture 단순 데이터 생성용
 * @author development-team
 * @since 1.0.0
 */
public class OrderCommands {

    /**
     * 신규 주문 Command (일반 고객, 소량 상품)
     *
     * <p><strong>비즈니스 시나리오</strong>: 일반 고객의 기본 주문</p>
     */
    public static CreateOrderCommand newOrderCommand() {
        return CreateOrderCommandFixture.create();
    }

    /**
     * VIP 고객 주문 Command (고액 주문)
     *
     * <p><strong>비즈니스 시나리오</strong>:</p>
     * <ul>
     *   <li>VIP 고객 (ID: 999L)</li>
     *   <li>고액 상품 (100만원 이상)</li>
     *   <li>특별 요청 사항 포함</li>
     * </ul>
     */
    public static CreateOrderCommand vipCustomerOrderCommand() {
        return new CreateOrderCommand(
            999L,  // VIP 고객
            List.of(
                new OrderItem(201L, 1, 1000000L),  // 고액 상품
                new OrderItem(202L, 2, 500000L)
            ),
            "VIP 고객 - 신속 배송 요청"
        );
    }

    /**
     * 대량 주문 Command (5개 이상 상품)
     *
     * <p><strong>비즈니스 시나리오</strong>: 도매상 대량 구매</p>
     */
    public static CreateOrderCommand bulkOrderCommand() {
        return new CreateOrderCommand(
            1L,
            List.of(
                new OrderItem(101L, 10, 100000L),
                new OrderItem(102L, 5, 50000L),
                new OrderItem(103L, 20, 200000L)
            ),
            "대량 주문 - 할인 적용 요청"
        );
    }

    /**
     * 단일 상품 주문 Command (간단한 주문)
     *
     * <p><strong>비즈니스 시나리오</strong>: 단일 상품 빠른 구매</p>
     */
    public static CreateOrderCommand singleItemOrderCommand() {
        return CreateOrderCommandFixture.createWithSingleItem(101L, 1, 10000L);
    }

    /**
     * 주문 취소 Command (대기 중인 주문)
     *
     * <p><strong>비즈니스 시나리오</strong>:</p>
     * <ul>
     *   <li>주문 상태: PENDING</li>
     *   <li>취소 사유: 고객 요청</li>
     * </ul>
     */
    public static CancelOrderCommand cancelPendingOrderCommand() {
        return new CancelOrderCommand(1L, "고객 요청으로 주문 취소");
    }

    /**
     * 주문 취소 Command (재고 부족)
     *
     * <p><strong>비즈니스 시나리오</strong>: 시스템에 의한 자동 취소</p>
     */
    public static CancelOrderCommand cancelDueToOutOfStockCommand() {
        return new CancelOrderCommand(1L, "재고 부족으로 자동 취소");
    }

    /**
     * 주문 승인 Command (결제 완료 후)
     *
     * <p><strong>비즈니스 시나리오</strong>: 결제 검증 완료 → 주문 승인</p>
     */
    public static ApproveOrderCommand approveAfterPaymentCommand() {
        return new ApproveOrderCommand(1L, "결제 완료 확인");
    }

    /**
     * 배송 시작 Command (승인된 주문)
     *
     * <p><strong>비즈니스 시나리오</strong>:</p>
     * <ul>
     *   <li>주문 상태: APPROVED</li>
     *   <li>배송사: CJ대한통운</li>
     *   <li>송장 번호 발급</li>
     * </ul>
     */
    public static ShipOrderCommand shipApprovedOrderCommand() {
        return new ShipOrderCommand(1L, "CJ대한통운", "123456789");
    }

    // Private 생성자 - 인스턴스화 방지
    private OrderCommands() {
        throw new AssertionError("Object Mother 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### Query Object Mother

```java
package com.company.application.order.mother;

import com.company.application.order.fixture.OrderQueryFixture;
import com.company.application.order.dto.query.*;

import java.time.LocalDate;

/**
 * Order Query Object Mother - 비즈니스 시나리오 표현
 *
 * <p>비즈니스적으로 의미 있는 검색 조건을 생성하는 클래스입니다.</p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class OrderQueries {

    /**
     * 특정 주문 조회 Query
     *
     * <p><strong>비즈니스 시나리오</strong>: 주문 상세 조회</p>
     */
    public static GetOrderQuery getOrderQuery() {
        return OrderQueryFixture.createGetOrderQuery();
    }

    /**
     * 특정 고객의 주문 목록 조회 Query
     *
     * <p><strong>비즈니스 시나리오</strong>: 마이페이지 - 주문 내역</p>
     */
    public static FindOrdersByCustomerQuery myOrdersQuery(Long customerId) {
        return OrderQueryFixture.createFindByCustomerQuery(customerId);
    }

    /**
     * VIP 고객 주문 검색 Query
     *
     * <p><strong>비즈니스 시나리오</strong>: VIP 고객 관리 - 주문 이력</p>
     */
    public static SearchOrdersQuery vipCustomerOrdersQuery() {
        return SearchOrdersQuery.builder()
            .customerId(999L)  // VIP 고객
            .minAmount(100000L)  // 10만원 이상
            .page(0)
            .size(50)
            .sortBy("totalAmount")
            .sortDirection("DESC")
            .build();
    }

    /**
     * 대기 중인 주문 검색 Query
     *
     * <p><strong>비즈니스 시나리오</strong>: 관리자 - 처리 대기 주문</p>
     */
    public static SearchOrdersQuery pendingOrdersQuery() {
        return SearchOrdersQuery.builder()
            .status("PENDING")
            .page(0)
            .size(20)
            .sortBy("createdAt")
            .sortDirection("ASC")  // 오래된 순서
            .build();
    }

    /**
     * 오늘 주문 검색 Query
     *
     * <p><strong>비즈니스 시나리오</strong>: 관리자 - 당일 주문 현황</p>
     */
    public static SearchOrdersQuery todayOrdersQuery() {
        LocalDate today = LocalDate.now();
        return SearchOrdersQuery.builder()
            .startDate(today)
            .endDate(today)
            .page(0)
            .size(100)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }

    /**
     * 이번 달 주문 검색 Query
     *
     * <p><strong>비즈니스 시나리오</strong>: 통계 - 월별 매출</p>
     */
    public static SearchOrdersQuery currentMonthOrdersQuery() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        return SearchOrdersQuery.builder()
            .startDate(startOfMonth)
            .endDate(endOfMonth)
            .page(0)
            .size(1000)  // 대량 조회
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }

    /**
     * 고액 주문 검색 Query (100만원 이상)
     *
     * <p><strong>비즈니스 시나리오</strong>: 통계 - 고액 주문 분석</p>
     */
    public static SearchOrdersQuery highValueOrdersQuery() {
        return SearchOrdersQuery.builder()
            .minAmount(1000000L)
            .page(0)
            .size(50)
            .sortBy("totalAmount")
            .sortDirection("DESC")
            .build();
    }

    // Private 생성자 - 인스턴스화 방지
    private OrderQueries() {
        throw new AssertionError("Object Mother 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### Response Object Mother

```java
package com.company.application.order.mother;

import com.company.application.order.fixture.OrderResponseFixture;
import com.company.application.order.dto.response.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response Object Mother - 비즈니스 시나리오 표현
 *
 * <p>비즈니스적으로 의미 있는 상태의 Response를 생성하는 클래스입니다.</p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class OrderResponses {

    /**
     * 대기 중인 주문 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: 주문 생성 직후</p>
     */
    public static OrderResponse pendingOrderResponse() {
        return OrderResponseFixture.createWithStatus("PENDING");
    }

    /**
     * 승인된 주문 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: 결제 완료 후</p>
     */
    public static OrderResponse approvedOrderResponse() {
        return OrderResponseFixture.createWithStatus("APPROVED");
    }

    /**
     * 배송 중인 주문 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: 배송 시작됨</p>
     */
    public static OrderResponse shippedOrderResponse() {
        return OrderResponseFixture.createWithStatus("SHIPPED");
    }

    /**
     * 완료된 주문 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: 배송 완료</p>
     */
    public static OrderResponse completedOrderResponse() {
        return OrderResponseFixture.createWithStatus("COMPLETED");
    }

    /**
     * 취소된 주문 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: 주문 취소됨</p>
     */
    public static OrderResponse cancelledOrderResponse() {
        return OrderResponseFixture.createWithStatus("CANCELLED");
    }

    /**
     * VIP 고객 주문 상세 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: VIP 고객 고액 주문</p>
     */
    public static OrderDetailResponse vipOrderDetailResponse() {
        return new OrderDetailResponse(
            1L,
            new OrderDetailResponse.CustomerInfo(999L, "VIP 홍길동", "vip@example.com"),
            List.of(
                new OrderDetailResponse.LineItem(201L, "프리미엄 상품", 1, 1000000L, 1000000L)
            ),
            1000000L,
            "APPROVED",
            LocalDateTime.now()
        );
    }

    /**
     * 빈 주문 목록 Response
     *
     * <p><strong>비즈니스 시나리오</strong>: 조회 결과 없음</p>
     */
    public static OrderListResponse emptyOrderListResponse() {
        return OrderResponseFixture.createEmptyList();
    }

    /**
     * 페이징된 주문 목록 Response (첫 페이지)
     *
     * <p><strong>비즈니스 시나리오</strong>: 주문 목록 조회 (20건)</p>
     */
    public static OrderListResponse pagedOrderListResponse() {
        return new OrderListResponse(
            List.of(
                approvedOrderResponse(),
                shippedOrderResponse(),
                completedOrderResponse()
            ),
            3
        );
    }

    // Private 생성자 - 인스턴스화 방지
    private OrderResponses() {
        throw new AssertionError("Object Mother 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

## 🎯 실전 사용 예시

### Before (Fixture만 사용)

```java
@Test
void createOrderWithPayment_ShouldSucceed() {
    // Given - ❌ 비즈니스 의미 불명확
    CreateOrderCommand orderCommand = CreateOrderCommandFixture.create();
    ProcessPaymentCommand paymentCommand = ProcessPaymentCommandFixture.create();

    // 이게 무슨 시나리오인지 불명확
    // When & Then...
}
```

**문제점**:
- ❌ 여러 Command를 조합한 의미가 불명확
- ❌ 테스트만 봐도 무엇을 검증하는지 알 수 없음
- ❌ 유지보수 어려움

---

### After (Object Mother 사용)

```java
@Test
void createVipOrderWithPriorityPayment_ShouldSucceed() {
    // Given - ✅ 비즈니스 의미 명확
    CreateOrderCommand orderCommand = OrderCommands.vipCustomerOrderCommand();
    ProcessPaymentCommand paymentCommand = PaymentCommands.priorityPaymentCommand();

    // ✅ "VIP 고객의 우선 결제" 시나리오 명확
    // When & Then...
}
```

**장점**:
- ✅ `vipCustomerOrderCommand()`가 비즈니스 시나리오를 명확히 표현
- ✅ 테스트 가독성 향상 (Given 단계만 봐도 이해 가능)
- ✅ 여러 Command의 조합 의미가 명확

---

## 📋 네이밍 규칙

### 클래스명: 복수형 명사 + 타입

```java
// ✅ Good
OrderCommands.vipCustomerOrderCommand()
OrderQueries.pendingOrdersQuery()
OrderResponses.approvedOrderResponse()

PaymentCommands.priorityPaymentCommand()
PaymentQueries.failedPaymentsQuery()
PaymentResponses.completedPaymentResponse()

// ❌ Bad
OrderCommandMother.vipCustomerOrderCommand()  // Mother 접미사 불필요
OrderFactory.vipCustomerOrderCommand()         // Factory는 다른 의미
```

**이유**:
- `OrderCommands`는 "주문 Command들의 집합"을 의미
- Martin Fowler의 Object Mother 패턴 원본 스타일
- 간결하고 자연스러운 네이밍

---

### 메서드명: 비즈니스 시나리오 + 타입 접미사

```java
// ✅ Good - Command
OrderCommands.newOrderCommand()               // 신규 주문
OrderCommands.vipCustomerOrderCommand()       // VIP 고객 주문
OrderCommands.bulkOrderCommand()              // 대량 주문
OrderCommands.cancelPendingOrderCommand()     // 대기 중인 주문 취소

// ✅ Good - Query
OrderQueries.pendingOrdersQuery()             // 대기 중인 주문 검색
OrderQueries.vipCustomerOrdersQuery()         // VIP 고객 주문 검색
OrderQueries.todayOrdersQuery()               // 오늘 주문 검색

// ✅ Good - Response
OrderResponses.approvedOrderResponse()        // 승인된 주문 응답
OrderResponses.vipOrderDetailResponse()       // VIP 주문 상세 응답

// ❌ Bad - 타입 접미사 없음
OrderCommands.vipCustomerOrder()              // ❌ Command 접미사 필수
OrderQueries.pendingOrders()                  // ❌ Query 접미사 필수
OrderResponses.approvedOrder()                // ❌ Response 접미사 필수
```

---

## 🔧 고급 패턴

### 패턴 1: 여러 DTO 조합 시나리오

```java
/**
 * Facade 테스트를 위한 복합 시나리오
 */
public class OrderFacadeScenarios {

    /**
     * 주문 생성 + 결제 + 재고 차감 통합 시나리오
     *
     * <p><strong>비즈니스 흐름</strong>:</p>
     * <ol>
     *   <li>주문 생성 (CreateOrderCommand)</li>
     *   <li>결제 처리 (ProcessPaymentCommand)</li>
     *   <li>재고 차감 (DeductInventoryCommand)</li>
     * </ol>
     */
    public record CreateOrderWithPaymentScenario(
        CreateOrderCommand orderCommand,
        ProcessPaymentCommand paymentCommand,
        DeductInventoryCommand inventoryCommand
    ) {
        public static CreateOrderWithPaymentScenario vipCustomer() {
            CreateOrderCommand orderCommand = OrderCommands.vipCustomerOrderCommand();

            return new CreateOrderWithPaymentScenario(
                orderCommand,
                PaymentCommands.priorityPaymentCommand(orderCommand.customerId()),
                InventoryCommands.deductForOrder(orderCommand.items())
            );
        }

        public static CreateOrderWithPaymentScenario normalCustomer() {
            CreateOrderCommand orderCommand = OrderCommands.newOrderCommand();

            return new CreateOrderWithPaymentScenario(
                orderCommand,
                PaymentCommands.normalPaymentCommand(orderCommand.customerId()),
                InventoryCommands.deductForOrder(orderCommand.items())
            );
        }
    }

    private OrderFacadeScenarios() {
        throw new AssertionError("시나리오 클래스는 인스턴스화할 수 없습니다.");
    }
}

// 사용 예시
@Test
void createOrderWithPayment_VipCustomer_ShouldSucceed() {
    // Given - 복합 시나리오를 하나의 객체로
    var scenario = OrderFacadeScenarios.CreateOrderWithPaymentScenario.vipCustomer();

    // When
    OrderResponse response = orderFacade.createOrderWithPayment(
        scenario.orderCommand(),
        scenario.paymentCommand(),
        scenario.inventoryCommand()
    );

    // Then
    assertThat(response.status()).isEqualTo("APPROVED");
}
```

---

### 패턴 2: 파라미터화된 시나리오

```java
/**
 * 특정 고객의 주문 Command
 */
public static CreateOrderCommand orderCommandForCustomer(Long customerId) {
    return new CreateOrderCommand(
        customerId,
        OrderItemFixture.createMultiple(3),
        String.format("고객 %d의 주문", customerId)
    );
}

/**
 * 특정 금액의 주문 Command
 */
public static CreateOrderCommand orderCommandWithAmount(Long totalAmount) {
    int itemCount = (int) (totalAmount / 10000);
    return new CreateOrderCommand(
        1L,
        OrderItemFixture.createMultiple(itemCount),
        String.format("%d원 주문", totalAmount)
    );
}
```

---

## 📋 체크리스트

### Object Mother 클래스 작성 체크리스트

- [ ] 클래스명은 **복수형 명사 + 타입** (`OrderCommands`, `OrderQueries`, `OrderResponses`)
- [ ] 메서드명은 **비즈니스 시나리오 + 타입 접미사** (`vipCustomerOrderCommand()`)
- [ ] Fixture 재사용 (복잡한 로직 없이 Fixture 조합)
- [ ] Private 생성자로 인스턴스화 방지
- [ ] Javadoc에 **비즈니스 시나리오** 명시
- [ ] `mother/` 패키지에 위치
- [ ] 파라미터화 옵션 제공 (필요 시)

---

## ⚠️ 주의사항

### ❌ 과도한 파라미터화 지양

```java
// ❌ Bad - 파라미터가 너무 많음
public static CreateOrderCommand orderCommand(
    Long customerId,
    List<OrderItem> items,
    String notes,
    String couponCode,
    String shippingAddress
) {
    // 이건 사실상 Builder 패턴...
}

// ✅ Good - 기본 시나리오 + 필요 시 오버로딩
public static CreateOrderCommand newOrderCommand() {
    // 기본값 사용
}

public static CreateOrderCommand orderCommandForCustomer(Long customerId) {
    // 고객만 변경
}

public static CreateOrderCommand vipCustomerOrderCommand() {
    // VIP 고객 특화 시나리오
}
```

---

## 🎓 실전 예제: Facade 테스트

```java
@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private ProcessPaymentUseCase processPaymentUseCase;

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    void createOrderWithPayment_VipCustomer_ShouldSucceedWithPriorityProcessing() {
        // Given - Object Mother로 복잡한 시나리오 표현
        var scenario = OrderFacadeScenarios.CreateOrderWithPaymentScenario.vipCustomer();

        OrderResponse orderResponse = OrderResponses.approvedOrderResponse();
        PaymentResponse paymentResponse = PaymentResponses.completedPaymentResponse();

        when(createOrderUseCase.executeOrderCreation(scenario.orderCommand()))
            .thenReturn(orderResponse);
        when(processPaymentUseCase.executePaymentProcessing(scenario.paymentCommand()))
            .thenReturn(paymentResponse);

        // When
        OrderResponse response = orderFacade.createOrderWithPayment(
            scenario.orderCommand(),
            scenario.paymentCommand()
        );

        // Then
        assertThat(response.status()).isEqualTo("APPROVED");
        verify(createOrderUseCase).executeOrderCreation(scenario.orderCommand());
        verify(processPaymentUseCase).executePaymentProcessing(scenario.paymentCommand());
    }

    @Test
    void searchPendingOrders_ShouldReturnWaitingOrders() {
        // Given - Query Object Mother로 검색 조건 표현
        SearchOrdersQuery query = OrderQueries.pendingOrdersQuery();
        OrderListResponse expectedResponse = OrderResponses.pagedOrderListResponse();

        when(searchOrdersUseCase.queryOrders(query)).thenReturn(expectedResponse);

        // When
        OrderListResponse response = orderFacade.searchOrders(query);

        // Then
        assertThat(response.orders()).hasSize(3);
        assertThat(response.totalCount()).isEqualTo(3);
    }
}
```

---

## 📚 관련 문서

**이전**:
- [03_test-fixture-pattern.md](03_test-fixture-pattern.md) - 기본 DTO 생성

**관련 가이드**:
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md) - DTO 네이밍 규칙
- [UseCase Method Naming](../usecase-design/04_usecase-method-naming.md) - UseCase 메서드명 규칙
- [Facade Usage Guide](../facade/01_facade-usage-guide.md) - Facade 테스트 시나리오

---

**작성자**: Development Team
**최종 수정일**: 2025-11-03
**버전**: 1.0.0
