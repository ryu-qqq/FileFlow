package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.UploadSessionApi;
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

    // ==================== Single Upload ====================

    @Override
    public InitSingleUploadResponse initSingle(InitSingleUploadRequest request) {
        return httpClient.post(
                BASE_PATH + "/single",
                request,
                new ParameterizedTypeReference<ApiResponse<InitSingleUploadResponse>>() {});
    }

    @Override
    public void completeSingle(String sessionId) {
        httpClient.patch(
                BASE_PATH + "/" + sessionId + "/single/complete",
                null,
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }

    // ==================== Multipart Upload ====================

    @Override
    public InitMultipartUploadResponse initMultipart(InitMultipartUploadRequest request) {
        return httpClient.post(
                BASE_PATH + "/multipart",
                request,
                new ParameterizedTypeReference<ApiResponse<InitMultipartUploadResponse>>() {});
    }

    @Override
    public MarkPartUploadedResponse markPartUploaded(
            String sessionId, MarkPartUploadedRequest request) {
        return httpClient.patch(
                BASE_PATH + "/" + sessionId + "/parts",
                request,
                new ParameterizedTypeReference<ApiResponse<MarkPartUploadedResponse>>() {});
    }

    @Override
    public CompleteMultipartUploadResponse completeMultipart(String sessionId) {
        return httpClient.patch(
                BASE_PATH + "/" + sessionId + "/multipart/complete",
                null,
                new ParameterizedTypeReference<ApiResponse<CompleteMultipartUploadResponse>>() {});
    }

    // ==================== Session Management ====================

    @Override
    public UploadSessionDetailResponse get(String sessionId) {
        return httpClient.get(
                BASE_PATH + "/" + sessionId,
                new ParameterizedTypeReference<ApiResponse<UploadSessionDetailResponse>>() {});
    }

    @Override
    public PageResponse<UploadSessionResponse> list(UploadSessionSearchRequest request) {
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
    public CancelUploadSessionResponse cancel(String sessionId) {
        return httpClient.patch(
                BASE_PATH + "/" + sessionId + "/cancel",
                null,
                new ParameterizedTypeReference<ApiResponse<CancelUploadSessionResponse>>() {});
    }
}
