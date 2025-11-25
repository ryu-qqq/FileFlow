package com.ryuqq.fileflow.domain.iam.vo;

/**
 * 테넌트 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>테넌트 ID는 1 이상이어야 한다.
 *   <li>테넌트명은 null이거나 빈 문자열일 수 없다.
 *   <li>현재 시스템은 단일 테넌트 (tenantId=1, Connectly)를 지원한다.
 *   <li>향후 멀티테넌트 확장을 위한 구조를 유지한다.
 * </ul>
 *
 * @param id 테넌트 ID (1부터 시작)
 * @param name 테넌트명 (예: "Connectly")
 */
public record Tenant(long id, String name) {

    /** Compact Constructor (검증 로직). */
    public Tenant {
        if (id < 1) {
            throw new IllegalArgumentException("테넌트 ID는 1 이상이어야 합니다: " + id);
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
    public static Tenant of(long id, String name) {
        return new Tenant(id, name);
    }

    /**
     * Connectly 테넌트 생성 (현재 유일한 테넌트).
     *
     * @return Connectly Tenant (id=1)
     */
    public static Tenant connectly() {
        return new Tenant(1L, "Connectly");
    }

    /**
     * Connectly 테넌트인지 확인한다.
     *
     * @return id가 1이고 name이 "Connectly"이면 true
     */
    public boolean isConnectly() {
        return id == 1L && "Connectly".equals(name);
    }
}
