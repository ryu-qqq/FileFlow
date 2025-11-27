package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.FileSize;

/**
 * FileSize Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileSizeFixture {

    private FileSizeFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 FileSize Fixture (10MB) */
    public static FileSize defaultFileSize() {
        return FileSize.of(10 * 1024 * 1024L); // 10MB
    }

    /** 작은 파일 크기 Fixture (1MB) */
    public static FileSize smallFileSize() {
        return FileSize.of(1024 * 1024L); // 1MB
    }

    /** 중간 파일 크기 Fixture (100MB) */
    public static FileSize mediumFileSize() {
        return FileSize.of(100 * 1024 * 1024L); // 100MB
    }

    /** 큰 파일 크기 Fixture (1GB) */
    public static FileSize largeFileSize() {
        return FileSize.of(1024 * 1024 * 1024L); // 1GB
    }

    /** Custom FileSize Fixture */
    public static FileSize customFileSize(long bytes) {
        return FileSize.of(bytes);
    }
}
