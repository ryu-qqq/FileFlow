package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Settings 바운디드 컨텍스트 엔드포인트 설정 Properties
 *
 * <p>Settings (EAV 설정 시스템) 도메인의 REST API 엔드포인트 경로를 관리합니다.</p>
 *
 * <p><strong>설정 예시 (application.yml):</strong></p>
 * <pre>{@code
 * api:
 *   endpoints:
 *     settings:
 *       base: /settings
 * }</pre>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("${api.endpoints.base-v1}${api.endpoints.settings.base}")
 * public class SettingsController { ... }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints.settings")
public class SettingsEndpointProperties {

    /**
     * Settings 기본 경로 (기본값: /settings)
     */
    private String base = "/settings";

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }
}
