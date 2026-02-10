package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.transform.CreateTransformRequestRequest;
import com.ryuqq.fileflow.sdk.model.transform.TransformRequestResponse;

public interface TransformRequestApi {

    ApiResponse<TransformRequestResponse> create(CreateTransformRequestRequest request);

    ApiResponse<TransformRequestResponse> get(String transformRequestId);
}
