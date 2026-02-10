package com.ryuqq.fileflow.domain.common.vo;

public class StorageInfoFixture {

    public static StorageInfo aStorageInfo() {
        return StorageInfo.of("test-bucket", "public/2025/01/test-file.jpg", AccessType.PUBLIC);
    }

    public static StorageInfo aStorageInfo(String bucket, String s3Key, AccessType accessType) {
        return StorageInfo.of(bucket, s3Key, accessType);
    }

    public static StorageInfo anInternalStorageInfo() {
        return StorageInfo.of(
                "internal-bucket", "internal/2025/01/report.xlsx", AccessType.INTERNAL);
    }
}
