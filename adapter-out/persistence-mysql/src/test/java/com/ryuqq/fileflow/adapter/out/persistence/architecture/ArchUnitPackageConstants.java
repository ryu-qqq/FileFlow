package com.ryuqq.fileflow.adapter.out.persistence.architecture;

/**
 * Persistence MySQL Layer ArchUnit 테스트 공통 패키지 상수
 *
 * <p>다른 프로젝트에 템플릿을 적용할 때 {@link #BASE_PACKAGE} 값만 변경하면 됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class ArchUnitPackageConstants {

    // ========================================================================
    // 프로젝트 적용 시 이 값만 변경하세요
    // ========================================================================

    /** 프로젝트 기본 패키지 (루트 패키지) */
    public static final String BASE_PACKAGE = "com.ryuqq.fileflow";

    // ========================================================================
    // 레이어별 패키지 (자동 파생 - 수정 불필요)
    // ========================================================================

    /** Persistence Adapter 패키지 */
    public static final String PERSISTENCE = BASE_PACKAGE + ".adapter.out.persistence";

    /** Domain Layer 패키지 */
    public static final String DOMAIN = BASE_PACKAGE + ".domain";

    /** Application Layer 패키지 */
    public static final String APPLICATION = BASE_PACKAGE + ".application";

    // ========================================================================
    // 패키지 패턴 (ArchUnit 규칙에서 사용)
    // ========================================================================

    /** Persistence 전체 패키지 패턴 */
    public static final String PERSISTENCE_ALL = PERSISTENCE + "..";

    /** Domain 전체 패키지 패턴 */
    public static final String DOMAIN_ALL = DOMAIN + "..";

    /** Application 전체 패키지 패턴 */
    public static final String APPLICATION_ALL = APPLICATION + "..";

    // ========================================================================
    // Persistence 서브 패키지 패턴
    // ========================================================================

    /** Entity 패키지 패턴 */
    public static final String ENTITY_PATTERN = "..entity..";

    /** Repository 패키지 패턴 */
    public static final String REPOSITORY_PATTERN = "..repository..";

    /** Mapper 패키지 패턴 */
    public static final String MAPPER_PATTERN = "..mapper..";

    /** Adapter 패키지 패턴 */
    public static final String ADAPTER_PATTERN = "..adapter..";

    /** Command Adapter 패키지 패턴 */
    public static final String ADAPTER_COMMAND_PATTERN = "..adapter.command..";

    /** Query Adapter 패키지 패턴 */
    public static final String ADAPTER_QUERY_PATTERN = "..adapter.query..";

    /** Config 패키지 패턴 */
    public static final String CONFIG_PATTERN = "..config..";

    /** Common 패키지 패턴 */
    public static final String COMMON_PATTERN = "..common..";

    /** Architecture 패키지 패턴 (테스트 제외용) */
    public static final String ARCHITECTURE_PATTERN = "..architecture..";

    private ArchUnitPackageConstants() {
        // 인스턴스화 방지
    }
}
