package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadOutboxEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository.ExternalDownloadOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * External Download Outbox Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link ExternalDownloadOutboxQueryPort}를 구현하는 Query Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownloadOutbox의 조회 (Query 전용)</li>
 *   <li>QueryDslRepository로 조회 위임</li>
 *   <li>Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ QueryDSL Repository 위임</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ CQRS Query Side 전용 (읽기만)</li>
 *   <li>❌ @Transactional 사용 금지 (Application Layer에서만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see ExternalDownloadOutboxCommandAdapter
 */
@Component
public class ExternalDownloadOutboxQueryAdapter implements ExternalDownloadOutboxQueryPort {

    private final ExternalDownloadOutboxQueryDslRepository repository;

    /**
     * 생성자
     *
     * @param repository ExternalDownloadOutbox QueryDSL Repository
     */
    public ExternalDownloadOutboxQueryAdapter(ExternalDownloadOutboxQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * Outbox ID로 조회
     *
     * @param outboxId Outbox ID
     * @return Outbox 메시지 (Optional)
     */
    @Override
    public Optional<ExternalDownloadOutbox> findById(Long outboxId) {
        return repository.findById(outboxId)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    /**
     * Download ID로 Outbox 조회
     *
     * @param downloadId Download ID
     * @return Outbox 메시지 (Optional)
     */
    @Override
    public Optional<ExternalDownloadOutbox> findByDownloadId(Long downloadId) {
        return repository.findByDownloadId(downloadId)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    /**
     * 멱등성 키로 Outbox 조회
     *
     * <p>중복 이벤트 발행을 방지하기 위해 사용합니다.</p>
     *
     * @param idempotencyKey 멱등성 키
     * @return Outbox 메시지 (Optional)
     */
    @Override
    public Optional<ExternalDownloadOutbox> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    /**
     * 상태별 Outbox 메시지 조회
     *
     * @param status 조회할 상태
     * @param limit  최대 조회 개수
     * @return Outbox 메시지 리스트
     */
    @Override
    public List<ExternalDownloadOutbox> findByStatus(OutboxStatus status, int limit) {
        return repository.findByStatus(status, limit).stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 오래된 PROCESSING 메시지 조회
     *
     * <p>장애로 인해 오래 걸린 PROCESSING 상태 메시지를 찾아 복구합니다.</p>
     *
     * @param threshold 임계 시간
     * @param limit     최대 조회 개수
     * @return 오래된 PROCESSING 메시지 리스트
     */
    @Override
    public List<ExternalDownloadOutbox> findStaleProcessingMessages(
        LocalDateTime threshold,
        int limit
    ) {
        return repository.findStaleProcessingMessages(threshold, limit).stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 처리 대기 중인 메시지 개수 조회
     *
     * @return PENDING 상태 메시지 개수
     */
    @Override
    public long countPendingMessages() {
        return repository.countPendingMessages();
    }

    /**
     * 실패한 메시지 조회
     *
     * @param limit 최대 조회 개수
     * @return 실패한 메시지 리스트
     */
    @Override
    public List<ExternalDownloadOutbox> findFailedMessages(int limit) {
        return repository.findFailedMessages(limit).stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter    이 시간 이후에 재시도 가능
     * @param limit         최대 조회 개수
     * @return 재시도 가능한 FAILED 메시지 리스트
     */
    @Override
    public List<ExternalDownloadOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int limit
    ) {
        return repository.findRetryableFailedMessages(maxRetryCount, retryAfter, limit).stream()
            .map(ExternalDownloadOutboxEntityMapper::toDomain)
            .collect(Collectors.toList());
    }
}
