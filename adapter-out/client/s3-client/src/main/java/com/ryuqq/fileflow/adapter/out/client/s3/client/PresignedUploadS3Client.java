package com.ryuqq.fileflow.adapter.out.client.s3.client;

import com.ryuqq.fileflow.adapter.out.client.s3.config.S3ClientProperties;
import com.ryuqq.fileflow.application.session.port.out.client.PresignedUploadClient;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class PresignedUploadS3Client implements PresignedUploadClient {

    private static final Logger log = LoggerFactory.getLogger(PresignedUploadS3Client.class);

    private final S3Presigner s3Presigner;
    private final S3ClientProperties properties;

    public PresignedUploadS3Client(S3Presigner s3Presigner, S3ClientProperties properties) {
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public String getBucket() {
        return properties.bucket();
    }

    @Override
    public String generatePresignedUploadUrl(String s3Key, String contentType, Duration ttl) {
        log.info(
                "Presigned Upload URL 생성: s3Key={}, contentType={}, ttl={}",
                s3Key,
                contentType,
                ttl);

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(ttl)
                        .putObjectRequest(
                                put ->
                                        put.bucket(properties.bucket())
                                                .key(s3Key)
                                                .contentType(contentType))
                        .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String url = presignedRequest.url().toString();

        log.info("Presigned Upload URL 생성 완료: s3Key={}", s3Key);
        return url;
    }
}
