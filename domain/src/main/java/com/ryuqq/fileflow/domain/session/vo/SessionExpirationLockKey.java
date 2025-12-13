package com.ryuqq.fileflow.domain.session.vo;

import com.ryuqq.fileflow.domain.common.vo.LockKey;

/**
 * Session 만료 스케줄러용 분산락 키.
 *
 * <p>다중 인스턴스 환경에서 세션 만료 작업의 중복 실행을 방지합니다.
 *
 * <p><strong>키 형식</strong>: {@code lock:session:expiration:{sessionType}}
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>{@code
 * SessionExpirationLockKey lockKey = SessionExpirationLockKey.singleUpload();
 * lockPort.tryLock(lockKey, 10, 60, TimeUnit.SECONDS);
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 */
public record SessionExpirationLockKey(String sessionType) implements LockKey {

    private static final String PREFIX = "lock:session:expiration:";

    /**
     * SessionExpirationLockKey 생성자.
     *
     * @param sessionType 세션 타입 (single, multipart)
     * @throws IllegalArgumentException sessionType이 null이거나 빈 문자열인 경우
     */
    public SessionExpirationLockKey {
        if (sessionType == null || sessionType.isBlank()) {
            throw new IllegalArgumentException("sessionType must not be null or blank");
        }
    }

    /**
     * Single Upload 세션 만료용 락 키 생성.
     *
     * @return SingleUpload 만료 작업용 락 키
     */
    public static SessionExpirationLockKey singleUpload() {
        return new SessionExpirationLockKey("single");
    }

    /**
     * Multipart Upload 세션 만료용 락 키 생성.
     *
     * @return MultipartUpload 만료 작업용 락 키
     */
    public static SessionExpirationLockKey multipartUpload() {
        return new SessionExpirationLockKey("multipart");
    }

    @Override
    public String value() {
        return PREFIX + sessionType;
    }
}
