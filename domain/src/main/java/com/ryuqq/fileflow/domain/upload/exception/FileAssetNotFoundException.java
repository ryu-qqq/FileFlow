package com.ryuqq.fileflow.domain.upload.exception;

import com.ryuqq.fileflow.domain.upload.vo.FileId;

/**
 * FileAsset을 찾을 수 없을 때 발생하는 예외
 *
 * @author sangwon-ryu
 */
public class FileAssetNotFoundException extends RuntimeException {

    private final String fileId;

    public FileAssetNotFoundException(String fileId) {
        super("FileAsset not found: " + fileId);
        this.fileId = fileId;
    }

    public FileAssetNotFoundException(FileId fileId) {
        this(fileId.value());
    }

    public FileAssetNotFoundException(String fileId, Throwable cause) {
        super("FileAsset not found: " + fileId, cause);
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }
}
