package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity.ExternalDownloadStatusJpa;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.WebhookOutboxJpaEntity.WebhookOutboxStatusJpa;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import org.springframework.stereotype.Component;

/**
 * WebhookOutbox JPA Mapper.
 *
 * <p>Domain ↔ Entity 변환을 담당합니다.
 */
@Component
public class WebhookOutboxJpaMapper {

    /**
     * Domain을 Entity로 변환합니다.
     *
     * @param domain WebhookOutbox Domain
     * @return WebhookOutboxJpaEntity
     */
    public WebhookOutboxJpaEntity toEntity(WebhookOutbox domain) {
        return WebhookOutboxJpaEntity.of(
                domain.getId().value(),
                domain.getExternalDownloadId().value(),
                domain.getWebhookUrl().value(),
                toJpaStatus(domain.getStatus()),
                toJpaDownloadStatus(domain.getDownloadStatus()),
                domain.getFileAssetId() != null ? domain.getFileAssetId().value() : null,
                domain.getErrorMessage(),
                domain.getRetryCount(),
                domain.getLastErrorMessage(),
                domain.getSentAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt());
    }

    /**
     * Entity를 Domain으로 변환합니다.
     *
     * @param entity WebhookOutboxJpaEntity
     * @return WebhookOutbox Domain
     */
    public WebhookOutbox toDomain(WebhookOutboxJpaEntity entity) {
        return WebhookOutbox.of(
                WebhookOutboxId.of(entity.getId()),
                ExternalDownloadId.of(entity.getExternalDownloadId()),
                WebhookUrl.of(entity.getWebhookUrl()),
                toDomainStatus(entity.getStatus()),
                toDomainDownloadStatus(entity.getDownloadStatus()),
                entity.getFileAssetId() != null ? FileAssetId.of(entity.getFileAssetId()) : null,
                entity.getErrorMessage(),
                entity.getRetryCount(),
                entity.getLastErrorMessage(),
                entity.getSentAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private WebhookOutboxStatusJpa toJpaStatus(WebhookOutboxStatus status) {
        return switch (status) {
            case PENDING -> WebhookOutboxStatusJpa.PENDING;
            case SENT -> WebhookOutboxStatusJpa.SENT;
            case FAILED -> WebhookOutboxStatusJpa.FAILED;
        };
    }

    private WebhookOutboxStatus toDomainStatus(WebhookOutboxStatusJpa status) {
        return switch (status) {
            case PENDING -> WebhookOutboxStatus.PENDING;
            case SENT -> WebhookOutboxStatus.SENT;
            case FAILED -> WebhookOutboxStatus.FAILED;
        };
    }

    private ExternalDownloadStatusJpa toJpaDownloadStatus(ExternalDownloadStatus status) {
        return switch (status) {
            case PENDING -> ExternalDownloadStatusJpa.PENDING;
            case PROCESSING -> ExternalDownloadStatusJpa.PROCESSING;
            case COMPLETED -> ExternalDownloadStatusJpa.COMPLETED;
            case FAILED -> ExternalDownloadStatusJpa.FAILED;
        };
    }

    private ExternalDownloadStatus toDomainDownloadStatus(ExternalDownloadStatusJpa status) {
        return switch (status) {
            case PENDING -> ExternalDownloadStatus.PENDING;
            case PROCESSING -> ExternalDownloadStatus.PROCESSING;
            case COMPLETED -> ExternalDownloadStatus.COMPLETED;
            case FAILED -> ExternalDownloadStatus.FAILED;
        };
    }
}
