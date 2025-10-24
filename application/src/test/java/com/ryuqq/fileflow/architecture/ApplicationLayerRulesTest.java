package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Application Layer 아키텍처 규칙 검증
 *
 * <p>Application Layer는 다음 규칙을 준수해야 합니다:
 * <ul>
 *   <li>Domain만 의존 가능</li>
 *   <li>UseCase 인터페이스는 *UseCase 네이밍 규칙</li>
 *   <li>@Transactional은 Public 메서드만</li>
 *   <li>@Transactional 메서드는 Final 금지</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-23
 * @see <a href="docs/coding_convention/03-application-layer/">Application Layer Conventions</a>
 */
@DisplayName("Application Layer 아키텍처 규칙 검증")
class ApplicationLayerRulesTest {

    private JavaClasses applicationClasses;

    @BeforeEach
    void setUp() {
        applicationClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ryuqq.fileflow.application");
    }

    @Test
    @DisplayName("Application Layer는 Domain만 의존 가능 - Adapter 의존 금지")
    void applicationLayerShouldOnlyDependOnDomain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..adapter.rest..",
                        "..adapter.web..",
                        "..adapter.persistence..",
                        "..adapter.external.."
                )
                .because("Application Layer는 Domain만 의존해야 하며, Adapter에 직접 의존하지 않아야 합니다.");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("UseCase 인터페이스는 *UseCase 네이밍 규칙 준수")
    void useCaseInterfacesShouldFollowNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAPackage("..application..port.in..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("UseCase")
                .because("UseCase 인터페이스는 *UseCase 네이밍 규칙을 따라야 합니다.");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("UseCase 인터페이스는 Public이어야 함")
    void useCaseInterfacesShouldBePublic() {
        ArchRule rule = classes()
                .that().resideInAPackage("..application..port.in..")
                .and().areInterfaces()
                .and().haveSimpleNameEndingWith("UseCase")
                .should().bePublic()
                .because("UseCase 인터페이스는 Public이어야 합니다.");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("@Transactional은 Public 메서드에만 사용 가능")
    void transactionalShouldOnlyBeUsedOnPublicMethods() {
        ArchRule rule = methods()
                .that().areAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .should().bePublic()
                .because("Spring AOP는 Public 메서드에서만 작동합니다. Private 메서드에 @Transactional은 작동하지 않습니다.");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("@Transactional 메서드는 Final 금지")
    void transactionalMethodsShouldNotBeFinal() {
        ArchRule rule = methods()
                .that().areAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .should().notBePackagePrivate()
                .andShould().bePublic()
                .because("Spring AOP는 Final 메서드를 프록시할 수 없으며, Public이어야 합니다.");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("@Transactional을 사용하는 클래스는 Public이어야 함")
    void transactionalClassesShouldBePublic() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .should().bePublic()
                .because("Spring AOP는 Public 클래스에서만 프록시가 정상 작동합니다.");

        rule.check(applicationClasses);
    }

    @Test
    @DisplayName("Application Service는 @Service 또는 @Component 어노테이션 사용")
    void applicationServicesShouldBeAnnotatedWithServiceOrComponent() {
        ArchRule rule = classes()
                .that().resideInAPackage("..application..service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith("org.springframework.stereotype.Service")
                .orShould().beAnnotatedWith("org.springframework.stereotype.Component")
                .because("Application Service는 @Service 또는 @Component로 Spring Bean으로 등록되어야 합니다.");

        rule.check(applicationClasses);
    }

}
