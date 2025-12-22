package com.ryuqq.fileflow.adapter.out.aws.s3.adapter;

import com.ryuqq.fileflow.application.common.metrics.annotation.DownstreamMetric;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadS3ClientPort;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * 외부 다운로드 전용 S3 Client Adapter.
 *
 * <p>외부에서 다운로드한 파일을 S3에 업로드합니다.
 *
 * <p><strong>메트릭</strong>: 모든 S3 작업에 대해 {@code downstream.s3.latency} 메트릭을 수집합니다.
 */
@Component
public class DownloadS3ClientAdapter implements DownloadS3ClientPort {

    private final S3Client s3Client;

    public DownloadS3ClientAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
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
}
