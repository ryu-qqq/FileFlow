package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.asset.AssetMetadataResponse;
import com.ryuqq.fileflow.sdk.model.asset.AssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.RegisterAssetRequest;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;

public interface AssetApi {

    ApiResponse<AssetResponse> register(RegisterAssetRequest request);

    ApiResponse<AssetResponse> get(String assetId);

    ApiResponse<AssetMetadataResponse> getMetadata(String assetId);

    void delete(String assetId, String source);
}
