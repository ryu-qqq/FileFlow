package com.ryuqq.fileflow.domain.pipeline.exception;

import com.ryuqq.fileflow.domain.common.ErrorCode;

/**
 * Pipeline Error Code
 *
 * <p>Pipeline 바운더리에서 발생하는 모든 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 체계:</strong></p>
 * <ul>
 *   <li>PIPELINE-001 ~ PIPELINE-099: Pipeline 처리 관련 에러</li>
 *   <li>PIPELINE-100 ~ PIPELINE-199: Outbox 관련 에러</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum PipelineErrorCode implements ErrorCode {
    /**
     * FileAsset을 찾을 수 없음
     */
    PIPELINE_FILE_ASSET_NOT_FOUND("PIPELINE-001", 404, "FileAsset not found for pipeline"),

    /**
     * 썸네일 생성 실패
     */
    PIPELINE_THUMBNAIL_GENERATION_FAILED("PIPELINE-002", 500, "Thumbnail generation failed"),

    /**
     * 메타데이터 추출 실패
     */
    PIPELINE_METADATA_EXTRACTION_FAILED("PIPELINE-003", 500, "Metadata extraction failed"),

    /**
     * Outbox가 이미 처리 중
     */
    OUTBOX_ALREADY_PROCESSING("PIPELINE-100", 409, "Outbox already processing"),

    /**
     * 잘못된 상태 전이
     */
    OUTBOX_INVALID_STATE_TRANSITION("PIPELINE-101", 400, "Invalid state transition"),

    /**
     * 최대 재시도 횟수 초과
     */
    OUTBOX_MAX_RETRY_EXCEEDED("PIPELINE-102", 400, "Max retry count exceeded");

    private final String code;
    private final int httpStatus;
    private final String message;

    PipelineErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

