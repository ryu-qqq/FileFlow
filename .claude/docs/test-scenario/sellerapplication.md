# SellerApplication E2E 통합 테스트 시나리오

셀러 입점 신청(SellerApplication) 도메인의 E2E 통합 테스트 시나리오 설계 문서.

---

## 목차

- [1. 개요](#1-개요)
- [2. 인증/인가 개요](#2-인증인가-개요)
- [3. Query 시나리오](#3-query-시나리오)
- [4. Command 시나리오](#4-command-시나리오)
- [5. 통합 플로우 시나리오](#5-통합-플로우-시나리오)
- [6. Fixture 설계](#6-fixture-설계)
- [7. 테스트 우선순위](#7-테스트-우선순위)

---

## 1. 개요

### 1.1 엔드포인트 목록

| Method | Path | 분류 | 설명 | 인증/인가 |
|--------|------|------|------|-----------|
| GET | /seller-applications | Query | 목록 조회 (페이징, 필터링, 검색) | `@access.superAdmin()` |
| GET | /seller-applications/{id} | Query | 상세 조회 | `@access.superAdmin()` |
| POST | /seller-applications | Command | 입점 신청 생성 | `@access.authenticated()` |
| POST | /seller-applications/{id}/approve | Command | 승인 (→ Seller 생성) | `@access.superAdmin()` |
| POST | /seller-applications/{id}/reject | Command | 거절 | `@access.superAdmin()` |

### 1.2 테스트 범위

- **Adapter-In (REST API)**: Controller → UseCase 호출
- **Application Layer**: Service → Manager → Port 호출
- **Domain Layer**: Aggregate 비즈니스 로직
- **Adapter-Out (Persistence)**: Repository → Database 저장/조회
- **Security**: 인증/인가 검증 (Spring Security + MarketAccessChecker)

### 1.3 검증 항목

- HTTP 응답 코드 (200, 201, 204, 400, 401, 403, 404, 409)
- 응답 Body 구조 및 필드 값
- Database 상태 변화 (INSERT/UPDATE 확인)
- 도메인 이벤트 발행 (CreatedEvent, ApprovedEvent, RejectedEvent)
- Validation 실패 (Jakarta Validation)
- 비즈니스 규칙 검증 (중복 신청, 상태 전이)
- 인증/인가 실패 (401 Unauthorized, 403 Forbidden)

---

## 2. 인증/인가 개요

### 2.1 MarketAccessChecker 주요 메서드

| 메서드 | 역할 | 설명 |
|-------|------|------|
| `authenticated()` | 인증된 사용자 확인 | 로그인만 되어 있으면 true |
| `superAdmin()` | 슈퍼어드민 권한 확인 | SUPER_ADMIN 역할 필수 |
| `hasPermission(String)` | 특정 권한 보유 확인 | Permission 토큰 검증 |

### 2.2 엔드포인트별 권한 매트릭스

| 엔드포인트 | @PreAuthorize | 성공 조건 | 실패 시나리오 |
|-----------|---------------|----------|--------------|
| GET /seller-applications | `@access.superAdmin()` | SUPER_ADMIN 역할 | 401 (토큰 없음), 403 (일반 사용자) |
| GET /seller-applications/{id} | `@access.superAdmin()` | SUPER_ADMIN 역할 | 401 (토큰 없음), 403 (일반 사용자) |
| POST /seller-applications | `@access.authenticated()` | 인증만 필요 | 401 (토큰 없음) |
| POST /seller-applications/{id}/approve | `@access.superAdmin()` | SUPER_ADMIN 역할 | 401 (토큰 없음), 403 (일반 사용자) |
| POST /seller-applications/{id}/reject | `@access.superAdmin()` | SUPER_ADMIN 역할 | 401 (토큰 없음), 403 (일반 사용자) |

### 2.3 테스트용 인증 컨텍스트

```java
// SUPER_ADMIN 권한 컨텍스트
Authentication superAdminAuth = createSuperAdminAuth();
SecurityContextHolder.getContext().setAuthentication(superAdminAuth);

// 일반 사용자 (SELLER_ADMIN) 권한 컨텍스트
Authentication sellerAdminAuth = createSellerAdminAuth();
SecurityContextHolder.getContext().setAuthentication(sellerAdminAuth);

// 인증 없음 (Anonymous)
SecurityContextHolder.clearContext();
```

---

## 3. Query 시나리오

### 3.1 GET /seller-applications - 목록 조회

#### 시나리오 1: 정상 조회 (데이터 존재) [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 다양한 상태의 입점 신청 5건 저장
  - PENDING 2건 (셀러명: "셀러A", "셀러B")
  - APPROVED 2건 (셀러명: "셀러C", "셀러D")
  - REJECTED 1건 (셀러명: "셀러E")

**When**:
```http
GET /api/v1/market/admin/seller-applications?page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 5`
  - `totalElements = 5`
  - `totalPages = 1`
  - `content[0].status` = 유효한 상태값
  - `content[0].sellerInfo.sellerName` = 저장한 셀러명
- DB 조회 확인 (no mutation)

---

#### 시나리오 2: 인증 토큰 없이 목록 조회 시도
**우선순위**: P0

**Given**:
- 인증 컨텍스트: 없음 (Anonymous)
- DB에 입점 신청 5건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?page=0&size=10
Authorization: (없음)
```

**Then**:
- HTTP 401 Unauthorized
- Response Body:
  - `success = false`
  - `error.code` = 인증 오류 코드
  - `error.message` 포함 "인증이 필요합니다" 또는 "Unauthorized"

---

#### 시나리오 3: 일반 사용자가 목록 조회 시도 (권한 없음)
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (일반 사용자)
- DB에 입점 신청 5건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?page=0&size=10
Authorization: Bearer {sellerAdminToken}
```

**Then**:
- HTTP 403 Forbidden
- Response Body:
  - `success = false`
  - `error.code` = 권한 오류 코드
  - `error.message` 포함 "권한이 없습니다" 또는 "Access Denied"

---

#### 시나리오 4: 빈 결과 조회 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 입점 신청 데이터 없음

**When**:
```http
GET /api/v1/market/admin/seller-applications?page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content = []`
  - `totalElements = 0`
  - `totalPages = 0`

---

#### 시나리오 5: 페이징 동작 확인 [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 입점 신청 10건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?page=1&size=3
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 3` (2번째 페이지)
  - `totalElements = 10`
  - `totalPages = 4`
  - `number = 1`
  - `content[0].id` = 4번째 신청 ID (정렬 확인)

---

#### 시나리오 6: 상태 필터링 (status) [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 3건, APPROVED 2건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?status=PENDING&page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 3`
  - `content[*].status` = 모두 "PENDING"

---

#### 시나리오 7: 복수 상태 필터링 [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 2건, APPROVED 3건, REJECTED 1건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?status=PENDING,APPROVED&page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 5`
  - `content[*].status` = "PENDING" 또는 "APPROVED"만 포함

---

#### 시나리오 8: 셀러명 검색 (searchField=sellerName) [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 셀러명 "테스트셀러", "ABC셀러", "XYZ마켓" 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?searchField=sellerName&searchWord=테스트&page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 1`
  - `content[0].sellerInfo.sellerName` = "테스트셀러"

---

#### 시나리오 9: 회사명 검색 (searchField=companyName) [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 회사명 "ABC컴퍼니", "XYZ주식회사", "테스트컴퍼니" 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?searchField=companyName&searchWord=XYZ&page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 1`
  - `content[0].businessInfo.companyName` = "XYZ주식회사"

---

#### 시나리오 10: 정렬 (sortKey, sortDirection) [SUPER_ADMIN]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 신청일시가 다른 입점 신청 5건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?sortKey=appliedAt&sortDirection=ASC&page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content[0].appliedAt` < `content[1].appliedAt` < ... (오름차순 확인)

---

#### 시나리오 11: 복합 필터 (상태 + 검색) [SUPER_ADMIN]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- PENDING 상태, 셀러명 "테스트A" 1건
- APPROVED 상태, 셀러명 "테스트B" 1건
- PENDING 상태, 셀러명 "ABC" 1건

**When**:
```http
GET /api/v1/market/admin/seller-applications?status=PENDING&searchField=sellerName&searchWord=테스트&page=0&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content.size = 1`
  - `content[0].status` = "PENDING"
  - `content[0].sellerInfo.sellerName` = "테스트A"

---

#### 시나리오 12: 페이징 범위 초과 [SUPER_ADMIN]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 입점 신청 5건 저장

**When**:
```http
GET /api/v1/market/admin/seller-applications?page=10&size=10
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `content = []`
  - `totalElements = 5`
  - `totalPages = 1`
  - `number = 10` (요청한 페이지 번호 그대로)

---

### 3.2 GET /seller-applications/{id} - 상세 조회

#### 시나리오 1: 정상 조회 (PENDING 상태) [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
GET /api/v1/market/admin/seller-applications/100
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `id = 100`
  - `status = "PENDING"`
  - `sellerInfo` 전체 필드 존재
  - `businessInfo` 전체 필드 존재
  - `csContact` 전체 필드 존재
  - `contactInfo` 전체 필드 존재
  - `agreement.agreedAt` = ISO 8601 형식
  - `appliedAt` = ISO 8601 형식
  - `processedAt = null`
  - `processedBy = null`
  - `rejectionReason = null`
  - `approvedSellerId = null`

---

#### 시나리오 2: 인증 토큰 없이 상세 조회 시도
**우선순위**: P0

**Given**:
- 인증 컨텍스트: 없음 (Anonymous)
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
GET /api/v1/market/admin/seller-applications/100
Authorization: (없음)
```

**Then**:
- HTTP 401 Unauthorized
- Response Body:
  - `success = false`
  - `error.message` 포함 "인증이 필요합니다"

---

#### 시나리오 3: 일반 사용자가 상세 조회 시도 (권한 없음)
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (일반 사용자)
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
GET /api/v1/market/admin/seller-applications/100
Authorization: Bearer {sellerAdminToken}
```

**Then**:
- HTTP 403 Forbidden
- Response Body:
  - `success = false`
  - `error.message` 포함 "권한이 없습니다"

---

#### 시나리오 4: 정상 조회 (APPROVED 상태) [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 APPROVED 상태 입점 신청 1건 저장 (ID=101, approvedSellerId=200)

**When**:
```http
GET /api/v1/market/admin/seller-applications/101
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `id = 101`
  - `status = "APPROVED"`
  - `approvedSellerId = 200`
  - `processedAt` != null (ISO 8601 형식)
  - `processedBy` != null
  - `rejectionReason = null`

---

#### 시나리오 5: 정상 조회 (REJECTED 상태) [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 REJECTED 상태 입점 신청 1건 저장 (ID=102, rejectionReason="서류 미비")

**When**:
```http
GET /api/v1/market/admin/seller-applications/102
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `id = 102`
  - `status = "REJECTED"`
  - `rejectionReason = "서류 미비"`
  - `processedAt` != null
  - `processedBy` != null
  - `approvedSellerId = null`

---

#### 시나리오 6: 존재하지 않는 ID 조회 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 ID=99999인 입점 신청 없음

**When**:
```http
GET /api/v1/market/admin/seller-applications/99999
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 404 Not Found
- Response Body:
  - `success = false`
  - `error.code = "SELAPP-001"`
  - `error.message = "입점 신청을 찾을 수 없습니다"`

---

#### 시나리오 7: 잘못된 ID 형식 (음수) [SUPER_ADMIN]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SUPER_ADMIN

**When**:
```http
GET /api/v1/market/admin/seller-applications/-1
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 400 Bad Request

---

#### 시나리오 8: 모든 필드 완전성 검증 [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 모든 선택 필드를 포함한 입점 신청 1건 저장
  - displayName, logoUrl, description, saleReportNumber 모두 설정
  - contactInfo.name, phone, email 모두 설정

**When**:
```http
GET /api/v1/market/admin/seller-applications/{id}
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - 모든 필드가 null이 아니고 값이 일치

---

## 4. Command 시나리오

### 4.1 POST /seller-applications - 입점 신청 생성

#### 시나리오 1: 정상 생성 (최소 필수 필드만) [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)
- DB에 동일 사업자등록번호의 PENDING 신청 없음

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": {
    "sellerName": "신규셀러"
  },
  "businessInfo": {
    "registrationNumber": "999-88-77766",
    "companyName": "신규컴퍼니",
    "representative": "김대표",
    "businessAddress": {
      "zipCode": "12345",
      "line1": "서울시 강남구"
    }
  },
  "csContact": {
    "phone": "02-1234-5678",
    "email": "cs@newcompany.com"
  },
  "settlementInfo": {
    "bankName": "국민은행",
    "accountNumber": "123456789012",
    "accountHolderName": "김대표"
  }
}
```

**Then**:
- HTTP 201 Created
- Response Body:
  - `applicationId` != null (Long)
- DB 검증:
  - `SELECT * FROM seller_application WHERE id = {applicationId}`
    - `seller_name = "신규셀러"`
    - `display_name = "신규셀러"` (자동 설정)
    - `status = "PENDING"`
    - `settlement_cycle = "MONTHLY"` (기본값)
    - `settlement_day = 1` (기본값)
    - `applied_at` != null
    - `agreement_agreed_at` != null
- 도메인 이벤트 발행 확인:
  - `SellerApplicationCreatedEvent` (TransactionEventRegistry)

---

#### 시나리오 2: 인증 토큰 없이 신청 시도
**우선순위**: P0

**Given**:
- 인증 컨텍스트: 없음 (Anonymous)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: (없음)
Content-Type: application/json

{
  "sellerInfo": {
    "sellerName": "신규셀러"
  },
  ...
}
```

**Then**:
- HTTP 401 Unauthorized
- Response Body:
  - `success = false`
  - `error.message` 포함 "인증이 필요합니다"
- DB 검증:
  - 새로운 신청 생성되지 않음

---

#### 시나리오 3: 정상 생성 (모든 필드 포함) [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": {
    "sellerName": "풀옵션셀러",
    "displayName": "풀옵션 브랜드",
    "logoUrl": "https://example.com/full-logo.png",
    "description": "모든 필드를 입력한 셀러입니다."
  },
  "businessInfo": {
    "registrationNumber": "111-22-33344",
    "companyName": "풀옵션컴퍼니",
    "representative": "이대표",
    "saleReportNumber": "제2025-서울강남-9999호",
    "businessAddress": {
      "zipCode": "54321",
      "line1": "서울시 서초구",
      "line2": "서초대로 123"
    }
  },
  "csContact": {
    "phone": "02-9999-8888",
    "email": "cs@fullopt.com",
    "mobile": "010-1111-2222"
  },
  "contactInfo": {
    "name": "박담당",
    "phone": "010-3333-4444",
    "email": "contact@fullopt.com"
  },
  "settlementInfo": {
    "bankCode": "004",
    "bankName": "KB국민은행",
    "accountNumber": "987654321098",
    "accountHolderName": "이대표",
    "settlementCycle": "WEEKLY",
    "settlementDay": 5
  }
}
```

**Then**:
- HTTP 201 Created
- DB 검증:
  - `display_name = "풀옵션 브랜드"` (명시적 값 사용)
  - `logo_url = "https://example.com/full-logo.png"`
  - `description = "모든 필드를 입력한 셀러입니다."`
  - `sale_report_number = "제2025-서울강남-9999호"`
  - `cs_mobile = "010-1111-2222"`
  - `contact_name = "박담당"`
  - `settlement_cycle = "WEEKLY"`
  - `settlement_day = 5`

---

#### 시나리오 4: 필수 필드 누락 - sellerName [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": {
    "sellerName": null
  },
  "businessInfo": { ... }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `success = false`
  - `error.message` 포함 "셀러명은 필수입니다"

---

#### 시나리오 5: 필수 필드 누락 - registrationNumber [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": { ... },
  "businessInfo": {
    "registrationNumber": ""
  }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "사업자등록번호는 필수입니다"

---

#### 시나리오 6: 잘못된 이메일 형식 (csContact.email) [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  ...
  "csContact": {
    "phone": "02-1234-5678",
    "email": "invalid-email-format"
  }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "올바른 이메일 형식이 아닙니다"

---

#### 시나리오 7: 계좌번호 숫자 외 입력 (settlementInfo.accountNumber) [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  ...
  "settlementInfo": {
    "bankName": "국민은행",
    "accountNumber": "12345-67890",
    "accountHolderName": "홍길동"
  }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "계좌번호는 숫자만 입력 가능합니다"

---

#### 시나리오 8: 정산 주기 잘못된 값 (settlementCycle) [AUTHENTICATED]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  ...
  "settlementInfo": {
    ...
    "settlementCycle": "DAILY"
  }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "정산 주기는 WEEKLY, BIWEEKLY, MONTHLY 중 하나여야 합니다"

---

#### 시나리오 9: 정산일 범위 초과 (settlementDay) [AUTHENTICATED]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  ...
  "settlementInfo": {
    ...
    "settlementDay": 32
  }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "정산일은 31 이하여야 합니다"

---

#### 시나리오 10: 중복 신청 (동일 사업자등록번호, PENDING) [AUTHENTICATED]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)
- DB에 사업자등록번호 "123-45-67890"인 PENDING 상태 신청 존재

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": { ... },
  "businessInfo": {
    "registrationNumber": "123-45-67890",
    ...
  }
}
```

**Then**:
- HTTP 409 Conflict
- Response Body:
  - `success = false`
  - `error.code = "SELAPP-003"`
  - `error.message = "이미 대기 중인 신청이 존재합니다"`
- DB 검증:
  - 새로운 신청 생성되지 않음 (COUNT 불변)

---

#### 시나리오 11: 중복 신청 허용 (동일 사업자등록번호, APPROVED) [AUTHENTICATED]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)
- DB에 사업자등록번호 "123-45-67890"인 APPROVED 상태 신청 존재

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": { ... },
  "businessInfo": {
    "registrationNumber": "123-45-67890",
    ...
  }
}
```

**Then**:
- HTTP 201 Created (승인/거절된 신청은 중복 검증 대상 아님)
- DB 검증:
  - 새로운 PENDING 신청 생성됨

---

#### 시나리오 12: 필드 길이 초과 (sellerName) [AUTHENTICATED]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": {
    "sellerName": "A".repeat(101)
  }
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "셀러명은 100자 이하여야 합니다"

---

#### 시나리오 13: JSON 형식 오류 [AUTHENTICATED]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (인증된 사용자)

**When**:
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
Content-Type: application/json

{
  "sellerInfo": {
    "sellerName": "테스트셀러"
  // 닫는 괄호 누락
```

**Then**:
- HTTP 400 Bad Request

---

### 4.2 POST /seller-applications/{id}/approve - 승인

#### 시나리오 1: 정상 승인 (PENDING → APPROVED) [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)
- 셀러명 "승인대상셀러", 사업자등록번호 "555-44-33322" (중복 없음)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 200 OK
- Response Body:
  - `sellerId` != null (Long, 새로 생성된 셀러 ID)
- DB 검증 (seller_application):
  - `status = "APPROVED"`
  - `approved_seller_id = {sellerId}`
  - `processed_at` != null
  - `processed_by` != null
- DB 검증 (seller 관련 테이블 7개):
  1. `seller` 테이블: 1건 INSERT
     - `id = {sellerId}`
     - `seller_name = "승인대상셀러"`
     - `status = "ACTIVE"`
  2. `seller_business_info` 테이블: 1건 INSERT
     - `seller_id = {sellerId}`
     - `registration_number = "555-44-33322"`
  3. `seller_cs_contact` 테이블: 1건 INSERT
  4. `seller_contract` 테이블: 1건 INSERT
  5. `seller_settlement` 테이블: 1건 INSERT
  6. `seller_auth_outbox` 테이블: 1건 INSERT (관리자 인증 대기)
- 도메인 이벤트 발행:
  - `SellerApplicationApprovedEvent`

---

#### 시나리오 2: 인증 토큰 없이 승인 시도
**우선순위**: P0

**Given**:
- 인증 컨텍스트: 없음 (Anonymous)
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: (없음)
```

**Then**:
- HTTP 401 Unauthorized
- Response Body:
  - `success = false`
  - `error.message` 포함 "인증이 필요합니다"
- DB 검증:
  - `seller_application.status` = "PENDING" (변경 없음)
  - seller 관련 테이블 생성 없음

---

#### 시나리오 3: 일반 사용자가 승인 시도 (권한 없음)
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (일반 사용자)
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {sellerAdminToken}
```

**Then**:
- HTTP 403 Forbidden
- Response Body:
  - `success = false`
  - `error.message` 포함 "권한이 없습니다"
- DB 검증:
  - `seller_application.status` = "PENDING" (변경 없음)
  - seller 관련 테이블 생성 없음

---

#### 시나리오 4: 존재하지 않는 신청 승인 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 ID=99999인 입점 신청 없음

**When**:
```http
POST /api/v1/market/admin/seller-applications/99999/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 404 Not Found
- Response Body:
  - `success = false`
  - `error.code = "SELAPP-001"`
  - `error.message = "입점 신청을 찾을 수 없습니다"`

---

#### 시나리오 5: 이미 승인된 신청 재승인 시도 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 APPROVED 상태 입점 신청 1건 저장 (ID=101)

**When**:
```http
POST /api/v1/market/admin/seller-applications/101/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 400 Bad Request (or 409 Conflict)
- Response Body:
  - `success = false`
  - `error.message` 포함 "이미 처리된 신청입니다"
- DB 검증:
  - seller 관련 테이블에 중복 생성 없음

---

#### 시나리오 6: 이미 거절된 신청 승인 시도 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 REJECTED 상태 입점 신청 1건 저장 (ID=102)

**When**:
```http
POST /api/v1/market/admin/seller-applications/102/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `success = false`
  - `error.message` 포함 "이미 처리된 신청입니다"

---

#### 시나리오 7: 셀러명 중복 검증 [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 `seller` 테이블에 `seller_name = "기존셀러"` 존재
- DB에 PENDING 상태 신청 (ID=100, sellerName="기존셀러")

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 409 Conflict
- Response Body:
  - `success = false`
  - `error.code = "SELLER-XXX"` (Seller 도메인 에러)
  - `error.message` 포함 "이미 존재하는 셀러명입니다" (추정)
- DB 검증:
  - seller 관련 테이블에 새로운 데이터 생성 없음
  - `seller_application.status` = "PENDING" (변경 없음)

---

#### 시나리오 8: 사업자등록번호 중복 검증 [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 `seller_business_info` 테이블에 `registration_number = "111-22-33344"` 존재
- DB에 PENDING 상태 신청 (ID=100, registrationNumber="111-22-33344")

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 409 Conflict
- Response Body:
  - `success = false`
  - `error.message` 포함 "이미 존재하는 사업자등록번호입니다" (추정)
- DB 검증:
  - seller 관련 테이블에 새로운 데이터 생성 없음
  - `seller_application.status` = "PENDING" (변경 없음)

---

#### 시나리오 9: 트랜잭션 롤백 확인 (통합 시나리오) [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 신청 1건 (ID=100)
- 승인 처리 중 예외 발생 시뮬레이션 (예: SellerAuthOutbox 저장 실패)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
```

**Then**:
- HTTP 500 Internal Server Error (or 적절한 에러 코드)
- DB 검증:
  - `seller_application.status` = "PENDING" (롤백)
  - seller 관련 테이블 6개: 모두 INSERT 없음 (롤백)

> **Note**: 이 시나리오는 실제 예외 발생 지점을 Mock으로 제어해야 테스트 가능

---

### 4.3 POST /seller-applications/{id}/reject - 거절

#### 시나리오 1: 정상 거절 (PENDING → REJECTED) [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/reject
Authorization: Bearer {superAdminToken}
Content-Type: application/json

{
  "rejectionReason": "서류 미비로 인한 반려"
}
```

**Then**:
- HTTP 204 No Content
- Response Body: 없음
- DB 검증:
  - `status = "REJECTED"`
  - `rejection_reason = "서류 미비로 인한 반려"`
  - `processed_at` != null
  - `processed_by` != null
  - `approved_seller_id = null`
- 도메인 이벤트 발행:
  - `SellerApplicationRejectedEvent`

---

#### 시나리오 2: 인증 토큰 없이 거절 시도
**우선순위**: P0

**Given**:
- 인증 컨텍스트: 없음 (Anonymous)
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/reject
Authorization: (없음)
Content-Type: application/json

{
  "rejectionReason": "테스트 거절"
}
```

**Then**:
- HTTP 401 Unauthorized
- Response Body:
  - `success = false`
  - `error.message` 포함 "인증이 필요합니다"
- DB 검증:
  - `seller_application.status` = "PENDING" (변경 없음)

---

#### 시나리오 3: 일반 사용자가 거절 시도 (권한 없음)
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SELLER_ADMIN (일반 사용자)
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/reject
Authorization: Bearer {sellerAdminToken}
Content-Type: application/json

{
  "rejectionReason": "테스트 거절"
}
```

**Then**:
- HTTP 403 Forbidden
- Response Body:
  - `success = false`
  - `error.message` 포함 "권한이 없습니다"
- DB 검증:
  - `seller_application.status` = "PENDING" (변경 없음)

---

#### 시나리오 4: 거절 사유 누락 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 입점 신청 1건 저장 (ID=100)

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/reject
Authorization: Bearer {superAdminToken}
Content-Type: application/json

{
  "rejectionReason": ""
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `success = false`
  - `error.message` 포함 "거절 사유는 필수입니다"

---

#### 시나리오 5: 존재하지 않는 신청 거절 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 ID=99999인 입점 신청 없음

**When**:
```http
POST /api/v1/market/admin/seller-applications/99999/reject
Authorization: Bearer {superAdminToken}
Content-Type: application/json

{
  "rejectionReason": "테스트 거절"
}
```

**Then**:
- HTTP 404 Not Found
- Response Body:
  - `success = false`
  - `error.code = "SELAPP-001"`

---

#### 시나리오 6: 이미 승인된 신청 거절 시도 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 APPROVED 상태 입점 신청 1건 저장 (ID=101)

**When**:
```http
POST /api/v1/market/admin/seller-applications/101/reject
Authorization: Bearer {superAdminToken}
Content-Type: application/json

{
  "rejectionReason": "잘못된 거절"
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `success = false`
  - `error.message` 포함 "이미 처리된 신청입니다"

---

#### 시나리오 7: 이미 거절된 신청 재거절 시도 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 REJECTED 상태 입점 신청 1건 저장 (ID=102)

**When**:
```http
POST /api/v1/market/admin/seller-applications/102/reject
Authorization: Bearer {superAdminToken}
Content-Type: application/json

{
  "rejectionReason": "재거절"
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `success = false`
  - `error.message` 포함 "이미 처리된 신청입니다"

---

#### 시나리오 8: 거절 사유 길이 초과 [SUPER_ADMIN]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SUPER_ADMIN

**When**:
```http
POST /api/v1/market/admin/seller-applications/100/reject
Authorization: Bearer {superAdminToken}
Content-Type: application/json

{
  "rejectionReason": "A".repeat(501)
}
```

**Then**:
- HTTP 400 Bad Request
- Response Body:
  - `error.message` 포함 "거절 사유는 500자 이하여야 합니다"

---

## 5. 통합 플로우 시나리오

### 시나리오 1: CRUD 전체 플로우 (인증 포함)
**우선순위**: P0

**Step 1**: 입점 신청 생성 [AUTHENTICATED]
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
{
  "sellerInfo": { "sellerName": "플로우테스트셀러" },
  ...
}
→ HTTP 201, applicationId = 100
```

**Step 2**: 일반 사용자가 상세 조회 시도 (403)
```http
GET /api/v1/market/admin/seller-applications/100
Authorization: Bearer {sellerAdminToken}
→ HTTP 403 Forbidden
```

**Step 3**: SUPER_ADMIN이 상세 조회 (PENDING 확인)
```http
GET /api/v1/market/admin/seller-applications/100
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ status = "PENDING"
→ approvedSellerId = null
```

**Step 4**: SUPER_ADMIN이 목록 조회 (신청 포함 확인)
```http
GET /api/v1/market/admin/seller-applications?searchField=sellerName&searchWord=플로우테스트
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ content.size = 1
→ content[0].id = 100
```

**Step 5**: 일반 사용자가 승인 시도 (403)
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {sellerAdminToken}
→ HTTP 403 Forbidden
```

**Step 6**: SUPER_ADMIN이 승인 처리
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ sellerId = 200
```

**Step 7**: 상세 조회 (APPROVED 확인)
```http
GET /api/v1/market/admin/seller-applications/100
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ status = "APPROVED"
→ approvedSellerId = 200
→ processedAt != null
```

**Step 8**: Seller 생성 확인 (Cross-Domain)
```http
GET /api/v1/market/admin/sellers/200 (Seller API)
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ sellerName = "플로우테스트셀러"
```

---

### 시나리오 2: 신청 → 거절 플로우 (인증 포함)
**우선순위**: P0

**Step 1**: 입점 신청 생성 [AUTHENTICATED]
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
{
  "sellerInfo": { "sellerName": "거절테스트셀러" },
  ...
}
→ HTTP 201, applicationId = 101
```

**Step 2**: 일반 사용자가 거절 시도 (403)
```http
POST /api/v1/market/admin/seller-applications/101/reject
Authorization: Bearer {sellerAdminToken}
{
  "rejectionReason": "사업자등록증 미제출"
}
→ HTTP 403 Forbidden
```

**Step 3**: SUPER_ADMIN이 거절 처리
```http
POST /api/v1/market/admin/seller-applications/101/reject
Authorization: Bearer {superAdminToken}
{
  "rejectionReason": "사업자등록증 미제출"
}
→ HTTP 204
```

**Step 4**: 상세 조회 (REJECTED 확인)
```http
GET /api/v1/market/admin/seller-applications/101
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ status = "REJECTED"
→ rejectionReason = "사업자등록증 미제출"
→ approvedSellerId = null
```

**Step 5**: Seller 생성 안 됨 확인
- DB 검증: `seller` 테이블에 해당 신청 관련 데이터 없음

---

### 시나리오 3: 목록 조회 → 상세 조회 연계 [SUPER_ADMIN]
**우선순위**: P1

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 입점 신청 3건 저장 (PENDING 2건, APPROVED 1건)

**Step 1**: 목록 조회
```http
GET /api/v1/market/admin/seller-applications?page=0&size=10
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ content.size = 3
→ content[0].id = 103
```

**Step 2**: 첫 번째 항목 ID 추출 후 상세 조회
```http
GET /api/v1/market/admin/seller-applications/103
Authorization: Bearer {superAdminToken}
→ HTTP 200
→ id = 103
→ (목록의 데이터와 일치 확인)
```

---

### 시나리오 4: 상태 전이 플로우 (PENDING만 처리 가능)
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 신청 1건 (ID=100)

**Step 1**: 승인 처리
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
→ HTTP 200, sellerId = 200
```

**Step 2**: 동일 신청 재승인 시도
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
→ HTTP 400 (이미 처리됨)
```

**Step 3**: 동일 신청 거절 시도
```http
POST /api/v1/market/admin/seller-applications/100/reject
Authorization: Bearer {superAdminToken}
{
  "rejectionReason": "테스트"
}
→ HTTP 400 (이미 처리됨)
```

---

### 시나리오 5: 페이징 + 상세 조회 플로우 [SUPER_ADMIN]
**우선순위**: P2

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 입점 신청 10건 저장

**Step 1**: 1페이지 조회 (size=5)
```http
GET /api/v1/market/admin/seller-applications?page=0&size=5
Authorization: Bearer {superAdminToken}
→ content[0].id, content[1].id, ... 추출
```

**Step 2**: 2페이지 조회
```http
GET /api/v1/market/admin/seller-applications?page=1&size=5
Authorization: Bearer {superAdminToken}
→ content[0].id, content[1].id, ... 추출
```

**Step 3**: 각 페이지의 첫 번째 항목 상세 조회
```http
GET /api/v1/market/admin/seller-applications/{page1_first_id}
GET /api/v1/market/admin/seller-applications/{page2_first_id}
Authorization: Bearer {superAdminToken}
→ 모두 HTTP 200
```

---

### 시나리오 6: 승인 → Seller 관련 엔티티 검증 플로우 [SUPER_ADMIN]
**우선순위**: P0

**Given**:
- 인증 컨텍스트: SUPER_ADMIN
- DB에 PENDING 상태 신청 1건 (ID=100)

**Step 1**: 승인 처리
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
→ HTTP 200, sellerId = 200
```

**Step 2**: DB 검증 (7개 테이블)
```sql
-- 1. seller
SELECT * FROM seller WHERE id = 200;
→ EXISTS, seller_name = "..."

-- 2. seller_business_info
SELECT * FROM seller_business_info WHERE seller_id = 200;
→ EXISTS, registration_number = "..."

-- 3. seller_cs_contact
SELECT * FROM seller_cs_contact WHERE seller_id = 200;
→ EXISTS, phone = "...", email = "..."

-- 4. seller_contract
SELECT * FROM seller_contract WHERE seller_id = 200;
→ EXISTS

-- 5. seller_settlement
SELECT * FROM seller_settlement WHERE seller_id = 200;
→ EXISTS, bank_name = "...", account_number = "..."

-- 6. seller_auth_outbox
SELECT * FROM seller_auth_outbox WHERE seller_id = 200;
→ EXISTS, status = "PENDING" (이메일 발송 대기)

-- 7. seller_application (상태 업데이트)
SELECT * FROM seller_application WHERE id = 100;
→ status = "APPROVED", approved_seller_id = 200
```

---

### 시나리오 7: 중복 신청 → 승인 → 재신청 플로우
**우선순위**: P1

**Step 1**: 첫 번째 신청 [AUTHENTICATED]
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
{
  "businessInfo": {
    "registrationNumber": "777-88-99900"
  }
}
→ HTTP 201, applicationId = 100
```

**Step 2**: 동일 사업자등록번호로 중복 신청
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
{
  "businessInfo": {
    "registrationNumber": "777-88-99900"
  }
}
→ HTTP 409 (이미 대기 중인 신청 존재)
```

**Step 3**: 첫 번째 신청 승인 [SUPER_ADMIN]
```http
POST /api/v1/market/admin/seller-applications/100/approve
Authorization: Bearer {superAdminToken}
→ HTTP 200
```

**Step 4**: 동일 사업자등록번호로 재신청 [AUTHENTICATED]
```http
POST /api/v1/market/admin/seller-applications
Authorization: Bearer {authenticatedToken}
{
  "businessInfo": {
    "registrationNumber": "777-88-99900"
  }
}
→ HTTP 201 (승인된 신청은 중복 검증 대상 아님)
```

---

## 6. Fixture 설계

### 6.1 필요 Repository 목록

```java
// Adapter-Out (Persistence)
- SellerApplicationJpaRepository (Command)
- SellerApplicationQueryDslRepository (Query)

// Seller 도메인 (승인 시 필요)
- SellerJpaRepository
- SellerBusinessInfoJpaRepository
- SellerCsJpaRepository
- SellerContractJpaRepository
- SellerSettlementJpaRepository
- SellerAuthOutboxJpaRepository
```

### 6.2 testFixtures 활용

**기존 Fixtures**:
- `SellerApplicationJpaEntityFixtures`
  - `pendingEntity()`: PENDING 상태 기본 엔티티
  - `approvedEntity(Long sellerId)`: APPROVED 상태 엔티티
  - `rejectedEntity()`: REJECTED 상태 엔티티
  - `pendingEntityWithRegistrationNumber(String)`: 사업자등록번호 지정
  - `pendingEntityWithName(String sellerName, String companyName)`: 이름 지정

### 6.3 인증 컨텍스트 Fixture

```java
// SUPER_ADMIN 권한 생성
public static Authentication createSuperAdminAuth() {
    Map<String, Object> claims = Map.of(
        "roles", List.of("SUPER_ADMIN"),
        "permissions", List.of(
            "seller-application:read",
            "seller-application:approve",
            "seller-application:reject"
        ),
        "organizationId", "org-super-admin"
    );
    return createAuth(claims);
}

// 일반 사용자 (SELLER_ADMIN) 권한 생성
public static Authentication createSellerAdminAuth() {
    Map<String, Object> claims = Map.of(
        "roles", List.of("SELLER_ADMIN"),
        "permissions", List.of(
            "seller-application:write"
        ),
        "organizationId", "org-seller-123"
    );
    return createAuth(claims);
}

// 인증된 사용자 (최소 권한)
public static Authentication createAuthenticatedUser() {
    Map<String, Object> claims = Map.of(
        "roles", List.of("USER"),
        "permissions", List.of(),
        "organizationId", "org-user-456"
    );
    return createAuth(claims);
}
```

### 6.4 setUp() 메서드 설계

```java
@BeforeEach
void setUp() {
    // 모든 관련 테이블 초기화
    sellerApplicationJpaRepository.deleteAll();
    sellerJpaRepository.deleteAll();
    sellerBusinessInfoJpaRepository.deleteAll();
    sellerCsJpaRepository.deleteAll();
    sellerContractJpaRepository.deleteAll();
    sellerSettlementJpaRepository.deleteAll();
    sellerAuthOutboxJpaRepository.deleteAll();

    // 인증 컨텍스트 초기화
    SecurityContextHolder.clearContext();
}
```

### 6.5 사전 데이터 설정 예시

```java
// 목록 조회 테스트용 (SUPER_ADMIN)
@Test
void 목록_조회_정상_SUPER_ADMIN() {
    // Given: SUPER_ADMIN 권한 설정
    Authentication superAdminAuth = AuthFixtures.createSuperAdminAuth();
    SecurityContextHolder.getContext().setAuthentication(superAdminAuth);

    // Given: PENDING 2건, APPROVED 2건, REJECTED 1건
    SellerApplicationJpaEntity pending1 =
        SellerApplicationJpaEntityFixtures.pendingEntityWithName("셀러A", "회사A");
    SellerApplicationJpaEntity pending2 =
        SellerApplicationJpaEntityFixtures.pendingEntityWithName("셀러B", "회사B");
    SellerApplicationJpaEntity approved1 =
        SellerApplicationJpaEntityFixtures.approvedEntity(200L);
    // ... 저장

    // When: GET /seller-applications
    // Then: content.size = 5
}
```

```java
// 승인 테스트용 (SUPER_ADMIN)
@Test
void 승인_정상_SUPER_ADMIN() {
    // Given: SUPER_ADMIN 권한 설정
    Authentication superAdminAuth = AuthFixtures.createSuperAdminAuth();
    SecurityContextHolder.getContext().setAuthentication(superAdminAuth);

    // Given: PENDING 신청 1건 (중복 없는 셀러명/사업자등록번호)
    SellerApplicationJpaEntity pending =
        SellerApplicationJpaEntityFixtures.pendingEntity();
    SellerApplicationJpaEntity saved =
        sellerApplicationJpaRepository.save(pending);
    Long applicationId = saved.getId();

    // When: POST /seller-applications/{id}/approve
    // Then: sellerId 반환, seller 관련 테이블 7개 검증
}
```

---

## 7. 테스트 우선순위

### P0 (필수 시나리오) - 31개

**Query (인증/인가 포함)**:
1. 목록 조회 - 정상 (SUPER_ADMIN)
2. 목록 조회 - 401 (토큰 없음)
3. 목록 조회 - 403 (일반 사용자)
4. 목록 조회 - 빈 결과 (SUPER_ADMIN)
5. 상세 조회 - 정상 PENDING (SUPER_ADMIN)
6. 상세 조회 - 401 (토큰 없음)
7. 상세 조회 - 403 (일반 사용자)
8. 상세 조회 - 정상 APPROVED (SUPER_ADMIN)
9. 상세 조회 - 정상 REJECTED (SUPER_ADMIN)
10. 상세 조회 - 존재하지 않는 ID (404)

**Command (인증/인가 포함)**:
11. 신청 생성 - 정상 (최소 필드, AUTHENTICATED)
12. 신청 생성 - 401 (토큰 없음)
13. 신청 생성 - 정상 (모든 필드, AUTHENTICATED)
14. 신청 생성 - 필수 필드 누락 (sellerName)
15. 신청 생성 - 필수 필드 누락 (registrationNumber)
16. 신청 생성 - 이메일 형식 오류
17. 신청 생성 - 계좌번호 숫자 외 입력
18. 신청 생성 - 중복 신청 (409)
19. 승인 - 정상 (Seller 생성 확인, SUPER_ADMIN)
20. 승인 - 401 (토큰 없음)
21. 승인 - 403 (일반 사용자)
22. 승인 - 존재하지 않는 신청 (404)
23. 승인 - 이미 처리된 신청 (400)
24. 거절 - 정상 (SUPER_ADMIN)
25. 거절 - 401 (토큰 없음)
26. 거절 - 403 (일반 사용자)
27. 거절 - 거절 사유 누락 (400)
28. 거절 - 존재하지 않는 신청 (404)

**통합 플로우**:
29. CRUD 전체 플로우 (인증 포함)
30. 신청 → 거절 플로우 (인증 포함)
31. 상태 전이 플로우 (PENDING만 처리 가능)

### P1 (중요 시나리오) - 16개

**Query**:
1. 페이징 동작 확인 (SUPER_ADMIN)
2. 상태 필터링 (SUPER_ADMIN)
3. 복수 상태 필터링 (SUPER_ADMIN)
4. 셀러명 검색 (SUPER_ADMIN)
5. 회사명 검색 (SUPER_ADMIN)
6. 상세 조회 - 모든 필드 완전성 검증 (SUPER_ADMIN)

**Command**:
7. 신청 생성 - 정산 주기 잘못된 값
8. 신청 생성 - 정산일 범위 초과
9. 신청 생성 - 중복 신청 허용 (APPROVED 상태)
10. 승인 - 셀러명 중복 검증 (409)
11. 승인 - 사업자등록번호 중복 검증 (409)
12. 승인 - 트랜잭션 롤백 확인
13. 거절 - 이미 승인된 신청 거절 시도 (400)
14. 거절 - 이미 거절된 신청 재거절 시도 (400)

**통합 플로우**:
15. 목록 조회 → 상세 조회 연계 (SUPER_ADMIN)
16. 중복 신청 → 승인 → 재신청 플로우

### P2 (권장 시나리오) - 9개

**Query**:
1. 정렬 (sortKey, sortDirection)
2. 복합 필터 (상태 + 검색)
3. 페이징 범위 초과
4. 상세 조회 - 잘못된 ID 형식 (음수)

**Command**:
5. 신청 생성 - 필드 길이 초과
6. 신청 생성 - JSON 형식 오류
7. 거절 - 거절 사유 길이 초과

**통합 플로우**:
8. 페이징 + 상세 조회 플로우 (SUPER_ADMIN)
9. 승인 → Seller 관련 엔티티 검증 플로우 (SUPER_ADMIN)

---

## 총 시나리오 개수: 56개

- **P0 (필수)**: 31개 (인증/인가 시나리오 11개 추가)
- **P1 (중요)**: 16개 (거절 관련 인증 시나리오 2개 추가)
- **P2 (권장)**: 9개

---

## 인증/인가 테스트 추가 요약

### 추가된 시나리오 (13개)
1. 목록 조회 - 401 (토큰 없음)
2. 목록 조회 - 403 (일반 사용자)
3. 상세 조회 - 401 (토큰 없음)
4. 상세 조회 - 403 (일반 사용자)
5. 신청 생성 - 401 (토큰 없음)
6. 승인 - 401 (토큰 없음)
7. 승인 - 403 (일반 사용자)
8. 거절 - 401 (토큰 없음)
9. 거절 - 403 (일반 사용자)
10. CRUD 전체 플로우 - 권한 검증 단계 추가
11. 신청 → 거절 플로우 - 권한 검증 단계 추가

### 기존 시나리오 업데이트
- 모든 성공 시나리오에 인증 컨텍스트 명시 ([SUPER_ADMIN], [AUTHENTICATED])
- Request 예시에 `Authorization: Bearer {token}` 헤더 추가

---

## 다음 단계

이 시나리오 설계 문서를 기반으로 다음 작업을 진행합니다:

1. **E2E 테스트 구현**:
   - `integration-test/src/test/java/.../sellerapplication/SellerApplicationE2ETest.java`
   - P0 시나리오부터 우선 구현 (인증/인가 포함)

2. **RestDocs 통합**:
   - API 문서 자동 생성을 위한 snippet 작성
   - request-fields, response-fields, path-parameters, request-headers 등

3. **CI/CD 통합**:
   - 테스트 자동화 파이프라인 구성
   - Coverage 목표: P0 시나리오 100% 통과

---

**작성 일시**: 2026-02-06 (인증/인가 추가: 2026-02-06)
**대상 도메인**: SellerApplication (셀러 입점 신청)
**테스트 레벨**: E2E 통합 테스트 (인증/인가 포함)
