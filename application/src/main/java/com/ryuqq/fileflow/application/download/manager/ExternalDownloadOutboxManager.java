package com.ryuqq.fileflow.application.download.manager;

import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.OutboxStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * External Download Outbox Manager
 * Outbox 메시지의 상태 변경 및 조회를 담당하는 Manager
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Outbox 메시지 상태 변경 (트랜잭션 보장)</li>
 *   <li>Outbox 메시지 조회</li>
 *   <li>Spring 프록시 문제 해결 (별도 Bean으로 분리)</li>
 * </ul>
 *
 * <p><strong>설계 이유:</strong></p>
 * <ul>
 *   <li>Scheduler 내부 메서드 호출 시 @Transactional 무시 문제 해결</li>
 *   <li>트랜잭션 경계 명확화</li>
 *   <li>상태 변경 로직 중앙화</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadOutboxManager {

    private final ExternalDownloadOutboxQueryPort queryPort;
    private final ExternalDownloadOutboxCommandPort commandPort;

    public ExternalDownloadOutboxManager(
        ExternalDownloadOutboxQueryPort queryPort,
        ExternalDownloadOutboxCommandPort commandPort
    ) {
        this.queryPort = queryPort;
        this.commandPort = commandPort;
    }

    /**
     * PENDING 상태의 메시지 조회
     *
     * @param batchSize 배치 크기
     * @return PENDING 상태 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<ExternalDownloadOutbox> findNewMessages(int batchSize) {
        return queryPort.findByStatus(OutboxStatus.PENDING, batchSize);
    }

    /**
     * 오래된 PROCESSING 메시지 조회 (장애 복구)
     *
     * @param staleThreshold 임계 시간
     * @param batchSize 배치 크기
     * @return 오래된 PROCESSING 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<ExternalDownloadOutbox> findStaleProcessingMessages(
        LocalDateTime staleThreshold,
        int batchSize
    ) {
        return queryPort.findStaleProcessingMessages(staleThreshold, batchSize);
    }

    /**
     * Outbox 상태를 PROCESSING으로 변경
     *
     * <p>⭐ 별도 트랜잭션으로 실행 (REQUIRES_NEW)</p>
     * <p>다른 스케줄러 인스턴스와의 경쟁 상태 방지</p>
     *
     * @param outbox ExternalDownloadOutbox
     * @return 업데이트된 Outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalDownloadOutbox markProcessing(ExternalDownloadOutbox outbox) {
        outbox.markProcessing();
        return commandPort.save(outbox);
    }

    /**
     * Outbox 상태를 PROCESSED로 변경
     *
     * <p>⭐ 별도 트랜잭션으로 실행</p>
     *
     * @param outbox ExternalDownloadOutbox
     * @return 업데이트된 Outbox
     */
    @Transactional
    public ExternalDownloadOutbox markProcessed(ExternalDownloadOutbox outbox) {
        outbox.markProcessed();
        return commandPort.save(outbox);
    }

    /**
     * Outbox 처리 실패 기록
     *
     * <p>⭐ 별도 트랜잭션으로 실행</p>
     * <p>재시도 카운트 증가 및 에러 메시지 저장</p>
     *
     * @param outbox ExternalDownloadOutbox
     * @param errorMessage 오류 메시지
     * @return 업데이트된 Outbox
     */
    @Transactional
    public ExternalDownloadOutbox markFailed(ExternalDownloadOutbox outbox, String errorMessage) {
        outbox.markFailed(errorMessage);
        return commandPort.save(outbox);
    }

    /**
     * Outbox를 영구 실패로 표시
     *
     * <p>최대 재시도 횟수 초과 시 사용</p>
     *
     * @param outbox ExternalDownloadOutbox
     * @param errorMessage 최종 에러 메시지
     * @return 업데이트된 Outbox
     */
    @Transactional
    public ExternalDownloadOutbox markPermanentlyFailed(
        ExternalDownloadOutbox outbox,
        String errorMessage
    ) {
        outbox.markPermanentlyFailed(errorMessage);
        return commandPort.save(outbox);
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p>지수 백오프 시간이 경과한 실패 메시지를 조회</p>
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter 이 시간 이후 재시도 가능
     * @param batchSize 배치 크기
     * @return 재시도 가능한 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<ExternalDownloadOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int batchSize
    ) {
        return queryPort.findRetryableFailedMessages(maxRetryCount, retryAfter, batchSize);
    }

    /**
     * FAILED 메시지를 PENDING으로 변경하여 재시도 준비
     *
     * <p>⭐ 별도 트랜잭션으로 실행 (REQUIRES_NEW)</p>
     * <p>재시도를 위해 상태를 PENDING으로 되돌림</p>
     *
     * @param outbox ExternalDownloadOutbox
     * @return 업데이트된 Outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalDownloadOutbox prepareForRetry(ExternalDownloadOutbox outbox) {
        outbox.retryFromFailed();  // FAILED → PENDING
        return commandPort.save(outbox);
    }
}