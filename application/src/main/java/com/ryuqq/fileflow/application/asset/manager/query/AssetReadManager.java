package com.ryuqq.fileflow.application.asset.manager.query;

import com.ryuqq.fileflow.application.asset.port.out.query.AssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.exception.AssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AssetReadManager {

    private final AssetQueryPort assetQueryPort;

    public AssetReadManager(AssetQueryPort assetQueryPort) {
        this.assetQueryPort = assetQueryPort;
    }

    @Transactional(readOnly = true)
    public Asset getAsset(String assetId) {
        return assetQueryPort
                .findById(AssetId.of(assetId))
                .orElseThrow(() -> new AssetNotFoundException(assetId));
    }
}
