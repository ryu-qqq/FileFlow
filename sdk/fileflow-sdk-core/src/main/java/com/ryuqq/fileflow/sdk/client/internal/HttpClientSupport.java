package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.fileflow.sdk.config.FileFlowConfig;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowConflictException;
import com.ryuqq.fileflow.sdk.exception.FileFlowException;
import com.ryuqq.fileflow.sdk.exception.FileFlowForbiddenException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.exception.FileFlowServerException;
import com.ryuqq.fileflow.sdk.exception.FileFlowUnauthorizedException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
class HttpClientSupport {

    private static final Logger log = LoggerFactory.getLogger(HttpClientSupport.class);
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final FileFlowConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    HttpClientSupport(FileFlowConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().connectTimeout(config.connectTimeout()).build();
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public <T> T get(String path, Class<T> responseType) {
        return get(path, Map.of(), responseType);
    }

    public <T> T get(String path, Map<String, Object> queryParams, Class<T> responseType) {
        String url = buildUrl(path, queryParams);
        HttpRequest request = newRequestBuilder(url).GET().build();
        return execute(request, responseType);
    }

    public <T> T get(String path, TypeReference<T> typeReference) {
        return get(path, Map.of(), typeReference);
    }

    public <T> T get(String path, Map<String, Object> queryParams, TypeReference<T> typeReference) {
        String url = buildUrl(path, queryParams);
        HttpRequest request = newRequestBuilder(url).GET().build();
        return executeWithTypeReference(request, typeReference);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        String url = buildUrl(path, Map.of());
        HttpRequest request =
                newRequestBuilder(url)
                        .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                        .build();
        return execute(request, responseType);
    }

    public <T> T post(String path, Object body, TypeReference<T> typeReference) {
        String url = buildUrl(path, Map.of());
        HttpRequest request =
                newRequestBuilder(url)
                        .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                        .build();
        return executeWithTypeReference(request, typeReference);
    }

    public void postVoid(String path, Object body) {
        String url = buildUrl(path, Map.of());
        HttpRequest request =
                newRequestBuilder(url)
                        .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                        .build();
        executeVoid(request);
    }

    public void postVoid(String path) {
        String url = buildUrl(path, Map.of());
        HttpRequest request =
                newRequestBuilder(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        executeVoid(request);
    }

    public void delete(String path) {
        String url = buildUrl(path, Map.of());
        HttpRequest request = newRequestBuilder(url).DELETE().build();
        executeVoid(request);
    }

    public void delete(String path, Map<String, Object> queryParams) {
        String url = buildUrl(path, queryParams);
        HttpRequest request = newRequestBuilder(url).DELETE().build();
        executeVoid(request);
    }

    private HttpRequest.Builder newRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                .header(SERVICE_NAME_HEADER, config.serviceName())
                .header(SERVICE_TOKEN_HEADER, config.serviceToken())
                .timeout(config.readTimeout());
    }

    private <T> T execute(HttpRequest request, Class<T> responseType) {
        try {
            log.debug("Executing {} {}", request.method(), request.uri());
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleErrorResponse(response);
            return fromJson(response.body(), responseType);
        } catch (FileFlowException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileFlowServerException(
                    500, "REQUEST_INTERRUPTED", "Request was interrupted", e);
        } catch (IOException e) {
            throw new FileFlowServerException(
                    500, "CONNECTION_ERROR", "Failed to connect to FileFlow server", e);
        }
    }

    private <T> T executeWithTypeReference(HttpRequest request, TypeReference<T> typeReference) {
        try {
            log.debug("Executing {} {}", request.method(), request.uri());
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleErrorResponse(response);
            return fromJson(response.body(), typeReference);
        } catch (FileFlowException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileFlowServerException(
                    500, "REQUEST_INTERRUPTED", "Request was interrupted", e);
        } catch (IOException e) {
            throw new FileFlowServerException(
                    500, "CONNECTION_ERROR", "Failed to connect to FileFlow server", e);
        }
    }

    private void executeVoid(HttpRequest request) {
        try {
            log.debug("Executing {} {}", request.method(), request.uri());
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleErrorResponse(response);
        } catch (FileFlowException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileFlowServerException(
                    500, "REQUEST_INTERRUPTED", "Request was interrupted", e);
        } catch (IOException e) {
            throw new FileFlowServerException(
                    500, "CONNECTION_ERROR", "Failed to connect to FileFlow server", e);
        }
    }

    private void handleErrorResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        ErrorResponse error = parseErrorResponse(response.body());

        switch (statusCode) {
            case 400 -> throw new FileFlowBadRequestException(error.errorCode(), error.message());
            case 401 -> throw new FileFlowUnauthorizedException(error.errorCode(), error.message());
            case 403 -> throw new FileFlowForbiddenException(error.errorCode(), error.message());
            case 404 -> throw new FileFlowNotFoundException(error.errorCode(), error.message());
            case 409 -> throw new FileFlowConflictException(error.errorCode(), error.message());
            default -> {
                if (statusCode >= 500) {
                    throw new FileFlowServerException(
                            statusCode, error.errorCode(), error.message());
                }
                throw new FileFlowException(statusCode, error.errorCode(), error.message());
            }
        }
    }

    private ErrorResponse parseErrorResponse(String body) {
        try {
            ProblemDetailResponse problemDetail =
                    objectMapper.readValue(body, ProblemDetailResponse.class);
            return problemDetail.toErrorResponse();
        } catch (JsonProcessingException e) {
            return new ErrorResponse("UNKNOWN_ERROR", body);
        }
    }

    private String buildUrl(String path, Map<String, Object> queryParams) {
        StringBuilder url = new StringBuilder(config.baseUrl());
        if (!path.startsWith("/")) {
            url.append("/");
        }
        url.append(path);

        if (!queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach(
                    (key, value) -> {
                        if (value != null) {
                            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                            String encodedValue =
                                    URLEncoder.encode(
                                            String.valueOf(value), StandardCharsets.UTF_8);
                            url.append(encodedKey).append("=").append(encodedValue).append("&");
                        }
                    });
            url.setLength(url.length() - 1);
        }

        return url.toString();
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new FileFlowBadRequestException(
                    "SERIALIZATION_ERROR", "Failed to serialize request body", e);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new FileFlowServerException(
                    500, "DESERIALIZATION_ERROR", "Failed to parse response", e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new FileFlowServerException(
                    500, "DESERIALIZATION_ERROR", "Failed to parse response", e);
        }
    }

    private record ErrorResponse(String errorCode, String message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ProblemDetailResponse(
            String type,
            String title,
            Integer status,
            String detail,
            String instance,
            String timestamp,
            String code,
            Object errors) {

        ErrorResponse toErrorResponse() {
            String resolvedErrorCode = resolveErrorCode();
            String resolvedMessage = resolveMessage();
            return new ErrorResponse(resolvedErrorCode, resolvedMessage);
        }

        private String resolveErrorCode() {
            if (code != null && !code.isBlank()) {
                return code;
            }
            if (title != null && !title.isBlank()) {
                return title.toUpperCase().replace(" ", "_");
            }
            return "UNKNOWN_ERROR";
        }

        private String resolveMessage() {
            if (detail != null && !detail.isBlank()) {
                return detail;
            }
            if (title != null && !title.isBlank()) {
                return title;
            }
            return "An unknown error occurred";
        }
    }
}
