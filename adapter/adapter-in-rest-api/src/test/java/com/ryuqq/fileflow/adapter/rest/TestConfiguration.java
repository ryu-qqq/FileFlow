package com.ryuqq.fileflow.adapter.rest;

import com.ryuqq.fileflow.application.common.port.out.DomainEventPublisher;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test Configuration for REST Adapter Integration Tests
 *
 * Spring Boot Application Context for integration tests.
 * Scans all necessary packages to load application services,
 * adapters, and infrastructure components.
 *
 * @author sangwon-ryu
 */
@SpringBootApplication(scanBasePackages = {"com.ryuqq.fileflow"})
@EnableJpaRepositories(basePackages = {"com.ryuqq.fileflow.adapter.persistence.repository"})
@EntityScan(basePackages = {"com.ryuqq.fileflow.adapter.persistence.entity"})
public class TestConfiguration {
    // Full application context for integration tests
    // Scans entire com.ryuqq.fileflow package for components
    // Enables JPA repositories and entity scanning for database integration

    /**
     * No-op DomainEventPublisher for integration tests
     */
    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return new DomainEventPublisher() {
            @Override
            public void publish(com.ryuqq.fileflow.domain.common.event.DomainEvent event) {
                // No-op implementation for tests
            }

            @Override
            public void publishAll(Iterable<? extends com.ryuqq.fileflow.domain.common.event.DomainEvent> events) {
                // No-op implementation for tests
            }
        };
    }

    /**
     * S3 bucket name for integration tests
     */
    @Bean
    public String s3BucketName() {
        return "test-bucket";
    }
}
