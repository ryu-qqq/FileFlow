package com.ryuqq.fileflow.domain.pipeline.exception;

/**
 * 썸네일 생성 실패 시 발생하는 예외
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ThumbnailGenerationFailedException extends PipelineException {

    /**
     * 썸네일 생성 실패 예외 생성
     *
     * @param fileAssetId FileAsset ID
     * @param cause       원인 예외
     */
    public ThumbnailGenerationFailedException(Long fileAssetId, Throwable cause) {
        super(
            PipelineErrorCode.PIPELINE_THUMBNAIL_GENERATION_FAILED,
            "Thumbnail generation failed for fileAsset: " + fileAssetId,
            cause
        );
    }
}

