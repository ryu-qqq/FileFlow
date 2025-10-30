package com.ryuqq.application.{domain_lower}.scheduler;

import com.ryuqq.adapter.out.persistence.{domain_lower}.entity.{Domain}OperationEntity;
import com.ryuqq.adapter.out.persistence.{domain_lower}.repository.{Domain}OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {Domain} Reaper - TIMEOUT 처리
 *
 * <p>MAX_ATTEMPTS를 초과한 Operation을 TIMEOUT 처리합니다.
 * 10초마다 실행됩니다.</p>
 *
 * @author {author_name}
 * @since {version}
 */
@Component
public class {Domain}Reaper {

    private static final Logger log = LoggerFactory.getLogger({Domain}Reaper.class);

    private final {Domain}OperationRepository operationRepository;

    public {Domain}Reaper({Domain}OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    /**
     * TIMEOUT 처리 (10초마다 실행)
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processTimeoutOperations() {
        try {
            List<{Domain}OperationEntity> timeoutOps = operationRepository.findTimeoutOperations();

            if (timeoutOps.isEmpty()) {
                return;
            }

            log.warn("Processing {} timeout operations", timeoutOps.size());

            for ({Domain}OperationEntity op : timeoutOps) {
                try {
                    op.markFailed("TIMEOUT", "Max retry attempts exceeded");
                    operationRepository.save(op);

                    log.warn("Marked operation as TIMEOUT: opId={}, attempts={}/{}",
                        op.getOpId(), op.getAttemptCount(), op.getMaxAttempts());

                } catch (Exception e) {
                    log.error("Failed to mark operation as timeout: opId={}", op.getOpId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error in processTimeoutOperations", e);
        }
    }
}
