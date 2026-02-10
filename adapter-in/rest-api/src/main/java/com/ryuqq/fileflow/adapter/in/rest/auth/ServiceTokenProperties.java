package com.ryuqq.fileflow.adapter.in.rest.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Service Token 인증 설정.
 *
 * <p>서버 간 내부 통신에서 사용되는 Service Token 설정을 바인딩합니다.
 *
 * <pre>{@code
 * security:
 *   service-token:
 *     enabled: true
 *     secret: ${SECURITY_SERVICE_TOKEN_SECRET}
 * }</pre>
 */
@ConfigurationProperties(prefix = "security.service-token")
public class ServiceTokenProperties {

    private boolean enabled;
    private String secret = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
