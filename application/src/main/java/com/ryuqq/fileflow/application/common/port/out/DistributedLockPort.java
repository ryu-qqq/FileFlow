package com.ryuqq.fileflow.application.common.port.out;

import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;

/**
 * 분산락 포트 (출력 포트)
 *
 * <p>Redisson 기반 분산락 추상화입니다.
 */
public interface DistributedLockPort {

    /**
     * 분산락 획득 시도
     *
     * @param key Lock 키
     * @param waitTime 최대 대기 시간
     * @param leaseTime Lock 유지 시간
     * @param unit 시간 단위
     * @return Lock 획득 성공 여부
     */
    boolean tryLock(LockKey key, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 분산락 해제
     *
     * @param key Lock 키
     */
    void unlock(LockKey key);

    /**
     * 현재 스레드가 Lock을 보유 중인지 확인
     *
     * @param key Lock 키
     * @return 현재 스레드가 Lock을 보유 중인지 여부
     */
    boolean isHeldByCurrentThread(LockKey key);

    /**
     * Lock 상태 확인
     *
     * @param key Lock 키
     * @return Lock이 걸려있는지 여부
     */
    boolean isLocked(LockKey key);
}
