# Application Layer 규칙 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Application Layer 핵심 요약본
> **상세 규칙**: Hook이 자동으로 20개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## 🚨 Zero-Tolerance (절대 금지)

- ❌ **Lombok 사용**: `@RequiredArgsConstructor`, `@Data` 등 모두 금지
- ❌ **`@Transactional` 내 외부 API 호출**: RestTemplate, WebClient, Feign 등 ⭐
- ❌ **Private/Final 메서드에 `@Transactional`**: Spring Proxy 제약사항 위반
- ❌ **같은 클래스 내부 호출**: `this.method()` (Proxy 우회)
- ❌ **UseCase에 비즈니스 로직**: Domain으로 위임 (Application은 조율만)

---

## ✅ 필수 규칙

### 1️⃣ UseCase 설계 (Single Responsibility)
- ✅ **Command UseCase**: 상태 변경 (Write) - `PlaceOrderUseCase`
- ✅ **Query UseCase**: 상태 조회 (Read) - `GetOrderDetailQuery`
- ✅ **Port Interface**: `port.in` 패키지에 UseCase 인터페이스
- ✅ **Service 구현**: `service` 패키지에 구현체

### 2️⃣ Assembler 패턴 (DTO ↔ Domain 변환)
- ✅ **Assembler Responsibility**: Domain ↔ DTO 변환만 담당
- ✅ **UseCase Inner DTO**: UseCase 내부에서만 사용하는 DTO
- ✅ **Static Factory Method**: `OrderAssembler.toCommand()`

### 3️⃣ Transaction 관리 (핵심!)
- ✅ **Transaction Boundaries**: UseCase 메서드 = 트랜잭션 경계 ⭐
- ✅ **Spring Proxy Limitations**: Public 메서드만 `@Transactional`
- ✅ **Best Practices**:
  - 트랜잭션은 짧게 유지
  - 외부 API 호출은 트랜잭션 밖에서
  - **Transactional Outbox Pattern 사용** (Pattern B 권장)

### 4️⃣ Facade 패턴 (복잡한 워크플로우)
- ✅ **Facade Usage**: 여러 UseCase 조율
- ✅ **Transaction 조율**: 여러 UseCase를 하나의 트랜잭션으로
- ✅ **Controller 의존성 감소**: Controller → Facade

### 5️⃣ Component 패턴 (공통 로직)
- ✅ **횡단 관심사**: 여러 UseCase가 공통으로 사용하는 로직
- ✅ **`@Component` 사용**: Spring Bean 등록

### 6️⃣ DTO 패턴 (Command/Query 분리)
- ✅ **Request/Response DTO**: API ↔ UseCase
- ✅ **Command/Query DTO**: UseCase 내부
- ✅ **DTO Validation**: `@Valid` + `@Validated`
- ✅ **DTO Naming**: `PlaceOrderCommand`, `OrderDetailQuery`

### 7️⃣ Testing (Service 계층 테스트)
- ✅ **Application Service Testing**: `@SpringBootTest` or Mock
- ✅ **Test Fixture**: 테스트 데이터 재사용
- ✅ **Object Mother**: 비즈니스 시나리오 표현
- ✅ **ArchUnit Rules**: Application Layer 의존성 검증

---

## 📊 레이어 통계

- **총 규칙 수**: 20개
- **Zero-Tolerance**: 5개
- **필수 규칙**: 15개
- **Cache Rules**: 20개 (Hook 자동 주입)

---

## 🔗 상세 문서

**Hook이 자동으로 주입하는 Cache Rules (20개)**:

### Assembler Pattern
- `01_assembler-responsibility.md` - Assembler 책임
- `02_usecase-inner-dto.md` - UseCase Inner DTO

### Component
- `01_component-pattern.md` - Component 패턴

### DTO Patterns
- `01_request-response-dto.md` - Request/Response DTO
- `02_command-query-dto.md` - Command/Query DTO
- `03_dto-validation.md` - DTO Validation
- `04_dto-naming-convention.md` - DTO 네이밍

### Facade
- `01_facade-usage-guide.md` - Facade 사용 가이드

### Package Guide
- `01_application_package_guide.md` - Application 패키지 구조

### Testing
- `01_application-service-testing.md` - Service 테스트
- `03_test-fixture-pattern.md` - Test Fixture
- `04_object-mother-pattern.md` - Object Mother
- `08_archunit-rules.md` - ArchUnit 규칙

### Transaction Management (핵심!)
- `01_transaction-boundaries.md` - 트랜잭션 경계 ⭐
- `02_spring-proxy-limitations.md` - Spring Proxy 제약사항 ⭐
- `03_transaction-best-practices.md` - Transaction Best Practices ⭐

### UseCase Design
- `01_command-usecase.md` - Command UseCase
- `02_query-usecase.md` - Query UseCase
- `03_orchestration-pattern.md` - Orchestration Pattern
- `04_usecase-method-naming.md` - UseCase Method Naming

**완전한 규칙은 Hook 시스템이 실시간으로 제공합니다!**

---

## 🎯 핵심 패턴

### UseCase 구조 (Transaction 경계)
```java
@Service
public class PlaceOrderService implements PlaceOrderUseCase {
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;

    // ✅ Pure Java Constructor (Lombok 금지)
    public PlaceOrderService(LoadOrderPort loadOrderPort, SaveOrderPort saveOrderPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
    }

    // ✅ Transaction 경계
    @Transactional
    @Override
    public OrderResult execute(PlaceOrderCommand command) {
        // 1. Domain 조회
        Order order = loadOrderPort.load(command.orderId());

        // 2. Domain 비즈니스 로직 (Application은 조율만!)
        order.place();

        // 3. Domain 저장
        saveOrderPort.save(order);

        // ❌ 외부 API 호출 금지! (트랜잭션 내부)
        // paymentClient.processPayment(...); ← 절대 금지!

        return OrderAssembler.toResult(order);
    }
}
```

### Transactional Outbox Pattern (외부 API 호출)
```java
@Service
public class PlaceOrderService implements PlaceOrderUseCase {
    private final SaveOrderPort saveOrderPort;
    private final OutboxStateManager outboxStateManager;

    @Transactional
    @Override
    public OrderResult execute(PlaceOrderCommand command) {
        // 1. Domain 저장 (트랜잭션 내부)
        Order order = Order.createNew(...);
        saveOrderPort.save(order);

        // 2. Outbox 엔트리 생성 (트랜잭션 내부)
        outboxStateManager.createOutboxEntry(
            new PaymentRequestCommand(order.getId(), order.getTotalAmount())
        );

        return OrderAssembler.toResult(order);
    }
}

// 3. Scheduler가 Outbox Polling → 외부 API 호출 (트랜잭션 밖!)
@Component
public class OutboxScheduler {
    @Scheduled(fixedDelay = 1000)
    public void pollOutbox() {
        List<OutboxEntry> entries = outboxRepository.findPending();
        for (OutboxEntry entry : entries) {
            paymentClient.processPayment(entry.getPayload()); // 트랜잭션 밖!
            outboxRepository.markAsPublished(entry.getId());
        }
    }
}
```

---

**✅ 이 요약본은 Application Layer 20개 규칙의 핵심만 포함합니다.**

**🔥 Transaction 경계 위반은 Git Pre-commit Hook으로 자동 차단!**
