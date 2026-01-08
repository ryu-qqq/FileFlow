package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.ExternalDownloadAsyncApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadSearchRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

/** Async implementation of ExternalDownloadApi using WebClient. */
public final class ExternalDownloadAsyncApiImpl implements ExternalDownloadAsyncApi {

    private static final String BASE_PATH = "/api/v1/file/external-downloads";

    private final HttpClientAsyncSupport httpClient;

    /**
     * Creates a new ExternalDownloadAsyncApiImpl.
     *
     * @param httpClient the async HTTP client support
     */
    public ExternalDownloadAsyncApiImpl(HttpClientAsyncSupport httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Mono<String> request(String idempotencyKey, String sourceUrl, String webhookUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("idempotencyKey", idempotencyKey);
        request.put("sourceUrl", sourceUrl);
        if (webhookUrl != null) {
            request.put("webhookUrl", webhookUrl);
        }

        return httpClient.post(
                BASE_PATH, request, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    @Override
    public Mono<ExternalDownloadDetailResponse> get(String id) {
        return httpClient.get(
                BASE_PATH + "/" + id,
                new ParameterizedTypeReference<ApiResponse<ExternalDownloadDetailResponse>>() {});
    }

    @Override
    public Mono<PageResponse<ExternalDownloadResponse>> list(
            ExternalDownloadSearchRequest request) {
        StringBuilder path = new StringBuilder(BASE_PATH);
        path.append("?page=").append(request.getPage());
        path.append("&size=").append(request.getSize());

        if (request.getStatus() != null) {
            path.append("&status=").append(request.getStatus());
        }

        return httpClient.get(
                path.toString(),
                new ParameterizedTypeReference<
                        ApiResponse<PageResponse<ExternalDownloadResponse>>>() {});
    }
}
