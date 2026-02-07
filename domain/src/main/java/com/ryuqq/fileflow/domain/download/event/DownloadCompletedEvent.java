package com.ryuqq.fileflow.domain.download.event;

import java.time.Instant;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * 외부 다운로드 완료 이벤트.
 * DownloadTask가 완료되면 발행되어 Asset 생성을 트리거합니다.
 */
public record DownloadCompletedEvent(
        String downloadTaskId,
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

    public static DownloadCompletedEvent of(
            String downloadTaskId, String s3Key, String bucket,
            AccessType accessType, String fileName, String contentType,
            long fileSize, String etag, String purpose, String source,
            Instant occurredAt
    ) {
        return new DownloadCompletedEvent(
                downloadTaskId, s3Key, bucket, accessType,
                fileName, contentType, fileSize, etag, purpose, source, occurredAt
        );
    }
}
