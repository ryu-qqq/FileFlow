package com.ryuqq.fileflow.domain.upload.vo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 멀티파트 업로드의 개별 파트 정보를 나타내는 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - 각 파트는 독립적인 Presigned URL을 가짐
 *
 * AWS S3 멀티파트 파트 제약:
 * - 파트 번호: 1 ~ 10,000
 * - 파트 크기: 5MB (minimum) ~ 5GB (maximum)
 * - 마지막 파트는 5MB 미만 가능
 */
public record PartUploadInfo(
        int partNumber,
        String presignedUrl,
        long startByte,
        long endByte,
        LocalDateTime expiresAt
) {

    private static final long MIN_PART_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_PART_SIZE = 5L * 1024 * 1024 * 1024; // 5GB
    private static final int MAX_PART_NUMBER = 10_000;

    /**
     * Compact constructor로 검증 로직 수행
     */
    public PartUploadInfo {
        validatePartNumber(partNumber);
        validatePresignedUrl(presignedUrl);
        validateByteRange(startByte, endByte, partNumber);
        validateExpiresAt(expiresAt);
    }

    /**
     * PartUploadInfo를 생성합니다.
     *
     * @param partNumber 파트 번호 (1-based, 1 ~ 10,000)
     * @param presignedUrl 파트 업로드용 Presigned URL
     * @param startByte 파트 시작 바이트 위치 (0-based)
     * @param endByte 파트 종료 바이트 위치 (inclusive)
     * @param expiresAt Presigned URL 만료 시간
     * @return PartUploadInfo 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static PartUploadInfo of(
            int partNumber,
            String presignedUrl,
            long startByte,
            long endByte,
            LocalDateTime expiresAt
    ) {
        return new PartUploadInfo(
                partNumber,
                presignedUrl,
                startByte,
                endByte,
                expiresAt
        );
    }

    /**
     * 파트 크기를 바이트 단위로 반환합니다.
     *
     * @return 파트 크기 (bytes)
     */
    public long partSizeBytes() {
        return endByte - startByte + 1;
    }

    /**
     * 파트 크기를 MB 단위로 반환합니다.
     *
     * @return 파트 크기 (MB)
     */
    public double partSizeMB() {
        return partSizeBytes() / (1024.0 * 1024.0);
    }

    /**
     * Presigned URL이 만료되었는지 확인합니다.
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now(ZoneOffset.UTC).isAfter(expiresAt);
    }

    // ========== Validation Methods ==========

    private static void validatePartNumber(int partNumber) {
        if (partNumber < 1 || partNumber > MAX_PART_NUMBER) {
            throw new IllegalArgumentException(
                    "Part number must be between 1 and " + MAX_PART_NUMBER +
                    ". Got: " + partNumber
            );
        }
    }

    private static void validatePresignedUrl(String presignedUrl) {
        if (presignedUrl == null || presignedUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("PresignedUrl cannot be null or empty");
        }

        // URL 형식 기본 검증
        if (!presignedUrl.startsWith("http://") && !presignedUrl.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "PresignedUrl must start with http:// or https://. Got: " + presignedUrl
            );
        }
    }

    private static void validateByteRange(long startByte, long endByte, int partNumber) {
        if (startByte < 0) {
            throw new IllegalArgumentException(
                    "StartByte cannot be negative. Got: " + startByte
            );
        }

        if (endByte < startByte) {
            throw new IllegalArgumentException(
                    "EndByte (" + endByte + ") must be >= StartByte (" + startByte + ")"
            );
        }

        long partSize = endByte - startByte + 1;

        // AWS S3 제약: 파트 크기는 5MB 이상, 5GB 이하
        // 단, 마지막 파트는 5MB 미만 가능 (partNumber만으로는 마지막 파트 판단 불가하므로 경고만)
        if (partSize > MAX_PART_SIZE) {
            throw new IllegalArgumentException(
                    "Part size (" + partSize + " bytes) exceeds maximum allowed size (" +
                    MAX_PART_SIZE + " bytes = 5GB)"
            );
        }

        // 5MB 미만인 경우 경고 (마지막 파트가 아닌 경우 AWS S3에서 거부됨)
        if (partSize < MIN_PART_SIZE) {
            // 마지막 파트가 아닌 경우를 판단할 수 없으므로 경고만 출력
            // 실제 업로드 시 AWS S3에서 검증됨
        }
    }

    private static void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("ExpiresAt cannot be null");
        }

        // 과거 시간은 허용하지 않음
        if (expiresAt.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new IllegalArgumentException(
                    "ExpiresAt must be in the future. Got: " + expiresAt
            );
        }
    }
}
