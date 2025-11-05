# OpenAPI/Swagger 문서화 컨벤션

## 개요

REST API Controller는 **OpenAPI (Swagger) 어노테이션을 필수로 사용**하여 API 문서를 자동 생성해야 합니다.

---

## 필수 어노테이션

### 1. Controller 클래스 레벨: `@Tag`

모든 Controller 클래스는 `@Tag` 어노테이션을 사용하여 API 그룹을 정의해야 합니다.

```java
@Tag(
    name = "Example API",
    description = "Example 도메인 관리 API. CQRS 패턴 적용 (Command/Query 분리)"
)
@RestController
@RequestMapping("${api.endpoints.base-v1}")
public class ExampleController {
    // ...
}
```

**필수 속성**:
- `name`: API 그룹 이름 (Swagger UI에 표시됨)
- `description`: API 그룹 설명 (아키텍처 패턴, 주요 기능 설명)

### 2. HTTP 메서드 레벨: `@Operation`

모든 HTTP 매핑 메서드는 `@Operation` 어노테이션을 사용해야 합니다.

```java
@Operation(
    summary = "Example 생성",
    description = "새로운 Example을 생성합니다. (CQRS Command)",
    responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "생성 성공",
            content = @Content(schema = @Schema(implementation = ExampleApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (Validation 실패)"
        )
    }
)
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
    @Parameter(description = "Example 생성 요청", required = true)
    @RequestBody @Valid ExampleApiRequest request
) {
    // ...
}
```

**필수 속성**:
- `summary`: API 동작 요약 (간단명료하게)
- `description`: 상세 설명 (Command/Query 구분 명시)
- `responses`: 응답 코드별 설명 (성공/실패 케이스)

### 3. 파라미터 레벨: `@Parameter`

`@PathVariable`, `@RequestParam` 파라미터에는 `@Parameter` 어노테이션을 사용합니다.

```java
@GetMapping("${api.endpoints.example.base}${api.endpoints.example.by-id}")
public ResponseEntity<ApiResponse<ExampleDetailApiResponse>> getExample(
    @Parameter(description = "Example ID (양수)", required = true, example = "123")
    @PathVariable @Positive Long id
) {
    // ...
}
```

**필수 속성**:
- `description`: 파라미터 설명
- `required`: 필수 여부
- `example`: 예시 값 (선택사항, 권장)

### 4. Request Body: `@Parameter` + `@RequestBody`

Request Body에도 `@Parameter` 설명을 추가합니다.

```java
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
    @Parameter(description = "Example 생성 요청", required = true)
    @RequestBody @Valid ExampleApiRequest request
) {
    // ...
}
```

---

## DTO 레벨: `@Schema`

Request/Response DTO에는 `@Schema` 어노테이션을 사용합니다.

```java
@Schema(description = "Example 생성 요청 DTO")
public record ExampleApiRequest(

    @Schema(description = "메시지 내용", example = "Hello World", required = true)
    @NotBlank(message = "메시지는 필수입니다")
    String message

) {
}
```

**필수 속성**:
- `description`: 필드 설명
- `example`: 예시 값 (Swagger UI에서 Try it out 시 사용)
- `required`: 필수 여부 (record 컴포넌트는 기본적으로 required=true)

---

## Import 충돌 해결

`com.ryuqq.adapter.in.rest.common.dto.ApiResponse`와 `io.swagger.v3.oas.annotations.responses.ApiResponse`의 이름 충돌이 발생합니다.

### ✅ 해결 방법: Fully Qualified Name 사용

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
// ❌ import io.swagger.v3.oas.annotations.responses.ApiResponse; (충돌)

import com.ryuqq.adapter.in.rest.common.dto.ApiResponse; // 우리 프로젝트의 ApiResponse

// ...

@Operation(
    summary = "...",
    responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse( // Fully Qualified Name 사용
            responseCode = "200",
            description = "성공"
        )
    }
)
```

---

## 전체 예시

### Controller 예시

```java
@Tag(
    name = "Example API",
    description = "Example 도메인 관리 API. CQRS 패턴 적용 (Command/Query 분리)"
)
@RestController
@RequestMapping("${api.endpoints.base-v1}")
@Validated
public class ExampleController {

    private final CreateExampleUseCase createExampleUseCase;
    private final GetExampleQueryService getExampleQueryService;
    private final ExampleApiMapper exampleApiMapper;

    public ExampleController(
        CreateExampleUseCase createExampleUseCase,
        GetExampleQueryService getExampleQueryService,
        ExampleApiMapper exampleApiMapper
    ) {
        this.createExampleUseCase = createExampleUseCase;
        this.getExampleQueryService = getExampleQueryService;
        this.exampleApiMapper = exampleApiMapper;
    }

    /**
     * Example을 생성합니다. (CQRS Command)
     */
    @Operation(
        summary = "Example 생성",
        description = "새로운 Example을 생성합니다. (CQRS Command)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content = @Content(schema = @Schema(implementation = ExampleApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (Validation 실패)"
            )
        }
    )
    @PostMapping("${api.endpoints.example.base}")
    public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @Parameter(description = "Example 생성 요청", required = true)
        @RequestBody @Valid ExampleApiRequest request
    ) {
        CreateExampleCommand command = exampleApiMapper.toCreateCommand(request);
        ExampleResponse result = createExampleUseCase.execute(command);
        ExampleApiResponse response = exampleApiMapper.toApiResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(response));
    }

    /**
     * Example ID로 단건 조회합니다. (CQRS Query)
     */
    @Operation(
        summary = "Example 단건 조회",
        description = "Example ID로 상세 정보를 조회합니다. (CQRS Query)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ExampleDetailApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Example을 찾을 수 없음"
            )
        }
    )
    @GetMapping("${api.endpoints.example.base}${api.endpoints.example.by-id}")
    public ResponseEntity<ApiResponse<ExampleDetailApiResponse>> getExample(
        @Parameter(description = "Example ID (양수)", required = true, example = "123")
        @PathVariable @Positive Long id
    ) {
        var query = exampleApiMapper.toGetQuery(id);
        var appResponse = getExampleQueryService.getById(query);
        var apiResponse = exampleApiMapper.toDetailApiResponse(appResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
```

### Request DTO 예시

```java
@Schema(description = "Example 생성 요청 DTO")
public record ExampleApiRequest(

    @Schema(
        description = "메시지 내용",
        example = "Hello World",
        required = true,
        minLength = 1,
        maxLength = 500
    )
    @NotBlank(message = "메시지는 필수입니다")
    @Size(min = 1, max = 500, message = "메시지는 1~500자 이내여야 합니다")
    String message

) {
}
```

### Response DTO 예시

```java
@Schema(description = "Example 생성 응답 DTO")
public record ExampleApiResponse(

    @Schema(description = "메시지 내용", example = "Hello World")
    String message

) {
    public static ExampleApiResponse fromResponse(String message) {
        return new ExampleApiResponse(message);
    }
}
```

---

## Swagger UI 접근

SpringDoc OpenAPI가 설정되면 다음 URL에서 Swagger UI에 접근할 수 있습니다:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON 스펙:
```
http://localhost:8080/v3/api-docs
```

---

## ArchUnit 검증

OpenAPI 어노테이션 사용을 강제하기 위한 ArchUnit 테스트:

```java
@Test
@DisplayName("Controller는 @Tag 어노테이션을 가져야 함")
void controllersShouldBeAnnotatedWithTag() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller..")
        .and().haveSimpleNameEndingWith("Controller")
        .and().areAnnotatedWith(RestController.class)
        .should().beAnnotatedWith(Tag.class)
        .because("REST API Controller는 OpenAPI 문서화를 위해 @Tag 어노테이션을 사용해야 합니다.");

    rule.check(importedClasses);
}

@Test
@DisplayName("HTTP 매핑 메서드는 @Operation 어노테이션을 가져야 함")
void httpMappingMethodsShouldBeAnnotatedWithOperation() {
    ArchRule rule = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
        .and().arePublic()
        .and().areAnnotatedWith(GetMapping.class)
        .or().areAnnotatedWith(PostMapping.class)
        .or().areAnnotatedWith(PutMapping.class)
        .or().areAnnotatedWith(DeleteMapping.class)
        .or().areAnnotatedWith(PatchMapping.class)
        .should().beAnnotatedWith(Operation.class)
        .because("REST API 메서드는 OpenAPI 문서화를 위해 @Operation 어노테이션을 사용해야 합니다.");

    rule.check(importedClasses);
}
```

---

## 체크리스트

### OpenAPI 문서화 체크리스트

- [ ] Controller 클래스에 `@Tag` 어노테이션 추가
- [ ] 모든 HTTP 매핑 메서드에 `@Operation` 어노테이션 추가
- [ ] 각 `@Operation`에 `summary`, `description`, `responses` 명시
- [ ] PathVariable/RequestParam에 `@Parameter` 어노테이션 추가
- [ ] Request/Response DTO에 `@Schema` 어노테이션 추가
- [ ] 각 DTO 필드에 `@Schema` 설명 및 `example` 추가
- [ ] Import 충돌 시 Fully Qualified Name 사용
- [ ] Swagger UI에서 API 문서 확인 (`/swagger-ui.html`)

---

## 이점

### 1. 자동 API 문서 생성

OpenAPI 어노테이션을 사용하면 Swagger UI가 자동으로 생성됩니다.

- 개발자가 수동으로 API 문서를 작성할 필요 없음
- 코드와 문서가 항상 동기화됨
- 프론트엔드 개발자가 즉시 API 스펙 확인 가능

### 2. Try it out 기능

Swagger UI에서 직접 API를 테스트할 수 있습니다.

- 브라우저에서 바로 API 호출 가능
- Request Body 예시 자동 생성
- Response 결과 실시간 확인

### 3. 클라이언트 코드 생성

OpenAPI 스펙을 기반으로 클라이언트 코드를 자동 생성할 수 있습니다.

```bash
# TypeScript 클라이언트 생성
openapi-generator-cli generate -i http://localhost:8080/v3/api-docs -g typescript-axios -o ./client
```

---

## 참고

### SpringDoc OpenAPI 공식 문서

- [SpringDoc OpenAPI](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)

### 관련 규칙

- [DTO 네이밍 규칙](../dto-patterns/03_naming-conventions.md) - ApiRequest/ApiResponse 접미사
- [Controller 생성자 주입](../controller-design/02_constructor-injection-pattern.md) - 의존성 주입 패턴

---

## 요약

| 레벨 | 필수 어노테이션 | 목적 |
|------|---------------|------|
| **Controller 클래스** | `@Tag` | API 그룹 정의 |
| **HTTP 메서드** | `@Operation` | API 동작 설명, 응답 코드 정의 |
| **파라미터** | `@Parameter` | PathVariable/RequestParam 설명 |
| **Request Body** | `@Parameter` | Request Body 설명 |
| **DTO 클래스** | `@Schema` | DTO 전체 설명 |
| **DTO 필드** | `@Schema` | 필드별 설명 및 예시 |

**✅ OpenAPI 어노테이션은 필수이며, ArchUnit으로 자동 검증됩니다. (Zero-Tolerance)**

**✅ Swagger UI는 `/swagger-ui.html`에서 접근 가능하며, API 문서와 테스트 도구를 제공합니다.**
