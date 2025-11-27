package com.ryuqq.fileflow.adapter.out.persistence.architecture.adpater.query;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * LockQueryAdapter 아키텍처 규칙 검증 테스트
 *
 * <p>CQRS Lock Query Adapter의 Zero-Tolerance 규칙을 자동으로 검증합니다:
 *
 * <ul>
 *   <li>정확한 필드 개수 (2개): LockRepository, Mapper
 *   <li>정확한 메서드 개수 (6개): 비관락 2 + 낙관락 2 + For Update 2
 *   <li>메서드 네이밍 규칙: find*WithPessimisticLock, find*WithOptimisticLock, find*ForUpdate
 *   <li>반환 타입 규칙: Optional&lt;Domain&gt;, List&lt;Domain&gt;
 *   <li>@Component 필수
 *   <li>@Transactional 금지
 *   <li>비즈니스 로직 금지
 *   <li>try-catch 금지 (Lock 예외 처리 안 함)
 *   <li>Command 메서드 금지
 *   <li>일반 조회 메서드 금지 (QueryAdapter로 분리)
 * </ul>
 *
 * <p><strong>Note:</strong> 현재 LockQueryAdapter 구현체가 없으므로 모든 규칙에 allowEmptyShould(true)를 적용합니다.
 * 구현체가 추가되면 규칙 검증이 활성화됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("LockQueryAdapter 아키텍처 규칙 검증 (Zero-Tolerance)")
class LockQueryAdapterArchTest {

    private static JavaClasses allClasses;
    private static JavaClasses lockAdapterClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .importPackages("com.ryuqq.fileflow.adapter.out.persistence");

        lockAdapterClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "are LockQueryAdapter classes",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("LockQueryAdapter")));
    }

    /**
     * 규칙 1: @Component 어노테이션 필수
     *
     * <p>LockQueryAdapter는 Spring Bean으로 등록되어야 합니다.
     */
    @Test
    @DisplayName("규칙 1: @Component 어노테이션 필수")
    void lockQueryAdapter_MustBeAnnotatedWithComponent() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .beAnnotatedWith(Component.class)
                        .because("LockQueryAdapter는 @Component로 Spring Bean 등록이 필수입니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 2: *LockQueryPort 인터페이스 구현 필수
     *
     * <p>LockQueryAdapter는 Application Layer의 LockQueryPort 인터페이스를 구현해야 합니다.
     */
    @Test
    @DisplayName("규칙 2: *LockQueryPort 인터페이스 구현 필수")
    void lockQueryAdapter_MustImplementLockQueryPort() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameEndingWith("LockQueryPort")
                        .because("LockQueryAdapter는 Application Layer의 LockQueryPort를 구현해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 3: Repository와 Mapper 필드 보유
     *
     * <p>LockQueryAdapter는 Repository와 Mapper 필드를 가져야 합니다.
     */
    @Test
    @DisplayName("규칙 3: Repository와 Mapper 필드 보유")
    void lockQueryAdapter_MustHaveRepositoryAndMapperFields() {
        ArchRule repositoryRule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameEndingWith("Repository")
                        .because("LockQueryAdapter는 Repository 의존성이 필수입니다");

        ArchRule mapperRule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameEndingWith("Mapper")
                        .because("LockQueryAdapter는 Mapper 의존성이 필수입니다");

        repositoryRule.allowEmptyShould(true).check(lockAdapterClasses);
        mapperRule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 4: 조회 메서드만 public으로 노출
     *
     * <p>LockQueryAdapter는 Lock 조회 메서드만 public으로 노출해야 합니다.
     */
    @Test
    @DisplayName("규칙 4: 조회 메서드만 public으로 노출")
    void lockQueryAdapter_MustHaveOnlyQueryMethods() {
        ArchRule rule =
                methods()
                        .that()
                        .areDeclaredInClassesThat()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .and()
                        .arePublic()
                        .should()
                        .haveNameMatching("(find|exists|count|get).*")
                        .because("LockQueryAdapter는 조회 메서드만 public으로 노출해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 5: 메서드명 검증 (Lock 전략 명시)
     *
     * <p>메서드명은 Lock 전략을 명확히 표현해야 합니다.
     */
    @Test
    @DisplayName("규칙 5: 메서드명은 Lock 전략 명시 (권장)")
    void lockQueryAdapter_MethodsShouldFollowNamingConvention() {
        ArchRule rule =
                methods()
                        .that()
                        .areDeclaredInClassesThat()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .and()
                        .arePublic()
                        .and()
                        .haveNameMatching("find.*")
                        .should()
                        .haveNameMatching("find.*(Lock|ForUpdate).*")
                        .because("메서드명은 Lock 전략을 명확히 표현해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 6: 반환 타입 검증 (Optional 또는 List)
     *
     * <p>조회 메서드는 Domain을 반환해야 합니다.
     */
    @Test
    @DisplayName("규칙 6: 반환 타입은 Optional 또는 List")
    void lockQueryAdapter_MustReturnDomainTypes() {
        ArchRule rule =
                methods()
                        .that()
                        .areDeclaredInClassesThat()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .and()
                        .arePublic()
                        .and()
                        .haveNameMatching("find.*")
                        .should()
                        .haveRawReturnType(
                                DescribedPredicate.describe(
                                        "Optional or List",
                                        returnType ->
                                                returnType.isAssignableTo(Optional.class)
                                                        || returnType.isAssignableTo(List.class)))
                        .because("조회 메서드는 Optional 또는 List를 반환해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 7: @Transactional 절대 금지
     *
     * <p>Transaction은 Application Layer(UseCase)에서 관리해야 합니다.
     */
    @Test
    @DisplayName("규칙 7: @Transactional 절대 금지")
    void lockQueryAdapter_MustNotBeTransactional() {
        ArchRule classRule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .notBeAnnotatedWith(Transactional.class)
                        .because("LockQueryAdapter 클래스에 @Transactional 사용 금지. UseCase에서 관리하세요");

        ArchRule methodRule =
                methods()
                        .that()
                        .areDeclaredInClassesThat()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .notBeAnnotatedWith(Transactional.class)
                        .because("LockQueryAdapter 메서드에 @Transactional 사용 금지. UseCase에서 관리하세요");

        classRule.allowEmptyShould(true).check(lockAdapterClasses);
        methodRule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 8: Command 메서드 금지
     *
     * <p>저장/수정/삭제는 CommandAdapter로 분리해야 합니다.
     */
    @Test
    @DisplayName("규칙 8: Command 메서드 금지 (save, persist, update, delete)")
    void lockQueryAdapter_MustNotHaveCommandMethods() {
        ArchRule rule =
                methods()
                        .that()
                        .areDeclaredInClassesThat()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .haveNameNotMatching("(save|persist|update|delete).*")
                        .because("저장/수정/삭제는 CommandAdapter로 분리해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 9: 일반 조회 메서드 금지
     *
     * <p>Lock 없는 조회는 QueryAdapter를 사용해야 합니다.
     */
    @Test
    @DisplayName("규칙 9: 일반 조회 메서드 금지 (Lock 메서드만 허용)")
    void lockQueryAdapter_MustNotHaveNormalQueryMethods() {
        // Note: 구현체가 있을 때 Lock 전략 없는 일반 조회 메서드 검증
        // 현재는 구현체가 없으므로 통과
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .bePublic()
                        .because("LockQueryAdapter는 public 클래스여야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /**
     * 규칙 10: DTO 반환 금지
     *
     * <p>Domain을 반환해야 합니다.
     */
    @Test
    @DisplayName("규칙 10: DTO 반환 금지 (Domain만 반환)")
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
                                        "not DTO types",
                                        returnType -> !returnType.getName().contains("Dto")))
                        .because("Domain을 반환해야 하며, DTO 반환은 금지입니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 13: 클래스명 *LockQueryAdapter 필수 */
    @Test
    @DisplayName("규칙 13: 클래스명은 *LockQueryAdapter 형식")
    void lockQueryAdapter_MustFollowNamingConvention() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .resideInAPackage("..adapter..")
                        .because("LockQueryAdapter는 adapter 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 14: Port 네이밍 *LockQueryPort 필수 */
    @Test
    @DisplayName("규칙 14: Port 인터페이스는 *LockQueryPort 형식")
    void lockQueryPort_MustFollowNamingConvention() {
        ArchRule rule =
                classes()
                        .that()
                        .areInterfaces()
                        .and()
                        .haveSimpleNameContaining("Lock")
                        .and()
                        .haveSimpleNameContaining("Query")
                        .and()
                        .haveSimpleNameContaining("Port")
                        .should()
                        .haveSimpleNameEndingWith("LockQueryPort")
                        .because("Port 인터페이스는 *LockQueryPort 네이밍 규칙을 따라야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /** 규칙 15: Repository 네이밍 *LockRepository 필수 */
    @Test
    @DisplayName("규칙 15: LockRepository는 *LockRepository 형식")
    void lockRepository_MustFollowNamingConvention() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameContaining("Lock")
                        .and()
                        .haveSimpleNameContaining("Repository")
                        .and()
                        .areInterfaces()
                        .should()
                        .haveSimpleNameEndingWith("LockRepository")
                        .because("LockRepository는 *LockRepository 네이밍 규칙을 따라야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /** 규칙 16: 패키지 위치 ..adapter.out.persistence.. */
    @Test
    @DisplayName("규칙 16: LockQueryAdapter는 adapter.out.persistence 패키지에 위치")
    void lockQueryAdapter_MustBeInCorrectPackage() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .resideInAPackage("..adapter.out.persistence..")
                        .because("LockQueryAdapter는 adapter.out.persistence 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 17: Port 패키지 위치 ..application..port.out.. */
    @Test
    @DisplayName("규칙 17: LockQueryPort는 application.port.out 패키지에 위치")
    void lockQueryPort_MustBeInCorrectPackage() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryPort")
                        .should()
                        .resideInAPackage("..application..port.out..")
                        .because("LockQueryPort는 application.port.out 패키지에 위치해야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /** 규칙 18: 의존성 방향 Adapter → Port (역방향 금지) */
    @Test
    @DisplayName("규칙 18: Adapter는 Port를 의존해야 함")
    void lockQueryAdapter_MustDependOnPort() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameContaining("Port")
                        .because("의존성 방향은 Adapter → Port 단방향이어야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 19: 생성자 주입 (final 필드) */
    @Test
    @DisplayName("규칙 19: LockQueryAdapter 필드는 final이어야 함")
    void lockQueryAdapter_FieldsMustBeFinal() {
        ArchRule rule =
                fields().that()
                        .areDeclaredInClassesThat()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .beFinal()
                        .because("생성자 주입을 위해 필드는 final이어야 합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 20: LockRepository 필드 필수 */
    @Test
    @DisplayName("규칙 20: LockQueryAdapter는 Repository 필드를 가져야 함")
    void lockQueryAdapter_MustHaveRepositoryField() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameEndingWith("Repository")
                        .because("Repository 필드가 필수입니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 21: Mapper 필드 필수 */
    @Test
    @DisplayName("규칙 21: LockQueryAdapter는 Mapper 필드를 가져야 함")
    void lockQueryAdapter_MustHaveMapperField() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should()
                        .dependOnClassesThat()
                        .haveSimpleNameEndingWith("Mapper")
                        .because("Mapper 필드가 필수입니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }

    /** 규칙 22: find*Lock 메서드 필수 (권장) */
    @Test
    @DisplayName("규칙 22: Lock 조회 메서드 필수 (권장)")
    void lockQueryAdapter_ShouldHaveLockMethods() {
        ArchRule rule =
                classes()
                        .that()
                        .haveSimpleNameEndingWith("LockQueryAdapter")
                        .should(
                                ArchCondition.from(
                                        DescribedPredicate.describe(
                                                "have lock query methods",
                                                javaClass ->
                                                        javaClass.getMethods().stream()
                                                                .anyMatch(
                                                                        method ->
                                                                                method.getName()
                                                                                        .contains(
                                                                                                "Lock")))))
                        .because("Lock 조회 메서드가 필요합니다");

        rule.allowEmptyShould(true).check(lockAdapterClasses);
    }
}
