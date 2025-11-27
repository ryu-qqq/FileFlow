package com.ryuqq.fileflow.domain.download.event;

import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.LocalDateTime;

/**
 * 외부 다운로드 파일 생성 완료 이벤트.
 *
 * <p>ExternalDownload 완료 시 FileAsset 생성을 위해 발행됩니다.
 *
 * <p>이벤트 리스너에서 FileAsset을 생성하고 저장합니다.
 */
public record ExternalDownloadFileCreatedEvent(
        ExternalDownloadId downloadId,
        SourceUrl sourceUrl,
        FileName fileName,
        FileSize fileSize,
        ContentType contentType,
        FileCategory category,
        S3Bucket bucket,
        S3Key s3Key,
        ETag etag,
        Long organizationId,
        Long tenantId,
        LocalDateTime completedAt)
        implements DomainEvent {

    /**
     * 팩토리 메서드.
     *
     * @param downloadId 외부 다운로드 ID
     * @param sourceUrl 원본 URL
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType 컨텐츠 타입
     * @param category 파일 카테고리
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param etag ETag
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @param completedAt 완료 시간
     * @return ExternalDownloadFileCreatedEvent
     */
    public static ExternalDownloadFileCreatedEvent of(
            ExternalDownloadId downloadId,
            SourceUrl sourceUrl,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            Long organizationId,
            Long tenantId,
            LocalDateTime completedAt) {
        return new ExternalDownloadFileCreatedEvent(
                downloadId,
                sourceUrl,
                fileName,
                fileSize,
                contentType,
                category,
                bucket,
                s3Key,
                etag,
                organizationId,
                tenantId,
                completedAt);
    }

    /**
     * 이벤트 발생 시간을 반환합니다.
     *
     * @return 완료 시간
     */
    public LocalDateTime occurredAt() {
        return completedAt;
    }
}
