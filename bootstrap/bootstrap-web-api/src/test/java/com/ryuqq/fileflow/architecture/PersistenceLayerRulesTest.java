package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Persistence Layer 아키텍처 규칙 검증
 *
 * <p>Persistence Layer는 다음 규칙을 준수해야 합니다:
 * <ul>
 *   <li>Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>Entity 불변성 (Setter 금지)</li>
 *   <li>Adapter에서 @Transactional 금지</li>
 *   <li>Lombok 사용 금지</li>
 *   <li>Domain만 의존 가능</li>
 * </ul>
 *
 * @author windsurf
 * @since 2025-10-28
 * @see <a href="docs/coding_convention/04-persistence-layer/">Persistence Layer Conventions</a>
 */
@DisplayName("Persistence Layer 아키텍처 규칙 검증")
class PersistenceLayerRulesTest {

    private JavaClasses persistenceClasses;

    @BeforeEach
    void setUp() {
        persistenceClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ryuqq.fileflow.adapter.out.persistence");
    }

    // ========================================
    // Long FK 전략, Entity 불변성, Adapter/Mapper 규칙
    // ========================================
    //
    // 다음 규칙들은 더 상세한 컨벤션 테스트로 이동되었습니다:
    // - JPA Entity 규칙 (Long FK, @Entity, @Table 등) → JpaEntityConventionTest
    // - Entity Mapper 규칙 (@Component 금지, Utility Class 등) → MapperConventionTest
    // - Repository Adapter 규칙 (@Component, @Transactional 등) → RepositoryAdapterConventionTest
    //
    // 이 파일은 Persistence Layer 전반의 공통 규칙만 포함합니다.

    // ========================================
    // 스프링 어노테이션 검증 (QueryDSL Repository만)
    // ========================================

    @Test
    @DisplayName("QueryDSL Repository는 @Repository 어노테이션 사용")
    void queryDslRepositoriesShouldBeAnnotatedWithRepository() {
        ArchRule rule = classes()
                .that().resideInAPackage("..adapter.out.persistence..querydsl..")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beAnnotatedWith("org.springframework.stereotype.Repository")
                .allowEmptyShould(true)  // QueryDSL Repository가 없어도 테스트 통과
                .because("QueryDSL Repository는 @Repository로 Spring Bean으로 등록되어야 합니다.");

        rule.check(persistenceClasses);
    }

    // ========================================
    // Lombok 금지 검증
    // ========================================

    @Test
    @DisplayName("Persistence Layer는 Lombok 사용 금지")
    void persistenceLayerShouldNotUseLombok() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.out.persistence..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "lombok.."
                )
                .because("Persistence Layer에서 Lombok 사용은 금지되어 있습니다. Pure Java를 사용하세요.");

        rule.check(persistenceClasses);
    }

    // ========================================
    // 의존성 검증
    // ========================================

    @Test
    @DisplayName("Persistence Layer는 Domain만 의존 가능 - Web/External Adapter 의존 금지")
    void persistenceLayerShouldOnlyDependOnDomain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.out.persistence..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..adapter.in.web..",
                        "..adapter.in.rest..",
                        "..adapter.out.external.."
                )
                .because("Persistence Layer는 Domain과 Application만 의존해야 하며, 다른 Adapter에 직접 의존하지 않아야 합니다.");

        rule.check(persistenceClasses);
    }

    // ========================================
    // 패키지 구조 검증 (QueryDSL과 Spring Data JPA만)
    // ========================================

    @Test
    @DisplayName("Spring Data JPA Repository는 repository 패키지에 위치")
    void springDataRepositoriesShouldResideInRepositoryPackage() {
        ArchRule rule = classes()
                .that().areAssignableTo("org.springframework.data.jpa.repository.JpaRepository")
                .should().resideInAPackage("..repository..")
                .because("Spring Data JPA Repository는 repository 패키지에 위치해야 합니다.");

        rule.check(persistenceClasses);
    }

    @Test
    @DisplayName("QueryDSL Repository는 querydsl 패키지에 위치")
    void queryDslRepositoriesShouldResideInQueryDslPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameContaining("QueryDsl")
                .and().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..querydsl..")
                .allowEmptyShould(true)  // QueryDSL Repository가 없어도 테스트 통과
                .because("QueryDSL Repository는 querydsl 패키지에 위치하여 관심사를 분리해야 합니다.");

        rule.check(persistenceClasses);
    }

    // ========================================
    // 네이밍 규칙 검증 (QueryDSL만)
    // ========================================

    @Test
    @DisplayName("QueryDSL Repository는 *QueryDslRepository 네이밍 규칙 준수")
    void queryDslRepositoriesShouldFollowNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAPackage("..querydsl..")
                .and().haveSimpleNameContaining("QueryDsl")
                .should().haveSimpleNameEndingWith("Repository")
                .allowEmptyShould(true)  // QueryDSL Repository가 없어도 테스트 통과
                .because("QueryDSL Repository는 *QueryDslRepository 네이밍 규칙을 따라야 합니다.");

        rule.check(persistenceClasses);
    }
}
