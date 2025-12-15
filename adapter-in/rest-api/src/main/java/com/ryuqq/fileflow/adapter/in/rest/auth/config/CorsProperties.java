package com.ryuqq.fileflow.adapter.in.rest.auth.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CORS 설정 Properties
 *
 * <p>application.yml에서 CORS 관련 설정을 로드합니다.
 *
 * <p>설정 예시:
 *
 * <pre>{@code
 * security:
 *   cors:
 *     allowed-origins:
 *       - http://localhost:3000
 *       - https://admin.example.com
 *     allowed-methods:
 *       - GET
 *       - POST
 *       - PUT
 *       - PATCH
 *       - DELETE
 *     allowed-headers:
 *       - "*"
 *     exposed-headers:
 *       - X-Total-Count
 *     allow-credentials: true
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /** 허용할 Origin 목록 */
    private List<String> allowedOrigins = new ArrayList<>();

    /** 허용할 HTTP 메서드 목록 */
    private List<String> allowedMethods = new ArrayList<>();

    /** 허용할 헤더 목록 */
    private List<String> allowedHeaders = new ArrayList<>();

    /** 노출할 헤더 목록 */
    private List<String> exposedHeaders = new ArrayList<>();

    /** 자격 증명 허용 여부 */
    private boolean allowCredentials = true;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
}
