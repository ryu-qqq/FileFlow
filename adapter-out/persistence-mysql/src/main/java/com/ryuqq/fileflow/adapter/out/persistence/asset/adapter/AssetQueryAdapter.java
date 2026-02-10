package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetQueryDslRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.AssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AssetQueryAdapter implements AssetQueryPort {

    private final AssetQueryDslRepository queryDslRepository;
    private final AssetJpaMapper mapper;

    public AssetQueryAdapter(AssetQueryDslRepository queryDslRepository, AssetJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Asset> findById(AssetId assetId) {
        return queryDslRepository.findById(assetId.value()).map(mapper::toDomain);
    }
}
