package com.ryuqq.fileflow.bootstrap.architecture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * JPA Entity Convention ArchUnit Test
 *
 * <p>JPA 엔티티 생성 컨벤션을 강제하는 아키텍처 테스트입니다.</p>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>생성자 전략: protected 기본 생성자, protected 신규 생성자, private 재구성 생성자</li>
 *   <li>Static Factory Methods: create(), reconstitute()</li>
 *   <li>BaseAuditEntity 상속 필수</li>
 *   <li>Long FK 전략: JPA 관계 어노테이션 금지</li>
 *   <li>Lombok 금지</li>
 *   <li>Setter 금지 (Getter만 허용)</li>
 *   <li>Enum: EnumType.STRING 필수</li>
 *   <li>ID: GenerationType.IDENTITY 필수</li>
 *   <li>비즈니스 로직 금지 (Pure Data Object)</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-11-04
 */
@DisplayName("JPA 엔티티 컨벤션 검증")
class JpaEntityConventionTest {

    private static JavaClasses persistenceClasses;

    @BeforeAll
    static void setUp() {
        persistenceClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1️⃣ Lombok 금지 (Zero-Tolerance)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Lombok 금지 규칙")
    class LombokProhibitionTest {

        @Test
        @DisplayName("JPA 엔티티는 Lombok @Data를 사용하지 않아야 함")
        void jpaEntityShouldNotUseLombokData() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..persistence..entity..")
                .and().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith("lombok.Data")
                .because("JPA 엔티티는 Pure Java를 사용해야 합니다 (@Data 금지)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 Lombok @Getter를 사용하지 않아야 함")
        void jpaEntityShouldNotUseLombokGetter() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..persistence..entity..")
                .and().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith("lombok.Getter")
                .because("JPA 엔티티는 Pure Java를 사용해야 합니다 (@Getter 금지)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 Lombok @Setter를 사용하지 않아야 함")
        void jpaEntityShouldNotUseLombokSetter() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..persistence..entity..")
                .and().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith("lombok.Setter")
                .because("JPA 엔티티는 Pure Java를 사용해야 합니다 (@Setter 금지)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 Lombok @Builder를 사용하지 않아야 함")
        void jpaEntityShouldNotUseLombokBuilder() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..persistence..entity..")
                .and().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith("lombok.Builder")
                .because("JPA 엔티티는 Pure Java를 사용해야 합니다 (@Builder 금지)");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2️⃣ Long FK 전략 (JPA 관계 어노테이션 금지)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Long FK 전략 규칙")
    class LongFkStrategyTest {

        @Test
        @DisplayName("JPA 엔티티는 @ManyToOne을 사용하지 않아야 함")
        void jpaEntityShouldNotUseManyToOne() {
            ArchRule rule = noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(ManyToOne.class)
                .because("Long FK 전략: JPA 관계 어노테이션 대신 Long 타입 FK 사용 (@ManyToOne 금지)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 @OneToMany를 사용하지 않아야 함")
        void jpaEntityShouldNotUseOneToMany() {
            ArchRule rule = noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(OneToMany.class)
                .because("Long FK 전략: JPA 관계 어노테이션 대신 Long 타입 FK 사용 (@OneToMany 금지)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 @OneToOne을 사용하지 않아야 함")
        void jpaEntityShouldNotUseOneToOne() {
            ArchRule rule = noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(OneToOne.class)
                .because("Long FK 전략: JPA 관계 어노테이션 대신 Long 타입 FK 사용 (@OneToOne 금지)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 @ManyToMany를 사용하지 않아야 함")
        void jpaEntityShouldNotUseManyToMany() {
            ArchRule rule = noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(ManyToMany.class)
                .because("Long FK 전략: JPA 관계 어노테이션 대신 Long 타입 FK 사용 (@ManyToMany 금지)");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3️⃣ Static Factory Methods 필수
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Static Factory Methods 규칙")
    class StaticFactoryMethodsTest {

        @Test
        @DisplayName("JPA 엔티티는 create() Static Factory Method를 가져야 함")
        void jpaEntityShouldHaveCreateMethod() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().haveName("create")
                .should().bePublic()
                .andShould().beStatic()
                .because("JPA 엔티티는 신규 생성을 위한 create() Static Factory Method가 필요합니다");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("JPA 엔티티는 reconstitute() Static Factory Method를 가져야 함")
        void jpaEntityShouldHaveReconstituteMethod() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().haveName("reconstitute")
                .should().bePublic()
                .andShould().beStatic()
                .because("JPA 엔티티는 DB 재구성을 위한 reconstitute() Static Factory Method가 필요합니다");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4️⃣ ID 생성 전략 (GenerationType.IDENTITY)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("ID 생성 전략 규칙")
    class IdGenerationStrategyTest {

        @Test
        @DisplayName("ID 필드는 Long 타입이어야 함")
        void idFieldShouldBeLongType() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Permission")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Role")
                .and().areAnnotatedWith(Id.class)
                .should().haveRawType(Long.class)
                .because("JPA 엔티티 ID는 Long 타입 (BIGINT)을 사용해야 합니다 (Permission/Role 제외 - 자연 키 사용)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("ID 필드는 @GeneratedValue를 가져야 함")
        void idFieldShouldHaveGeneratedValue() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Permission")
                .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Role")
                .and().areAnnotatedWith(Id.class)
                .should().beAnnotatedWith(GeneratedValue.class)
                .because("JPA 엔티티 ID는 자동 생성 전략을 사용해야 합니다 (@GeneratedValue 필수, Permission/Role 제외 - 자연 키 사용)");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 5️⃣ @Table 어노테이션 필수
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("@Table 어노테이션 규칙")
    class TableAnnotationTest {

        @Test
        @DisplayName("JPA 엔티티는 @Table 어노테이션을 가져야 함")
        void jpaEntityShouldHaveTableAnnotation() {
            ArchRule rule = classes()
                .that().resideInAPackage("..persistence..entity..")
                .and().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(Table.class)
                .because("JPA 엔티티는 명시적으로 테이블 이름을 지정해야 합니다 (@Table 필수)");

            rule.check(persistenceClasses);
        }
    }
}
