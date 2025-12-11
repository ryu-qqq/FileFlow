package com.ryuqq.fileflow.adapter.out.persistence.architecture.repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminQueryDslRepositoryArchTest - Admin QueryDSL Repository 아키텍처 규칙 검증 (6개 규칙)
 *
 * <p>admin-querydsl-repository-archunit.md의 핵심 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>Admin QueryDSL Repository 역할:</strong>
 *
 * <ul>
 *   <li>관리자용 복잡 조회 전용
 *   <li>Join 허용 (Long FK 기반 명시적 조인)
 *   <li>DTO Projection 권장
 *   <li>메서드 제한 없음 (자유로운 메서드 정의)
 * </ul>
 *
 * <p><strong>일반 QueryDslRepository와 차이점:</strong>
 *
 * <ul>
 *   <li>일반: 4개 메서드 고정, Join 금지
 *   <li>Admin: 메서드 자유, Join 허용, DTO Projection
 * </ul>
 *
 * <p><strong>검증 그룹:</strong>
 *
 * <ul>
 *   <li>클래스 구조 규칙 (3개)
 *   <li>금지 사항 규칙 (2개)
 *   <li>네이밍 규칙 (1개)
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 * @see <a
 *     href="docs/coding_convention/04-persistence-layer/mysql/repository/admin/admin-querydsl-repository-archunit.md">Admin
 *     QueryDSL Repository ArchUnit Guide</a>
 */
@DisplayName("Admin QueryDSL Repository 아키텍처 규칙 검증 (Zero-Tolerance)")
class AdminQueryDslRepositoryArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses adminQueryDslRepositoryClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);

        // AdminQueryDslRepository 클래스만
        adminQueryDslRepositoryClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Admin QueryDSL Repository 클래스 (*AdminQueryDslRepository)",
                                javaClass ->
                                        javaClass
                                                        .getSimpleName()
                                                        .endsWith("AdminQueryDslRepository")
                                                && !javaClass.isInterface()));
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: AdminQueryDslRepository는 클래스여야 합니다")
        void adminQueryDslRepository_MustBeClass() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .notBeInterfaces()
                            .allowEmptyShould(true)
                            .because("Admin QueryDSL Repository는 클래스로 정의되어야 합니다");

            rule.check(adminQueryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Repository 어노테이션이 필수입니다")
        void adminQueryDslRepository_MustHaveRepositoryAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beAnnotatedWith(Repository.class)
                            .allowEmptyShould(true)
                            .because("Admin QueryDSL Repository는 @Repository 어노테이션이 필수입니다");

            rule.check(adminQueryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-3: JPAQueryFactory 의존성이 필수입니다")
        void adminQueryDslRepository_MustHaveJPAQueryFactory() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .dependOnClassesThat()
                            .areAssignableTo(JPAQueryFactory.class)
                            .allowEmptyShould(true)
                            .because("Admin QueryDSL Repository는 JPAQueryFactory 의존성이 필수입니다");

            rule.check(adminQueryDslRepositoryClasses);
        }
    }

    // ========================================================================
    // 2. 금지 사항 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("2. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 2-1: @Transactional 사용이 금지됩니다")
        void adminQueryDslRepository_MustNotHaveTransactional() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because(
                                    "Admin QueryDSL Repository는 @Transactional 사용이 금지됩니다 (Service"
                                            + " Layer에서 관리)");

            rule.check(adminQueryDslRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 2-2: Mapper 의존성이 금지됩니다")
        void adminQueryDslRepository_MustNotDependOnMapper() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .dependOnClassesThat()
                            .haveSimpleNameEndingWith("Mapper")
                            .allowEmptyShould(true)
                            .because(
                                    "Admin QueryDSL Repository는 Mapper 의존성이 금지됩니다 (DTO Projection"
                                            + " 직접 사용 또는 Adapter에서 처리)");

            rule.check(adminQueryDslRepositoryClasses);
        }
    }

    // ========================================================================
    // 3. 네이밍 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("3. 네이밍 규칙")
    class NamingRules {

        @Test
        @DisplayName("규칙 3-1: *AdminQueryDslRepository 네이밍 규칙을 따라야 합니다")
        void adminQueryDslRepository_MustFollowNamingConvention() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..repository..")
                            .and()
                            .areAnnotatedWith(Repository.class)
                            .and()
                            .areNotInterfaces()
                            .and()
                            .haveSimpleNameContaining("Admin")
                            .and()
                            .haveSimpleNameContaining("QueryDsl")
                            .should()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .allowEmptyShould(true)
                            .because(
                                    "Admin QueryDSL Repository는 *AdminQueryDslRepository 네이밍 규칙을"
                                            + " 따라야 합니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 참고: Admin QueryDslRepository에서 허용되는 사항
    // ========================================================================
    //
    // ✅ Join 허용 (Long FK 기반 명시적 조인)
    // ✅ 메서드 제한 없음 (자유로운 메서드 정의)
    // ✅ DTO Projection 권장
    //
    // 예시:
    // @Repository
    // public class OrderAdminQueryDslRepository {
    //     public List<AdminOrderResponse> findOrderListWithMember(AdminOrderListQuery criteria) {
    //         return queryFactory
    //             .select(Projections.constructor(
    //                 AdminOrderResponse.class,
    //                 qOrder.id,
    //                 qOrder.orderNumber,
    //                 qMember.name,
    //                 qMember.email
    //             ))
    //             .from(qOrder)
    //             .leftJoin(qMember).on(qOrder.memberId.eq(qMember.id))  // ✅ Join 허용
    //             .where(buildConditions(criteria))
    //             .fetch();
    //     }
    // }
    //
}
