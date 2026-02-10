package com.ryuqq.fileflow.application.session.service.query;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.query.GetMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Service;

/** 멀티파트 업로드 세션 조회 서비스 */
@Service
public class GetMultipartUploadSessionService implements GetMultipartUploadSessionUseCase {

    private final SessionReadManager sessionReadManager;
    private final SessionAssembler sessionAssembler;

    public GetMultipartUploadSessionService(
            SessionReadManager sessionReadManager, SessionAssembler sessionAssembler) {
        this.sessionReadManager = sessionReadManager;
        this.sessionAssembler = sessionAssembler;
    }

    @Override
    public MultipartUploadSessionResponse execute(String sessionId) {
        MultipartUploadSession session = sessionReadManager.getMultipart(sessionId);
        return sessionAssembler.toResponse(session);
    }
}
