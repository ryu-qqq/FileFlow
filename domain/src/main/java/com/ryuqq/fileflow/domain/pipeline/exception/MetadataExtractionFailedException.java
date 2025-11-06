package com.ryuqq.fileflow.domain.pipeline.exception;

/**
 * 메타데이터 추출 실패 시 발생하는 예외
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MetadataExtractionFailedException extends PipelineException {

    /**
     * 메타데이터 추출 실패 예외 생성
     *
     * @param fileAssetId FileAsset ID
     * @param cause       원인 예외
     */
    public MetadataExtractionFailedException(Long fileAssetId, Throwable cause) {
        super(
            PipelineErrorCode.PIPELINE_METADATA_EXTRACTION_FAILED,
            "Metadata extraction failed for fileAsset: " + fileAssetId,
            cause
        );
    }
}

