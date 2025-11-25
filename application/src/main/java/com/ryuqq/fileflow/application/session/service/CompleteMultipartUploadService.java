package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Multipart 파일 업로드 완료 Service.
 *
 * <p>모든 Part가 업로드된 후 S3에 병합 요청하고 세션을 완료합니다.
 */
@Service
public class CompleteMultipartUploadService implements CompleteMultipartUploadUseCase {

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final FindCompletedPartQueryPort findCompletedPartQueryPort;
    private final S3ClientPort s3ClientPort;
    private final UploadSessionFacade uploadSessionFacade;
    private final MultiPartUploadAssembler multiPartUploadAssembler;

    public CompleteMultipartUploadService(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            FindCompletedPartQueryPort findCompletedPartQueryPort,
            S3ClientPort s3ClientPort,
            UploadSessionFacade uploadSessionFacade,
            MultiPartUploadAssembler multiPartUploadAssembler) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.findCompletedPartQueryPort = findCompletedPartQueryPort;
        this.s3ClientPort = s3ClientPort;
        this.uploadSessionFacade = uploadSessionFacade;
        this.multiPartUploadAssembler = multiPartUploadAssembler;
    }

    @Override
    @Transactional
    public CompleteMultipartUploadResponse execute(CompleteMultipartUploadCommand command) {
        // 1. SessionId로 세션 조회
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        MultipartUploadSession session =
                findUploadSessionQueryPort
                        .findMultipartUploadById(sessionId)
                        .orElseThrow(() -> new DomainException(SessionErrorCode.SESSION_NOT_FOUND));

        // 2. CompletedParts 조회 및 정렬
        List<CompletedPart> completedParts =
                findCompletedPartQueryPort.findAllBySessionId(sessionId);
        List<CompletedPart> sortedParts =
                completedParts.stream()
                        .sorted(Comparator.comparingInt(CompletedPart::getPartNumberValue))
                        .toList();

        // 3. S3 CompleteMultipartUpload API 호출
        ETag mergedETag =
                s3ClientPort.completeMultipartUpload(
                        session.getBucket(),
                        session.getS3Key(),
                        session.getS3UploadIdValue(),
                        sortedParts);

        // 4. Domain 완료 처리 (검증 + 상태 전환 + mergedETag 저장 + 이벤트 생성)
        session.complete(mergedETag, completedParts);

        // 5. RDB 저장 + 이벤트 발행 (Facade)
        MultipartUploadSession completedSession = uploadSessionFacade.saveAndPublishEvents(session);

        // 6. Response 변환
        return multiPartUploadAssembler.toCompleteResponse(completedSession, sortedParts);
    }
}
