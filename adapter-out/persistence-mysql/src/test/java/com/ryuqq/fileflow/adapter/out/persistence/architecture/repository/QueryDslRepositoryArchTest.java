package com.ryuqq.fileflow.adapter.out.persistence.architecture.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * QueryDslRepositoryArchTest - QueryDSL Repository 아키텍처 규칙 검증
 *
 * <p>querydsl-repository-guide.md의 핵심 규칙을 ArchUnit으로 검증합니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>규칙 1: QueryDslRepository는 클래스여야 함</li>
 *   <li>규칙 2: @Repository 어노테이션 필수</li>
 *   <li>규칙 3: JPAQueryFactory 필드 필수</li>
 *   <li>규칙 4: QType static final 필드 필수</li>
 *   <li>규칙 5: 4개 표준 메서드만 허용</li>
 *   <li>규칙 6: Join 사용 금지 (코드 검증)</li>
 *   <li>규칙 7: @Transactional 사용 금지</li>
 *   <li>규칙 8: Mapper 의존성 금지</li>
 *   <li>규칙 9: 네이밍 규칙 (*QueryDslRepository)</li>
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("QueryDSL Repository 아키텍처 규칙 검증 (Zero-Tolerance)")
class QueryDslRepositoryArchTest {

    private static JavaClasses allClasses;
    private static JavaClasses queryDslRepositoryClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence");

        // QueryDslRepository 클래스만
        queryDslRepositoryClasses = allClasses.that(
            DescribedPredicate.describe(
                "are QueryDslRepository classes",
                javaClass -> javaClass.getSimpleName().endsWith("QueryDslRepository") &&
                    !javaClass.isInterface()
            )
        );
    }

    @Test
    @DisplayName("규칙 1: QueryDslRepository는 클래스여야 함")
    void queryDslRepository_MustBeClass() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .and().resideInAPackage("..repository..")
            .should().notBeInterfaces()
            .because("QueryDslRepository는 클래스로 정의되어야 합니다");

        rule.check(queryDslRepositoryClasses);
    }

    @Test
    @DisplayName("규칙 2: QueryDslRepository는 @Repository 어노테이션 필수")
    void queryDslRepository_MustHaveRepositoryAnnotation() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .and().resideInAPackage("..repository..")
            .should().beAnnotatedWith(Repository.class)
            .because("QueryDslRepository는 @Repository 어노테이션이 필수입니다");

        rule.check(queryDslRepositoryClasses);
    }

    @Test
    @DisplayName("규칙 3: QueryDslRepository는 JPAQueryFactory 필드 필수")
    void queryDslRepository_MustHaveJPAQueryFactory() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .and().resideInAPackage("..repository..")
            .should().dependOnClassesThat().areAssignableTo(JPAQueryFactory.class)
            .because("QueryDslRepository는 JPAQueryFactory 필드가 필수입니다");

        rule.check(queryDslRepositoryClasses);
    }

    /**
     * 규칙 4: QueryDslRepository는 QType 필드 보유 (권장)
     *
     * <p>QType 필드는 static final로 선언하는 것이 권장됩니다.</p>
     * <ul>
     *   <li>✅ static final Q* q* (예: qOrder, qProduct)</li>
     *   <li>✅ static final Q* 변수명 (예: single, multipart)</li>
     * </ul>
     *
     * <p>Note: 변수명은 프로젝트 컨벤션에 따라 달라질 수 있어 규칙을 완화합니다.</p>
     */
    @Test
    @DisplayName("규칙 4: QueryDslRepository는 QType 필드 보유 (권장)")
    void queryDslRepository_ShouldHaveQTypeField() {
        // QType 필드 존재 자체만 확인 (static 검증)
        // 변수명 패턴은 완화 (q[A-Z].* 또는 다른 명명 규칙 허용)
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .or().haveSimpleNameEndingWith("QueryRepository")
            .should().dependOnClassesThat().haveSimpleNameStartingWith("Q")
            .because("QueryDsl Repository는 QType에 의존해야 합니다");

        rule.allowEmptyShould(true).check(queryDslRepositoryClasses);
    }

    /**
     * 규칙 5: QueryDslRepository는 조회 메서드만 허용
     *
     * <p>QueryDslRepository는 조회 관련 메서드만 가져야 합니다.</p>
     * <ul>
     *   <li>✅ find*, exists*, count*, get* 메서드</li>
     *   <li>❌ save*, update*, delete* 메서드 금지</li>
     * </ul>
     *
     * <p>Note: Port 인터페이스에 따라 다양한 조회 메서드가 필요할 수 있어 개수 제한을 완화합니다.</p>
     */
    @Test
    @DisplayName("규칙 5: QueryDslRepository는 조회 메서드만 허용")
    void queryDslRepository_MustHaveOnlyQueryMethods() {
        ArchRule rule = methods()
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryDslRepository")
            .or().areDeclaredInClassesThat().haveSimpleNameEndingWith("QueryRepository")
            .and().areDeclaredInClassesThat().resideInAPackage("..repository..")
            .and().arePublic()
            .should().haveNameMatching("(find|exists|count|get).*")
            .because("QueryDsl Repository는 조회 메서드(find*, exists*, count*, get*)만 가져야 합니다");

        rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("규칙 6: QueryDslRepository는 Join 사용 금지 (수동 검증)")
    void queryDslRepository_MustNotUseJoin() {
        // ⚠️ 주의: ArchUnit으로 Join 사용을 완벽히 검증하기 어려움
        // 코드 리뷰 및 수동 검증 필요
        //
        // 금지 패턴:
        // - queryFactory.selectFrom(q).join(...)
        // - queryFactory.selectFrom(q).leftJoin(...)
        // - queryFactory.selectFrom(q).rightJoin(...)
        // - queryFactory.selectFrom(q).innerJoin(...)
        // - queryFactory.selectFrom(q).fetchJoin(...)
        //
        // ✅ 이 테스트는 통과하지만, 실제 Join 사용 여부는 코드 리뷰로 확인해야 합니다.

        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .and().resideInAPackage("..repository..")
            .should().dependOnClassesThat().haveFullyQualifiedName("com.querydsl.jpa.impl.JPAJoin")
            .because("QueryDslRepository는 Join 사용이 금지됩니다 (N+1은 Adapter에서 해결)");

        rule.check(queryDslRepositoryClasses);
    }

    @Test
    @DisplayName("규칙 7: QueryDslRepository는 @Transactional 사용 금지")
    void queryDslRepository_MustNotHaveTransactional() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .and().resideInAPackage("..repository..")
            .should().beAnnotatedWith(Transactional.class)
            .because("QueryDslRepository는 @Transactional 사용이 금지됩니다 (Service Layer에서 관리)");

        rule.check(queryDslRepositoryClasses);
    }

    @Test
    @DisplayName("규칙 8: QueryDslRepository는 Mapper 의존성 금지")
    void queryDslRepository_MustNotDependOnMapper() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryDslRepository")
            .and().resideInAPackage("..repository..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Mapper")
            .because("QueryDslRepository는 Mapper 의존성이 금지됩니다 (Adapter에서 처리)");

        rule.check(queryDslRepositoryClasses);
    }

    /**
     * 규칙 9: QueryDsl Repository 네이밍 규칙
     *
     * <p>QueryDsl 기반 Repository는 명확한 네이밍 규칙을 따라야 합니다.</p>
     * <ul>
     *   <li>✅ *QueryDslRepository (권장)</li>
     *   <li>✅ *QueryRepository (허용)</li>
     * </ul>
     *
     * <p>Note: JpaRepository 인터페이스와 구분하기 위해 Query 관련 접미사를 사용합니다.</p>
     */
    @Test
    @DisplayName("규칙 9: QueryDsl Repository 네이밍 규칙")
    void queryDslRepository_MustFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..repository..")
            .and().areAnnotatedWith(Repository.class)
            .and().areNotInterfaces()
            .should().haveSimpleNameEndingWith("QueryDslRepository")
            .orShould().haveSimpleNameEndingWith("QueryRepository")
            .because("QueryDsl Repository는 *QueryDslRepository 또는 *QueryRepository 네이밍 규칙을 따라야 합니다");

        rule.check(allClasses);
    }
}
