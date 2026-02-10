package com.ryuqq.fileflow.domain.session.vo;

public class MultipartUploadSessionUpdateDataFixture {

    public static MultipartUploadSessionUpdateData anUpdateData() {
        return MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");
    }

    public static MultipartUploadSessionUpdateData anUpdateData(long totalFileSize, String etag) {
        return MultipartUploadSessionUpdateData.of(totalFileSize, etag);
    }
}
