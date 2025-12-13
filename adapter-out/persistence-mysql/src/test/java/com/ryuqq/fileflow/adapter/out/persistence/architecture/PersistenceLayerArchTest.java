package com.ryuqq.fileflow.adapter.out.persistence.architecture;

import static com.ryuqq.fileflow.adapter.out.persistence.architecture.ArchUnitPackageConstants.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * PersistenceLayerArchTest - Persistence Layer 전체 아키텍처 규칙 검증
 *
 * <p>Persistence Layer의 핵심 아키텍처 규칙을 검증합니다:
 *
 * <p><strong>검증 규칙:</strong>
 *
 * <ul>
 *   <li>규칙 1: Package 구조 검증 (adapter, entity, repository, mapper)
 *   <li>규칙 2: Port 구현 검증 (CommandPort, QueryPort, LockQueryPort)
 *   <li>규칙 3: JPA Entity와 Domain 분리 검증
 *   <li>규칙 4: Layer 의존성 검증 (단방향 의존성)
 *   <li>규칙 5: Application Layer 의존 금지
 *   <li>규칙 6: Domain Layer 의존 금지 (Port를 통해서만)
 *   <li>규칙 7: Adapter 네이밍 규칙 (*CommandAdapter, *QueryAdapter)
 *   <li>규칙 8: Repository 네이밍 규칙 (*Repository, *QueryDslRepository)
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("Persistence Layer 아키텍처 규칙 검증 (Zero-Tolerance)")
@Tag("architecture")
class PersistenceLayerArchTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(
                                com.tngtech.archunit.core.importer.ImportOption.Predefined
                                        .DO_NOT_INCLUDE_TESTS)
                        .importPackages(PERSISTENCE);
    }

    /** 규칙 1: Package 구조 검증 */
    @Test
    @DisplayName("[필수] Adapter는 ..adapter.. 패키지에 위치해야 한다")
    void persistence_AdaptersMustBeInAdapterPackage() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("Adapter")
                        .should()
                        .resideInAPackage(ADAPTER_PATTERN)
                        .because("Adapter 클래스는 adapter 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("[필수] Entity는 ..entity.. 패키지에 위치해야 한다")
    void persistence_EntitiesMustBeInEntityPackage() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("JpaEntity")
                        .should()
                        .resideInAPackage(ENTITY_PATTERN)
                        .because("JPA Entity 클래스는 entity 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Repository는 ..repository.. 패키지에 위치해야 합니다.
     *
     * <p>JpaRepository 인터페이스와 QueryDslRepository 클래스 모두 포함합니다.
     */
    @Test
    @DisplayName("[필수] Repository는 ..repository.. 패키지에 위치해야 한다")
    void persistence_RepositoriesMustBeInRepositoryPackage() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("Repository")
                        .and()
                        .haveSimpleNameNotContaining("Test")
                        .should()
                        .resideInAPackage(REPOSITORY_PATTERN)
                        .because("Repository 인터페이스/클래스는 repository 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("[필수] Mapper는 ..mapper.. 패키지에 위치해야 한다")
    void persistence_MappersMustBeInMapperPackage() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("Mapper")
                        .should()
                        .resideInAPackage(MAPPER_PATTERN)
                        .because("Mapper 클래스는 mapper 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * 규칙 2: Port 구현 검증
     *
     * <p>CommandAdapter 또는 Persist*Adapter는 *PersistencePort를 구현해야 합니다. 쓰기 작업을 담당하는 Adapter들이
     * 해당됩니다.
     */
    @Test
    @DisplayName("[필수] CommandAdapter/Persist*Adapter는 *PersistencePort를 구현해야 한다")
    void persistence_WriteAdapterMustImplementPersistencePort() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("Adapter")
                        .and()
                        .areNotInterfaces()
                        .and(
                                com.tngtech.archunit.base.DescribedPredicate.describe(
                                        "is CommandAdapter or Persist*Adapter",
                                        javaClass ->
                                                javaClass.getSimpleName().endsWith("CommandAdapter")
                                                        || javaClass
                                                                .getSimpleName()
                                                                .startsWith("Persist")
                                                        || javaClass
                                                                .getSimpleName()
                                                                .contains("PersistenceAdapter")))
                        .should(
                                com.tngtech.archunit.lang.ArchCondition.from(
                                        com.tngtech.archunit.base.DescribedPredicate.describe(
                                                "*PersistencePort 인터페이스 구현",
                                                javaClass ->
                                                        javaClass.getAllRawInterfaces().stream()
                                                                .anyMatch(
                                                                        i ->
                                                                                i.getSimpleName()
                                                                                        .endsWith(
                                                                                                "PersistencePort")))))
                        .because("쓰기 Adapter는 *PersistencePort 인터페이스를 구현해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * QueryAdapter는 *QueryPort 인터페이스를 구현해야 합니다.
     *
     * <p>허용되는 인터페이스 패턴: *QueryPort (예: FileAssetQueryPort, FindCompletedPartQueryPort)
     */
    @Test
    @DisplayName("[필수] QueryAdapter는 *QueryPort를 구현해야 한다")
    void persistence_QueryAdapterMustImplementQueryPort() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("QueryAdapter")
                        .and()
                        .haveSimpleNameNotContaining("Lock")
                        .should(
                                com.tngtech.archunit.lang.ArchCondition.from(
                                        com.tngtech.archunit.base.DescribedPredicate.describe(
                                                "*QueryPort 인터페이스 구현",
                                                javaClass ->
                                                        javaClass.getAllRawInterfaces().stream()
                                                                .anyMatch(
                                                                        i ->
                                                                                i.getSimpleName()
                                                                                        .endsWith(
                                                                                                "QueryPort")))))
                        .because("QueryAdapter는 *QueryPort 인터페이스를 구현해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("[필수] LockQueryAdapter는 LockQueryPort를 구현해야 한다")
    void persistence_LockQueryAdapterMustImplementLockQueryPort() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .implement(
                                com.tngtech.archunit.base.DescribedPredicate.describe(
                                        "LockQueryPort interface",
                                        javaClass ->
                                                javaClass.getAllRawInterfaces().stream()
                                                        .anyMatch(
                                                                i ->
                                                                        i.getSimpleName()
                                                                                .endsWith(
                                                                                        "LockQueryPort"))))
                        .because("LockQueryAdapter는 LockQueryPort 인터페이스를 구현해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /** 규칙 3: JPA Entity와 Domain 분리 검증 (Enum은 허용) */
    @Test
    @DisplayName("[필수] JPA Entity는 Domain Layer의 Enum만 의존할 수 있다")
    void persistence_JpaEntityCanOnlyDependOnDomainEnums() {
        ArchRule rule =
                noClasses()
                        .that()
                        .haveSimpleNameEndingWith("JpaEntity")
                        .should()
                        .dependOnClassesThat(
                                com.tngtech.archunit.base.DescribedPredicate.describe(
                                        "Domain Layer classes that are not enums",
                                        javaClass ->
                                                javaClass.getPackageName().contains(".domain.")
                                                        && !javaClass.isEnum()))
                        .because(
                                "JPA Entity는 Domain Layer의 Enum만 의존할 수 있습니다 "
                                        + "(VO, Entity 등 다른 Domain 클래스 의존 금지)");

        rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("[필수] JPA Entity는 Application Layer를 의존하지 않아야 한다")
    void persistence_JpaEntityMustNotDependOnApplication() {
        ArchRule rule =
                noClasses()
                        .that()
                        .haveSimpleNameEndingWith("JpaEntity")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(APPLICATION_ALL)
                        .because("JPA Entity는 Application Layer에 의존하면 안 됩니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("[필수] Domain은 JPA Entity를 의존하지 않아야 한다")
    void persistence_DomainMustNotDependOnJpaEntity() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAnyPackage(DOMAIN_ALL)
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameEndingWith("JpaEntity")
                        .because("Domain은 JPA Entity에 의존하면 안 됩니다 (Clean Architecture 원칙)");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * 규칙 5: Application Layer 의존 금지
     *
     * <p>Persistence Layer는 Application Layer를 직접 의존하면 안 됩니다. 단, Port 인터페이스(..port.out..)는 예외입니다
     * (Adapter가 구현해야 함).
     */
    @Test
    @DisplayName("[금지] Persistence Layer는 Application Layer를 직접 의존하지 않아야 한다 (Port 제외)")
    void persistence_MustNotDependOnApplicationLayerExceptPorts() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAnyPackage(PERSISTENCE_ALL)
                        .and()
                        .haveSimpleNameNotContaining("Test")
                        .and()
                        .resideOutsideOfPackages(ARCHITECTURE_PATTERN)
                        .should()
                        .dependOnClassesThat(
                                com.tngtech.archunit.base.DescribedPredicate.describe(
                                        "Application Layer classes excluding Ports",
                                        javaClass ->
                                                javaClass.getPackageName().contains(".application.")
                                                        && !javaClass
                                                                .getPackageName()
                                                                .contains(".port.")))
                        .because(
                                "Persistence Layer는 Application Layer를 직접 의존하면 안 됩니다 (Port 인터페이스"
                                        + " 제외)");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * 규칙 6: Domain Layer 의존 금지 (Adapter, Mapper 제외)
     *
     * <p>Entity는 Domain Layer의 Enum만 의존 가능합니다 (규칙 3에서 별도 검증). Mapper는 Domain 변환을 담당하므로 Domain 의존이
     * 필수입니다. Repository는 Domain Layer를 직접 의존하면 안 됩니다.
     */
    @Test
    @DisplayName("[금지] Repository/Entity는 Domain Layer의 비-Enum 클래스를 의존하지 않아야 한다")
    void persistence_RepositoryEntityMustNotDependOnDomainNonEnum() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAnyPackage(REPOSITORY_PATTERN)
                        .and()
                        .haveSimpleNameNotContaining("Test")
                        .and()
                        .resideOutsideOfPackages(ARCHITECTURE_PATTERN)
                        .should()
                        .dependOnClassesThat(
                                com.tngtech.archunit.base.DescribedPredicate.describe(
                                        "Domain Layer non-enum, non-VO classes",
                                        javaClass ->
                                                javaClass.getPackageName().contains(".domain.")
                                                        && !javaClass.isEnum()
                                                        && !javaClass
                                                                .getSimpleName()
                                                                .endsWith("Status")
                                                        && !javaClass
                                                                .getSimpleName()
                                                                .endsWith("Type")))
                        .because("Repository는 Domain Layer를 직접 의존하면 안 됩니다 (Enum/Status/Type 제외)");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * 규칙 7: Adapter 네이밍 규칙 검증
     *
     * <p>허용되는 Adapter 네이밍 패턴: - *CommandAdapter: 쓰기 작업 (예: FileAssetCommandAdapter) -
     * *QueryAdapter: 읽기 작업 (예: FileAssetQueryAdapter) - *LockQueryAdapter: 락을 사용하는 읽기 작업 -
     * Persist*Adapter: 영속화 작업 (예: PersistCompletedPartAdapter) - *PersistenceAdapter: 영속화 작업 (예:
     * ExternalDownloadPersistenceAdapter)
     */
    @Test
    @DisplayName("[필수] Adapter는 표준 네이밍 규칙을 따라야 한다")
    void persistence_AdaptersMustFollowNamingConvention() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("Adapter")
                        .and()
                        .areNotInterfaces()
                        .and()
                        .haveSimpleNameNotContaining("Test")
                        .and()
                        .resideInAPackage(ADAPTER_PATTERN)
                        .should()
                        .haveSimpleNameEndingWith("CommandAdapter")
                        .orShould()
                        .haveSimpleNameEndingWith("QueryAdapter")
                        .orShould()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .orShould()
                        .haveSimpleNameEndingWith("PersistenceAdapter")
                        .orShould()
                        .haveSimpleNameStartingWith("Persist")
                        .because(
                                "Adapter는 *CommandAdapter, *QueryAdapter, *LockQueryAdapter,"
                                        + " *PersistenceAdapter, 또는 Persist*Adapter 네이밍 규칙을 따라야"
                                        + " 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /** 규칙 8: Repository 네이밍 규칙 */
    @Test
    @DisplayName("[필수] Repository는 *Repository 또는 *QueryDslRepository 네이밍 규칙을 따라야 한다")
    void persistence_RepositoriesMustFollowNamingConvention() {
        ArchRule rule =
                classes()
                        .that()
                        .resideInAPackage(REPOSITORY_PATTERN)
                        .and()
                        .haveSimpleNameNotContaining("Test")
                        .should()
                        .haveSimpleNameEndingWith("Repository")
                        .orShould()
                        .haveSimpleNameEndingWith("QueryDslRepository")
                        .because("Repository는 *Repository 또는 *QueryDslRepository 네이밍 규칙을 따라야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }
}
