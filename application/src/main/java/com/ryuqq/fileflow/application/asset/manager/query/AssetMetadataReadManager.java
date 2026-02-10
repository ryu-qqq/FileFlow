package com.ryuqq.fileflow.application.asset.manager.query;

import com.ryuqq.fileflow.application.asset.port.out.query.AssetMetadataQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.exception.AssetMetadataNotFoundException;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AssetMetadataReadManager {

    private final AssetMetadataQueryPort assetMetadataQueryPort;

    public AssetMetadataReadManager(AssetMetadataQueryPort assetMetadataQueryPort) {
        this.assetMetadataQueryPort = assetMetadataQueryPort;
    }

    @Transactional(readOnly = true)
    public AssetMetadata getAssetMetadata(String assetId) {
        return assetMetadataQueryPort
                .findByAssetId(AssetId.of(assetId))
                .orElseThrow(() -> new AssetMetadataNotFoundException(assetId));
    }
}
