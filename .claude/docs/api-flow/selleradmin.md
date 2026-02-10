# Seller Admin API Flow Analysis

## 개요

셀러 관리자(SellerAdmin) 도메인의 API 호출 흐름을 Hexagonal Architecture 레이어별로 분석한 문서입니다.

**엔드포인트 위치**: `/v2/admin/seller-admins`

**기능**: 셀러 관리자 가입 신청, 승인, 거절, 비밀번호 관리

---

## 목차

1. [Query 엔드포인트](#query-엔드포인트)
   - [목록 조회](#1-get-seller-admins---목록-조회)
   - [상세 조회](#2-get-seller-adminsid---상세-조회)

2. [Command 엔드포인트](#command-엔드포인트)
   - [가입 신청](#3-post-seller-admins---가입-신청)
   - [승인](#4-post-seller-adminsidapprove---승인)
   - [거절](#5-post-seller-adminsidreject---거절)
   - [일괄 승인](#6-post-seller-adminsbatch-approve---일괄-승인)
   - [일괄 거절](#7-post-seller-adminsbatch-reject---일괄-거절)
   - [비밀번호 초기화](#8-post-seller-adminsidresetpassword---비밀번호-초기화)
   - [비밀번호 변경](#9-patch-seller-adminsidpassword---비밀번호-변경)

---

## Query 엔드포인트

### 1. GET /seller-admins - 목록 조회

셀러 관리자 가입 신청 목록을 조회합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationQueryController.search(SearchSellerAdminApplicationsApiRequest)
  ├─ SellerAdminApplicationQueryApiMapper.toSearchParams(request)
  │   └─ → SellerAdminApplicationSearchParams
  │
  ├─ SearchSellerAdminApplicationsUseCase.execute(params)              [Port]
  │   └─ SearchSellerAdminApplicationsService.execute(params)         [Impl]
  │       ├─ SellerAdminQueryFactory.createCriteria(params)
  │       │   └─ → SellerAdminSearchCriteria
  │       │
  │       ├─ SellerAdminReadManager.findByCriteria(criteria)
  │       │   └─ SellerAdminQueryPort.findByCriteria(criteria)        [Port]
  │       │       └─ SellerAdminQueryAdapter.findByCriteria(criteria) [Impl]
  │       │           └─ SellerAdminQueryDslRepository.findByCriteria()
  │       │               └─ QueryDSL: WHERE, JOIN, ORDER BY, LIMIT
  │       │
  │       ├─ SellerAdminReadManager.countByCriteria(criteria)
  │       │   └─ SellerAdminQueryPort.countByCriteria(criteria)
  │       │       └─ SellerAdminQueryAdapter.countByCriteria(criteria)
  │       │           └─ SellerAdminQueryDslRepository.countByCriteria()
  │       │
  │       └─ SellerAdminAssembler.toPageResult(sellerAdmins, page, size, totalCount)
  │           └─ → SellerAdminApplicationPageResult
  │
  └─ SellerAdminApplicationPageApiResponse.from(result)
      └─ → ApiResponse<SellerAdminApplicationPageApiResponse>
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationQueryController`
- **Request DTO**: `SearchSellerAdminApplicationsApiRequest`
  - sellerIds: List<Long> (선택, 특정 셀러들의 신청만 조회)
  - status: List<String> (PENDING_APPROVAL, REJECTED, ACTIVE 등)
  - searchField: String (NAME, LOGIN_ID, PHONE_NUMBER)
  - searchWord: String
  - startDate/endDate: String (yyyy-MM-dd)
  - page, size, sortKey, sortDirection
- **Response DTO**: `SellerAdminApplicationPageApiResponse`
- **ApiMapper**: `SellerAdminApplicationQueryApiMapper`
  - toSearchParams(): Request → SearchParams 변환
  - DateRange 파싱, Status enum 변환 처리

**Application Layer**
- **UseCase**: `SearchSellerAdminApplicationsUseCase`
- **Service**: `SearchSellerAdminApplicationsService`
- **Params**: `SellerAdminApplicationSearchParams`
- **Result**: `SellerAdminApplicationPageResult`
- **Factory**: `SellerAdminQueryFactory` - Criteria 생성
- **Manager**: `SellerAdminReadManager` - Port 조율
- **Assembler**: `SellerAdminAssembler` - Domain → Result 변환

**Domain Layer**
- **Port**: `SellerAdminQueryPort.findByCriteria()`
- **SearchCriteria**: `SellerAdminSearchCriteria`
  - sellerIds, statuses, searchField, searchWord, dateRange
  - page, size, sortKey, sortDirection

**Adapter-Out Layer**
- **Adapter**: `SellerAdminQueryAdapter`
- **Repository**: `SellerAdminQueryDslRepository`
- **JPA Entity**: `SellerAdminJpaEntity`
- **EntityMapper**: `SellerAdminJpaEntityMapper`

**Database Query**
- **테이블**: seller_admin
- **WHERE**: seller_id IN (?), status IN (?), search_field LIKE ?, created_at BETWEEN
- **ORDER BY**: created_at DESC (기본값)
- **PAGING**: LIMIT ? OFFSET ?

---

### 2. GET /seller-admins/{id} - 상세 조회

특정 셀러 관리자 가입 신청의 상세 정보를 조회합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationQueryController.get(sellerAdminId)
  ├─ SellerAdminApplicationQueryApiMapper.toGetQuery(sellerAdminId)
  │   └─ → GetSellerAdminApplicationQuery
  │
  ├─ GetSellerAdminApplicationUseCase.execute(query)                   [Port]
  │   └─ GetSellerAdminApplicationService.execute(query)              [Impl]
  │       ├─ SellerAdminId.of(query.sellerAdminId())
  │       │
  │       ├─ SellerAdminReadManager.getByIdAndStatuses(sellerAdminId, statuses)
  │       │   └─ SellerAdminQueryPort.findByIdAndStatuses()           [Port]
  │       │       └─ SellerAdminQueryAdapter.findByIdAndStatuses()    [Impl]
  │       │           └─ SellerAdminQueryDslRepository.findByIdAndStatuses()
  │       │               └─ QueryDSL: WHERE id = ? AND status IN (?)
  │       │
  │       └─ SellerAdminAssembler.toResult(sellerAdmin)
  │           └─ → SellerAdminApplicationResult
  │
  └─ SellerAdminApplicationApiResponse.from(result)
      └─ → ApiResponse<SellerAdminApplicationApiResponse>
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationQueryController`
- **Request**: Path Variable `sellerAdminId` (UUIDv7 String)
- **Response DTO**: `SellerAdminApplicationApiResponse`
  - sellerAdminId, sellerId, loginId, name, phoneNumber
  - status, createdAt, updatedAt
- **ApiMapper**: `SellerAdminApplicationQueryApiMapper.toGetQuery()`

**Application Layer**
- **UseCase**: `GetSellerAdminApplicationUseCase`
- **Service**: `GetSellerAdminApplicationService`
- **Query**: `GetSellerAdminApplicationQuery`
  - sellerAdminId
  - statuses (기본값: PENDING_APPROVAL, REJECTED)
- **Result**: `SellerAdminApplicationResult`

**Domain Layer**
- **Port**: `SellerAdminQueryPort.findByIdAndStatuses()`
- **Aggregate**: `SellerAdmin`

**Adapter-Out Layer**
- **Adapter**: `SellerAdminQueryAdapter`
- **Repository**: `SellerAdminQueryDslRepository`

**Database Query**
- **WHERE**: id = ? AND status IN ('PENDING_APPROVAL', 'REJECTED')
- **예외**: 찾지 못하면 SellerAdminNotFoundException

---

## Command 엔드포인트

### 3. POST /seller-admins - 가입 신청

셀러 하위에 새로운 관리자 가입을 신청합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.apply(ApplySellerAdminApiRequest)
  ├─ SellerAdminApplicationCommandApiMapper.toCommand(request)
  │   └─ → ApplySellerAdminCommand
  │
  ├─ ApplySellerAdminUseCase.execute(command)                          [Port]
  │   └─ ApplySellerAdminService.execute(command)                     [Impl]
  │       ├─ SellerAdminValidator.validateSellerExists(command.sellerId())
  │       │   └─ SellerQueryPort → SellerNotFoundException
  │       │
  │       ├─ SellerAdminCommandFactory.createForApplication(command)
  │       │   └─ SellerAdmin.apply(sellerId, loginId, name, phoneNumber, password)
  │       │       └─ → SellerAdmin (PENDING_APPROVAL 상태)
  │       │
  │       └─ SellerAdminCommandManager.persist(admin)
  │           └─ SellerAdminCommandPort.persist(admin)                [Port]
  │               └─ SellerAdminCommandAdapter.persist(admin)         [Impl]
  │                   ├─ SellerAdminJpaEntityMapper.toEntity(admin)
  │                   └─ SellerAdminJpaRepository.save(entity)
  │                       └─ INSERT INTO seller_admin (...)
  │
  └─ ApplySellerAdminApiResponse(sellerAdminId)
      └─ → ApiResponse<ApplySellerAdminApiResponse> (201 Created)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request DTO**: `ApplySellerAdminApiRequest`
  - sellerId: Long (필수)
  - loginId: String (필수, 중복 검증)
  - name: String (필수)
  - phoneNumber: String (필수)
  - password: String (필수, 최소 8자 이상)
- **Response DTO**: `ApplySellerAdminApiResponse(sellerAdminId)`
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper.toCommand()`

**Application Layer**
- **UseCase**: `ApplySellerAdminUseCase`
- **Service**: `ApplySellerAdminService`
- **Command**: `ApplySellerAdminCommand`
- **Validator**: `SellerAdminValidator.validateSellerExists()`
- **Factory**: `SellerAdminCommandFactory.createForApplication()`
- **Manager**: `SellerAdminCommandManager.persist()`

**Domain Layer**
- **Port**: `SellerAdminCommandPort.persist()`
- **Aggregate**: `SellerAdmin.apply()`
  - UUIDv7 ID 자동 생성
  - 상태: PENDING_APPROVAL
  - 비밀번호 암호화 처리

**Adapter-Out Layer**
- **Adapter**: `SellerAdminCommandAdapter`
- **Repository**: `SellerAdminJpaRepository`
- **EntityMapper**: `SellerAdminJpaEntityMapper`

**Database Query**
- **INSERT**: seller_admin (id, seller_id, login_id, name, phone_number, password_hash, status, created_at, updated_at)

**비즈니스 규칙**
- sellerId 존재 여부 검증 필수
- loginId 중복 불가
- 최초 상태는 PENDING_APPROVAL
- authUserId는 승인 시 생성됨

---

### 4. POST /seller-admins/{id}/approve - 승인

PENDING_APPROVAL 상태의 가입 신청을 승인하고 인증 서버 연동용 Outbox를 생성합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.approve(sellerAdminId)
  ├─ SellerAdminApplicationCommandApiMapper.toApproveCommand(sellerAdminId)
  │   └─ → ApproveSellerAdminCommand
  │
  ├─ ApproveSellerAdminUseCase.execute(command)                        [Port]
  │   └─ ApproveSellerAdminService.execute(command)                   [Impl]
  │       ├─ SellerAdminApprovalCoordinator.approve(command)
  │       │   ├─ SellerAdminReadManager.getByIdAndStatuses(id, [PENDING_APPROVAL])
  │       │   │   └─ SellerAdminQueryPort.findByIdAndStatuses()
  │       │   │
  │       │   ├─ SellerAdminCommandFactory.createApprovalBundle(sellerAdmin)
  │       │   │   └─ → SellerAdminApprovalBundle
  │       │   │       ├─ SellerAdmin (with authUserId)
  │       │   │       └─ SellerAdminAuthOutbox (PENDING 상태)
  │       │   │
  │       │   └─ SellerAdminApprovalFacade.approveAndPersist(bundle)
  │       │       ├─ SellerAdmin.approve(authUserId, approvedAt)
  │       │       │   ├─ 상태: PENDING_APPROVAL → ACTIVE
  │       │       │   └─ 이벤트: SellerAdminApprovedEvent 등록
  │       │       │
  │       │       ├─ SellerAdminCommandManager.persist(admin)
  │       │       │   └─ SellerAdminCommandPort.persist()
  │       │       │       └─ UPDATE seller_admin SET status='ACTIVE', auth_user_id=?, updated_at=?
  │       │       │
  │       │       └─ SellerAdminAuthOutboxCommandManager.persist(outbox)
  │       │           └─ SellerAdminAuthOutboxCommandPort.persist()
  │       │               └─ INSERT INTO seller_admin_auth_outbox
  │       │
  │       └─ TransactionEventRegistry.registerForPublish(events)
  │           └─ SellerAdminApprovedEvent (트랜잭션 커밋 후 발행)
  │
  └─ ApproveSellerAdminApiResponse.from(savedSellerAdminId)
      └─ → ApiResponse<ApproveSellerAdminApiResponse> (200 OK)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request**: Path Variable `sellerAdminId`
- **Response DTO**: `ApproveSellerAdminApiResponse(sellerAdminId)`
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper.toApproveCommand()`

**Application Layer**
- **UseCase**: `ApproveSellerAdminUseCase`
- **Service**: `ApproveSellerAdminService`
- **Command**: `ApproveSellerAdminCommand`
- **Coordinator**: `SellerAdminApprovalCoordinator`
  - 검증 → Bundle 생성 → 승인 및 저장 조율
- **Facade**: `SellerAdminApprovalFacade`
  - @Transactional 경계 (승인 + Outbox 저장)
- **Bundle**: `SellerAdminApprovalBundle`
  - SellerAdmin + SellerAdminAuthOutbox
- **Event**: `SellerAdminApprovedEvent`
  - 트랜잭션 커밋 후 이벤트 리스너가 처리

**Domain Layer**
- **Port**: `SellerAdminCommandPort.persist()`
- **Aggregate**: `SellerAdmin.approve()`
  - 상태 검증: PENDING_APPROVAL만 승인 가능
  - authUserId 설정 (UUIDv7)
  - 상태 변경: ACTIVE
  - 도메인 이벤트 등록

**Adapter-Out Layer**
- **Adapter**: `SellerAdminCommandAdapter`
- **AuthOutbox Adapter**: `SellerAdminAuthOutboxCommandAdapter`
- **Repository**: `SellerAdminJpaRepository`, `SellerAdminAuthOutboxJpaRepository`

**Database Query**
- **UPDATE**: seller_admin SET status='ACTIVE', auth_user_id=?, updated_at=? WHERE id=?
- **INSERT**: seller_admin_auth_outbox (인증 서버 연동 대기 메시지)

**비동기 처리**
- Outbox는 스케줄러/이벤트 리스너가 주기적으로 처리
- 실제 인증 서버 연동은 비동기로 수행
- 실패 시 재시도 가능 (Outbox 패턴)

---

### 5. POST /seller-admins/{id}/reject - 거절

PENDING_APPROVAL 상태의 가입 신청을 거절합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.reject(sellerAdminId)
  ├─ SellerAdminApplicationCommandApiMapper.toRejectCommand(sellerAdminId)
  │   └─ → RejectSellerAdminCommand
  │
  ├─ RejectSellerAdminUseCase.execute(command)                         [Port]
  │   └─ RejectSellerAdminService.execute(command)                    [Impl]
  │       ├─ SellerAdminCommandFactory.createRejectContext(command)
  │       │   └─ → StatusChangeContext<SellerAdminId>
  │       │
  │       ├─ SellerAdminReadManager.getById(context.id())
  │       │   └─ SellerAdminQueryPort.findById()
  │       │
  │       ├─ SellerAdmin.reject(context.changedAt())
  │       │   ├─ 상태 검증: PENDING_APPROVAL만 거절 가능
  │       │   └─ 상태 변경: PENDING_APPROVAL → REJECTED
  │       │
  │       └─ SellerAdminCommandManager.persist(sellerAdmin)
  │           └─ SellerAdminCommandPort.persist()
  │               └─ UPDATE seller_admin SET status='REJECTED', updated_at=?
  │
  └─ ResponseEntity.noContent() (204 No Content)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request**: Path Variable `sellerAdminId`
- **Response**: 204 No Content
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper.toRejectCommand()`

**Application Layer**
- **UseCase**: `RejectSellerAdminUseCase`
- **Service**: `RejectSellerAdminService`
- **Command**: `RejectSellerAdminCommand`
- **Factory**: `SellerAdminCommandFactory.createRejectContext()`
- **Context**: `StatusChangeContext<SellerAdminId>`
  - TimeProvider를 통한 타임스탬프 주입

**Domain Layer**
- **Port**: `SellerAdminCommandPort.persist()`
- **Aggregate**: `SellerAdmin.reject()`
  - 상태 검증: PENDING_APPROVAL만 가능
  - 상태 변경: REJECTED

**Database Query**
- **UPDATE**: seller_admin SET status='REJECTED', updated_at=? WHERE id=?

---

### 6. POST /seller-admins/batch-approve - 일괄 승인

여러 건의 가입 신청을 한 번에 승인합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.bulkApprove(BulkApproveSellerAdminApiRequest)
  ├─ SellerAdminApplicationCommandApiMapper.toBulkApproveCommand(request)
  │   └─ → BulkApproveSellerAdminCommand
  │
  ├─ BulkApproveSellerAdminUseCase.execute(command)                    [Port]
  │   └─ BulkApproveSellerAdminService.execute(command)               [Impl]
  │       └─ for (sellerAdminId : command.sellerAdminIds()) {
  │           ├─ ApproveSellerAdminCommand(sellerAdminId)
  │           │
  │           ├─ SellerAdminApprovalCoordinator.approve(approveCommand)
  │           │   └─ (단일 승인과 동일한 흐름)
  │           │
  │           ├─ SUCCESS → BatchItemResult.success(sellerAdminId)
  │           └─ FAILURE → BatchItemResult.failure(sellerAdminId, errorCode, message)
  │       }
  │       └─ → BatchProcessingResult<String>
  │
  ├─ SellerAdminApplicationCommandApiMapper.toResponse(result)
  │   └─ → BulkApproveSellerAdminApiResponse
  │
  └─ ApiResponse<BulkApproveSellerAdminApiResponse> (200 OK)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request DTO**: `BulkApproveSellerAdminApiRequest`
  - sellerAdminIds: List<String> (필수)
- **Response DTO**: `BulkApproveSellerAdminApiResponse`
  - totalCount, successCount, failureCount
  - items: List<ItemResult> (id, success, errorCode, errorMessage)
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper`
  - toBulkApproveCommand()
  - toResponse(BatchProcessingResult)

**Application Layer**
- **UseCase**: `BulkApproveSellerAdminUseCase`
- **Service**: `BulkApproveSellerAdminService`
- **Command**: `BulkApproveSellerAdminCommand`
- **Result**: `BatchProcessingResult<String>`
- **Coordinator**: `SellerAdminApprovalCoordinator` (각 항목별 호출)

**처리 방식**
- 순차 처리 (for loop)
- 실패해도 다음 항목 계속 진행
- 각 항목의 성공/실패 개별 기록
- DomainException 캐치하여 실패 결과 생성

**Database Query**
- 각 항목마다 개별 트랜잭션
- 실패한 항목은 롤백, 성공한 항목은 커밋

---

### 7. POST /seller-admins/batch-reject - 일괄 거절

여러 건의 가입 신청을 한 번에 거절합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.bulkReject(BulkRejectSellerAdminApiRequest)
  ├─ SellerAdminApplicationCommandApiMapper.toBulkRejectCommand(request)
  │   └─ → BulkRejectSellerAdminCommand
  │
  ├─ BulkRejectSellerAdminUseCase.execute(command)                     [Port]
  │   └─ BulkRejectSellerAdminService.execute(command)                [Impl]
  │       ├─ SellerAdminCommandFactory.createBulkRejectContext(command)
  │       │   └─ → BulkStatusChangeContext<SellerAdminId>
  │       │
  │       ├─ SellerAdminReadManager.getAllByIds(context.ids())
  │       │   └─ SellerAdminQueryPort.findAllByIds()
  │       │       └─ QueryDSL: WHERE id IN (?, ?, ...)
  │       │
  │       ├─ for (SellerAdmin sellerAdmin : sellerAdmins) {
  │       │   └─ sellerAdmin.reject(context.changedAt())
  │       │ }
  │       │
  │       └─ SellerAdminCommandManager.persistAll(sellerAdmins)
  │           └─ SellerAdminCommandPort.persistAll()
  │               └─ UPDATE seller_admin SET status='REJECTED', updated_at=? WHERE id IN (?)
  │
  └─ ResponseEntity.noContent() (204 No Content)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request DTO**: `BulkRejectSellerAdminApiRequest`
  - sellerAdminIds: List<String> (필수)
- **Response**: 204 No Content
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper.toBulkRejectCommand()`

**Application Layer**
- **UseCase**: `BulkRejectSellerAdminUseCase`
- **Service**: `BulkRejectSellerAdminService`
- **Command**: `BulkRejectSellerAdminCommand`
- **Factory**: `SellerAdminCommandFactory.createBulkRejectContext()`
- **Context**: `BulkStatusChangeContext<SellerAdminId>`

**처리 방식**
- 일괄 조회 → 일괄 상태 변경 → 일괄 저장
- 하나의 트랜잭션에서 모두 처리
- 실패 시 전체 롤백

**Database Query**
- **SELECT**: WHERE id IN (?, ?, ...)
- **UPDATE**: SET status='REJECTED', updated_at=? WHERE id IN (?, ?, ...)

---

### 8. POST /seller-admins/{id}/reset-password - 비밀번호 초기화

ACTIVE 상태의 셀러 관리자 비밀번호를 초기화합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.resetPassword(sellerAdminId)
  ├─ SellerAdminApplicationCommandApiMapper.toResetPasswordCommand(sellerAdminId)
  │   └─ → ResetSellerAdminPasswordCommand
  │
  ├─ ResetSellerAdminPasswordUseCase.execute(command)                  [Port]
  │   └─ ResetSellerAdminPasswordService.execute(command)             [Impl]
  │       ├─ SellerAdminId.of(command.sellerAdminId())
  │       │
  │       ├─ SellerAdminReadManager.getById(sellerAdminId)
  │       │   └─ SellerAdminQueryPort.findById()
  │       │
  │       ├─ SellerAdmin.validatePasswordResetEligibility()
  │       │   ├─ 상태 검증: ACTIVE만 가능
  │       │   └─ authUserId 존재 여부 검증
  │       │
  │       └─ SellerAdminIdentityClient.resetSellerAdminPassword(authUserId)
  │           └─ 외부 인증 서버 (AuthHub) 동기 호출
  │               └─ POST /auth/seller-admins/{authUserId}/reset-password
  │
  └─ ResponseEntity.noContent() (204 No Content)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request**: Path Variable `sellerAdminId`
- **Response**: 204 No Content
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper.toResetPasswordCommand()`

**Application Layer**
- **UseCase**: `ResetSellerAdminPasswordUseCase`
- **Service**: `ResetSellerAdminPasswordService`
- **Command**: `ResetSellerAdminPasswordCommand`
- **Client**: `SellerAdminIdentityClient` (외부 인증 서버 연동)

**Domain Layer**
- **Aggregate**: `SellerAdmin.validatePasswordResetEligibility()`
  - 상태: ACTIVE만 가능
  - authUserId 필수

**외부 연동**
- **인증 서버**: AuthHub (동기 호출)
- **기능**: 임시 비밀번호 생성 및 이메일/문자 발송
- **실패 처리**: Exception 발생 시 롤백

---

### 9. PATCH /seller-admins/{id}/password - 비밀번호 변경

본인인증 완료 후 새 비밀번호로 변경합니다.

#### 호출 흐름

```
[Adapter-In]
SellerAdminApplicationCommandController.changePassword(sellerAdminId, ChangeSellerAdminPasswordApiRequest)
  ├─ SellerAdminApplicationCommandApiMapper.toChangePasswordCommand(sellerAdminId, request)
  │   └─ → ChangeSellerAdminPasswordCommand
  │
  ├─ ChangeSellerAdminPasswordUseCase.execute(command)                 [Port]
  │   └─ ChangeSellerAdminPasswordService.execute(command)            [Impl]
  │       ├─ SellerAdminId.of(command.sellerAdminId())
  │       │
  │       ├─ SellerAdminReadManager.getById(sellerAdminId)
  │       │   └─ SellerAdminQueryPort.findById()
  │       │
  │       ├─ SellerAdmin.validatePasswordChangeEligibility()
  │       │   ├─ 상태 검증: ACTIVE만 가능
  │       │   └─ authUserId 존재 여부 검증
  │       │
  │       └─ SellerAdminIdentityClient.changeSellerAdminPassword(authUserId, newPassword)
  │           └─ 외부 인증 서버 (AuthHub) 동기 호출
  │               └─ PATCH /auth/seller-admins/{authUserId}/password
  │
  └─ ResponseEntity.noContent() (204 No Content)
```

#### 레이어별 상세

**Adapter-In Layer**
- **Controller**: `SellerAdminApplicationCommandController`
- **Request**:
  - Path Variable: `sellerAdminId`
  - Body: `ChangeSellerAdminPasswordApiRequest`
    - newPassword: String (필수, 최소 8자 이상)
- **Response**: 204 No Content
- **ApiMapper**: `SellerAdminApplicationCommandApiMapper.toChangePasswordCommand()`

**Application Layer**
- **UseCase**: `ChangeSellerAdminPasswordUseCase`
- **Service**: `ChangeSellerAdminPasswordService`
- **Command**: `ChangeSellerAdminPasswordCommand`
  - sellerAdminId, newPassword
- **Client**: `SellerAdminIdentityClient`

**Domain Layer**
- **Aggregate**: `SellerAdmin.validatePasswordChangeEligibility()`
  - 상태: ACTIVE만 가능
  - authUserId 필수

**외부 연동**
- **인증 서버**: AuthHub (동기 호출)
- **기능**: 새 비밀번호로 업데이트
- **전제조건**: 외부 본인인증(이메일/문자) 완료 필수

---

## 아키텍처 패턴 요약

### Hexagonal Architecture 레이어

```
┌─────────────────────────────────────────────────────────────┐
│  Adapter-In (REST API)                                      │
│  - Controller: HTTP 요청 수신                                │
│  - ApiMapper: Request → Command/Query 변환                  │
│  - ApiResponse: Result → Response DTO 변환                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Application (Use Case)                                     │
│  - UseCase (Port-In): 비즈니스 인터페이스                    │
│  - Service: UseCase 구현체                                  │
│  - Factory: Command/Criteria 생성                           │
│  - Manager: Port-Out 조율                                   │
│  - Assembler: Domain → Result 변환                          │
│  - Coordinator: 복잡한 비즈니스 플로우 조율                   │
│  - Facade: 트랜잭션 경계                                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Domain (Core Business Logic)                               │
│  - Aggregate: SellerAdmin                                   │
│  - Port-Out: QueryPort, CommandPort                         │
│  - SearchCriteria: 검색 조건 객체                            │
│  - Domain Event: SellerAdminApprovedEvent                   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Adapter-Out (Persistence)                                  │
│  - Adapter: Port-Out 구현체                                 │
│  - Repository: JpaRepository, QueryDslRepository            │
│  - JpaEntity: 영속성 엔티티                                  │
│  - EntityMapper: Domain ↔ Entity 변환                       │
└─────────────────────────────────────────────────────────────┘
```

### CQRS 분리

- **Query Controller**: `SellerAdminApplicationQueryController`
- **Command Controller**: `SellerAdminApplicationCommandController`
- **Query Service**: 읽기 전용, @Transactional 없음
- **Command Service**: 쓰기, @Transactional(Facade에서)

### 트랜잭션 경계

- **Facade**: `SellerAdminApprovalFacade.approveAndPersist()` (@Transactional)
- **Service**: @Transactional 없음 (Facade가 제어)
- **Controller**: @Transactional 금지
- **Adapter**: @Transactional 금지

### 도메인 이벤트

- **이벤트 등록**: `SellerAdmin.approve()` → `SellerAdminApprovedEvent`
- **이벤트 발행**: `TransactionEventRegistry.registerForPublish()`
- **이벤트 처리**: `SellerAdminApprovedEventListener` (트랜잭션 커밋 후)

### Outbox 패턴

- **용도**: 인증 서버 연동 메시지 임시 저장
- **생성**: 승인 시 SellerAdminAuthOutbox (PENDING 상태)
- **처리**: 스케줄러/이벤트 리스너가 주기적으로 처리
- **재시도**: 실패 시 재시도 가능 (Idempotent)

### 외부 클라이언트

- **SellerAdminIdentityClient**: 인증 서버 (AuthHub) 연동
  - resetSellerAdminPassword()
  - changeSellerAdminPassword()
- **SellerAdminEmailClient**: 이메일 발송 연동

---

## 데이터베이스 테이블

### seller_admin

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | VARCHAR(36) | UUIDv7 (PK) |
| seller_id | BIGINT | 셀러 ID (FK) |
| login_id | VARCHAR(100) | 로그인 ID (UNIQUE) |
| name | VARCHAR(100) | 이름 |
| phone_number | VARCHAR(20) | 전화번호 |
| password_hash | VARCHAR(255) | 비밀번호 해시 |
| auth_user_id | VARCHAR(36) | 인증 서버 사용자 ID (승인 후 설정) |
| status | VARCHAR(20) | 상태 (PENDING_APPROVAL, ACTIVE, REJECTED, INACTIVE) |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

### seller_admin_auth_outbox

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT | Auto Increment (PK) |
| seller_admin_id | VARCHAR(36) | 셀러 관리자 ID (FK) |
| event_type | VARCHAR(50) | 이벤트 타입 (SELLER_ADMIN_APPROVED) |
| payload | JSON | 이벤트 페이로드 |
| status | VARCHAR(20) | 상태 (PENDING, PROCESSING, COMPLETED, FAILED) |
| retry_count | INT | 재시도 횟수 |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

---

## 주요 비즈니스 규칙

### 상태 전이

```
PENDING_APPROVAL
  ├─ approve() → ACTIVE
  └─ reject() → REJECTED

REJECTED
  └─ approve() → ACTIVE (재승인 가능)

ACTIVE
  ├─ 비밀번호 초기화 가능
  └─ 비밀번호 변경 가능
```

### 검증 규칙

1. **가입 신청**
   - sellerId 존재 여부 필수
   - loginId 중복 불가
   - 비밀번호 최소 8자 이상

2. **승인**
   - PENDING_APPROVAL 또는 REJECTED 상태만 가능
   - authUserId 자동 생성 (UUIDv7)
   - Outbox 생성하여 비동기 처리

3. **거절**
   - PENDING_APPROVAL 상태만 가능

4. **비밀번호 초기화**
   - ACTIVE 상태만 가능
   - authUserId 존재 필수
   - 외부 인증 서버 동기 호출

5. **비밀번호 변경**
   - ACTIVE 상태만 가능
   - authUserId 존재 필수
   - 외부 본인인증 완료 전제

---

## 에러 처리

### Domain Exception

- `SellerAdminNotFoundException`: 셀러 관리자를 찾을 수 없음
- `SellerAdminInvalidStatusException`: 상태 전이 불가
- `LoginIdDuplicateException`: 로그인 ID 중복
- `PasswordResetNotAllowedException`: 비밀번호 초기화 불가 상태

### 에러 매핑

- `SellerAdminErrorMapper`: Domain Exception → API Error Response
- `ErrorMapperRegistry`: 전역 에러 매퍼 등록

---

## 테스트 전략

### 단위 테스트

- **Domain**: Aggregate 비즈니스 로직 검증
- **Application**: Service, Factory, Manager 단위 테스트
- **Adapter**: Mapper, Repository 단위 테스트

### 통합 테스트

- **API**: @WebMvcTest (Controller + UseCase Mock)
- **Persistence**: @DataJpaTest (Repository + QueryDSL)
- **E2E**: RestAssured (전체 흐름)

### 테스트 픽스처

- `domain/src/testFixtures`: Domain 객체 픽스처
- `application/src/testFixtures`: Application DTO 픽스처
- `adapter-in/rest-api/src/testFixtures`: API DTO 픽스처

---

## 참고 사항

### TimeProvider 사용

- **APP-TIM-001**: Service에서 TimeProvider 직접 사용 금지
- Factory에서 TimeProvider를 통해 타임스탬프 주입
- 테스트 시 FakeTimeProvider 사용 가능

### Manager 패턴

- **ReadManager**: Query Port 조율, 공통 조회 로직
- **CommandManager**: Command Port 조율, 공통 저장 로직

### Coordinator/Facade 패턴

- **Coordinator**: 복잡한 비즈니스 플로우 조율 (트랜잭션 없음)
- **Facade**: 트랜잭션 경계 제공 (@Transactional)

---

## 문서 버전

- **작성일**: 2026-02-06
- **작성자**: Claude Code
- **기준 코드**: feature/MAR-56-full-project-refactoring 브랜치
- **Spring Boot**: 3.5.x
- **Java**: 21
