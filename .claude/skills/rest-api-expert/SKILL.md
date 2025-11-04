---
name: "rest-api-expert"
description: "Spring REST API Layer 전문가. Controller, DTO, Exception 처리, Mapper 패턴을 준수하는 REST API 코드를 생성하고 검증합니다. OpenAPI/Swagger 문서화, 일관된 Error Response, HTTP 상태 코드 정확한 사용을 보장합니다."
---

# Spring REST API Layer Expert

Spring Boot REST API Layer (Adapter-In) 전문가 Skill입니다. HTTP 인터페이스 설계, 요청/응답 처리, 예외 핸들링, API 문서화를 담당합니다.

## 전문 분야

1. **Controller 설계**: REST 표준, HTTP 메서드, 상태 코드
2. **DTO 패턴**: Request/Response DTO, Validation
3. **Exception 처리**: Global Handler, Error Response, Custom Error Code
4. **Mapper 패턴**: DTO ↔ Domain/Command 변환
5. **API 문서화**: OpenAPI/Swagger, Spring REST Docs

## 사용 시점

- REST API Controller 생성 또는 수정
- API 요청/응답 DTO 설계
- Exception 처리 구현
- API 문서화 필요 시

## 핵심 규칙

### 1. Controller 설계 원칙

**위치**: `docs/coding_convention/01-adapter-rest-api-layer/controller-design/`

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
        @Valid @RequestBody OrderRequest request
    ) {
        PlaceOrderCommand command = orderMapper.toCommand(request);
        OrderResult result = placeOrderUseCase.execute(command);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(orderMapper.toResponse(result));
    }
}
```

**규칙**:
- ✅ `@RestController` + `@RequestMapping` 조합
- ✅ UseCase 의존성 주입 (Constructor Injection)
- ✅ `@Valid` + `@RequestBody` 검증
- ✅ HTTP 상태 코드 정확히 사용 (POST → 201 CREATED)
- ✅ Mapper를 통한 DTO ↔ Command 변환

### 2. DTO 패턴

**Request DTO** (`dto/request/`):
```java
public record OrderRequest(
    @NotBlank(message = "주문 번호는 필수입니다")
    String orderNumber,

    @NotNull(message = "고객 ID는 필수입니다")
    @Positive(message = "고객 ID는 양수여야 합니다")
    Long customerId,

    @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다")
    List<OrderItemRequest> items
) {
    // Compact Constructor (Validation)
    public OrderRequest {
        Objects.requireNonNull(orderNumber);
        Objects.requireNonNull(customerId);
        items = List.copyOf(items);  // Immutable
    }
}
```

**Response DTO** (`dto/response/`):
```java
public record OrderResponse(
    Long orderId,
    String orderNumber,
    String status,
    LocalDateTime createdAt
) {
    // Response는 Validation 불필요
}
```

**규칙**:
- ✅ Record 패턴 사용 (Java 21)
- ✅ Jakarta Validation (`@NotBlank`, `@NotNull`, `@Positive` 등)
- ✅ Compact Constructor로 추가 검증
- ✅ Immutable (final fields, List.copyOf)

### 3. Exception 처리

**Global Exception Handler** (`exception/GlobalExceptionHandler.java`):
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
        DomainException ex
    ) {
        log.warn("Domain exception: {}", ex.getMessage());
        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(ErrorResponse.of(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        log.warn("Validation failed: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.ofValidation(ex));
    }
}
```

**Error Response** (`dto/response/ErrorResponse.java`):
```java
public record ErrorResponse(
    String errorCode,
    String message,
    LocalDateTime timestamp,
    List<FieldError> fieldErrors
) {
    public static ErrorResponse of(DomainException ex) {
        return new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            LocalDateTime.now(),
            Collections.emptyList()
        );
    }

    public record FieldError(String field, String message) {}
}
```

**규칙**:
- ✅ `@RestControllerAdvice` 사용
- ✅ Domain Exception → HTTP 상태 코드 매핑
- ✅ Validation Exception → 400 Bad Request
- ✅ 일관된 Error Response 형식

### 4. Mapper 패턴

**Request Mapper** (`mapper/OrderRequestMapper.java`):
```java
@Component
public class OrderRequestMapper {

    public PlaceOrderCommand toCommand(OrderRequest request) {
        return new PlaceOrderCommand(
            request.orderNumber(),
            request.customerId(),
            request.items().stream()
                .map(this::toItemCommand)
                .toList()
        );
    }

    private OrderItemCommand toItemCommand(OrderItemRequest item) {
        return new OrderItemCommand(
            item.productId(),
            item.quantity(),
            item.price()
        );
    }
}
```

**Response Mapper** (`mapper/OrderResponseMapper.java`):
```java
@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(OrderResult result) {
        return new OrderResponse(
            result.orderId(),
            result.orderNumber(),
            result.status().name(),
            result.createdAt()
        );
    }
}
```

**규칙**:
- ✅ `@Component` 등록
- ✅ Request → Command 변환 (UseCase 입력)
- ✅ Result → Response 변환 (클라이언트 출력)
- ✅ 복잡한 변환은 private 메서드로 분리

### 5. API 문서화

**OpenAPI/Swagger** (`config/OpenApiConfig.java`):
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order Management API")
                .version("v1.0.0")
                .description("주문 관리 REST API"));
    }
}
```

**Controller 문서화**:
```java
@Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다")
@ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "주문 생성 성공",
        content = @Content(schema = @Schema(implementation = OrderResponse.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
})
@PostMapping
public ResponseEntity<OrderResponse> placeOrder(
    @Valid @RequestBody OrderRequest request
) {
    // ...
}
```

## 패키지 구조

```
adapter-in/rest-api/
├── controller/
│   ├── OrderController.java
│   └── CustomerController.java
├── dto/
│   ├── request/
│   │   ├── OrderRequest.java
│   │   └── OrderItemRequest.java
│   └── response/
│       ├── OrderResponse.java
│       └── ErrorResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ErrorCode.java
├── mapper/
│   ├── OrderRequestMapper.java
│   └── OrderResponseMapper.java
└── config/
    ├── OpenApiConfig.java
    └── WebMvcConfig.java
```

## HTTP 상태 코드 가이드

| 작업 | 성공 상태 코드 | 실패 상태 코드 |
|------|---------------|----------------|
| POST (생성) | 201 CREATED | 400 Bad Request, 409 Conflict |
| GET (조회) | 200 OK | 404 Not Found |
| PUT (전체 수정) | 200 OK | 400 Bad Request, 404 Not Found |
| PATCH (부분 수정) | 200 OK | 400 Bad Request, 404 Not Found |
| DELETE (삭제) | 204 No Content | 404 Not Found, 409 Conflict |

## 검증 체크리스트

REST API 코드 작성 후 다음을 확인하세요:

- [ ] `@RestController` + `@RequestMapping` 사용
- [ ] UseCase 의존성 Constructor Injection
- [ ] Request DTO는 Record + Validation
- [ ] Response DTO는 Record (Immutable)
- [ ] HTTP 상태 코드 정확히 사용
- [ ] Exception은 GlobalExceptionHandler에서 처리
- [ ] ErrorResponse 형식 일관성 유지
- [ ] Mapper로 DTO ↔ Command 변환
- [ ] OpenAPI/Swagger 문서화
- [ ] 통합 테스트 작성 (MockMvc)

## 추가 리소스

상세 규칙은 다음 파일을 참조하세요:

```bash
cat .claude/skills/rest-api-expert/REFERENCE.md
```

검증 스크립트:

```bash
bash .claude/skills/rest-api-expert/scripts/validate-rest-api.sh [file_path]
```

## 참고 문서

- `docs/coding_convention/01-adapter-rest-api-layer/`
- `docs/coding_convention/01-adapter-rest-api-layer/package-guide/01_rest_api_package_guide.md`
- `docs/coding_convention/01-adapter-rest-api-layer/controller-design/01_rest-api-conventions.md`
- `docs/coding_convention/01-adapter-rest-api-layer/dto-patterns/`
- `docs/coding_convention/01-adapter-rest-api-layer/exception-handling/`
