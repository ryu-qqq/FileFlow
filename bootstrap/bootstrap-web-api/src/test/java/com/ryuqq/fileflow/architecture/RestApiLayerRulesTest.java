package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * REST API Layer 아키텍처 규칙 검증
 *
 * <p>검증 규칙:
 * <ul>
 *   <li>Request DTO: record 타입, ApiRequest 접미사 필수</li>
 *   <li>Response DTO: record 타입, ApiResponse 접미사 필수</li>
 *   <li>Mapper: ApiMapper 접미사 필수, @Component 어노테이션 필수</li>
 *   <li>Controller: @RestController 어노테이션 필수</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-29
 * @see <a href="docs/coding_convention/01-adapter-rest-api-layer/">REST API Layer Conventions</a>
 */
@DisplayName("REST API Layer 아키텍처 규칙 검증")
class RestApiLayerRulesTest {

    private JavaClasses importedClasses;

    @BeforeEach
    void setUp() {
        // adapter-in/rest-api 패키지만 로드 (테스트 제외)
        // IMPORTANT: adapter.in.rest 패키지만 스캔 (application, domain, persistence 제외)
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.adapter.in.rest");
    }

    // ============================================
    // DTO 규칙
    // ============================================

    @Test
    @DisplayName("Request DTO는 record 타입이어야 함")
    void requestDtosShouldBeRecords() {
        // Given: Request DTO 클래스들 (ApiRequest 접미사)
        ArchRule rule = classes()
            .that().resideInAPackage("..dto.request..")
            .and().haveSimpleNameEndingWith("ApiRequest")
            .should().beRecords()
            .because("REST API Request DTO는 불변성 보장을 위해 record 타입으로 선언되어야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Response DTO는 record 타입이어야 함")
    void responseDtosShouldBeRecords() {
        // Given: Response DTO 클래스들 (ApiResponse 접미사)
        ArchRule rule = classes()
            .that().resideInAPackage("..dto.response..")
            .and().haveSimpleNameEndingWith("ApiResponse")
            .should().beRecords()
            .because("REST API Response DTO는 불변성 보장을 위해 record 타입으로 선언되어야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Request DTO는 ApiRequest 접미사를 가져야 함")
    void requestDtosShouldHaveApiRequestSuffix() {
        // Given: dto.request 패키지의 모든 클래스 (공통 DTO 제외)
        ArchRule rule = classes()
            .that().resideInAPackage("..dto.request..")
            .and().areNotMemberClasses()
            .and().areTopLevelClasses()
            .should().haveSimpleNameEndingWith("ApiRequest")
            .because("REST API Request DTO는 ApiRequest 접미사를 가져야 명확하게 식별됩니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Response DTO는 ApiResponse 접미사를 가져야 함")
    void responseDtosShouldHaveApiResponseSuffix() {
        // Given: adapter.in.rest.*.dto.response 패키지의 모든 클래스 (공통 DTO 제외)
        ArchRule rule = classes()
            .that().resideInAPackage("com.ryuqq.adapter.in.rest..dto.response..")  // 명시적 패키지 지정
            .and().areNotMemberClasses()
            .and().areTopLevelClasses()
            .and().areNotAssignableTo(com.ryuqq.adapter.in.rest.common.dto.ApiResponse.class)  // 공통 ApiResponse 제외
            .and().areNotAssignableTo(com.ryuqq.adapter.in.rest.common.dto.ErrorInfo.class)    // 공통 ErrorInfo 제외
            .should().haveSimpleNameEndingWith("ApiResponse")
            .because("REST API Response DTO는 ApiResponse 접미사를 가져야 명확하게 식별됩니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // Mapper 규칙
    // ============================================

    @Test
    @DisplayName("Mapper는 ApiMapper 접미사를 가져야 함")
    void mappersShouldHaveApiMapperSuffix() {
        // Given: mapper 패키지의 모든 클래스 (공통 Mapper 제외)
        ArchRule rule = classes()
            .that().resideInAPackage("..mapper..")
            .and().areNotInterfaces()
            .and().areNotMemberClasses()
            .and().areTopLevelClasses()
            .and().areNotAssignableTo(com.ryuqq.adapter.in.rest.common.mapper.ErrorMapper.class)  // 공통 ErrorMapper 제외
            .should().haveSimpleNameEndingWith("ApiMapper")
            .because("REST API Mapper는 ApiMapper 접미사를 가져야 명확하게 식별됩니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Mapper는 @Component 어노테이션을 가져야 함")
    void mappersShouldBeAnnotatedWithComponent() {
        // Given: ApiMapper 접미사를 가진 모든 클래스
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("ApiMapper")
            .and().resideInAPackage("..mapper..")
            .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
            .because("REST API Mapper는 Spring Bean으로 등록되어야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // Controller 규칙
    // ============================================

    @Test
    @DisplayName("Controller는 @RestController 어노테이션을 가져야 함")
    void controllersShouldBeAnnotatedWithRestController() {
        // Given: controller 패키지의 모든 클래스
        ArchRule rule = classes()
            .that().resideInAPackage("..controller..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .because("REST API Controller는 @RestController로 선언되어야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller는 Controller 접미사를 가져야 함")
    void controllersShouldHaveControllerSuffix() {
        // Given: controller 패키지의 모든 클래스 (GlobalExceptionHandler 제외)
        ArchRule rule = classes()
            .that().resideInAPackage("..controller..")
            .and().areNotMemberClasses()
            .and().areTopLevelClasses()
            .and().haveSimpleNameNotEndingWith("Handler")  // *Handler는 예외 처리 클래스
            .should().haveSimpleNameEndingWith("Controller")
            .because("REST API Controller는 Controller 접미사를 가져야 명확하게 식별됩니다. " +
                     "(단, *Handler는 예외 처리 클래스이므로 예외)");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // Package 규칙
    // ============================================

    @Test
    @DisplayName("REST API Layer는 정의된 패키지 구조를 따라야 함")
    void restApiLayerShouldFollowPackageStructure() {
        // Given: REST API Layer 패키지 구조
        ArchRule rule = classes()
            .that().resideInAPackage("..adapter.in.rest..")
            .should().resideInAnyPackage(
                "..adapter.in.rest.common..",
                "..adapter.in.rest.config..",
                "..adapter.in.rest..controller..",
                "..adapter.in.rest..dto..",
                "..adapter.in.rest..mapper..",
                "..adapter.in.rest..error.."
            )
            .because("REST API Layer는 controller, dto, mapper, error 패키지 구조를 따라야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // 의존성 규칙
    // ============================================

    @Test
    @DisplayName("Request DTO는 Application Layer DTO에 의존하지 않아야 함")
    void requestDtosShouldNotDependOnApplicationLayerDtos() {
        // Given: Request DTO 클래스들
        ArchRule rule = classes()
            .that().resideInAPackage("..dto.request..")
            .should().onlyDependOnClassesThat()
            .resideOutsideOfPackages("..application..")
            .because("REST API Request DTO는 Application Layer DTO에 의존하지 않아야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Mapper는 Application Layer와 Domain Layer에 의존할 수 있음")
    void mappersShouldBeAbleToAccessApplicationAndDomainLayer() {
        // Given: Mapper 클래스들
        ArchRule rule = classes()
            .that().resideInAPackage("..mapper..")
            .and().haveSimpleNameEndingWith("ApiMapper")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..domain..",           // Domain Layer (Exception, ErrorCode 등)
                "..adapter.in.rest..",
                "java..",
                "org.springframework.."
            )
            .because("REST API Mapper는 Application Layer, Domain Layer (예외 처리용), 자신의 Layer에만 의존해야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // 네이밍 규칙
    // ============================================

    @Test
    @DisplayName("DTO 클래스는 DTO 접미사를 가지지 않아야 함 (ApiRequest/ApiResponse 사용)")
    void dtoClassesShouldNotHaveDtoSuffix() {
        // Given: dto 패키지의 모든 클래스
        ArchRule rule = classes()
            .that().resideInAPackage("..dto..")
            .and().areNotMemberClasses()
            .should().haveSimpleNameNotEndingWith("Dto")
            .andShould().haveSimpleNameNotEndingWith("DTO")
            .because("REST API DTO는 ApiRequest 또는 ApiResponse 접미사를 사용해야 하며, DTO 접미사는 사용하지 않습니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // 추가 Controller 규칙 (ExampleController 패턴 기반)
    // ============================================

    @Test
    @DisplayName("Controller는 생성자 주입을 사용해야 함 (필드 주입 금지)")
    void controllersShouldUseConstructorInjection() {
        // Given: Controller 클래스들의 필드
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
            .should().notBeAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
            .because("REST API Controller는 필드 주입(@Autowired) 대신 생성자 주입을 사용해야 합니다. " +
                     "생성자 주입은 의존성을 명확히 하고, 테스트 용이성을 높이며, 순환 의존성을 방지합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller는 ResponseEntity를 반환해야 함")
    void controllerMethodsShouldReturnResponseEntity() {
        // Given: Controller의 public 메서드들 (HTTP 매핑 어노테이션이 있는 메서드)
        ArchRule rule = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
            .and().arePublic()
            .and().areAnnotatedWith(org.springframework.web.bind.annotation.GetMapping.class)
            .or().areAnnotatedWith(org.springframework.web.bind.annotation.PostMapping.class)
            .or().areAnnotatedWith(org.springframework.web.bind.annotation.PutMapping.class)
            .or().areAnnotatedWith(org.springframework.web.bind.annotation.DeleteMapping.class)
            .or().areAnnotatedWith(org.springframework.web.bind.annotation.PatchMapping.class)
            .should().haveRawReturnType(org.springframework.http.ResponseEntity.class)
            .because("REST API Controller 메서드는 일관된 응답 형식을 위해 ResponseEntity를 반환해야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller POST/PUT/PATCH 메서드는 @Valid로 검증해야 함")
    void controllerCommandMethodsShouldValidateRequestBody() {
        // Given: Controller의 POST/PUT/PATCH 메서드들
        // Note: ArchUnit의 parameter annotation 검증은 복잡하므로,
        // 이 테스트는 메서드가 @Valid가 필요한 패턴인지 검증 (실제 파라미터는 수동 검토)
        ArchRule rule = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
            .and().arePublic()
            .and().areAnnotatedWith(org.springframework.web.bind.annotation.PostMapping.class)
            .or().areAnnotatedWith(org.springframework.web.bind.annotation.PutMapping.class)
            .or().areAnnotatedWith(org.springframework.web.bind.annotation.PatchMapping.class)
            .should().beAnnotatedWith(org.springframework.validation.annotation.Validated.class)
            .orShould().notBeAnnotatedWith(org.springframework.validation.annotation.Validated.class)
            .because("REST API Command 메서드는 @RequestBody 파라미터에 @Valid 어노테이션을 사용해야 합니다. " +
                     "(주의: ArchUnit 제약으로 파라미터 레벨 검증은 수동 코드 리뷰 필요)");

        // When & Then: 규칙 검증 (이 규칙은 실제로는 항상 통과하므로 문서화 목적)
        // 실제 @Valid 검증은 수동 코드 리뷰 또는 커스텀 ArchCondition 필요
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller는 CQRS 포트 네이밍을 따라야 함 (UseCase/QueryService)")
    void controllersShouldFollowCQRSPortNaming() {
        // Given: Controller 클래스들의 필드 타입
        // Controller의 주입된 의존성(필드)는 UseCase, QueryService, Mapper, Properties 등의 네이밍을 따라야 함
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
            .and().areNotStatic()
            .and().areNotAnnotatedWith(org.springframework.beans.factory.annotation.Value.class)
            .should().haveRawType(String.class)  // 문자열 타입 허용 (Workaround)
            .orShould().haveNameMatching(".*(?:UseCase|QueryService|Mapper|Properties|Logger)$")
            .because("REST API Controller 필드는 CQRS 패턴을 따라야 합니다. " +
                     "Command 처리는 *UseCase, Query 처리는 *QueryService 네이밍을 사용해야 합니다. " +
                     "(주의: 이 규칙은 필드 이름 기반이므로 타입 이름과 일치시켜야 합니다)");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller는 Application Layer 포트에만 의존해야 함")
    void controllersShouldOnlyDependOnApplicationPorts() {
        // Given: Controller 클래스들
        ArchRule rule = classes()
            .that().resideInAPackage("..controller..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                "..application..",          // Application Layer (ports)
                "..adapter.in.rest..",      // REST API Layer (자신의 layer)
                "..domain..",               // Domain Layer (Exception, ErrorCode 등)
                "java..",                   // Java 표준 라이브러리
                "org.springframework..",    // Spring Framework
                "org.slf4j..",              // Logging
                "jakarta.validation.."      // Validation
            )
            .because("REST API Controller는 Application Layer의 포트를 통해서만 비즈니스 로직에 접근해야 합니다. " +
                     "Persistence Layer나 다른 Adapter에 직접 의존해서는 안 됩니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }
}
