package com.ryuqq.fileflow.domain.session.event;

import java.time.Instant;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * 업로드 완료 이벤트.
 * Single 또는 Multipart 세션이 완료되면 발행됩니다.
 * 이 이벤트를 수신하여 Asset을 생성합니다.
 *
 * @param sessionId 완료된 세션의 ID
 * @param sessionType SINGLE 또는 MULTIPART
 * @param s3Key S3 객체 키
 * @param bucket S3 버킷명
 * @param accessType 접근 유형
 * @param fileName 원본 파일명
 * @param contentType MIME 타입
 * @param fileSize 파일 크기 (bytes)
 * @param etag S3 ETag
 * @param purpose 파일 용도
 * @param source 요청 서비스명
 * @param occurredAt 이벤트 발생 시각
 */
public record UploadCompletedEvent(
        String sessionId,
        String sessionType,
        String s3Key,
        String bucket,
        AccessType accessType,
        String fileName,
        String contentType,
        long fileSize,
        String etag,
        String purpose,
        String source,
        Instant occurredAt
) implements DomainEvent {

    public static UploadCompletedEvent of(
            String sessionId,
            String sessionType,
            String s3Key,
            String bucket,
            AccessType accessType,
            String fileName,
            String contentType,
            long fileSize,
            String etag,
            String purpose,
            String source,
            Instant occurredAt
    ) {
        return new UploadCompletedEvent(
                sessionId, sessionType, s3Key, bucket, accessType,
                fileName, contentType, fileSize, etag, purpose, source, occurredAt
        );
    }
}
