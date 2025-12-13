package com.ryuqq.fileflow.adapter.out.persistence.redis.lock.adapter;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 분산락 어댑터.
 *
 * <p>Redisson을 사용한 DistributedLockPort 구현체.
 *
 * <p><strong>특징</strong>:
 *
 * <ul>
 *   <li>Redisson RLock 기반 분산락
 *   <li>Watchdog 자동 갱신 지원 (leaseTime -1)
 *   <li>Reentrant 락 지원
 *   <li>try-finally 패턴 자동 처리
 * </ul>
 *
 * <p><strong>락 키 형식</strong>: {@code {lockType-prefix}:{identifier}}
 *
 * <ul>
 *   <li>EXTERNAL_DOWNLOAD: {@code external-download:{externalDownloadId}}
 *   <li>UPLOAD_SESSION: {@code upload-session:{sessionId}}
 * </ul>
 *
 * <p><strong>활성화 조건</strong>: {@code redisson.enabled=true}
 */
@Component
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true", matchIfMissing = true)
public class DistributedLockAdapter implements DistributedLockPort {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAdapter.class);

    private final RedissonClient redissonClient;

    public DistributedLockAdapter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(LockKey key, long waitTime, long leaseTime, TimeUnit unit) {
        String lockKey = key.value();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (acquired) {
                log.debug("락 획득 성공: key={}", lockKey);
            } else {
                log.debug("락 획득 실패 (대기 시간 초과): key={}", lockKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("락 획득 중 인터럽트 발생: key={}", lockKey);
            return false;
        }
    }

    @Override
    public void unlock(LockKey key) {
        String lockKey = key.value();
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("락 해제: key={}", lockKey);
        } else {
            log.warn("현재 스레드가 보유하지 않은 락 해제 시도: key={}", lockKey);
        }
    }

    @Override
    public boolean isHeldByCurrentThread(LockKey key) {
        RLock lock = redissonClient.getLock(key.value());
        return lock.isHeldByCurrentThread();
    }

    @Override
    public boolean isLocked(LockKey key) {
        RLock lock = redissonClient.getLock(key.value());
        return lock.isLocked();
    }
}
