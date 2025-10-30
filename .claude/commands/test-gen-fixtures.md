---
description: Test Fixture 자동 생성 (Layer별: Domain, Application, REST API, Persistence)
---

# Test Fixture 자동 생성

**목적**: Layer별 Test Fixture 클래스 자동 생성 (Gradle testFixtures)

**타겟**: 모든 Layer (Domain, Application, Adapter-REST, Adapter-Persistence)

**생성 패턴**: TestFixtures pattern (Factory 클래스)

---

## 🎯 사용법

```bash
# Domain Layer Fixture
/test-gen-fixtures OrderDomain --layer domain

# Application Layer Fixture
/test-gen-fixtures CreateOrderCommand --layer application

# REST API Layer Fixture
/test-gen-fixtures OrderApiRequest --layer rest

# Persistence Layer Fixture
/test-gen-fixtures OrderJpaEntity --layer persistence
```

---

## 📚 Test Fixture란?

테스트에서 객체를 쉽게 생성하기 위한 **Factory 클래스**입니다.

### 핵심 개념
- **위치**: `src/testFixtures/java/` (별도 source set)
- **공유**: 다른 모듈에서 `testFixtures` 의존성으로 사용 가능
- **네이밍**: `*Fixture` 접미사 필수

---

## ✅ 자동 생성되는 메서드

### Domain Layer Fixture

```java
package {package}.fixture;

/**
 * {Aggregate}Domain 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 */
public class {Aggregate}DomainFixture {

    /**
     * 기본값으로 생성
     */
    public static {Aggregate}Domain create() {
        return createWith{MainField}("Test {MainField}");
    }

    /**
     * 특정 값으로 생성
     */
    public static {Aggregate}Domain createWith{MainField}(String {mainField}) {
        return {Aggregate}Domain.create({mainField});
    }

    /**
     * ID 포함하여 생성 (조회 시나리오용)
     */
    public static {Aggregate}Domain createWithId(Long id, String {mainField}) {
        LocalDateTime now = LocalDateTime.now();
        return {Aggregate}Domain.of(id, {mainField}, "ACTIVE", now, now);
    }

    /**
     * 여러 개 생성 (목록 테스트용)
     */
    public static {Aggregate}Domain[] createMultiple(int count) {
        {Aggregate}Domain[] aggregates = new {Aggregate}Domain[count];
        for (int i = 0; i < count; i++) {
            aggregates[i] = createWith{MainField}("Test {MainField} " + (i + 1));
        }
        return aggregates;
    }

    /**
     * ID를 포함한 여러 개 생성
     */
    public static {Aggregate}Domain[] createMultipleWithId(long startId, int count) {
        {Aggregate}Domain[] aggregates = new {Aggregate}Domain[count];
        for (int i = 0; i < count; i++) {
            aggregates[i] = createWithId(startId + i, "Test {MainField} " + (i + 1));
        }
        return aggregates;
    }

    // Private 생성자 - Utility 클래스
    private {Aggregate}DomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

### Application Layer Fixture

```java
package {package}.fixture;

/**
 * {Command} 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 */
public class {Command}Fixture {

    public static {Command} create() {
        return {Command}.builder()
            .field1("Test Value 1")
            .field2("Test Value 2")
            .build();
    }

    public static {Command} createWith(String field1, String field2) {
        return {Command}.builder()
            .field1(field1)
            .field2(field2)
            .build();
    }

    private {Command}Fixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

### Persistence Layer Fixture

```java
package {package}.fixture;

/**
 * {Entity}JpaEntity 테스트 Fixture
 *
 * @author Claude Code
 * @since 1.0.0
 */
public class {Entity}JpaEntityFixture {

    /**
     * 기본값으로 생성 (ID 없음 - 신규)
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
     * ID 포함하여 생성 (조회 시나리오)
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
     * 여러 개 생성
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

## 🔧 생성 규칙

### 1. 파일 위치

```
{layer}/src/testFixtures/java/{package}/fixture/
└── {Entity}Fixture.java
```

### 2. 네이밍 규칙

- **클래스명**: `*Fixture` 접미사 필수
- **패키지**: `{package}.fixture`
- **메서드**: `create*()`, `createWith*()`

### 3. Gradle 설정 자동 추가

```kotlin
// {layer}/build.gradle.kts
plugins {
    `java-library`
    `java-test-fixtures`  // ← 자동 추가
}
```

---

## 🎯 사용 예시

### Domain 테스트에서

```java
@Test
void testDomainLogic() {
    // Given
    OrderDomain order = OrderDomainFixture.create();

    // When
    order.confirmOrder();

    // Then
    assertThat(order.getStatus()).isEqualTo("CONFIRMED");
}
```

### Application 테스트에서

```java
@Test
void testUseCase() {
    // Given
    CreateOrderCommand command = CreateOrderCommandFixture.create();

    // When
    OrderResponse response = createOrderUseCase.execute(command);

    // Then
    assertThat(response).isNotNull();
}
```

### Persistence 테스트에서

```java
@Test
void testRepository() {
    // Given
    OrderJpaEntity entity = OrderJpaEntityFixture.create();

    // When
    OrderJpaEntity saved = repository.save(entity);

    // Then
    assertThat(saved.getId()).isNotNull();
}
```

---

## 📋 자동 생성 체크리스트

### Fixture 생성 시

- [x] `src/testFixtures/java/` 디렉토리 생성
- [x] `{Entity}Fixture.java` 클래스 생성
- [x] `create()` 기본 메서드 구현
- [x] `createWith*()` 커스터마이징 메서드 구현
- [x] `createWithId()` 조회 시나리오용 메서드 구현
- [x] `createMultiple()` 목록 테스트용 메서드 구현
- [x] Javadoc 작성 (사용 예시 포함)
- [x] Private 생성자로 인스턴스화 방지

### Gradle 설정

- [x] `java-test-fixtures` 플러그인 추가
- [x] 다른 모듈에서 `testFixtures()` 의존성 추가

---

## 🚀 고급 기능

### 1. 테스트와 함께 생성

```bash
# Domain 테스트 + Fixture 동시 생성
/test-gen-domain Order --with-fixtures

# 생성 결과:
# - OrderDomainTest.java
# - OrderDomainFixture.java
```

### 2. 여러 Layer Fixture 일괄 생성

```bash
# Order 관련 모든 Fixture 생성
/test-gen-fixtures Order --all-layers

# 생성 결과:
# - OrderDomainFixture.java (domain)
# - CreateOrderCommandFixture.java (application)
# - OrderApiRequestFixture.java (rest)
# - OrderJpaEntityFixture.java (persistence)
```

---

## 💡 Benefits

### 1. 테스트 코드 간결화
- Fixture 사용 전: 10줄 setup 코드
- Fixture 사용 후: 1줄로 객체 생성

### 2. 재사용성
- 모든 테스트에서 동일한 Fixture 공유
- testFixtures 의존성으로 다른 모듈에서도 사용

### 3. 유지보수성
- 테스트 데이터 변경 시 Fixture만 수정
- 모든 테스트에 자동 반영

---

**✅ 이 명령어는 Claude Code가 Layer별 Test Fixture를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: 테스트 데이터 생성 표준화 → 테스트 코드 간결화 → 유지보수성 향상!
