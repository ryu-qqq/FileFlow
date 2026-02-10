package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.common.manager.DistributedLockManager;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireSingleUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.LockedExpireSingleUploadSessionUseCase;
import com.ryuqq.fileflow.domain.session.vo.SessionLockKey;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LockedExpireSingleUploadSessionService
        implements LockedExpireSingleUploadSessionUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(LockedExpireSingleUploadSessionService.class);

    private final DistributedLockManager distributedLockManager;
    private final ExpireSingleUploadSessionUseCase expireSingleUploadSessionUseCase;

    public LockedExpireSingleUploadSessionService(
            DistributedLockManager distributedLockManager,
            ExpireSingleUploadSessionUseCase expireSingleUploadSessionUseCase) {
        this.distributedLockManager = distributedLockManager;
        this.expireSingleUploadSessionUseCase = expireSingleUploadSessionUseCase;
    }

    @Override
    public void execute(String sessionId) {
        SessionLockKey lockKey = new SessionLockKey(sessionId);
        boolean lockAcquired = distributedLockManager.tryLock(lockKey, 0, 30, TimeUnit.SECONDS);

        if (!lockAcquired) {
            log.info("단건 세션 만료 락 획득 실패 (다른 인스턴스 처리 중): sessionId={}", sessionId);
            return;
        }

        try {
            expireSingleUploadSessionUseCase.execute(sessionId);
        } finally {
            distributedLockManager.unlock(lockKey);
        }
    }
}
