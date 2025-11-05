# ArchUnit JPA Entity Rules (JPA 엔티티 자동 검증 규칙)

**목적**: JPA Entity 설계 규칙의 자동 검증

**위치**: `application/src/test/java/.../architecture/PersistenceLayerTest.java`

**필수 버전**: ArchUnit 1.0.0+, JUnit 5

---

## 🎯 검증 항목

1. **Lombok 금지** - 모든 Lombok 어노테이션 사용 금지
2. **JPA 관계 어노테이션 금지** - `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` 금지
3. **Setter 금지** - `setXxx()` 메서드 금지
4. **BaseAuditEntity 상속** - 모든 Entity는 `BaseAuditEntity` 또는 `SoftDeletableEntity` 상속
5. **Entity 불변성** - 비즈니스 메서드 금지 (Getter, Constructor, Static Factory만 허용)

---

## 📦 의존성 추가

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.0.1</version>
    <scope>test</scope>
</dependency>
```

---

## 🧪 ArchUnit 테스트 클래스

### PersistenceLayerTest.java

```java
package com.company.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

/**
 * JPA Entity Layer ArchUnit 검증 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Persistence Layer ArchUnit Tests")
class PersistenceLayerTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .importPackages("com.company.adapter.out.persistence");
    }

    /**
     * 규칙 1: Lombok 금지
     */
    @Test
    @DisplayName("JPA Entity는 Lombok 어노테이션을 사용하지 않아야 한다")
    void jpaEntityShouldNotUseLombok() {
        ArchRule rule = noClasses()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith("lombok.Data")
            .orShould().beAnnotatedWith("lombok.Builder")
            .orShould().beAnnotatedWith("lombok.Getter")
            .orShould().beAnnotatedWith("lombok.Setter")
            .orShould().beAnnotatedWith("lombok.AllArgsConstructor")
            .orShould().beAnnotatedWith("lombok.NoArgsConstructor")
            .orShould().beAnnotatedWith("lombok.RequiredArgsConstructor")
            .orShould().beAnnotatedWith("lombok.ToString")
            .orShould().beAnnotatedWith("lombok.EqualsAndHashCode")
            .because("Lombok은 JPA Entity에서 금지됩니다 (Lazy Loading 문제, 불변성 위반)");

        rule.check(classes);
    }

    /**
     * 규칙 2: JPA 관계 어노테이션 금지
     */
    @Test
    @DisplayName("JPA Entity는 관계 어노테이션을 사용하지 않아야 한다")
    void jpaEntityShouldNotUseRelationshipAnnotations() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .should().notBeAnnotatedWith(OneToMany.class)
            .andShould().notBeAnnotatedWith(ManyToOne.class)
            .andShould().notBeAnnotatedWith(OneToOne.class)
            .andShould().notBeAnnotatedWith(ManyToMany.class)
            .because("JPA 관계 어노테이션 대신 Long FK를 사용해야 합니다 (N+1 문제 방지, Law of Demeter 준수)");

        rule.check(classes);
    }

    /**
     * 규칙 3: Setter 금지
     */
    @Test
    @DisplayName("JPA Entity는 Setter 메서드를 가지지 않아야 한다")
    void jpaEntityShouldNotHaveSetters() {
        ArchRule rule = noMethods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and().arePublic()
            .and().haveNameMatching("set[A-Z].*")
            .should().beDeclared()
            .because("JPA Entity는 불변이어야 하며 Setter를 가질 수 없습니다");

        rule.check(classes);
    }

    /**
     * 규칙 4: BaseAuditEntity 상속 검증
     */
    @Test
    @DisplayName("JPA Entity는 BaseAuditEntity 또는 SoftDeletableEntity를 상속해야 한다")
    void jpaEntityShouldExtendBaseAuditEntity() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo("com.company.common.entity.BaseAuditEntity")
            .orShould().beAssignableTo("com.company.common.entity.SoftDeletableEntity")
            .because("모든 JPA Entity는 감사 필드를 위해 BaseAuditEntity 또는 SoftDeletableEntity를 상속해야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 5: Entity 네이밍 규칙
     */
    @Test
    @DisplayName("JPA Entity 클래스는 'JpaEntity' 접미사를 가져야 한다")
    void jpaEntityShouldHaveCorrectSuffix() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().haveSimpleNameEndingWith("JpaEntity")
            .because("JPA Entity는 Domain Model과 구분하기 위해 'JpaEntity' 접미사를 사용해야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 6: Entity는 adapter.out.persistence 패키지에 위치
     */
    @Test
    @DisplayName("JPA Entity는 adapter.out.persistence 패키지에 위치해야 한다")
    void jpaEntityShouldBeInCorrectPackage() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..adapter.out.persistence..")
            .because("JPA Entity는 Persistence Layer의 adapter.out.persistence 패키지에 위치해야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 7: Entity는 public이어야 함
     */
    @Test
    @DisplayName("JPA Entity는 public 클래스여야 한다")
    void jpaEntityShouldBePublic() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().bePublic()
            .because("JPA Entity는 JPA가 접근할 수 있도록 public이어야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 8: @Table 어노테이션 필수
     */
    @Test
    @DisplayName("JPA Entity는 @Table 어노테이션을 가져야 한다")
    void jpaEntityShouldHaveTableAnnotation() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Table.class)
            .because("명시적 테이블 명 지정을 위해 @Table 어노테이션이 필수입니다");

        rule.check(classes);
    }

    /**
     * 규칙 9: Entity는 final이 아니어야 함
     */
    @Test
    @DisplayName("JPA Entity는 final 클래스가 아니어야 한다")
    void jpaEntityShouldNotBeFinal() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().notBeFinal()
            .because("JPA는 프록시 생성을 위해 Entity가 final이 아니어야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 10: Long FK 필드 명명 규칙
     */
    @Test
    @DisplayName("Entity의 외래키 필드는 'Id' 접미사를 가져야 한다")
    void entityForeignKeyFieldsShouldEndWithId() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and().haveRawType(Long.class)
            .and().areNotAnnotatedWith(Id.class)
            .and().areNotStatic()
            .should().haveNameMatching(".*Id")
            .because("Long FK 필드는 명확한 식별을 위해 'Id' 접미사를 사용해야 합니다 (예: userId, orderId)");

        rule.check(classes);
    }

    /**
     * 규칙 11: ID 필드는 Long 타입
     */
    @Test
    @DisplayName("Entity의 ID 필드는 Long 타입이어야 한다")
    void entityIdFieldShouldBeLong() {
        ArchRule rule = fields()
            .that().areAnnotatedWith(Id.class)
            .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .should().haveRawType(Long.class)
            .because("ID 필드는 확장성을 위해 Long 타입을 사용해야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 12: Enum 필드는 @Enumerated(EnumType.STRING) 사용
     */
    @Test
    @DisplayName("Entity의 Enum 필드는 EnumType.STRING을 사용해야 한다")
    void entityEnumFieldsShouldUseStringType() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and().haveRawType(Enum.class)
            .should().beAnnotatedWith(Enumerated.class)
            .because("Enum 필드는 유지보수성을 위해 @Enumerated(EnumType.STRING)을 사용해야 합니다");

        rule.check(classes);
    }
}
```

---

## 🏃 실행 방법

### Maven

```bash
mvn test -Dtest=PersistenceLayerTest
```

### Gradle

```bash
./gradlew test --tests PersistenceLayerTest
```

### IntelliJ IDEA

1. `PersistenceLayerTest` 클래스 열기
2. 클래스명 왼쪽 초록색 화살표 클릭
3. "Run 'PersistenceLayerTest'" 선택

---

## 📊 검증 결과 해석

### ✅ 성공

```
PersistenceLayerTest > jpaEntityShouldNotUseLombok() PASSED
PersistenceLayerTest > jpaEntityShouldNotUseRelationshipAnnotations() PASSED
PersistenceLayerTest > jpaEntityShouldNotHaveSetters() PASSED
...
```

### ❌ 실패 (예시)

```
PersistenceLayerTest > jpaEntityShouldNotUseLombok() FAILED
    com.tngtech.archunit.lang.AssertionError:
    Architecture Violation [Priority: MEDIUM] -
    Rule 'no classes that are annotated with @Entity should be annotated with @lombok.Data'
    was violated (1 times):
    Class <com.company.adapter.out.persistence.OrderJpaEntity> is annotated with @lombok.Data
        in (OrderJpaEntity.java:15)
```

**해결 방법**: `OrderJpaEntity`에서 `@Data` 어노테이션 제거

---

## 🔄 CI/CD 통합

### GitHub Actions

```yaml
name: Architecture Tests

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  archunit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run ArchUnit Tests
        run: mvn test -Dtest=PersistenceLayerTest
```

---

## 📋 체크리스트

ArchUnit 테스트 작성 시:
- [ ] `PersistenceLayerTest.java` 클래스 생성
- [ ] 12개 규칙 모두 구현
- [ ] 빌드 시 자동 실행 설정
- [ ] CI/CD 파이프라인에 통합
- [ ] 위반 시 빌드 실패 설정

---

## 📖 관련 문서

- **[Core Rules](../../04-persistence-layer/jpa-entity-design/00_jpa-entity-core-rules.md)** - JPA Entity 핵심 규칙
- **[Long FK Strategy](../../04-persistence-layer/jpa-entity-design/01_long-fk-strategy.md)** - Long FK 전략
- **[Constructor Pattern](../../04-persistence-layer/jpa-entity-design/02_constructor-pattern.md)** - 3-Tier Constructor
- **[Audit Entity Pattern](../../04-persistence-layer/jpa-entity-design/03_audit-entity-pattern.md)** - BaseAuditEntity

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
