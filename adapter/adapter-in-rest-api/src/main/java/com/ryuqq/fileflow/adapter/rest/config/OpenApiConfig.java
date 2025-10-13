package com.ryuqq.fileflow.adapter.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration
 *
 * SpringDoc OpenAPI 설정을 제공합니다.
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * 제약사항:
 * - NO Lombok
 * - NO Inner Class
 *
 * @author sangwon-ryu
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String BEARER_FORMAT = "JWT";
    private static final String SECURITY_SCHEME_TYPE = "bearer";

    @Value("${spring.application.name:FileFlow}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${api.contact.email:fileflow@example.com}")
    private String contactEmail;

    @Value("${api.contact.url:https://github.com/your-org/fileflow}")
    private String contactUrl;

    @Value("${api.server.dev.url:}")
    private String devServerUrl;

    @Value("${api.server.prod.url:}")
    private String prodServerUrl;

    /**
     * OpenAPI 설정을 생성합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(createSecurityRequirement());
    }

    /**
     * API 정보를 생성합니다.
     *
     * @return API 정보 객체
     */
    private Info createApiInfo() {
        return new Info()
                .title(applicationName + " API Documentation")
                .description("""
                        FileFlow API 문서입니다.

                        ## Epic 2: 파일 업로드 & 저장

                        ### 주요 기능
                        1. **정책 관리** - 업로드 정책 조회/수정/활성화
                        2. **업로드 세션** - Presigned URL 발급 및 세션 관리
                        3. **업로드 상태** - 업로드 진행률 및 상태 추적
                        4. **멀티파트 업로드** - 대용량 파일 멀티파트 업로드

                        ### 아키텍처
                        - Hexagonal Architecture
                        - NO Lombok
                        - DDD (Domain-Driven Design)
                        """)
                .version(applicationVersion)
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * 연락처 정보를 생성합니다.
     *
     * @return 연락처 정보 객체
     */
    private Contact createContact() {
        return new Contact()
                .name("FileFlow Team")
                .email(contactEmail)
                .url(contactUrl);
    }

    /**
     * 라이선스 정보를 생성합니다.
     *
     * @return 라이선스 정보 객체
     */
    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * 서버 정보 목록을 생성합니다.
     *
     * @return 서버 정보 목록
     */
    private List<Server> createServers() {
        java.util.List<Server> servers = new java.util.ArrayList<>();

        // Local server is always included
        servers.add(new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server"));

        // Add dev server only if URL is configured
        if (devServerUrl != null && !devServerUrl.isBlank()) {
            servers.add(new Server()
                    .url(devServerUrl)
                    .description("Development Server"));
        }

        // Add prod server only if URL is configured
        if (prodServerUrl != null && !prodServerUrl.isBlank()) {
            servers.add(new Server()
                    .url(prodServerUrl)
                    .description("Production Server"));
        }

        return servers;
    }

    /**
     * OpenAPI Components를 생성합니다.
     * Security Scheme을 포함합니다.
     *
     * @return Components 객체
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme());
    }

    /**
     * Security Scheme을 생성합니다.
     * Bearer Token (JWT) 인증 방식을 사용합니다.
     *
     * @return SecurityScheme 객체
     */
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(SECURITY_SCHEME_TYPE)
                .bearerFormat(BEARER_FORMAT)
                .description("""
                        JWT Bearer Token 인증

                        Authorization 헤더에 Bearer Token을 포함하여 요청합니다.

                        예시:
                        ```
                        Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                        ```
                        """);
    }

    /**
     * Security Requirement를 생성합니다.
     *
     * @return SecurityRequirement 객체
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }
}
