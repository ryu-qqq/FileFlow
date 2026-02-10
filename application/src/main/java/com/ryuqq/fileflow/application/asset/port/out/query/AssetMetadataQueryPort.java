package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import java.util.Optional;

public interface AssetMetadataQueryPort {

    Optional<AssetMetadata> findByAssetId(AssetId assetId);
}
