package com.ryuqq.fileflow.sdk.client.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryuqq.fileflow.sdk.api.MultipartUploadSessionApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.AddCompletedPartRequest;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.PresignedPartUrlResponse;

class DefaultMultipartUploadSessionApi implements MultipartUploadSessionApi {

    private static final String BASE_PATH = "/api/v1/sessions/multipart";
    private static final TypeReference<ApiResponse<MultipartUploadSessionResponse>> RESPONSE_TYPE =
            new TypeReference<>() {};
    private static final TypeReference<ApiResponse<PresignedPartUrlResponse>>
            PRESIGNED_URL_RESPONSE_TYPE = new TypeReference<>() {};

    private final HttpClientSupport http;

    DefaultMultipartUploadSessionApi(HttpClientSupport http) {
        this.http = http;
    }

    @Override
    public ApiResponse<MultipartUploadSessionResponse> create(
            CreateMultipartUploadSessionRequest request) {
        return http.post(BASE_PATH, request, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<MultipartUploadSessionResponse> get(String sessionId) {
        return http.get(BASE_PATH + "/" + sessionId, RESPONSE_TYPE);
    }

    @Override
    public ApiResponse<PresignedPartUrlResponse> getPresignedPartUrl(
            String sessionId, int partNumber) {
        String path = BASE_PATH + "/" + sessionId + "/parts/" + partNumber + "/presigned-url";
        return http.get(path, PRESIGNED_URL_RESPONSE_TYPE);
    }

    @Override
    public void addCompletedPart(String sessionId, AddCompletedPartRequest request) {
        http.postVoid(BASE_PATH + "/" + sessionId + "/parts", request);
    }

    @Override
    public void complete(String sessionId, CompleteMultipartUploadSessionRequest request) {
        http.postVoid(BASE_PATH + "/" + sessionId + "/complete", request);
    }

    @Override
    public void abort(String sessionId) {
        http.postVoid(BASE_PATH + "/" + sessionId + "/abort");
    }
}
