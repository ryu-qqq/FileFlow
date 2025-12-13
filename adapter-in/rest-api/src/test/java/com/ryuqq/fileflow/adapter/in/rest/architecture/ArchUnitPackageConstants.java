package com.ryuqq.fileflow.adapter.in.rest.architecture;

/**
 * REST API Layer ArchUnit 테스트 공통 패키지 상수
 *
 * <p>다른 프로젝트에 템플릿을 적용할 때 {@link #BASE_PACKAGE} 값만 변경하면 됩니다.
 *
 * <p><strong>사용 예시 (프로젝트 적용 시):</strong>
 *
 * <pre>{@code
 * // 기본값
 * private static final String BASE_PACKAGE = "com.ryuqq";
 *
 * // 다른 프로젝트 적용 시
 * private static final String BASE_PACKAGE = "com.mycompany.myproject";
 * }</pre>
 *
 * <p><strong>참고 문서:</strong>
 *
 * <ul>
 *   <li>docs/coding_convention/00-project-setup/project-customization-guide.md
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class ArchUnitPackageConstants {

    // ========================================================================
    // 🔧 프로젝트 적용 시 이 값만 변경하세요
    // ========================================================================

    /**
     * 프로젝트 기본 패키지 (루트 패키지)
     *
     * <p>다른 프로젝트에 적용할 때 이 값을 해당 프로젝트의 기본 패키지로 변경합니다.
     *
     * <p>예시:
     *
     * <ul>
     *   <li>기본값: "com.ryuqq"
     *   <li>E커머스: "com.acme.ecommerce"
     *   <li>결제 시스템: "com.acme.payment"
     * </ul>
     */
    public static final String BASE_PACKAGE = "com.ryuqq.fileflow";

    // ========================================================================
    // 레이어별 패키지 (자동 파생 - 수정 불필요)
    // ========================================================================

    /** REST API Adapter Layer 패키지 */
    public static final String ADAPTER_IN_REST = BASE_PACKAGE + ".adapter.in.rest";

    /** Domain Layer 패키지 */
    public static final String DOMAIN = BASE_PACKAGE + ".domain";

    /** Domain Common 패키지 */
    public static final String DOMAIN_COMMON = DOMAIN + ".common";

    /** Domain Exception 패키지 */
    public static final String DOMAIN_EXCEPTION = DOMAIN + "..exception..";

    /** Application Layer 패키지 */
    public static final String APPLICATION = BASE_PACKAGE + ".application";

    /** Application Port 패키지 */
    public static final String APPLICATION_PORT = APPLICATION + "..port..";

    /** Persistence Adapter Layer 패키지 */
    public static final String ADAPTER_OUT_PERSISTENCE = BASE_PACKAGE + ".adapter.out.persistence";

    // ========================================================================
    // 서브 패키지 패턴 (ArchUnit 규칙에서 사용)
    // ========================================================================

    /** Controller 패키지 패턴 */
    public static final String CONTROLLER_PATTERN = "..controller..";

    /** DTO 패키지 패턴 */
    public static final String DTO_PATTERN = "..dto..";

    /** Mapper 패키지 패턴 */
    public static final String MAPPER_PATTERN = "..mapper..";

    /** Error 패키지 패턴 */
    public static final String ERROR_PATTERN = "..error..";

    /** Config 패키지 패턴 */
    public static final String CONFIG_PATTERN = "..config..";

    /** Filter 패키지 패턴 */
    public static final String FILTER_PATTERN = "..filter..";

    /** Auth 패키지 패턴 */
    public static final String AUTH_PATTERN = "..auth..";

    /** Common 패키지 패턴 */
    public static final String COMMON_PATTERN = "..common..";

    /** Architecture 패키지 패턴 (테스트 제외용) */
    public static final String ARCHITECTURE_PATTERN = "..architecture..";

    // ========================================================================
    // DTO 서브 패키지 패턴
    // ========================================================================

    /** Command DTO 패키지 패턴 */
    public static final String DTO_COMMAND_PATTERN = "..dto.command..";

    /** Query DTO 패키지 패턴 */
    public static final String DTO_QUERY_PATTERN = "..dto.query..";

    /** Response DTO 패키지 패턴 */
    public static final String DTO_RESPONSE_PATTERN = "..dto.response..";

    // ========================================================================
    // Common 서브 패키지 패턴
    // ========================================================================

    /** Common DTO 패키지 패턴 */
    public static final String COMMON_DTO_PATTERN = "..common.dto..";

    /** Common Controller 패키지 패턴 */
    public static final String COMMON_CONTROLLER_PATTERN = "..common.controller..";

    /** Common Error 패키지 패턴 */
    public static final String COMMON_ERROR_PATTERN = "..common.error..";

    /** Common Mapper 패키지 패턴 */
    public static final String COMMON_MAPPER_PATTERN = "..common.mapper..";

    /** Common Filter 패키지 패턴 */
    public static final String COMMON_FILTER_PATTERN = "..common.filter..";

    // ========================================================================
    // Config 서브 패키지 패턴
    // ========================================================================

    /** Config Properties 패키지 패턴 */
    public static final String CONFIG_PROPERTIES_PATTERN = "..config.properties..";

    // ========================================================================
    // Auth 서브 패키지 패턴
    // ========================================================================

    /** Auth Paths 패키지 패턴 */
    public static final String AUTH_PATHS_PATTERN = "..auth.paths..";

    /** Auth Config 패키지 패턴 */
    public static final String AUTH_CONFIG_PATTERN = "..auth.config..";

    /** Auth Filter 패키지 패턴 */
    public static final String AUTH_FILTER_PATTERN = "..auth.filter..";

    /** Auth Handler 패키지 패턴 */
    public static final String AUTH_HANDLER_PATTERN = "..auth.handler..";

    /** Auth Component 패키지 패턴 */
    public static final String AUTH_COMPONENT_PATTERN = "..auth.component..";

    // ========================================================================
    // 도메인 관련 패턴 (와일드카드 포함)
    // ========================================================================

    /** Domain 전체 패키지 패턴 (와일드카드) */
    public static final String DOMAIN_ALL = DOMAIN + "..";

    /** Domain Exception 패턴 (와일드카드) */
    public static final String DOMAIN_EXCEPTION_ALL = DOMAIN + "..exception..";

    /** Domain IAM 패턴 (인증/인가 관련 - REST API에서 예외 허용) */
    public static final String DOMAIN_IAM_ALL = DOMAIN + ".iam..";

    /** Persistence 전체 패키지 패턴 (와일드카드) */
    public static final String PERSISTENCE_ALL = ADAPTER_OUT_PERSISTENCE + "..";

    /** Port In 패키지 패턴 */
    public static final String PORT_IN_PATTERN = "..port.in..";

    /** Port Out 패키지 패턴 */
    public static final String PORT_OUT_PATTERN = "..port.out..";

    private ArchUnitPackageConstants() {
        // 인스턴스화 방지
    }
}
