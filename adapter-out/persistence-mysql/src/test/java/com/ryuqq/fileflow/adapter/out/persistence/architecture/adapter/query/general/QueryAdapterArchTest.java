package com.ryuqq.fileflow.adapter.out.persistence.architecture.adapter.query.general;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * QueryAdapterArchTest - Query Adapter 아키텍처 규칙 검증 (20개 규칙)
 *
 * <p>query-adapter-guide.md 규칙을 ArchUnit으로 검증합니다.</p>
 *
 * <p><strong>핵심 원칙:</strong></p>
 * <ul>
 *   <li>✅ QueryDslRepository와 1:1 매핑</li>
 *   <li>✅ 필드 2개 (QueryDslRepository + Mapper)</li>
 *   <li>✅ 메서드 4개 (findById, existsById, findByCriteria, countByCriteria)</li>
 *   <li>✅ 반환 타입: Domain (Optional/List/boolean/long)</li>
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("QueryAdapter 아키텍처 규칙 검증 (Zero-Tolerance)")
class QueryAdapterArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses queryAdapterClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

        // QueryAdapter만 선택 (LockQueryAdapter, AdminQueryAdapter 제외)
        queryAdapterClasses = allClasses.that(
            DescribedPredicate.describe(
                "Query Adapter 클래스 (Lock, Admin 제외)",
                javaClass -> javaClass.getSimpleName().endsWith("QueryAdapter") &&
                    !javaClass.getSimpleName().contains("Lock") &&
                    !javaClass.getSimpleName().contains("Admin") &&
                    !javaClass.isInterface()
            )
        );
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (5개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: QueryAdapter는 클래스여야 합니다")
        void queryAdapter_MustBeClass() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should().notBeInterfaces()
                .allowEmptyShould(true)
                .because("Query Adapter는 클래스로 정의되어야 합니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Component 어노테이션이 필수입니다")
        void queryAdapter_MustHaveComponentAnnotation() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should().beAnnotatedWith(Component.class)
                .allowEmptyShould(true)
                .because("Query Adapter는 @Component 어노테이션이 필수입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-3: QueryPort 또는 LoadPort 인터페이스를 구현해야 합니다")
        void queryAdapter_MustImplementPort() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should().implement(
                    DescribedPredicate.describe(
                        "QueryPort 또는 LoadPort 인터페이스",
                        javaClass -> javaClass.getAllRawInterfaces().stream()
                            .anyMatch(iface ->
                                iface.getSimpleName().endsWith("QueryPort") ||
                                    iface.getSimpleName().endsWith("LoadPort")
                            )
                    )
                )
                .allowEmptyShould(true)
                .because("Query Adapter는 QueryPort 또는 LoadPort 인터페이스를 구현해야 합니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-4: 정확히 2개 필드만 허용됩니다 (QueryDslRepository + Mapper)")
        void queryAdapter_MustHaveExactlyTwoFields() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "정확히 2개의 필드",
                        javaClass -> javaClass.getAllFields().size() == 2
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 정확히 2개의 필드(QueryDslRepository, Mapper)만 가져야 합니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-5: 모든 필드는 final이어야 합니다")
        void queryAdapter_AllFieldsMustBeFinal() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryAdapter")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Lock")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .should().beFinal()
                .allowEmptyShould(true)
                .because("Query Adapter의 모든 필드는 final로 불변성을 보장해야 합니다");

            rule.check(queryAdapterClasses);
        }
    }

    // ========================================================================
    // 2. 의존성 규칙 (4개)
    // ========================================================================

    @Nested
    @DisplayName("2. 의존성 규칙")
    class DependencyRules {

        @Test
        @DisplayName("규칙 2-1: QueryDslRepository 의존성이 필수입니다")
        void queryAdapter_MustDependOnQueryDslRepository() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("QueryDslRepository")
                .allowEmptyShould(true)
                .because("Query Adapter는 QueryDslRepository 의존성이 필수입니다 (1:1 매핑)");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-2: Mapper 의존성이 필수입니다")
        void queryAdapter_MustDependOnMapper() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Mapper")
                .allowEmptyShould(true)
                .because("Query Adapter는 Mapper 의존성이 필수입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-3: @Autowired 필드 주입이 금지됩니다")
        void queryAdapter_MustNotUseFieldInjection() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryAdapter")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Lock")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .should().notBeAnnotatedWith(Autowired.class)
                .allowEmptyShould(true)
                .because("Query Adapter는 생성자 주입만 허용되며, @Autowired 필드 주입은 금지입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-4: JPAQueryFactory 직접 사용이 금지됩니다")
        void queryAdapter_MustNotUseJPAQueryFactoryDirectly() {
            ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .should().accessClassesThat().haveNameMatching(".*JPAQueryFactory.*")
                .allowEmptyShould(true)
                .because("Query Adapter는 JPAQueryFactory를 직접 사용하지 않고 QueryDslRepository를 통해 조회해야 합니다");

            rule.check(queryAdapterClasses);
        }
    }

    // ========================================================================
    // 3. 메서드 규칙 (6개)
    // ========================================================================

    @Nested
    @DisplayName("3. 메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("규칙 3-1: 정확히 4개의 public 메서드만 허용됩니다")
        void queryAdapter_MustHaveExactlyFourPublicMethods() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "정확히 4개의 public 메서드 (생성자 제외)",
                        javaClass -> javaClass.getMethods().stream()
                            .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                            .filter(method -> !method.getName().equals("<init>"))
                            .count() == 4
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 findById, existsById, findByCriteria, countByCriteria 메서드만 public으로 노출해야 합니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-2: findById 메서드가 필수입니다")
        void queryAdapter_MustHaveFindByIdMethod() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "public findById 메서드",
                        javaClass -> javaClass.getMethods().stream()
                            .anyMatch(method -> method.getName().equals("findById") &&
                                method.getModifiers().contains(JavaModifier.PUBLIC))
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 단건 조회를 위한 findById() 메서드가 필수입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-3: existsById 메서드가 필수입니다")
        void queryAdapter_MustHaveExistsByIdMethod() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "public existsById 메서드",
                        javaClass -> javaClass.getMethods().stream()
                            .anyMatch(method -> method.getName().equals("existsById") &&
                                method.getModifiers().contains(JavaModifier.PUBLIC))
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 존재 여부 확인을 위한 existsById() 메서드가 필수입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-4: findByCriteria 메서드가 필수입니다")
        void queryAdapter_MustHaveFindByCriteriaMethod() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "public findByCriteria 메서드",
                        javaClass -> javaClass.getMethods().stream()
                            .anyMatch(method -> method.getName().equals("findByCriteria") &&
                                method.getModifiers().contains(JavaModifier.PUBLIC))
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 목록 조회를 위한 findByCriteria() 메서드가 필수입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-5: countByCriteria 메서드가 필수입니다")
        void queryAdapter_MustHaveCountByCriteriaMethod() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "public countByCriteria 메서드",
                        javaClass -> javaClass.getMethods().stream()
                            .anyMatch(method -> method.getName().equals("countByCriteria") &&
                                method.getModifiers().contains(JavaModifier.PUBLIC))
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 개수 조회를 위한 countByCriteria() 메서드가 필수입니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-6: Command 메서드가 금지됩니다")
        void queryAdapter_MustNotContainCommandMethods() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryAdapter")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Lock")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .should().haveNameNotMatching("(save|persist|update|delete|insert|remove|create).*")
                .allowEmptyShould(true)
                .because("Query Adapter는 Command 메서드를 포함하면 안 됩니다. CommandAdapter로 분리하세요");

            rule.check(queryAdapterClasses);
        }
    }

    // ========================================================================
    // 4. 금지 사항 규칙 (5개)
    // ========================================================================

    @Nested
    @DisplayName("4. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 4-1: @Transactional 사용이 금지됩니다")
        void queryAdapter_MustNotBeAnnotatedWithTransactional() {
            ArchRule classRule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should().notBeAnnotatedWith(Transactional.class)
                .allowEmptyShould(true)
                .because("Query Adapter 클래스에 @Transactional 사용 금지. 읽기 전용 작업입니다");

            ArchRule methodRule = methods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryAdapter")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Lock")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .should().notBeAnnotatedWith(Transactional.class)
                .allowEmptyShould(true)
                .because("Query Adapter 메서드에 @Transactional 사용 금지. 읽기 전용 작업입니다");

            classRule.check(queryAdapterClasses);
            methodRule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-2: 비즈니스 메서드가 금지됩니다")
        void queryAdapter_MustNotContainBusinessMethods() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryAdapter")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Lock")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Admin")
                .should().haveNameNotMatching("(confirm|cancel|approve|reject|modify|change|validate|calculate).*")
                .allowEmptyShould(true)
                .because("Query Adapter는 비즈니스 메서드를 포함하면 안 됩니다. Domain에서 처리하세요");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-3: 로깅이 금지됩니다")
        void queryAdapter_MustNotContainLogging() {
            ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .should().accessClassesThat().haveNameMatching(".*Logger.*")
                .allowEmptyShould(true)
                .because("Query Adapter는 로깅을 포함하지 않습니다. AOP로 처리하세요");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-4: Validator 의존성이 금지됩니다")
        void queryAdapter_MustNotDependOnValidator() {
            ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .should().accessClassesThat().haveNameMatching(".*Validator.*")
                .allowEmptyShould(true)
                .because("Query Adapter는 Validator를 사용하지 않습니다");

            rule.check(queryAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-5: private helper 메서드가 금지됩니다")
        void queryAdapter_MustNotHavePrivateHelperMethods() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("QueryAdapter")
                .and().haveSimpleNameNotContaining("Lock")
                .and().haveSimpleNameNotContaining("Admin")
                .and().resideInAPackage("..adapter..")
                .should(ArchCondition.from(
                    DescribedPredicate.describe(
                        "private 메서드 없음",
                        javaClass -> javaClass.getMethods().stream()
                            .filter(method -> method.getModifiers().contains(JavaModifier.PRIVATE))
                            .count() == 0
                    )
                ))
                .allowEmptyShould(true)
                .because("Query Adapter는 private helper 메서드를 가질 수 없습니다");

            rule.check(queryAdapterClasses);
        }
    }
}
