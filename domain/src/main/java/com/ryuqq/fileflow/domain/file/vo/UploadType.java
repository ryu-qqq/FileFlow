package com.ryuqq.fileflow.domain.file.vo;

/**
 * 업로드 타입 정의 (단일/멀티파트)
 */
public enum UploadType {

    SINGLE(gigabytes(5)),
    MULTIPART(terabytes(5));

    private final long maxSize;

    UploadType(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    private static long gigabytes(long size) {
        return size * 1024L * 1024L * 1024L;
    }

    private static long terabytes(long size) {
        return size * 1024L * 1024L * 1024L * 1024L;
    }
}

