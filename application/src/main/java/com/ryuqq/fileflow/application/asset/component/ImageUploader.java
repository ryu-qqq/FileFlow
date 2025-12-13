package com.ryuqq.fileflow.application.asset.component;

import com.ryuqq.fileflow.application.asset.port.out.client.AssetS3ClientPort;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 이미지 업로드 컴포넌트.
 *
 * <p>처리된 이미지를 S3에 업로드합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>AssetS3ClientPort를 통한 이미지 업로드
 *   <li>업로드 로깅
 * </ul>
 */
@Component
public class ImageUploader {

    private static final Logger log = LoggerFactory.getLogger(ImageUploader.class);

    private final AssetS3ClientPort assetS3ClientPort;

    public ImageUploader(AssetS3ClientPort assetS3ClientPort) {
        this.assetS3ClientPort = assetS3ClientPort;
    }

    /**
     * 처리된 이미지를 S3에 업로드합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @param data 업로드할 이미지 바이트 배열
     * @return 업로드된 객체의 ETag
     */
    public ETag upload(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] data) {
        log.debug(
                "이미지 업로드 시작: bucket={}, key={}, size={} bytes",
                bucket.bucketName(),
                s3Key.key(),
                data.length);

        ETag etag = assetS3ClientPort.putObject(bucket, s3Key, contentType, data);

        log.debug(
                "이미지 업로드 완료: bucket={}, key={}, etag={}",
                bucket.bucketName(),
                s3Key.key(),
                etag.value());

        return etag;
    }

    /**
     * 처리된 이미지를 S3에 업로드합니다. (mimeType 문자열 버전)
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param mimeType MIME 타입 문자열
     * @param data 업로드할 이미지 바이트 배열
     * @return 업로드된 객체의 ETag
     */
    public ETag upload(S3Bucket bucket, S3Key s3Key, String mimeType, byte[] data) {
        return upload(bucket, s3Key, ContentType.of(mimeType), data);
    }
}
