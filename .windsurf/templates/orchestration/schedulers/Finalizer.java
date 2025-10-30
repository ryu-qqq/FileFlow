package com.ryuqq.application.{domain_lower}.scheduler;

import com.ryuqq.adapter.out.persistence.{domain_lower}.entity.{Domain}WriteAheadLogEntity;
import com.ryuqq.adapter.out.persistence.{domain_lower}.repository.{Domain}WriteAheadLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {Domain} Finalizer - PENDING WAL 자동 복구
 *
 * <p>크래시 복구를 위한 Finalizer 스케줄러입니다.
 * 5초마다 PENDING 상태의 WAL을 조회하여 자동으로 Finalize를 완료합니다.</p>
 *
 * @author {author_name}
 * @since {version}
 */
@Component
public class {Domain}Finalizer {

    private static final Logger log = LoggerFactory.getLogger({Domain}Finalizer.class);

    private final {Domain}WriteAheadLogRepository walRepository;
    // TODO: OperationRepository 등 필요한 의존성 추가

    public {Domain}Finalizer({Domain}WriteAheadLogRepository walRepository) {
        this.walRepository = walRepository;
    }

    /**
     * PENDING WAL 처리 (5초마다 실행)
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingWal() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusSeconds(5);
            List<{Domain}WriteAheadLogEntity> pendingWals =
                walRepository.findStalePendingWal(WriteAheadState.PENDING, threshold);

            if (pendingWals.isEmpty()) {
                return;
            }

            log.info("Processing {} pending WAL entries", pendingWals.size());

            for ({Domain}WriteAheadLogEntity wal : pendingWals) {
                try {
                    // TODO: Finalize 로직 구현
                    // 1. Operation 상태 업데이트
                    // 2. 비즈니스 로직 처리 (필요시)
                    // 3. WAL COMPLETED 처리
                    wal.markCompleted();
                    walRepository.save(wal);

                    log.info("Finalized WAL: opId={}", wal.getOpId());
                } catch (Exception e) {
                    log.error("Failed to finalize WAL: opId={}", wal.getOpId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error in processPendingWal", e);
        }
    }
}
