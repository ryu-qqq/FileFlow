package com.ryuqq.fileflow.application.transform.manager.client;

import com.ryuqq.fileflow.application.common.port.out.client.FileStorageDownloadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileStorageDownloadManager {

    private static final Logger log = LoggerFactory.getLogger(FileStorageDownloadManager.class);

    private final FileStorageDownloadClient fileStorageDownloadClient;

    public FileStorageDownloadManager(FileStorageDownloadClient fileStorageDownloadClient) {
        this.fileStorageDownloadClient = fileStorageDownloadClient;
    }

    public byte[] download(String bucket, String s3Key) {
        log.info("파일 스토리지 다운로드 시작: bucket={}, s3Key={}", bucket, s3Key);
        byte[] data = fileStorageDownloadClient.download(bucket, s3Key);
        log.info("파일 스토리지 다운로드 완료: s3Key={}, size={}", s3Key, data.length);
        return data;
    }
}
