package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.FileSize;

/**
 * FileSize Test Fixture (Object Mother)
 */
public final class FileSizeFixture {

    public static final long ONE_MB_IN_BYTES = 1024L * 1024L;
    public static final long ONE_GB_IN_BYTES = 1024L * ONE_MB_IN_BYTES;
    public static final long ONE_TB_IN_BYTES = 1024L * ONE_GB_IN_BYTES;

    private FileSizeFixture() {
    }

    public static FileSize ofBytes(long bytes) {
        return FileSize.of(bytes);
    }

    public static FileSize oneMegabyte() {
        return ofBytes(ONE_MB_IN_BYTES);
    }

    public static FileSize tenMegabytes() {
        return ofBytes(10 * ONE_MB_IN_BYTES);
    }

    public static FileSize oneGigabyte() {
        return ofBytes(ONE_GB_IN_BYTES);
    }

    public static FileSize fiveGigabytes() {
        return ofBytes(5 * ONE_GB_IN_BYTES);
    }

    public static FileSize fiveTerabytes() {
        return ofBytes(5 * ONE_TB_IN_BYTES);
    }

    public static long exceedingSingleUploadSize() {
        return 6 * ONE_GB_IN_BYTES;
    }

    public static long exceedingMultipartUploadSize() {
        return 6 * ONE_TB_IN_BYTES;
    }
}

