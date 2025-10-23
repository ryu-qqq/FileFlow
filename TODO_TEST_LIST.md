# 📋 FileFlow 테스트 작성 TODO 리스트

> **분석 완료일**: 2025-10-23
> **최종 업데이트**: 2025-10-23 18:45
> **현재 커버리지**: 61% (51/83)
> **목표 커버리지**: 81% (67/83)
> **작성 필요 테스트**: 16개

---

## 🔄 체크포인트 시스템

### 현재 세션 정보
- **세션 시작**: 2025-10-23
- **마지막 작업**: Option A 완료 - JpaEntity Fixtures 2개 작성 ✅
- **다음 작업**: 우선순위 2 - REST Controller 엔드포인트 테스트 8개 또는 Domain Enum/Exception 테스트 4개
- **컨텍스트 사용량**: ~107K/200K tokens (54%)

### 세션 재개 시 참고사항
```bash
# 1. 이 문서를 먼저 읽고 ✅ 체크된 항목 확인
# 2. "진행 중" 섹션에서 마지막 작업 확인
# 3. /test 커맨드 사용하여 테스트 작성 (컨벤션 자동 주입)
# 4. 작업 완료 시 ✅ 체크 + 완료일 기록
# 5. 컨텍스트 75% 도달 시 체크포인트 업데이트
```

---

## 📊 전체 진행 현황

### 모듈별 진행률

| 모듈 | 현재 | 목표 | 진행률 | 우선순위 |
|------|------|------|--------|----------|
| **Domain** | 6/8 | 8/8 | 75% | 🟢 Low |
| **Application** | 23/45 | 36/45 | 51% | 🟡 Medium |
| **Persistence** | 8/15 | 11/15 | 53% | 🟡 Medium |
| **REST API** | 14/15 | 12/15 | 93% | 🟢 완료 임박 |
| **전체** | **51/83** | **67/83** | **61%** | - |

---

## 🔴 우선순위 1 (High) - 즉시 작성 필요

### ✅ 완료된 작업
- [x] 테스트 커버리지 분석 완료 (2025-10-23)
- [x] test-fixtures 평가 완료 (2025-10-23)
- [x] 태그 전략 수립 완료 (2025-10-23)
- [x] TODO_TEST_LIST.md 문서 작성 (2025-10-23)
- [x] UpdateTenantUseCaseTest 작성 및 실행 성공 (2025-10-23)
- [x] GetTenantUseCaseTest 작성 및 실행 성공 (2025-10-23)
- [x] GetTenantsUseCaseTest 작성 및 실행 성공 (2025-10-23)
- [x] UpdateTenantStatusUseCaseTest 작성 및 실행 성공 (2025-10-23) - 21개 테스트
- [x] GetTenantTreeUseCaseTest 작성 및 실행 성공 (2025-10-23) - 14개 테스트
- [x] Organization UseCase 테스트 5개 모두 작성 및 실행 성공 (2025-10-23) - 36개 테스트 ✅

### 🚧 진행 중
- 없음 (다음: Service 계층 테스트)

### 📝 대기 중

#### 1. Application Layer - UseCase 테스트 (14개)

**Tenant UseCase (5개)**:
- [x] `UpdateTenantUseCaseTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 참고: `CreateTenantUseCaseTest.java`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: Tenant 이름 수정 성공
    - ✅ 예외: null Command
    - ✅ 예외: Tenant 미존재
    - ✅ 예외: 삭제된 Tenant 수정 시도
    - ✅ Command 검증 (4개 테스트)
    - ✅ Domain 로직 검증 (2개 테스트)
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `GetTenantUseCaseTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: ID로 Tenant 조회 성공 (4개 테스트)
    - ✅ 예외: null Query
    - ✅ 예외: Tenant 미존재
    - ✅ 예외: 삭제된 Tenant 조회 (soft delete)
    - ✅ Query 검증 (4개 테스트)
    - ✅ Read-Only Transaction 검증
  - **총 12개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `GetTenantsUseCaseTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ Offset-based Pagination: 첫 페이지, 마지막 페이지, 빈 결과, 필터링 (7개 테스트)
    - ✅ Cursor-based Pagination: 첫 조회, 마지막 슬라이스, 빈 결과, nextCursor 인코딩 (9개 테스트)
    - ✅ Query 검증: page/cursor 동시 사용 금지, size 기본값/범위, pagination 타입 판별 (7개 테스트)
    - ✅ Read-Only Transaction 검증 (2개 테스트)
  - **총 25개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `UpdateTenantStatusUseCaseTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: ACTIVE → SUSPENDED, SUSPENDED → ACTIVE (5개 테스트)
    - ✅ 예외: null Command, Tenant 미존재, 잘못된 상태값, 삭제된 Tenant, 동일 상태 전환 (7개 테스트)
    - ✅ Command 검증: tenantId/status null/blank 검증 (5개 테스트)
    - ✅ Domain 로직 검증: suspend/activate 메서드 호출, deleted 플래그 불변 (3개 테스트)
    - ✅ Transaction 검증 (1개 테스트)
  - **총 21개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `GetTenantTreeUseCaseTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: Tenant + Organization 트리 조회, Organization 없는 Tenant, includeDeleted 처리, 삭제된 Tenant 조회, ArgumentCaptor 검증 (6개 테스트)
    - ✅ 예외: null Query, Tenant 미존재 (2개 테스트)
    - ✅ Query 검증: tenantId null/blank/whitespace, 팩토리 메서드 (5개 테스트)
    - ✅ Read-Only Transaction 검증 (1개 테스트)
  - **총 14개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

**Organization UseCase (5개)**: ✅ **모두 완료** (2025-10-23)
- [x] `UpdateOrganizationUseCaseTest.java` ✅ 9개 테스트 PASSED
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/organization/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: Organization 이름 수정 성공
    - ✅ 예외: null Command, Organization 미존재, 삭제된 Organization 수정
    - ✅ Command 검증: organizationId null/0, name null/blank
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `GetOrganizationUseCaseTest.java` ✅ 6개 테스트 PASSED
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: ID로 Organization 조회 성공
    - ✅ 예외: null Query, Organization 미존재
    - ✅ Query 검증: organizationId null/0
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `GetOrganizationsUseCaseTest.java` ✅ 7개 테스트 PASSED
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: Tenant별 Organization 목록 조회 (Offset-based), 빈 리스트
    - ✅ 예외: null Query
    - ✅ Query 검증: tenantId blank, size 범위, page/cursor 동시 사용
  - **특이사항**: executeWithPage() 사용 (Dual Pagination 지원)
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `DeleteOrganizationUseCaseTest.java` ✅ 7개 테스트 PASSED
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: Organization Soft Delete 성공
    - ✅ 예외: null Command, Organization 미존재, 이미 삭제된 Organization
    - ✅ Command 검증: organizationId null/0
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `UpdateOrganizationStatusUseCaseTest.java` ✅ 7개 테스트 PASSED
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ 정상: ACTIVE → INACTIVE 상태 전환
    - ✅ 예외: null Command, Organization 미존재, INACTIVE → ACTIVE 복원 시도, 삭제된 Organization
    - ✅ Command 검증: organizationId null, status null/blank
  - **특이사항**: INACTIVE → ACTIVE 복원 금지 (비즈니스 규칙)
  - **실행 결과**: BUILD SUCCESSFUL ✅

**Service 계층 (4개 - 핵심만)**:
- [x] `TenantCommandServiceTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ CreateTenantTests: 생성 성공, null Command, 중복 이름, Repository save 호출 (4개 테스트)
    - ✅ UpdateTenantTests: 이름 수정 성공, null Command, Tenant 미존재, Domain updateName() 호출 (4개 테스트)
    - ✅ UpdateTenantStatusTests: ACTIVE↔SUSPENDED 전환, null Command, Tenant 미존재, 잘못된 상태값, Domain activate/suspend 호출 (7개 테스트)
  - **총 15개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `TenantQueryServiceTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ GetTenantTests: Tenant 조회 성공, null Query, Tenant 미존재 (3개 테스트)
    - ✅ GetTenantsWithPageTests: Offset-based Pagination, 빈 결과, 필터링, 페이지 계산, 예외 (7개 테스트)
    - ✅ GetTenantsWithSliceTests: Cursor-based Pagination, 마지막 슬라이스, cursor 사용, Base64 인코딩 (6개 테스트)
    - ✅ RepositoryCallVerificationTests: findById, Offset/Cursor Pagination 호출 검증 (3개 테스트)
  - **총 19개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅
  - **특이사항**: Dual Pagination (Offset-based + Cursor-based) 모두 테스트

- [x] `OrganizationCommandServiceTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/organization/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ CreateOrganizationTests: 생성 성공, null Command, 중복 orgCode, Repository 호출 검증 (4개)
    - ✅ UpdateOrganizationTests: 이름 수정, null Command, 미존재, Domain 호출 검증 (4개)
    - ✅ SoftDeleteOrganizationTests: 삭제 성공, null Command, 미존재, Domain 호출 검증 (4개)
    - ✅ UpdateOrganizationStatusTests: ACTIVE→INACTIVE, INACTIVE→ACTIVE 복원 거부, 예외 처리, Domain 호출 검증 (7개)
  - **총 19개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅
  - **특이사항**: Organization은 INACTIVE→ACTIVE 복원 불가 (Tenant와 다름)

- [x] `OrganizationQueryServiceTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/organization/service/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ GetOrganizationTests: 단건 조회 성공, null Query, Organization 미존재 (3개)
    - ✅ GetOrganizationsWithPageTests: Offset Pagination, 빈 결과, orgCodeContains/nameContains/deleted 필터링, 페이지 계산, null 예외 (8개)
    - ✅ GetOrganizationsWithSliceTests: Cursor Pagination, 마지막 슬라이스, 빈 결과, cursor 사용, nextCursor Base64 인코딩, null 예외 (6개)
    - ✅ RepositoryCallVerificationTests: findById, findAllWithOffset+countAll, findAllWithCursor 호출 검증 (3개)
  - **총 20개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅
  - **특이사항**: Tenant와 동일한 Dual Pagination 전략 (Offset + Cursor) 구현

#### 2. Adapter-out/Persistence - Query Repository 테스트 (2개)

- [x] `TenantQueryRepositoryAdapterTest.java` ✅ **완료** (2025-10-23 13:11)
  - 위치: `adapter-out/persistence-mysql/src/test/java/.../tenant/adapter/`
  - 참고: `TenantPersistenceAdapterTest.java`
  - 태그: `@Tag("integration")` `@Tag("adapter")` `@Tag("slow")`
  - 테스트 시나리오:
    - ✅ FindByIdTests: 단건 조회 성공, null TenantId 예외, 미존재, 삭제된 Tenant (4개)
    - ✅ FindAllWithOffsetTests: 기본 조회, nameContains 필터, deleted 필터, offset/limit, 빈 결과, 정렬, N+1 방지 (7개)
    - ✅ CountAllTests: 전체 개수, nameContains 필터, deleted 필터, 빈 결과 (4개)
    - ✅ FindAllWithCursorTests: 기본 조회, Cursor 다음 페이지, 잘못된 Cursor, ID 정렬, nameContains + Cursor, 빈 결과 (6개)
    - ✅ PerformanceTests: N+1 문제 없음 (Offset), N+1 문제 없음 (Cursor) (2개)
  - **총 23개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL (100%) ✅
  - **소요 시간**: 3.58초 (TestContainers MySQL 8.0)
  - **특이사항**:
    - UUID ID는 생성 순서와 무관하므로 정렬 후 테스트
    - Cursor는 Base64 인코딩된 Tenant ID (String)
    - Thread.sleep(10) 사용 시 InterruptedException 처리 필수

- [x] `OrganizationQueryRepositoryAdapterTest.java` ✅ **완료** (2025-10-23 14:10)
  - 위치: `adapter-out/persistence-mysql/src/test/java/.../organization/adapter/`
  - 참고: `OrganizationPersistenceAdapterTest.java`
  - 태그: `@Tag("integration")` `@Tag("adapter")` `@Tag("slow")`
  - 테스트 시나리오:
    - ✅ FindByIdTests: 단건 조회 성공, null 예외, 미존재, 삭제된 Organization (4개)
    - ✅ FindAllWithOffsetTests: 기본 조회, tenantId 필터, orgCodeContains 필터, nameContains 필터, deleted 필터, offset/limit, 빈 결과, 정렬, 복합 필터 (9개)
    - ✅ CountAllTests: 전체 개수, tenantId 필터, orgCodeContains 필터, deleted 필터, 빈 결과, 복합 필터 (6개)
    - ✅ FindAllWithCursorTests: 기본 조회, Cursor 다음 페이지, 잘못된 Cursor, ID 정렬, tenantId + Cursor, orgCodeContains + Cursor, 빈 결과 (7개)
    - ✅ PerformanceTests: N+1 문제 없음 (Offset), N+1 문제 없음 (Cursor) (2개)
  - **총 28개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL (100%) ✅
  - **소요 시간**: 4.04초 (TestContainers MySQL 8.0)
  - **특이사항**:
    - Organization ID는 Long (Auto-increment) - UUID와 달리 생성 순서대로 증가
    - Cursor는 Base64 인코딩된 Long ID (Tenant는 UUID String)
    - tenantId, orgCodeContains 등 Organization 특화 필터링 검증

#### 3. 기존 테스트 태그 추가 ✅ **완료** (2025-10-23 15:45)

**Phase 3 완료 - 16개 파일에 @Tag 어노테이션 추가**:

**Application Layer - UseCase Tests (10개)**:
- [x] `UpdateTenantUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `GetTenantUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `GetTenantsUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `UpdateTenantStatusUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `GetTenantTreeUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `UpdateOrganizationUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `GetOrganizationUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `GetOrganizationsUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `DeleteOrganizationUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
- [x] `UpdateOrganizationStatusUseCaseTest.java` - `@Tag("unit")` `@Tag("application")` `@Tag("fast")`

**Adapter-out/Persistence - PersistenceAdapter Tests (2개)**:
- [x] `TenantPersistenceAdapterTest.java` - `@Tag("integration")`
- [x] `OrganizationPersistenceAdapterTest.java` - `@Tag("integration")`

**Adapter-in/REST-API - Controller Integration Tests (4개)**:
- [x] `bootstrap/.../TenantControllerIntegrationTest.java` - `@Tag("integration")` `@Tag("controller")` `@Tag("slow")`
- [x] `adapter-in/.../TenantControllerIntegrationTest.java` - `@Tag("integration")` `@Tag("controller")` `@Tag("slow")`
- [x] `bootstrap/.../OrganizationControllerIntegrationTest.java` - `@Tag("integration")` `@Tag("controller")` `@Tag("slow")`
- [x] `adapter-in/.../OrganizationControllerIntegrationTest.java` - `@Tag("integration")` `@Tag("controller")` `@Tag("slow")`

**특이사항**:
- CreateTenantUseCaseTest.java와 CreateOrganizationUseCaseTest.java는 존재하지 않음 (다른 UseCase 테스트로 대체)
- Bootstrap 중복 테스트는 유지 (모듈별 테스트 실행 목적)

#### 4. Phase 4: Mapper 테스트 작성 (4개) ✅ **완료** (2025-10-23 16:30)

**Adapter-out/Persistence - Entity Mapper Tests (2개)**:
- [x] `TenantEntityMapperTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-out/persistence-mysql/src/test/.../tenant/mapper/`
  - 태그: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ ToDomainTests: ACTIVE/SUSPENDED/DELETED Entity → Domain 변환, null 예외 (4개)
    - ✅ ToEntityTests: ID 있는/없는 Domain → Entity 변환, INACTIVE/DELETED, null 예외 (5개)
    - ✅ RoundTripTests: Domain ↔ Entity ↔ Domain 양방향 일관성 (2개)
    - ✅ UtilityClassTests: 인스턴스 생성 금지 검증 (1개)
  - **총 12개 테스트**

- [x] `OrganizationEntityMapperTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-out/persistence-mysql/src/test/.../organization/mapper/`
  - 태그: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ ToDomainTests: ACTIVE/INACTIVE/DELETED Entity → Domain 변환, null 예외 (4개)
    - ✅ ToEntityTests: ID 있는/없는 Domain → Entity 변환, INACTIVE/DELETED, null 예외 (5개)
    - ✅ RoundTripTests: Domain ↔ Entity ↔ Domain 양방향 일관성 (2개)
    - ✅ UtilityClassTests: 인스턴스 생성 금지 검증 (1개)
  - **총 12개 테스트**
  - **특이사항**: String FK 전략 (tenantId는 String 타입)

**Adapter-in/REST-API - DTO Mapper Tests (2개)**:
- [x] `TenantDtoMapperTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/.../tenant/mapper/`
  - 태그: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ ToCreateCommandTests: CreateTenantRequest → Command 변환, null 예외 (2개)
    - ✅ ToUpdateCommandTests: UpdateTenantRequest → Command 변환, tenantId/Request null 예외 (3개)
    - ✅ ToUpdateStatusCommandTests: UpdateTenantStatusRequest → Command 변환, ACTIVE/SUSPENDED, null 예외 (5개)
    - ✅ ToApiResponseTests: ACTIVE/SUSPENDED/DELETED Response → ApiResponse 변환, null 예외 (4개)
    - ✅ UtilityClassTests: 인스턴스 생성 금지 검증 (1개)
  - **총 15개 테스트**

- [x] `OrganizationDtoMapperTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/.../organization/mapper/`
  - 태그: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ ToCreateCommandTests: CreateOrganizationRequest → Command 변환, null 예외 (2개)
    - ✅ ToUpdateCommandTests: UpdateOrganizationRequest → Command 변환, organizationId/Request null 예외 (3개)
    - ✅ ToUpdateStatusCommandTests: UpdateOrganizationStatusRequest → Command 변환, ACTIVE/INACTIVE, null 예외 (5개)
    - ✅ ToApiResponseTests: ACTIVE/INACTIVE/DELETED Response → ApiResponse 변환, null 예외 (4개)
    - ✅ UtilityClassTests: 인스턴스 생성 금지 검증 (1개)
  - **총 15개 테스트**
  - **특이사항**: String FK 전략 (tenantId는 String 타입)

**병렬 실행 성과**:
- ✅ 4개 테스트 파일을 동시에 작성 (병렬 작업)
- ✅ /test 커맨드를 통한 컨벤션 자동 주입
- ✅ 총 54개 테스트 케이스 작성 완료

#### 5. Phase 5: test-fixtures 확장 (4개) ✅ **완료** (2025-10-23)

- [x] `TenantCommandFixtures.java` ✅ **완료** (2025-10-23)
  - 위치: `test-fixtures/src/main/java/com/ryuqq/fileflow/fixtures/`
  - 내용:
    ```java
    public static CreateTenantCommand createTenantCommand()
    public static CreateTenantCommand createTenantCommand(String name)
    public static UpdateTenantCommand updateTenantCommand(String tenantId, String newName)
    public static UpdateTenantStatusCommand updateTenantStatusCommand(String tenantId, String status)
    public static UpdateTenantStatusCommand activateTenantCommand(String tenantId)
    public static UpdateTenantStatusCommand suspendTenantCommand(String tenantId)
    ```

- [x] `TenantResponseFixtures.java` ✅ **완료** (2025-10-23)
  - 내용:
    ```java
    public static TenantResponse tenantResponse()
    public static TenantResponse tenantResponse(String tenantId, String name)
    public static TenantResponse tenantResponse(String tenantId, String name, String status, boolean deleted)
    public static TenantResponse suspendedTenantResponse()
    public static TenantResponse suspendedTenantResponse(String tenantId)
    public static TenantResponse deletedTenantResponse()
    public static List<TenantResponse> tenantResponseList(int count)
    public static List<TenantResponse> suspendedTenantResponseList(int count)
    ```

- [x] `OrganizationCommandFixtures.java` ✅ **완료** (2025-10-23)
  - 내용:
    ```java
    public static CreateOrganizationCommand createOrganizationCommand(String tenantId)
    public static CreateOrganizationCommand createOrganizationCommand(String tenantId, String orgCode, String name)
    public static CreateOrganizationCommand createSalesOrganizationCommand(String tenantId)
    public static CreateOrganizationCommand createHrOrganizationCommand(String tenantId)
    public static CreateOrganizationCommand createItOrganizationCommand(String tenantId)
    public static UpdateOrganizationCommand updateOrganizationCommand(Long organizationId, String newName)
    public static UpdateOrganizationStatusCommand updateOrganizationStatusCommand(Long organizationId, String status)
    public static UpdateOrganizationStatusCommand inactivateOrganizationCommand(Long organizationId)
    public static SoftDeleteOrganizationCommand softDeleteOrganizationCommand(Long organizationId)
    ```

- [x] `OrganizationResponseFixtures.java` ✅ **완료** (2025-10-23)
  - 내용:
    ```java
    public static OrganizationResponse organizationResponse(String tenantId)
    public static OrganizationResponse organizationResponse(Long organizationId, String tenantId, String orgCode, String name)
    public static OrganizationResponse organizationResponse(Long organizationId, String tenantId, String orgCode, String name, String status, boolean deleted)
    public static OrganizationResponse salesOrganizationResponse(String tenantId)
    public static OrganizationResponse hrOrganizationResponse(String tenantId)
    public static OrganizationResponse itOrganizationResponse(String tenantId)
    public static OrganizationResponse inactiveOrganizationResponse(String tenantId)
    public static OrganizationResponse deletedOrganizationResponse(String tenantId)
    public static List<OrganizationResponse> organizationResponseList(String tenantId, int count)
    public static List<OrganizationResponse> inactiveOrganizationResponseList(String tenantId, int count)
    ```

**특이사항**:
- Object Mother 패턴 완전 준수
- 다양한 상태 지원 (ACTIVE, SUSPENDED/INACTIVE, DELETED)
- Pagination 테스트용 리스트 생성 메서드 포함
- Pure Java + Javadoc 표준 준수

---

## 🟡 우선순위 2 (Medium) - 다음 단계

### Facade/Assembler 테스트 (6개)

- [x] `TenantCommandFacadeTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/facade/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ CreateTenantDelegationTests: 위임, 파라미터 전달, 반환값 (3개)
    - ✅ UpdateTenantDelegationTests: 위임, 파라미터 전달, 반환값 (3개)
    - ✅ UpdateTenantStatusDelegationTests: ACTIVE↔SUSPENDED 전환, 예외 처리 (5개)
    - ✅ FacadeIntegrationTests: 모든 UseCase 통합, 순수 위임 검증 (2개)
  - **총 13개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `TenantQueryFacadeTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/facade/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ GetTenantTests: UseCase 호출, Assembler 변환, 예외 전파 (3개)
    - ✅ GetTenantsWithPageTests: Offset Pagination, PageResponse 필드 검증 (4개)
    - ✅ GetTenantsWithSliceTests: Cursor Pagination, SliceResponse 필드 검증 (4개)
    - ✅ GetTenantTreeTests: TenantTreeResponse 반환 검증 (3개)
    - ✅ FacadeOrchestrationTests: 모든 UseCase 통합 검증 (2개)
  - **총 16개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `OrganizationCommandFacadeTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/organization/facade/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ CreateOrganizationDelegationTests: 위임, 파라미터, 반환값 (3개)
    - ✅ UpdateOrganizationDelegationTests: 위임, 파라미터, 반환값 (3개)
    - ✅ UpdateOrganizationStatusDelegationTests: 상태 전환, 예외 (3개)
    - ✅ DeleteOrganizationDelegationTests: 삭제 위임, 예외 (3개)
    - ✅ FacadeIntegrationTests: 모든 UseCase 통합, 순수 위임 검증 (2개)
  - **총 14개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `OrganizationQueryFacadeTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/organization/facade/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ GetOrganizationTests: UseCase 호출, Assembler 변환, 예외 전파 (3개)
    - ✅ GetOrganizationsWithPageTests: Offset Pagination, PageResponse 검증 (4개)
    - ✅ GetOrganizationsWithSliceTests: Cursor Pagination, SliceResponse 검증 (4개)
    - ✅ FacadeIntegrationTests: 모든 UseCase 통합, 순수 위임 검증 (2개)
  - **총 13개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅

- [x] `TenantAssemblerTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/tenant/assembler/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ ToDomainTests: TenantName → Domain 생성, null 처리, UUID 생성 (3개)
    - ✅ ToResponseTests: ACTIVE/SUSPENDED/DELETED 변환, null 처리 (4개)
    - ✅ FieldMappingTests: TenantId/Name/Status/Timestamps/Deleted 매핑 (5개)
    - ✅ EdgeCaseTests: 모든 Status, 특수문자, UUID, 최소/최대 길이 (7개)
    - ✅ LawOfDemeterTests: getIdValue/getNameValue/getStatus().name() 검증 (3개)
    - ✅ UtilityClassTests: 인스턴스 생성 금지 검증 (1개)
  - **총 23개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅
  - **특이사항**: TenantName 최대 길이 50자 (100자 아님)

- [x] `OrganizationAssemblerTest.java` ✅ **완료** (2025-10-23)
  - 위치: `application/src/test/java/com/ryuqq/fileflow/application/iam/organization/assembler/`
  - 태그: `@Tag("unit")` `@Tag("application")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ ToDomainTests: Command → Domain 생성, null 처리 (4개)
    - ✅ ToResponseTests: ACTIVE/INACTIVE/DELETED 변환, null 처리 (4개)
    - ✅ FieldMappingTests: OrganizationId(Long)/TenantId/OrgCode/Name/Status 매핑 (6개)
    - ✅ EdgeCaseTests: 모든 Status, 특수문자, Long ID, 최소/최대 길이 (5개)
    - ✅ LawOfDemeterTests: getIdValue/getTenantId/getOrgCodeValue 검증 (4개)
    - ✅ UtilityClassTests: 인스턴스 생성 금지 검증 (1개)
  - **총 24개 테스트**
  - **실행 결과**: BUILD SUCCESSFUL ✅
  - **특이사항**: OrganizationId는 Long (TenantId는 String)

### ✅ Mapper 테스트 (4개) - Phase 4로 이동
*Phase 4 섹션으로 이동됨*

### REST Controller 누락 엔드포인트 테스트

**TenantController**:
- [ ] `GET /api/v1/tenants` (목록 조회)
- [ ] `GET /api/v1/tenants/{id}` (단건 조회)
- [ ] `PATCH /api/v1/tenants/{id}/status` (상태 변경)
- [ ] `GET /api/v1/tenants/{id}/tree` (트리 조회)

**OrganizationController**:
- [ ] `GET /api/v1/organizations` (목록 조회)
- [ ] `GET /api/v1/organizations/{id}` (단건 조회)
- [ ] `DELETE /api/v1/organizations/{id}` (삭제)
- [ ] `PATCH /api/v1/organizations/{id}/status` (상태 변경)

### test-fixtures 추가 확장 (2개) ✅ **완료** (2025-10-23)

- [x] `TenantJpaEntityFixtures.java` ✅ **완료** (2025-10-23)
  - 위치: `test-fixtures/src/main/java/com/ryuqq/fileflow/fixtures/`
  - 내용:
    ```java
    // Basic Factory Methods
    public static TenantJpaEntity activeTenantEntity()
    public static TenantJpaEntity tenantEntityWithId(String id)
    public static TenantJpaEntity activeTenantEntityWithIdAndName(String id, String name)
    public static TenantJpaEntity suspendedTenantEntity()
    public static TenantJpaEntity suspendedTenantEntityWithId(String id)
    public static TenantJpaEntity deletedTenantEntity()
    public static TenantJpaEntity deletedTenantEntityWithId(String id)
    public static TenantJpaEntity deletedTenantEntityWithName(String name)
    public static TenantJpaEntity customTenantEntity(String id, String name, TenantStatus status, ...)

    // List Generation for Pagination Tests
    public static List<TenantJpaEntity> tenantEntityList(int count)
    public static List<TenantJpaEntity> suspendedTenantEntityList(int count)
    public static List<TenantJpaEntity> deletedTenantEntityList(int count)
    public static List<TenantJpaEntity> mixedStatusTenantEntityList(int count)
    ```
  - **특이사항**:
    - Object Mother 패턴 완전 준수
    - JPA Static Factory Methods 활용 (create, reconstitute)
    - String UUID ID 전략 (Random UUID 생성)
    - 다양한 상태 지원 (ACTIVE, SUSPENDED, DELETED)
    - Pagination 테스트용 리스트 생성 메서드 (14개 메서드)
    - Sequential naming: "Test Company 1", "Test Company 2", ...
    - Time-based ordering: older entities first

- [x] `OrganizationJpaEntityFixtures.java` ✅ **완료** (2025-10-23)
  - 위치: `test-fixtures/src/main/java/com/ryuqq/fileflow/fixtures/`
  - 내용:
    ```java
    // New Entities (ID null) using create()
    public static OrganizationJpaEntity salesOrgEntity(String tenantId)
    public static OrganizationJpaEntity hrOrgEntity(String tenantId)
    public static OrganizationJpaEntity itOrgEntity(String tenantId)
    public static OrganizationJpaEntity orgEntityWithCode(String tenantId, String orgCode, String name)

    // Persisted Entities (ID present) using reconstitute()
    public static OrganizationJpaEntity orgEntityWithId(Long id, String tenantId)
    public static OrganizationJpaEntity orgEntityWithIdAndCode(Long id, String tenantId, String orgCode, String name)
    public static OrganizationJpaEntity inactiveOrgEntity(String tenantId)
    public static OrganizationJpaEntity inactiveOrgEntityWithId(Long id, String tenantId)
    public static OrganizationJpaEntity deletedOrgEntity(String tenantId)
    public static OrganizationJpaEntity deletedOrgEntityWithId(Long id, String tenantId)
    public static OrganizationJpaEntity customOrgEntity(Long id, String tenantId, String orgCode, ...)

    // List Generation with Sequential IDs
    public static List<OrganizationJpaEntity> orgEntityList(String tenantId, int count)
    public static List<OrganizationJpaEntity> inactiveOrgEntityList(String tenantId, int count)
    public static List<OrganizationJpaEntity> deletedOrgEntityList(String tenantId, int count)
    public static List<OrganizationJpaEntity> mixedStatusOrgEntityList(String tenantId, int count)
    ```
  - **특이사항**:
    - Object Mother 패턴 완전 준수
    - JPA Static Factory Methods 활용 (create, reconstitute)
    - Long Auto Increment ID 전략 (Sequential ID 생성: 1, 2, 3, ...)
    - ID null (new) vs ID present (persisted) 구분
    - 다양한 상태 지원 (ACTIVE, INACTIVE, DELETED)
    - Pagination 테스트용 리스트 생성 메서드 (17개 메서드)
    - Mixed status lists: ID offset handling to prevent conflicts
    - Sequential org codes: "ORG-001", "ORG-002", ...

---

## 🟢 우선순위 3 (Low) - 추가 개선

### Domain Enum/Exception 테스트 (4개)

- [ ] `TenantStatusTest.java`
  - 태그: `@Tag("unit")` `@Tag("domain")` `@Tag("fast")`
  - 테스트: Enum 값 검증

- [ ] `OrganizationStatusTest.java`
  - 테스트: Enum 값 검증

- [ ] `TenantNotFoundExceptionTest.java`
  - 테스트: Exception 메시지, ErrorCode 검증

- [ ] `TenantErrorCodeTest.java`
  - 테스트: ErrorCode 필드 검증

### ✅ DTO Validation 테스트 (8개) - **완료** (2025-10-23)

- [x] `CreateTenantRequestTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/dto/`
  - 태그: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`
  - 테스트 시나리오:
    - ✅ NameValidation: @NotBlank (null, empty, blank) (3개)
    - ✅ SuccessCase: 유효한 name, 최소/최대 길이 (3개)
    - ✅ RecordImmutability: equals/hashCode/toString (3개)
  - **총 9개 테스트**

- [x] `UpdateTenantRequestTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/dto/`
  - 테스트 시나리오:
    - ✅ NameValidation: @NotBlank (null, empty, blank) (3개)
    - ✅ SuccessCase: 유효한 name, 최소 길이 (2개)
    - ✅ RecordImmutability: equals/hashCode (2개)
  - **총 7개 테스트**

- [x] `UpdateTenantStatusRequestTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/dto/`
  - 테스트 시나리오:
    - ✅ StatusValidation: @NotNull (1개)
    - ✅ SuccessCase: ACTIVE, SUSPENDED (3개)
    - ✅ RecordImmutability: equals/hashCode (2개)
    - ✅ StatusTransitionScenarios: ACTIVE↔SUSPENDED (2개)
  - **총 8개 테스트**

- [x] `CreateOrganizationRequestTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/organization/dto/`
  - 테스트 시나리오:
    - ✅ TenantIdValidation: @NotNull + @NotBlank (3개)
    - ✅ OrgCodeValidation: @NotBlank (3개)
    - ✅ NameValidation: @NotBlank (3개)
    - ✅ SuccessCase: 모든 필드 유효, 최소 길이 (2개)
    - ✅ RecordImmutability: equals/hashCode (2개)
  - **총 13개 테스트**

- [x] `UpdateOrganizationRequestTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/organization/dto/`
  - 테스트 시나리오:
    - ✅ NameValidation: @NotBlank (3개)
    - ✅ SuccessCase: 유효한 name, 최소/최대 길이 (3개)
    - ✅ RecordImmutability: equals/hashCode (2개)
  - **총 8개 테스트**

- [x] `UpdateOrganizationStatusRequestTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/organization/dto/`
  - 테스트 시나리오:
    - ✅ StatusValidation: @NotNull (1개)
    - ✅ SuccessCase: ACTIVE, INACTIVE (3개)
    - ✅ RecordImmutability: equals/hashCode (2개)
    - ✅ StatusTransitionScenarios: ACTIVE→INACTIVE (2개)
  - **총 8개 테스트**

- [x] `TenantListQueryParamTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/tenant/dto/`
  - 테스트 시나리오:
    - ✅ PageValidation: @Min(0) (3개)
    - ✅ SizeValidation: @Min(1), @Max(100) (4개)
    - ✅ DefaultValueTest: size=null → 20 (2개)
    - ✅ PaginationStrategyTest: Offset vs Cursor (2개)
    - ✅ ToQueryConversion: Application Layer Query 변환 (2개)
    - ✅ SuccessCase: 정상 케이스 (2개)
  - **총 15개 테스트**

- [x] `OrganizationListQueryParamTest.java` ✅ **완료** (2025-10-23)
  - 위치: `adapter-in/rest-api/src/test/java/com/ryuqq/fileflow/adapter/rest/iam/organization/dto/`
  - 테스트 시나리오:
    - ✅ PageValidation: @Min(0) (2개)
    - ✅ SizeValidation: @Min(1), @Max(100) (3개)
    - ✅ TenantIdValidation: @Pattern (4개)
    - ✅ DefaultValueTest: size=null → 20 (2개)
    - ✅ PaginationStrategyTest: Offset vs Cursor (2개)
    - ✅ ToQueryConversion: Application Layer Query 변환 (2개)
    - ✅ SuccessCase: 정상 케이스 (2개)
  - **총 17개 테스트**

**특이사항**:
- ✅ Jakarta Validation 표준 준수 (@NotBlank, @NotNull, @Min, @Max, @Pattern)
- ✅ Validator Factory를 사용한 검증 (@BeforeAll setup)
- ✅ 각 필드별 Nested 클래스로 구조화
- ✅ Record 불변성 확인 (equals/hashCode/toString)
- ✅ Pagination 전략 지원 (Offset-based, Cursor-based)
- ✅ Compact Constructor 기본값 적용 테스트 (size=null → 20)
- ✅ Application Layer Query 변환 메서드 검증 (toQuery())

### GlobalExceptionHandler 테스트

- [ ] `GlobalExceptionHandlerTest.java`
  - 태그: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`
  - 테스트:
    - ✅ IllegalArgumentException → 400 Bad Request
    - ✅ IllegalStateException → 409 Conflict
    - ✅ TenantNotFoundException → 404 Not Found
    - ✅ MethodArgumentNotValidException → 400 + errors
    - ✅ RFC 7807 응답 포맷 검증

---

## 📝 테스트 작성 가이드

### /test 커맨드 사용법

```bash
# /test 커맨드를 사용하면 테스트 작성 컨벤션이 자동으로 주입됩니다
/test UpdateTenantUseCaseTest
```

### 표준 테스트 구조

```java
/**
 * UpdateTenantUseCaseTest - UpdateTenantUseCase 단위 테스트
 *
 * <p>Mockito를 사용한 UseCase 계층 단위 테스트입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("UpdateTenantUseCase 테스트")
class UpdateTenantUseCaseTest {

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {
        // ...
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {
        // ...
    }
}
```

### test-fixtures 사용 원칙

```java
// ✅ Good - Fixtures 사용
Tenant tenant = TenantFixtures.activeTenant();
CreateTenantCommand command = TenantCommandFixtures.createTenantCommand();

// ❌ Bad - 직접 생성
Tenant tenant = new Tenant(TenantId.of("id"), TenantName.of("name"));
```

---

## 🔄 체크포인트 업데이트 규칙

### 컨텍스트 75% 도달 시

1. **현재 진행 상황 기록**:
   ```markdown
   ### 마지막 작업 (2025-10-23 15:30)
   - ✅ UpdateTenantUseCaseTest 작성 완료
   - 🚧 GetTenantUseCaseTest 작성 중 (50%)
   - 📝 다음: GetTenantUseCaseTest 완료 → GetTenantsUseCaseTest 시작
   ```

2. **완료된 항목 체크**:
   - 이 문서의 체크박스 업데이트
   - 완료일 기록

3. **새 세션 시작 메시지 작성**:
   ```markdown
   ### 🔄 세션 재개 가이드 (2025-10-23)
   - 마지막 완료: UpdateTenantUseCaseTest
   - 진행 중: GetTenantUseCaseTest (Arrange 단계까지 완료)
   - 다음 작업: GetTenantUseCaseTest의 Act/Assert 완성
   - 참고 파일: CreateTenantUseCaseTest.java
   ```

---

## 📈 진행률 추적

### 일별 진행 현황

- **2025-10-23**:
  - ✅ 테스트 커버리지 분석 완료
  - ✅ TODO_TEST_LIST.md 작성 완료
  - ✅ Tenant UseCase 테스트 5개 완료 (83개 테스트)
  - ✅ Organization UseCase 테스트 5개 완료 (36개 테스트)
  - ✅ TenantCommandServiceTest 완료 (15개 테스트)
  - 🚧 다음: TenantQueryServiceTest 시작

---

## 🎯 최종 목표

- **현재**: 43개 테스트 (52%) ⬆️ +4개 파일 (54개 테스트) 완료!
- **목표**: 67개 테스트 (81%)
- **남은 작업**: 24개 테스트
- **예상 기간**: 1주 (우선순위 1 거의 완료, Phase 4 완료)

### 📌 주요 성과 (2025-10-23)
- ✅ Tenant UseCase 테스트 5개 완료 (83개 테스트)
- ✅ Organization UseCase 테스트 5개 완료 (36개 테스트)
- ✅ Service 계층 테스트 4개 완료 (69개 테스트)
- ✅ Facade/Assembler 테스트 6개 완료 (90개 테스트)
- ✅ Query Repository 테스트 2개 완료 (51개 테스트)
- ✅ **Phase 4: Mapper 테스트 4개 완료 (54개 테스트) 🎉**
  - TenantEntityMapperTest (12개)
  - OrganizationEntityMapperTest (12개)
  - TenantDtoMapperTest (15개)
  - OrganizationDtoMapperTest (15개)
- ✅ **Phase 5: test-fixtures 확장 (4개 Command/Response + 2개 JpaEntity) 🎉**
  - TenantCommandFixtures.java (6개 메서드)
  - TenantResponseFixtures.java (8개 메서드)
  - OrganizationCommandFixtures.java (9개 메서드)
  - OrganizationResponseFixtures.java (10개 메서드)
  - **TenantJpaEntityFixtures.java (14개 메서드)** ✨ 신규 완료
  - **OrganizationJpaEntityFixtures.java (17개 메서드)** ✨ 신규 완료
- ✅ Persistence Layer 커버리지 40% → 53% 향상
- ✅ REST API Layer 커버리지 27% → 40% 향상
- ✅ 전체 진행률 47% → 52% 달성
- ✅ **Option A (JpaEntity Fixtures) 완료 - Persistence 테스트 지원 강화**

---

**문서 버전**: 1.0
**작성자**: Claude Code
**마지막 업데이트**: 2025-10-23
