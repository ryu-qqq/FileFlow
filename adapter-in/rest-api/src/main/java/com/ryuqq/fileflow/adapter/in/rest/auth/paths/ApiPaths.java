package com.ryuqq.fileflow.adapter.in.rest.auth.paths;

/**
 * API 경로 상수 정의
 *
 * <p>모든 REST API 엔드포인트 경로를 중앙 집중 관리합니다. Controller에서 @RequestMapping에 사용됩니다.
 *
 * <p>경로 구조:
 *
 * <ul>
 *   <li>/api/v1/file/* - 모든 FileFlow API (Gateway 라우팅용)
 *   <li>Upload Session: 파일 업로드 세션 관리
 *   <li>File Asset: 파일 자산 관리
 *   <li>External Download: 외부 URL 다운로드
 * </ul>
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping(ApiPaths.UploadSession.BASE)
 * public class UploadSessionController { ... }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@SuppressWarnings("PMD.DataClass")
public final class ApiPaths {

    /** API 버전 */
    public static final String API_VERSION = "/api/v1";

    /** File 서비스 기본 경로 - Gateway 라우팅용 */
    public static final String FILE_SERVICE_BASE = API_VERSION + "/file";

    private ApiPaths() {}

    // ========================================
    // Upload Session 도메인 경로
    // ========================================

    /**
     * Upload Session API 경로
     *
     * <p>파일 업로드 세션 관리 API입니다.
     */
    public static final class UploadSession {
        public static final String BASE = FILE_SERVICE_BASE + "/upload-sessions";
        public static final String SINGLE_INIT = "/single";
        public static final String SINGLE_COMPLETE = "/{sessionId}/single/complete";
        public static final String MULTIPART_INIT = "/multipart";
        public static final String MULTIPART_COMPLETE = "/{sessionId}/multipart/complete";
        public static final String PARTS = "/{sessionId}/parts";
        public static final String CANCEL = "/{sessionId}/cancel";
        public static final String BY_ID = "/{sessionId}";

        private UploadSession() {}
    }

    // ========================================
    // File Asset 도메인 경로
    // ========================================

    /**
     * File Asset API 경로
     *
     * <p>파일 자산 관리 API입니다.
     */
    public static final class FileAsset {
        public static final String BASE = FILE_SERVICE_BASE + "/file-assets";
        public static final String BY_ID = "/{id}";
        public static final String DELETE = "/{id}/delete";
        public static final String DOWNLOAD_URL = "/{id}/download-url";
        public static final String BATCH_DOWNLOAD_URL = "/batch-download-url";

        private FileAsset() {}
    }

    // ========================================
    // External Download 도메인 경로
    // ========================================

    /**
     * External Download API 경로
     *
     * <p>외부 URL 다운로드 요청 API입니다.
     */
    public static final class ExternalDownload {
        public static final String BASE = FILE_SERVICE_BASE + "/external-downloads";
        public static final String BY_ID = "/{id}";

        private ExternalDownload() {}
    }

    // ========================================
    // 공통/인프라 경로
    // ========================================

    /** Actuator 경로 (헬스체크, 메트릭 등) */
    public static final class Actuator {
        public static final String BASE = "/actuator";
        public static final String HEALTH = BASE + "/health";
        public static final String INFO = BASE + "/info";

        private Actuator() {}
    }

    /** API 문서 경로 (Spring REST Docs) */
    public static final class Docs {
        public static final String BASE = "/docs";
        public static final String ALL = BASE + "/**";

        private Docs() {}
    }

    /** OpenAPI/Swagger 경로 */
    public static final class OpenApi {
        public static final String SWAGGER_UI = "/swagger-ui/**";
        public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
        public static final String SWAGGER_REDIRECT = "/swagger-ui/index.html";
        public static final String DOCS = "/v3/api-docs/**";

        private OpenApi() {}
    }
}
