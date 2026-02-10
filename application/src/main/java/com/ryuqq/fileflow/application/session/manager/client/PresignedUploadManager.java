package com.ryuqq.fileflow.application.session.manager.client;

import com.ryuqq.fileflow.application.session.port.out.client.PresignedUploadClient;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class PresignedUploadManager {

    private final PresignedUploadClient presignedUploadClient;

    public PresignedUploadManager(PresignedUploadClient presignedUploadClient) {
        this.presignedUploadClient = presignedUploadClient;
    }

    public String getBucket() {
        return presignedUploadClient.getBucket();
    }

    public String generatePresignedUploadUrl(String s3Key, String contentType, Duration ttl) {
        return presignedUploadClient.generatePresignedUploadUrl(s3Key, contentType, ttl);
    }
}
