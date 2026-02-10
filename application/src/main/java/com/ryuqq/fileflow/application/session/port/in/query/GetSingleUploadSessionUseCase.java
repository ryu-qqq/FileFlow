package com.ryuqq.fileflow.application.session.port.in.query;

import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;

/** 단건 업로드 세션 조회 UseCase (Query) */
public interface GetSingleUploadSessionUseCase {

    SingleUploadSessionResponse execute(String sessionId);
}
