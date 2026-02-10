package com.ryuqq.fileflow.domain.common.vo;

import java.util.Objects;

/**
 * S3 저장 위치 정보. Asset, UploadSession, DownloadTask 등 여러 도메인에서 공통으로 사용합니다.
 *
 * @param bucket S3 버킷명
 * @param s3Key S3 객체 키 (예: "public/2026/02/uuid.jpg")
 * @param accessType 접근 유형 (PUBLIC / INTERNAL)
 */
public record StorageInfo(String bucket, String s3Key, AccessType accessType) {

    public StorageInfo {
        Objects.requireNonNull(bucket, "bucket must not be null");
        Objects.requireNonNull(s3Key, "s3Key must not be null");
        Objects.requireNonNull(accessType, "accessType must not be null");
    }

    public static StorageInfo of(String bucket, String s3Key, AccessType accessType) {
        return new StorageInfo(bucket, s3Key, accessType);
    }
}
