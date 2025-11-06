package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.file.asset.FileId;

import java.util.Map;

/**
 * Invalid FileAsset State Exception
 *
 * <p>FileAsset의 상태가 작업을 수행하기에 적절하지 않을 때 발생합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidFileAssetStateException extends FileAssetException {

    /**
     * InvalidFileAssetStateException 생성자
     *
     * @param fileId FileAsset ID
     * @param currentState 현재 상태
     * @param expectedState 기대 상태
     */
    public InvalidFileAssetStateException(FileId fileId, String currentState, String expectedState) {
        super(
            FileErrorCode.INVALID_FILE_ASSET_STATE,
            Map.of(
                "fileId", fileId.value(),
                "currentState", currentState,
                "expectedState", expectedState
            )
        );
    }
}

