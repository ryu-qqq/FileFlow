package com.ryuqq.fileflow.adapter.out.persistence.architecture.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * QueryDslRepositoryArchTest - QueryDSL Repository 아키텍처 규칙 검증 (9개 규칙)
 *
 * <p>querydsl-repository-guide.md의 핵심 규칙을 ArchUnit으로 검증합니다.</p>
 *
 * <p><strong>QueryDSL Repository 역할:</strong></p>
 * <ul>
 *   <li>일반 Query 전용 (4개 메서드: findById, existsById, findByCriteria, countByCriteria)</li>
 *   <li>Join 금지 (N+1은 Adapter에서 해결)</li>
 *   <li>Entity 반환 (Domain 변환은 Adapter에서)</li>
 * </ul>
 *
 * <p><strong>검증 그룹:</strong></p>
 * <ul>
 *   <li>클래스 구조 규칙 (3개)</li>
 *   <li>메서드 규칙 (2개)</li>
 *   <li>금지 사항 규칙 (3개)</li>
 *   <li>네이밍 규칙 (1개)</li>
 * </ul>
 *
 * <p><strong>Admin QueryDslRepository와 차이점:</strong></p>
 * <ul>
 *   <li>일반: 4개 메서드 고정, Join 금지</li>
 *   <li>Admin: 메서드 자유, Join 허용, DTO Projection</li>
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 * @see <a href="docs/coding_convention/04-persistence-layer/mysql/repository/querydsl/querydsl-repository-guide.md">QueryDSL Repository Guide</a>
 */
@DisplayName("QueryDSL Repository 아키텍처 규칙 검증 (Zero-Tolerance)")
class QueryDslRepositoryArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses queryDslRepositoryClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

        // 일반 QueryDslRepository 클래스만 (Admin 제외)
        queryDslRepositoryClasses = allClasses.that(
            DescribedPredicate.describe(
                "일반 QueryDSL Repository 클래스 (*QueryDslRepository, Admin 제외)",
                javaClass -> javaClass.getSimpleName().endsWith("QueryDslRepository") &&
                    !javaClass.getSimpleName().contains("Admin") &&
                    !javaClass.isInterface()
            )
        );
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: QueryDslRepository는 클래스여야 합니다")
        void queryDslRepository_MustBeClass() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryDslRepository")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..repository..")
                .should().notBeInterfaces()
                .allowEmptyShould(true)
                .because("QueryDslRepository는 클래스로 정의되어야 합니다");

            rule.check(queryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Repository 어노테이션이 필수입니다")
        void queryDslRepository_MustHaveRepositoryAnnotation() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryDslRepository")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .allowEmptyShould(true)
                .because("QueryDslRepository는 @Repository 어노테이션이 필수입니다");

            rule.check(queryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-3: JPAQueryFactory 의존성이 필수입니다")
        void queryDslRepository_MustHaveJPAQueryFactory() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryDslRepository")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..repository..")
                .should().dependOnClassesThat().areAssignableTo(JPAQueryFactory.class)
                .allowEmptyShould(true)
                .because("QueryDslRepository는 JPAQueryFactory 의존성이 필수입니다");

            rule.check(queryDslRepositoryClasses);
        }
    }

    // ========================================================================
    // 2. 메서드 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("2. 메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("규칙 2-1: 4개 표준 메서드만 허용됩니다")
        void queryDslRepository_MustHaveOnlyStandardMethods() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryDslRepository")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .and().areDeclaredInClassesThat().resideInAPackage("..repository..")
                .and().arePublic()
                .and().areNotStatic()
                .and().doNotHaveName("equals")
                .and().doNotHaveName("hashCode")
                .and().doNotHaveName("toString")
                .should().haveName("findById")
                .orShould().haveName("existsById")
                .orShould().haveName("findByCriteria")
                .orShould().haveName("countByCriteria")
                .allowEmptyShould(true)
                .because("QueryDslRepository는 4개 표준 메서드만 허용됩니다 (findById, existsById, findByCriteria, countByCriteria)");

            rule.check(queryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 2-2: QType static final 필드가 필수입니다")
        void queryDslRepository_MustHaveStaticFinalQTypeField() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryDslRepository")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .and().haveNameMatching("^q[A-Z].*")
                .should().beStatic()
                .andShould().beFinal()
                .allowEmptyShould(true)
                .because("QType 필드는 static final이어야 합니다 (예: qOrder, qProduct)");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 3. 금지 사항 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("3. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 3-1: Join 사용이 금지됩니다 (의존성 검증)")
        void queryDslRepository_MustNotUseJoin() {
            // ⚠️ 주의: ArchUnit으로 Join 메서드 호출을 완벽히 검증하기 어려움
            // 코드 리뷰 및 PR 검증 필요
            //
            // 금지 패턴:
            // - .join(...), .leftJoin(...), .rightJoin(...), .innerJoin(...), .fetchJoin(...)
            //
            // ✅ 이 테스트는 JPAJoin 클래스 의존성만 검증합니다.

            ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("QueryDslRepository")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..repository..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.querydsl.jpa.impl.JPAJoin")
                .allowEmptyShould(true)
                .because("QueryDslRepository는 Join 사용이 금지됩니다 (N+1은 Adapter에서 해결)");

            rule.check(queryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 3-2: @Transactional 사용이 금지됩니다")
        void queryDslRepository_MustNotHaveTransactional() {
            ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("QueryDslRepository")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..repository..")
                .should().beAnnotatedWith(Transactional.class)
                .allowEmptyShould(true)
                .because("QueryDslRepository는 @Transactional 사용이 금지됩니다 (Service Layer에서 관리)");

            rule.check(queryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 3-3: Mapper 의존성이 금지됩니다")
        void queryDslRepository_MustNotDependOnMapper() {
            ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("QueryDslRepository")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..repository..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Mapper")
                .allowEmptyShould(true)
                .because("QueryDslRepository는 Mapper 의존성이 금지됩니다 (Adapter에서 처리)");

            rule.check(queryDslRepositoryClasses);
        }
    }

    // ========================================================================
    // 4. 네이밍 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("4. 네이밍 규칙")
    class NamingRules {

        @Test
        @DisplayName("규칙 4-1: *QueryDslRepository 네이밍 규칙을 따라야 합니다")
        void queryDslRepository_MustFollowNamingConvention() {
            // 일반 QueryDslRepository만 검증 (Admin, Lock, Jdbc 제외)
            ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areAnnotatedWith(Repository.class)
                .and().areNotInterfaces()
                .and().haveSimpleNameContaining("QueryDsl")
                .and().haveSimpleNameNotContaining("Admin")
                .should().haveSimpleNameEndingWith("QueryDslRepository")
                .allowEmptyShould(true)
                .because("일반 QueryDslRepository는 *QueryDslRepository 네이밍 규칙을 따라야 합니다");

            rule.check(allClasses);
        }
    }
}
