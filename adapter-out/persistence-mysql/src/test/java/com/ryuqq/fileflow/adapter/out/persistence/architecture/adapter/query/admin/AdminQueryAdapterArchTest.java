package com.ryuqq.fileflow.adapter.out.persistence.architecture.adapter.query.admin;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminQueryAdapterArchTest - Admin Query Adapter 아키텍처 규칙 검증 (6개 규칙)
 *
 * <p>admin-query-adapter-guide.md 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>일반 QueryAdapter와 차이점:</strong>
 *
 * <ul>
 *   <li>✅ AdminQueryDslRepository와 1:1 매핑
 *   <li>✅ DTO Projection 직접 반환 (Domain 아님)
 *   <li>✅ 메서드 제한 없음
 *   <li>✅ Join 허용
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("AdminQueryAdapter 아키텍처 규칙 검증 (Zero-Tolerance)")
class AdminQueryAdapterArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses adminQueryAdapterClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);

        // AdminQueryAdapter 클래스만
        adminQueryAdapterClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Admin Query Adapter 클래스",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("AdminQueryAdapter")
                                                && !javaClass.isInterface()));
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: AdminQueryAdapter는 클래스여야 합니다")
        void adminQueryAdapter_MustBeClass() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .notBeInterfaces()
                            .allowEmptyShould(true)
                            .because("Admin Query Adapter는 클래스로 정의되어야 합니다");

            rule.check(adminQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Component 어노테이션이 필수입니다")
        void adminQueryAdapter_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .allowEmptyShould(true)
                            .because("Admin Query Adapter는 @Component 어노테이션이 필수입니다");

            rule.check(adminQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-3: AdminQueryPort 인터페이스를 구현해야 합니다")
        void adminQueryAdapter_MustImplementPort() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .implement(
                                    DescribedPredicate.describe(
                                            "AdminQueryPort 인터페이스",
                                            javaClass ->
                                                    javaClass.getAllRawInterfaces().stream()
                                                            .anyMatch(
                                                                    iface ->
                                                                            iface.getSimpleName()
                                                                                    .endsWith(
                                                                                            "AdminQueryPort"))))
                            .allowEmptyShould(true)
                            .because("Admin Query Adapter는 AdminQueryPort 인터페이스를 구현해야 합니다");

            rule.check(adminQueryAdapterClasses);
        }
    }

    // ========================================================================
    // 2. 의존성 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("2. 의존성 규칙")
    class DependencyRules {

        @Test
        @DisplayName("규칙 2-1: AdminQueryDslRepository 의존성이 필수입니다")
        void adminQueryAdapter_MustDependOnAdminQueryDslRepository() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .dependOnClassesThat()
                            .haveSimpleNameEndingWith("AdminQueryDslRepository")
                            .allowEmptyShould(true)
                            .because(
                                    "Admin Query Adapter는 AdminQueryDslRepository 의존성이 필수입니다 (1:1"
                                            + " 매핑)");

            rule.check(adminQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-2: 다른 타입의 Repository 의존성이 금지됩니다")
        void adminQueryAdapter_MustNotDependOnOtherRepositories() {
            // JpaRepository, QueryDslRepository, LockRepository 등 금지
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .dependOnClassesThat(
                                    DescribedPredicate.describe(
                                            "AdminQueryDslRepository가 아닌 다른 Repository",
                                            javaClass ->
                                                    javaClass.getSimpleName().endsWith("Repository")
                                                            && !javaClass
                                                                    .getSimpleName()
                                                                    .endsWith(
                                                                            "AdminQueryDslRepository")))
                            .allowEmptyShould(true)
                            .because(
                                    "Admin Query Adapter는 AdminQueryDslRepository만 의존해야 합니다 (1:1 매핑"
                                            + " 원칙)");

            rule.check(adminQueryAdapterClasses);
        }
    }

    // ========================================================================
    // 3. 금지 사항 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("3. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 3-1: @Transactional 사용이 금지됩니다")
        void adminQueryAdapter_MustNotHaveTransactional() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("AdminQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .beAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because(
                                    "Admin Query Adapter는 @Transactional 사용이 금지됩니다 (Service Layer에서"
                                            + " 관리)");

            rule.check(adminQueryAdapterClasses);
        }
    }

    // ========================================================================
    // 참고: Admin Query Adapter 구현 예시
    // ========================================================================
    //
    // @Component
    // public class OrderAdminQueryAdapter implements OrderAdminQueryPort {
    //
    //     private final OrderAdminQueryDslRepository adminQueryDslRepository;
    //
    //     public OrderAdminQueryAdapter(OrderAdminQueryDslRepository adminQueryDslRepository) {
    //         this.adminQueryDslRepository = adminQueryDslRepository;
    //     }
    //
    //     @Override
    //     public List<AdminOrderListResponse> findList(AdminOrderSearchCriteria criteria) {
    //         return adminQueryDslRepository.findList(criteria);
    //     }
    //
    //     @Override
    //     public Optional<AdminOrderDetailResponse> findDetail(OrderId id) {
    //         return adminQueryDslRepository.findDetail(id.getValue());
    //     }
    // }
    //
}
