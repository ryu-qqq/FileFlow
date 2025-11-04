package com.ryuqq.fileflow.application.file.manager;

import com.ryuqq.fileflow.application.file.port.out.PipelineOutboxPort;
import com.ryuqq.fileflow.application.file.port.out.PipelineOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.OutboxStatus;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pipeline Outbox Manager
 *
 * <p>PipelineOutbox 메시지의 상태 변경 및 조회를 담당하는 Manager 컴포넌트입니다.</p>
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
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>조회: readOnly=true (조회 최적화)</li>
 *   <li>상태 변경: REQUIRES_NEW (격리된 트랜잭션)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineOutboxManager {

    private final PipelineOutboxQueryPort queryPort;
    private final PipelineOutboxPort commandPort;

    /**
     * 생성자
     *
     * @param queryPort   Pipeline Outbox Query Port
     * @param commandPort Pipeline Outbox Command Port
     */
    public PipelineOutboxManager(
        PipelineOutboxQueryPort queryPort,
        PipelineOutboxPort commandPort
    ) {
        this.queryPort = queryPort;
        this.commandPort = commandPort;
    }

    /**
     * PENDING 상태의 메시지 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxScheduler - 처리 대기 중인 메시지 조회</li>
     * </ul>
     *
     * @param batchSize 배치 크기
     * @return PENDING 상태 메시지 리스트 (생성 시간 오름차순)
     */
    @Transactional(readOnly = true)
    public List<PipelineOutbox> findNewMessages(int batchSize) {
        return queryPort.findByStatus(OutboxStatus.PENDING, batchSize);
    }

    /**
     * 오래된 PROCESSING 메시지 조회 (장애 복구)
     *
     * <p><strong>장애 시나리오:</strong></p>
     * <ul>
     *   <li>Worker 크래시</li>
     *   <li>네트워크 단절</li>
     *   <li>예외 발생 후 상태 업데이트 실패</li>
     * </ul>
     *
     * @param staleThreshold PROCESSING 상태가 이 시간보다 오래된 메시지
     * @param batchSize      배치 크기
     * @return 오래된 PROCESSING 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<PipelineOutbox> findStaleProcessingMessages(
        LocalDateTime staleThreshold,
        int batchSize
    ) {
        return queryPort.findStaleProcessingMessages(staleThreshold, batchSize);
    }

    /**
     * Outbox 상태를 PROCESSING으로 변경
     *
     * <p><strong>트랜잭션 전략:</strong></p>
     * <ul>
     *   <li>REQUIRES_NEW: 별도 트랜잭션으로 실행</li>
     *   <li>다른 스케줄러 인스턴스와의 경쟁 상태 방지</li>
     *   <li>즉시 커밋되어 다른 스케줄러가 중복 처리 방지</li>
     * </ul>
     *
     * @param outbox PipelineOutbox
     * @return 업데이트된 Outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineOutbox markProcessing(PipelineOutbox outbox) {
        outbox.markProcessing();
        return commandPort.save(outbox);
    }

    /**
     * Outbox 상태를 COMPLETED로 변경
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>Pipeline 처리 성공</li>
     *   <li>Worker가 정상 완료</li>
     * </ul>
     *
     * @param outbox PipelineOutbox
     * @return 업데이트된 Outbox
     */
    @Transactional
    public PipelineOutbox markProcessed(PipelineOutbox outbox) {
        outbox.markProcessed();
        return commandPort.save(outbox);
    }

    /**
     * Outbox 처리 실패 기록
     *
     * <p><strong>재시도 전략:</strong></p>
     * <ul>
     *   <li>retryCount 증가</li>
     *   <li>다음 스케줄링에서 재시도</li>
     *   <li>지수 백오프 적용</li>
     * </ul>
     *
     * @param outbox       PipelineOutbox
     * @param errorMessage 오류 메시지
     * @return 업데이트된 Outbox
     */
    @Transactional
    public PipelineOutbox markFailed(PipelineOutbox outbox, String errorMessage) {
        outbox.markFailed(errorMessage);
        return commandPort.save(outbox);
    }

    /**
     * Outbox를 영구 실패로 표시
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>최대 재시도 횟수 초과</li>
     *   <li>더 이상 재시도 불가능</li>
     *   <li>수동 개입 필요</li>
     * </ul>
     *
     * @param outbox       PipelineOutbox
     * @param errorMessage 최종 에러 메시지
     * @return 업데이트된 Outbox
     */
    @Transactional
    public PipelineOutbox markPermanentlyFailed(
        PipelineOutbox outbox,
        String errorMessage
    ) {
        outbox.markPermanentlyFailed(errorMessage);
        return commandPort.save(outbox);
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p><strong>재시도 조건:</strong></p>
     * <ul>
     *   <li>retryCount < maxRetryCount</li>
     *   <li>updatedAt < retryAfter (지수 백오프 경과)</li>
     * </ul>
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter    이 시간 이전에 실패한 메시지만 재시도
     * @param batchSize     배치 크기
     * @return 재시도 가능한 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<PipelineOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int batchSize
    ) {
        return queryPort.findRetryableFailedMessages(maxRetryCount, retryAfter, batchSize);
    }

    /**
     * FAILED 메시지를 PENDING으로 변경하여 재시도 준비
     *
     * <p><strong>트랜잭션 전략:</strong></p>
     * <ul>
     *   <li>REQUIRES_NEW: 별도 트랜잭션으로 실행</li>
     *   <li>재시도를 위해 상태를 PENDING으로 되돌림</li>
     * </ul>
     *
     * @param outbox PipelineOutbox
     * @return 업데이트된 Outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PipelineOutbox prepareForRetry(PipelineOutbox outbox) {
        outbox.retryFromFailed();  // FAILED → PENDING
        return commandPort.save(outbox);
    }
}
