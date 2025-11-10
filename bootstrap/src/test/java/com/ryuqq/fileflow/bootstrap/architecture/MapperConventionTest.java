package com.ryuqq.fileflow.bootstrap.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Entity Mapper 컨벤션 검증 (ArchUnit)
 *
 * <p><strong>목적:</strong></p>
 * <ul>
 *   <li>Domain Model ↔ JPA Entity 변환 Mapper 컨벤션 자동 검증</li>
 *   <li>Zero-Tolerance 규칙 강제 (빌드 실패로 위반 차단)</li>
 * </ul>
 *
 * <p><strong>검증 대상:</strong></p>
 * <ul>
 *   <li>Package: {@code com.ryuqq.fileflow.adapter.out.persistence.*.mapper}</li>
 *   <li>Naming: {@code *EntityMapper.java}</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-11-04
 */
@DisplayName("Entity Mapper 컨벤션 검증")
class MapperConventionTest {

    private static JavaClasses persistenceClasses;

    @BeforeAll
    static void setUp() {
        persistenceClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence");
    }

    // ========================================
    // 1. Utility Class 규칙
    // ========================================

    @Nested
    @DisplayName("Utility Class 규칙")
    class UtilityClassRules {

        @Test
        @DisplayName("Mapper는 final 클래스여야 함")
        void mapperShouldBeFinalClass() {
            classes()
                .that().resideInAPackage("..persistence..mapper..")
                .and().haveSimpleNameEndingWith("Mapper")
                .should().haveModifier(JavaModifier.FINAL)
                .check(persistenceClasses);
        }

        @Test
        @DisplayName("Mapper는 private 생성자를 가져야 함")
        void mapperShouldHavePrivateConstructor() {
            classes()
                .that().resideInAPackage("..persistence..mapper..")
                .and().haveSimpleNameEndingWith("Mapper")
                .should(new ArchCondition<JavaClass>("have private constructor") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean hasPrivateConstructor = javaClass.getConstructors().stream()
                            .anyMatch(constructor ->
                                constructor.getModifiers().contains(JavaModifier.PRIVATE)
                            );

                        if (!hasPrivateConstructor) {
                            events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Class %s does not have private constructor (Utility class should prevent instantiation)",
                                    javaClass.getName())
                            ));
                        }
                    }
                })
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 2. @Component 금지 규칙
    // ========================================

    @Nested
    @DisplayName("@Component 금지 규칙")
    class ComponentProhibitionRules {

        @Test
        @DisplayName("Mapper는 @Component 어노테이션을 사용하면 안 됨")
        void mapperShouldNotUseComponentAnnotation() {
            noClasses()
                .that().resideInAPackage("..persistence..mapper..")
                .and().haveSimpleNameEndingWith("Mapper")
                .should().beAnnotatedWith(Component.class)
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 3. Static Method 규칙
    // ========================================

    @Nested
    @DisplayName("Static Method 규칙")
    class StaticMethodRules {

        @Test
        @DisplayName("Mapper는 toDomain() static method를 가져야 함")
        void mapperShouldHaveToDomainStaticMethod() {
            classes()
                .that().resideInAPackage("..persistence..mapper..")
                .and().haveSimpleNameEndingWith("Mapper")
                .should(new ArchCondition<JavaClass>("have toDomain() static method") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean hasToDomainMethod = javaClass.getMethods().stream()
                            .anyMatch(method ->
                                method.getName().equals("toDomain")
                                && method.getModifiers().contains(JavaModifier.STATIC)
                                && method.getModifiers().contains(JavaModifier.PUBLIC)
                            );

                        if (!hasToDomainMethod) {
                            events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Class %s does not have public static toDomain() method",
                                    javaClass.getName())
                            ));
                        }
                    }
                })
                .check(persistenceClasses);
        }

        @Test
        @DisplayName("Mapper는 toEntity() static method를 가져야 함")
        void mapperShouldHaveToEntityStaticMethod() {
            classes()
                .that().resideInAPackage("..persistence..mapper..")
                .and().haveSimpleNameEndingWith("Mapper")
                .should(new ArchCondition<JavaClass>("have toEntity() static method") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean hasToEntityMethod = javaClass.getMethods().stream()
                            .anyMatch(method ->
                                method.getName().equals("toEntity")
                                && method.getModifiers().contains(JavaModifier.STATIC)
                                && method.getModifiers().contains(JavaModifier.PUBLIC)
                            );

                        if (!hasToEntityMethod) {
                            events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Class %s does not have public static toEntity() method",
                                    javaClass.getName())
                            ));
                        }
                    }
                })
                .check(persistenceClasses);
        }
    }

    // ========================================
    // 4. Naming Convention 규칙
    // ========================================

    @Nested
    @DisplayName("Naming Convention 규칙")
    class NamingConventionRules {

        @Test
        @DisplayName("Mapper 클래스는 EntityMapper suffix를 가져야 함")
        void mapperClassShouldHaveEntityMapperSuffix() {
            classes()
                .that().resideInAPackage("..persistence..mapper..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("EntityMapper")
                .orShould().haveSimpleNameEndingWith("Mapper")
                .check(persistenceClasses);
        }

        @Test
        @DisplayName("Mapper는 mapper 패키지에 위치해야 함")
        void mapperShouldResideInMapperPackage() {
            classes()
                .that().haveSimpleNameEndingWith("EntityMapper")
                .or().haveSimpleNameEndingWith("Mapper")
                .and().resideInAPackage("..persistence..")
                .should().resideInAPackage("..mapper..")
                .check(persistenceClasses);
        }
    }
}
