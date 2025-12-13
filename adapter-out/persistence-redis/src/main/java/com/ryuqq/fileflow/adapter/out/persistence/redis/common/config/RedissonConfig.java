package com.ryuqq.fileflow.adapter.out.persistence.redis.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 설정
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>RedissonClient 생성
 *   <li>분산락 기능 제공
 *   <li>분산 자료구조 지원
 * </ul>
 *
 * <p><strong>용도:</strong>
 *
 * <ul>
 *   <li>분산락 (RLock, FairLock, ReadWriteLock)
 *   <li>분산 컬렉션 (RMap, RSet, RList 등)
 *   <li>분산 객체 (RSemaphore, RCountDownLatch 등)
 * </ul>
 *
 * <p><strong>왜 Redisson인가?</strong>
 *
 * <ul>
 *   <li>Pub/Sub 기반 Lock (스핀락 아님)
 *   <li>Watchdog 자동 연장
 *   <li>FairLock, MultiLock, RedLock 지원
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 * @see LettuceConfig
 */
@Configuration
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${redisson.single-server-config.connection-pool-size:16}")
    private int connectionPoolSize;

    @Value("${redisson.single-server-config.connection-minimum-idle-size:4}")
    private int connectionMinimumIdleSize;

    @Value("${redisson.single-server-config.idle-connection-timeout:10000}")
    private int idleConnectionTimeout;

    @Value("${redisson.single-server-config.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${redisson.single-server-config.timeout:3000}")
    private int timeout;

    @Value("${redisson.single-server-config.retry-attempts:3}")
    private int retryAttempts;

    @Value("${redisson.single-server-config.retry-interval:1500}")
    private int retryInterval;

    @Value("${redisson.threads:4}")
    private int threads;

    @Value("${redisson.netty-threads:4}")
    private int nettyThreads;

    /**
     * RedissonClient 생성 (Single Server Mode)
     *
     * <p>분산락 및 분산 자료구조에 사용됩니다.
     *
     * <p><strong>Watchdog 동작:</strong>
     *
     * <ul>
     *   <li>leaseTime 미지정 시 Watchdog 활성화
     *   <li>기본 30초 주기로 TTL 자동 연장
     *   <li>unlock() 호출 시 Watchdog 종료
     * </ul>
     *
     * @return RedissonClient 인스턴스
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        String address = String.format("redis://%s:%d", host, port);

        config.useSingleServer()
                .setAddress(address)
                .setPassword(isPasswordEmpty() ? null : password)
                .setDatabase(database)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setConnectTimeout(connectTimeout)
                .setTimeout(timeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval);

        // Thread Settings
        config.setThreads(threads);
        config.setNettyThreads(nettyThreads);

        return Redisson.create(config);
    }

    /**
     * 비밀번호 비어있는지 확인
     *
     * @return 비밀번호 비어있으면 true
     */
    private boolean isPasswordEmpty() {
        return password == null || password.isEmpty() || "null".equals(password);
    }

    // ========================================
    // Cluster Mode Configuration (Production)
    // ========================================
    // 아래 메서드는 Cluster 모드 사용 시 활성화
    //
    // @Bean(destroyMethod = "shutdown")
    // @Profile("prod-cluster")
    // public RedissonClient redissonClientCluster(
    //         @Value("${redisson.cluster-servers-config.node-addresses}") List<String> nodes,
    //         @Value("${redisson.cluster-servers-config.password:}") String clusterPassword) {
    //
    //     Config config = new Config();
    //
    //     config.useClusterServers()
    //         .addNodeAddress(nodes.toArray(new String[0]))
    //         .setPassword(clusterPassword.isEmpty() ? null : clusterPassword)
    //         .setScanInterval(2000)
    //         .setMasterConnectionPoolSize(connectionPoolSize)
    //         .setSlaveConnectionPoolSize(connectionPoolSize);
    //
    //     return Redisson.create(config);
    // }
}
