package com.ryuqq.fileflow.application.asset.manager.command;

import com.ryuqq.fileflow.application.asset.port.out.command.AssetMetadataPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AssetMetadataCommandManager {

    private final AssetMetadataPersistencePort assetMetadataPersistencePort;

    public AssetMetadataCommandManager(AssetMetadataPersistencePort assetMetadataPersistencePort) {
        this.assetMetadataPersistencePort = assetMetadataPersistencePort;
    }

    @Transactional
    public void persist(AssetMetadata assetMetadata) {
        assetMetadataPersistencePort.persist(assetMetadata);
    }
}
