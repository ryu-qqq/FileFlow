package com.ryuqq.fileflow.domain.session.vo;

public class SingleUploadSessionUpdateDataFixture {

    public static SingleUploadSessionUpdateData anUpdateData() {
        return SingleUploadSessionUpdateData.of(1024L, "etag-123");
    }

    public static SingleUploadSessionUpdateData anUpdateData(long fileSize, String etag) {
        return SingleUploadSessionUpdateData.of(fileSize, etag);
    }
}
