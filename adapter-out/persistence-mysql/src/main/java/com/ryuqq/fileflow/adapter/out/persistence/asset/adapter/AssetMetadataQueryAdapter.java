package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetMetadataJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetMetadataQueryDslRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.AssetMetadataQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AssetMetadataQueryAdapter implements AssetMetadataQueryPort {

    private final AssetMetadataQueryDslRepository queryDslRepository;
    private final AssetMetadataJpaMapper mapper;

    public AssetMetadataQueryAdapter(
            AssetMetadataQueryDslRepository queryDslRepository, AssetMetadataJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AssetMetadata> findByAssetId(AssetId assetId) {
        return queryDslRepository.findByAssetId(assetId.value()).map(mapper::toDomain);
    }
}
