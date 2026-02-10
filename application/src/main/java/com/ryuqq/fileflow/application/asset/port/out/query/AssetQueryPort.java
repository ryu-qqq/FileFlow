package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import java.util.Optional;

public interface AssetQueryPort {

    Optional<Asset> findById(AssetId assetId);
}
