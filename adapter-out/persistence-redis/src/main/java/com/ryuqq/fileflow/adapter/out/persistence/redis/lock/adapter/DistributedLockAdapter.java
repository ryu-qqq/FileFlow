package com.ryuqq.fileflow.adapter.out.persistence.redis.lock.adapter;

import com.ryuqq.fileflow.application.common.port.out.lock.DistributedLockPort;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@Component
public class DistributedLockAdapter implements DistributedLockPort {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAdapter.class);

    private final RedissonClient redissonClient;

    public DistributedLockAdapter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
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
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("락 해제: key={}", lockKey);
        } else {
            log.warn("현재 스레드가 보유하지 않은 락 해제 시도: key={}", lockKey);
        }
    }

    @Override
    public <T> T executeWithLock(
            String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> action) {

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                log.debug("락 획득 후 작업 실행: key={}", lockKey);
                try {
                    return action.get();
                } finally {
                    safeUnlock(lock, lockKey);
                }
            } else {
                log.debug("락 획득 실패로 작업 건너뜀: key={}", lockKey);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("락 획득 중 인터럽트 발생: key={}", lockKey);
            return null;
        }
    }

    @Override
    public boolean executeWithLock(
            String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable action) {

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                log.debug("락 획득 후 작업 실행: key={}", lockKey);
                try {
                    action.run();
                    return true;
                } finally {
                    safeUnlock(lock, lockKey);
                }
            } else {
                log.debug("락 획득 실패로 작업 건너뜀: key={}", lockKey);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("락 획득 중 인터럽트 발생: key={}", lockKey);
            return false;
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    /**
     * 안전한 락 해제.
     *
     * <p>현재 스레드가 보유한 경우에만 해제.
     *
     * @param lock RLock 인스턴스
     * @param lockKey 락 키 (로깅용)
     */
    private void safeUnlock(RLock lock, String lockKey) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("락 해제: key={}", lockKey);
        }
    }
}
