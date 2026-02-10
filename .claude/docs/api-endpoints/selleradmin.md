# SellerAdmin API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 7개 |
| **합계** | **9개** |

**Base Path**: `/api/v1/market/admin/seller-admin-applications`

**도메인**: 셀러 관리자 가입 신청 관리

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | `/api/v1/market/admin/seller-admin-applications` | SellerAdminApplicationQueryController | search | SearchSellerAdminApplicationsUseCase |
| 2 | GET | `/api/v1/market/admin/seller-admin-applications/{sellerAdminId}` | SellerAdminApplicationQueryController | get | GetSellerAdminApplicationUseCase |

### Q1. search - 셀러 관리자 가입 신청 목록 조회
- **Path**: `GET /api/v1/market/admin/seller-admin-applications`
- **Controller**: `SellerAdminApplicationQueryController`
- **Method**: `search`
- **Request**: `SearchSellerAdminApplicationsApiRequest` (@ModelAttribute)
- **Response**: `ApiResponse<SellerAdminApplicationPageApiResponse>`
- **UseCase**: `SearchSellerAdminApplicationsUseCase`
- **설명**:
  - 셀러 관리자 가입 신청 목록을 페이징 조회합니다.
  - `sellerIds`를 지정하면 해당 셀러들의 신청만 조회 (일반 관리자).
  - `sellerIds` 생략 시 전체 조회 (슈퍼 관리자).

### Q2. get - 셀러 관리자 가입 신청 상세 조회
- **Path**: `GET /api/v1/market/admin/seller-admin-applications/{sellerAdminId}`
- **Controller**: `SellerAdminApplicationQueryController`
- **Method**: `get`
- **Request**: `sellerAdminId` (@PathVariable)
- **Response**: `ApiResponse<SellerAdminApplicationApiResponse>`
- **UseCase**: `GetSellerAdminApplicationUseCase`
- **설명**: 특정 가입 신청의 상세 정보를 조회합니다.

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | `/api/v1/market/admin/seller-admin-applications` | SellerAdminApplicationCommandController | apply | ApplySellerAdminUseCase |
| 2 | POST | `/api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve` | SellerAdminApplicationCommandController | approve | ApproveSellerAdminUseCase |
| 3 | POST | `/api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject` | SellerAdminApplicationCommandController | reject | RejectSellerAdminUseCase |
| 4 | POST | `/api/v1/market/admin/seller-admin-applications/bulk-approve` | SellerAdminApplicationCommandController | bulkApprove | BulkApproveSellerAdminUseCase |
| 5 | POST | `/api/v1/market/admin/seller-admin-applications/bulk-reject` | SellerAdminApplicationCommandController | bulkReject | BulkRejectSellerAdminUseCase |
| 6 | POST | `/api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password` | SellerAdminApplicationCommandController | resetPassword | ResetSellerAdminPasswordUseCase |
| 7 | PATCH | `/api/v1/market/admin/seller-admin-applications/{sellerAdminId}/change-password` | SellerAdminApplicationCommandController | changePassword | ChangeSellerAdminPasswordUseCase |

### C1. apply - 셀러 관리자 가입 신청
- **Path**: `POST /api/v1/market/admin/seller-admin-applications`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `apply`
- **Request**: `ApplySellerAdminApiRequest` (@RequestBody)
- **Response**: `ApiResponse<ApplySellerAdminApiResponse>` (201 Created)
- **UseCase**: `ApplySellerAdminUseCase`
- **설명**:
  - 셀러 하위에 새로운 관리자 가입을 신청합니다.
  - `sellerId` 포함 필수.
  - 성공 시 생성된 `sellerAdminId` 반환.

### C2. approve - 셀러 관리자 가입 신청 승인
- **Path**: `POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `approve`
- **Request**: `sellerAdminId` (@PathVariable)
- **Response**: `ApiResponse<ApproveSellerAdminApiResponse>`
- **UseCase**: `ApproveSellerAdminUseCase`
- **설명**:
  - 대기 중인 가입 신청을 승인합니다.
  - 인증 서버에 사용자를 생성하고 `authUserId`를 반환합니다.
  - 이미 처리된 신청은 409 Conflict 반환.

### C3. reject - 셀러 관리자 가입 신청 거절
- **Path**: `POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `reject`
- **Request**: `sellerAdminId` (@PathVariable)
- **Response**: `Void` (204 No Content)
- **UseCase**: `RejectSellerAdminUseCase`
- **설명**:
  - 대기 중인 가입 신청을 거절합니다.
  - 이미 처리된 신청은 409 Conflict 반환.

### C4. bulkApprove - 셀러 관리자 가입 신청 일괄 승인
- **Path**: `POST /api/v1/market/admin/seller-admin-applications/bulk-approve`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `bulkApprove`
- **Request**: `BulkApproveSellerAdminApiRequest` (@RequestBody)
- **Response**: `ApiResponse<BulkApproveSellerAdminApiResponse>`
- **UseCase**: `BulkApproveSellerAdminUseCase`
- **설명**:
  - 여러 건의 가입 신청을 한 번에 승인합니다.
  - `BatchProcessingResult` 타입으로 성공/실패 결과 반환.

### C5. bulkReject - 셀러 관리자 가입 신청 일괄 거절
- **Path**: `POST /api/v1/market/admin/seller-admin-applications/bulk-reject`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `bulkReject`
- **Request**: `BulkRejectSellerAdminApiRequest` (@RequestBody)
- **Response**: `Void` (204 No Content)
- **UseCase**: `BulkRejectSellerAdminUseCase`
- **설명**: 여러 건의 가입 신청을 한 번에 거절합니다.

### C6. resetPassword - 셀러 관리자 비밀번호 초기화
- **Path**: `POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `resetPassword`
- **Request**: `sellerAdminId` (@PathVariable)
- **Response**: `Void` (204 No Content)
- **UseCase**: `ResetSellerAdminPasswordUseCase`
- **설명**:
  - **ACTIVE 상태**의 셀러 관리자 비밀번호를 초기화합니다.
  - 인증 서버에서 임시 비밀번호를 생성하고 사용자에게 전달합니다.
  - 비활성 상태는 400 Bad Request 반환.

### C7. changePassword - 셀러 관리자 비밀번호 변경
- **Path**: `PATCH /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/change-password`
- **Controller**: `SellerAdminApplicationCommandController`
- **Method**: `changePassword`
- **Request**:
  - `sellerAdminId` (@PathVariable)
  - `ChangeSellerAdminPasswordApiRequest` (@RequestBody)
- **Response**: `Void` (204 No Content)
- **UseCase**: `ChangeSellerAdminPasswordUseCase`
- **설명**:
  - **외부 본인인증(이메일/문자 등) 완료 후** 호출합니다.
  - **ACTIVE 상태**의 셀러 관리자 비밀번호를 새 비밀번호로 변경합니다.
  - 현재 비밀번호는 검증하지 않습니다 (본인인증으로 대체).

---

## 아키텍처 특징

### CQRS 패턴 적용
- **Query Controller**: 조회 전용 (2개 엔드포인트)
- **Command Controller**: 명령 전용 (7개 엔드포인트)

### Hexagonal Architecture
- **Adapter-In (REST API)**: Controller → Mapper → UseCase
- **Application Layer**: UseCase 인터페이스 정의
- **Port-In**: Command/Query UseCase 분리

### 비즈니스 플로우
1. **가입 신청** (`apply`) → **승인**(`approve`) 또는 **거절**(`reject`)
2. **승인 완료** → 인증 서버 사용자 생성 → `authUserId` 발급
3. **비밀번호 관리**: 초기화(`resetPassword`) 또는 변경(`changePassword`)

### 일괄 처리 지원
- `bulkApprove`: 여러 신청 동시 승인 + 결과 통계 반환
- `bulkReject`: 여러 신청 동시 거절

---

## UseCase 연결

### Query UseCases
| UseCase | 설명 |
|---------|------|
| `SearchSellerAdminApplicationsUseCase` | 가입 신청 목록 조회 (페이징) |
| `GetSellerAdminApplicationUseCase` | 가입 신청 상세 조회 |

### Command UseCases
| UseCase | 설명 |
|---------|------|
| `ApplySellerAdminUseCase` | 가입 신청 생성 |
| `ApproveSellerAdminUseCase` | 개별 승인 |
| `RejectSellerAdminUseCase` | 개별 거절 |
| `BulkApproveSellerAdminUseCase` | 일괄 승인 |
| `BulkRejectSellerAdminUseCase` | 일괄 거절 |
| `ResetSellerAdminPasswordUseCase` | 비밀번호 초기화 |
| `ChangeSellerAdminPasswordUseCase` | 비밀번호 변경 |

---

## DTO 구조

### Request DTOs
- **Query**: `SearchSellerAdminApplicationsApiRequest`
- **Command**:
  - `ApplySellerAdminApiRequest`
  - `BulkApproveSellerAdminApiRequest`
  - `BulkRejectSellerAdminApiRequest`
  - `ChangeSellerAdminPasswordApiRequest`

### Response DTOs
- `SellerAdminApplicationPageApiResponse` (페이징)
- `SellerAdminApplicationApiResponse` (상세)
- `ApplySellerAdminApiResponse` (신청 결과)
- `ApproveSellerAdminApiResponse` (승인 결과)
- `BulkApproveSellerAdminApiResponse` (일괄 승인 결과)

---

## 참고사항

### 권한 구분
- **슈퍼 관리자**: 전체 신청 조회 가능
- **일반 관리자**: 자신이 속한 셀러의 신청만 조회 (`sellerIds` 필터)

### 상태 전이
```
[신청] → [대기(PENDING)] → [승인(APPROVED) 또는 거절(REJECTED)]
                          ↓
                    [활성화(ACTIVE)]
```

### 비밀번호 관리 정책
- **초기화**: ACTIVE 상태만 가능, 인증 서버가 임시 비밀번호 생성
- **변경**: 본인인증 완료 후, 현재 비밀번호 불필요

### 에러 응답
- `400`: 잘못된 요청, 상태 불일치
- `404`: 신청/관리자를 찾을 수 없음
- `409`: 이미 처리된 신청 (중복 승인/거절)

---

**분석일시**: 2026-02-06
**분석자**: Claude Code
**문서 버전**: 1.0
