package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Upload API 엔드포인트 설정
 *
 * <p>application.yml의 {@code api.endpoints.upload} 설정을 매핑합니다.</p>
 *
 * <p><strong>설정 예시 (application.yml):</strong></p>
 * <pre>{@code
 * api:
 *   endpoints:
 *     upload:
 *       base: /uploads
 *       multipart:
 *         init: /multipart/init
 *         part-url: /multipart/{sessionKey}/parts/{partNumber}/url
 *         mark-part: /multipart/{sessionKey}/parts/{partNumber}
 *         complete: /multipart/{sessionKey}/complete
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints.upload")
public class UploadEndpointProperties {

    private String base = "/uploads";
    private MultipartEndpoints multipart = new MultipartEndpoints();

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public MultipartEndpoints getMultipart() {
        return multipart;
    }

    public void setMultipart(MultipartEndpoints multipart) {
        this.multipart = multipart;
    }

    /**
     * Multipart Upload API 엔드포인트
     */
    public static class MultipartEndpoints {
        private String init = "/multipart/init";
        private String partUrl = "/multipart/{sessionKey}/parts/{partNumber}/url";
        private String markPart = "/multipart/{sessionKey}/parts/{partNumber}";
        private String complete = "/multipart/{sessionKey}/complete";

        public String getInit() {
            return init;
        }

        public void setInit(String init) {
            this.init = init;
        }

        public String getPartUrl() {
            return partUrl;
        }

        public void setPartUrl(String partUrl) {
            this.partUrl = partUrl;
        }

        public String getMarkPart() {
            return markPart;
        }

        public void setMarkPart(String markPart) {
            this.markPart = markPart;
        }

        public String getComplete() {
            return complete;
        }

        public void setComplete(String complete) {
            this.complete = complete;
        }
    }
}
