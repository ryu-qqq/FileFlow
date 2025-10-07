package com.ryuqq.fileflow.domain.upload.vo;

/**
 * 업로드 세션의 상태를 나타내는 Enum
 *
 * 상태 전이 흐름:
 * PENDING → UPLOADING → COMPLETED
 * PENDING → CANCELLED
 * UPLOADING → FAILED
 * UPLOADING → CANCELLED
 */
public enum UploadStatus {
    /**
     * 대기 중 - Presigned URL 발급 완료, 업로드 대기
     */
    PENDING,

    /**
     * 업로드 중 - 클라이언트가 파일을 업로드하는 중
     */
    UPLOADING,

    /**
     * 완료 - 파일 업로드 성공적으로 완료
     */
    COMPLETED,

    /**
     * 실패 - 파일 업로드 실패
     */
    FAILED,

    /**
     * 취소 - 사용자 또는 시스템에 의해 업로드 취소됨
     */
    CANCELLED
}
