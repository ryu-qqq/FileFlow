# Swagger/OpenAPI 설정 가이드

## 목차
1. [개요](#1-개요)
2. [SpringDoc vs Swagger 2](#2-springdoc-vs-swagger-2)
3. [의존성 설정](#3-의존성-설정)
4. [Application 설정](#4-application-설정)
5. [Controller 어노테이션 패턴](#5-controller-어노테이션-패턴)
6. [DTO Record 문서화](#6-dto-record-문서화)
7. [에러 응답 문서화](#7-에러-응답-문서화)
8. [API 버저닝 전략](#8-api-버저닝-전략)
9. [Swagger UI 커스터마이징](#9-swagger-ui-커스터마이징)
10. [보안 스킴 문서화](#10-보안-스킴-문서화)
11. [베스트 프랙티스](#11-베스트-프랙티스)

---

## 1. 개요

**Spring Boot 3.x 기준 OpenAPI 문서화 표준**

이 가이드는 **SpringDoc OpenAPI 3.0**을 사용한 REST API 문서 자동화를 다룹니다.

### 왜 SpringDoc을 사용하는가?

| 항목 | SpringDoc OpenAPI | Swagger 2 (Springfox) |
|------|-------------------|------------------------|
| Spring Boot 3.x 지원 | ✅ 완벽 지원 | ❌ 미지원 |
| OpenAPI 3.0 | ✅ 표준 지원 | ❌ Swagger 2.0만 지원 |
| Jakarta EE | ✅ 지원 | ❌ javax 패키지만 지원 |
| 유지보수 | ✅ 활발함 | ❌ 중단됨 |
| 성능 | ⚡ 빠름 (런타임 생성 안 함) | 🐌 느림 (런타임 스캔) |

**✅ 결론**: Spring Boot 3.x 프로젝트에서는 **SpringDoc OpenAPI**를 사용해야 합니다.

---

## 2. SpringDoc vs Swagger 2

### 어노테이션 비교

| Swagger 2 (Springfox) | SpringDoc OpenAPI 3.0 | 용도 |
|-----------------------|-----------------------|------|
| `@Api` | `@Tag` | Controller 설명 |
| `@ApiOperation` | `@Operation` | API 메서드 설명 |
| `@ApiParam` | `@Parameter` | 파라미터 설명 |
| `@ApiModel` | `@Schema` | DTO 클래스 설명 |
| `@ApiModelProperty` | `@Schema` | DTO 필드 설명 |
| `@ApiResponse` | `@ApiResponse` | 응답 설명 |

### 마이그레이션 예시

**Before (Swagger 2 - Springfox)**:
```java
@Api(tags = "Example API")
@RestController
public class ExampleController {

    @ApiOperation(value = "Example 생성", notes = "새로운 Example을 생성합니다")
    @ApiResponses({
        @ApiResponse(code = 201, message = "생성 성공"),
        @ApiResponse(code = 400, message = "잘못된 요청")
    })
    @PostMapping("/api/v1/examples")
    public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
            @ApiParam(value = "Example 생성 요청", required = true)
            @RequestBody @Valid ExampleApiRequest request) {
        // ...
    }
}
```

**After (SpringDoc OpenAPI 3.0)**:
```java
@Tag(name = "Example API", description = "Example 도메인 관리 API")
@RestController
public class ExampleController {

    @Operation(
        summary = "Example 생성",
        description = "새로운 Example을 생성합니다"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/api/v1/examples")
    public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
            @Parameter(description = "Example 생성 요청", required = true)
            @RequestBody @Valid ExampleApiRequest request) {
        // ...
    }
}
```

**주요 변경점**:
- `@Api` → `@Tag`
- `@ApiOperation` → `@Operation`
- `@ApiParam` → `@Parameter`
- `@ApiResponse(code = 201)` → `@ApiResponse(responseCode = "201")`

---

## 3. 의존성 설정

### build.gradle.kts

```kotlin
dependencies {
    // SpringDoc OpenAPI 3.0
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
}
```

### gradle/libs.versions.toml

```toml
[versions]
springdoc = "2.5.0"

[libraries]
springdoc-openapi = { module = "org.springdoc:springdoc-openapi-starter-webmvc-ui", version.ref = "springdoc" }
```

**의존성 하나로 포함되는 것들**:
- ✅ OpenAPI 3.0 스펙 생성
- ✅ Swagger UI (인터랙티브 문서)
- ✅ `/v3/api-docs` (JSON/YAML 스펙)
- ✅ `/swagger-ui.html` (UI 페이지)

---

## 4. Application 설정

### application.yml

```yaml
# ========================================
# SpringDoc OpenAPI 설정
# ========================================
springdoc:
  # OpenAPI 문서 경로 설정
  api-docs:
    path: /v3/api-docs  # OpenAPI JSON 스펙 경로
    enabled: true  # OpenAPI 문서 활성화

  # Swagger UI 경로 설정
  swagger-ui:
    path: /swagger-ui.html  # Swagger UI 접근 경로
    enabled: true  # Swagger UI 활성화
    operations-sorter: method  # 메서드별 정렬 (alpha, method)
    tags-sorter: alpha  # 태그 알파벳 정렬
    doc-expansion: none  # 기본 확장 상태 (none, list, full)
    display-request-duration: true  # 요청 시간 표시
    default-models-expand-depth: 1  # Schema 모델 기본 확장 깊이
    default-model-expand-depth: 1  # Schema 속성 기본 확장 깊이
    show-extensions: true  # 확장 속성 표시
    show-common-extensions: true  # 공통 확장 속성 표시

  # 패키지 스캔 설정
  packages-to-scan: com.ryuqq.adapter.in.rest  # 문서화할 패키지

  # 경로 필터 설정
  paths-to-match:
    - /api/**  # 문서화할 경로 패턴

  # 기본 응답 생성 설정
  default-consumes-media-type: application/json
  default-produces-media-type: application/json

  # 페이징 지원
  pageable:
    enabled: true

  # 정렬 지원
  sort:
    enabled: true
```

### Profile별 설정

```yaml
# application-local.yml (로컬 개발 환경)
springdoc:
  swagger-ui:
    enabled: true
  api-docs:
    enabled: true

# application-dev.yml (개발 서버)
springdoc:
  swagger-ui:
    enabled: true
  api-docs:
    enabled: true

# application-prod.yml (운영 환경)
springdoc:
  swagger-ui:
    enabled: false  # ⚠️ 운영 환경에서는 비활성화 권장
  api-docs:
    enabled: false  # ⚠️ 보안을 위해 비활성화
```

**⚠️ 운영 환경 주의사항**:
- Swagger UI는 API 구조를 노출하므로 운영 환경에서는 **비활성화**를 권장합니다.
- 필요시 IP 화이트리스트, Spring Security로 접근 제한을 설정하세요.

---

## 5. Controller 어노테이션 패턴

### 5.1 Controller 레벨 - @Tag

**Controller 클래스에 API 그룹 설명 추가**:

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

**@Tag 속성**:
- `name`: Swagger UI에 표시될 API 그룹 이름
- `description`: 상세 설명 (Markdown 지원)

### 5.2 메서드 레벨 - @Operation

**API 메서드에 상세 설명 추가**:

```java
@Operation(
    summary = "Example 생성",  // 짧은 요약 (필수)
    description = """
        새로운 Example을 생성합니다. (CQRS Command)

        **비즈니스 규칙:**
        - message는 1-500자 제한
        - 중복 메시지는 허용됨

        **처리 흐름:**
        1. Validation 검증
        2. UseCase 실행
        3. Domain Event 발행
        """  // 상세 설명 (Markdown 지원)
)
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @RequestBody @Valid ExampleApiRequest request) {
    // ...
}
```

### 5.3 응답 문서화 - @ApiResponses

**성공/실패 응답 케이스 문서화**:

```java
@Operation(summary = "Example 생성")
@ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "생성 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ExampleApiResponse.class)
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (Validation 실패)",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorInfo.class),
            examples = @ExampleObject(
                name = "Validation Error",
                value = """
                    {
                      "success": false,
                      "error": {
                        "code": "VALIDATION_FAILED",
                        "message": "message는 1자 이상이어야 합니다",
                        "field": "message"
                      }
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Example을 찾을 수 없음"
    )
})
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @RequestBody @Valid ExampleApiRequest request) {
    // ...
}
```

**@ApiResponse 속성**:
- `responseCode`: HTTP 상태 코드 (String)
- `description`: 응답 설명
- `content`: 응답 본문 정의
  - `mediaType`: Content-Type
  - `schema`: 응답 스키마 (DTO 클래스)
  - `examples`: 예시 응답 JSON

### 5.4 파라미터 문서화 - @Parameter

**PathVariable, RequestParam 문서화**:

```java
@Operation(summary = "Example 단건 조회")
@GetMapping("${api.endpoints.example.base}/{id}")
public ResponseEntity<ApiResponse<ExampleDetailApiResponse>> getExample(
        @Parameter(
            description = "Example ID (양수)",
            required = true,
            example = "123",
            schema = @Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable @Positive Long id) {
    // ...
}
```

**QueryString 파라미터 문서화**:

```java
@Operation(summary = "Example 검색")
@GetMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleSliceApiResponse>> searchExamples(
        @Parameter(description = "검색 키워드", example = "hello")
        @RequestParam(required = false) String message,

        @Parameter(description = "상태 필터", example = "ACTIVE")
        @RequestParam(required = false) ExampleStatus status,

        @Parameter(description = "커서", example = "eyJpZCI6MTIzfQ==")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "페이지 크기", example = "20", schema = @Schema(minimum = "1", maximum = "100"))
        @RequestParam(defaultValue = "20") @Positive @Max(100) int size) {
    // ...
}
```

**@ModelAttribute DTO 파라미터는 자동 문서화**:

```java
@GetMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleSliceApiResponse>> searchExamples(
        @Valid @ModelAttribute ExampleSearchApiRequest searchRequest) {
    // searchRequest의 필드는 자동으로 QueryString으로 문서화됨
}
```

---

## 6. DTO Record 문서화

### 6.1 Request DTO

**Java 21 Record + @Schema**:

```java
/**
 * Example 생성 요청 DTO
 *
 * @param message Example 메시지 (1-500자)
 * @author windsurf
 * @since 1.0.0
 */
@Schema(
    description = "Example 생성 요청 DTO",
    example = """
        {
          "message": "Hello World"
        }
        """
)
public record ExampleApiRequest(

    @Schema(
        description = "메시지 (1-500자)",
        example = "Hello World",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 500
    )
    @NotBlank(message = "message는 필수입니다")
    @Size(min = 1, max = 500, message = "message는 1-500자여야 합니다")
    String message

) {
    // Record Compact Constructor (Validation)
    public ExampleApiRequest {
        if (message != null) {
            message = message.trim();
        }
    }
}
```

### 6.2 Response DTO

```java
/**
 * Example 응답 DTO
 *
 * @param id Example ID
 * @param message Example 메시지
 * @author windsurf
 * @since 1.0.0
 */
@Schema(
    description = "Example 응답 DTO",
    example = """
        {
          "id": 1,
          "message": "Hello World"
        }
        """
)
public record ExampleApiResponse(

    @Schema(
        description = "Example ID",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long id,

    @Schema(
        description = "메시지",
        example = "Hello World",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String message

) {}
```

### 6.3 Pagination Response DTO

```java
/**
 * Example Slice 응답 DTO (Cursor 기반 페이징)
 *
 * @param content Example 목록
 * @param size 페이지 크기
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 * @author windsurf
 * @since 1.0.0
 */
@Schema(description = "Example Slice 응답 DTO (Cursor 기반 페이징)")
public record ExampleSliceApiResponse(

    @Schema(description = "Example 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ExampleDetailApiResponse> content,

    @Schema(description = "페이지 크기", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    int size,

    @Schema(description = "다음 페이지 존재 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean hasNext,

    @Schema(description = "다음 페이지 커서", example = "eyJpZCI6MTIzfQ==")
    String nextCursor

) {}
```

### 6.4 Enum 문서화

```java
/**
 * Example 상태
 */
@Schema(description = "Example 상태")
public enum ExampleStatus {

    @Schema(description = "활성")
    ACTIVE,

    @Schema(description = "비활성")
    INACTIVE,

    @Schema(description = "삭제됨")
    DELETED
}
```

---

## 7. 에러 응답 문서화

### 7.1 공통 에러 응답 정의

**ErrorInfo DTO**:

```java
/**
 * 에러 정보 DTO
 *
 * @param code 에러 코드
 * @param message 에러 메시지
 * @param field 에러 필드 (Validation 실패 시)
 * @author windsurf
 * @since 1.0.0
 */
@Schema(description = "에러 정보")
public record ErrorInfo(

    @Schema(
        description = "에러 코드",
        example = "EXAMPLE_NOT_FOUND",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code,

    @Schema(
        description = "에러 메시지",
        example = "Example을 찾을 수 없습니다",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String message,

    @Schema(
        description = "에러 필드 (Validation 실패 시)",
        example = "message"
    )
    String field

) {}
```

### 7.2 GlobalExceptionHandler에 에러 응답 문서화

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Domain Exception 처리
     */
    @ExceptionHandler(DomainException.class)
    @Operation(hidden = true)  // Swagger UI에서 숨김
    public ResponseEntity<ApiResponse<Void>> handleDomainException(
            DomainException ex,
            HttpServletRequest request) {

        ErrorMapper errorMapper = errorMapperRegistry.getMapper(ex.getClass());
        HttpErrorMapping errorMapping = errorMapper.map(ex);

        ErrorInfo errorInfo = ErrorInfo.of(
            errorMapping.getErrorCode(),
            messageSource.getMessage(errorMapping.getMessageKey(), ex.getArgs(), LocaleContextHolder.getLocale()),
            null
        );

        return ResponseEntity
            .status(errorMapping.getStatus())
            .body(ApiResponse.ofError(errorInfo));
    }
}
```

### 7.3 Controller에서 에러 응답 문서화

```java
@Operation(summary = "Example 단건 조회")
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ExampleDetailApiResponse.class))
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Example을 찾을 수 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorInfo.class),
            examples = @ExampleObject(
                name = "Example Not Found",
                value = """
                    {
                      "success": false,
                      "error": {
                        "code": "EXAMPLE_NOT_FOUND",
                        "message": "요청한 Example(ID: 123)을 찾을 수 없습니다.",
                        "field": null
                      },
                      "timestamp": "2025-10-28T10:30:00",
                      "requestId": "550e8400-e29b-41d4-a716-446655440000"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (ID 양수 제약 위반)",
        content = @Content(
            schema = @Schema(implementation = ErrorInfo.class),
            examples = @ExampleObject(
                name = "Validation Error",
                value = """
                    {
                      "success": false,
                      "error": {
                        "code": "VALIDATION_FAILED",
                        "message": "ID는 양수여야 합니다",
                        "field": "id"
                      },
                      "timestamp": "2025-10-28T10:30:00",
                      "requestId": "550e8400-e29b-41d4-a716-446655440000"
                    }
                    """
            )
        )
    )
})
@GetMapping("${api.endpoints.example.base}/{id}")
public ResponseEntity<ApiResponse<ExampleDetailApiResponse>> getExample(
        @PathVariable @Positive Long id) {
    // ...
}
```

---

## 8. API 버저닝 전략

### 8.1 URL 경로 기반 버저닝 (권장)

**application.yml**:

```yaml
api:
  endpoints:
    base-v1: /api/v1
    base-v2: /api/v2
```

**Controller**:

```java
@Tag(name = "Example API v1")
@RestController
@RequestMapping("${api.endpoints.base-v1}/examples")
public class ExampleV1Controller {
    // v1 API
}

@Tag(name = "Example API v2")
@RestController
@RequestMapping("${api.endpoints.base-v2}/examples")
public class ExampleV2Controller {
    // v2 API (Breaking Changes)
}
```

**Swagger UI에서 버전별 문서 분리**:

```yaml
springdoc:
  group-configs:
    - group: v1
      paths-to-match: /api/v1/**
      display-name: API v1
    - group: v2
      paths-to-match: /api/v2/**
      display-name: API v2
```

### 8.2 Header 기반 버저닝 (선택)

**Controller**:

```java
@Tag(name = "Example API")
@RestController
@RequestMapping("/api/examples")
public class ExampleController {

    @Operation(summary = "Example 생성 (v1)")
    @PostMapping(headers = "API-Version=1")
    public ResponseEntity<ApiResponse<ExampleApiResponse>> createExampleV1(
            @RequestBody @Valid ExampleApiRequest request) {
        // v1 로직
    }

    @Operation(summary = "Example 생성 (v2)")
    @PostMapping(headers = "API-Version=2")
    public ResponseEntity<ApiResponse<ExampleV2ApiResponse>> createExampleV2(
            @RequestBody @Valid ExampleV2ApiRequest request) {
        // v2 로직 (Breaking Changes)
    }
}
```

**⚠️ Header 기반 버저닝의 단점**:
- Swagger UI에서 테스트가 불편함 (Header를 수동으로 입력해야 함)
- URL 캐싱 전략 적용이 어려움
- RESTful 원칙에 어긋남

**✅ 권장**: URL 경로 기반 버저닝을 사용하세요.

---

## 9. Swagger UI 커스터마이징

### 9.1 OpenAPI 메타데이터 설정

**Configuration 클래스**:

```java
package com.ryuqq.adapter.in.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 *
 * <p>SpringDoc OpenAPI 3.0을 사용한 API 문서 자동화 설정</p>
 *
 * <ul>
 *   <li>Swagger UI: /swagger-ui.html</li>
 *   <li>OpenAPI JSON: /v3/api-docs</li>
 * </ul>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 메타데이터 설정
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(apiServers());
    }

    /**
     * API 정보 설정
     */
    private Info apiInfo() {
        return new Info()
            .title("Spring Standards REST API")
            .description("""
                Spring Boot 3.5.x 헥사고날 아키텍처 표준 프로젝트

                **주요 특징:**
                - CQRS 패턴 (Command/Query 분리)
                - Cursor & Offset 기반 Pagination
                - ApiResponse<T> 표준 응답 래퍼
                - ErrorMapper 패턴 기반 에러 처리

                **기술 스택:**
                - Spring Boot 3.5.x
                - Java 21 (Record 패턴)
                - PostgreSQL + QueryDSL
                """)
            .version("1.0.0")
            .contact(apiContact())
            .license(apiLicense());
    }

    /**
     * 연락처 정보
     */
    private Contact apiContact() {
        return new Contact()
            .name("개발팀")
            .email("dev@example.com")
            .url("https://github.com/your-org/spring-standards");
    }

    /**
     * 라이선스 정보
     */
    private License apiLicense() {
        return new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");
    }

    /**
     * 서버 URL 설정
     */
    private List<Server> apiServers() {
        return List.of(
            new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버"),
            new Server()
                .url("https://dev-api.example.com")
                .description("개발 서버"),
            new Server()
                .url("https://api.example.com")
                .description("운영 서버")
        );
    }
}
```

### 9.2 Global 보안 스킴 설정

**Bearer Token (JWT) 인증**:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .servers(apiServers())
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Access Token")
            )
        )
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

**Controller에서 인증 필요 API 표시**:

```java
@Operation(
    summary = "Example 생성",
    security = @SecurityRequirement(name = "bearerAuth")
)
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @RequestBody @Valid ExampleApiRequest request) {
    // ...
}
```

### 9.3 API 그룹핑 (GroupedOpenApi)

**여러 API 그룹으로 분리**:

```java
@Configuration
public class OpenApiConfig {

    /**
     * 일반 사용자 API 그룹
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/v1/**")
            .pathsToExclude("/api/v1/admin/**")
            .displayName("Public API")
            .build();
    }

    /**
     * 관리자 API 그룹
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/v1/admin/**")
            .displayName("Admin API")
            .build();
    }

    /**
     * 내부 API 그룹
     */
    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
            .group("internal")
            .pathsToMatch("/internal/**")
            .displayName("Internal API")
            .build();
    }
}
```

**Swagger UI에서 드롭다운으로 그룹 선택 가능**

---

## 10. 보안 스킴 문서화

### 10.1 JWT Bearer Token

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Access Token을 Authorization 헤더에 포함하세요")
            )
        )
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

**Swagger UI 사용법**:
1. Swagger UI 상단의 **Authorize** 버튼 클릭
2. `Bearer <your-jwt-token>` 입력 (Bearer 접두어 자동 추가됨)
3. Authorize 클릭
4. 이후 모든 API 요청에 자동으로 Token 포함

### 10.2 API Key (Header)

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("apiKey",
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-KEY")
                    .description("API Key를 X-API-KEY 헤더에 포함하세요")
            )
        );
}
```

### 10.3 Basic Authentication

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("basicAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("사용자명과 비밀번호를 Base64로 인코딩하여 전송")
            )
        );
}
```

### 10.4 OAuth2

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("oauth2",
                new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl("https://auth.example.com/oauth/authorize")
                            .tokenUrl("https://auth.example.com/oauth/token")
                            .scopes(new Scopes()
                                .addString("read", "Read access")
                                .addString("write", "Write access")
                            )
                        )
                    )
            )
        );
}
```

---

## 11. 베스트 프랙티스

### 11.1 코드 오염 방지

**❌ 안 좋은 예 (과도한 어노테이션)**:
```java
@Operation(
    summary = "Example 생성",
    description = "새로운 Example을 생성합니다. (CQRS Command)",
    tags = {"Example API"},
    operationId = "createExample",
    deprecated = false,
    hidden = false,
    extensions = {
        @Extension(name = "x-rate-limit", properties = @ExtensionProperty(name = "limit", value = "100"))
    }
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(schema = @Schema(implementation = ExampleApiResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "401", description = "인증 실패"),
    @ApiResponse(responseCode = "403", description = "권한 없음"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
})
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @Parameter(description = "Example 생성 요청", required = true, schema = @Schema(implementation = ExampleApiRequest.class))
        @RequestBody @Valid ExampleApiRequest request) {
    // ...
}
```

**✅ 좋은 예 (핵심만 명시)**:
```java
@Operation(
    summary = "Example 생성",
    description = "새로운 Example을 생성합니다. (CQRS Command)"
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "생성 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
})
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @RequestBody @Valid ExampleApiRequest request) {
    // SpringDoc은 @Valid, @RequestBody를 자동으로 파싱하여 문서화함
}
```

**원칙**:
- ✅ `summary`, `description`만 명시 (필수)
- ✅ 중요한 응답 케이스만 `@ApiResponse`로 문서화
- ✅ Parameter는 SpringDoc 자동 추론 활용 (`@RequestBody`, `@PathVariable`, `@RequestParam` 자동 파싱)
- ❌ `operationId`, `tags`, `deprecated`, `hidden` 등 자동 추론 가능한 속성은 생략
- ❌ 모든 HTTP 상태 코드를 다 나열하지 않음 (401, 403, 500은 GlobalExceptionHandler가 처리)

### 11.2 Javadoc과 Swagger 어노테이션의 역할 분리

**Javadoc**:
- **대상**: 개발자 (코드 레벨)
- **위치**: 클래스, 메서드, 필드에 주석으로 작성
- **내용**: 구현 상세, 비즈니스 로직, 내부 동작 설명

**Swagger 어노테이션**:
- **대상**: API 사용자 (외부 클라이언트)
- **위치**: Controller 메서드, DTO 클래스
- **내용**: API 사용법, 요청/응답 예시, 에러 케이스

**예시**:

```java
/**
 * Example을 생성합니다. (CQRS Command)
 *
 * <p><strong>비즈니스 로직:</strong></p>
 * <ol>
 *   <li>Validation 검증 (message는 1-500자)</li>
 *   <li>CreateExampleCommand 생성</li>
 *   <li>CreateExampleUseCase 실행</li>
 *   <li>Domain Event 발행 (ExampleCreatedEvent)</li>
 * </ol>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>UseCase 내부에서 @Transactional 처리됨</li>
 *   <li>Controller는 트랜잭션 경계 밖에 있음</li>
 * </ul>
 *
 * @param request Example 생성 요청 DTO
 * @return Example 생성 결과 (201 Created)
 */
@Operation(
    summary = "Example 생성",
    description = "새로운 Example을 생성합니다. (CQRS Command)"
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "생성 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
})
@PostMapping("${api.endpoints.example.base}")
public ResponseEntity<ApiResponse<ExampleApiResponse>> createExample(
        @RequestBody @Valid ExampleApiRequest request) {
    // ...
}
```

**역할 분리**:
- **Javadoc**: 개발자에게 비즈니스 로직, 트랜잭션 경계, 내부 동작 설명
- **@Operation**: API 사용자에게 API 사용법, 요청/응답 설명

### 11.3 운영 환경 보안

**application-prod.yml**:

```yaml
springdoc:
  swagger-ui:
    enabled: false  # ⚠️ 운영 환경에서는 Swagger UI 비활성화
  api-docs:
    enabled: false  # ⚠️ 운영 환경에서는 OpenAPI 문서 비활성화
```

**또는 Spring Security로 접근 제한**:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .hasRole("ADMIN")  // 관리자만 접근 가능
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
```

### 11.4 Performance 최적화

**1. 패키지 스캔 범위 최소화**:

```yaml
springdoc:
  packages-to-scan: com.ryuqq.adapter.in.rest  # 특정 패키지만 스캔
  paths-to-match: /api/**  # 특정 경로만 문서화
```

**2. Production 환경에서 비활성화**:

```yaml
springdoc:
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}  # 환경 변수로 제어
  api-docs:
    enabled: ${API_DOCS_ENABLED:false}
```

**3. 불필요한 API 숨기기**:

```java
@Operation(hidden = true)  // Swagger UI에서 숨김
@GetMapping("/internal/health")
public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("OK");
}
```

### 11.5 Versioning 전략

**URL 경로 기반 버저닝 (권장)**:

```java
@Tag(name = "Example API v1")
@RestController
@RequestMapping("${api.endpoints.base-v1}/examples")
public class ExampleV1Controller {
    // v1 API
}

@Tag(name = "Example API v2")
@RestController
@RequestMapping("${api.endpoints.base-v2}/examples")
public class ExampleV2Controller {
    // v2 API (Breaking Changes)
}
```

**GroupedOpenApi로 버전별 문서 분리**:

```java
@Bean
public GroupedOpenApi v1Api() {
    return GroupedOpenApi.builder()
        .group("v1")
        .pathsToMatch("/api/v1/**")
        .displayName("API v1")
        .build();
}

@Bean
public GroupedOpenApi v2Api() {
    return GroupedOpenApi.builder()
        .group("v2")
        .pathsToMatch("/api/v2/**")
        .displayName("API v2")
        .build();
}
```

### 11.6 Spring REST Docs와 통합

**SpringDoc OpenAPI**와 **Spring REST Docs**를 함께 사용하는 전략:

- **SpringDoc**: 개발 중 빠른 피드백 (Swagger UI)
- **REST Docs**: 테스트 기반 정확한 문서 (운영 배포용)

**통합 방법**:

1. **SpringDoc으로 빠른 개발**:
   - Swagger UI로 API 테스트
   - 개발 단계에서 빠른 피드백

2. **REST Docs로 정확한 문서 작성**:
   - 테스트 기반 문서 생성
   - AsciiDoc으로 커스터마이징
   - 운영 환경에 배포

3. **OpenAPI 3.0 통합**:
   - REST Docs로 생성한 JSON을 SpringDoc의 OpenAPI 스펙으로 변환
   - Swagger UI에서 REST Docs 기반 정확한 문서 확인

**참고**: [REST Docs 가이드](../testing/04_rest-docs-guide.md)

---

## 요약

### 핵심 원칙

1. **SpringDoc OpenAPI 3.0 사용** (Swagger 2는 Spring Boot 3.x 미지원)
2. **최소한의 어노테이션 사용** (SpringDoc 자동 추론 활용)
3. **Javadoc과 Swagger 역할 분리** (개발자용 vs API 사용자용)
4. **운영 환경에서는 비활성화** (또는 접근 제한)
5. **URL 경로 기반 버저닝** (Header 기반보다 RESTful)

### 디렉토리 구조

```
adapter-in/rest-api/
├── config/
│   └── OpenApiConfig.java  # Swagger/OpenAPI 설정
├── example/
│   ├── controller/
│   │   └── ExampleController.java  # @Tag, @Operation 사용
│   └── dto/
│       ├── request/
│       │   └── ExampleApiRequest.java  # @Schema 사용
│       └── response/
│           └── ExampleApiResponse.java  # @Schema 사용
```

### 접근 URL

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

**✅ 이 가이드를 따르면 Spring Boot 3.x 프로젝트에서 정확하고 효율적인 API 문서를 자동화할 수 있습니다.**
