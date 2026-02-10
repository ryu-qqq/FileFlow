package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.FileFlowClient;
import com.ryuqq.fileflow.sdk.api.AssetApi;
import com.ryuqq.fileflow.sdk.api.DownloadTaskApi;
import com.ryuqq.fileflow.sdk.api.MultipartUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.SingleUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.TransformRequestApi;
import com.ryuqq.fileflow.sdk.config.FileFlowConfig;

public class DefaultFileFlowClient implements FileFlowClient {

    private final SingleUploadSessionApi singleUploadSessionApi;
    private final MultipartUploadSessionApi multipartUploadSessionApi;
    private final AssetApi assetApi;
    private final DownloadTaskApi downloadTaskApi;
    private final TransformRequestApi transformRequestApi;

    public DefaultFileFlowClient(FileFlowConfig config) {
        HttpClientSupport http = new HttpClientSupport(config);
        this.singleUploadSessionApi = new DefaultSingleUploadSessionApi(http);
        this.multipartUploadSessionApi = new DefaultMultipartUploadSessionApi(http);
        this.assetApi = new DefaultAssetApi(http);
        this.downloadTaskApi = new DefaultDownloadTaskApi(http);
        this.transformRequestApi = new DefaultTransformRequestApi(http);
    }

    @Override
    public SingleUploadSessionApi singleUploadSession() {
        return singleUploadSessionApi;
    }

    @Override
    public MultipartUploadSessionApi multipartUploadSession() {
        return multipartUploadSessionApi;
    }

    @Override
    public AssetApi asset() {
        return assetApi;
    }

    @Override
    public DownloadTaskApi downloadTask() {
        return downloadTaskApi;
    }

    @Override
    public TransformRequestApi transformRequest() {
        return transformRequestApi;
    }
}
