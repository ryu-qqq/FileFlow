package com.ryuqq.fileflow.application.session.port.in.query;

import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;

/** 멀티파트 업로드 세션 조회 UseCase (Query) */
public interface GetMultipartUploadSessionUseCase {

    MultipartUploadSessionResponse execute(String sessionId);
}
