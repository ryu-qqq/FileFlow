package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * FileAssetStatusHistory Domain ↔ JPA Entity 매퍼.
 *
 * <p>Append-Only 테이블로 toEntity만 사용합니다.
 */
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
}
