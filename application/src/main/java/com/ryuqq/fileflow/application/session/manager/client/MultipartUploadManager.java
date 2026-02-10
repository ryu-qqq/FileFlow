package com.ryuqq.fileflow.application.session.manager.client;

import com.ryuqq.fileflow.application.session.port.out.client.MultipartUploadClient;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpec;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MultipartUploadManager {

    private final MultipartUploadClient multipartUploadClient;

    public MultipartUploadManager(MultipartUploadClient multipartUploadClient) {
        this.multipartUploadClient = multipartUploadClient;
    }

    public String createMultipartUpload(String s3Key, String contentType) {
        return multipartUploadClient.createMultipartUpload(s3Key, contentType);
    }

    public String generatePresignedPartUrl(PartPresignedUrlSpec spec) {
        return multipartUploadClient.generatePresignedPartUrl(
                spec.s3Key(), spec.uploadId(), spec.partNumber(), spec.ttl());
    }

    public String completeMultipartUpload(
            String s3Key, String uploadId, List<CompletedPart> parts) {
        return multipartUploadClient.completeMultipartUpload(s3Key, uploadId, parts);
    }

    public void abortMultipartUpload(String s3Key, String uploadId) {
        multipartUploadClient.abortMultipartUpload(s3Key, uploadId);
    }
}
