package com.ryuqq.fileflow.domain.image.command;

/**
 * Command 객체의 공통 검증 로직을 제공하는 유틸리티 클래스
 *
 * 목적:
 * - Command 객체들 간의 검증 로직 중복 제거
 * - 일관된 검증 규칙 적용
 * - 유지보수성 향상
 *
 * 사용:
 * - OptimizeImageCommand
 * - GenerateThumbnailCommand
 * - ExtractImageTextCommand
 */
public final class CommandValidators {

    private CommandValidators() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * ImageId의 유효성을 검증합니다.
     *
     * @param imageId 검증할 이미지 ID
     * @throws IllegalArgumentException imageId가 null이거나 빈 문자열인 경우
     */
    public static void validateImageId(String imageId) {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("ImageId cannot be null or empty");
        }
    }

    /**
     * S3 URI의 유효성을 검증합니다.
     *
     * @param s3Uri 검증할 S3 URI
     * @throws IllegalArgumentException s3Uri가 null, 빈 문자열이거나 's3://'로 시작하지 않는 경우
     */
    public static void validateS3Uri(String s3Uri) {
        if (s3Uri == null || s3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 URI cannot be null or empty");
        }
        if (!s3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("S3 URI must start with 's3://'");
        }
    }

    /**
     * Source S3 URI의 유효성을 검증합니다.
     * (validateS3Uri의 별칭 메서드로, 더 명확한 의미 전달을 위해 제공)
     *
     * @param sourceS3Uri 검증할 Source S3 URI
     * @throws IllegalArgumentException sourceS3Uri가 유효하지 않은 경우
     */
    public static void validateSourceS3Uri(String sourceS3Uri) {
        if (sourceS3Uri == null || sourceS3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Source S3 URI cannot be null or empty");
        }
        if (!sourceS3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Source S3 URI must start with 's3://'");
        }
    }
}
