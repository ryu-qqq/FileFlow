# Test Fixtures Guide

## 📚 개요

이 프로젝트는 **Gradle TestFixtures** 패턴을 사용하여 테스트에서 객체를 쉽게 생성할 수 있도록 합니다.

TestFixtures는 `src/testFixtures/` 디렉토리에 위치하며, 다른 모듈에서 `testFixtures()` 의존성으로 재사용 가능합니다.

---

## 🏗️ Layer별 Fixture 구조

### 1️⃣ Domain Layer Fixtures

**위치**: `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/iam/`

#### OrganizationDomainFixture

```java
// 기본 생성 (신규, ID 없음)
Organization org = OrganizationDomainFixture.create();

// 특정 이름으로 생성
Organization org = OrganizationDomainFixture.createWithName("Engineering");

// ID 포함 생성 (조회 시나리오)
Organization org = OrganizationDomainFixture.createWithId(1L, 1L, "Engineering");

// 여러 개 생성
Organization[] orgs = OrganizationDomainFixture.createMultiple(10);
Organization[] orgs = OrganizationDomainFixture.createMultipleWithId(1L, 10);

// 특정 상태 생성
Organization inactive = OrganizationDomainFixture.createInactive(1L, 1L);
Organization deleted = OrganizationDomainFixture.createDeleted(1L, 1L);
```

#### TenantDomainFixture

```java
// 기본 생성 (신규, ID 없음)
Tenant tenant = TenantDomainFixture.create();

// 특정 이름으로 생성
Tenant tenant = TenantDomainFixture.createWithName("My Tenant");

// ID 포함 생성
Tenant tenant = TenantDomainFixture.createWithId(1L, "My Tenant");

// 여러 개 생성
Tenant[] tenants = TenantDomainFixture.createMultiple(10);

// 특정 상태 생성
Tenant suspended = TenantDomainFixture.createSuspended(1L);
Tenant deleted = TenantDomainFixture.createDeleted(1L);
```

---

### 2️⃣ Application Layer Fixtures

**위치**: `application/src/testFixtures/java/com/ryuqq/fileflow/application/iam/`

#### Command Fixtures

```java
// CreateOrganizationCommand
CreateOrganizationCommand cmd = CreateOrganizationCommandFixture.create();
CreateOrganizationCommand cmd = CreateOrganizationCommandFixture.createWith(1L, "ORG001", "Engineering");
CreateOrganizationCommand cmd = CreateOrganizationCommandFixture.createWithTenantId(123L);

// CreateTenantCommand
CreateTenantCommand cmd = CreateTenantCommandFixture.create();
CreateTenantCommand cmd = CreateTenantCommandFixture.createWith("My Tenant");
```

#### Query Fixtures

```java
// GetOrganizationQuery
GetOrganizationQuery query = GetOrganizationQueryFixture.create();
GetOrganizationQuery query = GetOrganizationQueryFixture.createWith(123L);

// GetTenantQuery
GetTenantQuery query = GetTenantQueryFixture.create();
GetTenantQuery query = GetTenantQueryFixture.createWith(123L);
```

---

### 3️⃣ REST API Layer Fixtures

**위치**: `adapter-in/rest-api/src/testFixtures/java/com/ryuqq/fileflow/adapter/rest/iam/`

#### Request Fixtures

```java
// CreateOrganizationApiRequest
CreateOrganizationApiRequest req = CreateOrganizationApiRequestFixture.create();
CreateOrganizationApiRequest req = CreateOrganizationApiRequestFixture.createWith(1L, "ORG001", "Engineering");

// CreateTenantApiRequest
CreateTenantApiRequest req = CreateTenantApiRequestFixture.create();
CreateTenantApiRequest req = CreateTenantApiRequestFixture.createWith("My Tenant");
```

#### Response Fixtures

```java
// OrganizationApiResponse
OrganizationApiResponse res = OrganizationApiResponseFixture.create();
OrganizationApiResponse res = OrganizationApiResponseFixture.createWithId(123L);
OrganizationApiResponse res = OrganizationApiResponseFixture.createWith(1L, 1L, "ORG001", "Engineering");

// TenantApiResponse
TenantApiResponse res = TenantApiResponseFixture.create();
TenantApiResponse res = TenantApiResponseFixture.createWithId(123L);
TenantApiResponse res = TenantApiResponseFixture.createWith(1L, "My Tenant", "ACTIVE");
```

---

### 4️⃣ Persistence Layer Fixtures

**위치**: `adapter-out/persistence-mysql/src/testFixtures/java/.../iam/`

#### JPA Entity Fixtures

```java
// OrganizationJpaEntity (신규 - ID 없음)
OrganizationJpaEntity entity = OrganizationJpaEntityFixture.create();
OrganizationJpaEntity entity = OrganizationJpaEntityFixture.createWithTenantId(123L);
OrganizationJpaEntity entity = OrganizationJpaEntityFixture.createWith(1L, "ORG001", "Engineering");

// OrganizationJpaEntity (ID 포함 - 조회 시나리오)
OrganizationJpaEntity entity = OrganizationJpaEntityFixture.createWithId(123L);

// 여러 개 생성
OrganizationJpaEntity[] entities = OrganizationJpaEntityFixture.createMultipleWithId(1L, 10);

// TenantJpaEntity
TenantJpaEntity entity = TenantJpaEntityFixture.create();
TenantJpaEntity entity = TenantJpaEntityFixture.createWithId(123L);
TenantJpaEntity entity = TenantJpaEntityFixture.createWith(1L, "My Tenant");
TenantJpaEntity[] entities = TenantJpaEntityFixture.createMultipleWithId(1L, 10);

// 특정 상태
TenantJpaEntity suspended = TenantJpaEntityFixture.createSuspended(1L);
```

---

## 📦 Gradle 설정

각 모듈의 `build.gradle.kts`에 다음과 같이 설정되어 있습니다:

### 1. TestFixtures 플러그인 추가

```kotlin
plugins {
    java
    `java-test-fixtures`  // ← 추가
}
```

### 2. 다른 모듈에서 사용

```kotlin
dependencies {
    // Domain TestFixtures 사용
    testImplementation(testFixtures(project(":domain")))

    // Application TestFixtures 사용
    testImplementation(testFixtures(project(":application")))

    // REST API TestFixtures 사용
    testImplementation(testFixtures(project(":adapter-in:rest-api")))

    // Persistence TestFixtures 사용
    testImplementation(testFixtures(project(":adapter-out:persistence-mysql")))
}
```

---

## 🎯 실전 사용 예시

### Example 1: Domain 테스트

```java
@Test
void testOrganizationDomain() {
    // Given
    Organization org = OrganizationDomainFixture.create();

    // When
    org.updateName(OrgCode.of("NEW-ORG"), "New Name");

    // Then
    assertThat(org.getName()).isEqualTo("New Name");
}
```

### Example 2: UseCase 테스트

```java
@Test
void testCreateOrganizationUseCase() {
    // Given
    CreateOrganizationCommand command = CreateOrganizationCommandFixture.create();

    // When
    OrganizationResponse response = createOrganizationUseCase.execute(command);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.organizationId()).isNotNull();
}
```

### Example 3: Controller 테스트

```java
@Test
void testCreateOrganizationApi() throws Exception {
    // Given
    CreateOrganizationApiRequest request = CreateOrganizationApiRequestFixture.create();

    // When & Then
    mockMvc.perform(post("/api/v1/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.organizationId").exists());
}
```

### Example 4: Repository 테스트

```java
@Test
void testOrganizationRepository() {
    // Given
    OrganizationJpaEntity entity = OrganizationJpaEntityFixture.create();

    // When
    OrganizationJpaEntity saved = repository.save(entity);

    // Then
    assertThat(saved.getId()).isNotNull();
}
```

---

## ✅ Fixture 작성 규칙

### 1. 네이밍 규칙

- **클래스명**: `{Entity}Fixture` (예: `OrganizationDomainFixture`)
- **패키지**: `{원본패키지}.fixture`
- **메서드**:
  - `create()` - 기본값으로 생성
  - `createWith*()` - 특정 값으로 생성
  - `createWithId()` - ID 포함 생성 (조회 시나리오)
  - `createMultiple()` - 여러 개 생성

### 2. 메서드 종류

#### 기본 생성 메서드
```java
public static {Entity} create() {
    // 기본값으로 생성
}
```

#### 커스터마이징 메서드
```java
public static {Entity} createWith({Type} field) {
    // 특정 값으로 생성
}

public static {Entity} createWithId(Long id, {Type} field) {
    // ID 포함 생성
}
```

#### 대량 생성 메서드
```java
public static {Entity}[] createMultiple(int count) {
    // 여러 개 생성 (ID 없음)
}

public static {Entity}[] createMultipleWithId(long startId, int count) {
    // 여러 개 생성 (ID 포함)
}
```

### 3. Private 생성자

```java
// Utility 클래스이므로 인스턴스화 방지
private {Entity}Fixture() {
    throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
}
```

---

## 📚 참고

- [Gradle TestFixtures 문서](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures)
- [claude-spring-standards TestFixtures 워크플로우](../.windsurf/workflows/create-test-fixtures.md)

---

## 🎓 핵심 개념

### TestFixtures vs Test 디렉토리

| 구분 | TestFixtures (`src/testFixtures/`) | Test (`src/test/`) |
|------|--------------------------------------|---------------------|
| **목적** | 재사용 가능한 테스트 객체 생성 | 실제 테스트 코드 작성 |
| **공유** | 다른 모듈에서 사용 가능 | 해당 모듈에서만 사용 |
| **의존성** | `testFixtures(project(":module"))` | `testImplementation(project(":module"))` |
| **예시** | `OrganizationDomainFixture.java` | `OrganizationTest.java` |

### 언제 Fixture를 만드나?

- ✅ **반복적으로 생성되는 테스트 객체**가 있을 때
- ✅ **여러 테스트에서 동일한 패턴으로 객체를 생성**할 때
- ✅ **다른 모듈에서도 해당 객체가 필요**할 때
- ❌ 한 번만 사용되는 객체는 Fixture로 만들지 않음
- ❌ 테스트마다 완전히 다른 값이 필요한 경우 Fixture보다 직접 생성

---

**✅ 이 가이드를 참고하여 일관된 TestFixture를 작성하세요!**
