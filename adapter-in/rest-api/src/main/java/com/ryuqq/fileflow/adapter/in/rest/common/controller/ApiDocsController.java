package com.ryuqq.fileflow.adapter.in.rest.common.controller;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 문서 접근 및 Swagger UI 서빙 Controller.
 *
 * <p>모든 문서 경로를 {@code /api/v1/market} 하위로 통합하여 API Gateway 라우팅 패턴과 일치시킵니다.
 *
 * <p><strong>접근 경로:</strong>
 *
 * <ul>
 *   <li>{@code /api/v1/market/docs} - REST Docs 메인 페이지
 *   <li>{@code /api/v1/market/swagger} - Swagger UI
 *   <li>{@code /api/v1/market/api-docs} - OpenAPI JSON (springdoc)
 * </ul>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Controller
public class ApiDocsController implements WebMvcConfigurer {

    private static final String DOCS_BASE = "/api/v1/market";
    private static final String SWAGGER_UI_WEBJAR_PATTERN =
            "classpath*:META-INF/resources/webjars/swagger-ui/*/index.html";

    /**
     * REST Docs 메인 페이지로 리다이렉트.
     *
     * @return 리다이렉트 경로
     */
    @GetMapping(DOCS_BASE + "/docs")
    public String redirectToRestDocs() {
        return "redirect:" + DOCS_BASE + "/docs/index.html";
    }

    /**
     * Swagger UI 페이지로 리다이렉트.
     *
     * <p>springdoc의 기본 redirect는 {@code /swagger-ui/index.html}로 이동하여 Gateway 라우팅 범위를 벗어나므로, {@code
     * /api/v1/market/swagger-ui/} 하위로 직접 서빙합니다.
     *
     * @return 리다이렉트 경로
     */
    @GetMapping(DOCS_BASE + "/swagger")
    public String redirectToSwagger() {
        return "redirect:" + DOCS_BASE + "/swagger-ui/index.html?url=" + DOCS_BASE + "/api-docs";
    }

    /**
     * Swagger UI 초기화 스크립트.
     *
     * <p>Webjar에 포함된 기본 {@code swagger-initializer.js}를 오버라이드하여 이 서버의 OpenAPI 스펙 경로를 주입합니다.
     *
     * @return swagger-initializer.js 내용
     */
    @GetMapping(
            value = DOCS_BASE + "/swagger-ui/swagger-initializer.js",
            produces = "application/javascript")
    @ResponseBody
    public String swaggerInitializer() {
        return "window.onload = function() {\n"
                + "  window.ui = SwaggerUIBundle({\n"
                + "    url: \""
                + DOCS_BASE
                + "/api-docs\",\n"
                + "    dom_id: '#swagger-ui',\n"
                + "    deepLinking: true,\n"
                + "    presets: [\n"
                + "      SwaggerUIBundle.presets.apis,\n"
                + "      SwaggerUIStandalonePreset\n"
                + "    ],\n"
                + "    plugins: [\n"
                + "      SwaggerUIBundle.plugins.DownloadUrl\n"
                + "    ],\n"
                + "    layout: \"StandaloneLayout\",\n"
                + "    operationsSorter: \"method\",\n"
                + "    tagsSorter: \"alpha\",\n"
                + "    tryItOutEnabled: true,\n"
                + "    displayRequestDuration: true\n"
                + "  });\n"
                + "};\n";
    }

    /**
     * Swagger UI 정적 리소스 핸들러.
     *
     * <p>swagger-ui webjar의 리소스를 {@code /api/v1/market/swagger-ui/} 경로에서 서빙합니다. webjar 내부의 버전 디렉토리는
     * classpath 스캔으로 자동 감지합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String swaggerUiLocation = findSwaggerUiResourceLocation();
        if (swaggerUiLocation != null) {
            registry.addResourceHandler(DOCS_BASE + "/swagger-ui/**")
                    .addResourceLocations(swaggerUiLocation);
        }
    }

    private String findSwaggerUiResourceLocation() {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(SWAGGER_UI_WEBJAR_PATTERN);
            if (resources.length > 0) {
                String url = resources[0].getURL().toString();
                return url.substring(0, url.lastIndexOf("index.html"));
            }
        } catch (IOException ignored) {
            // Swagger UI webjar not on classpath
        }
        return null;
    }
}
