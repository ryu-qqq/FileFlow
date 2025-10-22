package com.ryuqq.fileflow.adapter.out.persistence.mysql.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration Test Base Class
 *
 * <p><strong>역할</strong>: TestContainers 기반 MySQL 통합 테스트 기본 클래스</p>
 *
 * <h3>제공 기능</h3>
 * <ul>
 *   <li>✅ TestContainers MySQL 8.0 자동 시작</li>
 *   <li>✅ Spring Data JPA 자동 설정</li>
 *   <li>✅ QueryDSL Config 자동 주입</li>
 *   <li>✅ 테스트 간 데이터 격리 (각 테스트 전 DB 초기화)</li>
 * </ul>
 *
 * <h3>사용 방법</h3>
 * <pre>{@code
 * class TenantPersistenceAdapterTest extends IntegrationTestBase {
 *     // 테스트 작성
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {TestJpaConfiguration.class, QueryDslConfig.class})
public abstract class IntegrationTestBase {

    /**
     * MySQL 8.0 TestContainer
     *
     * <p>모든 테스트 클래스에서 공유되는 싱글톤 컨테이너입니다.</p>
     */
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("fileflow_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * TestContainers MySQL 설정을 Spring에 동적으로 주입
     *
     * @param registry Spring Dynamic Property Registry
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // JPA 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");
    }

    /**
     * 각 테스트 전 DB 초기화
     *
     * <p>테스트 간 데이터 격리를 보장합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE organizations");
        jdbcTemplate.execute("TRUNCATE TABLE tenants");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
