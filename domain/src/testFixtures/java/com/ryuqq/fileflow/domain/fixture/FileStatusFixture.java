package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.vo.FileStatus;

/**
 * FileStatus TestFixture (Object Mother 패턴)
 */
public class FileStatusFixture {

    /**
     * PENDING 상태 생성
     */
    public static FileStatus pending() {
        return FileStatus.PENDING;
    }

    /**
     * UPLOADING 상태 생성
     */
    public static FileStatus uploading() {
        return FileStatus.UPLOADING;
    }

    /**
     * COMPLETED 상태 생성
     */
    public static FileStatus completed() {
        return FileStatus.COMPLETED;
    }

    /**
     * FAILED 상태 생성
     */
    public static FileStatus failed() {
        return FileStatus.FAILED;
    }

    /**
     * RETRY_PENDING 상태 생성
     */
    public static FileStatus retryPending() {
        return FileStatus.RETRY_PENDING;
    }

    /**
     * PROCESSING 상태 생성
     */
    public static FileStatus processing() {
        return FileStatus.PROCESSING;
    }
}
