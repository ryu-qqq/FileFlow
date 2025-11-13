package com.ryuqq.fileflow.application.download.dto.response;

import com.ryuqq.fileflow.application.upload.dto.command.UploadStreamResult;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.domain.upload.StorageKey;

/**
 * 다운로드 결과 (내부 사용)
 * S3 업로드 결과, Storage Key, S3 메타데이터를 함께 반환
 *
 * @param uploadResult 업로드 결과 (ETag, Size 포함)
 * @param storageKey S3 Storage Key
 * @param s3Metadata S3 메타데이터 (Checksum, MimeType 포함)
 */
public record DownloadResult(
    UploadStreamResult uploadResult,
    StorageKey storageKey,
    S3HeadObjectResponse s3Metadata
) {
    public Long getSize() {
        return uploadResult.size();
    }
}
