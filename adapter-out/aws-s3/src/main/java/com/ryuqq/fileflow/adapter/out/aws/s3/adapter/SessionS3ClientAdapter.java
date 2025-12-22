package com.ryuqq.fileflow.adapter.out.aws.s3.adapter;

import com.ryuqq.fileflow.application.common.metrics.annotation.DownstreamMetric;
import com.ryuqq.fileflow.application.session.port.out.client.SessionS3ClientPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

/**
 * 세션/업로드 전용 S3 Client Adapter.
 *
 * <p>Presigned URL 발급 및 Multipart Upload 관리를 담당합니다.
 *
 * <p><strong>메트릭</strong>: 모든 S3 작업에 대해 {@code downstream.s3.latency} 메트릭을 수집합니다.
 */
@Component
public class SessionS3ClientAdapter implements SessionS3ClientPort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public SessionS3ClientAdapter(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "presign-put")
    public String generatePresignedPutUrl(
            S3Bucket bucket, S3Key s3Key, ContentType contentType, Duration duration) {
        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(duration)
                        .putObjectRequest(
                                builder ->
                                        builder.bucket(bucket.bucketName())
                                                .key(s3Key.key())
                                                .contentType(contentType.type()))
                        .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "initiate")
    public String initiateMultipartUpload(S3Bucket bucket, S3Key s3Key, ContentType contentType) {
        CreateMultipartUploadRequest request =
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket.bucketName())
                        .key(s3Key.key())
                        .contentType(contentType.type())
                        .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
        return response.uploadId();
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "presign-part")
    public String generatePresignedUploadPartUrl(
            S3Bucket bucket, S3Key s3Key, String uploadId, int partNumber, Duration duration) {
        UploadPartRequest uploadPartRequest =
                UploadPartRequest.builder()
                        .bucket(bucket.bucketName())
                        .key(s3Key.key())
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build();

        UploadPartPresignRequest presignRequest =
                UploadPartPresignRequest.builder()
                        .signatureDuration(duration)
                        .uploadPartRequest(uploadPartRequest)
                        .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "head")
    public Optional<ETag> getObjectETag(S3Bucket bucket, S3Key s3Key) {
        try {
            HeadObjectRequest request =
                    HeadObjectRequest.builder()
                            .bucket(bucket.bucketName())
                            .key(s3Key.key())
                            .build();

            HeadObjectResponse response = s3Client.headObject(request);
            String etag = response.eTag().replace("\"", "");
            return Optional.of(ETag.of(etag));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "complete")
    public ETag completeMultipartUpload(
            S3Bucket bucket, S3Key s3Key, String uploadId, List<CompletedPart> completedParts) {
        List<software.amazon.awssdk.services.s3.model.CompletedPart> sdkParts =
                completedParts.stream()
                        .map(
                                part ->
                                        software.amazon.awssdk.services.s3.model.CompletedPart
                                                .builder()
                                                .partNumber(part.getPartNumberValue())
                                                .eTag(part.getETagValue())
                                                .build())
                        .toList();

        CompletedMultipartUpload completedMultipartUpload =
                CompletedMultipartUpload.builder().parts(sdkParts).build();

        CompleteMultipartUploadRequest request =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket.bucketName())
                        .key(s3Key.key())
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(request);
        String etag = response.eTag().replace("\"", "");
        return ETag.of(etag);
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "abort")
    public void abortMultipartUpload(S3Bucket bucket, S3Key s3Key, String uploadId) {
        AbortMultipartUploadRequest request =
                AbortMultipartUploadRequest.builder()
                        .bucket(bucket.bucketName())
                        .key(s3Key.key())
                        .uploadId(uploadId)
                        .build();

        s3Client.abortMultipartUpload(request);
    }
}
