package com.ryuqq.fileflow.domain.download.vo;

import com.ryuqq.fileflow.domain.common.vo.LockKey;

/**
 * Outbox 재시도 스케줄러용 분산락 키.
 *
 * <p>다중 인스턴스 환경에서 Outbox 재시도 작업의 중복 실행을 방지합니다.
 *
 * <p><strong>키 형식</strong>: {@code lock:outbox:retry:{domain}}
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>{@code
 * OutboxRetryLockKey lockKey = OutboxRetryLockKey.externalDownload();
 * lockPort.tryLock(lockKey, 10, 300, TimeUnit.SECONDS);
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 */
public record OutboxRetryLockKey(String domain) implements LockKey {

    private static final String PREFIX = "lock:outbox:retry:";

    /**
     * OutboxRetryLockKey 생성자.
     *
     * @param domain 도메인 이름 (예: external-download)
     * @throws IllegalArgumentException domain이 null이거나 빈 문자열인 경우
     */
    public OutboxRetryLockKey {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be null or blank");
        }
    }

    /**
     * ExternalDownload Outbox 재시도용 락 키 생성.
     *
     * @return ExternalDownload Outbox 재시도 작업용 락 키
     */
    public static OutboxRetryLockKey externalDownload() {
        return new OutboxRetryLockKey("external-download");
    }

    @Override
    public String value() {
        return PREFIX + domain;
    }
}
