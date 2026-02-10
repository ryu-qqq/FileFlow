# CommonCodeType API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 3개 |
| **합계** | **4개** |

**Base Path**: `/api/v1/market/admin/common-code-types`

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/admin/common-code-types | CommonCodeTypeQueryController | search | SearchCommonCodeTypeUseCase |

### Q1. search - 공통 코드 타입 목록 조회

공통 코드 타입 목록을 페이지 기반으로 조회합니다. 활성화 여부, 키워드 필터링을 지원합니다.

- **HTTP Method**: `GET`
- **Path**: `/api/v1/market/admin/common-code-types`
- **Controller**: `CommonCodeTypeQueryController`
- **Method**: `search`
- **Request Type**: Query Parameters
- **Request DTO**: `SearchCommonCodeTypesPageApiRequest`
- **Response**: `ApiResponse<PageApiResponse<CommonCodeTypeApiResponse>>`
- **UseCase**: `SearchCommonCodeTypeUseCase`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| active | Boolean | ❌ | 활성화 여부 필터 (null이면 전체 조회) | `true` |
| searchField | String | ❌ | 검색 필드 (null: 전체, code: 코드, name: 이름) | `code` |
| searchWord | String | ❌ | 검색어 | `결제` |
| type | String | ❌ | 공통 코드 값 필터 (해당 값을 가진 공통코드를 갖는 타입만 조회) | `CARD` |
| sortKey | String | ❌ | 정렬 키 (CREATED_AT, DISPLAY_ORDER, CODE) | `CREATED_AT` |
| sortDirection | String | ❌ | 정렬 방향 (ASC, DESC) | `DESC` |
| page | Integer | ❌ | 페이지 번호 (0부터 시작, 최소 0) | `0` |
| size | Integer | ❌ | 페이지 크기 (1~100) | `20` |

#### Response Fields

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "PAYMENT_METHOD",
        "name": "결제수단",
        "description": "결제 시 사용 가능한 결제수단 목록",
        "displayOrder": 1,
        "active": true,
        "createdAt": "2025-01-23T10:30:00+09:00",
        "updatedAt": "2025-01-23T10:30:00+09:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/admin/common-code-types | CommonCodeTypeCommandController | register | RegisterCommonCodeTypeUseCase |
| 2 | PUT | /api/v1/market/admin/common-code-types/{commonCodeTypeId} | CommonCodeTypeCommandController | update | UpdateCommonCodeTypeUseCase |
| 3 | PATCH | /api/v1/market/admin/common-code-types/active-status | CommonCodeTypeCommandController | changeActiveStatus | ChangeCommonCodeTypeStatusUseCase |

### C1. register - 공통 코드 타입 등록

새로운 공통 코드 타입을 등록합니다.

- **HTTP Method**: `POST`
- **Path**: `/api/v1/market/admin/common-code-types`
- **Controller**: `CommonCodeTypeCommandController`
- **Method**: `register`
- **Request Type**: `@RequestBody`
- **Request DTO**: `RegisterCommonCodeTypeApiRequest`
- **Response**: `ApiResponse<Long>`
- **UseCase**: `RegisterCommonCodeTypeUseCase`

#### Request Body

```json
{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "description": "결제 시 사용 가능한 결제수단 목록",
  "displayOrder": 1
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|----------|------|
| code | String | ✅ | @NotBlank, @Size(max=50) | 코드 (영문 대문자, 언더스코어) |
| name | String | ✅ | @NotBlank, @Size(max=100) | 이름 |
| description | String | ❌ | @Size(max=500) | 설명 |
| displayOrder | int | ✅ | @Min(0) | 표시 순서 |

#### Response

```json
{
  "success": true,
  "data": 1
}
```

- **Status Code**: `201 Created`

#### Error Responses

| Status | 설명 |
|--------|------|
| 400 | 잘못된 요청 (검증 실패) |
| 409 | 중복된 코드 |

---

### C2. update - 공통 코드 타입 수정

기존 공통 코드 타입의 정보를 수정합니다.

- **HTTP Method**: `PUT`
- **Path**: `/api/v1/market/admin/common-code-types/{commonCodeTypeId}`
- **Controller**: `CommonCodeTypeCommandController`
- **Method**: `update`
- **Request Type**: `@PathVariable` + `@RequestBody`
- **Request DTO**: `UpdateCommonCodeTypeApiRequest`
- **Response**: `ApiResponse<Void>`
- **UseCase**: `UpdateCommonCodeTypeUseCase`

#### Path Variables

| 변수 | 타입 | 필수 | 설명 |
|------|------|------|------|
| commonCodeTypeId | Long | ✅ | 공통 코드 타입 ID |

#### Request Body

```json
{
  "name": "결제수단",
  "description": "결제 시 사용 가능한 결제수단 목록",
  "displayOrder": 1
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|----------|------|
| name | String | ✅ | @NotBlank, @Size(max=100) | 이름 |
| description | String | ❌ | @Size(max=500) | 설명 |
| displayOrder | int | ✅ | @Min(0) | 표시 순서 |

#### Response

```json
{
  "success": true
}
```

- **Status Code**: `200 OK`

#### Error Responses

| Status | 설명 |
|--------|------|
| 400 | 잘못된 요청 (검증 실패) |
| 404 | 공통 코드 타입을 찾을 수 없음 |

---

### C3. changeActiveStatus - 공통 코드 타입 활성화/비활성화

선택한 공통 코드 타입들의 활성화 상태를 일괄 변경합니다.

- **HTTP Method**: `PATCH`
- **Path**: `/api/v1/market/admin/common-code-types/active-status`
- **Controller**: `CommonCodeTypeCommandController`
- **Method**: `changeActiveStatus`
- **Request Type**: `@RequestBody`
- **Request DTO**: `ChangeActiveStatusApiRequest`
- **Response**: `ApiResponse<Void>`
- **UseCase**: `ChangeCommonCodeTypeStatusUseCase`

#### Request Body

```json
{
  "ids": [1, 2, 3],
  "active": true
}
```

| 필드 | 타입 | 필수 | 검증 규칙 | 설명 |
|------|------|------|----------|------|
| ids | List\<Long\> | ✅ | @NotEmpty | 대상 공통 코드 타입 ID 목록 |
| active | Boolean | ✅ | @NotNull | 활성화 여부 (true: 활성화, false: 비활성화) |

#### Response

```json
{
  "success": true
}
```

- **Status Code**: `200 OK`

#### Error Responses

| Status | 설명 |
|--------|------|
| 400 | 잘못된 요청 (검증 실패) |

---

## 아키텍처 연결

### Layer Flow

```
[REST API Layer]
CommonCodeTypeQueryController → SearchCommonCodeTypeUseCase
CommonCodeTypeCommandController → RegisterCommonCodeTypeUseCase
                                → UpdateCommonCodeTypeUseCase
                                → ChangeCommonCodeTypeStatusUseCase

[Application Layer]
SearchCommonCodeTypeUseCase
RegisterCommonCodeTypeUseCase
UpdateCommonCodeTypeUseCase
ChangeCommonCodeTypeStatusUseCase
```

### Mapper Chain

```
[Request → Command/Query]
CommonCodeTypeCommandApiMapper
  - RegisterCommonCodeTypeApiRequest → RegisterCommonCodeTypeCommand
  - UpdateCommonCodeTypeApiRequest → UpdateCommonCodeTypeCommand
  - ChangeActiveStatusApiRequest → ChangeActiveStatusCommand

CommonCodeTypeQueryApiMapper
  - SearchCommonCodeTypesPageApiRequest → CommonCodeTypeSearchParams
  - CommonCodeTypePageResult → PageApiResponse<CommonCodeTypeApiResponse>
```

---

## 설계 원칙 준수

### CQRS 분리

✅ **Query Controller**: `CommonCodeTypeQueryController` (조회 전용)
✅ **Command Controller**: `CommonCodeTypeCommandController` (생성/수정 전용)

### API 규칙 준수

| 규칙 | 준수 내역 |
|------|----------|
| API-CTR-001 | ✅ @RestController 어노테이션 사용 |
| API-CTR-003 | ✅ UseCase(Port-In) 인터페이스 의존 |
| API-CTR-004 | ✅ ResponseEntity<ApiResponse<T>> 래핑 |
| API-CTR-002 | ✅ DELETE 메서드 미사용 (PATCH로 상태 변경) |
| API-CTR-005 | ✅ Controller에서 @Transactional 미사용 |
| API-CTR-007 | ✅ Controller에 비즈니스 로직 미포함 |
| API-CTR-009 | ✅ @Valid 어노테이션 필수 적용 |
| API-CTR-010 | ✅ CQRS Controller 분리 |
| API-CTR-011 | ✅ List 직접 반환 금지, PageApiResponse 사용 |
| API-CTR-012 | ✅ URL 경로 소문자 + 복수형 (/common-code-types) |

### DTO 규칙 준수

| 규칙 | 준수 내역 |
|------|----------|
| API-DTO-001 | ✅ Record 타입 사용 |
| API-DTO-002 | ✅ DTO 불변성 보장 |
| API-DTO-003 | ✅ Validation 어노테이션은 API Request에만 적용 |
| API-DTO-004 | ✅ createdAt/updatedAt 필수 포함 |
| API-DTO-005 | ✅ 날짜 String 변환 (Instant 타입 미사용) |
| API-DTO-010 | ✅ Request DTO 네이밍 규칙 (Search*ApiRequest) |

### Endpoints 규칙 준수

| 규칙 | 준수 내역 |
|------|----------|
| API-END-001 | ✅ Endpoints final class |
| API-END-002 | ✅ static final 상수 |
| API-END-003 | ✅ Path Variable 상수 정의 |

---

## 특징

### 1. 일괄 상태 변경 지원

- **PATCH** `/active-status` 엔드포인트를 통해 여러 공통 코드 타입의 활성화 상태를 한 번에 변경 가능
- 소프트 삭제 개념으로 DELETE 메서드 미사용

### 2. 유연한 검색 기능

- 활성화 상태 필터링
- 코드/이름 필드별 검색
- 공통 코드 값(CommonCodeValue) 기반 필터링
- 다양한 정렬 옵션 (생성일, 표시순서, 코드)

### 3. 페이지네이션

- Offset 기반 페이지네이션
- 페이지 크기 제한 (최대 100)
- 페이지 메타데이터 제공 (전체 개수, 페이지 수, 이전/다음 여부)

### 4. 검증 규칙

- 코드: 최대 50자, 필수 입력
- 이름: 최대 100자, 필수 입력
- 설명: 최대 500자, 선택 입력
- 표시 순서: 0 이상, 필수 입력

---

## 관련 도메인

- **CommonCode**: 공통 코드 타입에 속하는 실제 코드 값들
- 공통 코드 타입은 코드 값들의 그룹 역할 수행

---

생성일: 2026-02-06
분석 대상: adapter-in/rest-api/commoncodetype
