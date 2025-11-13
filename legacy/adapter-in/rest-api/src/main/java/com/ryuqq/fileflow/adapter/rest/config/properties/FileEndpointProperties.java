package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * File API 엔드포인트 설정
 *
 * <p>application.yml의 {@code api.endpoints.file} 설정을 매핑합니다.</p>
 *
 * <p><strong>설정 예시 (application.yml):</strong></p>
 * <pre>{@code
 * api:
 *   endpoints:
 *     file:
 *       base: /files
 *       by-id: /{fileId}
 *       download-url: /{fileId}/download-url
 * }</pre>
 *
 * <p><strong>사용 예시 (Controller):</strong></p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("${api.endpoints.base-v1}${api.endpoints.file.base}")
 * public class FileController {
 *     // GET /api/v1/files/{fileId}
 *     // POST /api/v1/files/{fileId}/download-url
 *     // DELETE /api/v1/files/{fileId}
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints.file")
public class FileEndpointProperties {

    private String base = "/files";
    private String byId = "/{fileId}";
    private String downloadUrl = "/{fileId}/download-url";

    /**
     * Base Path Getter
     *
     * @return Base Path (예: /files)
     */
    public String getBase() {
        return base;
    }

    /**
     * Base Path Setter
     *
     * @param base Base Path
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * By ID Path Getter
     *
     * @return By ID Path (예: /{fileId})
     */
    public String getById() {
        return byId;
    }

    /**
     * By ID Path Setter
     *
     * @param byId By ID Path
     */
    public void setById(String byId) {
        this.byId = byId;
    }

    /**
     * Download URL Path Getter
     *
     * @return Download URL Path (예: /{fileId}/download-url)
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Download URL Path Setter
     *
     * @param downloadUrl Download URL Path
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
