package com.ryuqq.fileflow.domain.file.fixture;

/**
 * FileName Test Fixture (Object Mother)
 */
public final class FileNameFixture {

    private FileNameFixture() {
    }

    public static String imageJpegName() {
        return "product-image.jpg";
    }

    public static String htmlPageName() {
        return "landing-page.html";
    }

    public static String nameWithoutExtension() {
        return "product-image";
    }

    public static String overMaxLengthName() {
        return "a".repeat(256) + ".jpg";
    }
}

