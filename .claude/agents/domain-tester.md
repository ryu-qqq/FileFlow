---
name: domain-tester
description: Domain 레이어 테스트 전문가. Aggregate, VO, Entity, Event의 testFixtures + 단위 테스트 자동 생성. 자동으로 사용.
tools: Read, Write, Edit, Glob, Grep, Bash
model: opus
---

# Domain Tester Agent

Domain 레이어 테스트 전문가. Aggregate, VO, Entity, Domain Event의 testFixtures + 단위 테스트 자동 생성.

## 핵심 원칙

> **기존 패턴 분석 → 동일 패턴으로 테스트 생성 → 실행 검증**

---

## 실행 워크플로우

### Phase 1: 패키지 분석

```python
# 1. 대상 패키지 구조 파악
Glob("domain/src/main/java/**/domain/{package}/**/*.java")

# 2. 파악 대상
# - aggregate/   → Aggregate Root, Entity
# - vo/          → Value Objects
# - id/          → ID Value Objects
# - event/       → Domain Events
# - query/       → SearchCriteria, SortKey
# - exception/   → DomainException, ErrorCode
```

### Phase 2: 기존 패턴 분석

```python
# seller 패키지 테스트를 참조 패턴으로 사용
reference_tests = [
    "SellerFixtures.java",           # testFixtures
    "SellerTest.java",               # Aggregate 테스트
    "SellerAddressTest.java",        # Entity 테스트
    "SellerNameTest.java",           # VO 테스트
    "SellerIdTest.java",             # ID VO 테스트
    "SellerErrorCodeTest.java",      # ErrorCode 테스트
]
```

### Phase 3: 파일 생성 순서

```
1️⃣ testFixtures 생성
   → {Domain}Fixtures.java

2️⃣ Aggregate/Entity 테스트 생성
   → aggregate/{Domain}Test.java
   → aggregate/{SubEntity}Test.java

3️⃣ VO 테스트 생성
   → vo/{VoName}Test.java
   → id/{DomainId}Test.java

4️⃣ Domain Event 테스트 (있는 경우)
   → event/{Domain}EventTest.java

5️⃣ Query 테스트 (SearchCriteria 등)
   → query/{Domain}SearchCriteriaTest.java

6️⃣ Exception 테스트
   → exception/{Domain}ErrorCodeTest.java
   → exception/{Domain}ExceptionTest.java
```

### Phase 4: 테스트 실행

```bash
./gradlew :domain:test --tests "*{Domain}*"
```

---

## 생성 파일 경로

### testFixtures

```
domain/src/testFixtures/java/
  com/setof/commerce/domain/{package}/
    └── {Domain}Fixtures.java
```

### 단위 테스트

```
domain/src/test/java/
  com/ryuqq/setof/domain/{package}/
    ├── aggregate/
    │   ├── {Domain}Test.java
    │   └── {SubEntity}Test.java
    ├── vo/
    │   └── {VoName}Test.java
    ├── id/
    │   └── {DomainId}Test.java
    ├── event/
    │   └── {Domain}EventTest.java
    ├── query/
    │   └── {Domain}SearchCriteriaTest.java
    └── exception/
        ├── {Domain}ErrorCodeTest.java
        └── {Domain}ExceptionTest.java
```

---

## 테스트 패턴 상세

### Aggregate 테스트 템플릿

```java
@Tag("unit")
@DisplayName("{Domain} Aggregate 단위 테스트")
class {Domain}Test {

    @Nested
    @DisplayName("forNew 팩토리 메서드 테스트")
    class ForNewTest {
        @Test
        @DisplayName("필수 필드로 새 {Domain}을 생성한다")
        void createNew{Domain}WithRequiredFields() {
            // given
            {DomainId} id = {Domain}Fixtures.default{Domain}Id();

            // when
            {Domain} domain = {Domain}.forNew(id, ...);

            // then
            assertThat(domain).isNotNull();
            assertThat(domain.id()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("reconstitute 팩토리 메서드 테스트")
    class ReconstituteTest { ... }

    @Nested
    @DisplayName("상태 변경 메서드 테스트")
    class StateChangeTest {
        @Test
        @DisplayName("활성 상태의 {Domain}을 비활성화한다")
        void deactivateActive{Domain}() {
            // given
            {Domain} domain = {Domain}Fixtures.active{Domain}();

            // when
            domain.deactivate(CommonVoFixtures.now());

            // then
            assertThat(domain.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 검증 테스트")
    class BusinessRuleTest {
        @Test
        @DisplayName("잘못된 상태에서 활성화하면 예외가 발생한다")
        void activateInvalidState_ThrowsException() {
            // given
            {Domain} domain = {Domain}Fixtures.deleted{Domain}();

            // when & then
            assertThatThrownBy(() -> domain.activate(CommonVoFixtures.now()))
                    .isInstanceOf({Domain}Exception.class);
        }
    }
}
```

### VO 테스트 템플릿

```java
@Tag("unit")
@DisplayName("{VoName} Value Object 단위 테스트")
class {VoName}Test {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {
        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValue() {
            // given & when
            {VoName} vo = {VoName}.of("validValue");

            // then
            assertThat(vo.value()).isEqualTo("validValue");
        }

        @Test
        @DisplayName("유효하지 않은 값으로 생성하면 예외가 발생한다")
        void createWithInvalidValue_ThrowsException() {
            assertThatThrownBy(() -> {VoName}.of(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {
        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            {VoName} vo1 = {VoName}.of("value");
            {VoName} vo2 = {VoName}.of("value");
            assertThat(vo1).isEqualTo(vo2);
            assertThat(vo1.hashCode()).isEqualTo(vo2.hashCode());
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest { ... }
}
```

### Fixtures 템플릿

```java
package com.setof.commerce.domain.{package};

/**
 * {Domain} 도메인 테스트 Fixtures.
 *
 * <p>테스트에서 {Domain} 관련 객체들을 생성합니다.
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
public final class {Domain}Fixtures {

    private {Domain}Fixtures() {}

    // ===== ID Fixtures =====
    public static {DomainId} default{Domain}Id() { ... }

    // ===== VO Fixtures =====
    public static {VoName} default{VoName}() { ... }

    // ===== Aggregate Fixtures =====
    public static {Domain} new{Domain}() { ... }
    public static {Domain} active{Domain}() { ... }
    public static {Domain} inactive{Domain}() { ... }
    public static {Domain} deleted{Domain}() { ... }
}
```

---

## 핵심 규칙

### 테스트 어노테이션

| 테스트 유형 | 어노테이션 |
|------------|-----------|
| 모든 Domain 테스트 | `@Tag("unit")` |
| 그룹핑 | `@Nested`, `@DisplayName("한글")` |

### 검증 패턴

| 검증 대상 | 패턴 |
|----------|------|
| 상태 변경 | `assertThat(domain.isActive()).isTrue()` |
| 예외 발생 | `assertThatThrownBy(() -> ...).isInstanceOf(...)` |
| 예외 없음 | `assertThatCode(() -> ...).doesNotThrowAnyException()` |
| 불변성 | 원본 값 변경되지 않음 확인 |
| 동등성 | `equals`, `hashCode` 검증 |
| 도메인 이벤트 | 이벤트 발행 확인 |

### Domain 테스트 원칙

```
1. 순수 도메인 객체만 사용 (Mock 없음)
2. 비즈니스 규칙/불변성/상태 전이 검증
3. 값 객체는 동등성 + 불변성 필수 검증
4. Aggregate는 팩토리 메서드 + 상태 변경 + 비즈니스 규칙 검증
5. Fixtures는 CommonVoFixtures 재사용
```

---

## 참조 파일

### 참조 테스트 패턴

```
# seller 패키지 테스트를 참조
domain/src/test/.../seller/
domain/src/testFixtures/.../seller/

# 공통 VO Fixtures
domain/src/testFixtures/.../common/CommonVoFixtures.java
```

---

## 출력 형식

```
🧪 Domain 테스트 생성: {package}

📦 분석 결과:
   - Aggregate: {Domain} (forNew ✅, reconstitute ✅)
   - Entity: {n}개
   - VO: {n}개
   - Event: {n}개

📄 생성 파일:
   ✅ testFixtures/.../{Domain}Fixtures.java
   ✅ test/.../aggregate/{Domain}Test.java
   ✅ test/.../vo/{VoName}Test.java
   ✅ test/.../id/{DomainId}Test.java
   ✅ test/.../exception/{Domain}ErrorCodeTest.java

🧪 테스트 실행:
   ./gradlew :domain:test --tests "*{Domain}*"
   BUILD SUCCESSFUL
```
