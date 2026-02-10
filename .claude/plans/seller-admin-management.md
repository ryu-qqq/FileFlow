# SellerAdmin 관리 시스템 개발 계획

> **작성일**: 2026-01-30
> **브랜치**: feature/seller-policy-redesign
> **상태**: 진행중

---

## 1. 개요

### 1.1 목적
셀러 관리자(SellerAdmin) 관리 시스템 구현. 가입 신청, 승인/거절, 외부 인증 서버 연동 포함.

### 1.2 아키텍처 결정사항

| 항목 | 결정 | 이유 |
|------|------|------|
| 추가 관리자 가입 | 신청 → 승인 프로세스 (B안) | 관리 체계 필요 |
| 인증 서버 연동 시점 | 승인 시점 | 승인 확정 후 리소스 생성 |
| Tenant/Org 등록 | Outbox 패턴 (비동기) | 외부 서버 장애 격리 |
| SellerAdmin 등록 | 동기 + 롤백 | 즉시 로그인 필요 |

---

## 2. 플로우 설계

### 2.1 첫 번째 관리자 (자동 승인)

```
[SellerApplication 승인]
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  단일 API 호출                                           │
│  1. Seller 생성                                         │
│  2. SellerAdmin 생성 (ACTIVE 상태)                      │
│  3. 인증 서버에 SellerAdmin 등록 (동기)                  │
│     - 실패 시 전체 롤백                                  │
│  4. SellerAuthOutbox 생성 (Tenant/Org 등록용)           │
└─────────────────────────────────────────────────────────┘
    │
    ▼
[비동기 처리 - Event Listener / Scheduler]
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  Outbox 처리                                            │
│  1. 인증 서버에 Tenant + Organization 생성 (단일 호출)   │
│  2. Seller.updateAuthInfo(tenantId, orgId) 업데이트     │
│  3. Outbox 상태 COMPLETED로 변경                        │
│  * 실패 시 재시도 (최대 3회)                             │
└─────────────────────────────────────────────────────────┘
```

### 2.2 추가 관리자 (승인 필요)

```
[가입 신청 API]
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  SellerAdmin 생성                                       │
│  - 상태: PENDING_APPROVAL                               │
│  - authUserId: null (승인 전이므로)                     │
└─────────────────────────────────────────────────────────┘
    │
    ▼
[승인 API - 기존 관리자가 호출]
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  승인 처리                                              │
│  1. 인증 서버에 SellerAdmin 등록 (동기)                 │
│     - 실패 시 롤백                                      │
│  2. SellerAdmin.approve(authUserId, now)               │
│     - 상태: ACTIVE                                      │
│     - authUserId 설정                                   │
└─────────────────────────────────────────────────────────┘

[거절 API - 기존 관리자가 호출]
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  거절 처리                                              │
│  1. SellerAdmin.reject(now)                            │
│     - 상태: REJECTED                                    │
│     - 인증 서버 호출 없음                               │
└─────────────────────────────────────────────────────────┘
```

---

## 3. 완료된 작업

### 3.1 Domain Layer ✅

| 파일 | 상태 | 설명 |
|------|------|------|
| `SellerAuthOutbox.java` | ✅ 완료 | Outbox Aggregate |
| `SellerAuthOutboxId.java` | ✅ 완료 | ID Value Object |
| `SellerAuthOutboxStatus.java` | ✅ 완료 | PENDING, PROCESSING, COMPLETED, FAILED |
| `Seller.java` | ✅ 수정 | authTenantId, authOrganizationId 추가 |
| `SellerAdmin.java` | ✅ 수정 | forApplication(), approve(), reject() 추가 |
| `SellerAdminStatus.java` | ✅ 수정 | PENDING_APPROVAL, REJECTED 추가 |

---

## 4. 남은 작업

### 4.1 Persistence Layer (adapter-out)

#### 4.1.1 SellerAuthOutbox 영속성
```
□ SellerAuthOutboxJpaEntity.java
  - id, seller_id, payload, status, retry_count, max_retry
  - created_at, processed_at, error_message

□ SellerAuthOutboxJpaRepository.java
  - findByStatusIn(List<SellerAuthOutboxStatus> statuses)
  - findBySellerId(Long sellerId)

□ SellerAuthOutboxJpaEntityMapper.java
  - toEntity(SellerAuthOutbox domain)
  - toDomain(SellerAuthOutboxJpaEntity entity)

□ SellerAuthOutboxPersistenceAdapter.java
  - save(SellerAuthOutbox outbox)
  - findPendingOutboxes(): List<SellerAuthOutbox>
  - findBySellerId(SellerId sellerId): Optional<SellerAuthOutbox>
```

#### 4.1.2 SellerAdmin 영속성 수정
```
□ SellerAdminJpaEntity.java
  - status 필드 추가/확인 (PENDING_APPROVAL, REJECTED 지원)

□ SellerAdminJpaRepository.java
  - findBySellerId(Long sellerId): List<SellerAdminJpaEntity>
  - findBySellerIdAndStatus(Long sellerId, String status)
```

### 4.2 Application Layer

#### 4.2.1 Port 정의
```
□ SellerAuthOutboxRepository.java (out port)
  - save(SellerAuthOutbox outbox)
  - findPendingOutboxes(): List<SellerAuthOutbox>
  - findBySellerId(SellerId sellerId): Optional<SellerAuthOutbox>

□ AuthServerClient.java (out port)
  - registerSellerAdmin(RegisterSellerAdminCommand): AuthUserId
  - createTenantAndOrganization(CreateTenantOrgCommand): TenantOrgResult
```

#### 4.2.2 UseCase 정의
```
□ ApplySellerAdminUseCase.java (in port)
  - apply(ApplySellerAdminCommand): SellerAdminId
  - 추가 관리자 가입 신청

□ ApproveSellerAdminUseCase.java (in port)
  - approve(ApproveSellerAdminCommand): void
  - 가입 신청 승인

□ RejectSellerAdminUseCase.java (in port)
  - reject(RejectSellerAdminCommand): void
  - 가입 신청 거절

□ ProcessSellerAuthOutboxUseCase.java (in port)
  - processOutbox(SellerAuthOutboxId): void
  - Outbox 처리 (Tenant/Org 생성)
```

#### 4.2.3 Service 구현
```
□ SellerAdminApplicationService.java
  - 가입 신청, 승인, 거절 로직

□ SellerAuthOutboxProcessingService.java
  - Outbox 처리 로직
  - 외부 인증 서버 호출
  - 재시도 로직
```

#### 4.2.4 Event 처리
```
□ SellerApplicationApprovedEventListener.java
  - SellerApplicationApprovedEvent 수신
  - Seller, SellerAdmin 생성
  - SellerAuthOutbox 생성
  - 인증 서버에 SellerAdmin 등록 (동기)

□ SellerAuthOutboxCreatedEventListener.java (선택)
  - 즉시 처리 시도 (실패 시 스케줄러가 처리)
```

### 4.3 Scheduler

```
□ SellerAuthOutboxScheduler.java
  - @Scheduled 메서드
  - PENDING 상태 Outbox 조회
  - 처리 시도 및 재시도 관리
  - 실패 시 알림 (선택)
```

### 4.4 REST API (adapter-in)

```
□ SellerAdminV2Controller.java
  - POST /api/v2/sellers/{sellerId}/admins/apply
    → 가입 신청
  - POST /api/v2/sellers/{sellerId}/admins/{adminId}/approve
    → 승인
  - POST /api/v2/sellers/{sellerId}/admins/{adminId}/reject
    → 거절
  - GET /api/v2/sellers/{sellerId}/admins/pending
    → 승인 대기 목록 조회
```

### 4.5 Infrastructure (adapter-out)

```
□ AuthServerClientAdapter.java
  - HTTP/gRPC 클라이언트
  - 인증 서버 API 호출 구현
  - 타임아웃, 재시도 설정
```

---

## 5. 구현 순서 (권장)

```
Phase 1: Persistence Layer
├── SellerAuthOutbox JPA Entity
├── SellerAuthOutbox Repository
└── SellerAuthOutbox Mapper & Adapter

Phase 2: Application Layer - Ports
├── SellerAuthOutboxRepository (out port)
├── AuthServerClient (out port)
└── UseCase interfaces (in ports)

Phase 3: Application Layer - Services
├── SellerAdminApplicationService
└── SellerAuthOutboxProcessingService

Phase 4: Event Listeners
├── SellerApplicationApprovedEventListener
└── SellerAuthOutboxCreatedEventListener

Phase 5: Scheduler
└── SellerAuthOutboxScheduler

Phase 6: REST API
├── DTOs (Request/Response)
└── Controller

Phase 7: Infrastructure
└── AuthServerClientAdapter
```

---

## 6. 테스트 계획

### 6.1 Unit Tests
- `SellerAuthOutboxTest` - Outbox 상태 전이 테스트
- `SellerAdminTest` - 가입 신청/승인/거절 테스트

### 6.2 Integration Tests
- Persistence Adapter 테스트
- Event Listener 테스트

### 6.3 E2E Tests
- 전체 플로우 테스트 (Testcontainers)

---

## 7. 주의사항

### 7.1 트랜잭션 경계
- SellerAdmin 등록 시 외부 API 호출은 트랜잭션 **외부**에서
- Outbox 저장은 트랜잭션 **내부**에서

### 7.2 멱등성
- Outbox 처리는 멱등하게 구현
- 동일 Outbox 중복 처리 방지

### 7.3 에러 핸들링
- 인증 서버 장애 시 적절한 에러 메시지
- 재시도 횟수 초과 시 알림

---

## 8. 참고 자료

### 8.1 관련 도메인 객체
- `domain/.../seller/aggregate/Seller.java`
- `domain/.../seller/aggregate/SellerAdmin.java`
- `domain/.../seller/aggregate/SellerAuthOutbox.java`

### 8.2 관련 테스트
- `domain/src/test/.../SellerTest.java`
- `domain/src/testFixtures/.../SellerFixtures.java`
