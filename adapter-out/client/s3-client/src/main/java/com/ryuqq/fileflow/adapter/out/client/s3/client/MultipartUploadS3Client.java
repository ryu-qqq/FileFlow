package com.ryuqq.fileflow.adapter.out.client.s3.client;

import com.ryuqq.fileflow.adapter.out.client.s3.config.S3ClientProperties;
import com.ryuqq.fileflow.adapter.out.client.s3.mapper.MultipartUploadS3Mapper;
import com.ryuqq.fileflow.application.session.port.out.client.MultipartUploadClient;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@Component
public class MultipartUploadS3Client implements MultipartUploadClient {

    private static final Logger log = LoggerFactory.getLogger(MultipartUploadS3Client.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3ClientProperties properties;
    private final MultipartUploadS3Mapper mapper;

    public MultipartUploadS3Client(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3ClientProperties properties,
            MultipartUploadS3Mapper mapper) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public String createMultipartUpload(String s3Key, String contentType) {
        log.info("멀티파트 업로드 시작: s3Key={}, contentType={}", s3Key, contentType);

        CreateMultipartUploadRequest request =
                CreateMultipartUploadRequest.builder()
                        .bucket(properties.bucket())
                        .key(s3Key)
                        .contentType(contentType)
                        .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
        String uploadId = response.uploadId();

        log.info("멀티파트 업로드 생성 완료: s3Key={}, uploadId={}", s3Key, uploadId);
        return uploadId;
    }

    @Override
    public String generatePresignedPartUrl(
            String s3Key, String uploadId, int partNumber, Duration ttl) {
        log.debug(
                "파트 Presigned URL 생성: s3Key={}, uploadId={}, partNumber={}",
                s3Key,
                uploadId,
                partNumber);

        UploadPartPresignRequest presignRequest =
                UploadPartPresignRequest.builder()
                        .signatureDuration(ttl)
                        .uploadPartRequest(
                                part ->
                                        part.bucket(properties.bucket())
                                                .key(s3Key)
                                                .uploadId(uploadId)
                                                .partNumber(partNumber))
                        .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public String completeMultipartUpload(
            String s3Key, String uploadId, List<CompletedPart> parts) {
        log.info("멀티파트 업로드 완료: s3Key={}, uploadId={}, parts={}", s3Key, uploadId, parts.size());

        List<software.amazon.awssdk.services.s3.model.CompletedPart> s3Parts =
                mapper.toS3Parts(parts);

        CompleteMultipartUploadRequest request =
                CompleteMultipartUploadRequest.builder()
                        .bucket(properties.bucket())
                        .key(s3Key)
                        .uploadId(uploadId)
                        .multipartUpload(CompletedMultipartUpload.builder().parts(s3Parts).build())
                        .build();

        CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(request);
        String etag = response.eTag();

        log.info("멀티파트 업로드 완료 처리: s3Key={}, etag={}", s3Key, etag);
        return etag;
    }

    @Override
    public void abortMultipartUpload(String s3Key, String uploadId) {
        log.info("멀티파트 업로드 중단: s3Key={}, uploadId={}", s3Key, uploadId);

        AbortMultipartUploadRequest request =
                AbortMultipartUploadRequest.builder()
                        .bucket(properties.bucket())
                        .key(s3Key)
                        .uploadId(uploadId)
                        .build();

        s3Client.abortMultipartUpload(request);

        log.info("멀티파트 업로드 중단 완료: s3Key={}, uploadId={}", s3Key, uploadId);
    }
}
