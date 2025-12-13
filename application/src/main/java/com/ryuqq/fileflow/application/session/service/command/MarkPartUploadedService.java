package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.CompletedPartTransactionManager;
import com.ryuqq.fileflow.application.session.manager.query.CompletedPartReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.MarkPartUploadedUseCase;
import com.ryuqq.fileflow.application.session.service.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.springframework.stereotype.Service;

/**
 * Part 업로드 완료 표시 Service.
 *
 * <p>클라이언트가 S3에 Part 업로드를 완료한 후 세션에 기록합니다.
 *
 * <p><strong>설계 결정</strong>:
 *
 * <ul>
 *   <li>진행률 계산 제거: 병렬 요청 시 정확하지 않음
 *   <li>CompletedPart만 조회/저장: 최적화된 쿼리
 *   <li>클라이언트가 진행률 관리: 클라이언트가 전송한 Part 수로 직접 계산
 * </ul>
 */
@Service
public class MarkPartUploadedService implements MarkPartUploadedUseCase {

    private final CompletedPartReadManager completedPartReadManager;
    private final CompletedPartTransactionManager completedPartTransactionManager;
    private final MultiPartUploadAssembler multiPartUploadAssembler;
    private final UploadSessionCommandFactory commandFactory;

    public MarkPartUploadedService(
            CompletedPartReadManager completedPartReadManager,
            CompletedPartTransactionManager completedPartTransactionManager,
            MultiPartUploadAssembler multiPartUploadAssembler,
            UploadSessionCommandFactory commandFactory) {
        this.completedPartReadManager = completedPartReadManager;
        this.completedPartTransactionManager = completedPartTransactionManager;
        this.multiPartUploadAssembler = multiPartUploadAssembler;
        this.commandFactory = commandFactory;
    }

    @Override
    public MarkPartUploadedResponse execute(MarkPartUploadedCommand command) {
        // 1. Command → Domain VO 변환
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        ETag etag = ETag.of(command.etag());

        // 2. CompletedPart 조회 (없으면 예외)
        CompletedPart part =
                completedPartReadManager
                        .findBySessionIdAndPartNumber(sessionId, command.partNumber())
                        .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // 3. Domain 메서드 호출 (Part 완료 처리)
        commandFactory.completePart(part, etag, command.size());

        // 4. CompletedPart 저장
        CompletedPart completedPart = completedPartTransactionManager.persist(sessionId, part);

        // 5. Response 반환 (진행률 없이 완료된 Part 정보만)
        return multiPartUploadAssembler.toResponseForMarkPart(completedPart);
    }
}
