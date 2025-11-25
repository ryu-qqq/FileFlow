package com.ryuqq.fileflow.domain.iam.vo;

import com.ryuqq.fileflow.domain.session.vo.FileSize;

/**
 * 파일 업로드 세션을 위한 사용자 Role 정의.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>ADMIN: 무제한 업로드 (namespace: connectly)
 *   <li>SELLER: 제한적 업로드 (namespace: setof, 최대 5GB)
 *   <li>DEFAULT: 기본 업로드 (namespace: setof, 최대 1GB)
 * </ul>
 */
public enum UserRole {
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
     * 관리자 권한인지 확인한다.
     *
     * @return ADMIN이면 true
     */
    public boolean isAdmin() {
        return this == ADMIN;
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
}
