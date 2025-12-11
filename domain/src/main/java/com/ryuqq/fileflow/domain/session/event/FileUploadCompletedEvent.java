package com.ryuqq.fileflow.domain.session.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Instant;

/**
 * 파일 업로드 완료 이벤트.
 *
 * <p>Single/Multipart 업로드 완료 시 발행됩니다.
 *
 * @param sessionId 업로드 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기
 * @param contentType 컨텐츠 타입
 * @param bucket S3 버킷
 * @param s3Key S3 키
 * @param etag ETag
 * @param userId 사용자 ID (Customer만, Admin/Seller는 null) - UUIDv7
 * @param organizationId 조직 ID (Seller만, Admin/Customer는 null) - UUIDv7
 * @param tenantId 테넌트 ID - UUIDv7
 * @param completedAt 완료 시간
 */
public record FileUploadCompletedEvent(
        UploadSessionId sessionId,
        FileName fileName,
        FileSize fileSize,
        ContentType contentType,
        S3Bucket bucket,
        S3Key s3Key,
        ETag etag,
        UserId userId,
        OrganizationId organizationId,
        TenantId tenantId,
        Instant completedAt)
        implements DomainEvent {

    public static FileUploadCompletedEvent of(
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            UserId userId,
            OrganizationId organizationId,
            TenantId tenantId,
            Instant completedAt) {
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

    public Instant occurredAt() {
        return completedAt;
    }
}
