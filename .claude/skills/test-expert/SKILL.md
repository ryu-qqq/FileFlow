---
name: "test-expert"
description: "Spring 테스트 전문가. Unit Test, Integration Test, ArchUnit, Test Fixture, Object Mother 패턴을 활용한 효과적인 테스트 코드를 작성합니다. Domain/Application/REST API 레이어별 테스트 전략을 제공합니다."
---

# Spring Test Expert

테스트 전문가 Skill입니다. Unit Test, Integration Test, ArchUnit을 활용한 효과적인 테스트 전략을 제공합니다.

## 전문 분야

1. **Unit Test**: Domain, Application, REST API 레이어별 단위 테스트
2. **Integration Test**: TestContainers, MockMvc, REST Docs
3. **ArchUnit**: 아키텍처 규칙 자동 검증
4. **Test Fixture**: Test Fixture Builder, Object Mother

## 사용 시점

- Domain/Application/REST API 테스트 작성
- ArchUnit 아키텍처 검증 구현
- Test Fixture 및 테스트 데이터 생성
- TestContainers 통합 테스트

## 핵심 규칙

### 1. Domain Layer Unit Test

**Domain 비즈니스 로직 테스트**:
```java
@DisplayName("Order Domain 테스트")
class OrderTest {

    @Test
    @DisplayName("주문 생성 시 상태는 PLACED여야 한다")
    void testCreateOrder() {
        // Given
        String orderNumber = "ORD-20250104-001";
        Long customerId = 1L;
        List<OrderItem> items = List.of(
            new OrderItem(1L, "Product A", 2, BigDecimal.valueOf(10000))
        );
        Address address = new Address("12345", "Main St", "Seoul", "KR");

        // When
        Order order = new Order(orderNumber, customerId, items, address);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("PLACED 상태에서 confirm() 호출 시 CONFIRMED로 변경")
    void testConfirmOrder() {
        // Given
        Order order = OrderFixture.createOrder();

        // When
        order.confirm();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 confirm() 호출 시 예외 발생")
    void testConfirmOrder_AlreadyConfirmed() {
        // Given
        Order order = OrderFixture.createConfirmedOrder();

        // When & Then
        assertThatThrownBy(() -> order.confirm())
            .isInstanceOf(OrderStatusException.class)
            .hasMessageContaining("PLACED 상태에서만 가능");
    }
}
```

### 2. Application Layer Unit Test

**UseCase Mock 테스트**:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceOrderService 테스트")
class PlaceOrderServiceTest {

    @InjectMocks
    private PlaceOrderService placeOrderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderAssembler orderAssembler;

    @Test
    @DisplayName("주문 생성 성공")
    void testPlaceOrder_Success() {
        // Given
        PlaceOrderCommand command = PlaceOrderCommandFixture.create();
        Customer customer = CustomerFixture.create();
        Order order = OrderFixture.createOrder();
        OrderResult expected = OrderResultFixture.create();

        when(customerRepository.findById(command.customerId()))
            .thenReturn(Optional.of(customer));
        when(orderAssembler.toDomain(command)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderAssembler.toResult(order)).thenReturn(expected);

        // When
        OrderResult result = placeOrderService.execute(command);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("고객이 존재하지 않으면 예외 발생")
    void testPlaceOrder_CustomerNotFound() {
        // Given
        PlaceOrderCommand command = PlaceOrderCommandFixture.create();
        when(customerRepository.findById(command.customerId()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> placeOrderService.execute(command))
            .isInstanceOf(CustomerNotFoundException.class);
    }
}
```

### 3. REST API Integration Test

**MockMvc + REST Docs**:
```java
@WebMvcTest(OrderController.class)
@AutoConfigureRestDocs
@DisplayName("OrderController 통합 테스트")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaceOrderUseCase placeOrderUseCase;

    @MockBean
    private OrderRequestMapper requestMapper;

    @MockBean
    private OrderResponseMapper responseMapper;

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 성공")
    void testPlaceOrder_Success() throws Exception {
        // Given
        OrderRequest request = OrderRequestFixture.create();
        PlaceOrderCommand command = PlaceOrderCommandFixture.create();
        OrderResult result = OrderResultFixture.create();
        OrderResponse response = OrderResponseFixture.create();

        when(requestMapper.toCommand(any())).thenReturn(command);
        when(placeOrderUseCase.execute(command)).thenReturn(result);
        when(responseMapper.toResponse(result)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(response.orderId()))
            .andExpect(jsonPath("$.orderNumber").value(response.orderNumber()))
            .andDo(document("place-order",
                requestFields(
                    fieldWithPath("orderNumber").description("주문 번호"),
                    fieldWithPath("customerId").description("고객 ID"),
                    fieldWithPath("items").description("주문 항목")
                ),
                responseFields(
                    fieldWithPath("orderId").description("주문 ID"),
                    fieldWithPath("orderNumber").description("주문 번호"),
                    fieldWithPath("status").description("주문 상태")
                )
            ));
    }
}
```

### 4. ArchUnit 아키텍처 검증

**Zero-Tolerance Rules**:
```java
@AnalyzeClasses(packages = "com.ryuqq")
@DisplayName("Zero-Tolerance 아키텍처 규칙")
public class ZeroToleranceArchitectureTest {

    @ArchTest
    static final ArchRule lombok_is_prohibited_in_domain_layer =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().haveNameMatching(".*Lombok.*")
            .because("Lombok is absolutely prohibited in Domain layer");

    @ArchTest
    static final ArchRule jpa_relationships_prohibited =
        noFields()
            .should().beAnnotatedWith(ManyToOne.class)
            .orShould().beAnnotatedWith(OneToMany.class)
            .orShould().beAnnotatedWith(OneToOne.class)
            .orShould().beAnnotatedWith(ManyToMany.class)
            .because("JPA relationship annotations are prohibited (Long FK strategy)");

    @ArchTest
    static final ArchRule transactional_only_on_public_methods =
        methods()
            .that().areAnnotatedWith(Transactional.class)
            .should().bePublic()
            .because("@Transactional only works on public methods (Spring Proxy)");
}
```

### 5. Test Fixture Pattern

**Fixture Builder**:
```java
public class OrderFixture {

    public static Order createOrder() {
        return new Order(
            "ORD-20250104-001",
            1L,
            List.of(createOrderItem()),
            createAddress()
        );
    }

    public static Order createConfirmedOrder() {
        Order order = createOrder();
        order.confirm();
        return order;
    }

    public static OrderItem createOrderItem() {
        return new OrderItem(1L, "Product A", 2, BigDecimal.valueOf(10000));
    }

    public static Address createAddress() {
        return new Address("12345", "Main St", "Seoul", "KR");
    }
}
```

### 6. TestContainers 통합 테스트

**MySQL TestContainers**:
```java
@SpringBootTest
@Testcontainers
@DisplayName("Order Repository 통합 테스트")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 저장 및 조회")
    void testSaveAndFind() {
        // Given
        Order order = OrderFixture.createOrder();

        // When
        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo(order.getOrderNumber());
    }
}
```

## 테스트 체크리스트

- [ ] **Given-When-Then**: 모든 테스트는 Given-When-Then 구조
- [ ] **@DisplayName**: 테스트 설명을 한글로 명확히
- [ ] **AssertJ**: assertThat() 사용 (JUnit assertEquals 지양)
- [ ] **Test Fixture**: 테스트 데이터는 Fixture로 생성
- [ ] **Mock 최소화**: 가능하면 Real Object 사용
- [ ] **Integration Test**: @SpringBootTest + TestContainers
- [ ] **ArchUnit**: 아키텍처 규칙 자동 검증

## 추가 리소스

```bash
cat .claude/skills/test-expert/REFERENCE.md
```

## 참고 문서

- `docs/coding_convention/05-testing/`
- `docs/coding_convention/02-domain-layer/testing/`
- `docs/coding_convention/03-application-layer/testing/`
- `docs/coding_convention/01-adapter-rest-api-layer/testing/`
