package com.ryuqq.fileflow.adapter.out.persistence.transform.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import org.springframework.stereotype.Component;

@Component
public class TransformRequestJpaMapper {

    public TransformRequestJpaEntity toEntity(TransformRequest domain) {
        return TransformRequestJpaEntity.create(
                domain.idValue(),
                domain.sourceAssetIdValue(),
                domain.sourceContentType(),
                domain.type(),
                domain.status(),
                domain.resultAssetIdValue(),
                domain.lastError(),
                domain.params().width(),
                domain.params().height(),
                domain.params().maintainAspectRatio(),
                domain.params().targetFormat(),
                domain.params().quality(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.completedAt());
    }

    public TransformRequest toDomain(TransformRequestJpaEntity entity) {
        return TransformRequest.reconstitute(
                TransformRequestId.of(entity.getId()),
                AssetId.of(entity.getSourceAssetId()),
                entity.getSourceContentType(),
                entity.getType(),
                new TransformParams(
                        entity.getWidth(),
                        entity.getHeight(),
                        entity.isMaintainAspectRatio(),
                        entity.getTargetFormat(),
                        entity.getQuality()),
                entity.getStatus(),
                entity.getResultAssetId() != null ? AssetId.of(entity.getResultAssetId()) : null,
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt());
    }
}
