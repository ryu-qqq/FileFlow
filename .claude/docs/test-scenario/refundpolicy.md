# RefundPolicy E2E 통합 테스트 시나리오 설계

## 1. 입력 분석

### 참고 문서
- ✅ api-endpoints: `.claude/docs/api-endpoints/refundpolicy.md`
- ✅ api-flow: `.claude/docs/api-flow/refundpolicy.md`

### 엔드포인트 분석
| 분류 | 개수 | 엔드포인트 | @PreAuthorize |
|------|------|------------|---------------|
| Query | 1개 | GET /sellers/{sellerId}/refund-policies | `@access.isSellerOwnerOr(#sellerId, 'refund-policy:read')` |
| Command | 3개 | POST /sellers/{sellerId}/refund-policies (등록), PUT /sellers/{sellerId}/refund-policies/{id} (수정), PATCH /sellers/{sellerId}/refund-policies/status (상태 변경) | `@access.isSellerOwnerOr(#sellerId, 'refund-policy:write')` |
| **합계** | **4개** | - | - |

### Request DTO Validation 규칙
| 필드 | 규칙 | 설명 |
|------|------|------|
| policyName | @NotBlank, @Size(1~100) | 정책명 필수, 1~100자 |
| defaultPolicy | @NotNull | 기본 정책 여부 필수 (등록/수정) |
| returnPeriodDays | @NotNull, @Min(1), @Max(90) | 반품 기간 1~90일 |
| exchangePeriodDays | @NotNull, @Min(1), @Max(90) | 교환 기간 1~90일 |
| inspectionPeriodDays | @Min(0) | 검수 기간 0일 이상 |
| additionalInfo | @Size(max=1000) | 추가 안내 최대 1000자 |
| policyIds | @NotEmpty (상태 변경) | 정책 ID 목록 필수 |
| active | @NotNull (상태 변경) | 활성화 상태 필수 |

### Domain 비즈니스 규칙
| 규칙 코드 | 설명 | 검증 위치 |
|-----------|------|----------|
| **POL-DEF-001** | 셀러당 기본 정책은 정확히 1개 | DefaultRefundPolicyResolver |
| **POL-DEF-002** | 기본 정책은 활성화 상태여야 함 | DefaultRefundPolicyResolver |
| **POL-DEACT-001** | 기본 정책은 비활성화 불가 | RefundPolicy.deactivate() |
| **POL-DEACT-002** | 마지막 활성 정책은 비활성화 불가 | RefundPolicyValidator |

### 인증/인가 규칙
| 메커니즘 | 설명 |
|----------|------|
| **MarketAccessChecker.isSellerOwnerOr()** | 1. superAdmin → 자동 통과<br>2. organizationId → sellerId 매핑 조회<br>3. URL의 sellerId와 매칭 확인<br>4. 실패 시 permission 보유 여부 확인 |
| **권한 체계** | `refund-policy:read` (조회), `refund-policy:write` (등록/수정/상태 변경) |

---

## 2. 시나리오 설계

### 📖 Query 시나리오: 11개

#### 2.1. searchRefundPolicies (목록 조회) - 9개

##### P0: 인증/인가 시나리오 (4개)

**[Q0-1] 토큰 없이 요청 시 401**
- **분류**: 인증 실패
- **우선순위**: P0
- **사전 데이터**: 없음
- **Request**: `GET /api/v1/market/sellers/1/refund-policies` (토큰 없음)
- **Expected**:
  - Status: 401 UNAUTHORIZED
- **DB 검증**: 없음

**[Q0-2] 다른 셀러의 정책 조회 시도 시 403**
- **분류**: 인가 실패 (리소스 소유자 검증)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller2" (sellerId=2에 매핑)
- **사전 데이터**: sellerId=1의 정책 3건
- **Request**: `GET /api/v1/market/sellers/1/refund-policies`
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 없음

**[Q0-3] refund-policy:read 권한 없는 사용자 403**
- **분류**: 인가 실패 (권한 부족)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-other" (sellerId 매핑 없음), permissions=["other:read"]
- **사전 데이터**: 없음
- **Request**: `GET /api/v1/market/sellers/1/refund-policies`
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 없음

**[Q0-4] superAdmin 바이패스 성공**
- **분류**: 인증 성공 (superAdmin)
- **우선순위**: P0
- **인증 컨텍스트**: role=SUPER_ADMIN
- **사전 데이터**: sellerId=999의 정책 2건
- **Request**: `GET /api/v1/market/sellers/999/refund-policies`
- **Expected**:
  - Status: 200 OK
  - `content.size = 2`
- **DB 검증**: 없음

##### P0: 기본 조회 시나리오 (2개)

**[Q1-1] 정책 존재 시 정상 조회 (리소스 소유자)**
- **분류**: 기본 조회
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 3건 저장 (sellerId=1)
- **Request**: `GET /api/v1/market/sellers/1/refund-policies`
- **Expected**:
  - Status: 200 OK
  - `content.size = 3`
  - `totalElements = 3`
  - Response에 policyId, policyName, defaultPolicy, active, returnPeriodDays 등 포함
- **DB 검증**: 없음 (조회만)

**[Q1-2] 정책 없을 때 빈 목록 반환**
- **분류**: 빈 결과
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller999" (sellerId=999에 매핑)
- **사전 데이터**: 없음 (다른 셀러의 정책만 존재)
- **Request**: `GET /api/v1/market/sellers/999/refund-policies`
- **Expected**:
  - Status: 200 OK
  - `content.size = 0`
  - `totalElements = 0`
- **DB 검증**: 없음

##### P1: 검색 및 정렬 시나리오 (3개)

**[Q1-3] 페이징 동작 확인**
- **분류**: 페이징
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 5건 저장 (sellerId=1)
- **Request**: `GET /api/v1/market/sellers/1/refund-policies?page=0&size=2`
- **Expected**:
  - Status: 200 OK
  - `content.size = 2`
  - `totalElements = 5`
  - `totalPages = 3`
- **DB 검증**: 없음

**[Q1-4] 정렬 기능 확인 (CREATED_AT DESC)**
- **분류**: 정렬
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 3건 저장 (시간차를 두고 생성)
- **Request**: `GET /api/v1/market/sellers/1/refund-policies?sortKey=CREATED_AT&sortDirection=DESC`
- **Expected**:
  - Status: 200 OK
  - `content[0].createdAt > content[1].createdAt > content[2].createdAt`
- **DB 검증**: 없음

**[Q1-5] 정렬 기능 확인 (POLICY_NAME ASC)**
- **분류**: 정렬
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 3건 저장 ("C정책", "A정책", "B정책")
- **Request**: `GET /api/v1/market/sellers/1/refund-policies?sortKey=POLICY_NAME&sortDirection=ASC`
- **Expected**:
  - Status: 200 OK
  - `content[0].policyName = "A정책"`
  - `content[1].policyName = "B정책"`
  - `content[2].policyName = "C정책"`
- **DB 검증**: 없음

#### 2.2. 셀러 격리 검증 (2개)

**[Q2-1] 다른 셀러 정책 조회 불가**
- **분류**: 보안
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**:
  - sellerId=1의 정책 3건
  - sellerId=2의 정책 2건
- **Request**: `GET /api/v1/market/sellers/1/refund-policies`
- **Expected**:
  - Status: 200 OK
  - `content.size = 3` (sellerId=1의 정책만)
- **DB 검증**: 없음

**[Q2-2] 삭제된 정책 조회 제외**
- **분류**: 소프트 삭제
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**:
  - 활성 정책 2건
  - 삭제된 정책 1건 (deletedAt != null)
- **Request**: `GET /api/v1/market/sellers/1/refund-policies`
- **Expected**:
  - Status: 200 OK
  - `content.size = 2` (활성 정책만)
- **DB 검증**: 없음

---

### ✏️ Command 시나리오: 33개

#### 3.1. POST /refund-policies (등록) - 12개

##### P0: 인증/인가 시나리오 (4개)

**[C0-1] 토큰 없이 등록 시도 시 401**
- **분류**: 인증 실패
- **우선순위**: P0
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies (토큰 없음)
  {
    "policyName": "테스트 정책",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 401 UNAUTHORIZED
- **DB 검증**: 데이터 생성 안 됨

**[C0-2] 다른 셀러의 정책 등록 시도 시 403**
- **분류**: 인가 실패 (리소스 소유자 검증)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller2" (sellerId=2에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "테스트 정책",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 생성 안 됨

**[C0-3] refund-policy:write 권한 없는 사용자 403**
- **분류**: 인가 실패 (권한 부족)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-other", permissions=["refund-policy:read"]
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "테스트 정책",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 생성 안 됨

**[C0-4] superAdmin 바이패스 성공**
- **분류**: 인증 성공 (superAdmin)
- **우선순위**: P0
- **인증 컨텍스트**: role=SUPER_ADMIN
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/999/refund-policies
  {
    "policyName": "관리자 정책",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 201 CREATED
  - Response: `policyId > 0`
- **DB 검증**: `repository.findById(policyId).isPresent()`

##### P0: 필수 시나리오 (4개)

**[C1-1] 유효한 요청으로 정책 생성 (첫 번째 정책, 리소스 소유자)**
- **분류**: 생성 성공
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음 (첫 번째 정책)
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "기본 환불정책",
    "defaultPolicy": false,
    "returnPeriodDays": 7,
    "exchangePeriodDays": 14,
    "nonReturnableConditions": ["OPENED_PACKAGING"],
    "partialRefundEnabled": true,
    "inspectionRequired": true,
    "inspectionPeriodDays": 3,
    "additionalInfo": "추가 안내"
  }
  ```
- **Expected**:
  - Status: 201 CREATED
  - Response: `policyId > 0`
- **DB 검증**:
  - `repository.findById(policyId).isPresent()`
  - `defaultPolicy = true` (첫 번째 정책이므로 자동으로 기본 정책 설정)
  - `active = true`

**[C1-2] 유효한 요청으로 정책 생성 (두 번째 정책)**
- **분류**: 생성 성공
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 기본 정책 1건 존재
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "추가 환불정책",
    "defaultPolicy": false,
    "returnPeriodDays": 14,
    "exchangePeriodDays": 30
  }
  ```
- **Expected**:
  - Status: 201 CREATED
  - Response: `policyId > 0`
- **DB 검증**:
  - `repository.findById(policyId).isPresent()`
  - `defaultPolicy = false`
  - `active = true`

**[C1-3] 기본 정책 등록 시 기존 기본 정책 해제**
- **분류**: 기본 정책 규칙 (POL-DEF-001)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 기본 정책 1건 (id=1, defaultPolicy=true)
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "새 기본 정책",
    "defaultPolicy": true,
    "returnPeriodDays": 30,
    "exchangePeriodDays": 30
  }
  ```
- **Expected**:
  - Status: 201 CREATED
  - Response: `policyId > 0`
- **DB 검증**:
  - 새 정책: `defaultPolicy = true`
  - 기존 정책(id=1): `defaultPolicy = false`, `updatedAt` 갱신

**[C1-4] 필수 필드 누락 시 400 에러 (policyName)**
- **분류**: 필수 필드 누락
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "정책명은 필수입니다"
- **DB 검증**: 데이터 생성 안 됨

##### P1: Validation 실패 시나리오 (4개)

**[C1-5] returnPeriodDays 범위 벗어남 (0일)**
- **분류**: Validation 실패
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "테스트 정책",
    "returnPeriodDays": 0,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "반품 가능 기간은 1일 이상이어야 합니다"
- **DB 검증**: 데이터 생성 안 됨

**[C1-6] returnPeriodDays 범위 벗어남 (91일)**
- **분류**: Validation 실패
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "테스트 정책",
    "returnPeriodDays": 91,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "반품 가능 기간은 90일 이하여야 합니다"
- **DB 검증**: 데이터 생성 안 됨

**[C1-7] policyName 길이 초과 (101자)**
- **분류**: Validation 실패
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "A".repeat(101),
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "정책명은 1~100자여야 합니다"
- **DB 검증**: 데이터 생성 안 됨

**[C1-8] additionalInfo 길이 초과 (1001자)**
- **분류**: Validation 실패
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  POST /api/v1/market/sellers/1/refund-policies
  {
    "policyName": "테스트 정책",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7,
    "additionalInfo": "A".repeat(1001)
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "추가 안내 문구는 1000자 이하여야 합니다"
- **DB 검증**: 데이터 생성 안 됨

---

#### 3.2. PUT /refund-policies/{policyId} (수정) - 11개

##### P0: 인증/인가 시나리오 (4개)

**[C2-0-1] 토큰 없이 수정 시도 시 401**
- **분류**: 인증 실패
- **우선순위**: P0
- **사전 데이터**: 환불정책 1건 (id=1, sellerId=1)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1 (토큰 없음)
  {
    "policyName": "수정 시도",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 401 UNAUTHORIZED
- **DB 검증**: 데이터 변경 안 됨

**[C2-0-2] 다른 셀러의 정책 수정 시도 시 403**
- **분류**: 인가 실패 (리소스 소유자 검증)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller2" (sellerId=2에 매핑)
- **사전 데이터**: sellerId=1의 정책 1건 (id=1)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1
  {
    "policyName": "수정 시도",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 변경 안 됨

**[C2-0-3] refund-policy:write 권한 없는 사용자 403**
- **분류**: 인가 실패 (권한 부족)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-other", permissions=["refund-policy:read"]
- **사전 데이터**: sellerId=1의 정책 1건 (id=1)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1
  {
    "policyName": "수정 시도",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 변경 안 됨

**[C2-0-4] superAdmin 바이패스 성공**
- **분류**: 인증 성공 (superAdmin)
- **우선순위**: P0
- **인증 컨텍스트**: role=SUPER_ADMIN
- **사전 데이터**: sellerId=999의 정책 1건 (id=1, policyName="기존 정책")
- **Request**:
  ```json
  PUT /api/v1/market/sellers/999/refund-policies/1
  {
    "policyName": "관리자 수정",
    "returnPeriodDays": 14,
    "exchangePeriodDays": 14
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**:
  - `policyName = "관리자 수정"`
  - `returnPeriodDays = 14`

##### P0: 필수 시나리오 (4개)

**[C2-1] 존재하는 정책 수정 성공 (리소스 소유자)**
- **분류**: 수정 성공
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 1건 (id=1, policyName="기존 정책", returnPeriodDays=7)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1
  {
    "policyName": "수정된 정책",
    "defaultPolicy": false,
    "returnPeriodDays": 14,
    "exchangePeriodDays": 30
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**:
  - `policyName = "수정된 정책"`
  - `returnPeriodDays = 14`
  - `updatedAt` 갱신

**[C2-2] 기본 정책으로 변경 시 기존 기본 정책 해제**
- **분류**: 기본 정책 규칙 (POL-DEF-001)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**:
  - 기본 정책 1건 (id=1, defaultPolicy=true)
  - 일반 정책 1건 (id=2, defaultPolicy=false)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/2
  {
    "policyName": "새 기본 정책",
    "defaultPolicy": true,
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**:
  - 정책2(id=2): `defaultPolicy = true`
  - 정책1(id=1): `defaultPolicy = false`, `updatedAt` 갱신

**[C2-3] 존재하지 않는 정책 수정 시 404**
- **분류**: 존재하지 않는 리소스
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 없음
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/99999
  {
    "policyName": "테스트",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 404 NOT_FOUND
  - Error: "환불 정책을 찾을 수 없습니다"
- **DB 검증**: 없음

**[C2-4] 다른 셀러의 정책 수정 시 404 (비즈니스 로직 레벨)**
- **분류**: 보안
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: sellerId=2의 정책 1건 (id=1)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/2/refund-policies/1
  {
    "policyName": "수정 시도",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 변경 안 됨

##### P1: 비즈니스 규칙 검증 (3개)

**[C2-5] 유일한 기본 정책 해제 시도 시 409**
- **분류**: 기본 정책 규칙 위반 (POL-DEF-001)
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 기본 정책 1건만 존재 (id=1, defaultPolicy=true)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1
  {
    "policyName": "기존 정책",
    "defaultPolicy": false,
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 409 CONFLICT
  - Error: "유일한 기본 정책은 해제할 수 없습니다"
- **DB 검증**: `defaultPolicy = true` 유지

**[C2-6] returnPeriodDays 범위 벗어남 수정 시 400**
- **분류**: Validation 실패
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 1건 (id=1)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1
  {
    "policyName": "테스트",
    "returnPeriodDays": 100,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "반품 가능 기간은 90일 이하여야 합니다"
- **DB 검증**: 데이터 변경 안 됨

**[C2-7] policyName 빈 값 수정 시 400**
- **분류**: Validation 실패
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 환불정책 1건 (id=1)
- **Request**:
  ```json
  PUT /api/v1/market/sellers/1/refund-policies/1
  {
    "policyName": "",
    "returnPeriodDays": 7,
    "exchangePeriodDays": 7
  }
  ```
- **Expected**:
  - Status: 400 BAD_REQUEST
  - Error: "정책명은 필수입니다"
- **DB 검증**: 데이터 변경 안 됨

---

#### 3.3. PATCH /refund-policies/status (상태 변경) - 10개

##### P0: 인증/인가 시나리오 (4개)

**[C3-0-1] 토큰 없이 상태 변경 시도 시 401**
- **분류**: 인증 실패
- **우선순위**: P0
- **사전 데이터**: 환불정책 1건 (id=1, sellerId=1)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status (토큰 없음)
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**:
  - Status: 401 UNAUTHORIZED
- **DB 검증**: 데이터 변경 안 됨

**[C3-0-2] 다른 셀러의 정책 상태 변경 시도 시 403**
- **분류**: 인가 실패 (리소스 소유자 검증)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller2" (sellerId=2에 매핑)
- **사전 데이터**: sellerId=1의 정책 1건 (id=1)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 변경 안 됨

**[C3-0-3] refund-policy:write 권한 없는 사용자 403**
- **분류**: 인가 실패 (권한 부족)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-other", permissions=["refund-policy:read"]
- **사전 데이터**: sellerId=1의 정책 1건 (id=1)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**:
  - Status: 403 FORBIDDEN
- **DB 검증**: 데이터 변경 안 됨

**[C3-0-4] superAdmin 바이패스 성공**
- **분류**: 인증 성공 (superAdmin)
- **우선순위**: P0
- **인증 컨텍스트**: role=SUPER_ADMIN
- **사전 데이터**:
  - sellerId=999의 비활성 정책 1건 (id=1, active=false, defaultPolicy=false)
  - sellerId=999의 활성 기본 정책 1건 (id=2, active=true, defaultPolicy=true)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/999/refund-policies/status
  {
    "policyIds": [1],
    "active": true
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**: `active = true`

##### P0: 필수 시나리오 (4개)

**[C3-1] 정책 활성화 성공 (단건, 리소스 소유자)**
- **분류**: 상태 변경 성공
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 비활성 정책 1건 (id=1, active=false, defaultPolicy=false)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1],
    "active": true
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**:
  - 정책1(id=1): `active = true`, `updatedAt` 갱신

**[C3-2] 정책 비활성화 성공 (비기본 정책, 다른 활성 정책 존재)**
- **분류**: 상태 변경 성공
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**:
  - 활성 기본 정책 1건 (id=1, active=true, defaultPolicy=true)
  - 활성 일반 정책 1건 (id=2, active=true, defaultPolicy=false)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [2],
    "active": false
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**:
  - 정책2(id=2): `active = false`, `updatedAt` 갱신

**[C3-3] 정책 다건 활성화 성공**
- **분류**: 상태 변경 성공 (다건)
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**:
  - 비활성 정책 3건 (id=1,2,3, active=false, defaultPolicy=false)
  - 활성 기본 정책 1건 (id=4, active=true, defaultPolicy=true)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1, 2, 3],
    "active": true
  }
  ```
- **Expected**:
  - Status: 204 NO_CONTENT
- **DB 검증**:
  - 정책1,2,3: `active = true`, `updatedAt` 갱신

**[C3-4] 존재하지 않는 정책 ID 포함 시 404**
- **분류**: 존재하지 않는 리소스
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 정책 1건 (id=1)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1, 99999],
    "active": false
  }
  ```
- **Expected**:
  - Status: 404 NOT_FOUND
  - Error: "환불 정책을 찾을 수 없습니다"
- **DB 검증**: 정책1 상태 변경 안 됨 (트랜잭션 롤백)

##### P1: 비즈니스 규칙 검증 (2개)

**[C3-5] 기본 정책 비활성화 시도 시 409 (POL-DEACT-001)**
- **분류**: 기본 정책 규칙 위반
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**: 활성 기본 정책 1건 (id=1, active=true, defaultPolicy=true)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**:
  - Status: 409 CONFLICT
  - Error: "기본 정책은 비활성화할 수 없습니다"
- **DB 검증**: `active = true` 유지

**[C3-6] 마지막 활성 정책 비활성화 시도 시 409 (POL-DEACT-002)**
- **분류**: 마지막 활성 정책 규칙 위반
- **우선순위**: P1
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **사전 데이터**:
  - 활성 정책 1건만 존재 (id=1, active=true, defaultPolicy=false)
  - 비활성 정책 2건 (id=2,3, active=false)
- **Request**:
  ```json
  PATCH /api/v1/market/sellers/1/refund-policies/status
  {
    "policyIds": [1],
    "active": false
  }
  ```
- **Expected**:
  - Status: 409 CONFLICT
  - Error: "마지막 활성 정책은 비활성화할 수 없습니다"
- **DB 검증**: `active = true` 유지

---

### 🔄 전체 플로우 시나리오: 2개

#### 4.1. CRUD 전체 플로우 (P0)

**[FLOW-1] 생성 → 조회 → 수정 → 상태 변경 전체 플로우**
- **분류**: CRUD 플로우
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **Steps**:
  1. **POST** `/refund-policies` → 정책 생성 (201)
     - Request: `{ "policyName": "테스트 정책", "returnPeriodDays": 7, ... }`
     - Response: `policyId = 1`
  2. **GET** `/refund-policies` → 목록 조회 (200)
     - Response: `content[0].policyId = 1`, `content[0].policyName = "테스트 정책"`
  3. **PUT** `/refund-policies/1` → 정책 수정 (204)
     - Request: `{ "policyName": "수정된 정책", ... }`
  4. **GET** `/refund-policies` → 수정 확인 (200)
     - Response: `content[0].policyName = "수정된 정책"`
  5. **POST** `/refund-policies` → 추가 정책 생성 (201)
     - Response: `policyId = 2`
  6. **PATCH** `/refund-policies/status` → 정책1 비활성화 (204)
     - Request: `{ "policyIds": [1], "active": false }`
  7. **GET** `/refund-policies` → 상태 변경 확인 (200)
     - Response: `content[0].active = true` (정책2), `content[1].active = false` (정책1)

#### 4.2. 기본 정책 전이 플로우 (P0)

**[FLOW-2] 기본 정책 생성 → 기본 정책 전환 → 검증**
- **분류**: 기본 정책 전이 플로우
- **우선순위**: P0
- **인증 컨텍스트**: organizationId="org-seller1" (sellerId=1에 매핑)
- **Steps**:
  1. **POST** `/refund-policies` → 첫 번째 정책 생성 (201)
     - Request: `{ "policyName": "정책1", "defaultPolicy": false, ... }`
     - DB 검증: `defaultPolicy = true` (자동 설정)
  2. **POST** `/refund-policies` → 두 번째 정책 생성 (기본 정책 지정) (201)
     - Request: `{ "policyName": "정책2", "defaultPolicy": true, ... }`
  3. **GET** `/refund-policies` → 기본 정책 전환 확인 (200)
     - Response: `정책1.defaultPolicy = false`, `정책2.defaultPolicy = true`
  4. **PUT** `/refund-policies/1` → 정책1을 다시 기본 정책으로 변경 (204)
     - Request: `{ ..., "defaultPolicy": true }`
  5. **GET** `/refund-policies` → 기본 정책 재전환 확인 (200)
     - Response: `정책1.defaultPolicy = true`, `정책2.defaultPolicy = false`

---

## 3. Fixture 설계

### 필요 Repository 목록
- `RefundPolicyJpaRepository`

### testFixtures 사용
- ✅ `RefundPolicyJpaEntityFixtures` (adapter-out/persistence-mysql/src/testFixtures/)

### Fixture 메서드 활용
| Fixture 메서드 | 용도 | 시나리오 |
|----------------|------|---------|
| `activeEntity()` | 활성 상태 기본 정책 Entity | 기본 조회 테스트 |
| `activeEntity(Long id, Long sellerId)` | 특정 ID/sellerId 활성 Entity | 셀러 격리 테스트 |
| `inactiveEntity()` | 비활성 상태 Entity | 상태 변경 테스트 |
| `deletedEntity()` | 삭제된 Entity | 소프트 삭제 테스트 |
| `newDefaultEntity(Long sellerId)` | 새 기본 정책 Entity (ID null) | 등록 테스트 |
| `newActiveEntityWithName(Long sellerId, String name)` | 이름 지정 Entity | 정렬 테스트 |

### 사전 데이터 설정 방법

#### setUp (테스트 클래스 초기화)
```java
@BeforeEach
void setUp() {
    refundPolicyJpaRepository.deleteAll();
}
```

#### 시나리오별 데이터 설정

**기본 조회 테스트**:
```java
// 활성 정책 3건
RefundPolicyJpaEntity policy1 = refundPolicyJpaRepository.save(
    RefundPolicyJpaEntityFixtures.newDefaultEntity(sellerId)
);
RefundPolicyJpaEntity policy2 = refundPolicyJpaRepository.save(
    RefundPolicyJpaEntityFixtures.newActiveEntityWithName(sellerId, "정책2")
);
RefundPolicyJpaEntity policy3 = refundPolicyJpaRepository.save(
    RefundPolicyJpaEntityFixtures.newActiveEntityWithName(sellerId, "정책3")
);
```

**페이징 테스트**:
```java
// 5건 이상
for (int i = 1; i <= 5; i++) {
    refundPolicyJpaRepository.save(
        RefundPolicyJpaEntityFixtures.newActiveEntityWithName(sellerId, "정책" + i)
    );
}
```

**셀러 격리 테스트**:
```java
// sellerId=1의 정책 3건
refundPolicyJpaRepository.save(RefundPolicyJpaEntityFixtures.newDefaultEntity(1L));
refundPolicyJpaRepository.save(RefundPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책2"));
refundPolicyJpaRepository.save(RefundPolicyJpaEntityFixtures.newActiveEntityWithName(1L, "정책3"));

// sellerId=2의 정책 2건
refundPolicyJpaRepository.save(RefundPolicyJpaEntityFixtures.newDefaultEntity(2L));
refundPolicyJpaRepository.save(RefundPolicyJpaEntityFixtures.newActiveEntityWithName(2L, "정책2"));
```

---

## 4. 시나리오 요약

### 우선순위별 통계
| 우선순위 | Query | Command | 전체 플로우 | 합계 |
|----------|-------|---------|-------------|------|
| **P0** | 8개 | 24개 | 2개 | **34개** |
| **P1** | 3개 | 9개 | 0개 | **12개** |
| **합계** | **11개** | **33개** | **2개** | **46개** |

### 카테고리별 분류
| 카테고리 | 개수 | 시나리오 |
|----------|------|---------|
| 인증/인가 | 16개 | Q0-1~Q0-4, C0-1~C0-4, C2-0-1~C2-0-4, C3-0-1~C3-0-4 |
| 기본 조회 | 2개 | Q1-1, Q1-2 |
| 페이징/정렬 | 3개 | Q1-3, Q1-4, Q1-5 |
| 보안 | 2개 | Q2-1, C2-4 |
| 생성 성공 | 3개 | C1-1, C1-2, C1-3 |
| 수정 성공 | 2개 | C2-1, C2-2 |
| 상태 변경 성공 | 4개 | C3-1, C3-2, C3-3, C3-4 |
| Validation 실패 | 7개 | C1-4~C1-8, C2-6, C2-7 |
| 비즈니스 규칙 위반 | 3개 | C2-5, C3-5, C3-6 |
| 존재하지 않는 리소스 | 2개 | C2-3, C3-4 |
| 전체 플로우 | 2개 | FLOW-1, FLOW-2 |

---

## 5. 체크리스트

### 인증/인가 엔드포인트
- [x] 토큰 없이 요청 → 401 - Q0-1, C0-1, C2-0-1, C3-0-1
- [x] 다른 셀러 리소스 접근 시도 → 403 - Q0-2, C0-2, C2-0-2, C3-0-2
- [x] 권한 없는 사용자 403 - Q0-3, C0-3, C2-0-3, C3-0-3
- [x] superAdmin 바이패스 확인 - Q0-4, C0-4, C2-0-4, C3-0-4
- [x] 리소스 소유자 검증 - 모든 Command 시나리오

### Query 엔드포인트
- [x] 정상 조회 (데이터 있을 때) - Q1-1
- [x] 빈 결과 (데이터 없을 때) - Q1-2
- [x] 페이징 동작 확인 - Q1-3
- [x] 검색 필터 각각 동작 확인 - 해당 없음 (검색 필터 없음)
- [x] 복합 필터 조합 - 해당 없음
- [x] 정렬 동작 확인 - Q1-4, Q1-5
- [x] 셀러 격리 검증 - Q2-1
- [x] 소프트 삭제 제외 확인 - Q2-2

### Command 엔드포인트
- [x] 생성 성공 + DB 검증 - C1-1, C1-2, C1-3
- [x] 필수 필드 누락 → 400 - C1-4
- [x] 잘못된 타입/범위 → 400 - C1-5, C1-6, C1-7, C1-8, C2-6, C2-7
- [x] 수정 성공 + DB 검증 - C2-1, C2-2
- [x] 존재하지 않는 리소스 수정/상태 변경 → 404 - C2-3, C3-4
- [x] 기본 정책 규칙 위반 → 409 - C2-5, C3-5
- [x] 마지막 활성 정책 규칙 위반 → 409 - C3-6

### 전체 플로우
- [x] CRUD 전체 플로우 - FLOW-1
- [x] 기본 정책 전이 플로우 - FLOW-2

---

## 6. 다음 단계

이 시나리오 설계를 기반으로 실제 E2E 통합 테스트 코드를 작성하려면:

```bash
# E2E 테스트 코드 생성 (예시)
# /test-e2e admin:refundpolicy
```

**테스트 패키지 구조**:
```
integration-test/src/test/java/com/ryuqq/marketplace/integration/
└── refundpolicy/
    ├── RefundPolicyAuthIntegrationTest.java          (인증/인가 시나리오)
    ├── RefundPolicyCrudIntegrationTest.java          (CRUD 플로우)
    ├── RefundPolicyQueryIntegrationTest.java         (Query 시나리오)
    ├── RefundPolicyValidationIntegrationTest.java    (Validation 시나리오)
    ├── RefundPolicyBusinessRuleIntegrationTest.java  (비즈니스 규칙)
    └── fixture/
        └── RefundPolicyIntegrationTestFixture.java
```

---

## 문서 정보

- **작성일**: 2026-02-06
- **수정일**: 2026-02-06 (인증/인가 시나리오 추가)
- **대상 도메인**: refundpolicy
- **총 시나리오 개수**: 46개 (P0: 34개, P1: 12개)
- **참조 문서**:
  - api-endpoints: `.claude/docs/api-endpoints/refundpolicy.md`
  - api-flow: `.claude/docs/api-flow/refundpolicy.md`
