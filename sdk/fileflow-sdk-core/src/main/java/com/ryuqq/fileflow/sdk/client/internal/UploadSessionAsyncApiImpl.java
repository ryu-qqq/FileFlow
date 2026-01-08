package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.UploadSessionAsyncApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.session.CancelUploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.InitMultipartUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitMultipartUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.MarkPartUploadedRequest;
import com.ryuqq.fileflow.sdk.model.session.MarkPartUploadedResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionDetailResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionSearchRequest;
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

    // ==================== Single Upload ====================

    @Override
    public Mono<InitSingleUploadResponse> initSingle(InitSingleUploadRequest request) {
        return httpClient.post(
                BASE_PATH + "/single",
                request,
                new ParameterizedTypeReference<ApiResponse<InitSingleUploadResponse>>() {});
    }

    @Override
    public Mono<Void> completeSingle(String sessionId) {
        return httpClient
                .<Void>patch(
                        BASE_PATH + "/" + sessionId + "/single/complete",
                        null,
                        new ParameterizedTypeReference<ApiResponse<Void>>() {})
                .then();
    }

    // ==================== Multipart Upload ====================

    @Override
    public Mono<InitMultipartUploadResponse> initMultipart(InitMultipartUploadRequest request) {
        return httpClient.post(
                BASE_PATH + "/multipart",
                request,
                new ParameterizedTypeReference<ApiResponse<InitMultipartUploadResponse>>() {});
    }

    @Override
    public Mono<MarkPartUploadedResponse> markPartUploaded(
            String sessionId, MarkPartUploadedRequest request) {
        return httpClient.patch(
                BASE_PATH + "/" + sessionId + "/parts",
                request,
                new ParameterizedTypeReference<ApiResponse<MarkPartUploadedResponse>>() {});
    }

    @Override
    public Mono<CompleteMultipartUploadResponse> completeMultipart(String sessionId) {
        return httpClient.patch(
                BASE_PATH + "/" + sessionId + "/multipart/complete",
                null,
                new ParameterizedTypeReference<ApiResponse<CompleteMultipartUploadResponse>>() {});
    }

    // ==================== Session Management ====================

    @Override
    public Mono<UploadSessionDetailResponse> get(String sessionId) {
        return httpClient.get(
                BASE_PATH + "/" + sessionId,
                new ParameterizedTypeReference<ApiResponse<UploadSessionDetailResponse>>() {});
    }

    @Override
    public Mono<PageResponse<UploadSessionResponse>> list(UploadSessionSearchRequest request) {
        StringBuilder path = new StringBuilder(BASE_PATH);
        path.append("?page=").append(request.getPage());
        path.append("&size=").append(request.getSize());

        if (request.getStatus() != null) {
            path.append("&status=").append(request.getStatus().name());
        }
        if (request.getUploadType() != null) {
            path.append("&uploadType=").append(request.getUploadType().name());
        }

        return httpClient.get(
                path.toString(),
                new ParameterizedTypeReference<
                        ApiResponse<PageResponse<UploadSessionResponse>>>() {});
    }

    @Override
    public Mono<CancelUploadSessionResponse> cancel(String sessionId) {
        return httpClient.patch(
                BASE_PATH + "/" + sessionId + "/cancel",
                null,
                new ParameterizedTypeReference<ApiResponse<CancelUploadSessionResponse>>() {});
    }
}
