package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.metrics.annotation.SessionMetric;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.query.CompletedPartReadManager;
import com.ryuqq.fileflow.application.session.manager.query.UploadSessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.out.client.SessionS3ClientPort;
import com.ryuqq.fileflow.application.session.service.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Multipart 파일 업로드 완료 Service.
 *
 * <p>모든 Part가 업로드된 후 S3에 병합 요청하고 세션을 완료합니다.
 */
@Service
public class CompleteMultipartUploadService implements CompleteMultipartUploadUseCase {

    private final UploadSessionReadManager uploadSessionReadManager;
    private final CompletedPartReadManager completedPartReadManager;
    private final SessionS3ClientPort sessionS3ClientPort;
    private final UploadSessionFacade uploadSessionFacade;
    private final MultiPartUploadAssembler multiPartUploadAssembler;
    private final UploadSessionCommandFactory commandFactory;

    public CompleteMultipartUploadService(
            UploadSessionReadManager uploadSessionReadManager,
            CompletedPartReadManager completedPartReadManager,
            SessionS3ClientPort sessionS3ClientPort,
            UploadSessionFacade uploadSessionFacade,
            MultiPartUploadAssembler multiPartUploadAssembler,
            UploadSessionCommandFactory commandFactory) {
        this.uploadSessionReadManager = uploadSessionReadManager;
        this.completedPartReadManager = completedPartReadManager;
        this.sessionS3ClientPort = sessionS3ClientPort;
        this.uploadSessionFacade = uploadSessionFacade;
        this.multiPartUploadAssembler = multiPartUploadAssembler;
        this.commandFactory = commandFactory;
    }

    @Override
    @SessionMetric(operation = "complete", type = "multipart")
    public CompleteMultipartUploadResponse execute(CompleteMultipartUploadCommand command) {
        // 1. SessionId로 세션 조회
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        MultipartUploadSession session =
                uploadSessionReadManager
                        .findMultipartUploadById(sessionId)
                        .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // 2. CompletedParts 조회 및 정렬
        List<CompletedPart> completedParts = completedPartReadManager.findAllBySessionId(sessionId);
        List<CompletedPart> sortedParts =
                completedParts.stream()
                        .sorted(Comparator.comparingInt(CompletedPart::getPartNumberValue))
                        .toList();

        // 3. S3 CompleteMultipartUpload API 호출
        ETag mergedETag =
                sessionS3ClientPort.completeMultipartUpload(
                        session.getBucket(),
                        session.getS3Key(),
                        session.getS3UploadIdValue(),
                        sortedParts);

        // 4. Domain 완료 처리 (검증 + 상태 전환 + mergedETag 저장 + 이벤트 생성)
        commandFactory.completeMultipartUpload(session, mergedETag, completedParts);

        // 5. RDB 저장 + 이벤트 발행 (Facade)
        MultipartUploadSession completedSession = uploadSessionFacade.saveAndPublishEvents(session);

        // 6. Response 변환
        return multiPartUploadAssembler.toResponseForComplete(completedSession, sortedParts);
    }
}
