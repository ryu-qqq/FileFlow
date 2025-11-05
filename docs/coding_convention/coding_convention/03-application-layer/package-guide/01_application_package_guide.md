# APPLICATION 패키지 가이드

> 유즈케이스 구현, 트랜잭션 경계, 포트 호출, 도메인 조립(Assembler). **헥사고날 아키텍처의 핵심 계층**.

**중요**: Application 계층은 **UseCase 구현**과 **도메인 오케스트레이션**을 담당합니다. Port(인터페이스)는 Application 계층에 위치하며, Adapter(구현체)는 별도 Adapter 계층에 위치합니다.

## 🎯 핵심 원칙

> **Application은 흐름 연결(오케스트레이션)만! 비즈니스 로직은 Domain에 위임.**
>
> - ✅ Domain 메서드 호출 + 트랜잭션 경계 설정
> - ❌ 비즈니스 로직 작성 금지 (if-else로 규칙 판단 금지)
> - ❌ 데이터 직접 변경 금지 (setter 호출 금지)
>
> 상세한 Layer별 책임은 [비즈니스 로직 배치 원칙](../_shared/business-logic-placement.md) 참조.

## 디렉터리 구조
```
application/
└─ [context]/
   ├─ port/
   │  ├─ in/          # Inbound Port (UseCase 인터페이스)
   │  │  ├─ CreateOrderUseCase.java
   │  │  └─ GetOrderUseCase.java
   │  └─ out/         # Outbound Port (외부 의존 인터페이스)
   │     ├─ SaveOrderPort.java
   │     ├─ LoadOrderPort.java
   │     └─ ExternalApiPort.java
   ├─ dto/            # ⭐ Command/Query/Response DTO
   │  ├─ command/     # 쓰기 DTO (Record 필수)
   │  │  └─ CreateOrderCommand.java
   │  ├─ query/       # 읽기 조건 DTO (Record 필수)
   │  │  └─ GetOrderQuery.java
   │  └─ response/    # 응답 DTO (Record 필수)
   │     └─ OrderResponse.java
   ├─ assembler/      # Command/Query/Response ↔ Domain 변환
   │  └─ OrderAssembler.java
   ├─ service/
   │  ├─ command/     # 쓰기 UseCase 구현 (@Transactional)
   │  │  └─ CreateOrderService.java
   │  └─ query/       # 읽기 UseCase 구현 (@Transactional(readOnly=true))
   │     └─ GetOrderService.java
   ├─ facade/         # ⭐ UseCase 조율 (3개 이상 UseCase 사용 시)
   │  └─ OrderFacade.java
   └─ component/      # ⭐ 횡단 관심사 (Transactional Component)
      └─ OrderManager.java
```

## 기본 템플릿 (DDD_Hexagonal_CQRS_Template.md 기반)
```
application/
├─ [context]/
│  ├─ dto/           # ⭐ Command/Query/Response DTO (Record)
│  │  ├─ command/    # 쓰기 DTO
│  │  ├─ query/      # 읽기 조건 DTO
│  │  └─ response/   # 응답 DTO
│  ├─ port/
│  │  ├─ in/         # UseCase 인터페이스
│  │  └─ out/        # 외부 의존 (DB, API 등)
│  ├─ assembler/     # Command/Query/Response → Domain 변환
│  ├─ service/
│  │  ├─ command/    # 쓰기 UseCase 구현 (@Transactional)
│  │  └─ query/      # 읽기 UseCase 구현 (@Transactional(readOnly=true))
│  ├─ facade/        # UseCase 조율 (3개 이상 UseCase 사용 시)
│  └─ component/     # 횡단 관심사 (Transactional Component)
```

**템플릿 개선사항**:
- `dto/` 디렉터리 **사용** (command, query, response 분리)
- Command/Query/Response는 **별도 Record 파일**로 정의
- `facade/` 추가 (여러 UseCase 조율)
- `component/` 추가 (횡단 관심사 트랜잭션 관리)

## 포함할 객체 & 역할

### 1. Port (In/Out)
**Inbound Port (UseCase 인터페이스)**
- 애플리케이션 경계 인터페이스
- Command/Query/Response DTO를 파라미터/반환값으로 사용
- 비즈니스 유즈케이스의 추상화
- **메서드명 컨벤션**: `execute{Aggregate}{Action}()` (Command), `query{Aggregate}By{Condition}()` (Query)

**Outbound Port (외부 의존 인터페이스)**
- 영속성(Repository), 메시징, 외부 API 추상화
- 구현체는 **Adapter 계층**에 위치
- 의존성 역전 원칙(DIP) 적용

### 2. Service (Command/Query)
**Command Service (쓰기 유즈케이스)**
- `@Transactional` 적용
- Assembler로 Command → Domain 변환
- Outbound Port로 저장
- Domain Event 발행

**Query Service (읽기 유즈케이스)**
- `@Transactional(readOnly = true)` 적용
- Outbound Port로 조회
- Assembler로 Domain → Response 변환
- 조회 최적화 (N+1 방지, Projection 활용)

### 3. Assembler
**Command → Domain 변환**
- UseCase Command를 Domain Aggregate로 변환
- Value Object 생성 로직 포함
- 복잡한 도메인 객체 조립

**Domain → Response 변환**
- Domain Aggregate를 UseCase Response로 변환
- 여러 Aggregate 조합 (필요 시 Outbound Port 사용)
- DTO Projection 생성

### 4. DTO (Command/Query/Response)
**Command (쓰기 DTO)**
- 위치: `application/[context]/dto/command/`
- 상태 변경 의도 표현
- 네이밍: `{Verb}{Aggregate}Command` (예: `CreateOrderCommand`, `UpdateOrderStatusCommand`)
- Compact Constructor로 검증
- 불변 객체 (Record 필수)

**Query (읽기 조건 DTO)**
- 위치: `application/[context]/dto/query/`
- 조회 조건 표현
- 네이밍: `{Verb}{Aggregate}Query` (예: `GetOrderQuery`, `SearchOrdersQuery`)
- Compact Constructor로 검증
- 불변 객체 (Record 필수)

**Response (응답 DTO)**
- 위치: `application/[context]/dto/response/`
- UseCase 실행 결과
- 네이밍: `{Aggregate}Response` (예: `OrderResponse`, `OrderSummaryResponse`)
- 최소한의 정보만 반환
- 불변 객체 (Record 필수)

### 5. Facade (UseCase 조율)
**역할**
- 여러 UseCase를 하나의 진입점으로 조율
- Controller 의존성 단순화 (Controller → 단일 Facade)
- Transaction과 외부 호출 분리 (Transaction 내 DB, 외부는 밖에서)

**사용 조건 (Rule of 3)**
- 3개 이상 UseCase 호출 필요
- Transaction + 외부 호출 분리 필요
- Controller 의존성 감소 필요

**예시**: `OrderFacade`, `PaymentFacade`

### 6. Component (횡단 관심사)
**역할**
- 여러 UseCase가 공통으로 사용하는 트랜잭션 로직
- Bounded Context 상태 변경 관리 (Order, Product 등)
- Outbox, Saga, Event 같은 횡단 관심사 처리

**사용 조건**
- 여러 Command/Query Service에서 공통 사용
- 트랜잭션 경계가 필요한 공통 로직
- Bounded Context별 상태 변경 관리

**네이밍**: `{Context}Manager` (예: `OrderManager`, `ProductManager`, `OutboxManager`)
**예시**: `OrderManager`, `OutboxManager`, `SagaCoordinator`

## 계층 간 데이터 변환 흐름

### 전체 데이터 변환 흐름
```
[Adapter Layer - Controller]
API Request (OrderApiRequest)
    ↓
[Adapter Layer - Mapper]
    ↓ toCommand()
Application Command (CreateOrderCommand)  # ⭐ 별도 Record 파일
    ↓
[Application Layer - Service]
    ↓
[Application Layer - Assembler]
    ↓ toDomain()
Domain Object (Order)
    ↓
[Domain Layer - Business Logic]
    ↓
Domain Result (Order)
    ↓
[Application Layer - Assembler]
    ↓ toResponse()
Application Response (OrderResponse)  # ⭐ 별도 Record 파일
    ↓
[Adapter Layer - Mapper]
    ↓ toApiResponse()
API Response (OrderApiResponse)
    ↓
[Adapter Layer - Controller]
```

### Mapper vs Assembler 비교
| 구분 | Mapper (Adapter) | Assembler (Application) |
|------|------------------|-------------------------|
| **위치** | `adapter/in/web/mapper/` | `application/[context]/assembler/` |
| **변환** | Request ↔ Command<br>Response ↔ API Response | Command → Domain<br>Domain → Response |
| **의존성** | Adapter DTO | UseCase Command/Response, Domain |
| **복잡도** | 단순 매핑 | Value Object 변환, 조립 로직 |
| **예제** | `OrderApiMapper` | `OrderAssembler` |

**중요**: Mapper와 Assembler는 **역할이 다릅니다**!
- **Mapper**: Adapter Layer에서 API DTO ↔ UseCase DTO 변환
- **Assembler**: Application Layer에서 UseCase DTO ↔ Domain 변환

## 허용/금지 의존

### 허용 의존성
- `java.*` (표준 자바 API)
- `domain..` (Domain Layer)
- `application..port.out..` (Outbound Port)
- `org.springframework.stereotype.*` (최소한의 Spring 어노테이션)
- `org.springframework.transaction.annotation.Transactional`

### 금지 의존성
- `adapter..` (Adapter Layer 직접 의존 금지)
- `jakarta.persistence.*` (JPA는 Adapter Layer에서만)
- `org.springframework.web.*` (REST는 Adapter Layer에서만)
- `com.fasterxml.jackson.*` (JSON 직렬화는 Adapter Layer에서만)
- `lombok.*` (Lombok 사용 금지)

### 의존성 방향
```
Adapter (in) → Application → Domain
         ↑         ↓
         └─── Port (out) ←─ Adapter (out)
```

**핵심**: 의존성은 항상 **밖 → 안** (Adapter → Application → Domain)

## 네이밍 규약

### UseCase (Inbound Port)
- `CreateXxxUseCase`, `UpdateXxxUseCase`, `DeleteXxxUseCase` (Command)
- `GetXxxUseCase`, `FindXxxUseCase`, `SearchXxxUseCase` (Query)
- **규칙**: 동사 + Aggregate + `UseCase`

### UseCase 메서드명
- **Command**: `execute{Aggregate}{Action}()`
  - 예: `executeOrderCreation()`, `executeOrderCancellation()`
- **Query**: `query{Aggregate}By{Condition}()`
  - 예: `queryOrderById()`, `queryOrdersByCustomer()`

### Service (UseCase 구현체)
- `XxxCommandService` (Command UseCase 구현)
- `XxxQueryService` (Query UseCase 구현)
- **규칙**: Aggregate + Command/Query + `Service`

### DTO (Command/Query/Response)
- **Command**: `{Verb}{Aggregate}Command`
  - 예: `CreateOrderCommand`, `UpdateOrderStatusCommand`
- **Query**: `{Verb}{Aggregate}Query`
  - 예: `GetOrderQuery`, `SearchOrdersQuery`
- **Response**: `{Aggregate}Response` 또는 `{Aggregate}{Detail}Response`
  - 예: `OrderResponse`, `OrderSummaryResponse`

### Port (Outbound Port)
- `SaveXxxPort`, `LoadXxxPort`, `DeleteXxxPort` (Repository)
- `XxxCommandPort`, `XxxQueryPort` (CQRS 스타일)
- `ExternalXxxPort` (외부 API)
- **규칙**: 동사 + Aggregate + `Port` 또는 `외부시스템 + Port`

### Assembler
- `XxxAssembler` (도메인별 단일 Assembler)
- **규칙**: Aggregate + `Assembler`
- **예시**: `OrderAssembler`, `PaymentAssembler`

### Facade
- `XxxFacade` (Bounded Context별 Facade)
- **규칙**: Aggregate + `Facade`
- **예시**: `OrderFacade`, `PaymentFacade`

### Component
- `{Context}Manager` (Bounded Context별 상태 관리)
- **규칙**: BoundedContext + `Manager`
- **예시**: `OrderManager`, `ProductManager`, `OutboxManager`

## CQRS 명명 규칙 (템플릿 기반)

### Command Side (쓰기)
| 타입 | 인터페이스 | 구현체 | 포트(out) |
|------|-------------|----------|------------|
| 생성 | `CreateXxxUseCase` | `XxxCommandService` | `SaveXxxPort` |
| 수정 | `UpdateXxxUseCase` | `XxxCommandService` | `LoadXxxPort`, `SaveXxxPort` |
| 삭제 | `DeleteXxxUseCase` | `XxxCommandService` | `DeleteXxxPort` |

### Query Side (읽기)
| 타입 | 인터페이스 | 구현체 | 포트(out) |
|------|-------------|----------|------------|
| 단건 조회 | `GetXxxUseCase` | `XxxQueryService` | `LoadXxxPort` |
| 목록 조회 | `FindXxxUseCase` | `XxxQueryService` | `FindXxxPort` |
| 검색 | `SearchXxxUseCase` | `XxxQueryService` | `SearchXxxPort` |

## Do / Don't

### Do ✅
- DTO는 **별도 Record 파일**로 정의 (`dto/command/`, `dto/query/`, `dto/response/`)
- DTO 네이밍 규칙 준수 (`{Verb}{Aggregate}Command`, `{Verb}{Aggregate}Query`, `{Aggregate}Response`)
- UseCase 메서드명 컨벤션 준수 (`execute{Action}()`, `query{Condition}()`)
- Service에서 **Assembler**로 Command/Query → Domain, Domain → Response 변환
- 트랜잭션 경계 명확히 설정 (`@Transactional`)
- 외부 I/O는 트랜잭션 **밖**에서 실행 (Facade에서 분리)
- Command UseCase가 조회 필요 시 **Outbound Port** 사용 (다른 Service 직접 호출 금지)
- **3개 이상 UseCase 호출 시 Facade 사용** (Controller 의존성 단순화)
- **여러 UseCase 공통 로직은 Component로 추출** (트랜잭션 재사용)
- Spring Proxy 제약사항 고려 (Private/Final/Self-invocation 금지)
- Aggregate 단위 트랜잭션 유지

### Don't ❌
- DTO를 UseCase 내부 Record로 정의 금지 (별도 파일로)
- Adapter DTO를 Application Layer에서 직접 사용 금지
- Mapper와 Assembler 혼동 금지 (역할이 다름!)
- **다른 Application Service 직접 호출 금지 (순환 의존 위험)**
- **다른 UseCase 직접 호출 금지 (Port로 간접 의존)**
- REST/JPA 어노테이션 사용 금지 (Adapter Layer로 이동)
- 도메인 규칙을 Application Layer에서 구현 금지 (Domain Layer로 이동)
- Self-invocation (`this.method()`) 금지 (프록시 우회)
- Private/Final 메서드에 `@Transactional` 사용 금지
- `@Transactional` 메서드 내에서 외부 API 호출 금지

## Spring Proxy 제약사항 주의

### ❌ 작동하지 않는 경우
```java
// ❌ Private 메서드 - 프록시 무시
@Transactional
private void saveOrder(Order order) { ... }

// ❌ Final 클래스/메서드 - 프록시 생성 불가
@Transactional
public final void createOrder(Command cmd) { ... }

// ❌ Self-invocation - 프록시 우회
@Service
public class OrderService {
    @Transactional
    public void processOrder() {
        this.saveOrder(); // ❌ @Transactional 무시됨!
    }

    @Transactional
    void saveOrder() { ... }
}
```

### ✅ 올바른 방법
```java
// ✅ Public 메서드
@Transactional
public void saveOrder(Order order) { ... }

// ✅ 별도 빈으로 분리
@Service
public class OrderService {
    private final OrderPersistenceService persistenceService;

    public void processOrder() {
        persistenceService.saveOrder(); // ✅ 프록시 정상 작동
    }
}

@Service
public class OrderPersistenceService {
    @Transactional
    public void saveOrder() { ... }
}
```

## 트랜잭션 경계 및 외부 호출 규칙

### ❌ 트랜잭션 내 외부 API 호출
```java
// ❌ Bad - 트랜잭션 내 외부 API 호출
@Transactional
public void processOrder(Command cmd) {
    Order order = orderAssembler.toDomain(cmd);
    saveOrderPort.save(order);

    s3Client.uploadFile(); // ❌ 외부 API - 트랜잭션 길어짐
    emailService.send();   // ❌ 외부 API - 실패 시 롤백 문제
}
```

### ✅ 트랜잭션 분리
```java
// ✅ Good - 트랜잭션과 외부 호출 분리
@Service
public class OrderFacadeService {
    private final CreateOrderService createOrderService;
    private final FileUploadPort fileUploadPort;
    private final EmailPort emailPort;

    public void processOrder(Command cmd) {
        // ✅ 1. 트랜잭션 내에서 DB 저장
        Response response = createOrderService.createOrder(cmd);

        // ✅ 2. 트랜잭션 밖에서 외부 호출
        fileUploadPort.upload(response.orderId());
        emailPort.sendConfirmation(response.orderId());
    }
}

@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    @Override
    public Response createOrder(Command cmd) {
        // ✅ 짧은 트랜잭션 (DB 저장만)
        Order order = orderAssembler.toDomain(cmd);
        Order savedOrder = saveOrderPort.save(order);
        return orderAssembler.toResponse(savedOrder);
    }
}
```

## Application 서비스 간 호출 규칙

### ❌ 서비스 직접 호출
```java
// ❌ Bad - 순환 의존 위험
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final GetCustomerService getCustomerService; // ❌

    @Override
    public Response createOrder(Command cmd) {
        // ❌ 다른 서비스 직접 호출
        CustomerResponse customer = getCustomerService.getCustomer(cmd.customerId());

        Order order = orderAssembler.toDomain(cmd, customer);
        return orderAssembler.toResponse(saveOrderPort.save(order));
    }
}
```

### ✅ QueryPort 사용
```java
// ✅ Good - QueryPort 사용
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadCustomerPort loadCustomerPort; // ✅ Port 사용

    @Override
    public Response createOrder(Command cmd) {
        // ✅ QueryPort로 조회
        Customer customer = loadCustomerPort.load(cmd.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        Order order = orderAssembler.toDomain(cmd, customer);
        return orderAssembler.toResponse(saveOrderPort.save(order));
    }
}
```

## ArchUnit 룰 스니펫

```java
// ==================== Layer 의존성 규칙 ====================

// Application Layer는 Adapter Layer에 의존하지 않음
noClasses().that().resideInAPackage("..application..")
    .should().dependOnClassesThat().resideInAnyPackage("..adapter..");

// ==================== 네이밍 규칙 ====================

// Inbound Port는 UseCase로 끝나야 함
classes().that().resideInAPackage("..application..port.in..")
    .should().haveSimpleNameEndingWith("UseCase");

// Outbound Port는 Port로 끝나야 함
classes().that().resideInAPackage("..application..port.out..")
    .should().haveSimpleNameEndingWith("Port");

// Service는 Service로 끝나야 함
classes().that().resideInAPackage("..application..service..")
    .should().haveSimpleNameEndingWith("Service");

// Assembler는 Assembler로 끝나야 함
classes().that().resideInAPackage("..application..assembler..")
    .should().haveSimpleNameEndingWith("Assembler");

// Facade는 Facade로 끝나야 함
classes().that().resideInAPackage("..application..facade..")
    .should().haveSimpleNameEndingWith("Facade");

// Component는 Manager로 끝나야 함
classes().that().resideInAPackage("..application..component..")
    .should().haveSimpleNameEndingWith("Manager");

// Command DTO는 Command로 끝나야 함
classes().that().resideInAPackage("..application..dto.command..")
    .should().haveSimpleNameEndingWith("Command")
    .andShould().beRecords();

// Query DTO는 Query로 끝나야 함
classes().that().resideInAPackage("..application..dto.query..")
    .should().haveSimpleNameEndingWith("Query")
    .andShould().beRecords();

// Response DTO는 Response로 끝나야 함
classes().that().resideInAPackage("..application..dto.response..")
    .should().haveSimpleNameEndingWith("Response")
    .andShould().beRecords();

// ==================== 순환 의존성 방지 규칙 ====================

// Service는 다른 Service에 의존하지 않음 (순환 의존 방지)
noClasses().that().resideInAPackage("..application..service..")
    .should().dependOnClassesThat().resideInAnyPackage("..application..service..")
    .because("Services should depend on Ports only, not other Services (prevents circular dependencies)");

// Service는 UseCase(Inbound Port)에 의존하지 않음
noClasses().that().resideInAPackage("..application..service..")
    .should().dependOnClassesThat().resideInAnyPackage("..application..port.in..")
    .because("Services implement UseCases but should not depend on other UseCases directly");

// Service는 Outbound Port만 의존 (순환 의존 방지)
classes().that().resideInAPackage("..application..service..")
    .and().areNotInterfaces()
    .should().onlyDependOnClassesThat()
    .resideInAnyPackage(
        "..application..port.out..",  // Outbound Port만 허용
        "..application..assembler..",  // Assembler 허용
        "..application..dto..",        // DTO 허용
        "..application..component..",  // Component 허용
        "..domain..",                  // Domain 허용
        "java..",                      // 표준 자바 API
        "org.springframework.."        // Spring 프레임워크
    )
    .because("Services should depend on Ports/Assembler/DTO/Component only");

// ==================== 트랜잭션 규칙 ====================

// Command Service는 @Transactional 필수
classes().that().resideInAPackage("..application..service.command..")
    .should().beAnnotatedWith(Transactional.class);

// Query Service는 @Transactional(readOnly=true) 권장
classes().that().resideInAPackage("..application..service.query..")
    .should().beAnnotatedWith(Transactional.class);

// ==================== DTO 위치 규칙 ====================

// DTO는 dto/ 패키지에만 위치 (UseCase 내부 Record 금지)
classes().that().areRecords()
    .and().haveSimpleNameMatching(".*Command|.*Query|.*Response")
    .and().resideInAPackage("..application..")
    .should().resideInAnyPackage("..application..dto..")
    .because("Command/Query/Response DTOs must be in dto/ package (not inside UseCase)");
```

## 관련 문서

### UseCase 설계
- [Command UseCase](../usecase-design/01_command-usecase.md) - 상태 변경 유즈케이스
- [Query UseCase](../usecase-design/02_query-usecase.md) - 조회 유즈케이스
- [UseCase Inner DTO](../assembler-pattern/02_usecase-inner-dto.md) - 내부 Record 패턴

### Assembler 패턴
- [Assembler Responsibility](../assembler-pattern/01_assembler-responsibility.md) - Assembler 역할
- [Mapper vs Assembler](../assembler-pattern/02_mapper-vs-assembler.md) - 차이점

### DTO 패턴
- [Request/Response DTO](../dto-patterns/01_request-response-dto.md) - Adapter Layer DTO
- [Command/Query DTO](../dto-patterns/02_command-query-dto.md) - Application Layer DTO
- [DTO Validation](../dto-patterns/03_dto-validation.md) - 검증 전략

### 트랜잭션 관리
- [Transaction Boundaries](../transaction-management/01_transaction-boundaries.md) - 트랜잭션 경계
- [Spring Proxy Limitations](../transaction-management/02_spring-proxy-limitations.md) - 프록시 제약사항
- [Transaction Best Practices](../transaction-management/03_transaction-best-practices.md) - 모범 사례

### 테스트
- [Application Service Testing](../testing/01_application-service-testing.md) - 서비스 테스트

---

**작성자**: Development Team
**최종 수정일**: 2025-10-17
**버전**: 1.0.0
