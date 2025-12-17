package com.ryuqq.fileflow.adapter.in.rest.auth.paths;

import java.util.List;

/**
 * 보안 경로 분류 상수 정의
 *
 * <p>접근 권한별 경로를 정의합니다. SecurityConfig에서 참조하여 권한 설정에 사용됩니다.
 *
 * <p>경로 분류:
 *
 * <ul>
 *   <li>PUBLIC: 인증 불필요 (헬스체크, 에러 페이지)
 *   <li>DOCS: 인증된 사용자면 접근 가능 (API 문서)
 *   <li>AUTHENTICATED: 인증된 사용자 + @PreAuthorize 권한 검사 (파일 API)
 * </ul>
 *
 * <p>권한 처리:
 *
 * <ul>
 *   <li>URL 기반 역할 검사 제거 → @PreAuthorize 어노테이션으로 대체
 *   <li>ResourceAccessChecker SpEL 함수로 리소스 접근 제어
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class SecurityPaths {

    private SecurityPaths() {}

    /**
     * 인증 불필요 경로 (Public)
     *
     * <p>JWT 검증 없이 접근 가능한 경로입니다.
     */
    public static final class Public {

        /** Public 경로 목록 */
        public static final List<String> PATTERNS =
                List.of(
                        // 헬스체크
                        ApiPaths.Actuator.BASE + "/**",
                        ApiPaths.Actuator.HEALTH,

                        // 에러 페이지
                        "/error");

        private Public() {}
    }

    /**
     * API 문서 경로 (Swagger, REST Docs)
     *
     * <p>인증된 사용자면 접근 가능한 API 문서 경로입니다.
     *
     * <p>Gateway prefix(/file) 적용 경로와 직접 접근용 경로 모두 포함합니다.
     */
    public static final class Docs {

        /** API 문서 경로 목록 */
        public static final List<String> PATTERNS =
                List.of(
                        // Gateway prefix 적용 경로
                        ApiPaths.OpenApi.SWAGGER_REDIRECT,
                        ApiPaths.OpenApi.SWAGGER_UI,
                        ApiPaths.OpenApi.SWAGGER_UI_HTML,
                        ApiPaths.OpenApi.DOCS,
                        ApiPaths.OpenApi.DOCS_ALL,
                        ApiPaths.Docs.BASE,
                        ApiPaths.Docs.ALL,
                        // 직접 접근용 경로 (로컬 개발 시)
                        ApiPaths.OpenApi.LOCAL_SWAGGER_UI,
                        ApiPaths.OpenApi.LOCAL_DOCS,
                        ApiPaths.Docs.LOCAL_BASE,
                        ApiPaths.Docs.LOCAL_ALL);

        private Docs() {}
    }

    /**
     * 헤더 상수
     *
     * <p>Gateway에서 전달하는 인증 정보 헤더입니다.
     */
    public static final class Headers {

        /** 사용자 ID 헤더 - Gateway에서 JWT userId 클레임 추출 */
        public static final String USER_ID = "X-User-Id";

        /** 테넌트 ID 헤더 - Gateway에서 JWT tenantId 클레임 추출 */
        public static final String TENANT_ID = "X-Tenant-Id";

        /** 조직 ID 헤더 - Gateway에서 JWT organizationId 클레임 추출 */
        public static final String ORGANIZATION_ID = "X-Organization-Id";

        /** 역할 헤더 - JSON 배열 형식 (예: ["ROLE_ADMIN", "ROLE_USER"]) */
        public static final String ROLES = "X-Roles";

        /** 권한 헤더 - 콤마 구분 형식 (예: file:read,file:write) */
        public static final String PERMISSIONS = "X-Permissions";

        /** 추적 ID 헤더 - 분산 추적용 */
        public static final String TRACE_ID = "X-Trace-Id";

        /**
         * Service Token 헤더 - 서버 간 내부 통신 인증용.
         *
         * <p>이 헤더에 유효한 Service Token이 포함되어 있으면 시스템 내부 호출로 인식하고 SYSTEM 권한을 부여합니다.
         */
        public static final String SERVICE_TOKEN = "X-Service-Token";

        /**
         * Service Name 헤더 - 호출하는 서비스 식별용.
         *
         * <p>Service Token과 함께 사용되어 어떤 서비스에서 호출했는지 식별합니다. 감사 로그 및 추적에 활용됩니다.
         *
         * <p>예시: "setof-server", "partner-admin", "batch-worker"
         */
        public static final String SERVICE_NAME = "X-Service-Name";

        private Headers() {}
    }

    /**
     * 파일 서비스 권한 상수
     *
     * <p>FileFlow 서비스의 리소스 권한입니다.
     */
    public static final class Permissions {

        /** 파일 읽기 권한 */
        public static final String FILE_READ = "file:read";

        /** 파일 쓰기 권한 (업로드, 생성) */
        public static final String FILE_WRITE = "file:write";

        /** 파일 삭제 권한 */
        public static final String FILE_DELETE = "file:delete";

        /** 다운로드 권한 */
        public static final String FILE_DOWNLOAD = "file:download";

        private Permissions() {}
    }
}
