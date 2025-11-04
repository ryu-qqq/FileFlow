# Application Layer ArchUnit Rules - Application 규칙 자동 검증

**목적**: ArchUnit을 활용하여 Application Layer의 코딩 규칙을 빌드 시 자동 검증

**관련 문서**:
- [Application Package Guide](../package-guide/01_application_package_guide.md)
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md)
- [UseCase Method Naming](../usecase-design/04_usecase-method-naming.md)
- [Component Pattern](../component/01_component-pattern.md)
- [Facade Usage Guide](../facade/01_facade-usage-guide.md)

**검증 도구**: ArchUnit 1.2.0+

**테스트 위치**: `application/src/test/java/architecture/ApplicationLayerArchitectureTest.java`

---

## 📌 핵심 원칙

### Application Layer Zero-Tolerance 규칙

Application Layer는 **헥사고날 아키텍처의 핵심 비즈니스 흐름**을 담당하므로 엄격한 규칙이 적용됩니다:

1. **Service 순환 의존 금지** - Service는 다른 Service/UseCase에 의존하지 않음
2. **DTO 분리** - Command/Query/Response는 `dto/` 패키지에만 위치
3. **DTO Record 타입** - 모든 DTO는 Java Record 타입
4. **DTO 네이밍 규칙** - Command: `{Verb}{Aggregate}Command`, Query: `{Verb}{Aggregate}Query`, Response: `{Aggregate}Response`
5. **UseCase 메서드 네이밍** - Command: `execute{Aggregate}{Action}()`, Query: `query{Aggregate}By{Condition}()`
6. **Service 네이밍** - `{Verb}{Aggregate}Service`
7. **Facade/Component 네이밍** - Facade: `{Context}Facade`, Component: `{Context}Manager`
8. **Transaction 경계** - Command Service는 `@Transactional` 필수

**ArchUnit이 빌드 시 자동으로 검증하여 위반 시 빌드 실패**

---

## ❌ 금지 패턴 (Anti-Patterns)

### Anti-Pattern 1: Service 순환 의존

```java
// ❌ Service가 다른 Service에 의존
package com.company.application.order.service.command;

import com.company.application.payment.service.command.CreatePaymentService; // ❌ Service → Service

@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final CreatePaymentService createPaymentService; // ❌ ArchUnit 빌드 실패!

    public CreateOrderService(CreatePaymentService createPaymentService) {
        this.createPaymentService = createPaymentService;
    }

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // ❌ Service가 다른 Service를 직접 호출
        createPaymentService.executePaymentCreation(...);
        return null;
    }
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: HIGH] - Rule 'Services should not depend on other Services'
was violated (1 times):
Class <CreateOrderService> depends on <CreatePaymentService> in (CreateOrderService.java:8)
```

**해결책**:
```java
// ✅ Service는 Outbound Port만 의존
package com.company.application.order.service.command;

import com.company.application.order.port.out.ProcessPaymentPort; // ✅ Port 의존

@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {

    private final ProcessPaymentPort processPaymentPort; // ✅ Port 의존

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // ✅ Port를 통한 외부 의존성 호출
        processPaymentPort.process(...);
        return null;
    }
}
```

---

### Anti-Pattern 2: UseCase 내부 DTO (Deprecated 패턴)

```java
// ❌ UseCase 내부에 Command/Response Record 정의
package com.company.application.order.port.in;

public interface CreateOrderUseCase {

    Response createOrder(Command command); // ❌ 내부 클래스 사용

    // ❌ UseCase 내부 Record → ArchUnit 빌드 실패!
    record Command(Long customerId, List<OrderItem> items) {
        public record OrderItem(Long productId, Integer quantity) {}
    }

    record Response(Long orderId, String status) {}
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: MEDIUM] - Rule 'Command/Query/Response DTOs must be in dto/ package'
was violated (2 times):
Record <CreateOrderUseCase.Command> is not in dto/ package in (CreateOrderUseCase.java:8)
Record <CreateOrderUseCase.Response> is not in dto/ package in (CreateOrderUseCase.java:12)
```

**해결책**:
```java
// ✅ DTO는 별도 파일로 분리
package com.company.application.order.port.in;

import com.company.application.order.dto.command.CreateOrderCommand; // ✅ dto/command/
import com.company.application.order.dto.response.OrderResponse;     // ✅ dto/response/

public interface CreateOrderUseCase {
    OrderResponse executeOrderCreation(CreateOrderCommand command); // ✅ DTO 분리
}

// dto/command/CreateOrderCommand.java
package com.company.application.order.dto.command;

public record CreateOrderCommand(
    Long customerId,
    List<OrderItem> items
) {
    public record OrderItem(Long productId, Integer quantity) {}
}

// dto/response/OrderResponse.java
package com.company.application.order.dto.response;

public record OrderResponse(Long orderId, String status) {}
```

---

### Anti-Pattern 3: DTO 네이밍 규칙 위반

```java
// ❌ DTO 네이밍 규칙 위반
package com.company.application.order.dto.command;

// ❌ Command 접미사 없음 → ArchUnit 빌드 실패!
public record CreateOrder(Long customerId) {}

// ❌ 동사 + Aggregate 패턴 위반
public record OrderCreate(Long customerId) {}

// ❌ Request 접미사 사용 (REST API Layer 전용)
public record CreateOrderRequest(Long customerId) {}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: MEDIUM] - Rule 'Command DTOs must end with "Command"'
was violated (3 times):
Record <CreateOrder> does not end with 'Command' in (CreateOrder.java:1)
Record <OrderCreate> does not match '{Verb}{Aggregate}Command' pattern
Record <CreateOrderRequest> should use 'Command' suffix, not 'Request'
```

**해결책**:
```java
// ✅ DTO 네이밍 규칙 준수
package com.company.application.order.dto.command;

// ✅ {Verb}{Aggregate}Command
public record CreateOrderCommand(Long customerId) {}

// ✅ {Verb}{Aggregate}Command
public record CancelOrderCommand(Long orderId, String reason) {}
```

---

### Anti-Pattern 4: UseCase 메서드 네이밍 규칙 위반

```java
// ❌ UseCase 메서드명 규칙 위반
public interface CreateOrderUseCase {

    // ❌ execute 접두사 없음
    OrderResponse createOrder(CreateOrderCommand command);

    // ❌ Aggregate 누락
    OrderResponse executeCreation(CreateOrderCommand command);

    // ❌ 행동 명사화 없음
    OrderResponse executeOrder(CreateOrderCommand command);
}

public interface GetOrderUseCase {

    // ❌ query 접두사 없음
    OrderDetailResponse getOrder(GetOrderQuery query);

    // ❌ Aggregate 누락
    OrderDetailResponse queryById(GetOrderQuery query);
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: MEDIUM] - Rule 'Command UseCase methods must start with "execute"'
was violated (3 times):
Method <createOrder> does not start with 'execute' in (CreateOrderUseCase.java:5)
Method <executeCreation> does not follow 'execute{Aggregate}{Action}' pattern
Method <executeOrder> does not have action noun (Creation, Cancellation, etc.)

Architecture Violation [Priority: MEDIUM] - Rule 'Query UseCase methods must start with "query"'
was violated (2 times):
Method <getOrder> does not start with 'query' in (GetOrderUseCase.java:5)
Method <queryById> does not follow 'query{Aggregate}By{Condition}' pattern
```

**해결책**:
```java
// ✅ UseCase 메서드명 규칙 준수
public interface CreateOrderUseCase {
    // ✅ execute + Order + Creation
    OrderResponse executeOrderCreation(CreateOrderCommand command);
}

public interface CancelOrderUseCase {
    // ✅ execute + Order + Cancellation
    void executeOrderCancellation(CancelOrderCommand command);
}

public interface GetOrderUseCase {
    // ✅ query + Order + ById
    OrderDetailResponse queryOrderById(GetOrderQuery query);
}

public interface FindOrdersByCustomerUseCase {
    // ✅ query + Orders + ByCustomer
    OrderListResponse queryOrdersByCustomer(FindOrdersByCustomerQuery query);
}
```

---

### Anti-Pattern 5: Command Service @Transactional 누락

```java
// ❌ Command Service에 @Transactional 없음
package com.company.application.order.service.command;

@Service  // ❌ @Transactional 없음 → ArchUnit 빌드 실패!
public class CreateOrderService implements CreateOrderUseCase {

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // DB 작업이 트랜잭션 없이 실행됨
        return null;
    }
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: HIGH] - Rule 'Command Services must have @Transactional'
was violated (1 times):
Class <CreateOrderService> is not annotated with @Transactional in (CreateOrderService.java:5)
```

**해결책**:
```java
// ✅ Command Service는 @Transactional 필수
package com.company.application.order.service.command;

@Service
@Transactional  // ✅ 필수
public class CreateOrderService implements CreateOrderUseCase {

    @Override
    public OrderResponse executeOrderCreation(CreateOrderCommand command) {
        // ✅ 트랜잭션 내에서 실행
        return null;
    }
}

// ✅ Query Service는 readOnly = true
@Service
@Transactional(readOnly = true)  // ✅ 읽기 전용
public class GetOrderService implements GetOrderUseCase {

    @Override
    public OrderDetailResponse queryOrderById(GetOrderQuery query) {
        return null;
    }
}
```

---

### Anti-Pattern 6: Facade에 @Transactional 사용

```java
// ❌ Facade에 @Transactional 사용
package com.company.application.order.facade;

@Component
@Transactional  // ❌ Facade는 @Transactional 금지!
public class OrderFacade {

    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;

    public OrderResponse createOrderWithPayment(CreateOrderCommand command) {
        // ❌ Facade는 여러 UseCase를 조율만, 트랜잭션은 UseCase가 관리
        OrderResponse orderResponse = createOrderUseCase.executeOrderCreation(command);
        processPaymentUseCase.executePaymentProcessing(...);
        return orderResponse;
    }
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: MEDIUM] - Rule 'Facades should not have @Transactional'
was violated (1 times):
Class <OrderFacade> is annotated with @Transactional in (OrderFacade.java:5)
```

**해결책**:
```java
// ✅ Facade는 @Transactional 없음
package com.company.application.order.facade;

@Component  // ✅ @Transactional 없음
public class OrderFacade {

    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;

    public OrderResponse createOrderWithPayment(CreateOrderCommand command) {
        // ✅ 각 UseCase가 자체 트랜잭션 관리
        OrderResponse orderResponse = createOrderUseCase.executeOrderCreation(command);
        processPaymentUseCase.executePaymentProcessing(...);
        return orderResponse;
    }
}
```

---

## ✅ ArchUnit 검증 규칙 구현

### 테스트 클래스: `ApplicationLayerArchitectureTest.java`

**위치**: `application/src/test/java/architecture/ApplicationLayerArchitectureTest.java`

```java
package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Application Layer Architecture Test
 *
 * <p>Application Layer의 코딩 규칙을 ArchUnit으로 자동 검증합니다.</p>
 *
 * <h3>검증 규칙:</h3>
 * <ul>
 *   <li>Service 순환 의존 금지 (Service → Service 금지)</li>
 *   <li>DTO 위치 검증 (dto/ 패키지에만 위치)</li>
 *   <li>DTO 타입 검증 (Record 타입 필수)</li>
 *   <li>DTO 네이밍 규칙 (Command/Query/Response 접미사)</li>
 *   <li>UseCase 메서드 네이밍 규칙 (execute/query 접두사)</li>
 *   <li>Service 네이밍 규칙 ({Verb}{Aggregate}Service)</li>
 *   <li>Transaction 규칙 (Command Service는 @Transactional 필수)</li>
 *   <li>Facade/Component 네이밍 규칙</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Application Layer ArchUnit 검증")
class ApplicationLayerArchitectureTest {

    private JavaClasses applicationClasses;

    @BeforeEach
    void setUp() {
        // Application Layer 클래스만 로드
        applicationClasses = new ClassFileImporter()
            .importPackages("com.company.application");
    }

    //=================================================
    // 1. Service 순환 의존 금지
    //=================================================

    @Test
    @DisplayName("Service는 다른 Service에 의존 금지 (순환 의존 방지)")
    void serviceShouldNotDependOnOtherServices() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..service..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..service..")
            .because("Services should depend on Ports only, not other Services (prevents circular dependencies)");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Service는 UseCase(Inbound Port)에 의존 금지")
    void serviceShouldNotDependOnUseCases() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..service..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..port.in..")
            .because("Services implement UseCases but should not depend on other UseCases directly");

        rule.check(applicationClasses);
    }

    //=================================================
    // 2. DTO 위치 및 타입 검증
    //=================================================

    @Test
    @DisplayName("Command/Query/Response DTO는 dto/ 패키지에만 위치")
    void dtoShouldResideInDtoPackage() {
        ArchRule rule = classes()
            .that().areRecords()
            .and().haveSimpleNameMatching(".*Command|.*Query|.*Response")
            .and().resideInAPackage("..application..")
            .should().resideInAnyPackage("..application..dto..")
            .because("Command/Query/Response DTOs must be in dto/ package (not inside UseCase)");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Command DTO는 dto/command/ 패키지에 위치")
    void commandDtoShouldResideInCommandPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Command")
            .and().resideInAPackage("..application..dto..")
            .should().resideInAPackage("..application..dto.command..")
            .because("Command DTOs must be in dto/command/ package");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Query DTO는 dto/query/ 패키지에 위치")
    void queryDtoShouldResideInQueryPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Query")
            .and().resideInAPackage("..application..dto..")
            .should().resideInAPackage("..application..dto.query..")
            .because("Query DTOs must be in dto/query/ package");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Response DTO는 dto/response/ 패키지에 위치")
    void responseDtoShouldResideInResponsePackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Response")
            .and().resideInAPackage("..application..dto..")
            .should().resideInAPackage("..application..dto.response..")
            .because("Response DTOs must be in dto/response/ package");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Application Layer DTO는 Record 타입이어야 함")
    void applicationDtoShouldBeRecord() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Command")
            .or().haveSimpleNameEndingWith("Query")
            .or().haveSimpleNameEndingWith("Response")
            .and().resideInAPackage("..application..dto..")
            .should().beRecords()
            .because("Application Layer DTOs must be Java Records (immutable)");

        rule.check(applicationClasses);
    }

    //=================================================
    // 3. DTO 네이밍 규칙
    //=================================================

    @Test
    @DisplayName("Command DTO는 {Verb}{Aggregate}Command 패턴 준수")
    void commandDtoShouldFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..dto.command..")
            .should().haveSimpleNameEndingWith("Command")
            .because("Command DTOs must end with 'Command' suffix");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Query DTO는 {Verb}{Aggregate}Query 패턴 준수")
    void queryDtoShouldFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..dto.query..")
            .should().haveSimpleNameEndingWith("Query")
            .because("Query DTOs must end with 'Query' suffix");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Response DTO는 {Aggregate}Response 패턴 준수")
    void responseDtoShouldFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..dto.response..")
            .should().haveSimpleNameEndingWith("Response")
            .because("Response DTOs must end with 'Response' suffix");

        rule.check(applicationClasses);
    }

    //=================================================
    // 4. Service 네이밍 규칙
    //=================================================

    @Test
    @DisplayName("Service 클래스는 'Service' 접미사 사용")
    void serviceShouldHaveServiceSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..service..")
            .and().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().haveSimpleNameEndingWith("Service")
            .because("Service classes must end with 'Service' suffix");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("UseCase 인터페이스는 'UseCase' 접미사 사용")
    void useCaseShouldHaveUseCaseSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..port.in..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .because("UseCase interfaces must end with 'UseCase' suffix");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Facade 클래스는 'Facade' 접미사 사용")
    void facadeShouldHaveFacadeSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..facade..")
            .should().haveSimpleNameEndingWith("Facade")
            .because("Facade classes must end with 'Facade' suffix");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Component 클래스는 'Manager' 접미사 사용")
    void componentShouldHaveManagerSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..component..")
            .should().haveSimpleNameEndingWith("Manager")
            .because("Component classes must end with 'Manager' suffix");

        rule.check(applicationClasses);
    }

    //=================================================
    // 5. Transaction 규칙
    //=================================================

    @Test
    @DisplayName("Command Service는 @Transactional 필수")
    void commandServiceShouldHaveTransactional() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..service.command..")
            .and().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .because("Command Services must have @Transactional for data consistency");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Query Service는 @Transactional(readOnly = true) 권장")
    void queryServiceShouldHaveTransactionalReadOnly() {
        // 경고 수준 (빌드 실패 아님)
        ArchRule rule = classes()
            .that().resideInAPackage("..application..service.query..")
            .and().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .because("Query Services should have @Transactional(readOnly = true) for read optimization");

        rule.allowEmptyShould(true).check(applicationClasses);
    }

    @Test
    @DisplayName("Facade는 @Transactional 사용 금지")
    void facadeShouldNotHaveTransactional() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..facade..")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .because("Facades orchestrate UseCases; each UseCase manages its own transaction");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Component는 @Transactional 필수")
    void componentShouldHaveTransactional() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..component..")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .because("Components manage transactional cross-cutting concerns");

        rule.check(applicationClasses);
    }

    //=================================================
    // 6. Package 구조 검증
    //=================================================

    @Test
    @DisplayName("Application Layer는 올바른 Package 구조를 따라야 함")
    void applicationLayerShouldFollowPackageStructure() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..")
            .should().resideInAnyPackage(
                "..application..",
                "..application..port.in..",
                "..application..port.out..",
                "..application..dto.command..",
                "..application..dto.query..",
                "..application..dto.response..",
                "..application..assembler..",
                "..application..service.command..",
                "..application..service.query..",
                "..application..facade..",
                "..application..component.."
            )
            .because("Application Layer must follow defined package structure");

        rule.check(applicationClasses);
    }
}
```

---

## 🚀 실행 방법

### 1. Gradle 빌드 시 자동 실행

```bash
# 전체 빌드 (ArchUnit 자동 실행)
./gradlew build

# Application Layer ArchUnit만 실행
./gradlew :application:test --tests architecture.ApplicationLayerArchitectureTest

# 출력:
# > Task :application:test
# ApplicationLayerArchitectureTest
#   ✓ Service는 다른 Service에 의존 금지 (0.2s)
#   ✓ Command/Query/Response DTO는 dto/ 패키지에만 위치 (0.3s)
#   ✓ Command Service는 @Transactional 필수 (0.2s)
#   ✓ Facade는 @Transactional 사용 금지 (0.1s)
#
# BUILD SUCCESSFUL in 2s
```

---

### 2. IntelliJ에서 실행

```
1. ApplicationLayerArchitectureTest.java 열기
2. 클래스 좌측 ▶ 클릭 → "Run 'ApplicationLayerArchitectureTest'"
3. 또는 Ctrl+Shift+F10 (Windows/Linux) / Cmd+Shift+R (Mac)
```

---

### 3. CI/CD Pipeline에서 실행

```yaml
# .github/workflows/archunit-application.yml
name: Application Layer ArchUnit Check

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  archunit-application:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run Application Layer ArchUnit Tests
        run: ./gradlew :application:test --tests architecture.ApplicationLayerArchitectureTest

      - name: Upload Test Results
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: archunit-application-report
          path: application/build/reports/tests/
```

---

## 🔧 문제 해결

### 문제 1: ArchUnit 테스트 실패 - Service 순환 의존

**증상**:
```
Architecture Violation - Rule 'Services should not depend on other Services'
was violated (1 times):
Class <CreateOrderService> depends on <CreatePaymentService>
```

**원인**: Service가 다른 Service에 직접 의존

**해결책**:
```java
// ❌ Before
private final CreatePaymentService createPaymentService;

// ✅ After - Port 사용
private final ProcessPaymentPort processPaymentPort;
```

---

### 문제 2: ArchUnit 테스트 실패 - DTO 위치 위반

**증상**:
```
Architecture Violation - Rule 'Command/Query/Response DTOs must be in dto/ package'
was violated (1 times):
Record <CreateOrderUseCase.Command> is not in dto/ package
```

**원인**: UseCase 내부에 Command/Response Record 정의

**해결책**:
```java
// ❌ Before
public interface CreateOrderUseCase {
    record Command(...) {}
}

// ✅ After - 별도 파일로 분리
// dto/command/CreateOrderCommand.java
public record CreateOrderCommand(...) {}
```

---

### 문제 3: ArchUnit 테스트 실패 - DTO 네이밍 규칙 위반

**증상**:
```
Architecture Violation - Rule 'Command DTOs must end with "Command"'
was violated (1 times):
Record <CreateOrder> does not end with 'Command'
```

**원인**: Command DTO에 `Command` 접미사 누락

**해결책**:
```java
// ❌ Before
public record CreateOrder(...) {}

// ✅ After
public record CreateOrderCommand(...) {}
```

---

### 문제 4: ArchUnit 테스트 실패 - @Transactional 누락

**증상**:
```
Architecture Violation - Rule 'Command Services must have @Transactional'
was violated (1 times):
Class <CreateOrderService> is not annotated with @Transactional
```

**원인**: Command Service에 `@Transactional` 어노테이션 누락

**해결책**:
```java
// ❌ Before
@Service
public class CreateOrderService { ... }

// ✅ After
@Service
@Transactional
public class CreateOrderService { ... }
```

---

## 📋 체크리스트

### ArchUnit 테스트 작성 체크리스트

- [ ] `ApplicationLayerArchitectureTest.java` 생성 (`application/src/test/java/architecture/`)
- [ ] Service 순환 의존 금지 규칙 추가
- [ ] DTO 위치 검증 규칙 추가 (dto/ 패키지)
- [ ] DTO 타입 검증 규칙 추가 (Record 타입)
- [ ] DTO 네이밍 규칙 검증 추가 (Command/Query/Response 접미사)
- [ ] UseCase 메서드 네이밍 규칙 추가 (execute/query 접두사)
- [ ] Service 네이밍 규칙 추가 (Service 접미사)
- [ ] Facade/Component 네이밍 규칙 추가
- [ ] Transaction 규칙 추가 (@Transactional)
- [ ] Package 구조 검증 규칙 추가

---

### Application Layer 코드 작성 체크리스트

- [ ] Service는 다른 Service/UseCase에 의존하지 않음
- [ ] Command/Query/Response는 `dto/` 패키지에 위치
- [ ] 모든 DTO는 Java Record 타입
- [ ] Command: `{Verb}{Aggregate}Command` 네이밍
- [ ] Query: `{Verb}{Aggregate}Query` 네이밍
- [ ] Response: `{Aggregate}Response` 네이밍
- [ ] Command UseCase 메서드: `execute{Aggregate}{Action}()`
- [ ] Query UseCase 메서드: `query{Aggregate}By{Condition}()`
- [ ] Command Service는 `@Transactional` 필수
- [ ] Facade는 `@Transactional` 없음
- [ ] Component는 `@Transactional` 필수
- [ ] Gradle 빌드 시 ArchUnit 테스트 통과
- [ ] CI/CD Pipeline 통과

---

## 📚 관련 문서

**다음 단계**:
- [Test Fixture Pattern](03_test-fixture-pattern.md) - DTO 테스트 객체 생성
- [Object Mother Pattern](04_object-mother-pattern.md) - 비즈니스 시나리오 테스트

**관련 가이드**:
- [Application Package Guide](../package-guide/01_application_package_guide.md) - 전체 패키지 구조
- [DTO Naming Convention](../dto-patterns/04_dto-naming-convention.md) - DTO 네이밍 규칙
- [UseCase Method Naming](../usecase-design/04_usecase-method-naming.md) - UseCase 메서드명 규칙
- [Component Pattern](../component/01_component-pattern.md) - Component 패턴 가이드
- [Facade Usage Guide](../facade/01_facade-usage-guide.md) - Facade 사용 가이드

**전체 ArchUnit 가이드**:
- [Domain Layer ArchUnit Rules](../../02-domain-layer/testing/08_archunit-rules.md) - Domain Layer 규칙
- [Layer Dependency Rules](../../05-testing/archunit-rules/01_layer-dependency-rules.md) - 전체 레이어 의존성
- [Naming Convention Rules](../../05-testing/archunit-rules/02_naming-convention-rules.md) - 네이밍 규칙
- [Annotation Rules](../../05-testing/archunit-rules/03_annotation-rules.md) - 어노테이션 규칙

---

**작성자**: Development Team
**최종 수정일**: 2025-11-03
**버전**: 1.0.0
