---
description: Layer별 Test Fixture 생성 (Domain, Application, REST API, Persistence)
---

# Create Test Fixtures

**🎯 역할**: Layer별 Test Fixture 클래스 자동 생성

**📋 패턴**: TestFixtures (Gradle testFixtures source set)

---

## 📚 Test Fixture란?

테스트에서 객체를 쉽게 생성하기 위한 **Factory 클래스**입니다.

### 핵심 개념
- **위치**: `src/testFixtures/java/` (별도 source set)
- **공유**: 다른 모듈에서 `testFixtures` 의존성으로 사용 가능
- **네이밍**: `*Fixture` 접미사 필수

---

## 🏗️ Layer별 Fixture 구조

### 1️⃣ Domain Layer Fixture

**위치**: `domain/src/testFixtures/java/{package}/fixture/`

**파일명**: `{Aggregate}DomainFixture.java`

**템플릿**:
```java
package {package}.fixture;

import {package}.{Aggregate}Domain;
import {package}.{Aggregate}Id;
import java.time.LocalDateTime;

/**
 * {Aggregate}Domain 테스트 Fixture
 *
 * <p>테스트에서 {Aggregate}Domain 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>네이밍 규칙:</h3>
 * <ul>
 *   <li>클래스명: {@code *Fixture} 접미사 필수</li>
 *   <li>기본 생성 메서드: {@code create*()} - 기본값으로 객체 생성</li>
 *   <li>커스터마이징 메서드: {@code create*With*()} - 특정 값 지정하여 생성</li>
 * </ul>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 기본값으로 생성
 * {Aggregate}Domain aggregate = {Aggregate}DomainFixture.create();
 *
 * // 특정 값으로 생성
 * {Aggregate}Domain aggregate = {Aggregate}DomainFixture.createWith{Field}("Custom Value");
 *
 * // ID 포함하여 생성 (조회 시나리오)
 * {Aggregate}Domain aggregate = {Aggregate}DomainFixture.createWithId(123L, "Value");
 * }</pre>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see {Aggregate}Domain
 */
public class {Aggregate}DomainFixture {

    /**
     * 기본값으로 {Aggregate}Domain 생성
     *
     * @return 기본값을 가진 {Aggregate}Domain
     */
    public static {Aggregate}Domain create() {
        return createWith{MainField}("Test {MainField}");
    }

    /**
     * 특정 값으로 {Aggregate}Domain 생성
     *
     * @param {mainField} {MainField} 내용
     * @return 지정된 값을 가진 {Aggregate}Domain
     */
    public static {Aggregate}Domain createWith{MainField}(String {mainField}) {
        return {Aggregate}Domain.create({mainField});
    }

    /**
     * ID 포함하여 {Aggregate}Domain 생성 (조회 시나리오용)
     *
     * <p>영속화된 상태의 Domain 객체를 테스트할 때 사용합니다.</p>
     *
     * @param id {Aggregate} ID
     * @param {mainField} {MainField} 내용
     * @return ID를 가진 {Aggregate}Domain
     */
    public static {Aggregate}Domain createWithId(Long id, String {mainField}) {
        LocalDateTime now = LocalDateTime.now();
        return {Aggregate}Domain.of(
            id,
            {mainField},
            "ACTIVE",
            now,  // createdAt
            now   // updatedAt
        );
    }

    /**
     * 여러 개의 {Aggregate}Domain 생성 (목록 테스트용)
     *
     * @param count 생성할 개수
     * @return {Aggregate}Domain 배열
     */
    public static {Aggregate}Domain[] createMultiple(int count) {
        {Aggregate}Domain[] aggregates = new {Aggregate}Domain[count];
        for (int i = 0; i < count; i++) {
            aggregates[i] = createWith{MainField}("Test {MainField} " + (i + 1));
        }
        return aggregates;
    }

    /**
     * ID를 포함한 여러 개의 {Aggregate}Domain 생성
     *
     * @param startId 시작 ID
     * @param count 생성할 개수
     * @return {Aggregate}Domain 배열
     */
    public static {Aggregate}Domain[] createMultipleWithId(long startId, int count) {
        {Aggregate}Domain[] aggregates = new {Aggregate}Domain[count];
        for (int i = 0; i < count; i++) {
            aggregates[i] = createWithId(startId + i, "Test {MainField} " + (i + 1));
        }
        return aggregates;
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private {Aggregate}DomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### 2️⃣ Application Layer Fixture

**위치**: `application/src/testFixtures/java/{package}/fixture/`

**파일명**: `{Command/Query}Fixture.java`

**템플릿**:
```java
package {package}.fixture;

import {package}.{Command};
import java.time.LocalDateTime;

/**
 * {Command} 테스트 Fixture
 *
 * <p>테스트에서 {Command} 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see {Command}
 */
public class {Command}Fixture {

    /**
     * 기본값으로 {Command} 생성
     *
     * @return 기본값을 가진 {Command}
     */
    public static {Command} create() {
        return {Command}.builder()
            .field1("Test Value 1")
            .field2("Test Value 2")
            .build();
    }

    /**
     * 특정 값으로 {Command} 생성
     *
     * @param field1 Field 1
     * @param field2 Field 2
     * @return 지정된 값을 가진 {Command}
     */
    public static {Command} createWith(String field1, String field2) {
        return {Command}.builder()
            .field1(field1)
            .field2(field2)
            .build();
    }

    // Private 생성자
    private {Command}Fixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### 3️⃣ REST API Layer Fixture

**위치**: `adapter-in/rest-api/src/testFixtures/java/{package}/fixture/`

**파일명**: `{Entity}ApiRequestFixture.java`, `{Entity}ApiResponseFixture.java`

**템플릿 (Request)**:
```java
package {package}.fixture;

import {package}.{Entity}ApiRequest;

/**
 * {Entity}ApiRequest 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see {Entity}ApiRequest
 */
public class {Entity}ApiRequestFixture {

    public static {Entity}ApiRequest create() {
        return new {Entity}ApiRequest(
            "Test Field 1",
            "Test Field 2"
        );
    }

    public static {Entity}ApiRequest createWith(String field1, String field2) {
        return new {Entity}ApiRequest(field1, field2);
    }

    private {Entity}ApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

**템플릿 (Response)**:
```java
package {package}.fixture;

import {package}.{Entity}ApiResponse;
import java.time.LocalDateTime;

/**
 * {Entity}ApiResponse 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see {Entity}ApiResponse
 */
public class {Entity}ApiResponseFixture {

    public static {Entity}ApiResponse create() {
        return {Entity}ApiResponse.builder()
            .id(1L)
            .field1("Test Field 1")
            .field2("Test Field 2")
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static {Entity}ApiResponse createWithId(Long id) {
        return {Entity}ApiResponse.builder()
            .id(id)
            .field1("Test Field 1")
            .field2("Test Field 2")
            .createdAt(LocalDateTime.now())
            .build();
    }

    private {Entity}ApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

### 4️⃣ Persistence Layer Fixture

**위치**: `adapter-out/persistence-mysql/src/testFixtures/java/{package}/fixture/`

**파일명**: `{Entity}JpaEntityFixture.java`

**템플릿**:
```java
package {package}.fixture;

import {package}.{Entity}JpaEntity;
import java.time.LocalDateTime;

/**
 * {Entity}JpaEntity 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 * @see {Entity}JpaEntity
 */
public class {Entity}JpaEntityFixture {

    /**
     * 기본값으로 {Entity}JpaEntity 생성 (ID 없음 - 신규)
     */
    public static {Entity}JpaEntity create() {
        LocalDateTime now = LocalDateTime.now();
        return {Entity}JpaEntity.builder()
            .field1("Test Field 1")
            .field2("Test Field 2")
            .status("ACTIVE")
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * ID 포함하여 {Entity}JpaEntity 생성 (조회 시나리오)
     */
    public static {Entity}JpaEntity createWithId(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return {Entity}JpaEntity.builder()
            .id(id)
            .field1("Test Field 1")
            .field2("Test Field 2")
            .status("ACTIVE")
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * 특정 값으로 {Entity}JpaEntity 생성
     */
    public static {Entity}JpaEntity createWith(String field1, String field2) {
        LocalDateTime now = LocalDateTime.now();
        return {Entity}JpaEntity.builder()
            .field1(field1)
            .field2(field2)
            .status("ACTIVE")
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * 여러 개의 {Entity}JpaEntity 생성
     */
    public static {Entity}JpaEntity[] createMultipleWithId(long startId, int count) {
        {Entity}JpaEntity[] entities = new {Entity}JpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(startId + i);
        }
        return entities;
    }

    private {Entity}JpaEntityFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

---

## 📦 Gradle 설정

각 모듈의 `build.gradle.kts`에 testFixtures 플러그인 추가:

```kotlin
plugins {
    `java-library`
    `java-test-fixtures`  // ← TestFixtures 플러그인
}

// 다른 모듈에서 사용 시:
dependencies {
    testImplementation(testFixtures(project(":domain")))
    testImplementation(testFixtures(project(":application")))
}
```

---

## 🎯 사용 예시

### Domain 테스트
```java
@Test
void testDomainLogic() {
    // Given
    ExampleDomain example = ExampleDomainFixture.create();

    // When
    example.updateMessage("New Message");

    // Then
    assertThat(example.getMessage()).isEqualTo("New Message");
}
```

### Application 테스트
```java
@Test
void testUseCase() {
    // Given
    CreateExampleCommand command = CreateExampleCommandFixture.create();

    // When
    ExampleResponse response = createExampleUseCase.execute(command);

    // Then
    assertThat(response).isNotNull();
}
```

### REST API 테스트
```java
@Test
void testController() {
    // Given
    ExampleApiRequest request = ExampleApiRequestFixture.create();

    // When
    mockMvc.perform(post("/api/examples")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))

    // Then
        .andExpect(status().isOk());
}
```

### Persistence 테스트
```java
@Test
void testRepository() {
    // Given
    ExampleJpaEntity entity = ExampleJpaEntityFixture.create();

    // When
    ExampleJpaEntity saved = repository.save(entity);

    // Then
    assertThat(saved.getId()).isNotNull();
}
```

---

## ✅ 체크리스트

### 생성 시
- [ ] `src/testFixtures/java/` 디렉토리 생성
- [ ] `{Entity}Fixture.java` 클래스 생성
- [ ] `create()` 기본 메서드 구현
- [ ] `createWith*()` 커스터마이징 메서드 구현
- [ ] `createWithId()` 조회 시나리오용 메서드 구현
- [ ] `createMultiple()` 목록 테스트용 메서드 구현
- [ ] Javadoc 작성 (사용 예시 포함)
- [ ] Private 생성자로 인스턴스화 방지

### Gradle 설정
- [ ] `java-test-fixtures` 플러그인 추가
- [ ] 다른 모듈에서 `testFixtures()` 의존성 추가

### 네이밍 규칙
- [ ] 클래스명: `*Fixture` 접미사
- [ ] 패키지: `{package}.fixture`
- [ ] 메서드: `create*()`, `createWith*()`

---

## 🎨 Cascade 사용법

```
IntelliJ Cascade:
"Order Domain Fixture를 생성해줘"

→ Cascade가 이 워크플로우 읽고
→ domain/src/testFixtures/java/.../OrderDomainFixture.java 생성
```

---

## 📚 참고

- [Gradle TestFixtures 문서](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures)
- `/Users/sangwon-ryu/claude-spring-standards/domain/src/testFixtures/` (실제 예시)
