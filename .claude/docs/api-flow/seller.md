# Seller Domain API Flow Analysis

셀러 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 📌 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/sellers/{sellerId}` | 셀러 상세 조회 | `getSeller()` |
| GET | `/sellers` | 셀러 목록 검색 | `searchSellersByOffset()` |
| POST | `/sellers` | 셀러 등록 | `registerSeller()` |
| PUT | `/sellers/{sellerId}` | 셀러 전체정보 수정 | `updateSellerFull()` |
| PATCH | `/sellers/{sellerId}` | 셀러 기본정보 수정 | `updateSeller()` |

---

## 1️⃣ GET /sellers/{sellerId} - 셀러 상세 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
SellerQueryController.getSeller(sellerId)
  ├─ GetSellerForAdminUseCase.execute(sellerId)              [Port Interface]
  └─ SellerQueryApiMapper.toDetailResponse(result)           [Response 변환]

[Application]
GetSellerForAdminService.execute(sellerId)                   [UseCase 구현]
  ├─ SellerCompositionReadManager.getAdminComposite(sellerId)
  │   └─ SellerCompositionQueryPort.findAdminCompositeById() [Port]
  ├─ SellerCompositionReadManager.getPolicyComposite(sellerId)
  │   └─ SellerCompositionQueryPort.findPolicyCompositeById() [Port]
  └─ SellerCompositeAssembler.assemble()                     [Result 조합]

[Adapter-Out]
SellerCompositionQueryAdapter                                [Port 구현]
  ├─ SellerCompositeQueryDslRepository.findAdminCompositeById()
  │   └─ QueryDSL: Seller + BusinessInfo + CS + Contract + Settlement JOIN
  └─ SellerPolicyCompositeQueryDslRepository.findBySellerId()
      └─ QueryDSL: ShippingPolicy + RefundPolicy JOIN

[Database]
- seller (기본정보)
- seller_business_info (사업자정보)
- seller_cs (CS 정보)
- seller_contract (계약정보)
- seller_settlement (정산정보)
- shipping_policy (배송정책)
- refund_policy (환불정책)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `SellerQueryController`
  - Method: `getSeller(Long sellerId)`
  - Response: `ApiResponse<SellerDetailApiResponse>`
  - HTTP Status: 200 OK

- **Request DTO**: 없음 (Path Variable만 사용)

- **Response DTO**: `SellerDetailApiResponse`
  ```java
  record SellerDetailApiResponse(
    SellerInfo sellerInfo,           // 기본정보
    BusinessInfo businessInfo,       // 사업자정보
    CsInfo csInfo,                   // CS 정보
    ContractInfo contractInfo,       // 계약정보
    SettlementInfo settlementInfo    // 정산정보
  )
  ```

- **ApiMapper**: `SellerQueryApiMapper`
  - `toDetailResponse(SellerFullCompositeResult)` → `SellerDetailApiResponse`
  - 날짜 필드는 ISO8601 문자열로 변환

#### Application Layer
- **UseCase Interface**: `GetSellerForAdminUseCase`
  - `execute(Long sellerId)` → `SellerFullCompositeResult`

- **Service 구현**: `GetSellerForAdminService`
  - Manager 2개 호출 (Admin Composite + Policy Composite)
  - Assembler로 최종 조합

- **Manager**: `SellerCompositionReadManager`
  - `getAdminComposite(Long)`: Seller + BusinessInfo + CS + Contract + Settlement
  - `getPolicyComposite(Long)`: ShippingPolicy + RefundPolicy
  - **@Transactional(readOnly = true)**

- **Result DTO**: `SellerFullCompositeResult`
  - `SellerAdminCompositeResult` + `SellerPolicyCompositeResult` 조합

#### Domain Layer
- **Port**: `SellerCompositionQueryPort`
  - `findAdminCompositeById(Long)` → `Optional<SellerAdminCompositeResult>`
  - `findPolicyCompositeById(Long)` → `Optional<SellerPolicyCompositeResult>`

#### Adapter-Out Layer
- **Adapter**: `SellerCompositionQueryAdapter`
  - Port 인터페이스 구현
  - **@Component** 등록

- **Repository**: `SellerCompositeQueryDslRepository`, `SellerPolicyCompositeQueryDslRepository`
  - QueryDSL 기반 JOIN 쿼리
  - 성능 최적화를 위한 크로스 도메인 조인

- **Database Query**:
  ```sql
  -- Admin Composite
  SELECT s.*, sbi.*, scs.*, sc.*, ss.*
  FROM seller s
  LEFT JOIN seller_business_info sbi ON s.id = sbi.seller_id
  LEFT JOIN seller_cs scs ON s.id = scs.seller_id
  LEFT JOIN seller_contract sc ON s.id = sc.seller_id
  LEFT JOIN seller_settlement ss ON s.id = ss.seller_id
  WHERE s.id = ? AND s.deleted_at IS NULL

  -- Policy Composite
  SELECT sp.*, rp.*
  FROM shipping_policy sp
  LEFT JOIN refund_policy rp ON sp.seller_id = rp.seller_id
  WHERE sp.seller_id = ? AND sp.deleted_at IS NULL
  ```

---

## 2️⃣ GET /sellers - 셀러 목록 검색

### 호출 흐름 다이어그램

```
[Adapter-In]
SellerQueryController.searchSellersByOffset(request)
  ├─ SellerQueryApiMapper.toSearchParams(request)            [Params 변환]
  ├─ SearchSellerByOffsetUseCase.execute(params)             [Port Interface]
  └─ SellerQueryApiMapper.toPageResponse(pageResult)         [Response 변환]

[Application]
SearchSellerByOffsetService.execute(params)                  [UseCase 구현]
  ├─ SellerQueryFactory.createCriteria(params)               [Criteria 생성]
  ├─ SellerReadManager.findByCriteria(criteria)              [조회]
  │   └─ SellerQueryPort.findByCriteria()                    [Port]
  ├─ SellerReadManager.countByCriteria(criteria)             [카운트]
  │   └─ SellerQueryPort.countByCriteria()                   [Port]
  └─ SellerAssembler.toPageResult()                          [Result 조합]

[Adapter-Out]
SellerQueryAdapter                                            [Port 구현]
  └─ SellerQueryDslRepository
      ├─ findByCriteria(): WHERE + ORDER BY + LIMIT/OFFSET
      └─ countByCriteria(): COUNT(*)

[Database]
- seller (검색 대상)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `SellerQueryController`
  - Method: `searchSellersByOffset(SearchSellersApiRequest)`
  - Response: `ApiResponse<PageApiResponse<SellerApiResponse>>`
  - HTTP Status: 200 OK

- **Request DTO**: `SearchSellersApiRequest`
  ```java
  record SearchSellersApiRequest(
    Boolean active,           // 활성 상태 필터
    String searchField,       // 검색 필드 (sellerName, displayName)
    String searchWord,        // 검색어
    Integer page,             // 페이지 번호 (default: 0)
    Integer size,             // 페이지 크기 (default: 20)
    String sortKey,           // 정렬 키 (default: createdAt)
    String sortDirection      // 정렬 방향 (default: DESC)
  )
  ```

- **Response DTO**: `SellerApiResponse`
  ```java
  record SellerApiResponse(
    Long id,
    String sellerName,
    String displayName,
    String logoUrl,
    String description,
    Boolean active,
    String createdAt,         // ISO8601 문자열
    String updatedAt          // ISO8601 문자열
  )
  ```

- **ApiMapper**: `SellerQueryApiMapper`
  - `toSearchParams()`: 기본값 처리 (page=0, size=20, sortKey=createdAt, sortDirection=DESC)
  - `toPageResponse()`: List + 페이징 메타데이터 조합

#### Application Layer
- **UseCase Interface**: `SearchSellerByOffsetUseCase`
  - `execute(SellerSearchParams)` → `SellerPageResult`

- **Service 구현**: `SearchSellerByOffsetService`
  - QueryFactory로 Params → Criteria 변환
  - Manager 통해 조회 + 카운트
  - Assembler로 PageResult 생성

- **Params DTO**: `SellerSearchParams`
  - Application 레벨 검색 파라미터

- **Result DTO**: `SellerPageResult`
  ```java
  record SellerPageResult(
    List<SellerResult> content,
    int page,
    int size,
    long totalCount
  )
  ```

#### Domain Layer
- **Port**: `SellerQueryPort`
  - `findByCriteria(SellerSearchCriteria)` → `List<Seller>`
  - `countByCriteria(SellerSearchCriteria)` → `long`

- **Criteria**: `SellerSearchCriteria`
  - Domain 레벨 검색 조건 (불변 객체)

#### Adapter-Out Layer
- **Adapter**: `SellerQueryAdapter`
  - QueryDslRepository 호출
  - Entity → Domain 변환

- **Repository**: `SellerQueryDslRepository`
  - QueryDSL 기반 동적 쿼리
  - ConditionBuilder 사용

- **Database Query**:
  ```sql
  -- findByCriteria
  SELECT *
  FROM seller
  WHERE deleted_at IS NULL
    AND active = ?             -- 옵션: active 필터
    AND seller_name LIKE ?     -- 옵션: 검색어 (searchField 기준)
  ORDER BY created_at DESC     -- sortKey, sortDirection
  LIMIT ? OFFSET ?             -- 페이징

  -- countByCriteria
  SELECT COUNT(*)
  FROM seller
  WHERE deleted_at IS NULL
    AND active = ?
    AND seller_name LIKE ?
  ```

---

## 3️⃣ POST /sellers - 셀러 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
SellerCommandController.registerSeller(request)
  ├─ SellerCommandApiMapper.toCommand(request)               [Command 변환]
  └─ RegisterSellerUseCase.execute(command)                  [Port Interface]

[Application]
RegisterSellerService.execute(command)                       [UseCase 구현]
  ├─ SellerCommandFactory.createRegistrationBundle(command)  [Bundle 생성]
  └─ SellerRegistrationCoordinator.register(bundle)          [등록 조율]
      ├─ SellerValidator.validateSellerNameNotDuplicate()
      ├─ SellerBusinessInfoValidator.validateRegistrationNumberNotDuplicate()
      └─ SellerCommandFacade.registerSeller(bundle)          [트랜잭션]
          ├─ SellerCommandManager.persist(seller)
          │   └─ SellerCommandPort.persist()                 [Port]
          └─ SellerBusinessInfoCommandManager.persist(businessInfo)
              └─ SellerBusinessInfoCommandPort.persist()     [Port]

[Adapter-Out]
SellerCommandAdapter                                          [Port 구현]
  ├─ SellerJpaEntityMapper.toEntity(seller)
  └─ SellerJpaRepository.save(entity)

[Database]
- INSERT INTO seller
- INSERT INTO seller_business_info
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `SellerCommandController`
  - Method: `registerSeller(RegisterSellerApiRequest)`
  - Response: `ApiResponse<SellerIdApiResponse>`
  - HTTP Status: 201 Created

- **Request DTO**: `RegisterSellerApiRequest`
  ```java
  record RegisterSellerApiRequest(
    SellerInfoRequest seller,
    BusinessInfoRequest businessInfo
  )

  record SellerInfoRequest(
    String sellerName,
    String displayName,
    String logoUrl,
    String description
  )

  record BusinessInfoRequest(
    String registrationNumber,
    String companyName,
    String representative,
    String saleReportNumber,
    AddressRequest businessAddress,
    CsContactRequest csContact
  )
  ```

- **Response DTO**: `SellerIdApiResponse`
  ```java
  record SellerIdApiResponse(Long sellerId)
  ```

- **ApiMapper**: `SellerCommandApiMapper`
  - `toCommand(RegisterSellerApiRequest)` → `RegisterSellerCommand`
  - 중첩 DTO 구조 그대로 Command로 변환

#### Application Layer
- **UseCase Interface**: `RegisterSellerUseCase`
  - `execute(RegisterSellerCommand)` → `Long` (sellerId)

- **Service 구현**: `RegisterSellerService`
  - Factory로 Bundle 생성
  - Coordinator에게 위임

- **Command DTO**: `RegisterSellerCommand`
  - `SellerInfoCommand` + `SellerBusinessInfoCommand`

- **Coordinator**: `SellerRegistrationCoordinator`
  1. 검증 (중복 체크)
  2. Facade 호출 (트랜잭션)

- **Facade**: `SellerCommandFacade`
  - **@Transactional**: Seller + BusinessInfo 원자적 저장
  - Manager를 통한 영속화

- **Validator**:
  - `SellerValidator.validateSellerNameNotDuplicate()`
  - `SellerBusinessInfoValidator.validateRegistrationNumberNotDuplicate()`

#### Domain Layer
- **Port**:
  - `SellerCommandPort.persist(Seller)` → `Long`
  - `SellerBusinessInfoCommandPort.persist(SellerBusinessInfo)` → `Long`

- **Aggregate**:
  - `Seller`: 셀러 루트 Aggregate
  - `SellerBusinessInfo`: 사업자정보 (1:1 관계)

#### Adapter-Out Layer
- **Adapter**: `SellerCommandAdapter`, `SellerBusinessInfoCommandAdapter`
  - Domain → Entity 변환
  - JpaRepository.save() 호출

- **Repository**: `SellerJpaRepository` (Spring Data JPA)
  - 단순 save/saveAll만 사용

- **Database Query**:
  ```sql
  INSERT INTO seller (
    seller_name, display_name, logo_url, description,
    active, created_at, updated_at
  ) VALUES (?, ?, ?, ?, true, NOW(), NOW())

  INSERT INTO seller_business_info (
    seller_id, registration_number, company_name, representative,
    sale_report_number, business_zipcode, business_address,
    business_address_detail, created_at, updated_at
  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
  ```

---

## 4️⃣ PUT /sellers/{sellerId} - 셀러 전체정보 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
SellerCommandController.updateSellerFull(sellerId, request)
  ├─ SellerCommandApiMapper.toCommand(sellerId, request)     [Command 변환]
  └─ UpdateSellerFullUseCase.execute(command)                [Port Interface]

[Application]
UpdateSellerFullService.execute(command)                     [UseCase 구현]
  ├─ SellerCommandFactory.createUpdateBundle(command)        [Bundle 생성]
  └─ SellerUpdateCoordinator.update(bundle)                  [수정 조율]
      ├─ SellerValidator.findExistingOrThrow()               [존재 확인]
      ├─ SellerBusinessInfoValidator.findExistingOrThrow()
      ├─ SellerCsValidator.findExistingOrThrow()
      ├─ SellerContractValidator.findExistingOrThrow()
      ├─ SellerSettlementValidator.findExistingOrThrow()
      ├─ 중복 검사 (자기 자신 제외)
      └─ SellerCommandFacade.updateSeller(bundle)            [트랜잭션]
          ├─ seller.update()                                 [Domain 로직]
          ├─ businessInfo.update()
          ├─ sellerCs.update()
          ├─ sellerContract.update()
          ├─ sellerSettlement.update()
          ├─ SellerCommandManager.persist(seller)
          ├─ SellerBusinessInfoCommandManager.persist()
          ├─ SellerCsCommandManager.persist()
          ├─ SellerContractCommandManager.persist()
          └─ SellerSettlementCommandManager.persist()

[Adapter-Out]
(각 Aggregate별 Adapter + JpaRepository)
  - SellerCommandAdapter → SellerJpaRepository
  - SellerBusinessInfoCommandAdapter → SellerBusinessInfoJpaRepository
  - SellerCsCommandAdapter → SellerCsJpaRepository
  - SellerContractCommandAdapter → SellerContractJpaRepository
  - SellerSettlementCommandAdapter → SellerSettlementJpaRepository

[Database]
- UPDATE seller
- UPDATE seller_business_info
- UPDATE seller_cs
- UPDATE seller_contract
- UPDATE seller_settlement
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `SellerCommandController`
  - Method: `updateSellerFull(Long sellerId, UpdateSellerFullApiRequest)`
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `UpdateSellerFullApiRequest`
  ```java
  record UpdateSellerFullApiRequest(
    SellerInfoRequest seller,
    BusinessInfoRequest businessInfo,
    CsInfoRequest csInfo,
    ContractInfoRequest contractInfo,
    SettlementInfoRequest settlementInfo
  )
  ```

- **ApiMapper**: `SellerCommandApiMapper`
  - `toCommand(Long, UpdateSellerFullApiRequest)` → `UpdateSellerFullCommand`

#### Application Layer
- **UseCase Interface**: `UpdateSellerFullUseCase`
  - `execute(UpdateSellerFullCommand)` → `void`

- **Service 구현**: `UpdateSellerFullService`
  - Factory로 Bundle 생성
  - Coordinator에게 위임

- **Command DTO**: `UpdateSellerFullCommand`
  - sellerId + 5개 UpdateData

- **Coordinator**: `SellerUpdateCoordinator`
  1. 존재 여부 검증 (5개 Aggregate)
  2. Bundle에 Domain 객체 설정
  3. 중복 검사 (자기 자신 제외)
  4. Facade 호출

- **Facade**: `SellerCommandFacade`
  - **@Transactional**: 5개 Aggregate 원자적 수정
  - 순서: Seller → BusinessInfo → CS → Contract → Settlement

- **Bundle**: `SellerUpdateBundle`
  - 검증된 Domain 객체 보관
  - UpdateData 보관

#### Domain Layer
- **Port**:
  - `SellerCommandPort.persist(Seller)`
  - `SellerBusinessInfoCommandPort.persist()`
  - `SellerCsCommandPort.persist()`
  - `SellerContractCommandPort.persist()`
  - `SellerSettlementCommandPort.persist()`

- **Aggregate**:
  - 각 Aggregate의 `update()` 메서드 호출
  - 불변성 유지 (새로운 VO 생성)
  - 도메인 규칙 검증

#### Adapter-Out Layer
- **Adapter**: 5개 CommandAdapter
  - Domain → Entity 변환
  - JpaRepository.save() 호출

- **Database Query**:
  ```sql
  UPDATE seller SET
    seller_name = ?, display_name = ?, logo_url = ?,
    description = ?, updated_at = NOW()
  WHERE id = ?

  UPDATE seller_business_info SET
    registration_number = ?, company_name = ?, representative = ?,
    sale_report_number = ?, business_zipcode = ?, business_address = ?,
    business_address_detail = ?, updated_at = NOW()
  WHERE seller_id = ?

  UPDATE seller_cs SET
    cs_phone = ?, cs_mobile = ?, cs_email = ?,
    operating_start_time = ?, operating_end_time = ?, operating_days = ?,
    kakao_channel_url = ?, updated_at = NOW()
  WHERE seller_id = ?

  UPDATE seller_contract SET
    commission_rate = ?, contract_start_date = ?, contract_end_date = ?,
    status = ?, special_terms = ?, updated_at = NOW()
  WHERE seller_id = ?

  UPDATE seller_settlement SET
    bank_code = ?, bank_name = ?, account_number = ?, account_holder_name = ?,
    settlement_cycle = ?, settlement_day = ?, updated_at = NOW()
  WHERE seller_id = ?
  ```

---

## 5️⃣ PATCH /sellers/{sellerId} - 셀러 기본정보 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
SellerCommandController.updateSeller(sellerId, request)
  ├─ SellerCommandApiMapper.toCommand(sellerId, request)     [Command 변환]
  └─ UpdateSellerUseCase.execute(command)                    [Port Interface]

[Application]
UpdateSellerService.execute(command)                         [UseCase 구현]
  ├─ SellerCommandFactory.createUpdateContext(command)       [Context 생성]
  ├─ SellerValidator.findExistingOrThrow()                   [존재 확인]
  ├─ seller.update(updateData)                               [Domain 로직]
  ├─ SellerCommandManager.persist(seller)
  ├─ updateCsIfPresent()                                     [옵션: CS 수정]
  │   ├─ SellerCsReadManager.getBySellerId()
  │   ├─ sellerCs.update()
  │   └─ SellerCsCommandManager.persist()
  └─ updateBusinessInfoIfPresent()                           [옵션: BusinessInfo 수정]
      ├─ SellerBusinessInfoReadManager.getBySellerId()
      ├─ businessInfo.update()
      └─ SellerBusinessInfoCommandManager.persist()

[Adapter-Out]
(각 Aggregate별 Adapter + Repository)
  - SellerCommandAdapter
  - SellerCsCommandAdapter (옵션)
  - SellerBusinessInfoCommandAdapter (옵션)

[Database]
- UPDATE seller (필수)
- UPDATE seller_cs (옵션)
- UPDATE seller_business_info (옵션)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `SellerCommandController`
  - Method: `updateSeller(Long sellerId, UpdateSellerApiRequest)`
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `UpdateSellerApiRequest`
  ```java
  record UpdateSellerApiRequest(
    String sellerName,        // 필수
    String displayName,       // 필수
    String logoUrl,           // 옵션
    String description,       // 옵션
    CsInfoRequest csInfo,     // 옵션
    BusinessInfoRequest businessInfo  // 옵션
  )
  ```

#### Application Layer
- **UseCase Interface**: `UpdateSellerUseCase`
  - `execute(UpdateSellerCommand)` → `void`

- **Service 구현**: `UpdateSellerService`
  - Seller는 필수 수정
  - CS, BusinessInfo는 옵션 (null이 아니면 수정)

- **Command DTO**: `UpdateSellerCommand`
  - sellerId + 기본정보 + 옵션(csInfo, businessInfo)

- **UpdateContext**: `UpdateContext<SellerId, SellerUpdateData>`
  - id + updateData + changedAt (시점 일관성)

#### Domain Layer
- **Port**:
  - `SellerCommandPort.persist(Seller)`
  - `SellerCsCommandPort.persist()` (옵션)
  - `SellerBusinessInfoCommandPort.persist()` (옵션)

- **Aggregate**:
  - `Seller.update(SellerUpdateData, Instant)`
  - `SellerCs.update(...)` (옵션)
  - `SellerBusinessInfo.update(...)` (옵션)

#### Adapter-Out Layer
- **Database Query**:
  ```sql
  -- 필수: Seller 수정
  UPDATE seller SET
    seller_name = ?, display_name = ?, logo_url = ?,
    description = ?, updated_at = ?
  WHERE id = ?

  -- 옵션: CS 수정
  UPDATE seller_cs SET
    cs_phone = ?, cs_email = ?, cs_mobile = ?, updated_at = ?
  WHERE seller_id = ?

  -- 옵션: BusinessInfo 수정
  UPDATE seller_business_info SET
    registration_number = ?, company_name = ?, representative = ?,
    sale_report_number = ?, business_zipcode = ?, business_address = ?,
    business_address_detail = ?, updated_at = ?
  WHERE seller_id = ?
  ```

---

## 📊 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest/ApiResponse | HTTP 계층 관심사 (Validation, 직렬화) |
| **Application** | Command/Params/Result | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | Aggregate, VO, Criteria | 비즈니스 규칙, 불변성 |
| **Adapter-Out** | Entity | 영속화 기술 관심사 (JPA) |

### 2. CQRS 분리

- **Query**: `SellerQueryController` → `SellerQueryApiMapper` → QueryUseCase → QueryService
- **Command**: `SellerCommandController` → `SellerCommandApiMapper` → CommandUseCase → CommandService

### 3. 트랜잭션 경계

| 계층 | @Transactional 위치 |
|------|---------------------|
| Adapter-In | ❌ 금지 |
| Application Service | ❌ 금지 (Facade/Manager에 위임) |
| **Facade** | ✅ 트랜잭션 경계 |
| **Manager** | ✅ readOnly=true (조회) |
| Adapter-Out | ❌ 금지 |

### 4. Coordinator 패턴

복잡한 등록/수정은 Coordinator가 조율:
1. **검증 단계** (Validator)
2. **번들 생성** (Factory)
3. **트랜잭션 실행** (Facade)

### 5. Manager 패턴

- **ReadManager**: Port 호출 + 예외 처리 (@Transactional readOnly)
- **CommandManager**: Port 호출 (Facade가 트랜잭션 관리)

### 6. Composite 조회 최적화

단일 조회 시 N+1 방지를 위해 **SellerCompositionQueryPort** 사용:
- JOIN 기반 한번에 조회
- QueryDSL로 동적 쿼리
- DTO 프로젝션으로 성능 최적화

### 7. 변환 체인

```
[Query]
ApiRequest → Params → Criteria → Query → Entity → Domain → Result → ApiResponse

[Command]
ApiRequest → Command → UpdateData → Domain.update() → Entity → DB
```

---

## 🔍 주요 설계 결정

### ✅ 장점

1. **레이어 격리**: 각 레이어가 독립적 DTO 사용 (변경 격리)
2. **테스트 용이성**: Port 인터페이스로 Mocking 가능
3. **읽기 최적화**: Composite 패턴으로 JOIN 쿼리 최적화
4. **트랜잭션 명확성**: Facade에 집중 → 디버깅 용이
5. **검증 분리**: Validator/Coordinator로 비즈니스 규칙 명확화

### ⚠️ 트레이드오프

1. **DTO 변환 오버헤드**: 4단계 변환 (성능보다 유지보수성 우선)
2. **코드량 증가**: Port/Adapter/Mapper 모두 필요
3. **학습 곡선**: Hexagonal 아키텍처 이해 필요

---

## 📌 다음 단계

1. **E2E 테스트 작성**: `/test-scenario seller` 명령
2. **성능 테스트**: Composite 조회 vs N+1 비교
3. **API 문서 자동화**: Spring REST Docs 적용 확인
