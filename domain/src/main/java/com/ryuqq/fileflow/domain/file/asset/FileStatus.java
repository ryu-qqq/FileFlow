package com.ryuqq.fileflow.domain.file.asset;

/**
 * File Status Enum
 *
 * <p>파일의 생명주기 상태를 정의합니다.</p>
 *
 * <p><strong>상태 전이:</strong></p>
 * <pre>
 * UPLOADING → PROCESSING → AVAILABLE → ARCHIVED → DELETED
 *                ↓
 *              FAILED
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum FileStatus {

    /**
     * 업로드 진행 중
     * - UploadSession이 INITIATED 또는 IN_PROGRESS 상태
     */
    UPLOADING,

    /**
     * 후처리 진행 중
     * - 업로드 완료 후 바이러스 스캔, 메타데이터 추출 등
     */
    PROCESSING,

    /**
     * 사용 가능
     * - 다운로드 가능 상태
     */
    AVAILABLE,

    /**
     * 보관됨
     * - 접근 빈도가 낮아 Glacier로 이동
     */
    ARCHIVED,

    /**
     * 삭제됨
     * - Soft Delete 상태
     */
    DELETED,

    /**
     * 실패
     * - 업로드 또는 처리 중 오류 발생
     */
    FAILED
}
