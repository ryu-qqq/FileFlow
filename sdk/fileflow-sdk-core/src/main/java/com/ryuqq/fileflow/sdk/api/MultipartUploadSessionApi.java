package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.AddCompletedPartRequest;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.PresignedPartUrlResponse;

public interface MultipartUploadSessionApi {

    ApiResponse<MultipartUploadSessionResponse> create(CreateMultipartUploadSessionRequest request);

    ApiResponse<MultipartUploadSessionResponse> get(String sessionId);

    ApiResponse<PresignedPartUrlResponse> getPresignedPartUrl(String sessionId, int partNumber);

    void addCompletedPart(String sessionId, AddCompletedPartRequest request);

    void complete(String sessionId, CompleteMultipartUploadSessionRequest request);

    void abort(String sessionId);
}
