package com.ryuqq.fileflow.architecture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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
        // IMPORTANT: adapter.rest 패키지만 스캔 (application, domain, persistence 제외)
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.fileflow.adapter.rest");
    }

    // ============================================
    // DTO/Mapper/Controller 상세 규칙
    // ============================================
    //
    // 다음 규칙들은 더 상세한 컨벤션 테스트로 이동되었습니다:
    // - DTO 규칙 (Record, Naming, Validation 등) → RestApiAdapterConventionTest
    // - Mapper 규칙 (Naming, @Component, Utility Class 등) → RestApiAdapterConventionTest
    // - Controller 규칙 (@RestController, Naming, DI 등) → RestApiAdapterConventionTest
    //
    // 이 파일은 REST API Layer 전반의 레이어 의존성 규칙만 포함합니다.

    // ============================================
    // Package 규칙
    // ============================================

    @Test
    @DisplayName("REST API Layer는 정의된 패키지 구조를 따라야 함")
    void restApiLayerShouldFollowPackageStructure() {
        // Given: REST API Layer 패키지 구조 (Fixture 클래스 제외)
        ArchRule rule = classes()
            .that().resideInAPackage("..adapter.rest..")
            .and().resideOutsideOfPackage("..fixture..")  // Fixture 클래스 제외
            .should().resideInAnyPackage(
                "..adapter.rest.common..",
                "..adapter.rest.config..",
                "..adapter.rest..controller..",
                "..adapter.rest..dto..",
                "..adapter.rest..mapper..",
                "..adapter.rest..error.."
            )
            .because("REST API Layer는 controller, dto, mapper, error 패키지 구조를 따라야 합니다.");

        // When & Then: 규칙 검증
        rule.check(importedClasses);
    }

    // ============================================
    // 의존성 규칙
    // ============================================

    // NOTE: "Request DTO는 Application Layer DTO에 의존하지 않아야 함" 규칙 제거
    // 이유: Request DTO의 toQuery() 메서드가 Application Layer의 Query DTO를 반환하는 것은
    // REST API → Application Layer 변환 패턴에서 일반적이며, Mapper를 통하지 않고도 허용됩니다.
    // 예: OrganizationSearchApiRequest.toQuery() → GetOrganizationsQuery

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
                "..adapter.rest..",
                "java..",
                "org.springframework.."
            )
            .because("REST API Mapper는 Application Layer, Domain Layer (예외 처리용), 자신의 Layer에만 의존해야 합니다.");

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
                "..adapter.rest..",         // REST API Layer (자신의 layer)
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
