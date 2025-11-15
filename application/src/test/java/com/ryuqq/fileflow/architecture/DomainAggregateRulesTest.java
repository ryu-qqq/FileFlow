package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Domain Aggregate 컨벤션 검증 (ArchUnit)
 * <p>
 * FILE-001-domain-refactoring-plan.md Cycle 19 검증
 * </p>
 */
@DisplayName("Domain Aggregate 아키텍처 규칙 테스트")
class DomainAggregateRulesTest {

    private static JavaClasses domainClasses;

    @BeforeAll
    static void setUp() {
        domainClasses = new ClassFileImporter().importPackages("com.ryuqq.fileflow.domain");
    }

    @Test
    @DisplayName("Aggregate Root 생성자는 private이어야 한다")
    void aggregateConstructorsShouldBePrivate() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(havePrivateConstructors())
                .because("Aggregate Root는 팩토리 메서드를 통해서만 생성되어야 합니다 (forNew, of, reconstitute)")
                .check(domainClasses);
    }

    @Test
    @DisplayName("Aggregate Root는 3종 팩토리 메서드를 가져야 한다 (forNew, of, reconstitute)")
    void aggregatesShouldHaveThreeFactoryMethods() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(haveThreeFactoryMethods())
                .because("Aggregate Root는 forNew (생성), of (비즈니스 생성), reconstitute (재구성) 팩토리를 제공해야 합니다")
                .check(domainClasses);
    }

    @Test
    @DisplayName("Aggregate Root는 ID Value Object를 사용해야 한다")
    void aggregatesShouldUseIdValueObjects() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(haveIdValueObject())
                .because("Aggregate Root는 ID를 VO로 관리해야 합니다 (예: FileId, MessageOutboxId)")
                .check(domainClasses);
    }

    @Test
    @DisplayName("Aggregate Root는 Clock 필드를 가져야 한다")
    void aggregatesShouldHaveClockField() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(haveClockField())
                .because("Aggregate Root는 테스트 가능성을 위해 Clock 의존성을 주입받아야 합니다")
                .check(domainClasses);
    }

    @Test
    @DisplayName("Aggregate Root는 LocalDateTime.now()를 사용하지 않아야 한다")
    void aggregatesShouldNotUseLocalDateTimeNow() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(notUseLocalDateTimeNow())
                .because("시간 생성은 Clock을 통해서만 이루어져야 합니다 (LocalDateTime.now(clock))")
                .check(domainClasses);
    }

    @Test
    @DisplayName("Aggregate Root는 updatedAt 필드를 가져야 한다")
    void aggregatesShouldHaveUpdatedAtField() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(haveUpdatedAtField())
                .because("Aggregate Root는 변경 추적을 위해 updatedAt 필드를 가져야 합니다")
                .check(domainClasses);
    }

    @Test
    @DisplayName("Aggregate Root는 getIdValue() 메서드를 가져야 한다 (Law of Demeter)")
    void aggregatesShouldHaveGetIdValueMethod() {
        classes()
                .that().resideInAPackage("..aggregate..")
                .and().areNotInterfaces()
                .and().areNotEnums()
                .should(haveGetIdValueMethod())
                .because("Law of Demeter를 준수하기 위해 getIdValue() 편의 메서드를 제공해야 합니다")
                .check(domainClasses);
    }

    // ===== Custom ArchCondition Helpers =====

    private static ArchCondition<JavaClass> havePrivateConstructors() {
        return new ArchCondition<>("have private constructors") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasPublicConstructor = javaClass.getConstructors().stream()
                        .anyMatch(constructor -> constructor.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC));

                if (hasPublicConstructor) {
                    String message = String.format(
                            "Class %s has public constructor, but should have private constructors only",
                            javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveThreeFactoryMethods() {
        return new ArchCondition<>("have three factory methods (forNew, of, reconstitute)") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasForNew = javaClass.getMethods().stream()
                        .anyMatch(method -> method.getName().equals("forNew"));
                boolean hasOf = javaClass.getMethods().stream()
                        .anyMatch(method -> method.getName().equals("of"));
                boolean hasReconstitute = javaClass.getMethods().stream()
                        .anyMatch(method -> method.getName().equals("reconstitute"));

                if (!hasForNew || !hasOf || !hasReconstitute) {
                    String message = String.format(
                            "Class %s is missing factory methods - forNew: %s, of: %s, reconstitute: %s",
                            javaClass.getName(), hasForNew, hasOf, hasReconstitute
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveIdValueObject() {
        return new ArchCondition<>("have ID Value Object field") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasIdVo = javaClass.getFields().stream()
                        .anyMatch(field -> field.getRawType().getName().endsWith("Id")
                                && field.getRawType().getPackageName().contains(".vo"));

                if (!hasIdVo) {
                    String message = String.format(
                            "Class %s does not have ID Value Object field (e.g., FileId, MessageOutboxId)",
                            javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveClockField() {
        return new ArchCondition<>("have Clock field") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasClock = javaClass.getFields().stream()
                        .anyMatch(field -> field.getRawType().isEquivalentTo(Clock.class));

                if (!hasClock) {
                    String message = String.format(
                            "Class %s does not have Clock field for testable time generation",
                            javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notUseLocalDateTimeNow() {
        return new ArchCondition<>("not use LocalDateTime.now()") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().forEach(method -> {
                    boolean usesLocalDateTimeNow = method.getCallsFromSelf().stream()
                            .anyMatch(call -> call.getTargetOwner().getName().equals("java.time.LocalDateTime")
                                    && call.getName().equals("now")
                                    && call.getTarget().getRawParameterTypes().isEmpty());

                    if (usesLocalDateTimeNow) {
                        String message = String.format(
                                "Method %s.%s() uses LocalDateTime.now() without Clock, use LocalDateTime.now(clock) instead",
                                javaClass.getName(), method.getName()
                        );
                        events.add(SimpleConditionEvent.violated(javaClass, message));
                    }
                });
            }
        };
    }

    private static ArchCondition<JavaClass> haveUpdatedAtField() {
        return new ArchCondition<>("have updatedAt field") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasUpdatedAt = javaClass.getFields().stream()
                        .anyMatch(field -> field.getName().equals("updatedAt"));

                if (!hasUpdatedAt) {
                    String message = String.format(
                            "Class %s does not have updatedAt field for change tracking",
                            javaClass.getName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveGetIdValueMethod() {
        return new ArchCondition<>("have getIdValue() method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                // 허용되는 메서드 이름 패턴:
                // 1. get{ClassName}IdValue() - 예: getFileIdValue(), getMessageOutboxIdValue()
                // 2. getIdValue() - 간결한 버전
                // 3. get{ShortName}IdValue() - 예: getJobIdValue() (FileProcessingJob의 경우)
                String classNamePattern = "get" + javaClass.getSimpleName() + "IdValue";
                String genericPattern = "getIdValue";
                String shortNamePattern = "getJobIdValue"; // FileProcessingJob → jobId

                boolean hasGetIdValue = javaClass.getMethods().stream()
                        .anyMatch(method -> method.getName().equals(classNamePattern)
                                || method.getName().equals(genericPattern)
                                || (method.getName().endsWith("IdValue") && method.getName().startsWith("get")));

                if (!hasGetIdValue) {
                    String message = String.format(
                            "Class %s does not have getIdValue() method for Law of Demeter (expected one of: %s(), getIdValue(), or get*IdValue())",
                            javaClass.getName(), classNamePattern
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
