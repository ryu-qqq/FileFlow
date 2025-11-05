package com.ryuqq.fileflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Download Bounded Context 아키텍처 규칙 검증
 *
 * <p>Download 바운디드 컨텍스트의 레이어별 아키텍처 규칙을 검증합니다.</p>
 *
 * <p><strong>검증 범위:</strong></p>
 * <ul>
 *   <li>Domain Layer: Download 도메인 예외, CQRS 분리 확인</li>
 *   <li>Application Layer: Port CQRS 분리, UseCase 패턴</li>
 *   <li>Adapter Layer: REST API 컨벤션, ErrorMapper</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("Download Bounded Context 아키텍처 규칙 검증")
class DownloadBoundedContextArchitectureTest {

    private JavaClasses downloadDomainClasses;
    private JavaClasses downloadApplicationClasses;
    private JavaClasses downloadAdapterClasses;

    @BeforeEach
    void setUp() {
        ClassFileImporter importer = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);

        downloadDomainClasses = importer.importPackages("com.ryuqq.fileflow.domain.download");
        downloadApplicationClasses = importer.importPackages("com.ryuqq.fileflow.application.download");
        downloadAdapterClasses = importer.importPackages("com.ryuqq.fileflow.adapter.rest.download");
    }

    @Nested
    @DisplayName("Domain Layer 규칙 검증")
    class DomainLayerRules {

        @Test
        @DisplayName("Download Domain은 DomainException 사용 - 표준 Java 예외 금지")
        void downloadDomainShouldUseDomainException() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain.download..")
                .and().haveSimpleNameEndingWith("Exception")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "java.lang.IllegalStateException",
                    "java.lang.IllegalArgumentException"
                )
                .because("Download Domain은 DomainException 계층을 사용해야 하며, 표준 Java 예외를 직접 사용하지 않아야 합니다.");

            rule.check(downloadDomainClasses);
        }

        @Test
        @DisplayName("Download Domain Exception은 DownloadException을 상속해야 함")
        void downloadExceptionsShouldExtendDomainException() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain.download.exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .should().beAssignableTo(com.ryuqq.fileflow.domain.common.DomainException.class)
                .because("Download Domain 예외는 DomainException을 상속해야 합니다.");

            rule.check(downloadDomainClasses);
        }

        @Test
        @DisplayName("Download Domain은 Lombok 사용 금지")
        void downloadDomainShouldNotUseLombok() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain.download..")
                .should().dependOnClassesThat().resideInAnyPackage("lombok..")
                .because("Download Domain은 Lombok을 사용하지 않아야 합니다.");

            rule.check(downloadDomainClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer 규칙 검증")
    class ApplicationLayerRules {

        @Test
        @DisplayName("Download Application Port는 CQRS 분리되어야 함 - Command/Query Port 분리")
        void downloadPortsShouldBeSeparatedByCQRS() {
            ArchRule commandPortRule = classes()
                .that().resideInAPackage("..application.download.port.out..")
                .and().haveSimpleNameContaining("Command")
                .should().haveSimpleNameEndingWith("Port")
                .because("Command Port는 *CommandPort 네이밍 규칙을 따라야 합니다.");

            ArchRule queryPortRule = classes()
                .that().resideInAPackage("..application.download.port.out..")
                .and().haveSimpleNameContaining("Query")
                .should().haveSimpleNameEndingWith("Port")
                .because("Query Port는 *QueryPort 네이밍 규칙을 따라야 합니다.");

            commandPortRule.check(downloadApplicationClasses);
            queryPortRule.check(downloadApplicationClasses);
        }

        @Test
        @DisplayName("Download Command Port는 쓰기 메서드만 포함해야 함")
        void commandPortsShouldOnlyHaveWriteMethods() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..application.download.port.out..")
                .and().haveSimpleNameContaining("Command")
                .should().haveSimpleNameContaining("find")
                .orShould().haveSimpleNameContaining("get")
                .orShould().haveSimpleNameContaining("query")
                .because("Command Port는 쓰기 메서드(save, delete)만 포함해야 하며, 조회 메서드는 QueryPort에 있어야 합니다.");

            rule.check(downloadApplicationClasses);
        }

        @Test
        @DisplayName("Download Query Port는 읽기 메서드만 포함해야 함")
        void queryPortsShouldOnlyHaveReadMethods() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..application.download.port.out..")
                .and().haveSimpleNameContaining("Query")
                .should().haveSimpleNameContaining("save")
                .orShould().haveSimpleNameContaining("delete")
                .orShould().haveSimpleNameContaining("update")
                .because("Query Port는 읽기 메서드(find, get)만 포함해야 하며, 쓰기 메서드는 CommandPort에 있어야 합니다.");

            rule.check(downloadApplicationClasses);
        }

        @Test
        @DisplayName("Download UseCase는 @Service 어노테이션 사용")
        void downloadUseCasesShouldBeAnnotatedWithService() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application.download.service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith("org.springframework.stereotype.Service")
                .because("Download UseCase는 @Service 어노테이션으로 Spring Bean으로 등록되어야 합니다.");

            rule.check(downloadApplicationClasses);
        }
    }

    @Nested
    @DisplayName("Adapter Layer 규칙 검증")
    class AdapterLayerRules {

        @Test
        @DisplayName("Download REST API는 ErrorMapper 구현")
        void downloadRestApiShouldHaveErrorMapper() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter.rest.download.error..")
                .and().haveSimpleNameContaining("ErrorMapper")
                .should().beAnnotatedWith("org.springframework.stereotype.Component")
                .andShould().implement(com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper.class)
                .because("Download REST API는 DownloadErrorMapper를 구현하여 Domain 예외를 HTTP 응답으로 변환해야 합니다.");

            rule.check(downloadAdapterClasses);
        }

        @Test
        @DisplayName("Download Controller는 @RestController 사용")
        void downloadControllerShouldBeAnnotatedWithRestController() {
            ArchRule rule = classes()
                .that().resideInAPackage("..adapter.rest.download.controller..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .because("Download Controller는 @RestController 어노테이션을 사용해야 합니다.");

            rule.check(downloadAdapterClasses);
        }
    }
}

