package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.util.Map;

/**
 * FileAsset Processing Exception
 *
 * <p>FileAsset이 아직 처리 중일 때 작업을 시도할 때 발생합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetProcessingException extends FileAssetException {

    /**
     * FileAssetProcessingException 생성자
     *
     * @param fileId FileAsset ID
     */
    public FileAssetProcessingException(FileAssetId fileId) {
        super(
            FileErrorCode.FILE_ASSET_PROCESSING,
            Map.of("fileId", fileId.value())
        );
    }
}

