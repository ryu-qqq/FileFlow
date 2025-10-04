package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;

/**
 * Persistence Adapter Architecture Tests
 *
 * Enforces strict JPA Entity rules for Hexagonal Architecture:
 * - NO JPA relationship annotations (OneToMany, ManyToOne, etc.)
 * - Entities use foreign key fields (Long) only
 * - NO setter methods in Entity classes
 * - NO public constructors in Entity classes
 * - NO complex business logic in entities
 * - NO Transactional in persistence adapters
 * - Mapper classes must exist for each entity
 *
 * @author Architecture Team (arch-team@company.com)
 * @since 2024-01-01
 */
@Disabled("TODO: Task 1.3에서 JPA 영속성 어댑터 구현 후 활성화")
@DisplayName("💾 Persistence Adapter Architecture Enforcement")
class PersistenceArchitectureTest {

    // TODO: Task 1.3에서 JPA 영속성 어댑터 구현 후 활성화
    // 컴파일 오류 방지를 위해 전체 주석 처리
    /*
    private static JavaClasses persistenceClasses;
    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        persistenceClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.adapter.out.persistence");

        allClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow");
    }

    // ========================================
    // JPA Relationship Prohibition (CRITICAL)
    // ========================================

    @Nested
    @DisplayName("🚫 JPA Relationship Annotation Prohibition (CRITICAL)")
    class JpaRelationshipProhibitionTests {

        @Test
        @DisplayName("Entities MUST NOT use JPA relationship annotations")
        void noJpaRelationshipAnnotations() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter..persistence..")
                .should().beAnnotatedWith("jakarta.persistence.OneToMany")
                .orShould().beAnnotatedWith("jakarta.persistence.ManyToOne")
                .orShould().beAnnotatedWith("jakarta.persistence.OneToOne")
                .orShould().beAnnotatedWith("jakarta.persistence.ManyToMany")
                .because("JPA relationships are STRICTLY PROHIBITED - use foreign key Long fields instead");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Entities SHOULD use foreign key Long fields")
        void entitiesShouldUseForeignKeyFields() {
            ArchRule rule = fields()
                .that().areDeclaredInClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
                .and().haveNameMatching(".*Id$")
                .and().areNotStatic()
                .should().haveRawType(Long.class)
                .orShould().haveRawType(long.class)
                .because("Foreign key relationships should be represented as Long fields (e.g., userId, orderId)");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Entity Immutability Rules
    // ========================================

    @Nested
    @DisplayName("🔒 Entity Immutability Enforcement")
    class EntityImmutabilityTests {

        @Test
        @DisplayName("Entities MUST NOT have setter methods")
        void noSetterMethodsInEntities() {
            ArchRule rule = noMethods()
                .that().areDeclaredInClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
                .and().haveNameMatching("set[A-Z].*")
                .and().arePublic()
                .should().beDeclared()
                .because("JPA entities should be immutable - no public setters allowed");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Entities MUST NOT have public constructors")
        void noPublicConstructorsInEntities() {
            ArchRule rule = constructors()
                .that().areDeclaredInClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
                .should(new ArchCondition<>("not be public (except default protected constructor)") {
                    @Override
                    public void check(com.tngtech.archunit.core.domain.JavaConstructor constructor, ConditionEvents events) {
                        boolean isPublic = constructor.getModifiers().contains(JavaModifier.PUBLIC);
                        boolean hasParameters = !constructor.getParameters().isEmpty();

                        if (isPublic && hasParameters) {
                            String message = String.format(
                                "Entity constructor %s is public with parameters - use private constructors and static factory methods",
                                constructor.getFullName()
                            );
                            events.add(SimpleConditionEvent.violated(constructor, message));
                        }
                    }
                })
                .because("Entities should have protected no-arg constructor for JPA and private constructors with static factories");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Business Logic Rules
    // ========================================

    @Nested
    @DisplayName("🏢 Business Logic Prohibition")
    class BusinessLogicTests {

        @Test
        @DisplayName("Entities SHOULD NOT have complex business logic")
        void noBusinessLogicInEntities() {
            ArchRule rule = methods()
                .that().areDeclaredInClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
                .and().arePublic()
                .and().doNotHaveName("equals")
                .and().doNotHaveName("hashCode")
                .and().doNotHaveName("toString")
                .and().doNotHaveNameMatching("get.*")
                .and().doNotHaveNameMatching("is.*")
                .should().haveNameMatching("create|reconstitute")
                .because("Entities should only have static factory methods, not business logic");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Transaction Management Rules
    // ========================================

    @Nested
    @DisplayName("💾 Transaction Prohibition in Adapters")
    class TransactionProhibitionTests {

        @Test
        @DisplayName("Persistence adapters MUST NOT use Transactional")
        void noTransactionalInAdapters() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter..persistence..")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("Transactional should only be in application layer, not adapters");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Persistence adapter methods MUST NOT be Transactional")
        void noTransactionalMethodsInAdapters() {
            ArchRule rule = noMethods()
                .that().areDeclaredInClassesThat().resideInAPackage("..adapter..persistence..")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("Transaction management is application layer responsibility");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Mapper Rules
    // ========================================

    @Nested
    @DisplayName("🔄 Entity Mapper Enforcement")
    class EntityMapperTests {

        @Test
        @DisplayName("Entity mapper classes SHOULD exist for domain conversion")
        void mapperClassesShouldExist() {
            ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should(new ArchCondition<>("have corresponding mapper class") {
                    @Override
                    public void check(com.tngtech.archunit.core.domain.JavaClass entityClass, ConditionEvents events) {
                        String entityName = entityClass.getSimpleName().replace("Entity", "");
                        String expectedMapperName = entityName + "EntityMapper";

                        boolean mapperExists = persistenceClasses.contain(
                            c -> c.getSimpleName().equals(expectedMapperName)
                        );

                        if (!mapperExists) {
                            String message = String.format(
                                "Entity %s should have a corresponding mapper class %s for domain conversion",
                                entityClass.getSimpleName(),
                                expectedMapperName
                            );
                            events.add(SimpleConditionEvent.violated(entityClass, message));
                        }
                    }
                })
                .because("Each entity should have a mapper for entity <-> domain conversion");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Mapper classes SHOULD end with 'Mapper' or 'EntityMapper'")
        void mappersShouldFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter..persistence..")
                .and().haveSimpleNameContaining("Mapper")
                .should().haveSimpleNameEndingWith("Mapper")
                .orShould().haveSimpleNameEndingWith("EntityMapper")
                .because("Mapper classes should follow naming convention");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Repository Rules
    // ========================================

    @Nested
    @DisplayName("📦 Repository Enforcement")
    class RepositoryTests {

        @Test
        @DisplayName("JpaRepository interfaces SHOULD be package-private")
        void jpaRepositoriesShouldBePackagePrivate() {
            ArchRule rule = classes()
                .that().areAssignableTo("org.springframework.data.jpa.repository.JpaRepository")
                .and().areInterfaces()
                .should().notBePublic()
                .because("JpaRepository interfaces should be package-private, only adapters are public");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Port implementations SHOULD be public")
        void portImplementationsShouldBePublic() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter..persistence..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().bePublic()
                .because("Port implementation adapters should be public for Spring DI");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Naming Convention Rules
    // ========================================

    @Nested
    @DisplayName("📝 Naming Convention Enforcement")
    class NamingConventionTests {

        @Test
        @DisplayName("JPA entities MUST end with 'Entity'")
        void entitiesMustEndWithEntity() {
            ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().haveSimpleNameEndingWith("Entity")
                .because("JPA entities must be clearly distinguished from domain objects with 'Entity' suffix");

            rule.check(persistenceClasses);
        }

        @Test
        @DisplayName("Persistence adapters MUST end with 'Adapter'")
        void persistenceAdaptersMustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter..persistence..")
                .and().areNotInterfaces()
                .and().areNotAnnotatedWith("jakarta.persistence.Entity")
                .and().doNotHaveSimpleName("package-info")
                .and().haveSimpleNameNotContaining("Mapper")
                .and().haveSimpleNameNotContaining("Repository")
                .should().haveSimpleNameEndingWith("Adapter")
                .because("Persistence port implementations should end with 'Adapter'");

            rule.check(persistenceClasses);
        }
    }

    // ========================================
    // Lombok Prohibition
    // ========================================

    @Nested
    @DisplayName("🚫 Lombok Prohibition")
    class LombokProhibitionTests {

        @Test
        @DisplayName("Persistence adapters MUST NOT use Lombok")
        void noLombokInPersistence() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter..persistence..")
                .should().dependOnClassesThat().resideInPackage("lombok..")
                .because("Lombok is strictly prohibited across entire project");

            rule.check(persistenceClasses);
        }
    }
    */
}
