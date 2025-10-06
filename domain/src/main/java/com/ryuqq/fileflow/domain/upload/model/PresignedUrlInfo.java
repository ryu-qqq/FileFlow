package com.ryuqq.fileflow.domain.upload.model;

import java.time.LocalDateTime;

/**
 * Presigned URL 정보를 나타내는 Value Object
 * S3 등의 스토리지에 업로드하기 위한 임시 URL 정보를 포함합니다.
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - URL과 메타데이터를 안전하게 캡슐화
 */
public record PresignedUrlInfo(
        String presignedUrl,
        String uploadPath,
        LocalDateTime expiresAt
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public PresignedUrlInfo {
        validatePresignedUrl(presignedUrl);
        validateUploadPath(uploadPath);
        validateExpiresAt(expiresAt);
    }

    /**
     * PresignedUrlInfo를 생성합니다.
     *
     * @param presignedUrl Presigned URL
     * @param uploadPath 업로드 경로
     * @param expiresAt 만료 시간
     * @return PresignedUrlInfo 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static PresignedUrlInfo of(
            String presignedUrl,
            String uploadPath,
            LocalDateTime expiresAt
    ) {
        return new PresignedUrlInfo(presignedUrl, uploadPath, expiresAt);
    }

    /**
     * URL이 만료되었는지 확인합니다.
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * URL이 유효한지 확인합니다.
     *
     * @return 유효 여부
     */
    public boolean isValid() {
        return !isExpired();
    }

    // ========== Validation Methods ==========

    private static void validatePresignedUrl(String presignedUrl) {
        if (presignedUrl == null || presignedUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("PresignedUrl cannot be null or empty");
        }
        if (!presignedUrl.startsWith("http://") && !presignedUrl.startsWith("https://")) {
            throw new IllegalArgumentException("PresignedUrl must start with http:// or https://");
        }
    }

    private static void validateUploadPath(String uploadPath) {
        if (uploadPath == null || uploadPath.trim().isEmpty()) {
            throw new IllegalArgumentException("UploadPath cannot be null or empty");
        }
    }

    private static void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("ExpiresAt cannot be null");
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("ExpiresAt cannot be in the past");
        }
    }
}
