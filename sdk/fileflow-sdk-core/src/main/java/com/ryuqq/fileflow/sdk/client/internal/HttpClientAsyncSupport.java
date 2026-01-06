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
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Internal async HTTP client support using WebClient.
 *
 * <p>Handles authentication, error mapping, and response parsing reactively.
 */
public final class HttpClientAsyncSupport {

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final TokenResolver tokenResolver;
    private final String serviceName;

    /**
     * Creates a new HttpClientAsyncSupport.
     *
     * @param webClient the WebClient for HTTP requests
     * @param objectMapper the ObjectMapper for JSON processing
     * @param config the client configuration
     */
    public HttpClientAsyncSupport(
            WebClient webClient, ObjectMapper objectMapper, FileFlowClientConfig config) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.tokenResolver = config.getTokenResolver();
        this.serviceName = config.getServiceName();
    }

    /**
     * Performs a GET request.
     *
     * @param path the API path
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return Mono emitting the response data
     */
    public <T> Mono<T> get(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return webClient
                .get()
                .uri(path)
                .headers(this::addAuthHeader)
                .exchangeToMono(response -> handleResponse(response, responseType));
    }

    /**
     * Performs a GET request with query parameters.
     *
     * @param path the API path
     * @param queryParams the query parameters
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return Mono emitting the response data
     */
    public <T> Mono<T> get(
            String path,
            Map<String, Object> queryParams,
            ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return webClient
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
                .exchangeToMono(response -> handleResponse(response, responseType));
    }

    /**
     * Performs a POST request.
     *
     * @param path the API path
     * @param body the request body
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return Mono emitting the response data
     */
    public <T> Mono<T> post(
            String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return webClient
                .post()
                .uri(path)
                .headers(this::addAuthHeader)
                .bodyValue(body)
                .exchangeToMono(response -> handleResponse(response, responseType));
    }

    /**
     * Performs a POST request without request body.
     *
     * @param path the API path
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return Mono emitting the response data
     */
    public <T> Mono<T> post(String path, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return webClient
                .post()
                .uri(path)
                .headers(this::addAuthHeader)
                .exchangeToMono(response -> handleResponse(response, responseType));
    }

    /**
     * Performs a PATCH request.
     *
     * @param path the API path
     * @param body the request body (can be null)
     * @param responseType the response type reference
     * @param <T> the response data type
     * @return Mono emitting the response data
     */
    public <T> Mono<T> patch(
            String path, Object body, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        var request = webClient.patch().uri(path).headers(this::addAuthHeader);

        if (body != null) {
            return request.bodyValue(body)
                    .exchangeToMono(response -> handleResponse(response, responseType));
        }

        return request.exchangeToMono(response -> handleResponse(response, responseType));
    }

    /**
     * Performs a DELETE request.
     *
     * @param path the API path
     * @return Mono completing when done
     */
    public Mono<Void> delete(String path) {
        return webClient
                .delete()
                .uri(path)
                .headers(this::addAuthHeader)
                .exchangeToMono(
                        response -> {
                            if (response.statusCode().isError()) {
                                return handleError(response);
                            }
                            return response.releaseBody();
                        });
    }

    private void addAuthHeader(HttpHeaders headers) {
        tokenResolver.resolve().ifPresent(token -> headers.set(SERVICE_TOKEN_HEADER, token));
        if (serviceName != null && !serviceName.isBlank()) {
            headers.set(SERVICE_NAME_HEADER, serviceName);
        }
    }

    private <T> Mono<T> handleResponse(
            ClientResponse response, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        if (response.statusCode().isError()) {
            return handleError(response);
        }

        return response.bodyToMono(responseType)
                .flatMap(
                        apiResponse -> {
                            if (apiResponse == null || apiResponse.getData() == null) {
                                return Mono.empty();
                            }
                            return Mono.just(apiResponse.getData());
                        });
    }

    private <T> Mono<T> handleError(ClientResponse response) {
        HttpStatusCode status = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(
                        body -> {
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

                            return Mono.error(
                                    mapException(status.value(), errorCode, errorMessage));
                        });
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
}
