package com.ryuqq.fileflow.application.common.manager;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockManager {

    private final DistributedLockPort distributedLockPort;

    public DistributedLockManager(DistributedLockPort distributedLockPort) {
        this.distributedLockPort = distributedLockPort;
    }

    public boolean tryLock(LockKey key, long waitTime, long leaseTime, TimeUnit unit) {
        return distributedLockPort.tryLock(key, waitTime, leaseTime, unit);
    }

    public void unlock(LockKey key) {
        distributedLockPort.unlock(key);
    }

    public boolean isHeldByCurrentThread(LockKey key) {
        return distributedLockPort.isHeldByCurrentThread(key);
    }
}
