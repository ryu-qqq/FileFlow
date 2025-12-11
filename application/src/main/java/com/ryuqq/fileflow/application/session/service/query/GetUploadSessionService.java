package com.ryuqq.fileflow.application.session.service.query;

import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.manager.query.CompletedPartReadManager;
import com.ryuqq.fileflow.application.session.manager.query.UploadSessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.service.assembler.UploadSessionQueryAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * UploadSession 단건 조회 Service.
 *
 * <p>GetUploadSessionUseCase 구현체입니다.
 */
@Service
public class GetUploadSessionService implements GetUploadSessionUseCase {

    private final UploadSessionReadManager uploadSessionReadManager;
    private final CompletedPartReadManager completedPartReadManager;
    private final UploadSessionQueryAssembler uploadSessionQueryAssembler;

    public GetUploadSessionService(
            UploadSessionReadManager uploadSessionReadManager,
            CompletedPartReadManager completedPartReadManager,
            UploadSessionQueryAssembler uploadSessionQueryAssembler) {
        this.uploadSessionReadManager = uploadSessionReadManager;
        this.completedPartReadManager = completedPartReadManager;
        this.uploadSessionQueryAssembler = uploadSessionQueryAssembler;
    }

    @Override
    public UploadSessionDetailResponse execute(GetUploadSessionQuery query) {
        UploadSessionId sessionId = UploadSessionId.of(query.sessionId());
        UploadSession session =
                uploadSessionReadManager
                        .findByIdAndTenantId(sessionId, query.tenantId())
                        .orElseThrow(() -> new SessionNotFoundException(query.sessionId()));

        List<CompletedPart> completedParts = getCompletedPartsIfMultipart(session);

        return uploadSessionQueryAssembler.toResponseForDetail(session, completedParts);
    }

    private List<CompletedPart> getCompletedPartsIfMultipart(UploadSession session) {
        if (session instanceof MultipartUploadSession) {
            return completedPartReadManager.findAllBySessionId(session.getId());
        }
        return null;
    }
}
