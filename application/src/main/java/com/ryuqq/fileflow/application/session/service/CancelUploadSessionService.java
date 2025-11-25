package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.session.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.in.command.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업로드 세션 취소 Service.
 *
 * <p>진행 중인 업로드를 취소하고 세션을 실패 상태로 전환합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>SessionId로 세션 조회 (RDB QueryPort)
 *   <li>Domain에서 취소 처리 (session.fail())
 *       <ul>
 *         <li>상태 전환 검증 (PREPARING/ACTIVE → FAILED)
 *       </ul>
 *   <li>RDB에 저장 (Manager)
 *   <li>Response 변환 (Assembler)
 * </ol>
 */
@Service
public class CancelUploadSessionService implements CancelUploadSessionUseCase {

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final UploadSessionManager uploadSessionManager;
    private final SingleUploadAssembler singleUploadAssembler;

    public CancelUploadSessionService(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            UploadSessionManager uploadSessionManager,
            SingleUploadAssembler singleUploadAssembler) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.uploadSessionManager = uploadSessionManager;
        this.singleUploadAssembler = singleUploadAssembler;
    }

    @Override
    @Transactional
    public CancelUploadSessionResponse execute(CancelUploadSessionCommand command) {
        // 1. SessionId로 세션 조회
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        SingleUploadSession session =
                findUploadSessionQueryPort
                        .findSingleUploadById(sessionId)
                        .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 2. Domain에서 취소 처리 (상태 전환 검증 자동 수행)
        session.fail();

        // 3. RDB 저장
        SingleUploadSession failedSession = uploadSessionManager.save(session);

        // 4. Response 변환
        return singleUploadAssembler.toCancelResponse(failedSession);
    }
}
