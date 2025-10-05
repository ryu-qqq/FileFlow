package com.ryuqq.fileflow.adapter.persistence;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 통합 테스트 설정
 *
 * Testcontainers를 사용하여 실제 PostgreSQL 컨테이너에서 테스트합니다.
 * - PostgreSQL 15 이미지 사용
 *
 * @author sangwon-ryu
 */
@TestConfiguration(proxyBeanMethods = false)
public class PersistenceTestConfiguration {

    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

        container.start();

        // 환경 변수 설정
        System.setProperty("spring.datasource.url", container.getJdbcUrl());
        System.setProperty("spring.datasource.username", container.getUsername());
        System.setProperty("spring.datasource.password", container.getPassword());

        return container;
    }
}
