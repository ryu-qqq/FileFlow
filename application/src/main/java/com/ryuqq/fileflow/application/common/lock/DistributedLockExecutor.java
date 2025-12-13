package com.ryuqq.fileflow.application.common.lock;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * 분산락 실행기.
 *
 * <p>LockType 기반으로 분산락을 관리하고 작업을 실행합니다.
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>{@code
 * // 락 획득 후 작업 실행 (반환값 있음)
 * Result result = lockExecutor.executeWithLock(
 *     LockType.EXTERNAL_DOWNLOAD,
 *     downloadId,
 *     () -> processDownload(downloadId)
 * );
 *
 * // 락 획득 시도만 (즉시 반환)
 * boolean executed = lockExecutor.tryExecuteWithLock(
 *     LockType.EXTERNAL_DOWNLOAD,
 *     downloadId,
 *     () -> processDownload(downloadId)
 * );
 * }</pre>
 */
@Component
public class DistributedLockExecutor {

    private final DistributedLockPort lockPort;

    public DistributedLockExecutor(DistributedLockPort lockPort) {
        this.lockPort = lockPort;
    }

    /**
     * 락을 획득하고 작업을 실행합니다.
     *
     * @param lockType 락 타입
     * @param identifier 락 식별자
     * @param action 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과 또는 락 획득 실패 시 null
     */
    public <T> T executeWithLock(LockType lockType, Object identifier, Supplier<T> action) {
        LockKey lockKey = createLockKey(lockType, identifier);
        boolean acquired =
                lockPort.tryLock(
                        lockKey,
                        lockType.getWaitTimeMs(),
                        lockType.getLeaseTimeMs(),
                        TimeUnit.MILLISECONDS);

        if (!acquired) {
            return null;
        }

        try {
            return action.get();
        } finally {
            if (lockPort.isHeldByCurrentThread(lockKey)) {
                lockPort.unlock(lockKey);
            }
        }
    }

    /**
     * 락을 획득하고 작업을 실행합니다 (반환값 없음).
     *
     * @param lockType 락 타입
     * @param identifier 락 식별자
     * @param action 실행할 작업
     * @return 락 획득 및 작업 실행 성공 여부
     */
    public boolean executeWithLock(LockType lockType, Object identifier, Runnable action) {
        LockKey lockKey = createLockKey(lockType, identifier);
        boolean acquired =
                lockPort.tryLock(
                        lockKey,
                        lockType.getWaitTimeMs(),
                        lockType.getLeaseTimeMs(),
                        TimeUnit.MILLISECONDS);

        if (!acquired) {
            return false;
        }

        try {
            action.run();
            return true;
        } finally {
            if (lockPort.isHeldByCurrentThread(lockKey)) {
                lockPort.unlock(lockKey);
            }
        }
    }

    /**
     * 락 획득을 시도하고 성공하면 작업을 실행합니다.
     *
     * <p>waitTime이 0인 LockType에 적합합니다 (즉시 반환).
     *
     * @param lockType 락 타입
     * @param identifier 락 식별자
     * @param action 실행할 작업
     * @return 락 획득 및 작업 실행 성공 여부
     */
    public boolean tryExecuteWithLock(LockType lockType, Object identifier, Runnable action) {
        return executeWithLock(lockType, identifier, action);
    }

    /**
     * 특정 락이 현재 잠겨있는지 확인합니다.
     *
     * @param lockType 락 타입
     * @param identifier 락 식별자
     * @return 락 잠금 여부
     */
    public boolean isLocked(LockType lockType, Object identifier) {
        LockKey lockKey = createLockKey(lockType, identifier);
        return lockPort.isLocked(lockKey);
    }

    private LockKey createLockKey(LockType lockType, Object identifier) {
        String keyValue = lockType.createKey(identifier);
        return new SimpleLockKey(keyValue);
    }

    /** LockType 기반 간단한 LockKey 구현체 */
    private record SimpleLockKey(String keyValue) implements LockKey {

        @Override
        public String value() {
            return keyValue;
        }
    }
}
