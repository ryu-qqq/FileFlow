package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.util.Map;

/**
 * FileAsset Not Found Exception
 *
 * <p>FileAsset을 찾을 수 없을 때 발생합니다.</p>
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>존재하지 않는 FileAsset ID로 조회</li>
 *   <li>삭제된 FileAsset 접근</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetNotFoundException extends FileAssetException {

    /**
     * FileAssetNotFoundException 생성자 (FileAssetId 사용)
     *
     * @param fileId FileAsset ID
     */
    public FileAssetNotFoundException(FileAssetId fileId) {
        super(
            FileErrorCode.FILE_ASSET_NOT_FOUND,
            Map.of("fileId", fileId.value())
        );
    }

    /**
     * FileAssetNotFoundException 생성자 (Long 사용)
     *
     * @param fileId FileAsset ID (Long)
     */
    public FileAssetNotFoundException(Long fileId) {
        super(
            FileErrorCode.FILE_ASSET_NOT_FOUND,
            Map.of("fileId", fileId)
        );
    }
}

