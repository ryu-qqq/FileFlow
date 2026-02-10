package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryuqq.fileflow.sdk.api.SingleUploadSessionApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.SingleUploadSessionResponse;

class DefaultSingleUploadSessionApi implements SingleUploadSessionApi {

    private static final String BASE_PATH = "/api/v1/sessions/single";
    private static final TypeReference<ApiResponse<SingleUploadSessionResponse>> RESPONSE_TYPE =
            new TypeReference<>() {};

    private final HttpClientSupport http;

    DefaultSingleUploadSessionApi(HttpClientSupport http) {
        this.http = http;
    }

    @Override
    public ApiResponse<SingleUploadSessionResponse> create(
            CreateSingleUploadSessionRequest request) {
        return http.post(BASE_PATH, request, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<SingleUploadSessionResponse> get(String sessionId) {
        return http.get(BASE_PATH + "/" + sessionId, RESPONSE_TYPE);
    }

    @Override
    public void complete(String sessionId, CompleteSingleUploadSessionRequest request) {
        http.postVoid(BASE_PATH + "/" + sessionId + "/complete", request);
    }
}
