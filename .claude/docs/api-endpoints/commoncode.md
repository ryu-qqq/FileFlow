# CommonCode API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 3개 |
| **합계** | **4개** |

**Base Path**: `/api/v1/market/admin/common-codes`

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/admin/common-codes | CommonCodeQueryController | search | SearchCommonCodeUseCase |

### Q1. search - 공통 코드 목록 조회

- **Path**: `GET /api/v1/market/admin/common-codes`
- **Controller**: `CommonCodeQueryController`
- **Method**: `search(SearchCommonCodesPageApiRequest request)`
- **Request**: `SearchCommonCodesPageApiRequest` (@ModelAttribute - Query String)
  - `commonCodeTypeId`: Long (필수) - 공통 코드 타입 ID
  - `active`: Boolean (옵션) - 활성화 여부 필터
  - `code`: String (옵션) - 코드 검색 (부분 일치)
  - `sortKey`: String (옵션) - 정렬 키 (CREATED_AT, DISPLAY_ORDER, CODE)
  - `sortDirection`: String (옵션) - 정렬 방향 (ASC, DESC)
  - `page`: Integer (옵션, 기본값 0) - 페이지 번호
  - `size`: Integer (옵션, 1-100) - 페이지 크기
- **Response**: `ApiResponse<PageApiResponse<CommonCodeApiResponse>>`
  - `CommonCodeApiResponse`:
    - `id`: Long - 공통 코드 ID
    - `commonCodeTypeId`: Long - 공통 코드 타입 ID
    - `code`: String - 코드값
    - `displayName`: String - 표시명
    - `displayOrder`: int - 표시 순서
    - `active`: boolean - 활성화 여부
    - `createdAt`: String (ISO 8601) - 생성일시
    - `updatedAt`: String (ISO 8601) - 수정일시
- **UseCase**: `SearchCommonCodeUseCase`
- **설명**: 타입별 공통 코드 목록을 페이지 기반으로 조회합니다. 활성화 여부, 코드 검색을 지원합니다.

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/admin/common-codes | CommonCodeCommandController | register | RegisterCommonCodeUseCase |
| 2 | PUT | /api/v1/market/admin/common-codes/{id} | CommonCodeCommandController | update | UpdateCommonCodeUseCase |
| 3 | PATCH | /api/v1/market/admin/common-codes/active-status | CommonCodeCommandController | changeActiveStatus | ChangeCommonCodeStatusUseCase |

### C1. register - 공통 코드 등록

- **Path**: `POST /api/v1/market/admin/common-codes`
- **Controller**: `CommonCodeCommandController`
- **Method**: `register(RegisterCommonCodeApiRequest request)`
- **Request**: `RegisterCommonCodeApiRequest` (@RequestBody)
  - `commonCodeTypeId`: Long (필수) - 공통 코드 타입 ID
  - `code`: String (필수, max 50) - 코드
  - `displayName`: String (필수, max 100) - 표시명
  - `displayOrder`: int (필수, ≥0) - 표시 순서
- **Response**: `ApiResponse<Long>` - 생성된 공통 코드 ID
- **HTTP Status**: 201 Created
- **UseCase**: `RegisterCommonCodeUseCase`
- **설명**: 새로운 공통 코드를 등록합니다.

### C2. update - 공통 코드 수정

- **Path**: `PUT /api/v1/market/admin/common-codes/{id}`
- **Controller**: `CommonCodeCommandController`
- **Method**: `update(Long id, UpdateCommonCodeApiRequest request)`
- **Request**:
  - Path Variable: `id` (Long) - 공통 코드 ID
  - Request Body: `UpdateCommonCodeApiRequest`
    - `displayName`: String (필수, max 100) - 표시명
    - `displayOrder`: int (필수, ≥0) - 표시 순서
- **Response**: `ApiResponse<Void>`
- **UseCase**: `UpdateCommonCodeUseCase`
- **설명**: 공통 코드의 표시명과 순서를 수정합니다.

### C3. changeActiveStatus - 공통 코드 활성화 상태 변경

- **Path**: `PATCH /api/v1/market/admin/common-codes/active-status`
- **Controller**: `CommonCodeCommandController`
- **Method**: `changeActiveStatus(ChangeActiveStatusApiRequest request)`
- **Request**: `ChangeActiveStatusApiRequest` (@RequestBody)
  - `ids`: List&lt;Long&gt; (필수, 비어있지 않음) - 공통 코드 ID 목록
  - `active`: Boolean (필수) - 활성화 여부 (true: 활성화, false: 비활성화)
- **Response**: `ApiResponse<Void>`
- **UseCase**: `ChangeCommonCodeStatusUseCase`
- **설명**: 공통 코드의 활성화 상태를 일괄 변경합니다.

---

## 아키텍처 패턴

### CQRS 분리
- **Query Controller**: 조회 전용 (GET 메서드만)
- **Command Controller**: 명령 전용 (POST, PUT, PATCH)

### Hexagonal Architecture
- **Adapter-In (REST)**: Controller → Mapper → UseCase
- **Application Layer**: UseCase (Port-In) 인터페이스 정의
- **Adapter-Out**: Persistence 구현 (Port-Out)

### 네이밍 규칙
- Query Request: `Search*ApiRequest` (페이지 조회)
- Command Request: `Register*ApiRequest`, `Update*ApiRequest`, `Change*ApiRequest`
- Response: `*ApiResponse`
- UseCase: `*UseCase` (동사 + 명사)

### Validation 규칙
- API Request DTO에만 Validation 어노테이션 적용
- Record 타입으로 불변성 보장
- OpenAPI @Schema 어노테이션 필수

---

## 사용 예시

### 공통 코드 목록 조회
```http
GET /api/v1/market/admin/common-codes?commonCodeTypeId=1&active=true&page=0&size=20
```

### 공통 코드 등록
```http
POST /api/v1/market/admin/common-codes
Content-Type: application/json

{
  "commonCodeTypeId": 1,
  "code": "CARD",
  "displayName": "신용카드",
  "displayOrder": 1
}
```

### 공통 코드 수정
```http
PUT /api/v1/market/admin/common-codes/1
Content-Type: application/json

{
  "displayName": "신용/체크카드",
  "displayOrder": 1
}
```

### 공통 코드 활성화 상태 변경
```http
PATCH /api/v1/market/admin/common-codes/active-status
Content-Type: application/json

{
  "ids": [1, 2, 3],
  "active": false
}
```

---

## 관련 문서
- CommonCodeType API: `.claude/docs/api-endpoints/commoncodetype.md`
- Domain Layer: `domain/src/main/java/com/ryuqq/marketplace/domain/commoncode/`
- Application Layer: `application/src/main/java/com/ryuqq/marketplace/application/commoncode/`
