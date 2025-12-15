package com.ryuqq.fileflow.adapter.in.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Service Token 인증 설정 Properties.
 *
 * <p>서버 간 내부 통신에서 사용되는 Service Token 인증을 위한 설정입니다.
 *
 * <p><strong>설정 예시 (rest-api.yml):</strong>
 *
 * <pre>{@code
 * security:
 *   service-token:
 *     enabled: true
 *     secret: ${SECURITY_SERVICE_TOKEN_SECRET:}
 * }</pre>
 *
 * <p><strong>환경변수 설정:</strong>
 *
 * <ul>
 *   <li>SECURITY_SERVICE_TOKEN_SECRET: SSM Parameter Store에서 주입되는 공유 비밀키
 * </ul>
 *
 * <p><strong>사용 방법:</strong>
 *
 * <pre>{@code
 * @Component
 * public class UserContextFilter {
 *     private final ServiceTokenProperties serviceTokenProperties;
 *
 *     public boolean isValidServiceToken(String token) {
 *         return serviceTokenProperties.isEnabled()
 *             && serviceTokenProperties.getSecret() != null
 *             && serviceTokenProperties.getSecret().equals(token);
 *     }
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "security.service-token")
public class ServiceTokenProperties {

    /**
     * Service Token 인증 활성화 여부.
     *
     * <p>기본값: false (명시적으로 활성화 필요)
     */
    private boolean enabled = false;

    /**
     * Service Token 공유 비밀키.
     *
     * <p>SSM Parameter Store에서 환경변수로 주입됩니다. 모든 서버가 동일한 값을 공유해야 합니다.
     */
    private String secret;

    /**
     * Service Token 인증이 활성화되어 있는지 확인합니다.
     *
     * @return 활성화되어 있으면 true
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Service Token 인증 활성화 여부를 설정합니다.
     *
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Service Token 공유 비밀키를 반환합니다.
     *
     * @return 공유 비밀키 (설정되지 않은 경우 null)
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Service Token 공유 비밀키를 설정합니다.
     *
     * @param secret 공유 비밀키
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Service Token 인증이 사용 가능한 상태인지 확인합니다.
     *
     * <p>활성화되어 있고, 비밀키가 설정되어 있어야 합니다.
     *
     * @return 사용 가능하면 true
     */
    public boolean isAvailable() {
        return enabled && secret != null && !secret.isBlank();
    }

    /**
     * 주어진 토큰이 유효한 Service Token인지 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 유효하면 true
     */
    public boolean isValidToken(String token) {
        return isAvailable() && secret.equals(token);
    }
}
