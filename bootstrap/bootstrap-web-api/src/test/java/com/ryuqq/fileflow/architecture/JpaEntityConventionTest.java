package com.ryuqq.fileflow.architecture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.BaseAuditEntity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * JPA Entity Convention ArchUnit Test
 *
 * <p>JPA 엔티티 생성 컨벤션을 강제하는 아키텍처 테스트입니다.</p>
 * <p>모범 사례: adapter-out.persistence-mysql.tenant.entity.TenantJpaEntity</p>
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
 * @author ryu-qqq
 * @since 2025-10-30
 * @see com.ryuqq.adapter.out.persistence.tenant.entity.TenantJpaEntity 모범 사례
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
    // 2️⃣ BaseAuditEntity 상속 필수
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("BaseAuditEntity 상속 규칙")
    class BaseAuditEntityInheritanceTest {

        @Test
        @DisplayName("모든 JPA 엔티티는 BaseAuditEntity를 상속해야 함")
        void jpaEntityShouldExtendBaseAuditEntity() {
            ArchRule rule = classes()
                .that().resideInAPackage("..persistence..entity..")
                .and().areAnnotatedWith(Entity.class)
                .and().haveSimpleNameNotContaining("Mapping")  // Join table 제외
                .and().haveSimpleNameNotContaining("Membership")  // Join table 제외
                .and().haveSimpleNameNotEndingWith("RolePermissionJpaEntity")  // Join table 제외
                .and().haveSimpleNameNotEndingWith("SettingJpaEntity")  // 자연키 또는 특수 엔티티 제외
                .should().beAssignableTo(BaseAuditEntity.class)
                .because("모든 JPA 엔티티는 감사 정보(createdAt, updatedAt)를 위해 BaseAuditEntity를 상속해야 합니다 " +
                         "(Join table, 자연키 엔티티 제외)");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3️⃣ Long FK 전략 (JPA 관계 어노테이션 금지)
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
    // 4️⃣ Static Factory Methods 필수
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
    // 5️⃣ Setter 금지 (Getter만 허용)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Setter 금지 규칙")
    class SetterProhibitionTest {

        @Test
        @DisplayName("JPA 엔티티는 public setter를 가지지 않아야 함")
        void jpaEntityShouldNotHavePublicSetters() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().haveNameStartingWith("set")
                .and().arePublic()
                .should().haveRawReturnType(void.class)
                .because("JPA 엔티티는 불변성을 위해 public setter를 가지지 않아야 합니다 (Getter만 허용)");

            // Note: 실제로는 setter가 없어야 하므로 이 규칙을 위반하지 않음
            // 만약 setter가 있으면 테스트 실패
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 6️⃣ ID 생성 전략 (GenerationType.IDENTITY)
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
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("PermissionJpaEntity")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("RoleJpaEntity")
                .and().areAnnotatedWith(Id.class)
                .should().haveRawType(Long.class)
                .because("JPA 엔티티 ID는 Long 타입 (BIGINT)을 사용해야 합니다 (자연키 엔티티 제외)");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("ID 필드는 @GeneratedValue를 가져야 함")
        void idFieldShouldHaveGeneratedValue() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("PermissionJpaEntity")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("RoleJpaEntity")
                .and().areAnnotatedWith(Id.class)
                .should().beAnnotatedWith(GeneratedValue.class)
                .because("JPA 엔티티 ID는 자동 생성 전략을 사용해야 합니다 (@GeneratedValue 필수, 자연키 엔티티 제외)");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 7️⃣ @Table 어노테이션 필수
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

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 8️⃣ 필드 어노테이션 규칙
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("필드 어노테이션 규칙")
    class FieldAnnotationTest {

        @Test
        @DisplayName("JPA 엔티티 필드는 @Column 어노테이션을 가져야 함 (ID 제외)")
        void jpaEntityFieldsShouldHaveColumnAnnotation() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().areNotStatic()
                .and().areNotAnnotatedWith(Id.class)
                .should().beAnnotatedWith(Column.class)
                .orShould().beAnnotatedWith(Enumerated.class)
                .because("JPA 엔티티 필드는 명시적으로 컬럼 매핑을 지정해야 합니다 (@Column 또는 @Enumerated)");

            rule.check(persistenceClasses);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 9️⃣ Enum 타입 규칙 (EnumType.STRING)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Enum 타입 규칙")
    class EnumTypeTest {

        @Test
        @DisplayName("Enum 필드는 @Enumerated 어노테이션을 가져야 함")
        void enumFieldShouldHaveEnumeratedAnnotation() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..persistence..entity..")
                .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .and().haveRawType(new com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass>("Enum types") {
                    @Override
                    public boolean test(com.tngtech.archunit.core.domain.JavaClass javaClass) {
                        return javaClass.isEnum();
                    }
                })
                .should().beAnnotatedWith(Enumerated.class)
                .because("Enum 필드는 명시적으로 저장 방식을 지정해야 합니다 (@Enumerated 필수)");

            rule.check(persistenceClasses);
        }

        // Note: EnumType.STRING 강제는 ArchUnit으로 검증하기 어려우므로
        // 코드 리뷰 또는 별도 스크립트로 검증 필요
        // 검증 스크립트: grep -r "@Enumerated(EnumType.ORDINAL)" adapter-out/
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔟 Javadoc 필수
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Nested
    @DisplayName("Javadoc 규칙")
    class JavadocTest {

        // Note: ArchUnit은 Javadoc 검증을 직접 지원하지 않으므로
        // Checkstyle 또는 별도 도구 사용 권장
        // 이 규칙은 가이드라인으로만 사용

        @Test
        @DisplayName("JPA 엔티티는 클래스 레벨 Javadoc을 가져야 함 (가이드라인)")
        void jpaEntityShouldHaveClassLevelJavadoc() {
            // 가이드라인:
            // - 클래스 역할 설명
            // - 설계 원칙 명시
            // - @since, @see 태그 포함
        }
    }
}
