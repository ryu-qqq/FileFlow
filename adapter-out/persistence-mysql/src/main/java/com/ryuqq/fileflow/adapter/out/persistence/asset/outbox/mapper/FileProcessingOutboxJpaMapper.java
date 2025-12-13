package com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.FileProcessingOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity.FileProcessingOutboxJpaEntity.OutboxStatusEnum;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * FileProcessingOutbox JPA Mapper.
 *
 * <p>Domain ↔ Entity 변환을 담당합니다.
 */
@Component
public class FileProcessingOutboxJpaMapper {

    private final ClockHolder clockHolder;

    public FileProcessingOutboxJpaMapper(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * Domain을 Entity로 변환합니다.
     *
     * @param domain FileProcessingOutbox Domain
     * @return FileProcessingOutboxJpaEntity
     */
    public FileProcessingOutboxJpaEntity toEntity(FileProcessingOutbox domain) {
        Instant now = clockHolder.getClock().instant();
        return FileProcessingOutboxJpaEntity.of(
                domain.getId().value(),
                domain.getFileAssetId().getValue(),
                domain.getEventType(),
                domain.getPayload(),
                toEntityStatus(domain.getStatus()),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                domain.getProcessedAt(),
                domain.getCreatedAt() != null ? domain.getCreatedAt() : now,
                now);
    }

    /**
     * Entity를 Domain으로 변환합니다.
     *
     * @param entity FileProcessingOutboxJpaEntity
     * @return FileProcessingOutbox Domain
     */
    public FileProcessingOutbox toDomain(FileProcessingOutboxJpaEntity entity) {
        return FileProcessingOutbox.reconstitute(
                FileProcessingOutboxId.of(entity.getId()),
                FileAssetId.of(entity.getFileAssetId()),
                entity.getEventType(),
                entity.getPayload(),
                toDomainStatus(entity.getStatus()),
                entity.getRetryCount(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }

    /**
     * Domain 상태를 Entity 상태로 변환합니다.
     *
     * @param domainStatus Domain 상태
     * @return Entity 상태
     */
    private OutboxStatusEnum toEntityStatus(OutboxStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> OutboxStatusEnum.PENDING;
            case SENT -> OutboxStatusEnum.SENT;
            case FAILED -> OutboxStatusEnum.FAILED;
        };
    }

    /**
     * Entity 상태를 Domain 상태로 변환합니다.
     *
     * @param entityStatus Entity 상태
     * @return Domain 상태
     */
    private OutboxStatus toDomainStatus(OutboxStatusEnum entityStatus) {
        return switch (entityStatus) {
            case PENDING -> OutboxStatus.PENDING;
            case SENT -> OutboxStatus.SENT;
            case FAILED -> OutboxStatus.FAILED;
        };
    }
}
