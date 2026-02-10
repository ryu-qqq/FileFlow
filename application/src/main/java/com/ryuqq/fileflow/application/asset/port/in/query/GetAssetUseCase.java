package com.ryuqq.fileflow.application.asset.port.in.query;

import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;

public interface GetAssetUseCase {

    AssetResponse execute(String assetId);
}
