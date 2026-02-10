package com.ryuqq.fileflow.application.session.service.query;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.query.GetSingleUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Service;

/** 단건 업로드 세션 조회 서비스 */
@Service
public class GetSingleUploadSessionService implements GetSingleUploadSessionUseCase {

    private final SessionReadManager sessionReadManager;
    private final SessionAssembler sessionAssembler;

    public GetSingleUploadSessionService(
            SessionReadManager sessionReadManager, SessionAssembler sessionAssembler) {
        this.sessionReadManager = sessionReadManager;
        this.sessionAssembler = sessionAssembler;
    }

    @Override
    public SingleUploadSessionResponse execute(String sessionId) {
        SingleUploadSession session = sessionReadManager.getSingle(sessionId);
        return sessionAssembler.toResponse(session);
    }
}
