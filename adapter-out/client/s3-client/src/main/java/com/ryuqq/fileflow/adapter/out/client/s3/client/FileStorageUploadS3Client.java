package com.ryuqq.fileflow.adapter.out.client.s3.client;

import com.ryuqq.fileflow.application.common.port.out.client.FileStorageUploadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Component
public class FileStorageUploadS3Client implements FileStorageUploadClient {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUploadS3Client.class);

    private final S3Client s3Client;

    public FileStorageUploadS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(String bucket, String s3Key, byte[] data, String contentType) {
        log.info("S3 파일 업로드 시작: bucket={}, s3Key={}, size={}", bucket, s3Key, data.length);

        PutObjectRequest putRequest =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3Key)
                        .contentType(contentType)
                        .contentLength((long) data.length)
                        .build();

        PutObjectResponse response = s3Client.putObject(putRequest, RequestBody.fromBytes(data));

        log.info("S3 파일 업로드 완료: s3Key={}, etag={}", s3Key, response.eTag());
        return response.eTag();
    }
}
