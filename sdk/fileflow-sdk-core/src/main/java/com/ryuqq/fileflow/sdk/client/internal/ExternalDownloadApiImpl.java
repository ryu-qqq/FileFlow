package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
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

        return httpClient.post(
                BASE_PATH, request, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
