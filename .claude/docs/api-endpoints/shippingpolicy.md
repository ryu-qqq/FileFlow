# ShippingPolicy API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 3개 |
| **합계** | **4개** |

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/sellers/{sellerId}/shipping-policies | ShippingPolicyQueryController | search | SearchShippingPolicyUseCase |

### Q1. search
- **Path**: `GET /api/v1/market/sellers/{sellerId}/shipping-policies`
- **Controller**: `ShippingPolicyQueryController`
- **설명**: 배송정책 목록을 페이지 기반으로 조회합니다.
- **Request**:
  - Path Variable: `sellerId` (Long) - 셀러 ID
  - Query Parameters: `SearchShippingPoliciesPageApiRequest` (@ModelAttribute)
- **Response**: `ApiResponse<PageApiResponse<ShippingPolicyApiResponse>>`
- **UseCase**: `SearchShippingPolicyUseCase`
- **HTTP Status**: `200 OK`

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/sellers/{sellerId}/shipping-policies | ShippingPolicyCommandController | register | RegisterShippingPolicyUseCase |
| 2 | PUT | /api/v1/market/sellers/{sellerId}/shipping-policies/{policyId} | ShippingPolicyCommandController | update | UpdateShippingPolicyUseCase |
| 3 | PATCH | /api/v1/market/sellers/{sellerId}/shipping-policies/status | ShippingPolicyCommandController | changeStatus | ChangeShippingPolicyStatusUseCase |

### C1. register
- **Path**: `POST /api/v1/market/sellers/{sellerId}/shipping-policies`
- **Controller**: `ShippingPolicyCommandController`
- **설명**: 새로운 배송정책을 등록합니다.
- **Request**:
  - Path Variable: `sellerId` (Long) - 셀러 ID
  - Body: `RegisterShippingPolicyApiRequest` (@RequestBody, @Valid)
- **Response**: `ApiResponse<RegisterShippingPolicyApiResponse>` (생성된 배송정책 ID)
- **UseCase**: `RegisterShippingPolicyUseCase`
- **HTTP Status**: `201 CREATED`

### C2. update
- **Path**: `PUT /api/v1/market/sellers/{sellerId}/shipping-policies/{policyId}`
- **Controller**: `ShippingPolicyCommandController`
- **설명**: 기존 배송정책의 정보를 수정합니다.
- **Request**:
  - Path Variables:
    - `sellerId` (Long) - 셀러 ID
    - `policyId` (Long) - 배송정책 ID
  - Body: `UpdateShippingPolicyApiRequest` (@RequestBody, @Valid)
- **Response**: `Void`
- **UseCase**: `UpdateShippingPolicyUseCase`
- **HTTP Status**: `204 NO_CONTENT`

### C3. changeStatus
- **Path**: `PATCH /api/v1/market/sellers/{sellerId}/shipping-policies/status`
- **Controller**: `ShippingPolicyCommandController`
- **설명**: 선택한 배송정책들의 활성화 상태를 변경합니다 (다건 처리).
- **Request**:
  - Path Variable: `sellerId` (Long) - 셀러 ID
  - Body: `ChangeShippingPolicyStatusApiRequest` (@RequestBody, @Valid)
- **Response**: `Void`
- **UseCase**: `ChangeShippingPolicyStatusUseCase`
- **HTTP Status**: `204 NO_CONTENT`

---

## 엔드포인트 상수

**파일**: `ShippingPolicyAdminEndpoints.java`

```java
// Base Path
SHIPPING_POLICIES = "/api/v1/market/sellers/{sellerId}/shipping-policies"

// Path Variables
PATH_SELLER_ID = "sellerId"
PATH_POLICY_ID = "policyId"

// Segments
ID = "/{policyId}"
STATUS = "/status"
```

---

## 아키텍처 특징

### CQRS 패턴
- **Query Controller**: 조회 전용 (`ShippingPolicyQueryController`)
- **Command Controller**: 생성/수정 전용 (`ShippingPolicyCommandController`)
- 각 Controller는 독립적인 UseCase 의존

### Hexagonal Architecture
- **Adapter-In (REST)**: Controller → Mapper → UseCase
- **Port-In**: UseCase 인터페이스 (`SearchShippingPolicyUseCase`, `RegisterShippingPolicyUseCase`, etc.)
- **Service**: UseCase 구현체 (`SearchShippingPolicyService`, `RegisterShippingPolicyService`, etc.)

### 응답 래핑
- 모든 응답은 `ApiResponse<T>` 또는 `ApiResponse<PageApiResponse<T>>`로 래핑
- 일관된 응답 구조 제공

### Path 설계
- RESTful 명명 규칙: 소문자 + 하이픈 (`shipping-policies`)
- 복수형 사용 (`shipping-policies`)
- 계층 구조: `/api/v1/market/sellers/{sellerId}/shipping-policies`

### Validation
- `@Valid` 어노테이션을 통한 요청 검증
- DTO 레벨에서 제약 조건 정의

---

## 관련 파일 위치

### Controller
- `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/shippingpolicy/controller/`
  - `ShippingPolicyQueryController.java`
  - `ShippingPolicyCommandController.java`

### DTO
- `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/shippingpolicy/dto/`
  - `query/SearchShippingPoliciesPageApiRequest.java`
  - `command/RegisterShippingPolicyApiRequest.java`
  - `command/UpdateShippingPolicyApiRequest.java`
  - `command/ChangeShippingPolicyStatusApiRequest.java`
  - `response/ShippingPolicyApiResponse.java`
  - `response/RegisterShippingPolicyApiResponse.java`

### UseCase (Port-In)
- `application/src/main/java/com/ryuqq/marketplace/application/shippingpolicy/port/in/`
  - `query/SearchShippingPolicyUseCase.java`
  - `command/RegisterShippingPolicyUseCase.java`
  - `command/UpdateShippingPolicyUseCase.java`
  - `command/ChangeShippingPolicyStatusUseCase.java`

### Service (UseCase 구현)
- `application/src/main/java/com/ryuqq/marketplace/application/shippingpolicy/service/`
  - `query/SearchShippingPolicyService.java`
  - `command/RegisterShippingPolicyService.java`
  - `command/UpdateShippingPolicyService.java`
  - `command/ChangeShippingPolicyStatusService.java`

---

## 규칙 준수 사항

### API Controller 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-002**: DELETE 메서드 금지 (상태 변경은 PATCH 사용)
- ✅ **API-CTR-003**: UseCase(Port-In) 인터페이스 의존
- ✅ **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑
- ✅ **API-CTR-005**: `@Transactional` 금지
- ✅ **API-CTR-007**: 비즈니스 로직 금지 (Mapper에서 변환)
- ✅ **API-CTR-009**: `@Valid` 어노테이션 필수
- ✅ **API-CTR-010**: CQRS Controller 분리
- ✅ **API-CTR-011**: List 직접 반환 금지 (PageApiResponse 사용)
- ✅ **API-CTR-012**: URL 경로 소문자 + 복수형

### Endpoints 클래스 규칙
- ✅ **API-END-001**: `final class`로 정의
- ✅ **API-END-002**: `static final` 상수 사용
- ✅ **API-END-003**: Path Variable 상수 정의

---

## 분석 일시
- **생성일**: 2026-02-06
- **분석 대상**: ShippingPolicy 도메인 REST API
- **모듈**: `adapter-in/rest-api`
