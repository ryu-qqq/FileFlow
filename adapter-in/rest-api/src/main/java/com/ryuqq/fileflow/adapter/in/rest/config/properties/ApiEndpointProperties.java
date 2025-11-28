package com.ryuqq.fileflow.adapter.in.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * API 엔드포인트 경로 설정 Properties
 *
 * <p>REST API 엔드포인트 경로를 application.yml에서 중앙 관리합니다.
 *
 * <p><strong>설정 예시 (rest-api.yml):</strong>
 *
 * <pre>{@code
 * api:
 *   endpoints:
 *     base-v1: /api/v1
 *     upload-session:
 *       base: /upload-sessions
 *       single-init: /single
 *       single-complete: /{sessionId}/single/complete
 * }</pre>
 *
 * <p><strong>사용 방법:</strong>
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping("${api.endpoints.base-v1}${api.endpoints.upload-session.base}")
 * public class UploadSessionController {
 *
 *     @PostMapping("${api.endpoints.upload-session.single-init}")
 *     public ResponseEntity<?> initSingleUpload() { ... }
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints")
public class ApiEndpointProperties {

    /** API v1 베이스 경로 (기본값: /api/v1) */
    private String baseV1 = "/api/v1";

    /** Upload Session 도메인 엔드포인트 설정 */
    private UploadSessionEndpoints uploadSession = new UploadSessionEndpoints();

    /** File Asset 도메인 엔드포인트 설정 */
    private FileAssetEndpoints fileAsset = new FileAssetEndpoints();

    /** External Download 도메인 엔드포인트 설정 */
    private ExternalDownloadEndpoints externalDownload = new ExternalDownloadEndpoints();

    /** Upload Session 도메인 엔드포인트 경로 */
    public static class UploadSessionEndpoints {
        /** Upload Session 기본 경로 (기본값: /upload-sessions) */
        private String base = "/upload-sessions";

        /** 단일 업로드 초기화 경로 (기본값: /single) */
        private String singleInit = "/single";

        /** 단일 업로드 완료 경로 (기본값: /{sessionId}/single/complete) */
        private String singleComplete = "/{sessionId}/single/complete";

        /** Multipart 업로드 초기화 경로 (기본값: /multipart) */
        private String multipartInit = "/multipart";

        /** Multipart 업로드 완료 경로 (기본값: /{sessionId}/multipart/complete) */
        private String multipartComplete = "/{sessionId}/multipart/complete";

        /** Part 업로드 완료 경로 (기본값: /{sessionId}/parts) */
        private String parts = "/{sessionId}/parts";

        /** 세션 취소 경로 (기본값: /{sessionId}/cancel) */
        private String cancel = "/{sessionId}/cancel";

        /** 세션 ID 조회 경로 (기본값: /{sessionId}) */
        private String byId = "/{sessionId}";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getSingleInit() {
            return singleInit;
        }

        public void setSingleInit(String singleInit) {
            this.singleInit = singleInit;
        }

        public String getSingleComplete() {
            return singleComplete;
        }

        public void setSingleComplete(String singleComplete) {
            this.singleComplete = singleComplete;
        }

        public String getMultipartInit() {
            return multipartInit;
        }

        public void setMultipartInit(String multipartInit) {
            this.multipartInit = multipartInit;
        }

        public String getMultipartComplete() {
            return multipartComplete;
        }

        public void setMultipartComplete(String multipartComplete) {
            this.multipartComplete = multipartComplete;
        }

        public String getParts() {
            return parts;
        }

        public void setParts(String parts) {
            this.parts = parts;
        }

        public String getCancel() {
            return cancel;
        }

        public void setCancel(String cancel) {
            this.cancel = cancel;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }
    }

    /** File Asset 도메인 엔드포인트 경로 */
    public static class FileAssetEndpoints {
        /** File Asset 기본 경로 (기본값: /file-assets) */
        private String base = "/file-assets";

        /** ID 조회 경로 (기본값: /{id}) */
        private String byId = "/{id}";

        /** 삭제 경로 (기본값: /{id}/delete) */
        private String delete = "/{id}/delete";

        /** 다운로드 URL 생성 경로 (기본값: /{id}/download-url) */
        private String downloadUrl = "/{id}/download-url";

        /** 일괄 다운로드 URL 생성 경로 (기본값: /batch-download-url) */
        private String batchDownloadUrl = "/batch-download-url";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }

        public String getDelete() {
            return delete;
        }

        public void setDelete(String delete) {
            this.delete = delete;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getBatchDownloadUrl() {
            return batchDownloadUrl;
        }

        public void setBatchDownloadUrl(String batchDownloadUrl) {
            this.batchDownloadUrl = batchDownloadUrl;
        }
    }

    /** External Download 도메인 엔드포인트 경로 */
    public static class ExternalDownloadEndpoints {
        /** External Download 기본 경로 (기본값: /external-downloads) */
        private String base = "/external-downloads";

        /** ID 조회 경로 (기본값: /{id}) */
        private String byId = "/{id}";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }
    }

    public String getBaseV1() {
        return baseV1;
    }

    public void setBaseV1(String baseV1) {
        this.baseV1 = baseV1;
    }

    public UploadSessionEndpoints getUploadSession() {
        return uploadSession;
    }

    public void setUploadSession(UploadSessionEndpoints uploadSession) {
        this.uploadSession = uploadSession;
    }

    public FileAssetEndpoints getFileAsset() {
        return fileAsset;
    }

    public void setFileAsset(FileAssetEndpoints fileAsset) {
        this.fileAsset = fileAsset;
    }

    public ExternalDownloadEndpoints getExternalDownload() {
        return externalDownload;
    }

    public void setExternalDownload(ExternalDownloadEndpoints externalDownload) {
        this.externalDownload = externalDownload;
    }
}
