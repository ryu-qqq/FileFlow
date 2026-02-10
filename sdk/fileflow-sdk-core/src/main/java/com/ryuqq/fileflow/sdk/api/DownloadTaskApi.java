package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.download.CreateDownloadTaskRequest;
import com.ryuqq.fileflow.sdk.model.download.DownloadTaskResponse;

public interface DownloadTaskApi {

    ApiResponse<DownloadTaskResponse> create(CreateDownloadTaskRequest request);

    ApiResponse<DownloadTaskResponse> get(String downloadTaskId);
}
