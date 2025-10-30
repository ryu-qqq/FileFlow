package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * 공통 테스트 규칙 검증 (전체 모듈)
 *
 * <p>TestFixture 패턴 등 모든 레이어에 공통으로 적용되는 테스트 컨벤션을 검증합니다.</p>
 *
 * <h3>검증 대상 모듈:</h3>
 * <ul>
 *   <li>domain</li>
 *   <li>application</li>
 *   <li>adapter-in/rest-api</li>
 *   <li>adapter-out/persistence-mysql</li>
 * </ul>
 *
 * <h3>검증 규칙:</h3>
 * <ul>
 *   <li>Fixture 클래스는 {@code Fixture} 접미사를 가져야 함</li>
 *   <li>Fixture 클래스는 {@code create} 로 시작하는 메서드를 가져야 함</li>
 *   <li>Fixture 클래스는 {@code testFixtures} 소스셋에 위치해야 함</li>
 *   <li>Fixture 메서드는 {@code static} 이어야 함</li>
 * </ul>
 *
 * @see <a href="file://docs/coding_convention/05-testing/02_test-fixture-pattern.md">TestFixture Pattern 가이드</a>
 * @author Claude Code
 * @since 1.0.0
 */
@DisplayName("공통 테스트 규칙 검증 (전체 모듈)")
class CommonTestingRulesTest {

    private JavaClasses allModulesClasses;

    @BeforeEach
    void setUp() {
        // 모든 모듈의 testFixtures 스캔
        allModulesClasses = new ClassFileImporter()
            .importPackages(
                "com.ryuqq.fileflow.domain",
                "com.ryuqq.fileflow.application",
                "com.ryuqq.fileflow.adapter.rest",
                "com.ryuqq.fileflow.adapter.out.persistence"
            );
    }

    // =====================================================
    // TestFixture Pattern 검증
    // =====================================================

    @Test
    @DisplayName("Fixture 클래스는 Fixture 접미사를 가져야 함")
    void fixtureClassesShouldHaveFixtureSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage("..fixture..")
            .and().areNotMemberClasses()
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotAnonymousClasses()  // 익명 클래스 제외 (람다 표현식의 내부 클래스)
            .should().haveSimpleNameEndingWith("Fixture")
            .because("Fixture 클래스는 Fixture 접미사를 사용해야 합니다. " +
                     "예: ExampleDomainFixture, CreateExampleCommandFixture");

        rule.check(allModulesClasses);
    }

    @Test
    @DisplayName("Fixture 접미사를 가진 클래스는 fixture 패키지에 위치해야 함")
    void classesWithFixtureSuffixShouldBeInFixturePackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Fixture")
            .and().areNotMemberClasses()
            .should().resideInAPackage("..fixture..")
            .because("Fixture 클래스는 반드시 fixture 패키지에 위치해야 합니다. " +
                     "예: com.ryuqq.domain.example.fixture");

        rule.check(allModulesClasses);
    }

    @Test
    @DisplayName("Fixture 클래스는 create로 시작하는 static 메서드를 가져야 함")
    void fixtureClassesShouldHaveCreateMethod() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Fixture")
            .should(haveStaticMethodWithNameStartingWith("create"))
            .because("Fixture 클래스는 create*() 형태의 static 메서드를 제공해야 합니다. " +
                     "예: create(), createWithMessage(), createWithId()");

        rule.check(allModulesClasses);
    }

    @Test
    @DisplayName("Fixture 클래스의 생성자는 private이어야 함")
    void fixtureClassesShouldHavePrivateConstructor() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Fixture")
            .should(haveOnlyPrivateConstructors())
            .because("Fixture 클래스는 Utility 클래스이므로 인스턴스화를 방지해야 합니다. " +
                     "생성자를 private으로 선언하세요.");

        rule.check(allModulesClasses);
    }

    // =====================================================
    // Custom ArchConditions
    // =====================================================

    /**
     * 클래스가 특정 이름으로 시작하는 static 메서드를 가지는지 검증
     */
    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass> haveStaticMethodWithNameStartingWith(String prefix) {
        return new ArchCondition<com.tngtech.archunit.core.domain.JavaClass>("have static method with name starting with '" + prefix + "'") {
            @Override
            public void check(com.tngtech.archunit.core.domain.JavaClass javaClass, ConditionEvents events) {
                boolean hasCreateMethod = javaClass.getMethods().stream()
                    .anyMatch(method ->
                        method.getName().startsWith(prefix) &&
                        method.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC)
                    );

                if (!hasCreateMethod) {
                    String message = String.format(
                        "Class %s does not have a static method starting with '%s'",
                        javaClass.getName(),
                        prefix
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * 클래스가 private 생성자만 가지는지 검증
     */
    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass> haveOnlyPrivateConstructors() {
        return new ArchCondition<com.tngtech.archunit.core.domain.JavaClass>("have only private constructors") {
            @Override
            public void check(com.tngtech.archunit.core.domain.JavaClass javaClass, ConditionEvents events) {
                boolean hasPublicOrProtectedConstructor = javaClass.getConstructors().stream()
                    .anyMatch(constructor ->
                        constructor.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC) ||
                        constructor.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PROTECTED)
                    );

                if (hasPublicOrProtectedConstructor) {
                    String message = String.format(
                        "Class %s has public or protected constructors. Fixture classes should only have private constructors.",
                        javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
