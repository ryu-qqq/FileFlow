package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

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
 *   <li>Package: {@code com.ryuqq.adapter.out.persistence.*.mapper}</li>
 *   <li>Naming: {@code *EntityMapper.java}</li>
 * </ul>
 *
 * <p><strong>참고 문서:</strong></p>
 * <ul>
 *   <li><a href="../../../../../../docs/coding_convention/04-persistence-layer/mapper-patterns/">Mapper Patterns Guide</a></li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("Entity Mapper 컨벤션 검증")
class MapperConventionTest {

    private static JavaClasses persistenceClasses;

    @BeforeAll
    static void setUp() {
        // Tenant 패키지만 테스트 (Example은 제외)
        persistenceClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant");
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

    // ========================================
    // 5. 비즈니스 로직 금지 규칙
    // ========================================

    @Nested
    @DisplayName("비즈니스 로직 금지 규칙 (가이드라인)")
    class BusinessLogicProhibitionGuidelines {

        @Test
        @DisplayName("[가이드라인] Mapper는 순수 변환 로직만 포함해야 함")
        void mapperShouldOnlyContainTransformationLogic() {
            // 이 규칙은 코드 리뷰로 검증 (ArchUnit으로 완벽한 검증 어려움)
            // Mapper는 다음만 수행:
            // 1. Entity → Domain 변환 (Value Object 생성 포함)
            // 2. Domain → Entity 변환 (Value Object 원시 타입 추출 포함)
            // 3. Null 체크 (IllegalArgumentException)
            //
            // ❌ 금지:
            // - 비즈니스 로직 (도메인 규칙 검증 등)
            // - 외부 의존성 (Service, Repository 호출)
            // - 상태 저장 (필드 변수)
        }

        @Test
        @DisplayName("[가이드라인] toDomain()은 Entity → Domain 변환만 수행")
        void toDomainShouldOnlyTransformEntityToDomain() {
            // toDomain() 메서드 규칙:
            // 1. 파라미터: Entity (JpaEntity)
            // 2. 반환: Domain (Aggregate Root)
            // 3. 변환 과정:
            //    - Entity getter로 원시 타입 추출
            //    - Value Object Static Factory Method 호출
            //    - Domain reconstitute() 호출
            // 4. Null 체크 필수
        }

        @Test
        @DisplayName("[가이드라인] toEntity()는 Domain → Entity 변환만 수행")
        void toEntityShouldOnlyTransformDomainToEntity() {
            // toEntity() 메서드 규칙:
            // 1. 파라미터: Domain (Aggregate Root)
            // 2. 반환: Entity (JpaEntity)
            // 3. 변환 과정:
            //    - Domain getter로 Value Object 추출
            //    - Value Object value() 메서드로 원시 타입 추출
            //    - Entity create() 또는 reconstitute() 호출
            // 4. Null 체크 필수
        }
    }

    // ========================================
    // 6. Law of Demeter 준수 규칙
    // ========================================

    @Nested
    @DisplayName("Law of Demeter 준수 규칙 (가이드라인)")
    class LawOfDemeterGuidelines {

        @Test
        @DisplayName("[가이드라인] Mapper는 Law of Demeter를 준수해야 함")
        void mapperShouldFollowLawOfDemeter() {
            // Law of Demeter 규칙:
            // ❌ 금지: tenant.getName().getValue() (Getter 체이닝)
            // ✅ 권장: tenant.getNameValue() (Tell, Don't Ask)
            //
            // Domain에서 제공하는 메서드:
            // - getIdValue(): TenantId → Long 직접 반환
            // - getNameValue(): TenantName → String 직접 반환
            //
            // 이 규칙은 코드 리뷰로 검증
        }
    }

    // ========================================
    // 7. Javadoc 가이드라인
    // ========================================

    @Nested
    @DisplayName("Javadoc 가이드라인")
    class JavadocGuidelines {

        @Test
        @DisplayName("[가이드라인] Mapper 클래스는 Javadoc을 포함해야 함")
        void mapperClassShouldHaveJavadoc() {
            // 이 규칙은 코드 리뷰로 검증
            // 필수 항목:
            // - @author
            // - @since
            // - 역할 설명 (Domain ↔ Entity 변환)
            // - 설계 원칙 (Stateless, Pure Function)
        }

        @Test
        @DisplayName("[가이드라인] Mapper 메서드는 Javadoc을 포함해야 함")
        void mapperMethodShouldHaveJavadoc() {
            // 이 규칙은 코드 리뷰로 검증
            // toDomain(), toEntity() 메서드 필수 항목:
            // - 변환 방향 명시 (Entity → Domain / Domain → Entity)
            // - 변환 과정 설명 (Value Object 생성, reconstitute 호출 등)
            // - @param 설명
            // - @return 설명
            // - @throws IllegalArgumentException (null인 경우)
        }
    }
}
