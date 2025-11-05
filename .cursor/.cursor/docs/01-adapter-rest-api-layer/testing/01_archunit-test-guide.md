# REST API Layer ArchUnit 테스트 가이드

> **Zero-Tolerance**: 모든 REST API Layer 코드는 ArchUnit 테스트를 통과해야 합니다.
> 빌드 시 자동 실행되며, 규칙 위반 시 빌드가 실패합니다.

## 목차
1. [ArchUnit 소개](#1-archunit-소개)
2. [프로젝트 설정](#2-프로젝트-설정)
3. [테스트 구조](#3-테스트-구조)
4. [컨벤션 규칙 테스트](#4-컨벤션-규칙-테스트)
5. [Layer 의존성 규칙 테스트](#5-layer-의존성-규칙-테스트)
6. [실행 및 검증](#6-실행-및-검증)
7. [위반 사례 및 수정](#7-위반-사례-및-수정)
8. [Best Practices](#8-best-practices)

---

## 1. ArchUnit 소개

### 1.1 ArchUnit이란?

**ArchUnit**은 Java 아키텍처 및 코딩 규칙을 **자동으로 검증**하는 테스트 프레임워크입니다.

**핵심 가치**:
- 📋 **규칙 자동화**: 수동 코드 리뷰를 자동화된 테스트로 대체
- 🔒 **빌드 시 검증**: 규칙 위반 시 빌드 실패 (Zero-Tolerance)
- 📚 **문서화**: 테스트 코드가 곧 아키텍처 문서
- 🎯 **일관성**: 모든 개발자가 동일한 규칙을 자동으로 준수

### 1.2 왜 필요한가?

#### ❌ 수동 코드 리뷰의 한계
```java
// 리뷰어가 놓칠 수 있는 위반 사례들
@Data  // ⚠️ Lombok 금지 규칙 위반
public class OrderApiRequest { }

public class ProductController {  // ⚠️ @RestController 누락
    private OrderService orderService;  // ⚠️ final 누락
}

public class OrderApiMapper {  // ⚠️ Utility 클래스인데 인스턴스화 가능
    public void toCommand() { }  // ⚠️ static 누락
}
```

#### ✅ ArchUnit 자동 검증
```java
@Test
@DisplayName("Lombok @Data는 사용하지 않아야 함")
void shouldNotUseLombokData() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..adapter.rest..")
        .should().beAnnotatedWith("lombok.Data");

    rule.check(importedClasses);  // ❌ 빌드 실패 (즉시 감지!)
}
```

**결과**: 100% 규칙 준수 보장

---

## 2. 프로젝트 설정

### 2.1 의존성 추가

**`build.gradle` (bootstrap-web-api 모듈)**:
```gradle
dependencies {
    // ArchUnit (Architecture Testing)
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
}
```

### 2.2 테스트 패키지 구조

```
bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/
├── RestApiLayerRulesTest.java         # Layer 의존성 규칙 (3개 테스트)
└── RestApiAdapterConventionTest.java  # 세부 컨벤션 규칙 (25개 테스트)
```

**역할 분리**:
- **RestApiLayerRulesTest**: Layer 간 의존성 규칙 (헥사고날 아키텍처 검증)
- **RestApiAdapterConventionTest**: REST API Layer 내부 컨벤션 규칙 (네이밍, 어노테이션 등)

---

## 3. 테스트 구조

### 3.1 RestApiAdapterConventionTest (컨벤션 규칙)

**파일**: `RestApiAdapterConventionTest.java`
**목적**: REST API Layer 내부 세부 컨벤션 검증

```java
@DisplayName("REST API Adapter Layer 컨벤션 테스트")
public class RestApiAdapterConventionTest {

    private static JavaClasses restApiClasses;

    @BeforeAll
    static void setUp() {
        restApiClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.adapter.in.rest");
    }

    @Nested
    @DisplayName("Lombok 금지 규칙")
    class LombokProhibitionTest { }

    @Nested
    @DisplayName("Controller 컨벤션")
    class ControllerConventionTest { }

    @Nested
    @DisplayName("DTO 컨벤션")
    class DtoConventionTest { }

    @Nested
    @DisplayName("Mapper 컨벤션")
    class MapperConventionTest { }

    @Nested
    @DisplayName("Error Mapper 컨벤션")
    class ErrorMapperConventionTest { }

    @Nested
    @DisplayName("Properties 컨벤션")
    class PropertiesConventionTest { }
}
```

**검증 카테고리**:
1. 🚫 **Lombok 금지**: @Data, @Getter, @Setter, @Builder (4개 테스트)
2. 🎮 **Controller**: 네이밍, 어노테이션, 필드 불변성 (6개 테스트)
3. 📦 **DTO**: Record 사용, 네이밍, Pagination (5개 테스트)
4. 🔀 **Mapper**: Utility 클래스 패턴, static 메서드 (4개 테스트)
5. ⚠️ **Error Mapper**: 인터페이스 구현, 네이밍 (3개 테스트)
6. ⚙️ **Properties**: @ConfigurationProperties 패턴 (3개 테스트)

### 3.2 RestApiLayerRulesTest (Layer 의존성 규칙)

**파일**: `RestApiLayerRulesTest.java`
**목적**: Layer 간 의존성 규칙 검증 (헥사고날 아키텍처)

```java
@DisplayName("REST API Layer 아키텍처 규칙 검증")
class RestApiLayerRulesTest {

    private JavaClasses importedClasses;

    @BeforeEach
    void setUp() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.adapter.in.rest");
    }

    @Test
    @DisplayName("REST API Layer는 정의된 패키지 구조를 따라야 함")
    void restApiLayerShouldFollowPackageStructure() { }

    @Test
    @DisplayName("Request DTO는 Application Layer DTO에 의존하지 않아야 함")
    void requestDtosShouldNotDependOnApplicationLayerDtos() { }

    @Test
    @DisplayName("Mapper는 Application Layer와 Domain Layer에 의존할 수 있음")
    void mappersShouldBeAbleToAccessApplicationAndDomainLayer() { }

    @Test
    @DisplayName("Controller는 Application Layer 포트에만 의존해야 함")
    void controllersShouldOnlyDependOnApplicationPorts() { }
}
```

**검증 규칙**:
1. 📁 **Package 구조**: controller, dto, mapper, error 패키지 준수
2. 🔗 **의존성 방향**: REST API → Application → Domain (단방향)
3. 🚫 **Persistence Layer 직접 접근 금지**: Controller/Mapper는 Persistence 접근 불가

---

## 4. 컨벤션 규칙 테스트

### 4.1 Lombok 금지 규칙 (4개 테스트)

#### 왜 금지하는가?

**Zero-Tolerance 규칙**: REST API Layer는 Pure Java 사용 (Lombok 금지)

**이유**:
- 🔍 **명시성**: 코드를 보면 정확히 무엇이 있는지 알 수 있어야 함
- 🧪 **테스트 가능성**: 생성자 기반 테스트 작성 용이
- 🐛 **디버깅**: 런타임 에러 추적 용이 (컴파일 타임에 생성된 코드 문제 회피)
- 📚 **Java 21 Record**: Record 패턴이 Lombok을 대체

#### 테스트 코드

```java
@Nested
@DisplayName("Lombok 금지 규칙")
class LombokProhibitionTest {

    @Test
    @DisplayName("@Data 금지")
    void shouldNotUseLombokData() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.rest..")
            .should().beAnnotatedWith("lombok.Data")
            .because("Pure Java를 사용해야 합니다");

        rule.check(restApiClasses);
    }

    @Test
    @DisplayName("@Getter 금지")
    void shouldNotUseLombokGetter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.rest..")
            .should().beAnnotatedWith("lombok.Getter")
            .because("Pure Java getter를 직접 작성해야 합니다");

        rule.check(restApiClasses);
    }

    @Test
    @DisplayName("@Setter 금지")
    void shouldNotUseLombokSetter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.rest..")
            .should().beAnnotatedWith("lombok.Setter")
            .because("불변 객체 원칙에 따라 Setter를 사용하지 않습니다");

        rule.check(restApiClasses);
    }

    @Test
    @DisplayName("@Builder 금지")
    void shouldNotUseLombokBuilder() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.rest..")
            .should().beAnnotatedWith("lombok.Builder")
            .because("Pure Java 생성자를 직접 작성해야 합니다");

        rule.check(restApiClasses);
    }
}
```

#### 올바른 예시

❌ **Lombok 사용 (금지)**:
```java
@Data  // ❌ Lombok 금지
public class CreateOrderApiRequest {
    private Long productId;
    private int quantity;
}
```

✅ **Pure Java (권장)**:
```java
public record CreateOrderApiRequest(
    Long productId,
    int quantity
) {
    // Record는 getter, equals, hashCode, toString 자동 생성
    // 불변성 보장 (Lombok @Data보다 우수)
}
```

---

### 4.2 Controller 컨벤션 (6개 테스트)

#### 4.2.1 네이밍 규칙

**규칙**: `*Controller` 접미사 필수

```java
@Test
@DisplayName("Controller는 *Controller 네이밍을 따라야 함")
void controllerShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller")
        .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .should().haveSimpleNameEndingWith("Controller")
        .because("{Domain}Controller 네이밍 규칙을 따라야 합니다");

    rule.check(restApiClasses);
}
```

**예시**:
```java
✅ OrderController, ProductController, PaymentController
❌ OrderApi, OrderResource, OrderHandler
```

#### 4.2.2 @RestController 필수

**규칙**: 모든 Controller는 `@RestController` 어노테이션 필수

```java
@Test
@DisplayName("Controller는 @RestController 어노테이션을 가져야 함")
void controllerShouldHaveRestControllerAnnotation() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller")
        .and().haveSimpleNameEndingWith("Controller")
        .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .because("Controller는 @RestController를 사용해야 합니다");

    rule.check(restApiClasses);
}
```

**올바른 예시**:
```java
@RestController  // ✅ 필수
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.order.base}")
public class OrderController {
    // ...
}
```

#### 4.2.3 @RequestMapping 필수

**규칙**: 베이스 경로 설정을 위한 `@RequestMapping` 필수

```java
@Test
@DisplayName("Controller는 @RequestMapping 어노테이션을 가져야 함")
void controllerShouldHaveRequestMappingAnnotation() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller")
        .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .should().beAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
        .because("@RequestMapping으로 베이스 경로를 설정해야 합니다");

    rule.check(restApiClasses);
}
```

**올바른 예시**:
```java
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.order.base}")  // ✅
public class OrderController {

    @GetMapping("${api.endpoints.order.by-id}")  // /api/v1/orders/{id}
    public ResponseEntity<ApiResponse<OrderApiResponse>> getOrder(@PathVariable Long id) {
        // ...
    }
}
```

#### 4.2.4 필드 불변성 (final)

**규칙**: Controller 필드는 `final` (Constructor Injection 패턴)

```java
@Test
@DisplayName("Controller는 final 필드만 가져야 함")
void controllerFieldsShouldBeFinal() {
    ArchRule rule = fields()
        .that().areDeclaredInClassesThat().resideInAPackage("..controller")
        .and().areDeclaredInClassesThat().areAnnotatedWith(
            "org.springframework.web.bind.annotation.RestController"
        )
        .should().beFinal()
        .because("Controller 필드는 final이어야 합니다 (Constructor Injection)");

    rule.check(restApiClasses);
}
```

**올바른 예시**:
```java
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.order.base}")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;  // ✅ final
    private final CancelOrderUseCase cancelOrderUseCase;  // ✅ final

    public OrderController(
        PlaceOrderUseCase placeOrderUseCase,
        CancelOrderUseCase cancelOrderUseCase
    ) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }
}
```

❌ **잘못된 예시**:
```java
@RestController
public class OrderController {
    private PlaceOrderUseCase placeOrderUseCase;  // ❌ final 누락

    @Autowired  // ❌ Field Injection (금지)
    private CancelOrderUseCase cancelOrderUseCase;
}
```

#### 4.2.5 Facade vs UseCase 직접 호출

**Note**: 이 규칙은 복잡도가 높아 자동화하지 않음 (수동 코드 리뷰)

**판단 기준** (YAGNI 원칙):
- ✅ **UseCase 2개 이상**: Facade 사용 (의존성 감소)
- ✅ **UseCase 1개 + 단순 위임**: UseCase 직접 호출 (Facade 불필요)

**예시**:
```java
// ✅ Case 1: UseCase 1개 → 직접 호출
@RestController
public class CreateOrderController {
    private final PlaceOrderUseCase placeOrderUseCase;  // 1개만 사용 → 직접 호출
}

// ✅ Case 2: UseCase 2개 이상 → Facade 사용
@RestController
public class OrderController {
    private final OrderFacade orderFacade;  // 2개 이상 → Facade로 감싸기
}
```

**참고 문서**:
- `docs/coding_convention/03-application-layer/facade/01_facade-usage-guide.md`

---

### 4.3 DTO 컨벤션 (5개 테스트)

#### 4.3.1 Request DTO 네이밍

**규칙**: `*ApiRequest` 접미사 필수

```java
@Test
@DisplayName("Request DTO는 *ApiRequest 네이밍을 따라야 함")
void requestDtoShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..dto.request")
        .should().haveSimpleNameEndingWith("ApiRequest")
        .because("{Operation}{Domain}ApiRequest 네이밍 규칙을 따라야 합니다");

    rule.check(restApiClasses);
}
```

**네이밍 패턴**:
```java
✅ CreateOrderApiRequest      (Command)
✅ UpdateOrderApiRequest       (Command)
✅ SearchOrderApiRequest       (Query - Pagination)
✅ GetOrderApiRequest          (Query)

❌ OrderRequest, CreateOrder, OrderDTO
```

#### 4.3.2 Response DTO 네이밍

**규칙**: `*ApiResponse` 접미사 필수

```java
@Test
@DisplayName("Response DTO는 *ApiResponse 네이밍을 따라야 함")
void responseDtoShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..dto.response")
        .should().haveSimpleNameEndingWith("ApiResponse")
        .because("{Domain}ApiResponse 네이밍 규칙을 따라야 합니다");

    rule.check(restApiClasses);
}
```

**네이밍 패턴**:
```java
✅ OrderApiResponse
✅ ProductApiResponse
✅ PaymentApiResponse

❌ OrderResponse, Order, OrderDTO
```

#### 4.3.3 Java Record 사용 (Zero-Tolerance)

**규칙**: Request/Response DTO는 **반드시** Java 21 Record 사용

```java
@Test
@DisplayName("Request/Response DTO는 Java Record여야 함")
void dtoShouldBeRecord() {
    ArchRule requestRule = classes()
        .that().resideInAPackage("..dto..")
        .and().haveSimpleNameEndingWith("ApiRequest")
        .should().beRecords()
        .because("Java 21 Record를 사용해야 합니다 (불변성 보장)");

    ArchRule responseRule = classes()
        .that().resideInAPackage("..dto..")
        .and().haveSimpleNameEndingWith("ApiResponse")
        .should().beRecords()
        .because("Java 21 Record를 사용해야 합니다 (불변성 보장)");

    requestRule.check(restApiClasses);
    responseRule.check(restApiClasses);
}
```

**올바른 예시**:
```java
// ✅ Record 사용
public record CreateOrderApiRequest(
    @NotNull Long productId,
    @Min(1) int quantity
) {
    // Compact Constructor (Validation)
    public CreateOrderApiRequest {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}

public record OrderApiResponse(
    Long id,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount
) { }
```

❌ **잘못된 예시**:
```java
// ❌ Class 사용 (금지)
public class CreateOrderApiRequest {
    private Long productId;
    private int quantity;

    // getter, setter, constructor 수동 작성 (Record가 자동 생성)
}
```

**Record의 장점**:
- 🔒 **불변성**: 모든 필드 `final` (자동)
- 🧪 **테스트 가능성**: `equals()`, `hashCode()` 자동 생성
- 📝 **간결성**: Boilerplate 코드 제거
- 🎯 **명시성**: DTO의 본질 (데이터 전송) 명확히 표현

#### 4.3.4 Query Parameter DTO - isOffsetBased()

**규칙**: Pagination 전략 판별 메서드 필수

```java
@Test
@DisplayName("Query Parameter DTO는 isOffsetBased() 메서드를 가져야 함")
void queryParamDtoShouldHaveIsOffsetBasedMethod() {
    ArchRule rule = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..dto.request")
        .and().areDeclaredInClassesThat().haveSimpleNameContaining("Search")
        .and().haveName("isOffsetBased")
        .should().bePublic()
        .andShould().haveRawReturnType(boolean.class)
        .because("Pagination 전략 판별을 위해 isOffsetBased() 필요");

    rule.check(restApiClasses);
}
```

**올바른 예시**:
```java
public record SearchOrderApiRequest(
    @Min(0) Integer page,      // Offset-based (page)
    @Min(1) @Max(100) Integer size,
    Long cursor,               // Cursor-based (cursor)
    SortDirection sortDirection
) {
    /**
     * Pagination 전략 판별
     *
     * @return true: Offset-based (page != null), false: Cursor-based (cursor != null)
     */
    public boolean isOffsetBased() {
        return page != null;
    }

    /**
     * Application Layer Query로 변환
     */
    public OrderQuery toQuery() {
        if (isOffsetBased()) {
            return OrderQuery.ofOffset(page, size, sortDirection);
        }
        return OrderQuery.ofCursor(cursor, size, sortDirection);
    }
}
```

#### 4.3.5 Query Parameter DTO - toQuery()

**규칙**: Application Layer Query 변환 메서드 필수

```java
@Test
@DisplayName("Query Parameter DTO는 toQuery() 메서드를 가져야 함")
void queryParamDtoShouldHaveToQueryMethod() {
    ArchRule rule = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..dto.request")
        .and().areDeclaredInClassesThat().haveSimpleNameContaining("Search")
        .and().haveName("toQuery")
        .should().bePublic()
        .because("Application Layer Query로 변환하기 위해 toQuery() 필요");

    rule.check(restApiClasses);
}
```

**설명**:
- REST API DTO → Application Layer Query 변환
- Mapper 대신 DTO 내부에서 변환 (응집도 향상)

---

### 4.4 Mapper 컨벤션 (4개 테스트)

#### 4.4.1 네이밍 규칙

**규칙**: `*ApiMapper` 또는 `*ApiErrorMapper` 접미사 필수

```java
@Test
@DisplayName("Mapper는 *ApiMapper 네이밍을 따라야 함")
void mapperShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..mapper")
        .and().areNotInterfaces()
        .and().areNotMemberClasses()  // 내부 클래스 제외
        .and().areNotEnums()
        .should().haveSimpleNameEndingWith("ApiMapper")
        .orShould().haveSimpleNameEndingWith("ApiErrorMapper")
        .because("{Domain}ApiMapper 네이밍 규칙을 따라야 합니다");

    rule.check(restApiClasses);
}
```

**네이밍 패턴**:
```java
✅ OrderApiMapper
✅ ProductApiMapper
✅ PaymentApiErrorMapper

❌ OrderMapper, OrderConverter, OrderTransformer
```

#### 4.4.2 final 클래스 (Utility Class)

**규칙**: Mapper는 `final` 클래스 (상속 금지)

```java
@Test
@DisplayName("Mapper는 final 클래스여야 함")
void mapperShouldBeFinalClass() {
    ArchRule rule = classes()
        .that().resideInAPackage("..mapper")
        .and().haveSimpleNameEndingWith("ApiMapper")
        .and().areNotInterfaces()
        .should().haveModifier(JavaModifier.FINAL)
        .because("Utility 클래스이므로 final이어야 합니다 (상속 금지)");

    rule.check(restApiClasses);
}
```

#### 4.4.3 private 생성자

**규칙**: Mapper는 `private` 생성자 (인스턴스화 방지)

```java
@Test
@DisplayName("Mapper는 private 생성자를 가져야 함")
void mapperShouldHavePrivateConstructor() {
    ArchRule rule = constructors()
        .that().areDeclaredInClassesThat().resideInAPackage("..mapper")
        .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("ApiMapper")
        .and().areDeclaredInClassesThat().areNotInterfaces()
        .should().bePrivate()
        .because("인스턴스 생성을 방지하기 위해 private 생성자 필요");

    rule.check(restApiClasses);
}
```

#### 4.4.4 static 메서드

**규칙**: Mapper의 모든 public 메서드는 `static`

```java
@Test
@DisplayName("Mapper의 모든 메서드는 static이어야 함")
void mapperMethodsShouldBeStatic() {
    ArchRule rule = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..mapper")
        .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("ApiMapper")
        .and().areDeclaredInClassesThat().areNotInterfaces()
        .and().arePublic()
        .and().doNotHaveName("<init>")  // 생성자 제외
        .should().beStatic()
        .because("Stateless여야 하므로 모든 메서드가 static이어야 함");

    rule.check(restApiClasses);
}
```

#### 올바른 Mapper 예시

```java
// ✅ Utility Class 패턴
public final class OrderApiMapper {  // ✅ final

    private OrderApiMapper() {  // ✅ private 생성자
        throw new UnsupportedOperationException("Utility class");
    }

    // ✅ static 메서드
    public static PlaceOrderCommand toCommand(CreateOrderApiRequest request) {
        return PlaceOrderCommand.builder()
            .productId(request.productId())
            .quantity(request.quantity())
            .build();
    }

    public static OrderApiResponse toResponse(OrderResult result) {
        return new OrderApiResponse(
            result.id(),
            result.orderNumber(),
            result.status(),
            result.totalAmount()
        );
    }
}
```

❌ **잘못된 예시**:
```java
// ❌ 인스턴스화 가능 (금지)
public class OrderApiMapper {  // ❌ final 누락

    public OrderApiMapper() { }  // ❌ public 생성자

    // ❌ static 누락
    public PlaceOrderCommand toCommand(CreateOrderApiRequest request) {
        // ...
    }
}
```

---

### 4.5 Error Mapper 컨벤션 (3개 테스트)

#### 4.5.1 네이밍 규칙

**규칙**: `*ApiErrorMapper` 접미사 필수

```java
@Test
@DisplayName("Error Mapper는 *ApiErrorMapper 네이밍을 따라야 함")
void errorMapperShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..error")
        .and().areAnnotatedWith(Component.class)
        .should().haveSimpleNameEndingWith("ApiErrorMapper")
        .because("{Domain}ApiErrorMapper 네이밍 규칙을 따라야 합니다");

    rule.check(restApiClasses);
}
```

#### 4.5.2 @Component 필수

**규칙**: Error Mapper는 Spring Bean 등록 필수

```java
@Test
@DisplayName("Error Mapper는 @Component 어노테이션을 가져야 함")
void errorMapperShouldHaveComponentAnnotation() {
    ArchRule rule = classes()
        .that().resideInAPackage("..error")
        .and().haveSimpleNameEndingWith("ApiErrorMapper")
        .should().beAnnotatedWith(Component.class)
        .because("Spring Bean으로 등록되어야 합니다");

    rule.check(restApiClasses);
}
```

#### 4.5.3 ErrorMapper 인터페이스 구현

**규칙**: Error Mapper는 `ErrorMapper` 인터페이스 구현 필수

```java
@Test
@DisplayName("Error Mapper는 ErrorMapper 인터페이스를 구현해야 함")
void errorMapperShouldImplementErrorMapperInterface() {
    ArchRule rule = classes()
        .that().resideInAPackage("..error")
        .and().haveSimpleNameEndingWith("ApiErrorMapper")
        .should().implement("com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper")
        .because("ErrorMapper 인터페이스를 구현해야 합니다");

    rule.check(restApiClasses);
}
```

#### 올바른 Error Mapper 예시

**ErrorMapper 인터페이스**:
```java
public interface ErrorMapper {
    boolean supports(String code);
    MappedError map(DomainException ex, Locale locale);

    record MappedError(HttpStatus status, String title, String detail, URI type) { }
}
```

**구현체**:
```java
@Component  // ✅ Spring Bean
public class OrderApiErrorMapper implements ErrorMapper {  // ✅ 인터페이스 구현

    private final MessageSource messageSource;

    public OrderApiErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String code) {
        return code.startsWith("ORDER.");  // ORDER.NOT_FOUND, ORDER.ALREADY_CANCELLED
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        String code = ex.getCode();
        HttpStatus status = switch (code) {
            case "ORDER.NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ORDER.ALREADY_CANCELLED" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        String title = messageSource.getMessage(
            "error.order." + code,
            null,
            locale
        );

        return new MappedError(status, title, ex.getMessage(), URI.create("about:blank"));
    }
}
```

---

### 4.6 Properties 컨벤션 (3개 테스트)

#### 4.6.1 네이밍 규칙

**규칙**: `*Properties` 접미사 필수

```java
@Test
@DisplayName("Properties는 *Properties 네이밍을 따라야 함")
void propertiesShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..config.properties")
        .and().areNotMemberClasses()  // 내부 클래스 제외
        .should().haveSimpleNameEndingWith("Properties")
        .because("{Feature}Properties 네이밍 규칙을 따라야 합니다");

    rule.check(restApiClasses);
}
```

#### 4.6.2 @Component 필수

**규칙**: Properties는 Spring Bean 등록 필수

```java
@Test
@DisplayName("Properties는 @Component 어노테이션을 가져야 함")
void propertiesShouldHaveComponentAnnotation() {
    ArchRule rule = classes()
        .that().resideInAPackage("..config.properties")
        .and().haveSimpleNameEndingWith("Properties")
        .should().beAnnotatedWith(Component.class)
        .because("Spring Bean으로 등록되어야 합니다");

    rule.check(restApiClasses);
}
```

#### 4.6.3 @ConfigurationProperties 필수

**규칙**: YAML 바인딩을 위한 `@ConfigurationProperties` 필수

```java
@Test
@DisplayName("Properties는 @ConfigurationProperties 어노테이션을 가져야 함")
void propertiesShouldHaveConfigurationPropertiesAnnotation() {
    ArchRule rule = classes()
        .that().resideInAPackage("..config.properties")
        .and().haveSimpleNameEndingWith("Properties")
        .should().beAnnotatedWith(
            "org.springframework.boot.context.properties.ConfigurationProperties"
        )
        .because("@ConfigurationProperties로 YAML 바인딩을 설정해야 합니다");

    rule.check(restApiClasses);
}
```

#### 올바른 Properties 예시

**application.yml**:
```yaml
api:
  endpoints:
    base-v1: /api/v1
    order:
      base: /orders
      by-id: /{id}
  error:
    base-url: about:blank
    use-about-blank: true
```

**ApiEndpointProperties.java**:
```java
@Component  // ✅ Spring Bean
@ConfigurationProperties(prefix = "api.endpoints")  // ✅ YAML 바인딩
public class ApiEndpointProperties {  // ✅ *Properties 네이밍

    private String baseV1;
    private OrderEndpoints order;
    private ErrorEndpoints error;

    // Getter/Setter (Pure Java)

    public static class OrderEndpoints {
        private String base;
        private String byId;
        // Getter/Setter
    }

    public static class ErrorEndpoints {
        private String baseUrl;
        private boolean useAboutBlank;
        // Getter/Setter
    }
}
```

---

## 5. Layer 의존성 규칙 테스트

### 5.1 패키지 구조 검증

**규칙**: REST API Layer는 정의된 패키지 구조를 따라야 함

```java
@Test
@DisplayName("REST API Layer는 정의된 패키지 구조를 따라야 함")
void restApiLayerShouldFollowPackageStructure() {
    ArchRule rule = classes()
        .that().resideInAPackage("..adapter.in.rest..")
        .should().resideInAnyPackage(
            "..adapter.in.rest.common..",
            "..adapter.in.rest.config..",
            "..adapter.in.rest..controller..",
            "..adapter.in.rest..dto..",
            "..adapter.in.rest..mapper..",
            "..adapter.in.rest..error.."
        )
        .because("controller, dto, mapper, error 패키지 구조를 따라야 합니다");

    rule.check(importedClasses);
}
```

**올바른 패키지 구조**:
```
adapter-in/rest-api/src/main/java/com/ryuqq/adapter/in/rest/
├── common/               # 공통 DTO, GlobalExceptionHandler
│   ├── controller/       # GlobalExceptionHandler
│   ├── dto/              # ApiResponse<T>, ErrorInfo
│   └── mapper/           # ErrorMapper interface
├── config/               # Configuration
│   ├── properties/       # Properties classes
│   └── ErrorHandlingConfig.java
└── {boundedContext}/     # Bounded Context (예: order, product)
    ├── controller/       # OrderController
    ├── dto/
    │   ├── request/      # *ApiRequest
    │   └── response/     # *ApiResponse
    ├── mapper/           # *ApiMapper (Utility class)
    └── error/            # *ApiErrorMapper (ErrorMapper 구현체)
```

### 5.2 Request DTO 의존성 규칙

**규칙**: Request DTO는 Application Layer DTO에 의존하지 않음

```java
@Test
@DisplayName("Request DTO는 Application Layer DTO에 의존하지 않아야 함")
void requestDtosShouldNotDependOnApplicationLayerDtos() {
    ArchRule rule = classes()
        .that().resideInAPackage("..dto.request..")
        .should().onlyDependOnClassesThat()
        .resideOutsideOfPackages("..application..")
        .because("REST API Request DTO는 Application Layer DTO와 독립적이어야 합니다");

    rule.check(importedClasses);
}
```

**이유**:
- REST API DTO와 Application Layer DTO는 **독립적으로 진화**
- API 버전 관리 용이 (v1, v2 API가 동일한 Application Layer 사용 가능)
- **Mapper를 통한 변환** (toCommand(), toQuery())

**올바른 예시**:
```java
// ✅ REST API Request DTO (독립적)
public record CreateOrderApiRequest(
    Long productId,
    int quantity
) {
    // Application Layer Command로 변환
    public PlaceOrderCommand toCommand() {
        return PlaceOrderCommand.builder()
            .productId(productId)
            .quantity(quantity)
            .build();
    }
}
```

### 5.3 Mapper 의존성 규칙

**규칙**: Mapper는 Application Layer와 Domain Layer에 의존 가능

```java
@Test
@DisplayName("Mapper는 Application Layer와 Domain Layer에 의존할 수 있음")
void mappersShouldBeAbleToAccessApplicationAndDomainLayer() {
    ArchRule rule = classes()
        .that().resideInAPackage("..mapper..")
        .and().haveSimpleNameEndingWith("ApiMapper")
        .should().onlyAccessClassesThat()
        .resideInAnyPackage(
            "..application..",       // Application Layer (Command, Query, Result)
            "..domain..",            // Domain Layer (Exception, ErrorCode)
            "..adapter.in.rest..",   // REST API Layer (자신의 Layer)
            "java..",                // Java 표준 라이브러리
            "org.springframework.."  // Spring Framework
        )
        .because("REST API Mapper는 Application/Domain Layer에 의존 가능");

    rule.check(importedClasses);
}
```

**의존성 방향**:
```
REST API Mapper → Application Layer Command/Query/Result
                → Domain Layer Exception/ErrorCode
```

### 5.4 Controller 의존성 규칙 (Zero-Tolerance)

**규칙**: Controller는 **오직** Application Layer 포트에만 의존

```java
@Test
@DisplayName("Controller는 Application Layer 포트에만 의존해야 함")
void controllersShouldOnlyDependOnApplicationPorts() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller..")
        .and().haveSimpleNameEndingWith("Controller")
        .should().onlyAccessClassesThat()
        .resideInAnyPackage(
            "..application..",          // Application Layer (UseCase, Facade)
            "..adapter.in.rest..",      // REST API Layer (자신의 Layer)
            "..domain..",               // Domain Layer (Exception, ErrorCode)
            "java..",                   // Java 표준 라이브러리
            "org.springframework..",    // Spring Framework
            "org.slf4j..",              // Logging
            "jakarta.validation.."      // Validation
        )
        .because("Controller는 Application Layer 포트를 통해서만 비즈니스 로직에 접근");

    rule.check(importedClasses);
}
```

**헥사고날 아키텍처 (Ports & Adapters)**:
```
┌─────────────────────────────────────────────┐
│ REST API Adapter (Controller)               │
│  - HTTP 요청/응답 처리                        │
└─────────────────────────────────────────────┘
           ↓ (의존)
┌─────────────────────────────────────────────┐
│ Application Layer (UseCase Port)             │
│  - 비즈니스 로직 조율                          │
└─────────────────────────────────────────────┘
           ↓ (의존)
┌─────────────────────────────────────────────┐
│ Domain Layer                                 │
│  - 핵심 비즈니스 로직                          │
└─────────────────────────────────────────────┘
```

**❌ 금지 사항**:
- Controller → Persistence Layer 직접 접근 (**절대 금지**)
- Controller → 다른 Adapter 직접 접근 (**절대 금지**)

---

## 6. 실행 및 검증

### 6.1 Gradle 실행

#### 전체 아키텍처 테스트 실행
```bash
# 모든 ArchUnit 테스트 실행 (LayerRules + Convention)
./gradlew test --tests "*ArchitectureTest"
```

#### 특정 테스트 실행
```bash
# REST API Layer 규칙만 실행
./gradlew test --tests RestApiLayerRulesTest

# REST API Adapter 컨벤션만 실행
./gradlew test --tests RestApiAdapterConventionTest

# 특정 카테고리만 실행
./gradlew test --tests "RestApiAdapterConventionTest\$LombokProhibitionTest"
./gradlew test --tests "RestApiAdapterConventionTest\$ControllerConventionTest"
./gradlew test --tests "RestApiAdapterConventionTest\$DtoConventionTest"
```

### 6.2 빌드 시 자동 검증

**`build.gradle`** 설정으로 빌드 시 자동 실행:
```gradle
tasks.named('test') {
    useJUnitPlatform()

    // ArchUnit 테스트 자동 실행
    filter {
        includeTestsMatching "*ArchitectureTest"
        includeTestsMatching "*ConventionTest"
    }
}
```

**결과**:
- ✅ 규칙 준수 시: 빌드 성공
- ❌ 규칙 위반 시: 빌드 실패 (커밋 불가)

### 6.3 IDE에서 실행 (IntelliJ IDEA)

1. **테스트 파일 열기**: `RestApiAdapterConventionTest.java`
2. **실행**:
   - 클래스 레벨: 전체 25개 테스트 실행
   - Nested 클래스 레벨: 특정 카테고리만 실행 (예: Lombok 금지 4개)
   - 메서드 레벨: 개별 테스트 실행
3. **결과 확인**: 실패한 규칙 상세 메시지 확인

---

## 7. 위반 사례 및 수정

### 7.1 Lombok 위반

#### ❌ 위반 사례
```java
@Data  // ❌ Lombok 금지
public class CreateOrderApiRequest {
    private Long productId;
    private int quantity;
}
```

#### ArchUnit 오류 메시지
```
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'no classes that reside in a package '..adapter.rest..' should be annotated with @lombok.Data' was violated (1 times):
Class <com.ryuqq.adapter.in.rest.order.dto.request.CreateOrderApiRequest> is annotated with @Data in (CreateOrderApiRequest.java:5)
```

#### ✅ 수정 방법
```java
// Pure Java Record 사용
public record CreateOrderApiRequest(
    @NotNull Long productId,
    @Min(1) int quantity
) { }
```

---

### 7.2 Controller 네이밍 위반

#### ❌ 위반 사례
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderApi {  // ❌ *Controller 네이밍 위반
    // ...
}
```

#### ArchUnit 오류 메시지
```
java.lang.AssertionError: Architecture Violation - Rule 'classes that reside in a package '..controller' and are annotated with @RestController should have simple name ending with 'Controller'' was violated (1 times):
Class <com.ryuqq.adapter.in.rest.order.controller.OrderApi> does not have simple name ending with 'Controller' in (OrderApi.java:8)
```

#### ✅ 수정 방법
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {  // ✅ 올바른 네이밍
    // ...
}
```

---

### 7.3 DTO Record 위반

#### ❌ 위반 사례
```java
public class OrderApiResponse {  // ❌ Class 사용 (Record 위반)
    private Long id;
    private String orderNumber;

    // Getter, Constructor...
}
```

#### ArchUnit 오류 메시지
```
java.lang.AssertionError: Architecture Violation - Rule 'classes that reside in a package '..dto..' and have simple name ending with 'ApiResponse' should be records' was violated (1 times):
Class <com.ryuqq.adapter.in.rest.order.dto.response.OrderApiResponse> is not a record in (OrderApiResponse.java:5)
```

#### ✅ 수정 방법
```java
public record OrderApiResponse(  // ✅ Record 사용
    Long id,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount
) { }
```

---

### 7.4 Mapper Utility Class 위반

#### ❌ 위반 사례
```java
public class OrderApiMapper {  // ❌ final 누락, 인스턴스화 가능

    public OrderApiMapper() { }  // ❌ public 생성자

    public PlaceOrderCommand toCommand(CreateOrderApiRequest request) {  // ❌ static 누락
        // ...
    }
}
```

#### ArchUnit 오류 메시지
```
java.lang.AssertionError: Architecture Violation [Multiple violations]:
1. Class <OrderApiMapper> is not final
2. Constructor <OrderApiMapper()> is not private
3. Method <toCommand> is not static
```

#### ✅ 수정 방법
```java
public final class OrderApiMapper {  // ✅ final

    private OrderApiMapper() {  // ✅ private 생성자
        throw new UnsupportedOperationException("Utility class");
    }

    public static PlaceOrderCommand toCommand(CreateOrderApiRequest request) {  // ✅ static
        return PlaceOrderCommand.builder()
            .productId(request.productId())
            .quantity(request.quantity())
            .build();
    }
}
```

---

### 7.5 Controller 의존성 위반 (Zero-Tolerance)

#### ❌ 위반 사례
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderJpaRepository orderRepository;  // ❌ Persistence Layer 직접 접근

    public OrderController(OrderJpaRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderApiResponse> getOrder(@PathVariable Long id) {
        OrderJpaEntity entity = orderRepository.findById(id)  // ❌ Repository 직접 호출
            .orElseThrow(() -> new OrderNotFoundException(id));

        return ResponseEntity.ok(toResponse(entity));
    }
}
```

#### ArchUnit 오류 메시지
```
java.lang.AssertionError: Architecture Violation - Rule 'classes that reside in a package '..controller..' and have simple name ending with 'Controller' should only access classes that reside in any package ['..application..', '..adapter.in.rest..', '..domain..', 'java..', 'org.springframework..']' was violated (1 times):
Method <com.ryuqq.adapter.in.rest.order.controller.OrderController.getOrder> calls method <com.ryuqq.adapter.out.persistence.order.OrderJpaRepository.findById> in (OrderController.java:15)
because Controller는 Application Layer의 포트를 통해서만 비즈니스 로직에 접근해야 합니다
```

#### ✅ 수정 방법
```java
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.order.base}")
public class OrderController {

    private final GetOrderUseCase getOrderUseCase;  // ✅ Application Layer 포트

    public OrderController(GetOrderUseCase getOrderUseCase) {
        this.getOrderUseCase = getOrderUseCase;
    }

    @GetMapping("${api.endpoints.order.by-id}")
    public ResponseEntity<ApiResponse<OrderApiResponse>> getOrder(@PathVariable Long id) {
        OrderQuery query = OrderQuery.ofId(id);
        OrderResult result = getOrderUseCase.execute(query);  // ✅ UseCase 호출

        OrderApiResponse response = OrderApiMapper.toResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## 8. Best Practices

### 8.1 테스트 우선 개발 (Test-Driven Architecture)

**개발 순서**:
1. ArchUnit 테스트 작성 (규칙 정의)
2. 테스트 실행 → 실패 확인
3. 코드 작성 (규칙 준수)
4. 테스트 실행 → 통과 확인

**예시**:
```java
// 1. ArchUnit 테스트 작성
@Test
@DisplayName("Response DTO는 *ApiResponse 네이밍을 따라야 함")
void responseDtoShouldFollowNamingConvention() {
    ArchRule rule = classes()
        .that().resideInAPackage("..dto.response")
        .should().haveSimpleNameEndingWith("ApiResponse");

    rule.check(restApiClasses);  // ❌ 실패 (아직 Response DTO 없음)
}

// 2. Response DTO 작성
public record OrderApiResponse(Long id, String orderNumber) { }

// 3. 테스트 재실행 → ✅ 통과
```

### 8.2 규칙 위반 즉시 수정

**CI/CD 파이프라인**:
```yaml
# GitHub Actions 예시
name: CI Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Build with Gradle
        run: ./gradlew build  # ArchUnit 테스트 자동 실행
```

**결과**:
- PR 생성 시 자동 검증
- ArchUnit 실패 시 Merge 불가

### 8.3 신규 규칙 추가 프로세스

**프로세스**:
1. 규칙 문서화 (`docs/coding_convention/`)
2. ArchUnit 테스트 작성
3. 기존 코드 수정 (규칙 준수)
4. PR 생성 및 리뷰
5. Merge 후 팀 공지

**예시**:
```java
// 신규 규칙: Controller 메서드는 @Operation 어노테이션 필수 (Swagger)
@Test
@DisplayName("Controller 메서드는 @Operation 어노테이션을 가져야 함")
void controllerMethodsShouldHaveOperationAnnotation() {
    ArchRule rule = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..controller")
        .and().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
        .and().arePublic()
        .and().areAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
        .or().areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
        .should().beAnnotatedWith("io.swagger.v3.oas.annotations.Operation")
        .because("Controller 메서드는 Swagger 문서화를 위해 @Operation 필요");

    rule.check(restApiClasses);
}
```

### 8.4 규칙 예외 처리

**예외가 필요한 경우** (매우 드뭄):
- 레거시 시스템 통합
- 외부 라이브러리 제약
- 특수한 기술적 요구사항

**예외 처리 방법**:
```java
@Test
@DisplayName("Mapper는 final 클래스여야 함 (예외: LegacyMapper)")
void mapperShouldBeFinalClass() {
    ArchRule rule = classes()
        .that().resideInAPackage("..mapper")
        .and().haveSimpleNameEndingWith("ApiMapper")
        .and().areNotInterfaces()
        .and().haveSimpleNameNotContaining("Legacy")  // ✅ 예외 추가
        .should().haveModifier(JavaModifier.FINAL)
        .because("Utility 클래스이므로 final이어야 합니다 (상속 금지)");

    rule.check(restApiClasses);
}
```

**문서화 필수**:
```java
/**
 * Legacy Mapper 예외 처리
 *
 * <p><strong>예외 사유:</strong></p>
 * - 외부 라이브러리 (XxxFramework)가 Mapper 상속을 요구
 * - 마이그레이션 계획: 2025년 Q2까지 신규 Mapper로 전환
 *
 * @see <a href="docs/architecture/legacy-mapper-exception.md">Legacy Mapper 예외 처리 문서</a>
 */
```

### 8.5 성능 최적화

**ArchUnit 테스트 성능 개선**:
```java
@BeforeAll
static void setUp() {
    // ✅ 클래스 로딩 1회만 수행 (모든 테스트에서 재사용)
    restApiClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)  // JAR 제외
        .importPackages("com.ryuqq.adapter.in.rest");
}
```

**실행 시간**:
- 25개 테스트: 약 2-3초 (충분히 빠름)
- 캐싱으로 재실행 시 더 빠름

---

## 요약

### ArchUnit 테스트 체크리스트

#### Lombok 금지 (4개)
- [ ] `@Data` 금지
- [ ] `@Getter` 금지
- [ ] `@Setter` 금지
- [ ] `@Builder` 금지

#### Controller 컨벤션 (6개)
- [ ] `*Controller` 네이밍
- [ ] `@RestController` 어노테이션
- [ ] `@RequestMapping` 어노테이션
- [ ] 필드 `final` (Constructor Injection)
- [ ] Facade/UseCase 의존성
- [ ] `ResponseEntity` 반환

#### DTO 컨벤션 (5개)
- [ ] Request DTO: `*ApiRequest` 네이밍
- [ ] Response DTO: `*ApiResponse` 네이밍
- [ ] Request/Response DTO는 Java Record
- [ ] Query Parameter DTO: `isOffsetBased()` 메서드
- [ ] Query Parameter DTO: `toQuery()` 메서드

#### Mapper 컨벤션 (4개)
- [ ] `*ApiMapper` 네이밍
- [ ] `final` 클래스 (Utility Class)
- [ ] `private` 생성자
- [ ] 모든 메서드 `static`

#### Error Mapper 컨벤션 (3개)
- [ ] `*ApiErrorMapper` 네이밍
- [ ] `@Component` 어노테이션
- [ ] `ErrorMapper` 인터페이스 구현

#### Properties 컨벤션 (3개)
- [ ] `*Properties` 네이밍
- [ ] `@Component` 어노테이션
- [ ] `@ConfigurationProperties` 어노테이션

#### Layer 의존성 규칙 (3개)
- [ ] 패키지 구조 준수 (controller, dto, mapper, error)
- [ ] Request DTO → Application Layer DTO 의존 금지
- [ ] Controller → Application Layer 포트만 의존 (Persistence 직접 접근 금지)

---

## 참고 문서

### REST API Layer 컨벤션
- [00_IMPLEMENTATION_ROADMAP.md](../00_IMPLEMENTATION_ROADMAP.md) - 전체 로드맵
- [Controller 디자인](../controller-design/) - Controller 설계 가이드
- [DTO 패턴](../dto-patterns/) - Request/Response DTO 가이드
- [Exception Handling](../exception-handling/) - 에러 처리 가이드
- [Mapper 패턴](../mapper-patterns/) - Mapper 설계 가이드
- [Package 가이드](../package-guide/) - 패키지 구조 가이드

### Application Layer
- [Facade 사용 가이드](../../03-application-layer/facade/01_facade-usage-guide.md)

### 외부 링크
- [ArchUnit 공식 문서](https://www.archunit.org/)
- [ArchUnit GitHub](https://github.com/TNG/ArchUnit)
- [Java 21 Record 가이드](https://openjdk.org/jeps/395)

---

**✅ 이 가이드를 따르면 REST API Layer의 100% 규칙 준수를 자동으로 보장할 수 있습니다!**
