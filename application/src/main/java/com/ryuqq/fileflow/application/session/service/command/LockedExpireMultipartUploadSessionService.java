package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.manager.DistributedLockManager;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.LockedExpireMultipartUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.vo.SessionLockKey;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LockedExpireMultipartUploadSessionService
        implements LockedExpireMultipartUploadSessionUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(LockedExpireMultipartUploadSessionService.class);

    private final DistributedLockManager distributedLockManager;
    private final ExpireMultipartUploadSessionUseCase expireMultipartUploadSessionUseCase;

    public LockedExpireMultipartUploadSessionService(
            DistributedLockManager distributedLockManager,
            ExpireMultipartUploadSessionUseCase expireMultipartUploadSessionUseCase) {
        this.distributedLockManager = distributedLockManager;
        this.expireMultipartUploadSessionUseCase = expireMultipartUploadSessionUseCase;
    }

    @Override
    public void execute(String sessionId) {
        SessionLockKey lockKey = new SessionLockKey(sessionId);
        boolean lockAcquired = distributedLockManager.tryLock(lockKey, 0, 30, TimeUnit.SECONDS);

        if (!lockAcquired) {
            log.info("멀티파트 세션 만료 락 획득 실패 (다른 인스턴스 처리 중): sessionId={}", sessionId);
            return;
        }

        try {
            expireMultipartUploadSessionUseCase.execute(sessionId);
        } finally {
            distributedLockManager.unlock(lockKey);
        }
    }
}
