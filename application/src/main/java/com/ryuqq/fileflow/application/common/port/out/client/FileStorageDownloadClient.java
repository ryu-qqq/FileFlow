package com.ryuqq.fileflow.application.common.port.out.client;

public interface FileStorageDownloadClient {

    byte[] download(String bucket, String s3Key);
}
