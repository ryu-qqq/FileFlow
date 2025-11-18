package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.JobStatus;

/**
 * JobStatus TestFixture (Object Mother 패턴)
 */
public class JobStatusFixture {

    /**
     * PENDING 상태 생성
     */
    public static JobStatus pending() {
        return JobStatus.PENDING;
    }

    /**
     * PROCESSING 상태 생성
     */
    public static JobStatus processing() {
        return JobStatus.PROCESSING;
    }

    /**
     * COMPLETED 상태 생성
     */
    public static JobStatus completed() {
        return JobStatus.COMPLETED;
    }

    /**
     * FAILED 상태 생성
     */
    public static JobStatus failed() {
        return JobStatus.FAILED;
    }

    /**
     * RETRY_PENDING 상태 생성
     */
    public static JobStatus retryPending() {
        return JobStatus.RETRY_PENDING;
    }
}
