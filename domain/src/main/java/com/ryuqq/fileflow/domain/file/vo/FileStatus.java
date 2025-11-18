package com.ryuqq.fileflow.domain.file.vo;

/**
 * 파일 상태를 나타내는 Value Object
 */
public enum FileStatus {
    /**
     * 대기 중 - 파일 메타데이터 생성됨, 업로드 시작 전
     */
    PENDING,

    /**
     * 업로드 중 - 클라이언트가 S3에 업로드 진행 중
     */
    UPLOADING,

    /**
     * 업로드 완료 - S3에 파일 업로드 성공
     */
    COMPLETED,

    /**
     * 실패 - 업로드 또는 가공 실패
     */
    FAILED,

    /**
     * 재시도 대기 - 실패 후 재시도 대기 중
     */
    RETRY_PENDING,

    /**
     * 가공 중 - 파일 가공 작업 진행 중
     */
    PROCESSING
}
