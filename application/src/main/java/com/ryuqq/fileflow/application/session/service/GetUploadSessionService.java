package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.session.assembler.UploadSessionQueryAssembler;
import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UploadSession 단건 조회 Service.
 *
 * <p>GetUploadSessionUseCase 구현체입니다.
 */
@Service
public class GetUploadSessionService implements GetUploadSessionUseCase {

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final FindCompletedPartQueryPort findCompletedPartQueryPort;
    private final UploadSessionQueryAssembler uploadSessionQueryAssembler;

    public GetUploadSessionService(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            FindCompletedPartQueryPort findCompletedPartQueryPort,
            UploadSessionQueryAssembler uploadSessionQueryAssembler) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.findCompletedPartQueryPort = findCompletedPartQueryPort;
        this.uploadSessionQueryAssembler = uploadSessionQueryAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public UploadSessionDetailResponse execute(GetUploadSessionQuery query) {
        UploadSessionId sessionId = UploadSessionId.of(query.sessionId());
        UploadSession session =
                findUploadSessionQueryPort
                        .findByIdAndTenantId(sessionId, query.tenantId())
                        .orElseThrow(() -> new SessionNotFoundException(query.sessionId()));

        List<CompletedPart> completedParts = getCompletedPartsIfMultipart(session);

        return uploadSessionQueryAssembler.toDetailResponse(session, completedParts);
    }

    private List<CompletedPart> getCompletedPartsIfMultipart(UploadSession session) {
        if (session instanceof MultipartUploadSession) {
            return findCompletedPartQueryPort.findAllBySessionId(session.getId());
        }
        return null;
    }
}
