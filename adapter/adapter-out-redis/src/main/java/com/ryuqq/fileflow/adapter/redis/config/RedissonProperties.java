package com.ryuqq.fileflow.adapter.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redisson Configuration Properties
 *
 * Redisson 클라이언트의 커넥션 풀 및 타임아웃 설정을 관리합니다.
 * application.yml의 fileflow.redisson 네임스페이스와 매핑됩니다.
 *
 * 설정 항목:
 * - connectionPoolSize: 커넥션 풀 최대 크기 (기본값: 64)
 * - connectionMinimumIdleSize: 최소 유휴 커넥션 수 (기본값: 10)
 * - idleConnectionTimeout: 유휴 커넥션 타임아웃 (기본값: 10000ms)
 * - connectTimeout: 연결 타임아웃 (기본값: 10000ms)
 * - timeout: 명령 실행 타임아웃 (기본값: 3000ms)
 * - retryAttempts: 재시도 횟수 (기본값: 3)
 * - retryInterval: 재시도 간격 (기본값: 1500ms)
 *
 * @author sangwon-ryu
 */
@Component
@ConfigurationProperties(prefix = "fileflow.redisson")
public class RedissonProperties {

    /**
     * 커넥션 풀 최대 크기
     * Redis 서버와의 동시 연결 최대 개수
     */
    private int connectionPoolSize = 64;

    /**
     * 최소 유휴 커넥션 수
     * 항상 유지할 최소 연결 개수 (성능 최적화)
     */
    private int connectionMinimumIdleSize = 10;

    /**
     * 유휴 커넥션 타임아웃 (밀리초)
     * 사용되지 않는 연결이 닫히기까지의 시간
     */
    private int idleConnectionTimeout = 10000;

    /**
     * 연결 타임아웃 (밀리초)
     * Redis 서버 연결 시도 최대 대기 시간
     */
    private int connectTimeout = 10000;

    /**
     * 명령 실행 타임아웃 (밀리초)
     * Redis 명령 실행 최대 대기 시간
     */
    private int timeout = 3000;

    /**
     * 재시도 횟수
     * 연결 실패 시 재시도할 횟수
     */
    private int retryAttempts = 3;

    /**
     * 재시도 간격 (밀리초)
     * 재시도 사이의 대기 시간
     */
    private int retryInterval = 1500;

    /**
     * Default Constructor
     */
    public RedissonProperties() {
    }

    /**
     * All-Args Constructor
     *
     * @param connectionPoolSize 커넥션 풀 최대 크기
     * @param connectionMinimumIdleSize 최소 유휴 커넥션 수
     * @param idleConnectionTimeout 유휴 커넥션 타임아웃 (ms)
     * @param connectTimeout 연결 타임아웃 (ms)
     * @param timeout 명령 실행 타임아웃 (ms)
     * @param retryAttempts 재시도 횟수
     * @param retryInterval 재시도 간격 (ms)
     */
    public RedissonProperties(
            int connectionPoolSize,
            int connectionMinimumIdleSize,
            int idleConnectionTimeout,
            int connectTimeout,
            int timeout,
            int retryAttempts,
            int retryInterval
    ) {
        this.connectionPoolSize = connectionPoolSize;
        this.connectionMinimumIdleSize = connectionMinimumIdleSize;
        this.idleConnectionTimeout = idleConnectionTimeout;
        this.connectTimeout = connectTimeout;
        this.timeout = timeout;
        this.retryAttempts = retryAttempts;
        this.retryInterval = retryInterval;
    }

    // ========== Getters and Setters ==========

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public int getConnectionMinimumIdleSize() {
        return connectionMinimumIdleSize;
    }

    public void setConnectionMinimumIdleSize(int connectionMinimumIdleSize) {
        this.connectionMinimumIdleSize = connectionMinimumIdleSize;
    }

    public int getIdleConnectionTimeout() {
        return idleConnectionTimeout;
    }

    public void setIdleConnectionTimeout(int idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}
