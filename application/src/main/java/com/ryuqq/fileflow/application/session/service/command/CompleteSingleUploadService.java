package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.metrics.annotation.SessionMetric;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.query.UploadSessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.out.client.SessionS3ClientPort;
import com.ryuqq.fileflow.application.session.service.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.ETagMismatchException;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.springframework.stereotype.Service;

/**
 * 단일 파일 업로드 완료 Service.
 *
 * <p>클라이언트가 S3에 업로드 완료 후 세션을 종료합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>SessionId로 세션 조회 (RDB QueryPort)
 *   <li>S3에서 실제 업로드된 파일의 ETag 조회
 *   <li>클라이언트 제공 ETag와 S3 ETag 비교 검증
 *   <li>Domain에서 완료 처리 (session.complete())
 *       <ul>
 *         <li>상태 전환 검증 (ACTIVE → COMPLETED)
 *         <li>completedAt 설정
 *         <li>⚠️ 만료 검증하지 않음 (S3 업로드 성공 후 세션 만료되어도 완료 가능해야 함)
 *       </ul>
 *   <li>RDB에 저장 (Manager)
 *   <li>Response 변환 (Assembler)
 * </ol>
 */
@Service
public class CompleteSingleUploadService implements CompleteSingleUploadUseCase {

    private final UploadSessionReadManager uploadSessionReadManager;
    private final SessionS3ClientPort sessionS3ClientPort;
    private final UploadSessionFacade uploadSessionFacade;
    private final SingleUploadAssembler singleUploadAssembler;
    private final UploadSessionCommandFactory commandFactory;

    public CompleteSingleUploadService(
            UploadSessionReadManager uploadSessionReadManager,
            SessionS3ClientPort sessionS3ClientPort,
            UploadSessionFacade uploadSessionFacade,
            SingleUploadAssembler singleUploadAssembler,
            UploadSessionCommandFactory commandFactory) {
        this.uploadSessionReadManager = uploadSessionReadManager;
        this.sessionS3ClientPort = sessionS3ClientPort;
        this.uploadSessionFacade = uploadSessionFacade;
        this.singleUploadAssembler = singleUploadAssembler;
        this.commandFactory = commandFactory;
    }

    @Override
    @SessionMetric(operation = "complete", type = "single")
    public CompleteSingleUploadResponse execute(CompleteSingleUploadCommand command) {
        // 1. SessionId로 세션 조회
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        SingleUploadSession session =
                uploadSessionReadManager
                        .findSingleUploadById(sessionId)
                        .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 2. S3에서 실제 업로드된 파일의 ETag 조회
        ETag clientETag = ETag.of(command.etag());
        ETag s3ETag =
                sessionS3ClientPort
                        .getObjectETag(session.getBucket(), session.getS3Key())
                        .orElseThrow(
                                () ->
                                        new ETagMismatchException(
                                                "S3에 파일이 존재하지 않습니다", command.etag()));

        // 3. Domain에서 완료 처리 (ETag 검증 + 상태 전환 검증 + 이벤트 생성)
        commandFactory.completeSingleUpload(session, clientETag, s3ETag);

        // 4. RDB 저장 + 이벤트 발행 (Facade)
        SingleUploadSession completedSession = uploadSessionFacade.saveAndPublishEvents(session);

        // 5. Response 변환
        return singleUploadAssembler.toResponseForComplete(completedSession);
    }
}
