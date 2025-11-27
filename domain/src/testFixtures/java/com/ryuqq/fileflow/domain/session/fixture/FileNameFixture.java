package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.FileName;

/**
 * FileName Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileNameFixture {

    private FileNameFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 FileName Fixture */
    public static FileName defaultFileName() {
        return FileName.of("test-file.jpg");
    }

    /** 이미지 파일 FileName Fixture */
    public static FileName imageFileName() {
        return FileName.of("image-sample.png");
    }

    /** 비디오 파일 FileName Fixture */
    public static FileName videoFileName() {
        return FileName.of("video-sample.mp4");
    }

    /** PDF 파일 FileName Fixture */
    public static FileName pdfFileName() {
        return FileName.of("document.pdf");
    }

    /** Custom FileName Fixture */
    public static FileName customFileName(String name) {
        return FileName.of(name);
    }
}
