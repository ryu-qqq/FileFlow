# TestFixture 패턴 (전체 레이어 공통)

## 개요

모든 레이어(Domain, Application, REST API, Persistence)의 테스트 코드는 **TestFixture 패턴**을 사용하여 테스트 객체를 생성해야 합니다.

---

## TestFixture란?

TestFixture는 테스트에서 사용할 객체를 쉽게 생성하기 위한 **Factory 클래스**입니다.

### 이점

1. **테스트 코드 간소화**: 복잡한 객체 생성 로직을 재사용
2. **가독성 향상**: `ExampleDomainFixture.create()` 한 줄로 객체 생성
3. **유지보수 용이**: Fixture 수정 시 모든 테스트에 자동 반영
4. **기본값 제공**: Given 단계에서 불필요한 세부사항 숨김

---

## 네이밍 규칙 (Zero-Tolerance)

### 1. 클래스명: `*Fixture`

모든 Fixture 클래스는 `Fixture` 접미사를 **필수로** 사용해야 합니다.

```java
// ✅ 올바른 네이밍
ExampleDomainFixture.java
CreateExampleCommandFixture.java
ExampleApiRequestFixture.java
ExampleJpaEntityFixture.java

// ❌ 잘못된 네이밍
ExampleFactory.java      // Factory는 금지
ExampleBuilder.java      // Builder는 금지
ExampleTestData.java     // TestData는 금지
TestExample.java         // Test 접두사는 금지
```

### 2. 메서드명: `create*()`

Fixture 메서드는 `create`로 시작해야 합니다.

```java
// ✅ 올바른 메서드명
create()                    // 기본값으로 생성
createWithMessage(String)   // 특정 값 지정
createWithId(Long, String)  // 여러 값 지정
createMultiple(int)         // 여러 개 생성

// ❌ 잘못된 메서드명
build()                     // build는 금지
of()                        // of는 금지 (Domain 객체 전용)
example()                   // 타입명만 사용 금지
getExample()                // get 접두사 금지
```

---

## 구조

### 1. Gradle testFixtures 소스셋 사용

각 모듈은 `testFixtures` 소스셋을 사용하여 Fixture 클래스를 관리합니다.

```gradle
// build.gradle.kts
plugins {
    `java-library`
    `java-test-fixtures`  // ← 필수
}

dependencies {
    // TestFixtures는 다른 모듈의 testFixtures 재사용 가능
    testFixturesApi(project(":domain"))
    testFixturesApi(testFixtures(project(":domain")))
}
```

### 2. 디렉토리 구조

```
domain/
└── src/
    ├── main/java/com/ryuqq/domain/example/
    │   └── ExampleDomain.java
    ├── test/java/com/ryuqq/domain/example/
    │   └── ExampleDomainTest.java
    └── testFixtures/java/com/ryuqq/domain/example/fixture/
        └── ExampleDomainFixture.java  ← Fixture 클래스

application/
└── src/
    └── testFixtures/java/com/ryuqq/application/example/fixture/
        ├── CreateExampleCommandFixture.java
        └── ExampleResponseFixture.java

adapter-in/rest-api/
└── src/
    └── testFixtures/java/com/ryuqq/adapter/in/rest/example/fixture/
        ├── ExampleApiRequestFixture.java
        └── ExampleApiResponseFixture.java

adapter-out/persistence-mysql/
└── src/
    └── testFixtures/java/com/ryuqq/adapter/out/persistence/example/fixture/
        └── ExampleJpaEntityFixture.java
```

---

## Fixture 클래스 작성 패턴

### 기본 템플릿

```java
package com.ryuqq.domain.example.fixture;

import com.ryuqq.domain.example.ExampleDomain;

/**
 * ExampleDomain 테스트 Fixture
 *
 * <p>테스트에서 ExampleDomain 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * ExampleDomain example = ExampleDomainFixture.create();
 * ExampleDomain example = ExampleDomainFixture.createWithMessage("Custom");
 * }</pre>
 */
public class ExampleDomainFixture {

    /**
     * 기본값으로 ExampleDomain 생성
     */
    public static ExampleDomain create() {
        return createWithMessage("Test Message");
    }

    /**
     * 특정 메시지로 ExampleDomain 생성
     */
    public static ExampleDomain createWithMessage(String message) {
        // 객체 생성 로직
        return ExampleDomain.create(ExampleContent.of(message));
    }

    /**
     * ID 포함하여 생성 (조회 시나리오용)
     */
    public static ExampleDomain createWithId(Long id, String message) {
        // ID를 가진 객체 생성 로직
        return ExampleDomain.of(ExampleId.of(id), ExampleContent.of(message), ...);
    }

    // Private 생성자 - 인스턴스화 방지
    private ExampleDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

### 필수 요소

1. **static 메서드**: 모든 Fixture 메서드는 `static`이어야 함
2. **create*() 네이밍**: `create`로 시작하는 메서드명
3. **Private 생성자**: 인스턴스화 방지
4. **Javadoc**: 사용 예시 포함

---

## 레이어별 Fixture 가이드

### 1. Domain Layer Fixture

**목적**: Domain 객체 (Aggregate, Entity, Value Object) 생성
**위치**: `domain/src/testFixtures/java/com/ryuqq/domain/example/fixture/`

```java
// ExampleDomainFixture.java (✅ 구현 완료)
public class ExampleDomainFixture {
    public static ExampleDomain create() {
        return createWithMessage("Test Message");
    }

    public static ExampleDomain createWithMessage(String message) {
        return ExampleDomain.create(message);
    }

    public static ExampleDomain createWithId(Long id, String message) {
        LocalDateTime now = LocalDateTime.now();
        return ExampleDomain.of(id, message, "ACTIVE", now, now);
    }

    public static ExampleDomain[] createMultiple(int count) { ... }
    public static ExampleDomain[] createMultipleWithId(long startId, int count) { ... }

    private ExampleDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

**파일 경로**:
- `domain/src/testFixtures/java/com/ryuqq/domain/example/fixture/ExampleDomainFixture.java`

---

### 2. Application Layer Fixture

**목적**: Command, Query, Response DTO 생성
**위치**: `application/src/testFixtures/java/com/ryuqq/application/example/fixture/`

```java
// CreateExampleCommandFixture.java (✅ 구현 완료)
public class CreateExampleCommandFixture {
    public static CreateExampleCommand create() {
        return createWithMessage("Test Message");
    }

    public static CreateExampleCommand createWithMessage(String message) {
        return new CreateExampleCommand(message);
    }

    public static CreateExampleCommand[] createMultiple(int count) { ... }

    private CreateExampleCommandFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}

// ExampleResponseFixture.java (✅ 구현 완료)
public class ExampleResponseFixture {
    public static ExampleResponse create() {
        return createWithIdAndMessage(1L, "Test Message");
    }

    public static ExampleResponse createWithId(Long id) { ... }
    public static ExampleResponse createWithMessage(String message) { ... }
    public static ExampleResponse createWithIdAndMessage(Long id, String message) { ... }
    public static ExampleResponse[] createMultiple(long startId, int count) { ... }

    private ExampleResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

**파일 경로**:
- `application/src/testFixtures/java/com/ryuqq/application/example/fixture/CreateExampleCommandFixture.java`
- `application/src/testFixtures/java/com/ryuqq/application/example/fixture/ExampleResponseFixture.java`

---

### 3. REST API Layer Fixture

**목적**: ApiRequest, ApiResponse DTO 생성
**위치**: `adapter-in/rest-api/src/testFixtures/java/com/ryuqq/adapter/in/rest/example/fixture/`

```java
// ExampleApiRequestFixture.java (✅ 구현 완료)
public class ExampleApiRequestFixture {
    public static ExampleApiRequest create() {
        return createWithMessage("Test Message");
    }

    public static ExampleApiRequest createWithMessage(String message) {
        return new ExampleApiRequest(message);
    }

    public static ExampleApiRequest[] createMultiple(int count) { ... }

    private ExampleApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}

// ExampleApiResponseFixture.java (✅ 구현 완료)
public class ExampleApiResponseFixture {
    public static ExampleApiResponse create() {
        return createWithMessage("Test Message");
    }

    public static ExampleApiResponse createWithMessage(String message) {
        return new ExampleApiResponse(message);
    }

    public static ExampleApiResponse[] createMultiple(int count) { ... }

    private ExampleApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

**파일 경로**:
- `adapter-in/rest-api/src/testFixtures/java/com/ryuqq/adapter/in/rest/example/fixture/ExampleApiRequestFixture.java`
- `adapter-in/rest-api/src/testFixtures/java/com/ryuqq/adapter/in/rest/example/fixture/ExampleApiResponseFixture.java`

---

### 4. Persistence Layer Fixture

**목적**: JPA Entity, QueryDSL 테스트용 객체 생성
**위치**: `adapter-out/persistence-mysql/src/testFixtures/java/com/ryuqq/adapter/out/persistence/example/fixture/`

```java
// ExampleJpaEntityFixture.java (✅ 구현 완료)
public class ExampleJpaEntityFixture {
    public static ExampleJpaEntity create() {
        return createWithMessage("Test Message");
    }

    public static ExampleJpaEntity createWithMessage(String message) {
        LocalDateTime now = LocalDateTime.now();
        return new ExampleJpaEntity(null, message, ExampleStatus.ACTIVE, now, now);
    }

    public static ExampleJpaEntity createWithId(Long id, String message) {
        LocalDateTime now = LocalDateTime.now();
        return new ExampleJpaEntity(id, message, ExampleStatus.ACTIVE, now, now);
    }

    public static ExampleJpaEntity createWithIdAndStatus(Long id, String message, ExampleStatus status) { ... }
    public static List<ExampleJpaEntity> createMultiple(int count) { ... }
    public static List<ExampleJpaEntity> createMultipleWithId(long startId, int count) { ... }

    private ExampleJpaEntityFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

**파일 경로**:
- `adapter-out/persistence-mysql/src/testFixtures/java/com/ryuqq/adapter/out/persistence/example/fixture/ExampleJpaEntityFixture.java`

---

## 사용 예시

### Before (Fixture 없이)

```java
@Test
void testCreateExample() {
    // Given: 복잡한 객체 생성 로직이 테스트마다 반복됨
    ExampleId id = ExampleId.of(1L);
    ExampleContent content = ExampleContent.of("Test Message");
    LocalDateTime now = LocalDateTime.now();
    ExampleDomain example = ExampleDomain.of(id, content, now, now);

    // When
    example.updateMessage("New Message");

    // Then
    assertEquals("New Message", example.getMessage());
}
```

### After (Fixture 사용)

```java
@Test
void testCreateExample() {
    // Given: Fixture로 간단하게 객체 생성
    ExampleDomain example = ExampleDomainFixture.createWithId(1L, "Test Message");

    // When
    example.updateMessage("New Message");

    // Then
    assertEquals("New Message", example.getMessage());
}
```

---

## ArchUnit 검증

**위치**: `bootstrap/bootstrap-web-api/src/test/java/.../architecture/CommonTestingRulesTest.java`

```java
@DisplayName("공통 테스트 규칙 검증 (전체 모듈)")
class CommonTestingRulesTest {

    private JavaClasses allModulesClasses;

    @BeforeEach
    void setUp() {
        // 모든 모듈의 testFixtures 스캔
        allModulesClasses = new ClassFileImporter()
            .importPackages(
                "com.ryuqq.domain",
                "com.ryuqq.application",
                "com.ryuqq.adapter.in.rest",
                "com.ryuqq.adapter.out.persistence"
            );
    }

    @Test
    @DisplayName("Fixture 클래스는 Fixture 접미사를 가져야 함")
    void fixtureClassesShouldHaveFixtureSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("..fixture..")
            .and().areNotMemberClasses()
            .and().areNotInterfaces()
            .and().areNotEnums()
            .should().haveSimpleNameEndingWith("Fixture")
            .because("Fixture 클래스는 Fixture 접미사를 사용해야 합니다. " +
                     "예: ExampleDomainFixture, CreateExampleCommandFixture");

        rule.check(allModulesClasses);
    }

    @Test
    @DisplayName("Fixture 접미사를 가진 클래스는 fixture 패키지에 위치해야 함")
    void classesWithFixtureSuffixShouldBeInFixturePackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Fixture")
            .and().areNotMemberClasses()
            .should().resideInAPackage("..fixture..")
            .because("Fixture 클래스는 반드시 fixture 패키지에 위치해야 합니다. " +
                     "예: com.ryuqq.domain.example.fixture");

        rule.check(allModulesClasses);
    }

    @Test
    @DisplayName("Fixture 클래스는 create로 시작하는 static 메서드를 가져야 함")
    void fixtureClassesShouldHaveCreateMethod() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Fixture")
            .should(haveStaticMethodWithNameStartingWith("create"))
            .because("Fixture 클래스는 create*() 형태의 static 메서드를 제공해야 합니다. " +
                     "예: create(), createWithMessage(), createWithId()");

        rule.check(allModulesClasses);
    }

    @Test
    @DisplayName("Fixture 클래스의 생성자는 private이어야 함")
    void fixtureClassesShouldHavePrivateConstructor() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Fixture")
            .should(haveOnlyPrivateConstructors())
            .because("Fixture 클래스는 Utility 클래스이므로 인스턴스화를 방지해야 합니다. " +
                     "생성자를 private으로 선언하세요.");

        rule.check(allModulesClasses);
    }
}
```

### 검증 실행

```bash
# CommonTestingRulesTest 실행
./gradlew :bootstrap:bootstrap-web-api:test --tests CommonTestingRulesTest

# 결과: 4개 테스트 모두 통과 ✅
# - Fixture 클래스 Fixture 접미사 검증
# - Fixture 접미사 클래스 패키지 위치 검증
# - Fixture 클래스 create*() 메서드 검증
# - Fixture 클래스 private 생성자 검증
```

---

## 체크리스트

### Fixture 클래스 작성 체크리스트

- [ ] 클래스명에 `Fixture` 접미사 사용
- [ ] `testFixtures` 소스셋에 위치
- [ ] `fixture` 패키지에 위치 (예: `com.ryuqq.domain.example.fixture`)
- [ ] 모든 메서드는 `static`으로 선언
- [ ] 기본 생성 메서드 `create()` 제공
- [ ] 커스터마이징 메서드 `createWith*()` 제공
- [ ] Private 생성자로 인스턴스화 방지
- [ ] Javadoc에 사용 예시 포함

---

## 이점

### 1. 테스트 코드 간소화

Fixture를 사용하면 Given 단계가 매우 간결해집니다.

```java
// Before: 10줄
ExampleId id = ExampleId.of(1L);
ExampleContent content = ExampleContent.of("Test");
LocalDateTime now = LocalDateTime.now();
ExampleDomain example = ExampleDomain.of(id, content, now, now);

// After: 1줄
ExampleDomain example = ExampleDomainFixture.createWithId(1L, "Test");
```

### 2. 기본값 관리

테스트에서 중요하지 않은 값은 Fixture가 기본값으로 제공합니다.

```java
// 메시지만 중요한 테스트
ExampleDomain example = ExampleDomainFixture.createWithMessage("Important");

// ID와 메시지가 중요한 테스트
ExampleDomain example = ExampleDomainFixture.createWithId(123L, "Important");
```

### 3. 변경 영향 최소화

Domain 객체 생성자가 변경되어도 Fixture만 수정하면 됩니다.

```java
// Domain 객체에 status 필드 추가
public class ExampleDomain {
    // 새 필드 추가: ExampleStatus status
}

// Fixture만 수정
public class ExampleDomainFixture {
    public static ExampleDomain create() {
        return createWithMessage("Test Message");
    }

    public static ExampleDomain createWithMessage(String message) {
        ExampleContent content = ExampleContent.of(message);
        ExampleStatus status = ExampleStatus.ACTIVE;  // ← 기본값 추가
        return ExampleDomain.create(content, status);
    }
}

// 모든 테스트 코드는 수정 불필요!
```

---

## 요약

| 항목 | 규칙 |
|------|------|
| **클래스명** | `*Fixture` 접미사 필수 |
| **메서드명** | `create*()` 접두사 필수 |
| **위치** | `src/testFixtures/java/.../fixture/` |
| **메서드 타입** | `static` 필수 |
| **생성자** | Private (인스턴스화 방지) |
| **Javadoc** | 사용 예시 포함 필수 |
| **ArchUnit 검증** | `CommonTestingRulesTest`에서 자동 검증 |

**✅ TestFixture 패턴은 모든 레이어에서 필수이며, ArchUnit으로 자동 검증됩니다. (Zero-Tolerance)**
