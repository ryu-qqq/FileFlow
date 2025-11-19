package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.FileName;

/**
 * FileName Test Fixture (Object Mother)
 */
public final class FileNameFixture {

    public static final String IMAGE_JPEG_NAME = "product-image.jpg";
    public static final String HTML_PAGE_NAME = "landing-page.html";
    public static final String WITHOUT_EXTENSION_NAME = "product-image";

    private FileNameFixture() {
    }

    public static FileName from(String value) {
        return FileName.from(value);
    }

    public static FileName imageJpeg() {
        return from(IMAGE_JPEG_NAME);
    }

    public static FileName htmlPage() {
        return from(HTML_PAGE_NAME);
    }

    public static FileName withoutExtensionFile() {
        return from(WITHOUT_EXTENSION_NAME);
    }

    public static String overMaxLengthName() {
        return "a".repeat(256) + ".jpg";
    }
}

