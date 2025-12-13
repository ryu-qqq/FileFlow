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
     */
    public static final class Docs {

        /** API 문서 경로 목록 */
        public static final List<String> PATTERNS =
                List.of(
                        ApiPaths.OpenApi.SWAGGER_REDIRECT,
                        ApiPaths.OpenApi.SWAGGER_UI,
                        ApiPaths.OpenApi.SWAGGER_UI_HTML,
                        ApiPaths.OpenApi.DOCS,
                        ApiPaths.Docs.BASE,
                        ApiPaths.Docs.ALL);

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
