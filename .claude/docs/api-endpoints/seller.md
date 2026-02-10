# Seller API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 3개 |
| **합계** | **5개** |

**Base Path**: `/api/v1/market/admin/sellers`

---

## Query Endpoints

### Q1. getSeller (셀러 상세 조회)

- **Path**: `GET /api/v1/market/admin/sellers/{sellerId}`
- **Controller**: `SellerQueryController`
- **Method**: `getSeller(Long sellerId)`
- **Request**: `@PathVariable Long sellerId`
- **Response**: `ApiResponse<SellerDetailApiResponse>`
- **UseCase**: `GetSellerForAdminUseCase`

**설명**: 셀러 ID로 상세 정보(기본 정보, 주소, 사업자 정보, CS 정보, 계약 정보, 정산 정보)를 조회합니다.

**Response 구조**:
```json
{
  "seller": {
    "id": 1,
    "sellerName": "테스트셀러",
    "displayName": "테스트 브랜드",
    "logoUrl": "https://example.com/logo.png",
    "description": "테스트 셀러 설명입니다.",
    "active": true,
    "createdAt": "2025-01-23T10:30:00+09:00",
    "updatedAt": "2025-01-23T10:30:00+09:00"
  },
  "businessInfo": {
    "id": 1,
    "registrationNumber": "123-45-67890",
    "companyName": "테스트컴퍼니",
    "representative": "홍길동",
    "saleReportNumber": "제2025-서울강남-1234호",
    "businessZipcode": "12345",
    "businessAddress": "서울시 강남구",
    "businessAddressDetail": "테헤란로 123"
  },
  "csInfo": {
    "id": 1,
    "csPhone": "02-1234-5678",
    "csMobile": "010-1234-5678",
    "csEmail": "cs@example.com",
    "operatingStartTime": "09:00",
    "operatingEndTime": "18:00",
    "operatingDays": "MON,TUE,WED,THU,FRI",
    "kakaoChannelUrl": "https://pf.kakao.com/xxx"
  },
  "contractInfo": {
    "id": 1,
    "commissionRate": "10.5",
    "contractStartDate": "2025-01-01",
    "contractEndDate": "2025-12-31",
    "status": "ACTIVE",
    "specialTerms": "신규 셀러 수수료 할인",
    "createdAt": "2025-01-23T10:30:00+09:00",
    "updatedAt": "2025-01-23T10:30:00+09:00"
  },
  "settlementInfo": {
    "id": 1,
    "bankCode": "088",
    "bankName": "신한은행",
    "accountNumber": "123-456-789",
    "accountHolderName": "테스트컴퍼니",
    "settlementCycle": "MONTHLY",
    "settlementDay": 15,
    "verified": true,
    "verifiedAt": "2025-01-23T10:30:00+09:00",
    "createdAt": "2025-01-23T10:30:00+09:00",
    "updatedAt": "2025-01-23T10:30:00+09:00"
  }
}
```

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 셀러를 찾을 수 없음

---

### Q2. searchSellersByOffset (셀러 목록 검색)

- **Path**: `GET /api/v1/market/admin/sellers`
- **Controller**: `SellerQueryController`
- **Method**: `searchSellersByOffset(SearchSellersApiRequest request)`
- **Request**: `@ParameterObject SearchSellersApiRequest` (Query Parameters)
- **Response**: `ApiResponse<PageApiResponse<SellerApiResponse>>`
- **UseCase**: `SearchSellerByOffsetUseCase`

**설명**: 복합 조건으로 셀러 목록을 페이지 기반(Offset)으로 검색합니다.

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `active` | Boolean | X | 활성화 여부 (null: 전체) | `true` |
| `searchField` | String | X | 검색 필드 (sellerId, sellerName) | `sellerName` |
| `searchWord` | String | X | 검색어 | `테스트` |
| `sortKey` | String | X | 정렬 기준 | `createdAt` |
| `sortDirection` | String | X | 정렬 방향 (ASC/DESC) | `DESC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작, 기본값: 0) | `0` |
| `size` | Integer | X | 페이지 크기 (1~100, 기본값: 20) | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "id": 1,
      "sellerName": "테스트셀러",
      "displayName": "테스트 브랜드",
      "logoUrl": "https://example.com/logo.png",
      "description": "테스트 셀러 설명입니다.",
      "active": true,
      "createdAt": "2025-01-23T10:30:00+09:00",
      "updatedAt": "2025-01-23T10:30:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

## Command Endpoints

### C1. registerSeller (셀러 등록)

- **Path**: `POST /api/v1/market/admin/sellers`
- **Controller**: `SellerCommandController`
- **Method**: `registerSeller(RegisterSellerApiRequest request)`
- **Request**: `@RequestBody RegisterSellerApiRequest`
- **Response**: `ApiResponse<SellerIdApiResponse>`
- **UseCase**: `RegisterSellerUseCase`

**설명**: 새로운 셀러를 등록합니다. (최고 마스터 권한 필요)

**Request Body**:
```json
{
  "seller": {
    "sellerName": "테스트셀러",
    "displayName": "테스트 브랜드",
    "logoUrl": "https://example.com/logo.png",
    "description": "테스트 셀러 설명입니다."
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

**Response**:
```json
{
  "sellerId": 1
}
```

**HTTP Status**:
- `201 CREATED`: 등록 성공
- `400 BAD REQUEST`: 잘못된 요청

**TODO**: 권한 체크 구현 필요 (Gateway 또는 @PreAuthorize)

---

### C2. updateSellerFull (셀러 전체정보 수정)

- **Path**: `PUT /api/v1/market/admin/sellers/{sellerId}`
- **Controller**: `SellerCommandController`
- **Method**: `updateSellerFull(Long sellerId, UpdateSellerFullApiRequest request)`
- **Request**:
  - `@PathVariable Long sellerId`
  - `@RequestBody UpdateSellerFullApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateSellerFullUseCase`

**설명**: 셀러의 기본정보, 사업자정보, CS정보, 계약정보, 정산정보를 한번에 수정합니다. (최고 마스터 권한 필요)

**Request Body**:
```json
{
  "seller": {
    "sellerName": "테스트셀러",
    "displayName": "테스트 브랜드",
    "logoUrl": "https://example.com/logo.png",
    "description": "테스트 셀러 설명입니다."
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
    }
  },
  "csInfo": {
    "phone": "02-1234-5678",
    "email": "cs@example.com",
    "mobile": "010-1234-5678",
    "operatingStartTime": "09:00",
    "operatingEndTime": "18:00",
    "operatingDays": "MON,TUE,WED,THU,FRI",
    "kakaoChannelUrl": "https://pf.kakao.com/xxx"
  },
  "contractInfo": {
    "commissionRate": 10.5,
    "contractStartDate": "2025-01-01",
    "contractEndDate": "2025-12-31",
    "specialTerms": "특별 계약 조건입니다."
  },
  "settlementInfo": {
    "bankAccount": {
      "bankCode": "004",
      "bankName": "국민은행",
      "accountNumber": "12345678901234",
      "accountHolderName": "홍길동"
    },
    "settlementCycle": "MONTHLY",
    "settlementDay": 15
  }
}
```

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 셀러를 찾을 수 없음

**TODO**:
- 권한 체크 구현 필요 (Gateway 또는 @PreAuthorize)
- 계약정보(ContractInfo), 정산정보(SettlementInfo) UseCase 구현 후 추가 예정

---

### C3. updateSeller (셀러 기본정보 수정)

- **Path**: `PATCH /api/v1/market/admin/sellers/{sellerId}`
- **Controller**: `SellerCommandController`
- **Method**: `updateSeller(Long sellerId, UpdateSellerApiRequest request)`
- **Request**:
  - `@PathVariable Long sellerId`
  - `@RequestBody UpdateSellerApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateSellerUseCase`

**설명**: 셀러의 기본정보(셀러명, 표시명, 로고, 설명)만 수정합니다. 선택적으로 주소, CS정보, 사업자정보도 수정 가능합니다.

**Request Body**:
```json
{
  "sellerName": "테스트셀러",
  "displayName": "테스트 브랜드",
  "logoUrl": "https://example.com/logo.png",
  "description": "테스트 셀러 설명입니다.",
  "address": {
    "zipCode": "12345",
    "line1": "서울시 강남구",
    "line2": "테헤란로 123"
  },
  "csInfo": {
    "phone": "02-1234-5678",
    "email": "cs@example.com",
    "mobile": "010-1234-5678"
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
    }
  }
}
```

**필드 설명**:
- 기본정보 (sellerName, displayName, logoUrl, description): **필수**
- 하위 정보 (address, csInfo, businessInfo): **선택적** (null 허용)

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 셀러를 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/sellers/{sellerId}` | SellerQueryController | GetSellerForAdminUseCase | Query |
| Q2 | GET | `/sellers` | SellerQueryController | SearchSellerByOffsetUseCase | Query |
| C1 | POST | `/sellers` | SellerCommandController | RegisterSellerUseCase | Command |
| C2 | PUT | `/sellers/{sellerId}` | SellerCommandController | UpdateSellerFullUseCase | Command |
| C3 | PATCH | `/sellers/{sellerId}` | SellerCommandController | UpdateSellerUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-002**: DELETE 메서드 금지 (소프트 삭제는 PATCH 사용)
- ✅ **API-CTR-003**: UseCase (Port-In) 인터페이스 의존
- ✅ **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑
- ✅ **API-CTR-005**: Controller에 `@Transactional` 금지
- ✅ **API-CTR-007**: Controller에 비즈니스 로직 포함 금지
- ✅ **API-CTR-009**: `@Valid` 어노테이션 필수
- ✅ **API-CTR-010**: CQRS Controller 분리 (Query/Command)
- ✅ **API-CTR-011**: List 직접 반환 금지 → PageApiResponse 페이징 필수
- ✅ **API-CTR-012**: URL 경로 소문자 + 복수형 (`/sellers`)
- ✅ **API-CTR-013**: 복합 조건 + Offset 페이징은 `searchSellersByOffset` 네이밍

### DTO 규칙
- ✅ **API-DTO-001**: Record 타입 필수
- ✅ **API-DTO-003**: Validation 어노테이션 사용
- ✅ **API-DTO-004**: createdAt/updatedAt 필수
- ✅ **API-DTO-005**: 날짜 String 변환 필수
- ✅ **API-DTO-006**: 복잡한 구조는 중첩 Record로 표현
- ✅ **API-DTO-007**: `@Schema` 어노테이션 사용
- ✅ **API-DTO-010**: Offset 페이징은 `Search{Bc}ApiRequest` 네이밍

### 엔드포인트 규칙
- ✅ **API-END-001**: Endpoints final class + private 생성자
- ✅ **API-END-002**: static final 상수 사용
- ✅ **API-END-003**: Path Variable 상수화

---

## 관련 UseCase

### Command UseCases
- `RegisterSellerUseCase`: 셀러 등록
- `UpdateSellerUseCase`: 셀러 기본정보 수정
- `UpdateSellerFullUseCase`: 셀러 전체정보 수정
- `UpdateSellerBusinessInfoUseCase`: 셀러 사업자정보 수정 (내부용)
- `UpdateSellerCsUseCase`: 셀러 CS정보 수정 (내부용)
- `ProcessPendingOutboxUseCase`: Outbox 처리 (Batch)
- `RecoverTimeoutOutboxUseCase`: Timeout Outbox 복구 (Batch)

### Query UseCases
- `GetSellerForAdminUseCase`: 관리자용 셀러 상세 조회
- `GetSellerForCustomerUseCase`: 고객용 셀러 조회
- `SearchSellerByOffsetUseCase`: 셀러 검색 (Offset 페이징)

---

## 권한 요구사항 (TODO)

현재 권한 체크가 구현되지 않았습니다. 향후 다음 중 하나로 구현 예정:

1. **Gateway 레벨 권한 체크** (권장)
2. **@PreAuthorize 어노테이션** (Spring Security)

### 권한 레벨
- **SUPER_ADMIN**:
  - C1 (registerSeller) - 셀러 등록
  - C2 (updateSellerFull) - 셀러 전체정보 수정
- **ADMIN**:
  - C3 (updateSeller) - 셀러 기본정보 수정
  - Q1 (getSeller) - 셀러 상세 조회
  - Q2 (searchSellersByOffset) - 셀러 목록 검색

---

## 향후 개선사항

1. **권한 체크 구현**: Gateway 또는 @PreAuthorize
2. **ContractInfo/SettlementInfo UseCase 구현**: C2 엔드포인트 활성화
3. **Rate Limiting**: API 호출 제한
4. **Monitoring**: API 성능 모니터링 (APM)
5. **API Documentation**: Swagger/OpenAPI 문서 자동 생성

---

**분석 일시**: 2026-02-06
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/seller/`
**총 엔드포인트**: 5개 (Query 2개, Command 3개)
