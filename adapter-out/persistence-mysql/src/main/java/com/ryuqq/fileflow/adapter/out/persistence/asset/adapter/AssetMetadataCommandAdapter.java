package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetMetadataJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetMetadataJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.command.AssetMetadataPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import org.springframework.stereotype.Component;

@Component
public class AssetMetadataCommandAdapter implements AssetMetadataPersistencePort {

    private final AssetMetadataJpaRepository jpaRepository;
    private final AssetMetadataJpaMapper mapper;

    public AssetMetadataCommandAdapter(
            AssetMetadataJpaRepository jpaRepository, AssetMetadataJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(AssetMetadata assetMetadata) {
        AssetMetadataJpaEntity entity = mapper.toEntity(assetMetadata);
        jpaRepository.save(entity);
    }
}
