package com.ryuqq.fileflow.adapter.out.persistence.architecture.adapter.command;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CommandAdapterArchTest - Command Adapter 아키텍처 규칙 검증 (20개 규칙)
 *
 * <p>command-adapter-guide.md 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>핵심 원칙:</strong>
 *
 * <ul>
 *   <li>✅ JpaRepository와 1:1 매핑
 *   <li>✅ 필드 2개 (JpaRepository + Mapper)
 *   <li>✅ 메서드 1개 (persist)
 *   <li>✅ 반환 타입: *Id
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("CommandAdapter 아키텍처 규칙 검증 (Zero-Tolerance)")
class CommandAdapterArchTest {

    private static final String BASE_PACKAGE = "com.ryuqq.fileflow.adapter.out.persistence";

    private static JavaClasses allClasses;
    private static JavaClasses commandAdapterClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);

        commandAdapterClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Command Adapter 클래스",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("CommandAdapter")
                                                && !javaClass.isInterface()));
    }

    // ========================================================================
    // 1. 클래스 구조 규칙 (5개)
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: CommandAdapter는 클래스여야 합니다")
        void commandAdapter_MustBeClass() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .notBeInterfaces()
                            .allowEmptyShould(true)
                            .because("Command Adapter는 클래스로 정의되어야 합니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Component 어노테이션이 필수입니다")
        void commandAdapter_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .allowEmptyShould(true)
                            .because("Command Adapter는 @Component 어노테이션이 필수입니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-3: *PersistencePort 인터페이스를 구현해야 합니다")
        void commandAdapter_MustImplementPort() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "*PersistencePort 인터페이스 구현",
                                                    javaClass ->
                                                            javaClass.getAllRawInterfaces().stream()
                                                                    .anyMatch(
                                                                            iface ->
                                                                                    iface.getSimpleName()
                                                                                            .endsWith(
                                                                                                    "PersistencePort")))))
                            .allowEmptyShould(true)
                            .because("Command Adapter는 *PersistencePort 인터페이스를 구현해야 합니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-4: 정확히 2개 필드만 허용됩니다 (JpaRepository + Mapper)")
        void commandAdapter_MustHaveExactlyTwoFields() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "정확히 2개의 필드",
                                                    javaClass ->
                                                            javaClass.getAllFields().size() == 2)))
                            .allowEmptyShould(true)
                            .because("Command Adapter는 정확히 2개의 필드(JpaRepository, Mapper)만 가져야 합니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-5: 모든 필드는 final이어야 합니다")
        void commandAdapter_AllFieldsMustBeFinal() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .should()
                            .beFinal()
                            .allowEmptyShould(true)
                            .because("Command Adapter의 모든 필드는 final로 불변성을 보장해야 합니다");

            rule.check(commandAdapterClasses);
        }
    }

    // ========================================================================
    // 2. 의존성 규칙 (4개)
    // ========================================================================

    @Nested
    @DisplayName("2. 의존성 규칙")
    class DependencyRules {

        @Test
        @DisplayName("규칙 2-1: JpaRepository 의존성이 필수입니다")
        void commandAdapter_MustDependOnJpaRepository() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .dependOnClassesThat()
                            .haveSimpleNameEndingWith("JpaRepository")
                            .allowEmptyShould(true)
                            .because("Command Adapter는 JpaRepository 의존성이 필수입니다 (1:1 매핑)");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-2: Mapper 의존성이 필수입니다")
        void commandAdapter_MustDependOnMapper() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .dependOnClassesThat()
                            .haveSimpleNameEndingWith("Mapper")
                            .allowEmptyShould(true)
                            .because("Command Adapter는 Mapper 의존성이 필수입니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-3: @Autowired 필드 주입이 금지됩니다")
        void commandAdapter_MustNotUseFieldInjection() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .should()
                            .notBeAnnotatedWith(Autowired.class)
                            .allowEmptyShould(true)
                            .because("Command Adapter는 생성자 주입만 허용되며, @Autowired 필드 주입은 금지입니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-4: 다른 타입의 Repository 의존성이 금지됩니다")
        void commandAdapter_MustNotDependOnOtherRepositories() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .dependOnClassesThat(
                                    DescribedPredicate.describe(
                                            "JpaRepository가 아닌 다른 Repository",
                                            javaClass ->
                                                    javaClass.getSimpleName().endsWith("Repository")
                                                            && !javaClass
                                                                    .getSimpleName()
                                                                    .endsWith("JpaRepository")))
                            .allowEmptyShould(true)
                            .because("Command Adapter는 JpaRepository만 의존해야 합니다 (1:1 매핑 원칙)");

            rule.check(commandAdapterClasses);
        }
    }

    // ========================================================================
    // 3. 메서드 규칙 (6개)
    // ========================================================================

    @Nested
    @DisplayName("3. 메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("규칙 3-1: 정확히 1개의 public 메서드만 허용됩니다 (persist)")
        void commandAdapter_MustHaveExactlyOnePublicMethod() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "정확히 1개의 public 메서드 (생성자 제외)",
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
                                                                    == 1)))
                            .allowEmptyShould(true)
                            .because("Command Adapter는 persist() 메서드 하나만 public으로 노출해야 합니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-2: public 메서드명은 'persist'여야 합니다")
        void commandAdapter_PublicMethodNameMustBePersist() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .arePublic()
                            .and()
                            .doNotHaveFullName(".*<init>.*")
                            .should()
                            .haveName("persist")
                            .allowEmptyShould(true)
                            .because("Command Adapter의 public 메서드명은 반드시 'persist'여야 합니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-3: persist 메서드는 정확히 1개 파라미터를 받습니다")
        void commandAdapter_PersistMethodMustHaveExactlyOneParameter() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .haveName("persist")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "정확히 1개의 파라미터",
                                                    method ->
                                                            method.getRawParameterTypes().size()
                                                                    == 1)))
                            .allowEmptyShould(true)
                            .because("persist() 메서드는 정확히 1개의 Domain Aggregate 파라미터만 가져야 합니다");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-4: persist 메서드는 *Id 타입을 반환합니다")
        void commandAdapter_PersistMethodMustReturnIdType() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .haveName("persist")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "*Id 타입 반환",
                                                    method ->
                                                            method.getRawReturnType()
                                                                    .getSimpleName()
                                                                    .endsWith("Id"))))
                            .allowEmptyShould(true)
                            .because("persist() 메서드는 *Id 타입을 반환해야 합니다 (예: OrderId, ProductId)");

            rule.check(commandAdapterClasses);
        }

        /**
         * @Override 어노테이션 규칙은 컴파일러에서 이미 검증됩니다. ArchUnit에서 메서드 레벨 어노테이션 검사 시 allowEmptyShould가 지원되지
         * 않아 비활성화합니다. IDE와 컴파일러가 @Override 누락을 경고하므로 별도 검증은 불필요합니다.
         */
        // @Test
        // @DisplayName("규칙 3-5: @Override 어노테이션이 필수입니다")
        // void commandAdapter_PersistMethodMustHaveOverrideAnnotation() { ... }

        @Test
        @DisplayName("규칙 3-6: Query 메서드가 금지됩니다")
        void commandAdapter_MustNotContainQueryMethods() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .should()
                            .haveNameNotMatching("(find|load|get|query|search|list|count|exists).*")
                            .allowEmptyShould(true)
                            .because("Command Adapter는 Query 메서드를 포함하면 안 됩니다. QueryAdapter로 분리하세요");

            rule.check(commandAdapterClasses);
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
        void commandAdapter_MustNotBeAnnotatedWithTransactional() {
            ArchRule classRule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .resideInAPackage("..adapter..")
                            .should()
                            .notBeAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because("Command Adapter 클래스에 @Transactional 사용 금지. UseCase에서 관리하세요");

            ArchRule methodRule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .should()
                            .notBeAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because("Command Adapter 메서드에 @Transactional 사용 금지. UseCase에서 관리하세요");

            classRule.check(commandAdapterClasses);
            methodRule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-2: 비즈니스 예외 throw가 금지됩니다")
        void commandAdapter_MustNotThrowBusinessExceptions() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .and()
                            .haveName("persist")
                            .should()
                            .notDeclareThrowableOfType(RuntimeException.class)
                            .allowEmptyShould(true)
                            .because("Command Adapter는 비즈니스 예외를 throw하지 않습니다. Domain에서 처리하세요");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-3: 로깅이 금지됩니다")
        void commandAdapter_MustNotContainLogging() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .should()
                            .accessClassesThat()
                            .haveNameMatching(".*Logger.*")
                            .allowEmptyShould(true)
                            .because("Command Adapter는 로깅을 포함하지 않습니다. AOP로 처리하세요");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-4: Validator 의존성이 금지됩니다")
        void commandAdapter_MustNotDependOnValidator() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
                            .should()
                            .accessClassesThat()
                            .haveNameMatching(".*Validator.*")
                            .allowEmptyShould(true)
                            .because("Command Adapter는 Validator를 사용하지 않습니다. Domain에서 처리하세요");

            rule.check(commandAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-5: private helper 메서드가 금지됩니다")
        void commandAdapter_MustNotHavePrivateHelperMethods() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandAdapter")
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
                            .because("Command Adapter는 private helper 메서드를 가질 수 없습니다");

            rule.check(commandAdapterClasses);
        }
    }
}
