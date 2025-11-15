package com.ryuqq.fileflow.domain.vo;

/**
 * 파일 가공 작업 상태를 나타내는 Value Object
 */
public enum JobStatus {
    /**
     * 대기 중 - 작업 생성됨, 처리 시작 전
     */
    PENDING,

    /**
     * 처리 중 - 파일 가공 작업 진행 중
     */
    PROCESSING,

    /**
     * 완료 - 파일 가공 작업 성공
     */
    COMPLETED,

    /**
     * 실패 - 파일 가공 작업 실패
     */
    FAILED,

    /**
     * 재시도 대기 - 실패 후 재시도 대기 중
     */
    RETRY_PENDING
}
