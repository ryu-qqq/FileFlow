package com.ryuqq.fileflow.application.session.dto.bundle;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import com.ryuqq.fileflow.domain.session.vo.UploadTarget;
import java.time.Instant;
import java.util.Objects;

/**
 * 단건 업로드 세션 생성 번들.
 *
 * <p>Factory에서 도메인 계산 결과를 담고, Coordinator에서 외부 데이터(bucket, presignedUrl)를 enrichment한 뒤 {@link
 * #toSession()}으로 최종 도메인 객체를 생성합니다.
 *
 * @param sessionId 세션 ID
 * @param s3Key S3 객체 키
 * @param bucket S3 버킷명 (Coordinator에서 withBucket으로 설정)
 * @param accessType 접근 유형
 * @param fileName 원본 파일명
 * @param contentType MIME 타입
 * @param presignedUrl Presigned URL (Coordinator에서 withPresignedUrl로 설정)
 * @param purpose 파일 용도
 * @param source 요청 서비스명
 * @param expiresAt 만료 시각
 * @param createdAt 생성 시각
 * @param expiration 세션 만료 등록 정보
 */
public record SingleSessionCreationBundle(
        SingleUploadSessionId sessionId,
        String s3Key,
        String bucket,
        AccessType accessType,
        String fileName,
        String contentType,
        String presignedUrl,
        String purpose,
        String source,
        Instant expiresAt,
        Instant createdAt,
        SessionExpiration expiration) {

    public static SingleSessionCreationBundle of(
            SingleUploadSessionId sessionId,
            String s3Key,
            AccessType accessType,
            String fileName,
            String contentType,
            String purpose,
            String source,
            Instant expiresAt,
            Instant createdAt,
            SessionExpiration expiration) {
        return new SingleSessionCreationBundle(
                sessionId,
                s3Key,
                null,
                accessType,
                fileName,
                contentType,
                null,
                purpose,
                source,
                expiresAt,
                createdAt,
                expiration);
    }

    public SingleSessionCreationBundle withBucket(String bucket) {
        return new SingleSessionCreationBundle(
                sessionId,
                s3Key,
                bucket,
                accessType,
                fileName,
                contentType,
                presignedUrl,
                purpose,
                source,
                expiresAt,
                createdAt,
                expiration);
    }

    public SingleSessionCreationBundle withPresignedUrl(String presignedUrl) {
        return new SingleSessionCreationBundle(
                sessionId,
                s3Key,
                bucket,
                accessType,
                fileName,
                contentType,
                presignedUrl,
                purpose,
                source,
                expiresAt,
                createdAt,
                expiration);
    }

    public SingleUploadSession toSession() {
        Objects.requireNonNull(
                bucket, "bucket must be set via withBucket before calling toSession");
        Objects.requireNonNull(
                presignedUrl,
                "presignedUrl must be set via withPresignedUrl before calling toSession");

        UploadTarget uploadTarget =
                UploadTarget.of(s3Key, bucket, accessType, fileName, contentType);

        return SingleUploadSession.forNew(
                sessionId, uploadTarget, presignedUrl, purpose, source, expiresAt, createdAt);
    }
}
