package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.UploadSessionApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import org.springframework.core.ParameterizedTypeReference;

/** Implementation of UploadSessionApi. */
public final class UploadSessionApiImpl implements UploadSessionApi {

    private static final String BASE_PATH = "/api/v1/file/upload-sessions";

    private final HttpClientSupport httpClient;

    /**
     * Creates a new UploadSessionApiImpl.
     *
     * @param httpClient the HTTP client support
     */
    public UploadSessionApiImpl(HttpClientSupport httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public InitSingleUploadResponse initSingle(InitSingleUploadRequest request) {
        return httpClient.post(
                BASE_PATH + "/single/init",
                request,
                new ParameterizedTypeReference<ApiResponse<InitSingleUploadResponse>>() {});
    }

    @Override
    public void completeSingle(String sessionId) {
        httpClient.post(
                BASE_PATH + "/" + sessionId + "/complete",
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }

    @Override
    public void cancel(String sessionId) {
        httpClient.post(
                BASE_PATH + "/" + sessionId + "/cancel",
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }
}
