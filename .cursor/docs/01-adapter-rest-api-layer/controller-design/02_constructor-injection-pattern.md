# Controller 생성자 주입 패턴

## 개요

REST API Controller는 **생성자 주입(Constructor Injection)** 방식만 사용해야 하며, 필드 주입(`@Autowired`)은 금지됩니다.

---

## 규칙

### ✅ 필수 사항

1. **생성자 주입 사용**: 모든 의존성은 생성자를 통해 주입
2. **`@Autowired` 금지**: 필드에 `@Autowired` 어노테이션 사용 금지
3. **final 키워드 사용**: 주입된 필드는 `final`로 선언하여 불변성 보장

### ❌ 금지 사항

- 필드 주입 (`@Autowired` on fields)
- Setter 주입 (`@Autowired` on setter methods)

---

## 이유

### 1. 의존성 명확화

생성자 주입은 클래스가 필요로 하는 의존성을 명확하게 드러냅니다.

```java
// ✅ 생성자 주입 - 의존성이 명확함
public class ExampleController {
    private final CreateExampleUseCase createExampleUseCase;
    private final GetExampleQueryService getExampleQueryService;

    public ExampleController(
        CreateExampleUseCase createExampleUseCase,
        GetExampleQueryService getExampleQueryService
    ) {
        this.createExampleUseCase = createExampleUseCase;
        this.getExampleQueryService = getExampleQueryService;
    }
}

// ❌ 필드 주입 - 의존성이 숨겨짐
public class ExampleController {
    @Autowired
    private CreateExampleUseCase createExampleUseCase;  // 의존성이 숨겨짐

    @Autowired
    private GetExampleQueryService getExampleQueryService;  // 의존성이 숨겨짐
}
```

### 2. 테스트 용이성

생성자 주입은 Mock 객체를 사용한 단위 테스트가 쉽습니다.

```java
// ✅ 생성자 주입 - Mock 객체 주입 쉬움
@Test
void testCreateExample() {
    // Given: Mock 객체 생성
    CreateExampleUseCase mockUseCase = mock(CreateExampleUseCase.class);
    ExampleApiMapper mockMapper = mock(ExampleApiMapper.class);

    // Controller에 Mock 주입
    ExampleController controller = new ExampleController(mockUseCase, mockMapper);

    // When & Then: 테스트 수행
    // ...
}

// ❌ 필드 주입 - Reflection 또는 Spring Context 필요
@Test
void testCreateExample() {
    // 필드 주입된 Controller는 Reflection이나 Spring Context 없이 테스트 불가
    // ReflectionTestUtils.setField()를 사용해야 함 (복잡하고 느림)
}
```

### 3. 불변성 보장

생성자 주입은 `final` 키워드와 함께 사용하여 의존성의 불변성을 보장합니다.

```java
// ✅ 생성자 주입 + final - 불변성 보장
public class ExampleController {
    private final CreateExampleUseCase createExampleUseCase;  // 생성 후 변경 불가

    public ExampleController(CreateExampleUseCase createExampleUseCase) {
        this.createExampleUseCase = createExampleUseCase;
    }

    // createExampleUseCase = null;  // 컴파일 에러 - 재할당 불가
}

// ❌ 필드 주입 - 불변성 보장 불가
public class ExampleController {
    @Autowired
    private CreateExampleUseCase createExampleUseCase;  // final 사용 불가

    public void someMethod() {
        createExampleUseCase = null;  // 컴파일 에러 없음 - 재할당 가능 (위험!)
    }
}
```

### 4. 순환 의존성 방지

생성자 주입은 순환 의존성을 컴파일 시점에 발견할 수 있습니다.

```java
// ✅ 생성자 주입 - 순환 의존성 시 컴파일 에러
@RestController
public class ControllerA {
    private final ControllerB controllerB;

    public ControllerA(ControllerB controllerB) {  // 순환 의존성 시 Spring 컨텍스트 로딩 실패
        this.controllerB = controllerB;
    }
}

@RestController
public class ControllerB {
    private final ControllerA controllerA;

    public ControllerB(ControllerA controllerA) {  // 순환 의존성 시 Spring 컨텍스트 로딩 실패
        this.controllerA = controllerA;
    }
}

// ❌ 필드 주입 - 순환 의존성이 런타임에 발생
// Spring이 BeanCurrentlyInCreationException을 던지지만, 이미 애플리케이션이 시작된 후
```

---

## 예시

### ✅ 올바른 패턴

```java
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {

    // 1. 모든 의존성은 final로 선언
    private final CreateExampleUseCase createExampleUseCase;
    private final GetExampleQueryService getExampleQueryService;
    private final SearchExampleQueryService searchExampleQueryService;
    private final ExampleApiMapper mapper;

    // 2. 생성자를 통해 의존성 주입 (Lombok @RequiredArgsConstructor 사용 가능)
    public ExampleController(
        CreateExampleUseCase createExampleUseCase,
        GetExampleQueryService getExampleQueryService,
        SearchExampleQueryService searchExampleQueryService,
        ExampleApiMapper mapper
    ) {
        this.createExampleUseCase = createExampleUseCase;
        this.getExampleQueryService = getExampleQueryService;
        this.searchExampleQueryService = searchExampleQueryService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @RequestBody @Valid ExampleApiRequest request
    ) {
        CreateExampleCommand command = mapper.toCreateCommand(request);
        ExampleResponse response = createExampleUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success(mapper.toApiResponse(response)));
    }
}
```

### ❌ 잘못된 패턴

```java
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {

    // ❌ 필드 주입 사용
    @Autowired
    private CreateExampleUseCase createExampleUseCase;

    @Autowired
    private GetExampleQueryService getExampleQueryService;

    @Autowired
    private ExampleApiMapper mapper;

    // 문제:
    // 1. 의존성이 숨겨져 있음
    // 2. 테스트 시 Mock 주입 어려움
    // 3. final 사용 불가 (불변성 보장 안 됨)
    // 4. 순환 의존성 발견 늦음
}
```

---

## ArchUnit 검증

**위치**: `bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/RestApiLayerRulesTest.java`

```java
@Test
@DisplayName("Controller는 생성자 주입을 사용해야 함 (필드 주입 금지)")
void controllersShouldUseConstructorInjection() {
    ArchRule rule = fields()
        .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
        .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
        .should().notBeAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
        .because("REST API Controller는 필드 주입(@Autowired) 대신 생성자 주입을 사용해야 합니다.");

    rule.check(importedClasses);
}
```

### 위반 시 에러 메시지

```
Architecture Violation [Priority: MEDIUM] - Rule 'fields that are declared in classes that reside in a package '..controller..' and are declared in classes that have simple name ending with 'Controller' should not be annotated with @Autowired' was violated (1 times):
Field <com.ryuqq.adapter.in.rest.example.controller.ExampleController.createExampleUseCase> is annotated with @Autowired in (ExampleController.java:15)
```

---

## 체크리스트

### Controller 생성자 주입 체크리스트

- [ ] 모든 의존성 필드는 `final`로 선언
- [ ] 생성자를 통해서만 의존성 주입
- [ ] 필드에 `@Autowired` 어노테이션 사용 안 함
- [ ] Setter 메서드에 `@Autowired` 사용 안 함
- [ ] 생성자에 필요한 모든 의존성 명시

---

## 참고

### Spring 공식 문서

- [Constructor-based Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection)
- [Field vs Constructor Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-setter-injection)

### 관련 규칙

- [Controller 의존성 규칙](./03_controller-dependency-rules.md) - Application Layer 포트에만 의존
- [CQRS 포트 네이밍](./04_cqrs-port-naming.md) - UseCase/QueryService 네이밍 규칙

---

## 요약

| 항목 | 생성자 주입 | 필드 주입 |
|------|-----------|----------|
| **의존성 명확성** | ✅ 명확함 | ❌ 숨겨짐 |
| **테스트 용이성** | ✅ 쉬움 (Mock 주입) | ❌ 어려움 (Reflection 필요) |
| **불변성 보장** | ✅ `final` 사용 가능 | ❌ `final` 사용 불가 |
| **순환 의존성 감지** | ✅ 빠름 (컴파일 시) | ❌ 늦음 (런타임) |
| **ArchUnit 검증** | ✅ 자동 검증 | - |

**✅ 생성자 주입은 필수이며, ArchUnit으로 자동 검증됩니다. (Zero-Tolerance)**
