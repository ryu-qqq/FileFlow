package com.ryuqq.fileflow.domain.iam.vo;

/**
 * 테넌트 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>테넌트 ID는 UUIDv7 형식이어야 한다.
 *   <li>테넌트명은 null이거나 빈 문자열일 수 없다.
 *   <li>현재 시스템은 단일 테넌트 (Connectly)를 지원한다.
 *   <li>향후 멀티테넌트 확장을 위한 구조를 유지한다.
 * </ul>
 *
 * @param id 테넌트 ID (UUIDv7)
 * @param name 테넌트명 (예: "Connectly")
 */
public record Tenant(TenantId id, String name) {

    // Connectly 테넌트의 Well-Known ID (UUIDv7 형식, 시스템에서 고정값 사용)
    private static final String CONNECTLY_TENANT_ID = "b77f45da-d64b-71f0-ac37-02d75368c93d";
    private static final String CONNECTLY_NAME = "connectly";

    /** Compact Constructor (검증 로직). */
    public Tenant {
        if (id == null) {
            throw new IllegalArgumentException("테넌트 ID는 null일 수 없습니다.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테넌트명은 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param id 테넌트 ID
     * @param name 테넌트명
     * @return Tenant
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static Tenant of(TenantId id, String name) {
        return new Tenant(id, name);
    }

    /**
     * Connectly 테넌트 생성 (현재 유일한 테넌트).
     *
     * <p>시스템에서 사용하는 Well-Known TenantId를 사용한다.
     *
     * @return Connectly Tenant
     */
    public static Tenant connectly() {
        return new Tenant(TenantId.of(CONNECTLY_TENANT_ID), CONNECTLY_NAME);
    }

    /**
     * 새로운 테넌트 생성 (향후 멀티테넌트 지원용).
     *
     * @param name 테넌트명
     * @return 새로운 UUIDv7 기반 Tenant
     */
    public static Tenant create(String name) {
        return new Tenant(TenantId.generate(), name);
    }

    /**
     * Connectly 테넌트인지 확인한다.
     *
     * @return Connectly 테넌트이면 true
     */
    public boolean isConnectly() {
        return CONNECTLY_NAME.equals(name);
    }
}
