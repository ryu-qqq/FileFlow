package com.ryuqq.fileflow.domain.upload.vo;

/**
 * 업로드 세션의 상태를 나타내는 Enum
 *
 * 각 상태는 진행률(progress) 정보를 포함합니다.
 * S3 Presigned URL 직접 업로드 방식에서는 실시간 바이트 추적이 불가능하므로
 * 상태 기반의 추정 진행률을 제공합니다.
 *
 * 상태 전이 흐름:
 * PENDING → UPLOADING → COMPLETED
 * PENDING → CANCELLED
 * UPLOADING → FAILED
 * UPLOADING → CANCELLED
 */
public enum UploadStatus {
    /**
     * 대기 중 - Presigned URL 발급 완료, 업로드 대기 (진행률: 0%)
     */
    PENDING(0),

    /**
     * 업로드 중 - 클라이언트가 파일을 업로드하는 중 (진행률: 50%)
     */
    UPLOADING(50),

    /**
     * 완료 - 파일 업로드 성공적으로 완료 (진행률: 100%)
     */
    COMPLETED(100),

    /**
     * 실패 - 파일 업로드 실패 (진행률: 0%)
     */
    FAILED(0),

    /**
     * 취소 - 사용자 또는 시스템에 의해 업로드 취소됨 (진행률: 0%)
     */
    CANCELLED(0);

    private final int progress;

    UploadStatus(int progress) {
        this.progress = progress;
    }

    /**
     * 해당 상태의 진행률을 반환합니다.
     *
     * @return 진행률 (0-100)
     */
    public int getProgress() {
        return progress;
    }
}
