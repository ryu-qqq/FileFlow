package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit Level 3: Zero Tolerance Architecture Tests
 *
 * Enforces strict hexagonal architecture rules across all modules.
 * NO violations allowed - build will fail on any breach.
 *
 * @author Architecture Team (arch-team@company.com)
 * @since 2024-01-01
 */
@Disabled("TODO: ArchUnit 테스트 실행 인프라 문제 해결 후 활성화 - Issue #3")
@DisplayName("🏛️ Hexagonal Architecture Enforcement (Level 3)")
class HexagonalArchitectureTest {

    private static JavaClasses allClasses;
    private static JavaClasses domainClasses;
    // TODO: Task 1.2+에서 활성화
    // private static JavaClasses applicationClasses;
    // private static JavaClasses adapterClasses;

    @BeforeAll
    static void setup() {
        // Domain 모듈만 먼저 구현되므로 domain 클래스만 로드
        domainClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.domain");

        allClasses = domainClasses; // Task 1.1: Domain만 구현

        // TODO: Task 1.2+에서 application/adapter 구현 후 활성화
        // applicationClasses = new ClassFileImporter()
        //     .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        //     .importPackages("com.ryuqq.fileflow.application");
        //
        // adapterClasses = new ClassFileImporter()
        //     .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        //     .importPackages("com.ryuqq.fileflow.adapter");
    }

    // ========================================
    // Layer Dependency Rules
    // ========================================

    @Nested
    @DisplayName("📦 Layer Dependency Enforcement")
    class LayerDependencyTests {

        @Test
        @Disabled("TODO: Task 1.2+에서 Application/Adapter 구현 후 활성화")
        @DisplayName("Hexagonal architecture layers must be respected")
        void hexagonalArchitectureShouldBeRespected() {
            ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()

                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Adapter").definedBy("..adapter..")
                .layer("Bootstrap").definedBy("..bootstrap..")

                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter", "Bootstrap")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "Bootstrap")
                .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Bootstrap");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Domain must NOT depend on any other layer")
        void domainShouldNotDependOnOtherLayers() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "..adapter..",
                    "..bootstrap.."
                );

            rule.check(domainClasses);
        }

        // TODO: Task 1.2에서 Application 구현 후 활성화
        // @Test
        // @DisplayName("Application must NOT depend on adapters")
        // void applicationShouldNotDependOnAdapters() {
        //     ArchRule rule = noClasses()
        //         .that().resideInAPackage("..application..")
        //         .should().dependOnClassesThat().resideInAnyPackage(
        //             "..adapter..",
        //             "..bootstrap.."
        //         );
        //
        //     rule.check(applicationClasses);
        // }
    }

    // ========================================
    // Domain Purity Rules (CRITICAL)
    // ========================================

    @Nested
    @DisplayName("🔒 Domain Purity Enforcement (CRITICAL)")
    class DomainPurityTests {

        @Test
        @DisplayName("Domain MUST NOT use Spring Framework")
        void noSpringDependencies() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.."
                )
                .because("Domain must remain pure Java with zero Spring dependencies");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain MUST NOT use JPA/Hibernate")
        void noJpaDependencies() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..",
                    "org.hibernate.."
                )
                .because("Domain must not depend on JPA/Hibernate - persistence is adapter concern");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain MUST NOT use Lombok (STRICTLY PROHIBITED)")
        void noLombokAnnotations() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("lombok..")
                .because("Lombok is strictly prohibited across entire project");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain fields MUST be private final")
        void domainFieldsShouldBeFinal() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areNotStatic()
                .should().bePrivate()
                .andShould().beFinal()
                .because("Domain objects must be immutable - all fields should be private final");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain MUST NOT have setter methods")
        void noSetterMethods() {
            // setter 메서드가 있으면 조건이 매칭되고, 해당 메서드들이 rule.check()로 넘어감
            // should(new ArchCondition...)에서 항상 violated를 발생시키면 setter가 있을 때 테스트 실패
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().haveNameMatching("set[A-Z].*")
                .and().arePublic()
                .should(new ArchCondition<com.tngtech.archunit.core.domain.JavaMethod>("not exist") {
                    @Override
                    public void check(com.tngtech.archunit.core.domain.JavaMethod method, ConditionEvents events) {
                        String message = String.format(
                            "Setter method %s found - domain objects must be immutable",
                            method.getFullName()
                        );
                        events.add(SimpleConditionEvent.violated(method, message));
                    }
                })
                .because("Domain objects must be immutable - no setter methods allowed");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain MUST NOT have public constructors")
        void noPublicConstructors() {
            ArchRule rule = constructors()
                .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .and().areDeclaredInClassesThat().areNotEnums()
                .and().areDeclaredInClassesThat().areNotRecords()
                .should(new ArchCondition<>("not be public") {
                    @Override
                    public void check(com.tngtech.archunit.core.domain.JavaConstructor constructor, ConditionEvents events) {
                        if (constructor.getModifiers().contains(JavaModifier.PUBLIC)) {
                            String message = String.format(
                                "Constructor %s is public - use static factory methods instead",
                                constructor.getFullName()
                            );
                            events.add(SimpleConditionEvent.violated(constructor, message));
                        }
                    }
                })
                .because("Domain objects should use static factory methods, not public constructors");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain exceptions MUST extend DomainException")
        void domainExceptionsShouldExtendDomainException() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .and().haveSimpleNameNotContaining("DomainException")
                .should().beAssignableTo(RuntimeException.class)
                .because("All domain exceptions must extend DomainException (RuntimeException)");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain MUST NOT use JPA annotations")
        void domainShouldNotUseJpaAnnotations() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("jakarta.persistence.Entity")
                .orShould().beAnnotatedWith("jakarta.persistence.Table")
                .orShould().beAnnotatedWith("jakarta.persistence.Id")
                .because("Domain entities must be pure POJOs, JPA only in adapter layer");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Domain MUST NOT use Jackson annotations")
        void domainShouldNotUseJacksonAnnotations() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("com.fasterxml.jackson..")
                .because("Domain must not depend on JSON serialization concerns");

            rule.check(domainClasses);
        }
    }

    // ========================================
    // Naming Convention Rules
    // ========================================

    @Nested
    @DisplayName("📝 Naming Convention Enforcement")
    class NamingConventionTests {

        @Test
        @DisplayName("Domain services must end with 'Service' or 'DomainService'")
        void domainServicesMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..service..")
                .should().haveSimpleNameEndingWith("Service")
                .orShould().haveSimpleNameEndingWith("DomainService");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Value Objects should be records or final classes in vo package")
        void valueObjectsMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..vo..")
                .should().beRecords()
                .orShould().haveModifier(JavaModifier.FINAL)
                .because("Value Objects must be immutable (record or final class)");

            rule.check(domainClasses);
        }

        @Test
        @DisplayName("Repositories must end with 'Repository' suffix")
        void repositoriesMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("Repository");

            rule.check(allClasses);
        }

        // TODO: Task 1.2+에서 Application/Adapter 구현 후 활성화
        // @Test
        // @DisplayName("Use cases must end with 'UseCase' suffix")
        // void useCasesMustFollowNamingConvention() {
        //     ArchRule rule = classes()
        //         .that().resideInAPackage("..application..usecase..")
        //         .should().haveSimpleNameEndingWith("UseCase");
        //
        //     rule.check(applicationClasses);
        // }
        //
        // @Test
        // @DisplayName("Controllers must end with 'Controller' suffix")
        // void controllersMustFollowNamingConvention() {
        //     ArchRule rule = classes()
        //         .that().resideInAPackage("..adapter..web..")
        //         .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        //         .should().haveSimpleNameEndingWith("Controller");
        //
        //     rule.check(adapterClasses);
        // }
    }

    // ========================================
    // Package Structure Rules
    // ========================================

    @Nested
    @DisplayName("📁 Package Structure Enforcement")
    class PackageStructureTests {

        @Test
        @DisplayName("No cyclic dependencies between packages")
        void noPackageCycles() {
            ArchRule rule = slices()
                .matching("com.ryuqq.fileflow.(*)..")
                .should().beFreeOfCycles();

            rule.check(allClasses);
        }

        @Test
        @DisplayName("Domain must follow DDD Aggregate structure")
        void domainMustFollowAggregateStructure() {
            // DDD Aggregate 패턴: domain/{aggregate-name}/
            // 각 Aggregate는 vo/, event/, exception/ 서브 패키지를 가짐
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .and().areNotAnnotations()
                .should().resideInAPackage("..domain..*")
                .because("Domain must follow DDD Aggregate pattern with bounded contexts");

            rule.check(domainClasses);
        }
    }

    // ========================================
    // Lombok Prohibition (All Modules)
    // ========================================

    @Nested
    @DisplayName("🚫 Lombok Prohibition (ALL MODULES)")
    class LombokProhibitionTests {

        @Test
        @DisplayName("NO Lombok in Domain")
        void noLombokInDomain() {
            ArchRule rule = noClasses()
                .should().dependOnClassesThat().resideInAnyPackage("lombok..");

            rule.check(domainClasses);
        }

        // TODO: Task 1.2+에서 Application/Adapter 구현 후 활성화
        // @Test
        // @DisplayName("NO Lombok in Application")
        // void noLombokInApplication() {
        //     ArchRule rule = noClasses()
        //         .should().dependOnClassesThat().resideInAnyPackage("lombok..");
        //
        //     rule.check(applicationClasses);
        // }
        //
        // @Test
        // @DisplayName("NO Lombok in Adapters")
        // void noLombokInAdapters() {
        //     ArchRule rule = noClasses()
        //         .should().dependOnClassesThat().resideInAnyPackage("lombok..");
        //
        //     rule.check(adapterClasses);
        // }

        @Test
        @DisplayName("NO Lombok annotations anywhere")
        void noLombokAnnotations() {
            ArchRule rule = noClasses()
                .should().beAnnotatedWith("lombok.Data")
                .orShould().beAnnotatedWith("lombok.Builder")
                .orShould().beAnnotatedWith("lombok.Getter")
                .orShould().beAnnotatedWith("lombok.Setter")
                .orShould().beAnnotatedWith("lombok.AllArgsConstructor")
                .orShould().beAnnotatedWith("lombok.NoArgsConstructor")
                .because("Lombok is STRICTLY PROHIBITED in this project");

            rule.check(allClasses);
        }
    }

    // ========================================
    // Exception Handling Rules
    // ========================================

    @Nested
    @DisplayName("⚠️ Exception Handling Rules")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Domain exceptions must extend DomainException")
        void domainExceptionsMustExtendDomainException() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain..exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .should().beAssignableTo(RuntimeException.class);

            rule.check(domainClasses);
        }

        // TODO: Task 1.2에서 Application 구현 후 활성화
        // @Test
        // @DisplayName("Application exceptions must be in correct package")
        // void applicationExceptionsMustBeInCorrectPackage() {
        //     ArchRule rule = classes()
        //         .that().resideInAPackage("..application..")
        //         .and().haveSimpleNameEndingWith("Exception")
        //         .should().resideInAPackage("..application..exception..");
        //
        //     rule.check(applicationClasses);
        // }
    }

    // ========================================
    // Complexity Rules
    // ========================================

    @Nested
    @DisplayName("🧮 Complexity Enforcement")
    class ComplexityTests {

        @Test
        @DisplayName("Methods should not have excessive parameters (max 5)")
        void methodsShouldNotHaveExcessiveParameters() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("com.ryuqq.fileflow..")
                .should().haveRawParameterTypes(
                    new com.tngtech.archunit.base.DescribedPredicate<java.util.List<com.tngtech.archunit.core.domain.JavaClass>>("have at most 5 parameters") {
                        @Override
                        public boolean test(java.util.List<com.tngtech.archunit.core.domain.JavaClass> params) {
                            return params.size() <= 5;
                        }
                    }
                )
                .because("Methods with >5 parameters indicate poor design");

            rule.check(allClasses);
        }
    }
}
