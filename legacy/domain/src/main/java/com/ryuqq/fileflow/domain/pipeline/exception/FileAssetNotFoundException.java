package com.ryuqq.fileflow.domain.pipeline.exception;

/**
 * FileAsset을 찾을 수 없을 때 발생하는 예외
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetNotFoundException extends PipelineException {

    /**
     * FileAsset ID로 예외 생성
     *
     * @param fileAssetId FileAsset ID
     */
    public FileAssetNotFoundException(Long fileAssetId) {
        super(
            PipelineErrorCode.PIPELINE_FILE_ASSET_NOT_FOUND,
            "FileAsset not found for pipeline: " + fileAssetId
        );
    }
}

