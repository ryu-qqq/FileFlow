package com.ryuqq.fileflow.adapter.in.rest.architecture.mapper;

import static com.ryuqq.fileflow.adapter.in.rest.architecture.ArchUnitPackageConstants.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Mapper ArchUnit 검증 테스트 (Zero-Tolerance)
 *
 * <p>모든 Mapper는 정확히 이 규칙을 따라야 합니다.
 *
 * <p><strong>검증 그룹:</strong>
 *
 * <ul>
 *   <li>1. @Component 규칙 (1개)
 *   <li>2. 네이밍/위치 규칙 (2개)
 *   <li>3. Lombok 금지 규칙 (1개)
 *   <li>4. 메서드 규칙 (2개)
 *   <li>5. 의존성 금지 규칙 (2개)
 *   <li>6. 어노테이션 금지 규칙 (2개)
 *   <li>7. 메서드 네이밍 규칙 (1개, 권장)
 * </ul>
 *
 * <p><strong>참고 문서:</strong>
 *
 * <ul>
 *   <li>mapper/mapper-guide.md - Mapper 작성 가이드
 *   <li>mapper/mapper-archunit.md - ArchUnit 검증 규칙
 * </ul>
 *
 * @author development-team
 * @since 2.0.0
 */
@DisplayName("Mapper 아키텍처 규칙 검증 (Zero-Tolerance)")
@Tag("architecture")
@Tag("adapter-rest")
class MapperArchTest {

    private static JavaClasses allClasses;
    private static JavaClasses mapperClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(ADAPTER_IN_REST);

        mapperClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Mapper 클래스",
                                javaClass -> javaClass.getSimpleName().endsWith("ApiMapper")));
    }

    // ========================================================================
    // 1. @Component 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("1. @Component 규칙")
    class ComponentRules {

        @Test
        @DisplayName("규칙 1-1: Mapper는 @Component 어노테이션이 필수입니다")
        void mapper_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..mapper..")
                            .and()
                            .haveSimpleNameEndingWith("ApiMapper")
                            .should()
                            .beAnnotatedWith("org.springframework.stereotype.Component")
                            .allowEmptyShould(true)
                            .because("Mapper는 @Component로 Bean 등록되어야 하며 Static 메서드는 금지됩니다");

            rule.check(mapperClasses);
        }
    }

    // ========================================================================
    // 2. 네이밍/위치 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("2. 네이밍/위치 규칙")
    class NamingAndLocationRules {

        @Test
        @DisplayName("규칙 2-1: Mapper는 *ApiMapper 접미사가 필수입니다")
        void mapper_MustHaveApiMapperSuffix() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..mapper..")
                            .and()
                            .areAnnotatedWith("org.springframework.stereotype.Component")
                            .and()
                            .areNotNestedClasses()
                            .should()
                            .haveSimpleNameEndingWith("ApiMapper")
                            .allowEmptyShould(true)
                            .because("Mapper는 *ApiMapper 네이밍 규칙을 따라야 합니다");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("규칙 2-2: Mapper는 mapper 패키지에 위치해야 합니다")
        void mapper_MustBeInCorrectPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("ApiMapper")
                            .and()
                            .resideInAPackage("..adapter.in.rest..")
                            .and()
                            .areNotNestedClasses()
                            .should()
                            .resideInAPackage("..mapper..")
                            .allowEmptyShould(true)
                            .because("Mapper는 mapper 패키지에 위치해야 합니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 3. Lombok 금지 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("3. Lombok 금지 규칙")
    class LombokProhibitionRules {

        @Test
        @DisplayName("규칙 3-1: Mapper는 Lombok 어노테이션이 금지됩니다")
        void mapper_MustNotUseLombok() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..mapper..")
                            .should()
                            .beAnnotatedWith("lombok.Data")
                            .orShould()
                            .beAnnotatedWith("lombok.Builder")
                            .orShould()
                            .beAnnotatedWith("lombok.Getter")
                            .orShould()
                            .beAnnotatedWith("lombok.Setter")
                            .orShould()
                            .beAnnotatedWith("lombok.AllArgsConstructor")
                            .orShould()
                            .beAnnotatedWith("lombok.NoArgsConstructor")
                            .orShould()
                            .beAnnotatedWith("lombok.RequiredArgsConstructor")
                            .orShould()
                            .beAnnotatedWith("lombok.Value")
                            .allowEmptyShould(true)
                            .because("Mapper는 Pure Java를 사용해야 하며 Lombok은 금지됩니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 4. 메서드 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("4. 메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("규칙 4-1: Mapper는 Public Static 메서드가 금지됩니다")
        void mapper_MustNotHavePublicStaticMethods() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage("..mapper..")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("ApiMapper")
                            .and()
                            .arePublic()
                            .should()
                            .notBeStatic()
                            .allowEmptyShould(true)
                            .because("Mapper는 @Component Bean이므로 Public Static 메서드는 금지됩니다");

            rule.check(mapperClasses);
        }

        @Test
        @DisplayName("규칙 4-2: Mapper는 비즈니스 로직 메서드가 금지됩니다")
        void mapper_MustNotHaveBusinessLogicMethods() {
            ArchRule rule =
                    noMethods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage("..mapper..")
                            .and()
                            .haveNameMatching(
                                    "calculate|compute|validate|isValid|check|process|execute")
                            .should()
                            .beDeclaredInClassesThat()
                            .resideInAPackage("..mapper..")
                            .allowEmptyShould(true)
                            .because("Mapper는 필드 매핑만 담당하며 비즈니스 로직은 금지됩니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 5. 의존성 금지 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("5. 의존성 금지 규칙")
    class DependencyProhibitionRules {

        @Test
        @DisplayName("규칙 5-1: Mapper는 Domain 객체 직접 사용이 금지됩니다 (ErrorMapper 제외)")
        void mapper_MustNotUseDomainObjects() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..mapper..")
                            .and()
                            .areNotNestedClasses()
                            .and()
                            .haveSimpleNameNotContaining("Error")
                            .should()
                            .dependOnClassesThat()
                            .resideInAPackage("..domain..")
                            .allowEmptyShould(true)
                            .because(
                                    "Mapper는 Application DTO만 사용하며 Domain 직접 의존은 금지됩니다"
                                            + " (ErrorMapper는 예외)");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("규칙 5-2: Mapper는 Port 의존성 주입이 금지됩니다")
        void mapper_MustNotDependOnPorts() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..mapper..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAPackage("..port.in..")
                            .orShould()
                            .dependOnClassesThat()
                            .resideInAPackage("..port.out..")
                            .allowEmptyShould(true)
                            .because("Mapper는 UseCase/Repository를 주입받지 않으며 Controller가 주입합니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 6. 어노테이션 금지 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("6. 어노테이션 금지 규칙")
    class AnnotationProhibitionRules {

        @Test
        @DisplayName("규칙 6-1: Mapper는 @Service/@Repository 어노테이션이 금지됩니다")
        void mapper_MustNotUseServiceOrRepository() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..mapper..")
                            .should()
                            .beAnnotatedWith("org.springframework.stereotype.Service")
                            .orShould()
                            .beAnnotatedWith("org.springframework.stereotype.Repository")
                            .allowEmptyShould(true)
                            .because("Mapper는 @Component 어노테이션만 사용해야 합니다");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("규칙 6-2: Mapper는 @Transactional 어노테이션이 금지됩니다")
        void mapper_MustNotUseTransactional() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..mapper..")
                            .should()
                            .beAnnotatedWith(
                                    "org.springframework.transaction.annotation.Transactional")
                            .allowEmptyShould(true)
                            .because("Mapper는 변환만 담당하며 Transaction은 UseCase 책임입니다");

            rule.check(allClasses);
        }
    }

    // ========================================================================
    // 7. 메서드 네이밍 규칙 (1개, 권장)
    // ========================================================================

    @Nested
    @DisplayName("7. 메서드 네이밍 규칙")
    class MethodNamingRules {

        /**
         * 규칙 7-1: 메서드 네이밍 규칙 (to* 접두사)
         *
         * <p>이 규칙은 권장사항이므로 실패 시 경고만 표시합니다.
         *
         * <p>Mapper 변환 메서드는 to* 접두사를 사용하는 것이 좋습니다 (예: toCommand, toApiResponse)
         */
        @Test
        @DisplayName("[권장] Mapper 메서드는 to* 접두사를 가져야 한다 (예: toCommand, toApiResponse)")
        void mapper_ShouldHaveToMethodPrefix() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage("..mapper..")
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("ApiMapper")
                            .and()
                            .arePublic()
                            .and()
                            .doNotHaveName("equals")
                            .and()
                            .doNotHaveName("hashCode")
                            .and()
                            .doNotHaveName("toString")
                            .should()
                            .haveNameMatching("to[A-Z].*")
                            .allowEmptyShould(true)
                            .because(
                                    "Mapper 변환 메서드는 to* 접두사를 사용하는 것이 좋습니다 (예: toCommand,"
                                            + " toApiResponse)");

            // Note: 이 규칙은 권장사항이므로 실패 시 경고만 표시
            try {
                rule.check(mapperClasses);
            } catch (AssertionError e) {
                System.out.println("⚠️  Warning (권장사항): " + e.getMessage());
            }
        }
    }
}
