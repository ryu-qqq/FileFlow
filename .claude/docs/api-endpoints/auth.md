# Auth API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 2개 |
| **합계** | **3개** |

**Base Path**: `/api/v1/market/admin/auth`

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/admin/auth/me | AuthQueryController | me | GetMyInfoUseCase |

### Q1. me
- **Path**: `GET /api/v1/market/admin/auth/me`
- **Controller**: `AuthQueryController`
- **Summary**: 내 정보 조회
- **Description**: 현재 로그인한 관리자의 정보를 조회합니다
- **Request**:
  - **Type**: `@RequestHeader`
  - **Header**: `Authorization` (Bearer 토큰)
  - **처리**: `AuthQueryApiMapper.extractToken()` → 토큰 추출
- **Response**: `ApiResponse<MyInfoApiResponse>`
  ```json
  {
    "userId": "user-123",
    "email": "admin@example.com",
    "name": "관리자",
    "tenantId": "tenant-123",
    "tenantName": "테넌트명",
    "organizationId": "org-123",
    "organizationName": "조직명",
    "roles": [
      {
        "id": "role-123",
        "name": "ADMIN"
      }
    ],
    "permissions": ["READ", "WRITE"]
  }
  ```
- **UseCase**: `GetMyInfoUseCase.execute(String accessToken)`
- **HTTP Status**:
  - `200`: 조회 성공
  - `401`: 인증되지 않은 요청

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/admin/auth/login | AuthCommandController | login | LoginUseCase |
| 2 | POST | /api/v1/market/admin/auth/logout | AuthCommandController | logout | LogoutUseCase |

### C1. login
- **Path**: `POST /api/v1/market/admin/auth/login`
- **Controller**: `AuthCommandController`
- **Summary**: 로그인
- **Description**: 사용자 인증 후 액세스 토큰을 발급합니다
- **Request**: `LoginApiRequest` (@RequestBody, @Valid)
  ```json
  {
    "identifier": "admin@example.com",
    "password": "password123!"
  }
  ```
  - **Validation**:
    - `identifier`: @NotBlank, @Size(max=100)
    - `password`: @NotBlank, @Size(max=100)
- **Response**: `ApiResponse<LoginApiResponse>`
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
  ```
- **Flow**:
  1. `AuthCommandApiMapper.toCommand()` → `LoginCommand`
  2. `LoginUseCase.execute(command)` → `LoginResult`
  3. `AuthCommandApiMapper.toResponse(result)` → `LoginApiResponse`
- **UseCase**: `LoginUseCase.execute(LoginCommand)`
- **HTTP Status**:
  - `200`: 로그인 성공
  - `400`: 잘못된 요청
  - `401`: 인증 실패

### C2. logout
- **Path**: `POST /api/v1/market/admin/auth/logout`
- **Controller**: `AuthCommandController`
- **Summary**: 로그아웃
- **Description**: 현재 세션의 토큰을 무효화합니다
- **Request**: 없음
  - **인증 정보**: `SecurityContextHolder.getContext().getAuthentication().getName()` → userId 추출
- **Response**: `ApiResponse<Void>` (빈 응답)
- **Flow**:
  1. `SecurityContext`에서 userId 추출
  2. `AuthCommandApiMapper.toCommand(userId)` → `LogoutCommand`
  3. `LogoutUseCase.execute(command)`
- **UseCase**: `LogoutUseCase.execute(LogoutCommand)`
- **HTTP Status**:
  - `200`: 로그아웃 성공
  - `401`: 인증되지 않은 요청

---

## 아키텍처 패턴

### CQRS 분리
- **Query Controller**: `AuthQueryController` - 조회 전용
- **Command Controller**: `AuthCommandController` - 명령 전용

### Hexagonal Architecture
```
Controller (Adapter-In)
    ↓ Mapper
Command/Query (DTO)
    ↓
UseCase (Port-In)
    ↓
Service (Application)
```

### 의존성 구조
```
AuthCommandController
├─ LoginUseCase (Port-In)
├─ LogoutUseCase (Port-In)
└─ AuthCommandApiMapper

AuthQueryController
├─ GetMyInfoUseCase (Port-In)
└─ AuthQueryApiMapper
```

---

## 컨벤션 준수

### API-CTR-001: @RestController
- ✅ 모든 Controller는 `@RestController` 선언

### API-CTR-003: UseCase 의존
- ✅ Port-In 인터페이스만 의존 (구현체 의존 금지)

### API-CTR-004: ResponseEntity 래핑
- ✅ `ResponseEntity<ApiResponse<T>>` 형식 준수

### API-CTR-005: @Transactional 금지
- ✅ Controller에 트랜잭션 어노테이션 없음

### API-CTR-007: 비즈니스 로직 금지
- ✅ 단순 위임 패턴만 사용 (Mapper → UseCase → Mapper)

### API-CTR-009: @Valid 필수
- ✅ `@Valid @RequestBody LoginApiRequest`

### API-CTR-010: CQRS 분리
- ✅ Query/Command Controller 명확히 분리

---

## Swagger 문서화

### Tags
- **인증**: 로그인/로그아웃 API (Command)
- **인증**: 관리자 인증 정보 조회 API (Query)

### 각 엔드포인트
- `@Operation`: summary, description 제공
- `@ApiResponses`: 모든 HTTP 상태 코드 문서화
- `@Schema`: DTO 필드 상세 설명 포함

---

## 특이사항

### 1. Authorization 헤더 처리
- **Query (me)**: `@RequestHeader("Authorization")` → Mapper에서 토큰 추출
- **Command (logout)**: `SecurityContextHolder` → Spring Security Context 사용

### 2. 토큰 추출 로직
- `AuthQueryApiMapper.extractToken(authorization)`
  - "Bearer " 접두사 제거
  - 실제 JWT 토큰만 UseCase로 전달

### 3. 보안
- Spring Security 통합 (SecurityContextHolder 사용)
- Bearer 토큰 인증 방식
- 토큰 무효화 메커니즘 (로그아웃)

### 4. Response 구조
- 성공: `ApiResponse.of(data)` → `{ "success": true, "data": {...} }`
- 로그아웃: `ApiResponse.of()` → `{ "success": true }`

---

## 관련 파일

### Controller
- `AuthCommandController.java`
- `AuthQueryController.java`
- `AuthAdminEndpoints.java` (Path 상수)

### DTO
- `LoginApiRequest.java`
- `LoginApiResponse.java`
- `MyInfoApiResponse.java`

### Mapper
- `AuthCommandApiMapper.java`
- `AuthQueryApiMapper.java`

### UseCase (Port-In)
- `LoginUseCase.java`
- `LogoutUseCase.java`
- `GetMyInfoUseCase.java`

### Error Handling
- `AuthErrorMapper.java`

---

## 테스트 고려사항

### Controller 테스트
- `@WebMvcTest` + `@MockitoBean`
- ErrorMapperRegistry Mock 필요

### 테스트 케이스
1. **로그인 성공**: 유효한 자격증명 → 200 + 토큰 발급
2. **로그인 실패**: 잘못된 자격증명 → 401
3. **Validation 실패**: 빈 값 → 400
4. **내 정보 조회 성공**: 유효한 토큰 → 200 + 사용자 정보
5. **내 정보 조회 실패**: 잘못된 토큰 → 401
6. **로그아웃 성공**: 인증된 요청 → 200
7. **로그아웃 실패**: 인증되지 않은 요청 → 401
