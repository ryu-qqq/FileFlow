# 헥사고날 아키텍처 + Kent Beck TDD 병렬 개발 시스템 설계

**작성일**: 2025-11-13
**버전**: v1.0
**목적**: Layer별 독립 TDD를 통한 병렬 개발 가능 시스템 구축

---

## 🎯 핵심 개념

### 1. Domain-First + 병렬 전략

```
Phase 1: Domain Layer (독립)
   ↓ Port 인터페이스 확정
Phase 2: Application + Persistence + REST (병렬)
   ↓ 각 Layer 100% 검증
Phase 3: Integration (통합)
```

**왜 Domain-First?**
- Law of Demeter, Tell Don't Ask → Domain 설계가 Port 품질 결정
- Port 인터페이스 = 병렬 개발의 계약(Contract)
- Domain 완성 후 Port 확정 → 변경 최소화

### 2. Port 인터페이스 = 계약(Contract)

**Port In (UseCase)**:
```java
public interface PlaceOrderUseCase {
    OrderResponse execute(PlaceOrderCommand command);
}

public record PlaceOrderCommand(
    Long customerId,
    List<OrderLineRequest> orderLines,
    String deliveryAddress
) {}

public record OrderResponse(
    Long orderId,
    OrderStatus status,
    LocalDateTime createdAt
) {}
```

**Port Out (Persistence)**:
```java
public interface OrderPersistencePort {
    OrderId persist(Order order);
    Optional<Order> findById(OrderId orderId);
}
```

**Port Out (External API)**:
```java
public interface PaymentPort {
    PaymentResult processPayment(PaymentRequest request);
}
```

**핵심**: Port 인터페이스만 정의되면, 각 레이어는 Mock 기반으로 독립 TDD 가능!

---

## 📁 새로운 디렉토리 구조

```
kentback/
├── plan-domain.md           # Domain Layer TDD Plan
├── plan-application.md      # Application Layer TDD Plan
├── plan-persistence.md      # Persistence Layer TDD Plan
├── plan-rest-api.md         # REST API Layer TDD Plan
└── plan-integration.md      # 통합 테스트 TDD Plan

.claude/commands/kb/
├── domain/
│   ├── go.md                # Domain Layer TDD 실행
│   ├── red.md
│   ├── green.md
│   ├── refactor.md
│   └── tidy.md
├── application/
│   ├── go.md                # Application Layer TDD 실행
│   └── ...
├── persistence/
│   ├── go.md                # Persistence Layer TDD 실행
│   └── ...
├── rest-api/
│   ├── go.md                # REST API Layer TDD 실행
│   └── ...
└── integration/
    ├── go.md                # 통합 TDD 실행
    └── ...
```

---

## 🚀 워크플로우 상세

### Phase 1: Domain Layer (2-3일, Developer A)

**1. Jira Story 생성**
```bash
/create-prd Order
/jira-from-prd
→ PROJ-123-domain: Order Domain Layer 개발
```

**2. Domain TDD Plan 생성**
```bash
/jira-task PROJ-123-domain
→ kentback/plan-domain.md 생성
```

**plan-domain.md 예시**:
```markdown
# Order Domain TDD Plan

## RED Phase Tests

### 1. Order Aggregate 생성
- [ ] Test: Order.createNew() 정적 팩토리 메서드
- [ ] Test: OrderId Value Object 생성
- [ ] Test: OrderStatus Enum 정의

### 2. placeOrder() 비즈니스 로직
- [ ] Test: PENDING 상태에서만 주문 가능
- [ ] Test: placeOrder() 성공 시 PLACED 상태 변경
- [ ] Test: placeOrder() 성공 시 OrderPlaced 이벤트 발행
- [ ] Test: 이미 PLACED 상태면 IllegalStateException

### 3. cancelOrder() 불변식
- [ ] Test: PLACED 상태에서만 취소 가능
- [ ] Test: cancelOrder() 성공 시 CANCELLED 상태 변경
- [ ] Test: cancelOrder() 성공 시 OrderCancelled 이벤트 발행
- [ ] Test: CANCELLED 상태면 IllegalStateException

### 4. Law of Demeter 준수
- [ ] Test: getCustomerZipCode() 메서드 (Getter 체이닝 방지)
- [ ] Test: isSeoulAreaOrder() 메서드 (Tell, Don't Ask)

## GREEN Phase Implementation

(RED 테스트 통과 후 자동 진행)

## REFACTOR Phase

- [ ] 중복 제거
- [ ] 메서드 추출
- [ ] 명확한 네이밍

## TIDY Phase

- [ ] Javadoc 추가
- [ ] 주석 정리
- [ ] Import 정리
```

**3. kentback TDD 실행**
```bash
/kb-domain /go

# 자동 실행:
# 1. plan-domain.md에서 첫 번째 미완료 테스트 찾기
# 2. RED: 테스트 작성 (실패 확인)
# 3. GREEN: 최소 구현 (테스트 통과)
# 4. REFACTOR: 구조 개선
# 5. TIDY: 정리
# 6. plan-domain.md 테스트 완료 체크
# 7. git commit (자동 또는 수동)
# 8. CI: Domain 모듈만 검증 (2분)

# 반복 (x10회)
/kb-domain /go
/kb-domain /go
...
```

**4. Port 인터페이스 확정**

Domain 완성 후, Domain Event 기반으로 Port 인터페이스 정의:

```java
// Port In (UseCase)
public interface PlaceOrderUseCase {
    OrderResponse execute(PlaceOrderCommand command);
}

// Port Out (Persistence)
public interface OrderPersistencePort {
    OrderId persist(Order order);
    Optional<Order> findById(OrderId orderId);
}

// Port Out (External API)
public interface PaymentPort {
    PaymentResult processPayment(PaymentRequest request);
}
```

**5. PR 머지**
```bash
git push origin feature/PROJ-123-domain
→ CI: Domain 모듈만 검증 (2분) ✅
→ PR 생성 + 리뷰
→ main 머지
```

---

### Phase 2: 병렬 TDD (3-4일, Developer B/C/D 동시)

**공통 시작점**: Port 인터페이스 확정 (Phase 1 완료 후)

#### Developer B: Application Layer

**1. Jira Story + TDD Plan**
```bash
/jira-task PROJ-123-application
→ kentback/plan-application.md
```

**plan-application.md 예시**:
```markdown
# Order Application TDD Plan

## RED Phase Tests

### 1. PlaceOrderService (UseCase 구현)
- [ ] Test: PlaceOrderCommand 유효성 검증
- [ ] Test: OrderPersistencePort.findById() 호출 (Mock)
- [ ] Test: Order.placeOrder() 호출
- [ ] Test: PaymentPort.processPayment() 호출 (Mock)
- [ ] Test: OrderPersistencePort.persist() 호출 (Mock)
- [ ] Test: OrderResponse 반환

### 2. Transaction 경계
- [ ] Test: @Transactional 적용 확인
- [ ] Test: 외부 API 호출은 트랜잭션 밖 (PaymentPort)

### 3. Assembler (Domain ↔ DTO 변환)
- [ ] Test: PlaceOrderCommand → Order 변환
- [ ] Test: Order → OrderResponse 변환

## GREEN Phase Implementation

(Mock 기반 구현)
```

**2. kentback TDD 실행**
```bash
/kb-application /go

# Mock Port로 TDD:
@Test
void shouldPlaceOrderSuccessfully() {
    // Given
    PlaceOrderCommand command = PlaceOrderCommand.of(...);
    Order order = OrderFixture.create();

    // Mock Port
    when(orderPersistencePort.persist(any())).thenReturn(OrderId.of(1L));
    when(paymentPort.processPayment(any())).thenReturn(PaymentResult.success());

    // When
    OrderResponse response = placeOrderService.execute(command);

    // Then
    assertThat(response.orderId()).isEqualTo(1L);
    verify(orderPersistencePort).persist(any());
}

# 반복
/kb-application /go (x8회)
```

**3. PR 머지**
```bash
git push origin feature/PROJ-123-application
→ CI: Application 모듈만 검증 (2분) ✅
→ PR 머지
```

#### Developer C: Persistence Layer (동시에)

**1. Jira Story + TDD Plan**
```bash
/jira-task PROJ-123-persistence
→ kentback/plan-persistence.md
```

**plan-persistence.md 예시**:
```markdown
# Order Persistence TDD Plan

## RED Phase Tests

### 1. OrderEntity (JPA Entity)
- [ ] Test: Long FK 전략 (userId: Long, NOT @ManyToOne)
- [ ] Test: @UniqueConstraint 적용
- [ ] Test: Audit 필드 (@CreatedDate, @LastModifiedDate)

### 2. OrderCommandAdapter (Port 구현)
- [ ] Test: persist(Order) → OrderEntity 저장
- [ ] Test: Domain → Entity 변환 (Mapper)
- [ ] Test: Entity → Domain 변환 (Mapper)
- [ ] Test: OrderId 반환

### 3. OrderQueryAdapter (Port 구현)
- [ ] Test: findById(OrderId) → Optional<Order>
- [ ] Test: QueryDSL DTO Projection

## GREEN Phase Implementation

(TestContainers 기반 실제 DB 테스트)
```

**2. kentback TDD 실행**
```bash
/kb-persistence /go

# TestContainers 기반 실제 DB 테스트:
@DataJpaTest
@Testcontainers
class OrderCommandAdapterTest {
    @Container
    static PostgreSQLContainer<?> postgres = ...;

    @Test
    void shouldPersistOrder() {
        // Given
        Order order = OrderFixture.create();

        // When
        OrderId orderId = orderCommandAdapter.persist(order);

        // Then
        assertThat(orderId).isNotNull();
        assertThat(orderRepository.findById(orderId.value())).isPresent();
    }
}

# 반복
/kb-persistence /go (x7회)
```

**3. PR 머지**
```bash
git push origin feature/PROJ-123-persistence
→ CI: Persistence 모듈만 검증 (3분) ✅
→ PR 머지
```

#### Developer D: REST API Layer (동시에)

**1. Jira Story + TDD Plan**
```bash
/jira-task PROJ-123-rest-api
→ kentback/plan-rest-api.md
```

**plan-rest-api.md 예시**:
```markdown
# Order REST API TDD Plan

## RED Phase Tests

### 1. OrderController
- [ ] Test: POST /api/orders (MockMvc)
- [ ] Test: PlaceOrderRequest → PlaceOrderCommand 변환
- [ ] Test: PlaceOrderUseCase.execute() 호출 (Mock)
- [ ] Test: OrderResponse → PlaceOrderApiResponse 변환
- [ ] Test: 201 Created 응답

### 2. Exception Handling
- [ ] Test: 400 Bad Request (유효성 실패)
- [ ] Test: 404 Not Found (주문 없음)
- [ ] Test: 500 Internal Server Error

### 3. Mapper
- [ ] Test: PlaceOrderRequest → PlaceOrderCommand
- [ ] Test: OrderResponse → PlaceOrderApiResponse

## GREEN Phase Implementation

(MockMvc + Mock UseCase)
```

**2. kentback TDD 실행**
```bash
/kb-rest-api /go

# MockMvc + Mock UseCase:
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean PlaceOrderUseCase placeOrderUseCase;

    @Test
    void shouldPlaceOrder() throws Exception {
        // Given
        PlaceOrderRequest request = new PlaceOrderRequest(...);
        OrderResponse response = OrderResponse.of(...);

        when(placeOrderUseCase.execute(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1L));
    }
}

# 반복
/kb-rest-api /go (x6회)
```

**3. PR 머지**
```bash
git push origin feature/PROJ-123-rest-api
→ CI: REST API 모듈만 검증 (2분) ✅
→ PR 머지
```

---

### Phase 3: 통합 (1일, Developer A 리드)

**1. Jira Story + TDD Plan**
```bash
/jira-task PROJ-123-integration
→ kentback/plan-integration.md
```

**plan-integration.md 예시**:
```markdown
# Order Integration TDD Plan

## RED Phase Tests

### 1. Mock 제거, 실제 연결
- [ ] Test: OrderController → PlaceOrderService (실제)
- [ ] Test: PlaceOrderService → OrderCommandAdapter (실제)
- [ ] Test: PlaceOrderService → PaymentPort (실제 또는 Stub)

### 2. 통합 시나리오
- [ ] Test: 주문 생성 → 결제 → 저장 (End-to-End)
- [ ] Test: 주문 취소 → 환불 → 저장 (End-to-End)

### 3. 설정 검증
- [ ] Test: @SpringBootTest 전체 컨텍스트 로딩
- [ ] Test: TestContainers DB 연결
- [ ] Test: 트랜잭션 전파 확인

## GREEN Phase Implementation

(실제 통합 테스트)
```

**2. kentback TDD 실행**
```bash
/kb-integration /go

# 실제 통합 테스트:
@SpringBootTest
@Testcontainers
class OrderIntegrationTest {
    @Autowired OrderController orderController;
    @Autowired OrderRepository orderRepository;

    @Test
    void shouldPlaceOrderEndToEnd() {
        // Given
        PlaceOrderRequest request = new PlaceOrderRequest(...);

        // When
        ResponseEntity<PlaceOrderApiResponse> response =
            orderController.placeOrder(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long orderId = response.getBody().orderId();
        OrderEntity entity = orderRepository.findById(orderId).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo("PLACED");
    }
}

# 반복
/kb-integration /go (x3회)
```

**3. PR 머지**
```bash
git push origin feature/PROJ-123-integration
→ CI: 전체 통합 검증 (8분) ✅
→ PR 머지 → 배포
```

---

## 📊 효과 측정

### 개발 시간 비교

| 방식 | Domain | Application | Persistence | REST API | Integration | 총합 |
|------|--------|-------------|-------------|----------|-------------|------|
| **순차** | 3일 | 4일 | 4일 | 3일 | 1일 | **15일** |
| **병렬** | 3일 | 4일 (동시) | 4일 (동시) | 3일 (동시) | 1일 | **8일** |
| **개선율** | - | - | - | - | - | **47% 단축** |

### CI 피드백 비교

| 방식 | Domain | Application | Persistence | REST API | Integration |
|------|--------|-------------|-------------|----------|-------------|
| **순차** | 8분 (전체) | 8분 (전체) | 8분 (전체) | 8분 (전체) | 8분 (전체) |
| **병렬** | 2분 (모듈) | 2분 (모듈) | 3분 (모듈) | 2분 (모듈) | 8분 (전체) |
| **개선율** | 75% | 75% | 62% | 75% | - |

### 팀 협업 효율

| 방식 | 동시 작업 인원 | 총 Man-Days | 효율 |
|------|---------------|-------------|------|
| **순차** | 1명 | 15 man-days | 1x |
| **병렬** | 4명 (Phase 2) | 32 man-days | 2x |

---

## 🛠️ 구현 계획

### 1. Layer별 kentback 커맨드

```
.claude/commands/kb/
├── domain/go.md          # /kb-domain /go
├── application/go.md     # /kb-application /go
├── persistence/go.md     # /kb-persistence /go
├── rest-api/go.md        # /kb-rest-api /go
└── integration/go.md     # /kb-integration /go
```

### 2. TDD Plan 생성 커맨드

```bash
/jira-task-layered PROJ-123
→ kentback/plan-domain.md
→ kentback/plan-application.md
→ kentback/plan-persistence.md
→ kentback/plan-rest-api.md
→ kentback/plan-integration.md
```

### 3. Branch 전략 커맨드

```bash
/create-layer-branches PROJ-123
→ feature/PROJ-123-domain
→ feature/PROJ-123-application
→ feature/PROJ-123-persistence
→ feature/PROJ-123-rest-api
→ feature/PROJ-123-integration
```

---

## 🎓 학습 경로

### Week 1: 개념 이해
1. Domain-First 전략 이해
2. Port 인터페이스 계약 개념
3. Mock 기반 독립 TDD

### Week 2: 실전 적용
1. 실제 Jira 티켓으로 Domain Layer TDD
2. Port 인터페이스 정의 연습
3. Application Layer Mock 기반 TDD

### Week 3: 병렬 개발
1. 팀 3-4명 병렬 개발 실습
2. Layer별 CI 검증 확인
3. 통합 테스트 작성

---

## 📚 참고 문서

- [Kent Beck TDD](kentback_claude.md)
- [Hexagonal Architecture](docs/architecture/HEXAGONAL.md)
- [CI/CD Guide](docs/cicd/README.md)
- [Coding Convention](docs/coding_convention/)

---

**✅ 이 시스템은 현재 인프라(Hook, Cache, CI/CD, Conventions)를 100% 활용합니다!**
