package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.session.vo.FileSize;

/**
 * 파일 업로드 세션을 위한 사용자 Role 정의.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>SYSTEM: 서버 간 내부 호출 (최상위 권한, namespace: connectly)
 *   <li>SUPER_ADMIN: 시스템 전체 관리자 (namespace: connectly, 무제한)
 *   <li>ADMIN: 테넌트 관리자 (namespace: connectly, 무제한)
 *   <li>SELLER: 제한적 업로드 (namespace: setof, 최대 5GB)
 *   <li>DEFAULT: 기본 업로드 (namespace: setof, 최대 1GB)
 * </ul>
 *
 * <p><strong>역할 우선순위</strong>: SYSTEM > SUPER_ADMIN > ADMIN > SELLER > DEFAULT
 */
public enum UserRole {
    /** 시스템 내부 호출 (서버 간 통신, 최상위 권한) */
    SYSTEM("connectly", 5L * 1024 * 1024 * 1024 * 1024), // 5TB (실질적 무제한)

    SUPER_ADMIN("connectly", 5L * 1024 * 1024 * 1024 * 1024), // 5TB (실질적 무제한)
    ADMIN("connectly", 5L * 1024 * 1024 * 1024 * 1024), // 5TB (실질적 무제한)
    SELLER("setof", 5L * 1024 * 1024 * 1024), // 5GB
    DEFAULT("setof", (long) 1024 * 1024 * 1024); // 1GB

    private final String namespace;
    private final long maxFileSizeBytes;

    UserRole(String namespace, long maxFileSizeBytes) {
        this.namespace = namespace;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    /**
     * Role별 네임스페이스 반환.
     *
     * @return 네임스페이스 문자열
     */
    public String namespace() {
        return namespace;
    }

    /**
     * Role별 네임스페이스 반환 (Getter).
     *
     * @return 네임스페이스 문자열
     */
    public String getNamespace() {
        return namespace();
    }

    /**
     * Role별 최대 파일 크기 반환 (바이트).
     *
     * @return 최대 파일 크기 (바이트)
     */
    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    /**
     * 업로드 권한이 있는지 확인한다.
     *
     * @return 업로드 가능하면 true (모든 Role은 업로드 가능)
     */
    public boolean hasUploadPermission() {
        return true; // 모든 Role은 기본적으로 업로드 가능
    }

    /**
     * 파일 크기가 허용 범위 내인지 확인한다.
     *
     * @param fileSize 파일 크기 (바이트)
     * @return 허용 범위 내이면 true
     */
    public boolean canUpload(long fileSize) {
        return fileSize > 0 && fileSize <= maxFileSizeBytes;
    }

    /**
     * FileSize VO로 파일 크기 허용 여부를 확인한다.
     *
     * @param fileSize FileSize VO
     * @return 허용 범위 내이면 true
     */
    public boolean canUpload(FileSize fileSize) {
        if (fileSize == null) {
            return false;
        }
        return canUpload(fileSize.size());
    }

    /**
     * 시스템 호출인지 확인한다.
     *
     * @return SYSTEM이면 true
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }

    /**
     * 관리자 권한인지 확인한다.
     *
     * @return SYSTEM, SUPER_ADMIN 또는 ADMIN이면 true
     */
    public boolean isAdmin() {
        return this == SYSTEM || this == SUPER_ADMIN || this == ADMIN;
    }

    /**
     * 슈퍼 관리자 권한인지 확인한다.
     *
     * @return SUPER_ADMIN이면 true
     */
    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }

    /**
     * 판매자 권한인지 확인한다.
     *
     * @return SELLER이면 true
     */
    public boolean isSeller() {
        return this == SELLER;
    }

    /**
     * 기본 사용자인지 확인한다.
     *
     * @return DEFAULT이면 true
     */
    public boolean isDefault() {
        return this == DEFAULT;
    }

    /**
     * 최대 파일 크기를 사람이 읽기 쉬운 형식으로 반환한다.
     *
     * @return 포맷된 최대 파일 크기 (예: "1 GB", "5 GB", "5 TB")
     */
    public String getMaxFileSizeFormatted() {
        return FileSize.of(maxFileSizeBytes).toHumanReadable();
    }

    /**
     * 문자열에서 UserRole을 파싱한다.
     *
     * <p>대소문자 구분 없이 매칭하며, 매칭되지 않으면 DEFAULT를 반환한다.
     *
     * @param roleStr 역할 문자열 (예: "SYSTEM", "SUPER_ADMIN", "admin", "SELLER")
     * @return 매칭되는 UserRole, 없으면 DEFAULT
     */
    public static UserRole fromString(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return DEFAULT;
        }

        String normalized = roleStr.trim().toUpperCase();
        for (UserRole role : values()) {
            if (role.name().equals(normalized)) {
                return role;
            }
        }
        return DEFAULT;
    }

    /**
     * 역할 목록에서 가장 높은 우선순위의 역할을 반환한다.
     *
     * <p>우선순위: SYSTEM > SUPER_ADMIN > ADMIN > SELLER > DEFAULT
     *
     * @param roles 역할 문자열 목록
     * @return 가장 높은 우선순위의 UserRole
     */
    public static UserRole highestPriority(java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return DEFAULT;
        }

        UserRole highest = DEFAULT;
        for (String roleStr : roles) {
            UserRole role = fromString(roleStr);
            if (role.ordinal() < highest.ordinal()) {
                highest = role;
            }
        }
        return highest;
    }
}
