package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper.ExternalDownloadOutboxEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository.ExternalDownloadOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxCommandPort;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * External Download Outbox Command Adapter (CQRS - Command Side)
 *
 * <p>Application Layer의 {@link ExternalDownloadOutboxCommandPort}를 구현하는 Command Adapter입니다.</p>
 * <p>Transactional Outbox Pattern의 쓰기 전용 구현체입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownloadOutbox의 생성/수정/삭제 (Command 전용)</li>
 *   <li>JPA Repository를 통한 쓰기 작업</li>
 *   <li>트랜잭션 관리 (명령 작업은 트랜잭션 필수)</li>
 * </ul>
 *
 * <p><strong>CQRS 설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Command (쓰기) 전용 - 조회 메서드 없음</li>
 *   <li>✅ @Transactional 필수 - 모든 Command는 트랜잭션 내에서 실행</li>
 *   <li>✅ JPA save/delete 사용 - 쓰기 최적화</li>
 *   <li>❌ 조회 메서드 금지 - Query Adapter로 분리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see ExternalDownloadOutboxQueryAdapter
 */
@Component
public class ExternalDownloadOutboxCommandAdapter implements ExternalDownloadOutboxCommandPort {

    private final ExternalDownloadOutboxJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository External Download Outbox JPA Repository
     */
    public ExternalDownloadOutboxCommandAdapter(
        ExternalDownloadOutboxJpaRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Outbox 메시지 저장 또는 업데이트
     *
     * <p>Domain Aggregate와 동일한 트랜잭션 내에서 Outbox 메시지를 저장합니다.</p>
     * <p>트랜잭션이 커밋되면 Outbox 메시지도 함께 저장되어 일관성을 보장합니다.</p>
     *
     * <p><strong>신규 저장</strong>:</p>
     * <ul>
     *   <li>outbox.getId() == null</li>
     *   <li>JPA AUTO_INCREMENT로 ID 생성</li>
     *   <li>INSERT 쿼리 실행</li>
     * </ul>
     *
     * <p><strong>업데이트</strong>:</p>
     * <ul>
     *   <li>outbox.getId() != null</li>
     *   <li>기존 Entity 업데이트</li>
     *   <li>UPDATE 쿼리 실행</li>
     * </ul>
     *
     * @param outbox 저장할 Outbox 메시지
     * @return 저장된 Outbox 메시지 (ID 포함)
     */
    @Override
    public ExternalDownloadOutbox save(ExternalDownloadOutbox outbox) {
        if (outbox == null) {
            throw new IllegalArgumentException("Outbox는 null일 수 없습니다");
        }

        // 1. Domain → Entity 변환
        ExternalDownloadOutboxJpaEntity entity =
            ExternalDownloadOutboxEntityMapper.toEntity(outbox);

        // 2. 저장 (신규 또는 업데이트)
        ExternalDownloadOutboxJpaEntity saved = repository.save(entity);

        // 3. Entity → Domain 변환
        return ExternalDownloadOutboxEntityMapper.toDomain(saved);
    }

    /**
     * Outbox 메시지 삭제
     *
     * <p>처리 완료된 Outbox 메시지를 삭제합니다.</p>
     * <p>물리 삭제 방식으로 DB에서 완전히 제거됩니다.</p>
     *
     * @param outboxId 삭제할 Outbox ID
     * @throws IllegalArgumentException Outbox ID가 null이거나 존재하지 않는 경우
     */
    @Override
    public void deleteById(Long outboxId) {
        if (outboxId == null) {
            throw new IllegalArgumentException("Outbox ID는 null일 수 없습니다");
        }

        if (!repository.existsById(outboxId)) {
            throw new IllegalArgumentException(
                "삭제할 Outbox를 찾을 수 없습니다: outboxId=" + outboxId
            );
        }

        repository.deleteById(outboxId);
    }

    /**
     * 오래된 처리 완료 메시지 일괄 삭제
     *
     * <p>COMPLETED 상태이면서 특정 날짜 이전에 생성된 메시지를 일괄 삭제합니다.</p>
     * <p>Batch Job에서 호출하여 Outbox 테이블 크기를 관리합니다.</p>
     *
     * <p><strong>삭제 조건</strong>:</p>
     * <ul>
     *   <li>status = COMPLETED</li>
     *   <li>created_at < beforeDate</li>
     * </ul>
     *
     * @param beforeDate 이 날짜 이전의 COMPLETED 메시지 삭제
     * @return 삭제된 메시지 개수
     * @throws IllegalArgumentException beforeDate가 null인 경우
     */
    @Override
    @Transactional
    public int deleteProcessedMessagesBefore(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            throw new IllegalArgumentException("삭제 기준 날짜는 null일 수 없습니다");
        }

        // COMPLETED 상태이면서 beforeDate 이전에 생성된 메시지 삭제
        return repository.deleteByStatusAndCreatedAtBefore(
            OutboxStatus.COMPLETED,
            beforeDate
        );
    }
}
