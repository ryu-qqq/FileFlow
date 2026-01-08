package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadSearchRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

/** Implementation of ExternalDownloadApi. */
public final class ExternalDownloadApiImpl implements ExternalDownloadApi {

    private static final String BASE_PATH = "/api/v1/file/external-downloads";

    private final HttpClientSupport httpClient;

    /**
     * Creates a new ExternalDownloadApiImpl.
     *
     * @param httpClient the HTTP client support
     */
    public ExternalDownloadApiImpl(HttpClientSupport httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String request(String idempotencyKey, String sourceUrl, String webhookUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("idempotencyKey", idempotencyKey);
        request.put("sourceUrl", sourceUrl);
        if (webhookUrl != null) {
            request.put("webhookUrl", webhookUrl);
        }

        ExternalDownloadResponse response =
                httpClient.post(
                        BASE_PATH,
                        request,
                        new ParameterizedTypeReference<ApiResponse<ExternalDownloadResponse>>() {});
        return response.getId();
    }

    @Override
    public ExternalDownloadDetailResponse get(String id) {
        return httpClient.get(
                BASE_PATH + "/" + id,
                new ParameterizedTypeReference<ApiResponse<ExternalDownloadDetailResponse>>() {});
    }

    @Override
    public PageResponse<ExternalDownloadResponse> list(ExternalDownloadSearchRequest request) {
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
