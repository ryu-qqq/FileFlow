package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryuqq.fileflow.sdk.api.DownloadTaskApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.download.CreateDownloadTaskRequest;
import com.ryuqq.fileflow.sdk.model.download.DownloadTaskResponse;

class DefaultDownloadTaskApi implements DownloadTaskApi {

    private static final String BASE_PATH = "/api/v1/download-tasks";
    private static final TypeReference<ApiResponse<DownloadTaskResponse>> RESPONSE_TYPE =
            new TypeReference<>() {};

    private final HttpClientSupport http;

    DefaultDownloadTaskApi(HttpClientSupport http) {
        this.http = http;
    }

    @Override
    public ApiResponse<DownloadTaskResponse> create(CreateDownloadTaskRequest request) {
        return http.post(BASE_PATH, request, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<DownloadTaskResponse> get(String downloadTaskId) {
        return http.get(BASE_PATH + "/" + downloadTaskId, RESPONSE_TYPE);
    }
}
