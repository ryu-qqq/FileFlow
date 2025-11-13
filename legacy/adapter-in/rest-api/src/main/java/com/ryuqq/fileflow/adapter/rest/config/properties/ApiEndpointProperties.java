package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * API 공통 엔드포인트 설정 Properties
 *
 * <p>REST API의 공통 베이스 경로를 관리합니다.</p>
 * <p>바운디드 컨텍스트별 상세 엔드포인트는 각각의 Properties 클래스에서 관리합니다.</p>
 *
 * <p><strong>바운디드 컨텍스트별 Properties:</strong></p>
 * <ul>
 *   <li>{@link IamEndpointProperties} - IAM 도메인 (Organization, Tenant, Permission, UserContext)</li>
 *   <li>{@link SettingsEndpointProperties} - Settings 도메인 (EAV 설정 시스템)</li>
 * </ul>
 *
 * <p><strong>설정 예시 (application.yml):</strong></p>
 * <pre>{@code
 * api:
 *   endpoints:
 *     base-v1: /api/v1
 * }</pre>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.organization.base}")
 * public class OrganizationController { ... }
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints")
public class ApiEndpointProperties {

    /**
     * API v1 베이스 경로 (기본값: /api/v1)
     */
    private String baseV1 = "/api/v1";

    public String getBaseV1() {
        return baseV1;
    }

    public void setBaseV1(String baseV1) {
        this.baseV1 = baseV1;
    }
}
