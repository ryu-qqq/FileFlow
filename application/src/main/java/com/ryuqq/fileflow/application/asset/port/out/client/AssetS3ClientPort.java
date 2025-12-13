package com.ryuqq.fileflow.application.asset.port.out.client;

import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;

/**
 * Asset 처리 전용 S3 클라이언트 포트.
 *
 * <p>이미지 다운로드, 업로드 및 다운로드 URL 생성을 담당합니다.
 *
 * <p><strong>사용처</strong>:
 *
 * <ul>
 *   <li>ImageDownloader - 원본 이미지 다운로드
 *   <li>ImageUploader - 리사이징된 이미지 업로드
 *   <li>GenerateDownloadUrlService - 다운로드 URL 생성
 *   <li>BatchGenerateDownloadUrlService - 배치 다운로드 URL 생성
 * </ul>
 */
public interface AssetS3ClientPort {

    /**
     * S3 객체를 다운로드합니다.
     *
     * <p>이미지 처리 등을 위해 원본 파일을 바이트 배열로 다운로드합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @return 객체 바이트 배열
     */
    byte[] getObject(S3Bucket bucket, S3Key s3Key);

    /**
     * 바이트 배열을 S3에 직접 업로드합니다.
     *
     * <p>리사이징된 이미지를 S3에 업로드할 때 사용합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @param data 업로드할 바이트 배열
     * @return 업로드된 객체의 ETag
     */
    ETag putObject(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] data);

    /**
     * S3 객체의 다운로드용 Presigned URL을 발급합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param duration 유효 기간
     * @return Presigned GET URL
     */
    String generatePresignedGetUrl(S3Bucket bucket, S3Key s3Key, Duration duration);
}
