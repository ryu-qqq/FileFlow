# SellerAdmin 가입 신청 및 승인/거절 기능 구현 계획

> 작성일: 2026-02-04
> 상태: Phase 1-2 완료 (REST API + UseCase 인터페이스)

---

## 1. 비즈니스 요구사항

### 1.1 개요
셀러 하위에 존재하는 셀러 어드민(관리자) 가입 신청 및 승인/거절 기능

### 1.2 흐름
```
1. 가입 신청: 프론트에서 셀러 선택 → 해당 셀러 하위로 가입 신청
2. 신청 리스트 조회: 운영자/해당 셀러 관리자가 신청 목록 확인
3. 승인: 인증 서버에 요청 → authUserId 할당 → ACTIVE 상태
4. 거절: 상태만 REJECTED로 업데이트
```

### 1.3 핵심 도메인
- `SellerAdmin.java` - 가입 신청/승인/거절 로직 완비
- `SellerAdminStatus` - PENDING_APPROVAL, ACTIVE, INACTIVE, SUSPENDED, REJECTED

---

## 2. API 스펙

### 2.1 가입 신청
```
POST /api/admin/v1/sellers/{sellerId}/admins/applications

Request:
{
  "loginId": "admin@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "password": "..." // 인증 서버 전달용
}

Response:
{
  "sellerAdminId": 1,
  "sellerId": 100,
  "loginId": "admin@example.com",
  "name": "홍길동",
  "status": "PENDING_APPROVAL",
  "createdAt": "2026-02-04T10:00:00Z"
}
```

### 2.2 신청 리스트 조회
```
GET /api/admin/v1/sellers/{sellerId}/admins/applications?status=PENDING_APPROVAL&page=0&size=20

Response:
{
  "content": [
    {
      "sellerAdminId": 1,
      "sellerId": 100,
      "loginId": "admin@example.com",
      "name": "홍길동",
      "phoneNumber": "010-1234-5678",
      "status": "PENDING_APPROVAL",
      "createdAt": "2026-02-04T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 2.3 신청 상세 조회
```
GET /api/admin/v1/sellers/{sellerId}/admins/applications/{sellerAdminId}

Response:
{
  "sellerAdminId": 1,
  "sellerId": 100,
  "loginId": "admin@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "status": "PENDING_APPROVAL",
  "createdAt": "2026-02-04T10:00:00Z",
  "updatedAt": "2026-02-04T10:00:00Z"
}
```

### 2.4 승인
```
POST /api/admin/v1/sellers/{sellerId}/admins/applications/{sellerAdminId}/approve

Request: (없음 또는 빈 객체)

Response:
{
  "sellerAdminId": 1,
  "status": "ACTIVE",
  "authUserId": "auth-user-uuid-123",
  "approvedAt": "2026-02-04T11:00:00Z"
}
```

### 2.5 거절
```
POST /api/admin/v1/sellers/{sellerId}/admins/applications/{sellerAdminId}/reject

Request:
{
  "reason": "서류 미비" // optional
}

Response:
{
  "sellerAdminId": 1,
  "status": "REJECTED",
  "rejectedAt": "2026-02-04T11:00:00Z",
  "reason": "서류 미비"
}
```

---

## 3. 구현 컴포넌트

### Phase 1: REST API 스켈레톤 ✅ 완료 (2026-02-04)
| 구분 | 클래스 | 상태 |
|------|--------|------|
| Endpoints | `SellerAdminApplicationV1Endpoints` | ✅ |
| Request DTO | `ApplySellerAdminV1ApiRequest` | ✅ |
| Request DTO | `SearchSellerAdminApplicationsV1ApiRequest` | ✅ |
| Request DTO | `RejectSellerAdminV1ApiRequest` | ✅ |
| Response DTO | `SellerAdminApplicationV1ApiResponse` | ✅ |
| Response DTO | `ApplySellerAdminV1ApiResponse` | ✅ |
| Response DTO | `ApproveSellerAdminV1ApiResponse` | ✅ |
| Response DTO | `SellerAdminApplicationPageV1ApiResponse` | ✅ |
| Controller | `SellerAdminApplicationCommandV1Controller` | ✅ |
| Controller | `SellerAdminApplicationQueryV1Controller` | ✅ |
| Mapper | `SellerAdminApplicationCommandApiMapper` | ✅ |
| Mapper | `SellerAdminApplicationQueryApiMapper` | ✅ |

### Phase 2: Application Layer UseCase ✅ 완료 (2026-02-04)
| 구분 | 클래스 | 상태 |
|------|--------|------|
| UseCase | `ApplySellerAdminUseCase` | ✅ 인터페이스만 |
| UseCase | `ApproveSellerAdminUseCase` | ✅ 인터페이스만 |
| UseCase | `RejectSellerAdminUseCase` | ✅ 인터페이스만 |
| UseCase | `SearchSellerAdminApplicationsUseCase` | ✅ 인터페이스만 |
| UseCase | `GetSellerAdminApplicationUseCase` | ✅ 인터페이스만 |
| Command DTO | `ApplySellerAdminCommand` | ✅ |
| Command DTO | `ApproveSellerAdminCommand` | ✅ |
| Command DTO | `RejectSellerAdminCommand` | ✅ |
| Query DTO | `SellerAdminApplicationSearchParams` | ✅ |
| Result DTO | `SellerAdminApplicationResult` | ✅ |
| Result DTO | `SellerAdminApplicationPageResult` | ✅ |
| Result DTO | `SellerAdminApprovalResult` | ✅ |

### Phase 3: Application Layer Service (나중에)
- ApplySellerAdminService
- ApproveSellerAdminService
- RejectSellerAdminService
- SearchSellerAdminApplicationsService
- GetSellerAdminApplicationService

### Phase 4: Application Layer Manager/Factory/Assembler (나중에)
- SellerAdminCommandManager
- SellerAdminReadManager
- SellerAdminCommandFactory
- SellerAdminAssembler

### Phase 5: Application Layer Port-Out (나중에)
- SellerAdminCommandPort
- SellerAdminQueryPort
- SellerAdminIdentityClient

### Phase 6: Persistence Layer (나중에)
- SellerAdminJpaEntity
- SellerAdminJpaRepository
- SellerAdminQueryDslRepository
- SellerAdminCommandAdapter
- SellerAdminQueryAdapter
- SellerAdminJpaEntityMapper

### Phase 7: Client Adapter (나중에)
- AuthHubSellerAdminClientAdapter

---

## 4. 미결정 사항

### 4.1 비밀번호 처리
- [ ] A) 가입 신청 시 비밀번호 입력
- [ ] B) 승인 후 임시 비밀번호 발급
- [ ] C) 이메일/SMS로 비밀번호 설정 링크 발송

### 4.2 인증 서버 호출 방식
- [ ] 동기 호출 (단순, 즉시 결과)
- [ ] Outbox 패턴 (장애 복원력)

### 4.3 추가 기능
- [ ] 신청 취소 기능
- [ ] 거절 사유 필수 여부
- [ ] 재신청 허용 여부

---

## 5. 참고 파일

### Domain Layer (완료)
- `/domain/src/main/java/com/ryuqq/setof/domain/seller/aggregate/SellerAdmin.java`
- `/domain/src/main/java/com/ryuqq/setof/domain/seller/vo/SellerAdminStatus.java`
- `/domain/src/main/java/com/ryuqq/setof/domain/seller/id/SellerAdminId.java`

### 기존 패턴 참고
- Seller UseCase: `/application/src/main/java/com/ryuqq/setof/application/seller/port/in/`
- Seller Controller: `/adapter-in/rest-api-admin/src/main/java/com/ryuqq/setof/adapter/in/rest/admin/v2/seller/`
