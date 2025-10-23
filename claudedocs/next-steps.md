# 🎯 다음 진행 단계 가이드

**업데이트**: 2025-10-23 14:10
**현재 커버리지**: 47% (39/83)
**완료 단계**: Phase 1, Phase 2
**다음 목표**: 64% (53/83)

---

## ✅ 완료된 Phase

### Phase 1: Facade/Assembler 테스트 (6개) - 완료 ✅

**완료 일자**: 2025-10-23 (이전 세션)

1. ✅ TenantCommandFacadeTest (13개 테스트)
2. ✅ TenantQueryFacadeTest (16개 테스트)
3. ✅ OrganizationCommandFacadeTest (14개 테스트)
4. ✅ OrganizationQueryFacadeTest (13개 테스트)
5. ✅ TenantAssemblerTest (23개 테스트)
6. ✅ OrganizationAssemblerTest (24개 테스트)

**총 103개 테스트 - 100% 성공**

---

### Phase 2: Query Repository 통합 테스트 (2개) - 완료 ✅

**완료 일자**: 2025-10-23 (현재 세션)

1. ✅ **TenantQueryRepositoryAdapterTest** (23개 테스트)
   - 소요 시간: 3.58초 (TestContainers MySQL 8.0)
   - 특이사항: UUID ID 정렬 이슈 해결, Base64 Cursor 인코딩 검증

2. ✅ **OrganizationQueryRepositoryAdapterTest** (28개 테스트)
   - 소요 시간: 4.04초 (TestContainers MySQL 8.0)
   - 특이사항: Long ID Auto-increment 특성 활용, tenantId/orgCodeContains 필터링 검증

**총 51개 테스트 - 100% 성공**

---

## 🚀 즉시 시작 가능한 작업

### 🔴 Phase 3: 최우선 작업 (1시간 작업으로 47% → 64% 달성)

#### 1. 기존 테스트 태그 추가 (8개) - 10분

**간단한 작업**: 각 파일 상단에 3줄만 추가

```java
@Tag("unit")
@Tag("application")  // 또는 "adapter", "persistence"
@Tag("fast")
```

**대상 파일**:
- [ ] CreateTenantUseCaseTest.java
- [ ] CreateOrganizationUseCaseTest.java
- [ ] TenantPersistenceAdapterTest.java
- [ ] OrganizationPersistenceAdapterTest.java
- [ ] TenantControllerIntegrationTest.java
- [ ] OrganizationControllerIntegrationTest.java
- [ ] Bootstrap 중복 테스트 2개

**효과**: +8개 커버리지 (47% → 57%)

---

#### 2. test-fixtures 확장 (4개) - 30분

**Command Fixtures 작성**:

```bash
# TenantCommandFixtures.java
/test TenantCommandFixtures
```

**목적**:
- CreateTenantCommand 생성 헬퍼
- UpdateTenantCommand 생성 헬퍼
- SoftDeleteTenantCommand 생성 헬퍼

**예상 메서드**: 5-7개

---

```bash
# OrganizationCommandFixtures.java
/test OrganizationCommandFixtures
```

**목적**:
- CreateOrganizationCommand 생성 헬퍼
- UpdateOrganizationCommand 생성 헬퍼
- SoftDeleteOrganizationCommand 생성 헬퍼

**예상 메서드**: 5-7개

---

**Response Fixtures 작성**:

```bash
# TenantResponseFixtures.java
/test TenantResponseFixtures
```

**목적**:
- TenantResponse 생성 헬퍼
- PageResponse<TenantResponse> 생성 헬퍼
- SliceResponse<TenantResponse> 생성 헬퍼

**예상 메서드**: 5-7개

---

```bash
# OrganizationResponseFixtures.java
/test OrganizationResponseFixtures
```

**목적**:
- OrganizationResponse 생성 헬퍼
- PageResponse<OrganizationResponse> 생성 헬퍼
- SliceResponse<OrganizationResponse> 생성 헬퍼

**예상 메서드**: 5-7개

**효과**: +4개 커버리지 (57% → 62%)

---

#### 3. Entity Mapper 테스트 (2개) - 20분

```bash
# TenantEntityMapperTest.java
/test TenantEntityMapperTest
```

**위치**: `adapter-out/persistence-mysql/src/test/.../tenant/mapper/`

**테스트 내용**:
- `Domain → JpaEntity` 변환 (toDomain)
- `JpaEntity → Domain` 변환 (toEntity)
- null 필드 처리
- TenantId (String UUID) 변환
- TenantStatus Enum 변환 (ACTIVE, INACTIVE, SUSPENDED)

**예상 테스트 수**: 6-8개
**태그**: `@Tag("unit")` `@Tag("adapter")` `@Tag("fast")`

---

```bash
# OrganizationEntityMapperTest.java
/test OrganizationEntityMapperTest
```

**테스트 내용**:
- `Domain → JpaEntity` 변환
- `JpaEntity → Domain` 변환
- OrganizationId (Long) 변환
- OrgCode Value Object 변환
- OrganizationStatus Enum 변환 (ACTIVE, INACTIVE)
- tenantId (String FK) 매핑

**예상 테스트 수**: 6-8개

**효과**: +2개 커버리지 (62% → 64%)

---

### 📊 Phase 3 완료 후 예상 결과

| 작업 | 소요 시간 | 커버리지 증가 | 누적 커버리지 |
|------|----------|-------------|-------------|
| 태그 추가 (8개) | 10분 | +8개 | 47% → 57% |
| test-fixtures (4개) | 30분 | +4개 | 57% → 62% |
| Entity Mapper (2개) | 20분 | +2개 | 62% → 64% |
| **합계** | **1시간** | **+14개** | **47% → 64%** |

---

## 🟡 Phase 4: 다음 단계 (Medium Priority)

### 1. DTO Mapper 테스트 (2개) - 20분

#### TenantDtoMapperTest
```bash
/test TenantDtoMapperTest
```

**위치**: `adapter-in/rest-api/src/test/.../tenant/mapper/`

**테스트 내용**:
- `CreateTenantRequest → CreateTenantCommand` 변환
- `UpdateTenantRequest → UpdateTenantCommand` 변환
- `TenantResponse → ApiResponse<TenantResponse>` 변환
- Validation 어노테이션 검증 (@NotBlank, @Size)
- null 처리

**예상 테스트 수**: 8-10개

---

#### OrganizationDtoMapperTest
```bash
/test OrganizationDtoMapperTest
```

**테스트 내용**:
- `CreateOrganizationRequest → CreateOrganizationCommand` 변환
- `UpdateOrganizationRequest → UpdateOrganizationCommand` 변환
- tenantId (String) 매핑
- orgCode 검증
- `OrganizationResponse → ApiResponse` 변환

**예상 테스트 수**: 8-10개

---

### 2. REST Controller 누락 엔드포인트 (4개) - 1시간

#### TenantController 추가 테스트

**GET /api/v1/tenants (목록 조회)**
```bash
/test TenantController_GetTenants_Test
```

**테스트 내용**:
- Offset Pagination 응답 검증
- Cursor Pagination 응답 검증
- 필터링 (nameContains, deleted)
- RFC 7807 응답 포맷 (ApiResponse<PageResponse>)

**예상 테스트 수**: 6-8개

---

**GET /api/v1/tenants/{id} (단건 조회)**
```bash
/test TenantController_GetTenant_Test
```

**테스트 내용**:
- 정상 조회 (200 OK)
- 404 Not Found (미존재 Tenant)
- 400 Bad Request (잘못된 ID 형식)

**예상 테스트 수**: 3-4개

---

#### OrganizationController 추가 테스트

**GET /api/v1/organizations (목록 조회)**
```bash
/test OrganizationController_GetOrganizations_Test
```

**테스트 내용**:
- Tenant 필터링 (tenantId 쿼리 파라미터)
- orgCode/name 검색
- Dual Pagination (Offset + Cursor)

**예상 테스트 수**: 6-8개

---

**GET /api/v1/organizations/{id} (단건 조회)**
```bash
/test OrganizationController_GetOrganization_Test
```

**테스트 내용**:
- 정상 조회 (200 OK)
- 404 Not Found
- 400 Bad Request (Long ID 파싱 오류)

**예상 테스트 수**: 3-4개

---

### 3. test-fixtures 추가 확장 (2개) - 20분

#### TenantJpaEntityFixtures
```bash
/test TenantJpaEntityFixtures
```

**목적**: TenantJpaEntity 생성 헬퍼 (Integration 테스트용)

---

#### OrganizationJpaEntityFixtures
```bash
/test OrganizationJpaEntityFixtures
```

**목적**: OrganizationJpaEntity 생성 헬퍼 (Integration 테스트용)

---

## 🟢 Phase 5: 낮은 우선순위 (Low Priority)

### 1. Domain Enum/Exception 테스트 (4개)

- [ ] TenantStatusTest.java
- [ ] OrganizationStatusTest.java
- [ ] TenantNotFoundExceptionTest.java
- [ ] TenantErrorCodeTest.java

**예상 소요**: 30분

---

### 2. DTO Validation 테스트 (8개)

Request/QueryParam Validation 테스트:
- CreateTenantRequest Validation
- UpdateTenantRequest Validation
- CreateOrganizationRequest Validation
- UpdateOrganizationRequest Validation
- TenantQueryParam Validation
- OrganizationQueryParam Validation
- 기타 DTO Validation 2개

**예상 소요**: 1-2시간

---

## 📈 전체 로드맵 요약

### 현재 상태 (Phase 2 완료)
- **커버리지**: 47% (39/83)
- **완료**: Phase 1 (Facade/Assembler 6개), Phase 2 (Query Repository 2개)
- **남은 작업**: 28개 테스트

### Phase 3 (최우선, 1시간)
- **목표**: 47% → 64%
- **작업**: 태그 추가 8개 + test-fixtures 4개 + Entity Mapper 2개 (총 14개)

### Phase 4 (다음 단계, 2시간)
- **목표**: 64% → 72%
- **작업**: DTO Mapper 2개 + REST Controller 4개 + JpaEntity Fixtures 2개 (총 8개)

### Phase 5 (마무리, 2시간)
- **목표**: 72% → 81%
- **작업**: Domain Enum/Exception 4개 + DTO Validation 8개 (총 12개)

### 최종 목표
- **목표 커버리지**: 81% (67/83)
- **예상 총 소요**: 5시간

---

## 💡 핵심 학습 사항 (Phase 2에서 발견)

### 1. UUID vs Auto-increment ID 테스트 전략

**UUID (Tenant)**:
- 생성 순서와 무관하므로 정렬 필요
- Cursor: Base64(UUID String)
```java
List<TenantJpaEntity> sortedEntities = allEntities.stream()
    .sorted((e1, e2) -> e1.getId().compareTo(e2.getId()))
    .toList();
```

**Auto-increment Long (Organization)**:
- 생성 순서대로 증가하므로 정렬 불필요
- Cursor: Base64(Long.toString())
```java
// 저장 순서 = ID 순서
OrganizationJpaEntity first = createAndSave("ORG1");
OrganizationJpaEntity second = createAndSave("ORG2");
// first.getId() < second.getId() 보장됨
```

---

### 2. Domain 메서드명 규칙

**Value Object가 아닌 경우**:
- `getTenantId()` → String 직접 반환 (getTenantIdValue() ❌)
- `getName()` → String 직접 반환 (getNameValue() ❌)

**Value Object인 경우**:
- `getIdValue()` → Long 반환 (OrganizationId → Long)
- `getOrgCodeValue()` → String 반환 (OrgCode → String)

---

### 3. Organization vs Tenant 차이점

| 항목 | Organization | Tenant |
|------|-------------|--------|
| **PK 타입** | Long (Auto-increment) | String (UUID) |
| **FK 전략** | Long organizationId | String tenantId |
| **상태 전환** | ACTIVE → INACTIVE만 | 양방향 가능 |
| **복원 정책** | INACTIVE → ACTIVE 금지 | 복원 가능 |
| **Cursor 인코딩** | Base64(Long.toString()) | Base64(UUID String) |
| **정렬 필요 여부** | 불필요 (순차 증가) | 필요 (랜덤 UUID) |

---

## 📝 작업 팁

### /test 커맨드 사용
```bash
# 테스트 작성 시 자동으로 컨벤션 주입
/test TenantEntityMapperTest
```

### 컨텍스트 관리
- 현재 사용량: ~110K/200K (55%)
- 75% 도달 시 체크포인트 업데이트 필요
- 세션 종료 전 TODO_TEST_LIST.md 및 test-progress 업데이트

### 참고 파일
- **패턴 참고**: TenantQueryRepositoryAdapterTest, OrganizationQueryRepositoryAdapterTest
- **Fixture 참고**: TenantFixtures, OrganizationFixtures
- **규칙 문서**: docs/coding_convention/
- **진행 상황**: claudedocs/test-progress-2025-10-23.md

---

## 📋 체크리스트

### 테스트 작성 전
- [ ] 구현 파일 읽기 (Mapper, Entity, DTO)
- [ ] 의존성 파악 (Domain, Fixtures)
- [ ] Fixture 확인 및 필요 시 추가

### 테스트 작성 중
- [ ] @Nested 그룹 구조화 (필요 시)
- [ ] AAA 패턴 준수 (Arrange-Act-Assert)
- [ ] 태그 추가 (`@Tag("unit")`, `@Tag("adapter")`, `@Tag("fast")`)
- [ ] DisplayName 명확하게 작성

### 테스트 실행 후
- [ ] BUILD SUCCESSFUL 확인
- [ ] 100% 성공률 확인
- [ ] HTML 리포트 확인 (build/reports/tests/test/index.html)
- [ ] TODO_TEST_LIST.md 업데이트
- [ ] test-progress-2025-10-23.md 업데이트

---

## 🚀 다음 명령어

**즉시 시작 (10분 작업)**:
```bash
# 1. 기존 테스트에 태그 추가 (8개 파일)
# 각 파일 상단에 3줄 추가:
# @Tag("unit") @Tag("application") @Tag("fast")
```

**그 다음 (30분 작업)**:
```bash
# 2. test-fixtures 확장
/test TenantCommandFixtures
/test TenantResponseFixtures
/test OrganizationCommandFixtures
/test OrganizationResponseFixtures
```

**마지막 (20분 작업)**:
```bash
# 3. Entity Mapper 테스트
/test TenantEntityMapperTest
/test OrganizationEntityMapperTest
```

**예상 완료 시각**: 1시간 후 64% 달성 🎯

---

**작성자**: Claude Code
**세션**: 2025-10-23 Phase 2 완료
**다음 목표**: Phase 3 (47% → 64%)
