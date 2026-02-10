# CommonCode E2E 통합 테스트 시나리오 (인증/인가 포함)

## 개요

CommonCode 도메인의 E2E 통합 테스트 시나리오 설계 문서입니다.

**대상 모듈**: CommonCode (공통 코드 관리)
**엔드포인트**: 4개 (Query 1개, Command 3개)
**작성일**: 2026-02-06
**업데이트**: 2026-02-06 (인증/인가 시나리오 추가)

---

## 1. 입력 분석

### 분석 문서

- **api-endpoints**: `.claude/docs/api-endpoints/commoncode.md` ✅
- **api-flow**: `.claude/docs/api-flow/commoncode.md` ✅
- **엔드포인트 개수**: Query 1개, Command 3개

### 인증/인가 매핑

| 엔드포인트 | HTTP | @PreAuthorize | 설명 |
|-----------|------|---------------|------|
| 공통 코드 검색 | GET | `@RequirePermission("common-code:read")` | 인증 필요 (일반 사용자 허용) |
| 공통 코드 등록 | POST | `@access.superAdmin()` | 슈퍼어드민 전용 |
| 공통 코드 수정 | PUT | `@access.superAdmin()` | 슈퍼어드민 전용 |
| 활성화 상태 변경 | PATCH | `@access.superAdmin()` | 슈퍼어드민 전용 |

**참고**:
- Query(GET): `@RequirePermission("common-code:read")` → 인증된 사용자 필요
- Command(POST/PUT/PATCH): `@PreAuthorize("@access.superAdmin()")` → 슈퍼어드민만 허용

### Request DTO Validation 규칙

#### RegisterCommonCodeApiRequest
| 필드 | Validation | 실패 케이스 |
|------|------------|-------------|
| commonCodeTypeId | @NotNull | null → 400 |
| code | @NotBlank, @Size(max=50) | null/blank → 400, 51자 이상 → 400 |
| displayName | @NotBlank, @Size(max=100) | null/blank → 400, 101자 이상 → 400 |
| displayOrder | @Min(0) | 음수 → 400 |

#### SearchCommonCodesPageApiRequest
| 필드 | Validation | 실패 케이스 |
|------|------------|-------------|
| commonCodeTypeId | @NotNull | null → 400 |
| page | @Min(0) | 음수 → 400 |
| size | @Min(1), @Max(100) | 0 이하 → 400, 101 이상 → 400 |

#### UpdateCommonCodeApiRequest
| 필드 | Validation | 실패 케이스 |
|------|------------|-------------|
| displayName | @NotBlank | null/blank → 400 |
| displayOrder | @Min(0) | 음수 → 400 |

#### ChangeActiveStatusApiRequest
| 필드 | Validation | 실패 케이스 |
|------|------------|-------------|
| ids | @NotEmpty | null/empty → 400 |
| active | @NotNull | null → 400 |

### Domain 비즈니스 규칙

#### CommonCodeValue (코드값 VO)
- **패턴**: `^[A-Z][A-Z0-9_]*$` (영문 대문자로 시작, 영문 대문자/숫자/언더스코어만 허용)
- **최대 길이**: 50자
- **자동 변환**: 입력값 trim + toUpperCase

**실패 케이스**:
- `"abc"` → `IllegalArgumentException` (소문자로 시작)
- `"1CODE"` → `IllegalArgumentException` (숫자로 시작)
- `"CODE-123"` → `IllegalArgumentException` (하이픈 불허)
- `"CODE 123"` → `IllegalArgumentException` (공백 불허)

#### 중복 제약
- **Unique Key**: (commonCodeTypeId, code)
- **검증 위치**: `CommonCodeValidator.validateNotDuplicate()`
- **예외**: `CommonCodeDuplicateException` → 409 Conflict

#### 상태 전이
- **활성 상태**: `activate()` → `active = true`
- **비활성 상태**: `deactivate()` → `active = false`
- **Soft Delete**: `delete()` → `deletionStatus.deletedAt != null`
- **복원**: `restore()` → `deletionStatus.deletedAt = null`

---

## 2. 시나리오 설계

### 2.1 Query 시나리오: searchCommonCodes

#### P0: 필수 시나리오 (8개)

##### Q1-1. [인증/인가] 인증 없이 조회 시도 → 401
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1` (토큰 없음)
- **Expected**:
  - HTTP 401 Unauthorized
  - Error: "인증이 필요합니다" or "Authentication required"

##### Q1-2. [인증/인가] 인증된 일반 사용자 조회 성공 → 200
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개
- **Auth Context**: 일반 사용자 토큰 (SELLER_ADMIN 역할)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1`
- **Expected**:
  - HTTP 200
  - `@RequirePermission("common-code:read")` 통과
  - `content.size = 3`

##### Q1-3. 기본 조회 - 데이터 존재 시 정상 조회
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개 (활성)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1`
- **Expected**:
  - HTTP 200
  - `content.size = 3`
  - `totalElements = 3`
  - 각 응답 필드 검증 (id, code, displayName, displayOrder, active, createdAt, updatedAt)

##### Q1-4. 빈 결과 - 데이터 없을 때 빈 목록 반환
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 (코드 없음)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1`
- **Expected**:
  - HTTP 200
  - `content.size = 0`
  - `totalElements = 0`

##### Q1-5. 필수 필드 누락 - commonCodeTypeId 없으면 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**: `GET /api/v1/market/admin/common-codes` (commonCodeTypeId 누락)
- **Expected**:
  - HTTP 400
  - Error: "공통 코드 타입 ID는 필수입니다"

##### Q1-6. 존재하지 않는 타입 - 없는 commonCodeTypeId 조회 시 빈 목록
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=99999`
- **Expected**:
  - HTTP 200
  - `content.size = 0`
  - `totalElements = 0`

##### Q1-7. 페이징 동작 확인
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 5개
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&page=0&size=2`
- **Expected**:
  - HTTP 200
  - `content.size = 2`
  - `totalElements = 5`
  - `totalPages = 3`

##### Q1-8. Soft Delete 필터링 - 삭제된 코드는 조회 안 됨
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개 (활성 2개, 삭제 1개)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1`
- **Expected**:
  - HTTP 200
  - `content.size = 2` (삭제된 코드 제외)
  - `totalElements = 2`

#### P1: 중요 시나리오 (4개)

##### Q1-9. 활성화 필터 - active=true 조건 검색
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개 (활성 2개, 비활성 1개)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&active=true`
- **Expected**:
  - HTTP 200
  - `content.size = 2`
  - 모든 결과의 `active = true`

##### Q1-10. 비활성화 필터 - active=false 조건 검색
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 위와 동일
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&active=false`
- **Expected**:
  - HTTP 200
  - `content.size = 1`
  - 결과의 `active = false`

##### Q1-11. 코드 검색 - code 부분 일치 검색
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개 (CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&code=CARD`
- **Expected**:
  - HTTP 200
  - `content.size = 2` (CREDIT_CARD, DEBIT_CARD)

##### Q1-12. 정렬 동작 - sortKey, sortDirection 확인
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개 (displayOrder: 3, 1, 2)
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&sortKey=DISPLAY_ORDER&sortDirection=ASC`
- **Expected**:
  - HTTP 200
  - 결과 순서: displayOrder 1 → 2 → 3

#### P2: 엣지 케이스 (2개)

##### Q1-13. 페이지 범위 초과
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 3개
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&page=10&size=20`
- **Expected**:
  - HTTP 200
  - `content.size = 0`
  - `totalElements = 3`

##### Q1-14. 잘못된 size 값 - 101 이상
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**: `GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&size=101`
- **Expected**:
  - HTTP 400
  - Error: "페이지 크기는 100 이하여야 합니다"

---

### 2.2 Command 시나리오: register

#### P0: 필수 시나리오 (10개)

##### C1-1. [인증/인가] 토큰 없이 등록 시도 → 401
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  (토큰 없음)
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 401 Unauthorized
  - Error: "인증이 필요합니다"

##### C1-2. [인증/인가] 일반 사용자가 등록 시도 → 403
- **Auth Context**: 일반 사용자 토큰 (SELLER_ADMIN 역할)
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 403 Forbidden
  - Error: "권한이 없습니다" or "Access Denied"
  - `@access.superAdmin()` 실패

##### C1-3. 생성 성공 - 유효한 요청으로 생성
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 (PAYMENT_METHOD)
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 201
  - `data` = Long (생성된 ID > 0)
- **DB 검증**:
  - `SELECT * FROM common_code WHERE id = ?` → 존재
  - `active = true`
  - `deleted_at IS NULL`

##### C1-4. 필수 필드 누락 - commonCodeTypeId null → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "공통 코드 타입 ID는 필수입니다"

##### C1-5. 필수 필드 누락 - code blank → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "코드는 필수입니다"

##### C1-6. 필수 필드 누락 - displayName blank → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "표시명은 필수입니다"

##### C1-7. 잘못된 범위 - displayOrder 음수 → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": -1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "표시 순서는 0 이상이어야 합니다"

##### C1-8. 존재하지 않는 타입 → 404 or 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음 (CommonCodeType 없음)
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 99999,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 404 or 400
  - Error: "CommonCodeType을 찾을 수 없습니다"

##### C1-9. 중복 코드 → 409 Conflict
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (CREDIT_CARD)
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "신용카드2",
    "displayOrder": 2
  }
  ```
- **Expected**:
  - HTTP 409
  - Error: "이미 존재하는 공통 코드입니다"

##### C1-10. 다른 타입에서 동일 코드 - 허용됨
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 2개 + CommonCode 1개 (타입1에 CREDIT_CARD)
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 2,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 201
  - 생성 성공 (다른 타입이므로 중복 아님)

#### P1: 중요 시나리오 (3개)

##### C1-11. 코드값 패턴 위반 - 소문자로 시작 → 400 (Domain 검증)
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "creditCard",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "공통 코드 값은 영문 대문자로 시작하고, 영문 대문자/숫자/언더스코어만 허용됩니다"

##### C1-12. 코드값 패턴 위반 - 숫자로 시작 → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "1CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "공통 코드 값은 영문 대문자로 시작하고, 영문 대문자/숫자/언더스코어만 허용됩니다"

##### C1-13. 코드값 자동 변환 - 소문자 입력 → 대문자 저장
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "CREDIT_CARD",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 201
- **DB 검증**:
  - `code = "CREDIT_CARD"` (대문자 저장 확인)

#### P2: 엣지 케이스 (2개)

##### C1-14. 코드값 최대 길이 - 50자
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "A1234567890123456789012345678901234567890123456789",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 201 (정확히 50자는 허용)

##### C1-15. 코드값 최대 길이 초과 - 51자 → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개
- **Request**:
  ```json
  POST /api/v1/market/admin/common-codes
  {
    "commonCodeTypeId": 1,
    "code": "A12345678901234567890123456789012345678901234567890",
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "코드는 50자 이하여야 합니다" or "공통 코드 값은 50자를 초과할 수 없습니다"

---

### 2.3 Command 시나리오: update

#### P0: 필수 시나리오 (7개)

##### C2-1. [인증/인가] 토큰 없이 수정 시도 → 401
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (ID=1)
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  (토큰 없음)
  {
    "displayName": "신용/체크카드",
    "displayOrder": 2
  }
  ```
- **Expected**:
  - HTTP 401 Unauthorized

##### C2-2. [인증/인가] 일반 사용자가 수정 시도 → 403
- **Auth Context**: 일반 사용자 토큰 (SELLER_ADMIN 역할)
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (ID=1)
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  {
    "displayName": "신용/체크카드",
    "displayOrder": 2
  }
  ```
- **Expected**:
  - HTTP 403 Forbidden
  - `@access.superAdmin()` 실패

##### C2-3. 수정 성공 - 존재하는 리소스 수정
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (ID=1, displayName="신용카드", displayOrder=1)
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  {
    "displayName": "신용/체크카드",
    "displayOrder": 2
  }
  ```
- **Expected**:
  - HTTP 200
- **DB 검증**:
  - `displayName = "신용/체크카드"`
  - `displayOrder = 2`
  - `updated_at` 변경됨

##### C2-4. 존재하지 않는 리소스 → 404
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/99999
  {
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 404
  - Error: "공통 코드를 찾을 수 없습니다"

##### C2-5. 필수 필드 누락 - displayName blank → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  {
    "displayName": "",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "표시명은 필수입니다"

##### C2-6. 잘못된 범위 - displayOrder 음수 → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  {
    "displayName": "신용카드",
    "displayOrder": -1
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "표시 순서는 0 이상이어야 합니다"

##### C2-7. 삭제된 코드 수정 시도 → 404
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (deletedAt != null)
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  {
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 404
  - Error: "공통 코드를 찾을 수 없습니다" (Soft Delete 필터링)

#### P1: 중요 시나리오 (1개)

##### C2-8. 코드는 수정 불가 - code는 불변
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (code="CREDIT_CARD")
- **Request**:
  ```json
  PUT /api/v1/market/admin/common-codes/1
  {
    "displayName": "신용카드",
    "displayOrder": 1
  }
  ```
- **Expected**:
  - HTTP 200
- **DB 검증**:
  - `code = "CREDIT_CARD"` (변경 안 됨, API에 code 필드 없음)

---

### 2.4 Command 시나리오: changeActiveStatus

#### P0: 필수 시나리오 (8개)

##### C3-1. [인증/인가] 토큰 없이 상태 변경 시도 → 401
- **Pre-data**: CommonCodeType 1개 + CommonCode 2개
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  (토큰 없음)
  {
    "ids": [1, 2],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 401 Unauthorized

##### C3-2. [인증/인가] 일반 사용자가 상태 변경 시도 → 403
- **Auth Context**: 일반 사용자 토큰 (SELLER_ADMIN 역할)
- **Pre-data**: CommonCodeType 1개 + CommonCode 2개
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 2],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 403 Forbidden
  - `@access.superAdmin()` 실패

##### C3-3. 활성화 성공 - 비활성 코드 활성화
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 2개 (ID=1,2, 모두 active=false)
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 2],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 200
- **DB 검증**:
  - `SELECT active FROM common_code WHERE id IN (1, 2)` → 모두 `true`
  - `updated_at` 변경됨

##### C3-4. 비활성화 성공 - 활성 코드 비활성화
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 2개 (ID=1,2, 모두 active=true)
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 2],
    "active": false
  }
  ```
- **Expected**:
  - HTTP 200
- **DB 검증**:
  - `SELECT active FROM common_code WHERE id IN (1, 2)` → 모두 `false`

##### C3-5. 필수 필드 누락 - ids empty → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "ids는 비어있지 않아야 합니다"

##### C3-6. 필수 필드 누락 - active null → 400
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: 없음
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 2]
  }
  ```
- **Expected**:
  - HTTP 400
  - Error: "활성화 여부는 필수입니다"

##### C3-7. 존재하지 않는 ID 포함 → 404 or 부분 성공
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (ID=1)
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 99999],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 404
  - Error: "공통 코드를 찾을 수 없습니다: [99999]"

##### C3-8. 삭제된 코드 포함 → 404
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 2개 (ID=1 활성, ID=2 삭제됨)
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 2],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 404
  - Error: "공통 코드를 찾을 수 없습니다: [2]" (Soft Delete 필터링)

#### P1: 중요 시나리오 (2개)

##### C3-9. 일부만 존재 - 실패 전 상태 롤백 확인
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (ID=1, active=false)
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 99999],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 404
- **DB 검증**:
  - `SELECT active FROM common_code WHERE id = 1` → `false` (롤백됨)

##### C3-10. 중복 ID 포함 - 중복 제거 처리
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 1개 (ID=1)
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 1, 1],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 200 (중복은 내부에서 처리)
- **DB 검증**:
  - `active = true` (1번만 업데이트)

#### P2: 엣지 케이스 (1개)

##### C3-11. 대용량 ID 목록 - 100개
- **Auth Context**: SUPER_ADMIN 토큰
- **Pre-data**: CommonCodeType 1개 + CommonCode 100개
- **Request**:
  ```json
  PATCH /api/v1/market/admin/common-codes/active-status
  {
    "ids": [1, 2, 3, ..., 100],
    "active": true
  }
  ```
- **Expected**:
  - HTTP 200
- **DB 검증**:
  - 모든 100개 코드의 `active = true`
  - Batch UPDATE 확인 (성능)

---

### 2.5 통합 플로우 시나리오 (CommonCodeType 연관)

#### P0: 필수 플로우 (3개)

##### F1. CRUD 전체 플로우
- **Auth Context**: SUPER_ADMIN 토큰 (모든 단계)
- **Step 1**: CommonCodeType 생성 (PAYMENT_METHOD) → 201
- **Step 2**: CommonCode 생성 (CREDIT_CARD) → 201, ID 반환
- **Step 3**: 목록 조회 → 200, 1개 확인
- **Step 4**: 상세 조회 (GET by ID) 없음 → 목록에서 확인
- **Step 5**: 수정 (displayName 변경) → 200
- **Step 6**: 목록 조회 → 200, displayName 변경 확인
- **Step 7**: 비활성화 → 200
- **Step 8**: 목록 조회 (active=false 필터) → 200, 1개 확인
- **Step 9**: Soft Delete 없음 (API 없음)

##### F2. CommonCodeType 상태 연계 시나리오
- **Auth Context**: SUPER_ADMIN 토큰
- **Step 1**: CommonCodeType 생성 (active=true) → 201
- **Step 2**: CommonCode 생성 → 201
- **Step 3**: CommonCodeType 비활성화 → (별도 API)
- **Step 4**: CommonCode 조회 → 200 (타입 상태와 무관하게 조회 가능)
- **검증**: CommonCodeType이 비활성이어도 CommonCode는 조회/수정 가능

##### F3. 복수 타입에 동일 코드 생성
- **Auth Context**: SUPER_ADMIN 토큰
- **Step 1**: CommonCodeType 2개 생성 (PAYMENT_METHOD, DELIVERY_COMPANY)
- **Step 2**: CommonCode 생성 (타입1, CREDIT_CARD) → 201
- **Step 3**: CommonCode 생성 (타입2, CREDIT_CARD) → 201 (중복 아님)
- **Step 4**: 각 타입별 조회 → 각각 1개씩 조회됨

#### P1: 중요 플로우 (3개)

##### F4. 대량 등록 + 일괄 상태 변경
- **Auth Context**: SUPER_ADMIN 토큰
- **Step 1**: CommonCodeType 1개 생성
- **Step 2**: CommonCode 5개 생성 (순차 POST) → 모두 201
- **Step 3**: 목록 조회 → 200, 5개 확인
- **Step 4**: 일괄 비활성화 (5개 ID) → 200
- **Step 5**: 목록 조회 (active=false) → 200, 5개 확인

##### F5. 검색 + 필터 + 정렬 조합
- **Auth Context**: SUPER_ADMIN 토큰
- **Step 1**: CommonCodeType 1개 + CommonCode 5개 생성
  - CREDIT_CARD (active=true, displayOrder=1)
  - DEBIT_CARD (active=true, displayOrder=2)
  - BANK_TRANSFER (active=false, displayOrder=3)
  - VIRTUAL_ACCOUNT (active=true, displayOrder=4)
  - MOBILE_PAY (active=false, displayOrder=5)
- **Step 2**: 검색 + 필터 + 정렬
  - `GET ?commonCodeTypeId=1&code=CARD&active=true&sortKey=DISPLAY_ORDER&sortDirection=ASC`
- **Expected**:
  - HTTP 200
  - `content.size = 2` (CREDIT_CARD, DEBIT_CARD)
  - 순서: CREDIT_CARD → DEBIT_CARD (displayOrder ASC)

##### F6. [인증/인가] 권한별 플로우
- **Step 1**: 일반 사용자가 조회 (GET) → 200 (성공)
- **Step 2**: 일반 사용자가 등록 (POST) → 403 (실패)
- **Step 3**: 슈퍼어드민이 등록 (POST) → 201 (성공)
- **Step 4**: 슈퍼어드민이 수정 (PUT) → 200 (성공)
- **Step 5**: 슈퍼어드민이 상태 변경 (PATCH) → 200 (성공)
- **검증**: 조회는 인증만, CUD는 슈퍼어드민만

---

## 3. Fixture 설계

### 필요 Repository 목록

| Repository | 용도 |
|------------|------|
| CommonCodeJpaRepository | CommonCode CRUD |
| CommonCodeTypeJpaRepository | CommonCodeType 사전 데이터 |

### testFixtures 존재 여부

| Module | Fixtures 클래스 | 상태 |
|--------|----------------|------|
| domain | CommonCodeFixtures | ✅ 존재 |
| persistence-mysql | CommonCodeJpaEntityFixtures | ❓ 확인 필요 |
| persistence-mysql | CommonCodeTypeJpaEntityFixtures | ✅ 존재 |

### setUp 설계

```java
@BeforeEach
void setUp() {
    // 1. 전체 데이터 초기화
    commonCodeJpaRepository.deleteAll();
    commonCodeTypeJpaRepository.deleteAll();

    // 2. CommonCodeType 사전 데이터 (대부분 시나리오 공통)
    CommonCodeTypeJpaEntity paymentMethodType =
        commonCodeTypeJpaRepository.save(
            CommonCodeTypeJpaEntityFixtures.activeEntity("PAYMENT_METHOD", "결제수단")
        );
    defaultCommonCodeTypeId = paymentMethodType.getId();
}
```

### 인증/인가 Fixture 설계

```java
// 1. 슈퍼어드민 토큰 생성
private String superAdminToken;

@BeforeEach
void setUpAuth() {
    // AuthHub SDK 또는 Mock JWT 토큰 생성
    superAdminToken = authTestHelper.generateToken(
        AuthRole.SUPER_ADMIN,
        "superadmin@example.com"
    );
}

// 2. 일반 사용자 토큰 생성
private String sellerAdminToken;

@BeforeEach
void setUpSellerAuth() {
    sellerAdminToken = authTestHelper.generateToken(
        AuthRole.SELLER_ADMIN,
        "seller@example.com",
        "ORG_12345"
    );
}

// 3. Request Header 설정
RestAssured
    .given()
    .header("Authorization", "Bearer " + superAdminToken)
    .contentType(ContentType.JSON)
    .body(request)
    .when()
    .post("/api/v1/market/admin/common-codes")
    .then()
    .statusCode(201);
```

### 사전 데이터 설계

#### 목록 조회 테스트용
```java
// 활성 3개 + 비활성 1개 + 삭제 1개
CommonCodeJpaEntity cc1 = saveCommonCode("CREDIT_CARD", "신용카드", 1, true, null);
CommonCodeJpaEntity cc2 = saveCommonCode("DEBIT_CARD", "체크카드", 2, true, null);
CommonCodeJpaEntity cc3 = saveCommonCode("BANK_TRANSFER", "계좌이체", 3, true, null);
CommonCodeJpaEntity cc4 = saveCommonCode("MOBILE_PAY", "모바일페이", 4, false, null);
CommonCodeJpaEntity cc5 = saveCommonCode("DELETED_PAY", "삭제된결제", 5, false, Instant.now());
```

#### 검색 테스트용
```java
// 코드 패턴: CARD, TRANSFER, PAY
saveCommonCode("CREDIT_CARD", "신용카드", 1, true, null);
saveCommonCode("DEBIT_CARD", "체크카드", 2, true, null);
saveCommonCode("BANK_TRANSFER", "계좌이체", 3, true, null);
```

#### 정렬 테스트용
```java
// displayOrder: 3, 1, 2 (정렬 확인용)
saveCommonCode("CODE_A", "이름A", 3, true, null);
saveCommonCode("CODE_B", "이름B", 1, true, null);
saveCommonCode("CODE_C", "이름C", 2, true, null);
```

---

## 4. 시나리오 요약

### 통계

| 분류 | P0 | P1 | P2 | 합계 |
|------|----|----|----|----|
| **Query (searchCommonCodes)** | 8 (+2) | 4 | 2 | **14** |
| **Command (register)** | 10 (+2) | 3 | 2 | **15** |
| **Command (update)** | 7 (+2) | 1 | 0 | **8** |
| **Command (changeActiveStatus)** | 8 (+2) | 2 | 1 | **11** |
| **통합 플로우** | 3 | 3 (+1) | 0 | **6** |
| **총합** | **36** (+8) | **13** (+1) | **5** | **54** (+9) |

**변경사항**:
- 각 엔드포인트에 인증/인가 시나리오 추가 (401, 403)
- Query에 일반 사용자 접근 성공 시나리오 추가
- 통합 플로우에 권한별 플로우 추가
- 총 9개 시나리오 추가

### 우선순위별 실행 계획

#### Phase 1: P0 시나리오 (36개) - 필수 검증
- **인증/인가**: 401 Unauthorized (4개), 403 Forbidden (3개), 인증된 조회 (1개)
- Query: 기본 조회, 빈 결과, 필수 필드, 페이징, Soft Delete 필터링
- Command: 생성 성공, 필수 필드 누락, 범위 검증, 존재하지 않는 리소스
- 통합: CRUD 플로우, CommonCodeType 연계

#### Phase 2: P1 시나리오 (13개) - 중요 검증
- Query: 활성화 필터, 코드 검색, 정렬
- Command: 중복 처리, 코드 패턴 검증, 삭제된 리소스 처리
- 통합: 대량 처리, 복합 검색, 권한별 플로우

#### Phase 3: P2 시나리오 (5개) - 엣지 케이스
- Query: 페이지 범위 초과, 잘못된 size
- Command: 최대 길이 경계값, 대용량 ID 목록

---

## 5. 주의사항

### 인증/인가 테스트
- **AuthHub SDK 통합**: 실제 토큰 생성 방식 확인 필요
- **Mock JWT**: 개발 환경에서는 Mock 토큰 생성 헬퍼 사용 가능
- **@PreAuthorize 테스트**: `@access.superAdmin()` SpEL 표현식 실제 동작 확인
- **@RequirePermission**: AuthHub SDK의 권한 체크 로직 확인

### Validation 레이어 분리
- **API Request DTO**: `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max` → 400
- **Domain VO**: `CommonCodeValue` 패턴 검증 → 400 (IllegalArgumentException)
- **Service Validator**: 중복, 존재 여부 → 409, 404

### 트랜잭션 경계
- **Manager 레이어**: `@Transactional` 적용
- **실패 시 롤백**: 트랜잭션 내 예외 발생 → 전체 롤백

### CommonCodeType 의존성
- **Pre-data 필수**: 모든 시나리오에서 CommonCodeType 사전 생성 필요
- **FK 제약**: `common_code.common_code_type_id` → `common_code_type.id`
- **삭제 순서**: CommonCode 먼저 삭제 → CommonCodeType 삭제

### Soft Delete 동작
- **Soft Delete**: `deleted_at != null` → 조회 API에서 필터링
- **UPDATE/DELETE**: 삭제된 코드는 "존재하지 않음"으로 처리 (404)
- **복원 API**: 없음 (현재 스펙)

### 코드값 자동 변환
- **Domain VO**: `code.trim().toUpperCase()` 자동 적용
- **테스트 시**: 입력값과 저장값 대소문자 차이 확인 필요

---

## 6. 다음 단계

### E2E 테스트 구현
```bash
# 테스트 클래스 생성
integration-test/src/test/java/com/ryuqq/marketplace/integration/commoncode/
├── CommonCodeQueryE2ETest.java          # Query 시나리오 (인증 포함)
├── CommonCodeRegisterE2ETest.java       # Register 시나리오 (인증 포함)
├── CommonCodeUpdateE2ETest.java         # Update 시나리오 (인증 포함)
├── CommonCodeStatusChangeE2ETest.java   # ChangeActiveStatus 시나리오 (인증 포함)
└── CommonCodeFlowE2ETest.java           # 통합 플로우 시나리오 (권한별 플로우 포함)
```

### 인증 테스트 헬퍼 클래스
```bash
# AuthTestHelper 생성 (필요 시)
integration-test/src/test/java/com/ryuqq/marketplace/integration/common/
└── AuthTestHelper.java  # JWT Mock 토큰 생성 유틸
```

### Fixture 클래스 생성 (필요 시)
```bash
# persistence-mysql 모듈에 없으면 생성
adapter-out/persistence-mysql/src/testFixtures/java/.../
└── commoncode/CommonCodeJpaEntityFixtures.java
```

### 테스트 태그
```java
@Tag("e2e")
@Tag("commoncode")
@Tag("auth") // 인증/인가 시나리오용 추가 태그
public class CommonCodeQueryE2ETest { ... }
```

---

**작성일**: 2026-02-06
**업데이트**: 2026-02-06 (인증/인가 시나리오 추가)
**버전**: 1.1.0
**작성자**: Claude Code (test-scenario-designer)
