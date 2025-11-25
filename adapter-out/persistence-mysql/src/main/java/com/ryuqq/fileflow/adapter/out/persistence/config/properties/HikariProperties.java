package com.ryuqq.fileflow.adapter.out.persistence.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HikariCP Properties.
 *
 * <p>HikariCP 커넥션 풀 설정을 담당하는 Properties 클래스입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "persistence.hikari")
public class HikariProperties {

    private int minimumIdle = 5;
    private int maximumPoolSize = 20;
    private long idleTimeout = 300000;
    private String poolName = "FileFlowHikariPool";
    private long maxLifetime = 1800000;
    private long connectionTimeout = 30000;

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
