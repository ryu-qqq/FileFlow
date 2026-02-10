package com.ryuqq.fileflow.application.asset.manager.command;

import com.ryuqq.fileflow.application.asset.port.out.command.AssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AssetCommandManager {

    private final AssetPersistencePort assetPersistencePort;

    public AssetCommandManager(AssetPersistencePort assetPersistencePort) {
        this.assetPersistencePort = assetPersistencePort;
    }

    @Transactional
    public void persist(Asset asset) {
        assetPersistencePort.persist(asset);
    }
}
