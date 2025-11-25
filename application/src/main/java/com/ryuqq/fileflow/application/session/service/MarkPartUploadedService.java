package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.in.command.MarkPartUploadedUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final FindCompletedPartQueryPort findCompletedPartQueryPort;
    private final UploadSessionManager uploadSessionManager;
    private final MultiPartUploadAssembler multiPartUploadAssembler;

    public MarkPartUploadedService(
            FindCompletedPartQueryPort findCompletedPartQueryPort,
            UploadSessionManager uploadSessionManager,
            MultiPartUploadAssembler multiPartUploadAssembler) {
        this.findCompletedPartQueryPort = findCompletedPartQueryPort;
        this.uploadSessionManager = uploadSessionManager;
        this.multiPartUploadAssembler = multiPartUploadAssembler;
    }

    @Override
    @Transactional
    public MarkPartUploadedResponse execute(MarkPartUploadedCommand command) {
        // 1. Command → Domain VO 변환
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        ETag etag = ETag.of(command.etag());

        // 2. CompletedPart 조회 (없으면 예외)
        CompletedPart part =
                findCompletedPartQueryPort
                        .findBySessionIdAndPartNumber(sessionId, command.partNumber())
                        .orElseThrow(() -> new DomainException(SessionErrorCode.PART_NOT_FOUND));

        // 3. Domain 메서드 호출 (Part 완료 처리)
        part.complete(etag, command.size());

        // 4. CompletedPart 저장
        CompletedPart completedPart = uploadSessionManager.saveCompletedPart(sessionId, part);

        // 5. Response 반환 (진행률 없이 완료된 Part 정보만)
        return multiPartUploadAssembler.toCompleteMarkPartResponse(completedPart);
    }
}
