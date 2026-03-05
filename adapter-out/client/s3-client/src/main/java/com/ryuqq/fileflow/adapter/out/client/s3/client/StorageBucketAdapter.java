package com.ryuqq.fileflow.adapter.out.client.s3.client;

import com.ryuqq.fileflow.adapter.out.client.s3.config.S3ClientProperties;
import com.ryuqq.fileflow.application.common.port.out.StorageBucketPort;
import org.springframework.stereotype.Component;

@Component
public class StorageBucketAdapter implements StorageBucketPort {

    private final S3ClientProperties properties;

    public StorageBucketAdapter(S3ClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getBucket() {
        return properties.bucket();
    }
}
