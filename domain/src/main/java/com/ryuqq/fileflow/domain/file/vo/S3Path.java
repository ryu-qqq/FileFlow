package com.ryuqq.fileflow.domain.file.vo;

import static java.util.Objects.requireNonNull;

import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * S3 경로 Value Object.
 *
 * <p>
 * S3 객체 키를 구성하는 모든 요소를 캡슐화한다.
 * 역할(Role)에 따라 다른 경로 구조를 생성한다:
 * </p>
 * <ul>
 *     <li>ADMIN: {tenantId}/connectly/{customPath}/{fileId}.{ext}</li>
 *     <li>SELLER/DEFAULT: {tenantId}/setof/{sellerName}/{customPath}/{fileId}.{ext}</li>
 * </ul>
 */
public record S3Path(
    Long tenantId,
    String namespace,
    String sellerName,
    String customPath,
    String fileId,
    String extension
) {

    private static final String CONNECTLY_NAMESPACE = "connectly";
    private static final String DEFAULT_SELLER_NAME = "default";

    public S3Path {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("tenantId는 필수입니다");
        }
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace는 필수입니다");
        }
        if (fileId == null || fileId.isBlank()) {
            throw new IllegalArgumentException("fileId는 필수입니다");
        }
    }

    /**
     * S3Path 생성 팩토리 메서드
     *
     * @param role 사용자 역할 (namespace 결정에 사용)
     * @param tenantId 테넌트 ID (양수여야 함)
     * @param sellerName 셀러명 (SELLER, DEFAULT일 때 필수, ADMIN일 때 무시됨)
     * @param customPath 커스텀 경로 (null 가능)
     * @param fileId 파일 ID (필수, 빈 문자열 불가)
     * @param mimeType MIME 타입 (확장자 추출용)
     * @return S3Path VO
     * @throws IllegalArgumentException tenantId가 null이거나 0 이하인 경우
     * @throws NullPointerException SELLER/DEFAULT 역할일 때 sellerName이 null인 경우
     */
    public static S3Path from(
        UserRole role,
        Long tenantId,
        String sellerName,
        String customPath,
        String fileId,
        String mimeType
    ) {
        String namespace = role.getNamespace();
        String effectiveSellerName = determineSellerName(role, sellerName);
        String extension = extractExtension(mimeType);

        return new S3Path(
            tenantId,
            namespace,
            effectiveSellerName,
            customPath,
            fileId,
            extension
        );
    }

    /**
     * 역할에 따라 실제 사용될 셀러명을 결정한다.
     *
     * @param role 사용자 역할
     * @param sellerName 입력된 셀러명
     * @return ADMIN: "default", SELLER/DEFAULT: sellerName (null이면 예외)
     */
    private static String determineSellerName(UserRole role, String sellerName) {
        return switch (role) {
            case ADMIN -> DEFAULT_SELLER_NAME;
            case SELLER, DEFAULT -> requireNonNull(sellerName, "SELLER/DEFAULT 역할은 sellerName이 필수입니다");
        };
    }

    /**
     * S3 전체 경로를 생성하여 반환한다.
     *
     * <p>
     * 역할에 따라 다른 경로 구조를 생성:
     * </p>
     * <ul>
     *     <li>ADMIN (connectly): {tenantId}/connectly/{customPath}/{fileId}.{ext}</li>
     *     <li>SELLER/DEFAULT (setof): {tenantId}/setof/{sellerName}/{customPath}/{fileId}.{ext}</li>
     * </ul>
     *
     * @return S3 객체 키 (예: "123/setof/seller1/uploads/abc-123.jpg")
     */
    public String getFullPath() {
        return isAdminNamespace() ? buildAdminPath() : buildSellerPath();
    }

    /**
     * ADMIN 네임스페이스 여부를 확인한다.
     *
     * @return connectly 네임스페이스이면 true
     */
    private boolean isAdminNamespace() {
        return CONNECTLY_NAMESPACE.equals(namespace);
    }

    /**
     * ADMIN 역할용 경로를 생성한다.
     *
     * @return {tenantId}/connectly/{customPath}/{fileId}.{ext}
     */
    private String buildAdminPath() {
        return tenantId + "/" + namespace + "/" + customPath + "/" + fileId + extension;
    }

    /**
     * SELLER/DEFAULT 역할용 경로를 생성한다.
     *
     * @return {tenantId}/setof/{sellerName}/{customPath}/{fileId}.{ext}
     */
    private String buildSellerPath() {
        return tenantId + "/" + namespace + "/" + sellerName + "/" + customPath + "/" + fileId + extension;
    }

    /**
     * MIME 타입에서 확장자를 추출한다.
     *
     * <p>
     * 지원하는 MIME 타입:
     * </p>
     * <ul>
     *     <li>image/jpeg → .jpg</li>
     *     <li>image/png → .png</li>
     *     <li>image/gif → .gif</li>
     *     <li>image/webp → .webp</li>
     *     <li>text/html → .html</li>
     *     <li>기타 → 빈 문자열</li>
     * </ul>
     *
     * @param mimeType MIME 타입
     * @return 확장자 (예: ".jpg", ".png", ".html"), 지원하지 않는 타입은 빈 문자열
     */
    private static String extractExtension(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "text/html" -> ".html";
            default -> "";
        };
    }
}

