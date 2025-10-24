package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Hexagonal Architecture (Ports & Adapters) 의존성 규칙 검증
 *
 * <p>검증 규칙:
 * <ul>
 *   <li>Domain Layer: 외부 의존성 없음 (순수 비즈니스 로직)</li>
 *   <li>Application Layer: Domain만 의존 가능</li>
 *   <li>Adapter Layer: Application/Domain 의존 가능</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-23
 * @see <a href="docs/coding_convention/05-testing/archunit-rules/01_layer-dependency-rules.md">Layer Dependency Rules</a>
 */
@DisplayName("Hexagonal Architecture 의존성 규칙 검증")
class HexagonalArchitectureTest {

    private JavaClasses importedClasses;

    @BeforeEach
    void setUp() {
        // 프로젝트 전체 클래스 로드 (테스트 제외)
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ryuqq.fileflow");
    }

    @Test
    @DisplayName("헥사고날 아키텍처 레이어 의존성 규칙 - Domain은 외부 의존 금지")
    void hexagonalArchitectureLayersShouldRespectDependencies() {
        // Given: Hexagonal Architecture 레이어 정의 (Application 모듈은 Domain과 Application만 검증)
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()  // Java 기본 클래스 의존성 무시

                // Layer 정의 (Application 모듈에 존재하는 레이어만 정의)
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")

                // 의존성 규칙
                .whereLayer("Domain").mayNotAccessAnyLayer()
                .whereLayer("Application").mayOnlyAccessLayers("Domain");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain Layer는 외부 프레임워크 의존성 금지 - Spring, Jakarta EE 등")
    void domainLayerShouldNotDependOnExternalFrameworks() {
        // Given: Domain Layer 클래스들
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.transaction..",
                        "jakarta.validation..",
                        "com.fasterxml.jackson..",
                        "org.hibernate.."
                )
                .because("Domain Layer는 순수 비즈니스 로직만 포함해야 하며, 외부 프레임워크에 의존하지 않아야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Application Layer는 Domain만 의존 가능 - Adapter 의존 금지")
    void applicationLayerShouldOnlyDependOnDomain() {
        // Given: Application Layer 클래스들
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..adapter.rest..",
                        "..adapter.web..",
                        "..adapter.persistence..",
                        "..adapter.external.."
                )
                .because("Application Layer는 Domain만 의존해야 하며, Adapter에 직접 의존하지 않아야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // Note: Application 모듈에는 Adapter 레이어가 없으므로 Adapter 간 의존성 테스트는 제거됨
    // Adapter 간 의존성 테스트는 bootstrap-web-api 모듈에서 수행됨

    @Test
    @DisplayName("순환 의존성 금지 - 모든 레이어")
    void noCircularDependenciesBetweenLayers() {
        // Given: 순환 의존성 검증 규칙
        ArchRule rule = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                .matching("com.ryuqq.fileflow.(*)..")
                .should().beFreeOfCycles()
                .because("레이어 간 순환 의존성은 허용되지 않습니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }
}
