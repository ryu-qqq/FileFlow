# SellerApplication API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 3개 |
| **합계** | **5개** |

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/admin/seller-applications | SellerApplicationQueryController | search | SearchSellerApplicationByOffsetUseCase |
| 2 | GET | /api/v1/market/admin/seller-applications/{applicationId} | SellerApplicationQueryController | get | GetSellerApplicationUseCase |

### Q1. search (셀러 입점 신청 목록 조회)

- **Path**: `GET /api/v1/market/admin/seller-applications`
- **Controller**: `SellerApplicationQueryController`
- **Request**: `SearchSellerApplicationsApiRequest` (@ParameterObject, Query String)
  - `status` (List\<String\>): 신청 상태 목록 (PENDING, APPROVED, REJECTED), 복수 선택 가능
  - `searchField` (String): 검색 필드 (sellerName, companyName)
  - `searchWord` (String): 검색어
  - `sortKey` (String): 정렬 키
  - `sortDirection` (String): 정렬 방향 (ASC, DESC)
  - `page` (Integer): 페이지 번호 (0부터 시작, 최소 0)
  - `size` (Integer): 페이지 크기 (최소 1, 최대 100)
- **Response**: `ApiResponse<PageApiResponse<SellerApplicationApiResponse>>`
  - 페이징된 셀러 입점 신청 목록
- **UseCase**: `SearchSellerApplicationByOffsetUseCase`
- **Description**: 입점 신청 목록을 페이지 기반으로 조회합니다.

---

### Q2. get (셀러 입점 신청 상세 조회)

- **Path**: `GET /api/v1/market/admin/seller-applications/{applicationId}`
- **Controller**: `SellerApplicationQueryController`
- **Request**: Path Variable
  - `applicationId` (Long): 신청 ID
- **Response**: `ApiResponse<SellerApplicationApiResponse>`
  - `id` (Long): 신청 ID
  - `sellerInfo` (SellerInfo): 셀러 기본 정보
    - `sellerName` (String): 셀러명
    - `displayName` (String): 표시명
    - `logoUrl` (String): 로고 URL
    - `description` (String): 설명
  - `businessInfo` (BusinessInfo): 사업자 정보
    - `registrationNumber` (String): 사업자등록번호
    - `companyName` (String): 회사명
    - `representative` (String): 대표자명
    - `saleReportNumber` (String): 통신판매업 신고번호
    - `businessAddress` (AddressDetail): 사업장 주소 정보
  - `csContact` (CsContactInfo): CS 연락처 정보
    - `phone` (String): 전화번호
    - `email` (String): 이메일
    - `mobile` (String): 휴대폰번호
  - `contactInfo` (ContactInfo): 담당자 연락처
    - `name` (String): 담당자명
    - `phone` (String): 연락처
    - `email` (String): 이메일
  - `agreement` (AgreementInfo): 동의 정보
    - `agreedAt` (String): 동의 일시 (ISO 8601)
    - `termsAgreed` (boolean): 이용약관 동의
    - `privacyAgreed` (boolean): 개인정보 처리방침 동의
  - `status` (String): 신청 상태 (PENDING, APPROVED, REJECTED)
  - `appliedAt` (String): 신청 일시 (ISO 8601)
  - `processedAt` (String): 처리 일시 (ISO 8601)
  - `processedBy` (String): 처리자
  - `rejectionReason` (String): 거절 사유
  - `approvedSellerId` (Long): 승인된 셀러 ID
- **UseCase**: `GetSellerApplicationUseCase`
- **Description**: 특정 입점 신청의 상세 정보를 조회합니다.

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/admin/seller-applications | SellerApplicationCommandController | apply | ApplySellerApplicationUseCase |
| 2 | POST | /api/v1/market/admin/seller-applications/{applicationId}/approve | SellerApplicationCommandController | approve | ApproveSellerApplicationUseCase |
| 3 | POST | /api/v1/market/admin/seller-applications/{applicationId}/reject | SellerApplicationCommandController | reject | RejectSellerApplicationUseCase |

### C1. apply (셀러 입점 신청)

- **Path**: `POST /api/v1/market/admin/seller-applications`
- **Controller**: `SellerApplicationCommandController`
- **Request**: `ApplySellerApplicationApiRequest` (@RequestBody)
  - `sellerInfo` (SellerInfo, 필수): 셀러 기본 정보
    - `sellerName` (String, 필수): 셀러명 (최대 100자)
    - `displayName` (String): 표시명 (최대 100자)
    - `logoUrl` (String): 로고 URL (최대 500자)
    - `description` (String): 설명 (최대 1000자)
  - `businessInfo` (BusinessInfo, 필수): 사업자 정보
    - `registrationNumber` (String, 필수): 사업자등록번호 (최대 20자)
    - `companyName` (String, 필수): 회사명 (최대 100자)
    - `representative` (String, 필수): 대표자명 (최대 50자)
    - `saleReportNumber` (String): 통신판매업 신고번호 (최대 50자)
    - `businessAddress` (AddressDetail, 필수): 사업장 주소 정보
      - `zipCode` (String, 필수): 우편번호 (최대 10자)
      - `line1` (String, 필수): 주소1 (최대 200자)
      - `line2` (String): 주소2 (최대 200자)
  - `csContact` (CsContactInfo, 필수): CS 연락처 정보
    - `phone` (String, 필수): 전화번호 (최대 20자)
    - `email` (String, 필수): 이메일 (최대 100자, 이메일 형식)
    - `mobile` (String): 휴대폰번호 (최대 20자)
  - `contactInfo` (ContactInfo): 담당자 연락처
    - `name` (String): 담당자명 (최대 50자)
    - `phone` (String): 연락처 (최대 20자)
    - `email` (String): 이메일 (최대 100자, 이메일 형식)
  - `settlementInfo` (SettlementInfo, 필수): 정산 정보
    - `bankCode` (String): 은행 코드 (최대 10자)
    - `bankName` (String, 필수): 은행명 (최대 50자)
    - `accountNumber` (String, 필수): 계좌번호 (최대 30자, 숫자만)
    - `accountHolderName` (String, 필수): 예금주 (최대 50자)
    - `settlementCycle` (String): 정산 주기 (WEEKLY, BIWEEKLY, MONTHLY, 기본값 MONTHLY)
    - `settlementDay` (Integer): 정산일 (1-31, 기본값 1)
- **Response**: `ApiResponse<ApplySellerApplicationApiResponse>` (HTTP 201 Created)
  - `applicationId` (Long): 생성된 신청 ID
- **UseCase**: `ApplySellerApplicationUseCase`
- **Description**: 새로운 셀러 입점을 신청합니다.

---

### C2. approve (셀러 입점 신청 승인)

- **Path**: `POST /api/v1/market/admin/seller-applications/{applicationId}/approve`
- **Controller**: `SellerApplicationCommandController`
- **Request**: Path Variable
  - `applicationId` (Long): 신청 ID
- **Response**: `ApiResponse<ApproveSellerApplicationApiResponse>` (HTTP 200 OK)
  - `sellerId` (Long): 생성된 셀러 ID
- **UseCase**: `ApproveSellerApplicationUseCase`
- **Description**: 대기 중인 입점 신청을 승인하고 셀러를 생성합니다.
- **Error Codes**:
  - 400: 잘못된 요청
  - 404: 신청을 찾을 수 없음
  - 409: 이미 처리된 신청

---

### C3. reject (셀러 입점 신청 거절)

- **Path**: `POST /api/v1/market/admin/seller-applications/{applicationId}/reject`
- **Controller**: `SellerApplicationCommandController`
- **Request**:
  - Path Variable: `applicationId` (Long) - 신청 ID
  - Request Body: `RejectSellerApplicationApiRequest`
    - `rejectionReason` (String, 필수): 거절 사유 (최대 500자)
- **Response**: HTTP 204 No Content
- **UseCase**: `RejectSellerApplicationUseCase`
- **Description**: 대기 중인 입점 신청을 거절합니다.
- **Error Codes**:
  - 400: 잘못된 요청
  - 404: 신청을 찾을 수 없음
  - 409: 이미 처리된 신청

---

## 아키텍처 준수 사항

### CQRS 패턴
- **Query Controller**: `SellerApplicationQueryController`
  - GET 메서드만 사용
  - 조회 전용 UseCase 의존
- **Command Controller**: `SellerApplicationCommandController`
  - POST 메서드만 사용
  - 명령 전용 UseCase 의존

### Hexagonal Architecture
- **Adapter-In (REST)**: Controller → UseCase (Port-In)
- **Controller 규칙**:
  - UseCase 인터페이스 의존 (구현체 직접 의존 금지)
  - 비즈니스 로직 포함 금지
  - @Transactional 사용 금지
  - ApiResponse 래핑 필수

### DTO 규칙
- Record 타입 사용
- Jakarta Validation 적용
- 날짜는 String (ISO 8601 형식)
- 중첩 구조는 중첩 Record로 표현

---

## 엔드포인트 URL 구조

```
Base Path: /api/v1/market/admin/seller-applications

Query:
  GET  /                           → 목록 조회
  GET  /{applicationId}            → 상세 조회

Command:
  POST /                           → 신청 생성
  POST /{applicationId}/approve    → 승인
  POST /{applicationId}/reject     → 거절
```

---

## 관련 문서

- **도메인 모델**: `domain/src/main/java/com/ryuqq/marketplace/domain/sellerapplication/`
- **Application Layer**: `application/src/main/java/com/ryuqq/marketplace/application/sellerapplication/`
- **Persistence**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/sellerapplication/`

---

**분석 일시**: 2026-02-06
**대상 모듈**: adapter-in/rest-api
**도메인**: SellerApplication (셀러 입점 신청)
