package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.common.port.out.DomainEventPublisher;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.FailUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.event.UploadFailedEvent;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 업로드 세션 실패 처리 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 업로드 세션을 실패 처리하고 실패 이벤트를 발행합니다.
 *
 * 처리 흐름:
 * 1. 세션 조회
 * 2. 세션 상태를 FAILED로 전환
 * 3. 세션 저장
 * 4. UploadFailedEvent 발행
 *
 * @author sangwon-ryu
 */
public class FailUploadService implements FailUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final DomainEventPublisher eventPublisher;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param eventPublisher 도메인 이벤트 발행자
     */
    public FailUploadService(
            UploadSessionPort uploadSessionPort,
            DomainEventPublisher eventPublisher
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "DomainEventPublisher must not be null"
        );
    }

    /**
     * 업로드 세션을 실패 처리합니다.
     *
     * @param sessionId 세션 ID
     * @param reason 실패 사유
     * @return 실패 처리된 세션 정보
     * @throws IllegalArgumentException sessionId 또는 reason이 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 실패 처리 가능한 상태가 아니거나 세션이 만료된 경우
     */
    @Override
    @Transactional
    public UploadSessionResponse failSession(String sessionId, String reason) {
        // 1. 세션 조회 및 reason 검증
        UploadSession session = findSessionById(sessionId);
        validateReason(reason);

        // 2. 세션 상태를 FAILED로 전환
        UploadSession failedSession = session.fail();

        // 3. 세션 저장
        UploadSession savedSession = uploadSessionPort.save(failedSession);

        // 4. UploadFailedEvent 발행
        UploadFailedEvent event = UploadFailedEvent.of(
                savedSession.getSessionId(),
                savedSession.getUploaderId(),
                reason
        );
        eventPublisher.publish(event);

        // 5. Response 생성
        return UploadSessionResponse.from(savedSession);
    }

    // ========== Helper Methods ==========

    /**
     * 세션 ID를 검증하고 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 UploadSession
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    private UploadSession findSessionById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId must not be null or empty");
        }
        return uploadSessionPort.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));
    }

    /**
     * 실패 사유를 검증합니다.
     *
     * @param reason 실패 사유
     * @throws IllegalArgumentException reason이 null이거나 빈 문자열인 경우
     */
    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason must not be null or empty");
        }
    }
}
