package com.ryuqq.fileflow.application.common.port.out.client;

public interface FileStorageUploadClient {

    String upload(String bucket, String s3Key, byte[] data, String contentType);
}
