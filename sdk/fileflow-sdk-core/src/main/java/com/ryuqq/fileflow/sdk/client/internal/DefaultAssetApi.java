package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryuqq.fileflow.sdk.api.AssetApi;
import com.ryuqq.fileflow.sdk.model.asset.AssetMetadataResponse;
import com.ryuqq.fileflow.sdk.model.asset.AssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.RegisterAssetRequest;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import java.util.Map;

class DefaultAssetApi implements AssetApi {

    private static final String BASE_PATH = "/api/v1/assets";
    private static final TypeReference<ApiResponse<AssetResponse>> RESPONSE_TYPE =
            new TypeReference<>() {};
    private static final TypeReference<ApiResponse<AssetMetadataResponse>> METADATA_RESPONSE_TYPE =
            new TypeReference<>() {};

    private final HttpClientSupport http;

    DefaultAssetApi(HttpClientSupport http) {
        this.http = http;
    }

    @Override
    public ApiResponse<AssetResponse> register(RegisterAssetRequest request) {
        return http.post(BASE_PATH + "/register", request, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<AssetResponse> get(String assetId) {
        return http.get(BASE_PATH + "/" + assetId, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<AssetMetadataResponse> getMetadata(String assetId) {
        return http.get(BASE_PATH + "/" + assetId + "/metadata", METADATA_RESPONSE_TYPE);
    }

    @Override
    public void delete(String assetId, String source) {
        http.delete(BASE_PATH + "/" + assetId, Map.of("source", source));
    }
}
