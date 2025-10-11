package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Law of Demeter Enforcement Tests
 *
 * 데미터의 법칙 (Law of Demeter / Principle of Least Knowledge):
 * - 객체는 자기 자신, 메서드 파라미터, 생성한 객체, 인스턴스 변수만 접근
 * - Train wreck (obj.getX().getY().getZ()) 금지
 * - Tell, Don't Ask 원칙 준수
 *
 * 허용 패턴:
 * - Builder 패턴 (Fluent API)
 * - Stream API
 * - StringBuilder
 *
 * 금지 패턴:
 * - Getter 체이닝
 * - 중간 객체 조작
 * - JPA 관계 체이닝
 *
 * @author Sangwon Ryu (ryu@company.com)
 * @since 2025-01-10
 */
@DisplayName("⚖️ Law of Demeter Enforcement")
class LawOfDemeterTest {

    private static JavaClasses allClasses;
    private static JavaClasses domainClasses;
    private static JavaClasses applicationClasses;
    private static JavaClasses persistenceClasses;
    private static JavaClasses controllerClasses;

    @BeforeAll
    static void setup() {
        allClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow");

        domainClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.domain");

        applicationClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.application");

        persistenceClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence");

        controllerClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.adapter.in");
    }

    // ========================================
    // Domain Layer - 가장 엄격한 데미터 법칙
    // ========================================

    @Nested
    @DisplayName("🏛️ Domain Layer - Strict Demeter Enforcement")
    class DomainLayerDemeterTests {

        // Note: Getter chaining is more accurately detected by PMD's DomainLayerDemeterStrict rule
        // See config/pmd/pmd-ruleset.xml for XPath-based AST analysis

        @Test
        @DisplayName("Domain MUST use delegation instead of getters")
        void domainShouldUseDelegation() {
            // Domain 객체는 내부 구조를 노출하지 않고
            // 위임 메서드를 통해 기능 제공
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameNotEndingWith("Id")
                .and().haveSimpleNameNotEndingWith("Exception")
                .and().haveSimpleNameNotEndingWith("Key")      // PolicyKey 등 Value Object 예외
                .and().haveSimpleNameNotEndingWith("Status")   // UploadStatus 등 Value Object 예외
                .and().areNotRecords()                         // Record는 자동으로 getter만 제공
                .should(provideBusinessMethods())
                .because("Domain objects should provide behavior, not just getters");

            rule.check(domainClasses);
        }
    }

    // ========================================
    // Persistence Layer - Long FK 전략으로 데미터 위반 방지
    // ========================================

    @Nested
    @DisplayName("💾 Persistence Layer - Long FK Strategy")
    class PersistenceLayerDemeterTests {

        @Test
        @DisplayName("Entities MUST use Long FK, NOT JPA relationships")
        void entitiesMustUseLongFk() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.out.persistence..")
                .and().haveSimpleNameEndingWith("Entity")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Entity")
                .allowEmptyShould(true)  // Entity 없어도 OK
                .because("Use Long FK instead of JPA relationships to prevent Demeter violations");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Entities MUST NOT have setter methods")
        void entitiesShouldNotHaveSetters() {
            // Setter는 데미터 위반을 유발하므로 금지
            // 대신 static factory method 사용
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter.out.persistence..")
                .and().haveSimpleNameEndingWith("Entity")
                .should(notHaveSetterMethods())
                .allowEmptyShould(true)  // Entity 없어도 OK
                .because("Entities should be immutable - use static factory methods");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Controller Layer - Record로 체이닝 방지
    // ========================================

    @Nested
    @DisplayName("🌐 Controller Layer - Record DTOs")
    class ControllerLayerDemeterTests {

        @Test
        @DisplayName("Request/Response DTOs MUST be records")
        void dtosShouldBeRecords() {
            ArchRule requestRule = classes()
                .that().resideInAPackage("..adapter.in.web..")
                .and().haveSimpleNameEndingWith("Request")
                .should().beRecords()
                .allowEmptyShould(true)  // Request DTO 없어도 OK
                .because("Records prevent getter chaining and enforce immutability");

            ArchRule responseRule = classes()
                .that().resideInAPackage("..adapter.in.web..")
                .and().haveSimpleNameEndingWith("Response")
                .should().beRecords()
                .allowEmptyShould(true)  // Response DTO 없어도 OK
                .because("Records prevent getter chaining and enforce immutability");

            requestRule.check(controllerClasses);
            responseRule.check(controllerClasses);
        }

        @Test
        @DisplayName("Controllers MUST NOT access Repository directly")
        void controllersShouldNotAccessRepositoryDirectly() {
            // Controller → Repository는 데미터 위반
            // Controller → UseCase → Repository 패턴 강제
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.in..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Port")
                .allowEmptyShould(true)  // Controller 없어도 OK
                .because("Controllers must use UseCases only, not Repositories directly");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // 커스텀 ArchCondition 구현
    // ========================================

    // Note: Getter chaining detection is better handled by PMD's AST-based XPath rules
    // See config/pmd/pmd-ruleset.xml - DomainLayerDemeterStrict rule for accurate detection

    /**
     * 비즈니스 메서드 제공 여부 검사
     *
     * Domain 객체는 getter만 제공하는 것이 아닌
     * 비즈니스 로직을 캡슐화한 메서드를 제공해야 함
     */
    private static ArchCondition<JavaClass> provideBusinessMethods() {
        return new ArchCondition<JavaClass>("provide business methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long getterCount = javaClass.getMethods().stream()
                    .filter(m -> m.getName().startsWith("get") && m.getRawParameterTypes().isEmpty())
                    .count();

                long businessMethodCount = javaClass.getMethods().stream()
                    .filter(m -> !m.getName().startsWith("get") &&
                                !m.getName().startsWith("set") &&
                                !m.getName().equals("equals") &&
                                !m.getName().equals("hashCode") &&
                                !m.getName().equals("toString") &&
                                !m.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC))
                    .count();

                // Getter만 있고 비즈니스 메서드가 없으면 경고
                if (getterCount > 0 && businessMethodCount == 0) {
                    String message = String.format(
                        "Class <%s> only provides getters (%d) without business methods - violates Tell, Don't Ask",
                        javaClass.getName(),
                        getterCount
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * Setter 메서드 감지
     *
     * Setter는 데미터 위반을 유발하므로 금지
     */
    private static ArchCondition<JavaClass> notHaveSetterMethods() {
        return new ArchCondition<JavaClass>("not have setter methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                    .filter(m -> m.getName().startsWith("set") &&
                                m.getRawParameterTypes().size() == 1 &&
                                m.getRawReturnType().getName().equals("void"))
                    .forEach(setter -> {
                        String message = String.format(
                            "Class <%s> has setter method <%s> - use static factory methods instead",
                            javaClass.getName(),
                            setter.getName()
                        );
                        events.add(SimpleConditionEvent.violated(setter, message));
                    });
            }
        };
    }
}
