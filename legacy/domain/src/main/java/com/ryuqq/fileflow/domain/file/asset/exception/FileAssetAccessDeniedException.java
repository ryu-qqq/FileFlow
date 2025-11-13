package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.util.Map;

/**
 * FileAsset Access Denied Exception
 *
 * <p>FileAsset에 대한 접근 권한이 없을 때 발생합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetAccessDeniedException extends FileAssetException {

    /**
     * FileAssetAccessDeniedException 생성자
     *
     * @param fileId FileAsset ID
     * @param requesterId 요청자 ID
     */
    public FileAssetAccessDeniedException(FileAssetId fileId, Long requesterId) {
        super(
            FileErrorCode.FILE_ASSET_ACCESS_DENIED,
            Map.of(
                "fileId", fileId.value(),
                "requesterId", requesterId
            )
        );
    }
}

