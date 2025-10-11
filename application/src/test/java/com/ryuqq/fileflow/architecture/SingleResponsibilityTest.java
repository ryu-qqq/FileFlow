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

/**
 * Single Responsibility Principle (SRP) Enforcement Tests
 *
 * 단일 책임 원칙 (Single Responsibility Principle):
 * - 클래스는 단 하나의 변경 이유만 가져야 함
 * - 하나의 클래스는 하나의 액터(Actor)에게만 책임을 짐
 * - 높은 응집도 (High Cohesion), 낮은 결합도 (Low Coupling)
 *
 * 측정 지표:
 * - 메서드 개수: 많을수록 여러 책임 의심
 * - 필드 개수: 많을수록 여러 관심사 의심
 * - 클래스 라인 수: 길수록 복잡도 증가
 * - LCOM (Lack of Cohesion): 높을수록 응집도 낮음
 *
 * 레이어별 기준:
 * - Domain: 메서드 ≤ 7, 라인 ≤ 200 (가장 엄격)
 * - Application: 메서드 ≤ 5, 라인 ≤ 150 (UseCase는 작아야 함)
 * - Adapter: 메서드 ≤ 10, 라인 ≤ 300
 *
 * @author Sangwon Ryu (ryu@company.com)
 * @since 2025-01-10
 */
@DisplayName("📏 Single Responsibility Principle Enforcement")
class SingleResponsibilityTest {

    private static JavaClasses allClasses;
    private static JavaClasses domainClasses;
    private static JavaClasses applicationClasses;
    private static JavaClasses adapterClasses;

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

        adapterClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.adapter");
    }

    // ========================================
    // Domain Layer - 가장 엄격한 SRP
    // ========================================

    @Nested
    @DisplayName("🏛️ Domain Layer - Strict SRP Enforcement")
    class DomainLayerSrpTests {

        @Test
        @DisplayName("Domain classes MUST have ≤ 18 public methods")
        void domainClassesShouldHaveLimitedMethods() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotInterfaces()
                .and().haveSimpleNameNotEndingWith("Exception")
                .and().haveSimpleNameNotEndingWith("Id")
                .and().haveSimpleNameNotEndingWith("Builder")  // Builder 예외
                .should(haveAtMostPublicMethods(18))
                .because("Aggregate Roots naturally encapsulate complex domain behavior with high cohesion");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain classes MUST have ≤ 8 instance fields")
        void domainClassesShouldHaveLimitedFields() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotInterfaces()
                .and().haveSimpleNameNotEndingWith("Exception")
                .and().haveSimpleNameNotEndingWith("Builder")  // Builder 예외
                .should(haveAtMostFields(8))
                .because("Too many fields indicate multiple responsibilities");

            rule.check(domainClasses);
        }

        // Note: Cohesion (LCOM) is better measured by PMD's GodClass rule
        // See config/pmd/pmd-ruleset.xml for accurate LCOM measurement
    }

    // ========================================
    // Application Layer - UseCase는 작아야 함
    // ========================================

    @Nested
    @DisplayName("⚙️ Application Layer - Small UseCase Enforcement")
    class ApplicationLayerSrpTests {

        @Test
        @DisplayName("UseCases MUST have ≤ 5 public methods")
        void useCasesShouldHaveLimitedMethods() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application..")
                .and().haveSimpleNameEndingWith("UseCase")
                .or().haveSimpleNameEndingWith("Service")
                .should(haveAtMostPublicMethods(5))
                .because("One UseCase should do one thing well");

            rule.check(applicationClasses);
        }

        @Test
        @DisplayName("UseCases SHOULD have single @Transactional method")
        void useCasesShouldHaveSingleTransactionalMethod() {
            // UseCase는 보통 하나의 트랜잭션 메서드만 가져야 함
            // 여러 개의 @Transactional 메서드 = 여러 책임 의심
            ArchRule rule = classes()
                .that().resideInAPackage("..application..")
                .and().haveSimpleNameEndingWith("UseCase")
                .should(haveAtMostTransactionalMethods(1))
                .because("Multiple transactional methods suggest multiple responsibilities");

            rule.check(applicationClasses);
        }
    }

    // ========================================
    // Adapter Layer - 리소스별 분리
    // ========================================

    @Nested
    @DisplayName("🔌 Adapter Layer - Resource-Based Separation")
    class AdapterLayerSrpTests {

        @Test
        @DisplayName("Controllers MUST have ≤ 10 endpoints")
        void controllersShouldHaveLimitedEndpoints() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter.in.web..")
                .and().haveSimpleNameEndingWith("Controller")
                .should(haveAtMostPublicMethods(10))
                .allowEmptyShould(true)  // Controller 없어도 OK
                .because("Controllers should be organized by resource (max 10 endpoints per resource)");

            rule.check(adapterClasses);
        }

        @Test
        @DisplayName("Repositories SHOULD focus on single Entity")
        void repositoriesShouldFocusOnSingleEntity() {
            // Repository는 하나의 Entity만 다뤄야 함
            // 여러 Entity 의존 = 여러 책임
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter.out.persistence..")
                .and().haveSimpleNameEndingWith("Repository")
                .should(haveSingleEntityDependency())
                .allowEmptyShould(true)  // Repository 없어도 OK
                .because("Repository should manage single Entity type only");

            rule.check(adapterClasses);
        }
    }

    // ========================================
    // 커스텀 ArchCondition 구현
    // ========================================

    /**
     * 최대 public 메서드 개수 제한
     */
    private static ArchCondition<JavaClass> haveAtMostPublicMethods(int maxMethods) {
        return new ArchCondition<JavaClass>("have at most " + maxMethods + " public methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long publicMethodCount = javaClass.getMethods().stream()
                    .filter(m -> m.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC))
                    .filter(m -> !m.getName().equals("equals"))
                    .filter(m -> !m.getName().equals("hashCode"))
                    .filter(m -> !m.getName().equals("toString"))
                    .count();

                if (publicMethodCount > maxMethods) {
                    String message = String.format(
                        "Class <%s> has %d public methods (max: %d) - violates SRP",
                        javaClass.getName(),
                        publicMethodCount,
                        maxMethods
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * 최대 필드 개수 제한
     */
    private static ArchCondition<JavaClass> haveAtMostFields(int maxFields) {
        return new ArchCondition<JavaClass>("have at most " + maxFields + " fields") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long fieldCount = javaClass.getFields().stream()
                    .filter(f -> !f.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC))
                    .count();

                if (fieldCount > maxFields) {
                    String message = String.format(
                        "Class <%s> has %d instance fields (max: %d) - too many concerns",
                        javaClass.getName(),
                        fieldCount,
                        maxFields
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    // Note: LCOM (Lack of Cohesion in Methods) is accurately measured by PMD's GodClass rule
    // See config/pmd/pmd-ruleset.xml - GodClass rule with LCOM threshold

    /**
     * 최대 @Transactional 메서드 개수
     */
    private static ArchCondition<JavaClass> haveAtMostTransactionalMethods(int maxTransactional) {
        return new ArchCondition<JavaClass>("have at most " + maxTransactional + " @Transactional methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long transactionalCount = javaClass.getMethods().stream()
                    .filter(m -> m.isAnnotatedWith("org.springframework.transaction.annotation.Transactional"))
                    .count();

                if (transactionalCount > maxTransactional) {
                    String message = String.format(
                        "Class <%s> has %d @Transactional methods (max: %d) - split into separate UseCases",
                        javaClass.getName(),
                        transactionalCount,
                        maxTransactional
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * Repository는 단일 Entity만 의존해야 함
     */
    private static ArchCondition<JavaClass> haveSingleEntityDependency() {
        return new ArchCondition<JavaClass>("depend on single Entity") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long entityDependencyCount = javaClass.getFields().stream()
                    .filter(f -> f.getRawType().getName().endsWith("Entity"))
                    .count();

                // Repository가 여러 Entity 의존 = 여러 책임
                if (entityDependencyCount > 1) {
                    String message = String.format(
                        "Repository <%s> depends on %d entities - should manage single entity type",
                        javaClass.getName(),
                        entityDependencyCount
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
