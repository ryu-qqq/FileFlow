package com.ryuqq.fileflow.adapter.out.persistence.redis.lock;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockAdapter implements DistributedLockPort {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAdapter.class);

    private final RedissonClient redissonClient;

    public DistributedLockAdapter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(LockKey key, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(key.value());
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("락 획득 중 인터럽트 발생: key={}", key.value());
            return false;
        }
    }

    @Override
    public void unlock(LockKey key) {
        RLock lock = redissonClient.getLock(key.value());
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @Override
    public boolean isHeldByCurrentThread(LockKey key) {
        return redissonClient.getLock(key.value()).isHeldByCurrentThread();
    }

    @Override
    public boolean isLocked(LockKey key) {
        return redissonClient.getLock(key.value()).isLocked();
    }
}
