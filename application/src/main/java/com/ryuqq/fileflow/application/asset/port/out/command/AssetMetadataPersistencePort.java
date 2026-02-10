package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;

public interface AssetMetadataPersistencePort {

    void persist(AssetMetadata assetMetadata);
}
