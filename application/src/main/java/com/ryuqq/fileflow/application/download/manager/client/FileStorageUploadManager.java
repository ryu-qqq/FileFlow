package com.ryuqq.fileflow.application.download.manager.client;

import com.ryuqq.fileflow.application.common.port.out.client.FileStorageUploadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("downloadFileStorageUploadManager")
public class FileStorageUploadManager {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUploadManager.class);

    private final FileStorageUploadClient fileStorageUploadClient;

    public FileStorageUploadManager(FileStorageUploadClient fileStorageUploadClient) {
        this.fileStorageUploadClient = fileStorageUploadClient;
    }

    public String upload(String bucket, String s3Key, byte[] data, String contentType) {
        log.info("파일 스토리지 업로드 시작: bucket={}, s3Key={}, size={}", bucket, s3Key, data.length);
        String etag = fileStorageUploadClient.upload(bucket, s3Key, data, contentType);
        log.info("파일 스토리지 업로드 완료: s3Key={}, etag={}", s3Key, etag);
        return etag;
    }
}
