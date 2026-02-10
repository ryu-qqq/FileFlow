# Seller E2E 통합 테스트 시나리오 (인증/인가 포함)

**분석 일시**: 2026-02-06 (인증/인가 시나리오 추가: 2026-02-06)
**대상 도메인**: Seller (셀러)
**Base Path**: `/api/v1/market/admin/sellers`

---

## 0. 인증/인가 개요

### 0.1 @PreAuthorize 매핑

| 엔드포인트 | @PreAuthorize | 설명 |
|-----------|---------------|------|
| GET /sellers/{sellerId} | `@access.isSellerOwnerOr(#sellerId, 'seller:read')` | 셀러 상세 조회 (본인 or 권한) |
| GET /sellers | `@access.superAdmin()` | 셀러 목록 조회 (슈퍼어드민 전용) |
| POST /sellers | `@access.superAdmin()` | 셀러 등록 (슈퍼어드민 전용) |
| PUT /sellers/{sellerId} | `@access.superAdmin()` | 셀러 전체 수정 (슈퍼어드민 전용) |
| PATCH /sellers/{sellerId} | `@access.isSellerOwnerOr(#sellerId, 'seller:write')` | 셀러 기본정보 수정 (본인 or 권한) |

### 0.2 MarketAccessChecker 메서드

- **superAdmin()**: SUPER_ADMIN 역할 확인
- **authenticated()**: 인증된 사용자 확인
- **isSellerOwnerOr(long sellerId, String permission)**:
  1. superAdmin이면 true (바이패스)
  2. organizationId로 sellerId 조회
  3. URL의 sellerId와 매칭되면 true (본인)
  4. 아니면 permission 체크 fallback

### 0.3 인증 컨텍스트 용어

- **SUPER_ADMIN 토큰**: 슈퍼어드민 역할 (모든 리소스 접근 가능)
- **셀러 본인 토큰**: organizationId로 매칭된 셀러 (sellerId=1)
- **다른 셀러 토큰**: organizationId로 매칭된 다른 셀러 (sellerId=2)
- **일반 사용자 토큰**: 셀러가 아닌 인증된 사용자 (권한 없음)
- **토큰 없음**: 인증되지 않은 요청

---

## 1. 입력 분석 결과

### 1.1 엔드포인트 목록

| HTTP Method | Path | 분류 | 설명 | 권한 요구 |
|-------------|------|------|------|----------|
| GET | `/sellers/{sellerId}` | Query | 셀러 상세 조회 | 본인 or seller:read |
| GET | `/sellers` | Query | 셀러 목록 검색 | SUPER_ADMIN |
| POST | `/sellers` | Command | 셀러 등록 | SUPER_ADMIN |
| PUT | `/sellers/{sellerId}` | Command | 셀러 전체정보 수정 | SUPER_ADMIN |
| PATCH | `/sellers/{sellerId}` | Command | 셀러 기본정보 수정 | 본인 or seller:write |

**총 5개** (Query 2개, Command 3개)

### 1.2 Validation 규칙 분석

#### RegisterSellerApiRequest (POST /sellers)
- **필수 필드**: seller, businessInfo
- **seller**: sellerName, displayName, logoUrl, description (모두 @NotBlank)
- **businessInfo**: registrationNumber, companyName, representative, saleReportNumber, businessAddress, csContact (모두 필수)
- **중첩 검증**: AddressRequest (zipCode, line1, line2), CsContactRequest (phone, email, mobile)

#### UpdateSellerApiRequest (PATCH /sellers/{sellerId})
- **필수 필드**: sellerName, displayName, logoUrl, description
- **선택 필드**: address, csInfo, businessInfo (null 허용)
- **선택 필드 제공 시**: 내부 필드는 필수 (@NotBlank)

#### SearchSellersApiRequest (GET /sellers)
- **선택 필드**: active, searchField, searchWord, sortKey, sortDirection, page, size
- **page**: @Min(0)
- **size**: @Min(1), @Max(100)

### 1.3 Domain 규칙

#### SellerName
- **길이**: 1~100자
- **필수**: null/blank 불가
- **자동 trim**: 공백 제거

#### RegistrationNumber
- **형식**: XXX-XX-XXXXX (10자리 숫자)
- **자동 변환**: 숫자만 입력 시 자동으로 하이픈 추가

#### Business Rules
- **SEL-006**: 셀러명 중복 → 409 Conflict
- **SEL-103**: 사업자등록번호 중복 → 409 Conflict
- **SEL-001**: 셀러 미존재 → 404 Not Found
- **SEL-003**: 비활성화 셀러 → 400 Bad Request

---

## 2. Query 엔드포인트 시나리오

### 2.1 GET /sellers/{sellerId} - 셀러 상세 조회

#### 성공 시나리오

##### TC-Q1-S01: 존재하는 셀러 ID로 상세 조회 성공 (SUPER_ADMIN)
- **Priority**: P0
- **Pre-Data**:
  - Seller 1건 저장 (id=1, sellerName="테스트셀러", active=true)
  - SellerBusinessInfo 1건 저장 (sellerId=1, registrationNumber="123-45-67890")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers/1`
- **Expected**:
  - HTTP Status: 200 OK
  - Response Body:
    - `seller.id = 1`
    - `seller.sellerName = "테스트셀러"`
    - `businessInfo.registrationNumber = "123-45-67890"`
    - `createdAt`, `updatedAt` ISO8601 형식
- **DB Verify**: 조회 후 DB 변경 없음 (readOnly)

##### TC-Q1-S02: 모든 관련 정보가 있는 셀러 조회 (셀러 본인)
- **Priority**: P0
- **Pre-Data**:
  - Seller + BusinessInfo + SellerCs + SellerContract + SellerSettlement 모두 저장
  - organizationId="org-123" → sellerId=1 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123")
- **Request**: `GET /sellers/1`
- **Expected**:
  - HTTP Status: 200 OK
  - Response Body: 5개 섹션 모두 데이터 포함
    - `seller`, `businessInfo`, `csInfo`, `contractInfo`, `settlementInfo`
- **Verify**: JOIN 쿼리 1번으로 모든 데이터 조회 (N+1 방지)

##### TC-Q1-S03: seller:read 권한으로 다른 셀러 조회 성공
- **Priority**: P0
- **Pre-Data**:
  - Seller 2건 (id=1, id=2)
  - organizationId="org-456" → sellerId=2 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-456") + seller:read 권한 보유
- **Request**: `GET /sellers/1` (다른 셀러)
- **Expected**:
  - HTTP Status: 200 OK
  - seller:read 권한으로 다른 셀러 조회 허용

#### 인증/인가 실패 시나리오

##### TC-Q1-A01: 토큰 없이 요청 시 401
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: 토큰 없음
- **Request**: `GET /sellers/1`
- **Expected**:
  - HTTP Status: 401 Unauthorized
  - Error Message: "인증이 필요합니다"

##### TC-Q1-A02: 다른 셀러가 권한 없이 접근 시 403
- **Priority**: P0
- **Pre-Data**:
  - Seller 2건 (id=1, id=2)
  - organizationId="org-456" → sellerId=2 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-456", seller:read 권한 없음)
- **Request**: `GET /sellers/1` (다른 셀러)
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"

##### TC-Q1-A03: SUPER_ADMIN은 모든 셀러 조회 가능
- **Priority**: P0
- **Pre-Data**: Seller 2건 (id=1, id=2)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers/1`, `GET /sellers/2`
- **Expected**:
  - HTTP Status: 200 OK (둘 다)
  - SUPER_ADMIN은 모든 리소스 접근 허용

#### 실패 시나리오 (비즈니스)

##### TC-Q1-F01: 존재하지 않는 셀러 ID 조회 시 404
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers/99999`
- **Expected**:
  - HTTP Status: 404 Not Found
  - Error Code: `SEL-001`
  - Error Message: "셀러를 찾을 수 없습니다"

##### TC-Q1-F02: 잘못된 타입의 sellerId 전달 시 400
- **Priority**: P1
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers/invalid`
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Spring 기본 타입 변환 오류

#### 엣지 케이스

##### TC-Q1-E01: 소프트 삭제된 셀러 조회 시 404
- **Priority**: P1
- **Pre-Data**:
  - Seller 1건 저장 후 deleted_at 설정 (소프트 삭제)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers/1`
- **Expected**:
  - HTTP Status: 404 Not Found
  - 소프트 삭제 데이터는 조회 불가

##### TC-Q1-E02: 연관 정보 일부만 있는 셀러 조회
- **Priority**: P1
- **Pre-Data**:
  - Seller + BusinessInfo만 저장 (CS, Contract, Settlement 없음)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers/1`
- **Expected**:
  - HTTP Status: 200 OK
  - `csInfo`, `contractInfo`, `settlementInfo`는 null

---

### 2.2 GET /sellers - 셀러 목록 검색

#### 성공 시나리오

##### TC-Q2-S01: 기본 조회 (SUPER_ADMIN)
- **Priority**: P0
- **Pre-Data**: Seller 5건 저장
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `page = 0`
    - `size = 20` (기본값)
    - `totalElements = 5`
    - `items.length = 5`
    - `sortKey = createdAt`, `sortDirection = DESC` (기본값)

##### TC-Q2-S02: 페이징 동작 확인 (page=0, size=2)
- **Priority**: P0
- **Pre-Data**: Seller 5건 저장
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?page=0&size=2`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `page = 0`
    - `size = 2`
    - `totalElements = 5`
    - `totalPages = 3`
    - `items.length = 2`

##### TC-Q2-S03: active 필터 (active=true)
- **Priority**: P0
- **Pre-Data**:
  - Seller 3건 (active=true)
  - Seller 2건 (active=false)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?active=true`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `totalElements = 3`
    - 모든 items의 `active = true`

##### TC-Q2-S04: sellerName 검색 (searchField=sellerName, searchWord=테스트)
- **Priority**: P1
- **Pre-Data**:
  - Seller 2건 (sellerName="테스트셀러1", "테스트셀러2")
  - Seller 1건 (sellerName="다른셀러")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?searchField=sellerName&searchWord=테스트`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `totalElements = 2`
    - LIKE 검색으로 "테스트" 포함된 셀러만 조회

##### TC-Q2-S05: 정렬 동작 확인 (sortKey=sellerName, sortDirection=ASC)
- **Priority**: P1
- **Pre-Data**: Seller 3건 (sellerName="C", "A", "B")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?sortKey=sellerName&sortDirection=ASC`
- **Expected**:
  - HTTP Status: 200 OK
  - Response: items 순서가 ["A", "B", "C"]

##### TC-Q2-S06: 복합 필터 (active + 검색 + 페이징)
- **Priority**: P1
- **Pre-Data**:
  - Seller 3건 (active=true, sellerName="테스트*")
  - Seller 2건 (active=false, sellerName="테스트*")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?active=true&searchField=sellerName&searchWord=테스트&page=0&size=2`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `totalElements = 3`
    - `items.length = 2`

#### 인증/인가 실패 시나리오

##### TC-Q2-A01: 토큰 없이 요청 시 401
- **Priority**: P0
- **Pre-Data**: Seller 5건
- **Auth Context**: 토큰 없음
- **Request**: `GET /sellers`
- **Expected**:
  - HTTP Status: 401 Unauthorized
  - Error Message: "인증이 필요합니다"

##### TC-Q2-A02: SUPER_ADMIN 아닌 사용자 접근 시 403
- **Priority**: P0
- **Pre-Data**: Seller 5건
- **Auth Context**: 셀러 본인 토큰 (SUPER_ADMIN 아님)
- **Request**: `GET /sellers`
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"

##### TC-Q2-A03: 일반 사용자 접근 시 403
- **Priority**: P0
- **Pre-Data**: Seller 5건
- **Auth Context**: 일반 사용자 토큰 (seller:read 권한 있음)
- **Request**: `GET /sellers`
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"
  - **Note**: seller:read 권한으로도 목록 조회 불가 (SUPER_ADMIN 전용)

#### 실패 시나리오 (Validation)

##### TC-Q2-F01: 잘못된 page 값 (page=-1)
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?page=-1`
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "페이지 번호는 0 이상이어야 합니다."

##### TC-Q2-F02: 잘못된 size 값 (size=0)
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?size=0`
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "페이지 크기는 1 이상이어야 합니다."

##### TC-Q2-F03: 잘못된 size 값 (size=101)
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?size=101`
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "페이지 크기는 100 이하여야 합니다."

#### 엣지 케이스

##### TC-Q2-E01: 데이터 없을 때 빈 목록 반환
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `totalElements = 0`
    - `items = []`

##### TC-Q2-E02: 존재하지 않는 페이지 요청 (page=100)
- **Priority**: P1
- **Pre-Data**: Seller 5건
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers?page=100&size=20`
- **Expected**:
  - HTTP Status: 200 OK
  - Response:
    - `totalElements = 5`
    - `items = []` (빈 배열)

##### TC-Q2-E03: 소프트 삭제 데이터 제외 확인
- **Priority**: P1
- **Pre-Data**:
  - Seller 3건 (deleted_at = null)
  - Seller 2건 (deleted_at != null, 소프트 삭제)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `GET /sellers`
- **Expected**:
  - HTTP Status: 200 OK
  - Response: `totalElements = 3` (삭제된 데이터 제외)

---

## 3. Command 엔드포인트 시나리오

### 3.1 POST /sellers - 셀러 등록

#### 성공 시나리오

##### TC-C1-S01: 유효한 요청으로 셀러 등록 성공 (SUPER_ADMIN)
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers`
  ```json
  {
    "seller": {
      "sellerName": "테스트셀러",
      "displayName": "테스트 브랜드",
      "logoUrl": "https://example.com/logo.png",
      "description": "테스트 셀러 설명"
    },
    "businessInfo": {
      "registrationNumber": "123-45-67890",
      "companyName": "테스트컴퍼니",
      "representative": "홍길동",
      "saleReportNumber": "제2025-서울강남-1234호",
      "businessAddress": {
        "zipCode": "12345",
        "line1": "서울시 강남구",
        "line2": "테헤란로 123"
      },
      "csContact": {
        "phone": "02-1234-5678",
        "email": "cs@example.com",
        "mobile": "010-1234-5678"
      }
    }
  }
  ```
- **Expected**:
  - HTTP Status: 201 Created
  - Response Body: `{ "sellerId": 1 }`
- **DB Verify**:
  - `SELECT * FROM seller WHERE id = 1` → 존재
  - `SELECT * FROM seller_business_info WHERE seller_id = 1` → 존재
  - Seller와 BusinessInfo가 트랜잭션으로 원자적 저장

##### TC-C1-S02: 사업자등록번호 자동 포맷팅 (숫자만 입력)
- **Priority**: P1
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (registrationNumber="1234567890")
- **Expected**:
  - HTTP Status: 201 Created
- **DB Verify**:
  - `businessInfo.registrationNumber = "123-45-67890"` (자동 하이픈 추가)

#### 인증/인가 실패 시나리오

##### TC-C1-A01: 토큰 없이 요청 시 401
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: 토큰 없음
- **Request**: `POST /sellers` (유효한 body)
- **Expected**:
  - HTTP Status: 401 Unauthorized
  - Error Message: "인증이 필요합니다"

##### TC-C1-A02: SUPER_ADMIN 아닌 사용자 접근 시 403
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: 셀러 본인 토큰 (SUPER_ADMIN 아님)
- **Request**: `POST /sellers` (유효한 body)
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"

##### TC-C1-A03: seller:write 권한만으로는 등록 불가
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: 일반 사용자 토큰 (seller:write 권한 있음)
- **Request**: `POST /sellers` (유효한 body)
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"
  - **Note**: seller:write 권한으로도 등록 불가 (SUPER_ADMIN 전용)

#### 실패 시나리오 (Validation)

##### TC-C1-F01: seller 필드 누락 (필수 필드 오류)
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (seller=null)
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "셀러 기본 정보는 필수입니다"

##### TC-C1-F02: sellerName 필드 누락
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (seller.sellerName="")
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "셀러명은 필수입니다"

##### TC-C1-F03: businessInfo 필드 누락
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (businessInfo=null)
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "사업자 정보는 필수입니다"

##### TC-C1-F04: registrationNumber 필드 누락
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (businessInfo.registrationNumber="")
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "사업자등록번호는 필수입니다"

##### TC-C1-F05: businessAddress 필드 누락
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (businessInfo.businessAddress=null)
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "사업장 주소는 필수입니다"

##### TC-C1-F06: csContact 필드 누락
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (businessInfo.csContact=null)
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "CS 연락처는 필수입니다"

#### 실패 시나리오 (Business Rule)

##### TC-C1-F07: 셀러명 중복 시 409
- **Priority**: P0
- **Pre-Data**: Seller 1건 (sellerName="테스트셀러")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (sellerName="테스트셀러")
- **Expected**:
  - HTTP Status: 409 Conflict
  - Error Code: `SEL-006`
  - Error Message: "이미 존재하는 셀러명입니다"

##### TC-C1-F08: 사업자등록번호 중복 시 409
- **Priority**: P0
- **Pre-Data**: SellerBusinessInfo 1건 (registrationNumber="123-45-67890")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (registrationNumber="123-45-67890")
- **Expected**:
  - HTTP Status: 409 Conflict
  - Error Code: `SEL-103`
  - Error Message: "이미 등록된 사업자등록번호입니다"

##### TC-C1-F09: 잘못된 사업자등록번호 형식
- **Priority**: P1
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (registrationNumber="12345")
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Error Message: "유효하지 않은 사업자등록번호 형식입니다"

#### 엣지 케이스

##### TC-C1-E01: 셀러명 공백 제거 (trim)
- **Priority**: P1
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `POST /sellers` (sellerName="  테스트셀러  ")
- **Expected**:
  - HTTP Status: 201 Created
- **DB Verify**: `seller.sellerName = "테스트셀러"` (공백 제거)

##### TC-C1-E02: 트랜잭션 롤백 확인 (BusinessInfo 저장 실패 시)
- **Priority**: P1
- **Auth Context**: SUPER_ADMIN 토큰
- **Scenario**: Seller는 저장되고 BusinessInfo 저장 시 예외 발생
- **Expected**:
  - Seller도 롤백되어 DB에 저장되지 않음
  - 트랜잭션 원자성 보장

---

### 3.2 PUT /sellers/{sellerId} - 셀러 전체정보 수정

#### 성공 시나리오

##### TC-C2-S01: 모든 정보 수정 성공 (SUPER_ADMIN)
- **Priority**: P0
- **Pre-Data**:
  - Seller + BusinessInfo + CS + Contract + Settlement 저장
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PUT /sellers/1`
  ```json
  {
    "seller": {
      "sellerName": "수정된셀러",
      "displayName": "수정된 브랜드",
      "logoUrl": "https://example.com/new-logo.png",
      "description": "수정된 설명"
    },
    "businessInfo": {
      "registrationNumber": "999-99-99999",
      "companyName": "수정컴퍼니",
      "representative": "김철수",
      "saleReportNumber": "제2026-서울강남-5678호",
      "businessAddress": {
        "zipCode": "54321",
        "line1": "서울시 서초구",
        "line2": "강남대로 456"
      }
    },
    "csInfo": {
      "phone": "02-9999-9999",
      "email": "new-cs@example.com",
      "mobile": "010-9999-9999",
      "operatingStartTime": "10:00",
      "operatingEndTime": "19:00",
      "operatingDays": "MON,TUE,WED,THU,FRI,SAT",
      "kakaoChannelUrl": "https://pf.kakao.com/new"
    },
    "contractInfo": {
      "commissionRate": 15.0,
      "contractStartDate": "2026-01-01",
      "contractEndDate": "2026-12-31",
      "specialTerms": "신규 계약 조건"
    },
    "settlementInfo": {
      "bankAccount": {
        "bankCode": "088",
        "bankName": "신한은행",
        "accountNumber": "999-999-999",
        "accountHolderName": "수정컴퍼니"
      },
      "settlementCycle": "WEEKLY",
      "settlementDay": 5
    }
  }
  ```
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - 모든 테이블의 데이터가 수정됨
  - `updated_at` 타임스탬프 갱신

#### 인증/인가 실패 시나리오

##### TC-C2-A01: 토큰 없이 요청 시 401
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: 토큰 없음
- **Request**: `PUT /sellers/1` (유효한 body)
- **Expected**:
  - HTTP Status: 401 Unauthorized
  - Error Message: "인증이 필요합니다"

##### TC-C2-A02: SUPER_ADMIN 아닌 사용자 접근 시 403
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123" → sellerId=1)
- **Request**: `PUT /sellers/1` (유효한 body)
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"
  - **Note**: 본인 셀러라도 PUT은 SUPER_ADMIN 전용

##### TC-C2-A03: seller:write 권한만으로는 수정 불가
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: 일반 사용자 토큰 (seller:write 권한 있음)
- **Request**: `PUT /sellers/1` (유효한 body)
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"

#### 실패 시나리오 (비즈니스)

##### TC-C2-F01: 존재하지 않는 셀러 ID 수정 시 404
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PUT /sellers/99999`
- **Expected**:
  - HTTP Status: 404 Not Found
  - Error Code: `SEL-001`

##### TC-C2-F02: 필수 필드 누락 (seller=null)
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PUT /sellers/1` (seller=null)
- **Expected**:
  - HTTP Status: 400 Bad Request

##### TC-C2-F03: 다른 셀러의 셀러명으로 수정 시 409
- **Priority**: P1
- **Pre-Data**:
  - Seller 2건 (id=1, sellerName="셀러A"), (id=2, sellerName="셀러B")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PUT /sellers/1` (sellerName="셀러B")
- **Expected**:
  - HTTP Status: 409 Conflict
  - Error Code: `SEL-006`

##### TC-C2-F04: 다른 셀러의 사업자등록번호로 수정 시 409
- **Priority**: P1
- **Pre-Data**:
  - Seller 2건 (각각 다른 사업자등록번호)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PUT /sellers/1` (다른 셀러의 registrationNumber)
- **Expected**:
  - HTTP Status: 409 Conflict
  - Error Code: `SEL-103`

#### 엣지 케이스

##### TC-C2-E01: 자기 자신의 셀러명/사업자등록번호로 수정 (중복 체크 제외)
- **Priority**: P1
- **Pre-Data**: Seller 1건 (sellerName="테스트셀러", registrationNumber="123-45-67890")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PUT /sellers/1` (동일한 sellerName, registrationNumber)
- **Expected**:
  - HTTP Status: 204 No Content
  - 중복 검사에서 자기 자신 제외

##### TC-C2-E02: 트랜잭션 롤백 확인 (5개 Aggregate 중 하나 실패 시)
- **Priority**: P1
- **Auth Context**: SUPER_ADMIN 토큰
- **Scenario**: Seller, BusinessInfo는 성공, SellerCs 저장 시 예외 발생
- **Expected**:
  - 모든 변경 사항 롤백
  - 트랜잭션 원자성 보장

---

### 3.3 PATCH /sellers/{sellerId} - 셀러 기본정보 수정

#### 성공 시나리오

##### TC-C3-S01: 기본정보만 수정 (셀러 본인)
- **Priority**: P0
- **Pre-Data**:
  - Seller 1건 (id=1)
  - organizationId="org-123" → sellerId=1 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123")
- **Request**: `PATCH /sellers/1`
  ```json
  {
    "sellerName": "수정된셀러",
    "displayName": "수정된 브랜드",
    "logoUrl": "https://example.com/new-logo.png",
    "description": "수정된 설명"
  }
  ```
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - `seller` 테이블만 수정됨
  - `updated_at` 갱신

##### TC-C3-S02: 기본정보 + 주소 수정 (SUPER_ADMIN)
- **Priority**: P0
- **Pre-Data**: Seller 1건 + SellerAddress 1건
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PATCH /sellers/1` (address 포함)
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - `seller` + `seller_address` 수정

##### TC-C3-S03: 기본정보 + CS 정보 수정
- **Priority**: P0
- **Pre-Data**: Seller 1건 + SellerCs 1건
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123" → sellerId=1)
- **Request**: `PATCH /sellers/1` (csInfo 포함)
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - `seller` + `seller_cs` 수정

##### TC-C3-S04: 기본정보 + 사업자정보 수정
- **Priority**: P0
- **Pre-Data**: Seller 1건 + SellerBusinessInfo 1건
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123" → sellerId=1)
- **Request**: `PATCH /sellers/1` (businessInfo 포함)
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - `seller` + `seller_business_info` 수정

##### TC-C3-S05: 모든 선택 필드 포함 수정
- **Priority**: P1
- **Pre-Data**: Seller 1건 + 모든 관련 정보
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123" → sellerId=1)
- **Request**: `PATCH /sellers/1` (address, csInfo, businessInfo 모두 포함)
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - 4개 테이블 모두 수정

##### TC-C3-S06: seller:write 권한으로 다른 셀러 수정
- **Priority**: P0
- **Pre-Data**:
  - Seller 2건 (id=1, id=2)
  - organizationId="org-456" → sellerId=2 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-456", seller:write 권한 있음)
- **Request**: `PATCH /sellers/1` (다른 셀러)
- **Expected**:
  - HTTP Status: 204 No Content
  - seller:write 권한으로 다른 셀러 수정 허용

#### 인증/인가 실패 시나리오

##### TC-C3-A01: 토큰 없이 요청 시 401
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: 토큰 없음
- **Request**: `PATCH /sellers/1` (유효한 body)
- **Expected**:
  - HTTP Status: 401 Unauthorized
  - Error Message: "인증이 필요합니다"

##### TC-C3-A02: 다른 셀러가 권한 없이 수정 시 403
- **Priority**: P0
- **Pre-Data**:
  - Seller 2건 (id=1, id=2)
  - organizationId="org-456" → sellerId=2 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-456", seller:write 권한 없음)
- **Request**: `PATCH /sellers/1` (다른 셀러)
- **Expected**:
  - HTTP Status: 403 Forbidden
  - Error Message: "권한이 부족합니다"

##### TC-C3-A03: SUPER_ADMIN은 모든 셀러 수정 가능
- **Priority**: P0
- **Pre-Data**: Seller 2건 (id=1, id=2)
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PATCH /sellers/1`, `PATCH /sellers/2`
- **Expected**:
  - HTTP Status: 204 No Content (둘 다)
  - SUPER_ADMIN은 모든 리소스 수정 허용

#### 실패 시나리오 (비즈니스)

##### TC-C3-F01: 존재하지 않는 셀러 ID 수정 시 404
- **Priority**: P0
- **Pre-Data**: 없음
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PATCH /sellers/99999`
- **Expected**:
  - HTTP Status: 404 Not Found
  - Error Code: `SEL-001`

##### TC-C3-F02: 필수 필드 누락 (sellerName="")
- **Priority**: P0
- **Pre-Data**: Seller 1건
- **Auth Context**: 셀러 본인 토큰
- **Request**: `PATCH /sellers/1` (sellerName="")
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "셀러명은 필수입니다"

##### TC-C3-F03: csInfo 제공했지만 내부 필드 누락 (phone="")
- **Priority**: P1
- **Pre-Data**: Seller 1건
- **Auth Context**: 셀러 본인 토큰
- **Request**: `PATCH /sellers/1` (csInfo.phone="")
- **Expected**:
  - HTTP Status: 400 Bad Request
  - Validation Error: "CS 전화번호는 필수입니다"

##### TC-C3-F04: 다른 셀러의 셀러명으로 수정 시 409
- **Priority**: P1
- **Pre-Data**: Seller 2건 (sellerName="셀러A", "셀러B")
- **Auth Context**: SUPER_ADMIN 토큰
- **Request**: `PATCH /sellers/1` (sellerName="셀러B")
- **Expected**:
  - HTTP Status: 409 Conflict
  - Error Code: `SEL-006`

#### 엣지 케이스

##### TC-C3-E01: csInfo=null 제공 시 업데이트 스킵
- **Priority**: P1
- **Pre-Data**: Seller 1건 + SellerCs 1건 (phone="기존번호")
- **Auth Context**: 셀러 본인 토큰
- **Request**: `PATCH /sellers/1` (csInfo=null)
- **Expected**:
  - HTTP Status: 204 No Content
- **DB Verify**:
  - `seller_cs.phone = "기존번호"` (변경 없음)

##### TC-C3-E02: 존재하지 않는 SellerCs에 csInfo 제공 시 예외
- **Priority**: P1
- **Pre-Data**: Seller 1건 (SellerCs 없음)
- **Auth Context**: 셀러 본인 토큰
- **Request**: `PATCH /sellers/1` (csInfo 포함)
- **Expected**:
  - HTTP Status: 404 Not Found
  - Error Code: `SEL-300`
  - Error Message: "CS 정보를 찾을 수 없습니다"

---

## 4. 통합 시나리오 (여러 API 조합)

### 4.1 CRUD 전체 플로우

#### TC-FLOW-01: 생성 → 조회 → 수정 → 삭제 전체 플로우 (SUPER_ADMIN)
- **Priority**: P0
- **Auth Context**: SUPER_ADMIN 토큰
- **Steps**:
  1. **POST /sellers** → 셀러 생성 (201, sellerId 반환)
  2. **GET /sellers/{sellerId}** → 상세 조회 (200, 생성 데이터 확인)
  3. **GET /sellers** → 목록 조회 (200, totalElements=1)
  4. **PATCH /sellers/{sellerId}** → 기본정보 수정 (204)
  5. **GET /sellers/{sellerId}** → 수정 확인 (200, 변경된 데이터)
  6. **PUT /sellers/{sellerId}** → 전체정보 수정 (204)
  7. **GET /sellers/{sellerId}** → 수정 확인 (200, 변경된 데이터)
  8. *(소프트 삭제는 별도 API로 예정)*
- **DB Verify**:
  - 각 단계마다 DB 상태 확인
  - 트랜잭션 정합성 확인

#### TC-FLOW-02: 셀러 본인 플로우 (조회/수정)
- **Priority**: P0
- **Pre-Data**:
  - Seller 1건 생성 (SUPER_ADMIN)
  - organizationId="org-123" → sellerId=1 매핑
- **Auth Context**: 셀러 본인 토큰 (organizationId="org-123")
- **Steps**:
  1. **GET /sellers/{sellerId}** → 본인 셀러 조회 (200)
  2. **PATCH /sellers/{sellerId}** → 기본정보 수정 (204)
  3. **GET /sellers/{sellerId}** → 수정 확인 (200)
  4. **PUT /sellers/{sellerId}** → 전체 수정 시도 (403, SUPER_ADMIN 전용)
  5. **GET /sellers** → 목록 조회 시도 (403, SUPER_ADMIN 전용)
- **Expected**:
  - 본인 리소스는 조회/PATCH 가능
  - 목록 조회/PUT은 SUPER_ADMIN만 가능

### 4.2 목록 조회 → 상세 조회 플로우

#### TC-FLOW-03: 목록에서 ID 추출 → 상세 조회
- **Priority**: P1
- **Pre-Data**: Seller 3건 저장
- **Auth Context**: SUPER_ADMIN 토큰
- **Steps**:
  1. **GET /sellers** → 목록 조회
  2. Response에서 첫 번째 item의 id 추출
  3. **GET /sellers/{id}** → 상세 조회
- **Expected**:
  - 목록의 기본정보와 상세 정보 일치
  - 상세에만 있는 정보 (businessInfo 등) 추가 확인

### 4.3 검색 → 수정 → 재검색 플로우

#### TC-FLOW-04: 검색 → 수정 → 검색 결과 변경 확인
- **Priority**: P1
- **Pre-Data**: Seller 3건 (sellerName="테스트*")
- **Auth Context**: SUPER_ADMIN 토큰
- **Steps**:
  1. **GET /sellers?searchField=sellerName&searchWord=테스트** → 3건 조회
  2. 첫 번째 셀러의 sellerName을 "변경된셀러"로 수정
  3. **PATCH /sellers/{id}** → 수정 (204)
  4. **GET /sellers?searchField=sellerName&searchWord=테스트** → 2건 조회
  5. **GET /sellers?searchField=sellerName&searchWord=변경** → 1건 조회
- **Expected**:
  - 검색 결과가 수정에 따라 변경됨

### 4.4 동시성 시나리오

#### TC-FLOW-05: 동일 셀러명 중복 등록 경쟁 조건
- **Priority**: P2
- **Auth Context**: SUPER_ADMIN 토큰 (2개)
- **Scenario**:
  - 두 요청이 동시에 같은 sellerName으로 POST /sellers 호출
- **Expected**:
  - 하나는 201 Created, 다른 하나는 409 Conflict
  - DB에는 1건만 저장됨

#### TC-FLOW-06: 동시 수정 경쟁 조건 (Optimistic Lock)
- **Priority**: P2
- **Pre-Data**: Seller 1건
- **Auth Context**: SUPER_ADMIN 토큰 (2개)
- **Scenario**:
  - 두 요청이 동시에 PATCH /sellers/1 호출
- **Expected**:
  - 나중 요청이 먼저 요청의 변경 사항을 덮어씀
  - *(현재 Optimistic Lock 미구현 시 Last Write Wins)*

### 4.5 권한 전환 시나리오

#### TC-FLOW-07: 다양한 권한 레벨에서 동일 작업 수행
- **Priority**: P1
- **Pre-Data**: Seller 2건 (id=1, id=2), organizationId="org-123" → sellerId=1
- **Steps**:
  1. **토큰 없음** → GET /sellers/1 (401)
  2. **일반 사용자 토큰** → GET /sellers/1 (403)
  3. **셀러 본인 토큰 (id=1)** → GET /sellers/1 (200)
  4. **셀러 본인 토큰 (id=1)** → GET /sellers/2 (403, 다른 셀러)
  5. **seller:read 권한 + 셀러 토큰** → GET /sellers/2 (200, 권한으로 허용)
  6. **SUPER_ADMIN 토큰** → GET /sellers/1, GET /sellers/2 (200, 모두 허용)
- **Expected**:
  - 권한 레벨에 따라 접근 제어 정상 동작

---

## 5. Fixture 설계

### 5.1 필요 Repository 목록

- **SellerJpaRepository**
- **SellerBusinessInfoJpaRepository**
- **SellerCsJpaRepository**
- **SellerContractJpaRepository**
- **SellerSettlementJpaRepository**
- **SellerAddressJpaRepository** (선택)

### 5.2 testFixtures 활용

```java
// adapter-out/persistence-mysql/src/testFixtures/java/.../seller/

public class SellerJpaEntityFixtures {

    public static SellerJpaEntity activeSeller() {
        return SellerJpaEntity.builder()
            .id(1L)
            .sellerName("테스트셀러")
            .displayName("테스트 브랜드")
            .logoUrl("https://example.com/logo.png")
            .description("테스트 셀러 설명")
            .active(true)
            .build();
    }

    public static SellerJpaEntity inactiveSeller() {
        return activeSeller().toBuilder()
            .id(2L)
            .active(false)
            .build();
    }
}

public class SellerBusinessInfoJpaEntityFixtures {

    public static SellerBusinessInfoJpaEntity defaultBusinessInfo(Long sellerId) {
        return SellerBusinessInfoJpaEntity.builder()
            .sellerId(sellerId)
            .registrationNumber("123-45-67890")
            .companyName("테스트컴퍼니")
            .representative("홍길동")
            .saleReportNumber("제2025-서울강남-1234호")
            .businessZipcode("12345")
            .businessAddress("서울시 강남구")
            .businessAddressDetail("테헤란로 123")
            .build();
    }
}
```

### 5.3 setUp 패턴 (인증 Mock 포함)

```java
@SpringBootTest
@Transactional
class SellerIntegrationTest {

    @Autowired
    private SellerJpaRepository sellerRepository;

    @Autowired
    private SellerBusinessInfoJpaRepository businessInfoRepository;

    @MockBean
    private MarketAccessChecker accessChecker;

    @BeforeEach
    void setUp() {
        // 모든 Repository 초기화
        sellerRepository.deleteAll();
        businessInfoRepository.deleteAll();

        // 기본 Mock 설정 (SUPER_ADMIN 전역 허용)
        when(accessChecker.superAdmin()).thenReturn(true);
        when(accessChecker.isSellerOwnerOr(anyLong(), anyString())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        // 선택적 cleanup (Transaction 롤백으로 자동 처리됨)
    }

    // 공통 사전 데이터 생성 메서드
    private Long createSellerWithBusinessInfo() {
        var seller = sellerRepository.save(SellerJpaEntityFixtures.activeSeller());
        businessInfoRepository.save(
            SellerBusinessInfoJpaEntityFixtures.defaultBusinessInfo(seller.getId())
        );
        return seller.getId();
    }

    // 인증 Mock 설정 헬퍼
    private void mockSuperAdmin() {
        when(accessChecker.superAdmin()).thenReturn(true);
    }

    private void mockSellerOwner(Long sellerId) {
        when(accessChecker.superAdmin()).thenReturn(false);
        when(accessChecker.isSellerOwnerOr(eq(sellerId), anyString())).thenReturn(true);
        when(accessChecker.isSellerOwnerOr(not(eq(sellerId)), anyString())).thenReturn(false);
    }

    private void mockUnauthorized() {
        when(accessChecker.superAdmin()).thenReturn(false);
        when(accessChecker.isSellerOwnerOr(anyLong(), anyString())).thenReturn(false);
        when(accessChecker.authenticated()).thenReturn(false);
    }

    private void mockForbidden() {
        when(accessChecker.superAdmin()).thenReturn(false);
        when(accessChecker.isSellerOwnerOr(anyLong(), anyString())).thenReturn(false);
        when(accessChecker.authenticated()).thenReturn(true);
    }
}
```

---

## 6. 우선순위별 시나리오 요약

### P0 (필수 시나리오) - 48개

#### Query (18개)
- TC-Q1-S01~S03: 상세 조회 성공 (3개)
- TC-Q1-A01~A03: 상세 조회 인증/인가 (3개)
- TC-Q1-F01: 존재하지 않는 셀러 404
- TC-Q2-S01~S03: 목록 조회 성공 (3개)
- TC-Q2-A01~A03: 목록 조회 인증/인가 (3개)
- TC-Q2-F01~F03: Validation 오류 (3개)
- TC-Q2-E01: 빈 목록 반환

#### Command (27개)
- TC-C1-S01: 셀러 등록 성공
- TC-C1-A01~A03: 등록 인증/인가 (3개)
- TC-C1-F01~F06: 필수 필드 누락 (6개)
- TC-C1-F07~F08: 중복 409 (2개)
- TC-C2-S01: 전체정보 수정 성공
- TC-C2-A01~A03: 전체정보 수정 인증/인가 (3개)
- TC-C2-F01~F02: 전체정보 수정 실패 (2개)
- TC-C3-S01~S04: 기본정보 수정 성공 (4개, S06 포함)
- TC-C3-A01~A03: 기본정보 수정 인증/인가 (3개)
- TC-C3-F01~F02: 기본정보 수정 실패 (2개)

#### Flow (3개)
- TC-FLOW-01: CRUD 전체 플로우
- TC-FLOW-02: 셀러 본인 플로우
- TC-FLOW-07: 권한 전환 시나리오

**총 P0 시나리오**: 48개

### P1 (중요 시나리오) - 17개

- TC-Q1-F02: 잘못된 타입 sellerId
- TC-Q1-E01~E02: 엣지 케이스 (2개)
- TC-Q2-S04~S06: 검색/정렬/복합필터 (3개)
- TC-Q2-E02~E03: 엣지 케이스 (2개)
- TC-C1-S02: 사업자등록번호 자동 포맷팅
- TC-C1-F09: 잘못된 사업자등록번호 형식
- TC-C1-E01~E02: 엣지 케이스 (2개)
- TC-C2-F03~F04: 중복 수정 409 (2개)
- TC-C2-E01~E02: 엣지 케이스 (2개)
- TC-C3-S05: 모든 선택 필드 수정
- TC-C3-F03~F04: 실패 케이스 (2개)
- TC-C3-E01~E02: 엣지 케이스 (2개)
- TC-FLOW-03~04: 통합 플로우 (2개)

**총 P1 시나리오**: 23개

### P2 (선택 시나리오) - 2개

- TC-FLOW-05: 중복 등록 경쟁 조건
- TC-FLOW-06: 동시 수정 경쟁 조건

**총 P2 시나리오**: 2개

---

## 7. 총 시나리오 통계

| 분류 | 개수 |
|------|------|
| **Query 시나리오** | 24개 (성공 9, 인증/인가 6, 실패 6, 엣지 3) |
| **Command 시나리오** | 46개 (성공 12, 인증/인가 12, 실패 13, 엣지 9) |
| **통합 플로우 시나리오** | 7개 |
| **총 시나리오** | **77개** (기존 56개 + 인증/인가 21개) |
| **P0 (필수)** | 48개 (기존 28개 + 인증/인가 20개) |
| **P1 (중요)** | 23개 (기존 23개) |
| **P2 (선택)** | 2개 (기존 2개) |

---

## 8. 다음 단계

### 8.1 테스트 구현

```bash
# E2E 테스트 작성 시작
# integration-test/src/test/java/.../seller/

SellerQueryE2ETest.java          # Query 엔드포인트 24개 시나리오
SellerCommandE2ETest.java        # Command 엔드포인트 46개 시나리오
SellerFlowE2ETest.java           # 통합 플로우 7개 시나리오
SellerAuthE2ETest.java           # 인증/인가 전용 21개 시나리오
```

### 8.2 TestTags 활용

```java
@Tag("seller")
@Tag("e2e")
@Tag("query")  // or "command", "flow", "auth"
class SellerQueryE2ETest { }
```

### 8.3 테스트 실행

```bash
# 전체 실행
./gradlew :integration-test:test --tests "*SellerE2ETest"

# Priority별 실행
./gradlew :integration-test:test --tests "*SellerE2ETest" -Dgroups="P0"

# 특정 분류만 실행
./gradlew :integration-test:test --tests "SellerQueryE2ETest"
./gradlew :integration-test:test --tests "SellerAuthE2ETest"
```

### 8.4 인증/인가 테스트 주의사항

- **MarketAccessChecker Mock 필수**: @MockBean으로 권한 체크 시뮬레이션
- **SecurityContext 설정**: 필요 시 `@WithMockUser` 또는 직접 설정
- **organizationId 매핑**: ResolveSellerIdByOrganizationUseCase Mock 필요
- **권한 검증 순서**: 401 → 403 → 200 (인증 → 인가 → 성공)

---

**분석 완료**: 2026-02-06 (인증/인가 시나리오 추가 완료)
**다음 작업**: `/test-e2e seller` 명령으로 실제 테스트 코드 생성
