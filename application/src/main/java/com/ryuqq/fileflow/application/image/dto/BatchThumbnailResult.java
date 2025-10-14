package com.ryuqq.fileflow.application.image.dto;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Batch Thumbnail 생성 결과
 *
 * 원본 이미지에 대해 생성된 모든 썸네일의 정보를 담습니다.
 *
 * 포함 정보:
 * - 원본 이미지 ID
 * - 생성된 각 썸네일의 상세 정보 (ThumbnailGenerationResult 리스트)
 * - 전체 처리 시간 (원본 로드 + 모든 썸네일 생성 + S3 업로드 + DB 저장)
 * - 성공 여부 및 오류 메시지
 *
 * @author sangwon-ryu
 */
public final class BatchThumbnailResult {

    private final String sourceImageId;
    private final List<ThumbnailGenerationResult> thumbnails;
    private final int totalThumbnailCount;
    private final Duration totalProcessingTime;
    private final boolean success;
    private final String errorMessage;

    private BatchThumbnailResult(
            String sourceImageId,
            List<ThumbnailGenerationResult> thumbnails,
            int totalThumbnailCount,
            Duration totalProcessingTime,
            boolean success,
            String errorMessage
    ) {
        this.sourceImageId = sourceImageId;
        this.thumbnails = List.copyOf(thumbnails);
        this.totalThumbnailCount = totalThumbnailCount;
        this.totalProcessingTime = totalProcessingTime;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    /**
     * 성공한 Batch Thumbnail 결과를 생성합니다.
     *
     * @param sourceImageId 원본 이미지 ID
     * @param thumbnails 생성된 썸네일 목록
     * @param totalProcessingTime 전체 처리 시간
     * @return BatchThumbnailResult 인스턴스
     */
    public static BatchThumbnailResult success(
            String sourceImageId,
            List<ThumbnailGenerationResult> thumbnails,
            Duration totalProcessingTime
    ) {
        Objects.requireNonNull(sourceImageId, "Source image ID cannot be null");
        Objects.requireNonNull(thumbnails, "Thumbnails cannot be null");
        Objects.requireNonNull(totalProcessingTime, "Total processing time cannot be null");

        return new BatchThumbnailResult(
                sourceImageId,
                thumbnails,
                thumbnails.size(),
                totalProcessingTime,
                true,
                null
        );
    }

    /**
     * 실패한 Batch Thumbnail 결과를 생성합니다.
     *
     * @param sourceImageId 원본 이미지 ID
     * @param errorMessage 오류 메시지
     * @param totalProcessingTime 전체 처리 시간
     * @return BatchThumbnailResult 인스턴스
     */
    public static BatchThumbnailResult failure(
            String sourceImageId,
            String errorMessage,
            Duration totalProcessingTime
    ) {
        Objects.requireNonNull(sourceImageId, "Source image ID cannot be null");
        Objects.requireNonNull(errorMessage, "Error message cannot be null");
        Objects.requireNonNull(totalProcessingTime, "Total processing time cannot be null");

        return new BatchThumbnailResult(
                sourceImageId,
                List.of(),
                0,
                totalProcessingTime,
                false,
                errorMessage
        );
    }

    // ========== Getters ==========

    public String getSourceImageId() {
        return sourceImageId;
    }

    public List<ThumbnailGenerationResult> getThumbnails() {
        return thumbnails;
    }

    public int getTotalThumbnailCount() {
        return totalThumbnailCount;
    }

    public Duration getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "BatchThumbnailResult{" +
                "sourceImageId='" + sourceImageId + '\'' +
                ", totalThumbnailCount=" + totalThumbnailCount +
                ", totalProcessingTime=" + totalProcessingTime +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
