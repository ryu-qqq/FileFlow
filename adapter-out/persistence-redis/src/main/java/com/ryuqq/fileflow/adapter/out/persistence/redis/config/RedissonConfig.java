package com.ryuqq.fileflow.adapter.out.persistence.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    @ConfigurationProperties(prefix = "redisson")
    public RedissonProperties redissonProperties() {
        return new RedissonProperties();
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedissonProperties properties) {
        Config config = new Config();
        config.setThreads(properties.getThreads());
        config.setNettyThreads(properties.getNettyThreads());

        RedissonProperties.SingleServerProperties server = properties.getSingleServerConfig();
        SingleServerConfig serverConfig =
                config.useSingleServer()
                        .setAddress(server.getAddress())
                        .setDatabase(server.getDatabase())
                        .setConnectionPoolSize(server.getConnectionPoolSize())
                        .setConnectionMinimumIdleSize(server.getConnectionMinimumIdleSize())
                        .setIdleConnectionTimeout(server.getIdleConnectionTimeout())
                        .setConnectTimeout(server.getConnectTimeout())
                        .setTimeout(server.getTimeout())
                        .setRetryAttempts(server.getRetryAttempts())
                        .setRetryInterval(server.getRetryInterval());

        if (server.getPassword() != null && !server.getPassword().isBlank()) {
            serverConfig.setPassword(server.getPassword());
        }

        return Redisson.create(config);
    }

    public static class RedissonProperties {

        private int threads = 4;
        private int nettyThreads = 4;
        private SingleServerProperties singleServerConfig = new SingleServerProperties();

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getNettyThreads() {
            return nettyThreads;
        }

        public void setNettyThreads(int nettyThreads) {
            this.nettyThreads = nettyThreads;
        }

        public SingleServerProperties getSingleServerConfig() {
            return singleServerConfig;
        }

        public void setSingleServerConfig(SingleServerProperties singleServerConfig) {
            this.singleServerConfig = singleServerConfig;
        }

        public static class SingleServerProperties {

            private String address = "redis://localhost:6379";
            private String password;
            private int database = 0;
            private int connectionPoolSize = 8;
            private int connectionMinimumIdleSize = 2;
            private int idleConnectionTimeout = 10000;
            private int connectTimeout = 3000;
            private int timeout = 3000;
            private int retryAttempts = 3;
            private int retryInterval = 1500;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public int getDatabase() {
                return database;
            }

            public void setDatabase(int database) {
                this.database = database;
            }

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
    }
}
