package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Download API 엔드포인트 설정
 *
 * <p>application.yml의 {@code api.endpoints.download} 설정을 매핑합니다.</p>
 *
 * <p><strong>설정 예시 (application.yml):</strong></p>
 * <pre>{@code
 * api:
 *   endpoints:
 *     download:
 *       base: /downloads
 *       external:
 *         start: /external
 *         status: /external/{downloadId}/status
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints.download")
public class DownloadEndpointProperties {

    private String base = "/downloads";
    private ExternalEndpoints external = new ExternalEndpoints();

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public ExternalEndpoints getExternal() {
        return external;
    }

    public void setExternal(ExternalEndpoints external) {
        this.external = external;
    }

    /**
     * External Download API 엔드포인트
     */
    public static class ExternalEndpoints {
        private String start = "/external";
        private String status = "/external/{downloadId}/status";

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
