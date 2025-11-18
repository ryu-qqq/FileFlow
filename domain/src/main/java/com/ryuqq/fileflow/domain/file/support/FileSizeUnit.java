package com.ryuqq.fileflow.domain.file.support;

public final class FileSizeUnit {

    public static final long MB_IN_BYTES = 1024L * 1024L;
    public static final long GB_IN_BYTES = 1024L * MB_IN_BYTES;
    public static final long TB_IN_BYTES = 1024L * GB_IN_BYTES;

    private FileSizeUnit() {
    }

    public static long gigabytes(long size) {
        return size * GB_IN_BYTES;
    }

    public static long terabytes(long size) {
        return size * TB_IN_BYTES;
    }
}

