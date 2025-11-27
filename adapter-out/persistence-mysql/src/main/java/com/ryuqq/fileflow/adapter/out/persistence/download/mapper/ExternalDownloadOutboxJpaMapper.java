package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * ExternalDownloadOutbox JPA Mapper.
 *
 * <p>Domain ↔ Entity 변환을 담당합니다.
 */
@Component
public class ExternalDownloadOutboxJpaMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    /**
     * Domain을 Entity로 변환합니다.
     *
     * @param domain ExternalDownloadOutbox Domain
     * @return ExternalDownloadOutboxJpaEntity
     */
    public ExternalDownloadOutboxJpaEntity toEntity(ExternalDownloadOutbox domain) {
        return ExternalDownloadOutboxJpaEntity.of(
                domain.getId().isNew() ? null : domain.getId().value(),
                domain.getExternalDownloadId().value(),
                domain.isPublished(),
                toLocalDateTime(domain.getPublishedAt()),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getCreatedAt()) // updatedAt으로 createdAt 사용 (Outbox는 수정 없음)
                );
    }

    /**
     * Entity를 Domain으로 변환합니다.
     *
     * @param entity ExternalDownloadOutboxJpaEntity
     * @return ExternalDownloadOutbox Domain
     */
    public ExternalDownloadOutbox toDomain(ExternalDownloadOutboxJpaEntity entity) {
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.of(entity.getId()),
                ExternalDownloadId.of(entity.getExternalDownloadId()),
                entity.getPublished(),
                toInstant(entity.getPublishedAt()),
                toInstant(entity.getCreatedAt()));
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
