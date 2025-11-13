package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.util.Map;

/**
 * FileAsset Already Deleted Exception
 *
 * <p>이미 삭제된 FileAsset에 대해 작업을 시도할 때 발생합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetAlreadyDeletedException extends FileAssetException {

    /**
     * FileAssetAlreadyDeletedException 생성자
     *
     * @param fileId FileAsset ID
     */
    public FileAssetAlreadyDeletedException(FileAssetId fileId) {
        super(
            FileErrorCode.FILE_ASSET_ALREADY_DELETED,
            Map.of("fileId", fileId.value())
        );
    }
}

