package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.ContentType;

/**
 * ContentType Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class ContentTypeFixture {

    private ContentTypeFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 ContentType Fixture (image/jpeg) */
    public static ContentType defaultContentType() {
        return ContentType.of("image/jpeg");
    }

    /** PNG 이미지 ContentType Fixture */
    public static ContentType pngContentType() {
        return ContentType.of("image/png");
    }

    /** PDF ContentType Fixture */
    public static ContentType pdfContentType() {
        return ContentType.of("application/pdf");
    }

    /** 비디오 ContentType Fixture */
    public static ContentType videoContentType() {
        return ContentType.of("video/mp4");
    }

    /** Custom ContentType Fixture */
    public static ContentType customContentType(String type) {
        return ContentType.of(type);
    }
}
