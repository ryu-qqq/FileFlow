package com.ryuqq.fileflow.application.common.port.out;

import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;

/**
 * 분산락 포트 (출력 포트)
 *
 * <p>Redisson 기반 분산락 추상화입니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * OrderLockKey lockKey = new OrderLockKey(orderId);
 * boolean acquired = lockPort.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
 *
 * if (!acquired) {
 *     throw new LockAcquisitionException("Lock 획득 실패");
 * }
 *
 * try {
 *     // 비즈니스 로직
 * } finally {
 *     lockPort.unlock(lockKey);
 * }
 * }</pre>
 *
 * <p><strong>LockKey 구현:</strong>
 *
 * <p>각 Bounded Context에서 LockKey 인터페이스를 구현하여 도메인 특화 락 키를 정의합니다.
 *
 * <pre>{@code
 * public record OrderLockKey(Long orderId) implements LockKey {
 *     @Override
 *     public String value() {
 *         return "lock:order:" + orderId;
 *     }
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 * @see LockKey
 */
public interface DistributedLockPort {

    /**
     * 분산락 획득 시도
     *
     * <p><strong>파라미터 권장값:</strong>
     *
     * <ul>
     *   <li>빠른 작업: waitTime=5초, leaseTime=10초
     *   <li>일반 작업: waitTime=10초, leaseTime=30초
     *   <li>무거운 작업: waitTime=30초, leaseTime=60초
     * </ul>
     *
     * @param key Lock 키 (도메인 특화 LockKey 구현체)
     * @param waitTime 최대 대기 시간
     * @param leaseTime Lock 유지 시간
     * @param unit 시간 단위
     * @return Lock 획득 성공 여부
     */
    boolean tryLock(LockKey key, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 분산락 해제
     *
     * <p><strong>주의:</strong> 현재 스레드가 Lock을 보유한 경우에만 해제됩니다.
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
     * <p>어떤 스레드든 Lock을 보유 중인지 확인합니다.
     *
     * @param key Lock 키
     * @return Lock이 걸려있는지 여부
     */
    boolean isLocked(LockKey key);
}
