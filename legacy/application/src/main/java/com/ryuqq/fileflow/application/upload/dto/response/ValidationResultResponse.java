package com.ryuqq.fileflow.application.upload.dto.response;

import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * 검증 결과
 *
 * @param session UploadSession
 * @param multipart MultipartUpload
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ValidationResultResponse(
    UploadSession session,
    MultipartUpload multipart
) {
}
