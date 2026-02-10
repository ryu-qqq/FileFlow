package com.ryuqq.fileflow.application.session.dto.bundle;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import com.ryuqq.fileflow.domain.session.vo.UploadTarget;
import java.time.Instant;
import java.util.Objects;

/**
 * 멀티파트 업로드 세션 생성 번들.
 *
 * <p>Factory에서 도메인 계산 결과를 담고, Coordinator에서 외부 데이터(bucket, uploadId)를 enrichment한 뒤 {@link
 * #toSession()}으로 최종 도메인 객체를 생성합니다.
 *
 * @param sessionId 세션 ID
 * @param s3Key S3 객체 키
 * @param bucket S3 버킷명 (Coordinator에서 withBucket으로 설정)
 * @param accessType 접근 유형
 * @param fileName 원본 파일명
 * @param contentType MIME 타입
 * @param partSize 파트 크기 (bytes)
 * @param uploadId S3 멀티파트 업로드 ID (Coordinator에서 withUploadId로 설정)
 * @param purpose 파일 용도
 * @param source 요청 서비스명
 * @param expiresAt 만료 시각
 * @param createdAt 생성 시각
 * @param expiration 세션 만료 등록 정보
 */
public record MultipartSessionCreationBundle(
        MultipartUploadSessionId sessionId,
        String s3Key,
        String bucket,
        AccessType accessType,
        String fileName,
        String contentType,
        long partSize,
        String uploadId,
        String purpose,
        String source,
        Instant expiresAt,
        Instant createdAt,
        SessionExpiration expiration) {

    public static MultipartSessionCreationBundle of(
            MultipartUploadSessionId sessionId,
            String s3Key,
            AccessType accessType,
            String fileName,
            String contentType,
            long partSize,
            String purpose,
            String source,
            Instant expiresAt,
            Instant createdAt,
            SessionExpiration expiration) {
        return new MultipartSessionCreationBundle(
                sessionId,
                s3Key,
                null,
                accessType,
                fileName,
                contentType,
                partSize,
                null,
                purpose,
                source,
                expiresAt,
                createdAt,
                expiration);
    }

    public MultipartSessionCreationBundle withBucket(String bucket) {
        return new MultipartSessionCreationBundle(
                sessionId,
                s3Key,
                bucket,
                accessType,
                fileName,
                contentType,
                partSize,
                uploadId,
                purpose,
                source,
                expiresAt,
                createdAt,
                expiration);
    }

    public MultipartSessionCreationBundle withUploadId(String uploadId) {
        return new MultipartSessionCreationBundle(
                sessionId,
                s3Key,
                bucket,
                accessType,
                fileName,
                contentType,
                partSize,
                uploadId,
                purpose,
                source,
                expiresAt,
                createdAt,
                expiration);
    }

    public MultipartUploadSession toSession() {
        Objects.requireNonNull(
                bucket, "bucket must be set via withBucket before calling toSession");
        Objects.requireNonNull(
                uploadId, "uploadId must be set via withUploadId before calling toSession");

        UploadTarget uploadTarget =
                UploadTarget.of(s3Key, bucket, accessType, fileName, contentType);

        return MultipartUploadSession.forNew(
                sessionId, uploadTarget, uploadId, partSize, purpose, source, expiresAt, createdAt);
    }
}
