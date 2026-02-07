# SellerAddress E2E 통합 테스트 시나리오

## 요약

| 분류 | 시나리오 수 |
|------|------------|
| 인증/인가 시나리오 | 11개 |
| Query 시나리오 | 8개 |
| Command 시나리오 | 16개 |
| 통합 플로우 시나리오 | 3개 |
| **총계** | **38개** |

**우선순위 분류**:
- P0 (필수): 29개
- P1 (중요): 7개
- P2 (권장): 2개

---

## 0. 인증/인가 시나리오

### A1. 토큰 없이 요청 - 401 Unauthorized

**Priority**: P0

**적용 대상**: 모든 엔드포인트

**요청 예시** (주소 목록 조회):
```http
GET /api/v1/market/sellers/{sellerId}/addresses
# Authorization 헤더 없음
```

**기대 결과**:
- Status: 401 Unauthorized
- 에러 메시지: "인증이 필요합니다"

---

### A2. 다른 셀러의 주소 조회 시도 - 403 Forbidden

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller B (sellerId=200) 생성
- Seller A의 주소 5개 생성
- Seller B로 인증 (organizationId → sellerId=200)

**요청**:
```http
GET /api/v1/market/sellers/100/addresses
Authorization: Bearer {sellerB_token}
```

**기대 결과**:
- Status: 403 Forbidden
- 에러 메시지: "접근 권한이 없습니다"

**검증 사항**:
- `MarketAccessChecker.isSellerOwnerOr` 실패
- URL의 sellerId(100)와 인증 사용자의 sellerId(200) 불일치

---

### A3. superAdmin 바이패스 - 다른 셀러 주소 조회 가능

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller A의 주소 5개 생성
- superAdmin으로 인증

**요청**:
```http
GET /api/v1/market/sellers/100/addresses
Authorization: Bearer {superAdmin_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 5`

**검증 사항**:
- `MarketAccessChecker.superAdmin()` true 반환
- `isSellerOwnerOr` 조기 통과

---

### A4. metadata 조회 - 일반 사용자 403 Forbidden (본인 아닌 경우)

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller B (sellerId=200) 생성
- Seller B로 인증

**요청**:
```http
GET /api/v1/market/sellers/100/addresses/metadata
Authorization: Bearer {sellerB_token}
```

**기대 결과**:
- Status: 403 Forbidden

**검증 사항**:
- `@PreAuthorize("@access.isSellerOwnerOr(#sellerId, 'seller-address:read')")` 실패
- 본인 셀러(200)가 아니므로 거부

---

### A5. metadata 조회 - 본인 셀러 200 OK

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller A의 주소 3개 생성
- Seller A로 인증 (organizationId → sellerId=100)

**요청**:
```http
GET /api/v1/market/sellers/100/addresses/metadata
Authorization: Bearer {sellerA_token}
```

**기대 결과**:
- Status: 200 OK
- 메타데이터 정상 반환

**검증 사항**:
- `isSellerOwnerOr` 성공 (본인 소유)

---

### A6. metadata 조회 - superAdmin 전체 조회 가능

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- superAdmin으로 인증

**요청**:
```http
GET /api/v1/market/sellers/100/addresses/metadata
Authorization: Bearer {superAdmin_token}
```

**기대 결과**:
- Status: 200 OK

**검증 사항**:
- `superAdmin()` true → 바이패스

---

### A7. 주소 등록 - 다른 셀러에 등록 시도 403 Forbidden

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller B (sellerId=200) 생성
- Seller B로 인증

**요청**:
```http
POST /api/v1/market/sellers/100/addresses
Authorization: Bearer {sellerB_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 403 Forbidden

**검증 사항**:
- `@PreAuthorize("@access.isSellerOwnerOr(#sellerId, 'seller-address:write')")` 실패

---

### A8. 주소 등록 - 본인 셀러 201 Created

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller A로 인증

**요청**:
```http
POST /api/v1/market/sellers/100/addresses
Authorization: Bearer {sellerA_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 201 Created
- Response: `{ "data": { "addressId": 123 } }`

---

### A9. 주소 수정 - 다른 셀러 주소 수정 403 Forbidden

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller A의 주소 1개 (addressId=1001)
- Seller B (sellerId=200) 생성
- Seller B로 인증

**요청**:
```http
PUT /api/v1/market/sellers/100/addresses/1001
Authorization: Bearer {sellerB_token}
Content-Type: application/json

{
  "addressName": "물류센터",
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456"
  }
}
```

**기대 결과**:
- Status: 403 Forbidden

---

### A10. 주소 삭제 - 다른 셀러 주소 삭제 403 Forbidden

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller A의 비기본 주소 1개 (addressId=1001)
- Seller B (sellerId=200) 생성
- Seller B로 인증

**요청**:
```http
PATCH /api/v1/market/sellers/100/addresses/1001/status
Authorization: Bearer {sellerB_token}
```

**기대 결과**:
- Status: 403 Forbidden

---

### A11. superAdmin - 모든 Command 작업 가능

**Priority**: P0

**사전 조건**:
- Seller A (sellerId=100) 생성
- Seller A의 주소 1개 (addressId=1001)
- superAdmin으로 인증

**시나리오**:
1. **등록**: POST → 201
2. **수정**: PUT → 204
3. **삭제**: PATCH → 204

**검증 사항**:
- 모든 Command 작업이 `superAdmin()` 바이패스로 성공

---

## 1. Query 시나리오: 주소 목록 조회

### Q1-1. 정상 조회 - 데이터 존재 시 목록 반환

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SellerAddress 5개 생성 (다양한 타입, 기본 주소 포함)

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?page=0&size=10
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- Response: `PageApiResponse<SellerAddressApiResponse>`
- `content.size() >= 5`
- `totalElements = 5`
- 각 주소의 필드 값 확인

**DB 검증**:
- `deleted_at IS NULL` 조건으로만 조회됨

---

### Q1-2. 빈 결과 - 주소 데이터가 없을 때

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SellerAddress 없음

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 0`
- `totalElements = 0`

---

### Q1-3. 필터 - addressType 조건 검색 (SHIPPING)

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 주소 3개
- RETURN 주소 2개

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?addressTypes=SHIPPING
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 3`
- 모든 응답의 `addressType = "SHIPPING"`

---

### Q1-4. 필터 - addressType 복수 조건 검색

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 주소 3개
- RETURN 주소 2개

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?addressTypes=SHIPPING,RETURN
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 5`

---

### Q1-5. 필터 - defaultAddress 조건 검색

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 기본 주소 1개
- SHIPPING 비기본 주소 2개
- RETURN 기본 주소 1개
- RETURN 비기본 주소 1개

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?defaultAddress=true
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 2` (SHIPPING 기본 1개 + RETURN 기본 1개)
- 모든 응답의 `defaultAddress = true`

---

### Q1-6. 필터 - 검색어 조건 (주소명)

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- 주소 5개 생성 (addressName: "본사 창고", "물류센터", "강남센터", "반품센터", "기타")

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?searchField=addressName&searchWord=센터
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 3` (물류센터, 강남센터, 반품센터)

---

### Q1-7. 페이징 - page, size 동작 확인

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SellerAddress 7개 생성

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?page=0&size=3
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 3`
- `totalElements = 7`
- `totalPages = 3`

**추가 요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?page=1&size=3
Authorization: Bearer {seller_token}
```

**기대 결과**:
- `content.size() = 3`
- 다른 ID 값의 주소들 (첫 페이지와 다름)

---

### Q1-8. 복합 필터 - addressType + defaultAddress + 검색어

**Priority**: P2

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 기본 주소 1개 (addressName: "본사 창고")
- SHIPPING 비기본 주소 2개 (addressName: "물류센터", "강남센터")
- RETURN 주소 2개 (addressName: "반품센터", "기타")

**요청**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?addressTypes=SHIPPING&defaultAddress=true&searchWord=창고
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 200 OK
- `content.size() = 1`
- 응답: "본사 창고" (SHIPPING + 기본 + "창고" 포함)

---

## 2. Command 시나리오: 주소 등록

### C1-1. 생성 성공 - 유효한 요청으로 주소 등록

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123",
    "line2": "5층"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 201 Created
- Response: `{ "data": { "addressId": 123 } }`

**DB 검증**:
```sql
SELECT * FROM seller_addresses WHERE id = 123;
```
- `seller_id = {sellerId}`
- `address_type = 'SHIPPING'`
- `address_name = '본사 창고'`
- `is_default = false`
- `deleted_at IS NULL`

---

### C1-2. 생성 성공 - 기본 주소로 등록

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123",
    "line2": "5층"
  },
  "defaultAddress": true
}
```

**기대 결과**:
- Status: 201 Created

**DB 검증**:
- `is_default = true`

---

### C1-3. 기본 주소 자동 전환 - 기존 기본 주소가 있을 때

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 기본 주소 1개 (id=100)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "새 본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123",
    "line2": "5층"
  },
  "defaultAddress": true
}
```

**기대 결과**:
- Status: 201 Created
- Response: `addressId = 101`

**DB 검증**:
```sql
-- 기존 기본 주소 해제 확인
SELECT is_default FROM seller_addresses WHERE id = 100;
-- is_default = false

-- 새 주소가 기본 주소로 설정됨
SELECT is_default FROM seller_addresses WHERE id = 101;
-- is_default = true
```

---

### C1-4. Validation 실패 - addressType 누락

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 400 Bad Request
- 에러 메시지: `addressType은(는) 필수입니다`

---

### C1-5. Validation 실패 - address 누락

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 400 Bad Request

---

### C1-6. Validation 실패 - address.zipCode 누락

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 400 Bad Request

---

### C1-7. Validation 실패 - address.line1 누락

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 400 Bad Request

---

### C1-8. 중복 - 동일 타입 내 주소명 중복

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 주소 1개 (addressName: "본사 창고")

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 456"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 400 Bad Request
- 에러 코드: `ADDR-004`
- 에러 메시지: "동일한 배송지 타입에 같은 이름의 배송지가 이미 존재합니다"

---

### C1-9. 성공 - 다른 타입에서는 동일 주소명 허용

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 주소 1개 (addressName: "본사 창고")

**요청**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressType": "RETURN",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": false
}
```

**기대 결과**:
- Status: 201 Created
- RETURN 타입의 새 주소 생성됨

---

## 3. Command 시나리오: 주소 수정

### C2-1. 수정 성공 - 주소 정보 수정

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 주소 1개 (id=100, addressName: "본사 창고")

**요청**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/100
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressName": "물류센터",
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456",
    "line2": "6층"
  }
}
```

**기대 결과**:
- Status: 204 No Content

**DB 검증**:
```sql
SELECT * FROM seller_addresses WHERE id = 100;
```
- `address_name = '물류센터'`
- `zipcode = '06165'`
- `address = '서울 강남구 역삼로 456'`
- `address_detail = '6층'`

---

### C2-2. 수정 성공 - 기본 주소로 변경

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 기본 주소 1개 (id=100)
- SHIPPING 비기본 주소 1개 (id=101)

**요청**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/101
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressName": "새 기본 주소",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": true
}
```

**기대 결과**:
- Status: 204 No Content

**DB 검증**:
```sql
-- 기존 기본 주소 해제
SELECT is_default FROM seller_addresses WHERE id = 100;
-- is_default = false

-- 새로운 기본 주소 설정
SELECT is_default FROM seller_addresses WHERE id = 101;
-- is_default = true
```

---

### C2-3. 수정 성공 - addressName 생략 (Optional)

**Priority**: P1

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 주소 1개 (id=100)

**요청**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/100
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456"
  }
}
```

**기대 결과**:
- Status: 204 No Content
- 주소 정보만 수정, addressName은 변경 없음

---

### C2-4. 실패 - 존재하지 않는 주소 ID

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/99999
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  }
}
```

**기대 결과**:
- Status: 404 Not Found
- 에러 코드: `ADDR-001`
- 에러 메시지: "셀러 주소를 찾을 수 없습니다"

---

### C2-5. Validation 실패 - address 누락

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/100
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "addressName": "물류센터"
}
```

**기대 결과**:
- Status: 400 Bad Request

---

## 4. Command 시나리오: 주소 삭제

### C3-1. 삭제 성공 - 비기본 주소 소프트 삭제

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 비기본 주소 1개 (id=100)

**요청**:
```http
PATCH /api/v1/market/sellers/{sellerId}/addresses/100/status
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 204 No Content

**DB 검증**:
```sql
SELECT deleted_at FROM seller_addresses WHERE id = 100;
-- deleted_at IS NOT NULL
```

---

### C3-2. 실패 - 기본 주소 삭제 불가

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)
- SHIPPING 기본 주소 1개 (id=100)

**요청**:
```http
PATCH /api/v1/market/sellers/{sellerId}/addresses/100/status
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 400 Bad Request
- 에러 코드: `ADDR-002`
- 에러 메시지: "기본 주소는 삭제할 수 없습니다"

**DB 검증**:
```sql
SELECT deleted_at FROM seller_addresses WHERE id = 100;
-- deleted_at IS NULL (삭제되지 않음)
```

---

### C3-3. 실패 - 존재하지 않는 주소 ID

**Priority**: P0

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**요청**:
```http
PATCH /api/v1/market/sellers/{sellerId}/addresses/99999/status
Authorization: Bearer {seller_token}
```

**기대 결과**:
- Status: 404 Not Found
- 에러 코드: `ADDR-001`

---

## 5. 통합 플로우 시나리오

### F1. CRUD 전체 플로우

**Priority**: P0

**시나리오**:
1. **등록**: POST → 주소 생성 (201, id 반환)
2. **조회**: GET → 생성된 주소 확인 (200)
3. **수정**: PUT → 주소 정보 수정 (204)
4. **조회**: GET → 수정 확인 (200, 수정된 값)
5. **삭제**: PATCH → 소프트 삭제 (204)
6. **조회**: GET → 삭제 확인 (200, content에서 제외됨)

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**Step 1 - 등록**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": false
}
```
- Status: 201
- Response: `addressId = 100`

**Step 2 - 조회**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
```
- Status: 200
- `content[0].id = 100`
- `content[0].addressName = "본사 창고"`

**Step 3 - 수정**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/100
Authorization: Bearer {seller_token}
{
  "addressName": "물류센터",
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456"
  }
}
```
- Status: 204

**Step 4 - 수정 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
```
- Status: 200
- `content[0].addressName = "물류센터"`
- `content[0].address.zipCode = "06165"`

**Step 5 - 삭제**:
```http
PATCH /api/v1/market/sellers/{sellerId}/addresses/100/status
Authorization: Bearer {seller_token}
```
- Status: 204

**Step 6 - 삭제 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
```
- Status: 200
- `content.size() = 0` (삭제된 주소는 조회되지 않음)

---

### F2. 기본 주소 전환 플로우

**Priority**: P0

**시나리오**:
1. **등록**: SHIPPING 주소 1개 (기본 주소) 생성
2. **등록**: SHIPPING 주소 1개 (비기본 주소) 생성
3. **확인**: 기본 주소가 1개만 존재
4. **수정**: 비기본 주소를 기본 주소로 변경
5. **확인**: 기본 주소가 자동 전환됨 (기존 기본 해제 + 새 기본 설정)

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**Step 1 - 첫 번째 주소 등록 (기본 주소)**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
{
  "addressType": "SHIPPING",
  "addressName": "본사 창고",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": true
}
```
- Response: `addressId = 100`

**Step 2 - 두 번째 주소 등록 (비기본 주소)**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
{
  "addressType": "SHIPPING",
  "addressName": "물류센터",
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456"
  },
  "defaultAddress": false
}
```
- Response: `addressId = 101`

**Step 3 - 기본 주소 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?defaultAddress=true&addressTypes=SHIPPING
Authorization: Bearer {seller_token}
```
- `content.size() = 1`
- `content[0].id = 100`
- `content[0].defaultAddress = true`

**Step 4 - 두 번째 주소를 기본 주소로 변경**:
```http
PUT /api/v1/market/sellers/{sellerId}/addresses/101
Authorization: Bearer {seller_token}
{
  "addressName": "물류센터",
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456"
  },
  "defaultAddress": true
}
```
- Status: 204

**Step 5 - 기본 주소 전환 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?defaultAddress=true&addressTypes=SHIPPING
Authorization: Bearer {seller_token}
```
- `content.size() = 1`
- `content[0].id = 101` (기본 주소 변경됨)
- `content[0].defaultAddress = true`

**추가 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
```
- `content.size() = 2`
- id=100: `defaultAddress = false` (기존 기본 주소 해제)
- id=101: `defaultAddress = true` (새 기본 주소)

---

### F3. 타입별 기본 주소 독립 관리 플로우

**Priority**: P1

**시나리오**:
1. SHIPPING 기본 주소 1개 생성
2. RETURN 기본 주소 1개 생성
3. 확인: 각 타입별로 기본 주소 1개씩 존재
4. SHIPPING 기본 주소 변경 → RETURN 기본 주소는 영향 없음

**사전 조건**:
- Seller 1개 생성 (본인 인증)

**Step 1 - SHIPPING 기본 주소 생성**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
{
  "addressType": "SHIPPING",
  "addressName": "출고 주소",
  "address": {
    "zipCode": "06164",
    "line1": "서울 강남구 역삼로 123"
  },
  "defaultAddress": true
}
```
- Response: `addressId = 100`

**Step 2 - RETURN 기본 주소 생성**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
{
  "addressType": "RETURN",
  "addressName": "반품 주소",
  "address": {
    "zipCode": "06165",
    "line1": "서울 강남구 역삼로 456"
  },
  "defaultAddress": true
}
```
- Response: `addressId = 101`

**Step 3 - 타입별 기본 주소 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?defaultAddress=true
Authorization: Bearer {seller_token}
```
- `content.size() = 2`
- SHIPPING 기본 1개 (id=100)
- RETURN 기본 1개 (id=101)

**Step 4 - SHIPPING 새 주소 생성 (기본 주소)**:
```http
POST /api/v1/market/sellers/{sellerId}/addresses
Authorization: Bearer {seller_token}
{
  "addressType": "SHIPPING",
  "addressName": "새 출고 주소",
  "address": {
    "zipCode": "06166",
    "line1": "서울 강남구 역삼로 789"
  },
  "defaultAddress": true
}
```
- Response: `addressId = 102`

**Step 5 - RETURN 기본 주소 유지 확인**:
```http
GET /api/v1/market/sellers/{sellerId}/addresses?defaultAddress=true
Authorization: Bearer {seller_token}
```
- `content.size() = 2`
- SHIPPING 기본: id=102 (변경됨)
- RETURN 기본: id=101 (유지됨)

---

## 6. Fixture 설계

### 필요 Repository
- `SellerJpaRepository`
- `SellerAddressJpaRepository`

### TestFixtures
- `SellerJpaEntityFixtures` (이미 존재)
- `SellerAddressJpaEntityFixtures` (생성 필요)

### 사전 데이터 설정 방법

#### setUp 메서드
```java
@BeforeEach
void setUp() {
    // 모든 테스트 전 데이터 정리
    sellerAddressJpaRepository.deleteAll();
    sellerJpaRepository.deleteAll();
}
```

#### 시나리오별 Fixture 사용

**검색/페이징 테스트**:
```java
SellerJpaEntity seller = sellerJpaRepository.save(SellerJpaEntityFixtures.activeEntity());
Long sellerId = seller.getId();

// 다양한 타입과 상태의 주소 생성
SellerAddressJpaEntity addr1 = SellerAddressJpaEntityFixtures.shippingEntity(sellerId, "본사 창고", true);
SellerAddressJpaEntity addr2 = SellerAddressJpaEntityFixtures.shippingEntity(sellerId, "물류센터", false);
SellerAddressJpaEntity addr3 = SellerAddressJpaEntityFixtures.returnEntity(sellerId, "반품센터", true);
SellerAddressJpaEntity addr4 = SellerAddressJpaEntityFixtures.returnEntity(sellerId, "기타", false);
SellerAddressJpaEntity addr5 = SellerAddressJpaEntityFixtures.deletedEntity(sellerId);

sellerAddressJpaRepository.saveAll(List.of(addr1, addr2, addr3, addr4, addr5));
```

**상세 조회/수정 테스트**:
```java
SellerJpaEntity seller = sellerJpaRepository.save(SellerJpaEntityFixtures.activeEntity());
Long sellerId = seller.getId();

SellerAddressJpaEntity address = sellerAddressJpaRepository.save(
    SellerAddressJpaEntityFixtures.shippingEntity(sellerId, "본사 창고", false)
);
Long addressId = address.getId();
```

**인증 테스트**:
```java
// Seller A (본인)
SellerJpaEntity sellerA = sellerJpaRepository.save(SellerJpaEntityFixtures.activeEntity());
Long sellerIdA = sellerA.getId();

// Seller B (타인)
SellerJpaEntity sellerB = sellerJpaRepository.save(SellerJpaEntityFixtures.activeEntity());
Long sellerIdB = sellerB.getId();

// MockMvc + WithMockJwtAuth로 인증 컨텍스트 설정
// @WithMockJwtAuth(organizationId = "org-A") → sellerId=sellerIdA로 매핑
```

---

## 7. SellerAddressJpaEntityFixtures 설계안

```java
package com.ryuqq.marketplace.adapter.out.persistence.selleraddress;

import com.ryuqq.marketplace.adapter.out.persistence.selleraddress.entity.SellerAddressJpaEntity;
import java.time.Instant;

public final class SellerAddressJpaEntityFixtures {

    private SellerAddressJpaEntityFixtures() {}

    // 기본 상수
    public static final String DEFAULT_ADDRESS_TYPE_SHIPPING = "SHIPPING";
    public static final String DEFAULT_ADDRESS_TYPE_RETURN = "RETURN";
    public static final String DEFAULT_ADDRESS_NAME = "테스트 주소";
    public static final String DEFAULT_ZIPCODE = "06164";
    public static final String DEFAULT_ADDRESS = "서울 강남구 역삼로 123";
    public static final String DEFAULT_ADDRESS_DETAIL = "5층";

    /** SHIPPING 주소 Entity 생성. */
    public static SellerAddressJpaEntity shippingEntity(Long sellerId, String addressName, boolean isDefault) {
        Instant now = Instant.now();
        return SellerAddressJpaEntity.create(
                null,
                sellerId,
                DEFAULT_ADDRESS_TYPE_SHIPPING,
                addressName,
                DEFAULT_ZIPCODE,
                DEFAULT_ADDRESS,
                DEFAULT_ADDRESS_DETAIL,
                isDefault,
                now,
                now,
                null);
    }

    /** RETURN 주소 Entity 생성. */
    public static SellerAddressJpaEntity returnEntity(Long sellerId, String addressName, boolean isDefault) {
        Instant now = Instant.now();
        return SellerAddressJpaEntity.create(
                null,
                sellerId,
                DEFAULT_ADDRESS_TYPE_RETURN,
                addressName,
                DEFAULT_ZIPCODE,
                DEFAULT_ADDRESS,
                DEFAULT_ADDRESS_DETAIL,
                isDefault,
                now,
                now,
                null);
    }

    /** 삭제된 주소 Entity 생성. */
    public static SellerAddressJpaEntity deletedEntity(Long sellerId) {
        Instant now = Instant.now();
        return SellerAddressJpaEntity.create(
                null,
                sellerId,
                DEFAULT_ADDRESS_TYPE_SHIPPING,
                "삭제된 주소",
                DEFAULT_ZIPCODE,
                DEFAULT_ADDRESS,
                DEFAULT_ADDRESS_DETAIL,
                false,
                now,
                now,
                now); // deletedAt 설정
    }

    /** ID를 지정한 SHIPPING 주소 Entity 생성. */
    public static SellerAddressJpaEntity shippingEntityWithId(Long id, Long sellerId, String addressName, boolean isDefault) {
        Instant now = Instant.now();
        return SellerAddressJpaEntity.create(
                id,
                sellerId,
                DEFAULT_ADDRESS_TYPE_SHIPPING,
                addressName,
                DEFAULT_ZIPCODE,
                DEFAULT_ADDRESS,
                DEFAULT_ADDRESS_DETAIL,
                isDefault,
                now,
                now,
                null);
    }

    /** 커스텀 주소 정보를 가진 Entity 생성. */
    public static SellerAddressJpaEntity customEntity(
            Long sellerId,
            String addressType,
            String addressName,
            String zipcode,
            String address,
            String addressDetail,
            boolean isDefault) {
        Instant now = Instant.now();
        return SellerAddressJpaEntity.create(
                null,
                sellerId,
                addressType,
                addressName,
                zipcode,
                address,
                addressDetail,
                isDefault,
                now,
                now,
                null);
    }
}
```

---

## 8. 체크리스트

### 인증/인가 엔드포인트
- [x] 토큰 없이 요청 → 401
- [x] 다른 셀러의 리소스 접근 시도 → 403
- [x] superAdmin 바이패스로 모든 셀러 접근 가능
- [x] metadata 조회 - 본인 셀러만 가능 (superAdmin 제외)
- [x] Command 작업 - 본인 셀러만 가능 (superAdmin 제외)

### Query 엔드포인트
- [x] 정상 조회 (데이터 있을 때)
- [x] 빈 결과 (데이터 없을 때)
- [x] 검색 필터 각각 동작 확인 (addressType, defaultAddress, searchWord)
- [x] 페이징 동작 확인 (totalElements, content.size)
- [x] 복합 필터 조합

### Command 엔드포인트
- [x] 생성 성공 + DB 검증
- [x] 필수 필드 누락 → 400
- [x] 잘못된 타입 → 400
- [x] 수정 성공 + DB 검증
- [x] 존재하지 않는 리소스 수정/삭제 → 404
- [x] 중복 생성 → 400 (동일 타입 내 주소명 중복)
- [x] 기본 주소 삭제 → 400

### 전체 플로우
- [x] CRUD 전체 플로우
- [x] 기본 주소 전환 플로우
- [x] 타입별 기본 주소 독립 관리

### 비즈니스 규칙
- [x] 기본 주소 삭제 불가
- [x] 타입별 기본 주소 자동 전환 (등록/수정 시)
- [x] 타입별 기본 주소 독립 관리 (SHIPPING ≠ RETURN)
- [x] 동일 타입 내 주소명 중복 불가
- [x] 소프트 삭제 (deleted_at)

---

## 9. 우선순위별 시나리오 요약

### P0 (필수) - 29개
- 인증/인가 시나리오: A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11
- Query 시나리오: Q1-1, Q1-2, Q1-7
- Command 시나리오: C1-1, C1-2, C1-3, C1-4, C1-5, C1-6, C1-7, C2-1, C2-2, C2-4, C2-5, C3-1, C3-2, C3-3
- 통합 플로우: F1, F2

### P1 (중요) - 7개
- Query 시나리오: Q1-3, Q1-4, Q1-5, Q1-6
- Command 시나리오: C1-8, C1-9, C2-3
- 통합 플로우: F3

### P2 (권장) - 2개
- Query 시나리오: Q1-8

---

**분석 완료일**: 2026-02-06 (인증/인가 시나리오 추가)
**담당자**: Claude (Test Scenario Designer)
