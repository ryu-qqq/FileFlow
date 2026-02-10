package com.ryuqq.fileflow.application.asset.port.in.query;

import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;

public interface GetAssetMetadataUseCase {

    AssetMetadataResponse execute(String assetId);
}
