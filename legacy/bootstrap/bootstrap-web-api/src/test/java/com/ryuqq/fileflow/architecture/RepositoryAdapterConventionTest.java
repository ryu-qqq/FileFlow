package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Repository Adapter 컨벤션 검증 (ArchUnit)
 *
 * <p><strong>목적:</strong></p>
 * <ul>
 *   <li>Hexagonal Architecture Adapter Layer의 Repository Adapter 컨벤션 자동 검증</li>
 *   <li>Zero-Tolerance 규칙 강제 (빌드 실패로 위반 차단)</li>
 * </ul>
 *
 * <p><strong>검증 대상:</strong></p>
 * <ul>
 *   <li>Package: {@code com.ryuqq.adapter.out.persistence.*.adapter}</li>
 *   <li>Naming: {@code *Adapter.java} (Command, Query, Persistence)</li>
 * </ul>
 *
 * <p><strong>참고 문서:</strong></p>
 * <ul>
 *   <li><a href="../../../../../../docs/coding_convention/04-persistence-layer/repository-patterns/">Repository Patterns Guide</a></li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("Repository Adapter 컨벤션 검증")
class RepositoryAdapterConventionTest {

    private static JavaClasses persistenceClasses;

    @BeforeAll
    static void setUp() {
        // Tenant 패키지만 테스트 (Example은 제외)
        persistenceClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant");
    }

    // ========================================
    // 1. @Component vs @Repository 규칙
    // ========================================

    @Nested
    @DisplayName("@Component vs @Repository 규칙")
    class ComponentAnnotationRules {

        @Test
        @DisplayName("Adapter는 @Component 어노테이션을 사용해야 함")
        void adapterShouldUseComponentAnnotation() {
            classes()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().beAnnotatedWith(Component.class)
                .check(persistenceClasses);
        }

        @Test
        @DisplayName("Adapter는 @Repository 어노테이션을 사용하면 안 됨")
        void adapterShouldNotUseRepositoryAnnotation() {
            noClasses()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().beAnnotatedWith(Repository.class)
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 2. @Transactional 금지 규칙
    // ========================================

    @Nested
    @DisplayName("@Transactional 금지 규칙")
    class TransactionalProhibitionRules {

        @Test
        @DisplayName("Adapter 클래스는 @Transactional 어노테이션을 사용하면 안 됨")
        void adapterClassShouldNotUseTransactional() {
            noClasses()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().beAnnotatedWith(Transactional.class)
                .check(persistenceClasses);
        }

        @Test
        @DisplayName("Adapter 메서드는 @Transactional 어노테이션을 사용하면 안 됨")
        void adapterMethodShouldNotUseTransactional() {
            noClasses()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should(new ArchCondition<JavaClass>("not have @Transactional on methods") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        javaClass.getMethods().forEach(method -> {
                            boolean hasTransactional = method.isAnnotatedWith(Transactional.class);
                            if (hasTransactional) {
                                events.add(SimpleConditionEvent.violated(
                                    method,
                                    String.format("Method %s.%s() has @Transactional (only Application Layer UseCase should use @Transactional)",
                                        javaClass.getSimpleName(),
                                        method.getName())
                                ));
                            }
                        });
                    }
                })
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 3. Constructor Injection 규칙
    // ========================================

    @Nested
    @DisplayName("Constructor Injection 규칙")
    class ConstructorInjectionRules {

        @Test
        @DisplayName("Adapter는 단일 public 생성자를 가져야 함")
        void adapterShouldHaveSinglePublicConstructor() {
            classes()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should(new ArchCondition<JavaClass>("have single public constructor") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        long publicConstructorCount = javaClass.getConstructors().stream()
                            .filter(constructor -> constructor.getModifiers().contains(JavaModifier.PUBLIC))
                            .count();

                        if (publicConstructorCount != 1) {
                            events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Class %s has %d public constructors (expected 1 for constructor injection)",
                                    javaClass.getName(),
                                    publicConstructorCount)
                            ));
                        }
                    }
                })
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 4. Port 인터페이스 구현 규칙
    // ========================================

    @Nested
    @DisplayName("Port 인터페이스 구현 규칙")
    class PortImplementationRules {

        @Test
        @DisplayName("Adapter는 최소 하나의 Port 인터페이스를 구현해야 함")
        void adapterShouldImplementAtLeastOnePortInterface() {
            classes()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should(new ArchCondition<JavaClass>("implement at least one Port interface") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean implementsPortInterface = javaClass.getAllRawInterfaces().stream()
                            .anyMatch(iface ->
                                iface.getPackageName().contains("application")
                                && iface.getSimpleName().endsWith("Port")
                            );

                        if (!implementsPortInterface) {
                            events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Class %s does not implement any Port interface (Application Layer)",
                                    javaClass.getName())
                            ));
                        }
                    }
                })
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 5. Naming Convention 규칙
    // ========================================

    @Nested
    @DisplayName("Naming Convention 규칙")
    class NamingConventionRules {

        @Test
        @DisplayName("Persistence Adapter는 명확한 명명 규칙을 따라야 함")
        void persistenceAdapterShouldFollowNamingConvention() {
            classes()
                .that().resideInAPackage("..persistence..adapter..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Adapter")
                .orShould().haveSimpleNameEndingWith("PersistenceAdapter")
                .orShould().haveSimpleNameEndingWith("RepositoryAdapter")
                .check(persistenceClasses);
        }

        @Test
        @DisplayName("Query Adapter는 명확한 명명 규칙을 따라야 함")
        void queryAdapterShouldFollowNamingConvention() {
            classes()
                .that().resideInAPackage("..persistence..adapter..")
                .and().haveSimpleNameContaining("Query")
                .should().haveSimpleNameEndingWith("Adapter")
                .orShould().haveSimpleNameEndingWith("RepositoryAdapter")
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 6. 비즈니스 로직 금지 규칙
    // ========================================

    @Nested
    @DisplayName("비즈니스 로직 금지 규칙 (가이드라인)")
    class BusinessLogicProhibitionGuidelines {

        @Test
        @DisplayName("[가이드라인] Adapter는 단순 변환 및 위임만 수행해야 함")
        void adapterShouldOnlyDelegateAndTransform() {
            // 이 규칙은 코드 리뷰로 검증 (ArchUnit으로 완벽한 검증 어려움)
            // Adapter는 다음만 수행:
            // 1. Domain → Entity 변환 (Mapper 호출)
            // 2. JpaRepository 호출
            // 3. Entity → Domain 변환 (Mapper 호출)
            // 4. 결과 반환
            //
            // ❌ 금지:
            // - 비즈니스 로직 (도메인 규칙 검증 등)
            // - 복잡한 조건문 (QueryDSL BooleanExpression은 예외)
            // - 외부 API 호출
        }
    }

    // ========================================
    // 7. Javadoc 가이드라인
    // ========================================

    @Nested
    @DisplayName("Javadoc 가이드라인")
    class JavadocGuidelines {

        @Test
        @DisplayName("[가이드라인] Adapter 클래스는 Javadoc을 포함해야 함")
        void adapterClassShouldHaveJavadoc() {
            // 이 규칙은 코드 리뷰로 검증
            // 필수 항목:
            // - @author
            // - @since
            // - 역할 설명 (Port 구현, JpaRepository 위임)
            // - 설계 원칙 (DIP, 의존성 방향)
        }
    }
}
