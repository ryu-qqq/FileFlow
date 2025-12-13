package com.ryuqq.fileflow.adapter.out.persistence.architecture.adapter.query.lock;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * LockQueryAdapterArchTest - Lock Query Adapter 아키텍처 규칙 검증 (24개 규칙)
 *
 * <p>lock-query-adapter-guide.md 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>통합 Lock 네이밍 컨벤션:</strong>
 *
 * <ul>
 *   <li>ForUpdate: PESSIMISTIC_WRITE (SELECT ... FOR UPDATE)
 *   <li>ForShare: PESSIMISTIC_READ (SELECT ... FOR SHARE)
 *   <li>WithOptimisticLock: OPTIMISTIC (@Version)
 * </ul>
 *
 * <p><strong>6개 필수 메서드:</strong>
 *
 * <ul>
 *   <li>findByIdForUpdate, findByCriteriaForUpdate
 *   <li>findByIdForShare, findByCriteriaForShare
 *   <li>findByIdWithOptimisticLock, findByCriteriaWithOptimisticLock
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("LockQueryAdapter 아키텍처 규칙 검증 (Zero-Tolerance)")
class LockQueryAdapterArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses lockQueryAdapterClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);

        lockQueryAdapterClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Lock Query Adapter 클래스",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("LockQueryAdapter")
                                                && !javaClass.isInterface()));
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (5개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: LockQueryAdapter는 클래스여야 합니다")
        void lockQueryAdapter_MustBeClass() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .notBeInterfaces()
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 클래스로 정의되어야 합니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Component 어노테이션이 필수입니다")
        void lockQueryAdapter_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 @Component 어노테이션이 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-3: LockQueryPort 인터페이스를 구현해야 합니다")
        void lockQueryAdapter_MustImplementLockQueryPort() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .implement(
                                    DescribedPredicate.describe(
                                            "LockQueryPort 인터페이스",
                                            javaClass ->
                                                    javaClass.getAllRawInterfaces().stream()
                                                            .anyMatch(
                                                                    iface ->
                                                                            iface.getSimpleName()
                                                                                    .endsWith(
                                                                                            "LockQueryPort"))))
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 LockQueryPort 인터페이스를 구현해야 합니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-4: 정확히 2개 필드만 허용됩니다 (LockRepository + Mapper)")
        void lockQueryAdapter_MustHaveExactlyTwoFields() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "정확히 2개의 필드",
                                                    javaClass ->
                                                            javaClass.getAllFields().size() == 2)))
                            .allowEmptyShould(true)
                            .because(
                                    "Lock Query Adapter는 정확히 2개의 필드(LockRepository, Mapper)만 가져야"
                                            + " 합니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-5: 모든 필드는 final이어야 합니다")
        void lockQueryAdapter_AllFieldsMustBeFinal() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .beFinal()
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter의 모든 필드는 final로 불변성을 보장해야 합니다");

            rule.check(lockQueryAdapterClasses);
        }
    }

    // ========================================================================
    // 2. 의존성 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("2. 의존성 규칙")
    class DependencyRules {

        @Test
        @DisplayName("규칙 2-1: LockRepository 의존성이 필수입니다")
        void lockQueryAdapter_MustDependOnLockRepository() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "LockRepository 필드",
                                                    javaClass ->
                                                            javaClass.getAllFields().stream()
                                                                    .anyMatch(
                                                                            field ->
                                                                                    field.getRawType()
                                                                                            .getName()
                                                                                            .contains(
                                                                                                    "LockRepository")))))
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 LockRepository 의존성이 필수입니다 (1:1 매핑)");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-2: Mapper 의존성이 필수입니다")
        void lockQueryAdapter_MustDependOnMapper() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "Mapper 필드",
                                                    javaClass ->
                                                            javaClass.getAllFields().stream()
                                                                    .anyMatch(
                                                                            field ->
                                                                                    field.getRawType()
                                                                                            .getName()
                                                                                            .contains(
                                                                                                    "Mapper")))))
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 Mapper 의존성이 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-3: 다른 타입의 Repository 의존성이 금지됩니다")
        void lockQueryAdapter_MustNotDependOnOtherRepositories() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .dependOnClassesThat(
                                    DescribedPredicate.describe(
                                            "LockRepository가 아닌 다른 Repository",
                                            javaClass ->
                                                    javaClass.getSimpleName().endsWith("Repository")
                                                            && !javaClass
                                                                    .getSimpleName()
                                                                    .endsWith("LockRepository")))
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 LockRepository만 의존해야 합니다 (1:1 매핑 원칙)");

            rule.check(lockQueryAdapterClasses);
        }
    }

    // ========================================================================
    // 3. 메서드 규칙 - 6개 필수 메서드 (8개)
    // ========================================================================

    @Nested
    @DisplayName("3. 메서드 규칙 (6개 필수 메서드)")
    class MethodRules {

        @Test
        @DisplayName("규칙 3-1: 정확히 6개의 public 메서드만 허용됩니다")
        void lockQueryAdapter_MustHaveExactlySixPublicMethods() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "정확히 6개의 public 메서드 (생성자 제외)",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                            .filter(
                                                                                    method ->
                                                                                            method.getModifiers()
                                                                                                    .contains(
                                                                                                            JavaModifier
                                                                                                                    .PUBLIC))
                                                                            .filter(
                                                                                    method ->
                                                                                            !method.getName()
                                                                                                    .equals(
                                                                                                            "<init>"))
                                                                            .count()
                                                                    == 6)))
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 6개 조회 메서드만 public으로 노출해야 합니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-2: findByIdForUpdate 메서드가 필수입니다")
        void lockQueryAdapter_MustHaveFindByIdForUpdate() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "findByIdForUpdate 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "findByIdForUpdate")))))
                            .allowEmptyShould(true)
                            .because("PESSIMISTIC_WRITE 단건 조회 메서드 (findByIdForUpdate)가 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-3: findByCriteriaForUpdate 메서드가 필수입니다")
        void lockQueryAdapter_MustHaveFindByCriteriaForUpdate() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "findByCriteriaForUpdate 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "findByCriteriaForUpdate")))))
                            .allowEmptyShould(true)
                            .because(
                                    "PESSIMISTIC_WRITE 목록 조회 메서드 (findByCriteriaForUpdate)가 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-4: findByIdForShare 메서드가 필수입니다")
        void lockQueryAdapter_MustHaveFindByIdForShare() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "findByIdForShare 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "findByIdForShare")))))
                            .allowEmptyShould(true)
                            .because("PESSIMISTIC_READ 단건 조회 메서드 (findByIdForShare)가 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-5: findByCriteriaForShare 메서드가 필수입니다")
        void lockQueryAdapter_MustHaveFindByCriteriaForShare() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "findByCriteriaForShare 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "findByCriteriaForShare")))))
                            .allowEmptyShould(true)
                            .because("PESSIMISTIC_READ 목록 조회 메서드 (findByCriteriaForShare)가 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-6: findByIdWithOptimisticLock 메서드가 필수입니다")
        void lockQueryAdapter_MustHaveFindByIdWithOptimisticLock() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "findByIdWithOptimisticLock 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "findByIdWithOptimisticLock")))))
                            .allowEmptyShould(true)
                            .because("OPTIMISTIC 단건 조회 메서드 (findByIdWithOptimisticLock)가 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-7: findByCriteriaWithOptimisticLock 메서드가 필수입니다")
        void lockQueryAdapter_MustHaveFindByCriteriaWithOptimisticLock() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "findByCriteriaWithOptimisticLock 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "findByCriteriaWithOptimisticLock")))))
                            .allowEmptyShould(true)
                            .because(
                                    "OPTIMISTIC 목록 조회 메서드 (findByCriteriaWithOptimisticLock)가"
                                            + " 필수입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-8: 반환 타입은 Optional 또는 List여야 합니다")
        void lockQueryAdapter_MustReturnDomainTypes() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .arePublic()
                            .and()
                            .doNotHaveName("<init>")
                            .should()
                            .haveRawReturnType(
                                    DescribedPredicate.describe(
                                            "Optional 또는 List",
                                            returnType ->
                                                    returnType.isAssignableTo(Optional.class)
                                                            || returnType.isAssignableTo(
                                                                    List.class)))
                            .allowEmptyShould(true)
                            .because("조회 메서드는 Optional<Domain> 또는 List<Domain>을 반환해야 합니다");

            rule.check(lockQueryAdapterClasses);
        }
    }

    // ========================================================================
    // 4. 금지 사항 규칙 (8개)
    // ========================================================================

    @Nested
    @DisplayName("4. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 4-1: @Transactional 사용이 금지됩니다")
        void lockQueryAdapter_MustNotBeTransactional() {
            ArchRule classRule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .notBeAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because(
                                    "Lock Query Adapter 클래스에 @Transactional 사용 금지. UseCase에서"
                                            + " 관리하세요");

            ArchRule methodRule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .notBeAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because(
                                    "Lock Query Adapter 메서드에 @Transactional 사용 금지. UseCase에서"
                                            + " 관리하세요");

            classRule.check(lockQueryAdapterClasses);
            methodRule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-2: Command 메서드가 금지됩니다")
        void lockQueryAdapter_MustNotHaveCommandMethods() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .haveNameNotMatching(
                                    "(save|persist|update|delete|insert|remove|create).*")
                            .allowEmptyShould(true)
                            .because("저장/수정/삭제는 CommandAdapter로 분리해야 합니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-3: 일반 조회 메서드가 금지됩니다 (Lock 없는 조회)")
        void lockQueryAdapter_MustNotHaveNormalQueryMethods() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .haveNameNotMatching(
                                    "^(findById|existsById|findByCriteria|countByCriteria)$")
                            .allowEmptyShould(true)
                            .because("Lock 없는 일반 조회는 QueryAdapter를 사용해야 합니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-4: DTO 반환이 금지됩니다 (Domain만 반환)")
        void lockQueryAdapter_MustNotReturnDto() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .arePublic()
                            .should()
                            .haveRawReturnType(
                                    DescribedPredicate.describe(
                                            "DTO 타입이 아닌",
                                            returnType -> !returnType.getName().contains("Dto")))
                            .allowEmptyShould(true)
                            .because("Domain을 반환해야 하며, DTO 반환은 금지입니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-5: 로깅이 금지됩니다")
        void lockQueryAdapter_MustNotContainLogging() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .accessClassesThat()
                            .haveNameMatching(".*Logger.*")
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 로깅을 포함하지 않습니다. AOP로 처리하세요");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-6: 이전 네이밍(WithPessimisticLock)이 금지됩니다")
        void lockQueryAdapter_MustNotUseLegacyNaming() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .haveNameNotMatching(".*WithPessimisticLock.*")
                            .allowEmptyShould(true)
                            .because(
                                    "통합 네이밍 컨벤션을 사용하세요: ForUpdate (PESSIMISTIC_WRITE), ForShare"
                                            + " (PESSIMISTIC_READ)");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-7: private helper 메서드가 금지됩니다")
        void lockQueryAdapter_MustNotHavePrivateHelperMethods() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "private 메서드 없음",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                            .filter(
                                                                                    method ->
                                                                                            method.getModifiers()
                                                                                                    .contains(
                                                                                                            JavaModifier
                                                                                                                    .PRIVATE))
                                                                            .count()
                                                                    == 0)))
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 private helper 메서드를 가질 수 없습니다");

            rule.check(lockQueryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-8: Validator 의존성이 금지됩니다")
        void lockQueryAdapter_MustNotDependOnValidator() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("LockQueryAdapter")
                            .should()
                            .accessClassesThat()
                            .haveNameMatching(".*Validator.*")
                            .allowEmptyShould(true)
                            .because("Lock Query Adapter는 Validator를 사용하지 않습니다");

            rule.check(lockQueryAdapterClasses);
        }
    }
}
