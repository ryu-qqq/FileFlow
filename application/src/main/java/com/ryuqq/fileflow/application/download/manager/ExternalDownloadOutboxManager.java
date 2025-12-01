package com.ryuqq.fileflow.application.download.manager;

import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadOutboxPersistencePort;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 다운로드 Outbox 영속화 Manager.
 *
 * <p>ExternalDownloadOutbox Aggregate의 영속화를 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>save: Outbox 저장
 *   <li>markAsPublished: SQS 발행 성공 시 상태 업데이트
 *   <li>markAsFailed: SQS 발행 실패 시 처리 (재시도 스케줄러에서 처리)
 * </ul>
 */
@Component
public class ExternalDownloadOutboxManager {

    private final ExternalDownloadOutboxPersistencePort persistencePort;
    private final ClockHolder clockHolder;

    public ExternalDownloadOutboxManager(
            ExternalDownloadOutboxPersistencePort persistencePort, ClockHolder clockHolder) {
        this.persistencePort = persistencePort;
        this.clockHolder = clockHolder;
    }

    /**
     * ExternalDownloadOutbox를 저장합니다.
     *
     * @param outbox 저장할 ExternalDownloadOutbox
     * @return 생성된 ExternalDownloadOutboxId
     */
    @Transactional
    public ExternalDownloadOutboxId save(ExternalDownloadOutbox outbox) {
        return persistencePort.persist(outbox);
    }

    /**
     * Outbox를 발행 완료 상태로 업데이트합니다.
     *
     * <p>SQS 발행 성공 시 호출합니다. REQUIRES_NEW 트랜잭션으로 메인 트랜잭션과 독립적으로 커밋됩니다.
     *
     * @param outbox 발행 완료 처리할 Outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsPublished(ExternalDownloadOutbox outbox) {
        outbox.markAsPublished(clockHolder.getClock());
        persistencePort.persist(outbox);
    }

    /**
     * Outbox 발행 실패를 기록합니다.
     *
     * <p>SQS 발행 실패 시 호출합니다. 재시도 스케줄러에서 미발행 Outbox를 조회하여 재시도합니다.
     *
     * <p>현재는 published=false 상태를 유지하여 스케줄러가 재시도하도록 합니다.
     *
     * @param outbox 발행 실패한 Outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(ExternalDownloadOutbox outbox) {
        // published=false 상태 유지하여 재시도 스케줄러에서 처리
        // 향후 실패 횟수, 실패 사유 등을 기록하도록 확장 가능
    }
}
