package com.ryuqq.fileflow.domain.upload.model;

/**
 * 업로드 세션의 상태를 나타내는 Enum
 */
public enum UploadStatus {
    /**
     * 대기 중 - Presigned URL 발급 완료, 업로드 대기
     */
    PENDING,

    /**
     * 완료 - 파일 업로드 성공적으로 완료
     */
    COMPLETED,

    /**
     * 실패 - 파일 업로드 실패
     */
    FAILED
}
