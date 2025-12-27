package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.sdk.auth.TokenResolver;
import com.ryuqq.fileflow.sdk.config.FileFlowClientConfig;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowException;
import com.ryuqq.fileflow.sdk.exception.FileFlowForbiddenException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.exception.FileFlowServerException;
import com.ryuqq.fileflow.sdk.exception.FileFlowUnauthorizedException;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

/**
 * Internal HTTP client support for making API requests.
 *
 * <p>Handles authentication, error mapping, and response parsing.
 */
public final class HttpClientSupport {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TokenResolver tokenResolver;

    /**
     * Creates a new HttpClientSupport.
     *
     * @param restClient the RestClient for HTTP requests
     * @param objectMapper the ObjectMapper for JSON processing
     * @param config the client configuration
     */
    public HttpClientSupport(
            RestClient restClient, ObjectMapper objectMapper, FileFlowClientConfig config) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.tokenResolver = config.getTokenResolver();
    }

    /**
     * Performs a GET request.
     *
     * @param path the API path
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return the response data
     */
    public <T> T get(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        ApiResponse<T> response =
                restClient
                        .get()
                        .uri(path)
                        .headers(this::addAuthHeader)
                        .retrieve()
                        .onStatus(this::isError, this::handleError)
                        .body(responseType);

        return extractData(response);
    }

    /**
     * Performs a GET request with query parameters.
     *
     * @param path the API path
     * @param queryParams the query parameters
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return the response data
     */
    public <T> T get(
            String path,
            Map<String, Object> queryParams,
            ParameterizedTypeReference<ApiResponse<T>> responseType) {
        ApiResponse<T> response =
                restClient
                        .get()
                        .uri(
                                uriBuilder -> {
                                    var builder = uriBuilder.path(path);
                                    queryParams.forEach(
                                            (key, value) -> {
                                                if (value != null) {
                                                    builder.queryParam(key, value);
                                                }
                                            });
                                    return builder.build();
                                })
                        .headers(this::addAuthHeader)
                        .retrieve()
                        .onStatus(this::isError, this::handleError)
                        .body(responseType);

        return extractData(response);
    }

    /**
     * Performs a POST request.
     *
     * @param path the API path
     * @param body the request body
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return the response data
     */
    public <T> T post(
            String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        ApiResponse<T> response =
                restClient
                        .post()
                        .uri(path)
                        .headers(this::addAuthHeader)
                        .body(body)
                        .retrieve()
                        .onStatus(this::isError, this::handleError)
                        .body(responseType);

        return extractData(response);
    }

    /**
     * Performs a POST request without request body.
     *
     * @param path the API path
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return the response data
     */
    public <T> T post(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        ApiResponse<T> response =
                restClient
                        .post()
                        .uri(path)
                        .headers(this::addAuthHeader)
                        .retrieve()
                        .onStatus(this::isError, this::handleError)
                        .body(responseType);

        return extractData(response);
    }

    /**
     * Performs a PATCH request.
     *
     * @param path the API path
     * @param body the request body (can be null)
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return the response data
     */
    public <T> T patch(
            String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        var request = restClient.patch().uri(path).headers(this::addAuthHeader);

        if (body != null) {
            request.body(body);
        }

        ApiResponse<T> response =
                request.retrieve().onStatus(this::isError, this::handleError).body(responseType);

        return extractData(response);
    }

    /**
     * Performs a DELETE request.
     *
     * @param path the API path
     */
    public void delete(String path) {
        restClient
                .delete()
                .uri(path)
                .headers(this::addAuthHeader)
                .retrieve()
                .onStatus(this::isError, this::handleError)
                .toBodilessEntity();
    }

    private void addAuthHeader(HttpHeaders headers) {
        tokenResolver
                .resolve()
                .ifPresent(
                        token -> {
                            String authValue =
                                    token.startsWith(BEARER_PREFIX) ? token : BEARER_PREFIX + token;
                            headers.set(AUTHORIZATION_HEADER, authValue);
                        });
    }

    private boolean isError(HttpStatusCode status) {
        return status.isError();
    }

    private void handleError(HttpRequest request, ClientHttpResponse response) {
        int statusCode;
        String body = "";

        try {
            statusCode = response.getStatusCode().value();
            body = new String(response.getBody().readAllBytes());
        } catch (IOException e) {
            throw new FileFlowException(
                    500, "IO_ERROR", "Failed to read error response: " + e.getMessage());
        }

        String errorCode = "UNKNOWN";
        String errorMessage = "An error occurred";

        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("detail")) {
                errorMessage = node.get("detail").asText();
            }
            if (node.has("title")) {
                errorCode = node.get("title").asText();
            }
        } catch (JsonProcessingException ignored) {
            errorMessage = body.isEmpty() ? "Unknown error" : body;
        }

        throw mapException(statusCode, errorCode, errorMessage);
    }

    private FileFlowException mapException(int statusCode, String errorCode, String errorMessage) {
        return switch (statusCode) {
            case 400 -> new FileFlowBadRequestException(errorCode, errorMessage);
            case 401 -> new FileFlowUnauthorizedException(errorCode, errorMessage);
            case 403 -> new FileFlowForbiddenException(errorCode, errorMessage);
            case 404 -> new FileFlowNotFoundException(errorCode, errorMessage);
            default -> {
                if (statusCode >= 500) {
                    yield new FileFlowServerException(statusCode, errorCode, errorMessage);
                }
                yield new FileFlowException(statusCode, errorCode, errorMessage);
            }
        };
    }

    private <T> T extractData(ApiResponse<T> response) {
        if (response == null) {
            return null;
        }
        return response.getData();
    }
}
