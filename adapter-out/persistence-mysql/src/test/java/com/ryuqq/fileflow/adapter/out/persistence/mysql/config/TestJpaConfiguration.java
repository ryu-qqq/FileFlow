package com.ryuqq.fileflow.adapter.out.persistence.mysql.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test Configuration for JPA Integration Tests
 *
 * <p><strong>역할</strong>: Integration Test를 위한 Spring Boot Configuration</p>
 *
 * <h3>설정 범위</h3>
 * <ul>
 *   <li>✅ JPA Entity Scan (Tenant, Organization)</li>
 *   <li>✅ JPA Repository Scan</li>
 *   <li>✅ QueryDSL Configuration (via @Import)</li>
 * </ul>
 *
 * <h3>사용 목적</h3>
 * <p>Library 모듈에서는 {@code @SpringBootApplication}이 없으므로,
 * {@code @DataJpaTest}가 요구하는 {@code @SpringBootConfiguration}을
 * 테스트 전용으로 제공합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@SpringBootApplication
@EntityScan(basePackages = {
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.entity",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.repository",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.repository"
})
public class TestJpaConfiguration {
    // Test configuration class - no implementation needed
}
