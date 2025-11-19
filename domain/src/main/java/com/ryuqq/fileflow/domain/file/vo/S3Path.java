package com.ryuqq.fileflow.domain.file.vo;

import static java.util.Objects.requireNonNull;

import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * S3 경로 Value Object.
 */
public record S3Path(
    Long tenantId,
    String namespace,
    String sellerName,
    String customPath,
    String fileId,
    String extension
) {

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
     * @param role 사용자 역할
     * @param tenantId 테넌트 ID
     * @param sellerName 셀러명 (SELLER만 필수)
     * @param customPath 커스텀 경로
     * @param fileId 파일 ID
     * @param mimeType MIME 타입 (확장자 추출용)
     * @return S3Path VO
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
        String effectiveSellerName = switch (role) {
            case ADMIN -> "default";
            case SELLER, DEFAULT -> requireNonNull(sellerName);
        };
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
     * S3 전체 경로 반환
     *
     * @return S3 객체 키 (예: "123/setof/seller1/uploads/abc-123.jpg")
     */
    public String getFullPath() {
        String basePath = tenantId + "/" + namespace;

        if ("connectly".equals(namespace)) {
            // ADMIN: {tenantId}/connectly/{customPath}/{fileId}.{ext}
            return basePath + "/" + customPath + "/" + fileId + extension;
        } else {
            // SELLER/DEFAULT: {tenantId}/setof/{sellerName}/{customPath}/{fileId}.{ext}
            return basePath + "/" + sellerName + "/" + customPath + "/" + fileId + extension;
        }
    }

    /**
     * MIME 타입에서 확장자 추출
     *
     * @param mimeType MIME 타입
     * @return 확장자 (예: ".jpg", ".png", ".html")
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

