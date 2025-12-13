package com.ryuqq.fileflow.adapter.in.rest.architecture.openapi;

import static com.ryuqq.fileflow.adapter.in.rest.architecture.ArchUnitPackageConstants.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * OpenAPI ArchUnit 검증 테스트 (Zero-Tolerance)
 *
 * <p>Controller와 DTO의 OpenAPI 어노테이션 적용을 자동으로 검증합니다.
 *
 * <p><strong>검증 그룹:</strong>
 *
 * <ul>
 *   <li>1. Controller 규칙 (6개) - @Tag, @Operation, @ApiResponses, @Parameter
 *   <li>2. Request DTO 규칙 (3개) - Command/Query DTO @Schema
 *   <li>3. Response DTO 규칙 (1개) - Response DTO @Schema
 *   <li>4. DTO 필드 규칙 (2개) - 필드 @Schema, description 필수
 *   <li>5. Enum 규칙 (1개) - Enum @Schema
 * </ul>
 *
 * <p><strong>참고 문서:</strong>
 *
 * <ul>
 *   <li>openapi/openapi-guide.md - OpenAPI 어노테이션 가이드
 *   <li>openapi/openapi-archunit.md - ArchUnit 검증 규칙
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("OpenAPI ArchUnit Tests (Zero-Tolerance)")
@Tag("architecture")
@Tag("adapter-rest")
class OpenApiArchTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(ADAPTER_IN_REST);
    }

    // ========================================================================
    // 1. Controller OpenAPI 규칙 (6개)
    // ========================================================================

    @Nested
    @DisplayName("1. Controller OpenAPI 규칙")
    class ControllerOpenApiRules {

        /** 규칙 1-1: Controller 클래스에 @Tag 필수 */
        @Test
        @DisplayName("[필수] Controller 클래스는 @Tag 어노테이션이 있어야 한다")
        void controller_ShouldHaveTagAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage(CONTROLLER_PATTERN)
                            .and()
                            .haveSimpleNameEndingWith("Controller")
                            .and()
                            .areAnnotatedWith(
                                    "org.springframework.web.bind.annotation.RestController")
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.tags.Tag")
                            .allowEmptyShould(true)
                            .because("Controller는 @Tag로 API 그룹을 정의해야 합니다");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 1-2: Controller public 메서드에 @Operation 필수 */
        @Test
        @DisplayName("[필수] Controller HTTP 메서드는 @Operation 어노테이션이 있어야 한다")
        void controller_MethodsShouldHaveOperationAnnotation() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage(CONTROLLER_PATTERN)
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Controller")
                            .and()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(
                                    "org.springframework.web.bind.annotation.RestController") // @RestController만 검증
                            .and()
                            .arePublic()
                            .and(areHttpMappingMethods())
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.Operation")
                            .allowEmptyShould(true)
                            .because("모든 API 메서드는 @Operation으로 설명이 필요합니다 (ApiDocsController 제외)");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 1-3: Controller public 메서드에 @ApiResponses 필수 */
        @Test
        @DisplayName("[필수] Controller HTTP 메서드는 @ApiResponses 어노테이션이 있어야 한다")
        void controller_MethodsShouldHaveApiResponses() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage(CONTROLLER_PATTERN)
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Controller")
                            .and()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(
                                    "org.springframework.web.bind.annotation.RestController") // @RestController만 검증
                            .and()
                            .arePublic()
                            .and(areHttpMappingMethods())
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.responses.ApiResponses")
                            .allowEmptyShould(true)
                            .because(
                                    "모든 API 메서드는 @ApiResponses로 응답 코드를 정의해야 합니다 (ApiDocsController"
                                            + " 제외)");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 1-4: @PathVariable 파라미터에 @Parameter 필수 */
        @Test
        @DisplayName("[필수] @PathVariable 파라미터는 @Parameter 어노테이션이 있어야 한다")
        void pathVariable_ShouldHaveParameterAnnotation() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage(CONTROLLER_PATTERN)
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Controller")
                            .and()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(
                                    "org.springframework.web.bind.annotation.RestController") // @RestController만 검증
                            .and()
                            .arePublic()
                            .should(haveParameterAnnotationOnPathVariable())
                            .allowEmptyShould(true)
                            .because("PathVariable은 @Parameter로 설명이 필요합니다 (ApiDocsController 제외)");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 1-5: @RequestParam 파라미터에 @Parameter 필수 */
        @Test
        @DisplayName("[필수] @RequestParam 파라미터는 @Parameter 어노테이션이 있어야 한다")
        void requestParam_ShouldHaveParameterAnnotation() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage(CONTROLLER_PATTERN)
                            .and()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("Controller")
                            .and()
                            .areDeclaredInClassesThat()
                            .areAnnotatedWith(
                                    "org.springframework.web.bind.annotation.RestController") // @RestController만 검증
                            .and()
                            .arePublic()
                            .should(haveParameterAnnotationOnRequestParam())
                            .allowEmptyShould(true)
                            .because("RequestParam은 @Parameter로 설명이 필요합니다 (ApiDocsController 제외)");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 1-6: @Operation에 summary 필수 */
        @Test
        @DisplayName("[필수] @Operation 어노테이션에 summary가 비어있으면 안 된다")
        void operation_ShouldHaveSummary() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .resideInAPackage(CONTROLLER_PATTERN)
                            .and()
                            .areAnnotatedWith("io.swagger.v3.oas.annotations.Operation")
                            .should(haveNonEmptyOperationSummary())
                            .allowEmptyShould(true)
                            .because("@Operation의 summary는 필수입니다");

            rule.allowEmptyShould(true).check(allClasses);
        }
    }

    // ========================================================================
    // 2. Request DTO OpenAPI 규칙 (3개)
    // ========================================================================

    @Nested
    @DisplayName("2. Request DTO OpenAPI 규칙")
    class RequestDtoOpenApiRules {

        /** 규칙 2-1: Command Request DTO에 @Schema 필수 */
        @Test
        @DisplayName("[필수] Command Request DTO는 @Schema 어노테이션이 있어야 한다")
        void commandRequestDto_ShouldHaveSchema() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage(DTO_COMMAND_PATTERN)
                            .and()
                            .haveSimpleNameEndingWith("ApiRequest")
                            .and()
                            .areRecords()
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                            .allowEmptyShould(true)
                            .because("Request DTO는 @Schema로 문서화해야 합니다");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 2-2: Query Request DTO에 @Schema 필수 */
        @Test
        @DisplayName("[필수] Query Request DTO는 @Schema 어노테이션이 있어야 한다")
        void queryRequestDto_ShouldHaveSchema() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage(DTO_QUERY_PATTERN)
                            .and()
                            .haveSimpleNameEndingWith("ApiRequest")
                            .and()
                            .areRecords()
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                            .allowEmptyShould(true)
                            .because("Request DTO는 @Schema로 문서화해야 합니다");

            rule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 2-3: Query Request DTO (non-ApiRequest suffix)에 @Schema 필수 */
        @Test
        @DisplayName("[필수] Query 조건 DTO는 @Schema 어노테이션이 있어야 한다")
        void queryConditionDto_ShouldHaveSchema() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage(DTO_QUERY_PATTERN)
                            .and()
                            .areRecords()
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                            .allowEmptyShould(true)
                            .because("Query 조건 DTO는 @Schema로 문서화해야 합니다");

            rule.allowEmptyShould(true).check(allClasses);
        }
    }

    // ========================================================================
    // 3. Response DTO OpenAPI 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("3. Response DTO OpenAPI 규칙")
    class ResponseDtoOpenApiRules {

        /** 규칙 3-1: Response DTO에 @Schema 필수 */
        @Test
        @DisplayName("[필수] Response DTO는 @Schema 어노테이션이 있어야 한다")
        void responseDto_ShouldHaveSchema() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage(DTO_RESPONSE_PATTERN)
                            .and()
                            .haveSimpleNameEndingWith("ApiResponse")
                            .and()
                            .areRecords()
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                            .allowEmptyShould(true)
                            .because("Response DTO는 @Schema로 문서화해야 합니다");

            rule.allowEmptyShould(true).check(allClasses);
        }
    }

    // ========================================================================
    // 4. DTO 필드 OpenAPI 규칙 (2개)
    // ========================================================================

    @Nested
    @DisplayName("4. DTO 필드 OpenAPI 규칙")
    class DtoFieldOpenApiRules {

        /** 규칙 4-1: DTO 필드에 @Schema 필수 */
        @Test
        @DisplayName("[필수] Request/Response DTO의 모든 필드는 @Schema가 있어야 한다")
        void dtoFields_ShouldHaveSchema() {
            // Note: or()를 사용하면 조건 체인이 끊어지므로 별도 규칙으로 분리
            // Request DTO 규칙
            ArchRule requestRule =
                    classes()
                            .that()
                            .resideInAnyPackage(DTO_COMMAND_PATTERN, DTO_QUERY_PATTERN)
                            .and()
                            .areRecords()
                            .and()
                            .haveSimpleNameEndingWith("ApiRequest")
                            .should(haveSchemaOnAllRecordComponents())
                            .allowEmptyShould(true)
                            .because("Request DTO의 모든 필드는 @Schema로 문서화해야 합니다");

            // Response DTO 규칙 (common 패키지의 유틸리티 클래스 제외)
            ArchRule responseRule =
                    classes()
                            .that()
                            .resideInAPackage(DTO_RESPONSE_PATTERN)
                            .and()
                            .resideOutsideOfPackage("..common..")
                            .and()
                            .areRecords()
                            .and()
                            .haveSimpleNameEndingWith("ApiResponse")
                            .should(haveSchemaOnAllRecordComponents())
                            .allowEmptyShould(true)
                            .because("Response DTO의 모든 필드는 @Schema로 문서화해야 합니다");

            requestRule.allowEmptyShould(true).check(allClasses);
            responseRule.allowEmptyShould(true).check(allClasses);
        }

        /** 규칙 4-2: @Schema에 description 필수 */
        @Test
        @DisplayName("[필수] @Schema 어노테이션에 description이 비어있으면 안 된다")
        void schema_ShouldHaveDescription() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAnyPackage(
                                    DTO_COMMAND_PATTERN, DTO_QUERY_PATTERN, DTO_RESPONSE_PATTERN)
                            .and()
                            .areRecords()
                            .and()
                            .areAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                            .should(haveNonEmptySchemaDescription())
                            .allowEmptyShould(true)
                            .because("@Schema의 description은 필수입니다");

            rule.allowEmptyShould(true).check(allClasses);
        }
    }

    // ========================================================================
    // 5. Enum OpenAPI 규칙 (1개)
    // ========================================================================

    @Nested
    @DisplayName("5. Enum OpenAPI 규칙")
    class EnumOpenApiRules {

        /** 규칙 5-1: DTO에서 사용하는 Enum에 @Schema 필수 */
        @Test
        @DisplayName("[필수] DTO에서 사용하는 Enum은 @Schema 어노테이션이 있어야 한다")
        void enum_ShouldHaveSchema() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..dto..")
                            .and()
                            .areEnums()
                            .should()
                            .beAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                            .allowEmptyShould(true)
                            .because("Enum은 @Schema로 문서화해야 합니다");

            rule.allowEmptyShould(true).check(allClasses);
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /** HTTP 매핑 메서드 판별 Predicate */
    private static DescribedPredicate<JavaMethod> areHttpMappingMethods() {
        return new DescribedPredicate<>("HTTP 매핑 메서드") {
            @Override
            public boolean test(JavaMethod method) {
                return method.isAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
                        || method.isAnnotatedWith(
                                "org.springframework.web.bind.annotation.PostMapping")
                        || method.isAnnotatedWith(
                                "org.springframework.web.bind.annotation.PutMapping")
                        || method.isAnnotatedWith(
                                "org.springframework.web.bind.annotation.PatchMapping")
                        || method.isAnnotatedWith(
                                "org.springframework.web.bind.annotation.DeleteMapping")
                        || method.isAnnotatedWith(
                                "org.springframework.web.bind.annotation.RequestMapping");
            }
        };
    }

    /**
     * @PathVariable 파라미터에 @Parameter 어노테이션 검증 Condition
     */
    private static ArchCondition<JavaMethod> haveParameterAnnotationOnPathVariable() {
        return new ArchCondition<>("@PathVariable에 @Parameter가 있어야 함") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaParameter param : method.getParameters()) {
                    boolean hasPathVariable =
                            param.isAnnotatedWith(
                                    "org.springframework.web.bind.annotation.PathVariable");
                    boolean hasParameter =
                            param.isAnnotatedWith("io.swagger.v3.oas.annotations.Parameter");

                    if (hasPathVariable && !hasParameter) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        method,
                                        String.format(
                                                "%s의 @PathVariable 파라미터에 @Parameter가 없습니다",
                                                method.getFullName())));
                    }
                }
            }
        };
    }

    /**
     * @RequestParam 파라미터에 @Parameter 어노테이션 검증 Condition
     */
    private static ArchCondition<JavaMethod> haveParameterAnnotationOnRequestParam() {
        return new ArchCondition<>("@RequestParam에 @Parameter가 있어야 함") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaParameter param : method.getParameters()) {
                    boolean hasRequestParam =
                            param.isAnnotatedWith(
                                    "org.springframework.web.bind.annotation.RequestParam");
                    boolean hasParameter =
                            param.isAnnotatedWith("io.swagger.v3.oas.annotations.Parameter");

                    if (hasRequestParam && !hasParameter) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        method,
                                        String.format(
                                                "%s의 @RequestParam 파라미터에 @Parameter가 없습니다",
                                                method.getFullName())));
                    }
                }
            }
        };
    }

    /**
     * @Operation.summary 비어있지 않음 검증 Condition
     */
    private static ArchCondition<JavaMethod> haveNonEmptyOperationSummary() {
        return new ArchCondition<>("@Operation.summary가 비어있지 않아야 함") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                try {
                    var operation =
                            method.getAnnotationOfType(
                                    io.swagger.v3.oas.annotations.Operation.class);
                    if (operation != null && operation.summary().isBlank()) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        method,
                                        String.format(
                                                "%s의 @Operation.summary가 비어있습니다",
                                                method.getFullName())));
                    }
                } catch (Exception e) {
                    // 어노테이션이 없는 경우 무시 (다른 규칙에서 검증)
                }
            }
        };
    }

    /** Record 컴포넌트에 @Schema 어노테이션 검증 Condition */
    private static ArchCondition<JavaClass> haveSchemaOnAllRecordComponents() {
        return new ArchCondition<>("모든 Record 컴포넌트에 @Schema가 있어야 함") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaField field : javaClass.getFields()) {
                    // synthetic 필드 제외
                    if (field.getModifiers().contains(JavaModifier.SYNTHETIC)) {
                        continue;
                    }
                    // static 필드 제외
                    if (field.getModifiers().contains(JavaModifier.STATIC)) {
                        continue;
                    }

                    boolean hasSchema =
                            field.isAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema");
                    if (!hasSchema) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        javaClass,
                                        String.format(
                                                "%s.%s 필드에 @Schema가 없습니다",
                                                javaClass.getSimpleName(), field.getName())));
                    }
                }
            }
        };
    }

    /**
     * @Schema.description 비어있지 않음 검증 Condition
     */
    private static ArchCondition<JavaClass> haveNonEmptySchemaDescription() {
        return new ArchCondition<>("@Schema.description이 비어있지 않아야 함") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                try {
                    var schema =
                            javaClass.getAnnotationOfType(
                                    io.swagger.v3.oas.annotations.media.Schema.class);
                    if (schema != null && schema.description().isBlank()) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        javaClass,
                                        String.format(
                                                "%s의 @Schema.description이 비어있습니다",
                                                javaClass.getSimpleName())));
                    }
                } catch (Exception e) {
                    // 어노테이션이 없는 경우 무시 (다른 규칙에서 검증)
                }
            }
        };
    }
}
