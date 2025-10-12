package com.ryuqq.fileflow.adapter.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Session Expiration Configuration Properties
 *
 * Redis TTL 기반 세션 만료 처리를 위한 설정값을 관리합니다.
 * application.yml의 fileflow.session.expiration 네임스페이스와 매핑됩니다.
 *
 * 설정 항목:
 * - lockWaitTimeSeconds: 분산 락 획득 대기 시간 (기본값: 3초)
 * - lockLeaseTimeSeconds: 분산 락 자동 해제 시간 (기본값: 10초)
 *
 * @author sangwon-ryu
 */
@Component
@ConfigurationProperties(prefix = "fileflow.session.expiration")
public class SessionExpirationProperties {

    /**
     * 분산 락 획득 대기 시간 (초)
     * 다른 서버가 락을 보유 중일 때 최대 대기 시간
     */
    private long lockWaitTimeSeconds = 3;

    /**
     * 분산 락 자동 해제 시간 (초)
     * 락을 보유한 서버가 비정상 종료되어도 자동으로 해제되는 시간
     */
    private long lockLeaseTimeSeconds = 10;

    /**
     * Default Constructor
     */
    public SessionExpirationProperties() {
    }

    /**
     * All-Args Constructor
     *
     * @param lockWaitTimeSeconds 락 획득 대기 시간 (초)
     * @param lockLeaseTimeSeconds 락 자동 해제 시간 (초)
     */
    public SessionExpirationProperties(long lockWaitTimeSeconds, long lockLeaseTimeSeconds) {
        this.lockWaitTimeSeconds = lockWaitTimeSeconds;
        this.lockLeaseTimeSeconds = lockLeaseTimeSeconds;
    }

    // ========== Getters and Setters ==========

    public long getLockWaitTimeSeconds() {
        return lockWaitTimeSeconds;
    }

    public void setLockWaitTimeSeconds(long lockWaitTimeSeconds) {
        this.lockWaitTimeSeconds = lockWaitTimeSeconds;
    }

    public long getLockLeaseTimeSeconds() {
        return lockLeaseTimeSeconds;
    }

    public void setLockLeaseTimeSeconds(long lockLeaseTimeSeconds) {
        this.lockLeaseTimeSeconds = lockLeaseTimeSeconds;
    }
}
