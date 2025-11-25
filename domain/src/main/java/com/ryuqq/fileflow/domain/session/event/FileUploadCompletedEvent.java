package com.ryuqq.fileflow.domain.session.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;

/**
 * 파일 업로드 완료 이벤트.
 *
 * <p>Single/Multipart 업로드 완료 시 발행됩니다.
 */
public record FileUploadCompletedEvent(
        UploadSessionId sessionId,
        FileName fileName,
        FileSize fileSize,
        ContentType contentType,
        S3Bucket bucket,
        S3Key s3Key,
        ETag etag,
        Long userId,
        Long organizationId,
        Long tenantId,
        LocalDateTime completedAt)
        implements DomainEvent {

    public static FileUploadCompletedEvent of(
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            LocalDateTime completedAt) {
        return new FileUploadCompletedEvent(
                sessionId,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                completedAt);
    }

    public LocalDateTime occurredAt() {
        return completedAt;
    }
}
