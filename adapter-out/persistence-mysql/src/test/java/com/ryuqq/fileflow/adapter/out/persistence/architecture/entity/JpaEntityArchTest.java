package com.ryuqq.fileflow.adapter.out.persistence.architecture.entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JpaEntityArchTest - JPA Entity 아키텍처 규칙 검증
 *
 * <p>entity-guide.md의 핵심 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>검증 규칙 그룹:</strong>
 *
 * <ul>
 *   <li>Lombok 금지 (6개 규칙)
 *   <li>JPA 관계 어노테이션 금지 (4개 규칙)
 *   <li>메서드 패턴 규칙 (2개 규칙)
 *   <li>생성자 및 팩토리 규칙 (4개 규칙)
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 * @version 2.0.0
 */
@DisplayName("JPA Entity 아키텍처 규칙 검증 (Zero-Tolerance)")
class JpaEntityArchTest {

    private static JavaClasses allClasses;
    private static JavaClasses entityClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter().importPackages("com.ryuqq.adapter.out.persistence");

        entityClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "are JPA Entity classes",
                                javaClass -> javaClass.isAnnotatedWith(Entity.class)));
    }

    // ===== 1. Lombok 금지 규칙 =====

    @Nested
    @DisplayName("Lombok 금지 규칙")
    class LombokProhibitionRules {

        @Test
        @DisplayName("@Data 어노테이션 금지")
        void jpaEntity_MustNotUseLombok_Data() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith("lombok.Data")
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Lombok 사용이 금지됩니다 (Plain Java 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@Getter 어노테이션 금지")
        void jpaEntity_MustNotUseLombok_Getter() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith("lombok.Getter")
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Lombok 사용이 금지됩니다 (Plain Java 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@Setter 어노테이션 금지")
        void jpaEntity_MustNotUseLombok_Setter() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith("lombok.Setter")
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Lombok 사용이 금지됩니다 (Plain Java 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@Builder 어노테이션 금지")
        void jpaEntity_MustNotUseLombok_Builder() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith("lombok.Builder")
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Lombok 사용이 금지됩니다 (Plain Java 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@AllArgsConstructor 어노테이션 금지")
        void jpaEntity_MustNotUseLombok_AllArgsConstructor() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith("lombok.AllArgsConstructor")
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Lombok 사용이 금지됩니다 (Plain Java 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@NoArgsConstructor 어노테이션 금지")
        void jpaEntity_MustNotUseLombok_NoArgsConstructor() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith("lombok.NoArgsConstructor")
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Lombok 사용이 금지됩니다 (Plain Java 사용)");

            rule.check(entityClasses);
        }
    }

    // ===== 2. JPA 관계 어노테이션 금지 규칙 =====

    @Nested
    @DisplayName("JPA 관계 어노테이션 금지 규칙 (Long FK 전략)")
    class JpaRelationshipProhibitionRules {

        @Test
        @DisplayName("@ManyToOne 어노테이션 금지")
        void jpaEntity_MustNotUseJpaRelationship_ManyToOne() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith(ManyToOne.class)
                            .allowEmptyShould(true)
                            .because("JPA Entity는 관계 어노테이션 사용이 금지됩니다 (Long FK 전략 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@OneToMany 어노테이션 금지")
        void jpaEntity_MustNotUseJpaRelationship_OneToMany() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith(OneToMany.class)
                            .allowEmptyShould(true)
                            .because("JPA Entity는 관계 어노테이션 사용이 금지됩니다 (Long FK 전략 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@OneToOne 어노테이션 금지")
        void jpaEntity_MustNotUseJpaRelationship_OneToOne() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith(OneToOne.class)
                            .allowEmptyShould(true)
                            .because("JPA Entity는 관계 어노테이션 사용이 금지됩니다 (Long FK 전략 사용)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("@ManyToMany 어노테이션 금지")
        void jpaEntity_MustNotUseJpaRelationship_ManyToMany() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .notBeAnnotatedWith(ManyToMany.class)
                            .allowEmptyShould(true)
                            .because("JPA Entity는 관계 어노테이션 사용이 금지됩니다 (Long FK 전략 사용)");

            rule.check(entityClasses);
        }
    }

    // ===== 3. 메서드 패턴 규칙 =====

    @Nested
    @DisplayName("메서드 패턴 규칙")
    class MethodPatternRules {

        @Test
        @DisplayName("Setter 메서드 금지")
        void jpaEntity_MustNotHaveSetterMethods() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should(notHaveSetterMethods())
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Setter 메서드가 금지됩니다 (불변성 보장)");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("비즈니스 로직 메서드 금지")
        void jpaEntity_MustNotHaveBusinessLogicMethods() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should(notHaveBusinessLogicMethods())
                            .allowEmptyShould(true)
                            .because("JPA Entity는 비즈니스 로직이 금지됩니다 (Domain Layer에서 처리)");

            rule.check(entityClasses);
        }
    }

    // ===== 4. 생성자 및 팩토리 규칙 =====

    @Nested
    @DisplayName("생성자 및 팩토리 규칙")
    class ConstructorAndFactoryRules {

        @Test
        @DisplayName("@Entity 어노테이션 필수")
        void jpaEntity_MustBeAnnotatedWithEntity() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("JpaEntity")
                            .should()
                            .beAnnotatedWith(Entity.class)
                            .allowEmptyShould(true)
                            .because("JPA Entity 클래스는 @Entity 어노테이션이 필수입니다");

            rule.check(allClasses);
        }

        @Test
        @DisplayName("protected 또는 public 기본 생성자 필수")
        void jpaEntity_MustHaveProtectedOrPublicNoArgsConstructor() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should(haveProtectedOrPublicNoArgsConstructor())
                            .allowEmptyShould(true)
                            .because("JPA Entity는 JPA 스펙을 위해 protected 또는 public 기본 생성자가 필수입니다");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("private 전체 필드 생성자 필수")
        void jpaEntity_MustHavePrivateAllArgsConstructor() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should(havePrivateConstructorWithParameters())
                            .allowEmptyShould(true)
                            .because("JPA Entity는 무분별한 생성 방지를 위해 private 생성자가 필수입니다");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("public static of() 메서드 필수")
        void jpaEntity_MustHavePublicStaticOfMethod() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should(havePublicStaticOfMethod())
                            .allowEmptyShould(true)
                            .because("JPA Entity는 Mapper 전용 of() 스태틱 메서드가 필수입니다");

            rule.check(entityClasses);
        }

        @Test
        @DisplayName("Entity 네이밍 규칙 (*JpaEntity)")
        void jpaEntity_MustFollowNamingConvention() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAnnotatedWith(Entity.class)
                            .should()
                            .haveSimpleNameEndingWith("JpaEntity")
                            .allowEmptyShould(true)
                            .because("JPA Entity 클래스는 *JpaEntity 네이밍 규칙을 따라야 합니다");

            rule.check(entityClasses);
        }
    }

    // ===== 커스텀 ArchCondition =====

    /**
     * Setter 메서드가 없어야 함을 검증
     *
     * <p>setXxx() 패턴의 public 메서드를 검출합니다.
     */
    private static ArchCondition<JavaClass> notHaveSetterMethods() {
        return new ArchCondition<>("not have setter methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                        .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                        .filter(method -> method.getName().matches("set[A-Z].*"))
                        .filter(method -> method.getRawParameterTypes().size() == 1)
                        .forEach(
                                method -> {
                                    String message =
                                            String.format(
                                                    "클래스 %s가 setter 메서드 %s()를 가지고 있습니다 (Setter 금지)",
                                                    javaClass.getSimpleName(), method.getName());
                                    events.add(SimpleConditionEvent.violated(javaClass, message));
                                });
            }
        };
    }

    /**
     * 비즈니스 로직 메서드가 없어야 함을 검증
     *
     * <p>approve, cancel, complete 등의 비즈니스 동작 메서드를 검출합니다.
     */
    private static ArchCondition<JavaClass> notHaveBusinessLogicMethods() {
        return new ArchCondition<>("not have business logic methods") {
            private static final String BUSINESS_METHOD_PATTERN =
                    "(approve|cancel|complete|activate|deactivate|validate|calculate|process|execute|perform).*";

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                        .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                        .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                        .filter(method -> method.getName().matches(BUSINESS_METHOD_PATTERN))
                        .forEach(
                                method -> {
                                    String message =
                                            String.format(
                                                    "클래스 %s가 비즈니스 로직 메서드 %s()를 가지고 있습니다 (비즈니스 로직은"
                                                            + " Domain Layer에서 처리)",
                                                    javaClass.getSimpleName(), method.getName());
                                    events.add(SimpleConditionEvent.violated(javaClass, message));
                                });
            }
        };
    }

    /**
     * protected 또는 public 기본 생성자 존재 검증
     *
     * <p>JPA 스펙 준수를 위해 파라미터 없는 생성자가 필요합니다.
     */
    private static ArchCondition<JavaClass> haveProtectedOrPublicNoArgsConstructor() {
        return new ArchCondition<>("have protected or public no-args constructor") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasValidNoArgsConstructor =
                        javaClass.getConstructors().stream()
                                .anyMatch(
                                        constructor ->
                                                constructor.getParameters().isEmpty()
                                                        && (constructor
                                                                        .getModifiers()
                                                                        .contains(
                                                                                JavaModifier
                                                                                        .PROTECTED)
                                                                || constructor
                                                                        .getModifiers()
                                                                        .contains(
                                                                                JavaModifier
                                                                                        .PUBLIC)));

                if (!hasValidNoArgsConstructor) {
                    String message =
                            String.format(
                                    "클래스 %s가 protected 또는 public 기본 생성자를 가지고 있지 않습니다 (JPA 스펙 필수)",
                                    javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * private 파라미터 있는 생성자 존재 검증
     *
     * <p>무분별한 인스턴스 생성을 방지합니다.
     */
    private static ArchCondition<JavaClass> havePrivateConstructorWithParameters() {
        return new ArchCondition<>("have private constructor with parameters") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasPrivateConstructor =
                        javaClass.getConstructors().stream()
                                .anyMatch(
                                        constructor ->
                                                constructor
                                                                .getModifiers()
                                                                .contains(JavaModifier.PRIVATE)
                                                        && !constructor.getParameters().isEmpty());

                if (!hasPrivateConstructor) {
                    String message =
                            String.format(
                                    "클래스 %s가 private 파라미터 생성자를 가지고 있지 않습니다 (무분별한 생성 방지 필수)",
                                    javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * public static of() 메서드 존재 검증
     *
     * <p>Mapper에서 Entity 생성 시 사용하는 팩토리 메서드입니다.
     */
    private static ArchCondition<JavaClass> havePublicStaticOfMethod() {
        return new ArchCondition<>("have public static of() method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasOfMethod =
                        javaClass.getMethods().stream()
                                .anyMatch(
                                        method ->
                                                method.getName().equals("of")
                                                        && method.getModifiers()
                                                                .contains(JavaModifier.PUBLIC)
                                                        && method.getModifiers()
                                                                .contains(JavaModifier.STATIC));

                if (!hasOfMethod) {
                    String message =
                            String.format(
                                    "클래스 %s가 public static of() 메서드를 가지고 있지 않습니다 (Mapper 전용 팩토리 메서드"
                                            + " 필수)",
                                    javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
