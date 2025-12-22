package com.ryuqq.fileflow.adapter.out.aws.s3.adapter;

import com.ryuqq.fileflow.application.asset.port.out.client.AssetS3ClientPort;
import com.ryuqq.fileflow.application.common.metrics.annotation.DownstreamMetric;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Asset 처리 전용 S3 Client Adapter.
 *
 * <p>이미지 다운로드, 업로드 및 다운로드 URL 생성을 담당합니다.
 *
 * <p><strong>메트릭</strong>: 모든 S3 작업에 대해 {@code downstream.s3.latency} 메트릭을 수집합니다.
 */
@Component
public class AssetS3ClientAdapter implements AssetS3ClientPort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public AssetS3ClientAdapter(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "get")
    public byte[] getObject(S3Bucket bucket, S3Key s3Key) {
        GetObjectRequest request =
                GetObjectRequest.builder().bucket(bucket.bucketName()).key(s3Key.key()).build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "put")
    public ETag putObject(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] data) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket.bucketName())
                        .key(s3Key.key())
                        .contentType(contentType.type())
                        .contentLength((long) data.length)
                        .build();

        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(data));
        String etag = response.eTag().replace("\"", "");
        return ETag.of(etag);
    }

    @Override
    @DownstreamMetric(target = "s3", operation = "presign-get")
    public String generatePresignedGetUrl(S3Bucket bucket, S3Key s3Key, Duration duration) {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder().bucket(bucket.bucketName()).key(s3Key.key()).build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(duration)
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}
