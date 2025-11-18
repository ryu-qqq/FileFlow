package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.MimeType;

public final class MimeTypeFixture {

    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String IMAGE_PNG = "image/png";
    private static final String TEXT_HTML = "text/html";
    private static final String UNSUPPORTED = "application/xml";

    private MimeTypeFixture() {
    }

    public static MimeType of(String value) {
        return MimeType.of(value);
    }

    public static MimeType imageMimeType() {
        return MimeType.of(IMAGE_JPEG);
    }

    public static MimeType pngMimeType() {
        return MimeType.of(IMAGE_PNG);
    }

    public static MimeType htmlMimeType() {
        return MimeType.of(TEXT_HTML);
    }

    public static String textHtmlValue() {
        return TEXT_HTML;
    }

    public static String unsupportedMimeValue() {
        return UNSUPPORTED;
    }
}

