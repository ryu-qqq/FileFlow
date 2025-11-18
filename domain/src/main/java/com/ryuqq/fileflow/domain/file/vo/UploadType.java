package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.file.support.FileSizeUnit;

/**
 * 업로드 타입 정의 (단일/멀티파트)
 */
public enum UploadType {

    SINGLE(FileSizeUnit.gigabytes(5)),
    MULTIPART(FileSizeUnit.terabytes(5));

    private final long maxSize;

    UploadType(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getMaxSize() {
        return maxSize;
    }
}

