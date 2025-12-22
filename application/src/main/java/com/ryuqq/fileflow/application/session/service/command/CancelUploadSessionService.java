package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.metrics.annotation.SessionMetric;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SingleUploadSessionTransactionManager;
import com.ryuqq.fileflow.application.session.manager.query.UploadSessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.service.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.springframework.stereotype.Service;

/**
 * 업로드 세션 취소 Service.
 *
 * <p>진행 중인 업로드를 취소하고 세션을 실패 상태로 전환합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>SessionId로 세션 조회 (ReadManager)
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

    private final UploadSessionReadManager uploadSessionReadManager;
    private final SingleUploadSessionTransactionManager singleUploadSessionTransactionManager;
    private final SingleUploadAssembler singleUploadAssembler;
    private final UploadSessionCommandFactory commandFactory;

    public CancelUploadSessionService(
            UploadSessionReadManager uploadSessionReadManager,
            SingleUploadSessionTransactionManager singleUploadSessionTransactionManager,
            SingleUploadAssembler singleUploadAssembler,
            UploadSessionCommandFactory commandFactory) {
        this.uploadSessionReadManager = uploadSessionReadManager;
        this.singleUploadSessionTransactionManager = singleUploadSessionTransactionManager;
        this.singleUploadAssembler = singleUploadAssembler;
        this.commandFactory = commandFactory;
    }

    @Override
    @SessionMetric(operation = "cancel", type = "single")
    public CancelUploadSessionResponse execute(CancelUploadSessionCommand command) {
        // 1. SessionId로 세션 조회
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        SingleUploadSession session =
                uploadSessionReadManager
                        .findSingleUploadById(sessionId)
                        .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 2. Domain에서 취소 처리 (상태 전환 검증 자동 수행)
        commandFactory.failSingleUpload(session);

        // 3. RDB 저장
        SingleUploadSession failedSession = singleUploadSessionTransactionManager.persist(session);

        // 4. Response 변환
        return singleUploadAssembler.toResponseForCancel(failedSession);
    }
}
