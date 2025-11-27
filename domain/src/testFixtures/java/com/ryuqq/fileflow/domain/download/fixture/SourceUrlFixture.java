package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.vo.SourceUrl;

/** SourceUrl 테스트 Fixture. */
public final class SourceUrlFixture {

    private SourceUrlFixture() {}

    /** 기본 SourceUrl 생성. */
    public static SourceUrl defaultSourceUrl() {
        return SourceUrl.of("https://example.com/image.jpg");
    }

    /** HTTPS URL로 SourceUrl 생성. */
    public static SourceUrl httpsSourceUrl() {
        return SourceUrl.of("https://cdn.example.com/images/photo.png");
    }

    /** HTTP URL로 SourceUrl 생성. */
    public static SourceUrl httpSourceUrl() {
        return SourceUrl.of("http://example.com/image.gif");
    }

    /** 특정 URL로 SourceUrl 생성. */
    public static SourceUrl customSourceUrl(String url) {
        return SourceUrl.of(url);
    }
}
