package com.ryuqq.fileflow.domain.upload.event;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * S3 업로드 완료 이벤트
 *
 * S3에 파일 업로드가 성공적으로 완료되어 S3 Event를 수신했을 때 발생하는 도메인 이벤트입니다.
 *
 * 용도:
 * - 업로드 세션 상태를 UPLOAD_COMPLETED로 전환
 * - 파일 메타데이터 추출 프로세스 시작
 * - 파일 무결성 검증 트리거
 */
public record UploadConfirmed(
        String sessionId,
        String s3Bucket,
        String s3Key,
        long fileSizeBytes,
        String etag,
        LocalDateTime uploadedAt
) {

    /**
     * UploadConfirmed 이벤트를 생성합니다.
     *
     * @param sessionId 업로드 세션 ID
     * @param s3Bucket S3 버킷명
     * @param s3Key S3 객체 키
     * @param fileSizeBytes 실제 업로드된 파일 크기
     * @param etag S3 ETag (MD5 해시)
     * @param uploadedAt 업로드 완료 시간
     * @return UploadConfirmed 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static UploadConfirmed of(
            String sessionId,
            String s3Bucket,
            String s3Key,
            long fileSizeBytes,
            String etag,
            LocalDateTime uploadedAt
    ) {
        return of(sessionId, s3Bucket, s3Key, fileSizeBytes, etag, uploadedAt, Clock.systemDefaultZone());
    }

    /**
     * UploadConfirmed 이벤트를 생성합니다 (테스트용 Clock 주입).
     *
     * @param sessionId 업로드 세션 ID
     * @param s3Bucket S3 버킷명
     * @param s3Key S3 객체 키
     * @param fileSizeBytes 실제 업로드된 파일 크기
     * @param etag S3 ETag (MD5 해시)
     * @param uploadedAt 업로드 완료 시간
     * @param clock 시간 검증용 Clock
     * @return UploadConfirmed 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static UploadConfirmed of(
            String sessionId,
            String s3Bucket,
            String s3Key,
            long fileSizeBytes,
            String etag,
            LocalDateTime uploadedAt,
            Clock clock
    ) {
        validateSessionId(sessionId);
        validateS3Bucket(s3Bucket);
        validateS3Key(s3Key);
        validateFileSizeBytes(fileSizeBytes);
        validateEtag(etag);
        validateUploadedAt(uploadedAt, clock);
        return new UploadConfirmed(sessionId, s3Bucket, s3Key, fileSizeBytes, etag, uploadedAt);
    }

    /**
     * Compact constructor로 검증 로직 수행
     */
    public UploadConfirmed {
        // Note: Validation은 factory method에서 수행
        // Record는 불변이므로 생성 후 검증 불필요
    }

    // ========== Validation Methods ==========

    private static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
    }

    private static void validateS3Bucket(String s3Bucket) {
        if (s3Bucket == null || s3Bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("S3Bucket cannot be null or empty");
        }
    }

    private static void validateS3Key(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3Key cannot be null or empty");
        }
    }

    private static void validateFileSizeBytes(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw new IllegalArgumentException("FileSizeBytes must be positive");
        }
    }

    private static void validateEtag(String etag) {
        if (etag == null || etag.trim().isEmpty()) {
            throw new IllegalArgumentException("Etag cannot be null or empty");
        }
    }

    private static void validateUploadedAt(LocalDateTime uploadedAt, Clock clock) {
        if (uploadedAt == null) {
            throw new IllegalArgumentException("UploadedAt cannot be null");
        }
        if (uploadedAt.isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("UploadedAt cannot be in the future");
        }
    }

    // ========== Helper Methods ==========

    /**
     * S3 전체 경로를 반환합니다 (s3://bucket/key 형식).
     *
     * @return S3 URI 형식의 전체 경로
     */
    public String getFullS3Path() {
        return "s3://" + s3Bucket + "/" + s3Key;
    }
}
