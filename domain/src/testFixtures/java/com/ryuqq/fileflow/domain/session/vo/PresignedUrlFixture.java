package com.ryuqq.fileflow.domain.session.vo;

public class PresignedUrlFixture {

    public static PresignedUrl aPresignedUrl() {
        return PresignedUrl.of(
                "https://s3.amazonaws.com/fileflow-bucket/public/2026/01/file-001.jpg?signature=abc");
    }

    public static PresignedUrl aPresignedUrl(String url) {
        return PresignedUrl.of(url);
    }
}
