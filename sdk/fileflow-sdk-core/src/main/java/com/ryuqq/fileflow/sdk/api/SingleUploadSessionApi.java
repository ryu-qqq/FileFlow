package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.SingleUploadSessionResponse;

public interface SingleUploadSessionApi {

    ApiResponse<SingleUploadSessionResponse> create(CreateSingleUploadSessionRequest request);

    ApiResponse<SingleUploadSessionResponse> get(String sessionId);

    void complete(String sessionId, CompleteSingleUploadSessionRequest request);
}
