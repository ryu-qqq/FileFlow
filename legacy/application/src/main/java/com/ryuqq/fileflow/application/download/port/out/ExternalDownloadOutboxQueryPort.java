package com.ryuqq.fileflow.application.download.port.out;

import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * External Download Outbox Query Port (CQRS - Query Side)
 * 조회 전용 Port 인터페이스
 *
 * <p><strong>CQRS 패턴 적용:</strong></p>
 * <ul>
 *   <li>조회 메서드만 포함 (읽기 전용)</li>
 *   <li>트랜잭션: readOnly = true</li>
 *   <li>캐싱 가능</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ExternalDownloadOutboxQueryPort {

    /**
     * 멱등키로 아웃박스 조회
     *
     * @param idempotencyKey 멱등키
     * @return 아웃박스 옵셔널
     */
    Optional<ExternalDownloadOutbox> findByIdempotencyKey(String idempotencyKey);

    /**
     * ID로 아웃박스 조회
     *
     * @param outboxId 아웃박스 ID
     * @return 아웃박스 옵셔널
     */
    Optional<ExternalDownloadOutbox> findById(Long outboxId);

    /**
     * Download ID로 아웃박스 조회
     *
     * @param downloadId 다운로드 ID
     * @return 아웃박스 옵셔널
     */
    Optional<ExternalDownloadOutbox> findByDownloadId(Long downloadId);

    /**
     * 상태별 아웃박스 메시지 조회
     *
     * @param status 조회할 상태
     * @param limit 최대 조회 개수
     * @return 아웃박스 메시지 리스트
     */
    List<ExternalDownloadOutbox> findByStatus(OutboxStatus status, int limit);

    /**
     * 오래된 PROCESSING 메시지 조회 (장애 복구)
     *
     * @param threshold 임계 시간
     * @param limit 최대 조회 개수
     * @return 오래된 메시지 리스트
     */
    List<ExternalDownloadOutbox> findStaleProcessingMessages(
        LocalDateTime threshold,
        int limit
    );

    /**
     * 처리 대기 중인 메시지 개수 조회
     *
     * @return NEW 상태 메시지 개수
     */
    long countPendingMessages();

    /**
     * 실패한 메시지 조회
     *
     * @param limit 최대 조회 개수
     * @return 실패한 메시지 리스트
     */
    List<ExternalDownloadOutbox> findFailedMessages(int limit);

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p>조건:</p>
     * <ul>
     *   <li>상태가 FAILED</li>
     *   <li>재시도 횟수가 최대값 미만</li>
     *   <li>지수 백오프 시간이 경과</li>
     * </ul>
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter 이 시간 이후에 재시도 가능
     * @param limit 최대 조회 개수
     * @return 재시도 가능한 FAILED 메시지 리스트
     */
    List<ExternalDownloadOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int limit
    );
}