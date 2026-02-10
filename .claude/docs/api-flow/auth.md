# Auth API Flow Analysis

## 개요

인증(Auth) 도메인의 API 호출 흐름을 Hexagonal 아키텍처 레이어별로 추적한 문서입니다.

**분석 대상**: Auth API (로그인, 로그아웃, 내 정보 조회)
**분석 일자**: 2026-02-06
**외부 의존성**: AuthHub SDK (인증 서비스)

---

## 엔드포인트 목록

| HTTP Method | Path | Controller | UseCase |
|-------------|------|------------|---------|
| POST | `/api/v1/market/admin/auth/login` | AuthCommandController.login | LoginUseCase |
| POST | `/api/v1/market/admin/auth/logout` | AuthCommandController.logout | LogoutUseCase |
| GET | `/api/v1/market/admin/auth/me` | AuthQueryController.me | GetMyInfoUseCase |

---

## 1. POST /api/v1/market/admin/auth/login - 로그인

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | `/api/v1/market/admin/auth/login` |
| Controller | AuthCommandController |
| Method | login |
| UseCase | LoginUseCase |
| Service | LoginService |

### 호출 흐름 다이어그램

```
AuthCommandController.login(LoginApiRequest)
  ├─ AuthCommandApiMapper.toCommand(request)
  │   └─→ LoginCommand
  ├─ LoginUseCase.execute(command)              [Port-In Interface]
  │   └─ LoginService.execute(command)          [Service Implementation]
  │       └─ AuthManager.login(identifier, password)
  │           └─ AuthClient.login(identifier, password)  [Port-Out Interface]
  │               └─ AuthHubAuthClientAdapter.login()    [Adapter Implementation]
  │                   ├─ AuthHubAuthMapper.toLoginRequest(identifier, password)
  │                   │   └─→ LoginRequest (SDK)
  │                   ├─ AuthApi.login(loginRequest)     [AuthHub SDK]
  │                   │   └─→ ApiResponse<LoginResponse>
  │                   └─ AuthHubAuthMapper.toLoginResult(response.data())
  │                       └─→ LoginResult
  └─ AuthCommandApiMapper.toResponse(result)
      └─→ ApiResponse<LoginApiResponse>
```

### Layer별 상세

#### Adapter-In Layer

**Controller**: `AuthCommandController`
- 위치: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/auth/controller/AuthCommandController.java`
- 메서드: `login(@Valid @RequestBody LoginApiRequest request)`
- 역할: HTTP 요청 수신, UseCase 호출, HTTP 응답 반환

**Request DTO**: `LoginApiRequest`
```java
public record LoginApiRequest(
    @NotBlank @Size(max = 100) String identifier,  // 이메일 또는 사용자 ID
    @NotBlank @Size(max = 100) String password
)
```

**Response DTO**: `LoginApiResponse`
```java
public record LoginApiResponse(
    String accessToken,
    String refreshToken,
    String tokenType,      // "Bearer"
    long expiresIn        // 만료 시간(초)
)
```

**ApiMapper**: `AuthCommandApiMapper`
- `toCommand(LoginApiRequest)` → `LoginCommand`
- `toResponse(LoginResult)` → `LoginApiResponse`
- 로그인 실패 시 `IllegalArgumentException` 발생

#### Application Layer

**UseCase (Port-In)**: `LoginUseCase`
```java
LoginResult execute(LoginCommand command);
```

**Service**: `LoginService`
- 구현체: `LoginUseCase` 구현
- 역할: `AuthManager`에 위임
- 트랜잭션: 없음 (외부 API 호출)

**Command DTO**: `LoginCommand`
```java
public record LoginCommand(String identifier, String password) {
    // Compact Constructor: 필드 검증 (null/blank 체크)
}
```

**Result DTO**: `LoginResult`
```java
public record LoginResult(
    boolean success,
    String userId,
    String accessToken,
    String refreshToken,
    Long expiresIn,
    String tokenType,
    String errorCode,
    String errorMessage
) {
    static LoginResult success(...);
    static LoginResult failure(String errorCode, String errorMessage);
}
```

**Manager**: `AuthManager`
- 역할: Service와 Port-Out 사이의 중간 계층
- 기능: `AuthClient`를 통해 인증 작업 수행
- 재사용성과 확장성 제공

#### Domain Layer

**Port-Out**: `AuthClient` (인터페이스)
```java
LoginResult login(String identifier, String password);
```

**특징**:
- Domain Layer에는 별도 Aggregate 없음 (외부 인증 서비스 의존)
- 비즈니스 규칙은 AuthHub에서 처리

#### Adapter-Out Layer

**Adapter**: `AuthHubAuthClientAdapter`
- 위치: `adapter-out/client/authhub-client/src/main/java/com/ryuqq/marketplace/adapter/out/client/authhub/adapter/AuthHubAuthClientAdapter.java`
- 구현: `AuthClient` 인터페이스
- 외부 의존성: AuthHub SDK (`AuthApi`)

**Mapper**: `AuthHubAuthMapper`
- `toLoginRequest(identifier, password)` → `LoginRequest` (SDK)
- `toLoginResult(LoginResponse)` → `LoginResult` (Application)
- `toLoginFailure(errorCode, errorMessage)` → `LoginResult`

**외부 SDK**: AuthHub SDK
- API: `AuthApi.login(LoginRequest)`
- 응답: `ApiResponse<LoginResponse>`
- 예외:
  - `AuthHubUnauthorizedException` → "UNAUTHORIZED"
  - `AuthHubException` → "AUTH_ERROR"

### Database 접근

**없음**: 외부 AuthHub API를 통해 인증 처리

---

## 2. POST /api/v1/market/admin/auth/logout - 로그아웃

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | `/api/v1/market/admin/auth/logout` |
| Controller | AuthCommandController |
| Method | logout |
| UseCase | LogoutUseCase |
| Service | LogoutService |

### 호출 흐름 다이어그램

```
AuthCommandController.logout()
  ├─ SecurityContextHolder.getContext().getAuthentication().getName()
  │   └─→ userId (String)
  ├─ AuthCommandApiMapper.toCommand(userId)
  │   └─→ LogoutCommand
  ├─ LogoutUseCase.execute(command)              [Port-In Interface]
  │   └─ LogoutService.execute(command)          [Service Implementation]
  │       └─ AuthManager.logout(userId)
  │           └─ AuthClient.logout(userId)        [Port-Out Interface]
  │               └─ AuthHubAuthClientAdapter.logout()  [Adapter Implementation]
  │                   ├─ AuthHubAuthMapper.toLogoutRequest(userId)
  │                   │   └─→ LogoutRequest (SDK)
  │                   └─ AuthApi.logout(logoutRequest)  [AuthHub SDK]
  └─→ ApiResponse<Void>
```

### Layer별 상세

#### Adapter-In Layer

**Controller**: `AuthCommandController`
- 메서드: `logout()`
- 입력: SecurityContext에서 userId 추출
- 출력: `ApiResponse<Void>` (빈 응답)

**ApiMapper**: `AuthCommandApiMapper`
- `toCommand(String userId)` → `LogoutCommand`

#### Application Layer

**UseCase (Port-In)**: `LogoutUseCase`
```java
void execute(LogoutCommand command);
```

**Service**: `LogoutService`
- 구현체: `LogoutUseCase` 구현
- 역할: `AuthManager`에 위임

**Command DTO**: `LogoutCommand`
```java
public record LogoutCommand(String userId)
```

**Manager**: `AuthManager`
- `logout(String userId)` → `AuthClient.logout(userId)`

#### Domain Layer

**Port-Out**: `AuthClient`
```java
void logout(String userId);
```

#### Adapter-Out Layer

**Adapter**: `AuthHubAuthClientAdapter`
- `logout(String userId)` 구현
- 예외 처리: `AuthHubException` → `IllegalStateException`

**Mapper**: `AuthHubAuthMapper`
- `toLogoutRequest(userId)` → `LogoutRequest` (SDK)

**외부 SDK**: AuthHub SDK
- API: `AuthApi.logout(LogoutRequest)`
- 예외: `AuthHubException`

### Database 접근

**없음**: 외부 AuthHub API를 통해 로그아웃 처리

---

## 3. GET /api/v1/market/admin/auth/me - 내 정보 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | `/api/v1/market/admin/auth/me` |
| Controller | AuthQueryController |
| Method | me |
| UseCase | GetMyInfoUseCase |
| Service | GetMyInfoService |

### 호출 흐름 다이어그램

```
AuthQueryController.me(@RequestHeader("Authorization") authorization)
  ├─ AuthQueryApiMapper.extractToken(authorization)
  │   └─→ accessToken (String)
  ├─ GetMyInfoUseCase.execute(accessToken)       [Port-In Interface]
  │   └─ GetMyInfoService.execute(accessToken)   [Service Implementation]
  │       └─ AuthManager.getMyInfo(accessToken)
  │           └─ AuthClient.getMyInfo(accessToken)  [Port-Out Interface]
  │               └─ AuthHubAuthClientAdapter.getMyInfo()  [Adapter Implementation]
  │                   ├─ AuthApi.getMe()          [AuthHub SDK]
  │                   │   └─→ ApiResponse<MyContextResponse>
  │                   └─ AuthHubAuthMapper.toMyInfoResult(response.data())
  │                       └─→ MyInfoResult
  └─ AuthQueryApiMapper.toResponse(result)
      └─→ ApiResponse<MyInfoApiResponse>
```

### Layer별 상세

#### Adapter-In Layer

**Controller**: `AuthQueryController`
- 위치: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/auth/controller/AuthQueryController.java`
- 메서드: `me(@RequestHeader("Authorization") String authorization)`
- 역할: Authorization 헤더에서 토큰 추출, 사용자 정보 조회

**ApiMapper**: `AuthQueryApiMapper`
- `extractToken(authorization)`: "Bearer " 접두사 제거
- `toResponse(MyInfoResult)` → `MyInfoApiResponse`

**Response DTO**: `MyInfoApiResponse`
```java
public record MyInfoApiResponse(
    String userId,
    String email,
    String name,
    String tenantId,
    String tenantName,
    String organizationId,
    String organizationName,
    List<RoleApiResponse> roles,
    List<String> permissions
) {
    public record RoleApiResponse(String id, String name) {}
}
```

#### Application Layer

**UseCase (Port-In)**: `GetMyInfoUseCase`
```java
MyInfoResult execute(String accessToken);
```

**Service**: `GetMyInfoService`
- 구현체: `GetMyInfoUseCase` 구현
- 역할: `AuthManager`에 위임

**Result DTO**: `MyInfoResult`
```java
public record MyInfoResult(
    String userId,
    String email,
    String name,
    String tenantId,
    String tenantName,
    String organizationId,
    String organizationName,
    List<RoleInfo> roles,
    List<String> permissions
) {
    public record RoleInfo(String id, String name) {}
}
```

**Manager**: `AuthManager`
- `getMyInfo(String accessToken)` → `AuthClient.getMyInfo(accessToken)`

#### Domain Layer

**Port-Out**: `AuthClient`
```java
MyInfoResult getMyInfo(String accessToken);
```

#### Adapter-Out Layer

**Adapter**: `AuthHubAuthClientAdapter`
- `getMyInfo(String accessToken)` 구현
- **주의**: SDK의 `getMe()`는 파라미터 없이 호출 (내부적으로 토큰 관리)
- 예외 처리:
  - `AuthHubUnauthorizedException` → `IllegalStateException`
  - `AuthHubException` → `IllegalStateException`

**Mapper**: `AuthHubAuthMapper`
- `toMyInfoResult(MyContextResponse)` → `MyInfoResult`
- Roles 리스트 변환: SDK → Application DTO
- Permissions 리스트 변환

**외부 SDK**: AuthHub SDK
- API: `AuthApi.getMe()`
- 응답: `ApiResponse<MyContextResponse>`
- 응답 구조:
  - userId, email, name
  - tenant: { id, name }
  - organization: { id, name }
  - roles: [{ id, name }]
  - permissions: [String]

### Database 접근

**없음**: 외부 AuthHub API를 통해 사용자 정보 조회

---

## 아키텍처 특징

### 1. Hexagonal 패턴 준수

```
┌─────────────────────────────────────────────────────────────┐
│                    Adapter-In (REST API)                    │
│  Controller → ApiMapper → Request/Response DTO              │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application (Service)                     │
│  UseCase (Port-In) → Service → Manager → Command/Result    │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain (Port-Out)                        │
│  AuthClient (Interface)                                     │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Adapter-Out (External API)                  │
│  AuthHubAuthClientAdapter → Mapper → AuthHub SDK            │
└─────────────────────────────────────────────────────────────┘
```

### 2. CQRS 적용

- **Command**: `AuthCommandController` (로그인, 로그아웃)
- **Query**: `AuthQueryController` (내 정보 조회)

### 3. 레이어별 DTO 변환

| Layer | DTO Type | 예시 |
|-------|----------|------|
| Adapter-In | ApiRequest/ApiResponse | `LoginApiRequest`, `LoginApiResponse` |
| Application | Command/Result | `LoginCommand`, `LoginResult` |
| Adapter-Out | SDK Request/Response | `LoginRequest` (SDK), `LoginResponse` (SDK) |

### 4. 외부 의존성 관리

- **Port-Out 인터페이스**: `AuthClient`
- **Adapter 구현체**: `AuthHubAuthClientAdapter`
- **SDK 의존성**: AuthHub SDK (`AuthApi`, `OnboardingApi`)
- **Conditional Bean**: `@ConditionalOnBean(AuthApi.class)`

### 5. 예외 처리 전략

**AuthHub SDK 예외 → Application 예외 변환**:
- `AuthHubUnauthorizedException` → `LoginResult.failure("UNAUTHORIZED", ...)`
- `AuthHubException` → `LoginResult.failure("AUTH_ERROR", ...)`
- `AuthHubException` (로그아웃/조회) → `IllegalStateException`

### 6. 보안 처리

- **로그인**: identifier + password 검증
- **로그아웃**: SecurityContext에서 userId 추출
- **내 정보 조회**: Authorization 헤더에서 Bearer 토큰 추출

---

## 주요 설계 결정

### 1. AuthManager 계층

**목적**: Service와 Port-Out 사이의 중간 계층
**이점**:
- 인증 로직 재사용성 향상
- 여러 Service에서 공통 인증 기능 사용 가능
- 확장 가능한 구조 (캐싱, 로깅 등 추가 가능)

### 2. 외부 SDK 의존성 역전

**원칙**: Application Layer는 외부 SDK를 직접 알지 못함
**구현**:
- `AuthClient` 인터페이스 (Port-Out)
- `AuthHubAuthClientAdapter` 구현체 (Adapter-Out)
- Mapper를 통한 DTO 변환

### 3. Result 패턴 사용

**LoginResult**:
- 성공/실패를 하나의 객체로 표현
- 예외 발생 대신 실패 정보 포함
- Adapter-In에서 예외로 변환 (필요 시)

### 4. Conditional Bean

**@ConditionalOnBean**:
- `AuthApi.class` 빈이 있을 때만 활성화
- 테스트 환경에서 Mock 사용 가능
- 외부 서비스 의존성 격리

---

## 개선 고려 사항

### 1. 토큰 관리

**현재**:
```java
// SDK의 getMe()는 파라미터 없이 호출
ApiResponse<MyContextResponse> response = authApi.getMe();
```

**개선 방향**:
- SDK가 accessToken을 어떻게 관리하는지 확인 필요
- ThreadLocal 또는 SecurityContext 활용 여부 검토
- 명시적 토큰 전달 방식 고려

### 2. 캐싱 전략

**현재**: 매 요청마다 AuthHub API 호출

**개선 방향**:
- `MyInfoResult` 캐싱 (Redis)
- TTL 설정 (액세스 토큰 만료 시간과 동기화)
- 캐시 키: `userId` 또는 `accessToken` 해시

### 3. 에러 처리 일관성

**현재**:
- 로그인: `LoginResult.failure()` 반환
- 로그아웃/조회: `IllegalStateException` 발생

**개선 방향**:
- 모든 작업에 Result 패턴 적용
- 또는 Custom Exception 계층 구조 설계

### 4. 로그아웃 Flow

**현재**:
```java
String userId = SecurityContextHolder.getContext().getAuthentication().getName();
```

**개선 방향**:
- SecurityContext 직접 접근 대신 `@AuthenticationPrincipal` 사용
- Custom Principal 객체로 사용자 정보 관리

---

## 관련 문서

- AuthHub SDK: [문서 링크 필요]
- 인증/인가 아키텍처: [문서 링크 필요]
- API 명세: Swagger UI (`/swagger-ui/index.html`)

---

## 변경 이력

| 날짜 | 작성자 | 변경 내용 |
|------|--------|-----------|
| 2026-02-06 | Claude | 초기 작성 |
