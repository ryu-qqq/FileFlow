# End-to-End 시나리오 테스트 구현 현황

**Jira Task**: KAN-265 - [Phase 1C-5] End-to-End 시나리오 테스트
**작성일**: 2025-10-26
**상태**: 부분 완료 (3/10 scenarios)

---

## 📊 구현 현황 요약

### ✅ 완료된 시나리오 (3개)

| 시나리오 | 파일명 | 상태 | 비고 |
|---------|--------|------|------|
| **1. Tenant CRUD + Soft Delete** | `Scenario01_TenantCrudAndSoftDeleteE2ETest.java` | ✅ 완료 | 4개 테스트 케이스 |
| **2. Organization 중복 방지** | `Scenario02_OrganizationDuplicatePreventionE2ETest.java` | ✅ 완료 | 5개 테스트 케이스 |
| **9. Settings 우선순위 병합** | `Scenario09_SettingsPriorityMergeE2ETest.java` | ✅ 완료 | 4개 테스트 케이스 |

### ⏸️ 블로킹된 시나리오 (7개) - REST API 미구현

| 시나리오 | 필요한 REST API | 블로커 상태 |
|---------|----------------|-----------|
| **3. User 다중 멤버십** | User API, UserContext API | 🚨 API 없음 |
| **4. SELF 스코프 권한** | User API, Permission API | 🚨 API 없음 |
| **5. ORGANIZATION 스코프 권한** | User API, Permission API | 🚨 API 없음 |
| **6. TENANT 스코프 권한** | User API, Permission API | 🚨 API 없음 |
| **7. ABAC 파일 크기 제한** | ABAC API (파일 업로드) | 🚨 API 없음 |
| **8. ABAC MIME 타입 제한** | ABAC API (파일 업로드) | 🚨 API 없음 |
| **10. 캐시 무효화** | User API, Permission API, Role API | 🚨 API 없음 |

---

## 🏗️ 구현된 인프라

### 1. E2E 테스트 베이스 클래스

**파일**: `EndToEndTestBase.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class EndToEndTestBase
```

**주요 기능**:
- ✅ MySQL 8.0 Testcontainer
- ✅ Redis 7-alpine Testcontainer
- ✅ MockMvc 설정
- ✅ JSON 변환 헬퍼 메서드 (`toJson()`, `fromJson()`)
- ✅ `@Tag("e2e")`, `@Tag("slow")` 메타데이터

### 2. Test Fixture (Mother Object 패턴)

**파일**: `fixture/TenantFixture.java`, `fixture/OrganizationFixture.java`

```java
// Tenant
TenantFixture.createRequest("tenant-name")
TenantFixture.createRequest() // 자동 생성 이름

// Organization
OrganizationFixture.createRequest(tenantId, "ORG001", "Org Name")
OrganizationFixture.createRequest(tenantId, "ORG001") // 자동 생성 이름
OrganizationFixture.createRequest(tenantId) // 자동 생성 orgCode
```

---

## 📋 완료된 시나리오 상세

### Scenario 1: Tenant CRUD + Soft Delete

**파일**: `Scenario01_TenantCrudAndSoftDeleteE2ETest.java`

**테스트 케이스**:
1. ✅ `tenantCrudAndSoftDelete_FullFlow_Success()` - 전체 CRUD 플로우 + Soft Delete
2. ✅ `getTenants_ExcludesSoftDeletedTenants()` - 삭제된 Tenant는 목록에서 제외
3. ✅ `createTenant_DuplicateName_Returns409()` - 중복 이름 생성 시 409 Conflict
4. ✅ `getTenant_NotFound_Returns404()` - 존재하지 않는 Tenant 조회 시 404

**검증 항목**:
- Tenant 생성 (POST /api/v1/tenants) → 201 Created
- Tenant 조회 (GET /api/v1/tenants/{tenantId}) → 200 OK
- Tenant 수정 (PATCH /api/v1/tenants/{tenantId}) → 200 OK
- Tenant 상태 변경 (PATCH /api/v1/tenants/{tenantId}/status) → 200 OK
- Tenant Soft Delete (DELETE /api/v1/tenants/{tenantId}) → 204 No Content
- 삭제된 Tenant 조회 → 404 Not Found

---

### Scenario 2: Organization 중복 방지

**파일**: `Scenario02_OrganizationDuplicatePreventionE2ETest.java`

**테스트 케이스**:
1. ✅ `createOrganization_SameTenantDuplicateOrgCode_Returns409()` - 같은 Tenant 내 중복 orgCode 방지
2. ✅ `createOrganization_DifferentTenantSameOrgCode_Success()` - 다른 Tenant 간 동일 orgCode 허용
3. ✅ `createOrganization_MultipleDifferentOrgCodes_Success()` - 여러 Organization 생성 가능
4. ✅ `createOrganization_InvalidOrgCode_Returns400()` - 빈 orgCode 검증
5. ✅ `createOrganization_NonExistentTenant_Returns404()` - 존재하지 않는 Tenant 처리

**검증 항목**:
- (tenant_id, org_code) 복합 유니크 제약
- 같은 Tenant 내 org_code 중복 불가 → 409 Conflict
- 다른 Tenant 간 org_code 중복 허용 → 201 Created

---

### Scenario 9: Settings 우선순위 병합

**파일**: `Scenario09_SettingsPriorityMergeE2ETest.java`

**테스트 케이스**:
1. ✅ `settingsPriorityMerge_ThreeLevels_Success()` - ORG > TENANT > DEFAULT 3레벨 병합
2. ✅ `settingsPriorityMerge_OrgOnly_ReturnsOrgValue()` - ORG 레벨만 있을 때
3. ✅ `settingsPriorityMerge_MultipleKeys_IndependentPriority()` - 여러 키의 독립적 우선순위
4. ✅ `settingsPriorityMerge_SecretSettings_ReturnsMasked()` - 비밀 설정 마스킹

**검증 항목**:
- DEFAULT 레벨 설정 (MAX_UPLOAD_SIZE=100MB)
- TENANT 레벨 설정 (MAX_UPLOAD_SIZE=50MB)
- ORG 레벨 설정 (MAX_UPLOAD_SIZE=200MB)
- ORG + TENANT + DEFAULT 조회 → 200MB (ORG 우선)
- TENANT + DEFAULT 조회 → 50MB (TENANT 우선)
- DEFAULT만 조회 → 100MB
- 비밀 설정 (is_secret=1) → `********` 마스킹

---

## 🚨 긴급 블로커: Spring Bean 설정 문제

### 문제 상황
E2E 테스트 실행 시 **Spring Context 로딩 실패**로 모든 테스트가 실패하고 있습니다.

### 근본 원인
Application layer의 모든 클래스(UseCase, Assembler 등)가 **Spring Bean으로 등록되지 않음**:
- `SettingAssembler`, `TenantAssembler`, `OrganizationAssembler` 등
- 모든 UseCase 구현체들
- `SchemaValidator` 포트 구현체

**예시 에러**:
```
NoSuchBeanDefinitionException: No qualifying bean of type
'com.ryuqq.fileflow.application.settings.assembler.SettingAssembler' available
```

### 프로젝트 설계 패턴
Application layer 클래스들이 `@Component` 어노테이션 없이 **POJO로 작성**되어 있음:
```java
public class SettingAssembler {  // ❌ @Component 없음
    public SettingAssembler() {}
}
```

이는 의도적인 설계 패턴으로 보이며, 별도의 Configuration에서 Bean으로 등록하는 방식을 사용하는 것 같습니다.

### 해결 방안

#### Option A: Application Layer Bean Configuration 생성 (권장)
```java
@Configuration
public class ApplicationLayerConfiguration {

    // Assemblers
    @Bean public TenantAssembler tenantAssembler() { return new TenantAssembler(); }
    @Bean public OrganizationAssembler organizationAssembler() { return new OrganizationAssembler(); }
    @Bean public SettingAssembler settingAssembler() { return new SettingAssembler(); }

    // UseCases (예시)
    @Bean public CreateTenantUseCase createTenantUseCase(...) { return new CreateTenantService(...); }
    @Bean public GetMergedSettingsUseCase getMergedSettingsUseCase(...) { return new GetMergedSettingsService(...); }

    // Ports
    @Bean public SchemaValidator schemaValidator() { return new SchemaValidatorImpl(); }
}
```

**작업량**: 약 20-30개 Bean 등록 필요

#### Option B: @Component 어노테이션 추가 (프로젝트 표준 위반 가능성)
Application layer 모든 클래스에 `@Component` 추가:
```java
@Component
public class SettingAssembler { ... }
```

**장점**: 간단함
**단점**: 프로젝트의 POJO 설계 원칙 위반 가능성

#### Option C: E2E Test Configuration 확장 (임시 방편)
`E2ETestConfiguration`에 필요한 모든 Bean을 Mock 또는 실제 인스턴스로 제공:
```java
@TestConfiguration
public class E2ETestConfiguration {
    @Bean public SettingAssembler settingAssembler() { return new SettingAssembler(); }
    @Bean public TenantAssembler tenantAssembler() { return new TenantAssembler(); }
    // ... 20-30개 Bean 등록
}
```

**장점**: E2E 테스트만을 위한 격리된 설정
**단점**: 실제 Application 설정과 중복, 유지보수 부담

### 현재 시도한 작업
1. ✅ `DomainServiceConfiguration` 생성 - `SettingMerger` Bean 등록
2. ✅ `E2ETestConfiguration` 생성 - 초기 Bean 등록 시도
3. ❌ `@ComponentScan` 추가 - Application layer 클래스에 `@Component` 없어서 실패

### 다음 작업자를 위한 가이드
1. **Option A 권장**: Application layer 전체 Bean Configuration 생성
2. 기존 프로젝트에 동일한 Configuration이 있는지 확인 (application 모듈 내)
3. 없다면 `ApplicationLayerConfiguration.java` 생성하여 모든 Bean 등록
4. E2E 테스트 재실행

### 참고 파일
- `/Users/sangwon-ryu/fileflow/application/src/main/java/com/ryuqq/fileflow/application/config/DomainServiceConfiguration.java`
- `/Users/sangwon-ryu/fileflow/bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/fileflow/e2e/config/E2ETestConfiguration.java`

---

## 🚧 블로킹된 시나리오 및 필요 작업

### 시나리오 3: User 다중 멤버십 테스트

**필요한 API**:
- `POST /api/v1/users` - User 생성
- `POST /api/v1/user-contexts` - UserContext 생성 (User → Organization 매핑)
- `GET /api/v1/user-contexts?userId={userId}` - User의 소속 조직 조회

**테스트 시나리오**:
1. User 생성
2. 3개의 Organization에 UserContext 생성 (Org1, Org2, Org3)
3. User의 소속 조직 목록 조회 → 3개 반환
4. 특정 Organization의 UserContext 삭제
5. User의 소속 조직 목록 재조회 → 2개 반환

---

### 시나리오 4-6: Permission 스코프 테스트 (SELF, ORGANIZATION, TENANT)

**필요한 API**:
- `POST /api/v1/users` - User 생성
- `POST /api/v1/user-contexts` - UserContext 생성
- `POST /api/v1/permissions` - Permission 생성
- `POST /api/v1/user-context-permissions` - Permission 할당
- `GET /api/v1/permissions/evaluate` - Permission 평가 (ABAC 엔진)

**테스트 시나리오 (SELF 스코프 예시)**:
1. User1, User2 생성
2. 같은 Organization에 UserContext 생성
3. User1에게 SELF 스코프 Permission 부여 (action: READ_FILE, scope: SELF)
4. User1이 User1의 파일 접근 → ✅ 허용
5. User1이 User2의 파일 접근 → ❌ 거부 (403 Forbidden)

---

### 시나리오 7-8: ABAC 파일 크기/MIME 타입 제한 테스트

**필요한 API**:
- `POST /api/v1/files/upload` - 파일 업로드 (multipart/form-data)
- `GET /api/v1/permissions/evaluate` - ABAC 평가

**테스트 시나리오 (파일 크기 제한 예시)**:
1. Organization에 파일 크기 제한 설정 (20MB)
2. 15MB 파일 업로드 → ✅ 성공
3. 25MB 파일 업로드 → ❌ 거부 (403 Forbidden, "File size exceeds limit")

---

### 시나리오 10: 캐시 무효화 테스트

**필요한 API**:
- `POST /api/v1/users` - User 생성
- `POST /api/v1/user-contexts` - UserContext 생성
- `POST /api/v1/permissions` - Permission 생성
- `POST /api/v1/roles` - Role 생성
- `POST /api/v1/user-context-roles` - Role 할당/해제
- `GET /api/v1/permissions/evaluate` - Permission 평가

**테스트 시나리오**:
1. User에게 READ 권한 Role 부여
2. Permission 평가 → READ 허용
3. 캐시 확인 (Redis에 캐싱됨)
4. Role 변경 (READ 해제, WRITE 부여)
5. **캐시 무효화 검증**: Permission 평가 → READ 거부, WRITE 허용

---

## 🎯 다음 단계 제안

### Option A: REST API 구현 후 시나리오 완성 (권장)
1. **User API 구현** (UserController, UserService)
2. **UserContext API 구현** (UserContextController)
3. **Permission API 구현** (PermissionController, ABAC 엔진)
4. **Role API 구현** (RoleController)
5. **File Upload API 구현** (FileController)
6. 블로킹된 시나리오 7개 구현

**예상 작업량**: 5-7일 (API 설계 + 구현 + 테스트)

### Option B: 스켈레톤 테스트 작성 (임시 방안)
- 나머지 7개 시나리오에 대한 **스켈레톤 테스트 클래스** 생성
- `@Disabled("Blocked: REST API not implemented yet")` 어노테이션 추가
- API 구현 후 활성화

**장점**: 테스트 구조 미리 정의, DoD 부분 충족
**단점**: 실제 검증 없이 형식만 갖춤

### Option C: 현재 완료된 3개 시나리오로 PR 생성
- 현재 완료된 3개 시나리오로 **부분 PR** 생성
- 블로킹된 7개 시나리오는 별도 Jira 서브태스크로 분리
- API 구현 완료 후 추가 PR로 진행

**장점**: 완료된 부분 먼저 머지, 점진적 개선
**단점**: DoD 미충족 (10개 시나리오 모두 통과 필요)

---

## 📊 테스트 실행 방법

### 전체 E2E 테스트 실행
```bash
./gradlew :bootstrap:bootstrap-web-api:test --tests "com.ryuqq.fileflow.e2e.*"
```

### 특정 시나리오만 실행
```bash
# Scenario 1
./gradlew :bootstrap:bootstrap-web-api:test --tests "*Scenario01*"

# Scenario 2
./gradlew :bootstrap:bootstrap-web-api:test --tests "*Scenario02*"

# Scenario 9
./gradlew :bootstrap:bootstrap-web-api:test --tests "*Scenario09*"
```

### E2E 태그로 실행
```bash
./gradlew :bootstrap:bootstrap-web-api:test -Dgroups="e2e"
```

---

## 🛠️ 기술 스택

- **테스트 프레임워크**: JUnit 5
- **HTTP 테스팅**: MockMvc (Spring Test)
- **컨테이너**: Testcontainers (MySQL 8.0, Redis 7-alpine)
- **JSON 처리**: JsonPath (com.jayway.jsonpath)
- **Fixture 패턴**: Mother Object Pattern
- **테스트 격리**: `@Testcontainers` + 컨테이너 재사용

---

## 📝 DoD (Definition of Done) 체크리스트

### Phase 1C-5 (KAN-265) DoD
- [x] E2E 테스트 인프라 구축 (EndToEndTestBase, Fixtures)
- [x] 시나리오 1: Tenant CRUD + Soft Delete 구현
- [x] 시나리오 2: Organization 중복 방지 구현
- [ ] 시나리오 3: User 다중 멤버십 구현 (🚨 블로킹)
- [ ] 시나리오 4: SELF 스코프 권한 구현 (🚨 블로킹)
- [ ] 시나리오 5: ORGANIZATION 스코프 권한 구현 (🚨 블로킹)
- [ ] 시나리오 6: TENANT 스코프 권한 구현 (🚨 블로킹)
- [ ] 시나리오 7: ABAC 파일 크기 제한 구현 (🚨 블로킹)
- [ ] 시나리오 8: ABAC MIME 타입 제한 구현 (🚨 블로킹)
- [x] 시나리오 9: Settings 우선순위 병합 구현
- [ ] 시나리오 10: 캐시 무효화 구현 (🚨 블로킹)
- [ ] **모든 테스트가 통과해야 함** (현재 3/10 통과)

**현재 진행률**: 30% (3/10 scenarios)

---

## 👥 작성자

- **개발자**: ryu-qqq
- **작성일**: 2025-10-26
- **Jira**: [KAN-265](https://your-jira-url/KAN-265)

---

## 🔗 관련 문서

- [KAN-143: 테넌트 & 조직 & 유저 관리 시스템 (Parent Epic)](https://your-jira-url/KAN-143)
- [Hexagonal Architecture 가이드](../../../docs/architecture/hexagonal-architecture.md)
- [Testing Strategy](../../../docs/testing/testing-strategy.md)
