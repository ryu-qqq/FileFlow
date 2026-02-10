# Auth 도메인 E2E 통합 테스트 시나리오

## 개요

인증(Auth) 도메인의 E2E 통합 테스트 시나리오 설계 문서입니다.

**분석 대상**: Auth API (로그인, 토큰 갱신, 로그아웃, 내 정보 조회)
**분석 일자**: 2026-02-06 (최종 업데이트: 2026-02-06)
**참고 문서**:
- `.claude/docs/api-endpoints/auth.md`
- `.claude/docs/api-flow/auth.md`

**외부 의존성**:
- AuthHub SDK (인증 서비스)
- Spring Security + @PreAuthorize

---

## 엔드포인트 개요

| HTTP Method | Path | 용도 | @PreAuthorize | 인증 필요 여부 |
|-------------|------|------|---------------|---------------|
| POST | `/api/v1/market/admin/auth/login` | 로그인 | 없음 | ❌ 비인증 허용 |
| POST | `/api/v1/market/admin/auth/refresh` | 토큰 갱신 | 없음 | ❌ 비인증 허용 |
| POST | `/api/v1/market/admin/auth/logout` | 로그아웃 | `@access.authenticated()` | ✅ 인증 필수 |
| GET | `/api/v1/market/admin/auth/me` | 내 정보 조회 | `@access.authenticated()` | ✅ 인증 필수 |

**총 시나리오 개수**: 22개 (P0: 18개, P1: 4개)

**인증/인가 정책**:
- `@access.authenticated()`: 인증된 사용자만 접근 가능 (토큰 유효성 검증)
- 권한 체크 없음: authenticated만 통과하면 모든 인증된 사용자 접근 가능

---

## 1. POST /api/v1/market/admin/auth/login - 로그인

**인증 필요 여부**: ❌ 비인증 허용 (@PreAuthorize 없음)
**인증 컨텍스트**: 익명 사용자 (Anonymous)

### 1.1 성공 시나리오 (P0)

#### TC-AUTH-001: 유효한 자격증명으로 로그인 성공
**우선순위**: P0
**카테고리**: 기본 로그인
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- AuthHub에 사용자가 등록되어 있음
- identifier: `admin@example.com`
- password: `password123!`

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "admin@example.com",
  "password": "password123!"
}
```

**예상 결과**:
- HTTP Status: `200 OK`
- Response Body:
  ```json
  {
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
      "tokenType": "Bearer",
      "expiresIn": 3600
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ `data.accessToken`이 존재하고 비어있지 않음
- ✅ `data.refreshToken`이 존재하고 비어있지 않음
- ✅ `data.tokenType`이 "Bearer"임
- ✅ `data.expiresIn`이 3600 (1시간)임
- ✅ 발급된 accessToken으로 `/me` API 호출 가능

---

### 1.2 실패 시나리오 (P0)

#### TC-AUTH-002: 잘못된 비밀번호로 로그인 실패
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- identifier는 유효하지만 password가 틀림

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "admin@example.com",
  "password": "wrongPassword123!"
}
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`
- Response Body (예상):
  ```json
  {
    "error": {
      "code": "UNAUTHORIZED",
      "message": "Invalid credentials"
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 에러 코드가 "UNAUTHORIZED"임
- ✅ accessToken이 발급되지 않음

---

#### TC-AUTH-003: 존재하지 않는 사용자로 로그인 실패
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- AuthHub에 등록되지 않은 identifier

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "nonexistent@example.com",
  "password": "password123!"
}
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 에러 메시지가 명확함 (보안상 "존재하지 않는 사용자"라고 노출하지 않음)

---

#### TC-AUTH-004: 필수 필드 누락 - identifier
**우선순위**: P0
**카테고리**: Validation 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "password": "password123!"
}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`
- Response Body (예상):
  ```json
  {
    "error": {
      "code": "VALIDATION_ERROR",
      "message": "identifier must not be blank"
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 400임
- ✅ Validation 에러 메시지가 포함됨

---

#### TC-AUTH-005: 필수 필드 누락 - password
**우선순위**: P0
**카테고리**: Validation 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "admin@example.com"
}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`

**검증**:
- ✅ HTTP 상태 코드가 400임
- ✅ password 필드 누락 에러 메시지 포함

---

#### TC-AUTH-006: 빈 문자열 필드 - identifier
**우선순위**: P0
**카테고리**: Validation 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "",
  "password": "password123!"
}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`

**검증**:
- ✅ HTTP 상태 코드가 400임
- ✅ `@NotBlank` Validation 위반

---

#### TC-AUTH-007: 빈 문자열 필드 - password
**우선순위**: P0
**카테고리**: Validation 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "admin@example.com",
  "password": ""
}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`

**검증**:
- ✅ HTTP 상태 코드가 400임

---

### 1.3 엣지 케이스 (P1)

#### TC-AUTH-008: 최대 길이 초과 - identifier
**우선순위**: P1
**카테고리**: Validation 경계값
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "a".repeat(101) + "@example.com",
  "password": "password123!"
}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`

**검증**:
- ✅ `@Size(max=100)` Validation 위반

---

#### TC-AUTH-009: 최대 길이 초과 - password
**우선순위**: P1
**카테고리**: Validation 경계값
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "admin@example.com",
  "password": "p".repeat(101)
}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`

**검증**:
- ✅ `@Size(max=100)` Validation 위반

---

## 2. POST /api/v1/market/admin/auth/refresh - 토큰 갱신

**인증 필요 여부**: ❌ 비인증 허용 (@PreAuthorize 없음)
**인증 컨텍스트**: 익명 사용자 (Anonymous, refreshToken만으로 인증)

### 2.1 성공 시나리오 (P0)

#### TC-AUTH-010: 유효한 refreshToken으로 토큰 갱신 성공
**우선순위**: P0
**카테고리**: 토큰 갱신
**인증 컨텍스트**: 익명 사용자 (비인증 상태, refreshToken 제공)

**전제조건**:
- 로그인하여 유효한 refreshToken 보유

**요청**:
```http
POST /api/v1/market/admin/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**예상 결과**:
- HTTP Status: `200 OK`
- Response Body:
  ```json
  {
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
      "tokenType": "Bearer",
      "expiresIn": 3600
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 200임
- ✅ `data.accessToken`이 새롭게 발급됨
- ✅ `data.refreshToken`이 새롭게 발급됨 (또는 기존 것 재사용)
- ✅ 새 accessToken으로 `/me` API 호출 가능

---

### 2.2 실패 시나리오 (P0)

#### TC-AUTH-011: 유효하지 않은 refreshToken
**우선순위**: P0
**카테고리**: 토큰 갱신 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- 만료되었거나 조작된 refreshToken

**요청**:
```http
POST /api/v1/market/admin/auth/refresh
Content-Type: application/json

{
  "refreshToken": "invalid.token.here"
}
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 새로운 accessToken이 발급되지 않음

---

#### TC-AUTH-012: 필수 필드 누락 - refreshToken
**우선순위**: P0
**카테고리**: Validation 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
POST /api/v1/market/admin/auth/refresh
Content-Type: application/json

{}
```

**예상 결과**:
- HTTP Status: `400 Bad Request`

**검증**:
- ✅ HTTP 상태 코드가 400임
- ✅ refreshToken 필드 누락 에러 메시지 포함

---

## 3. GET /api/v1/market/admin/auth/me - 내 정보 조회

**인증 필요 여부**: ✅ 인증 필수 (`@access.authenticated()`)
**인증 컨텍스트**: 인증된 사용자 (Authenticated User)

### 3.1 성공 시나리오 (P0)

#### TC-AUTH-013: 유효한 토큰으로 내 정보 조회 성공
**우선순위**: P0
**카테고리**: 기본 조회
**인증 컨텍스트**: 인증된 사용자 (로그인 후 발급받은 accessToken 사용)

**전제조건**:
- 로그인하여 accessToken 발급받음

**요청**:
```http
GET /api/v1/market/admin/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**예상 결과**:
- HTTP Status: `200 OK`
- Response Body:
  ```json
  {
    "data": {
      "userId": "user-123",
      "email": "admin@example.com",
      "name": "관리자",
      "tenantId": "tenant-123",
      "tenantName": "테넌트명",
      "organizationId": "org-123",
      "organizationName": "조직명",
      "roles": [
        { "id": "role-1", "name": "ADMIN" },
        { "id": "role-2", "name": "MANAGER" }
      ],
      "permissions": ["READ", "WRITE", "DELETE"]
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 200임
- ✅ `data.userId`가 존재함
- ✅ `data.email`이 로그인한 사용자의 이메일임
- ✅ `data.roles`가 배열이며 비어있지 않음
- ✅ `data.permissions`가 배열이며 비어있지 않음

---

### 3.2 실패 시나리오 (P0)

#### TC-AUTH-014: Authorization 헤더 없이 요청 (401 Unauthorized)
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- Authorization 헤더를 제공하지 않음

**요청**:
```http
GET /api/v1/market/admin/auth/me
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`
- Response Body (예상):
  ```json
  {
    "error": {
      "code": "UNAUTHORIZED",
      "message": "Missing Authorization header"
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 에러 메시지가 명확함 ("Missing Authorization header" 등)
- ✅ `@PreAuthorize("@access.authenticated()")` 검증 실패

---

#### TC-AUTH-015: 잘못된 형식의 Authorization 헤더 (401 Unauthorized)
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- Authorization 헤더가 "Bearer" 접두사 없이 제공됨

**요청**:
```http
GET /api/v1/market/admin/auth/me
Authorization: InvalidFormat eyJhbGciOiJIUzI1NiJ9...
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ "Bearer" 접두사 없음 에러

---

#### TC-AUTH-016: 유효하지 않은 토큰 (만료됨) (401 Unauthorized)
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (만료된 토큰 사용)

**전제조건**:
- 만료된 accessToken 사용

**요청**:
```http
GET /api/v1/market/admin/auth/me
Authorization: Bearer <expired_token>
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 토큰 만료 에러 메시지

---

#### TC-AUTH-017: 유효하지 않은 토큰 (조작됨) (401 Unauthorized)
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (조작된 토큰 사용)

**전제조건**:
- JWT 서명이 유효하지 않은 토큰

**요청**:
```http
GET /api/v1/market/admin/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.invalid.signature
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 토큰 검증 실패 에러

---

### 3.3 엣지 케이스 (P1)

#### TC-AUTH-018: Bearer 접두사 없는 토큰
**우선순위**: P1
**카테고리**: Authorization 헤더 처리
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**요청**:
```http
GET /api/v1/market/admin/auth/me
Authorization: eyJhbGciOiJIUzI1NiJ9...
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ `AuthQueryApiMapper.extractToken()`에서 "Bearer " 검증
- ✅ 에러 메시지가 명확함

---

## 4. POST /api/v1/market/admin/auth/logout - 로그아웃

**인증 필요 여부**: ✅ 인증 필수 (`@access.authenticated()`)
**인증 컨텍스트**: 인증된 사용자 (Authenticated User)

### 4.1 성공 시나리오 (P0)

#### TC-AUTH-019: 유효한 세션으로 로그아웃 성공
**우선순위**: P0
**카테고리**: 기본 로그아웃
**인증 컨텍스트**: 인증된 사용자 (SecurityContext에 인증 정보 있음)

**전제조건**:
- 로그인하여 SecurityContext에 인증 정보가 있음
- userId: `user-123`

**요청**:
```http
POST /api/v1/market/admin/auth/logout
```
(SecurityContextHolder에서 userId 추출)

**예상 결과**:
- HTTP Status: `200 OK`
- Response Body:
  ```json
  {
    "data": null,
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 200임
- ✅ `data` 필드가 null이거나 비어있음
- ✅ 로그아웃 후 동일 토큰으로 `/me` API 호출 시 401 반환

---

### 4.2 실패 시나리오 (P0)

#### TC-AUTH-020: 인증되지 않은 세션으로 로그아웃 시도 (401 Unauthorized)
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (비인증 상태)

**전제조건**:
- SecurityContext에 인증 정보가 없음

**요청**:
```http
POST /api/v1/market/admin/auth/logout
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`
- Response Body (예상):
  ```json
  {
    "error": {
      "code": "UNAUTHORIZED",
      "message": "Authentication required"
    },
    "timestamp": "2026-02-06T12:00:00Z",
    "requestId": "..."
  }
  ```

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ SecurityContext가 비어있음을 확인
- ✅ `@PreAuthorize("@access.authenticated()")` 검증 실패

---

#### TC-AUTH-021: 만료된 토큰으로 로그아웃 시도 (401 Unauthorized)
**우선순위**: P0
**카테고리**: 인증 실패
**인증 컨텍스트**: 익명 사용자 (만료된 토큰 사용)

**전제조건**:
- 만료된 accessToken으로 요청

**요청**:
```http
POST /api/v1/market/admin/auth/logout
Authorization: Bearer <expired_token>
```

**예상 결과**:
- HTTP Status: `401 Unauthorized`

**검증**:
- ✅ HTTP 상태 코드가 401임
- ✅ 토큰 만료 에러 메시지
- ✅ `@PreAuthorize("@access.authenticated()")` 검증 실패

---

## 5. 통합 플로우 시나리오

### 5.1 전체 인증 플로우 (P0)

#### TC-AUTH-022: 로그인 → 내 정보 조회 → 로그아웃 전체 플로우
**우선순위**: P0
**카테고리**: End-to-End 플로우
**인증 컨텍스트**: 익명 → 인증됨 → 로그아웃 → 익명

**시나리오**:

**Step 1: 로그인 (익명 사용자)**
```http
POST /api/v1/market/admin/auth/login
Content-Type: application/json

{
  "identifier": "admin@example.com",
  "password": "password123!"
}
```
- ✅ 200 OK 응답
- ✅ accessToken 발급

**Step 2: 내 정보 조회 (인증된 사용자, 발급받은 토큰 사용)**
```http
GET /api/v1/market/admin/auth/me
Authorization: Bearer <accessToken_from_step1>
```
- ✅ 200 OK 응답
- ✅ userId가 로그인한 사용자 ID와 일치
- ✅ `@access.authenticated()` 검증 통과

**Step 3: 로그아웃 (인증된 사용자)**
```http
POST /api/v1/market/admin/auth/logout
```
(SecurityContext에서 userId 추출)
- ✅ 200 OK 응답
- ✅ `@access.authenticated()` 검증 통과

**Step 4: 로그아웃 후 내 정보 조회 시도 (익명 사용자)**
```http
GET /api/v1/market/admin/auth/me
Authorization: Bearer <accessToken_from_step1>
```
- ✅ 401 Unauthorized 응답
- ✅ 토큰이 무효화되었음을 확인
- ✅ `@access.authenticated()` 검증 실패

**검증**:
- ✅ 전체 플로우가 예상대로 동작함
- ✅ 로그아웃 후 토큰이 무효화됨
- ✅ 각 단계에서 올바른 HTTP 상태 코드 반환
- ✅ 인증 상태 전이가 올바름 (익명 → 인증 → 로그아웃 → 익명)

---

## 6. 인증/인가 시나리오 요약

### 6.1 인증 필요 여부 매트릭스

| 엔드포인트 | @PreAuthorize | 인증 필요 | 401 발생 조건 |
|-----------|---------------|----------|--------------|
| POST /login | 없음 | ❌ | 잘못된 자격증명 |
| POST /refresh | 없음 | ❌ | 유효하지 않은 refreshToken |
| POST /logout | `@access.authenticated()` | ✅ | 토큰 없음/만료/조작 |
| GET /me | `@access.authenticated()` | ✅ | 토큰 없음/만료/조작 |

### 6.2 인증 컨텍스트별 시나리오

**익명 사용자 (Anonymous)**:
- ✅ POST /login 가능
- ✅ POST /refresh 가능
- ❌ POST /logout 불가 (401)
- ❌ GET /me 불가 (401)

**인증된 사용자 (Authenticated)**:
- ✅ POST /login 가능 (재로그인)
- ✅ POST /refresh 가능
- ✅ POST /logout 가능
- ✅ GET /me 가능

### 6.3 권한 체크 없음 (Authorization)

현재 Auth 도메인의 엔드포인트는:
- `@access.authenticated()`만 사용 (인증 여부만 확인)
- 특정 권한(permission) 체크 없음
- 인증만 통과하면 모든 인증된 사용자가 접근 가능
- **403 Forbidden 시나리오 없음**

---

## 테스트 환경 및 Fixture 설계

### 필요한 Fixtures

#### REST API Layer
- `AuthApiFixtures.java` (✅ 이미 존재)
  - `loginRequest()`: 기본 로그인 요청
  - `loginRequest(identifier, password)`: 커스텀 로그인 요청
  - `refreshRequest()`: 토큰 갱신 요청
  - `successLoginResult()`: 성공한 로그인 결과
  - `failureLoginResult()`: 실패한 로그인 결과
  - `myInfoResult()`: 내 정보 조회 결과
  - `DEFAULT_ACCESS_TOKEN`: 테스트용 액세스 토큰
  - `EXPIRED_ACCESS_TOKEN`: 만료된 액세스 토큰

#### Application Layer
- `AuthCommandFixtures.java` (✅ 이미 존재)
  - `loginCommand()`: 로그인 커맨드
  - `logoutCommand()`: 로그아웃 커맨드
  - `refreshCommand()`: 토큰 갱신 커맨드

- `AuthResultFixtures.java` (✅ 이미 존재)
  - `successLoginResult()`: 성공 결과
  - `failureLoginResult()`: 실패 결과
  - `myInfoResult()`: 내 정보 결과
  - `refreshResult()`: 토큰 갱신 결과

#### External Client Mock
- `AuthHubFixtures.java` (✅ 이미 존재)
  - AuthHub SDK 응답 모킹용

### Mock 전략

#### E2E 테스트에서의 Mock
- **AuthHub SDK**: 외부 서비스이므로 Mock 필요
  - `AuthApi.login()` → Mock 응답 반환
  - `AuthApi.logout()` → Mock 무응답
  - `AuthApi.getMe()` → Mock 사용자 정보 반환
  - `AuthApi.refresh()` → Mock 토큰 갱신 응답

#### Spring Security Mock
```java
// 인증된 사용자 시뮬레이션 (logout, me 테스트 시)
SecurityContextHolder.getContext()
    .setAuthentication(
        new UsernamePasswordAuthenticationToken(userId, null)
    );

// 익명 사용자 시뮬레이션 (401 테스트 시)
SecurityContextHolder.clearContext();
```

### 테스트 설정

#### 필요한 의존성
- `@SpringBootTest`: 전체 컨텍스트 로드
- `@AutoConfigureMockMvc`: MockMvc 자동 설정
- `@MockitoBean`: AuthHub SDK Mock

#### 테스트 클래스 구조
```java
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화 (수동 제어)
@DisplayName("Auth E2E 통합 테스트")
class AuthE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthApi authApi;  // AuthHub SDK Mock

    @BeforeEach
    void setUp() {
        // AuthHub Mock 설정
        SecurityContextHolder.clearContext();  // 매 테스트마다 초기화
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 익명 사용자 테스트 (login, refresh)
    @Test
    void testLogin_asAnonymous() {
        // SecurityContext 비어있음 (기본 상태)
        // POST /login → 200 OK
    }

    // 인증된 사용자 테스트 (logout, me)
    @Test
    void testLogout_asAuthenticated() {
        // SecurityContext 설정
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken("user-123", null));
        // POST /logout → 200 OK
    }

    // 인증 실패 테스트 (401)
    @Test
    void testMe_asAnonymous_shouldReturn401() {
        // SecurityContext 비어있음
        // GET /me → 401 Unauthorized
    }
}
```

---

## 테스트 실행 계획

### 실행 순서
1. **Unit 테스트**: Controller, Mapper, Service 단위 테스트
2. **E2E 테스트**: 전체 레이어 통합 테스트

### 테스트 우선순위
- **P0 (18개)**: 반드시 통과해야 함 (성공 시나리오, 주요 실패 시나리오, 인증/인가 실패)
- **P1 (4개)**: 가능하면 포함 (엣지 케이스, 경계값)

### 커버리지 목표
- **Line Coverage**: 80% 이상
- **Branch Coverage**: 70% 이상

---

## 주의사항

### 보안
- ⚠️ **테스트 토큰**: 실제 토큰이 아닌 테스트용 Mock 토큰 사용
- ⚠️ **비밀번호**: 평문 비밀번호는 테스트에서만 사용 (실제 환경에서는 암호화)
- ⚠️ **토큰 만료**: 만료된 토큰 테스트는 Mock으로 시뮬레이션

### AuthHub 의존성
- ⚠️ **외부 서비스**: AuthHub SDK가 없으면 테스트 실패
- ✅ **Mock 필수**: E2E 테스트에서는 AuthHub API를 Mock으로 대체

### Spring Security
- ⚠️ **SecurityContext**: 로그아웃 테스트 후 반드시 `SecurityContextHolder.clearContext()` 호출
- ⚠️ **필터 비활성화**: `@AutoConfigureMockMvc(addFilters = false)` 사용하여 수동 제어
- ⚠️ **@PreAuthorize 테스트**: SecurityContext 상태에 따라 401 발생 여부 확인

### 인증/인가 테스트 포인트
- ✅ **비인증 엔드포인트**: login, refresh는 토큰 없이도 접근 가능
- ✅ **인증 필수 엔드포인트**: logout, me는 유효한 토큰 필요
- ✅ **401 시나리오**: 토큰 없음/만료/조작 케이스 모두 커버
- ✅ **403 시나리오 없음**: 권한 체크가 없으므로 403 발생하지 않음

---

## 관련 문서

- API Endpoints: `.claude/docs/api-endpoints/auth.md`
- API Flow: `.claude/docs/api-flow/auth.md`
- Controller 테스트: `AuthCommandControllerRestDocsTest.java`, `AuthQueryControllerRestDocsTest.java`
- Fixtures: `AuthApiFixtures.java`, `AuthCommandFixtures.java`, `AuthResultFixtures.java`
- Spring Security: `MarketAccessChecker.java` (@PreAuthorize 구현체)

---

## 변경 이력

| 날짜 | 작성자 | 변경 내용 |
|------|--------|-----------|
| 2026-02-06 | Claude | 초기 작성 |
| 2026-02-06 | Claude | 인증/인가 시나리오 추가 (401 케이스, 인증 컨텍스트 명시, refresh 엔드포인트 추가) |
