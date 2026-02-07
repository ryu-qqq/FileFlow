# ShippingPolicy E2E 통합 테스트 시나리오

## 1. 입력 분석

### 문서 로드
- API Endpoints: `.claude/docs/api-endpoints/shippingpolicy.md` ✅
- API Flow: `.claude/docs/api-flow/shippingpolicy.md` ✅

### 엔드포인트 분석
- **Query 엔드포인트**: 1개
  - GET /sellers/{sellerId}/shipping-policies (배송정책 목록 조회)
- **Command 엔드포인트**: 3개
  - POST /sellers/{sellerId}/shipping-policies (배송정책 등록)
  - PUT /sellers/{sellerId}/shipping-policies/{id} (배송정책 수정)
  - PATCH /sellers/{sellerId}/shipping-policies/{id}/status (배송정책 상태 변경)

### 인증/인가 규칙
| 엔드포인트 | @PreAuthorize | 설명 |
|-----------|---------------|------|
| GET /sellers/{sellerId}/shipping-policies | `@access.isSellerOwnerOr(#sellerId, 'shipping-policy:read')` | 셀러 본인 또는 shipping-policy:read 권한 |
| POST /sellers/{sellerId}/shipping-policies | `@access.isSellerOwnerOr(#sellerId, 'shipping-policy:write')` | 셀러 본인 또는 shipping-policy:write 권한 |
| PUT /sellers/{sellerId}/shipping-policies/{id} | `@access.isSellerOwnerOr(#sellerId, 'shipping-policy:write')` | 셀러 본인 또는 shipping-policy:write 권한 |
| PATCH /sellers/{sellerId}/shipping-policies/{id}/status | `@access.isSellerOwnerOr(#sellerId, 'shipping-policy:write')` | 셀러 본인 또는 shipping-policy:write 권한 |

### 비즈니스 규칙
| 규칙 ID | 규칙 내용 | 검증 위치 |
|---------|----------|----------|
| **POL-DEF-001** | 셀러당 기본 정책은 정확히 1개만 존재 | DefaultShippingPolicyResolver |
| **POL-DEF-002** | 기본 정책은 활성화 상태여야 함 | Domain (forNew) |
| **POL-DEACT-001** | 기본 정책은 비활성화 불가 | Domain (deactivate) |
| **POL-DEACT-002** | 마지막 활성 정책은 비활성화 불가 | ShippingPolicyValidator |
| **POL-FEE-001** | CONDITIONAL_FREE는 freeThreshold 필수 | Domain (validateFeeSettings) |

---

## 2. 시나리오 설계

### Query 시나리오 (17개)

#### GET /sellers/{sellerId}/shipping-policies - 배송정책 목록 조회

##### P0: 필수 시나리오 (9개)

**AUTH-Q1. 401 Unauthorized - 토큰 없이 요청**
- **Category**: 인증 실패
- **Pre-data**: 셀러(ID=1)의 배송정책 3건 저장
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies` (Authorization 헤더 없음)
- **Expected**:
  - 401 Unauthorized
  - 에러 메시지: "인증이 필요합니다" 또는 유사 메시지

**AUTH-Q2. 403 Forbidden - 다른 셀러의 정책 조회 시도**
- **Category**: 인가 실패
- **Pre-data**:
  - 셀러1(ID=1)의 정책 3건
  - 셀러2(ID=2)의 토큰 (organizationId로 sellerId=2 매핑)
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies` (셀러2의 토큰 사용)
- **Expected**:
  - 403 Forbidden
  - 셀러2는 셀러1의 리소스에 접근 불가

**AUTH-Q3. 200 OK - superAdmin 바이패스**
- **Category**: superAdmin 권한
- **Pre-data**: 셀러(ID=1)의 정책 3건
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies` (superAdmin 토큰)
- **Expected**:
  - 200 OK
  - content.size = 3
  - superAdmin은 모든 셀러 정책 조회 가능

**AUTH-Q4. 200 OK - 리소스 소유자 접근**
- **Category**: 리소스 소유자 검증
- **Pre-data**:
  - 셀러(ID=1)의 정책 3건
  - 셀러1의 토큰 (organizationId로 sellerId=1 매핑)
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies` (셀러1의 토큰)
- **Expected**:
  - 200 OK
  - content.size = 3
  - 본인 정책 조회 성공

**AUTH-Q5. 200 OK - shipping-policy:read 권한으로 접근**
- **Category**: 권한 기반 접근
- **Pre-data**: 셀러(ID=1)의 정책 3건
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies` (shipping-policy:read 권한 보유 토큰)
- **Expected**:
  - 200 OK
  - content.size = 3
  - 권한 보유 시 다른 셀러 정책도 조회 가능

**Q1-1. 정상 조회 - 데이터 존재 시**
- **Category**: 기본 조회
- **Pre-data**: 셀러(ID=1)의 배송정책 3건 저장
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies?page=0&size=20`
- **Expected**:
  - 200 OK
  - content.size = 3
  - totalElements = 3

**Q1-2. 빈 결과 - 데이터 없을 때**
- **Category**: 빈 결과
- **Pre-data**: 없음 (다른 셀러 데이터만 존재)
- **Authentication**: 셀러999의 토큰
- **Request**: `GET /api/v1/market/sellers/999/shipping-policies?page=0&size=20`
- **Expected**:
  - 200 OK
  - content.size = 0
  - totalElements = 0

**Q1-3. 기본 정책 존재 확인**
- **Category**: 기본 조회
- **Pre-data**: 셀러(ID=1)의 정책 3건 (그 중 1건이 기본 정책)
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies`
- **Expected**:
  - 200 OK
  - 응답에서 `defaultPolicy=true`인 항목이 정확히 1개 존재

**Q1-4. 활성/비활성 정책 모두 조회**
- **Category**: 기본 조회
- **Pre-data**: 활성 정책 2건 + 비활성 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies`
- **Expected**:
  - 200 OK
  - content.size = 3
  - active=true 2건, active=false 1건 확인

##### P1: 중요 시나리오 (8개)

**Q1-5. 삭제된 정책 제외**
- **Category**: Soft Delete 검증
- **Pre-data**: 정책 3건 (정상 2건 + 삭제 1건)
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies`
- **Expected**:
  - 200 OK
  - content.size = 2 (삭제된 정책 제외)

**Q1-6. 페이징 동작 확인**
- **Category**: 페이징
- **Pre-data**: 셀러(ID=1)의 정책 5건
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies?page=0&size=2`
- **Expected**:
  - 200 OK
  - content.size = 2
  - totalElements = 5
  - totalPages = 3

**Q1-7. 정렬 - 생성일 내림차순 (기본)**
- **Category**: 정렬
- **Pre-data**: 시간차를 두고 생성된 정책 3건
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies?sortKey=CREATED_AT&sortDirection=DESC`
- **Expected**:
  - 200 OK
  - 최근 생성 순서로 정렬 확인

**Q1-8. 정렬 - 정책명 오름차순**
- **Category**: 정렬
- **Pre-data**: 정책명이 다른 정책 3건 ("A정책", "B정책", "C정책")
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies?sortKey=POLICY_NAME&sortDirection=ASC`
- **Expected**:
  - 200 OK
  - 알파벳 순서로 정렬 확인

**Q1-9. 정렬 - 배송비 내림차순**
- **Category**: 정렬
- **Pre-data**: 배송비가 다른 정책 3건 (1000원, 3000원, 5000원)
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies?sortKey=BASE_FEE&sortDirection=DESC`
- **Expected**:
  - 200 OK
  - 5000 → 3000 → 1000 순서 확인

**Q1-10. 응답 필드 검증 - CONDITIONAL_FREE**
- **Category**: 응답 필드
- **Pre-data**: CONDITIONAL_FREE 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies`
- **Expected**:
  - 200 OK
  - shippingFeeType = "CONDITIONAL_FREE"
  - shippingFeeTypeDisplayName = "조건부 무료배송"
  - freeThreshold != null

**Q1-11. 응답 필드 검증 - FREE**
- **Category**: 응답 필드
- **Pre-data**: FREE 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies`
- **Expected**:
  - 200 OK
  - shippingFeeType = "FREE"
  - baseFee = 0
  - freeThreshold = null

**Q1-12. ISO-8601 날짜 형식 검증**
- **Category**: 응답 필드
- **Pre-data**: 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**: `GET /api/v1/market/sellers/1/shipping-policies`
- **Expected**:
  - 200 OK
  - createdAt이 ISO-8601 형식 (예: "2026-02-06T10:30:00Z")

---

### Command 시나리오 (38개)

#### POST /sellers/{sellerId}/shipping-policies - 배송정책 등록

##### P0: 필수 시나리오 (13개)

**AUTH-C1-1. 401 Unauthorized - 토큰 없이 등록 시도**
- **Category**: 인증 실패
- **Pre-data**: 없음
- **Request**: `POST /api/v1/market/sellers/1/shipping-policies` (Authorization 헤더 없음)
  ```json
  {
    "policyName": "기본 배송정책",
    "defaultPolicy": false,
    "shippingFeeType": "FREE"
  }
  ```
- **Expected**: 401 Unauthorized

**AUTH-C1-2. 403 Forbidden - 다른 셀러의 정책 등록 시도**
- **Category**: 인가 실패
- **Pre-data**: 셀러2의 토큰 (organizationId로 sellerId=2 매핑)
- **Request**: `POST /api/v1/market/sellers/1/shipping-policies` (셀러2의 토큰으로 셀러1에게 등록 시도)
  ```json
  {
    "policyName": "기본 배송정책",
    "defaultPolicy": false,
    "shippingFeeType": "FREE"
  }
  ```
- **Expected**: 403 Forbidden

**AUTH-C1-3. 201 Created - superAdmin 바이패스**
- **Category**: superAdmin 권한
- **Pre-data**: 없음
- **Request**: `POST /api/v1/market/sellers/1/shipping-policies` (superAdmin 토큰)
  ```json
  {
    "policyName": "기본 배송정책",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "baseFee": 0
  }
  ```
- **Expected**:
  - 201 CREATED
  - superAdmin은 모든 셀러에게 정책 등록 가능

**AUTH-C1-4. 201 Created - 리소스 소유자 등록**
- **Category**: 리소스 소유자 검증
- **Pre-data**: 셀러1의 토큰 (organizationId로 sellerId=1 매핑)
- **Request**: `POST /api/v1/market/sellers/1/shipping-policies` (셀러1의 토큰)
  ```json
  {
    "policyName": "본인 정책",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "baseFee": 0
  }
  ```
- **Expected**:
  - 201 CREATED
  - 본인 정책 등록 성공

**AUTH-C1-5. 201 Created - shipping-policy:write 권한으로 등록**
- **Category**: 권한 기반 접근
- **Pre-data**: shipping-policy:write 권한 보유 토큰
- **Request**: `POST /api/v1/market/sellers/1/shipping-policies`
  ```json
  {
    "policyName": "관리자 정책",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "baseFee": 0
  }
  ```
- **Expected**:
  - 201 CREATED
  - 권한 보유 시 다른 셀러에게도 등록 가능

**C1-1. 생성 성공 - 첫 번째 정책 (자동 기본 정책)**
- **Category**: 생성 성공
- **Pre-data**: 없음
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "기본 배송정책",
    "defaultPolicy": false,  // false로 요청해도
    "shippingFeeType": "CONDITIONAL_FREE",
    "baseFee": 3000,
    "freeThreshold": 50000,
    "jejuExtraFee": 3000,
    "islandExtraFee": 5000,
    "returnFee": 3000,
    "exchangeFee": 6000,
    "leadTime": {
      "minDays": 1,
      "maxDays": 3,
      "cutoffTime": "14:00"
    }
  }
  ```
- **Expected**:
  - 201 CREATED
  - 응답 body에 생성된 policyId 반환
- **DB 검증**:
  - repository.findById(policyId).isPresent() = true
  - defaultPolicy = true (첫 번째 정책이므로 자동 설정)
  - active = true

**C1-2. 생성 성공 - 기본 정책 명시 등록 (기존 기본 정책 해제)**
- **Category**: 기본 정책 로직 (POL-DEF-001)
- **Pre-data**: 기존 기본 정책 1건 (policyId=1, defaultPolicy=true)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "새 기본 정책",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "baseFee": 0,
    "freeThreshold": null
  }
  ```
- **Expected**: 201 CREATED
- **DB 검증**:
  - 기존 정책(policyId=1): defaultPolicy = false
  - 새 정책: defaultPolicy = true
  - 기본 정책은 정확히 1개만 존재

**C1-3. 생성 성공 - 비기본 정책 등록**
- **Category**: 생성 성공
- **Pre-data**: 기본 정책 1건 존재
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "추가 배송정책",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 3000
  }
  ```
- **Expected**: 201 CREATED
- **DB 검증**:
  - defaultPolicy = false
  - active = true
  - 기존 기본 정책은 변경 없음

**C1-4. 필수 필드 누락 - policyName**
- **Category**: Validation 실패
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "",  // NotBlank 실패
    "defaultPolicy": true,
    "shippingFeeType": "FREE"
  }
  ```
- **Expected**: 400 Bad Request

**C1-5. 필수 필드 누락 - defaultPolicy**
- **Category**: Validation 실패
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "정책명",
    "defaultPolicy": null,  // NotNull 실패
    "shippingFeeType": "FREE"
  }
  ```
- **Expected**: 400 Bad Request

**C1-6. 필수 필드 누락 - shippingFeeType**
- **Category**: Validation 실패
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": null  // NotBlank 실패
  }
  ```
- **Expected**: 400 Bad Request

**C1-7. 잘못된 shippingFeeType 값**
- **Category**: Validation 실패
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": "INVALID_TYPE"
  }
  ```
- **Expected**: 400 Bad Request

**C1-8. POL-FEE-001 위반 - CONDITIONAL_FREE에 freeThreshold 누락**
- **Category**: 비즈니스 규칙 위반
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": "CONDITIONAL_FREE",
    "baseFee": 3000,
    "freeThreshold": null  // 필수인데 누락
  }
  ```
- **Expected**: 400 Bad Request (InvalidFreeThresholdException)

##### P1: 중요 시나리오 (4개)

**C1-9. FREE 타입 - freeThreshold null 허용**
- **Category**: 배송비 타입별 검증
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "무료배송",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "baseFee": 0,
    "freeThreshold": null
  }
  ```
- **Expected**: 201 CREATED
- **DB 검증**: freeThreshold = null

**C1-10. PAID 타입 - freeThreshold null 허용**
- **Category**: 배송비 타입별 검증
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "유료배송",
    "defaultPolicy": true,
    "shippingFeeType": "PAID",
    "baseFee": 3000,
    "freeThreshold": null
  }
  ```
- **Expected**: 201 CREATED

**C1-11. leadTime 없는 정책 등록**
- **Category**: 선택 필드
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "leadTime": null
  }
  ```
- **Expected**: 201 CREATED
- **DB 검증**: leadTime 관련 필드가 null

**C1-12. 음수 배송비 입력 (@Min 검증)**
- **Category**: Validation 실패
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/shipping-policies
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": "PAID",
    "baseFee": -1000  // @Min(0) 위반
  }
  ```
- **Expected**: 400 Bad Request

---

#### PUT /sellers/{sellerId}/shipping-policies/{id} - 배송정책 수정

##### P0: 필수 시나리오 (11개)

**AUTH-C2-1. 401 Unauthorized - 토큰 없이 수정 시도**
- **Category**: 인증 실패
- **Pre-data**: 정책 1건 (policyId=1)
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/1` (Authorization 헤더 없음)
  ```json
  {
    "policyName": "수정된 정책",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 5000
  }
  ```
- **Expected**: 401 Unauthorized

**AUTH-C2-2. 403 Forbidden - 다른 셀러의 정책 수정 시도**
- **Category**: 인가 실패
- **Pre-data**:
  - 셀러1의 정책 1건 (policyId=1)
  - 셀러2의 토큰 (organizationId로 sellerId=2 매핑)
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/1` (셀러2의 토큰)
  ```json
  {
    "policyName": "수정 시도",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 5000
  }
  ```
- **Expected**: 403 Forbidden

**AUTH-C2-3. 204 No Content - superAdmin 바이패스**
- **Category**: superAdmin 권한
- **Pre-data**: 셀러1의 정책 1건 (policyId=1)
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/1` (superAdmin 토큰)
  ```json
  {
    "policyName": "관리자 수정",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 5000
  }
  ```
- **Expected**: 204 No Content

**AUTH-C2-4. 204 No Content - 리소스 소유자 수정**
- **Category**: 리소스 소유자 검증
- **Pre-data**: 셀러1의 정책 1건 (policyId=1)
- **Authentication**: 셀러1의 토큰
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/1`
  ```json
  {
    "policyName": "본인 수정",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 5000
  }
  ```
- **Expected**: 204 No Content

**AUTH-C2-5. 204 No Content - shipping-policy:write 권한으로 수정**
- **Category**: 권한 기반 접근
- **Pre-data**: 셀러1의 정책 1건 (policyId=1)
- **Authentication**: shipping-policy:write 권한 보유 토큰
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/1`
  ```json
  {
    "policyName": "권한자 수정",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 5000
  }
  ```
- **Expected**: 204 No Content

**C2-1. 수정 성공 - 정책명 및 배송비 변경**
- **Category**: 수정 성공
- **Pre-data**: 정책 1건 (policyId=1, policyName="기존정책", baseFee=3000)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/shipping-policies/1
  {
    "policyName": "수정된 정책",
    "defaultPolicy": false,
    "shippingFeeType": "PAID",
    "baseFee": 5000
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**:
  - policyName = "수정된 정책"
  - baseFee = 5000
  - updatedAt 갱신 확인

**C2-2. 수정 성공 - 비기본 정책 → 기본 정책 변경**
- **Category**: 기본 정책 로직 (POL-DEF-001)
- **Pre-data**:
  - 기존 기본 정책 (policyId=1, defaultPolicy=true)
  - 비기본 정책 (policyId=2, defaultPolicy=false)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/shipping-policies/2
  {
    "policyName": "새 기본 정책",
    "defaultPolicy": true,
    "shippingFeeType": "FREE"
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**:
  - policyId=1: defaultPolicy = false
  - policyId=2: defaultPolicy = true

**C2-3. 수정 성공 - shippingFeeType 변경 (CONDITIONAL_FREE → FREE)**
- **Category**: 수정 성공
- **Pre-data**: CONDITIONAL_FREE 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/shipping-policies/1
  {
    "policyName": "무료배송으로 변경",
    "defaultPolicy": true,
    "shippingFeeType": "FREE",
    "baseFee": 0,
    "freeThreshold": null
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**: shippingFeeType = "FREE", freeThreshold = null

**C2-4. 존재하지 않는 정책 수정 시도**
- **Category**: 존재하지 않는 리소스
- **Pre-data**: 없음
- **Authentication**: 셀러1의 토큰
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/99999`
- **Expected**: 404 Not Found

**C2-5. 다른 셀러의 정책 수정 시도 (DB 레벨)**
- **Category**: 권한 검증
- **Pre-data**: 셀러(ID=2)의 정책 1건 (policyId=1)
- **Authentication**: 셀러1의 토큰
- **Request**: `PUT /api/v1/market/sellers/1/shipping-policies/1` (셀러 ID 불일치)
- **Expected**: 404 Not Found (셀러1의 정책 중 policyId=1이 없음)

**C2-6. POL-FEE-001 위반 - CONDITIONAL_FREE로 변경하면서 freeThreshold 누락**
- **Category**: 비즈니스 규칙 위반
- **Pre-data**: FREE 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/shipping-policies/1
  {
    "policyName": "조건부 무료로 변경",
    "defaultPolicy": true,
    "shippingFeeType": "CONDITIONAL_FREE",
    "baseFee": 3000,
    "freeThreshold": null  // 필수인데 누락
  }
  ```
- **Expected**: 400 Bad Request (InvalidFreeThresholdException)

##### P1: 중요 시나리오 (2개)

**C2-7. leadTime 추가**
- **Category**: 선택 필드 수정
- **Pre-data**: leadTime이 없는 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/shipping-policies/1
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": "PAID",
    "baseFee": 3000,
    "leadTime": {
      "minDays": 2,
      "maxDays": 5,
      "cutoffTime": "15:00"
    }
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**: leadTime 필드 설정 확인

**C2-8. leadTime 제거**
- **Category**: 선택 필드 수정
- **Pre-data**: leadTime이 있는 정책 1건
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/shipping-policies/1
  {
    "policyName": "정책명",
    "defaultPolicy": true,
    "shippingFeeType": "PAID",
    "baseFee": 3000,
    "leadTime": null
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**: leadTime 필드가 null

---

#### PATCH /sellers/{sellerId}/shipping-policies/{id}/status - 배송정책 상태 변경

##### P0: 필수 시나리오 (11개)

**AUTH-C3-1. 401 Unauthorized - 토큰 없이 상태 변경 시도**
- **Category**: 인증 실패
- **Pre-data**: 정책 2건 (policyId=2, 3)
- **Request**: `PATCH /api/v1/market/sellers/1/shipping-policies/status` (Authorization 헤더 없음)
  ```json
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 401 Unauthorized

**AUTH-C3-2. 403 Forbidden - 다른 셀러의 정책 상태 변경 시도**
- **Category**: 인가 실패
- **Pre-data**:
  - 셀러1의 정책 2건 (policyId=2, 3)
  - 셀러2의 토큰 (organizationId로 sellerId=2 매핑)
- **Request**: `PATCH /api/v1/market/sellers/1/shipping-policies/status` (셀러2의 토큰)
  ```json
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 403 Forbidden

**AUTH-C3-3. 204 No Content - superAdmin 바이패스**
- **Category**: superAdmin 권한
- **Pre-data**: 셀러1의 정책 2건 (policyId=2, 3)
- **Request**: `PATCH /api/v1/market/sellers/1/shipping-policies/status` (superAdmin 토큰)
  ```json
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 204 No Content

**AUTH-C3-4. 204 No Content - 리소스 소유자 상태 변경**
- **Category**: 리소스 소유자 검증
- **Pre-data**: 셀러1의 정책 2건 (policyId=2, 3)
- **Authentication**: 셀러1의 토큰
- **Request**: `PATCH /api/v1/market/sellers/1/shipping-policies/status`
  ```json
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 204 No Content

**AUTH-C3-5. 204 No Content - shipping-policy:write 권한으로 상태 변경**
- **Category**: 권한 기반 접근
- **Pre-data**: 셀러1의 정책 2건 (policyId=2, 3)
- **Authentication**: shipping-policy:write 권한 보유 토큰
- **Request**: `PATCH /api/v1/market/sellers/1/shipping-policies/status`
  ```json
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 204 No Content

**C3-1. 활성화 성공 - 비활성 정책 활성화**
- **Category**: 상태 변경 성공
- **Pre-data**: 비활성 정책 2건 (policyId=2, 3, active=false)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [2, 3],
    "active": true
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**:
  - policyId=2: active = true
  - policyId=3: active = true
  - updatedAt 갱신 확인

**C3-2. 비활성화 성공 - 비기본 정책 비활성화**
- **Category**: 상태 변경 성공
- **Pre-data**:
  - 기본 정책 (policyId=1, defaultPolicy=true, active=true)
  - 비기본 정책 (policyId=2, 3, defaultPolicy=false, active=true)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**:
  - policyId=1: active = true (유지)
  - policyId=2, 3: active = false

**C3-3. POL-DEACT-001 위반 - 기본 정책 비활성화 시도**
- **Category**: 비즈니스 규칙 위반
- **Pre-data**: 기본 정책 1건 (policyId=1, defaultPolicy=true, active=true)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**: 400 Bad Request (CannotDeactivateDefaultShippingPolicyException)

**C3-4. POL-DEACT-002 위반 - 마지막 활성 정책 비활성화 시도**
- **Category**: 비즈니스 규칙 위반
- **Pre-data**: 활성 정책 1건만 존재 (policyId=1, active=true)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**: 400 Bad Request (LastActiveShippingPolicyCannotBeDeactivatedException)

**C3-5. 존재하지 않는 정책 상태 변경 시도**
- **Category**: 존재하지 않는 리소스
- **Pre-data**: 없음
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [99999],
    "active": false
  }
  ```
- **Expected**: 404 Not Found

**C3-6. 빈 policyIds 리스트**
- **Category**: Validation 실패
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [],  // @NotEmpty 위반
    "active": false
  }
  ```
- **Expected**: 400 Bad Request

##### P1: 중요 시나리오 (2개)

**C3-7. 다건 동시 활성화**
- **Category**: 다건 처리
- **Pre-data**: 비활성 정책 5건
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [2, 3, 4, 5, 6],
    "active": true
  }
  ```
- **Expected**: 204 No Content
- **DB 검증**: 모든 정책이 활성화 확인

**C3-8. POL-DEACT-002 위반 - 일부 활성 정책 비활성화 (정확히 1개 남김)**
- **Category**: 경계 조건 (Edge Case)
- **Pre-data**: 활성 정책 3건 (policyId=1, 2, 3, active=true)
- **Authentication**: 셀러1의 토큰
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/shipping-policies/status
  {
    "policyIds": [2, 3],
    "active": false
  }
  ```
- **Expected**: 204 No Content (1개 남으므로 성공)
- **DB 검증**:
  - policyId=1: active = true
  - policyId=2, 3: active = false

---

### 전체 플로우 시나리오 (6개)

#### P0: CRUD 플로우 (3개)

**AUTH-F1. 인증 실패 플로우 - 토큰 없이 전체 CRUD 시도**
- **Category**: 인증 실패 플로우
- **Steps**:
  1. **POST** → 토큰 없이 생성 시도 (401)
  2. **GET** → 토큰 없이 조회 시도 (401)
  3. **PUT** → 토큰 없이 수정 시도 (401)
  4. **PATCH** → 토큰 없이 상태 변경 시도 (401)

**AUTH-F2. 권한 위반 플로우 - 다른 셀러 리소스 접근 시도**
- **Category**: 인가 실패 플로우
- **Pre-data**: 셀러1의 정책 1건 (policyId=1)
- **Authentication**: 셀러2의 토큰
- **Steps**:
  1. **GET** → 셀러1의 정책 조회 시도 (403)
  2. **POST** → 셀러1에게 정책 등록 시도 (403)
  3. **PUT** → 셀러1의 정책 수정 시도 (403)
  4. **PATCH** → 셀러1의 정책 상태 변경 시도 (403)

**F1. CRUD 전체 플로우**
- **Category**: CRUD 플로우
- **Authentication**: 셀러1의 토큰 (전체 플로우 공통)
- **Steps**:
  1. **POST** → 배송정책 생성 (201)
     - 응답에서 policyId 추출
  2. **GET** → 목록 조회로 생성 확인 (200)
     - content에서 생성된 정책 확인
  3. **PUT** → 정책 수정 (204)
     - policyName 변경, baseFee 변경
  4. **GET** → 목록 조회로 수정 확인 (200)
     - 수정된 값 확인
  5. **PATCH** → 비활성화 (204)
     - active = false로 변경
  6. **GET** → 목록 조회로 비활성화 확인 (200)
     - active = false 확인

#### P1: 상태 전이 플로우 (3개)

**F2. 기본 정책 변경 플로우**
- **Category**: 기본 정책 로직 (POL-DEF-001)
- **Authentication**: 셀러1의 토큰
- **Steps**:
  1. **POST** → 첫 번째 정책 생성 (201, 자동 기본 정책)
  2. **GET** → defaultPolicy=true 확인 (200)
  3. **POST** → 두 번째 정책 생성 (defaultPolicy=false) (201)
  4. **GET** → 기본 정책은 여전히 1개 (200)
  5. **PUT** → 두 번째 정책을 기본 정책으로 변경 (204)
  6. **GET** → 기본 정책 변경 확인 (200)
     - 첫 번째 정책: defaultPolicy=false
     - 두 번째 정책: defaultPolicy=true

**F3. 다중 정책 상태 전이 플로우**
- **Category**: 상태 전이 플로우
- **Authentication**: 셀러1의 토큰
- **Steps**:
  1. **POST** → 정책 3건 생성 (1개 기본, 2개 비기본)
  2. **GET** → 모두 active=true 확인 (200)
  3. **PATCH** → 비기본 정책 2건 비활성화 (204)
  4. **GET** → 비활성화 확인 (200)
  5. **PATCH** → 비활성화된 정책 1건 재활성화 (204)
  6. **GET** → 재활성화 확인 (200)
  7. **PATCH** → 기본 정책 비활성화 시도 (400, POL-DEACT-001)

**F4. 배송비 타입별 등록 + 조회 플로우**
- **Category**: 배송비 타입별 검증
- **Authentication**: 셀러1의 토큰
- **Steps**:
  1. **POST** → FREE 정책 등록 (201)
  2. **POST** → PAID 정책 등록 (201)
  3. **POST** → CONDITIONAL_FREE 정책 등록 (201)
  4. **GET** → 목록 조회 (200)
     - 각 타입별 shippingFeeTypeDisplayName 확인
     - CONDITIONAL_FREE: freeThreshold 존재 확인
     - FREE, PAID: freeThreshold null 확인

---

## 3. Fixture 설계

### 필요 Repository
- **ShippingPolicyJpaRepository**: save(), saveAll(), findById()

### testFixtures 사용
- **ShippingPolicyJpaEntityFixtures** (이미 존재) ✅
  - `activeEntity(Long id, Long sellerId)`: 활성 정책
  - `newDefaultEntity(Long sellerId)`: 새 기본 정책
  - `newActiveEntityWithName(Long sellerId, String policyName)`: 정책명 지정
  - `freeShippingEntity()`: 무료배송 정책
  - `paidShippingEntity()`: 유료배송 정책
  - `inactiveEntity()`: 비활성 정책
  - `deletedEntity()`: 삭제된 정책

### 인증 컨텍스트 설정 방법

#### TestSecurityContext 설정
```java
// 1. 셀러 본인 토큰 (organizationId → sellerId 매핑)
@BeforeEach
void setUpAuth() {
    // 셀러1 컨텍스트
    SecurityContextHolder.getContext().setAuthentication(
        createSellerAuthentication(1L, "org-seller-1")
    );
}

// 2. superAdmin 토큰
SecurityContextHolder.getContext().setAuthentication(
    createSuperAdminAuthentication()
);

// 3. 권한 보유 토큰 (shipping-policy:read, shipping-policy:write)
SecurityContextHolder.getContext().setAuthentication(
    createAuthenticationWithPermissions("shipping-policy:read", "shipping-policy:write")
);

// 4. 다른 셀러 토큰
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(2L, "org-seller-2")
);
```

### 사전 데이터 설정 방법

#### setUp()
```java
@BeforeEach
void setUp() {
    shippingPolicyJpaRepository.deleteAll(); // 전체 삭제
    SecurityContextHolder.clearContext(); // 인증 컨텍스트 초기화
}
```

#### 시나리오별 Pre-data

**1. 기본 조회 테스트**
```java
List<ShippingPolicyJpaEntity> policies = List.of(
    ShippingPolicyJpaEntityFixtures.newDefaultEntity(1L),      // 기본 정책
    ShippingPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책2"),
    ShippingPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책3")
);
shippingPolicyJpaRepository.saveAll(policies);

// 셀러1 인증 설정
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(1L, "org-seller-1")
);
```

**2. 권한 검증 테스트**
```java
// 셀러1의 정책 생성
ShippingPolicyJpaEntity policy = shippingPolicyJpaRepository.save(
    ShippingPolicyJpaEntityFixtures.newDefaultEntity(1L)
);

// 셀러2 인증 설정 (다른 셀러)
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(2L, "org-seller-2")
);
```

**3. superAdmin 테스트**
```java
// 셀러1의 정책 생성
shippingPolicyJpaRepository.save(
    ShippingPolicyJpaEntityFixtures.newDefaultEntity(1L)
);

// superAdmin 인증 설정
SecurityContextHolder.getContext().setAuthentication(
    createSuperAdminAuthentication()
);
```

**4. 페이징 테스트 (5건)**
```java
List<ShippingPolicyJpaEntity> policies = IntStream.rangeClosed(1, 5)
    .mapToObj(i -> ShippingPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책" + i))
    .toList();
shippingPolicyJpaRepository.saveAll(policies);

// 셀러1 인증 설정
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(1L, "org-seller-1")
);
```

**5. 기본 정책 변경 테스트**
```java
ShippingPolicyJpaEntity existingDefault = shippingPolicyJpaRepository.save(
    ShippingPolicyJpaEntityFixtures.newDefaultEntity(1L)
);
Long existingDefaultId = existingDefault.getId();

// 셀러1 인증 설정
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(1L, "org-seller-1")
);
```

**6. 비활성화 검증 테스트**
```java
// 기본 정책 + 비기본 정책 2건
ShippingPolicyJpaEntity defaultPolicy = shippingPolicyJpaRepository.save(
    ShippingPolicyJpaEntityFixtures.newDefaultEntity(1L)
);
List<ShippingPolicyJpaEntity> nonDefaultPolicies = List.of(
    ShippingPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책2"),
    ShippingPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책3")
);
List<ShippingPolicyJpaEntity> saved = shippingPolicyJpaRepository.saveAll(nonDefaultPolicies);
List<Long> nonDefaultIds = saved.stream().map(ShippingPolicyJpaEntity::getId).toList();

// 셀러1 인증 설정
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(1L, "org-seller-1")
);
```

**7. 삭제된 정책 제외 테스트**
```java
List<ShippingPolicyJpaEntity> policies = List.of(
    ShippingPolicyJpaEntityFixtures.newDefaultEntity(1L),
    ShippingPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책2"),
    ShippingPolicyJpaEntityFixtures.deletedEntity(1L)  // 삭제된 정책
);
shippingPolicyJpaRepository.saveAll(policies);

// 셀러1 인증 설정
SecurityContextHolder.getContext().setAuthentication(
    createSellerAuthentication(1L, "org-seller-1")
);
```

---

## 4. 문서 생성

### 출력 위치
- **파일**: `.claude/docs/test-scenario/shippingpolicy.md` ✅

### 다음 단계
- E2E 테스트 구현: `/test-e2e shippingpolicy`

---

## 5. 시나리오 요약

### 총 시나리오 개수
| 분류 | P0 (필수) | P1 (중요) | 합계 |
|------|-----------|-----------|------|
| **Query (인증/인가)** | 5개 | 0개 | **5개** |
| **Query (기능)** | 4개 | 8개 | **12개** |
| **Command - 등록 (인증/인가)** | 5개 | 0개 | **5개** |
| **Command - 등록 (기능)** | 8개 | 4개 | **12개** |
| **Command - 수정 (인증/인가)** | 5개 | 0개 | **5개** |
| **Command - 수정 (기능)** | 6개 | 2개 | **8개** |
| **Command - 상태변경 (인증/인가)** | 5개 | 0개 | **5개** |
| **Command - 상태변경 (기능)** | 6개 | 2개 | **8개** |
| **플로우 (인증/인가)** | 2개 | 0개 | **2개** |
| **플로우 (기능)** | 1개 | 3개 | **4개** |
| **총계** | **47개** | **19개** | **66개** |

### 우선순위별 요약
- **P0 (필수)**: 47개
  - 인증/인가: 22개 (401 Unauthorized, 403 Forbidden, superAdmin, 리소스 소유자, 권한 검증)
  - 기능: 25개 (기본 CRUD, 필수 검증, 핵심 비즈니스 규칙)
- **P1 (중요)**: 19개
  - 정렬/페이징, 타입별 검증, 경계 조건

### 비즈니스 규칙 커버리지
| 규칙 ID | 규칙 내용 | 관련 시나리오 |
|---------|----------|--------------|
| **POL-DEF-001** | 셀러당 기본 정책 1개 | C1-2, C2-2, F2 |
| **POL-DEF-002** | 기본 정책 활성화 필수 | C1-1 (자동 검증) |
| **POL-DEACT-001** | 기본 정책 비활성화 불가 | C3-3, F3 |
| **POL-DEACT-002** | 마지막 활성 정책 비활성화 불가 | C3-4, C3-8 |
| **POL-FEE-001** | CONDITIONAL_FREE는 freeThreshold 필수 | C1-8, C2-6 |

### 인증/인가 커버리지
| 검증 항목 | 관련 시나리오 |
|----------|--------------|
| **401 Unauthorized** | AUTH-Q1, AUTH-C1-1, AUTH-C2-1, AUTH-C3-1, AUTH-F1 |
| **403 Forbidden** | AUTH-Q2, AUTH-C1-2, AUTH-C2-2, AUTH-C3-2, AUTH-F2 |
| **superAdmin 바이패스** | AUTH-Q3, AUTH-C1-3, AUTH-C2-3, AUTH-C3-3 |
| **리소스 소유자 검증** | AUTH-Q4, AUTH-C1-4, AUTH-C2-4, AUTH-C3-4 |
| **권한 기반 접근** | AUTH-Q5, AUTH-C1-5, AUTH-C2-5, AUTH-C3-5 |

### Validation 커버리지
| Validation | 대상 필드 | 관련 시나리오 |
|------------|----------|--------------|
| **@NotBlank** | policyName, shippingFeeType | C1-4, C1-6 |
| **@NotNull** | defaultPolicy, active | C1-5, C3-6 |
| **@NotEmpty** | policyIds | C3-6 |
| **@Min(0)** | baseFee, freeThreshold, etc. | C1-12 |
| **@Size(1-100)** | policyName | C1-4 |
| **@Valid** | leadTime (nested) | C1-11, C2-7, C2-8 |

---

## 분석 일시
- **최초 생성일**: 2026-02-06
- **인증/인가 추가일**: 2026-02-06
- **분석 대상**: ShippingPolicy 도메인
- **참조 문서**:
  - `.claude/docs/api-endpoints/shippingpolicy.md`
  - `.claude/docs/api-flow/shippingpolicy.md`
  - `adapter-in/rest-api/.../controller/ShippingPolicyQueryController.java`
  - `adapter-in/rest-api/.../controller/ShippingPolicyCommandController.java`
  - `adapter-in/rest-api/.../security/MarketAccessChecker.java`
