package com.ryuqq.fileflow.adapter.out.persistence.architecture.repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

/**
 * LockRepositoryArchTest - Lock Repository 아키텍처 규칙 검증 (6개 규칙)
 *
 * <p>lock-repository-archunit.md의 핵심 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>Lock Repository 역할:</strong>
 *
 * <ul>
 *   <li>동시성 제어 전용 (Pessimistic/Optimistic Lock)
 *   <li>Lock 관련 메서드만 허용 (ForUpdate, ForShare, WithLock)
 *   <li>일반 조회/Command 메서드 금지
 * </ul>
 *
 * <p><strong>사용 케이스:</strong>
 *
 * <ul>
 *   <li>재고 차감 (PESSIMISTIC_WRITE)
 *   <li>포인트 잔액 (PESSIMISTIC_WRITE)
 *   <li>좌석 예약 (PESSIMISTIC_WRITE + 타임아웃)
 *   <li>조회 후 검증 (PESSIMISTIC_READ / FOR SHARE)
 * </ul>
 *
 * <p><strong>검증 그룹:</strong>
 *
 * <ul>
 *   <li>클래스 구조 규칙 (3개)
 *   <li>메서드 규칙 (2개)
 *   <li>네이밍 규칙 (1개)
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 * @see <a
 *     href="docs/coding_convention/04-persistence-layer/mysql/repository/lock/lock-repository-archunit.md">Lock
 *     Repository ArchUnit Guide</a>
 */
@DisplayName("Lock Repository 아키텍처 규칙 검증 (Zero-Tolerance)")
class LockRepositoryArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses lockRepositoryClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);

        // LockRepository 클래스만
        lockRepositoryClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Lock Repository 클래스 (*LockRepository)",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("LockRepository")
                                                && !javaClass.isInterface()));
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: LockRepository는 클래스여야 합니다")
        void lockRepository_MustBeClass() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .notBeInterfaces()
                            .allowEmptyShould(true)
                            .because("Lock Repository는 클래스로 정의되어야 합니다");

            rule.check(lockRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Repository 어노테이션이 필수입니다")
        void lockRepository_MustHaveRepositoryAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .beAnnotatedWith(Repository.class)
                            .allowEmptyShould(true)
                            .because("Lock Repository는 @Repository 어노테이션이 필수입니다");

            rule.check(lockRepositoryClasses);
        }

        @Test
        @DisplayName("규칙 1-3: JPAQueryFactory 또는 EntityManager 의존성이 필수입니다")
        void lockRepository_MustHaveQueryFactoryOrEntityManager() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockRepository")
                            .and()
                            .resideInAPackage("..repository..")
                            .should()
                            .dependOnClassesThat()
                            .areAssignableTo(JPAQueryFactory.class)
                            .orShould()
                            .dependOnClassesThat()
                            .areAssignableTo(EntityManager.class)
                            .allowEmptyShould(true)
                            .because(
                                    "Lock Repository는 JPAQueryFactory 또는 EntityManager 의존성이 필수입니다");

            rule.check(lockRepositoryClasses);
        }
    }

    // ========================================================================
    // 2. 메서드 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("2. 메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("규칙 2-1: Lock 관련 메서드만 허용됩니다")
        void lockRepository_MustHaveOnlyLockMethods() {
            // Lock 메서드 패턴 (통일된 네이밍):
            // - ForUpdate: PESSIMISTIC_WRITE (SELECT ... FOR UPDATE)
            // - ForShare: PESSIMISTIC_READ (SELECT ... FOR SHARE)
            // - WithOptimisticLock: OPTIMISTIC (@Version)
            // 예: findByIdForUpdate, findByCriteriaForUpdate, findByIdForShare,
            // findByIdWithOptimisticLock
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockRepository")
                            .and()
                            .areDeclaredInClassesThat()
                            .resideInAPackage("..repository..")
                            .and()
                            .arePublic()
                            .and()
                            .areNotStatic()
                            .and()
                            .doNotHaveName("equals")
                            .and()
                            .doNotHaveName("hashCode")
                            .and()
                            .doNotHaveName("toString")
                            .should()
                            .haveNameMatching(".*ForUpdate.*|.*ForShare.*|.*WithOptimisticLock.*")
                            .allowEmptyShould(true)
                            .because(
                                    "Lock Repository는 Lock 관련 메서드만 허용됩니다 (ForUpdate, ForShare,"
                                            + " WithOptimisticLock 패턴)");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("규칙 2-2: 일반 조회/Command 메서드가 금지됩니다")
        void lockRepository_MustNotHaveNonLockMethods() {
            // 일반 메서드 패턴: save, delete, findById (Lock 접미사 없는), findAll, existsById, count
            ArchRule rule =
                    noMethods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockRepository")
                            .and()
                            .haveNameMatching(
                                    "^save$|^delete.*|^findById$|^findAll$|^existsById$|^count$")
                            .should()
                            .beDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockRepository")
                            .allowEmptyShould(true)
                            .because(
                                    "Lock Repository는 일반 조회/Command 메서드가 금지됩니다 (JpaRepository,"
                                            + " QueryDslRepository에서 처리)");

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
        @DisplayName("규칙 3-1: *LockRepository 네이밍 규칙을 따라야 합니다")
        void lockRepository_MustFollowNamingConvention() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..repository..")
                            .and()
                            .areAnnotatedWith(Repository.class)
                            .and()
                            .areNotInterfaces()
                            .and()
                            .haveSimpleNameContaining("Lock")
                            .should()
                            .haveSimpleNameEndingWith("LockRepository")
                            .allowEmptyShould(true)
                            .because("Lock Repository는 *LockRepository 네이밍 규칙을 따라야 합니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 참고: Lock Repository 구현 예시 (통일된 네이밍)
    // ========================================================================
    //
    // @Repository
    // @RequiredArgsConstructor
    // public class OrderLockRepository {
    //
    //     private final JPAQueryFactory queryFactory;
    //     private static final QOrderJpaEntity qOrder = QOrderJpaEntity.orderJpaEntity;
    //
    //     // ================================================================
    //     // Pessimistic Write Lock (FOR UPDATE)
    //     // ================================================================
    //
    //     /**
    //      * ID로 단건 조회 (FOR UPDATE)
    //      * - 재고 차감, 포인트 사용 등 배타적 쓰기 락
    //      */
    //     public Optional<OrderJpaEntity> findByIdForUpdate(Long id) {
    //         return Optional.ofNullable(
    //             queryFactory.selectFrom(qOrder)
    //                 .where(qOrder.id.eq(id))
    //                 .setLockMode(LockModeType.PESSIMISTIC_WRITE)
    //                 .fetchOne()
    //         );
    //     }
    //
    //     /**
    //      * Criteria로 목록 조회 (FOR UPDATE)
    //      */
    //     public List<OrderJpaEntity> findByCriteriaForUpdate(OrderSearchCriteria criteria) {
    //         return queryFactory.selectFrom(qOrder)
    //             .where(buildConditions(criteria))
    //             .setLockMode(LockModeType.PESSIMISTIC_WRITE)
    //             .fetch();
    //     }
    //
    //     // ================================================================
    //     // Pessimistic Read Lock (FOR SHARE)
    //     // ================================================================
    //
    //     /**
    //      * ID로 단건 조회 (FOR SHARE)
    //      * - 조회 후 검증용, 공유 읽기 락
    //      */
    //     public Optional<OrderJpaEntity> findByIdForShare(Long id) {
    //         return Optional.ofNullable(
    //             queryFactory.selectFrom(qOrder)
    //                 .where(qOrder.id.eq(id))
    //                 .setLockMode(LockModeType.PESSIMISTIC_READ)
    //                 .fetchOne()
    //         );
    //     }
    //
    //     /**
    //      * Criteria로 목록 조회 (FOR SHARE)
    //      */
    //     public List<OrderJpaEntity> findByCriteriaForShare(OrderSearchCriteria criteria) {
    //         return queryFactory.selectFrom(qOrder)
    //             .where(buildConditions(criteria))
    //             .setLockMode(LockModeType.PESSIMISTIC_READ)
    //             .fetch();
    //     }
    //
    //     // ================================================================
    //     // Optimistic Lock (@Version)
    //     // ================================================================
    //
    //     /**
    //      * ID로 단건 조회 (Optimistic Lock)
    //      * - @Version 필드 활용, 업데이트 시 OptimisticLockException 발생 가능
    //      */
    //     public Optional<OrderJpaEntity> findByIdWithOptimisticLock(Long id) {
    //         return Optional.ofNullable(
    //             queryFactory.selectFrom(qOrder)
    //                 .where(qOrder.id.eq(id))
    //                 .setLockMode(LockModeType.OPTIMISTIC)
    //                 .fetchOne()
    //         );
    //     }
    //
    //     /**
    //      * Criteria로 목록 조회 (Optimistic Lock)
    //      */
    //     public List<OrderJpaEntity> findByCriteriaWithOptimisticLock(OrderSearchCriteria
    // criteria) {
    //         return queryFactory.selectFrom(qOrder)
    //             .where(buildConditions(criteria))
    //             .setLockMode(LockModeType.OPTIMISTIC)
    //             .fetch();
    //     }
    // }
    //
}
