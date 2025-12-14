package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import org.springframework.stereotype.Component;

/**
 * ProcessedFileAsset Domain ↔ JPA Entity 매퍼.
 */
@Component
public class ProcessedFileAssetJpaMapper {

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain ProcessedFileAsset 도메인 객체
     * @return JPA Entity
     */
    public ProcessedFileAssetJpaEntity toEntity(ProcessedFileAsset domain) {
        return ProcessedFileAssetJpaEntity.of(
                domain.getId().value(),
                domain.getOriginalAssetId().value().toString(),
                domain.getParentAssetId() != null
                        ? domain.getParentAssetId().value().toString()
                        : null,
                domain.getVariant().type(),
                domain.getFormat().type(),
                domain.getFileName().name(),
                domain.getFileSize().size(),
                domain.getBucket().bucketName(),
                domain.getS3Key().key(),
                domain.getUserId() != null ? domain.getUserId().value() : null,
                domain.getOrganizationId().value(),
                domain.getTenantId().value(),
                domain.getCreatedAt());
    }
}
