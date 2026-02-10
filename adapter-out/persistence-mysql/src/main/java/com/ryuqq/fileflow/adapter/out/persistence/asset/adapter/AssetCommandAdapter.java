package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetJpaRepository;
import com.ryuqq.fileflow.application.asset.port.out.command.AssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import org.springframework.stereotype.Component;

@Component
public class AssetCommandAdapter implements AssetPersistencePort {

    private final AssetJpaRepository jpaRepository;
    private final AssetJpaMapper mapper;

    public AssetCommandAdapter(AssetJpaRepository jpaRepository, AssetJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(Asset asset) {
        AssetJpaEntity entity = mapper.toEntity(asset);
        jpaRepository.save(entity);
    }
}
