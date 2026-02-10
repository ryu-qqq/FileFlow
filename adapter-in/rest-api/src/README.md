# AuthHub SDK

AuthHub REST API와 통합하기 위한 공식 Java SDK입니다. 멀티 테넌트 IAM(Identity and Access Management) 시스템과의 연동을 제공합니다.

## 목차

- [개요](#개요)
- [요구사항](#요구사항)
- [설치](#설치)
- [빠른 시작](#빠른-시작)
- [설정](#설정)
- [환경별 설정](#환경별-설정)
- [클라이언트 구조](#클라이언트-구조)
- [AuthHubClient API](#authhubclient-api)
- [GatewayClient API](#gatewayclient-api)
- [인증 메커니즘](#인증-메커니즘)
- [엔드포인트 자동 동기화](#엔드포인트-자동-동기화)
- [Spring Boot Starter 기능](#spring-boot-starter-기능)
- [접근 권한 검사 (AccessChecker)](#accesschecker---접근-권한-검사)
- [에러 처리](#에러-처리)
- [모듈 구조](#모듈-구조)

---

## 개요

AuthHub SDK는 두 개의 모듈로 구성됩니다:

| 모듈 | 설명 | 용도 |
|------|------|------|
| `authhub-sdk-core` | 순수 Java SDK 코어 | 모든 Java 프로젝트 |
| `authhub-sdk-spring-boot-starter` | Spring Boot 자동 설정 | Spring Boot 프로젝트 |

### 클라이언트 유형

| 클라이언트 | 인증 방식 | 용도 |
|------------|-----------|------|
| `AuthHubClient` | `Authorization: Bearer {token}` | 사용자 인증 API, 온보딩, 사용자 생성 |
| `GatewayClient` | `X-Service-Name` + `X-Service-Token` | Gateway 전용 Internal API |

---

## 요구사항

| 요구사항 | 버전 |
|----------|------|
| Java | 21+ |
| Spring Boot (선택) | 3.x |

---

## 설치

### Gradle (JitPack)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

#### Spring Boot 프로젝트 (권장)

```groovy
dependencies {
    implementation 'com.github.ryu-qqq.AuthHub:authhub-sdk-spring-boot-starter:{version}'
}
```

#### 순수 Java 프로젝트

```groovy
dependencies {
    implementation 'com.github.ryu-qqq.AuthHub:authhub-sdk-core:{version}'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ryu-qqq.AuthHub</groupId>
    <artifactId>authhub-sdk-spring-boot-starter</artifactId>
    <version>{version}</version>
</dependency>
```


---

## 빠른 시작

### Spring Boot

```yaml
# application.yml
authhub:
  base-url: https://auth.example.com
  service-token: ${AUTHHUB_SERVICE_TOKEN}
```

```java
@Service
public class AuthService {

    private final AuthApi authApi;

    public AuthService(AuthApi authApi) {
        this.authApi = authApi;
    }

    public LoginResponse login(String identifier, String password) {
        ApiResponse<LoginResponse> response = authApi.login(
            new LoginRequest(identifier, password)
        );
        return response.data();
    }
}
```

### 순수 Java

```java
AuthHubClient client = AuthHubClient.builder()
    .baseUrl("https://auth.example.com")
    .serviceToken("your-service-token")
    .build();

ApiResponse<LoginResponse> response = client.auth().login(
    new LoginRequest("user@example.com", "password")
);
```

---

## 설정

### Spring Boot 설정 옵션

```yaml
authhub:
  # [필수] AuthHub 서버 URL
  base-url: https://auth.example.com

  # [선택] 서비스 토큰 (M2M 통신용)
  # ThreadLocal 토큰이 없을 경우 fallback으로 사용
  service-token: ${AUTHHUB_SERVICE_TOKEN}

  # [선택] 서비스 코드 (엔드포인트 동기화 시 Role-Permission 자동 매핑용)
  service-code: SVC_STORE

  # [선택] 타임아웃 설정
  timeout:
    connect: 5s      # 연결 타임아웃 (기본: 5초)
    read: 30s        # 읽기 타임아웃 (기본: 30초)

  # [선택] 재시도 설정
  retry:
    enabled: true    # 재시도 활성화 (기본: true)
    max-attempts: 3  # 최대 재시도 횟수 (기본: 3)
    delay: 1s        # 재시도 대기 시간 (기본: 1초)
```

### 환경별 설정

AuthHub 서버는 환경(staging, production)별로 URL과 서비스 토큰이 다릅니다. Spring Boot Profile을 사용하여 환경을 분리하세요.

#### 서비스 토큰 조회 (AWS SSM Parameter Store)

서비스 토큰은 AWS SSM Parameter Store에 `SecureString`으로 관리됩니다.

| 환경 | SSM 파라미터 경로 |
|------|-------------------|
| Production | `/authhub/security/service-token-secret` |
| Staging | `/authhub/stage/security/service-token-secret` |

```bash
# Staging 서비스 토큰 조회
aws ssm get-parameter \
  --name "/authhub/stage/security/service-token-secret" \
  --with-decryption \
  --query "Parameter.Value" \
  --output text

# Production 서비스 토큰 조회
aws ssm get-parameter \
  --name "/authhub/security/service-token-secret" \
  --with-decryption \
  --query "Parameter.Value" \
  --output text
```

> ECS/EC2 등 AWS 환경에서는 Task Definition이나 Launch Template에서 SSM 파라미터를 환경 변수로 주입하는 것을 권장합니다.

#### 공통 설정

```yaml
# application.yml - 환경 공통 설정
authhub:
  service-code: SVC_STORE
  timeout:
    connect: 5s
    read: 30s
  retry:
    enabled: true
    max-attempts: 3
```

#### Staging 환경

```yaml
# application-staging.yml
authhub:
  base-url: https://auth-staging.example.com
  service-token: ${AUTHHUB_SERVICE_TOKEN}
  # AUTHHUB_SERVICE_TOKEN 환경변수에 SSM 파라미터 값 주입
  # SSM 경로: /authhub/stage/security/service-token-secret
```

#### Production 환경

```yaml
# application-prod.yml
authhub:
  base-url: https://auth.example.com
  service-token: ${AUTHHUB_SERVICE_TOKEN}
  # AUTHHUB_SERVICE_TOKEN 환경변수에 SSM 파라미터 값 주입
  # SSM 경로: /authhub/security/service-token-secret
```

#### ECS Task Definition에서 SSM 주입 예시

```json
{
  "containerDefinitions": [{
    "secrets": [
      {
        "name": "AUTHHUB_SERVICE_TOKEN",
        "valueFrom": "/authhub/stage/security/service-token-secret"
      }
    ]
  }]
}
```

Terraform 사용 시:

```hcl
secrets = [
  {
    name      = "AUTHHUB_SERVICE_TOKEN"
    valueFrom = "/authhub/${var.environment}/security/service-token-secret"
    # environment = "stage" 또는 "" (prod는 /authhub/security/... 경로)
  }
]
```

#### 실행 방법

```bash
# Staging 환경으로 실행
java -jar app.jar --spring.profiles.active=staging

# Production 환경으로 실행
java -jar app.jar --spring.profiles.active=prod

# 환경 변수로 프로필 지정
SPRING_PROFILES_ACTIVE=staging java -jar app.jar
```

#### 환경별 동작 요약

| 설정 항목 | Staging | Production | 비고 |
|-----------|---------|------------|------|
| `base-url` | auth-staging.example.com | auth.example.com | AuthHub 서버 주소 |
| `service-token` | SSM: `/authhub/stage/security/service-token-secret` | SSM: `/authhub/security/service-token-secret` | 환경변수로 주입 |
| `service-code` | 동일 | 동일 | 공통 설정에서 관리 |
| 엔드포인트 동기화 | staging AuthHub에 등록 | production AuthHub에 등록 | base-url 기준 자동 분리 |

> **주의**: `service-token`은 절대 코드나 설정 파일에 하드코딩하지 마세요. 반드시 AWS SSM Parameter Store에서 환경 변수로 주입하세요.

### 프로그래매틱 설정

```java
AuthHubClient client = AuthHubClient.builder()
    .baseUrl("https://auth.example.com")
    .serviceToken("your-service-token")
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(60))
    .build();
```

---

## 클라이언트 구조

SDK는 두 종류의 HTTP 클라이언트를 제공하며, 인증 방식과 용도가 다릅니다.

### AuthHubClient

사용자 인증이 필요한 API를 호출합니다. `Authorization: Bearer {token}` 헤더를 사용합니다.

```
AuthHubClient
├── auth()       → AuthApi        (로그인, 로그아웃, 토큰 갱신, 내 정보)
├── onboarding() → OnboardingApi  (테넌트 온보딩)
└── user()       → UserApi        (사용자 생성 + 역할 할당)
```

### GatewayClient

Gateway에서 Internal API를 호출합니다. `X-Service-Name` + `X-Service-Token` 헤더를 사용합니다.

```
GatewayClient
└── internal()   → InternalApi    (권한 스펙, JWKS, 테넌트 설정, 사용자 권한)
```

---

## AuthHubClient API

### AuthApi - 인증

```java
AuthApi auth = client.auth();

// 로그인
ApiResponse<LoginResponse> login = auth.login(
    new LoginRequest("user@example.com", "password")
);
// → LoginResponse(userId, accessToken, refreshToken, expiresIn, tokenType)

// 토큰 갱신
ApiResponse<TokenResponse> token = auth.refresh(
    new RefreshTokenRequest(refreshToken)
);
// → TokenResponse(accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn, tokenType)

// 로그아웃
auth.logout(new LogoutRequest(userId));

// 내 정보 조회 (테넌트, 조직, 역할, 권한 포함)
ApiResponse<MyContextResponse> me = auth.getMe();
// → MyContextResponse(userId, email, name, tenant, organization, roles, permissions)

// 사용자 정보 수정
ApiResponse<UserIdResponse> updated = auth.updateUser(userId,
    new UpdateUserRequest("010-1234-5678")
);

// 비밀번호 변경
auth.changePassword(userId,
    new ChangePasswordRequest("currentPassword", "newPassword")
);
```

### OnboardingApi - 테넌트 온보딩

테넌트 + 조직을 한 번에 생성합니다. 멱등키를 통해 중복 요청을 방지합니다.

```java
OnboardingApi onboarding = client.onboarding();

// 테넌트 온보딩 (X-Idempotency-Key 헤더 전송)
ApiResponse<TenantOnboardingResponse> result = onboarding.onboard(
    new TenantOnboardingRequest("My Company", "Main Organization"),
    UUID.randomUUID().toString()  // 멱등키
);
// → TenantOnboardingResponse(tenantId, organizationId)
// HTTP 201 Created 반환
```

### UserApi - 사용자 생성 + 역할 할당

Internal API로, 사용자 생성과 역할 할당을 한 번에 처리합니다.

```java
UserApi user = client.user();

// 사용자 생성 + 역할 할당
ApiResponse<CreateUserWithRolesResponse> result = user.createUserWithRoles(
    new CreateUserWithRolesRequest(
        "org-uuid",              // 소속 조직 ID (필수)
        "user@example.com",      // 로그인 식별자 (필수)
        "010-1234-5678",         // 전화번호 (선택)
        "password123",           // 비밀번호 (필수)
        "SVC_STORE",             // 서비스 코드 (선택, SERVICE scope Role 매핑용)
        List.of("ADMIN")         // 역할 이름 (선택)
    )
);
// → CreateUserWithRolesResponse(userId, assignedRoleCount)
// HTTP 201 Created 반환
```

**역할 매핑 로직:**
- `serviceCode` + `roleNames` 제공 → SERVICE scope Role 할당
- `roleNames`만 제공 → GLOBAL scope Role 할당
- `roleNames` 없음 → 사용자만 생성 (역할 할당 스킵)

---

## GatewayClient API

### InternalApi - Gateway 전용

Gateway에서 인증/인가 처리에 필요한 정보를 조회합니다.

```java
GatewayClient gateway = GatewayClient.builder()
    .baseUrl("https://authhub.example.com")
    .serviceName("gateway")
    .serviceToken("your-service-token")
    .build();

InternalApi internal = gateway.internal();

// 1. 엔드포인트-권한 스펙 조회 (Gateway 시작 시 캐싱 권장)
ApiResponse<EndpointPermissionSpecList> spec = internal.getPermissionSpec();
// → EndpointPermissionSpecList(version, updatedAt, endpoints[])
//   각 endpoint: EndpointPermissionSpec(serviceName, pathPattern, httpMethod,
//                requiredPermissions, requiredRoles, isPublic, description)

// 2. JWKS 공개키 조회 (JWT 서명 검증용, RFC 7517)
PublicKeys keys = internal.getJwks();
// → PublicKeys(keys[])
//   각 key: PublicKey(kid, kty, alg, use, n, e)

// 3. 테넌트 설정 조회 (테넌트 유효성 검증용)
ApiResponse<TenantConfig> config = internal.getTenantConfig("tenant-123");
// → TenantConfig(tenantId, name, status, active)

// 4. 사용자 권한 조회 (인가 검증용)
ApiResponse<UserPermissions> perms = internal.getUserPermissions("user-456");
// → UserPermissions(userId, roles, permissions, hash, generatedAt)
//   hash: 권한 해시 (변경 감지용), generatedAt: 권한 생성 시점
```

### 타임아웃 설정

```java
GatewayClient gateway = GatewayClient.builder()
    .baseUrl("https://authhub.example.com")
    .serviceName("gateway")
    .serviceToken("your-service-token")
    .connectTimeout(Duration.ofSeconds(5))    // 기본: 5초
    .readTimeout(Duration.ofSeconds(30))      // 기본: 30초
    .build();
```

---

## 인증 메커니즘

### AuthHubClient 인증 (Bearer Token)

AuthHubClient는 `Authorization: Bearer {token}` 헤더를 사용합니다. 토큰은 `TokenResolver` 체인을 통해 결정됩니다.

#### 토큰 우선순위

```
1. ThreadLocal 토큰 (사용자 요청에서 자동 추출)
2. 서비스 토큰 (authhub.service-token 설정값)
3. AuthHubUnauthorizedException 발생
```

#### TokenResolver 구현체

| 구현체 | 용도 |
|--------|------|
| `ThreadLocalTokenResolver` | HTTP 요청의 Bearer 토큰을 ThreadLocal에 저장/조회 |
| `StaticTokenResolver` | 고정 서비스 토큰 (M2M 통신) |
| `ChainTokenResolver` | 여러 Resolver를 순차적으로 시도 |

Spring Boot AutoConfiguration에서는 `ChainTokenResolver.withFallback(serviceToken)`이 자동 등록되어, ThreadLocal 토큰을 먼저 시도하고 없으면 서비스 토큰을 사용합니다.

#### 커스텀 TokenResolver

```java
TokenResolver customResolver = () -> {
    String token = MySecurityContext.getCurrentToken();
    return Optional.ofNullable(token);
};

AuthHubClient client = AuthHubClient.builder()
    .baseUrl("https://auth.example.com")
    .tokenResolver(customResolver)
    .build();
```

### GatewayClient 인증 (Service Token)

GatewayClient는 **Bearer Token이 아닌** 커스텀 헤더를 사용합니다:

| 헤더 | 설명 |
|------|------|
| `X-Service-Name` | 서비스 이름 (예: `gateway`) |
| `X-Service-Token` | 서비스 토큰 |

Gateway 아키텍처에서 다운스트림 서비스는 `Authorization: Bearer` 헤더를 받지 않고, `X-USER-ID` 같은 커스텀 헤더를 받습니다. GatewayClient는 이 구조에 맞게 설계되었습니다.

---

## 엔드포인트 자동 동기화

애플리케이션 시작 시 `@RequirePermission` 어노테이션이 붙은 엔드포인트를 AuthHub에 자동 동기화합니다.

> **참고**: 이 기능은 자동 설정(Auto-Configuration)에 포함되지 않습니다. 아래 3단계 설정을 직접 수행해야 합니다. SDK는 스캔/요청 생성만 담당하고, HTTP 전송 방식은 프로젝트에서 선택합니다 (RestTemplate, WebClient 등).

### 동작 흐름

```
앱 시작 → ApplicationRunner.run()
  → EndpointScanner: @RequirePermission 어노테이션 스캔
  → EndpointSyncRequest 생성 (serviceName, serviceCode, endpoints)
  → EndpointSyncClient.sync(): POST /api/v1/internal/endpoints/sync
  → AuthHub 서버: 없는 것만 새로 생성 (멱등성 보장)
```

### 설정 방법 (3단계)

#### Step 1. @RequirePermission 어노테이션 선언

Controller 메서드에 필요한 권한을 선언합니다. 이 어노테이션은 **권한 체크를 직접 수행하지 않으며**, 동기화 스캔 대상을 표시하는 역할입니다. 실제 권한 체크는 Gateway에서 수행됩니다.

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @GetMapping("/{id}")
    @RequirePermission(value = "product:read", description = "상품 조회")
    public ProductResponse getProduct(@PathVariable Long id) {
        // ...
    }

    @PostMapping
    @RequirePermission(value = "product:create", description = "상품 생성")
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
        // ...
    }

    // 백엔드에서도 추가 검증이 필요한 경우 @PreAuthorize와 함께 사용
    @DeleteMapping("/{id}")
    @RequirePermission(value = "product:delete", description = "상품 삭제")
    @PreAuthorize("@access.hasPermission('product:delete')")
    public void deleteProduct(@PathVariable Long id) {
        // ...
    }
}
```

#### Step 2. EndpointSyncClient 구현

SDK는 `EndpointSyncClient` 인터페이스만 제공합니다. HTTP 전송 방식은 프로젝트의 HTTP 클라이언트에 맞게 직접 구현하세요.

```java
@Component
public class HttpEndpointSyncClient implements EndpointSyncClient {

    private final RestTemplate restTemplate;
    private final String authHubUrl;
    private final String serviceToken;

    public HttpEndpointSyncClient(
            RestTemplate restTemplate,
            @Value("${authhub.base-url}") String authHubUrl,
            @Value("${authhub.service-token}") String serviceToken) {
        this.restTemplate = restTemplate;
        this.authHubUrl = authHubUrl;
        this.serviceToken = serviceToken;
    }

    @Override
    public void sync(EndpointSyncRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Name", request.serviceName());
        headers.set("X-Service-Token", serviceToken);

        restTemplate.postForEntity(
            authHubUrl + "/api/v1/internal/endpoints/sync",
            new HttpEntity<>(request, headers),
            Void.class
        );
    }
}
```

> `authhub.base-url`은 환경별 profile에 의해 자동으로 staging/production AuthHub 서버를 가리킵니다. [환경별 설정](#환경별-설정) 참고.

#### Step 3. EndpointSyncRunner 빈 등록

```java
@Configuration
public class EndpointSyncConfig {

    @Bean
    public EndpointSyncRunner endpointSyncRunner(
            RequestMappingHandlerMapping handlerMapping,
            EndpointSyncClient syncClient,
            @Value("${spring.application.name}") String serviceName,
            @Value("${authhub.service-code:}") String serviceCode) {
        return new EndpointSyncRunner(handlerMapping, syncClient, serviceName, serviceCode);
    }

    // 특정 환경에서 동기화를 비활성화하려면 enabled=false
    // @Bean
    // @Profile("test")
    // public EndpointSyncRunner disabledSyncRunner(...) {
    //     return new EndpointSyncRunner(handlerMapping, syncClient, serviceName, serviceCode, false);
    // }
}
```

#### application.yml 설정

```yaml
# application.yml - 공통
spring:
  application:
    name: product-service         # serviceName으로 사용됨

authhub:
  service-code: SVC_PRODUCT       # serviceCode (Role-Permission 자동 매핑용)

---
# application-staging.yml
authhub:
  base-url: https://auth-staging.example.com    # staging AuthHub에 동기화
  service-token: ${AUTHHUB_SERVICE_TOKEN}

---
# application-prod.yml
authhub:
  base-url: https://auth.example.com            # production AuthHub에 동기화
  service-token: ${AUTHHUB_SERVICE_TOKEN}
```

### 환경별 동기화 동작

| 환경 | 동기화 대상 | 설명 |
|------|------------|------|
| staging | auth-staging.example.com | staging AuthHub에 엔드포인트 등록 |
| production | auth.example.com | production AuthHub에 엔드포인트 등록 |
| test | 비활성화 권장 | `EndpointSyncRunner(... enabled=false)` 또는 빈 미등록 |

각 환경의 AuthHub 서버는 독립적으로 데이터를 관리하므로, staging에서 등록한 엔드포인트가 production에 영향을 주지 않습니다.

### 서버 측 동기화 로직

AuthHub 서버는 다음과 같은 **Create-Missing-Only** 패턴으로 동기화합니다:

1. **Permission 동기화**: `permissionKey`로 IN절 조회 → 없는 것만 생성
2. **PermissionEndpoint 동기화**: `(serviceName, urlPattern, httpMethod)` 조합으로 중복 판단 → 없는 것만 생성
3. **자동 Role-Permission 매핑** (serviceCode 제공 시):

| permissionKey의 action | 매핑 대상 Role |
|---|---|
| `read`, `list`, `search`, `get` | ADMIN, EDITOR, VIEWER |
| `create`, `update`, `write`, `edit` | ADMIN, EDITOR |
| `delete`, 기타 | ADMIN only |

기존 데이터는 건드리지 않으므로 **멱등성이 보장**됩니다. 서비스가 여러 번 재시작해도 중복 데이터가 생기지 않습니다.

### 동기화 요청 형식

```json
{
  "serviceName": "product-service",
  "serviceCode": "SVC_PRODUCT",
  "endpoints": [
    {
      "httpMethod": "GET",
      "pathPattern": "/api/v1/products/{id}",
      "permissionKey": "product:read",
      "description": "상품 조회"
    },
    {
      "httpMethod": "POST",
      "pathPattern": "/api/v1/products",
      "permissionKey": "product:create",
      "description": "상품 생성"
    }
  ]
}
```

### 주의사항

- 동기화 실패 시에도 애플리케이션 시작은 계속 진행됩니다 (fail-safe)
- 오토스케일링으로 여러 인스턴스가 동시에 시작해도 멱등성이 보장됩니다
- `enabled` 파라미터로 동기화를 비활성화할 수 있습니다

---

## Spring Boot Starter 기능

`authhub-sdk-spring-boot-starter`는 코어 SDK 외에 다음 기능을 추가로 제공합니다.

### 자동 등록되는 빈

| 빈 | 조건 | 설명 |
|---|---|---|
| `TokenResolver` | `authhub.base-url` 설정 시 | ChainTokenResolver (ThreadLocal + 서비스 토큰) |
| `AuthHubClient` | `authhub.base-url` 설정 시 | 메인 클라이언트 |
| `AuthApi` | AuthHubClient 존재 시 | 인증 API |
| `OnboardingApi` | AuthHubClient 존재 시 | 온보딩 API |
| `UserApi` | AuthHubClient 존재 시 | 사용자 API |
| `AuthHubTokenContextFilter` | Servlet 환경 | `Authorization` 헤더 → ThreadLocal 자동 전파 |

### UserContext

Gateway에서 전달한 헤더 정보를 기반으로 현재 요청의 사용자 컨텍스트에 접근합니다.

```java
UserContext context = UserContextHolder.getContext();

// 사용자 정보
String userId = context.getUserId();
String tenantId = context.getTenantId();
String organizationId = context.getOrganizationId();
String email = context.getEmail();

// 역할/권한 확인
Set<String> roles = context.getRoles();
Set<String> permissions = context.getPermissions();
boolean isAdmin = context.hasRole("ADMIN");
boolean canRead = context.hasPermission("product:read");

// 서비스 계정 여부
boolean isService = context.isServiceAccount();

// 인증 여부
boolean authenticated = context.isAuthenticated();

// 추가 컨텍스트 정보
String scope = context.getScope();                // 현재 범위
String correlationId = context.getCorrelationId(); // 분산 추적 ID
String requestSource = context.getRequestSource(); // 요청 출처 서비스
```

### 보안 헤더 상수

`SecurityHeaders` 클래스에서 제공하는 헤더 상수입니다:

```java
// ===== 사용자 컨텍스트 헤더 (Gateway → Backend) =====
SecurityHeaders.USER_ID                     // "X-User-Id"
SecurityHeaders.TENANT_ID                   // "X-Tenant-Id"
SecurityHeaders.ORGANIZATION_ID             // "X-Organization-Id"
SecurityHeaders.ROLES                       // "X-User-Roles" (쉼표 구분)
SecurityHeaders.PERMISSIONS                 // "X-User-Permissions" (쉼표 구분)
SecurityHeaders.USER_EMAIL                  // "X-User-Email"

// ===== 서비스 인증 헤더 (Service → Service) =====
SecurityHeaders.SERVICE_NAME                // "X-Service-Name"
SecurityHeaders.SERVICE_TOKEN               // "X-Service-Token"

// ===== 서비스간 통신 확장 헤더 =====
SecurityHeaders.ORIGINAL_USER_ID            // "X-Original-User-Id"
SecurityHeaders.ORIGINAL_TENANT_ID          // "X-Original-Tenant-Id"
SecurityHeaders.ORIGINAL_ORGANIZATION_ID    // "X-Original-Organization-Id"

// ===== 추적 헤더 =====
SecurityHeaders.CORRELATION_ID              // "X-Correlation-Id"
SecurityHeaders.REQUEST_SOURCE              // "X-Request-Source"
SecurityHeaders.REQUEST_ID                  // "X-Request-Id"

// ===== Gateway 내부 헤더 =====
SecurityHeaders.AUTHENTICATED               // "X-Authenticated"
SecurityHeaders.AUTH_TYPE                    // "X-Auth-Type"

// ===== 유틸리티 =====
SecurityHeaders.isSecurityHeader("X-User-Id")   // → true
SecurityHeaders.isSensitiveHeader("X-Service-Token") // → true (로깅 시 마스킹 필요)
```

### 권한 상수

`Permissions` 클래스에서 제공하는 권한 키 상수입니다:

```java
// User
Permissions.USER_READ          // "user:read"
Permissions.USER_WRITE         // "user:write"
Permissions.USER_DELETE        // "user:delete"
Permissions.USER_ROLE_ASSIGN   // "user:role:assign"

// Role
Permissions.ROLE_READ          // "role:read"
Permissions.ROLE_WRITE         // "role:write"
Permissions.ROLE_DELETE        // "role:delete"

// Permission
Permissions.PERMISSION_READ    // "permission:read"
Permissions.PERMISSION_WRITE   // "permission:write"
Permissions.PERMISSION_DELETE  // "permission:delete"

// Organization
Permissions.ORGANIZATION_READ   // "organization:read"
Permissions.ORGANIZATION_WRITE  // "organization:write"
Permissions.ORGANIZATION_DELETE // "organization:delete"

// Tenant
Permissions.TENANT_READ        // "tenant:read"
Permissions.TENANT_WRITE       // "tenant:write"
Permissions.TENANT_DELETE      // "tenant:delete"

// Product
Permissions.PRODUCT_READ       // "product:read"
Permissions.PRODUCT_WRITE      // "product:write"
Permissions.PRODUCT_DELETE     // "product:delete"

// File
Permissions.FILE_READ          // "file:read"
Permissions.FILE_UPLOAD        // "file:upload"
Permissions.FILE_DELETE        // "file:delete"

// Wildcard (Super Admin용)
Permissions.ALL                // "*:*"

// 유틸리티 - 생성
Permissions.of("product", "create")              // → "product:create"
Permissions.of("authhub", "user", "read")        // → "authhub:user:read" (3세그먼트)

// 유틸리티 - 추출
Permissions.extractDomain("user:read")           // → "user"
Permissions.extractAction("user:read")           // → "read"
Permissions.extractService("authhub:user:read")  // → "authhub" (3세그먼트에서만)

// 유틸리티 - 검증
Permissions.isValidFormat("user:read")           // → true (엄격한 검증)
Permissions.validateFormat("user:read")          // → Optional.empty() (상세 오류 메시지 포함)
```

### 역할 상수

`Roles` 클래스에서 제공하는 역할 상수입니다:

```java
Roles.SUPER_ADMIN    // "ROLE_SUPER_ADMIN"  - 전역 관리자
Roles.TENANT_ADMIN   // "ROLE_TENANT_ADMIN" - 테넌트 관리자
Roles.ORG_ADMIN      // "ROLE_ORG_ADMIN"    - 조직 관리자
Roles.USER           // "ROLE_USER"         - 일반 사용자
Roles.SERVICE        // "ROLE_SERVICE"      - 서비스 계정

// 유틸리티
Roles.isSystemRole("ROLE_SUPER_ADMIN")  // → true
Roles.isAdminRole("ROLE_TENANT_ADMIN")  // → true
```

### 범위 상수

`Scopes` 클래스에서 제공하는 범위 상수입니다:

```java
Scopes.GLOBAL        // "GLOBAL"       - 전역 (모든 테넌트/조직)
Scopes.TENANT        // "TENANT"       - 테넌트 내 모든 조직
Scopes.ORGANIZATION  // "ORGANIZATION" - 해당 조직만

// 유틸리티
Scopes.isValidScope("GLOBAL")              // → true
Scopes.getLevel("GLOBAL")                  // → 3 (GLOBAL=3, TENANT=2, ORGANIZATION=1)
Scopes.includes("GLOBAL", "TENANT")        // → true (상위 범위가 하위를 포함)
```

### AccessChecker - 접근 권한 검사

`AccessChecker`는 Spring Security `@PreAuthorize`에서 SpEL 표현식으로 사용할 수 있는 권한 검사 인터페이스입니다. `BaseAccessChecker` 추상 클래스가 기본 구현을 제공합니다.

#### 설정 방법

```java
@Component("access")
public class ResourceAccessChecker extends BaseAccessChecker {

    // 도메인별 확장 메서드 추가 가능
    public boolean canReadFile() {
        return hasPermission("file:read");
    }
}
```

#### 사용 예시

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // 인증 확인
    @PreAuthorize("@access.authenticated()")
    @GetMapping
    public List<UserResponse> listUsers() { ... }

    // 권한 확인 (SUPER_ADMIN은 자동 통과)
    @PreAuthorize("@access.hasPermission('user:read')")
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable String id) { ... }

    // 여러 권한 중 하나라도 있으면 통과
    @PreAuthorize("@access.hasAnyPermission('user:write', 'user:delete')")
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable String id) { ... }

    // 모든 권한이 있어야 통과
    @PreAuthorize("@access.hasAllPermissions('user:read', 'user:write')")
    @PostMapping
    public UserResponse createUser() { ... }

    // 본인이거나 권한이 있으면 통과
    @PreAuthorize("@access.myselfOr(#userId, 'user:read')")
    @GetMapping("/{userId}/profile")
    public ProfileResponse getProfile(@PathVariable String userId) { ... }

    // 테넌트 격리 (SUPER_ADMIN은 모든 테넌트 접근 가능)
    @PreAuthorize("@access.sameTenant(#tenantId)")
    @GetMapping("/tenants/{tenantId}/users")
    public List<UserResponse> getTenantUsers(@PathVariable String tenantId) { ... }

    // 조직 격리 (SUPER_ADMIN, TENANT_ADMIN은 자기 테넌트 내 모든 조직 접근 가능)
    @PreAuthorize("@access.sameOrganization(#orgId)")
    @GetMapping("/organizations/{orgId}/users")
    public List<UserResponse> getOrgUsers(@PathVariable String orgId) { ... }

    // 역할 확인
    @PreAuthorize("@access.superAdmin()")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) { ... }

    // 복합 조건
    @PreAuthorize("@access.superAdmin() or @access.hasPermission('user:delete')")
    @DeleteMapping("/{id}/soft")
    public void softDeleteUser(@PathVariable String id) { ... }

    // 서비스 계정 확인
    @PreAuthorize("@access.serviceAccount()")
    @PostMapping("/internal/sync")
    public void syncUsers() { ... }
}
```

#### API 목록

| 메서드 | 설명 |
|--------|------|
| `authenticated()` | 인증된 사용자인지 확인 |
| `superAdmin()` | SUPER_ADMIN 역할 확인 |
| `admin()` | 관리자 역할 확인 (SUPER_ADMIN, TENANT_ADMIN, ORG_ADMIN) |
| `hasRole(role)` | 특정 역할 보유 확인 |
| `hasAnyRole(roles...)` | 역할 중 하나라도 보유 확인 |
| `hasPermission(permission)` | 특정 권한 보유 확인 (SUPER_ADMIN 자동 통과) |
| `hasAnyPermission(permissions...)` | 권한 중 하나라도 보유 확인 |
| `hasAllPermissions(permissions...)` | 모든 권한 보유 확인 |
| `sameTenant(tenantId)` | 같은 테넌트 소속 확인 (SUPER_ADMIN 자동 통과) |
| `sameOrganization(orgId)` | 같은 조직 소속 확인 (SUPER_ADMIN, TENANT_ADMIN 자동 통과) |
| `myself(userId)` | 본인 확인 |
| `myselfOr(userId, permission)` | 본인이거나 권한 보유 확인 |
| `serviceAccount()` | 서비스 계정 여부 확인 |

#### 프로젝트별 SecurityContext 커스터마이징

프로젝트에서 자체 UserContext를 사용하는 경우 `getSecurityContext()`를 오버라이드하세요:

```java
@Component("access")
public class ResourceAccessChecker extends BaseAccessChecker {

    @Override
    protected SecurityContext getSecurityContext() {
        return MyProjectUserContextHolder.getContext();
    }
}
```

---

## 에러 처리

### SDK 예외 계층 (Core)

SDK API 호출 시 발생하는 HTTP 에러 예외입니다:

```
AuthHubException (base)
├── AuthHubBadRequestException     (400)
├── AuthHubUnauthorizedException   (401)
├── AuthHubForbiddenException      (403)
├── AuthHubNotFoundException       (404)
└── AuthHubServerException         (5xx)
```

### 예외 처리 예시

```java
try {
    ApiResponse<LoginResponse> response = client.auth().login(request);
} catch (AuthHubBadRequestException e) {
    // 400: 잘못된 요청 (유효성 검증 실패 등)
    log.error("Bad request: {} - {}", e.getErrorCode(), e.getMessage());
} catch (AuthHubUnauthorizedException e) {
    // 401: 인증 실패 (토큰 만료, 잘못된 토큰 등)
    log.error("Unauthorized: {}", e.getMessage());
} catch (AuthHubNotFoundException e) {
    // 404: 리소스 없음
    log.warn("Not found: {}", e.getMessage());
} catch (AuthHubServerException e) {
    // 5xx: 서버 오류
    log.error("Server error ({}): {}", e.getStatusCode(), e.getMessage());
}
```

### 예외 정보

```java
catch (AuthHubException e) {
    int statusCode = e.getStatusCode();    // HTTP 상태 코드
    String errorCode = e.getErrorCode();   // 에러 코드 (예: "USER_NOT_FOUND")
    String message = e.getMessage();       // 에러 메시지
}
```

### 보안 예외 계층 (Spring Boot Starter)

인증/인가 필터에서 발생하는 보안 예외입니다. SDK 예외와는 별도의 계층입니다:

```
SecurityException (abstract base)
├── AuthenticationException   (401) - 인증 실패
└── AuthorizationException    (403) - 인가 실패
```

#### SecurityErrorCode

표준화된 에러 코드 enum입니다:

| 코드 | 이름 | HTTP | 설명 |
|------|------|------|------|
| AUTH_001 | `UNAUTHENTICATED` | 401 | 인증되지 않은 요청 |
| AUTH_002 | `INVALID_CREDENTIALS` | 401 | 잘못된 인증 정보 |
| AUTH_003 | `ACCOUNT_DISABLED` | 401 | 계정 비활성화 |
| AUTH_004 | `ACCOUNT_LOCKED` | 401 | 계정 잠김 |
| AUTH_005 | `SESSION_EXPIRED` | 401 | 세션 만료 |
| AUTHZ_001 | `ACCESS_DENIED` | 403 | 접근 권한 없음 |
| AUTHZ_002 | `INSUFFICIENT_PERMISSION` | 403 | 권한 부족 |
| AUTHZ_003 | `RESOURCE_FORBIDDEN` | 403 | 리소스 접근 불가 |
| AUTHZ_004 | `TENANT_ACCESS_DENIED` | 403 | 테넌트 접근 불가 |
| AUTHZ_005 | `ORGANIZATION_ACCESS_DENIED` | 403 | 조직 접근 불가 |
| TOKEN_001 | `TOKEN_MISSING` | 401 | 토큰 없음 |
| TOKEN_002 | `TOKEN_MALFORMED` | 401 | 잘못된 토큰 형식 |
| TOKEN_003 | `TOKEN_EXPIRED` | 401 | 만료된 토큰 |
| TOKEN_004 | `TOKEN_INVALID` | 401 | 유효하지 않은 토큰 |
| TOKEN_005 | `SERVICE_TOKEN_INVALID` | 401 | 서비스 토큰 오류 |

#### 보안 예외 사용 예시

```java
// 인증 예외 - 팩토리 메서드 제공
throw AuthenticationException.unauthenticated();
throw AuthenticationException.tokenMissing();
throw AuthenticationException.tokenExpired();
throw AuthenticationException.tokenInvalid();
throw AuthenticationException.invalidCredentials();

// 에러 코드 직접 지정
throw new AuthenticationException(SecurityErrorCode.ACCOUNT_LOCKED);
throw new AuthenticationException(SecurityErrorCode.TOKEN_EXPIRED, "Token expired at: " + expiry);

// 인가 예외 - 팩토리 메서드 제공
throw AuthorizationException.accessDenied();
throw AuthorizationException.insufficientPermission("user:delete");
throw AuthorizationException.resourceForbidden("resource-123");
throw AuthorizationException.tenantAccessDenied("tenant-456");
throw AuthorizationException.organizationAccessDenied("org-789");
```

#### 보안 예외 처리 예시

```java
try {
    // 보안 검사 수행
} catch (AuthenticationException e) {
    // 401: 인증 실패
    log.error("[{}] {}", e.getCode(), e.getMessage());
    // e.getCode() → "AUTH_001", "TOKEN_003" 등
    // e.getHttpStatus() → 401
} catch (AuthorizationException e) {
    // 403: 인가 실패
    log.error("[{}] {} - Required: {}", e.getCode(), e.getMessage(), e.getRequiredPermission());
    // e.getRequiredPermission() → "user:delete" (설정된 경우)
    // e.getTargetResource() → "resource-123" (설정된 경우)
} catch (SecurityException e) {
    // 공통 처리
    boolean isAuthError = e.isAuthenticationError();  // 401 여부
    boolean isAuthzError = e.isAuthorizationError();  // 403 여부
}
```

---

## 모듈 구조

```
sdk/
├── authhub-sdk-core/                        # 순수 Java SDK
│   └── src/main/java/com/ryuqq/authhub/sdk/
│       ├── api/                              # API 인터페이스
│       │   ├── AuthApi.java                  #   로그인, 로그아웃, 토큰 갱신, 내 정보
│       │   ├── OnboardingApi.java            #   테넌트 온보딩
│       │   ├── UserApi.java                  #   사용자 생성 + 역할 할당
│       │   └── InternalApi.java              #   Gateway용 Internal API
│       ├── auth/                             # 토큰 리졸버
│       │   ├── TokenResolver.java            #   토큰 제공 인터페이스
│       │   ├── ThreadLocalTokenResolver.java #   요청별 사용자 토큰
│       │   ├── StaticTokenResolver.java      #   고정 서비스 토큰
│       │   └── ChainTokenResolver.java       #   순차적 토큰 시도
│       ├── client/                           # 클라이언트 구현
│       │   ├── AuthHubClient.java            #   메인 클라이언트 인터페이스
│       │   ├── AuthHubClientBuilder.java     #   빌더
│       │   ├── GatewayClient.java            #   Gateway 전용 클라이언트
│       │   ├── GatewayClientBuilder.java     #   빌더
│       │   └── internal/                     #   구현체 (패키지 프라이빗)
│       │       ├── HttpClientSupport.java    #     Bearer Token HTTP 클라이언트
│       │       └── ServiceTokenHttpClientSupport.java  # Service Token HTTP 클라이언트
│       ├── config/                           # 설정
│       │   ├── AuthHubConfig.java            #   AuthHubClient 설정
│       │   └── GatewayClientConfig.java      #   GatewayClient 설정
│       ├── exception/                        # 예외 계층
│       │   ├── AuthHubException.java
│       │   ├── AuthHubBadRequestException.java
│       │   ├── AuthHubUnauthorizedException.java
│       │   ├── AuthHubForbiddenException.java
│       │   ├── AuthHubNotFoundException.java
│       │   └── AuthHubServerException.java
│       └── model/                            # DTO 모델
│           ├── common/                       #   ApiResponse 등
│           ├── auth/                         #   LoginRequest, TokenResponse 등
│           ├── user/                         #   CreateUserWithRolesRequest/Response
│           ├── onboarding/                   #   TenantOnboardingRequest/Response
│           └── internal/                     #   EndpointPermissionSpec, TenantConfig 등
│
└── authhub-sdk-spring-boot-starter/         # Spring Boot 통합
    └── src/main/java/com/ryuqq/authhub/sdk/
        ├── autoconfigure/                    # 자동 설정
        │   ├── AuthHubAutoConfiguration.java #   빈 자동 등록
        │   ├── AuthHubProperties.java        #   application.yml 바인딩
        │   └── AuthHubTokenContextFilter.java#   Bearer 토큰 → ThreadLocal
        ├── access/                           # 접근 권한 검사
        │   ├── AccessChecker.java            #   권한 검사 인터페이스 (@PreAuthorize SpEL)
        │   └── BaseAccessChecker.java        #   기본 구현 (상속하여 확장)
        ├── annotation/                       # 어노테이션
        │   └── RequirePermission.java        #   엔드포인트 권한 선언
        ├── context/                          # 사용자 컨텍스트
        │   ├── SecurityContext.java          #   권한 검사 계약
        │   ├── UserContext.java              #   사용자 정보 (Builder 패턴)
        │   └── UserContextHolder.java        #   ThreadLocal 기반 관리
        ├── constant/                         # 상수
        │   ├── Permissions.java              #   권한 키 상수 + 유틸리티
        │   ├── Roles.java                    #   역할 상수
        │   └── Scopes.java                   #   스코프 상수
        ├── exception/                        # 보안 예외 계층
        │   ├── SecurityException.java        #   보안 예외 기본 클래스 (abstract)
        │   ├── AuthenticationException.java  #   인증 실패 (401)
        │   ├── AuthorizationException.java   #   인가 실패 (403)
        │   └── SecurityErrorCode.java        #   표준 에러 코드 enum
        ├── filter/                           # 인증 필터
        │   ├── GatewayAuthenticationFilter.java
        │   └── ServiceTokenAuthenticationFilter.java
        ├── header/                           # 헤더 처리
        │   ├── GatewayHeaderParser.java
        │   └── SecurityHeaders.java          #   헤더 키 상수
        ├── sync/                             # 엔드포인트 동기화
        │   ├── EndpointInfo.java             #   스캔된 엔드포인트 정보
        │   ├── EndpointScanner.java          #   @RequirePermission 스캐너
        │   ├── EndpointSyncClient.java       #   동기화 클라이언트 인터페이스
        │   ├── EndpointSyncRequest.java      #   동기화 요청 DTO
        │   ├── EndpointSyncRunner.java       #   ApplicationRunner 구현
        │   └── EndpointSyncException.java    #   동기화 예외
        └── util/                             # 유틸리티
            ├── PermissionMatcher.java
            └── ScopeValidator.java
```

## 버전 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|----------|
| **v2.0.3** | 2026-02-03 | UserPermissions에 hash/generatedAt 필드 추가 (권한 변경 감지 지원) |
| v2.0.2 | 2026-02-03 | JWKS/Tenant Config/User Permissions API 추가, SDK 테스트 개선, 문서화 보강 |
| v2.0.1 | 2026-02-03 | GatewayClient 추가 (Internal API 지원), Permission Spec API, 성능 최적화 |
| v2.0.0 | 2026-01-20 | Spring Boot Starter 추가, 권한 체크 기능, 엔드포인트 자동 동기화 |
| v1.0.0 | 2025-01-15 | 최초 릴리즈 (AuthHubClient) |

---

## 라이선스

MIT License
