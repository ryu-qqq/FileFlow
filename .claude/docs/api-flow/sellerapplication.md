# SellerApplication API Flow Analysis

셀러 입점 신청(SellerApplication) 도메인의 전체 API 호출 흐름 분석 문서.

---

## 목차

- [1. GET /seller-applications - 목록 조회](#1-get-seller-applications---목록-조회)
- [2. GET /seller-applications/{id} - 상세 조회](#2-get-seller-applicationsid---상세-조회)
- [3. POST /seller-applications - 입점 신청](#3-post-seller-applications---입점-신청)
- [4. POST /seller-applications/{id}/approve - 승인](#4-post-seller-applicationsidapprove---승인)
- [5. POST /seller-applications/{id}/reject - 거절](#5-post-seller-applicationsidreject---거절)

---

## 1. GET /seller-applications - 목록 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /api/v1/market/admin/seller-applications |
| Controller | SellerApplicationQueryController |
| Method | search |
| UseCase | SearchSellerApplicationByOffsetUseCase |
| Service | SearchSellerApplicationByOffsetService |

### 호출 흐름

```
SellerApplicationQueryController.search(SearchSellerApplicationsApiRequest)
  |- SellerApplicationQueryApiMapper.toSearchParams(request)
  |   +-- -> SellerApplicationSearchParams
  |
  |- SearchSellerApplicationByOffsetUseCase.execute(params)           [Port]
  |   +-- SearchSellerApplicationByOffsetService.execute(params)       [Impl]
  |       |- SellerApplicationQueryFactory.createCriteria(params)
  |       |   +-- -> SellerApplicationSearchCriteria
  |       |
  |       |- SellerApplicationReadManager.findByCriteria(criteria)
  |       |   +-- SellerApplicationQueryPort.findByCriteria(criteria)  [Port]
  |       |       +-- SellerApplicationQueryAdapter.findByCriteria()   [Impl]
  |       |           +-- SellerApplicationQueryDslRepository.findByCriteria()
  |       |               +-- QueryDSL: WHERE + ORDER BY + LIMIT/OFFSET
  |       |               +-- -> List<SellerApplicationJpaEntity>
  |       |           +-- SellerApplicationJpaEntityMapper.toDomain()
  |       |               +-- -> List<SellerApplication>
  |       |
  |       |- SellerApplicationReadManager.countByCriteria(criteria)
  |       |   +-- SellerApplicationQueryPort.countByCriteria(criteria)
  |       |       +-- SellerApplicationQueryAdapter.countByCriteria()
  |       |           +-- SellerApplicationQueryDslRepository.countByCriteria()
  |       |
  |       +-- SellerApplicationAssembler.toPageResult()
  |           +-- -> SellerApplicationPageResult
  |
  +-- SellerApplicationQueryApiMapper.toPageResponse(pageResult)
      +-- -> PageApiResponse<SellerApplicationApiResponse>
          +-- -> ApiResponse<PageApiResponse<...>>
```

### Layer별 상세

#### Adapter-In (REST API)
- **Controller**: `SellerApplicationQueryController`
- **Request DTO**: `SearchSellerApplicationsApiRequest`
  - `status`: List<String> - 신청 상태 필터 (PENDING, APPROVED, REJECTED)
  - `searchField`: String - 검색 필드 (sellerName, companyName)
  - `searchWord`: String - 검색어
  - `page`: Integer - 페이지 번호 (0부터, 기본값: 0)
  - `size`: Integer - 페이지 크기 (기본값: 20, 최대: 100)
  - `sortKey`: String - 정렬 키 (기본값: appliedAt)
  - `sortDirection`: String - 정렬 방향 (기본값: DESC)
- **Response DTO**: `SellerApplicationApiResponse`
- **Mapper**: `SellerApplicationQueryApiMapper`
  - `toSearchParams()`: ApiRequest → SearchParams
  - `toPageResponse()`: PageResult → PageApiResponse

#### Application Layer
- **UseCase**: `SearchSellerApplicationByOffsetUseCase`
- **Service**: `SearchSellerApplicationByOffsetService`
- **Input**: `SellerApplicationSearchParams`
- **Output**: `SellerApplicationPageResult`
- **Manager**: `SellerApplicationReadManager`
  - `findByCriteria()`: 목록 조회
  - `countByCriteria()`: 개수 조회
- **Factory**: `SellerApplicationQueryFactory`
  - `createCriteria()`: Params → Criteria 변환
- **Assembler**: `SellerApplicationAssembler`
  - `toPageResult()`: Domain → Result 변환

#### Domain Layer
- **Port**: `SellerApplicationQueryPort.findByCriteria()`
- **Domain**: `SellerApplication` (Aggregate)
- **Criteria**: `SellerApplicationSearchCriteria`

#### Adapter-Out (Persistence)
- **Adapter**: `SellerApplicationQueryAdapter`
- **Repository**: `SellerApplicationQueryDslRepository`
- **QueryDSL**:
  - WHERE: status IN (...), searchField LIKE ...
  - ORDER BY: appliedAt DESC
  - LIMIT/OFFSET: 페이징
- **Entity**: `SellerApplicationJpaEntity`
- **Mapper**: `SellerApplicationJpaEntityMapper`
  - `toDomain()`: Entity → Domain

#### Database Query
- **Table**: `seller_application`
- **Conditions**:
  - `status IN (?, ?, ...)` (선택)
  - `seller_name LIKE ?` 또는 `company_name LIKE ?` (검색어)
- **Order**: `applied_at DESC`
- **Paging**: `LIMIT ? OFFSET ?`

---

## 2. GET /seller-applications/{id} - 상세 조회

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /api/v1/market/admin/seller-applications/{applicationId} |
| Controller | SellerApplicationQueryController |
| Method | get |
| UseCase | GetSellerApplicationUseCase |
| Service | GetSellerApplicationService |

### 호출 흐름

```
SellerApplicationQueryController.get(applicationId)
  |- GetSellerApplicationUseCase.execute(applicationId)                [Port]
  |   +-- GetSellerApplicationService.execute(applicationId)            [Impl]
  |       |- SellerApplicationId.of(applicationId)
  |       |
  |       |- SellerApplicationReadManager.getById(id)
  |       |   +-- SellerApplicationQueryPort.findById(id)              [Port]
  |       |       +-- SellerApplicationQueryAdapter.findById(id)       [Impl]
  |       |           +-- SellerApplicationQueryDslRepository.findById()
  |       |               +-- QueryDSL: WHERE id = ?
  |       |               +-- -> SellerApplicationJpaEntity
  |       |           +-- SellerApplicationJpaEntityMapper.toDomain()
  |       |               +-- -> SellerApplication
  |       |   +-- SellerApplicationNotFoundException (if not found)
  |       |
  |       +-- SellerApplicationAssembler.toResult()
  |           +-- -> SellerApplicationResult
  |
  +-- SellerApplicationQueryApiMapper.toResponse(result)
      +-- -> SellerApplicationApiResponse
          +-- -> ApiResponse<SellerApplicationApiResponse>
```

### Layer별 상세

#### Adapter-In
- **Path Variable**: `applicationId` (Long)
- **Response**: 전체 입점 신청 정보
  - SellerInfo (셀러명, 표시명, 로고, 설명)
  - BusinessInfo (사업자등록번호, 회사명, 대표자, 주소)
  - CsContactInfo (전화, 이메일, 모바일)
  - ContactInfo (담당자 연락처)
  - AgreementInfo (약관 동의 정보)
  - status, appliedAt, processedAt, rejectionReason 등

#### Application Layer
- **Service**: `GetSellerApplicationService`
- **Manager**: `SellerApplicationReadManager.getById()`
  - 없으면 `SellerApplicationNotFoundException` 발생

#### Domain Layer
- **Port**: `SellerApplicationQueryPort.findById()`

#### Adapter-Out
- **QueryDSL**: `WHERE seller_application.id = ?`

---

## 3. POST /seller-applications - 입점 신청

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | /api/v1/market/admin/seller-applications |
| Controller | SellerApplicationCommandController |
| Method | apply |
| UseCase | ApplySellerApplicationUseCase |
| Service | ApplySellerApplicationService |

### 호출 흐름

```
SellerApplicationCommandController.apply(@RequestBody ApplySellerApplicationApiRequest)
  |- SellerApplicationCommandApiMapper.toCommand(request)
  |   +-- -> ApplySellerApplicationCommand
  |
  |- ApplySellerApplicationUseCase.execute(command)                    [Port]
  |   +-- ApplySellerApplicationService.execute(command)               [Impl]
  |       |- SellerApplicationValidator.validateNoPendingApplication()
  |       |   +-- SellerApplicationReadManager.existsPendingByRegistrationNumber()
  |       |       +-- SellerApplicationQueryPort.existsPendingByRegistrationNumber()
  |       |           +-- QueryDSL: WHERE registration_number = ? AND status = 'PENDING'
  |       |   +-- DuplicateSellerApplicationException (if exists)
  |       |
  |       |- SellerApplicationCommandFactory.create(command)
  |       |   +-- -> SellerApplication (new aggregate)
  |       |       +-- SellerApplicationCreatedEvent
  |       |
  |       |- SellerApplicationCommandManager.persist(application)
  |       |   +-- SellerApplicationCommandPort.persist(application)    [Port]
  |       |       +-- SellerApplicationCommandAdapter.persist()         [Impl]
  |       |           +-- SellerApplicationJpaEntityMapper.toEntity()
  |       |           +-- SellerApplicationJpaRepository.save(entity)
  |       |           +-- -> applicationId (Long)
  |       |
  |       +-- application.pollEvents() → TransactionEventRegistry.registerForPublish()
  |           +-- -> applicationId
  |
  +-- -> ApiResponse<ApplySellerApplicationApiResponse(applicationId)>
      +-- HTTP 201 Created
```

### Layer별 상세

#### Adapter-In
- **Request DTO**: `ApplySellerApplicationApiRequest`
  - `sellerInfo`: SellerInfo (셀러명, 표시명, 로고, 설명)
  - `businessInfo`: BusinessInfo (사업자등록번호*, 회사명*, 대표자*, 주소*)
  - `csContact`: CsContactInfo (전화*, 이메일*, 모바일)
  - `contactInfo`: ContactInfo (담당자 연락처)
  - `settlementInfo`: SettlementInfo (정산 정보*)
  - *: 필수 필드
- **Response**: `ApplySellerApplicationApiResponse(applicationId)`
- **Mapper**: `SellerApplicationCommandApiMapper`
  - `toCommand()`: ApiRequest → Command
  - displayName 미입력 시 sellerName으로 자동 설정
  - settlementCycle 미입력 시 "MONTHLY" 기본값
  - settlementDay 미입력 시 1일 기본값

#### Application Layer
- **Service**: `ApplySellerApplicationService`
- **Validator**: `SellerApplicationValidator`
  - `validateNoPendingApplication()`: 중복 신청 검증
    - 동일 사업자등록번호로 대기 중인 신청이 있으면 `DuplicateSellerApplicationException`
- **Factory**: `SellerApplicationCommandFactory`
  - `create()`: Command → Domain Aggregate 생성
- **Manager**: `SellerApplicationCommandManager.persist()`
- **Event**: `SellerApplicationCreatedEvent` 발생
  - `TransactionEventRegistry`에 등록 → 트랜잭션 커밋 후 발행

#### Domain Layer
- **Aggregate**: `SellerApplication.create()`
- **Port**: `SellerApplicationCommandPort.persist()`

#### Adapter-Out
- **Adapter**: `SellerApplicationCommandAdapter`
- **Repository**: `SellerApplicationJpaRepository.save()`
- **INSERT**: `seller_application` 테이블

---

## 4. POST /seller-applications/{id}/approve - 승인

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | /api/v1/market/admin/seller-applications/{applicationId}/approve |
| Controller | SellerApplicationCommandController |
| Method | approve |
| UseCase | ApproveSellerApplicationUseCase |
| Service | ApproveSellerApplicationService |

### 호출 흐름

```
SellerApplicationCommandController.approve(applicationId)
  |- SellerApplicationCommandApiMapper.toApproveCommand(applicationId)
  |   +-- -> ApproveSellerApplicationCommand(applicationId, processedBy=null)
  |
  |- ApproveSellerApplicationUseCase.execute(command)                  [Port]
  |   +-- ApproveSellerApplicationService.execute(command)             [Impl]
  |       |- SellerApplicationApprovalCoordinator.approve()
  |       |   |- SellerApplicationReadManager.getById(applicationId)
  |       |   |   +-- -> SellerApplication
  |       |   |
  |       |   |- SellerApplicationCommandFactory.createSellerBundle()
  |       |   |   +-- -> SellerCreationBundle
  |       |   |       (Seller, SellerBusinessInfo, SellerCs, SellerContract,
  |       |   |        SellerSettlement, SellerAuthOutbox)
  |       |   |
  |       |   |- validateBeforeApprove(bundle)
  |       |   |   |- SellerValidator.validateSellerNameNotDuplicate()
  |       |   |   +-- SellerBusinessInfoValidator.validateRegistrationNumberNotDuplicate()
  |       |   |
  |       |   +-- SellerCreationFacade.approveAndPersist()
  |       |       |- persistSeller(bundle)
  |       |       |   |- SellerCommandManager.persist(seller)
  |       |       |   |- SellerBusinessInfoCommandManager.persist(businessInfo)
  |       |       |   |- SellerCsCommandManager.persist(cs)
  |       |       |   |- SellerContractCommandManager.persist(contract)
  |       |       |   |- SellerSettlementCommandManager.persist(settlement)
  |       |       |   |- SellerAuthOutboxCommandManager.persist(authOutbox)
  |       |       |   +-- -> sellerId
  |       |       |
  |       |       |- application.approve(sellerId, processedBy, now)
  |       |       |   +-- SellerApplicationApprovedEvent
  |       |       |
  |       |       +-- SellerApplicationCommandManager.persist(application)
  |       |           +-- UPDATE seller_application
  |       |
  |       +-- application.pollEvents() → TransactionEventRegistry
  |           +-- -> sellerId
  |
  +-- -> ApiResponse<ApproveSellerApplicationApiResponse(sellerId)>
```

### Layer별 상세

#### Adapter-In
- **Path Variable**: `applicationId` (Long)
- **Response**: `ApproveSellerApplicationApiResponse(sellerId)`
- **processedBy**: 현재는 null (향후 인증 컨텍스트에서 주입 예정)

#### Application Layer
- **Service**: `ApproveSellerApplicationService`
- **Coordinator**: `SellerApplicationApprovalCoordinator`
  - 복잡한 승인 프로세스 조율
  - 검증 → Seller 생성 → 신청 상태 업데이트
- **Factory**: `SellerApplicationCommandFactory`
  - `createSellerBundle()`: Application → Seller 도메인 객체 생성
- **Facade**: `SellerCreationFacade`
  - **@Transactional**: 트랜잭션 경계
  - Seller 관련 6개 aggregate 저장
    1. Seller (셀러 기본 정보)
    2. SellerBusinessInfo (사업자 정보)
    3. SellerCs (CS 연락처)
    4. SellerContract (계약 정보)
    5. SellerSettlement (정산 정보)
    6. SellerAuthOutbox (관리자 인증 Outbox)
  - SellerApplication 상태 업데이트 (PENDING → APPROVED)
- **Validators**:
  - `SellerValidator`: 셀러명 중복 검증
  - `SellerBusinessInfoValidator`: 사업자등록번호 중복 검증
- **Event**: `SellerApplicationApprovedEvent` 발생

#### Domain Layer
- **Aggregate**: `SellerApplication.approve()`
  - status: PENDING → APPROVED
  - approvedSellerId 설정
  - processedAt, processedBy 저장
- **Port**: 다중 Port 호출
  - `SellerCommandPort.persist()`
  - `SellerBusinessInfoCommandPort.persist()`
  - `SellerCsCommandPort.persist()`
  - 등...

#### Adapter-Out
- **Database 변경**:
  1. INSERT `seller` 테이블
  2. INSERT `seller_business_info` 테이블
  3. INSERT `seller_cs_contact` 테이블
  4. INSERT `seller_contract` 테이블
  5. INSERT `seller_settlement` 테이블
  6. INSERT `seller_auth_outbox` 테이블
  7. UPDATE `seller_application` 테이블
     - status = 'APPROVED'
     - approved_seller_id = ?
     - processed_at = ?
     - processed_by = ?

---

## 5. POST /seller-applications/{id}/reject - 거절

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | POST |
| Path | /api/v1/market/admin/seller-applications/{applicationId}/reject |
| Controller | SellerApplicationCommandController |
| Method | reject |
| UseCase | RejectSellerApplicationUseCase |
| Service | RejectSellerApplicationService |

### 호출 흐름

```
SellerApplicationCommandController.reject(applicationId, RejectSellerApplicationApiRequest)
  |- SellerApplicationCommandApiMapper.toRejectCommand(applicationId, request)
  |   +-- -> RejectSellerApplicationCommand(applicationId, rejectionReason, processedBy=null)
  |
  |- RejectSellerApplicationUseCase.execute(command)                   [Port]
  |   +-- RejectSellerApplicationService.execute(command)              [Impl]
  |       |- SellerApplicationCommandFactory.createRejectContext(command)
  |       |   +-- -> StatusChangeContext<SellerApplicationId>
  |       |       (id, changedAt)
  |       |
  |       |- SellerApplicationValidator.findExistingOrThrow(id)
  |       |   +-- SellerApplicationReadManager.getById(id)
  |       |       +-- -> SellerApplication
  |       |
  |       |- application.reject(rejectionReason, processedBy, changedAt)
  |       |   +-- SellerApplicationRejectedEvent
  |       |
  |       |- SellerApplicationCommandManager.persist(application)
  |       |   +-- UPDATE seller_application
  |       |       (status = 'REJECTED', rejection_reason, processed_at, processed_by)
  |       |
  |       +-- application.pollEvents() → TransactionEventRegistry
  |
  +-- -> ResponseEntity.noContent() (HTTP 204)
```

### Layer별 상세

#### Adapter-In
- **Path Variable**: `applicationId` (Long)
- **Request DTO**: `RejectSellerApplicationApiRequest`
  - `rejectionReason`: String (필수) - 거절 사유
- **Response**: HTTP 204 No Content (빈 응답)
- **processedBy**: 현재는 null (향후 인증 컨텍스트에서 주입 예정)

#### Application Layer
- **Service**: `RejectSellerApplicationService`
- **Validator**: `SellerApplicationValidator`
  - `findExistingOrThrow()`: 신청 존재 여부 확인
    - 없으면 `SellerApplicationNotFoundException`
- **Factory**: `SellerApplicationCommandFactory`
  - `createRejectContext()`: TimeProvider를 통해 현재 시각 주입
    - Service에서 직접 TimeProvider 사용 금지 (APP-TIM-001)
- **Manager**: `SellerApplicationCommandManager.persist()`
- **Event**: `SellerApplicationRejectedEvent` 발생

#### Domain Layer
- **Aggregate**: `SellerApplication.reject()`
  - status: PENDING → REJECTED
  - rejectionReason 저장
  - processedAt, processedBy 저장
- **Port**: `SellerApplicationCommandPort.persist()`

#### Adapter-Out
- **UPDATE**: `seller_application` 테이블
  - status = 'REJECTED'
  - rejection_reason = ?
  - processed_at = ?
  - processed_by = ?

---

## 아키텍처 패턴 정리

### Hexagonal 레이어 구조

```
┌─────────────────────────────────────────────────────────────────────┐
│ Adapter-In (REST API)                                               │
│ - Controller: HTTP 요청 처리                                         │
│ - ApiMapper: ApiRequest ↔ Command/Params                            │
│ - ApiResponse: Application Result → API DTO                         │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Application Layer                                                   │
│ - UseCase (Port-In): 인터페이스 정의                                 │
│ - Service: UseCase 구현체                                            │
│ - Manager: Port-Out 호출 + 트랜잭션 관리                             │
│ - Factory: Command/Params → Domain 변환                             │
│ - Assembler: Domain → Result 변환                                   │
│ - Validator: 비즈니스 검증                                           │
│ - Coordinator/Facade: 복잡한 프로세스 조율                           │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Domain Layer                                                        │
│ - Aggregate: 비즈니스 로직 + 이벤트 발행                             │
│ - Port (Out): Adapter-Out 인터페이스                                 │
│ - Criteria: 검색 조건 VO                                             │
│ - Event: 도메인 이벤트                                               │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Adapter-Out (Persistence)                                           │
│ - Adapter: Port 구현체                                               │
│ - Repository: JpaRepository (Command), QueryDslRepository (Query)   │
│ - EntityMapper: Domain ↔ JpaEntity                                  │
│ - ConditionBuilder: QueryDSL BooleanExpression 생성                 │
└─────────────────────────────────────────────────────────────────────┘
```

### CQRS 패턴

- **Query (조회)**:
  - Controller: `SellerApplicationQueryController`
  - Service: `Search...Service`, `Get...Service`
  - Manager: `SellerApplicationReadManager`
  - Port: `SellerApplicationQueryPort`
  - Adapter: `SellerApplicationQueryAdapter`
  - Repository: `SellerApplicationQueryDslRepository`

- **Command (명령)**:
  - Controller: `SellerApplicationCommandController`
  - Service: `Apply...Service`, `Approve...Service`, `Reject...Service`
  - Manager: `SellerApplicationCommandManager`
  - Port: `SellerApplicationCommandPort`
  - Adapter: `SellerApplicationCommandAdapter`
  - Repository: `SellerApplicationJpaRepository`

### 트랜잭션 경계

- **Controller**: @Transactional 금지
- **Service**: @Transactional 금지 (위임)
- **Manager**: @Transactional 적용
- **Facade**: @Transactional 적용 (복합 저장)
- **Adapter**: @Transactional 금지

### 도메인 이벤트 처리

1. **이벤트 발생**: Aggregate 메서드 호출 시
   - `SellerApplicationCreatedEvent`
   - `SellerApplicationApprovedEvent`
   - `SellerApplicationRejectedEvent`

2. **이벤트 수집**: `application.pollEvents()`
   - Aggregate 내부 큐에서 이벤트 추출

3. **이벤트 등록**: `TransactionEventRegistry.registerForPublish()`
   - 트랜잭션 커밋 직전까지 보관

4. **이벤트 발행**: 트랜잭션 커밋 후
   - 외부 시스템 연동, 비동기 작업 트리거

---

## 주요 규칙 준수 사항

### API-CTR (Controller) 규칙
- ✅ API-CTR-001: @RestController 어노테이션 사용
- ✅ API-CTR-003: UseCase(Port-In) 인터페이스 의존
- ✅ API-CTR-004: ResponseEntity<ApiResponse<T>> 래핑
- ✅ API-CTR-005: @Transactional 금지
- ✅ API-CTR-007: 비즈니스 로직 포함 금지
- ✅ API-CTR-009: @Valid 어노테이션 필수
- ✅ API-CTR-010: CQRS Controller 분리
- ✅ API-CTR-011: List 직접 반환 금지 → PageApiResponse
- ✅ API-CTR-012: URL 소문자 + 복수형 (/seller-applications)

### API-MAP (Mapper) 규칙
- ✅ API-MAP-001: @Component로 등록
- ✅ API-MAP-002: 양방향 변환 지원
- ✅ API-MAP-003: Application Result → API Response
- ✅ API-MAP-004: Page/Slice 변환
- ✅ API-MAP-005: 순수 변환 로직만

### APP-UC (UseCase) 규칙
- ✅ APP-UC-001: Offset 페이징은 Search{Domain}ByOffsetService

### APP-TIM (TimeProvider) 규칙
- ✅ APP-TIM-001: Service에서 TimeProvider 직접 사용 금지 → Factory에서 처리

### PER-ADP (Adapter) 규칙
- ✅ PER-ADP-001: CommandAdapter는 JpaRepository만 사용
- ✅ PER-ADP-002: Adapter에서 @Transactional 금지
- ✅ PER-ADP-003: Domain 반환 (DTO 반환 금지)
- ✅ PER-ADP-004: QueryAdapter는 QueryDslRepository만 사용
- ✅ PER-ADP-005: Entity → Domain 변환 (Mapper 사용)

### PER-REP (Repository) 규칙
- ✅ PER-REP-003: 모든 조회는 QueryDslRepository

### PER-CND (Condition) 규칙
- ✅ PER-CND-001: BooleanExpression은 ConditionBuilder로 분리

---

## 엔드포인트 요약

| Method | Path | 설명 | 트랜잭션 | 이벤트 |
|--------|------|------|----------|--------|
| GET | /seller-applications | 목록 조회 | readOnly | - |
| GET | /seller-applications/{id} | 상세 조회 | readOnly | - |
| POST | /seller-applications | 입점 신청 | O | Created |
| POST | /seller-applications/{id}/approve | 승인 | O | Approved |
| POST | /seller-applications/{id}/reject | 거절 | O | Rejected |

### 승인 프로세스 특이사항

승인 시 **7개 테이블**에 데이터 저장:
1. seller
2. seller_business_info
3. seller_cs_contact
4. seller_contract
5. seller_settlement
6. seller_auth_outbox (관리자 인증용)
7. seller_application (상태 업데이트)

→ `SellerCreationFacade.approveAndPersist()`에서 단일 트랜잭션 처리
