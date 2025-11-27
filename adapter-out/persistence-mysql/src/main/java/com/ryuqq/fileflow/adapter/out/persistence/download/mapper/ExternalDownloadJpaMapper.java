package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * ExternalDownload JPA Mapper.
 *
 * <p>Domain ↔ Entity 변환을 담당합니다.
 */
@Component
public class ExternalDownloadJpaMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    /**
     * Domain을 Entity로 변환합니다.
     *
     * @param domain ExternalDownload Domain
     * @return ExternalDownloadJpaEntity
     */
    public ExternalDownloadJpaEntity toEntity(ExternalDownload domain) {
        return ExternalDownloadJpaEntity.of(
                domain.getId().isNew() ? null : domain.getId().value(),
                domain.getSourceUrl().value(),
                domain.getTenantId(),
                domain.getOrganizationId(),
                domain.getS3Bucket().bucketName(),
                domain.getS3PathPrefix(),
                domain.getStatus(),
                domain.getRetryCountValue(),
                domain.getFileAssetId() != null ? domain.getFileAssetId().getValue() : null,
                domain.getErrorMessage(),
                domain.hasWebhook() ? domain.getWebhookUrl().value() : null,
                null, // version은 JPA가 관리
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getUpdatedAt()));
    }

    /**
     * Entity를 Domain으로 변환합니다.
     *
     * @param entity ExternalDownloadJpaEntity
     * @return ExternalDownload Domain
     */
    public ExternalDownload toDomain(ExternalDownloadJpaEntity entity) {
        return ExternalDownload.of(
                ExternalDownloadId.of(entity.getId()),
                SourceUrl.of(entity.getSourceUrl()),
                entity.getTenantId(),
                entity.getOrganizationId(),
                S3Bucket.of(entity.getS3Bucket()),
                entity.getS3PathPrefix(),
                entity.getStatus(),
                RetryCount.of(entity.getRetryCount()),
                entity.getFileAssetId() != null ? FileAssetId.of(entity.getFileAssetId()) : null,
                entity.getErrorMessage(),
                entity.getWebhookUrl() != null ? WebhookUrl.of(entity.getWebhookUrl()) : null,
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt()));
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZONE_ID);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZONE_ID).toInstant();
    }
}
