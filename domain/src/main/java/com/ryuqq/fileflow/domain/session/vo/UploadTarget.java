package com.ryuqq.fileflow.domain.session.vo;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.util.Objects;

/**
 * 업로드 대상 파일 정보. SingleUploadSession, MultipartUploadSession에서 공통으로 사용.
 *
 * @param s3Key S3 객체 키 (예: "public/2026/02/uuid.jpg")
 * @param bucket S3 버킷명
 * @param accessType 접근 유형 (PUBLIC / INTERNAL)
 * @param fileName 원본 파일명
 * @param contentType MIME 타입 (예: "image/jpeg")
 */
public record UploadTarget(
        String s3Key, String bucket, AccessType accessType, String fileName, String contentType) {

    public UploadTarget {
        Objects.requireNonNull(s3Key, "s3Key must not be null");
        Objects.requireNonNull(bucket, "bucket must not be null");
        Objects.requireNonNull(accessType, "accessType must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");
    }

    public static UploadTarget of(
            String s3Key,
            String bucket,
            AccessType accessType,
            String fileName,
            String contentType) {
        return new UploadTarget(s3Key, bucket, accessType, fileName, contentType);
    }
}
