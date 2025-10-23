package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Domain Layer 아키텍처 규칙 검증
 *
 * <p>Domain Layer는 다음 규칙을 준수해야 합니다:
 * <ul>
 *   <li>외부 프레임워크 의존성 금지 (Spring, JPA, Jackson 등)</li>
 *   <li>순수 비즈니스 로직만 포함</li>
 *   <li>Aggregate, Entity, Value Object, Domain Event 패턴</li>
 *   <li>Lombok 사용 금지</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-23
 * @see <a href="docs/coding_convention/02-domain-layer/">Domain Layer Conventions</a>
 */
@DisplayName("Domain Layer 아키텍처 규칙 검증")
class DomainLayerRulesTest {

    private JavaClasses domainClasses;

    @BeforeEach
    void setUp() {
        domainClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ryuqq.fileflow.domain");
    }

    @Test
    @DisplayName("Domain Layer는 Spring Framework 의존 금지")
    void domainLayerShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "org.springframework.boot..",
                        "org.springframework.context..",
                        "org.springframework.transaction.."
                )
                .because("Domain Layer는 순수 비즈니스 로직만 포함해야 하며 Spring에 의존하지 않아야 합니다.");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 Jakarta Persistence (JPA) 의존 금지")
    void domainLayerShouldNotDependOnJPA() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "javax.persistence.."
                )
                .because("Domain Layer는 JPA에 의존하지 않아야 합니다. Long FK 전략을 사용하세요.");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 Jackson 의존 금지")
    void domainLayerShouldNotDependOnJackson() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.fasterxml.jackson.."
                )
                .because("Domain Layer는 JSON 직렬화 라이브러리에 의존하지 않아야 합니다.");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 Lombok 사용 금지")
    void domainLayerShouldNotUseLombok() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "lombok.."
                )
                .because("Domain Layer에서 Lombok 사용은 금지되어 있습니다. Pure Java를 사용하세요.");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 Application/Adapter Layer 의존 금지")
    void domainLayerShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..",
                        "..adapter.."
                )
                .because("Domain Layer는 외부 레이어에 의존하지 않아야 합니다 (의존성 역전).");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Entity는 @Entity 어노테이션 사용 금지 (Long FK 전략)")
    void domainEntitiesShouldNotUseJPAEntityAnnotation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("jakarta.persistence.Entity")
                .orShould().beAnnotatedWith("javax.persistence.Entity")
                .because("Domain Entity는 JPA @Entity 어노테이션을 사용하지 않습니다. Long FK 전략을 사용하세요.");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 @Transactional 사용 금지")
    void domainLayerShouldNotUseTransactional() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .orShould().beAnnotatedWith("jakarta.transaction.Transactional")
                .because("@Transactional은 Application Layer에서만 사용해야 합니다.");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain 패키지는 'domain'으로 명명되어야 함")
    void domainPackageShouldBeNamedCorrectly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .should().resideInAPackage("..domain..")
                .because("Domain Layer 클래스는 'domain' 패키지에 위치해야 합니다.");

        rule.check(domainClasses);
    }
}
