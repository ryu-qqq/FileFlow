# REST API Layer Code Generation Prompt (v1.0)

당신은 Spring REST API Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **RESTful 설계**: HTTP Method, Status Code 올바른 사용
- ✅ **RFC 7807 ProblemDetail**: 표준 에러 응답 형식
- ✅ **OpenAPI 문서화**: `@Operation`, `@Tag`, `@Parameter` 필수
- ✅ **Validation**: `@Valid`, `@NotNull` 등 적극 활용
- ✅ **DTO는 Record**: Request/Response 모두 Record 패턴
- ✅ **Mapper는 Component**: `@Component` 클래스 (Utility 아님)
- ✅ **CQRS 완전 분리**: UseCase vs QueryService
- ✅ **경로 중앙 관리**: `application.yml`에서 경로 설정
- ✅ **로깅 전략**: 5xx ERROR, 404 DEBUG, 4xx WARN
- ✅ **ApiResponse Wrapper**: 일관된 응답 형식

## 코드 생성 템플릿

### Controller (OpenAPI 완전 문서화)

```java
/**
 * {Resource} API 컨트롤러 (CQRS 패턴 적용)
 *
 * <p>{Resource} 도메인의 REST API 엔드포인트를 제공합니다.</p>
 * <p>CQRS 패턴을 적용하여 Command와 Query를 분리했습니다.</p>
 *
 * <p><strong>제공하는 API:</strong></p>
 * <ul>
 *   <li>POST /api/v1/{resources} - {Resource} 생성 (Command)</li>
 *   <li>GET /api/v1/{resources}/{id} - {Resource} 단건 조회 (Query)</li>
 *   <li>GET /api/v1/{resources} - {Resource} 검색 (Cursor 기반, Query)</li>
 *   <li>GET /api/v1/admin/{resources}/search - {Resource} 검색 (Offset 기반, Query)</li>
 * </ul>
 *
 * <p><strong>페이지네이션 전략:</strong></p>
 * <ul>
 *   <li>일반 사용자용: Cursor 기반 (무한 스크롤, 고성능)</li>
 *   <li>관리자용: Offset 기반 (페이지 번호, 전체 개수 제공)</li>
 * </ul>
 *
 * <p><strong>엔드포인트 경로 관리:</strong></p>
 * <ul>
 *   <li>application.yml에서 api.endpoints 설정으로 경로 중앙 관리</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@Tag(
    name = "{Resource} API",
    description = "{Resource} 도메인 관리 API. CQRS 패턴 적용 (Command/Query 분리)"
)
@RestController
@RequestMapping("${api.endpoints.base-v1}")
@Validated
public class {Resource}Controller {

    private final Create{Resource}UseCase create{Resource}UseCase;
    private final Get{Resource}QueryService get{Resource}QueryService;
    private final Search{Resource}QueryService search{Resource}QueryService;
    private final {Resource}ApiMapper {resource}ApiMapper;

    /**
     * {Resource}Controller 생성자
     *
     * @param create{Resource}UseCase {Resource} 생성 UseCase
     * @param get{Resource}QueryService {Resource} 조회 Query Service
     * @param search{Resource}QueryService {Resource} 검색 Query Service
     * @param {resource}ApiMapper {Resource} Mapper
     */
    public {Resource}Controller(
            Create{Resource}UseCase create{Resource}UseCase,
            Get{Resource}QueryService get{Resource}QueryService,
            Search{Resource}QueryService search{Resource}QueryService,
            {Resource}ApiMapper {resource}ApiMapper) {
        this.create{Resource}UseCase = create{Resource}UseCase;
        this.get{Resource}QueryService = get{Resource}QueryService;
        this.search{Resource}QueryService = search{Resource}QueryService;
        this.{resource}ApiMapper = {resource}ApiMapper;
    }

    /**
     * {Resource}을 생성합니다. (CQRS Command)
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * POST /api/v1/{resources}
     * {
     *   "name": "Example Name",
     *   "quantity": 100
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP 201 Created
     * {
     *   "success": true,
     *   "data": {
     *     "id": 123,
     *     "name": "Example Name"
     *   }
     * }
     * }</pre>
     *
     * @param request {Resource} 생성 요청 DTO
     * @return {Resource} 생성 결과 (201 Created)
     */
    @Operation(
        summary = "{Resource} 생성",
        description = "새로운 {Resource}을 생성합니다. (CQRS Command)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content = @Content(schema = @Schema(implementation = {Resource}ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (Validation 실패)"
            )
        }
    )
    @PostMapping("${api.endpoints.{resource}.base}")
    public ResponseEntity<ApiResponse<{Resource}ApiResponse>> create{Resource}(
            @Parameter(description = "{Resource} 생성 요청", required = true)
            @RequestBody @Valid {Resource}ApiRequest request) {
        Create{Resource}Command command = {resource}ApiMapper.toCreateCommand(request);
        {Resource}Response result = create{Resource}UseCase.execute(command);
        {Resource}ApiResponse response = {resource}ApiMapper.toApiResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ofSuccess(response));
    }

    /**
     * {Resource} ID로 단건 조회합니다. (CQRS Query)
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * GET /api/v1/{resources}/123
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "id": 123,
     *     "name": "Example Name",
     *     "status": "ACTIVE",
     *     "createdAt": "2025-10-28T10:30:00",
     *     "updatedAt": "2025-10-28T10:30:00"
     *   }
     * }
     * }</pre>
     *
     * @param id {Resource} ID (양수)
     * @return {Resource} 상세 정보 (200 OK)
     */
    @Operation(
        summary = "{Resource} 단건 조회",
        description = "{Resource} ID로 상세 정보를 조회합니다. (CQRS Query)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = {Resource}DetailApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "{Resource}을 찾을 수 없음"
            )
        }
    )
    @GetMapping("${api.endpoints.{resource}.base}${api.endpoints.{resource}.by-id}")
    public ResponseEntity<ApiResponse<{Resource}DetailApiResponse>> get{Resource}(
            @Parameter(description = "{Resource} ID (양수)", required = true, example = "123")
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long id) {
        var query = {resource}ApiMapper.toGetQuery(id);
        var appResponse = get{Resource}QueryService.getById(query);
        var apiResponse = {resource}ApiMapper.toDetailApiResponse(appResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
```

### Request DTO (Record, 단순 Validation)

```java
/**
 * {Resource} 생성 요청 DTO
 *
 * @author Claude Code
 * @since 1.0
 */
public record {Resource}ApiRequest(
    @NotNull(message = "Name cannot be null")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String name,

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity must be positive")
    Integer quantity
) {
    // Compact Constructor는 사용하지 않음 (어노테이션 Validation만 사용)
}
```

### Response DTO (Record, static factory)

```java
/**
 * {Resource} 응답 DTO
 *
 * @author Claude Code
 * @since 1.0
 */
public record {Resource}ApiResponse(
    Long id,
    String name
) {
    /**
     * Application Response → API Response 변환
     */
    public static {Resource}ApiResponse fromResponse({Resource}Response response) {
        return new {Resource}ApiResponse(
            response.id(),
            response.name()
        );
    }
}
```

### Mapper (@Component, 일반 클래스)

```java
/**
 * {Resource}ApiMapper - {Resource} REST API ↔ Application Layer 변환
 *
 * <p>REST API Layer와 Application Layer 간의 DTO 변환을 담당합니다.</p>
 *
 * <p><strong>변환 방향:</strong></p>
 * <ul>
 *   <li>Request → Command/Query (Controller → Application)</li>
 *   <li>Application Response → REST API Response (Application → Controller)</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴 적용:</strong></p>
 * <ul>
 *   <li>Command: Create/Update 요청 변환</li>
 *   <li>Query: Get/Search 요청 변환</li>
 * </ul>
 *
 * <p><strong>의존성 역전 원칙 준수:</strong></p>
 * <ul>
 *   <li>Application Layer Response를 REST API Layer Response로 변환</li>
 *   <li>REST API Layer가 Application Layer에 의존하는 것은 OK</li>
 *   <li>Application Layer가 REST API Layer에 의존하는 것은 NG</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@Component
public class {Resource}ApiMapper {

    /**
     * {Resource}ApiRequest → Create{Resource}Command 변환
     *
     * @param request REST API 생성 요청
     * @return Application Layer 생성 명령
     */
    public Create{Resource}Command toCreateCommand({Resource}ApiRequest request) {
        return Create{Resource}Command.of(request.name(), request.quantity());
    }

    /**
     * ID → Get{Resource}Query 변환
     *
     * @param id {Resource} ID
     * @return Application Layer 조회 쿼리
     */
    public Get{Resource}Query toGetQuery(Long id) {
        return Get{Resource}Query.of(id);
    }

    /**
     * {Resource}Response → {Resource}ApiResponse 변환
     *
     * <p>Application Layer의 응답을 REST API Layer의 응답으로 변환합니다.</p>
     *
     * @param response Application Layer 응답
     * @return REST API 응답
     */
    public {Resource}ApiResponse toApiResponse({Resource}Response response) {
        return {Resource}ApiResponse.fromResponse(response);
    }

    /**
     * {Resource}DetailResponse → {Resource}DetailApiResponse 변환
     *
     * <p>Application Layer의 상세 응답을 REST API Layer의 상세 응답으로 변환합니다.</p>
     *
     * @param appResponse Application Layer 상세 응답
     * @return REST API 상세 응답
     */
    public {Resource}DetailApiResponse toDetailApiResponse({Resource}DetailResponse appResponse) {
        return {Resource}DetailApiResponse.from(appResponse);
    }
}
```

### ApiResponse Wrapper (공통 응답 형식)

```java
/**
 * API 응답 Wrapper
 *
 * <p>모든 API 응답은 이 형식으로 감싸져 반환됩니다.</p>
 *
 * @author Claude Code
 * @since 1.0
 */
public record ApiResponse<T>(
    boolean success,
    T data
) {
    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(true, data);
    }
}
```

### Global Exception Handler (RFC 7807 ProblemDetail)

```java
/**
 * Global Exception Handler
 *
 * <p>RFC 7807 Problem Details 표준을 따르는 에러 응답을 생성합니다.</p>
 *
 * <p><strong>로깅 레벨 전략:</strong></p>
 * <ul>
 *   <li>5xx 에러 → ERROR 레벨 (서버 문제, 즉시 대응 필요, 스택트레이스 포함)</li>
 *   <li>404 에러 → DEBUG 레벨 (정상적인 흐름, 로그 노이즈 방지)</li>
 *   <li>4xx 에러 → WARN 레벨 (클라이언트 오류, 모니터링 필요)</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorMapperRegistry errorMapperRegistry;

    public GlobalExceptionHandler(ErrorMapperRegistry errorMapperRegistry) {
        this.errorMapperRegistry = errorMapperRegistry;
    }

    /**
     * RFC 7807 ProblemDetail 빌더
     */
    private ResponseEntity<ProblemDetail> build(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title != null ? title : status.getReasonPhrase());
        pd.setType(URI.create("about:blank"));

        // RFC 7807 확장 필드
        pd.setProperty("timestamp", Instant.now().toString());

        // 요청 경로를 instance로
        if (req != null) {
            String uri = req.getRequestURI();
            if (req.getQueryString() != null && !req.getQueryString().isBlank()) {
                uri = uri + "?" + req.getQueryString();
            }
            pd.setInstance(URI.create(uri));
        }

        // Tracing ID (Micrometer/Logback MDC)
        String traceId = MDC.get("traceId");
        String spanId  = MDC.get("spanId");
        if (traceId != null) pd.setProperty("traceId", traceId);
        if (spanId  != null) pd.setProperty("spanId",  spanId);

        return ResponseEntity.status(status).body(pd);
    }

    /**
     * 400 - Validation Exception (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        var res = build(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed for request", req);
        assert res.getBody() != null;
        res.getBody().setProperty("errors", errors);
        log.warn("MethodArgumentNotValid: errors={}", errors);
        return res;
    }

    /**
     * 404 - Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResource(
            NoResourceFoundException ex,
            HttpServletRequest req) {
        log.debug("NoResourceFound: resourcePath={}", ex.getResourcePath());
        return build(HttpStatus.NOT_FOUND, "Not Found", "요청한 리소스를 찾을 수 없습니다", req);
    }

    /**
     * 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobal(
            Exception ex,
            HttpServletRequest req) {
        log.error("Unexpected error occurred", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", req);
    }

    /**
     * 도메인 예외 처리 (ErrorMapperRegistry 활용)
     *
     * <p>Domain Layer에서 발생한 예외를 HTTP 응답으로 변환합니다.</p>
     * <p>ErrorMapperRegistry를 통해 도메인별 커스텀 매핑을 적용합니다.</p>
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(
            DomainException ex,
            HttpServletRequest req,
            Locale locale) {
        var mapped = errorMapperRegistry.map(ex, locale)
            .orElseGet(() -> errorMapperRegistry.defaultMapping(ex));

        var res = build(mapped.status(), mapped.title(), mapped.detail(), req);
        var pd  = res.getBody();

        assert pd != null;
        pd.setType(mapped.type());
        pd.setProperty("code", ex.code());
        if (!ex.args().isEmpty()) pd.setProperty("args", ex.args());

        // 로깅 레벨 차별화
        if (mapped.status().is5xxServerError()) {
            log.error("DomainException (Server Error): code={}, status={}, detail={}, args={}",
                ex.code(), mapped.status().value(), mapped.detail(), ex.args(), ex);
        } else if (mapped.status() == HttpStatus.NOT_FOUND) {
            log.debug("DomainException (Not Found): code={}, status={}, detail={}, args={}",
                ex.code(), mapped.status().value(), mapped.detail(), ex.args());
        } else {
            log.warn("DomainException (Client Error): code={}, status={}, detail={}, args={}",
                ex.code(), mapped.status().value(), mapped.detail(), ex.args());
        }

        return ResponseEntity.status(mapped.status()).body(pd);
    }
}
```

### application.yml (경로 중앙 관리)

```yaml
api:
  endpoints:
    base-v1: /api/v1
    {resource}:
      base: /{resources}
      by-id: /{id}
      admin-search: /admin/{resources}/search
```

## 검증 체크리스트

- [ ] RESTful 설계 (POST → 201, GET → 200, DELETE → 204)
- [ ] `@Valid` Validation 적용 (Compact Constructor 사용 안 함)
- [ ] RFC 7807 ProblemDetail 사용
- [ ] DTO는 record 패턴
- [ ] Mapper는 `@Component` (final class 아님)
- [ ] `@RestControllerAdvice` 전역 예외 처리
- [ ] OpenAPI 문서화 (`@Operation`, `@Tag`, `@Parameter`)
- [ ] 경로 중앙 관리 (`application.yml`)
- [ ] 로깅 레벨 차별화 (5xx ERROR, 404 DEBUG, 4xx WARN)
- [ ] CQRS 완전 분리 (UseCase vs QueryService)
- [ ] ApiResponse Wrapper 사용
- [ ] ErrorMapperRegistry 패턴
- [ ] Javadoc 포함 (`@author`, `@since`)
- [ ] Pure Java 생성자 (Lombok 금지)

## 안티패턴 (피해야 할 것)

### ❌ Mapper를 final class Utility로 만들기

```java
// ❌ Bad
public final class ExampleMapper {
    private ExampleMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static Command toCommand(Request request) { ... }
}

// ✅ Good
@Component
public class ExampleApiMapper {
    public Command toCommand(Request request) { ... }
}
```

### ❌ ErrorResponse record 사용

```java
// ❌ Bad
public record ErrorResponse(String code, String message) {}

// ✅ Good
// RFC 7807 ProblemDetail 사용
ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
```

### ❌ Compact Constructor 과용

```java
// ❌ Bad
public record Request(String name) {
    public Request {
        if (name == null) throw new IllegalArgumentException();
        if (name.isBlank()) throw new IllegalArgumentException();
    }
}

// ✅ Good
public record Request(
    @NotNull @NotBlank String name
) {
    // Compact Constructor 사용 안 함
}
```

### ❌ OpenAPI 문서화 누락

```java
// ❌ Bad
@PostMapping
public ResponseEntity<Response> create(@RequestBody Request request) { ... }

// ✅ Good
@Operation(
    summary = "Example 생성",
    description = "새로운 Example을 생성합니다.",
    responses = {
        @ApiResponse(responseCode = "201", description = "생성 성공")
    }
)
@PostMapping
public ResponseEntity<Response> create(@RequestBody Request request) { ... }
```

### ❌ 경로 하드코딩

```java
// ❌ Bad
@RequestMapping("/api/v1/examples")
public class ExampleController { ... }

// ✅ Good
@RequestMapping("${api.endpoints.base-v1}")
public class ExampleController { ... }
```

### ❌ Lombok 사용

```java
// ❌ Bad
@RequiredArgsConstructor
public class ExampleController {
    private final UseCase useCase;
}

// ✅ Good
public class ExampleController {
    private final UseCase useCase;

    public ExampleController(UseCase useCase) {
        this.useCase = useCase;
    }
}
```

## 참고 문서

- [REST API Layer 규칙](../../docs/coding_convention/01-adapter-rest-api-layer/)
- [Controller 설계](../../docs/coding_convention/01-adapter-rest-api-layer/controller-design/)
- [DTO 패턴](../../docs/coding_convention/01-adapter-rest-api-layer/dto-patterns/)
- [Exception Handling](../../docs/coding_convention/01-adapter-rest-api-layer/exception-handling/)
