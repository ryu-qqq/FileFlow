package com.ryuqq.fileflow.sdk;

import com.ryuqq.fileflow.sdk.api.AssetApi;
import com.ryuqq.fileflow.sdk.api.DownloadTaskApi;
import com.ryuqq.fileflow.sdk.api.MultipartUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.SingleUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.TransformRequestApi;

public interface FileFlowClient {

    SingleUploadSessionApi singleUploadSession();

    MultipartUploadSessionApi multipartUploadSession();

    AssetApi asset();

    DownloadTaskApi downloadTask();

    TransformRequestApi transformRequest();

    static FileFlowClientBuilder builder() {
        return new FileFlowClientBuilder();
    }
}
