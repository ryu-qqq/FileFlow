package com.ryuqq.fileflow.adapter.out.client.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3ClientProperties {

    private final String bucket;
    private final String region;
    private final String endpoint;
    private final String kmsKeyId;

    public S3ClientProperties(
            @Value("${fileflow.s3.bucket}") String bucket,
            @Value("${fileflow.s3.region:ap-northeast-2}") String region,
            @Value("${fileflow.s3.endpoint:}") String endpoint,
            @Value("${fileflow.s3.kms-key-id:}") String kmsKeyId) {
        this.bucket = bucket;
        this.region = region;
        this.endpoint = endpoint;
        this.kmsKeyId = kmsKeyId;
    }

    public String bucket() {
        return bucket;
    }

    public String region() {
        return region;
    }

    public String endpoint() {
        return endpoint;
    }

    public String kmsKeyId() {
        return kmsKeyId;
    }

    public boolean isKmsEncryptionEnabled() {
        return kmsKeyId != null && !kmsKeyId.isBlank();
    }
}
