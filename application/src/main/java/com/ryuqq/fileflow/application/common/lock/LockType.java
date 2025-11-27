package com.ryuqq.fileflow.application.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * 분산락 타입 정의.
 *
 * <p>각 비즈니스 도메인별 락 설정을 정의합니다.
 *
 * <p><strong>설정 항목</strong>:
 *
 * <ul>
 *   <li>keyPrefix: 락 키 prefix (예: "external-download:")
 *   <li>waitTime: 락 획득 대기 시간 (0이면 즉시 반환)
 *   <li>leaseTime: 락 유지 시간 (작업 최대 시간)
 * </ul>
 */
public enum LockType {

    /**
     * External Download 처리 락.
     *
     * <p>락 키: external-download:{externalDownloadId}
     *
     * <p>대기: 0ms (즉시 반환 - 다른 워커가 처리 중이면 skip)
     *
     * <p>유지: 300초 (5분 - 다운로드 최대 시간)
     */
    EXTERNAL_DOWNLOAD("external-download:", 0L, 300_000L),

    /**
     * Upload Session 완료 처리 락.
     *
     * <p>락 키: upload-session:{sessionId}
     *
     * <p>대기: 3초 (동시 완료 요청 대기)
     *
     * <p>유지: 60초 (완료 처리 최대 시간)
     */
    UPLOAD_SESSION("upload-session:", 3_000L, 60_000L);

    private final String keyPrefix;
    private final long waitTimeMs;
    private final long leaseTimeMs;

    LockType(String keyPrefix, long waitTimeMs, long leaseTimeMs) {
        this.keyPrefix = keyPrefix;
        this.waitTimeMs = waitTimeMs;
        this.leaseTimeMs = leaseTimeMs;
    }

    /**
     * 락 키를 생성합니다.
     *
     * @param identifier 고유 식별자 (예: externalDownloadId, sessionId)
     * @return 전체 락 키 (예: "external-download:123")
     */
    public String createKey(Object identifier) {
        return keyPrefix + identifier;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public long getWaitTimeMs() {
        return waitTimeMs;
    }

    public long getLeaseTimeMs() {
        return leaseTimeMs;
    }

    public long getWaitTime(TimeUnit unit) {
        return unit.convert(waitTimeMs, TimeUnit.MILLISECONDS);
    }

    public long getLeaseTime(TimeUnit unit) {
        return unit.convert(leaseTimeMs, TimeUnit.MILLISECONDS);
    }
}
