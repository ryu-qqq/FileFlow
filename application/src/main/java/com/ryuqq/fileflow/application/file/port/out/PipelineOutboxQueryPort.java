package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.download.OutboxStatus;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pipeline Outbox Query Port (Out)
 *
 * <p>CQRS Query Side - PipelineOutbox 조회 전용 Port 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PipelineOutbox 조회 (상태별, 시간별)</li>
 *   <li>Scheduler용 조회 최적화</li>
 *   <li>재시도 로직 지원</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Query: PipelineOutboxQueryPort (조회만)</li>
 *   <li>Command: PipelineOutboxPort (상태 변경)</li>
 * </ul>
 *
 * <p><strong>구현체:</strong></p>
 * <ul>
 *   <li>PipelineOutboxPersistenceAdapter (Persistence Layer)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface PipelineOutboxQueryPort {

    /**
     * 특정 상태의 Outbox 조회 (생성 시간 오름차순)
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxScheduler - PENDING 메시지 조회</li>
     * </ul>
     *
     * @param status    Outbox 상태
     * @param batchSize 배치 크기
     * @return Outbox 목록 (생성 시간 오름차순)
     */
    List<PipelineOutbox> findByStatus(OutboxStatus status, int batchSize);

    /**
     * 오래된 PROCESSING 메시지 조회 (장애 복구)
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>Worker 크래시 후 복구</li>
     *   <li>네트워크 단절 후 복구</li>
     * </ul>
     *
     * @param staleThreshold PROCESSING 상태가 이 시간보다 오래된 메시지
     * @param batchSize      배치 크기
     * @return 오래된 PROCESSING 메시지 목록
     */
    List<PipelineOutbox> findStaleProcessingMessages(
        LocalDateTime staleThreshold,
        int batchSize
    );

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p><strong>재시도 조건:</strong></p>
     * <ul>
     *   <li>retryCount < maxRetryCount</li>
     *   <li>updatedAt < retryAfter (지수 백오프)</li>
     * </ul>
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter    이 시간 이전에 실패한 메시지만 재시도
     * @param batchSize     배치 크기
     * @return 재시도 가능한 FAILED 메시지 목록
     */
    List<PipelineOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int batchSize
    );
}
