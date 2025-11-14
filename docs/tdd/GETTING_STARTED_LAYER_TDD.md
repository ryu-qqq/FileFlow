# Layer별 TDD 시작 가이드 (Getting Started)

이 가이드는 **Kent Beck TDD + Hexagonal Architecture + Parallel Development**를 결합한 실전 워크플로우를 설명합니다.

---

## ⚡ 5분 Quick Start

### 1단계: Jira Task 생성 (1분)

```bash
# PRD 작성
/create-prd Order "주문 생성, 취소, 상태 변경 기능"

# Jira Epic + Story 생성
/jira-from-prd

# 출력 예시:
# ✅ Epic 생성: PROJ-100 "Order System"
# ✅ Story 생성:
#    - PROJ-101: Order Domain Layer
#    - PROJ-102: Order Application Layer
#    - PROJ-103: Order Persistence Layer
#    - PROJ-104: Order REST API Layer
#    - PROJ-105: Order Integration Test
```

### 2단계: Domain Layer 개발 (3분)

```bash
# Domain Story 브랜치 생성
/jira-task PROJ-101

# Domain TDD Plan 확인
cat kentback/plan-domain.md

# TDD 실행
/kb-domain /go

# 출력 예시:
# 🔴 RED: OrderTest.shouldCreateNewOrder() 작성 완료
# 🟢 GREEN: Order.createNew() 최소 구현 완료
# 🔵 REFACTOR: 구조 개선 완료
# ✅ 전체 테스트 통과: domain/src/test/java/
```

### 3단계: 병렬 개발 시작 (1분)

```bash
# Application Layer (Developer A)
git checkout -b feature/PROJ-102-application
/jira-task PROJ-102
/kb-application /go

# Persistence Layer (Developer B)
git checkout -b feature/PROJ-103-persistence
/jira-task PROJ-103
/kb-persistence /go

# REST API Layer (Developer C)
git checkout -b feature/PROJ-104-rest-api
/jira-task PROJ-104
/kb-rest-api /go
```

**축하합니다! 이제 3-4명이 동시에 개발할 수 있습니다! 🎉**

---

## 🎯 핵심 개념

### 1. Domain-First + Parallel Strategy

```
┌─────────────────────────────────────────────┐
│ Phase 1: Domain Layer (독립)                │
│ - Pure Java (Spring 의존성 없음)            │
│ - Aggregate, Value Object, Domain Event     │
│ - Port 인터페이스 확정                       │
└─────────────────────────────────────────────┘
           ↓ Port 인터페이스 확정
┌─────────────────────────────────────────────┐
│ Phase 2: Application + Persistence + REST   │
│ (병렬 개발)                                  │
│ - Mock 기반 독립 개발                        │
│ - 각 Layer 100% 테스트 커버리지              │
└─────────────────────────────────────────────┘
           ↓ 각 Layer 100% 검증
┌─────────────────────────────────────────────┐
│ Phase 3: Integration (통합)                 │
│ - Mock 제거 → 실제 구현 연결                 │
│ - End-to-End 테스트                          │
└─────────────────────────────────────────────┘
```

### 2. Port Interface = Contract

Port 인터페이스는 **개발 계약서** 역할을 합니다:

```java
// Port In (UseCase) - REST API ← Application 계약
public interface PlaceOrderUseCase {
    OrderResponse execute(PlaceOrderCommand command);
}

// Port Out (Persistence) - Application → Persistence 계약
public interface OrderPersistencePort {
    OrderId persist(Order order);
    Optional<Order> findById(OrderId orderId);
}

// Port Out (External API) - Application → 외부 API 계약
public interface PaymentApiPort {
    PaymentResult processPayment(PaymentRequest request);
}
```

**핵심**: Port 인터페이스가 확정되면, 각 Layer는 독립적으로 개발 가능!

### 3. Mock 기반 독립 개발

```java
// Application Layer 개발 시 (Persistence 미완성)
class PlaceOrderUseCaseTest {
    @Mock
    private OrderPersistencePort mockPersistencePort;  // ← Mock 사용

    @InjectMocks
    private PlaceOrderUseCase sut;

    @Test
    void shouldPlaceOrder() {
        // Given
        when(mockPersistencePort.persist(any()))
            .thenReturn(new OrderId(1L));

        // When & Then
        OrderResponse response = sut.execute(command);
        assertThat(response.orderId()).isEqualTo(1L);
    }
}
```

**효과**: Persistence Layer 완성을 기다리지 않고 Application Layer 개발 가능!

---

## 📚 실전 예시: Order 기능 개발

### 전체 타임라인 (8일, 3명 병렬)

| Day | Developer A | Developer B | Developer C | Output |
|-----|-------------|-------------|-------------|--------|
| 1 | PRD + Jira (전체) | - | - | 5개 Story |
| 2-3 | Domain Layer | - | - | Port 인터페이스 확정 |
| 4-6 | Application Layer | Persistence Layer | REST API Layer | 병렬 개발 |
| 7 | Integration Test (전체) | - | - | End-to-End 검증 |
| 8 | Release | - | - | Production 배포 |

**기존 순차 방식**: 15일 → **병렬 방식**: 8일 (47% 단축)

---

## 🔧 Phase 1: Domain Layer (Day 2-3)

### 1. Jira Story 브랜치 생성

```bash
# Story: PROJ-101 "Order Domain Layer"
/jira-task PROJ-101

# 자동 실행:
# 1. feature/PROJ-101-domain 브랜치 생성
# 2. kentback/plan-domain.md 생성
# 3. .claude/cache 규칙 주입 (Domain Layer 규칙)
```

### 2. Domain TDD Plan 확인

```markdown
# kentback/plan-domain.md

## RED Phase Tests

### 1. Order Aggregate 생성
- [ ] Test: Order.createNew() 정적 팩토리 메서드
- [ ] Test: OrderId Value Object 생성
- [ ] Test: OrderStatus Enum 정의 (PENDING, PLACED, CONFIRMED, CANCELLED)

### 2. placeOrder() 비즈니스 로직
- [ ] Test: PENDING 상태에서만 주문 가능
- [ ] Test: placeOrder() 성공 시 PLACED 상태 변경
- [ ] Test: placeOrder() 성공 시 OrderPlaced 이벤트 발행
- [ ] Test: 이미 PLACED 상태면 예외 발생

### 3. cancelOrder() 비즈니스 로직
- [ ] Test: PLACED 상태에서만 취소 가능
- [ ] Test: cancelOrder() 성공 시 CANCELLED 상태 변경
- [ ] Test: cancelOrder() 성공 시 OrderCancelled 이벤트 발행
- [ ] Test: CONFIRMED 상태면 취소 불가 예외 발생

### 4. confirmOrder() 비즈니스 로직
- [ ] Test: PLACED 상태에서만 확인 가능
- [ ] Test: confirmOrder() 성공 시 CONFIRMED 상태 변경
- [ ] Test: confirmOrder() 성공 시 OrderConfirmed 이벤트 발행
```

### 3. Kent Beck TDD 실행

```bash
# 첫 번째 테스트 실행
/kb-domain /go

# 출력:
# 📋 Next Test: Order.createNew() 정적 팩토리 메서드
# 🔴 RED Phase 시작...
```

#### RED Phase (실패 테스트 작성)

```java
// domain/src/test/java/com/company/order/OrderTest.java
class OrderTest {
    @Test
    void shouldCreateNewOrder() {
        // Given
        OrderId orderId = new OrderId(1L);
        Long customerId = 100L;

        // When
        Order order = Order.createNew(orderId, customerId);

        // Then
        assertThat(order.getOrderId()).isEqualTo(orderId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
```

**실행 결과**: ❌ 컴파일 에러 (Order 클래스 없음)

#### GREEN Phase (최소 구현)

```java
// domain/src/main/java/com/company/order/Order.java
public class Order {
    private final OrderId orderId;
    private final Long customerId;
    private OrderStatus status;

    private Order(OrderId orderId, Long customerId, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
    }

    public static Order createNew(OrderId orderId, Long customerId) {
        return new Order(orderId, customerId, OrderStatus.PENDING);
    }

    public OrderId getOrderId() { return orderId; }
    public Long getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
}

// domain/src/main/java/com/company/order/OrderId.java
public record OrderId(Long value) {}

// domain/src/main/java/com/company/order/OrderStatus.java
public enum OrderStatus {
    PENDING, PLACED, CONFIRMED, CANCELLED
}
```

**실행 결과**: ✅ 테스트 통과

#### REFACTOR Phase (구조 개선)

```java
// Getter 체이닝 방지 (Law of Demeter)
public class Order {
    // ❌ 이렇게 하지 마세요
    public Customer getCustomer() { return customer; }

    // ✅ Tell, Don't Ask 패턴
    public boolean isOwnedBy(Long customerId) {
        return this.customerId.equals(customerId);
    }

    public boolean canBePlaced() {
        return this.status == OrderStatus.PENDING;
    }
}
```

#### TIDY Phase (정리)

```bash
# 1. 테스트 실행
./gradlew :domain:test

# 2. Checkstyle 검증
./gradlew :domain:checkstyleMain

# 3. 커밋
git add .
git commit -m "feat(domain): Order.createNew() 정적 팩토리 메서드 구현

- Order Aggregate 생성
- OrderId Value Object
- OrderStatus Enum 정의

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"

# 4. CI 실행 (2분)
git push origin feature/PROJ-101-domain
```

### 4. Port 인터페이스 확정

Domain Layer 완성 후 Port 인터페이스 정의:

```java
// domain/src/main/java/com/company/order/port/OrderPersistencePort.java
package com.company.order.port;

public interface OrderPersistencePort {
    OrderId persist(Order order);
    Optional<Order> findById(OrderId orderId);
    List<Order> findByCustomerId(Long customerId);
}
```

**핵심**: 이 인터페이스가 **Application ↔ Persistence 계약**이 됩니다!

### 5. Domain Layer 완료

```bash
# 모든 Domain 테스트 완료 후
/kb-domain /go  # 반복 실행

# 최종 확인
./gradlew :domain:test
# ✅ 15 tests passed

# PR 생성
gh pr create --title "feat(domain): Order Domain Layer 구현" \
  --body "$(cat <<'EOF'
## Summary
- Order Aggregate 구현
- placeOrder(), cancelOrder(), confirmOrder() 비즈니스 로직
- OrderPersistencePort 인터페이스 정의

## Test Coverage
- 15 tests, 100% coverage

🤖 Generated with Claude Code
EOF
)"
```

---

## 🔧 Phase 2: 병렬 개발 (Day 4-6)

이제 **3명이 동시에** 다른 Layer를 개발합니다!

### Developer A: Application Layer

```bash
# 1. Application Story 브랜치
git checkout main
git pull origin main  # Domain PR 머지됨
/jira-task PROJ-102

# 2. Application TDD Plan 확인
cat kentback/plan-application.md

# 3. TDD 실행 (Mock 사용)
/kb-application /go
```

#### Application Layer TDD Plan

```markdown
# kentback/plan-application.md

## RED Phase Tests

### 1. PlaceOrderUseCase
- [ ] Test: PlaceOrderCommand 생성
- [ ] Test: PlaceOrderUseCase.execute() 성공 케이스
- [ ] Test: OrderResponse DTO 매핑
- [ ] Test: 주문 저장 후 OrderId 반환

### 2. CancelOrderUseCase
- [ ] Test: CancelOrderCommand 생성
- [ ] Test: CancelOrderUseCase.execute() 성공 케이스
- [ ] Test: PLACED 상태가 아니면 예외 발생
```

#### Application Layer Mock 테스트

```java
// application/src/test/java/com/company/order/usecase/PlaceOrderUseCaseTest.java
class PlaceOrderUseCaseTest {
    @Mock
    private OrderPersistencePort mockPersistencePort;  // ← Persistence Mock

    @InjectMocks
    private PlaceOrderUseCase sut;

    @Test
    void shouldPlaceOrder() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(100L, List.of());
        Order expectedOrder = OrderDomainFixture.createPending();

        when(mockPersistencePort.persist(any()))
            .thenReturn(new OrderId(1L));

        // When
        OrderResponse response = sut.execute(command);

        // Then
        assertThat(response.orderId()).isEqualTo(1L);
        verify(mockPersistencePort).persist(any());
    }
}
```

**핵심**: Persistence Layer 미완성이어도 개발 가능!

### Developer B: Persistence Layer

```bash
# 1. Persistence Story 브랜치
git checkout main
git pull origin main  # Domain PR 머지됨
/jira-task PROJ-103

# 2. Persistence TDD Plan 확인
cat kentback/plan-persistence.md

# 3. TDD 실행 (TestContainers)
/kb-persistence /go
```

#### Persistence Layer TDD Plan

```markdown
# kentback/plan-persistence.md

## RED Phase Tests

### 1. OrderJpaEntity 매핑
- [ ] Test: Order → OrderJpaEntity 변환
- [ ] Test: OrderJpaEntity → Order 변환
- [ ] Test: OrderId FK 전략 (Long orderId)

### 2. OrderPersistenceAdapter
- [ ] Test: persist() 성공 케이스
- [ ] Test: findById() 존재하는 경우
- [ ] Test: findById() 존재하지 않는 경우
- [ ] Test: findByCustomerId() 여러 주문 조회
```

#### Persistence Layer TestContainers 테스트

```java
// persistence/src/test/java/com/company/order/adapter/OrderPersistenceAdapterTest.java
@DataJpaTest
@Testcontainers
class OrderPersistenceAdapterTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private OrderJpaRepository jpaRepository;

    private OrderPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderPersistenceAdapter(jpaRepository);
    }

    @Test
    void shouldPersistOrder() {
        // Given
        Order order = OrderDomainFixture.createPending();

        // When
        OrderId savedId = adapter.persist(order);

        // Then
        assertThat(savedId).isNotNull();
        Optional<Order> found = adapter.findById(savedId);
        assertThat(found).isPresent();
    }
}
```

**핵심**: 실제 MySQL 컨테이너로 통합 테스트!

### Developer C: REST API Layer

```bash
# 1. REST API Story 브랜치
git checkout main
git pull origin main  # Domain PR 머지됨
/jira-task PROJ-104

# 2. REST API TDD Plan 확인
cat kentback/plan-rest-api.md

# 3. TDD 실행 (MockMvc + Mock UseCase)
/kb-rest-api /go
```

#### REST API Layer TDD Plan

```markdown
# kentback/plan-rest-api.md

## RED Phase Tests

### 1. OrderApiController
- [ ] Test: POST /api/orders 성공 케이스
- [ ] Test: PlaceOrderApiRequest → PlaceOrderCommand 변환
- [ ] Test: OrderResponse → PlaceOrderApiResponse 변환
- [ ] Test: 400 Bad Request (Validation 실패)

### 2. OrderQueryApiController
- [ ] Test: GET /api/orders/{orderId} 성공 케이스
- [ ] Test: 404 Not Found (존재하지 않는 주문)
```

#### REST API Layer MockMvc 테스트

```java
// rest-api/src/test/java/com/company/order/controller/OrderApiControllerTest.java
@WebMvcTest(OrderApiController.class)
class OrderApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaceOrderUseCase mockPlaceOrderUseCase;  // ← UseCase Mock

    @Test
    void shouldPlaceOrder() throws Exception {
        // Given
        PlaceOrderApiRequest request = new PlaceOrderApiRequest(100L, List.of());
        OrderResponse expectedResponse = new OrderResponse(new OrderId(1L), ...);

        when(mockPlaceOrderUseCase.execute(any()))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1));
    }
}
```

**핵심**: Application Layer 미완성이어도 개발 가능!

### 병렬 개발 타임라인

| Time | Developer A | Developer B | Developer C |
|------|-------------|-------------|-------------|
| 09:00 | Application TDD 시작 | Persistence TDD 시작 | REST API TDD 시작 |
| 10:00 | Mock 테스트 작성 | TestContainers 설정 | MockMvc 테스트 작성 |
| 11:00 | UseCase 구현 | Entity 매핑 | Controller 구현 |
| 12:00 | CI 통과 (2분) ✅ | CI 통과 (3분) ✅ | CI 통과 (2분) ✅ |
| 14:00 | PR 생성 | PR 생성 | PR 생성 |

**핵심**: **3명이 동시에** 개발하여 **하루 만에** 3개 Layer 완성!

---

## 🔧 Phase 3: Integration (Day 7)

이제 **Mock을 제거**하고 **실제 구현을 연결**합니다.

### 1. Integration Story 브랜치

```bash
# 모든 PR 머지 후
git checkout main
git pull origin main

# Integration Story
/jira-task PROJ-105
```

### 2. Integration TDD Plan

```markdown
# kentback/plan-integration.md

## RED Phase Tests

### 1. End-to-End 주문 생성 플로우
- [ ] Test: POST /api/orders → DB 저장 → 조회 성공
- [ ] Test: 주문 상태 변경 플로우 (PENDING → PLACED → CONFIRMED)
- [ ] Test: 주문 취소 플로우 (PLACED → CANCELLED)

### 2. 동시성 테스트
- [ ] Test: 동일 주문 동시 취소 시도 (Optimistic Lock)
- [ ] Test: 대량 주문 생성 (성능 테스트)
```

### 3. Integration Test 실행

```java
// bootstrap/src/test/java/com/company/order/OrderIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteOrderLifecycle() {
        // Given
        PlaceOrderApiRequest request = new PlaceOrderApiRequest(100L, List.of());

        // When: 주문 생성
        ResponseEntity<PlaceOrderApiResponse> createResponse =
            restTemplate.postForEntity("/api/orders", request, PlaceOrderApiResponse.class);

        // Then: 주문 생성 성공
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long orderId = createResponse.getBody().orderId();

        // When: 주문 조회
        ResponseEntity<OrderDetailApiResponse> getResponse =
            restTemplate.getForEntity("/api/orders/" + orderId, OrderDetailApiResponse.class);

        // Then: 주문 조회 성공
        assertThat(getResponse.getBody().status()).isEqualTo("PENDING");

        // When: 주문 확인
        restTemplate.postForEntity("/api/orders/" + orderId + "/confirm", null, Void.class);

        // Then: 주문 상태 CONFIRMED
        OrderDetailApiResponse confirmed =
            restTemplate.getForObject("/api/orders/" + orderId, OrderDetailApiResponse.class);
        assertThat(confirmed.status()).isEqualTo("CONFIRMED");
    }
}
```

### 4. 전체 빌드 검증

```bash
# 전체 빌드 + 테스트 (8분)
./gradlew clean build

# 출력:
# :domain:test - 15 tests ✅
# :application:test - 12 tests ✅
# :persistence:test - 10 tests ✅
# :rest-api:test - 8 tests ✅
# :bootstrap:test - 5 tests ✅
#
# BUILD SUCCESSFUL in 8m 23s
```

### 5. 최종 PR 생성

```bash
gh pr create --title "feat(order): Order 기능 통합 완료" \
  --body "$(cat <<'EOF'
## Summary
- End-to-End 주문 생성/취소/확인 플로우
- 전체 Layer 통합 검증

## Test Coverage
- Total: 50 tests, 100% coverage
- Integration: 5 E2E tests

## Performance
- 개발 기간: 8일 (기존 15일 대비 47% 단축)
- CI 피드백: Layer별 2-3분 (기존 8분 대비 75% 개선)

🤖 Generated with Claude Code
EOF
)"
```

---

## ❓ FAQ & Troubleshooting

### Q1: Port 인터페이스가 자주 변경되면 어떻게 하나요?

**A1**: Domain-First 전략으로 Port 인터페이스를 **먼저 확정**합니다.

- Domain Layer 개발 시 Port 인터페이스를 **신중하게** 설계
- Domain Event 기반으로 Port 인터페이스 설계 (변경 최소화)
- Port 인터페이스 변경 시 PR에 명확히 표시

```java
// 나쁜 예 (자주 변경됨)
public interface OrderPersistencePort {
    void save(Order order);  // ← 반환 타입이 없어서 나중에 변경 필요
}

// 좋은 예 (안정적)
public interface OrderPersistencePort {
    OrderId persist(Order order);  // ← 처음부터 OrderId 반환
    Optional<Order> findById(OrderId orderId);
}
```

### Q2: Mock 테스트가 실제 구현과 다르면 어떻게 하나요?

**A2**: Integration Test에서 **반드시** 검증합니다.

- Phase 2: Mock 기반 Unit Test (각 Layer 독립)
- Phase 3: Integration Test (실제 구현 검증)
- Mock과 실제 동작이 다르면 **Integration Test 실패**

```java
// Application Layer Mock 테스트
when(mockPersistencePort.persist(any()))
    .thenReturn(new OrderId(1L));  // ← Mock 동작

// Integration Test (실제 구현 검증)
@Test
void shouldPersistOrderInRealDatabase() {
    // Given
    Order order = OrderDomainFixture.createPending();

    // When
    OrderId savedId = realPersistenceAdapter.persist(order);  // ← 실제 DB 저장

    // Then
    assertThat(savedId).isNotNull();  // ← Mock과 다르면 실패
}
```

### Q3: Layer별 CI가 실패하면 어떻게 하나요?

**A3**: **즉시** 수정 후 다시 커밋합니다.

- CI 피드백: 2-3분 (빠른 피드백)
- 실패 시 다른 Layer 개발 중단하지 않음
- 각 Layer는 독립적이므로 영향 최소화

```bash
# Domain Layer CI 실패
git commit -m "fix(domain): OrderStatus Enum 오타 수정"
git push origin feature/PROJ-101-domain
# → 2분 후 CI 통과 ✅

# Application Layer는 계속 개발 가능
# (Domain PR 머지 후 rebase)
```

### Q4: Integration Test에서 실패하면 어떻게 하나요?

**A4**: **해당 Layer로 돌아가서** 수정합니다.

- Integration Test 실패 = 계약(Port) 불일치
- 해당 Layer PR을 수정하여 다시 머지
- Integration Test 재실행

```bash
# Integration Test 실패: Persistence Layer 문제
git checkout feature/PROJ-103-persistence
# 수정 후
git commit -m "fix(persistence): OrderId FK 매핑 수정"
git push origin feature/PROJ-103-persistence
# PR 머지 후 Integration 재실행
```

### Q5: 3명이 없으면 병렬 개발이 불가능한가요?

**A5**: **아니요**. 혼자서도 병렬 개발 가능합니다.

- 한 사람이 Application → Persistence → REST 순서로 개발
- 각 Layer는 **Mock 기반**이므로 순차 개발도 빠름
- CI 피드백 (2-3분)이 빠르므로 효율적

```bash
# 혼자 개발하는 경우
Day 1: PRD + Jira
Day 2-3: Domain Layer
Day 4: Application Layer (Mock 사용)
Day 5: Persistence Layer (TestContainers)
Day 6: REST API Layer (MockMvc)
Day 7: Integration Test

# 총 7일 (기존 15일 대비 53% 단축)
```

---

## 📖 참고 문서

- [Layer별 TDD 설계 문서](./LAYER_PARALLEL_TDD_DESIGN.md) - 전체 아키텍처 설계
- [Kent Beck TDD 가이드](../../.claude/kentback_claude.md) - Kent Beck TDD 원칙
- [Zero-Tolerance 규칙](../coding_convention/README.md) - 코딩 컨벤션
- [Dynamic Hooks 가이드](../DYNAMIC_HOOKS_GUIDE.md) - 자동화 시스템

---

## 🎯 다음 단계

1. **실전 테스트**: 실제 Jira 티켓으로 워크플로우 검증
2. **Layer별 커맨드 구현**: `/kb-domain /go`, `/kb-application /go` 등
3. **TDD Plan 자동 생성**: `/jira-task-layered PROJ-123` 커맨드

**축하합니다! 이제 Layer별 TDD를 시작할 준비가 되었습니다! 🎉**
