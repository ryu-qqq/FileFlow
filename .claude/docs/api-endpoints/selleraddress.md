# SellerAddress API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 3개 |
| **합계** | **4개** |

**Base Path**: `/api/v1/market/sellers/{sellerId}/addresses`

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/sellers/{sellerId}/addresses | SellerAddressQueryController | search | SearchSellerAddressUseCase |

### Q1. search - 셀러 주소 목록 조회

- **Path**: `GET /api/v1/market/sellers/{sellerId}/addresses`
- **Controller**: `SellerAddressQueryController`
- **Summary**: 셀러 주소를 복합 조건으로 페이지 조회
- **Description**: 슈퍼 관리자는 request.sellerIds로 다건 조회 가능하며, 일반 셀러는 Resolver에서 현재 셀러 ID로 고정됨

#### Request Parameters
- **Path Variable**:
  - `sellerId` (Long, Required): 셀러 ID (sellerIds 미입력 시 이 값 1건 사용)

- **Query Parameters** (`SearchSellerAddressesApiRequest`):
  - `sellerIds` (List\<Long\>, Optional): 셀러 ID 목록 (미입력 시 path sellerId 1건 사용, 슈퍼관리자 복수 가능)
  - `addressTypes` (List\<String\>, Optional): 주소 유형 필터 (SHIPPING, RETURN) 복수 선택
  - `defaultAddress` (Boolean, Optional): 기본 주소 필터
  - `searchField` (String, Optional): 검색 필드 (addressName, address 등)
  - `searchWord` (String, Optional): 검색어
  - `page` (Integer, Optional): 페이지 번호 (0-based, default: 0)
  - `size` (Integer, Optional): 페이지 크기 (default: 20)

#### Response
- **Type**: `ApiResponse<PageApiResponse<SellerAddressApiResponse>>`
- **Status**: `200 OK`

#### UseCase
- `SearchSellerAddressUseCase.execute(SellerAddressSearchParams)`

#### 특징
- Resolver 패턴 사용: `SellerIdsResolver`가 권한에 따라 조회 대상 셀러 ID 결정
- 슈퍼 관리자: 다건 조회 가능
- 일반 셀러: 본인 주소만 조회

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/sellers/{sellerId}/addresses | SellerAddressCommandController | register | RegisterSellerAddressUseCase |
| 2 | PUT | /api/v1/market/sellers/{sellerId}/addresses/{addressId} | SellerAddressCommandController | update | UpdateSellerAddressUseCase |
| 3 | PATCH | /api/v1/market/sellers/{sellerId}/addresses/{addressId}/status | SellerAddressCommandController | delete | DeleteSellerAddressUseCase |

### C1. register - 셀러 주소 등록

- **Path**: `POST /api/v1/market/sellers/{sellerId}/addresses`
- **Controller**: `SellerAddressCommandController`
- **Summary**: 새로운 셀러 주소 등록
- **Description**: 새로운 셀러 주소를 등록합니다.

#### Request
- **Path Variable**:
  - `sellerId` (Long, Required): 셀러 ID

- **Body** (`RegisterSellerAddressApiRequest`):
  ```json
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

- **Validation**:
  - `addressType`: @NotBlank
  - `address`: @NotNull @Valid
  - `address.zipCode`: @NotBlank
  - `address.line1`: @NotBlank

#### Response
- **Type**: `ApiResponse<RegisterSellerAddressApiResponse>`
- **Status**: `201 Created`
- **Body**:
  ```json
  {
    "data": {
      "addressId": 123
    }
  }
  ```

#### UseCase
- `RegisterSellerAddressUseCase.execute(RegisterSellerAddressCommand)`

---

### C2. update - 셀러 주소 수정

- **Path**: `PUT /api/v1/market/sellers/{sellerId}/addresses/{addressId}`
- **Controller**: `SellerAddressCommandController`
- **Summary**: 셀러 주소 수정
- **Description**: 기존 셀러 주소의 정보를 수정합니다. 기본 주소 전환도 포함됩니다.

#### Request
- **Path Variable**:
  - `sellerId` (Long, Required): 셀러 ID
  - `addressId` (Long, Required): 주소 ID

- **Body** (`UpdateSellerAddressApiRequest`):
  ```json
  {
    "addressName": "물류센터",
    "address": {
      "zipCode": "06164",
      "line1": "서울 강남구 역삼로 123",
      "line2": "5층"
    },
    "defaultAddress": true
  }
  ```

- **Validation**:
  - `address`: @NotNull @Valid
  - `address.zipCode`: @NotBlank
  - `address.line1`: @NotBlank

#### Response
- **Status**: `204 No Content`

#### UseCase
- `UpdateSellerAddressUseCase.execute(UpdateSellerAddressCommand)`

#### 특징
- `addressName`: Optional, 생략 가능
- `defaultAddress`: Optional, Boolean으로 변경. 생략 시 변경 없음

---

### C3. delete - 셀러 주소 삭제 (소프트)

- **Path**: `PATCH /api/v1/market/sellers/{sellerId}/addresses/{addressId}/status`
- **Controller**: `SellerAddressCommandController`
- **Summary**: 셀러 주소 소프트 삭제
- **Description**: 주소를 소프트 삭제 처리합니다. 기본 주소는 삭제할 수 없습니다.

#### Request
- **Path Variable**:
  - `sellerId` (Long, Required): 셀러 ID
  - `addressId` (Long, Required): 주소 ID

#### Response
- **Status**: `204 No Content`

#### UseCase
- `DeleteSellerAddressUseCase.execute(DeleteSellerAddressCommand)`

#### 비즈니스 규칙
- **기본 주소 삭제 불가**: 기본 주소는 삭제할 수 없음 (400 Bad Request)
- **소프트 삭제**: DELETE HTTP 메서드 대신 PATCH /status 사용 (API-CTR-002 규칙 준수)

---

## 아키텍처 특징

### CQRS 분리
- **Query Controller**: `SellerAddressQueryController`
  - 조회 전용 (GET)
  - PageApiResponse 반환

- **Command Controller**: `SellerAddressCommandController`
  - 생성/수정/삭제 (POST, PUT, PATCH)
  - ID 반환 또는 204 No Content

### 규칙 준수

| 규칙 | 적용 내용 |
|------|----------|
| API-CTR-001 | @RestController 사용 |
| API-CTR-002 | DELETE 메서드 금지 → PATCH /status 사용 |
| API-CTR-003 | UseCase(Port-In) 인터페이스 의존 |
| API-CTR-004 | ResponseEntity\<ApiResponse\<T\>\> 래핑 |
| API-CTR-005 | Controller에서 @Transactional 금지 |
| API-CTR-007 | 비즈니스 로직 포함 금지 |
| API-CTR-009 | @Valid 어노테이션 필수 |
| API-CTR-010 | CQRS Controller 분리 |
| API-CTR-011 | List 직접 반환 금지 → PageApiResponse 사용 |
| API-CTR-012 | URL 경로 소문자 + 복수형 |

### Resolver 패턴
- **SellerIdsResolver**: 권한에 따른 조회 대상 셀러 ID 결정
  - 슈퍼 관리자: `request.sellerIds` 다건 조회 가능
  - 일반 셀러: path `sellerId`로 고정

### Nested Resource
- 셀러와 주소의 1:N 관계를 URL 경로에 반영
- `/sellers/{sellerId}/addresses` 형태의 Nested Resource 구조

---

## 도메인 모델

### AddressType (주소 유형)
- `SHIPPING`: 출고 주소
- `RETURN`: 반품 주소

### Address (주소 정보)
- `zipCode`: 우편번호
- `line1`: 도로명주소
- `line2`: 상세주소

### 주요 속성
- `addressId`: 주소 ID (PK)
- `sellerId`: 셀러 ID (FK)
- `addressType`: 주소 유형 (SHIPPING, RETURN)
- `addressName`: 주소명 (Optional)
- `address`: 주소 정보 (우편번호, 도로명주소, 상세주소)
- `defaultAddress`: 기본 주소 여부
- `deletedAt`: 소프트 삭제 시각

---

## 에러 응답

| HTTP Status | 상황 | 설명 |
|-------------|------|------|
| 400 Bad Request | 잘못된 요청 | Validation 실패, 기본 주소 삭제 시도 |
| 404 Not Found | 리소스 없음 | 주소 ID가 존재하지 않음 |

---

## 연관 모듈

| 모듈 | 역할 |
|------|------|
| `SellerAddressCommandApiMapper` | Command API Request → Command DTO 변환 |
| `SellerAddressQueryApiMapper` | Query API Request → SearchParams 변환, Result → Response 변환 |
| `SellerIdsResolver` | 권한에 따른 조회 대상 셀러 ID 결정 (슈퍼관리자/일반셀러 분기) |
| `SellerAddressErrorMapper` | 도메인 예외 → HTTP 응답 변환 |
