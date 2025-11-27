package com.ryuqq.fileflow.application.common.port.out.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 분산 락 Port (Port Out)
 *
 * <p><strong>용도</strong>: 분산 환경에서 동시성 제어를 위한 락 인터페이스
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>CrawlTask 트리거 중복 방지
 *   <li>CrawlTask 실행 중복 방지
 *   <li>분산 환경에서의 동시성 제어
 * </ul>
 *
 * <p><strong>구현체</strong>: Redis (Redisson)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface DistributedLockPort {

    /**
     * 락 획득 시도
     *
     * @param lockKey 락 키
     * @param waitTime 락 대기 시간
     * @param leaseTime 락 유지 시간 (Watchdog으로 자동 갱신 가능)
     * @param timeUnit 시간 단위
     * @return 락 획득 성공 여부
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit);

    /**
     * 락 해제
     *
     * @param lockKey 락 키
     */
    void unlock(String lockKey);

    /**
     * 락을 획득하고 작업 실행 후 락 해제
     *
     * <p>try-finally 패턴을 자동으로 처리
     *
     * @param lockKey 락 키
     * @param waitTime 락 대기 시간
     * @param leaseTime 락 유지 시간
     * @param timeUnit 시간 단위
     * @param action 락 획득 후 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과 또는 락 획득 실패 시 null
     */
    <T> T executeWithLock(
            String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> action);

    /**
     * 락을 획득하고 작업 실행 후 락 해제 (반환값 없음)
     *
     * @param lockKey 락 키
     * @param waitTime 락 대기 시간
     * @param leaseTime 락 유지 시간
     * @param timeUnit 시간 단위
     * @param action 락 획득 후 실행할 작업
     * @return 락 획득 및 작업 실행 성공 여부
     */
    boolean executeWithLock(
            String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable action);

    /**
     * 현재 스레드가 락을 보유하고 있는지 확인
     *
     * @param lockKey 락 키
     * @return 락 보유 여부
     */
    boolean isLocked(String lockKey);

    /**
     * 현재 스레드가 해당 락을 보유하고 있는지 확인
     *
     * @param lockKey 락 키
     * @return 현재 스레드의 락 보유 여부
     */
    boolean isHeldByCurrentThread(String lockKey);
}
