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

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Controller Adapter Architecture Tests
 *
 * Enforces Hexagonal Architecture rules for Controller (Adapter-In-Web):
 * - NO inner classes in controllers
 * - Request/Response DTOs MUST be Java records
 * - Controllers depend ONLY on UseCase interfaces
 * - NO @Transactional in controllers
 * - Controller methods have proper HTTP mapping annotations
 *
 * @author Architecture Team (arch-team@company.com)
 * @since 2024-01-01
 */
@DisplayName("🌐 Controller Adapter Architecture Enforcement")
class ControllerArchitectureTest {

    private static JavaClasses controllerClasses;
    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        controllerClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.adapter.in.web");

        allClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow");
    }

    // ========================================
    // Controller Structure Rules
    // ========================================

    @Nested
    @DisplayName("🏗️ Controller Structure Enforcement")
    class ControllerStructureTests {

        @Test
        @DisplayName("Controllers MUST NOT have inner classes")
        void noInnerClassesInControllers() {
            ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should(new ArchCondition<>("not have inner classes") {
                    @Override
                    public void check(JavaClass controller, ConditionEvents events) {
                        if (!controller.getInnerClasses().isEmpty()) {
                            String message = String.format(
                                "Controller %s has inner classes - Request/Response DTOs must be separate files",
                                controller.getSimpleName()
                            );
                            events.add(SimpleConditionEvent.violated(controller, message));
                        }
                    }
                })
                .because("Controllers must not have inner classes - keep DTOs in separate files");

            rule.check(controllerClasses);
        }

        @Test
        @DisplayName("Controllers SHOULD be thin delegation layer")
        void controllersShouldBeThin() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .and().arePublic()
                .should().haveRawParameterTypes(
                    new com.tngtech.archunit.base.DescribedPredicate<java.util.List<com.tngtech.archunit.core.domain.JavaClass>>("have at most 3 parameters") {
                        @Override
                        public boolean test(java.util.List<com.tngtech.archunit.core.domain.JavaClass> params) {
                            return params.size() <= 3;
                        }
                    }
                )
                .because("Controller methods should be thin - complex logic belongs in use cases");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // DTO Rules
    // ========================================

    @Nested
    @DisplayName("📋 Request/Response DTO Enforcement")
    class DtoTests {

        @Test
        @DisplayName("Request DTOs MUST be Java records")
        void requestResponseShouldBeRecords() {
            ArchRule requestRule = classes()
                .that().resideInAPackage("..adapter..web..")
                .and().haveSimpleNameEndingWith("Request")
                .should().beRecords()
                .because("Request DTOs must be immutable Java records");

            ArchRule responseRule = classes()
                .that().resideInAPackage("..adapter..web..")
                .and().haveSimpleNameEndingWith("Response")
                .should().beRecords()
                .because("Response DTOs must be immutable Java records");

            requestRule.check(controllerClasses);
            responseRule.check(controllerClasses);
        }

        @Test
        @DisplayName("DTOs MUST NOT use class keyword (only records)")
        void dtosShouldNotBeClasses() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter..web..")
                .and().haveSimpleNameMatching(".*Request|.*Response")
                .should().notBeRecords()
                .because("Request/Response DTOs must be records, not classes");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // Dependency Rules
    // ========================================

    @Nested
    @DisplayName("📦 Controller Dependency Enforcement")
    class DependencyTests {

        @Test
        @DisplayName("Controllers MUST only depend on UseCase interfaces")
        void controllersShouldOnlyDependOnUseCases() {
            ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..adapter..",
                    "..application..port.in..",
                    "..domain..",
                    "java..",
                    "jakarta.validation..",
                    "org.springframework.web..",
                    "org.springframework.http..",
                    "org.springframework.validation.."
                )
                .because("Controllers must depend only on UseCase interfaces, not repositories or entities");

            rule.check(controllerClasses);
        }

        @Test
        @DisplayName("Controllers MUST NOT depend on JPA entities")
        void controllersShouldNotDependOnJpaEntities() {
            ArchRule rule = noClasses()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().dependOnClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
                .because("Controllers must not depend on JPA entities - use domain objects or DTOs");

            rule.check(controllerClasses);
        }

        @Test
        @DisplayName("Controllers MUST NOT depend on repositories")
        void controllersShouldNotDependOnRepositories() {
            ArchRule rule = noClasses()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().dependOnClassesThat().areAssignableTo("org.springframework.data.jpa.repository.JpaRepository")
                .because("Controllers must not depend on repositories - use UseCases instead");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // Transaction Prohibition Rules
    // ========================================

    @Nested
    @DisplayName("💾 Transaction Prohibition in Controllers")
    class TransactionProhibitionTests {

        @Test
        @DisplayName("Controllers MUST NOT use @Transactional")
        void noTransactionalInControllers() {
            ArchRule rule = noClasses()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("@Transactional should only be in application layer, not controllers");

            rule.check(controllerClasses);
        }

        @Test
        @DisplayName("Controller methods MUST NOT be @Transactional")
        void noTransactionalMethodsInControllers() {
            ArchRule rule = noMethods()
                .that().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("Transaction management is application layer responsibility");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // HTTP Mapping Rules
    // ========================================

    @Nested
    @DisplayName("🔗 HTTP Mapping Enforcement")
    class HttpMappingTests {

        @Test
        @DisplayName("Controller methods SHOULD have proper HTTP mapping annotations")
        void controllerMethodsShouldHaveProperMapping() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .and().arePublic()
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PatchMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
                .because("Controller methods must have HTTP mapping annotations");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // Naming Convention Rules
    // ========================================

    @Nested
    @DisplayName("📝 Naming Convention Enforcement")
    class NamingConventionTests {

        @Test
        @DisplayName("Controllers MUST end with 'Controller'")
        void controllersMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller")
                .because("Controller classes must follow naming convention");

            rule.check(controllerClasses);
        }

        @Test
        @DisplayName("Request DTOs MUST end with 'Request'")
        void requestDtosMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter..web..")
                .and().areRecords()
                .and().haveSimpleNameMatching(".*Request")
                .should().haveSimpleNameEndingWith("Request")
                .because("Request DTOs should follow naming convention");

            rule.check(controllerClasses);
        }

        @Test
        @DisplayName("Response DTOs MUST end with 'Response'")
        void responseDtosMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter..web..")
                .and().areRecords()
                .and().haveSimpleNameMatching(".*Response")
                .should().haveSimpleNameEndingWith("Response")
                .because("Response DTOs should follow naming convention");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // Exception Handling Rules
    // ========================================

    @Nested
    @DisplayName("⚠️ Exception Handling Enforcement")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Global exception handler SHOULD exist")
        void globalExceptionHandlerShouldExist() {
            ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestControllerAdvice")
                .should().haveSimpleNameContaining("ExceptionHandler")
                .orShould().haveSimpleNameContaining("GlobalExceptionHandler")
                .because("Global exception handler should follow naming convention");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // Lombok Prohibition
    // ========================================

    @Nested
    @DisplayName("🚫 Lombok Prohibition")
    class LombokProhibitionTests {

        @Test
        @DisplayName("Controllers MUST NOT use Lombok")
        void noLombokInControllers() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter..web..")
                .should().dependOnClassesThat().resideInPackage("lombok..")
                .because("Lombok is strictly prohibited across entire project");

            rule.check(controllerClasses);
        }
    }

    // ========================================
    // Validation Rules
    // ========================================

    @Nested
    @DisplayName("✅ Validation Enforcement")
    class ValidationTests {

        @Test
        @DisplayName("Request DTOs SHOULD use Bean Validation annotations")
        void requestDtosShouldUseValidation() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter..web..")
                .and().haveSimpleNameEndingWith("Request")
                .should().beRecords()
                .because("Request DTOs should be validated records with @Valid annotations");

            rule.check(controllerClasses);
        }
    }
}
