package com.ryuqq.fileflow.domain.common.vo;

/**
 * 분산락 키 인터페이스
 *
 * <p>Redis 분산락에 사용되는 키의 기반 인터페이스입니다. 각 Bounded Context는 이 인터페이스를 구현하여 도메인 특화 락 키를 정의합니다.
 *
 * <p><strong>구현 가이드:</strong>
 *
 * <ul>
 *   <li>record로 구현 권장 (불변성, equals/hashCode 자동)
 *   <li>compact constructor에서 유효성 검증
 *   <li>키 형식: {@code lock:{domain}:{entity}:{id}}
 * </ul>
 *
 * <p><strong>구현 예시:</strong>
 *
 * <pre>{@code
 * public record SessionLockKey(String sessionId) implements LockKey {
 *
 *     private static final String PREFIX = "lock:session:";
 *
 *     public SessionLockKey {
 *         if (sessionId == null || sessionId.isBlank()) {
 *             throw new IllegalArgumentException("sessionId must not be blank");
 *         }
 *     }
 *
 *     @Override
 *     public String value() {
 *         return PREFIX + sessionId;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * SessionLockKey lockKey = new SessionLockKey(sessionId);
 * lockPort.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 */
public interface LockKey {

    /**
     * Redis Lock Key 값 반환
     *
     * <p><strong>형식 규칙:</strong>
     *
     * <pre>
     * lock:{domain}:{id}
     * lock:{domain}:{entity}:{id}
     * lock:{domain}:{entity}:{id}:{sub-entity}:{sub-id}
     * </pre>
     *
     * <p><strong>예시:</strong>
     *
     * <ul>
     *   <li>{@code lock:session:123}
     *   <li>{@code lock:download:task:456}
     * </ul>
     *
     * @return Redis에서 사용할 Lock Key 문자열
     */
    String value();
}
