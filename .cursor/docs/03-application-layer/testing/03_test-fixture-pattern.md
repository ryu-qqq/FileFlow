# Application Layer Test Fixture 패턴

**목적**: Application Layer DTO(Command/Query/Response)의 테스트 생성을 간소화

**위치**: `application/src/testFixtures/java/com/company/application/{context}/fixture/`

**관련 문서**:
- [Object Mother 패턴](04_object-mother-pattern.md) - 비즈니스 시나리오 표현
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md) - DTO 네이밍 규칙
- [ArchUnit Rules](08_archunit-rules.md) - Application Layer 규칙 검증

---

## 📌 핵심 원칙

### Fixture vs Object Mother

Application Layer에서는 **2가지 테스트 객체 생성 패턴**을 사용합니다:

| 패턴 | 목적 | 생성 방법 | 예시 | 사용 시기 |
|------|------|----------|------|----------|
| **Fixture** | 기본 DTO 생성 | `create()` | `CreateOrderCommandFixture.create()` | 단위 테스트, 단순 데이터 |
| **Object Mother** | 비즈니스 시나리오 | `pendingOrderCommand()` | `OrderCommands.pendingOrderCommand()` | 통합 테스트, 복잡한 시나리오 |

**선택 기준**:
- ✅ **Fixture**: 특정 필드만 설정, 비즈니스 맥락 불필요
- ✅ **Object Mother**: 여러 DTO 조합, 비즈니스 의미 명확히 표현

---

## ✅ Fixture 패턴 (Data-Centric)

### 사용 시기

- **단순 DTO 준비**: Command/Query/Response 기본 값 설정
- **Service 단위 테스트**: 특정 UseCase만 검증
- **Controller 테스트**: API 요청/응답 검증
- **빠른 테스트 작성**: Given 단계를 최소화

---

## 🏗️ Fixture 클래스 작성

### Command DTO Fixture

```java
package com.company.application.order.fixture;

import com.company.application.order.dto.command.CreateOrderCommand;
import com.company.application.order.dto.command.CreateOrderCommand.OrderItem;

/**
 * CreateOrderCommand Test Fixture
 *
 * <p>CreateOrderCommand DTO의 기본 데이터를 생성하는 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * CreateOrderCommand command = CreateOrderCommandFixture.create();
 * CreateOrderCommand command = CreateOrderCommandFixture.createWithCustomer(999L);
 * CreateOrderCommand command = CreateOrderCommandFixture.createWithItems(items);
 * }</pre>
 *
 * <h3>복잡한 시나리오:</h3>
 * <p>복잡한 비즈니스 시나리오는 {@link OrderCommands} Object Mother를 사용하세요.</p>
 *
 * @see OrderCommands Object Mother 패턴 (비즈니스 시나리오용)
 * @author development-team
 * @since 1.0.0
 */
public class CreateOrderCommandFixture {

    /**
     * 기본값으로 CreateOrderCommand 생성
     */
    public static CreateOrderCommand create() {
        return new CreateOrderCommand(
            1L,  // customerId
            createDefaultItems(),
            "테스트 주문"
        );
    }

    /**
     * 특정 고객으로 CreateOrderCommand 생성
     */
    public static CreateOrderCommand createWithCustomer(Long customerId) {
        return new CreateOrderCommand(
            customerId,
            createDefaultItems(),
            "테스트 주문"
        );
    }

    /**
     * 특정 상품 목록으로 CreateOrderCommand 생성
     */
    public static CreateOrderCommand createWithItems(List<OrderItem> items) {
        return new CreateOrderCommand(
            1L,
            items,
            "테스트 주문"
        );
    }

    /**
     * 단일 상품으로 CreateOrderCommand 생성
     */
    public static CreateOrderCommand createWithSingleItem(
        Long productId,
        Integer quantity,
        Long unitPrice
    ) {
        return new CreateOrderCommand(
            1L,
            List.of(new OrderItem(productId, quantity, unitPrice)),
            "테스트 주문"
        );
    }

    /**
     * 기본 상품 목록 생성
     */
    private static List<OrderItem> createDefaultItems() {
        return List.of(
            new OrderItem(101L, 2, 10000L),
            new OrderItem(102L, 1, 20000L)
        );
    }

    // Private 생성자 - 인스턴스화 방지
    private CreateOrderCommandFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### Query DTO Fixture

```java
package com.company.application.order.fixture;

import com.company.application.order.dto.query.GetOrderQuery;
import com.company.application.order.dto.query.FindOrdersByCustomerQuery;
import com.company.application.order.dto.query.SearchOrdersQuery;

/**
 * Order Query DTOs Test Fixture
 *
 * <p>Query DTO의 기본 데이터를 생성하는 Factory 클래스입니다.</p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class OrderQueryFixture {

    /**
     * GetOrderQuery 생성 (기본 ID = 1L)
     */
    public static GetOrderQuery createGetOrderQuery() {
        return new GetOrderQuery(1L);
    }

    /**
     * GetOrderQuery 생성 (특정 ID)
     */
    public static GetOrderQuery createGetOrderQuery(Long orderId) {
        return new GetOrderQuery(orderId);
    }

    /**
     * FindOrdersByCustomerQuery 생성 (기본 Customer ID = 1L)
     */
    public static FindOrdersByCustomerQuery createFindByCustomerQuery() {
        return new FindOrdersByCustomerQuery(1L);
    }

    /**
     * FindOrdersByCustomerQuery 생성 (특정 Customer ID)
     */
    public static FindOrdersByCustomerQuery createFindByCustomerQuery(Long customerId) {
        return new FindOrdersByCustomerQuery(customerId);
    }

    /**
     * SearchOrdersQuery 생성 (기본 검색 조건)
     */
    public static SearchOrdersQuery createSearchQuery() {
        return SearchOrdersQuery.builder()
            .customerId(null)
            .status(null)
            .startDate(null)
            .endDate(null)
            .page(0)
            .size(20)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }

    /**
     * SearchOrdersQuery 생성 (특정 고객)
     */
    public static SearchOrdersQuery createSearchQueryForCustomer(Long customerId) {
        return SearchOrdersQuery.builder()
            .customerId(customerId)
            .page(0)
            .size(20)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }

    /**
     * SearchOrdersQuery 생성 (특정 상태)
     */
    public static SearchOrdersQuery createSearchQueryForStatus(String status) {
        return SearchOrdersQuery.builder()
            .status(status)
            .page(0)
            .size(20)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }

    // Private 생성자 - 인스턴스화 방지
    private OrderQueryFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### Response DTO Fixture

```java
package com.company.application.order.fixture;

import com.company.application.order.dto.response.OrderResponse;
import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.dto.response.OrderListResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response DTOs Test Fixture
 *
 * <p>Response DTO의 기본 데이터를 생성하는 Factory 클래스입니다.</p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class OrderResponseFixture {

    /**
     * OrderResponse 생성 (기본값)
     */
    public static OrderResponse create() {
        return new OrderResponse(
            1L,
            "PENDING",
            30000L,
            LocalDateTime.now()
        );
    }

    /**
     * OrderResponse 생성 (특정 ID)
     */
    public static OrderResponse createWithId(Long orderId) {
        return new OrderResponse(
            orderId,
            "PENDING",
            30000L,
            LocalDateTime.now()
        );
    }

    /**
     * OrderResponse 생성 (특정 상태)
     */
    public static OrderResponse createWithStatus(String status) {
        return new OrderResponse(
            1L,
            status,
            30000L,
            LocalDateTime.now()
        );
    }

    /**
     * OrderDetailResponse 생성 (기본값)
     */
    public static OrderDetailResponse createDetail() {
        return new OrderDetailResponse(
            1L,
            new OrderDetailResponse.CustomerInfo(1L, "홍길동", "hong@example.com"),
            List.of(
                new OrderDetailResponse.LineItem(101L, "상품1", 2, 10000L, 20000L),
                new OrderDetailResponse.LineItem(102L, "상품2", 1, 10000L, 10000L)
            ),
            30000L,
            "PENDING",
            LocalDateTime.now()
        );
    }

    /**
     * OrderListResponse 생성 (기본값)
     */
    public static OrderListResponse createList() {
        return new OrderListResponse(
            List.of(
                create(),
                createWithId(2L),
                createWithId(3L)
            ),
            3
        );
    }

    /**
     * OrderListResponse 생성 (빈 목록)
     */
    public static OrderListResponse createEmptyList() {
        return new OrderListResponse(List.of(), 0);
    }

    // Private 생성자 - 인스턴스화 방지
    private OrderResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

## 🎯 Fixture 사용 예시

### Service 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderAssembler orderAssembler;

    @Mock
    private SaveOrderPort saveOrderPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    void executeOrderCreation_WithValidCommand_ShouldReturnOrderResponse() {
        // Given - Fixture로 기본 Command 생성
        CreateOrderCommand command = CreateOrderCommandFixture.create();

        Order order = OrderFixture.create();
        Order savedOrder = OrderFixture.createWithId(1L);
        OrderResponse expectedResponse = OrderResponseFixture.createWithId(1L);

        when(orderAssembler.toDomain(command)).thenReturn(order);
        when(saveOrderPort.save(order)).thenReturn(savedOrder);
        when(orderAssembler.toResponse(savedOrder)).thenReturn(expectedResponse);

        // When
        OrderResponse response = createOrderService.executeOrderCreation(command);

        // Then
        assertThat(response).isEqualTo(expectedResponse);
        assertThat(response.orderId()).isEqualTo(1L);
    }

    @Test
    void executeOrderCreation_WithCustomerId_ShouldCreateOrderForCustomer() {
        // Given - 특정 고객으로 Command 생성
        Long customerId = 999L;
        CreateOrderCommand command = CreateOrderCommandFixture.createWithCustomer(customerId);

        // When & Then
        // 테스트 로직...
    }
}
```

---

### Controller 테스트

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private GetOrderUseCase getOrderUseCase;

    @MockBean
    private OrderApiMapper orderApiMapper;

    @Test
    void createOrder_WithValidRequest_ShouldReturn200() throws Exception {
        // Given - Fixture로 DTO 생성
        CreateOrderCommand command = CreateOrderCommandFixture.create();
        OrderResponse response = OrderResponseFixture.createWithId(1L);

        when(orderApiMapper.toCommand(any())).thenReturn(command);
        when(createOrderUseCase.executeOrderCreation(command)).thenReturn(response);
        when(orderApiMapper.toApiResponse(response)).thenReturn(
            new OrderApiResponse(1L, "PENDING", 30000L)
        );

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "customerId": 1,
                        "items": [
                            {"productId": 101, "quantity": 2, "unitPrice": 10000}
                        ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_WithValidId_ShouldReturn200() throws Exception {
        // Given - Fixture로 Query 및 Response 생성
        GetOrderQuery query = OrderQueryFixture.createGetOrderQuery(1L);
        OrderDetailResponse response = OrderResponseFixture.createDetail();

        when(orderApiMapper.toQuery(1L)).thenReturn(query);
        when(getOrderUseCase.queryOrderById(query)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
```

---

## ⚠️ Fixture 사용 시 주의사항

### ❌ Bad - 복잡한 비즈니스 시나리오를 Fixture로 표현

```java
// ❌ Bad - 여러 Command를 조합하여 복잡한 시나리오 표현
@Test
void complexScenario_ShouldWork() {
    // Given - 여러 단계의 Command 생성 (비즈니스 의미 불명확)
    CreateOrderCommand createCommand = CreateOrderCommandFixture.create();
    ApproveOrderCommand approveCommand = ApproveOrderCommandFixture.create();
    ShipOrderCommand shipCommand = ShipOrderCommandFixture.create();

    // 이게 무슨 시나리오인지 불명확
    // When & Then...
}
```

**문제점**:
- ❌ 여러 Command를 조합한 비즈니스 흐름이 불명확
- ❌ 테스트만 봐도 무엇을 검증하는지 알 수 없음
- ❌ 유지보수 어려움

---

### ✅ Good - 단순 데이터 준비에만 Fixture 사용

```java
// ✅ Good - 단순한 데이터 준비
@Test
void executeOrderCreation_WithValidCommand_ShouldReturnOrderResponse() {
    // Given - 단순한 Command 생성
    CreateOrderCommand command = CreateOrderCommandFixture.create();

    // When
    OrderResponse response = createOrderUseCase.executeOrderCreation(command);

    // Then
    assertThat(response.orderId()).isNotNull();
}
```

**복잡한 시나리오는 Object Mother 사용!**
```java
// ✅ Good - Object Mother 사용
@Test
void shipOrder_WhenOrderIsApproved_ShouldTransitionToShipped() {
    // Given - 비즈니스 의미 명확 ("승인된 주문 Command")
    ShipOrderCommand command = OrderCommands.shipApprovedOrderCommand();

    // When & Then...
}
```

**참고**: [04_object-mother-pattern.md](04_object-mother-pattern.md)

---

## 📋 네이밍 규칙

### 클래스명: `*Fixture`

```java
// ✅ 올바른 네이밍
CreateOrderCommandFixture.java
GetOrderQueryFixture.java
OrderResponseFixture.java
OrderQueryFixture.java          // 여러 Query DTO 포함 가능

// ❌ 잘못된 네이밍
CreateOrderCommandFactory.java  // Factory는 금지
CreateOrderCommandBuilder.java  // Builder는 금지
CreateOrderCommandTestData.java // TestData는 금지
```

---

### 메서드명: `create*()`

```java
// ✅ 올바른 메서드명
create()                    // 기본값으로 생성
createWithCustomer(Long)    // 특정 값 지정
createWithItems(List)       // 특정 컬렉션 지정
createWithStatus(String)    // 상태 지정

// ❌ 잘못된 메서드명
build()                     // build는 금지
of()                        // of는 Record 생성자와 혼동
command()                   // 타입명만 사용 금지
getCommand()                // get 접두사 금지
```

---

## 🔧 고급 패턴

### 패턴 1: Nested Record Fixture

```java
/**
 * CreateOrderCommand의 Nested Record(OrderItem) Fixture
 */
public class OrderItemFixture {

    public static CreateOrderCommand.OrderItem create() {
        return new CreateOrderCommand.OrderItem(101L, 1, 10000L);
    }

    public static CreateOrderCommand.OrderItem createWithProduct(Long productId) {
        return new CreateOrderCommand.OrderItem(productId, 1, 10000L);
    }

    public static CreateOrderCommand.OrderItem createWithQuantity(Integer quantity) {
        return new CreateOrderCommand.OrderItem(101L, quantity, 10000L);
    }

    public static List<CreateOrderCommand.OrderItem> createMultiple(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> new CreateOrderCommand.OrderItem(100L + i, 1, 10000L))
            .toList();
    }

    private OrderItemFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### 패턴 2: 여러 DTO를 포함하는 통합 Fixture

```java
/**
 * Order 관련 모든 DTO Fixture를 포함하는 통합 Fixture
 *
 * <p>간단한 테스트에서 import를 줄이기 위해 사용</p>
 */
public class OrderFixtures {

    /**
     * Command Fixtures
     */
    public static class Commands {
        public static CreateOrderCommand createOrder() {
            return CreateOrderCommandFixture.create();
        }

        public static CancelOrderCommand cancelOrder() {
            return CancelOrderCommandFixture.create();
        }
    }

    /**
     * Query Fixtures
     */
    public static class Queries {
        public static GetOrderQuery getOrder() {
            return OrderQueryFixture.createGetOrderQuery();
        }

        public static SearchOrdersQuery searchOrders() {
            return OrderQueryFixture.createSearchQuery();
        }
    }

    /**
     * Response Fixtures
     */
    public static class Responses {
        public static OrderResponse order() {
            return OrderResponseFixture.create();
        }

        public static OrderDetailResponse orderDetail() {
            return OrderResponseFixture.createDetail();
        }
    }

    private OrderFixtures() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}

// 사용 예시
OrderResponse response = OrderFixtures.Responses.order();
CreateOrderCommand command = OrderFixtures.Commands.createOrder();
```

---

## 📋 체크리스트

### Fixture 클래스 작성 체크리스트

- [ ] 클래스명에 `Fixture` 접미사 사용
- [ ] `testFixtures/java/.../fixture/` 패키지에 위치
- [ ] 모든 메서드는 `static`으로 선언
- [ ] 기본 생성 메서드 `create()` 제공
- [ ] 커스터마이징 메서드 `createWith*()` 제공
- [ ] Private 생성자로 인스턴스화 방지
- [ ] Javadoc에 사용 예시 및 Object Mother 참조 포함
- [ ] ⚠️ 복잡한 비즈니스 시나리오는 Object Mother 사용

---

## 📚 관련 문서

**다음 단계**:
- [04_object-mother-pattern.md](04_object-mother-pattern.md) - 비즈니스 시나리오 표현

**관련 가이드**:
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md) - DTO 네이밍 규칙
- [Application Package Guide](../package-guide/01_application_package_guide.md) - 전체 패키지 구조
- [ArchUnit Rules](08_archunit-rules.md) - Application Layer 규칙 검증

---

**작성자**: Development Team
**최종 수정일**: 2025-11-03
**버전**: 1.0.0
