package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** FileAssetStatusHistory Domain ↔ JPA Entity 매퍼. */
@Component
public class FileAssetStatusHistoryJpaMapper {

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain FileAssetStatusHistory 도메인 객체
     * @return JPA Entity
     */
    public FileAssetStatusHistoryJpaEntity toEntity(FileAssetStatusHistory domain) {
        return FileAssetStatusHistoryJpaEntity.of(
                domain.getId().value(),
                domain.getFileAssetId().value().toString(),
                domain.getFromStatus(),
                domain.getToStatus(),
                domain.getMessage(),
                domain.getActor(),
                domain.getActorType(),
                domain.getChangedAt(),
                domain.getDurationMillis(),
                Instant.now());
    }

    /**
     * JPA Entity → Domain 변환.
     *
     * @param entity FileAssetStatusHistoryJpaEntity
     * @return FileAssetStatusHistory 도메인 객체
     */
    public FileAssetStatusHistory toDomain(FileAssetStatusHistoryJpaEntity entity) {
        return FileAssetStatusHistory.reconstitute(
                new FileAssetStatusHistoryId(entity.getId()),
                new FileAssetId(UUID.fromString(entity.getFileAssetId())),
                entity.getFromStatus(),
                entity.getToStatus(),
                entity.getMessage(),
                entity.getActor(),
                entity.getActorType(),
                entity.getChangedAt(),
                entity.getDurationMillis());
    }
}
