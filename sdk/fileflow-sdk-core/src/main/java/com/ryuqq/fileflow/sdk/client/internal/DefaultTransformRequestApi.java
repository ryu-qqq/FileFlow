package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryuqq.fileflow.sdk.api.TransformRequestApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.transform.CreateTransformRequestRequest;
import com.ryuqq.fileflow.sdk.model.transform.TransformRequestResponse;

class DefaultTransformRequestApi implements TransformRequestApi {

    private static final String BASE_PATH = "/api/v1/transform-requests";
    private static final TypeReference<ApiResponse<TransformRequestResponse>> RESPONSE_TYPE =
            new TypeReference<>() {};

    private final HttpClientSupport http;

    DefaultTransformRequestApi(HttpClientSupport http) {
        this.http = http;
    }

    @Override
    public ApiResponse<TransformRequestResponse> create(CreateTransformRequestRequest request) {
        return http.post(BASE_PATH, request, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<TransformRequestResponse> get(String transformRequestId) {
        return http.get(BASE_PATH + "/" + transformRequestId, RESPONSE_TYPE);
    }
}
