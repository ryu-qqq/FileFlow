# RefundPolicy API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 3개 |
| **합계** | **4개** |

## Base Path

```
/api/v1/market/sellers/{sellerId}/refund-policies
```

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/sellers/{sellerId}/refund-policies | RefundPolicyQueryController | search | SearchRefundPolicyUseCase |

### Q1. search (환불정책 목록 조회)

- **Path**: `GET /api/v1/market/sellers/{sellerId}/refund-policies`
- **Controller**: `RefundPolicyQueryController`
- **Method**: `search`
- **Request**: `SearchRefundPoliciesPageApiRequest` (@ModelAttribute - Query String)
  - `sortKey` (String, nullable): 정렬 키 (CREATED_AT, POLICY_NAME)
  - `sortDirection` (String, nullable): 정렬 방향 (ASC, DESC)
  - `page` (Integer, nullable): 페이지 번호 (0부터 시작, @Min(0))
  - `size` (Integer, nullable): 페이지 크기 (1~100, @Min(1), @Max(100))
- **Response**: `ApiResponse<PageApiResponse<RefundPolicyApiResponse>>`
  - `policyId` (Long): 정책 ID
  - `policyName` (String): 정책명
  - `defaultPolicy` (boolean): 기본 정책 여부
  - `active` (boolean): 활성화 상태
  - `returnPeriodDays` (int): 반품 가능 기간 (일)
  - `exchangePeriodDays` (int): 교환 가능 기간 (일)
  - `nonReturnableConditions` (List): 반품 불가 조건 목록
    - `code` (String): 조건 코드
    - `displayName` (String): 조건 표시명
  - `createdAt` (String): 생성일시 (ISO 8601)
- **UseCase**: `SearchRefundPolicyUseCase`
- **설명**: 환불정책 목록을 페이지 기반으로 조회합니다.

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/sellers/{sellerId}/refund-policies | RefundPolicyCommandController | register | RegisterRefundPolicyUseCase |
| 2 | PUT | /api/v1/market/sellers/{sellerId}/refund-policies/{policyId} | RefundPolicyCommandController | update | UpdateRefundPolicyUseCase |
| 3 | PATCH | /api/v1/market/sellers/{sellerId}/refund-policies/status | RefundPolicyCommandController | changeStatus | ChangeRefundPolicyStatusUseCase |

### C1. register (환불정책 등록)

- **Path**: `POST /api/v1/market/sellers/{sellerId}/refund-policies`
- **Controller**: `RefundPolicyCommandController`
- **Method**: `register`
- **Request**: `RegisterRefundPolicyApiRequest` (@RequestBody)
  - `policyName` (String, required): 정책명 (1~100자, @NotBlank, @Size)
  - `defaultPolicy` (Boolean, required): 기본 정책 여부 (@NotNull)
  - `returnPeriodDays` (Integer, required): 반품 가능 기간 (1~90일, @NotNull, @Min(1), @Max(90))
  - `exchangePeriodDays` (Integer, required): 교환 가능 기간 (1~90일, @NotNull, @Min(1), @Max(90))
  - `nonReturnableConditions` (List<String>, optional): 반품 불가 조건 목록
    - 가능한 값: OPENED_PACKAGING, USED_PRODUCT, TIME_EXPIRED, DIGITAL_CONTENT, CUSTOM_MADE, HYGIENE_PRODUCT, PARTIAL_SET, MISSING_TAG, DAMAGED_BY_CUSTOMER
  - `partialRefundEnabled` (Boolean, optional): 부분 환불 허용 여부
  - `inspectionRequired` (Boolean, optional): 검수 필요 여부
  - `inspectionPeriodDays` (Integer, optional): 검수 소요 기간 (일, @Min(0))
  - `additionalInfo` (String, optional): 추가 안내 문구 (최대 1000자, @Size)
- **Response**: `ApiResponse<RegisterRefundPolicyApiResponse>` (201 Created)
  - `policyId` (Long): 생성된 정책 ID
- **UseCase**: `RegisterRefundPolicyUseCase`
- **설명**: 새로운 환불정책을 등록합니다.

### C2. update (환불정책 수정)

- **Path**: `PUT /api/v1/market/sellers/{sellerId}/refund-policies/{policyId}`
- **Controller**: `RefundPolicyCommandController`
- **Method**: `update`
- **Request**: `UpdateRefundPolicyApiRequest` (@RequestBody)
  - `policyName` (String, required): 정책명 (1~100자, @NotBlank, @Size)
  - `defaultPolicy` (Boolean, required): 기본 정책 여부 (@NotNull)
  - `returnPeriodDays` (Integer, required): 반품 가능 기간 (1~90일, @NotNull, @Min(1), @Max(90))
  - `exchangePeriodDays` (Integer, required): 교환 가능 기간 (1~90일, @NotNull, @Min(1), @Max(90))
  - `nonReturnableConditions` (List<String>, optional): 반품 불가 조건 목록
    - 가능한 값: OPENED_PACKAGING, USED_PRODUCT, TIME_EXPIRED, DIGITAL_CONTENT, CUSTOM_MADE, HYGIENE_PRODUCT, PARTIAL_SET, MISSING_TAG, DAMAGED_BY_CUSTOMER
  - `partialRefundEnabled` (Boolean, optional): 부분 환불 허용 여부
  - `inspectionRequired` (Boolean, optional): 검수 필요 여부
  - `inspectionPeriodDays` (Integer, optional): 검수 소요 기간 (일, @Min(0))
  - `additionalInfo` (String, optional): 추가 안내 문구 (최대 1000자, @Size)
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateRefundPolicyUseCase`
- **설명**: 기존 환불정책의 정보를 수정합니다.

### C3. changeStatus (환불정책 다건 상태 변경)

- **Path**: `PATCH /api/v1/market/sellers/{sellerId}/refund-policies/status`
- **Controller**: `RefundPolicyCommandController`
- **Method**: `changeStatus`
- **Request**: `ChangeRefundPolicyStatusApiRequest` (@RequestBody)
  - `policyIds` (List<Long>, required): 상태 변경할 정책 ID 목록 (@NotEmpty)
  - `active` (Boolean, required): 변경할 활성화 상태 (true: 활성화, false: 비활성화, @NotNull)
- **Response**: `Void` (204 No Content)
- **UseCase**: `ChangeRefundPolicyStatusUseCase`
- **설명**: 선택한 환불정책들의 활성화 상태를 변경합니다.

---

## 아키텍처 특징

### CQRS 분리
- **Query Controller**: `RefundPolicyQueryController` (조회 전용)
- **Command Controller**: `RefundPolicyCommandController` (생성/수정/상태 변경)

### Hexagonal Architecture
- **Port-In**: UseCase 인터페이스 (application layer)
- **Adapter-In**: REST Controller (adapter-in/rest-api layer)
- **DTO 분리**: API Request/Response ↔ Application Command/Query/Result

### 규칙 준수
- **API-CTR-001**: @RestController 어노테이션 사용
- **API-CTR-002**: DELETE 메서드 금지 (소프트 삭제는 PATCH로 상태 변경)
- **API-CTR-003**: UseCase(Port-In) 인터페이스 의존
- **API-CTR-004**: ResponseEntity<ApiResponse<T>> 래핑 필수
- **API-CTR-005**: Controller에서 @Transactional 금지
- **API-CTR-007**: Controller에 비즈니스 로직 포함 금지
- **API-CTR-009**: @Valid 어노테이션 필수
- **API-CTR-010**: CQRS Controller 분리
- **API-CTR-011**: List 직접 반환 금지 → PageApiResponse 페이징 필수
- **API-CTR-012**: URL 경로 소문자 + 복수형 (/refund-policies)
- **API-DTO-001**: Request/Response DTO는 Record로 정의
- **API-DTO-003**: Validation 어노테이션은 API Request에만 적용
- **API-DTO-004**: Update Request에 ID 포함 금지 (PathVariable 사용)
- **API-DTO-005**: 날짜 String 변환 필수 (Instant 타입 사용 금지)

---

## 반품 불가 조건 코드

| 코드 | 설명 |
|------|------|
| OPENED_PACKAGING | 포장 개봉 |
| USED_PRODUCT | 사용된 제품 |
| TIME_EXPIRED | 기한 만료 |
| DIGITAL_CONTENT | 디지털 콘텐츠 |
| CUSTOM_MADE | 맞춤 제작 |
| HYGIENE_PRODUCT | 위생 제품 |
| PARTIAL_SET | 부분 세트 |
| MISSING_TAG | 택 분실 |
| DAMAGED_BY_CUSTOMER | 고객 과실 손상 |
