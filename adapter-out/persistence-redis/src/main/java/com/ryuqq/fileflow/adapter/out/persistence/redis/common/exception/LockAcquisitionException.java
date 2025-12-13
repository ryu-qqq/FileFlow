package com.ryuqq.fileflow.adapter.out.persistence.redis.common.exception;

/**
 * Lock 획득 실패 예외
 *
 * <p>분산락 획득 중 발생하는 예외를 나타냅니다.
 *
 * <p><strong>발생 상황:</strong>
 *
 * <ul>
 *   <li>Lock 획득 중 InterruptedException 발생
 *   <li>Lock 획득 타임아웃
 *   <li>Redis 연결 실패
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
public class LockAcquisitionException extends RuntimeException {

    private final String lockKey;

    /**
     * 메시지와 함께 예외 생성
     *
     * @param message 예외 메시지
     */
    public LockAcquisitionException(String message) {
        super(message);
        this.lockKey = null;
    }

    /**
     * Lock 키와 메시지와 함께 예외 생성
     *
     * @param lockKey Lock 키
     * @param message 예외 메시지
     */
    public LockAcquisitionException(String lockKey, String message) {
        super(message);
        this.lockKey = lockKey;
    }

    /**
     * 메시지와 원인 예외와 함께 예외 생성
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public LockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
        this.lockKey = null;
    }

    /**
     * Lock 키, 메시지, 원인 예외와 함께 예외 생성
     *
     * @param lockKey Lock 키
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public LockAcquisitionException(String lockKey, String message, Throwable cause) {
        super(message, cause);
        this.lockKey = lockKey;
    }

    /**
     * Lock 키 반환
     *
     * @return Lock 키 (없으면 null)
     */
    public String getLockKey() {
        return lockKey;
    }
}
