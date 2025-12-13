package com.ryuqq.fileflow.adapter.out.persistence.architecture.repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * JpaRepositoryArchTest - JPA Repository 아키텍처 규칙 검증 (7개 규칙)
 *
 * <p>jpa-repository-guide.md의 핵심 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>JPA Repository 역할:</strong>
 *
 * <ul>
 *   <li>Command 전용 (save, delete, deleteById)
 *   <li>Merge 기반 업데이트 (DDD 순수성 유지)
 *   <li>순수 JpaRepository 상속만 허용
 * </ul>
 *
 * <p><strong>검증 그룹:</strong>
 *
 * <ul>
 *   <li>인터페이스 구조 규칙 (2개)
 *   <li>금지 사항 규칙 (4개)
 *   <li>네이밍 규칙 (1개)
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 * @see <a
 *     href="docs/coding_convention/04-persistence-layer/mysql/repository/jpa/jpa-repository-guide.md">JPA
 *     Repository Guide</a>
 */
@DisplayName("JPA Repository 아키텍처 규칙 검증 (Zero-Tolerance)")
class JpaRepositoryArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses jpaRepositoryClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);

        // JpaRepository 인터페이스만 (QueryDsl, Admin, Lock, Jdbc 제외)
        jpaRepositoryClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "JPA Repository 인터페이스 (*Repository, QueryDsl/Admin/Lock/Jdbc 제외)",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("Repository")
                                                && !javaClass.getSimpleName().contains("QueryDsl")
                                                && !javaClass.getSimpleName().contains("Admin")
                                                && !javaClass.getSimpleName().contains("Lock")
                                                && !javaClass.getSimpleName().contains("Jdbc")
                                                && javaClass.isInterface()));
    }

    // ========================================================================
    // 1. 인터페이스 구조 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("1. 인터페이스 구조 규칙")
    class InterfaceStructureRules {

        @Test
        @DisplayName("규칙 1-1: JpaRepository는 인터페이스여야 합니다")
        void jpaRepository_MustBeInterface() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("Repository")
                            .and()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .and()
                            .haveSimpleNameNotContaining("Admin")
                            .and()
                            .haveSimpleNameNotContaining("Lock")
                            .and()
                            .haveSimpleNameNotContaining("Jdbc")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beInterfaces()
                            .allowEmptyShould(true)
                            .because("JpaRepository는 인터페이스로 정의되어야 합니다 (Spring Data JPA 표준)");

            rule.check(jpaRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-2: JpaRepository 상속이 필수입니다")
        void jpaRepository_MustExtendJpaRepository() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("Repository")
                            .and()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .and()
                            .haveSimpleNameNotContaining("Admin")
                            .and()
                            .haveSimpleNameNotContaining("Lock")
                            .and()
                            .haveSimpleNameNotContaining("Jdbc")
                            .and()
                            .areInterfaces()
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beAssignableTo(JpaRepository.class)
                            .allowEmptyShould(true)
                            .because("JpaRepository 인터페이스를 상속해야 합니다");

            rule.check(jpaRepositoryClasses);
        }
    }

    // ========================================================================
    // 2. 금지 사항 규칙 (4개)
    // ========================================================================

    @Nested
    @DisplayName("2. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 2-1: QuerydslPredicateExecutor 상속이 금지됩니다")
        void jpaRepository_MustNotExtendQuerydslPredicateExecutor() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("Repository")
                            .and()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .and()
                            .haveSimpleNameNotContaining("Admin")
                            .and()
                            .haveSimpleNameNotContaining("Lock")
                            .and()
                            .haveSimpleNameNotContaining("Jdbc")
                            .and()
                            .areInterfaces()
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beAssignableTo(QuerydslPredicateExecutor.class)
                            .allowEmptyShould(true)
                            .because(
                                    "JpaRepository는 QuerydslPredicateExecutor 상속이 금지됩니다 (순수 Command"
                                            + " 전용)");

            rule.check(jpaRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 2-2: Query Method 추가가 금지됩니다")
        void jpaRepository_MustNotHaveQueryMethods() {
            ArchRule rule =
                    noMethods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Repository")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("Admin")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("Lock")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("Jdbc")
                            .and()
                            .areDeclaredInClassesThat()
                            .areInterfaces()
                            .and()
                            .haveNameMatching("find.*|search.*|count.*|exists.*|get.*")
                            .should()
                            .beDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Repository")
                            .allowEmptyShould(true)
                            .because(
                                    "JpaRepository는 Query Method 추가가 금지됩니다 (QueryDslRepository"
                                            + " 사용)");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("규칙 2-3: @Query 어노테이션 사용이 금지됩니다")
        void jpaRepository_MustNotUseQueryAnnotation() {
            ArchRule rule =
                    noMethods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Repository")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("Admin")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("Lock")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameNotContaining("Jdbc")
                            .and()
                            .areDeclaredInClassesThat()
                            .areInterfaces()
                            .should()
                            .beAnnotatedWith(Query.class)
                            .allowEmptyShould(true)
                            .because("JpaRepository는 @Query 어노테이션 사용이 금지됩니다 (QueryDSL 사용)");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("규칙 2-4: Custom Repository 구현이 금지됩니다")
        void jpaRepository_MustNotHaveCustomImplementation() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("RepositoryImpl")
                            .and()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .and()
                            .haveSimpleNameNotContaining("Admin")
                            .and()
                            .haveSimpleNameNotContaining("Lock")
                            .and()
                            .haveSimpleNameNotContaining("Jdbc")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beTopLevelClasses()
                            .allowEmptyShould(true)
                            .because("Custom Repository 구현이 금지됩니다 (QueryDslRepository 사용)");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 3. 네이밍 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("3. 네이밍 규칙")
    class NamingRules {

        @Test
        @DisplayName("규칙 3-1: *Repository 네이밍 규칙을 따라야 합니다")
        void jpaRepository_MustFollowNamingConvention() {
            ArchRule rule =
                    classes()
                            .that()
                            .areInterfaces()
                            .and()
                            .areAssignableTo(JpaRepository.class)
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .haveSimpleNameEndingWith("Repository")
                            .andShould()
                            .haveSimpleNameNotContaining("QueryDsl")
                            .andShould()
                            .haveSimpleNameNotContaining("Admin")
                            .andShould()
                            .haveSimpleNameNotContaining("Lock")
                            .andShould()
                            .haveSimpleNameNotContaining("Jdbc")
                            .allowEmptyShould(true)
                            .because(
                                    "JpaRepository는 *Repository 네이밍 규칙을 따라야 합니다"
                                            + " (QueryDsl/Admin/Lock/Jdbc 제외)");

            rule.check(jpaRepositoryClasses);
        }
    }
}
