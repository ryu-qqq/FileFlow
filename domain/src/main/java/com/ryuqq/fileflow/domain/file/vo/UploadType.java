package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.file.support.FileSizeUnit;

/**
 * Presigned 업로드에서 지원하는 업로드 타입 정의.
 */
public enum UploadType {

    SINGLE(singleLimit()),
    MULTIPART(multipartLimit());

    private final long maxSize;

    UploadType(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 업로드 타입별 최대 허용 크기(바이트)를 반환한다.
     *
     * @return 허용 최대 크기 (bytes)
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * {@link #getMaxSize()} 별칭. Law of Demeter 규칙을 위해 명시적 이름 제공.
     *
     * @return 허용 최대 크기 (bytes)
     */
    public long getMaxSizeInBytes() {
        return maxSize;
    }

    private static long singleLimit() {
        return FileSizeUnit.gigabytes(5);
    }

    private static long multipartLimit() {
        return FileSizeUnit.terabytes(5);
    }
}

