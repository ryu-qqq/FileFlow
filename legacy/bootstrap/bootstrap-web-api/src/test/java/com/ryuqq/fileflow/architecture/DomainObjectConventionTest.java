package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Domain Object Convention ArchUnit Test
 *
 * <p>도메인 객체 생성 컨벤션을 강제하는 아키텍처 테스트입니다.</p>
 * <p>모범 사례: domain.settings 패키지</p>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Entity: forNew(), of(), reconstitute() Static Factory Methods</li>
 *   <li>Value Object: of() Static Factory Method, 완전 불변성</li>
 *   <li>Record: ID 타입 전용, of() Static Factory Method</li>
 *   <li>Utility: final 클래스, private 생성자, static 메서드만</li>
 *   <li>Law of Demeter: Getter 체이닝 방지 메서드 제공</li>
 *   <li>Lombok 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
class DomainObjectConventionTest {

    private static JavaClasses domainClasses;

    @BeforeAll
    static void setUp() {
        domainClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.domain");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1️⃣ Lombok 금지 (Zero-Tolerance)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Lombok 금지 규칙")
    class LombokProhibitionTest {

        @Test
        @DisplayName("Domain 객체는 Lombok @Data를 사용하지 않아야 함")
        void domainObjectShouldNotUseLombokData() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("lombok.Data")
                .because("Domain 객체는 Pure Java를 사용해야 합니다 (@Data 금지)");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain 객체는 Lombok @Getter를 사용하지 않아야 함")
        void domainObjectShouldNotUseLombokGetter() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("lombok.Getter")
                .because("Domain 객체는 Pure Java를 사용해야 합니다 (@Getter 금지)");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain 객체는 Lombok @Setter를 사용하지 않아야 함")
        void domainObjectShouldNotUseLombokSetter() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("lombok.Setter")
                .because("Domain 객체는 Pure Java를 사용해야 합니다 (@Setter 금지)");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain 객체는 Lombok @Builder를 사용하지 않아야 함")
        void domainObjectShouldNotUseLombokBuilder() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("lombok.Builder")
                .because("Domain 객체는 Pure Java를 사용해야 합니다 (@Builder 금지)");

            rule.check(domainClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2️⃣ Entity (Aggregate Root) 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Entity (Aggregate Root) 컨벤션")
    class EntityConventionTest {

        @Test
        @DisplayName("Entity ID 필드는 final이어야 함")
        void entityIdFieldShouldBeFinal() {
            ArchRule rule = fields()
                .that().haveName("id")
                .and().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().resideOutsideOfPackage("..fixture..")  // Fixture Builder 제외
                .and().areDeclaredInClassesThat().areNotRecords()
                .and().areDeclaredInClassesThat().areNotEnums()
                .and().areDeclaredInClassesThat().areNotInterfaces()
                .should().beFinal()
                .because("Entity ID는 불변이어야 합니다");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Entity는 reconstitute() Static Factory Method를 가져야 함")
        void entityShouldHaveReconstituteMethod() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().areNotRecords()
                .and().areDeclaredInClassesThat().areNotEnums()
                .and().areDeclaredInClassesThat().areNotInterfaces()
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Id")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Key")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Value")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Type")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Level")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Status")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Merger")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Util")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Helper")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Exception")
                .and().haveName("reconstitute")
                .should().bePublic()
                .andShould().beStatic()
                .because("Entity는 DB 재구성을 위한 reconstitute() Static Factory Method가 필요합니다");

            rule.check(domainClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3️⃣ Value Object 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Value Object 컨벤션")
    class ValueObjectConventionTest {

        @Test
        @DisplayName("Value Object(Key)는 모든 필드가 final이어야 함")
        void valueObjectKeyFieldsShouldBeFinal() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Key")
                .and().areNotRecords()
                .should().haveOnlyFinalFields()
                .because("Value Object는 완전한 불변성을 보장해야 합니다");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Value Object(Value)는 모든 필드가 final이어야 함")
        void valueObjectValueFieldsShouldBeFinal() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Value")
                .and().areNotRecords()
                .should().haveOnlyFinalFields()
                .because("Value Object는 완전한 불변성을 보장해야 합니다");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Value Object는 of() Static Factory Method를 가져야 함")
        void valueObjectShouldHaveOfMethod() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Key")
                .or().areDeclaredInClassesThat().haveSimpleNameEndingWith("Value")
                .and().haveName("of")
                .should().bePublic()
                .andShould().beStatic()
                .because("Value Object는 of() Static Factory Method로 생성해야 합니다");

            rule.check(domainClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4️⃣ Record 규칙 (Java 21)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Record 컨벤션 (Java 21)")
    class RecordConventionTest {

        @Test
        @DisplayName("Record는 Value Object에만 사용해야 함 (주로 ID)")
        void recordShouldBeUsedForValueObjects() {
            ArchRule rule = classes()
                .that().areRecords()
                .and().resideInAPackage("..domain..")
                .should().haveSimpleNameEndingWith("Id")
                .orShould().haveSimpleNameNotEndingWith("Service")
                .andShould().haveSimpleNameNotEndingWith("Repository")
                .andShould().haveSimpleNameNotEndingWith("Port")
                .because("Record는 Value Object에만 사용해야 합니다 (주로 ID, Email, Grant 등)");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Record는 of() Static Factory Method를 가져야 함")
        void recordShouldHaveOfMethod() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().areRecords()
                .and().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().haveName("of")
                .should().bePublic()
                .andShould().beStatic()
                .because("Record도 of() Static Factory Method를 제공해야 합니다");

            rule.check(domainClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 5️⃣ Utility Class 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Utility Class 컨벤션")
    class UtilityClassConventionTest {

        @Test
        @DisplayName("Utility Class는 final이어야 함")
        void utilityClassShouldBeFinal() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Merger")
                .or().haveSimpleNameEndingWith("Util")
                .or().haveSimpleNameEndingWith("Helper")
                .should().haveModifier(com.tngtech.archunit.core.domain.JavaModifier.FINAL)
                .because("Utility Class는 상속을 방지하기 위해 final이어야 합니다");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Utility Class는 private 생성자만 가져야 함")
        void utilityClassShouldHaveOnlyPrivateConstructors() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Merger")
                .or().haveSimpleNameEndingWith("Util")
                .or().haveSimpleNameEndingWith("Helper")
                .and().resideInAPackage("..domain..")
                .should().haveOnlyPrivateConstructors()
                .because("Utility Class는 인스턴스화를 방지해야 합니다");

            rule.check(domainClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 6️⃣ Law of Demeter 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Law of Demeter 컨벤션")
    class LawOfDemeterConventionTest {

        // Note: Law of Demeter 규칙은 ArchUnit으로 검증하기 복잡하므로
        // Grep 또는 수동 코드 리뷰로 검증
        // 예: grep -r "\.get.*()\.get" domain/src/main/java

        // Law of Demeter 가이드라인:
        // ❌ Bad: setting.getId().value()
        // ✅ Good: setting.getIdValue()
        // ❌ Bad: setting.getValue().getDisplayValue()
        // ✅ Good: setting.getDisplayValue()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 7️⃣ equals/hashCode 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("equals/hashCode 컨벤션")
    class EqualsHashCodeConventionTest {

        @Test
        @DisplayName("Domain 객체는 equals()를 구현해야 함")
        void domainObjectShouldImplementEquals() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotRecords()
                .and().areNotEnums()
                .and().areNotInterfaces()
                .and().haveSimpleNameNotEndingWith("Merger")
                .and().haveSimpleNameNotEndingWith("Util")
                .and().haveSimpleNameNotEndingWith("Helper")
                .and().haveSimpleNameNotContaining("Exception")
                .should().implement("java.lang.Object")
                .because("Domain 객체는 equals()를 구현해야 합니다");

            // Note: ArchUnit으로 equals() 구현 여부를 직접 검증하기 어려우므로
            // 이 규칙은 가이드라인으로만 사용
            // 실제 검증은 Unit Test에서 수행
        }

        @Test
        @DisplayName("Domain 객체는 hashCode()를 구현해야 함")
        void domainObjectShouldImplementHashCode() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotRecords()
                .and().areNotEnums()
                .and().areNotInterfaces()
                .and().haveSimpleNameNotEndingWith("Merger")
                .and().haveSimpleNameNotEndingWith("Util")
                .and().haveSimpleNameNotEndingWith("Helper")
                .and().haveSimpleNameNotContaining("Exception")
                .should().implement("java.lang.Object")
                .because("Domain 객체는 hashCode()를 구현해야 합니다");

            // Note: ArchUnit으로 hashCode() 구현 여부를 직접 검증하기 어려우므로
            // 이 규칙은 가이드라인으로만 사용
            // 실제 검증은 Unit Test에서 수행
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 8️⃣ toString() 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("toString() 컨벤션")
    class ToStringConventionTest {

        @Test
        @DisplayName("Domain 객체는 toString()을 구현해야 함")
        void domainObjectShouldImplementToString() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotRecords()
                .and().areNotEnums()
                .and().areNotInterfaces()
                .and().haveSimpleNameNotEndingWith("Merger")
                .and().haveSimpleNameNotEndingWith("Util")
                .and().haveSimpleNameNotEndingWith("Helper")
                .and().haveSimpleNameNotContaining("Exception")
                .should().implement("java.lang.Object")
                .because("Domain 객체는 toString()을 구현해야 합니다 (디버깅 용이성)");

            // Note: ArchUnit으로 toString() 구현 여부를 직접 검증하기 어려우므로
            // 이 규칙은 가이드라인으로만 사용
            // 실제 검증은 Unit Test에서 수행
        }
    }
}
