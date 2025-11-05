package com.ryuqq.fileflow.application.download.dto.response;

import com.ryuqq.fileflow.application.upload.dto.command.UploadStreamResult;
import com.ryuqq.fileflow.domain.upload.StorageKey;

/**
 * 다운로드 결과 (내부 사용)
 * S3 업로드 결과와 Storage Key를 함께 반환
 */
public record DownloadResult(
    UploadStreamResult uploadResult,
    StorageKey storageKey
) {
    public Long getSize() {
        return uploadResult.size();
    }
}
