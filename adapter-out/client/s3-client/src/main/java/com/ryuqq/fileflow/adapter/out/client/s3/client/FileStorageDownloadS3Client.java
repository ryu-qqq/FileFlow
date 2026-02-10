package com.ryuqq.fileflow.adapter.out.client.s3.client;

import com.ryuqq.fileflow.application.common.port.out.client.FileStorageDownloadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Component
public class FileStorageDownloadS3Client implements FileStorageDownloadClient {

    private static final Logger log = LoggerFactory.getLogger(FileStorageDownloadS3Client.class);

    private final S3Client s3Client;

    public FileStorageDownloadS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public byte[] download(String bucket, String s3Key) {
        log.info("S3 파일 다운로드 시작: bucket={}, s3Key={}", bucket, s3Key);

        GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(s3Key).build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        byte[] data = response.asByteArray();

        log.info("S3 파일 다운로드 완료: s3Key={}, size={}", s3Key, data.length);
        return data;
    }
}
