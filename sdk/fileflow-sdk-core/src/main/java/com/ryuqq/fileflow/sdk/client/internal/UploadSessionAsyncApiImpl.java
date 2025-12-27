package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.UploadSessionAsyncApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

/** Async implementation of UploadSessionApi using WebClient. */
public final class UploadSessionAsyncApiImpl implements UploadSessionAsyncApi {

    private static final String BASE_PATH = "/api/v1/file/upload-sessions";

    private final HttpClientAsyncSupport httpClient;

    /**
     * Creates a new UploadSessionAsyncApiImpl.
     *
     * @param httpClient the async HTTP client support
     */
    public UploadSessionAsyncApiImpl(HttpClientAsyncSupport httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Mono<InitSingleUploadResponse> initSingle(InitSingleUploadRequest request) {
        return httpClient.post(
                BASE_PATH + "/single/init",
                request,
                new ParameterizedTypeReference<ApiResponse<InitSingleUploadResponse>>() {});
    }

    @Override
    public Mono<Void> completeSingle(String sessionId) {
        return httpClient
                .<Void>post(
                        BASE_PATH + "/" + sessionId + "/complete",
                        new ParameterizedTypeReference<ApiResponse<Void>>() {})
                .then();
    }

    @Override
    public Mono<Void> cancel(String sessionId) {
        return httpClient
                .<Void>post(
                        BASE_PATH + "/" + sessionId + "/cancel",
                        new ParameterizedTypeReference<ApiResponse<Void>>() {})
                .then();
    }
}
