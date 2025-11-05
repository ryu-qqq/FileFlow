# REST API Adapter Layer 생성 가이드

## 📋 목차
1. [개요](#개요)
2. [아키텍처 구조](#아키텍처-구조)
3. [핵심 컨벤션](#핵심-컨벤션)
4. [단계별 구현 가이드](#단계별-구현-가이드)
5. [컨벤션 검증](#컨벤션-검증)
6. [참고 자료](#참고-자료)

---

## 개요

REST API Adapter Layer는 **헥사고날 아키텍처의 Inbound Adapter**로서, HTTP 요청을 Application Layer Command로 변환하는 역할을 합니다.

### 핵심 원칙
- ✅ **Thin Controller**: 비즈니스 로직 없이 Facade 호출만
- ✅ **Pure Java**: Lombok 사용 금지
- ✅ **Static Mapper**: Stateless Utility Class 패턴
- ✅ **Bean Validation**: 모든 Request DTO 검증
- ✅ **RFC 7807**: Problem Details 표준 준수

### 레이어 구성 (Spring Standards 기준)
```
adapter-in/rest-api (Inbound Adapter)
├── controller/      - REST Endpoint 정의
├── dto/             - Request/Response DTO (Java Record)
│   ├── request/     - Request Body, Query Parameters
│   └── response/    - Response Body
├── mapper/          - DTO ↔ Command/Response 변환 (Static)
├── error/           - Domain Exception → HTTP Error 변환
└── config/
    └── properties/  - application.yml 엔드포인트 매핑
```

---

## 아키텍처 구조

### 헥사고날 아키텍처 흐름
```
[HTTP Request]
    ↓
Controller (adapter-rest)
    ↓ (Mapper.toCommand)
Application Layer (Command)
    ↓
UseCase/Facade
    ↓
Domain Layer
    ↓ (Response)
Application Layer
    ↓ (Mapper.toApiResponse)
Controller (adapter-rest)
    ↓
[HTTP Response]
```

### 의존성 방향
```
adapter-rest (Controller, DTO, Mapper)
    ↓ (의존)
application (UseCase, Facade, Command, Response)
    ↓ (의존)
domain (Aggregate, Entity, Value Object)
```

**중요**: REST API Layer는 **Domain Layer에 직접 의존하지 않음**. Application Layer를 통해서만 접근.

---

## 핵심 컨벤션

### 1. Lombok 사용 금지 (Zero-Tolerance)
```java
// ❌ 금지
@Data
@Builder
@Getter
@Setter
public class TenantApiRequest { }

// ✅ 올바름 - Java 21 Record
public record TenantApiRequest(
    @NotBlank String name
) { }

// ✅ 올바름 - Plain Java
public final class TenantApiMapper {
    private TenantApiMapper() {
        throw new UnsupportedOperationException("...");
    }

    public static TenantCommand toCommand(TenantApiRequest request) {
        // ...
    }
}
```

### 2. Mapper는 Static Utility Class
```java
// ❌ 금지 - @Component Bean
@Component
public class TenantApiMapper {
    public TenantCommand toCommand(TenantApiRequest request) { }
}

// ✅ 올바름 - Static Utility Class
public final class TenantApiMapper {
    private TenantApiMapper() {
        throw new UnsupportedOperationException("...");
    }

    public static TenantCommand toCommand(TenantApiRequest request) {
        // null 검증 + 변환 로직
    }
}
```

**Controller에서 사용**:
```java
// ❌ 금지 - DI로 주입
public class TenantController {
    private final TenantApiMapper mapper;

    public TenantController(TenantApiMapper mapper) {
        this.mapper = mapper;
    }

    public ResponseEntity<?> create(@RequestBody TenantApiRequest request) {
        TenantCommand command = mapper.toCommand(request);  // ❌
    }
}

// ✅ 올바름 - Static 메서드 직접 호출
public class TenantController {
    private final TenantCommandFacade facade;

    public TenantController(TenantCommandFacade facade) {
        this.facade = facade;
    }

    public ResponseEntity<?> create(@RequestBody TenantApiRequest request) {
        TenantCommand command = TenantApiMapper.toCommand(request);  // ✅
        TenantResponse response = facade.createTenant(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(TenantApiMapper.toApiResponse(response)));
    }
}
```

### 3. Request DTO는 Java 21 Record
```java
// ✅ Request Body
public record CreateTenantApiRequest(
    @NotBlank(message = "Tenant 이름은 필수입니다")
    String name
) {
}

// ✅ Query Parameters (검색)
public record OrganizationSearchApiRequest(
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    String cursor,
    @Positive Long tenantId,
    String nameContains
) {
    // Compact Constructor - 기본값
    public OrganizationSearchApiRequest {
        size = (size == null) ? 20 : size;
    }

    // Pagination 전략 판단
    public boolean isOffsetBased() {
        return page != null;
    }

    // Application Layer Query로 변환
    public GetOrganizationsQuery toQuery() {
        return new GetOrganizationsQuery(
            page, size, cursor, tenantId, nameContains
        );
    }
}
```

### 4. Controller는 Thin + UseCase/Facade 의존

**중요**: **Facade는 필요할 때만 사용**합니다 (YAGNI 원칙).

#### Facade 사용 기준 (Decision Tree)
```
UseCase가 2개 이상인가?
├─ Yes → ✅ Facade 사용 (그룹화)
└─ No → UseCase 1개
         ↓
    추가 로직 필요? (트랜잭션 조율, 데이터 통합)
    ├─ Yes → ✅ Facade 권장
    └─ No → ❌ UseCase 직접 호출 (Facade 불필요)
```

**자세한 내용**: [Facade 사용 가이드](../03-application-layer/facade/01_facade-usage-guide.md)

#### 예시 1: Facade 사용 (UseCase 2개 이상)
```java
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.tenant.base}")
public class TenantController {

    private final TenantCommandFacade tenantCommandFacade;  // ✅ 3개 UseCase 통합
    private final TenantQueryFacade tenantQueryFacade;      // ✅ 2개 UseCase 통합

    // ✅ Constructor Injection (Field Injection 금지)
    public TenantController(
        TenantCommandFacade tenantCommandFacade,
        TenantQueryFacade tenantQueryFacade
    ) {
        this.tenantCommandFacade = tenantCommandFacade;
        this.tenantQueryFacade = tenantQueryFacade;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TenantApiResponse>> createTenant(
        @Valid @RequestBody CreateTenantApiRequest request  // ✅ @Valid 필수
    ) {
        // 1. DTO → Command (Mapper static 메서드)
        CreateTenantCommand command = TenantApiMapper.toCommand(request);

        // 2. Facade 호출 (비즈니스 로직은 Facade/UseCase에서)
        TenantResponse response = tenantCommandFacade.createTenant(command);

        // 3. Response → DTO (Mapper static 메서드)
        TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);

        // 4. HTTP 응답
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }
}
```

#### 예시 2: UseCase 직접 호출 (UseCase 1개만 존재)

**상황**: Controller가 단일 UseCase만 호출하고, 추가 로직(트랜잭션 조율, 데이터 변환)이 필요 없는 경우

```java
// ✅ 단순한 경우 - UseCase 직접 호출 (Facade 불필요)
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.user-context.base}")
public class UserContextController {

    private final CreateUserContextUseCase createUserContextUseCase;  // ✅ UseCase 직접 의존

    public UserContextController(CreateUserContextUseCase createUserContextUseCase) {
        this.createUserContextUseCase = createUserContextUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserContextApiResponse>> create(
        @Valid @RequestBody CreateUserContextApiRequest request
    ) {
        // 1. DTO → Command
        CreateUserContextCommand command = UserContextApiMapper.toCommand(request);

        // 2. UseCase 직접 호출 (단순 위임)
        UserContextResponse response = createUserContextUseCase.execute(command);

        // 3. Response → DTO
        UserContextApiResponse apiResponse = UserContextApiMapper.toApiResponse(response);

        // 4. HTTP 응답
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }
}
```

**비교**:
- **Facade 사용**: Controller → Facade → UseCase 1, UseCase 2, UseCase 3 (3개 의존성 → 1개로 감소)
- **UseCase 직접 호출**: Controller → UseCase (1개 의존성, 추가 계층 불필요)

---

### 5. Error Mapper는 @Component Bean
```java
@Component  // ✅ Error Mapper는 예외적으로 @Component 사용
public class TenantApiErrorMapper implements ErrorMapper {

    private static final String PREFIX = "TENANT-";

    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        TenantErrorCode errorCode = findErrorCode(ex.code());

        return switch (errorCode) {  // ✅ Java 21 Switch Expression
            case TENANT_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.tenant.not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-not-found"))
            );
            // ...
        };
    }
}
```

---

## 단계별 구현 가이드

### Step 1: application.yml 엔드포인트 설정
```yaml
# adapter-in/rest-api/src/main/resources/application.yml

api:
  endpoints:
    base-v1: /api/v1
    iam:
      tenant:
        base: /tenants
        by-id: /{tenantId}
        status: /{tenantId}/status
```

### Step 2: Properties 클래스 생성
```java
// adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/config/properties/IamEndpointProperties.java

@Component
@ConfigurationProperties(prefix = "api.endpoints.iam")
public class IamEndpointProperties {

    private TenantEndpoints tenant = new TenantEndpoints();

    public TenantEndpoints getTenant() {
        return tenant;
    }

    public void setTenant(TenantEndpoints tenant) {
        this.tenant = tenant;
    }

    public static class TenantEndpoints {
        private String base = "/tenants";
        private String byId = "/{tenantId}";
        private String status = "/{tenantId}/status";

        // getters and setters
    }
}
```

### Step 3: Request DTO 생성
```java
// adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/dto/request/CreateTenantApiRequest.java

/**
 * Tenant 생성 API 요청
 *
 * <p><strong>Request Body 예시</strong>:</p>
 * <pre>
 * {
 *   "name": "테넌트명"
 * }
 * </pre>
 *
 * @param name Tenant 이름
 * @author ryu-qqq
 * @since 2025-10-27
 */
public record CreateTenantApiRequest(
    @NotBlank(message = "Tenant 이름은 필수입니다")
    String name
) {
}
```

### Step 4: Response DTO 생성
```java
// adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/dto/response/TenantApiResponse.java

/**
 * Tenant API 응답
 *
 * <p><strong>Response Body 예시</strong>:</p>
 * <pre>
 * {
 *   "tenantId": 1,
 *   "name": "테넌트명",
 *   "status": "ACTIVE",
 *   "deleted": false,
 *   "createdAt": "2025-10-27T10:00:00",
 *   "updatedAt": "2025-10-27T15:30:00"
 * }
 * </pre>
 *
 * @param tenantId Tenant ID
 * @param name Tenant 이름
 * @param status 상태
 * @param deleted 삭제 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author ryu-qqq
 * @since 2025-10-27
 */
public record TenantApiResponse(
    Long tenantId,
    String name,
    String status,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
```

### Step 5: Mapper 생성
```java
// adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/mapper/TenantApiMapper.java

/**
 * Tenant DTO Mapper
 *
 * <p>Tenant REST API DTO ↔ Application DTO 변환을 담당합니다.</p>
 *
 * <p><strong>규칙 준수</strong>:</p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Static Utility Class (Stateless, 인스턴스 생성 금지)</li>
 *   <li>✅ Null-safe 변환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
public final class TenantApiMapper {

    private TenantApiMapper() {
        throw new UnsupportedOperationException("Utility 클래스는 인스턴스화할 수 없습니다");
    }

    /**
     * CreateTenantApiRequest → CreateTenantCommand 변환
     *
     * @param request Tenant 생성 요청 DTO
     * @return CreateTenantCommand
     * @throws IllegalArgumentException request가 null인 경우
     */
    public static CreateTenantCommand toCommand(CreateTenantApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateTenantRequest는 null일 수 없습니다");
        }

        return new CreateTenantCommand(request.name());
    }

    /**
     * TenantResponse → TenantApiResponse 변환
     *
     * @param response Tenant Response
     * @return TenantApiResponse
     * @throws IllegalArgumentException response가 null인 경우
     */
    public static TenantApiResponse toApiResponse(TenantResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("TenantResponse는 null일 수 없습니다");
        }

        return new TenantApiResponse(
            response.tenantId(),
            response.name(),
            response.status(),
            response.deleted(),
            response.createdAt(),
            response.updatedAt()
        );
    }
}
```

### Step 6: Controller 생성
```java
// adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/controller/TenantController.java

/**
 * Tenant REST API Controller
 *
 * <p>Tenant CRUD API를 제공합니다.</p>
 *
 * <p><strong>Endpoint Base Path</strong>: {@code /api/v1/tenants}</p>
 *
 * <p><strong>제공 API</strong>:</p>
 * <ul>
 *   <li>POST /api/v1/tenants - Tenant 생성</li>
 *   <li>GET /api/v1/tenants/{tenantId} - Tenant 조회</li>
 *   <li>PUT /api/v1/tenants/{tenantId} - Tenant 수정</li>
 *   <li>DELETE /api/v1/tenants/{tenantId} - Tenant 삭제</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.tenant.base}")
public class TenantController {

    private final TenantCommandFacade tenantCommandFacade;
    private final TenantQueryFacade tenantQueryFacade;

    public TenantController(
        TenantCommandFacade tenantCommandFacade,
        TenantQueryFacade tenantQueryFacade
    ) {
        this.tenantCommandFacade = tenantCommandFacade;
        this.tenantQueryFacade = tenantQueryFacade;
    }

    /**
     * Tenant 생성 API
     *
     * <p><strong>HTTP Method</strong>: POST</p>
     * <p><strong>Path</strong>: /api/v1/tenants</p>
     * <p><strong>Response</strong>: 201 Created + {@link TenantApiResponse}</p>
     *
     * @param request Tenant 생성 요청
     * @return 201 Created + Tenant 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TenantApiResponse>> createTenant(
        @Valid @RequestBody CreateTenantApiRequest request
    ) {
        CreateTenantCommand command = TenantApiMapper.toCommand(request);
        TenantResponse response = tenantCommandFacade.createTenant(command);
        TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }
}
```

### Step 7: Error Mapper 생성 (선택사항)
```java
// adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/error/TenantApiErrorMapper.java

@Component
public class TenantApiErrorMapper implements ErrorMapper {

    private static final String PREFIX = "TENANT-";

    private final MessageSource messageSource;
    private final ApiErrorProperties errorProperties;

    public TenantApiErrorMapper(MessageSource messageSource, ApiErrorProperties errorProperties) {
        this.messageSource = messageSource;
        this.errorProperties = errorProperties;
    }

    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        TenantErrorCode errorCode = findErrorCode(ex.code());

        return switch (errorCode) {
            case TENANT_NOT_FOUND -> new MappedError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                getLocalizedMessage("error.tenant.not_found", ex.args(), locale, ex.getMessage()),
                URI.create(errorProperties.buildTypeUri("tenant-not-found"))
            );
            // ...
        };
    }

    private TenantErrorCode findErrorCode(String code) {
        return Arrays.stream(TenantErrorCode.values())
            .filter(e -> e.code().equals(code))
            .findFirst()
            .orElse(TenantErrorCode.TENANT_INTERNAL_ERROR);
    }

    private String getLocalizedMessage(String key, Object[] args, Locale locale, String defaultMessage) {
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }
}
```

---

## 컨벤션 검증

### ArchUnit 자동 검증
```bash
./gradlew :adapter-in:rest-api:test --tests "*RestApiAdapterConventionTest*"
```

**검증 항목 (25개 테스트)**:
1. ✅ Lombok 금지 (4개 테스트)
   - @Data, @Builder, @Getter, @Setter 사용 금지
2. ✅ Controller 컨벤션 (6개 테스트)
   - @RestController 어노테이션
   - *Controller 네이밍
   - @RequestMapping 어노테이션
   - Handler 메서드는 public
   - @Valid 어노테이션 사용
3. ✅ DTO 컨벤션 (5개 테스트)
   - Request/Response DTO는 Record
   - *ApiRequest/*ApiResponse 네이밍
4. ✅ Mapper 컨벤션 (4개 테스트)
   - final 클래스
   - private 생성자
   - 모든 메서드 static
   - *ApiMapper 네이밍
5. ✅ Error Mapper 컨벤션 (3개 테스트)
   - @Component 어노테이션
   - ErrorMapper 인터페이스 구현
   - *ApiErrorMapper 네이밍
6. ✅ Properties 컨벤션 (3개 테스트)
   - @Component + @ConfigurationProperties
   - *Properties 네이밍

### 수동 체크리스트

#### Request DTO
- [ ] Java 21 Record 사용
- [ ] Bean Validation 어노테이션 (`@NotBlank`, `@Valid`, `@Min`, `@Max`)
- [ ] Query Parameter용: `isOffsetBased()` + `toQuery()` 메서드
- [ ] Compact Constructor (기본값 설정)
- [ ] Javadoc + JSON 예시

#### Response DTO
- [ ] Java 21 Record 사용
- [ ] Javadoc + JSON 예시

#### Mapper
- [ ] `final` 클래스
- [ ] `private` 생성자 (`throw new UnsupportedOperationException`)
- [ ] 모든 public/private 메서드 `static`
- [ ] Null-safe 검증 (모든 입력 파라미터)
- [ ] `toCommand()`, `toApiResponse()` 메서드명
- [ ] Javadoc

#### Controller
- [ ] `@RestController` + `@RequestMapping("${...}")` (Properties 사용)
- [ ] Constructor Injection (Field Injection 금지)
- [ ] Facade 의존성 (UseCase 직접 호출 금지)
- [ ] `@Valid` 어노테이션 (`@RequestBody`, `@ModelAttribute`)
- [ ] `ResponseEntity` + HTTP Status 명시
- [ ] Mapper static 메서드 직접 호출 (DI 금지)
- [ ] Thin Controller (비즈니스 로직 금지)
- [ ] 포괄적인 Javadoc (HTTP Method, Path, Status Codes, 예시)

#### Error Mapper
- [ ] `@Component` + `implements ErrorMapper`
- [ ] `supports(String code)` 메서드 (Prefix 검증)
- [ ] `map(DomainException ex, Locale locale)` 메서드 (Switch Expression)
- [ ] RFC 7807 Problem Details 포맷
- [ ] MessageSource를 통한 i18n
- [ ] Javadoc

#### Properties
- [ ] `@Component` + `@ConfigurationProperties`
- [ ] 중첩 static 클래스 (도메인별 그룹화)
- [ ] 필드 기본값 설정
- [ ] Pure Java getter/setter (Lombok 금지)
- [ ] Javadoc

---

## 참고 자료

### 참조 구현
- **Tenant** (기본 CRUD): `adapter-in/rest-api/.../tenant/`
- **Organization** (Query Parameter 검색): `adapter-in/rest-api/.../organization/`
- **Permission** (복잡한 요청 변환): `adapter-in/rest-api/.../permission/`

### 관련 문서
- [ArchUnit Test](../../adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/architecture/RestApiAdapterConventionTest.java)
- [Windsurf Cascade Workflow](../../.windsurf/workflows/cc-rest-api.md)
- [헥사고날 아키텍처 가이드](../00-architecture/hexagonal-architecture.md)
- [Application Layer 가이드](../03-application-layer/00_application-creation-guide.md)

### 외부 표준
- [RFC 7807 - Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc7807)
- [Bean Validation 3.0](https://beanvalidation.org/3.0/)
- [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)

---

## 문제 해결 (Troubleshooting)

### Q1: Mapper를 왜 @Component가 아닌 Static Utility로?
**A**: Mapper는 Stateless이고 의존성이 없으므로 Bean으로 등록할 필요가 없습니다. Static Utility Class 패턴이 더 간결하고 효율적입니다.

### Q2: Controller에서 UseCase를 직접 호출하면 안 되나요?
**A**: ❌ 안 됩니다. 반드시 **Facade**를 통해 호출해야 합니다. Facade는 여러 UseCase를 조율하고 트랜잭션 경계를 관리합니다.

### Q3: Query Parameter DTO에 왜 toQuery() 메서드가 필요한가요?
**A**: Controller Layer의 DTO를 Application Layer의 Query 객체로 변환하기 위함입니다. 레이어 간 DTO 변환을 명확하게 합니다.

### Q4: Error Mapper는 왜 @Component를 사용하나요?
**A**: Error Mapper는 `MessageSource`, `ApiErrorProperties` 등의 Bean에 의존하므로 Spring Bean으로 등록되어야 합니다.

### Q5: Bean Validation이 왜 필요한가요?
**A**: HTTP 요청 데이터의 기본적인 유효성 검증을 Controller Layer에서 처리하여, Application Layer에는 검증된 데이터만 전달하기 위함입니다.

---

## 체크리스트 요약

### 필수 준수 사항 (Zero-Tolerance)
- [ ] ❌ Lombok 사용 금지
- [ ] ✅ Request/Response DTO는 Java 21 Record
- [ ] ✅ Mapper는 `final` + `private` 생성자 + `static` 메서드
- [ ] ✅ Controller는 Facade 의존 (UseCase 직접 호출 금지)
- [ ] ✅ Controller는 Thin (비즈니스 로직 금지)
- [ ] ✅ Bean Validation 사용 (`@Valid` + 제약 조건 어노테이션)
- [ ] ✅ Error Mapper는 RFC 7807 준수

### 권장 사항
- [ ] Query Parameter DTO: `isOffsetBased()` + `toQuery()` 패턴
- [ ] Properties: 도메인별 중첩 static 클래스
- [ ] Controller: 포괄적인 Javadoc (HTTP Method, Path, Status, 예시)
- [ ] Mapper: Null-safe 검증
- [ ] Error Mapper: Switch Expression 사용

---

**✅ 이 가이드를 따르면 일관된 REST API Adapter Layer를 구축할 수 있으며, ArchUnit 테스트가 자동으로 컨벤션 준수를 검증합니다.**
